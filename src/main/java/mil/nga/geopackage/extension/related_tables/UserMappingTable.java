package mil.nga.geopackage.extension.related_tables;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.user.UserTable;

/**
 * Represents a user tile table
 * 
 * @author yutzlejp
 */
public class UserMappingTable extends UserTable<UserMappingColumn> {

	/**
	 * Base ID column name
	 */
	public static final String COLUMN_BASE_ID = "base_id";

	/**
	 * Related ID column name
	 */
	public static final String COLUMN_RELATED_ID = "related_id";

	/**
	 * Base ID column index
	 */
	private final int baseIdIndex;

	/**
	 * Related ID column index
	 */
	private final int relatedIdIndex;

	/**
	 * Constructor
	 * 
	 * @param tableName
	 * @param columns
	 */
	public UserMappingTable(String tableName) {
		this(tableName, createRequiredColumns());
	}
		/**
		 * Constructor
		 * 
		 * @param tableName
		 * @param columns
		 */
	public UserMappingTable(String tableName, List<UserMappingColumn> columns) {
		super(tableName, columns);

		Integer baseId = null;
		Integer relatedId = null;

		// Find the required columns
		for (UserMappingColumn column : columns) {

			String columnName = column.getName();
			int columnIndex = column.getIndex();

			if (columnName.equals(COLUMN_BASE_ID)) {
				duplicateCheck(columnIndex, baseId, COLUMN_BASE_ID);
				typeCheck(GeoPackageDataType.INTEGER, column);
				baseId = columnIndex;
			} else if (columnName.equals(COLUMN_RELATED_ID)) {
				duplicateCheck(columnIndex, relatedId, COLUMN_RELATED_ID);
				typeCheck(GeoPackageDataType.INTEGER, column);
				relatedId = columnIndex;
			}

		}

		// Verify the required columns were found
		missingCheck(relatedId, COLUMN_RELATED_ID);
		relatedIdIndex = relatedId;

		missingCheck(baseId, COLUMN_BASE_ID);
		baseIdIndex = baseId;

	}
	public int getBaseIdIndex() {
		return baseIdIndex;
	}
	public int getRelatedIdIndex() {
		return relatedIdIndex;
	}
	/**
	 * Get the base ID column
	 * 
	 * @return tile column
	 */
	public UserMappingColumn getBaseIdColumn() {
		return getColumn(baseIdIndex);
	}
	/**
	 * Get the related ID column
	 * 
	 * @return tile column
	 */
	public UserMappingColumn getRelatedIdColumn() {
		return getColumn(relatedIdIndex);
	}
	/**
	 * Create the required table columns, starting at index 0
	 * 
	 * @return tile columns
	 */
	public static List<UserMappingColumn> createRequiredColumns() {
		return createRequiredColumns(0);
	}

	/**
	 * Create the required table columns, starting at the provided index
	 * 
	 * @param startingIndex
	 *            starting index
	 * @return tile columns
	 */
	public static List<UserMappingColumn> createRequiredColumns(int startingIndex) {

		List<UserMappingColumn> columns = new ArrayList<UserMappingColumn>();
		columns.add(UserMappingColumn.createColumn(startingIndex++, COLUMN_BASE_ID, GeoPackageDataType.INTEGER, false, null));
		columns.add(UserMappingColumn.createColumn(startingIndex++, COLUMN_RELATED_ID, GeoPackageDataType.INTEGER, false, null));

		return columns;
	}
}
