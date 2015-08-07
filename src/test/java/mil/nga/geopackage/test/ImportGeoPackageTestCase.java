package mil.nga.geopackage.test;

import java.io.File;

import mil.nga.geopackage.GeoPackage;

import org.junit.After;

/**
 * Abstract Test Case for Imported GeoPackages
 * 
 * @author osbornb
 */
public abstract class ImportGeoPackageTestCase extends GeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ImportGeoPackageTestCase() {

	}

	@Override
	protected GeoPackage getGeoPackage() throws Exception {
		File testFolder = folder.newFolder();
		return TestSetupTeardown.setUpImport(testFolder);
	}

	@After
	public void tearDown() throws Exception {

		// Tear down the import database
		TestSetupTeardown.tearDownImport(geoPackage);

	}

}
