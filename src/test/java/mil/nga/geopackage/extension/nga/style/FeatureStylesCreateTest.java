package mil.nga.geopackage.extension.nga.style;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import mil.nga.geopackage.CreateGeoPackageTestCase;

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
	 *             upon error
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testFeatureStyles() throws SQLException, IOException {

		FeatureStylesUtils.testFeatureStyles(geoPackage);

	}

	/**
	 * Test shared feature styles
	 * 
	 * @throws SQLException
	 *             upon error
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testSharedFeatureStyles() throws SQLException, IOException {

		FeatureStylesUtils.testSharedFeatureStyles(geoPackage);

	}

}
