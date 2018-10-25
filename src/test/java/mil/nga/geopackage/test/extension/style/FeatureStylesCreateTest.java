package mil.nga.geopackage.test.extension.style;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Feature Styles from a created database
 * 
 * @author osbornb
 */
public class FeatureStylesCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public FeatureStylesCreateTest() {

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
