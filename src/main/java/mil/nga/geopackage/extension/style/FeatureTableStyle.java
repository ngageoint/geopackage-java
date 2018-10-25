package mil.nga.geopackage.extension.style;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;

/**
 * Feature Table Style, styles and icons for an individual feature table
 * 
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureTableStyle {

	/**
	 * Feature Styles
	 */
	private final FeatureStyles featureStyles;

	/**
	 * Feature Table name
	 */
	private final String tableName;

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param featureTable
	 *            feature table
	 */
	public FeatureTableStyle(GeoPackage geoPackage, FeatureTable featureTable) {
		this(geoPackage, featureTable.getTableName());
	}

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param geometryColumns
	 *            geometry columns
	 */
	public FeatureTableStyle(GeoPackage geoPackage,
			GeometryColumns geometryColumns) {
		this(geoPackage, geometryColumns.getTableName());
	}

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param contents
	 *            feature contents
	 */
	public FeatureTableStyle(GeoPackage geoPackage, Contents contents) {
		this(geoPackage, contents.getTableName());
	}

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param featureTable
	 *            feature table
	 */
	public FeatureTableStyle(GeoPackage geoPackage, String featureTable) {
		featureStyles = new FeatureStyles(geoPackage);
		tableName = featureTable;
		if (!geoPackage.isFeatureTable(featureTable)) {
			throw new GeoPackageException(
					"Table must be a feature table. Table: " + featureTable
							+ ", Actual Type: "
							+ geoPackage.getTableType(featureTable));
		}
	}

	/**
	 * Get the feature styles
	 * 
	 * @return feature styles
	 */
	public FeatureStyles getFeatureStyles() {
		return featureStyles;
	}

	/**
	 * Get the feature table name
	 * 
	 * @return feature table name
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Get the default feature style
	 * 
	 * @return default feature style or null
	 */
	public FeatureStyle getDefaultFeatureStyle() {
		return featureStyles.getDefaultFeatureStyle(tableName);
	}

	/**
	 * Get the default styles
	 * 
	 * @return default styles or null
	 */
	public Styles getDefaultStyles() {
		return featureStyles.getDefaultStyles(tableName);
	}

	/**
	 * Get the default icons for the feature table
	 * 
	 * @return default icons or null
	 */
	public Icons getDefaultIcons() {
		return featureStyles.getDefaultIcons(tableName);
	}

	/**
	 * Get the feature style for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return feature style or null
	 */
	public FeatureStyle getFeatureStyle(FeatureRow featureRow) {
		return featureStyles.getFeatureStyle(featureRow);
	}

	/**
	 * Get the feature style for the feature id
	 * 
	 * @param featureId
	 *            feature id
	 * @return feature style or null
	 */
	public FeatureStyle getFeatureStyle(long featureId) {
		return featureStyles.getFeatureStyle(tableName, featureId);
	}

	/**
	 * Get the styles for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return styles or null
	 */
	public Styles getStyles(FeatureRow featureRow) {
		return featureStyles.getStyles(featureRow);
	}

	/**
	 * Get the styles for the feature id
	 * 
	 * @param featureId
	 *            feature id
	 * @return styles or null
	 */
	public Styles getStyles(long featureId) {
		return featureStyles.getStyles(tableName, featureId);
	}

	/**
	 * Get the icons for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return icons or null
	 */
	public Icons getIcons(FeatureRow featureRow) {
		return featureStyles.getIcons(featureRow);
	}

	/**
	 * Get the icons for the feature id
	 * 
	 * @param featureId
	 *            feature id
	 * @return icons or null
	 */
	public Icons getIcons(long featureId) {
		return featureStyles.getIcons(tableName, featureId);
	}

}
