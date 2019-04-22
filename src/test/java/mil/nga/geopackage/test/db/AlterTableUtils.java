package mil.nga.geopackage.test.db;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.db.SQLiteMaster;
import mil.nga.geopackage.db.SQLiteMasterType;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.test.features.user.FeatureUtils;

/**
 * Alter Table test utils
 * 
 * @author osbornb
 */
public class AlterTableUtils {

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
					indexManager
							.prioritizeQueryLocation(FeatureIndexType.GEOPACKAGE);
					indexGeoPackageCount = (int) indexManager.count();
				} else {
					indexGeoPackageCount = indexManager
							.index(FeatureIndexType.GEOPACKAGE);
				}
				TestCase.assertTrue(indexManager
						.isIndexed(FeatureIndexType.GEOPACKAGE));

				int indexRTreeCount;
				if (indexManager.isIndexed(FeatureIndexType.RTREE)) {
					indexManager
							.prioritizeQueryLocation(FeatureIndexType.RTREE);
					indexRTreeCount = (int) indexManager.count();
				} else {
					indexRTreeCount = indexManager
							.index(FeatureIndexType.RTREE);
				}
				TestCase.assertTrue(indexManager
						.isIndexed(FeatureIndexType.RTREE));

				FeatureTable featureTable = dao.getTable();
				String tableName = featureTable.getTableName();

				for (FeatureColumn column : featureTable.getColumns()) {
					indexColumn(db, tableName, column);
				}

				int rowCount = dao.count();
				int tableCount = SQLiteMaster.count(geoPackage.getDatabase(),
						SQLiteMasterType.TABLE, tableName);
				int indexCount = SQLiteMaster.count(geoPackage.getDatabase(),
						SQLiteMasterType.INDEX, tableName);
				int triggerCount = SQLiteMaster.count(geoPackage.getDatabase(),
						SQLiteMasterType.TRIGGER, tableName);
				int viewCount = SQLiteMaster.count(geoPackage.getDatabase(),
						SQLiteMasterType.VIEW, tableName);

				FeatureTable table = dao.getTable();
				int existingColumns = table.getColumns().size();
				FeatureColumn pk = table.getPkColumn();
				FeatureColumn geometry = table.getGeometryColumn();

				int newColumns = 0;
				String newColumnName = "new_column";

				dao.addColumn(FeatureColumn.createColumn(newColumnName
						+ ++newColumns, GeoPackageDataType.TEXT, false, ""));
				dao.addColumn(FeatureColumn.createColumn(newColumnName
						+ ++newColumns, GeoPackageDataType.REAL));
				dao.addColumn(FeatureColumn.createColumn(newColumnName
						+ ++newColumns, GeoPackageDataType.BOOLEAN));
				dao.addColumn(FeatureColumn.createColumn(newColumnName
						+ ++newColumns, GeoPackageDataType.BLOB));
				dao.addColumn(FeatureColumn.createColumn(newColumnName
						+ ++newColumns, GeoPackageDataType.INTEGER));
				dao.addColumn(FeatureColumn.createColumn(newColumnName
						+ ++newColumns, GeoPackageDataType.TEXT, (long) UUID
						.randomUUID().toString().length()));
				dao.addColumn(FeatureColumn.createColumn(newColumnName
						+ ++newColumns, GeoPackageDataType.BLOB, (long) UUID
						.randomUUID().toString().getBytes().length));
				dao.addColumn(FeatureColumn.createColumn(newColumnName
						+ ++newColumns, GeoPackageDataType.DATE));
				dao.addColumn(FeatureColumn.createColumn(newColumnName
						+ ++newColumns, GeoPackageDataType.DATETIME));

				TestCase.assertEquals(existingColumns + newColumns, table
						.getColumns().size());
				TestCase.assertEquals(rowCount, dao.count());
				testTableCounts(db, tableName, tableCount, indexCount,
						triggerCount, viewCount);

				for (int index = existingColumns; index < table.getColumns()
						.size(); index++) {

					indexColumn(db, tableName, table.getColumn(index));

					String name = newColumnName + (index - existingColumns + 1);
					TestCase.assertEquals(name, table.getColumnName(index));
					TestCase.assertEquals(index, table.getColumnIndex(name));
					TestCase.assertEquals(name, table.getColumn(index)
							.getName());
					TestCase.assertEquals(index, table.getColumn(index)
							.getIndex());
					TestCase.assertEquals(name, table.getColumnNames()[index]);
					TestCase.assertEquals(name, table.getColumns().get(index)
							.getName());
					try {
						table.getColumn(index).setIndex(index - 1);
						TestCase.fail("Changed index on a created table column");
					} catch (Exception e) {
					}
					table.getColumn(index).setIndex(index);
				}

				testTableCounts(db, tableName, tableCount, indexCount
						+ newColumns, triggerCount, viewCount);

				TestCase.assertEquals(geometryColumns.getTableName(),
						table.getTableName());
				TestCase.assertEquals(pk, table.getPkColumn());
				TestCase.assertEquals(geometry, table.getGeometryColumn());

				testIndex(indexManager, indexGeoPackageCount, indexRTreeCount);

				FeatureUtils.testUpdate(dao);

				String newerColumnName = "newer_column";
				for (int newColumn = 2; newColumn <= newColumns; newColumn++) {
					dao.renameColumn(newColumnName + newColumn, newerColumnName
							+ newColumn);
				}
				for (int index = existingColumns + 1; index < table
						.getColumns().size(); index++) {
					String name = newerColumnName
							+ (index - existingColumns + 1);
					TestCase.assertEquals(name, table.getColumnName(index));
					TestCase.assertEquals(index, table.getColumnIndex(name));
					TestCase.assertEquals(name, table.getColumn(index)
							.getName());
					TestCase.assertEquals(index, table.getColumn(index)
							.getIndex());
					TestCase.assertEquals(name, table.getColumnNames()[index]);
					TestCase.assertEquals(name, table.getColumns().get(index)
							.getName());
				}

				TestCase.assertEquals(existingColumns + newColumns, table
						.getColumns().size());
				TestCase.assertEquals(rowCount, dao.count());
				testTableCounts(db, tableName, tableCount, indexCount
						+ newColumns, triggerCount, viewCount);
				TestCase.assertEquals(geometryColumns.getTableName(),
						table.getTableName());
				TestCase.assertEquals(pk, table.getPkColumn());
				TestCase.assertEquals(geometry, table.getGeometryColumn());

				testIndex(indexManager, indexGeoPackageCount, indexRTreeCount);

				FeatureUtils.testUpdate(dao);

				dao.dropColumn(newColumnName + 1);
				testTableCounts(db, tableName, tableCount, indexCount
						+ newColumns - 1, triggerCount, viewCount);
				for (int newColumn = 2; newColumn <= newColumns; newColumn++) {
					dao.dropColumn(newerColumnName + newColumn);
				}

				TestCase.assertEquals(existingColumns, table.getColumns()
						.size());
				TestCase.assertEquals(rowCount, dao.count());
				// Renamed columns double quote wrap columns and allow indexes
				// to be created on deleted columns, which is why there are more
				// than indexCount indexes
				testTableCounts(db, tableName, tableCount, indexCount
						+ newColumns - 1, triggerCount, viewCount);

				for (int index = 0; index < existingColumns; index++) {
					TestCase.assertEquals(index, table.getColumn(index)
							.getIndex());
				}

				TestCase.assertEquals(geometryColumns.getTableName(),
						table.getTableName());
				TestCase.assertEquals(pk, table.getPkColumn());
				TestCase.assertEquals(geometry, table.getGeometryColumn());

				testIndex(indexManager, indexGeoPackageCount, indexRTreeCount);

				FeatureUtils.testUpdate(dao);
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
			index.append(CoreSQLUtils.quoteWrap("ids_" + tableName + "_"
					+ column.getName()));
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
		TestCase.assertEquals(indexCount,
				SQLiteMaster.count(db, SQLiteMasterType.INDEX, tableName));
		TestCase.assertEquals(triggerCount,
				SQLiteMaster.count(db, SQLiteMasterType.TRIGGER, tableName));
		TestCase.assertEquals(viewCount,
				SQLiteMaster.count(db, SQLiteMasterType.VIEW, tableName));
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

		TestCase.assertTrue(indexManager.isIndexed(FeatureIndexType.GEOPACKAGE));
		indexManager.prioritizeQueryLocation(FeatureIndexType.GEOPACKAGE);
		TestCase.assertEquals(geoPackageCount, indexManager.count());

		TestCase.assertTrue(indexManager.isIndexed(FeatureIndexType.RTREE));
		indexManager.prioritizeQueryLocation(FeatureIndexType.RTREE);
		TestCase.assertEquals(rTreeCount, indexManager.count());

	}

}
