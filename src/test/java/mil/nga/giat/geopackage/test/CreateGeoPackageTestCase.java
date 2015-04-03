package mil.nga.giat.geopackage.test;

import java.io.File;

import mil.nga.giat.geopackage.GeoPackage;

import org.junit.After;

/**
 * Abstract Test Case for Created GeoPackages
 * 
 * @author osbornb
 */
public abstract class CreateGeoPackageTestCase extends GeoPackageTestCase {

	/**
	 * Constructor
	 */
	public CreateGeoPackageTestCase() {

	}

	@Override
	protected GeoPackage getGeoPackage() throws Exception {
		File testFolder = folder.newFolder();
		return TestSetupTeardown.setUpCreate(testFolder, true, true);
	}

	@After
	public void tearDown() throws Exception {

		// Tear down the create database
		TestSetupTeardown.tearDownCreate(geoPackage);
	}

}
