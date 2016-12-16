package mil.nga.geopackage.manager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.GeoPackageTableCreator;
import mil.nga.geopackage.validate.GeoPackageValidate;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;

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
	 * @return true if created
	 */
	public static boolean create(File file) {

		boolean created = false;

		// Validate the file extension
		GeoPackageValidate.validateGeoPackageExtension(file);

		if (file.exists()) {
			throw new GeoPackageException("GeoPackage already exists: "
					+ file.getAbsolutePath());
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
	 * @return GeoPackage
	 */
	public static GeoPackage open(File file) {

		// Validate the file extension
		GeoPackageValidate.validateGeoPackageExtension(file);

		// Create the GeoPackage Connection and table creator
		GeoPackageConnection connection = connect(file);
		GeoPackageTableCreator tableCreator = new GeoPackageTableCreator(
				connection);

		// Create a GeoPackage
		GeoPackage geoPackage = new GeoPackageImpl(file, connection,
				tableCreator);

		// Validate the GeoPackage has the minimum required tables
		try {
			GeoPackageValidate.validateMinimumTables(geoPackage);
		} catch (RuntimeException e) {
			geoPackage.close();
			throw e;
		}

		return geoPackage;
	}

	/**
	 * Connect to a GeoPackage file
	 * 
	 * @param file
	 * @return
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
							+ file.getAbsolutePath(), e);
		}

		ConnectionSource connectionSource;
		try {
			connectionSource = new JdbcConnectionSource(databaseUrl);
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get connection source to the SQLite file: "
							+ file.getAbsolutePath(), e);
		}

		// Create the GeoPackage Connection and table creator
		GeoPackageConnection connection = new GeoPackageConnection(file,
				databaseConnection, connectionSource);

		return connection;
	}
}
