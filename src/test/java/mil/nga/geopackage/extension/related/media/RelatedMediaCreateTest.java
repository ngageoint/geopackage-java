package mil.nga.geopackage.extension.related.media;

import java.sql.SQLException;

import org.junit.Test;

import mil.nga.geopackage.CreateGeoPackageTestCase;

/**
 * Test Related Media Tables from a created database
 * 
 * @author osbornb
 */
public class RelatedMediaCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public RelatedMediaCreateTest() {

	}

	/**
	 * Test related media tables
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testMedia() throws Exception {

		RelatedMediaUtils.testMedia(geoPackage);

	}

}
