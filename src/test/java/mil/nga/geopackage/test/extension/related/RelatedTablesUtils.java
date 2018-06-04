package mil.nga.geopackage.test.extension.related;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;
import mil.nga.geopackage.db.DateConverter;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.user.UserCoreResultUtils;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomRow;
import mil.nga.geopackage.user.custom.UserCustomTable;

/**
 * Related tables utils
 * 
 * @author osbornb
 */
public class RelatedTablesUtils {

	/**
	 * Create additional user table columns
	 * 
	 * @param startingIndex
	 *            starting index
	 * @return additional user table columns
	 */
	public static List<UserCustomColumn> createAdditionalUserColumns(
			int startingIndex) {

		List<UserCustomColumn> columns = new ArrayList<>();

		int columnIndex = startingIndex;
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
	 * Populate the user row additional column values
	 * 
	 * @param userTable
	 *            user custom table
	 * @param userRow
	 *            user custom row
	 * @param skipColumns
	 *            columns to skip populating
	 */
	public static void populateUserRow(UserCustomTable userTable,
			UserCustomRow userRow, List<String> skipColumns) {

		Set<String> skipColumnsSet = new HashSet<>(skipColumns);

		for (UserCustomColumn column : userTable.getColumns()) {
			if (!skipColumnsSet.contains(column.getName())) {

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

				userRow.setValue(column.getName(), value);

			}
		}
	}

	/**
	 * Validate a user row
	 * 
	 * @param columns
	 *            array of columns
	 * @param userRow
	 *            user custom row
	 */
	public static void validateUserRow(String[] columns, UserCustomRow userRow) {

		TestCase.assertEquals(columns.length, userRow.columnCount());
		TestCase.assertFalse(userRow.hasId());

		for (int i = 0; i < userRow.columnCount(); i++) {
			UserCustomColumn column = userRow.getTable().getColumns().get(i);
			GeoPackageDataType dataType = column.getDataType();
			TestCase.assertEquals(i, column.getIndex());
			TestCase.assertEquals(columns[i], userRow.getColumnName(i));
			TestCase.assertEquals(i, userRow.getColumnIndex(columns[i]));
			int rowType = userRow.getRowColumnType(i);
			Object value = userRow.getValue(i);

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
