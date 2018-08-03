package mil.nga.geopackage.test.features.index;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Feature Index Manager from a created database
 *
 * @author osbornb
 */
public class FeatureIndexManagerCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public FeatureIndexManagerCreateTest() {

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

	/**
	 * Test large index
	 *
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testLargeIndex() throws SQLException {

		FeatureIndexManagerUtils.testLargeIndex(geoPackage);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean allowEmptyFeatures() {
		return false;
	}

}
