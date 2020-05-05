package mil.nga.geopackage.test.extension.nga.style;

import java.io.IOException;
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
	 * @throws IOException
	 */
	@Test
	public void testFeatureStyles() throws SQLException, IOException {

		FeatureStylesUtils.testFeatureStyles(geoPackage);

	}

}
