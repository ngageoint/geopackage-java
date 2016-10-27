package mil.nga.geopackage.test;

import java.io.File;

import mil.nga.geopackage.GeoPackage;

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
		return TestSetupTeardown.setUpCreate(testFolder, true,
				allowEmptyFeatures(), true);
	}

	@After
	public void tearDown() throws Exception {

		// Tear down the create database
		TestSetupTeardown.tearDownCreate(geoPackage);
	}

	/**
	 * Return true to allow a chance that a feature will be created with an
	 * empty geometry
	 * 
	 * @return true to allow empty features
	 */
	public boolean allowEmptyFeatures() {
		return true;
	}

}
