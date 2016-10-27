package mil.nga.geopackage.user;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.GeoPackageDataType;

/**
 * Abstract User Result Set. The column index of the GeoPackage core is 0
 * indexed based and ResultSets are 1 indexed based.
 * 
 * @param <TColumn>
 * @param <TTable>
 * @param <TRow>
 * 
 * @author osbornb
 */
public abstract class UserResultSet<TColumn extends UserColumn, TTable extends UserTable<TColumn>, TRow extends UserRow<TColumn, TTable>>
		implements UserCoreResult<TColumn, TTable, TRow> {

	/**
	 * Table
	 */
	private final TTable table;

	/**
	 * Result Set
	 */
	private ResultSet resultSet;

	/**
	 * Result count
	 */
	private int count;

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param resultSet
	 */
	protected UserResultSet(TTable table, ResultSet resultSet, int count) {
		this.table = table;
		this.resultSet = resultSet;
		this.count = count;
	}

	/**
	 * Get the Result Set
	 * 
	 * @return result set
	 */
	public ResultSet getResultSet() {
		return resultSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(TColumn column) {
		Object value = getValue(column.getIndex(), column.getDataType());
		return value;
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
						columnType = UserCoreResultUtils.FIELD_TYPE_NULL;
					} else {
						int metadataColumnType = metaData
								.getColumnType(coreIndexToResultSetIndex(index));
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
	 * Get the SQLite type from the ResultSetMetaData column type
	 * 
	 * @param columnType
	 * @return
	 */
	private int resultSetTypeToSqlLite(int columnType) {

		int type;

		switch (columnType) {
		case Types.INTEGER:
		case Types.BIGINT:
		case Types.SMALLINT:
		case Types.TINYINT:
		case Types.BOOLEAN:
			type = UserCoreResultUtils.FIELD_TYPE_INTEGER;
			break;
		case Types.VARCHAR:
		case Types.DATE:
			type = UserCoreResultUtils.FIELD_TYPE_STRING;
			break;
		case Types.REAL:
		case Types.FLOAT:
		case Types.DOUBLE:
			type = UserCoreResultUtils.FIELD_TYPE_FLOAT;
			break;
		case Types.BLOB:
			type = UserCoreResultUtils.FIELD_TYPE_BLOB;
			break;
		case Types.NULL:
			type = UserCoreResultUtils.FIELD_TYPE_NULL;
			break;
		default:
			throw new GeoPackageException(
					"Unsupported ResultSet Metadata Column Type: " + columnType);
		}

		return type;
	}

	@Override
	public int getCount() {
		return count;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValue(int index, GeoPackageDataType dataType) {
		Object value = UserCoreResultUtils.getValue(this, index, dataType);
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean moveToNext() {
		try {
			return resultSet.next();
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to move ResultSet cursor to next", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean moveToFirst() {
		// For SQLite forward only, best we can do is assume the result set
		// is at the beginning
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean moveToPosition(int position) {
		try {
			// For SQLite forward only, best we can do is assume the result set
			// is at the beginning
			for (int i = 0; i < position; i++) {
				if (!resultSet.next()) {
					return false;
				}
			}
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to move ResultSet cursor to first", e);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumnIndex(String columnName) {
		int index;
		try {
			index = resultSetIndexToCoreIndex(resultSet.findColumn(columnName));
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to find column index for column name: "
							+ columnName, e);
		}
		return index;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getType(int columnIndex) {
		int type;
		try {
			int resultSetMetadataType = resultSet.getMetaData().getColumnType(
					coreIndexToResultSetIndex(columnIndex));
			type = resultSetTypeToSqlLite(resultSetMetadataType);
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get column type for column index: "
							+ columnIndex, e);
		}
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getString(int columnIndex) {
		String value;
		try {
			value = resultSet.getString(coreIndexToResultSetIndex(columnIndex));
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get String value for column index: "
							+ columnIndex, e);
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getInt(int columnIndex) {
		int value;
		try {
			value = resultSet.getInt(coreIndexToResultSetIndex(columnIndex));
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get int value for column index: " + columnIndex,
					e);
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getBlob(int columnIndex) {
		byte[] value;
		try {
			value = resultSet.getBytes(coreIndexToResultSetIndex(columnIndex));
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get blob bytes for column index: " + columnIndex,
					e);
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLong(int columnIndex) {
		long value;
		try {
			value = resultSet.getLong(coreIndexToResultSetIndex(columnIndex));
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get long value for column index: " + columnIndex,
					e);
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short getShort(int columnIndex) {
		short value;
		try {
			value = resultSet.getShort(coreIndexToResultSetIndex(columnIndex));
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get short value for column index: "
							+ columnIndex, e);
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDouble(int columnIndex) {
		double value;
		try {
			value = resultSet.getDouble(coreIndexToResultSetIndex(columnIndex));
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get double value for column index: "
							+ columnIndex, e);
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float getFloat(int columnIndex) {
		float value;
		try {
			value = resultSet.getFloat(coreIndexToResultSetIndex(columnIndex));
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get float value for column index: "
							+ columnIndex, e);
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean wasNull() {
		try {
			return resultSet.wasNull();
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to determine if previous value retrieved was null",
					e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		try {
			resultSet.getStatement().close();
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to close ResultSet Statement", e);
		}
		try {
			resultSet.close();
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to close ResultSet", e);
		}
	}

	/**
	 * Get the ResultSet index for the provided core index
	 * 
	 * @param coreIndex
	 * @return
	 */
	private int coreIndexToResultSetIndex(int coreIndex) {
		return coreIndex + 1;
	}

	/**
	 * Get the Core index for the provided ResultSet index
	 * 
	 * @param resultSetIndex
	 * @return
	 */
	private int resultSetIndexToCoreIndex(int resultSetIndex) {
		return resultSetIndex - 1;
	}

}
