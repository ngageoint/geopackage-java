package mil.nga.geopackage.user;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.ResultSetResult;
import mil.nga.geopackage.db.ResultUtils;

/**
 * Abstract User Result Set. The column index of the GeoPackage core is 0
 * indexed based and ResultSets are 1 indexed based.
 * 
 * @param <TColumn>
 *            column type
 * @param <TTable>
 *            table type
 * @param <TRow>
 *            row type
 * 
 * @author osbornb
 */
public abstract class UserResultSet<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>>
		extends ResultSetResult
		implements UserCoreResult<TColumn, TTable, TRow> {

	/**
	 * Table
	 */
	private final TTable table;

	/**
	 * Result count
	 */
	private int count;

	/**
	 * Constructor
	 * 
	 * @param table
	 *            table
	 * @param resultSet
	 *            result set
	 * @param count
	 *            count
	 */
	protected UserResultSet(TTable table, ResultSet resultSet, int count) {
		super(resultSet);
		this.table = table;
		this.count = count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(TColumn column) {
		return getValue(column.getIndex(), column.getDataType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(int index) {
		return getValue(table.getColumn(index));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(String columnName) {
		return getValue(table.getColumn(columnName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getId() {
		long id = -1;

		TColumn pkColumn = table.getPkColumn();
		if (pkColumn == null) {
			throw new GeoPackageException(
					"No primary key column for table: " + table.getTableName());
		}

		Object objectValue = getValue(pkColumn);
		if (objectValue instanceof Number) {
			id = ((Number) objectValue).longValue();
		} else {
			throw new GeoPackageException(
					"Primary Key value was not a number. Table: "
							+ table.getTableName() + ", Column Index: "
							+ pkColumn.getIndex() + ", Column Name: "
							+ pkColumn.getName() + ", Value: " + objectValue);
		}

		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TTable getTable() {
		return table;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TRow getRow() {

		TRow row = null;

		if (table != null) {

			int[] columnTypes = new int[table.columnCount()];
			Object[] values = new Object[table.columnCount()];

			try {

				ResultSetMetaData metaData = resultSet.getMetaData();

				for (TColumn column : table.getColumns()) {

					int index = column.getIndex();

					Object value = getValue(column);
					values[index] = value;

					int columnType;
					if (value == null) {
						columnType = ResultUtils.FIELD_TYPE_NULL;
					} else {
						int metadataColumnType = metaData.getColumnType(
								resultIndexToResultSetIndex(index));
						columnType = resultSetTypeToSqlLite(metadataColumnType);
					}
					columnTypes[index] = columnType;
				}
			} catch (SQLException e) {
				throw new GeoPackageException("Failed to retrieve the row", e);
			}

			row = getRow(columnTypes, values);

		}

		return row;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getCount() {
		return count;
	}

}
