package mil.nga.geopackage.test;

import mil.nga.geopackage.GeoPackage;

import org.junit.Before;

/**
 * Abstract Test Case on a single GeoPackage
 * 
 * @author osbornb
 */
public abstract class GeoPackageTestCase extends BaseTestCase {

	/**
	 * GeoPackage
	 */
	protected GeoPackage geoPackage = null;

	/**
	 * Constructor
	 */
	public GeoPackageTestCase() {

	}

	/**
	 * Get the geo package to test
	 * 
	 * @return
	 */
	protected abstract GeoPackage getGeoPackage() throws Exception;

	@Before
	public void geoPackageSetUp() throws Exception {
		geoPackage = getGeoPackage();
	}

}
