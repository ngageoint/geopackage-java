package mil.nga.geopackage.tiles.features;

import java.io.IOException;

import org.junit.Test;

import mil.nga.geopackage.ImportGeoPackageTestCase;

/**
 * Test Feature Preview from an imported database
 * 
 * @author osbornb
 */
public class FeaturePreviewImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public FeaturePreviewImportTest() {

	}

	/**
	 * Test draw
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testDraw() throws IOException {

		FeaturePreviewUtils.testDraw(geoPackage);

	}

}
