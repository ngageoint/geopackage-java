package mil.nga.geopackage.extension.related;

import mil.nga.geopackage.user.UserRow;

/**
 * User Mapping Row containing the values from a single cursor row
 * 
 * @author osbornb
 * @since 3.0.1
 */
public class UserMappingRow extends
		UserRow<UserMappingColumn, UserMappingTable> {

	/**
	 * Constructor
	 * 
	 * @param table
	 *            user mapping table
	 * @param columnTypes
	 *            column types
	 * @param values
	 *            values
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
	 * @param userMappingRow
	 *            user mapping row to copy
	 */
	public UserMappingRow(UserMappingRow userMappingRow) {
		super(userMappingRow);
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
	 * Get the base ID
	 * 
	 * @return base ID
	 */
	public long getBaseId() {
		return ((Number) getValue(getBaseIdColumnIndex())).longValue();
	}

	/**
	 * Set the base ID
	 * 
	 * @param baseId
	 *            base ID
	 */
	public void setBaseId(long baseId) {
		setValue(getBaseIdColumnIndex(), baseId);
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

	/**
	 * Get the related ID
	 * 
	 * @return related ID
	 */
	public long getRelatedId() {
		return ((Number) getValue(getRelatedIdColumnIndex())).longValue();
	}

	/**
	 * Set the related ID
	 * 
	 * @param relatedId
	 *            related ID
	 */
	public void setRelatedId(long relatedId) {
		setValue(getRelatedIdColumnIndex(), relatedId);
	}

}
