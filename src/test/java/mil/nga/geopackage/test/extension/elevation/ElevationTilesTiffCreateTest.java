package mil.nga.geopackage.test.extension.elevation;

import mil.nga.geopackage.extension.elevation.ElevationTilesAlgorithm;
import mil.nga.geopackage.test.CreateElevationTilesTiffGeoPackageTestCase;

import org.junit.Test;

/**
 * Elevation Tiles Tiff Extensions Tests from a created GeoPackage
 * 
 * @author osbornb
 */
public class ElevationTilesTiffCreateTest extends
		CreateElevationTilesTiffGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ElevationTilesTiffCreateTest() {
		super(true);
	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Nearest Neighbor Algorithm
	 */
	@Test
	public void testElevationsNearestNeighbor() throws Exception {

		ElevationTilesTiffTestUtils.testElevations(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.NEAREST_NEIGHBOR,
				allowNulls);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bilinear Algorithm
	 */
	@Test
	public void testElevationsBilinear() throws Exception {

		ElevationTilesTiffTestUtils.testElevations(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.BILINEAR,
				allowNulls);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bicubic Algorithm
	 */
	@Test
	public void testElevationsBicubic() throws Exception {

		ElevationTilesTiffTestUtils.testElevations(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.BICUBIC,
				allowNulls);

	}

	/**
	 * Test a random bounding box using the Nearest Neighbor Algorithm
	 */
	@Test
	public void testRandomBoundingBoxNearestNeighbor() throws Exception {

		ElevationTilesTiffTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.NEAREST_NEIGHBOR,
				allowNulls);

	}

	/**
	 * Test a random bounding box using the Bilinear Algorithm
	 */
	@Test
	public void testRandomBoundingBoxBilinear() throws Exception {

		ElevationTilesTiffTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.BILINEAR,
				allowNulls);

	}

	/**
	 * Test a random bounding box using the Bicubic Algorithm
	 */
	@Test
	public void testRandomBoundingBoxBicubic() throws Exception {

		ElevationTilesTiffTestUtils.testRandomBoundingBox(geoPackage,
				elevationTileValues, ElevationTilesAlgorithm.BICUBIC,
				allowNulls);

	}

}
