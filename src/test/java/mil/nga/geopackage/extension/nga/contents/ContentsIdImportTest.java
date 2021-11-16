package mil.nga.geopackage.extension.nga.contents;

import java.sql.SQLException;

import org.junit.Test;

import mil.nga.geopackage.ImportGeoPackageTestCase;

/**
 * Test Contents Id from an imported database
 * 
 * @author osbornb
 */
public class ContentsIdImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ContentsIdImportTest() {

	}

	/**
	 * Test contents id
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testContentsId() throws SQLException {

		ContentsIdUtils.testContentsId(geoPackage);

	}

}
