package mil.nga.geopackage;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.ConnectionSource;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageTableCreator;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.validate.GeoPackageValidate;

/**
 * GeoPackage Manager used to create and open GeoPackages
 * 
 * @author osbornb
 */
public class GeoPackageManager {

	static {
		// Change the ORMLite log backend
		Logger.setGlobalLogLevel(Level.ERROR);
		if (LogBackendType.JAVA_UTIL.isAvailable()) {
			LoggerFactory.setLogBackendType(LogBackendType.JAVA_UTIL);
		}
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @return created file
	 * @since 4.0.0
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
	 * @since 4.0.0
	 */
	public static File create(File file, boolean validate) {

		boolean isGeoPackage = true;

		// Validate or add the file extension
		if (GeoPackageIOUtils.hasFileExtension(file)) {
			if (validate) {
				GeoPackageValidate.validateGeoPackageExtension(file);
			} else {
				isGeoPackage = GeoPackageValidate.hasGeoPackageExtension(file);
			}
		} else {
			file = addExtension(file);
		}

		if (file.exists()) {
			throw new GeoPackageException(
					"File already exists: " + file.getAbsolutePath());
		}

		// Create the file connection
		GeoPackageConnection connection = connect(file);

		try {

			// Set GeoPackage values and create required tables
			if (isGeoPackage) {

				// Set the GeoPackage application id and user version
				connection.setApplicationId();
				connection.setUserVersion();

				// Create the minimum required tables
				GeoPackageTableCreator tableCreator = new GeoPackageTableCreator(
						connection);
				tableCreator.createRequired();

			}

		} finally {
			connection.close();
		}

		return file;
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param directory
	 *            base directory
	 * @param name
	 *            GeoPackage file name
	 * @return created file
	 * @since 6.1.2
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
	 * @since 6.1.2
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
		return open(true, file);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param writable
	 *            true if writable
	 * @param file
	 *            file
	 * @return GeoPackage
	 * @since 6.3.1
	 */
	public static GeoPackage open(boolean writable, File file) {
		return open(writable, file, true);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @param validate
	 *            validate the GeoPackage
	 * @return GeoPackage
	 * @since 3.3.0
	 */
	public static GeoPackage open(File file, boolean validate) {
		return open(true, file, validate);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param writable
	 *            true if writable
	 * @param file
	 *            file
	 * @param validate
	 *            validate the GeoPackage
	 * @return GeoPackage
	 * @since 6.3.1
	 */
	public static GeoPackage open(boolean writable, File file,
			boolean validate) {
		return open(file.getName(), writable, file, validate);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param name
	 *            GeoPackage name
	 * @param file
	 *            GeoPackage file
	 * @return GeoPackage
	 * @since 3.0.2
	 */
	public static GeoPackage open(String name, File file) {
		return open(name, true, file);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param name
	 *            GeoPackage name
	 * @param writable
	 *            true if writable
	 * @param file
	 *            GeoPackage file
	 * @return GeoPackage
	 * @since 6.3.1
	 */
	public static GeoPackage open(String name, boolean writable, File file) {
		return open(name, writable, file, true);
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
	 * @since 3.3.0
	 */
	public static GeoPackage open(String name, File file, boolean validate) {
		return open(name, true, file, validate);
	}

	/**
	 * Open a GeoPackage
	 * 
	 * @param name
	 *            GeoPackage name
	 * @param writable
	 *            true if writable
	 * @param file
	 *            GeoPackage file
	 * @param validate
	 *            validate the GeoPackage
	 * @return GeoPackage
	 * @since 6.3.1
	 */
	public static GeoPackage open(String name, boolean writable, File file,
			boolean validate) {

		// Check if the file exists
		File existingFile = existingFile(file);
		if (existingFile == null) {
			throw new GeoPackageException(
					"File not found: " + file.getAbsolutePath());
		}
		file = existingFile;

		// Validate the extension
		if (validate && GeoPackageIOUtils.hasFileExtension(file)) {
			GeoPackageValidate.validateGeoPackageExtension(file);
		}

		// Create the GeoPackage Connection and table creator
		GeoPackageConnection connection = connect(file);

		// Create a GeoPackage
		GeoPackage geoPackage = new GeoPackageImpl(name, file, connection,
				writable);

		// Validate the GeoPackage has the minimum required tables
		if (validate) {
			try {
				GeoPackageValidate.validateMinimumTables(geoPackage);
			} catch (RuntimeException e) {
				geoPackage.close();
				throw e;
			}
		}

		return geoPackage;
	}

	/**
	 * Determine if the file is writable
	 * 
	 * @param file
	 *            GeoPackage file
	 * @return true if writable
	 * @since 6.3.1
	 */
	public static boolean isWritable(File file) {
		return Files.isWritable(file.toPath());
	}

	/**
	 * Add a GeoPackage file extension to the file if it does not already
	 * contain a file extension
	 * 
	 * @param file
	 *            file
	 * @return original file or file with extension
	 * @since 4.0.0
	 */
	public static File addExtension(File file) {
		if (!GeoPackageIOUtils.hasFileExtension(file)) {
			file = GeoPackageIOUtils.addFileExtension(file,
					GeoPackageConstants.EXTENSION);
		}
		return file;
	}

	/**
	 * Add a GeoPackage file extension to the file if it does not already
	 * contain a file extension
	 * 
	 * @param file
	 *            file
	 * @return original file or file with extension
	 * @since 6.1.2
	 */
	public static String addExtension(String file) {
		if (!GeoPackageIOUtils.hasFileExtension(file)) {
			file = GeoPackageIOUtils.addFileExtension(file,
					GeoPackageConstants.EXTENSION);
		}
		return file;
	}

	/**
	 * Check if the file exists in either its' current form or with a GeoPackage
	 * file extension, and return the existing file
	 * 
	 * @param file
	 *            file
	 * @return existing file or null if not file exists
	 * @since 4.0.0
	 */
	public static File existingFile(File file) {

		File existingFile = null;

		if (file.exists()) {
			existingFile = file;
		} else {
			file = addExtension(file);
			if (file.exists()) {
				existingFile = file;
			}
		}

		return existingFile;
	}

	/**
	 * Check if the file exists in either its' current form or with a GeoPackage
	 * file extension
	 * 
	 * @param file
	 *            file
	 * @return true if exists
	 * @since 4.0.0
	 */
	public static boolean exists(File file) {
		return existingFile(file) != null;
	}

	/**
	 * Connect to a GeoPackage file
	 * 
	 * @param file
	 *            GeoPackage file
	 * @return connection
	 */
	private static GeoPackageConnection connect(File file) {

		String databaseUrl = "jdbc:sqlite:" + file.getPath();

		// load the sqlite-JDBC driver using the current class loader
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			throw new GeoPackageException(
					"Failed to load the SQLite JDBC driver", e);
		}

		// create a database connection
		Connection databaseConnection;
		try {
			databaseConnection = DriverManager.getConnection(databaseUrl);
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get connection to the SQLite file: "
							+ file.getAbsolutePath(),
					e);
		}

		ConnectionSource connectionSource;
		try {
			connectionSource = new JdbcConnectionSource(databaseUrl);
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get connection source to the SQLite file: "
							+ file.getAbsolutePath(),
					e);
		}

		// Create the GeoPackage Connection
		GeoPackageConnection connection = new GeoPackageConnection(file,
				databaseConnection, connectionSource);
		connection.enableForeignKeys();

		return connection;
	}

}
