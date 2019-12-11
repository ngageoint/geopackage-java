package mil.nga.geopackage.features.user;

import java.sql.ResultSet;

import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.user.UserResultSet;

/**
 * Feature Result Set to wrap a database ResultSet for feature queries
 * 
 * @author osbornb
 */
public class FeatureResultSet
		extends UserResultSet<FeatureColumn, FeatureTable, FeatureRow> {

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
	public FeatureResultSet(FeatureTable table, ResultSet resultSet,
			int count) {
		this(table, null, resultSet, count);
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            feature table
	 * @param columns
	 *            columns
	 * @param resultSet
	 *            result set
	 * @param count
	 *            count
	 * @since 3.5.0
	 */
	public FeatureResultSet(FeatureTable table, String[] columns,
			ResultSet resultSet, int count) {
		super(table, columns, resultSet, count);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FeatureRow getRow(int[] columnTypes, Object[] values) {
		return new FeatureRow(getTable(), getColumns(), columnTypes, values);
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

		int columnIndex = getColumns()
				.getColumnIndex(getTable().getGeometryColumn().getName()); // TODO
																			// feature
																			// columns?
		byte[] geometryBytes = getBlob(columnIndex);
		if (geometryBytes != null) {
			geometry = new GeoPackageGeometryData(geometryBytes);
		}

		return geometry;
	}

}
