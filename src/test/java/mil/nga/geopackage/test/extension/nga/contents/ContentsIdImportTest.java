package mil.nga.geopackage.test.extension.nga.contents;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import org.junit.Test;

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
