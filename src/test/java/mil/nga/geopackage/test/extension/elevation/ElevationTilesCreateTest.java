package mil.nga.geopackage.test.extension.elevation;

import mil.nga.geopackage.extension.elevation.ElevationTilesAlgorithm;
import mil.nga.geopackage.test.CreateElevationTilesGeoPackageTestCase;

import org.junit.Test;

/**
 * Elevation Tiles Extensions Tests from a created GeoPackage
 * 
 * @author osbornb
 */
public class ElevationTilesCreateTest extends
		CreateElevationTilesGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ElevationTilesCreateTest() {
		super(true);
	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Nearest Neighbor Algorithm
	 */
	@Test
	public void testElevationsNearestNeighbor() throws Exception {

		ElevationTilesTestUtils.testElevations(geoPackage, elevationTileValues,
				ElevationTilesAlgorithm.NEAREST_NEIGHBOR);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bilinear Algorithm
	 */
	@Test
	public void testElevationsBilinear() throws Exception {

		ElevationTilesTestUtils.testElevations(geoPackage, elevationTileValues,
				ElevationTilesAlgorithm.BILINEAR);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bicubic Algorithm
	 */
	@Test
	public void testElevationsBicubic() throws Exception {

		ElevationTilesTestUtils.testElevations(geoPackage, elevationTileValues,
				ElevationTilesAlgorithm.BICUBIC);

	}

	/**
	 * Test a random bounding box using the Nearest Neighbor Algorithm
	 */
	@Test
	public void testRandomBoundingBoxNearestNeighbor() throws Exception {

		ElevationTilesTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.NEAREST_NEIGHBOR,
				allowNulls);

	}

	/**
	 * Test a random bounding box using the Bilinear Algorithm
	 */
	@Test
	public void testRandomBoundingBoxBilinear() throws Exception {

		ElevationTilesTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.BILINEAR,
				allowNulls);

	}

	/**
	 * Test a random bounding box using the Bicubic Algorithm
	 */
	@Test
	public void testRandomBoundingBoxBicubic() throws Exception {

		ElevationTilesTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.BICUBIC,
				allowNulls);

	}

}
