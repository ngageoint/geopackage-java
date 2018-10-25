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
	 * Get the default style
	 * 
	 * @return default style or null
	 */
	public Style getDefaultStyle() {
		return featureStyles.getDefaultStyle(tableName);
	}

	/**
	 * Get the default icon for the feature table
	 * 
	 * @return default icon or null
	 */
	public Icon getDefaultIcon() {
		return featureStyles.getDefaultIcon(tableName);
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
	 * Get the style for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return style or null
	 */
	public Style getStyle(FeatureRow featureRow) {
		return featureStyles.getStyle(featureRow);
	}

	/**
	 * Get the style for the feature id
	 * 
	 * @param featureId
	 *            feature id
	 * @return style or null
	 */
	public Style getStyle(long featureId) {
		return featureStyles.getStyle(tableName, featureId);
	}

	/**
	 * Get the icon for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return icon or null
	 */
	public Icon getIcon(FeatureRow featureRow) {
		return featureStyles.getIcon(featureRow);
	}

	/**
	 * Get the icon for the feature id
	 * 
	 * @param featureId
	 *            feature id
	 * @return icon or null
	 */
	public Icon getIcon(long featureId) {
		return featureStyles.getIcon(tableName, featureId);
	}

}
