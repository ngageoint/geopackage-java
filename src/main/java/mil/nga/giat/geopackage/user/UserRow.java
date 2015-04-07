package mil.nga.giat.geopackage.user;

/**
 * User Row containing the values from a single Result Set row
 * 
 * @param <TColumn>
 * @param <TTable>
 * 
 * @author osbornb
 */
public abstract class UserRow<TColumn extends UserColumn, TTable extends UserTable<TColumn>>
		extends UserCoreRow<TColumn, TTable> {

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param columnTypes
	 * @param values
	 */
	protected UserRow(TTable table, int[] columnTypes, Object[] values) {
		super(table, columnTypes, values);
	}

	/**
	 * Constructor to create an empty row
	 * 
	 * @param table
	 */
	protected UserRow(TTable table) {
		super(table);
	}

}
