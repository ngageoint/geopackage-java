package mil.nga.geopackage.user;

import java.sql.Connection;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.db.SQLUtils;

/**
 * Abstract User DAO for reading user tables
 * 
 * @param <TColumn>
 * @param <TTable>
 * @param <TRow>
 * @param <TResult>
 * 
 * @author osbornb
 */
public abstract class UserDao<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TResult extends UserResultSet<TColumn, TTable, TRow>>
		extends UserCoreDao<TColumn, TTable, TRow, TResult> {

	/**
	 * Connection
	 */
	private final Connection connection;

	/**
	 * Constructor
	 * 
	 * @param database
	 * @param db
	 * @param userDb
	 * @param table
	 */
	protected UserDao(String database, GeoPackageConnection db,
			UserConnection<TColumn, TTable, TRow, TResult> userDb, TTable table) {
		super(database, db, userDb, table);
		this.connection = db.getConnection();
	}

	/**
	 * Get the database connection
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
	public int update(TRow row) {
		ContentValues contentValues = row.toContentValues();
		int updated = 0;
		if (contentValues.size() > 0) {
			updated = SQLUtils.update(connection, getTableName(),
					contentValues, getPkWhere(row.getId()),
					getPkWhereArgs(row.getId()));
		}
		return updated;
	}

	/**
	 * Update all rows matching the where clause with the provided values
	 * 
	 * @param values
	 * @param whereClause
	 * @param whereArgs
	 * @return updated count
	 */
	public int update(ContentValues values, String whereClause,
			String[] whereArgs) {
		return SQLUtils.update(connection, getTableName(), values, whereClause,
				whereArgs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long insert(TRow row) {
		long id = SQLUtils.insertOrThrow(connection, getTableName(),
				row.toContentValues());
		row.setId(id);
		return id;
	}

	/**
	 * Inserts a new row
	 * 
	 * @param values
	 * @return row id, -1 on error
	 */
	public long insert(ContentValues values) {
		return SQLUtils.insert(connection, getTableName(), values);
	}

	/**
	 * Inserts a new row
	 * 
	 * @param values
	 * @return row id
	 */
	public long insertOrThrow(ContentValues values) {
		return SQLUtils.insertOrThrow(connection, getTableName(), values);
	}

}
