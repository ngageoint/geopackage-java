package mil.nga.geopackage.db;

import java.sql.SQLException;

import org.junit.Test;

import mil.nga.geopackage.ImportGeoPackageTestCase;

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

	/**
	 * Test copy feature table
	 * 
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testCopyFeatureTable() throws SQLException {
		AlterTableUtils.testCopyFeatureTable(geoPackage);
	}

	/**
	 * Test copy tile table
	 * 
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testCopyTileTable() throws SQLException {
		AlterTableUtils.testCopyTileTable(geoPackage);
	}

	/**
	 * Test copy attributes table
	 * 
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testCopyAttributesTable() throws SQLException {
		AlterTableUtils.testCopyAttributesTable(geoPackage);
	}

	/**
	 * Test copy user table
	 * 
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testCopyUserTable() throws SQLException {
		AlterTableUtils.testCopyUserTable(geoPackage);
	}

}
