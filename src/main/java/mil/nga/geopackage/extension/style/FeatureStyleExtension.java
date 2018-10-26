package mil.nga.geopackage.extension.style;

import java.util.List;
import java.util.Map.Entry;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.sf.GeometryType;

/**
 * Feature Style extension for styling features
 * 
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureStyleExtension extends FeatureCoreStyleExtension {

	/**
	 * Related Tables extension
	 */
	protected final RelatedTablesExtension relatedTables;

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public FeatureStyleExtension(GeoPackage geoPackage) {
		super(geoPackage, new RelatedTablesExtension(geoPackage));
		this.relatedTables = (RelatedTablesExtension) super.getRelatedTables();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RelatedTablesExtension getRelatedTables() {
		return relatedTables;
	}

	/**
	 * Get a Style Mapping DAO
	 * 
	 * @param featureTable
	 *            feature table
	 * 
	 * @return style mapping DAO
	 */
	public StyleMappingDao getStyleMappingDao(String featureTable) {
		return getMappingDao(TABLE_MAPPING_STYLE, featureTable);
	}

	/**
	 * Get a Style Default Mapping DAO
	 * 
	 * @param featureTable
	 *            feature table
	 * 
	 * @return style default mapping DAO
	 */
	public StyleMappingDao getStyleDefaultMappingDao(String featureTable) {
		return getMappingDao(TABLE_MAPPING_STYLE_DEFAULT, featureTable);
	}

	/**
	 * Get a Icon Mapping DAO
	 * 
	 * @param featureTable
	 *            feature table
	 * 
	 * @return icon mapping DAO
	 */
	public StyleMappingDao getIconMappingDao(String featureTable) {
		return getMappingDao(TABLE_MAPPING_ICON, featureTable);
	}

	/**
	 * Get a Icon Default Mapping DAO
	 * 
	 * @param featureTable
	 *            feature table
	 * 
	 * @return icon default mapping DAO
	 */
	public StyleMappingDao getIconDefaultMappingDao(String featureTable) {
		return getMappingDao(TABLE_MAPPING_ICON_DEFAULT, featureTable);
	}

	/**
	 * Get a Style Mapping DAO from a table name
	 * 
	 * @param tablePrefix
	 *            table name prefix
	 * @param featureTable
	 *            feature table
	 * @return style mapping dao
	 */
	private StyleMappingDao getMappingDao(String tablePrefix,
			String featureTable) {
		String tableName = tablePrefix + featureTable;
		StyleMappingDao dao = null;
		if (geoPackage.isTable(tableName)) {
			dao = new StyleMappingDao(relatedTables.getUserDao(tableName));
		}
		return dao;
	}

	/**
	 * Get a style DAO
	 * 
	 * @return style DAO
	 */
	public StyleDao getStyleDao() {
		StyleDao styleDao = new StyleDao(
				relatedTables.getUserDao(StyleTable.TABLE_NAME));
		relatedTables.setContents(styleDao.getTable());
		return styleDao;
	}

	/**
	 * Get a icon DAO
	 * 
	 * @return icon DAO
	 */
	public IconDao getIconDao() {
		IconDao iconDao = new IconDao(
				relatedTables.getUserDao(IconTable.TABLE_NAME));
		relatedTables.setContents(iconDao.getTable());
		return iconDao;
	}

	/**
	 * Get the feature table default feature styles
	 * 
	 * @param featureTable
	 *            feature table
	 * @return table feature styles or null
	 */
	public FeatureStyles getTableFeatureStyles(FeatureTable featureTable) {
		return getTableFeatureStyles(featureTable.getTableName());
	}

	/**
	 * Get the feature table default feature styles
	 * 
	 * @param featureTable
	 *            feature table
	 * @return table feature styles or null
	 */
	public FeatureStyles getTableFeatureStyles(String featureTable) {

		FeatureStyles featureStyles = null;

		Long id = contentsId.getId(featureTable);
		if (id != null) {

			Styles styles = getTableStyles(featureTable, id);
			Icons icons = getTableIcons(featureTable, id);

			if (styles != null || icons != null) {
				featureStyles = new FeatureStyles(styles, icons);
			}

		}

		return featureStyles;
	}

	/**
	 * Get the feature table default styles
	 * 
	 * @param featureTable
	 *            feature table
	 * @return table styles or null
	 */
	public Styles getTableStyles(FeatureTable featureTable) {
		return getTableStyles(featureTable.getTableName());
	}

	/**
	 * Get the feature table default styles
	 * 
	 * @param featureTable
	 *            feature table
	 * @return table styles or null
	 */
	public Styles getTableStyles(String featureTable) {
		Styles styles = null;
		Long id = contentsId.getId(featureTable);
		if (id != null) {
			styles = getTableStyles(featureTable, id);
		}
		return styles;
	}

	/**
	 * Get the feature table default styles
	 * 
	 * @param featureTable
	 *            feature table
	 * @param contentsId
	 *            contents id
	 * @return table styles or null
	 */
	private Styles getTableStyles(String featureTable, long contentsId) {
		return getStyles(contentsId, getStyleDefaultMappingDao(featureTable));
	}

	/**
	 * Get the style of the feature table and geometry type
	 * 
	 * @param featureTable
	 *            feature table
	 * @param geometryType
	 *            geometry type
	 * @return style row
	 */
	public StyleRow getTableStyle(String featureTable, GeometryType geometryType) {
		StyleRow styleRow = null;
		Styles tableStyles = getTableStyles(featureTable);
		if (tableStyles != null) {
			styleRow = tableStyles.getStyle(geometryType);
		}
		return styleRow;
	}

	/**
	 * Get the default style of the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return style row
	 */
	public StyleRow getTableStyleDefault(String featureTable) {
		return getTableStyle(featureTable, null);
	}

	/**
	 * Get the feature table default icons
	 * 
	 * @param featureTable
	 *            feature table
	 * @return table icons or null
	 */
	public Icons getTableIcons(FeatureTable featureTable) {
		return getTableIcons(featureTable.getTableName());
	}

	/**
	 * Get the feature table default icons
	 * 
	 * @param featureTable
	 *            feature table
	 * @return table icons or null
	 */
	public Icons getTableIcons(String featureTable) {
		Icons icons = null;
		Long id = contentsId.getId(featureTable);
		if (id != null) {
			icons = getTableIcons(featureTable, id);
		}
		return icons;
	}

	/**
	 * Get the feature table default icons
	 * 
	 * @param featureTable
	 *            feature table
	 * @param contentsId
	 *            contents id
	 * @return table icons or null
	 */
	private Icons getTableIcons(String featureTable, long contentsId) {
		return getIcons(contentsId, getIconDefaultMappingDao(featureTable));
	}

	/**
	 * Get the default icon of the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return icon row
	 */
	public IconRow getTableIconDefault(String featureTable) {
		return getTableIcon(featureTable, null);
	}

	/**
	 * Get the icon of the feature table and geometry type
	 * 
	 * @param featureTable
	 *            feature table
	 * @param geometryType
	 *            geometry type
	 * @return icon row
	 */
	public IconRow getTableIcon(String featureTable, GeometryType geometryType) {
		IconRow iconRow = null;
		Icons tableIcons = getTableIcons(featureTable);
		if (tableIcons != null) {
			iconRow = tableIcons.getIcon(geometryType);
		}
		return iconRow;
	}

	/**
	 * Get the feature styles for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return feature styles or null
	 */
	public FeatureStyles getFeatureStyles(FeatureRow featureRow) {
		return getFeatureStyles(featureRow.getTable().getTableName(),
				featureRow.getId());
	}

	/**
	 * Get the feature styles for the feature table and feature id
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @return feature styles or null
	 */
	public FeatureStyles getFeatureStyles(String featureTable, long featureId) {

		Styles styles = getStyles(featureTable, featureId);
		Icons icons = getIcons(featureTable, featureId);

		FeatureStyles featureStyles = null;
		if (styles != null || icons != null) {
			featureStyles = new FeatureStyles(styles, icons);
		}

		return featureStyles;
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
		return getFeatureStyle(featureRow.getTable().getTableName(),
				featureRow.getId(), featureRow.getGeometryType());
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
		return getFeatureStyle(featureRow.getTable().getTableName(),
				featureRow.getId(), null);
	}

	/**
	 * Get the feature style (style and icon) of the feature, searching in
	 * order: feature geometry type style or icon, feature default style or
	 * icon, table geometry type style or icon, table default style or icon
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @param geometryType
	 *            geometry type
	 * @return feature style
	 */
	public FeatureStyle getFeatureStyle(String featureTable, long featureId,
			GeometryType geometryType) {

		FeatureStyle featureStyle = null;

		StyleRow style = getStyle(featureTable, featureId, geometryType);
		IconRow icon = getIcon(featureTable, featureId, geometryType);

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
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @return feature style
	 */
	public FeatureStyle getFeatureStyleDefault(String featureTable,
			long featureId) {
		return getFeatureStyle(featureTable, featureId, null);
	}

	/**
	 * Get the styles for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return styles or null
	 */
	public Styles getStyles(FeatureRow featureRow) {
		return getStyles(featureRow.getTable().getTableName(),
				featureRow.getId());
	}

	/**
	 * Get the styles for the feature table and feature id
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @return styles or null
	 */
	public Styles getStyles(String featureTable, long featureId) {
		return getStyles(featureId, getStyleMappingDao(featureTable));
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
		return getStyle(featureRow.getTable().getTableName(),
				featureRow.getId(), featureRow.getGeometryType());
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
		return getStyle(featureRow.getTable().getTableName(),
				featureRow.getId(), null);
	}

	/**
	 * Get the style of the feature, searching in order: feature geometry type
	 * style, feature default style, table geometry type style, table default
	 * style
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @param geometryType
	 *            geometry type
	 * @return style row
	 */
	public StyleRow getStyle(String featureTable, long featureId,
			GeometryType geometryType) {
		return getStyle(featureTable, featureId, geometryType, true);
	}

	/**
	 * Get the default style of the feature, searching in order: feature default
	 * style, table default style
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @return style row
	 */
	public StyleRow getStyleDefault(String featureTable, long featureId) {
		return getStyle(featureTable, featureId, null, true);
	}

	/**
	 * Get the style of the feature, searching in order: feature geometry type
	 * style, feature default style, when tableStyle enabled continue searching:
	 * table geometry type style, table default style
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @param geometryType
	 *            geometry type
	 * @param tableStyle
	 *            when true and a feature style is not found, query for a
	 *            matching table style
	 * 
	 * @return style row
	 */
	public StyleRow getStyle(String featureTable, long featureId,
			GeometryType geometryType, boolean tableStyle) {

		StyleRow styleRow = null;

		// Feature Style
		Styles styles = getStyles(featureTable, featureId);
		if (styles != null) {
			styleRow = styles.getStyle(geometryType);
		}

		if (styleRow == null && tableStyle) {

			// Table Style
			styleRow = getTableStyle(featureTable, geometryType);

		}

		return styleRow;
	}

	/**
	 * Get the default style of the feature, searching in order: feature default
	 * style, when tableStyle enabled continue searching: table default style
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @param tableStyle
	 *            when true and a feature style is not found, query for a
	 *            matching table style
	 * 
	 * @return style row
	 */
	public StyleRow getStyleDefault(String featureTable, long featureId,
			boolean tableStyle) {
		return getStyle(featureTable, featureId, null, tableStyle);
	}

	/**
	 * Get the icons for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return icons or null
	 */
	public Icons getIcons(FeatureRow featureRow) {
		return getIcons(featureRow.getTable().getTableName(),
				featureRow.getId());
	}

	/**
	 * Get the icons for the feature table and feature id
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @return icons or null
	 */
	public Icons getIcons(String featureTable, long featureId) {
		return getIcons(featureId, getIconMappingDao(featureTable));
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
		return getIcon(featureRow.getTable().getTableName(),
				featureRow.getId(), featureRow.getGeometryType());
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
		return getIcon(featureRow.getTable().getTableName(),
				featureRow.getId(), null);
	}

	/**
	 * Get the icon of the feature, searching in order: feature geometry type
	 * icon, feature default icon, table geometry type icon, table default icon
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @param geometryType
	 *            geometry type
	 * @return icon row
	 */
	public IconRow getIcon(String featureTable, long featureId,
			GeometryType geometryType) {
		return getIcon(featureTable, featureId, geometryType, true);
	}

	/**
	 * Get the default icon of the feature, searching in order: feature default
	 * icon, table default icon
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @return icon row
	 */
	public IconRow getIconDefault(String featureTable, long featureId) {
		return getIcon(featureTable, featureId, null, true);
	}

	/**
	 * Get the icon of the feature, searching in order: feature geometry type
	 * icon, feature default icon, when tableIcon enabled continue searching:
	 * table geometry type icon, table default icon
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @param geometryType
	 *            geometry type
	 * @param tableIcon
	 *            when true and a feature icon is not found, query for a
	 *            matching table icon
	 * @return icon row
	 */
	public IconRow getIcon(String featureTable, long featureId,
			GeometryType geometryType, boolean tableIcon) {

		IconRow iconRow = null;

		// Feature Icon
		Icons icons = getIcons(featureTable, featureId);
		if (icons != null) {
			iconRow = icons.getIcon(geometryType);
		}

		if (iconRow == null && tableIcon) {

			// Table Icon
			iconRow = getTableIcon(featureTable, geometryType);

		}

		return iconRow;
	}

	/**
	 * Get the default icon of the feature, searching in order: feature default
	 * icon, when tableIcon enabled continue searching: table default icon
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @param tableIcon
	 *            when true and a feature icon is not found, query for a
	 *            matching table icon
	 * @return icon row
	 */
	public IconRow getIconDefault(String featureTable, long featureId,
			boolean tableIcon) {
		return getIcon(featureTable, featureId, null, tableIcon);
	}

	/**
	 * Get the styles for feature id from the style mapping dao
	 * 
	 * @param featureId
	 *            geometry feature id or feature table id
	 * @param mappingDao
	 *            style mapping dao
	 * @return styles
	 */
	private Styles getStyles(long featureId, StyleMappingDao mappingDao) {

		Styles styles = null;

		if (mappingDao != null && geoPackage.isTable(StyleTable.TABLE_NAME)) {

			List<StyleMappingRow> styleMappingRows = mappingDao
					.queryByBaseFeatureId(featureId);
			if (!styleMappingRows.isEmpty()) {

				StyleDao styleDao = getStyleDao();
				for (StyleMappingRow styleMappingRow : styleMappingRows) {

					StyleRow styleRow = styleDao.queryForRow(styleMappingRow);
					if (styleRow != null) {
						if (styles == null) {
							styles = new Styles();
						}
						styles.setStyle(styleRow,
								styleMappingRow.getGeometryType());
					}
				}
			}

		}

		return styles;
	}

	/**
	 * Get the icons for feature id from the icon mapping dao
	 * 
	 * @param featureId
	 *            geometry feature id or feature table id
	 * @param mappingDao
	 *            icon mapping dao
	 * @return icons
	 */
	private Icons getIcons(long featureId, StyleMappingDao mappingDao) {

		Icons icons = null;

		if (mappingDao != null && geoPackage.isTable(IconTable.TABLE_NAME)) {

			List<StyleMappingRow> styleMappingRows = mappingDao
					.queryByBaseFeatureId(featureId);
			if (!styleMappingRows.isEmpty()) {

				IconDao iconDao = getIconDao();
				for (StyleMappingRow styleMappingRow : styleMappingRows) {

					IconRow iconRow = iconDao.queryForRow(styleMappingRow);
					if (iconRow != null) {
						if (icons == null) {
							icons = new Icons();
						}
						icons.setIcon(iconRow,
								styleMappingRow.getGeometryType());
					}
				}
			}

		}

		return icons;
	}

	/**
	 * Set the feature table default feature styles
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureStyles
	 *            default feature styles
	 */
	public void setTableFeatureStyle(FeatureTable featureTable,
			FeatureStyles featureStyles) {
		setTableFeatureStyles(featureTable.getTableName(), featureStyles);
	}

	/**
	 * Set the feature table default feature styles
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureStyles
	 *            default feature styles
	 */
	public void setTableFeatureStyles(String featureTable,
			FeatureStyles featureStyles) {
		if (featureStyles != null) {
			setTableStyles(featureTable, featureStyles.getStyles());
			setTableIcons(featureTable, featureStyles.getIcons());
		} else {
			deleteTableFeatureStyles(featureTable);
		}
	}

	/**
	 * Set the feature table default styles
	 * 
	 * @param featureTable
	 *            feature table
	 * @param styles
	 *            default styles
	 */
	public void setTableStyles(FeatureTable featureTable, Styles styles) {
		setTableStyles(featureTable.getTableName(), styles);
	}

	/**
	 * Set the feature table default styles
	 * 
	 * @param featureTable
	 *            feature table
	 * @param styles
	 *            default styles
	 */
	public void setTableStyles(String featureTable, Styles styles) {

		deleteTableStyles(featureTable);

		if (styles != null) {

			if (styles.getDefaultStyle() != null) {
				setTableStyleDefault(featureTable, styles.getDefaultStyle());
			}

			for (Entry<GeometryType, StyleRow> style : styles.getStyles()
					.entrySet()) {
				setTableStyle(featureTable, style.getKey(), style.getValue());
			}

		}
	}

	/**
	 * Set the feature table style default
	 * 
	 * @param featureTable
	 *            feature table
	 * @param style
	 *            style row
	 */
	public void setTableStyleDefault(FeatureTable featureTable, StyleRow style) {
		setTableStyleDefault(featureTable.getTableName(), style);
	}

	/**
	 * Set the feature table style default
	 * 
	 * @param featureTable
	 *            feature table
	 * @param style
	 *            style row
	 */
	public void setTableStyleDefault(String featureTable, StyleRow style) {
		setTableStyle(featureTable, null, style);
	}

	/**
	 * Set the feature table style for the geometry type
	 * 
	 * @param featureTable
	 *            feature table
	 * @param geometryType
	 *            geometry type
	 * @param style
	 *            style row
	 */
	public void setTableStyle(FeatureTable featureTable,
			GeometryType geometryType, StyleRow style) {
		setTableStyle(featureTable.getTableName(), geometryType, style);
	}

	/**
	 * Set the feature table style for the geometry type
	 * 
	 * @param featureTable
	 *            feature table
	 * @param geometryType
	 *            geometry type
	 * @param style
	 *            style row
	 */
	public void setTableStyle(String featureTable, GeometryType geometryType,
			StyleRow style) {

		deleteTableStyle(featureTable, geometryType);

		if (style != null) {

			// TODO

		}

	}

	/**
	 * Set the feature table default icons
	 * 
	 * @param featureTable
	 *            feature table
	 * @param icons
	 *            default icons
	 */
	public void setTableIcon(FeatureTable featureTable, Icons icons) {
		setTableIcons(featureTable.getTableName(), icons);
	}

	/**
	 * Set the feature table default icons
	 * 
	 * @param featureTable
	 *            feature table
	 * @param icons
	 *            default icons
	 */
	public void setTableIcons(String featureTable, Icons icons) {

		deleteTableIcons(featureTable);

		if (icons != null) {

			if (icons.getDefaultIcon() != null) {
				setTableIconDefault(featureTable, icons.getDefaultIcon());
			}

			for (Entry<GeometryType, IconRow> icon : icons.getIcons()
					.entrySet()) {
				setTableIcon(featureTable, icon.getKey(), icon.getValue());
			}

		}

	}

	/**
	 * Set the feature table icon default
	 * 
	 * @param featureTable
	 *            feature table
	 * @param icon
	 *            icon row
	 */
	public void setTableIconDefault(FeatureTable featureTable, IconRow icon) {
		setTableIconDefault(featureTable.getTableName(), icon);
	}

	/**
	 * Set the feature table icon default
	 * 
	 * @param featureTable
	 *            feature table
	 * @param icon
	 *            icon row
	 */
	public void setTableIconDefault(String featureTable, IconRow icon) {
		setTableIcon(featureTable, null, icon);
	}

	/**
	 * Set the feature table icon for the geometry type
	 * 
	 * @param featureTable
	 *            feature table
	 * @param geometryType
	 *            geometry type
	 * @param icon
	 *            icon row
	 */
	public void setTableIcon(FeatureTable featureTable,
			GeometryType geometryType, IconRow icon) {
		setTableIcon(featureTable.getTableName(), geometryType, icon);
	}

	/**
	 * Set the feature table icon for the geometry type
	 * 
	 * @param featureTable
	 *            feature table
	 * @param geometryType
	 *            geometry type
	 * @param icon
	 *            icon row
	 */
	public void setTableIcon(String featureTable, GeometryType geometryType,
			IconRow icon) {

		deleteTableIcon(featureTable, geometryType);

		if (icon != null) {

			// TODO

		}
	}

	/**
	 * Delete the feature table feature styles
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteTableFeatureStyles(FeatureTable featureTable) {
		deleteTableFeatureStyles(featureTable.getTableName());
	}

	/**
	 * Delete the feature table feature styles
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteTableFeatureStyles(String featureTable) {
		deleteTableStyles(featureTable);
		deleteTableIcons(featureTable);
	}

	/**
	 * Delete the feature table styles
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteTableStyles(FeatureTable featureTable) {
		deleteTableStyles(featureTable.getTableName());
	}

	/**
	 * Delete the feature table styles
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteTableStyles(String featureTable) {
		// TODO
	}

	/**
	 * Delete the feature table default style
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteTableStyleDefault(FeatureTable featureTable) {
		deleteTableStyleDefault(featureTable.getTableName());
	}

	/**
	 * Delete the feature table default style
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteTableStyleDefault(String featureTable) {
		deleteTableStyle(featureTable, null);
	}

	/**
	 * Delete the feature table style for the geometry type
	 * 
	 * @param featureTable
	 *            feature table
	 * @param geometryType
	 *            geometry type
	 */
	public void deleteTableStyle(FeatureTable featureTable,
			GeometryType geometryType) {
		deleteTableStyle(featureTable.getTableName(), geometryType);
	}

	/**
	 * Delete the feature table style for the geometry type
	 * 
	 * @param featureTable
	 *            feature table
	 * @param geometryType
	 *            geometry type
	 */
	public void deleteTableStyle(String featureTable, GeometryType geometryType) {
		// TODO
	}

	/**
	 * Delete the feature table icons
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteTableIcons(FeatureTable featureTable) {
		deleteTableIcons(featureTable.getTableName());
	}

	/**
	 * Delete the feature table icons
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteTableIcons(String featureTable) {
		// TODO
	}

	/**
	 * Delete the feature table default icon
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteTableIconDefault(FeatureTable featureTable) {
		deleteTableIconDefault(featureTable.getTableName());
	}

	/**
	 * Delete the feature table default icon
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteTableIconDefault(String featureTable) {
		deleteTableIcon(featureTable, null);
	}

	/**
	 * Delete the feature table icon for the geometry type
	 * 
	 * @param featureTable
	 *            feature table
	 * @param geometryType
	 *            geometry type
	 */
	public void deleteTableIcon(FeatureTable featureTable,
			GeometryType geometryType) {
		deleteTableIcon(featureTable.getTableName(), geometryType);
	}

	/**
	 * Delete the feature table icon for the geometry type
	 * 
	 * @param featureTable
	 *            feature table
	 * @param geometryType
	 *            geometry type
	 */
	public void deleteTableIcon(String featureTable, GeometryType geometryType) {
		// TODO
	}

}
