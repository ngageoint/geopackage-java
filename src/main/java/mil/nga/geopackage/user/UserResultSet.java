package mil.nga.geopackage.user;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.ResultSetResult;
import mil.nga.geopackage.db.ResultUtils;
import mil.nga.geopackage.db.SQLUtils;

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
	private Integer count;

	/**
	 * Executed SQL command
	 */
	private String sql;

	/**
	 * Selection arguments
	 */
	private String[] selectionArgs;

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
	 * Constructor
	 * 
	 * @param table
	 *            table
	 * @param resultSet
	 *            result set
	 * @param sql
	 *            SQL statement
	 * @param selectionArgs
	 *            selection arguments
	 * @since 4.0.0
	 */
	protected UserResultSet(TTable table, ResultSet resultSet, String sql,
			String[] selectionArgs) {
		this(table, table.getUserColumns(), resultSet, sql, selectionArgs);
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
	 * @param sql
	 *            SQL statement
	 * @param selectionArgs
	 *            selection arguments
	 * @since 4.0.0
	 */
	protected UserResultSet(TTable table, String[] columns, ResultSet resultSet,
			String sql, String[] selectionArgs) {
		super(resultSet);
		UserColumns<TColumn> userColumns = null;
		if (columns != null) {
			userColumns = table.createUserColumns(columns);
		} else {
			userColumns = table.getUserColumns();
		}
		this.table = table;
		this.columns = userColumns;
		this.sql = sql;
		this.selectionArgs = selectionArgs;
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
	 * @param sql
	 *            SQL statement
	 * @param selectionArgs
	 *            selection arguments
	 * @since 4.0.0
	 */
	protected UserResultSet(TTable table, UserColumns<TColumn> columns,
			ResultSet resultSet, String sql, String[] selectionArgs) {
		super(resultSet);
		this.table = table;
		this.columns = columns;
		this.sql = sql;
		this.selectionArgs = selectionArgs;
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
		if (count == null) {
			if (sql != null) {
				count = SQLUtils.count(resultSet, sql, selectionArgs);
			} else {
				count = -1;
			}
		}
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<TRow> iterator() {
		return new Iterator<TRow>() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean hasNext() {
				return moveToNext();
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public TRow next() {
				return getRow();
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSql() {
		return sql;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getSelectionArgs() {
		return selectionArgs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<Long> ids() {
		return new Iterable<Long>() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Iterator<Long> iterator() {
				return new Iterator<Long>() {

					/**
					 * {@inheritDoc}
					 */
					@Override
					public boolean hasNext() {
						return moveToNext();
					}

					/**
					 * {@inheritDoc}
					 */
					@Override
					public Long next() {
						return getId();
					}

				};
			}
		};
	}

}
