package mil.nga.geopackage.tiles.reproject;

import mil.nga.geopackage.tiles.user.TileDao;

/**
 * 
 * Tile Reprojection for reprojecting an existing tile table
 * 
 * @author osbornb
 * @since 4.0.1
 */
public class TileReprojection extends TileReprojectionCore {

	/**
	 * Tile DAO
	 */
	private TileDao tileDao;

	/**
	 * Reprojection Tile DAO
	 */
	private TileDao reprojectTileDao;

}
