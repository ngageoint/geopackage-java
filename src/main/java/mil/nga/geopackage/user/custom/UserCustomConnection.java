package mil.nga.geopackage.user.custom;

import java.sql.ResultSet;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserConnection;

/**
 * GeoPackage User Custom Connection
 * 
 * @author osbornb
 * @since 3.0.1
 */
public class UserCustomConnection
		extends
		UserConnection<UserCustomColumn, UserCustomTable, UserCustomRow, UserCustomResultSet> {

	/**
	 * Constructor
	 * 
	 * @param database
	 */
	public UserCustomConnection(GeoPackageConnection database) {
		super(database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected UserCustomResultSet createResult(ResultSet resultSet, int count) {
		return new UserCustomResultSet(table, resultSet, count);
	}

}
