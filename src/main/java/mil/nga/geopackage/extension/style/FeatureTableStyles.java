package mil.nga.geopackage.extension.style;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;

/**
 * Feature Table Styles, styles and icons for an individual feature table
 * 
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureTableStyles {

	/**
	 * Feature Styles
	 */
	private final FeatureStyleExtension featureStyleExtension;

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
	public FeatureTableStyles(GeoPackage geoPackage, FeatureTable featureTable) {
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
	public FeatureTableStyles(GeoPackage geoPackage,
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
	public FeatureTableStyles(GeoPackage geoPackage, Contents contents) {
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
	public FeatureTableStyles(GeoPackage geoPackage, String featureTable) {
		featureStyleExtension = new FeatureStyleExtension(geoPackage);
		tableName = featureTable;
		if (!geoPackage.isFeatureTable(featureTable)) {
			throw new GeoPackageException(
					"Table must be a feature table. Table: " + featureTable
							+ ", Actual Type: "
							+ geoPackage.getTableType(featureTable));
		}
	}

	/**
	 * Get the feature style extension
	 * 
	 * @return feature style extension
	 */
	public FeatureStyleExtension getFeatureStyleExtension() {
		return featureStyleExtension;
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
	 * Get the table feature styles
	 * 
	 * @return table feature styles or null
	 */
	public FeatureStyles getTableFeatureStyles() {
		return featureStyleExtension.getTableFeatureStyles(tableName);
	}

	/**
	 * Get the table styles
	 * 
	 * @return table styles or null
	 */
	public Styles getTableStyles() {
		return featureStyleExtension.getTableStyles(tableName);
	}

	/**
	 * Get the table icons
	 * 
	 * @return table icons or null
	 */
	public Icons getTableIcons() {
		return featureStyleExtension.getTableIcons(tableName);
	}

	/**
	 * Get the feature styles for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return feature styles or null
	 */
	public FeatureStyles getFeatureStyles(FeatureRow featureRow) {
		return featureStyleExtension.getFeatureStyles(featureRow);
	}

	/**
	 * Get the feature styles for the feature id
	 * 
	 * @param featureId
	 *            feature id
	 * @return feature styles or null
	 */
	public FeatureStyles getFeatureStyles(long featureId) {
		return featureStyleExtension.getFeatureStyles(tableName, featureId);
	}

	/**
	 * Get the styles for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return styles or null
	 */
	public Styles getStyles(FeatureRow featureRow) {
		return featureStyleExtension.getStyles(featureRow);
	}

	/**
	 * Get the styles for the feature id
	 * 
	 * @param featureId
	 *            feature id
	 * @return styles or null
	 */
	public Styles getStyles(long featureId) {
		return featureStyleExtension.getStyles(tableName, featureId);
	}

	/**
	 * Get the icons for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return icons or null
	 */
	public Icons getIcons(FeatureRow featureRow) {
		return featureStyleExtension.getIcons(featureRow);
	}

	/**
	 * Get the icons for the feature id
	 * 
	 * @param featureId
	 *            feature id
	 * @return icons or null
	 */
	public Icons getIcons(long featureId) {
		return featureStyleExtension.getIcons(tableName, featureId);
	}

}
