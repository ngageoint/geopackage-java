package mil.nga.geopackage.test.extension.style;

import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.style.FeatureTableStyles;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.sf.GeometryType;

public class FeatureStylesUtils {

	/**
	 * Test Feature Styles extension
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testFeatureStyles(GeoPackage geoPackage)
			throws SQLException {

		List<String> featureTables = geoPackage.getFeatureTables();

		if (!featureTables.isEmpty()) {

			for (String tableName : featureTables) {

				FeatureDao featureDao = geoPackage.getFeatureDao(tableName);

				FeatureTableStyles featureTableStyles = new FeatureTableStyles(
						geoPackage, featureDao.getTable());

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

			}

		}

	}

}
