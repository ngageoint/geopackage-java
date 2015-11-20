package mil.nga.geopackage.db;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		SQLUtils.execSQL(connection, sql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int delete(String table, String whereClause, String[] whereArgs) {
		return SQLUtils.delete(connection, table, whereClause, whereArgs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int count(String table, String where, String[] args) {
		return SQLUtils.count(connection, table, where, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer min(String table, String column, String where, String[] args) {
		return SQLUtils.min(connection, table, column, where, args);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer max(String table, String column, String where, String[] args) {
		return SQLUtils.max(connection, table, column, where, args);
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
