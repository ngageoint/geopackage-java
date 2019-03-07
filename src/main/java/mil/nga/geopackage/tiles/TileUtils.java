package mil.nga.geopackage.tiles;

/**
 * Tile utilities and constants
 *
 * @author osbornb
 * @since 3.1.1
 */
public class TileUtils {

	/**
	 * Displayed device-independent pixels
	 */
	public static final int TILE_DP = 256;

	/**
	 * Tile pixels for default dpi tiles
	 */
	public static final int TILE_PIXELS_DEFAULT = TILE_DP;

	/**
	 * Tile pixels for high dpi tiles
	 */
	public static final int TILE_PIXELS_HIGH = TILE_PIXELS_DEFAULT * 2;

	/**
	 * Get the tile side (width and height) dimension based upon the scale
	 *
	 * @param scale
	 *            scale
	 * @return default tile length
	 */
	public static int tileLength(float scale) {
		return Math.round(scale * TILE_DP);
	}

	/**
	 * Get the tile scale based upon the tile dimensions
	 *
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return tile scale
	 */
	public static float tileScale(int tileWidth, int tileHeight) {
		return tileScale(Math.min(tileWidth, tileHeight));
	}

	/**
	 * Get the tile scale based upon the tile length (width or height)
	 *
	 * @param tileLength
	 *            tile length (width or height)
	 * @return tile scale
	 */
	public static float tileScale(int tileLength) {
		return ((float) tileLength) / TILE_DP;
	}

}
