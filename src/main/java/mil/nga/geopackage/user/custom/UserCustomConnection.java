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
public class UserCustomConnection extends
		UserConnection<UserCustomColumn, UserCustomTable, UserCustomRow, UserCustomResultSet> {

	/**
	 * Constructor
	 * 
	 * @param database
	 *            database connection
	 */
	public UserCustomConnection(GeoPackageConnection database) {
		super(database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected UserCustomResultSet createResult(String[] columns,
			ResultSet resultSet, String sql, String[] selectionArgs) {
		return new UserCustomResultSet(table, columns, resultSet, sql,
				selectionArgs);
	}

}
