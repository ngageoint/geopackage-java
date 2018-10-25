package mil.nga.geopackage.test.extension.style;

import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.style.FeatureTableStyle;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;

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

				FeatureTableStyle featureTableStyle = new FeatureTableStyle(
						geoPackage, featureDao.getTable());

				TestCase.assertNotNull(featureTableStyle.getTableName());
				TestCase.assertEquals(tableName,
						featureTableStyle.getTableName());
				TestCase.assertNotNull(featureTableStyle.getFeatureStyles());

				TestCase.assertNull(featureTableStyle.getDefaultFeatureStyle());
				TestCase.assertNull(featureTableStyle.getDefaultStyle());
				TestCase.assertNull(featureTableStyle.getDefaultIcon());

				FeatureResultSet featureResultSet = featureDao.queryForAll();
				while (featureResultSet.moveToNext()) {
					FeatureRow featureRow = featureResultSet.getRow();

					TestCase.assertNull(featureTableStyle
							.getFeatureStyle(featureRow));
					TestCase.assertNull(featureTableStyle
							.getFeatureStyle(featureRow.getId()));
					TestCase.assertNull(featureTableStyle.getStyle(featureRow));
					TestCase.assertNull(featureTableStyle.getStyle(featureRow
							.getId()));
					TestCase.assertNull(featureTableStyle.getIcon(featureRow));
					TestCase.assertNull(featureTableStyle.getIcon(featureRow
							.getId()));
				}

			}

		}

	}
}
