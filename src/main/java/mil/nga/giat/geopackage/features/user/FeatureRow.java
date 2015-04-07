package mil.nga.giat.geopackage.features.user;

import mil.nga.giat.geopackage.geom.GeoPackageGeometryData;
import mil.nga.giat.geopackage.user.UserRow;

/**
 * Feature Row containing the values from a single cursor row
 * 
 * @author osbornb
 */
public class FeatureRow extends UserRow<FeatureColumn, FeatureTable> {

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param columnTypes
	 * @param values
	 */
	FeatureRow(FeatureTable table, int[] columnTypes, Object[] values) {
		super(table, columnTypes, values);
	}

	/**
	 * Constructor to create an empty row
	 * 
	 * @param table
	 */
	FeatureRow(FeatureTable table) {
		super(table);
	}

	/**
	 * Get the geometry column index
	 * 
	 * @return
	 */
	public int getGeometryColumnIndex() {
		return getTable().getGeometryColumnIndex();
	}

	/**
	 * Get the geometry feature column
	 * 
	 * @return
	 */
	public FeatureColumn getGeometryColumn() {
		return getTable().getGeometryColumn();
	}

	/**
	 * Get the geometry
	 * 
	 * @return
	 */
	public GeoPackageGeometryData getGeometry() {
		GeoPackageGeometryData geometryData = null;
		Object value = getValue(getGeometryColumnIndex());
		if (value != null) {
			geometryData = (GeoPackageGeometryData) value;
		}
		return geometryData;
	}

	/**
	 * Set the geometry data
	 * 
	 * @param geometryData
	 */
	public void setGeometry(GeoPackageGeometryData geometryData) {
		setValue(getGeometryColumnIndex(), geometryData);
	}

}
