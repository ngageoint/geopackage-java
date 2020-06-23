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
		super(table, resultSet, count);
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
	 * Constructor
	 * 
	 * @param table
	 *            feature table
	 * @param resultSet
	 *            result set
	 * @param sql
	 *            SQL statement
	 * @param selectionArgs
	 *            selection arguments
	 * @since 4.0.0
	 */
	public FeatureResultSet(FeatureTable table, ResultSet resultSet, String sql,
			String[] selectionArgs) {
		super(table, resultSet, sql, selectionArgs);
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
	 * @param sql
	 *            SQL statement
	 * @param selectionArgs
	 *            selection arguments
	 * @since 4.0.0
	 */
	public FeatureResultSet(FeatureTable table, String[] columns,
			ResultSet resultSet, String sql, String[] selectionArgs) {
		super(table, columns, resultSet, sql, selectionArgs);
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
	 * {@inheritDoc}
	 */
	@Override
	public FeatureColumns getColumns() {
		return (FeatureColumns) super.getColumns();
	}

	/**
	 * Get the geometry
	 * 
	 * @return geometry data
	 */
	public GeoPackageGeometryData getGeometry() {

		GeoPackageGeometryData geometry = null;

		int columnIndex = getColumns().getGeometryIndex();
		byte[] geometryBytes = getBlob(columnIndex);
		if (geometryBytes != null) {
			geometry = GeoPackageGeometryData.create(geometryBytes);
		}

		return geometry;
	}

}
