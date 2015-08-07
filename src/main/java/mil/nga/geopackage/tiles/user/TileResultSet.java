package mil.nga.geopackage.tiles.user;

import java.sql.ResultSet;

import mil.nga.geopackage.user.UserResultSet;

/**
 * Tile Result Set to wrap a database ResultSet for tile queries
 * 
 * @author osbornb
 */
public class TileResultSet extends
		UserResultSet<TileColumn, TileTable, TileRow> {

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param resultSet
	 * @param count
	 */
	public TileResultSet(TileTable table, ResultSet resultSet, int count) {
		super(table, resultSet, count);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileRow getRow(int[] columnTypes, Object[] values) {
		TileRow row = new TileRow(getTable(), columnTypes, values);
		return row;
	}

}
