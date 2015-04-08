package mil.nga.giat.geopackage.tiles.user;

import java.sql.ResultSet;

import mil.nga.giat.geopackage.db.GeoPackageConnection;
import mil.nga.giat.geopackage.user.UserConnection;

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
