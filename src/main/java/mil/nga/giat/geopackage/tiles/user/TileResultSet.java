package mil.nga.giat.geopackage.tiles.user;

import java.sql.ResultSet;

import mil.nga.giat.geopackage.user.UserResultSet;

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
	 */
	public TileResultSet(TileTable table, ResultSet resultSet) {
		super(table, resultSet);
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
