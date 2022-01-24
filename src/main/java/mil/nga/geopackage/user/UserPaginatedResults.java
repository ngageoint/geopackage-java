package mil.nga.geopackage.user;

/**
 * User Paginated Results for iterating and querying through chunks
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
 * @since 6.1.3
 */
public abstract class UserPaginatedResults<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TResult extends UserResultSet<TColumn, TTable, TRow>>
		extends UserCorePaginatedResults<TColumn, TTable, TRow, TResult> {

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            feature dao
	 * @param results
	 *            feature result set
	 */
	protected UserPaginatedResults(UserDao<TColumn, TTable, TRow, TResult> dao,
			UserResultSet<TColumn, TTable, TRow> results) {
		super(dao, results);
	}

}
