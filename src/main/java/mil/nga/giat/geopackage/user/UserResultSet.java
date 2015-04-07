package mil.nga.giat.geopackage.user;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import mil.nga.giat.geopackage.GeoPackageException;
import mil.nga.giat.geopackage.db.GeoPackageDataType;

/**
 * Abstract User Result Set
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
	 * Constructor
	 * 
	 * @param table
	 * @param resultSet
	 */
	protected UserResultSet(TTable table, ResultSet resultSet) {
		this.table = table;
		this.resultSet = resultSet;
	}

	/**
	 * Get the Result Set
	 * 
	 * @return
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

		int[] columnTypes = new int[table.columnCount()];
		Object[] values = new Object[table.columnCount()];

		try {

			ResultSetMetaData metaData = resultSet.getMetaData();

			for (TColumn column : table.getColumns()) {

				int index = column.getIndex();

				columnTypes[index] = metaData.getColumnType(index);

				values[index] = getValue(column);
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
		try {
			return resultSet.first();
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to move ResultSet cursor to first", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumnIndex(String columnName) {
		int index;
		try {
			index = resultSet.findColumn(columnName);
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
			type = resultSet.getMetaData().getColumnType(columnIndex);
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
			value = resultSet.getString(columnIndex);
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
			value = resultSet.getInt(columnIndex);
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
			value = resultSet.getBytes(columnIndex);
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
			value = resultSet.getLong(columnIndex);
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
			value = resultSet.getShort(columnIndex);
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
			value = resultSet.getDouble(columnIndex);
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
			value = resultSet.getFloat(columnIndex);
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
	public void close() {
		try {
			resultSet.close();
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to close ResultSet", e);
		}
	}

}
