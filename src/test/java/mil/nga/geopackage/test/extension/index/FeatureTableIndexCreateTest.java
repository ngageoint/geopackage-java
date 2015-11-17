package mil.nga.geopackage.test.extension.index;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Feature Table Index from a created database
 * 
 * @author osbornb
 */
public class FeatureTableIndexCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public FeatureTableIndexCreateTest() {

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

	@Override
	public boolean allowEmptyFeatures() {
		return false;
	}

}
