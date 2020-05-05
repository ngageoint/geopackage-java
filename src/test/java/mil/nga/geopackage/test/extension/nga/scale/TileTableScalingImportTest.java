package mil.nga.geopackage.test.extension.nga.scale;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Tile Table Scaling from an imported database
 * 
 * @author osbornb
 */
public class TileTableScalingImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public TileTableScalingImportTest() {

	}

	/**
	 * Test tile scaling
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testScaling() throws SQLException {

		TileTableScalingUtils.testScaling(geoPackage);

	}

}
