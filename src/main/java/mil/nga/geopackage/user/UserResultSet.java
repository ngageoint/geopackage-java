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
	 * Columns
	 */
	private final UserColumns<TColumn> columns;

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
		this(table, table.getUserColumns(), resultSet, count);
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            table
	 * @param columns
	 *            columns
	 * @param resultSet
	 *            result set
	 * @param count
	 *            count
	 * @since 3.5.0
	 */
	protected UserResultSet(TTable table, String[] columns, ResultSet resultSet,
			int count) {
		super(resultSet);
		UserColumns<TColumn> userColumns = null;
		if (columns != null) {
			userColumns = table.createUserColumns(columns);
		} else {
			userColumns = table.getUserColumns();
		}
		this.table = table;
		this.columns = userColumns;
		this.count = count;
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            table
	 * @param columns
	 *            columns
	 * @param resultSet
	 *            result set
	 * @param count
	 *            count
	 * @since 3.5.0
	 */
	protected UserResultSet(TTable table, UserColumns<TColumn> columns,
			ResultSet resultSet, int count) {
		super(resultSet);
		this.table = table;
		this.columns = columns;
		this.count = count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(TColumn column) {
		return getValue(columns.getColumnIndex(column.getName()),
				column.getDataType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(int index) {
		return getValue(index, columns.getColumn(index).getDataType());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(String columnName) {
		return getValue(columns.getColumnIndex(columnName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getId() {
		long id = -1;

		TColumn pkColumn = columns.getPkColumn();
		if (pkColumn == null) {
			StringBuilder error = new StringBuilder(
					"No primary key column in ");
			if (columns.isCustom()) {
				error.append("custom specified table columns. ");
			}
			error.append("table: " + columns.getTableName());
			if (columns.isCustom()) {
				error.append(", columns: " + columns.getColumnNames());
			}
			throw new GeoPackageException(error.toString());
		}

		Object objectValue = getValue(pkColumn);
		if (objectValue instanceof Number) {
			id = ((Number) objectValue).longValue();
		} else {
			throw new GeoPackageException(
					"Primary Key value was not a number. table: "
							+ columns.getTableName() + ", index: "
							+ pkColumn.getIndex() + ", name: "
							+ pkColumn.getName() + ", value: " + objectValue);
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
	public String getTableName() {
		return table.getTableName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserColumns<TColumn> getColumns() {
		return columns;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TRow getRow() {

		int[] columnTypes = new int[columns.columnCount()];
		Object[] values = new Object[columns.columnCount()];

		try {

			ResultSetMetaData metaData = resultSet.getMetaData();

			for (int index = 0; index < columns.columnCount(); index++) {
				TColumn column = columns.getColumn(index);

				Object value = getValue(column);
				values[index] = value;

				int columnType;
				if (value == null) {
					columnType = ResultUtils.FIELD_TYPE_NULL;
				} else {
					int metadataColumnType = metaData
							.getColumnType(resultIndexToResultSetIndex(index));
					columnType = resultSetTypeToSqlLite(metadataColumnType);
				}
				columnTypes[index] = columnType;
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to retrieve the row", e);
		}

		TRow row = getRow(columnTypes, values);

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
