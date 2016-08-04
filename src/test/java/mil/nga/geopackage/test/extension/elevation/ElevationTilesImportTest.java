package mil.nga.geopackage.test.extension.elevation;

import mil.nga.geopackage.test.ImportElevationTilesGeoPackageTestCase;

import org.junit.Test;

/**
 * Elevation Tiles Extensions Tests
 * 
 * @author osbornb
 */
public class ElevationTilesImportTest extends
		ImportElevationTilesGeoPackageTestCase {

	/**
	 * Test the Extension creation
	 */
	@Test
	public void testExtension() throws Exception {

		ElevationTilesTestUtils.testElevations(geoPackage, null);

	}

}
