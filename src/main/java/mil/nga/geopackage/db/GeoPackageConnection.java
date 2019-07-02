package mil.nga.geopackage.db;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.j256.ormlite.support.ConnectionSource;

import mil.nga.geopackage.GeoPackageException;

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
	 * Auto commit mode at the beginning of a transaction
	 */
	private Boolean autoCommit = null;

	/**
	 * Constructor
	 *
	 * @param file
	 *            file
	 * @param connection
	 *            connection
	 * @param connectionSource
	 *            connection source
	 */
	public GeoPackageConnection(File file, Connection connection,
			ConnectionSource connectionSource) {
		super(connectionSource);
		this.file = file;
		this.connection = connection;
	}

	/**
	 * Get the connection
	 *
	 * @return connection
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
	public void beginTransaction() {
		if (autoCommit != null) {
			throw new GeoPackageException(
					"Failed to begin transaction, previous transaction was not ended");
		}
		autoCommit = SQLUtils.beginTransaction(connection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endTransaction(boolean successful) {
		SQLUtils.endTransaction(connection, successful, autoCommit);
		autoCommit = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void commit() {
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to commit connection", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean inTransaction() {
		return autoCommit != null;
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
	public Integer min(String table, String column, String where,
			String[] args) {
		return SQLUtils.min(connection, table, column, where, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer max(String table, String column, String where,
			String[] args) {
		return SQLUtils.max(connection, table, column, where, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		super.close();
		try {
			connection.close();
		} catch (SQLException e) {
			log.log(Level.WARNING, "Failed to close GeoPackage connection to: "
					+ file.getAbsolutePath(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object querySingleResult(String sql, String[] args, int column,
			GeoPackageDataType dataType) {
		return SQLUtils.querySingleResult(connection, sql, args, column,
				dataType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> querySingleColumnResults(String sql, String[] args,
			int column, GeoPackageDataType dataType, Integer limit) {
		return SQLUtils.querySingleColumnResults(connection, sql, args, column,
				dataType, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<List<Object>> queryResults(String sql, String[] args,
			GeoPackageDataType[] dataTypes, Integer limit) {
		return SQLUtils.queryResults(connection, sql, args, dataTypes, limit);
	}

	/**
	 * Perform a database query
	 * 
	 * @param sql
	 *            sql statement
	 * @param args
	 *            arguments
	 * @return result set
	 * @since 1.1.2
	 */
	public ResultSet query(String sql, String[] args) {
		return SQLUtils.query(connection, sql, args);
	}

}
