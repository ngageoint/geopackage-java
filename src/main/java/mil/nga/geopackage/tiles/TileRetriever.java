package mil.nga.geopackage.tiles;

/**
 * Interface defining the tile retrieval methods
 *
 * @author osbornb
 * @since 1.2.0
 */
public interface TileRetriever {

	/**
	 * Check if there is a tile for the x, y, and zoom
	 *
	 * @param x
	 * @param y
	 * @param zoom
	 * @return true if a tile exists
	 */
	public boolean hasTile(int x, int y, int zoom);

	/**
	 * Get a tile from the x, y, and zoom
	 *
	 * @param x
	 * @param y
	 * @param zoom
	 * @return tile with dimensions and bytes
	 */
	public GeoPackageTile getTile(int x, int y, int zoom);

}
