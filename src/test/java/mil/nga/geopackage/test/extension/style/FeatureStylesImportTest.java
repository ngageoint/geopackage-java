package mil.nga.geopackage.test.extension.style;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Feature Styles from an imported database
 * 
 * @author osbornb
 */
public class FeatureStylesImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public FeatureStylesImportTest() {

	}

	/**
	 * Test feature styles
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testFeatureStyles() throws SQLException {

		FeatureStylesUtils.testFeatureStyles(geoPackage);

	}

}
