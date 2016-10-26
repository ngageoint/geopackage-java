package mil.nga.geopackage.attributes;

import java.sql.ResultSet;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserConnection;

/**
 * GeoPackage Attributes Connection
 * 
 * @author osbornb
 * @since 1.2.1
 */
public class AttributesConnection
		extends
		UserConnection<AttributesColumn, AttributesTable, AttributesRow, AttributesResultSet> {

	/**
	 * Constructor
	 * 
	 * @param database
	 */
	public AttributesConnection(GeoPackageConnection database) {
		super(database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AttributesResultSet createResult(ResultSet resultSet, int count) {
		return new AttributesResultSet(table, resultSet, count);
	}

}
