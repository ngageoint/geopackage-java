package mil.nga.giat.geopackage.db;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.giat.geopackage.GeoPackageException;

import com.j256.ormlite.support.ConnectionSource;

/**
 * GeoPackage Connection wrapper
 * 
 * @author osbornb
 */
public class GeoPackageConnection extends GeoPackageCoreConnection {

	/**
	 * Logger
	 */
	private static final Logger log = Logger
			.getLogger(GeoPackageConnection.class.getName());

	/**
	 * GeoPackage file
	 */
	private final File file;

	/**
	 * Connection
	 */
	private final Connection connection;

	/**
	 * Connection source
	 */
	private final ConnectionSource connectionSource;

	/**
	 * Constructor
	 *
	 * @param connection
	 */
	public GeoPackageConnection(File file, Connection connection,
			ConnectionSource connectionSource) {
		this.file = file;
		this.connection = connection;
		this.connectionSource = connectionSource;
	}

	/**
	 * Get the connection
	 *
	 * @return
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ConnectionSource getConnectionSource() {
		return connectionSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execSQL(String sql) {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.execute(sql);
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to execute SQL statement: "
					+ sql, e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					log.log(Level.WARNING, "Failed to close SQL statment: "
							+ sql, e);
				}
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean tableExists(String tableName) {
		boolean exists = false;

		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = connection.createStatement();
			rs = statement
					.executeQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"
							+ tableName + "'");
			exists = rs.next();
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to check if table exists: "
					+ tableName, e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					log.log(Level.WARNING,
							"Failed to close table exists result set: "
									+ tableName, e);
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					log.log(Level.WARNING,
							"Failed to close table exists statement: "
									+ tableName, e);
				}
			}
		}

		return exists;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int delete(String table, String whereClause, String[] whereArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		connectionSource.closeQuietly();
		try {
			connection.close();
		} catch (SQLException e) {
			log.log(Level.WARNING, "Failed to close GeoPackage connection to: "
					+ file.getAbsolutePath(), e);
		}
	}

}
