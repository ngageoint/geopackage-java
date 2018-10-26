package mil.nga.geopackage.extension.style;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.sf.GeometryType;

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
	 * Get the table style of the geometry type
	 * 
	 * @param geometryType
	 *            geometry type
	 * @return style row
	 */
	public StyleRow getTableStyle(GeometryType geometryType) {
		return featureStyleExtension.getTableStyle(tableName, geometryType);
	}

	/**
	 * Get the table style default
	 * 
	 * @return style row
	 */
	public StyleRow getTableStyleDefault() {
		return featureStyleExtension.getTableStyleDefault(tableName);
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
	 * Get the table icon of the geometry type
	 * 
	 * @param geometryType
	 *            geometry type
	 * @return icon row
	 */
	public IconRow getTableIcon(GeometryType geometryType) {
		return featureStyleExtension.getTableIcon(tableName, geometryType);
	}

	/**
	 * Get the table icon default
	 * 
	 * @return icon row
	 */
	public IconRow getTableIconDefault() {
		return featureStyleExtension.getTableIconDefault(tableName);
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
	 * Get the feature style (style and icon) of the feature row, searching in
	 * order: feature geometry type style or icon, feature default style or
	 * icon, table geometry type style or icon, table default style or icon
	 * 
	 * @param featureRow
	 *            feature row
	 * @return feature style
	 */
	public FeatureStyle getFeatureStyle(FeatureRow featureRow) {
		return getFeatureStyle(featureRow.getId(), featureRow.getGeometryType());
	}

	/**
	 * Get the feature style default (style and icon) of the feature row,
	 * searching in order: feature default style or icon, table default style or
	 * icon
	 * 
	 * @param featureRow
	 *            feature row
	 * @return feature style
	 */
	public FeatureStyle getFeatureStyleDefault(FeatureRow featureRow) {
		return getFeatureStyle(featureRow.getId(), null);
	}

	/**
	 * Get the feature style (style and icon) of the feature, searching in
	 * order: feature geometry type style or icon, feature default style or
	 * icon, table geometry type style or icon, table default style or icon
	 * 
	 * @param featureId
	 *            feature id
	 * @param geometryType
	 *            geometry type
	 * @return feature style
	 */
	public FeatureStyle getFeatureStyle(long featureId,
			GeometryType geometryType) {

		FeatureStyle featureStyle = null;

		StyleRow style = getStyle(featureId, geometryType);
		IconRow icon = getIcon(featureId, geometryType);

		if (style != null || icon != null) {
			featureStyle = new FeatureStyle(style, icon);
		}

		return featureStyle;
	}

	/**
	 * Get the feature style (style and icon) of the feature, searching in
	 * order: feature geometry type style or icon, feature default style or
	 * icon, table geometry type style or icon, table default style or icon
	 * 
	 * @param featureId
	 *            feature id
	 * @return feature style
	 */
	public FeatureStyle getFeatureStyleDefault(long featureId) {
		return getFeatureStyle(featureId, null);
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
	 * Get the style of the feature row, searching in order: feature geometry
	 * type style, feature default style, table geometry type style, table
	 * default style
	 * 
	 * @param featureRow
	 *            feature row
	 * @return style row
	 */
	public StyleRow getStyle(FeatureRow featureRow) {
		return getStyle(featureRow.getId(), featureRow.getGeometryType());
	}

	/**
	 * Get the default style of the feature row, searching in order: feature
	 * default style, table default style
	 * 
	 * @param featureRow
	 *            feature row
	 * @return style row
	 */
	public StyleRow getStyleDefault(FeatureRow featureRow) {
		return getStyle(featureRow.getId(), null);
	}

	/**
	 * Get the style of the feature, searching in order: feature geometry type
	 * style, feature default style, table geometry type style, table default
	 * style
	 * 
	 * @param featureId
	 *            feature id
	 * @param geometryType
	 *            geometry type
	 * @return style row
	 */
	public StyleRow getStyle(long featureId, GeometryType geometryType) {

		StyleRow styleRow = featureStyleExtension.getStyle(tableName,
				featureId, geometryType, false);

		if (styleRow == null) {

			// Table Style
			// TODO cache table styles
			styleRow = featureStyleExtension.getTableStyle(tableName,
					geometryType);

		}

		return styleRow;
	}

	/**
	 * Get the default style of the feature, searching in order: feature default
	 * style, table default style
	 * 
	 * @param featureId
	 *            feature id
	 * @return style row
	 */
	public StyleRow getStyleDefault(long featureId) {
		return getStyle(featureId, null);
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

	/**
	 * Get the icon of the feature row, searching in order: feature geometry
	 * type icon, feature default icon, table geometry type icon, table default
	 * icon
	 * 
	 * @param featureRow
	 *            feature row
	 * @return icon row
	 */
	public IconRow getIcon(FeatureRow featureRow) {
		return getIcon(featureRow.getId(), featureRow.getGeometryType());
	}

	/**
	 * Get the default icon of the feature row, searching in order: feature
	 * default icon, table default icon
	 * 
	 * @param featureRow
	 *            feature row
	 * @return icon row
	 */
	public IconRow getIconDefault(FeatureRow featureRow) {
		return getIcon(featureRow.getId(), null);
	}

	/**
	 * Get the icon of the feature, searching in order: feature geometry type
	 * icon, feature default icon, table geometry type icon, table default icon
	 * 
	 * @param featureId
	 *            feature id
	 * @param geometryType
	 *            geometry type
	 * @return icon row
	 */
	public IconRow getIcon(long featureId, GeometryType geometryType) {

		IconRow iconRow = featureStyleExtension.getIcon(tableName, featureId,
				geometryType, false);

		if (iconRow == null) {

			// Table Style
			// TODO cache table icons
			iconRow = featureStyleExtension.getTableIcon(tableName,
					geometryType);

		}

		return iconRow;
	}

	/**
	 * Get the default icon of the feature, searching in order: feature default
	 * icon, table default icon
	 * 
	 * @param featureId
	 *            feature id
	 * @return icon row
	 */
	public IconRow getIconDefault(long featureId) {
		return getIcon(featureId, null);
	}

}
