package mil.nga.geopackage;

import java.io.File;
import java.io.IOException;

import org.junit.After;

import mil.nga.geopackage.io.GeoPackageIOUtils;

/**
 * Abstract Test Case for Imported TIFF Tiled Gridded Coverage Data GeoPackages
 * 
 * @author osbornb
 */
public abstract class ImportCoverageDataTiffGeoPackageTestCase extends
		GeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ImportCoverageDataTiffGeoPackageTestCase() {

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
				.open(copyImportCoverageDataTiffDbFile(directory));
		if (geoPackage == null) {
			throw new GeoPackageException("Failed to open database");
		}

		return geoPackage;
	}

	/**
	 * Get the import coverage data tiff db file copied to the provided directory
	 * 
	 * @param directory
	 * @return
	 */
	private File copyImportCoverageDataTiffDbFile(File directory) {

		File file = TestUtils.getImportDbCoverageDataTiffFile();

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
