package mil.nga.geopackage.test.features.user;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Feature Row Cache from a created database
 *
 * @author osbornb
 */
public class FeatureCacheCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public FeatureCacheCreateTest() {

	}

	/**
	 * Test cache
	 *
	 * @throws SQLException
	 */
	@Test
	public void testCache() throws SQLException {

		FeatureCacheUtils.testCache(geoPackage);

	}

}
