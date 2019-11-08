package mil.nga.geopackage.manager;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesTable;
import mil.nga.geopackage.attributes.AttributesTableReader;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageTableCreator;
import mil.nga.geopackage.extension.RTreeIndexExtension;
import mil.nga.geopackage.factory.GeoPackageCoreImpl;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.features.user.FeatureTableReader;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrix.TileMatrixKey;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileTable;
import mil.nga.geopackage.tiles.user.TileTableReader;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomTable;
import mil.nga.geopackage.user.custom.UserCustomTableReader;
import mil.nga.sf.proj.Projection;

/**
 * GeoPackage implementation
 * 
 * @author osbornb
 */
class GeoPackageImpl extends GeoPackageCoreImpl implements GeoPackage {

	/**
	 * Database connection
	 */
	private final GeoPackageConnection database;

	/**
	 * Constructor
	 *
	 * @param name
	 *            GeoPackage name
	 * @param file
	 *            GeoPackage file
	 * @param database
	 *            connection
	 * @param tableCreator
	 *            table creator
	 */
	GeoPackageImpl(String name, File file, GeoPackageConnection database,
			GeoPackageTableCreator tableCreator) {
		super(name, file.getAbsolutePath(), database, tableCreator, true);
		this.database = database;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BoundingBox getFeatureBoundingBox(Projection projection,
			String table, boolean manual) {

		BoundingBox boundingBox = null;

		FeatureIndexManager indexManager = new FeatureIndexManager(this, table);
		try {
			if (manual || indexManager.isIndexed()) {
				boundingBox = indexManager.getBoundingBox(projection);
			}
		} finally {
			indexManager.close();
		}

		return boundingBox;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureDao getFeatureDao(GeometryColumns geometryColumns) {

		if (geometryColumns == null) {
			throw new GeoPackageException(
					"Non null " + GeometryColumns.class.getSimpleName()
							+ " is required to create "
							+ FeatureDao.class.getSimpleName());
		}

		// Read the existing table and create the dao
		FeatureTableReader tableReader = new FeatureTableReader(
				geometryColumns);
		final FeatureTable featureTable = tableReader.readTable(database);
		featureTable.setContents(geometryColumns.getContents());
		FeatureDao dao = new FeatureDao(getName(), database, geometryColumns,
				featureTable);

		// If the GeoPackage is writable and the feature table has a RTree Index
		// extension, create the SQL functions
		if (writable) {
			RTreeIndexExtension rtree = new RTreeIndexExtension(this);
			rtree.createFunctions(featureTable);
		}

		return dao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureDao getFeatureDao(Contents contents) {

		if (contents == null) {
			throw new GeoPackageException("Non null "
					+ Contents.class.getSimpleName() + " is required to create "
					+ FeatureDao.class.getSimpleName());
		}

		GeometryColumns geometryColumns = null;
		try {
			geometryColumns = getGeometryColumnsDao()
					.queryForTableName(contents.getTableName());
		} catch (SQLException e) {
			throw new GeoPackageException("No "
					+ GeometryColumns.class.getSimpleName()
					+ " could be retrieved for "
					+ Contents.class.getSimpleName() + " " + contents.getId());
		}

		if (geometryColumns == null) {
			throw new GeoPackageException("No "
					+ GeometryColumns.class.getSimpleName() + " exists for "
					+ Contents.class.getSimpleName() + " " + contents.getId());
		}

		return getFeatureDao(geometryColumns);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureDao getFeatureDao(String tableName) {
		GeometryColumnsDao dao = getGeometryColumnsDao();
		List<GeometryColumns> geometryColumnsList;
		try {
			geometryColumnsList = dao
					.queryForEq(GeometryColumns.COLUMN_TABLE_NAME, tableName);
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to retrieve "
					+ FeatureDao.class.getSimpleName() + " for table name: "
					+ tableName + ". Exception retrieving "
					+ GeometryColumns.class.getSimpleName() + ".", e);
		}
		if (geometryColumnsList.isEmpty()) {
			throw new GeoPackageException(
					"No Feature Table exists for table name: " + tableName);
		} else if (geometryColumnsList.size() > 1) {
			// This shouldn't happen with the table name unique constraint on
			// geometry columns
			throw new GeoPackageException("Unexpected state. More than one "
					+ GeometryColumns.class.getSimpleName()
					+ " matched for table name: " + tableName + ", count: "
					+ geometryColumnsList.size());
		}
		return getFeatureDao(geometryColumnsList.get(0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileDao getTileDao(TileMatrixSet tileMatrixSet) {

		if (tileMatrixSet == null) {
			throw new GeoPackageException(
					"Non null " + TileMatrixSet.class.getSimpleName()
							+ " is required to create "
							+ TileDao.class.getSimpleName());
		}

		// Get the Tile Matrix collection, order by zoom level ascending & pixel
		// size descending per requirement 51
		List<TileMatrix> tileMatrices;
		try {
			TileMatrixDao tileMatrixDao = getTileMatrixDao();
			QueryBuilder<TileMatrix, TileMatrixKey> qb = tileMatrixDao
					.queryBuilder();
			qb.where().eq(TileMatrix.COLUMN_TABLE_NAME,
					tileMatrixSet.getTableName());
			qb.orderBy(TileMatrix.COLUMN_ZOOM_LEVEL, true);
			qb.orderBy(TileMatrix.COLUMN_PIXEL_X_SIZE, false);
			qb.orderBy(TileMatrix.COLUMN_PIXEL_Y_SIZE, false);
			PreparedQuery<TileMatrix> query = qb.prepare();
			tileMatrices = tileMatrixDao.query(query);
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to retrieve " + TileDao.class.getSimpleName()
							+ " for table name: " + tileMatrixSet.getTableName()
							+ ". Exception retrieving "
							+ TileMatrix.class.getSimpleName() + " collection.",
					e);
		}

		// Read the existing table and create the dao
		TileTableReader tableReader = new TileTableReader(
				tileMatrixSet.getTableName());
		final TileTable tileTable = tableReader.readTable(database);
		tileTable.setContents(tileMatrixSet.getContents());
		TileDao dao = new TileDao(getName(), database, tileMatrixSet,
				tileMatrices, tileTable);

		return dao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileDao getTileDao(Contents contents) {

		if (contents == null) {
			throw new GeoPackageException("Non null "
					+ Contents.class.getSimpleName() + " is required to create "
					+ TileDao.class.getSimpleName());
		}

		TileMatrixSet tileMatrixSet = null;
		try {
			tileMatrixSet = getTileMatrixSetDao()
					.queryForId(contents.getTableName());
		} catch (SQLException e) {
			throw new GeoPackageException("No "
					+ TileMatrixSet.class.getSimpleName()
					+ " could be retrieved for "
					+ Contents.class.getSimpleName() + " " + contents.getId());
		}

		if (tileMatrixSet == null) {
			throw new GeoPackageException("No "
					+ TileMatrixSet.class.getSimpleName() + " exists for "
					+ Contents.class.getSimpleName() + " " + contents.getId());
		}

		return getTileDao(tileMatrixSet);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileDao getTileDao(String tableName) {

		TileMatrixSetDao dao = getTileMatrixSetDao();
		List<TileMatrixSet> tileMatrixSetList;
		try {
			tileMatrixSetList = dao.queryForEq(TileMatrixSet.COLUMN_TABLE_NAME,
					tableName);
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to retrieve "
					+ TileDao.class.getSimpleName() + " for table name: "
					+ tableName + ". Exception retrieving "
					+ TileMatrixSet.class.getSimpleName() + ".", e);
		}
		if (tileMatrixSetList.isEmpty()) {
			throw new GeoPackageException(
					"No Tile Table exists for table name: " + tableName
							+ ", Tile Tables: " + getTileTables());
		} else if (tileMatrixSetList.size() > 1) {
			// This shouldn't happen with the table name primary key on tile
			// matrix set table
			throw new GeoPackageException("Unexpected state. More than one "
					+ TileMatrixSet.class.getSimpleName()
					+ " matched for table name: " + tableName + ", count: "
					+ tileMatrixSetList.size());
		}
		return getTileDao(tileMatrixSetList.get(0));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttributesDao getAttributesDao(Contents contents) {

		if (contents == null) {
			throw new GeoPackageException("Non null "
					+ Contents.class.getSimpleName() + " is required to create "
					+ AttributesDao.class.getSimpleName());
		}
		if (contents.getDataType() != ContentsDataType.ATTRIBUTES) {
			throw new GeoPackageException(Contents.class.getSimpleName()
					+ " is required to be of type "
					+ ContentsDataType.ATTRIBUTES + ". Actual: "
					+ contents.getDataTypeString());
		}

		// Read the existing table and create the dao
		AttributesTableReader tableReader = new AttributesTableReader(
				contents.getTableName());
		final AttributesTable attributesTable = tableReader.readTable(database);
		attributesTable.setContents(contents);
		AttributesDao dao = new AttributesDao(getName(), database,
				attributesTable);

		return dao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttributesDao getAttributesDao(String tableName) {

		ContentsDao dao = getContentsDao();
		Contents contents = null;
		try {
			contents = dao.queryForId(tableName);
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to retrieve " + Contents.class.getSimpleName()
							+ " for table name: " + tableName,
					e);
		}
		if (contents == null) {
			throw new GeoPackageException(
					"No Contents Table exists for table name: " + tableName);
		}
		return getAttributesDao(contents);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserCustomDao getUserCustomDao(String tableName) {
		UserCustomTable table = UserCustomTableReader.readTable(database,
				tableName);
		return getUserCustomDao(table);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserCustomDao getUserCustomDao(UserCustomTable table) {
		return new UserCustomDao(getName(), database, table);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execSQL(String sql) {
		database.execSQL(sql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beginTransaction() {
		database.beginTransaction();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endTransaction(boolean successful) {
		database.endTransaction(successful);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit() {
		database.commit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean inTransaction() {
		return database.inTransaction();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultSet query(String sql, String[] args) {
		return database.query(sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeoPackageConnection getConnection() {
		return database;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultSet foreignKeyCheck() {
		return foreignKeyCheck(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultSet foreignKeyCheck(String tableName) {
		ResultSet resultSet = query(CoreSQLUtils.foreignKeyCheckSQL(tableName),
				null);
		try {
			if (!resultSet.next()) {
				resultSet.close();
				resultSet = null;
			}
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Foreign key check failed on database: " + getName(), e);
		}
		return resultSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultSet integrityCheck() {
		return integrityCheck(query(CoreSQLUtils.integrityCheckSQL(), null));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResultSet quickCheck() {
		return integrityCheck(query(CoreSQLUtils.quickCheckSQL(), null));
	}

	/**
	 * Check the result set returned from the integrity check to see if things
	 * are "ok"
	 *
	 * @param resultSet
	 * @return null if ok, else the open cursor
	 */
	private ResultSet integrityCheck(ResultSet resultSet) {
		try {
			if (resultSet.next()) {
				String value = resultSet.getString(1);
				if (value.equals("ok")) {
					resultSet.close();
					resultSet = null;
				}
			}
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Integrity check failed on database: " + getName(), e);
		}
		return resultSet;
	}

}
