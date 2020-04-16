package mil.nga.geopackage.tiles.user;

import java.sql.ResultSet;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserConnection;

/**
 * GeoPackage Tile Connection
 * 
 * @author osbornb
 */
public class TileConnection
		extends UserConnection<TileColumn, TileTable, TileRow, TileResultSet> {

	/**
	 * Constructor
	 * 
	 * @param database
	 *            GeoPackage connection
	 */
	public TileConnection(GeoPackageConnection database) {
		super(database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TileResultSet createResult(String[] columns, ResultSet resultSet,
			String sql, String[] selectionArgs) {
		return new TileResultSet(table, columns, resultSet, sql, selectionArgs);
	}

}
