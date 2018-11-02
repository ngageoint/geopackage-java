package mil.nga.geopackage.test.extension.style;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.contents.ContentsId;
import mil.nga.geopackage.extension.style.FeatureStyleExtension;
import mil.nga.geopackage.extension.style.FeatureStyles;
import mil.nga.geopackage.extension.style.FeatureTableStyles;
import mil.nga.geopackage.extension.style.IconRow;
import mil.nga.geopackage.extension.style.IconTable;
import mil.nga.geopackage.extension.style.Icons;
import mil.nga.geopackage.extension.style.StyleRow;
import mil.nga.geopackage.extension.style.StyleTable;
import mil.nga.geopackage.extension.style.Styles;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.test.TestConstants;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.sf.GeometryType;
import mil.nga.sf.util.GeometryUtils;

public class FeatureStylesUtils {

	/**
	 * Test Feature Styles extension
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void testFeatureStyles(GeoPackage geoPackage)
			throws SQLException, IOException {

		List<String> featureTables = geoPackage.getFeatureTables();

		if (!featureTables.isEmpty()) {

			TestCase.assertFalse(geoPackage.isTable(StyleTable.TABLE_NAME));
			TestCase.assertFalse(geoPackage.isTable(IconTable.TABLE_NAME));
			TestCase.assertFalse(geoPackage.isTable(ContentsId.TABLE_NAME));

			for (String tableName : featureTables) {

				FeatureDao featureDao = geoPackage.getFeatureDao(tableName);

				FeatureTableStyles featureTableStyles = new FeatureTableStyles(
						geoPackage, featureDao.getTable());

				GeometryType geometryType = featureDao.getGeometryType();
				Map<GeometryType, Map<GeometryType, ?>> childGeometryTypes = GeometryUtils
						.childHierarchy(geometryType);

				TestCase.assertFalse(featureTableStyles
						.hasTableStyleRelationship());
				TestCase.assertFalse(featureTableStyles.hasStyleRelationship());
				TestCase.assertFalse(featureTableStyles
						.hasTableIconRelationship());
				TestCase.assertFalse(featureTableStyles.hasIconRelationship());

				TestCase.assertNotNull(featureTableStyles.getTableName());
				TestCase.assertEquals(tableName,
						featureTableStyles.getTableName());
				TestCase.assertNotNull(featureTableStyles
						.getFeatureStyleExtension());

				TestCase.assertNull(featureTableStyles.getTableFeatureStyles());
				TestCase.assertNull(featureTableStyles.getTableStyles());
				TestCase.assertNull(featureTableStyles.getCachedTableStyles());
				TestCase.assertNull(featureTableStyles.getTableStyleDefault());
				TestCase.assertNull(featureTableStyles
						.getTableStyle(GeometryType.GEOMETRY));
				TestCase.assertNull(featureTableStyles.getTableIcons());
				TestCase.assertNull(featureTableStyles.getCachedTableIcons());
				TestCase.assertNull(featureTableStyles.getTableIconDefault());
				TestCase.assertNull(featureTableStyles
						.getTableIcon(GeometryType.GEOMETRY));

				FeatureResultSet featureResultSet = featureDao.queryForAll();
				while (featureResultSet.moveToNext()) {
					FeatureRow featureRow = featureResultSet.getRow();

					TestCase.assertNull(featureTableStyles
							.getFeatureStyles(featureRow));
					TestCase.assertNull(featureTableStyles
							.getFeatureStyles(featureRow.getId()));

					TestCase.assertNull(featureTableStyles
							.getFeatureStyle(featureRow));
					TestCase.assertNull(featureTableStyles
							.getFeatureStyleDefault(featureRow));
					TestCase.assertNull(featureTableStyles.getFeatureStyle(
							featureRow.getId(), featureRow.getGeometryType()));
					TestCase.assertNull(featureTableStyles
							.getFeatureStyleDefault(featureRow.getId()));

					TestCase.assertNull(featureTableStyles
							.getStyles(featureRow));
					TestCase.assertNull(featureTableStyles.getStyles(featureRow
							.getId()));

					TestCase.assertNull(featureTableStyles.getStyle(featureRow));
					TestCase.assertNull(featureTableStyles
							.getStyleDefault(featureRow));
					TestCase.assertNull(featureTableStyles.getStyle(
							featureRow.getId(), featureRow.getGeometryType()));
					TestCase.assertNull(featureTableStyles
							.getStyleDefault(featureRow.getId()));

					TestCase.assertNull(featureTableStyles.getIcons(featureRow));
					TestCase.assertNull(featureTableStyles.getIcons(featureRow
							.getId()));

					TestCase.assertNull(featureTableStyles.getIcon(featureRow));
					TestCase.assertNull(featureTableStyles
							.getIconDefault(featureRow));
					TestCase.assertNull(featureTableStyles.getIcon(
							featureRow.getId(), featureRow.getGeometryType()));
					TestCase.assertNull(featureTableStyles
							.getIconDefault(featureRow.getId()));

				}

				// Table Styles
				TestCase.assertFalse(featureTableStyles
						.hasTableStyleRelationship());
				TestCase.assertFalse(geoPackage
						.isTable(featureTableStyles
								.getFeatureStyleExtension()
								.getMappingTableName(
										FeatureStyleExtension.TABLE_MAPPING_TABLE_STYLE,
										tableName)));

				// Add a default table style
				StyleRow tableStyleDefault = randomStyle();
				featureTableStyles.setTableStyleDefault(tableStyleDefault);

				TestCase.assertTrue(featureTableStyles
						.hasTableStyleRelationship());
				TestCase.assertTrue(geoPackage.isTable(StyleTable.TABLE_NAME));
				TestCase.assertTrue(geoPackage.isTable(ContentsId.TABLE_NAME));
				TestCase.assertTrue(geoPackage
						.isTable(featureTableStyles
								.getFeatureStyleExtension()
								.getMappingTableName(
										FeatureStyleExtension.TABLE_MAPPING_TABLE_STYLE,
										tableName)));

				// Add geometry type table styles
				Map<GeometryType, StyleRow> geometryTypeStyles = randomStyles(childGeometryTypes);
				for (Entry<GeometryType, StyleRow> geometryTypeStyle : geometryTypeStyles
						.entrySet()) {
					featureTableStyles.setTableStyle(
							geometryTypeStyle.getKey(),
							geometryTypeStyle.getValue());
				}

				FeatureStyles featureStyles = featureTableStyles
						.getTableFeatureStyles();
				TestCase.assertNotNull(featureStyles);
				TestCase.assertNotNull(featureStyles.getStyles());
				TestCase.assertNull(featureStyles.getIcons());

				Styles tableStyles = featureTableStyles.getTableStyles();
				TestCase.assertNotNull(tableStyles);
				TestCase.assertNotNull(tableStyles.getDefault());
				TestCase.assertEquals(tableStyleDefault.getId(), tableStyles
						.getDefault().getId());
				TestCase.assertEquals(tableStyleDefault.getId(),
						featureTableStyles.getTableStyle(null).getId());
				TestCase.assertEquals(tableStyleDefault.getId(),
						featureTableStyles.getTableStyle(geometryType).getId());
				validateTableStyles(featureTableStyles, tableStyleDefault,
						geometryTypeStyles, childGeometryTypes);

				// Table Icons
				TestCase.assertFalse(featureTableStyles
						.hasTableIconRelationship());
				TestCase.assertFalse(geoPackage.isTable(featureTableStyles
						.getFeatureStyleExtension().getMappingTableName(
								FeatureStyleExtension.TABLE_MAPPING_TABLE_ICON,
								tableName)));

				// Create table icon relationship
				TestCase.assertFalse(featureTableStyles
						.hasTableIconRelationship());
				featureTableStyles.createTableIconRelationship();
				TestCase.assertTrue(featureTableStyles
						.hasTableIconRelationship());

				Icons createTableIcons = new Icons();
				IconRow tableIconDefault = randomIcon();
				createTableIcons.setDefault(tableIconDefault);
				IconRow baseGeometryTypeIcon = randomIcon();
				createTableIcons.setIcon(baseGeometryTypeIcon, geometryType);
				Map<GeometryType, IconRow> geometryTypeIcons = randomIcons(childGeometryTypes);
				for (Entry<GeometryType, IconRow> geometryTypeIcon : geometryTypeIcons
						.entrySet()) {
					createTableIcons.setIcon(geometryTypeIcon.getValue(),
							geometryTypeIcon.getKey());
				}

				// Set the table icons
				featureTableStyles.setTableIcons(createTableIcons);

				TestCase.assertTrue(featureTableStyles
						.hasTableIconRelationship());
				TestCase.assertTrue(geoPackage.isTable(IconTable.TABLE_NAME));
				TestCase.assertTrue(geoPackage.isTable(featureTableStyles
						.getFeatureStyleExtension().getMappingTableName(
								FeatureStyleExtension.TABLE_MAPPING_TABLE_ICON,
								tableName)));

				featureStyles = featureTableStyles.getTableFeatureStyles();
				TestCase.assertNotNull(featureStyles);
				TestCase.assertNotNull(featureStyles.getStyles());
				Icons tableIcons = featureStyles.getIcons();
				TestCase.assertNotNull(tableIcons);

				TestCase.assertNotNull(tableIcons.getDefault());
				TestCase.assertEquals(tableIconDefault.getId(), tableIcons
						.getDefault().getId());
				TestCase.assertEquals(tableIconDefault.getId(),
						featureTableStyles.getTableIcon(null).getId());
				TestCase.assertEquals(baseGeometryTypeIcon.getId(),
						featureTableStyles.getTableIcon(geometryType).getId());
				validateTableIcons(featureTableStyles, baseGeometryTypeIcon,
						geometryTypeIcons, childGeometryTypes);

			}

		}

	}

	private static void validateTableStyles(
			FeatureTableStyles featureTableStyles, StyleRow styleRow,
			Map<GeometryType, StyleRow> geometryTypeStyles,
			Map<GeometryType, Map<GeometryType, ?>> geometryTypes) {

		if (geometryTypes != null) {
			for (Entry<GeometryType, Map<GeometryType, ?>> type : geometryTypes
					.entrySet()) {
				StyleRow typeStyleRow = styleRow;
				if (geometryTypeStyles.containsKey(type.getKey())) {
					typeStyleRow = geometryTypeStyles.get(type.getKey());
				}
				TestCase.assertEquals(typeStyleRow.getId(), featureTableStyles
						.getTableStyle(type.getKey()).getId());
				@SuppressWarnings("unchecked")
				Map<GeometryType, Map<GeometryType, ?>> childGeometryTypes = (Map<GeometryType, Map<GeometryType, ?>>) type
						.getValue();
				validateTableStyles(featureTableStyles, typeStyleRow,
						geometryTypeStyles, childGeometryTypes);
			}
		}
	}

	private static void validateTableIcons(
			FeatureTableStyles featureTableStyles, IconRow iconRow,
			Map<GeometryType, IconRow> geometryTypeIcons,
			Map<GeometryType, Map<GeometryType, ?>> geometryTypes)
			throws IOException {

		if (geometryTypes != null) {
			for (Entry<GeometryType, Map<GeometryType, ?>> type : geometryTypes
					.entrySet()) {
				IconRow typeIconRow = iconRow;
				if (geometryTypeIcons.containsKey(type.getKey())) {
					typeIconRow = geometryTypeIcons.get(type.getKey());
					TestCase.assertTrue(typeIconRow.getId() >= 0);
					TestCase.assertNotNull(typeIconRow.getData());
					TestCase.assertEquals("image/"
							+ TestConstants.ICON_POINT_IMAGE_EXTENSION,
							typeIconRow.getContentType());
					BufferedImage iconImage = ImageUtils.getImage(typeIconRow
							.getData());
					TestCase.assertNotNull(iconImage);
					TestCase.assertTrue(iconImage.getWidth() > 0);
					TestCase.assertTrue(iconImage.getHeight() > 0);
				}
				TestCase.assertEquals(typeIconRow.getId(), featureTableStyles
						.getTableIcon(type.getKey()).getId());
				@SuppressWarnings("unchecked")
				Map<GeometryType, Map<GeometryType, ?>> childGeometryTypes = (Map<GeometryType, Map<GeometryType, ?>>) type
						.getValue();
				validateTableIcons(featureTableStyles, typeIconRow,
						geometryTypeIcons, childGeometryTypes);
			}
		}
	}

	private static StyleRow randomStyle() {
		StyleRow styleRow = new StyleRow();

		if (Math.random() < .5) {
			styleRow.setName("Style Name");
		}
		if (Math.random() < .5) {
			styleRow.setDescription("Style Description");
		}
		if (Math.random() < .5) {
			styleRow.setColor("#0000ff"); // TODO
			if (Math.random() < .75) {
				styleRow.setOpacity(Math.random());
			}
		}
		if (Math.random() < .5) {
			styleRow.setWidth(1.0 + (Math.random() * 3));
		}
		if (Math.random() < .5) {
			styleRow.setFillColor("#f00"); // TODO
			if (Math.random() < .75) {
				styleRow.setFillOpacity(Math.random());
			}
		}

		return styleRow;
	}

	private static Map<GeometryType, StyleRow> randomStyles(
			Map<GeometryType, Map<GeometryType, ?>> geometryTypes) {
		Map<GeometryType, StyleRow> rowMap = new HashMap<>();
		if (geometryTypes != null) {
			for (Entry<GeometryType, Map<GeometryType, ?>> type : geometryTypes
					.entrySet()) {
				if (Math.random() < .5) {
					rowMap.put(type.getKey(), randomStyle());
				}
				@SuppressWarnings("unchecked")
				Map<GeometryType, Map<GeometryType, ?>> childGeometryTypes = (Map<GeometryType, Map<GeometryType, ?>>) type
						.getValue();
				Map<GeometryType, StyleRow> childRowMap = randomStyles(childGeometryTypes);
				rowMap.putAll(childRowMap);
			}
		}
		return rowMap;
	}

	private static IconRow randomIcon() throws IOException {
		IconRow iconRow = new IconRow();

		File iconImageFile = TestUtils
				.getTestFile(TestConstants.ICON_POINT_IMAGE);
		byte[] iconBytes = GeoPackageIOUtils.fileBytes(iconImageFile);
		BufferedImage iconImage = ImageUtils.getImage(iconBytes);

		iconRow.setData(iconBytes);
		iconRow.setContentType("image/"
				+ TestConstants.ICON_POINT_IMAGE_EXTENSION);
		if (Math.random() < .5) {
			iconRow.setName("Icon Name");
		}
		if (Math.random() < .5) {
			iconRow.setDescription("Icon Description");
		}
		if (Math.random() < .5) {
			iconRow.setWidth(Math.random() * iconImage.getWidth());
		}
		if (Math.random() < .5) {
			iconRow.setHeight(Math.random() * iconImage.getHeight());
		}
		if (Math.random() < .5) {
			iconRow.setAnchorU(Math.random());
		}
		if (Math.random() < .5) {
			iconRow.setAnchorV(Math.random());
		}

		return iconRow;
	}

	private static Map<GeometryType, IconRow> randomIcons(
			Map<GeometryType, Map<GeometryType, ?>> geometryTypes)
			throws IOException {
		Map<GeometryType, IconRow> rowMap = new HashMap<>();
		if (geometryTypes != null) {
			for (Entry<GeometryType, Map<GeometryType, ?>> type : geometryTypes
					.entrySet()) {
				if (Math.random() < .5) {
					rowMap.put(type.getKey(), randomIcon());
				}
				@SuppressWarnings("unchecked")
				Map<GeometryType, Map<GeometryType, ?>> childGeometryTypes = (Map<GeometryType, Map<GeometryType, ?>>) type
						.getValue();
				Map<GeometryType, IconRow> childRowMap = randomIcons(childGeometryTypes);
				rowMap.putAll(childRowMap);
			}
		}
		return rowMap;
	}

}
