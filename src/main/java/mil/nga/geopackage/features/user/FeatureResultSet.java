package mil.nga.geopackage.features.user;

import java.sql.ResultSet;

import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.user.UserResultSet;

/**
 * Feature Result Set to wrap a database ResultSet for feature queries
 * 
 * @author osbornb
 */
public class FeatureResultSet extends
		UserResultSet<FeatureColumn, FeatureTable, FeatureRow> {

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param resultSet
	 * @param count
	 */
	public FeatureResultSet(FeatureTable table, ResultSet resultSet, int count) {
		super(table, resultSet, count);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureRow getRow(int[] columnTypes, Object[] values) {
		FeatureRow row = new FeatureRow(getTable(), columnTypes, values);
		return row;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Handles geometries
	 */
	@Override
	public Object getValue(FeatureColumn column) {
		Object value;
		if (column.isGeometry()) {
			value = getGeometry();
		} else {
			value = super.getValue(column);
		}
		return value;
	}

	/**
	 * Get the geometry
	 * 
	 * @return geometry data
	 */
	public GeoPackageGeometryData getGeometry() {

		byte[] geometryBytes = getBlob(getTable().getGeometryColumnIndex());

		GeoPackageGeometryData geometry = null;
		if (geometryBytes != null) {
			geometry = new GeoPackageGeometryData(geometryBytes);
		}

		return geometry;
	}

}
