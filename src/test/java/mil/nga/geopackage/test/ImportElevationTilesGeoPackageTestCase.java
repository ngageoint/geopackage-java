package mil.nga.geopackage.test;

import java.io.File;
import java.io.IOException;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.manager.GeoPackageManager;

import org.junit.After;

/**
 * Abstract Test Case for Imported Elevation Tiles GeoPackages
 * 
 * @author osbornb
 */
public abstract class ImportElevationTilesGeoPackageTestCase extends
		GeoPackageTestCase {

	/**
	 * Constructor
	 */
	public ImportElevationTilesGeoPackageTestCase() {

	}

	@Override
	protected GeoPackage getGeoPackage() throws Exception {
		File testFolder = folder.newFolder();
		return setUpImportElevationTiles(testFolder);
	}

	/**
	 * Set up the import elevation database
	 * 
	 * @param directory
	 * @return
	 */
	private GeoPackage setUpImportElevationTiles(File directory) {

		// Open
		GeoPackage geoPackage = GeoPackageManager
				.open(copyImportElevationTilesDbFile(directory));
		if (geoPackage == null) {
			throw new GeoPackageException("Failed to open database");
		}

		return geoPackage;
	}

	/**
	 * Get the import elevation db file copied to the provided directory
	 * 
	 * @param directory
	 * @return
	 */
	private File copyImportElevationTilesDbFile(File directory) {

		File file = TestUtils.getImportDbElevationTilesFile();

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
