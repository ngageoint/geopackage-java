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
 *            column type
 * @param <TTable>
 *            table type
 * @param <TRow>
 *            row type
 * @param <TResult>
 *            result type
 * 
 * @author osbornb
 */
public abstract class UserConnection<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TResult extends UserResultSet<TColumn, TTable, TRow>>
		extends UserCoreConnection<TColumn, TTable, TRow, TResult> {

	/**
	 * Connection
	 */
	protected final Connection connection;

	/**
	 * Table
	 */
	protected TTable table;

	/**
	 * Constructor
	 * 
	 * @param database
	 *            GeoPackage connection
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
	 *            table
	 */
	public void setTable(TTable table) {
		this.table = table;
	}

	/**
	 * Create a result by wrapping the ResultSet
	 * 
	 * @param resultSet
	 *            result set
	 * @param sql
	 *            SQL statement
	 * @param selectionArgs
	 *            selection arguments
	 * @return result
	 * @since 4.0.0
	 */
	protected TResult createResult(ResultSet resultSet, String sql,
			String[] selectionArgs) {
		return createResult(null, resultSet, sql, selectionArgs);
	}

	/**
	 * Create a result by wrapping the ResultSet
	 * 
	 * @param columns
	 *            columns
	 * @param resultSet
	 *            result set
	 * @param sql
	 *            SQL statement
	 * @param selectionArgs
	 *            selection arguments
	 * @return result
	 * @since 4.0.0
	 */
	protected abstract TResult createResult(String[] columns,
			ResultSet resultSet, String sql, String[] selectionArgs);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult rawQuery(String sql, String[] selectionArgs) {

		ResultSet resultSet = SQLUtils.query(connection, sql, selectionArgs);

		return createResult(resultSet, sql, selectionArgs);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult rawQuery(String sql, String[] columns, String[] selectionArgs) {

		ResultSet resultSet = SQLUtils.query(connection, sql, selectionArgs);

		return createResult(columns, resultSet, sql, selectionArgs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		return query(table, columns, null, selection, selectionArgs, groupBy,
				having, orderBy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(boolean distinct, String table, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		return query(distinct, table, columns, null, selection, selectionArgs,
				groupBy, having, orderBy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(String table, String[] columns, String[] columnsAs,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		return query(table, columns, columnsAs, selection, selectionArgs,
				groupBy, having, orderBy, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(boolean distinct, String table, String[] columns,
			String[] columnsAs, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy) {
		return query(distinct, table, columns, columnsAs, selection,
				selectionArgs, groupBy, having, orderBy, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		return query(table, columns, null, selection, selectionArgs, groupBy,
				having, orderBy, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(boolean distinct, String table, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy, String limit) {
		return query(distinct, table, columns, null, selection, selectionArgs,
				groupBy, having, orderBy, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(String table, String[] columns, String[] columnsAs,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy, String limit) {
		return query(false, table, columns, columnsAs, selection, selectionArgs,
				groupBy, having, orderBy, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(boolean distinct, String table, String[] columns,
			String[] columnsAs, String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy, String limit) {

		String sql = querySQL(distinct, table, columns, columnsAs, selection,
				groupBy, having, orderBy, limit);

		ResultSet resultSet = SQLUtils.query(connection, sql, selectionArgs);

		return createResult(columns, resultSet, sql, selectionArgs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String querySQL(String table, String[] columns, String selection,
			String groupBy, String having, String orderBy) {
		return querySQL(table, columns, null, selection, groupBy, having,
				orderBy, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String querySQL(boolean distinct, String table, String[] columns,
			String selection, String groupBy, String having, String orderBy) {
		return querySQL(distinct, table, columns, null, selection, groupBy,
				having, orderBy, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String querySQL(String table, String[] columns, String[] columnsAs,
			String selection, String groupBy, String having, String orderBy) {
		return querySQL(table, columns, columnsAs, selection, groupBy, having,
				orderBy, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String querySQL(boolean distinct, String table, String[] columns,
			String[] columnsAs, String selection, String groupBy, String having,
			String orderBy) {
		return querySQL(distinct, table, columns, columnsAs, selection, groupBy,
				having, orderBy, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String querySQL(String table, String[] columns, String selection,
			String groupBy, String having, String orderBy, String limit) {
		return querySQL(table, columns, null, selection, groupBy, having,
				orderBy, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String querySQL(boolean distinct, String table, String[] columns,
			String selection, String groupBy, String having, String orderBy,
			String limit) {
		return querySQL(distinct, table, columns, null, selection, groupBy,
				having, orderBy, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String querySQL(String table, String[] columns, String[] columnsAs,
			String selection, String groupBy, String having, String orderBy,
			String limit) {
		return querySQL(false, table, columns, columnsAs, selection, groupBy,
				having, orderBy, limit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String querySQL(boolean distinct, String table, String[] columns,
			String[] columnsAs, String selection, String groupBy, String having,
			String orderBy, String limit) {
		return SQLiteQueryBuilder.buildQueryString(distinct, table, columns,
				columnsAs, selection, groupBy, having, orderBy, limit);
	}

}
