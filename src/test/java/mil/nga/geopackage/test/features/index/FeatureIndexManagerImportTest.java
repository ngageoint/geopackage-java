package mil.nga.geopackage.test.features.index;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Feature Index Manager from an imported database
 * 
 * @author osbornb
 */
public class FeatureIndexManagerImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public FeatureIndexManagerImportTest() {

	}

	/**
	 * Test index
	 * 
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testIndex() throws SQLException {

		FeatureIndexManagerUtils.testIndex(geoPackage);

	}

}
