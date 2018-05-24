package mil.nga.geopackage.extension.related;

import java.sql.ResultSet;

import mil.nga.geopackage.user.UserResultSet;

/**
 * User Mapping Result Set to wrap a database ResultSet for tile queries
 * 
 * @author osbornb
 * @since 3.0.1
 */
public class UserMappingResultSet extends
		UserResultSet<UserMappingColumn, UserMappingTable, UserMappingRow> {

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param resultSet
	 * @param count
	 */
	public UserMappingResultSet(UserMappingTable table, ResultSet resultSet,
			int count) {
		super(table, resultSet, count);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserMappingRow getRow(int[] columnTypes, Object[] values) {
		return new UserMappingRow(getTable(), columnTypes, values);
	}

}
