package mil.nga.geopackage.extension.related_tables;

import java.sql.ResultSet;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserConnection;

/**
 * GeoPackage User Mapping Connection
 * 
 * @author yutzlejp
 */
public class UserMappingConnection extends
		UserConnection<UserMappingColumn, UserMappingTable, UserMappingRow, UserMappingResultSet> {

	/**
	 * Constructor
	 * 
	 * @param database
	 */
	public UserMappingConnection(GeoPackageConnection database) {
		super(database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected UserMappingResultSet createResult(ResultSet resultSet, int count) {
		return new UserMappingResultSet(table, resultSet, count);
	}

}
