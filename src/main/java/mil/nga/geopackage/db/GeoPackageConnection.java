package mil.nga.geopackage.db;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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
	 * Name column
	 */
	private static final String NAME_COLUMN = "name";

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
	 * @param file
	 *            file
	 * @param connection
	 *            connection
	 * @param connectionSource
	 *            connection source
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean columnExists(String tableName, String columnName) {

		boolean exists = false;

		ResultSet result = query(
				"PRAGMA table_info(" + CoreSQLUtils.quoteWrap(tableName) + ")",
				null);
		try {
			while (result.next()) {
				String name = result.getString(NAME_COLUMN);
				if (columnName.equals(name)) {
					exists = true;
					break;
				}
			}
		} catch (SQLException e) {
			log.log(Level.WARNING, "Failed to search table info: " + tableName
					+ ", looking for column: " + columnName, e);
		} finally {
			try {
				result.close();
			} catch (SQLException e) {
				log.log(Level.WARNING,
						"Failed to close result set to table info: "
								+ tableName, e);
			}
		}

		return exists;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object querySingleResult(String sql, String[] args) {
		return SQLUtils.querySingleResult(connection, sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T querySingleTypedResult(String sql, String[] args) {
		@SuppressWarnings("unchecked")
		T result = (T) querySingleResult(sql, args);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object querySingleResult(String sql, String[] args,
			GeoPackageDataType dataType) {
		return SQLUtils.querySingleResult(connection, sql, args, dataType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T querySingleTypedResult(String sql, String[] args,
			GeoPackageDataType dataType) {
		@SuppressWarnings("unchecked")
		T result = (T) querySingleResult(sql, args, dataType);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object querySingleResult(String sql, String[] args, int column) {
		return SQLUtils.querySingleResult(connection, sql, args, column);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T querySingleTypedResult(String sql, String[] args, int column) {
		@SuppressWarnings("unchecked")
		T result = (T) querySingleResult(sql, args, column);
		return result;
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
	public <T> T querySingleTypedResult(String sql, String[] args, int column,
			GeoPackageDataType dataType) {
		@SuppressWarnings("unchecked")
		T result = (T) querySingleResult(sql, args, column, dataType);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> querySingleColumnResults(String sql, String[] args) {
		return SQLUtils.querySingleColumnResults(connection, sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> querySingleColumnTypedResults(String sql, String[] args) {
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) querySingleColumnResults(sql, args);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> querySingleColumnResults(String sql, String[] args,
			GeoPackageDataType dataType) {
		return SQLUtils.querySingleColumnResults(connection, sql, args,
				dataType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> querySingleColumnTypedResults(String sql, String[] args,
			GeoPackageDataType dataType) {
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) querySingleColumnResults(sql, args, dataType);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> querySingleColumnResults(String sql, String[] args,
			int column) {
		return SQLUtils.querySingleColumnResults(connection, sql, args, column);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> querySingleColumnTypedResults(String sql, String[] args,
			int column) {
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) querySingleColumnResults(sql, args, column);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> querySingleColumnResults(String sql, String[] args,
			int column, GeoPackageDataType dataType) {
		return SQLUtils.querySingleColumnResults(connection, sql, args, column,
				dataType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> querySingleColumnTypedResults(String sql, String[] args,
			int column, GeoPackageDataType dataType) {
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) querySingleColumnResults(sql, args, column,
				dataType);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> querySingleColumnResults(String sql, String[] args,
			int column, Integer limit) {
		return SQLUtils.querySingleColumnResults(connection, sql, args, column,
				limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> querySingleColumnTypedResults(String sql, String[] args,
			int column, Integer limit) {
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) querySingleColumnResults(sql, args, column,
				limit);
		return result;
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
	public <T> List<T> querySingleColumnTypedResults(String sql, String[] args,
			int column, GeoPackageDataType dataType, Integer limit) {
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) querySingleColumnResults(sql, args, column,
				dataType, limit);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<List<Object>> queryResults(String sql, String[] args) {
		return SQLUtils.queryResults(connection, sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<List<T>> queryTypedResults(String sql, String[] args) {
		@SuppressWarnings("unchecked")
		List<List<T>> result = (List<List<T>>) (Object) queryResults(sql, args);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<List<Object>> queryResults(String sql, String[] args,
			GeoPackageDataType[] dataTypes) {
		return SQLUtils.queryResults(connection, sql, args, dataTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<List<T>> queryTypedResults(String sql, String[] args,
			GeoPackageDataType[] dataTypes) {
		@SuppressWarnings("unchecked")
		List<List<T>> result = (List<List<T>>) (Object) queryResults(sql, args,
				dataTypes);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> querySingleRowResults(String sql, String[] args) {
		return SQLUtils.querySingleRowResults(connection, sql, args);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> querySingleRowTypedResults(String sql, String[] args) {
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) querySingleRowResults(sql, args);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object> querySingleRowResults(String sql, String[] args,
			GeoPackageDataType[] dataTypes) {
		return SQLUtils.querySingleRowResults(connection, sql, args, dataTypes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<T> querySingleRowTypedResults(String sql, String[] args,
			GeoPackageDataType[] dataTypes) {
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) querySingleRowResults(sql, args, dataTypes);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<List<Object>> queryResults(String sql, String[] args,
			Integer limit) {
		return SQLUtils.queryResults(connection, sql, args, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<List<T>> queryTypedResults(String sql, String[] args,
			Integer limit) {
		@SuppressWarnings("unchecked")
		List<List<T>> result = (List<List<T>>) (Object) queryResults(sql, args,
				limit);
		return result;
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
	 * {@inheritDoc}
	 */
	@Override
	public <T> List<List<T>> queryTypedResults(String sql, String[] args,
			GeoPackageDataType[] dataTypes, Integer limit) {
		@SuppressWarnings("unchecked")
		List<List<T>> result = (List<List<T>>) (Object) queryResults(sql, args,
				dataTypes, limit);
		return result;
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
