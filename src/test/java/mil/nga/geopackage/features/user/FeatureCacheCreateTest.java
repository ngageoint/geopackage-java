package mil.nga.geopackage.features.user;

import java.sql.SQLException;

import org.junit.Test;

import mil.nga.geopackage.CreateGeoPackageTestCase;

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
