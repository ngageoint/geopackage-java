package mil.nga.geopackage.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import mil.nga.geopackage.BaseTestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.TestConstants;
import mil.nga.geopackage.TestUtils;
import mil.nga.geopackage.io.GeoPackageIOUtils;

/**
 * Test GeoPackage Manager methods
 * 
 * @author osbornb
 */
public class GeoPackageManagerTest extends BaseTestCase {

	/**
	 * Constructor
	 */
	public GeoPackageManagerTest() {

	}

	@Before
	public void setUp() {
	}

	/**
	 * Test creating and opening a database
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateOpen() throws IOException {

		File testFolder = folder.newFolder();
		File dbFile = new File(testFolder, TestConstants.TEST_DB_FILE_NAME);

		// Create
		GeoPackageManager.create(dbFile);
		assertTrue("Database does not exist", dbFile.exists());

		// Open
		GeoPackage geoPackage = GeoPackageManager.open(dbFile);
		assertNotNull("Failed to open database", geoPackage);
		geoPackage.close();

	}

	/**
	 * Test opening a database
	 * 
	 * @throws IOException
	 */
	@Test
	public void testOpen() throws IOException {

		File dbFile = TestUtils.getImportDbFile();

		assertTrue("Database does not exist", dbFile.exists());

		// Open
		GeoPackage geoPackage = GeoPackageManager.open(dbFile);
		assertNotNull("Failed to open database", geoPackage);
		geoPackage.close();
	}

	/**
	 * Test opening a database
	 * 
	 * @throws IOException
	 */
	@Test
	public void testOpenCorrupt() throws IOException {

		File dbFile = TestUtils.getImportDbCorruptFile();

		assertTrue("Database does not exist", dbFile.exists());

		// Open
		try {
			GeoPackageManager.open(dbFile);
			fail("Corrupt file did not fail");
		} catch (Exception e) {
			// Expected
		}
	}

	/**
	 * Test create attempt with no file extension
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateNoExtension() throws IOException {

		File testFolder = folder.newFolder();
		File dbFile = new File(testFolder, TestConstants.TEST_DB_NAME);

		// Create
		GeoPackageManager.create(dbFile);
		assertTrue("Database does not exist",
				GeoPackageIOUtils
						.addFileExtension(dbFile, GeoPackageConstants.EXTENSION)
						.exists());

		// Open
		GeoPackage geoPackage = GeoPackageManager.open(dbFile);
		assertNotNull("Failed to open database", geoPackage);
		geoPackage.close();
	}

	/**
	 * Test create attempt with invalid extension
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateInvalidExtension() throws IOException {

		File testFolder = folder.newFolder();
		File dbFile = new File(testFolder,
				TestConstants.TEST_DB_FILE_NAME + "a");

		try {
			GeoPackageManager.create(dbFile);
			fail("Invalid extension did not fail");
		} catch (Exception e) {
			// Expected
		}
	}

	/**
	 * Test create attempt when the GeoPackage file already exists
	 * 
	 * @throws IOException
	 */
	@Test
	public void testCreateFileExists() throws IOException {

		File testFolder = folder.newFolder();
		File dbFile = new File(testFolder, TestConstants.TEST_DB_FILE_NAME);

		// Create
		GeoPackageManager.create(dbFile);
		assertTrue("Database does not exist", dbFile.exists());

		try {
			GeoPackageManager.create(dbFile);
			fail("Existing file did not fail");
		} catch (Exception e) {
			// Expected
		}

		dbFile.delete();

		// Create
		dbFile = GeoPackageManager.create(testFolder,
				TestConstants.TEST_DB_FILE_NAME);
		assertTrue("Database does not exist", dbFile.exists());

		try {
			GeoPackageManager.create(testFolder,
					TestConstants.TEST_DB_FILE_NAME);
			fail("Existing file did not fail");
		} catch (Exception e) {
			// Expected
		}
	}

	/**
	 * Test open attempt with no file extension
	 * 
	 * @throws IOException
	 */
	@Test
	public void testOpenNoExtension() throws IOException {

		File testFolder = folder.newFolder();
		File dbFile = new File(testFolder, TestConstants.TEST_DB_NAME);

		try {
			GeoPackageManager.open(dbFile);
			fail("No extension did not fail");
		} catch (Exception e) {
			// Expected
		}
	}

	/**
	 * Test open attempt with invalid extension
	 * 
	 * @throws IOException
	 */
	@Test
	public void testOpenInvalidExtension() throws IOException {

		File testFolder = folder.newFolder();
		File dbFile = new File(testFolder,
				TestConstants.TEST_DB_FILE_NAME + "a");

		try {
			GeoPackageManager.open(dbFile);
			fail("Invalid extension did not fail");
		} catch (Exception e) {
			// Expected
		}
	}

	/**
	 * Test open attempt with a nonexistent file
	 * 
	 * @throws IOException
	 */
	@Test
	public void testOpenFileDoesNotExist() throws IOException {

		File testFolder = folder.newFolder();
		File dbFile = new File(testFolder, TestConstants.TEST_DB_FILE_NAME);

		try {
			GeoPackageManager.open(dbFile);
			fail("Nonexistent file did not fail");
		} catch (Exception e) {
			// Expected
		}
	}

	/**
	 * Test the memory footprint when repeatedly opening and closing a
	 * GeoPackage
	 * 
	 * @throws IOException
	 */
	@Test
	public void memoryTest() throws IOException {

		File testFolder = folder.newFolder();
		File dbFile = new File(testFolder, TestConstants.TEST_DB_FILE_NAME);

		GeoPackageManager.create(dbFile);
		assertTrue("Database does not exist", dbFile.exists());

		// Try to garbage collect
		System.gc();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		// Track current memory usage
		long initialMemory = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();

		for (int i = 0; i < 5000; i++) {
			GeoPackage geoPackage = GeoPackageManager.open(dbFile);
			geoPackage.close();
		}

		// Try to garbage collect
		System.gc();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		long currentMemory = Runtime.getRuntime().totalMemory()
				- Runtime.getRuntime().freeMemory();

		// Less than 10 times initial memory
		assertTrue(currentMemory <= 10 * initialMemory);
	}

}
