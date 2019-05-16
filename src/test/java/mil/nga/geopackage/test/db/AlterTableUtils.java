package mil.nga.geopackage.test.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesTable;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageCoreConnection;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.db.master.SQLiteMaster;
import mil.nga.geopackage.db.master.SQLiteMasterColumn;
import mil.nga.geopackage.db.master.SQLiteMasterQuery;
import mil.nga.geopackage.db.master.SQLiteMasterType;
import mil.nga.geopackage.extension.contents.ContentsId;
import mil.nga.geopackage.extension.contents.ContentsIdExtension;
import mil.nga.geopackage.extension.coverage.CoverageData;
import mil.nga.geopackage.extension.coverage.GriddedCoverage;
import mil.nga.geopackage.extension.coverage.GriddedTile;
import mil.nga.geopackage.extension.link.FeatureTileTableLinker;
import mil.nga.geopackage.extension.scale.TileScaling;
import mil.nga.geopackage.extension.scale.TileTableScaling;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.metadata.reference.MetadataReference;
import mil.nga.geopackage.metadata.reference.MetadataReferenceDao;
import mil.nga.geopackage.schema.columns.DataColumns;
import mil.nga.geopackage.schema.columns.DataColumnsDao;
import mil.nga.geopackage.test.features.user.FeatureUtils;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileTable;

/**
 * Alter Table test utils
 * 
 * @author osbornb
 */
public class AlterTableUtils {

