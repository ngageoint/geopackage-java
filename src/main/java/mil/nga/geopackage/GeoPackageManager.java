package mil.nga.geopackage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
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
		// Change the ORMLite log level
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
	}

	/**
	 * Create a GeoPackage
	 * 
	 * @param file
	 *            file
	 * @return true if created
	 */
	public static boolean create(File file) {

		boolean created = false;

		// Validate or add the file extension
		if (GeoPackageIOUtils.hasFileExtension(file)) {
			GeoPackageValidate.validateGeoPackageExtension(file);
		} else {
			file = GeoPackageIOUtils.addFileExtension(file,
					GeoPackageConstants.EXTENSION);
		}

		if (file.exists()) {
			throw new GeoPackageException(
					"GeoPackage already exists: " + file.getAbsolutePath());
		} else {
			// Create the GeoPackage Connection
			GeoPackageConnection connection = connect(file);

			// Set the GeoPackage application id and user version
			connection.setApplicationId();
			connection.setUserVersion();

			// Create the minimum required tables
			GeoPackageTableCreator tableCreator = new GeoPackageTableCreator(
					connection);
			tableCreator.createRequired();

			connection.close();
			created = true;
		}

		return created;
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
	 * @since 3.3.0
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
	 * @since 3.0.2
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
	 * @since 3.3.0
	 */
	public static GeoPackage open(String name, File file, boolean validate) {

		// Validate or add the file extension
		if (validate) {
			if (GeoPackageIOUtils.hasFileExtension(file)) {
				GeoPackageValidate.validateGeoPackageExtension(file);
			} else {
				file = GeoPackageIOUtils.addFileExtension(file,
						GeoPackageConstants.EXTENSION);
			}
		}

		// Create the GeoPackage Connection and table creator
		GeoPackageConnection connection = connect(file);

		// Create a GeoPackage
		GeoPackage geoPackage = new GeoPackageImpl(name, file, connection);

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
