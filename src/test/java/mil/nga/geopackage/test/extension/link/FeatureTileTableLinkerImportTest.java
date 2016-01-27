package mil.nga.geopackage.test.extension.link;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Feature Tile Table Linker from an imported database
 * 
 * @author osbornb
 */
public class FeatureTileTableLinkerImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public FeatureTileTableLinkerImportTest() {

	}

	/**
	 * Test link
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testLink() throws SQLException {

		FeatureTileTableLinkerUtils.testLink(geoPackage);

	}

}
