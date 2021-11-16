package mil.nga.geopackage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.After;

import mil.nga.geopackage.io.GeoPackageIOUtils;

/**
 * Abstract Test Case for loading a GeoPackage for tests
 *
 * @author osbornb
 */
public abstract class LoadGeoPackageTestCase extends GeoPackageTestCase {

	private final String file;

	/**
	 * Constructor
	 */
	public LoadGeoPackageTestCase(String file) {
		this.file = file;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IOException
	 * @throws SQLException
	 */
	@Override
	protected GeoPackage getGeoPackage() throws Exception {
		File testFolder = folder.newFolder();

		File testFile = TestUtils.getTestFile(file);

		File newFile = new File(testFolder, testFile.getName());
		try {
			GeoPackageIOUtils.copyFile(testFile, newFile);
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed to copy GeoPackage to test directory. File: "
							+ testFile.getAbsolutePath() + ", Test Directory: "
							+ testFolder.getAbsolutePath(), e);
		}

		// Open
		GeoPackage geoPackage = GeoPackageManager.open(newFile);
		if (geoPackage == null) {
			throw new GeoPackageException("Failed to open database");
		}

		return geoPackage;
	}

	@After
	public void tearDown() throws Exception {

		// Close
		if (geoPackage != null) {
			geoPackage.close();
		}
	}

}
