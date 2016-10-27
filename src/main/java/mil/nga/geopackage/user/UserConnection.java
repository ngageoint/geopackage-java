package mil.nga.geopackage.user;

import java.sql.Connection;
import java.sql.ResultSet;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.SQLUtils;
import mil.nga.geopackage.db.SQLiteQueryBuilder;

/**
 * GeoPackage Connection used to define common functionality within different
 * connection types
 * 
 * @param <TColumn>
 * @param <TTable>
 * @param <TRow>
 * @param <TResult>
 * 
 * @author osbornb
 */
public abstract class UserConnection<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TResult extends UserResultSet<TColumn, TTable, TRow>>
		extends UserCoreConnection<TColumn, TTable, TRow, TResult> {

	/**
	 * Connection
	 */
	private final Connection connection;

	/**
	 * Table
	 */
	protected TTable table;

	/**
	 * Constructor
	 * 
	 * @param database
	 */
	protected UserConnection(GeoPackageConnection database) {
		this.connection = database.getConnection();
	}

	/**
	 * Get the table
	 * 
	 * @return table
	 */
	public TTable getTable() {
		return table;
	}

	/**
	 * Set the table
	 * 
	 * @param table
	 */
	public void setTable(TTable table) {
		this.table = table;
	}

	/**
	 * Create a result by wrapping the ResultSet
	 * 
	 * @param resultSet
	 * @param count
	 * @return result
	 */
	protected abstract TResult createResult(ResultSet resultSet, int count);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult rawQuery(String sql, String[] selectionArgs) {

		ResultSet resultSet = SQLUtils.query(connection, sql, selectionArgs);
		int count = SQLUtils.count(connection, sql, selectionArgs);

		return createResult(resultSet, count);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {

		String sql = SQLiteQueryBuilder.buildQueryString(false, table, columns,
				selection, groupBy, having, orderBy, null);

		ResultSet resultSet = SQLUtils.query(connection, sql, selectionArgs);
		int count = SQLUtils.count(connection, sql, selectionArgs);

		return createResult(resultSet, count);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {

		String sql = SQLiteQueryBuilder.buildQueryString(false, table, columns,
				selection, groupBy, having, orderBy, limit);

		ResultSet resultSet = SQLUtils.query(connection, sql, selectionArgs);
		int count = SQLUtils.count(connection, sql, selectionArgs);

		return createResult(resultSet, count);
	}

}
