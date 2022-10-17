package mil.nga.geopackage.dgiwg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import mil.nga.geopackage.BaseTestCase;
import mil.nga.geopackage.GeoPackageManager;

/**
 * Test DGIWG GeoPackage Manager methods
 * 
 * @author osbornb
 */
public class DGIWGGeoPackageManagerTest extends BaseTestCase {

	/**
	 * Test file name
	 */
	public static final String FILE_NAME = "AGC_BUCK_Ft-Bliss_14-20_v1-0_29AUG2016";

	/**
	 * Non informative Test file name
	 */
	public static final String FILE_NAME_NON_INFORMATIVE = "NonInformativeName";

	/**
	 * Non informative Test file name
	 */
	public static final String FILE_NAME_NON_INFORMATIVE2 = "Non-Informative_Name";

	/**
	 * Constructor
	 */
	public DGIWGGeoPackageManagerTest() {

	}

	/**
	 * Test creating and opening a database
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateOpenInformative() throws IOException {
		testCreateOpen(FILE_NAME, true);
	}

	/**
	 * Test creating and opening a database
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateOpenInformative2() throws IOException {
		testCreateOpen2(FILE_NAME, true);
	}

	/**
	 * Test creating and opening a database
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateOpenNonInformative() throws IOException {
		testCreateOpen(FILE_NAME_NON_INFORMATIVE, false);
	}

	/**
	 * Test creating and opening a database
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateOpenNonInformative2() throws IOException {
		testCreateOpen2(FILE_NAME_NON_INFORMATIVE, false);
	}

	/**
	 * Test creating and opening a database
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateOpenNonInformative3() throws IOException {
		testCreateOpen(FILE_NAME_NON_INFORMATIVE2, false);
	}

	/**
	 * Test creating and opening a database
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateOpenNonInformative4() throws IOException {
		testCreateOpen2(FILE_NAME_NON_INFORMATIVE2, false);
	}

	/**
	 * Test creating and opening a database
	 * 
	 * @param name
	 *            file name
	 * @param informative
	 *            expected complete informative file name
	 * @throws IOException
	 *             upon error
	 */
	private void testCreateOpen(String name, boolean informative)
			throws IOException {

		File testFolder = folder.newFolder();
		File dbFile = new File(testFolder,
				GeoPackageManager.addExtension(name));

		// Create
		GeoPackageFile file = DGIWGGeoPackageManager.create(dbFile,
				DGIWGGeoPackageTest.getMetadata());
		assertEquals(dbFile, file.getFile());
		assertEquals(GeoPackageManager.addExtension(name),
				file.getFile().getName());
		assertEquals(informative, file.getFileName().isInformative());
		assertEquals(name, file.getFileName().getName());
		assertTrue("Database does not exist", dbFile.exists());

		// Open
		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager.open(dbFile);
		assertNotNull("Failed to open database", geoPackage);
		assertEquals(GeoPackageManager.addExtension(name),
				geoPackage.getName());
		assertEquals(informative, geoPackage.getFileName().isInformative());
		assertEquals(name, geoPackage.getFileName().getName());
		geoPackage.close();

	}

	/**
	 * Test creating and opening a database
	 * 
	 * @param name
	 *            file name
	 * @param informative
	 *            expected complete informative file name
	 * @throws IOException
	 *             upon error
	 */
	private void testCreateOpen2(String name, boolean informative)
			throws IOException {

		File testFolder = folder.newFolder();

		GeoPackageFileName fileName = new GeoPackageFileName(name);
		File dbFile = new File(testFolder,
				GeoPackageManager.addExtension(fileName.toString()));

		// Create
		GeoPackageFile file = DGIWGGeoPackageManager.create(testFolder,
				fileName, DGIWGGeoPackageTest.getMetadata());
		assertEquals(dbFile, file.getFile());
		assertEquals(GeoPackageManager.addExtension(name),
				file.getFile().getName());
		assertEquals(informative, file.getFileName().isInformative());
		assertEquals(name, file.getFileName().toString());
		assertTrue("Database does not exist", dbFile.exists());

		// Open
		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager.open(file);
		assertNotNull("Failed to open database", geoPackage);
		assertEquals(GeoPackageManager.addExtension(name),
				geoPackage.getName());
		assertEquals(informative, geoPackage.getFileName().isInformative());
		assertEquals(name, geoPackage.getFileName().toString());
		geoPackage.close();

	}

}
