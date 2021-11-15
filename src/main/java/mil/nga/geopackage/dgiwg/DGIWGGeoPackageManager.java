package mil.nga.geopackage.dgiwg;

import java.io.File;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;

/**
 * DGIWG (Defence Geospatial Information Working Group) GeoPackage Manager used
 * to create and open GeoPackages
 * 
 * @author osbornb
 * @since 6.1.2
 */
public class DGIWGGeoPackageManager {

	/**
	 * Create a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @return created file
	 */
	public static File create(File file) {
		return create(file, true);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @param validate
	 *            validate the file extension
	 * @return created file
	 */
	public static File create(File file, boolean validate) {

		// TODO

		return GeoPackageManager.create(file, validate);
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
	public static File create(File directory, String name) {
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
	 *            validate the file extension
	 * @return created file
	 */
	public static File create(File directory, String name, boolean validate) {
		return create(new File(directory, name), validate);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @return GeoPackage
	 */
	public static GeoPackage open(File file) {
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
	public static GeoPackage open(File file, boolean validate) {
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
	public static GeoPackage open(String name, File file) {
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
	 *            validate the GeoPackage
	 * @return GeoPackage
	 */
	public static GeoPackage open(String name, File file, boolean validate) {

		if (validate) {
			// TODO
		}

		DGIWGGeoPackage geoPackage = new DGIWGGeoPackage(
				GeoPackageManager.open(name, file, validate));

		if (validate) {
			validate(geoPackage);
		}

		return geoPackage;
	}

	public static boolean isValid(DGIWGGeoPackage geoPackage) {
		return false; // TODO
	}

	public static void validate(DGIWGGeoPackage geoPackage) {
		// TODO
	}

}
