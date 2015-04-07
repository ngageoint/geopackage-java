package mil.nga.giat.geopackage.user;

import mil.nga.giat.geopackage.db.GeoPackageConnection;

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
	private final GeoPackageConnection db;

	/**
	 * Constructor
	 * 
	 * @param db
	 * @param userDb
	 * @param table
	 */
	protected UserDao(GeoPackageConnection db,
			UserConnection<TColumn, TTable, TRow, TResult> userDb, TTable table) {
		super(db, userDb, table);
		this.db = db;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int update(TRow row) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long insert(TRow row) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int count(String where, String[] args) {
		// TODO Auto-generated method stub
		return 0;
	}

}
