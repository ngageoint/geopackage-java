package mil.nga.geopackage.tiles.user;

import java.sql.ResultSet;

import mil.nga.geopackage.user.UserResultSet;

/**
 * Tile Result Set to wrap a database ResultSet for tile queries
 * 
 * @author osbornb
 */
public class TileResultSet
		extends UserResultSet<TileColumn, TileTable, TileRow> {

	/**
	 * Constructor
	 * 
	 * @param table
	 *            tile table
	 * @param resultSet
	 *            result set
	 * @param count
	 *            count
	 */
	public TileResultSet(TileTable table, ResultSet resultSet, int count) {
		this(table, null, resultSet, count);
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            tile table
	 * @param columns
	 *            columns
	 * @param resultSet
	 *            result set
	 * @param count
	 *            count
	 * @since 3.5.0
	 */
	public TileResultSet(TileTable table, String[] columns, ResultSet resultSet,
			int count) {
		super(table, columns, resultSet, count);
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            tile table
	 * @param resultSet
	 *            result set
	 * @param sql
	 *            SQL statement
	 * @param selectionArgs
	 *            selection arguments
	 * @since 3.5.1
	 */
	public TileResultSet(TileTable table, ResultSet resultSet, String sql,
			String[] selectionArgs) {
		this(table, null, resultSet, sql, selectionArgs);
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            tile table
	 * @param columns
	 *            columns
	 * @param resultSet
	 *            result set
	 * @param sql
	 *            SQL statement
	 * @param selectionArgs
	 *            selection arguments
	 * @since 3.5.1
	 */
	public TileResultSet(TileTable table, String[] columns, ResultSet resultSet,
			String sql, String[] selectionArgs) {
		super(table, columns, resultSet, sql, selectionArgs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileRow getRow(int[] columnTypes, Object[] values) {
		return new TileRow(getTable(), getColumns(), columnTypes, values);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileColumns getColumns() {
		return (TileColumns) super.getColumns();
	}

}
