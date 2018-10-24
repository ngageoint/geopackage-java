package mil.nga.geopackage.extension.style;

import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.contents.ContentsId;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;

/**
 * Feature Style extension for styling features
 * 
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureStyle extends FeatureCoreStyle {

	private final String TABLE_MAPPING_STYLE = EXTENSION_AUTHOR + "_style_";

	private final String TABLE_MAPPING_STYLE_DEFAULT = EXTENSION_AUTHOR
			+ "_style_default_";

	private final String TABLE_MAPPING_ICON = EXTENSION_AUTHOR + "_icon_";

	private final String TABLE_MAPPING_ICON_DEFAULT = EXTENSION_AUTHOR
			+ "_icon_default_";

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
	public FeatureStyle(GeoPackage geoPackage) {
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

	public void createStyleRelationship(String featureTable) {
		createStyleRelationship(TABLE_MAPPING_STYLE, featureTable, false, true);
	}

	public void createStyleDefaultRelationship(String featureTable) {
		createStyleRelationship(TABLE_MAPPING_STYLE_DEFAULT, featureTable,
				true, true);
	}

	public void createIconRelationship(String featureTable) {
		createStyleRelationship(TABLE_MAPPING_ICON, featureTable, false, false);
	}

	public void createIconDefaultRelationship(String featureTable) {
		createStyleRelationship(TABLE_MAPPING_ICON_DEFAULT, featureTable, true,
				false);
	}

	private void createStyleRelationship(String tablePrefix,
			String featureTable, boolean featureDefault, boolean style) {

		String baseTableName = null;
		if (featureDefault) {
			if (!contentsId.has()) {
				contentsId.getOrCreateExtension();
			}
			baseTableName = ContentsId.TABLE_NAME;
		} else {
			baseTableName = featureTable;
		}

		StyleMappingTable mappingTable = new StyleMappingTable(tablePrefix
				+ featureTable);

		if (style) {
			relatedTables.addSimpleAttributesRelationship(baseTableName,
					new StyleTable(), mappingTable);
		} else {
			relatedTables.addMediaRelationship(baseTableName, new IconTable(),
					mappingTable);
		}

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
	 * Get the default style for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return default style or null
	 */
	public Style getDefaultStyle(String featureTable) {

		Style style = null;

		Long featureId = contentsId.getId(featureTable);
		if (featureId != null) {

			StyleMappingDao styleDefaultMapping = getStyleDefaultMappingDao(featureTable);
			StyleMappingDao iconDefaultMapping = getIconDefaultMappingDao(featureTable);

			style = getStyle(featureId, styleDefaultMapping, iconDefaultMapping);
		}

		return style;
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

		StyleMappingDao styleMapping = getStyleMappingDao(featureTable);
		StyleMappingDao iconMapping = getIconMappingDao(featureTable);

		Style style = getStyle(featureId, styleMapping, iconMapping);

		return style;
	}

	/**
	 * Get the style for feature id from the style and icon mapping daos
	 * 
	 * @param featureId
	 *            geometry feature id or feature table id
	 * @param styleMapping
	 *            style mapping dao
	 * @param iconMapping
	 *            icon mapping dao
	 * @return style
	 */
	private Style getStyle(long featureId, StyleMappingDao styleMapping,
			StyleMappingDao iconMapping) {

		Style style = null;

		// Query for styles
		if (styleMapping != null && geoPackage.isTable(StyleTable.TABLE_NAME)) {

			List<StyleMappingRow> styleMappingRows = styleMapping
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

		// Query for icons
		if (iconMapping != null && geoPackage.isTable(IconTable.TABLE_NAME)) {

			List<StyleMappingRow> styleMappingRows = iconMapping
					.queryByBaseFeatureId(featureId);
			if (!styleMappingRows.isEmpty()) {

				IconDao iconDao = getIconDao();
				for (StyleMappingRow styleMappingRow : styleMappingRows) {

					IconRow iconRow = iconDao.queryForRow(styleMappingRow);
					if (iconRow != null) {
						if (style == null) {
							style = new Style();
						}
						style.setIcon(iconRow,
								styleMappingRow.getGeometryType());
					}
				}
			}

		}

		return style;
	}
}
