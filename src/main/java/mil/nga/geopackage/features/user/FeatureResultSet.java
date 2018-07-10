package mil.nga.geopackage.features.user;

import java.sql.ResultSet;

import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.user.UserCoreResultUtils;
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
	 *            feature table
	 * @param resultSet
	 *            result set
	 * @param count
	 *            count
	 */
	public FeatureResultSet(FeatureTable table, ResultSet resultSet, int count) {
		super(table, resultSet, count);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureRow getRow(int[] columnTypes, Object[] values) {
		return new FeatureRow(getTable(), columnTypes, values);
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

		GeoPackageGeometryData geometry = null;

		int columnIndex = getTable().getGeometryColumnIndex();
		int type = getType(columnIndex);

		if (type != UserCoreResultUtils.FIELD_TYPE_NULL) {
			byte[] geometryBytes = getBlob(columnIndex);

			if (geometryBytes != null) {
				geometry = new GeoPackageGeometryData(geometryBytes);
			}
		}

		return geometry;
	}

}
