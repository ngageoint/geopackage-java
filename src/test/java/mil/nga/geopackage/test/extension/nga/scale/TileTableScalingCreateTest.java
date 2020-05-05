package mil.nga.geopackage.test.extension.nga.scale;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Tile Table Scaling from a created database
 * 
 * @author osbornb
 */
public class TileTableScalingCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public TileTableScalingCreateTest() {

	}

	/**
	 * Test scaling
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testScaling() throws SQLException {

		TileTableScalingUtils.testScaling(geoPackage);

	}

}
