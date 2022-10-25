package mil.nga.geopackage.dgiwg;

import java.io.File;

import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.extension.CrsWktExtension;
import mil.nga.geopackage.extension.CrsWktExtensionVersion;

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
	 * @param metadata
	 *            metadata
	 * @return created file
	 */
	public static GeoPackageFile create(File file, String metadata) {
		return create(file, DGIWGConstants.DMF_DEFAULT_URI, metadata);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @param uri
	 *            URI
	 * @param metadata
	 *            metadata
	 * @return created file
	 */
	public static GeoPackageFile create(File file, String uri,
			String metadata) {
		return create(file, uri, metadata, true);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @param metadata
	 *            metadata
	 * @param validate
	 *            validate the file extension and name
	 * @return created file
	 */
	public static GeoPackageFile create(File file, String metadata,
			boolean validate) {
		return create(file, DGIWGConstants.DMF_DEFAULT_URI, metadata, validate);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @param uri
	 *            URI
	 * @param metadata
	 *            metadata
	 * @param validate
	 *            validate the file extension and name
	 * @return created file
	 */
	public static GeoPackageFile create(File file, String uri, String metadata,
			boolean validate) {
		GeoPackageFileName fileName = new GeoPackageFileName(file);
		return createFile(file, fileName, uri, metadata, validate);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param name
	 *            GeoPackage file name
	 * @param directory
	 *            base directory
	 * @param metadata
	 *            metadata
	 * @return created file
	 */
	public static GeoPackageFile create(String name, File directory,
			String metadata) {
		return create(name, directory, DGIWGConstants.DMF_DEFAULT_URI,
				metadata);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param name
	 *            GeoPackage file name
	 * @param directory
	 *            base directory
	 * @param uri
	 *            URI
	 * @param metadata
	 *            metadata
	 * @return created file
	 */
	public static GeoPackageFile create(String name, File directory, String uri,
			String metadata) {
		return create(name, directory, uri, metadata, true);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param name
	 *            GeoPackage file name
	 * @param directory
	 *            base directory
	 * @param metadata
	 *            metadata
	 * @param validate
	 *            validate the file extension and name
	 * @return created file
	 */
	public static GeoPackageFile create(String name, File directory,
			String metadata, boolean validate) {
		return create(name, directory, DGIWGConstants.DMF_DEFAULT_URI, metadata,
				validate);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param name
	 *            GeoPackage file name
	 * @param directory
	 *            base directory
	 * @param uri
	 *            URI
	 * @param metadata
	 *            metadata
	 * @param validate
	 *            validate the file extension and name
	 * @return created file
	 */
	public static GeoPackageFile create(String name, File directory, String uri,
			String metadata, boolean validate) {
		return create(new File(directory, name), uri, metadata, validate);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param directory
	 *            base directory
	 * @param fileName
	 *            DGIWG file name
	 * @param metadata
	 *            metadata
	 * @return created file
	 */
	public static GeoPackageFile create(File directory,
			GeoPackageFileName fileName, String metadata) {
		return create(directory, fileName, DGIWGConstants.DMF_DEFAULT_URI,
				metadata);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param directory
	 *            base directory
	 * @param fileName
	 *            DGIWG file name
	 * @param uri
	 *            URI
	 * @param metadata
	 *            metadata
	 * @return created file
	 */
	public static GeoPackageFile create(File directory,
			GeoPackageFileName fileName, String uri, String metadata) {
		return create(directory, fileName, uri, metadata, true);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param directory
	 *            base directory
	 * @param fileName
	 *            DGIWG file name
	 * @param metadata
	 *            metadata
	 * @param validate
	 *            validate the file extension and name
	 * @return created file
	 */
	public static GeoPackageFile create(File directory,
			GeoPackageFileName fileName, String metadata, boolean validate) {
		return create(directory, fileName, DGIWGConstants.DMF_DEFAULT_URI,
				metadata, validate);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param directory
	 *            base directory
	 * @param fileName
	 *            DGIWG file name
	 * @param uri
	 *            URI
	 * @param metadata
	 *            metadata
	 * @param validate
	 *            validate the file extension and name
	 * @return created file
	 */
	public static GeoPackageFile create(File directory,
			GeoPackageFileName fileName, String uri, String metadata,
			boolean validate) {
		File file = new File(directory, fileName.getNameWithExtension());
		return createFile(file, fileName, uri, metadata, validate);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param fileName
	 *            DGIWG file name
	 * @param metadata
	 *            metadata
	 * @return created file
	 */
	public static GeoPackageFile create(GeoPackageFileName fileName,
			String metadata) {
		return create(fileName, DGIWGConstants.DMF_DEFAULT_URI, metadata);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param fileName
	 *            DGIWG file name
	 * @param uri
	 *            URI
	 * @param metadata
	 *            metadata
	 * @return created file
	 */
	public static GeoPackageFile create(GeoPackageFileName fileName, String uri,
			String metadata) {
		return create(fileName, uri, metadata, true);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param fileName
	 *            DGIWG file name
	 * @param metadata
	 *            metadata
	 * @param validate
	 *            validate the file extension and name
	 * @return created file
	 */
	public static GeoPackageFile create(GeoPackageFileName fileName,
			String metadata, boolean validate) {
		return create(fileName, DGIWGConstants.DMF_DEFAULT_URI, metadata,
				validate);
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param fileName
	 *            DGIWG file name
	 * @param uri
	 *            URI
	 * @param metadata
	 *            metadata
	 * @param validate
	 *            validate the file extension and name
	 * @return created file
	 */
	public static GeoPackageFile create(GeoPackageFileName fileName, String uri,
			String metadata, boolean validate) {
		return createFile(fileName.getFile(), fileName, uri, metadata,
				validate);
	}

	/**
	 * Create the DGIWG GeoPackage file
	 * 
	 * @param file
	 *            file
	 * @param fileName
	 *            file name
	 * @param uri
	 *            URI
	 * @param metadata
	 *            metadata
	 * @param validate
	 *            validate the file extension and name
	 * @return
	 */
	private static GeoPackageFile createFile(File file,
			GeoPackageFileName fileName, String uri, String metadata,
			boolean validate) {

		file = GeoPackageManager.create(file, validate);

		DGIWGGeoPackage geoPackage = open(file, false);
		try {

			CrsWktExtension wktExtension = new CrsWktExtension(geoPackage);
			wktExtension.getOrCreate(CrsWktExtensionVersion.V_1);

			geoPackage.createGeoPackageDatasetMetadata(uri, metadata);

		} finally {
			geoPackage.close();
		}

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
