package mil.nga.geopackage.test.extension.elevation;

import mil.nga.geopackage.extension.elevation.ElevationTilesAlgorithm;
import mil.nga.geopackage.test.ImportElevationTilesGeoPackageTestCase;

import org.junit.Test;

/**
 * Elevation Tiles Extensions Tests from an imported GeoPackage
 * 
 * @author osbornb
 */
public class ElevationTilesImportTest extends
		ImportElevationTilesGeoPackageTestCase {

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Nearest Neighbor Algorithm
	 */
	@Test
	public void testElevationsNearestNeighbor() throws Exception {

		ElevationTilesTestUtils.testElevations(geoPackage, null,
				ElevationTilesAlgorithm.NEAREST_NEIGHBOR);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bilinear Algorithm
	 */
	@Test
	public void testElevationsBilinear() throws Exception {

		ElevationTilesTestUtils.testElevations(geoPackage, null,
				ElevationTilesAlgorithm.BILINEAR);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bicubic Algorithm
	 */
	@Test
	public void testElevationsBicubic() throws Exception {

		ElevationTilesTestUtils.testElevations(geoPackage, null,
				ElevationTilesAlgorithm.BICUBIC);

	}

	/**
	 * Test a random bounding box using the Nearest Neighbor Algorithm
	 */
	@Test
	public void testRandomBoundingBoxNearestNeighbor() throws Exception {

		ElevationTilesTestUtils.testRandomBoundingBox(geoPackage, null,
				ElevationTilesAlgorithm.NEAREST_NEIGHBOR, true);

	}

	/**
	 * Test a random bounding box using the Bilinear Algorithm
	 */
	@Test
	public void testRandomBoundingBoxBilinear() throws Exception {

		ElevationTilesTestUtils.testRandomBoundingBox(geoPackage, null,
				ElevationTilesAlgorithm.BILINEAR, true);

	}

	/**
	 * Test a random bounding box using the Bicubic Algorithm
	 */
	@Test
	public void testRandomBoundingBoxBicubic() throws Exception {

		ElevationTilesTestUtils.testRandomBoundingBox(geoPackage, null,
				ElevationTilesAlgorithm.BICUBIC, true);

	}

}
