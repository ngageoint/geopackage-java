package mil.nga.geopackage.tiles.user;

import java.sql.ResultSet;

import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserConnection;

/**
 * GeoPackage Tile Connection
 * 
 * @author osbornb
 */
public class TileConnection extends
		UserConnection<TileColumn, TileTable, TileRow, TileResultSet> {

	/**
	 * Constructor
	 * 
	 * @param database
	 */
	public TileConnection(GeoPackageConnection database) {
		super(database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TileResultSet createResult(ResultSet resultSet, int count) {
		return new TileResultSet(table, resultSet, count);
	}

}
