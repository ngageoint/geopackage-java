package mil.nga.geopackage.test;

import java.io.File;
import java.io.IOException;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.io.GeoPackageIOUtils;

import org.junit.After;

/**
 * Abstract Test Case for Imported Tiled Gridded Coverage Data GeoPackages
 * 
 * @author osbornb
 */
public abstract class ImportCoverageDataGeoPackageTestCase extends
		GeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ImportCoverageDataGeoPackageTestCase() {

	}

	@Override
	protected GeoPackage getGeoPackage() throws Exception {
		File testFolder = folder.newFolder();
		return setUpImportCoverageData(testFolder);
	}

	/**
	 * Set up the import coverage data database
	 * 
	 * @param directory
	 * @return
	 */
	private GeoPackage setUpImportCoverageData(File directory) {

		// Open
		GeoPackage geoPackage = GeoPackageManager
				.open(copyImportCoverageDataDbFile(directory));
		if (geoPackage == null) {
			throw new GeoPackageException("Failed to open database");
		}

		return geoPackage;
	}

	/**
	 * Get the import coverage data db file copied to the provided directory
	 * 
	 * @param directory
	 * @return
	 */
	private File copyImportCoverageDataDbFile(File directory) {

		File file = TestUtils.getImportDbCoverageDataFile();

		File newFile = new File(directory, file.getName());
		try {
			GeoPackageIOUtils.copyFile(file, newFile);
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed to copy GeoPackage to test directory. File: "
							+ file.getAbsolutePath() + ", Test Directory: "
							+ directory.getAbsolutePath(), e);
		}
		return newFile;
	}

	@After
	public void tearDown() throws Exception {

		// Close
		if (geoPackage != null) {
			geoPackage.close();
		}

	}

}
