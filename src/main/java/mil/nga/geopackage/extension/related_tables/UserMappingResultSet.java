package mil.nga.geopackage.extension.related_tables;

import java.sql.ResultSet;

import mil.nga.geopackage.user.UserResultSet;

/**
 * Result Set to wrap a database ResultSet for user mapping queries
 * 
 * @author yutzlejp
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
	public UserMappingResultSet(UserMappingTable table, ResultSet resultSet, int count) {
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
