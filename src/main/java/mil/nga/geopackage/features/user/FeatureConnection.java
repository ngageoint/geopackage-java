package mil.nga.geopackage.features.user;

import java.sql.ResultSet;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserConnection;

/**
 * GeoPackage Feature Connection
 * 
 * @author osbornb
 */
public class FeatureConnection
		extends
		UserConnection<FeatureColumn, FeatureTable, FeatureRow, FeatureResultSet> {

	/**
	 * Constructor
	 * 
	 * @param database
	 */
	public FeatureConnection(GeoPackageConnection database) {
		super(database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected FeatureResultSet createResult(ResultSet resultSet, int count) {
		return new FeatureResultSet(table, resultSet, count);
	}

}
