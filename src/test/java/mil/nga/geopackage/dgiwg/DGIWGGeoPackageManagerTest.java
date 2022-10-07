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
	public void testCreateOpen() throws IOException {

		File testFolder = folder.newFolder();
		File dbFile = new File(testFolder,
				GeoPackageManager.addExtension(FILE_NAME));

		// Create
		GeoPackageFile file = DGIWGGeoPackageManager.create(dbFile,
				DGIWGGeoPackageTest.getMetadata());
		assertEquals(dbFile, file.getFile());
		assertEquals(FILE_NAME, file.getFileName().toString());
		assertTrue("Database does not exist", dbFile.exists());

		// Open
		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager.open(dbFile);
		assertNotNull("Failed to open database", geoPackage);
		assertEquals(FILE_NAME, geoPackage.getFileName().toString());
		geoPackage.close();

	}

	/**
	 * Test creating and opening a database
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateOpen2() throws IOException {

		File testFolder = folder.newFolder();

		GeoPackageFileName fileName = new GeoPackageFileName(FILE_NAME);
		File dbFile = new File(testFolder,
				GeoPackageManager.addExtension(fileName.toString()));

		// Create
		GeoPackageFile file = DGIWGGeoPackageManager.create(testFolder,
				fileName, DGIWGGeoPackageTest.getMetadata());
		assertEquals(dbFile, file.getFile());
		assertEquals(FILE_NAME, file.getFileName().toString());
		assertTrue("Database does not exist", dbFile.exists());

		// Open
		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager.open(file);
		assertNotNull("Failed to open database", geoPackage);
		assertEquals(FILE_NAME, geoPackage.getFileName().toString());
		geoPackage.close();

	}

}
