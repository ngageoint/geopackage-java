package mil.nga.geopackage.features.index;

import java.sql.SQLException;

import org.junit.Test;

import mil.nga.geopackage.ImportGeoPackageTestCase;

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
	 * Test large index
	 *
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testLargeIndex() throws SQLException {

		FeatureIndexManagerUtils.testLargeIndex(geoPackage, 20000, false);

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