	/**
	 * Test column table alterations
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @throws SQLException
	 *             upon error
	 */
	public static void testColumns(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();

		if (geometryColumnsDao.isTableExists()) {
			List<GeometryColumns> results = geometryColumnsDao.queryForAll();

			for (GeometryColumns geometryColumns : results) {

				GeoPackageConnection db = geoPackage.getConnection();
				FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);

				FeatureIndexManager indexManager = new FeatureIndexManager(
						geoPackage, dao);
				int indexGeoPackageCount;
				if (indexManager.isIndexed(FeatureIndexType.GEOPACKAGE)) {
					indexManager.prioritizeQueryLocation(
							FeatureIndexType.GEOPACKAGE);
					indexGeoPackageCount = (int) indexManager.count();
				} else {
					indexGeoPackageCount = indexManager
							.index(FeatureIndexType.GEOPACKAGE);
				}
				TestCase.assertTrue(
						indexManager.isIndexed(FeatureIndexType.GEOPACKAGE));

				int indexRTreeCount;
				if (indexManager.isIndexed(FeatureIndexType.RTREE)) {
					indexManager
							.prioritizeQueryLocation(FeatureIndexType.RTREE);
					indexRTreeCount = (int) indexManager.count();
				} else {
					indexRTreeCount = indexManager
							.index(FeatureIndexType.RTREE);
				}
				TestCase.assertTrue(
						indexManager.isIndexed(FeatureIndexType.RTREE));

				FeatureTable featureTable = dao.getTable();
				String tableName = featureTable.getTableName();

				for (FeatureColumn column : featureTable.getColumns()) {
					indexColumn(db, tableName, column);
				}

				createView(db, featureTable, "v_", true);
				createView(db, featureTable, "v2_", false);

				int rowCount = dao.count();
				int tableCount = SQLiteMaster.count(geoPackage.getDatabase(),
						SQLiteMasterType.TABLE, tableName);
				int indexCount = indexCount(geoPackage.getDatabase(),
						tableName);
				int triggerCount = SQLiteMaster.count(geoPackage.getDatabase(),
						SQLiteMasterType.TRIGGER, tableName);
				int viewCount = SQLiteMaster
						.countViewsOnTable(geoPackage.getDatabase(), tableName);

				TestCase.assertEquals(1, tableCount);
				TestCase.assertTrue(
						indexCount >= featureTable.columnCount() - 2);
				TestCase.assertTrue(triggerCount >= 6);
				TestCase.assertTrue(viewCount >= 2);

				FeatureTable table = dao.getTable();
				int existingColumns = table.getColumns().size();
				FeatureColumn pk = table.getPkColumn();
				FeatureColumn geometry = table.getGeometryColumn();

				int newColumns = 0;
				String newColumnName = "new_column";

				dao.addColumn(
						FeatureColumn.createColumn(newColumnName + ++newColumns,
								GeoPackageDataType.TEXT, false, ""));
				dao.addColumn(FeatureColumn.createColumn(
						newColumnName + ++newColumns, GeoPackageDataType.REAL));
				dao.addColumn(
						FeatureColumn.createColumn(newColumnName + ++newColumns,
								GeoPackageDataType.BOOLEAN));
				dao.addColumn(FeatureColumn.createColumn(
						newColumnName + ++newColumns, GeoPackageDataType.BLOB));
				dao.addColumn(
						FeatureColumn.createColumn(newColumnName + ++newColumns,
								GeoPackageDataType.INTEGER));
				dao.addColumn(FeatureColumn.createColumn(
						newColumnName + ++newColumns, GeoPackageDataType.TEXT,
						(long) UUID.randomUUID().toString().length()));
				dao.addColumn(FeatureColumn.createColumn(
						newColumnName + ++newColumns, GeoPackageDataType.BLOB,
						(long) UUID.randomUUID().toString().getBytes().length));
				dao.addColumn(FeatureColumn.createColumn(
						newColumnName + ++newColumns, GeoPackageDataType.DATE));
				dao.addColumn(
						FeatureColumn.createColumn(newColumnName + ++newColumns,
								GeoPackageDataType.DATETIME));

				TestCase.assertEquals(existingColumns + newColumns,
						table.getColumns().size());
				TestCase.assertEquals(rowCount, dao.count());
				testTableCounts(db, tableName, tableCount, indexCount,
						triggerCount, viewCount);

				for (int index = existingColumns; index < table.getColumns()
						.size(); index++) {

					indexColumn(db, tableName, table.getColumn(index));

					String name = newColumnName + (index - existingColumns + 1);
					TestCase.assertEquals(name, table.getColumnName(index));
					TestCase.assertEquals(index, table.getColumnIndex(name));
					TestCase.assertEquals(name,
							table.getColumn(index).getName());
					TestCase.assertEquals(index,
							table.getColumn(index).getIndex());
					TestCase.assertEquals(name, table.getColumnNames()[index]);
					TestCase.assertEquals(name,
							table.getColumns().get(index).getName());
					try {
						table.getColumn(index).setIndex(index - 1);
						TestCase.fail(
								"Changed index on a created table column");
					} catch (Exception e) {
					}
					table.getColumn(index).setIndex(index);
				}

				testTableCounts(db, tableName, tableCount,
						indexCount + newColumns, triggerCount, viewCount);

				TestCase.assertEquals(geometryColumns.getTableName(),
						table.getTableName());
				TestCase.assertEquals(pk, table.getPkColumn());
				TestCase.assertEquals(geometry, table.getGeometryColumn());

				testIndex(indexManager, indexGeoPackageCount, indexRTreeCount);

				FeatureUtils.testUpdate(dao);

				String newerColumnName = "newer_column";
				for (int newColumn = 2; newColumn <= newColumns; newColumn++) {
					dao.renameColumn(newColumnName + newColumn,
							newerColumnName + newColumn);
				}

				dao.alterColumn(FeatureColumn.createColumn(newerColumnName + 3,
						GeoPackageDataType.BOOLEAN, true, false));

				List<FeatureColumn> alterColumns = new ArrayList<>();
				alterColumns.add(FeatureColumn.createColumn(newerColumnName + 5,
						GeoPackageDataType.FLOAT, true, 1.5f));
				alterColumns.add(FeatureColumn.createColumn(newerColumnName + 8,
						GeoPackageDataType.TEXT, true, "date_to_text"));
				alterColumns.add(FeatureColumn.createColumn(newerColumnName + 9,
						GeoPackageDataType.DATETIME, true,
						"(strftime('%Y-%m-%dT%H:%M:%fZ','now'))"));
				dao.alterColumns(alterColumns);

				for (int index = existingColumns + 1; index < table.getColumns()
						.size(); index++) {
					String name = newerColumnName
							+ (index - existingColumns + 1);
					TestCase.assertEquals(name, table.getColumnName(index));
					TestCase.assertEquals(index, table.getColumnIndex(name));
					TestCase.assertEquals(name,
							table.getColumn(index).getName());
					TestCase.assertEquals(index,
							table.getColumn(index).getIndex());
					TestCase.assertEquals(name, table.getColumnNames()[index]);
					TestCase.assertEquals(name,
							table.getColumns().get(index).getName());
				}

				TestCase.assertEquals(existingColumns + newColumns,
						table.getColumns().size());
				TestCase.assertEquals(rowCount, dao.count());
				testTableCounts(db, tableName, tableCount,
						indexCount + newColumns, triggerCount, viewCount);
				TestCase.assertEquals(geometryColumns.getTableName(),
						table.getTableName());
				TestCase.assertEquals(pk, table.getPkColumn());
				TestCase.assertEquals(geometry, table.getGeometryColumn());

				testIndex(indexManager, indexGeoPackageCount, indexRTreeCount);

				FeatureUtils.testUpdate(dao);

				dao.dropColumn(newColumnName + 1);
				testTableCounts(db, tableName, tableCount,
						indexCount + newColumns - 1, triggerCount, viewCount);
				dao.dropColumnNames(Arrays.asList(newerColumnName + 2,
						newerColumnName + 3, newerColumnName + 4));
				for (int newColumn = 5; newColumn <= newColumns; newColumn++) {
					dao.dropColumn(newerColumnName + newColumn);
				}

				TestCase.assertEquals(existingColumns,
						table.getColumns().size());
				TestCase.assertEquals(rowCount, dao.count());
				testTableCounts(db, tableName, tableCount, indexCount,
						triggerCount, viewCount);

				for (int index = 0; index < existingColumns; index++) {
					TestCase.assertEquals(index,
							table.getColumn(index).getIndex());
				}

				TestCase.assertEquals(geometryColumns.getTableName(),
						table.getTableName());
				TestCase.assertEquals(pk, table.getPkColumn());
				TestCase.assertEquals(geometry, table.getGeometryColumn());

				testIndex(indexManager, indexGeoPackageCount, indexRTreeCount);

				FeatureUtils.testUpdate(dao);

				indexManager.close();
			}
		}
	}

	/**
	 * Index the column
	 * 
	 * @param db
	 *            connection
	 * @param tableName
	 *            table name
	 * @param column
	 *            feature column
	 */
	private static void indexColumn(GeoPackageConnection db, String tableName,
			FeatureColumn column) {
		if (!column.isPrimaryKey() && !column.isGeometry()) {
			StringBuilder index = new StringBuilder(
					"CREATE INDEX IF NOT EXISTS ");
			index.append(CoreSQLUtils
					.quoteWrap("idx_" + tableName + "_" + column.getName()));
			index.append(" ON ");
			index.append(CoreSQLUtils.quoteWrap(tableName));
			index.append(" ( ");
			String columnName = column.getName();
			if (columnName.contains(" ")) {
				columnName = CoreSQLUtils.quoteWrap(columnName);
			}
			index.append(columnName);
			index.append(" )");

			db.execSQL(index.toString());
		}
	}

	/**
	 * Create a table view
	 * 
	 * @param db
	 *            connection
	 * @param featureTable
	 *            feature column
	 * @param namePrefix
	 *            view name prefix
	 * @param quoteWrap
	 */
	private static void createView(GeoPackageConnection db,
			FeatureTable featureTable, String namePrefix, boolean quoteWrap) {

		StringBuilder view = new StringBuilder("CREATE VIEW ");
		String viewName = namePrefix + featureTable.getTableName();
		if (quoteWrap) {
			viewName = CoreSQLUtils.quoteWrap(viewName);
		}
		view.append(viewName);
		view.append(" AS SELECT ");
		for (int i = 0; i < featureTable.columnCount(); i++) {
			if (i > 0) {
				view.append(", ");
			}
			view.append(CoreSQLUtils.quoteWrap(featureTable.getColumnName(i)));
			view.append(" AS ");
			String columnName = "column" + (i + 1);
			if (quoteWrap) {
				columnName = CoreSQLUtils.quoteWrap(columnName);
			}
			view.append(columnName);
		}
		view.append(" FROM ");
		String tableName = featureTable.getTableName();
		if (quoteWrap) {
			tableName = CoreSQLUtils.quoteWrap(tableName);
		}
		view.append(tableName);

		db.execSQL(view.toString());
	}

	/**
	 * Get the expected index count
	 * 
	 * @param db
	 *            connection
	 * @param tableName
	 *            table name
	 * @return index count
	 */
	private static int indexCount(GeoPackageCoreConnection db,
			String tableName) {
		SQLiteMasterQuery indexQuery = SQLiteMasterQuery.createAnd();
		indexQuery.add(SQLiteMasterColumn.TBL_NAME, tableName);
		indexQuery.addIsNotNull(SQLiteMasterColumn.SQL);
		int count = SQLiteMaster.count(db, SQLiteMasterType.INDEX, indexQuery);
		return count;
	}

	/**
	 * Test the table schema counts
	 * 
	 * @param db
	 *            connection
	 * @param tableName
	 *            table name
	 * @param tableCount
	 *            table count
	 * @param indexCount
	 *            index count
	 * @param triggerCount
	 *            trigger count
	 * @param viewCount
	 *            view count
	 */
	private static void testTableCounts(GeoPackageConnection db,
			String tableName, int tableCount, int indexCount, int triggerCount,
			int viewCount) {
		TestCase.assertEquals(tableCount,
				SQLiteMaster.count(db, SQLiteMasterType.TABLE, tableName));
		TestCase.assertEquals(indexCount, indexCount(db, tableName));
		TestCase.assertEquals(triggerCount,
				SQLiteMaster.count(db, SQLiteMasterType.TRIGGER, tableName));
		TestCase.assertEquals(viewCount,
				SQLiteMaster.countViewsOnTable(db, tableName));
	}

	/**
	 * Test the feature indexes
	 * 
	 * @param indexManager
	 *            index manager
	 * @param geoPackageCount
	 *            GeoPackage index count
	 * @param rTreeCount
	 *            RTree index count
	 */
	private static void testIndex(FeatureIndexManager indexManager,
			int geoPackageCount, int rTreeCount) {

		TestCase.assertTrue(
				indexManager.isIndexed(FeatureIndexType.GEOPACKAGE));
		indexManager.prioritizeQueryLocation(FeatureIndexType.GEOPACKAGE);
		TestCase.assertEquals(geoPackageCount, indexManager.count());

		TestCase.assertTrue(indexManager.isIndexed(FeatureIndexType.RTREE));
		indexManager.prioritizeQueryLocation(FeatureIndexType.RTREE);
		TestCase.assertEquals(rTreeCount, indexManager.count());

	}

	/**
	 * Test copy feature table
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @throws SQLException
	 *             upon error
	 */
	public static void testCopyFeatureTable(GeoPackage geoPackage)
			throws SQLException {

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();

		if (geometryColumnsDao.isTableExists()) {
			List<GeometryColumns> results = geometryColumnsDao.queryForAll();

			for (GeometryColumns geometryColumns : results) {

				GeoPackageConnection db = geoPackage.getConnection();
				FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);
				FeatureTable table = dao.getTable();
				String tableName = table.getTableName();
				String newTableName = tableName + "_copy";

				int existingColumns = table.columnCount();
				FeatureColumn pk = table.getPkColumn();
				FeatureColumn geometry = table.getGeometryColumn();

				FeatureIndexManager indexManager = new FeatureIndexManager(
						geoPackage, dao);

				int indexGeoPackageCount = 0;
				if (indexManager.isIndexed(FeatureIndexType.GEOPACKAGE)) {
					indexManager.prioritizeQueryLocation(
							FeatureIndexType.GEOPACKAGE);
					indexGeoPackageCount = (int) indexManager.count();
				}

				int indexRTreeCount = 0;
				if (indexManager.isIndexed(FeatureIndexType.RTREE)) {
					indexManager
							.prioritizeQueryLocation(FeatureIndexType.RTREE);
					indexRTreeCount = (int) indexManager.count();
				}

				FeatureTileTableLinker linker = new FeatureTileTableLinker(
						geoPackage);
				List<String> tileTables = linker
						.getTileTablesForFeatureTable(tableName);

				ContentsIdExtension contentsIdExtension = new ContentsIdExtension(
						geoPackage);
				ContentsId contentsId = contentsIdExtension.get(tableName);

				List<MetadataReference> metadataReference = null;
				MetadataReferenceDao metadataReferenceDao = geoPackage
						.getMetadataReferenceDao();
				if (metadataReferenceDao.isTableExists()) {
					metadataReference = metadataReferenceDao
							.queryByTable(tableName);
				}

				List<DataColumns> dataColumns = null;
				DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();
				if (dataColumnsDao.isTableExists()) {
					dataColumns = dataColumnsDao.queryByTable(tableName);
				}

				int rowCount = dao.count();
				int tableCount = SQLiteMaster.count(geoPackage.getDatabase(),
						SQLiteMasterType.TABLE, tableName);
				int indexCount = indexCount(geoPackage.getDatabase(),
						tableName);
				int triggerCount = SQLiteMaster.count(geoPackage.getDatabase(),
						SQLiteMasterType.TRIGGER, tableName);
				int viewCount = SQLiteMaster
						.countViewsOnTable(geoPackage.getDatabase(), tableName);

				geoPackage.copyTable(tableName, newTableName);

				FeatureUtils.testUpdate(dao);

				FeatureDao copyDao = geoPackage.getFeatureDao(newTableName);
				GeometryColumns copyGeometryColumns = copyDao
						.getGeometryColumns();

				FeatureUtils.testUpdate(copyDao);

				FeatureTable copyTable = copyDao.getTable();

				TestCase.assertEquals(existingColumns, table.columnCount());
				TestCase.assertEquals(existingColumns, copyTable.columnCount());
				TestCase.assertEquals(rowCount, dao.count());
				TestCase.assertEquals(rowCount, copyDao.count());
				testTableCounts(db, tableName, tableCount, indexCount,
						triggerCount, viewCount);
				testTableCounts(db, newTableName, tableCount, indexCount,
						triggerCount, viewCount);

				TestCase.assertEquals(geometryColumns.getTableName(),
						table.getTableName());
				TestCase.assertEquals(newTableName,
						copyGeometryColumns.getTableName());
				TestCase.assertEquals(newTableName, copyTable.getTableName());
				TestCase.assertEquals(pk, table.getPkColumn());
				TestCase.assertEquals(pk.getName(),
						copyTable.getPkColumn().getName());
				TestCase.assertEquals(pk.getIndex(),
						copyTable.getPkColumn().getIndex());
				TestCase.assertEquals(geometry, table.getGeometryColumn());
				TestCase.assertEquals(geometry.getName(),
						copyTable.getGeometryColumn().getName());
				TestCase.assertEquals(geometry.getIndex(),
						copyTable.getGeometryColumn().getIndex());

				FeatureIndexManager copyIndexManager = new FeatureIndexManager(
						geoPackage, copyDao);

				if (indexManager.isIndexed(FeatureIndexType.GEOPACKAGE)) {
					indexManager.prioritizeQueryLocation(
							FeatureIndexType.GEOPACKAGE);
					TestCase.assertEquals(indexGeoPackageCount,
							indexManager.count());
					TestCase.assertTrue(copyIndexManager
							.isIndexed(FeatureIndexType.GEOPACKAGE));
					copyIndexManager.prioritizeQueryLocation(
							FeatureIndexType.GEOPACKAGE);
					TestCase.assertEquals(indexGeoPackageCount,
							copyIndexManager.count());
				} else {
					TestCase.assertFalse(copyIndexManager
							.isIndexed(FeatureIndexType.GEOPACKAGE));
				}

				if (indexManager.isIndexed(FeatureIndexType.RTREE)) {
					indexManager
							.prioritizeQueryLocation(FeatureIndexType.RTREE);
					TestCase.assertEquals(indexRTreeCount,
							indexManager.count());
					TestCase.assertTrue(
							copyIndexManager.isIndexed(FeatureIndexType.RTREE));
					copyIndexManager
							.prioritizeQueryLocation(FeatureIndexType.RTREE);
					TestCase.assertEquals(indexRTreeCount,
							copyIndexManager.count());
				} else {
					TestCase.assertFalse(
							copyIndexManager.isIndexed(FeatureIndexType.RTREE));
				}

				List<String> copyTileTables = linker
						.getTileTablesForFeatureTable(newTableName);
				TestCase.assertEquals(tileTables.size(), copyTileTables.size());
				for (String tileTable : tileTables) {
					TestCase.assertTrue(copyTileTables.contains(tileTable));
				}

				ContentsId copyContentsId = contentsIdExtension
						.get(newTableName);
				if (contentsId != null) {
					TestCase.assertNotNull(copyContentsId);
					TestCase.assertEquals(newTableName,
							copyContentsId.getTableName());
					TestCase.assertTrue(copyContentsId.getId() >= 0);
					TestCase.assertTrue(
							copyContentsId.getId() > contentsId.getId());
				} else {
					TestCase.assertNull(copyContentsId);
				}

				if (metadataReference != null) {
					List<MetadataReference> copyMetadataReference = metadataReferenceDao
							.queryByTable(newTableName);
					TestCase.assertEquals(metadataReference.size(),
							copyMetadataReference.size());
					for (int i = 0; i < metadataReference.size(); i++) {
						TestCase.assertEquals(tableName,
								metadataReference.get(i).getTableName());
						TestCase.assertEquals(newTableName,
								copyMetadataReference.get(i).getTableName());
					}
				}

				if (dataColumns != null) {
					List<DataColumns> copyDataColumns = dataColumnsDao
							.queryByTable(newTableName);
					TestCase.assertEquals(dataColumns.size(),
							copyDataColumns.size());
					for (int i = 0; i < dataColumns.size(); i++) {
						TestCase.assertEquals(tableName,
								dataColumns.get(i).getTableName());
						TestCase.assertEquals(newTableName,
								copyDataColumns.get(i).getTableName());
					}
				}

				indexManager.close();
				copyIndexManager.close();
			}
		}
	}

	/**
	 * Test copy tile table
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @throws SQLException
	 *             upon error
	 */
	public static void testCopyTileTable(GeoPackage geoPackage)
			throws SQLException {

		TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();

		if (tileMatrixSetDao.isTableExists()) {
			List<TileMatrixSet> results = tileMatrixSetDao.queryForAll();

			for (TileMatrixSet tileMatrixSet : results) {

				GeoPackageConnection db = geoPackage.getConnection();
				TileDao dao = geoPackage.getTileDao(tileMatrixSet);
				TileTable table = dao.getTable();
				String tableName = table.getTableName();
				String newTableName = tableName + "_copy";

				int existingColumns = table.columnCount();

				FeatureTileTableLinker linker = new FeatureTileTableLinker(
						geoPackage);
				List<String> featureTables = linker
						.getFeatureTablesForTileTable(tableName);

				ContentsIdExtension contentsIdExtension = new ContentsIdExtension(
						geoPackage);
				ContentsId contentsId = contentsIdExtension.get(tableName);

				List<MetadataReference> metadataReference = null;
				MetadataReferenceDao metadataReferenceDao = geoPackage
						.getMetadataReferenceDao();
				if (metadataReferenceDao.isTableExists()) {
					metadataReference = metadataReferenceDao
							.queryByTable(tableName);
				}

				List<DataColumns> dataColumns = null;
				DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();
				if (dataColumnsDao.isTableExists()) {
					dataColumns = dataColumnsDao.queryByTable(tableName);
				}

				TileScaling tileScaling = null;
				TileTableScaling tileTableScaling = new TileTableScaling(
						geoPackage, tileMatrixSet);
				if (tileTableScaling.has()) {
					tileScaling = tileTableScaling.get();
				}

				GriddedCoverage griddedCoverage = null;
				List<GriddedTile> griddedTiles = null;
				if (geoPackage.isTableType(ContentsDataType.GRIDDED_COVERAGE,
						tableName)) {
					CoverageData<?> coverageData = CoverageData
							.getCoverageData(geoPackage, dao);
					griddedCoverage = coverageData.queryGriddedCoverage();
					griddedTiles = coverageData.getGriddedTile();
				}

				int rowCount = dao.count();
				int tableCount = SQLiteMaster.count(geoPackage.getDatabase(),
						SQLiteMasterType.TABLE, tableName);
				int indexCount = indexCount(geoPackage.getDatabase(),
						tableName);
				int triggerCount = SQLiteMaster.count(geoPackage.getDatabase(),
						SQLiteMasterType.TRIGGER, tableName);
				int viewCount = SQLiteMaster
						.countViewsOnTable(geoPackage.getDatabase(), tableName);

				geoPackage.copyTable(tableName, newTableName);

				// TileUtils.testUpdate(dao);

				TileDao copyDao = geoPackage.getTileDao(newTableName);
				TileMatrixSet copyTileMatrixSet = copyDao.getTileMatrixSet();

				// TileUtils.testUpdate(copyDao);

				TileTable copyTable = copyDao.getTable();

				TestCase.assertEquals(existingColumns, table.columnCount());
				TestCase.assertEquals(existingColumns, copyTable.columnCount());
				TestCase.assertEquals(rowCount, dao.count());
				TestCase.assertEquals(rowCount, copyDao.count());
				testTableCounts(db, tableName, tableCount, indexCount,
						triggerCount, viewCount);
				testTableCounts(db, newTableName, tableCount, indexCount,
						triggerCount, viewCount);

				TestCase.assertEquals(tileMatrixSet.getTableName(),
						table.getTableName());
				TestCase.assertEquals(newTableName,
						copyTileMatrixSet.getTableName());
				TestCase.assertEquals(newTableName, copyTable.getTableName());

				List<String> copyFeatureTables = linker
						.getFeatureTablesForTileTable(newTableName);
				TestCase.assertEquals(featureTables.size(),
						copyFeatureTables.size());
				for (String featureTable : featureTables) {
					TestCase.assertTrue(
							copyFeatureTables.contains(featureTable));
				}

				ContentsId copyContentsId = contentsIdExtension
						.get(newTableName);
				if (contentsId != null) {
					TestCase.assertNotNull(copyContentsId);
					TestCase.assertEquals(newTableName,
							copyContentsId.getTableName());
					TestCase.assertTrue(copyContentsId.getId() >= 0);
					TestCase.assertTrue(
							copyContentsId.getId() > contentsId.getId());
				} else {
					TestCase.assertNull(copyContentsId);
				}

				if (metadataReference != null) {
					List<MetadataReference> copyMetadataReference = metadataReferenceDao
							.queryByTable(newTableName);
					TestCase.assertEquals(metadataReference.size(),
							copyMetadataReference.size());
					for (int i = 0; i < metadataReference.size(); i++) {
						TestCase.assertEquals(tableName,
								metadataReference.get(i).getTableName());
						TestCase.assertEquals(newTableName,
								copyMetadataReference.get(i).getTableName());
					}
				}

				if (dataColumns != null) {
					List<DataColumns> copyDataColumns = dataColumnsDao
							.queryByTable(newTableName);
					TestCase.assertEquals(dataColumns.size(),
							copyDataColumns.size());
					for (int i = 0; i < dataColumns.size(); i++) {
						TestCase.assertEquals(tableName,
								dataColumns.get(i).getTableName());
						TestCase.assertEquals(newTableName,
								copyDataColumns.get(i).getTableName());
					}
				}

				if (tileScaling != null) {
					TileTableScaling copyTileTableScaling = new TileTableScaling(
							geoPackage, copyTileMatrixSet);
					TestCase.assertTrue(copyTileTableScaling.has());
					TileScaling copyTileScaling = copyTileTableScaling.get();
					TestCase.assertEquals(newTableName,
							copyTileScaling.getTableName());
					TestCase.assertEquals(tileScaling.getScalingTypeString(),
							copyTileScaling.getScalingTypeString());
					TestCase.assertEquals(tileScaling.getZoomIn(),
							copyTileScaling.getZoomIn());
					TestCase.assertEquals(tileScaling.getZoomOut(),
							copyTileScaling.getZoomOut());
				}

				if (griddedCoverage != null) {
					CoverageData<?> copyCoverageData = CoverageData
							.getCoverageData(geoPackage, copyDao);
					GriddedCoverage copyGriddedCoverage = copyCoverageData
							.queryGriddedCoverage();
					List<GriddedTile> copyGriddedTiles = copyCoverageData
							.getGriddedTile();
					TestCase.assertEquals(tableName,
							griddedCoverage.getTileMatrixSetName());
					TestCase.assertEquals(newTableName,
							copyGriddedCoverage.getTileMatrixSetName());
					TestCase.assertEquals(griddedTiles.size(),
							copyGriddedTiles.size());
					for (int i = 0; i < griddedTiles.size(); i++) {
						TestCase.assertEquals(tableName,
								griddedTiles.get(i).getTableName());
						TestCase.assertEquals(newTableName,
								copyGriddedTiles.get(i).getTableName());
					}
				}

			}
		}
	}

	/**
	 * Test copy attributes table
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @throws SQLException
	 *             upon error
	 */
	public static void testCopyAttributesTable(GeoPackage geoPackage)
			throws SQLException {

		List<String> attributesTables = geoPackage.getAttributesTables();

		for (String attributesTable : attributesTables) {

			GeoPackageConnection db = geoPackage.getConnection();
			AttributesDao dao = geoPackage.getAttributesDao(attributesTable);
			AttributesTable table = dao.getTable();
			String tableName = table.getTableName();
			String newTableName = tableName + "_copy";

			int existingColumns = table.columnCount();

			ContentsIdExtension contentsIdExtension = new ContentsIdExtension(
					geoPackage);
			ContentsId contentsId = contentsIdExtension.get(tableName);

			List<MetadataReference> metadataReference = null;
			MetadataReferenceDao metadataReferenceDao = geoPackage
					.getMetadataReferenceDao();
			if (metadataReferenceDao.isTableExists()) {
				metadataReference = metadataReferenceDao
						.queryByTable(tableName);
			}

			List<DataColumns> dataColumns = null;
			DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();
			if (dataColumnsDao.isTableExists()) {
				dataColumns = dataColumnsDao.queryByTable(tableName);
			}

			int rowCount = dao.count();
			int tableCount = SQLiteMaster.count(geoPackage.getDatabase(),
					SQLiteMasterType.TABLE, tableName);
			int indexCount = indexCount(geoPackage.getDatabase(), tableName);
			int triggerCount = SQLiteMaster.count(geoPackage.getDatabase(),
					SQLiteMasterType.TRIGGER, tableName);
			int viewCount = SQLiteMaster
					.countViewsOnTable(geoPackage.getDatabase(), tableName);

			geoPackage.copyTable(tableName, newTableName);

			// AttributesUtils.testUpdate(dao);

			AttributesDao copyDao = geoPackage.getAttributesDao(newTableName);

			// AttributesUtils.testUpdate(copyDao);

			AttributesTable copyTable = copyDao.getTable();

			TestCase.assertEquals(existingColumns, table.columnCount());
			TestCase.assertEquals(existingColumns, copyTable.columnCount());
			TestCase.assertEquals(rowCount, dao.count());
			TestCase.assertEquals(rowCount, copyDao.count());
			testTableCounts(db, tableName, tableCount, indexCount, triggerCount,
					viewCount);
			testTableCounts(db, newTableName, tableCount, indexCount,
					triggerCount, viewCount);

			TestCase.assertEquals(newTableName, copyTable.getTableName());

			ContentsId copyContentsId = contentsIdExtension.get(newTableName);
			if (contentsId != null) {
				TestCase.assertNotNull(copyContentsId);
				TestCase.assertEquals(newTableName,
						copyContentsId.getTableName());
				TestCase.assertTrue(copyContentsId.getId() >= 0);
				TestCase.assertTrue(
						copyContentsId.getId() > contentsId.getId());
			} else {
				TestCase.assertNull(copyContentsId);
			}

			if (metadataReference != null) {
				List<MetadataReference> copyMetadataReference = metadataReferenceDao
						.queryByTable(newTableName);
				TestCase.assertEquals(metadataReference.size(),
						copyMetadataReference.size());
				for (int i = 0; i < metadataReference.size(); i++) {
					TestCase.assertEquals(tableName,
							metadataReference.get(i).getTableName());
					TestCase.assertEquals(newTableName,
							copyMetadataReference.get(i).getTableName());
				}
			}

			if (dataColumns != null) {
				List<DataColumns> copyDataColumns = dataColumnsDao
						.queryByTable(newTableName);
				TestCase.assertEquals(dataColumns.size(),
						copyDataColumns.size());
				for (int i = 0; i < dataColumns.size(); i++) {
					TestCase.assertEquals(tableName,
							dataColumns.get(i).getTableName());
					TestCase.assertEquals(newTableName,
							copyDataColumns.get(i).getTableName());
				}
			}

		}
	}

}
