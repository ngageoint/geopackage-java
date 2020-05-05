package mil.nga.geopackage.test.extension.nga.style;

import java.io.IOException;
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
	 * @throws IOException
	 */
	@Test
	public void testFeatureStyles() throws SQLException, IOException {

		FeatureStylesUtils.testFeatureStyles(geoPackage);

	}

}
