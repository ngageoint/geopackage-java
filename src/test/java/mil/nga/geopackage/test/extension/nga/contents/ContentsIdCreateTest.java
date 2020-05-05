package mil.nga.geopackage.test.extension.nga.contents;

import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Contents Id from a created database
 * 
 * @author osbornb
 */
public class ContentsIdCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ContentsIdCreateTest() {

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
