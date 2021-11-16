package mil.nga.geopackage.extension.related;

import java.sql.SQLException;

import org.junit.Test;

import mil.nga.geopackage.CreateGeoPackageTestCase;

/**
 * Test Related Tiles Tables from a created database
 * 
 * @author osbornb
 */
public class RelatedTilesCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public RelatedTilesCreateTest() {

	}

	/**
	 * Test related tiles tables
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testTiles() throws Exception {

		RelatedTilesUtils.testTiles(geoPackage);

	}

}
