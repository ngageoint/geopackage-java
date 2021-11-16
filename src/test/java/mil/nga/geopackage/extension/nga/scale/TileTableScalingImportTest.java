package mil.nga.geopackage.extension.nga.scale;

import java.sql.SQLException;

import org.junit.Test;

import mil.nga.geopackage.ImportGeoPackageTestCase;

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
