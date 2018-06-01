package mil.nga.geopackage.test.extension.related;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;
import mil.nga.geopackage.db.DateConverter;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.related.ExtendedRelation;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.extension.related.RelationType;
import mil.nga.geopackage.extension.related.UserMappingDao;
import mil.nga.geopackage.extension.related.UserMappingRow;
import mil.nga.geopackage.extension.related.UserMappingTable;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.test.LoadGeoPackageTestCase;
import mil.nga.geopackage.test.TestConstants;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.user.UserCoreResultUtils;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomResultSet;

import org.junit.Test;

/**
 * Test Related Tables Extension writing
 * 
 * @author jyutzler
 */
public class RelatedTablesWriteTest extends LoadGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public RelatedTablesWriteTest() {
		super(TestConstants.IMPORT_DB_FILE_NAME);
	}

	/**
	 * Test get tile
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void testGetRelationships() throws SQLException, IOException {

		RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);

		// 1. Has extension
		TestCase.assertFalse(rte.has());

		// 2. Add extension
		rte.getOrCreate();
		TestCase.assertTrue(rte.has());

		// 4. Get relationships
		Collection<ExtendedRelation> extendedRelations = rte.getRelationships();
		TestCase.assertTrue(extendedRelations.isEmpty());

		// 5. Add relationship between "geometry2d" and "geometry3d"
		final String baseTableName = "geometry2d";
		final String relatedTableName = "geometry3d";
		final String mappingTableName = "g2d_3d";
		final RelationType relationType = RelationType.FEATURES;

		List<UserCustomColumn> additionalColumns = createAdditionalUserMappingColumns();

		UserMappingTable userMappingTable = UserMappingTable.create(
				mappingTableName, additionalColumns);
		TestCase.assertEquals(UserMappingTable.numRequiredColumns()
				+ additionalColumns.size(), userMappingTable.getColumns()
				.size());
		UserCustomColumn baseIdColumn = userMappingTable.getBaseIdColumn();
		TestCase.assertNotNull(baseIdColumn);
		TestCase.assertTrue(baseIdColumn
				.isNamed(UserMappingTable.COLUMN_BASE_ID));
		TestCase.assertTrue(baseIdColumn.isNotNull());
		TestCase.assertFalse(baseIdColumn.isPrimaryKey());
		UserCustomColumn relatedIdColumn = userMappingTable
				.getRelatedIdColumn();
		TestCase.assertNotNull(relatedIdColumn);
		TestCase.assertTrue(relatedIdColumn
				.isNamed(UserMappingTable.COLUMN_RELATED_ID));
		TestCase.assertTrue(relatedIdColumn.isNotNull());
		TestCase.assertFalse(relatedIdColumn.isPrimaryKey());

		TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
		ExtendedRelation extendedRelation = rte.addRelationship(baseTableName,
				relatedTableName, userMappingTable, relationType);
		TestCase.assertTrue(rte.has(userMappingTable.getTableName()));
		TestCase.assertNotNull(extendedRelation);
		extendedRelations = rte.getRelationships();
		TestCase.assertEquals(1, extendedRelations.size());
		TestCase.assertTrue(geoPackage.getDatabase().tableExists(
				mappingTableName));

		// 7. Add mappings
		FeatureDao baseDao = geoPackage.getFeatureDao(baseTableName);
		FeatureDao relatedDao = geoPackage.getFeatureDao(relatedTableName);
		FeatureResultSet baseFrs = baseDao.queryForAll();
		int baseCount = baseFrs.getCount();
		long[] baseIds = new long[baseCount];
		int inx = 0;
		while (baseFrs.moveToNext()) {
			baseIds[inx++] = baseFrs.getRow().getId();
		}
		baseFrs.close();
		FeatureResultSet relatedFrs = relatedDao.queryForAll();
		int relatedCount = relatedFrs.getCount();
		long[] relatedIds = new long[relatedCount];
		inx = 0;
		while (relatedFrs.moveToNext()) {
			relatedIds[inx++] = relatedFrs.getRow().getId();
		}
		relatedFrs.close();
		UserMappingDao dao = rte.getUserMappingDao(mappingTableName);
		UserMappingRow userMappingRow = null;
		for (inx = 0; inx < 10; inx++) {
			userMappingRow = dao.newRow();
			userMappingRow.setBaseId(((int) Math.floor(Math.random()
					* baseCount)));
			userMappingRow.setRelatedId(((int) Math.floor(Math.random()
					* relatedCount)));
			populateUserMappingRow(userMappingTable, userMappingRow);
			TestCase.assertTrue(dao.create(userMappingRow) > 0);
		}

		TestCase.assertEquals(10, dao.count());

		userMappingTable = dao.getTable();
		String[] columns = userMappingTable.getColumnNames();
		UserCustomResultSet resultSet = dao.queryForAll();
		int count = resultSet.getCount();
		TestCase.assertEquals(10, count);
		int manualCount = 0;
		while (resultSet.moveToNext()) {

			UserMappingRow resultRow = dao.getRow(resultSet);
			validateUserMappingRow(columns, resultRow);

			manualCount++;
		}
		TestCase.assertEquals(count, manualCount);
		resultSet.close();

		// 8. Remove mappings (note: it is plausible and allowed
		// to have duplicate entries)
		TestCase.assertTrue(dao.deleteByIds(userMappingRow) > 0);

		// 6. Remove relationship
		rte.removeRelationship(extendedRelation);
		TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
		extendedRelations = rte.getRelationships();
		TestCase.assertEquals(0, extendedRelations.size());
		TestCase.assertFalse(geoPackage.getDatabase().tableExists(
				mappingTableName));

		// 3. Remove extension
		rte.removeExtension();
		TestCase.assertFalse(rte.has());
	}

	/**
	 * Create additional user mapping columns
	 * 
	 * @return additional user mapping columns
	 */
	private static List<UserCustomColumn> createAdditionalUserMappingColumns() {

		List<UserCustomColumn> columns = new ArrayList<>();

		int columnIndex = UserMappingTable.numRequiredColumns();
		columns.add(UserCustomColumn.createColumn(columnIndex++, "test_text",
				GeoPackageDataType.TEXT, false, ""));
		columns.add(UserCustomColumn.createColumn(columnIndex++, "test_real",
				GeoPackageDataType.REAL, false, null));
		columns.add(UserCustomColumn.createColumn(columnIndex++,
				"test_boolean", GeoPackageDataType.BOOLEAN, false, null));
		columns.add(UserCustomColumn.createColumn(columnIndex++, "test_blob",
				GeoPackageDataType.BLOB, false, null));
		columns.add(UserCustomColumn.createColumn(columnIndex++,
				"test_integer", GeoPackageDataType.INTEGER, false, null));
		columns.add(UserCustomColumn.createColumn(columnIndex++,
				"test_text_limited", GeoPackageDataType.TEXT, 5L, false, null));
		columns.add(UserCustomColumn.createColumn(columnIndex++,
				"test_blob_limited", GeoPackageDataType.BLOB, 7L, false, null));
		columns.add(UserCustomColumn.createColumn(columnIndex++, "test_date",
				GeoPackageDataType.DATE, false, null));
		columns.add(UserCustomColumn.createColumn(columnIndex++,
				"test_datetime", GeoPackageDataType.DATETIME, false, null));

		return columns;
	}

	/**
	 * Populate the user mapping row additional column values
	 * 
	 * @param userMappingTable
	 *            user mapping table
	 * @param userMappingRow
	 *            user mapping row
	 */
	private static void populateUserMappingRow(
			UserMappingTable userMappingTable, UserMappingRow userMappingRow) {

		for (UserCustomColumn column : userMappingTable.getColumns()) {
			if (!column.isNamed(UserMappingTable.COLUMN_BASE_ID)
					&& !column.isNamed(UserMappingTable.COLUMN_RELATED_ID)) {

				// Leave nullable columns null 20% of the time
				if (!column.isNotNull()) {
					if (Math.random() < 0.2) {
						continue;
					}
				}

				Object value = null;

				switch (column.getDataType()) {

				case TEXT:
					String text = UUID.randomUUID().toString();
					if (column.getMax() != null
							&& text.length() > column.getMax()) {
						text = text.substring(0, column.getMax().intValue());
					}
					value = text;
					break;
				case REAL:
				case DOUBLE:
					value = Math.random() * 5000.0;
					break;
				case BOOLEAN:
					value = Math.random() < .5 ? false : true;
					break;
				case INTEGER:
				case INT:
					value = (int) (Math.random() * 500);
					break;
				case BLOB:
					byte[] blob = UUID.randomUUID().toString().getBytes();
					if (column.getMax() != null
							&& blob.length > column.getMax()) {
						byte[] blobLimited = new byte[column.getMax()
								.intValue()];
						ByteBuffer.wrap(blob, 0, column.getMax().intValue())
								.get(blobLimited);
						blob = blobLimited;
					}
					value = blob;
					break;
				case DATE:
				case DATETIME:
					DateConverter converter = DateConverter.converter(column
							.getDataType());
					Date date = new Date();
					if (Math.random() < .5) {
						value = date;
					} else {
						value = converter.stringValue(date);
					}
					break;
				default:
					throw new UnsupportedOperationException(
							"Not implemented for data type: "
									+ column.getDataType());
				}

				userMappingRow.setValue(column.getName(), value);

			}
		}
	}

	/**
	 * Validate a user mapping row
	 * 
	 * @param columns
	 * @param userMappingRow
	 */
	private static void validateUserMappingRow(String[] columns,
			UserMappingRow userMappingRow) {

		TestCase.assertEquals(columns.length, userMappingRow.columnCount());
		TestCase.assertFalse(userMappingRow.hasId());

		for (int i = 0; i < userMappingRow.columnCount(); i++) {
			UserCustomColumn column = userMappingRow.getTable().getColumns()
					.get(i);
			GeoPackageDataType dataType = column.getDataType();
			TestCase.assertEquals(i, column.getIndex());
			TestCase.assertEquals(columns[i], userMappingRow.getColumnName(i));
			TestCase.assertEquals(i, userMappingRow.getColumnIndex(columns[i]));
			int rowType = userMappingRow.getRowColumnType(i);
			Object value = userMappingRow.getValue(i);

			switch (rowType) {

			case UserCoreResultUtils.FIELD_TYPE_INTEGER:
				TestUtils.validateIntegerValue(value, column.getDataType());
				break;

			case UserCoreResultUtils.FIELD_TYPE_FLOAT:
				TestUtils.validateFloatValue(value, column.getDataType());
				break;

			case UserCoreResultUtils.FIELD_TYPE_STRING:
				if (dataType == GeoPackageDataType.DATE
						|| dataType == GeoPackageDataType.DATETIME) {
					TestCase.assertTrue(value instanceof Date);
					Date date = (Date) value;
					DateConverter converter = DateConverter.converter(dataType);
					String dateString = converter.stringValue(date);
					TestCase.assertEquals(date.getTime(),
							converter.dateValue(dateString).getTime());
				} else {
					TestCase.assertTrue(value instanceof String);
				}
				break;

			case UserCoreResultUtils.FIELD_TYPE_BLOB:
				TestCase.assertTrue(value instanceof byte[]);
				break;

			case UserCoreResultUtils.FIELD_TYPE_NULL:
				TestCase.assertNull(value);
				break;

			}
		}

	}

}
