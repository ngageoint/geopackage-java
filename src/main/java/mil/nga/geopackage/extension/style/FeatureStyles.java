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

			Style style = getDefaultStyle(featureTable, id);
			Icon icon = getDefaultIcon(featureTable, id);

			if (style != null || icon != null) {
				featureStyle = new FeatureStyle(style, icon);
			}

		}

		return featureStyle;
	}

	/**
	 * Get the default style for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return default style or null
	 */
	public Style getDefaultStyle(FeatureTable featureTable) {
		return getDefaultStyle(featureTable.getTableName());
	}

	/**
	 * Get the default style for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return default style or null
	 */
	public Style getDefaultStyle(String featureTable) {
		Style style = null;
		Long id = contentsId.getId(featureTable);
		if (id != null) {
			style = getDefaultStyle(featureTable, id);
		}
		return style;
	}

	/**
	 * Get the default style for the feature table and contents id
	 * 
	 * @param featureTable
	 *            feature table
	 * @param contentsId
	 *            contents id
	 * @return default style or null
	 */
	private Style getDefaultStyle(String featureTable, long contentsId) {
		return getStyle(contentsId, getStyleDefaultMappingDao(featureTable));
	}

	/**
	 * Get the default icon for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return default icon or null
	 */
	public Icon getDefaultIcon(FeatureTable featureTable) {
		return getDefaultIcon(featureTable.getTableName());
	}

	/**
	 * Get the default icon for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return default icon or null
	 */
	public Icon getDefaultIcon(String featureTable) {
		Icon icon = null;
		Long id = contentsId.getId(featureTable);
		if (id != null) {
			icon = getDefaultIcon(featureTable, id);
		}
		return icon;
	}

	/**
	 * Get the default icon for the feature table and contents id
	 * 
	 * @param featureTable
	 *            feature table
	 * @param contentsId
	 *            contents id
	 * @return default icon or null
	 */
	private Icon getDefaultIcon(String featureTable, long contentsId) {
		return getIcon(contentsId, getIconDefaultMappingDao(featureTable));
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

		Style style = getStyle(featureTable, featureId);
		Icon icon = getIcon(featureTable, featureId);

		FeatureStyle featureStyle = null;
		if (style != null || icon != null) {
			featureStyle = new FeatureStyle(style, icon);
		}

		return featureStyle;
	}

	/**
	 * Get the style for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return style or null
	 */
	public Style getStyle(FeatureRow featureRow) {
		return getStyle(featureRow.getTable().getTableName(),
				featureRow.getId());
	}

	/**
	 * Get the style for the feature table and feature id
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @return style or null
	 */
	public Style getStyle(String featureTable, long featureId) {
		return getStyle(featureId, getStyleMappingDao(featureTable));
	}

	/**
	 * Get the icon for the feature row
	 * 
	 * @param featureRow
	 *            feature row
	 * @return icon or null
	 */
	public Icon getIcon(FeatureRow featureRow) {
		return getIcon(featureRow.getTable().getTableName(), featureRow.getId());
	}

	/**
	 * Get the icon for the feature table and feature id
	 * 
	 * @param featureTable
	 *            feature table
	 * @param featureId
	 *            feature id
	 * @return icon or null
	 */
	public Icon getIcon(String featureTable, long featureId) {
		return getIcon(featureId, getIconMappingDao(featureTable));
	}

	/**
	 * Get the style for feature id from the style and icon mapping daos
	 * 
	 * @param featureId
	 *            geometry feature id or feature table id
	 * @param mappingDao
	 *            style mapping dao
	 * @return style
	 */
	private Style getStyle(long featureId, StyleMappingDao mappingDao) {

		Style style = null;

		if (mappingDao != null && geoPackage.isTable(StyleTable.TABLE_NAME)) {

			List<StyleMappingRow> styleMappingRows = mappingDao
					.queryByBaseFeatureId(featureId);
			if (!styleMappingRows.isEmpty()) {

				StyleDao styleDao = getStyleDao();
				for (StyleMappingRow styleMappingRow : styleMappingRows) {

					StyleRow styleRow = styleDao.queryForRow(styleMappingRow);
					if (styleRow != null) {
						if (style == null) {
							style = new Style();
						}
						style.setStyle(styleRow,
								styleMappingRow.getGeometryType());
					}
				}
			}

		}

		return style;
	}

	/**
	 * Get the style for feature id from the style and icon mapping daos
	 * 
	 * @param featureId
	 *            geometry feature id or feature table id
	 * @param mappingDao
	 *            icon mapping dao
	 * @return style
	 */
	private Icon getIcon(long featureId, StyleMappingDao mappingDao) {

		Icon icon = null;

		if (mappingDao != null && geoPackage.isTable(IconTable.TABLE_NAME)) {

			List<StyleMappingRow> styleMappingRows = mappingDao
					.queryByBaseFeatureId(featureId);
			if (!styleMappingRows.isEmpty()) {

				IconDao iconDao = getIconDao();
				for (StyleMappingRow styleMappingRow : styleMappingRows) {

					IconRow iconRow = iconDao.queryForRow(styleMappingRow);
					if (iconRow != null) {
						if (icon == null) {
							icon = new Icon();
						}
						icon.setIcon(iconRow, styleMappingRow.getGeometryType());
					}
				}
			}

		}

		return icon;
	}

}
