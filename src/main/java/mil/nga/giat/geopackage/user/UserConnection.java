package mil.nga.giat.geopackage.user;

import mil.nga.giat.geopackage.db.GeoPackageConnection;

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
	 * Database connection
	 */
	private final GeoPackageConnection database;

	/**
	 * Constructor
	 * 
	 * @param database
	 */
	protected UserConnection(GeoPackageConnection database) {
		this.database = database;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult rawQuery(String sql, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TResult query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		// TODO Auto-generated method stub
		return null;
	}

}
