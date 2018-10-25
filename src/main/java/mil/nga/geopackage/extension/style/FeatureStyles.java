package mil.nga.geopackage.extension.style;

import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;

/**
 * Feature Style extension for styling features
 * 
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureStyles extends FeatureCoreStyles {

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
	public FeatureStyles(GeoPackage geoPackage) {
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
	 * Get the default feature style for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return default feature style or null
	 */
	public FeatureStyle getDefaultFeatureStyle(FeatureTable featureTable) {
		return getDefaultFeatureStyle(featureTable.getTableName());
	}

	/**
	 * Get the default feature style for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return default feature style or null
	 */
	public FeatureStyle getDefaultFeatureStyle(String featureTable) {

		FeatureStyle featureStyle = null;

		Long id = contentsId.getId(featureTable);
		if (id != null) {

			Styles styles = getDefaultStyles(featureTable, id);
			Icons icons = getDefaultIcons(featureTable, id);

			if (styles != null || icons != null) {
				featureStyle = new FeatureStyle(styles, icons);
			}

		}

		return featureStyle;
	}

	/**
	 * Get the default styles for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return default styles or null
	 */
	public Styles getDefaultStyles(FeatureTable featureTable) {
		return getDefaultStyles(featureTable.getTableName());
	}

	/**
	 * Get the default styles for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return default styles or null
	 */
	public Styles getDefaultStyles(String featureTable) {
		Styles styles = null;
		Long id = contentsId.getId(featureTable);
		if (id != null) {
			styles = getDefaultStyles(featureTable, id);
		}
		return styles;
	}

	/**
	 * Get the default styles for the feature table and contents id
	 * 
	 * @param featureTable
	 *            feature table
	 * @param contentsId
	 *            contents id
	 * @return default styles or null
	 */
	private Styles getDefaultStyles(String featureTable, long contentsId) {
		return getStyles(contentsId, getStyleDefaultMappingDao(featureTable));
	}

	/**
	 * Get the default icons for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return default icons or null
	 */
	public Icons getDefaultIcons(FeatureTable featureTable) {
		return getDefaultIcons(featureTable.getTableName());
	}

	/**
	 * Get the default icons for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return default icons or null
	 */
	public Icons getDefaultIcons(String featureTable) {
		Icons icons = null;
		Long id = contentsId.getId(featureTable);
		if (id != null) {
			icons = getDefaultIcons(featureTable, id);
		}
		return icons;
	}

	/**
	 * Get the default icons for the feature table and contents id
	 * 
	 * @param featureTable
	 *            feature table
	 * @param contentsId
	 *            contents id
	 * @return default icons or null
	 */
	private Icons getDefaultIcons(String featureTable, long contentsId) {
		return getIcons(contentsId, getIconDefaultMappingDao(featureTable));
	}

	/**
	 * Get the feature style for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return feature style or null
	 */
	public FeatureStyle getFeatureStyle(FeatureRow featureRow) {
		return getFeatureStyle(featureRow.getTable().getTableName(),
				featureRow.getId());
	}

	/**
	 * Get the feature style for the feature table and feature id
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @return feature style or null
	 */
	public FeatureStyle getFeatureStyle(String featureTable, long featureId) {

		Styles styles = getStyles(featureTable, featureId);
		Icons icons = getIcons(featureTable, featureId);

		FeatureStyle featureStyle = null;
		if (styles != null || icons != null) {
			featureStyle = new FeatureStyle(styles, icons);
		}

		return featureStyle;
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
	 * Set the default feature style for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureStyle
	 *            default feature style
	 */
	public void setDefaultFeatureStyle(FeatureTable featureTable,
			FeatureStyle featureStyle) {
		setDefaultFeatureStyle(featureTable.getTableName(), featureStyle);
	}

	/**
	 * Set the default feature style for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureStyle
	 *            default feature style
	 */
	public void setDefaultFeatureStyle(String featureTable,
			FeatureStyle featureStyle) {
		if (featureStyle != null) {
			setDefaultStyles(featureTable, featureStyle.getStyles());
			setDefaultIcons(featureTable, featureStyle.getIcons());
		} else {
			deleteDefaultFeatureStyle(featureTable);
		}
	}

	/**
	 * Set the default styles for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @param styles
	 *            default styles
	 */
	public void setDefaultStyles(FeatureTable featureTable, Styles styles) {
		setDefaultStyles(featureTable.getTableName(), styles);
	}

	/**
	 * Set the default styles for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @param styles
	 *            default styles
	 */
	public void setDefaultStyles(String featureTable, Styles styles) {

		deleteDefaultStyles(featureTable);

		if (styles != null) {

			if (styles.getDefaultStyle() != null) {
				// TODO
			}

			// TODO
		}
	}

	/**
	 * Set the default icons for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @param icons
	 *            default icons
	 */
	public void setDefaultIcon(FeatureTable featureTable, Icons icons) {
		setDefaultIcons(featureTable.getTableName(), icons);
	}

	/**
	 * Set the default icons for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @param icons
	 *            default icons
	 */
	public void setDefaultIcons(String featureTable, Icons icons) {

		deleteDefaultIcons(featureTable);

		if (icons != null) {
			// TODO
		}
	}

	/**
	 * Delete the default feature style for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteDefaultFeatureStyle(FeatureTable featureTable) {
		deleteDefaultFeatureStyle(featureTable.getTableName());
	}

	/**
	 * Delete the default feature style for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteDefaultFeatureStyle(String featureTable) {
		deleteDefaultStyles(featureTable);
		deleteDefaultIcons(featureTable);
	}

	/**
	 * Delete the default styles for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteDefaultStyles(FeatureTable featureTable) {
		deleteDefaultStyles(featureTable.getTableName());
	}

	/**
	 * Delete the default styles for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteDefaultStyles(String featureTable) {
		// TODO
	}

	/**
	 * Delete the default icons for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteDefaultIcons(FeatureTable featureTable) {
		deleteDefaultIcons(featureTable.getTableName());
	}

	/**
	 * Delete the default icons for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 */
	public void deleteDefaultIcons(String featureTable) {
		// TODO
	}

}
