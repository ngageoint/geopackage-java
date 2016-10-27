package mil.nga.geopackage.test.extension.elevation;

import mil.nga.geopackage.extension.elevation.ElevationTilesAlgorithm;
import mil.nga.geopackage.test.CreateElevationTilesGeoPackageTestCase;

import org.junit.Test;

/**
 * Elevation Tiles Extensions Tests from a created GeoPackage
 * 
 * @author osbornb
 */
public class ElevationTilesPngCreateTest extends
		CreateElevationTilesGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ElevationTilesPngCreateTest() {
		super(true);
	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Nearest Neighbor Algorithm
	 */
	@Test
	public void testElevationsNearestNeighbor() throws Exception {

		ElevationTilesPngTestUtils.testElevations(geoPackage, elevationTileValues,
				ElevationTilesAlgorithm.NEAREST_NEIGHBOR, allowNulls);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bilinear Algorithm
	 */
	@Test
	public void testElevationsBilinear() throws Exception {

		ElevationTilesPngTestUtils.testElevations(geoPackage, elevationTileValues,
				ElevationTilesAlgorithm.BILINEAR, allowNulls);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bicubic Algorithm
	 */
	@Test
	public void testElevationsBicubic() throws Exception {

		ElevationTilesPngTestUtils.testElevations(geoPackage, elevationTileValues,
				ElevationTilesAlgorithm.BICUBIC, allowNulls);

	}

	/**
	 * Test a random bounding box using the Nearest Neighbor Algorithm
	 */
	@Test
	public void testRandomBoundingBoxNearestNeighbor() throws Exception {

		ElevationTilesPngTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.NEAREST_NEIGHBOR,
				allowNulls);

	}

	/**
	 * Test a random bounding box using the Bilinear Algorithm
	 */
	@Test
	public void testRandomBoundingBoxBilinear() throws Exception {

		ElevationTilesPngTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.BILINEAR,
				allowNulls);

	}

	/**
	 * Test a random bounding box using the Bicubic Algorithm
	 */
	@Test
	public void testRandomBoundingBoxBicubic() throws Exception {

		ElevationTilesPngTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.BICUBIC,
				allowNulls);

	}

}
