package mil.nga.geopackage.dgiwg;

import java.io.File;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;

/**
 * DGIWG (Defence Geospatial Information Working Group) GeoPackage Manager used
 * to create and open GeoPackages
 * 
 * @author osbornb
 * @since 6.5.1
 */
public class DGIWGGeoPackageManager {

	/**
	 * Create a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @return created file
	 */
	public static GeoPackageFile create(File file) {
		return create(file, true);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @param validate
	 *            validate the file extension and name
	 * @return created file
	 */
	public static GeoPackageFile create(File file, boolean validate) {
		GeoPackageFileName fileName = new GeoPackageFileName(file);
		return createFile(file, fileName, validate);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param directory
	 *            base directory
	 * @param name
	 *            GeoPackage file name
	 * @return created file
	 */
	public static GeoPackageFile create(File directory, String name) {
		return create(directory, name, true);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param directory
	 *            base directory
	 * @param name
	 *            GeoPackage file name
	 * @param validate
	 *            validate the file extension and name
	 * @return created file
	 */
	public static GeoPackageFile create(File directory, String name,
			boolean validate) {
		return create(new File(directory, name), validate);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param directory
	 *            base directory
	 * @param fileName
	 *            DGIWG file name
	 * @return created file
	 */
	public static GeoPackageFile create(File directory,
			GeoPackageFileName fileName) {
		return create(directory, fileName, true);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param directory
	 *            base directory
	 * @param fileName
	 *            DGIWG file name
	 * @param validate
	 *            validate the file extension and name
	 * @return created file
	 */
	public static GeoPackageFile create(File directory,
			GeoPackageFileName fileName, boolean validate) {
		File file = new File(directory, fileName.toString());
		return createFile(file, fileName, validate);
	}

	/**
	 * Create the DGIWG GeoPackage file
	 * 
	 * @param file
	 *            file
	 * @param fileName
	 *            file name
	 * @param validate
	 *            validate the file extension and name
	 * @return
	 */
	private static GeoPackageFile createFile(File file,
			GeoPackageFileName fileName, boolean validate) {

		if (validate && !fileName.isValid()) {
			throw new GeoPackageException(
					"Not a valid DGIWG file name: " + fileName);
		}

		file = GeoPackageManager.create(file, validate);

		return new GeoPackageFile(file, fileName);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param file
	 *            DGIWG file
	 * @return GeoPackage
	 */
	public static DGIWGGeoPackage open(GeoPackageFile file) {
		return open(file, true);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param file
	 *            DGIWG file
	 * @param validate
	 *            validate the GeoPackage
	 * @return GeoPackage
	 */
	public static DGIWGGeoPackage open(GeoPackageFile file, boolean validate) {
		return open(file.getFile(), validate);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param name
	 *            GeoPackage name
	 * @param file
	 *            DGIWG file
	 * @return GeoPackage
	 */
	public static DGIWGGeoPackage open(String name, GeoPackageFile file) {
		return open(name, file, true);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param name
	 *            GeoPackage name
	 * @param file
	 *            DGIWG file
	 * @param validate
	 *            validate the GeoPackage
	 * @return GeoPackage
	 */
	public static DGIWGGeoPackage open(String name, GeoPackageFile file,
			boolean validate) {
		return open(name, file.getFile(), validate);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @return GeoPackage
	 */
	public static DGIWGGeoPackage open(File file) {
		return open(file, true);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @param validate
	 *            validate the GeoPackage
	 * @return GeoPackage
	 */
	public static DGIWGGeoPackage open(File file, boolean validate) {
		return open(file.getName(), file, validate);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param name
	 *            GeoPackage name
	 * @param file
	 *            GeoPackage file
	 * @return GeoPackage
	 */
	public static DGIWGGeoPackage open(String name, File file) {
		return open(name, file, true);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param name
	 *            GeoPackage name
	 * @param file
	 *            GeoPackage file
	 * @param validate
	 *            validate the GeoPackage, storing any errors
	 * @return GeoPackage
	 */
	public static DGIWGGeoPackage open(String name, File file,
			boolean validate) {

		GeoPackageFileName fileName = new GeoPackageFileName(file);

		if (validate && !fileName.isValid()) {
			throw new GeoPackageException(
					"Not a valid DGIWG file name: " + fileName);
		}

		DGIWGGeoPackage geoPackage = new DGIWGGeoPackage(fileName,
				GeoPackageManager.open(name, file, validate));

		if (validate) {
			validate(geoPackage);
		}

		return geoPackage;
	}

	/**
	 * Is the GeoPackage valid according to the DGIWG GeoPackage Profile
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @return true if valid
	 */
	public static boolean isValid(DGIWGGeoPackage geoPackage) {
		return geoPackage.isValid();
	}

	/**
	 * Validate the GeoPackage against the DGIWG GeoPackage Profile
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @return validation errors
	 */
	public static DGIWGValidationErrors validate(DGIWGGeoPackage geoPackage) {
		return geoPackage.validate();
	}

}
