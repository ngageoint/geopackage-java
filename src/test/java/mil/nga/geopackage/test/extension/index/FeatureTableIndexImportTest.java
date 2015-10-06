package mil.nga.geopackage.test.extension.index;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Feature Table Index from an imported database
 * 
 * @author osbornb
 */
public class FeatureTableIndexImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public FeatureTableIndexImportTest() {

	}

	/**
	 * Test index
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testIndex() throws SQLException {

		FeatureTableIndexUtils.testIndex(geoPackage);

	}

}
