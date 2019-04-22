package mil.nga.geopackage.test.db;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import org.junit.Test;

/**
 * Alter Table Import Test
 * 
 * @author osbornb
 */
public class AlterTableImportTest extends ImportGeoPackageTestCase {

	/**
	 * Test column alters
	 * 
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testColumns() throws SQLException {
		AlterTableUtils.testColumns(geoPackage);
	}

}
