package mil.nga.geopackage.extension.related_tables;

import mil.nga.geopackage.user.UserRow;

/**
 * Tile Row containing the values from a single cursor row
 * 
 * @author osbornb
 */
public class UserMappingRow extends UserRow<UserMappingColumn, UserMappingTable> {

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param columnTypes
	 * @param values
	 */
	UserMappingRow(UserMappingTable table, int[] columnTypes, Object[] values) {
		super(table, columnTypes, values);
	}

	/**
	 * Constructor to create an empty row
	 * 
	 * @param table
	 */
	UserMappingRow(UserMappingTable table) {
		super(table);
	}

	/**
	 * Copy Constructor
	 * 
	 * @param tileRow
	 *            tile row to copy
	 * @since 1.3.0
	 */
	public UserMappingRow(UserMappingRow tileRow) {
		super(tileRow);
	}

	/**
	 * Get the base ID
	 * 
	 * @return base ID
	 */
	public long getBaseId() {
		return ((Number) getValue(getBaseIdColumnIndex())).longValue();
	}

	/**
	 * Get the base ID column index
	 * 
	 * @return base ID column index
	 */
	public int getBaseIdColumnIndex() {
		return getTable().getBaseIdIndex();
	}

	/**
	 * Get the base ID column
	 * 
	 * @return base ID column
	 */
	public UserMappingColumn getBaseIdColumn() {
		return getTable().getBaseIdColumn();
	}

	/**
	 * Get the related ID
	 * 
	 * @return related ID
	 */
	public long getRelatedId() {
		return ((Number) getValue(getBaseIdColumnIndex())).longValue();
	}

	/**
	 * Get the related ID column index
	 * 
	 * @return related ID column index
	 */
	public int getRelatedIdColumnIndex() {
		return getTable().getRelatedIdIndex();
	}

	/**
	 * Get the related ID column
	 * 
	 * @return related ID column
	 */
	public UserMappingColumn getRelatedIdColumn() {
		return getTable().getRelatedIdColumn();
	}
}
