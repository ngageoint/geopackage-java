package mil.nga.geopackage.extension.related;

import java.sql.SQLException;

import org.junit.Test;

import mil.nga.geopackage.ImportGeoPackageTestCase;

/**
 * Test Related Tiles Tables from an imported database
 * 
 * @author osbornb
 */
public class RelatedTilesImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public RelatedTilesImportTest() {

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
