package mil.nga.geopackage.features.index;

import java.sql.SQLException;

import org.junit.Test;

import mil.nga.geopackage.CreateGeoPackageTestCase;

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
	 * {@inheritDoc}
	 */
	@Override
	public boolean allowEmptyFeatures() {
		return false;
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
	 * Test index chunk
	 *
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testIndexChunk() throws SQLException {

		FeatureIndexManagerUtils.testIndexChunk(geoPackage);

	}

	/**
	 * Test index pagination
	 *
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testIndexPagination() throws SQLException {

		FeatureIndexManagerUtils.testIndexPagination(geoPackage);

	}

	/**
	 * Test large index
	 *
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testLargeIndex() throws SQLException {

		FeatureIndexManagerUtils.testLargeIndex(geoPackage, 30000, false);

	}

	/**
	 * Test timed index
	 *
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testTimedIndex() throws SQLException {

		FeatureIndexManagerUtils.testTimedIndex(geoPackage, false, false);

	}

}
