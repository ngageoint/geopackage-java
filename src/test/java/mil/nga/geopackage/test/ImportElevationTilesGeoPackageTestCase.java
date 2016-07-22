package mil.nga.geopackage.test;

import java.io.File;

import mil.nga.geopackage.GeoPackage;

import org.junit.After;

/**
 * Abstract Test Case for Imported Elevation Tiles GeoPackages
 * 
 * @author osbornb
 */
public abstract class ImportElevationTilesGeoPackageTestCase extends
		GeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ImportElevationTilesGeoPackageTestCase() {

	}

	@Override
	protected GeoPackage getGeoPackage() throws Exception {
		File testFolder = folder.newFolder();
		return TestSetupTeardown.setUpImportElevationTiles(testFolder);
	}

	@After
	public void tearDown() throws Exception {

		// Tear down the import database
		TestSetupTeardown.tearDownImportElevationTiles(geoPackage);

	}

}
