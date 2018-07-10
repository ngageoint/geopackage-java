package mil.nga.geopackage.user;

/**
 * Reads the metadata from an existing user table
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
public abstract class UserTableReader<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>, TResult extends UserResultSet<TColumn, TTable, TRow>>
		extends UserCoreTableReader<TColumn, TTable, TRow, TResult> {

	/**
	 * Constructor
	 * 
	 * @param tableName
	 *            table name
	 */
	protected UserTableReader(String tableName) {
		super(tableName);
	}

}
