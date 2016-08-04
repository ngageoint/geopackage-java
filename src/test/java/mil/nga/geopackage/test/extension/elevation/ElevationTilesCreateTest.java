package mil.nga.geopackage.test.extension.elevation;

import mil.nga.geopackage.test.CreateElevationTilesGeoPackageTestCase;

import org.junit.Test;

/**
 * Elevation Tiles Extensions Tests
 * 
 * @author osbornb
 */
public class ElevationTilesCreateTest extends
		CreateElevationTilesGeoPackageTestCase {

	/**
	 * Test the Extension creation
	 */
	@Test
	public void testExtension() throws Exception {

		ElevationTilesTestUtils.testElevations(geoPackage, elevationTileValues);

	}

}
