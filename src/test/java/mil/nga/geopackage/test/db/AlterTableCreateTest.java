package mil.nga.geopackage.test.db;

import java.sql.SQLException;

import org.junit.Test;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

/**
 * Alter Table Create Test
 * 
 * @author osbornb
 */
public class AlterTableCreateTest extends CreateGeoPackageTestCase {

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

}
