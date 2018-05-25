package mil.nga.geopackage.user.custom;

import mil.nga.geopackage.user.UserRow;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomTable;

/**
 * User Custom Row containing the values from a single cursor row
 * 
 * @author osbornb
 * @since 3.0.1
 */
public class UserCustomRow extends UserRow<UserCustomColumn, UserCustomTable> {

	/**
	 * Constructor
	 * 
	 * @param table
	 *            user custom table
	 * @param columnTypes
	 *            column types
	 * @param values
	 *            values
	 */
	UserCustomRow(UserCustomTable table, int[] columnTypes, Object[] values) {
		super(table, columnTypes, values);
	}

	/**
	 * Constructor to create an empty row
	 * 
	 * @param table
	 */
	protected UserCustomRow(UserCustomTable table) {
		super(table);
	}

	/**
	 * Copy Constructor
	 * 
	 * @param userCustomRow
	 *            user custom row to copy
	 */
	public UserCustomRow(UserCustomRow userCustomRow) {
		super(userCustomRow);
	}

}
