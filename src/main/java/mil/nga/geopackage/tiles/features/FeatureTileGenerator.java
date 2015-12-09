package mil.nga.geopackage.tiles.features;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.tiles.TileGenerator;

/**
 * Creates a set of tiles within a GeoPackage by generating tiles from features
 *
 * @author osbornb
 * @since 1.1.2
 */
public class FeatureTileGenerator extends TileGenerator {

	/**
	 * Feature tiles
	 */
	private final FeatureTiles featureTiles;

	/**
	 * Constructor
	 *
	 * @param geoPackage
	 * @param tableName
	 * @param featureTiles
	 * @param minZoom
	 * @param maxZoom
	 */
	public FeatureTileGenerator(GeoPackage geoPackage, String tableName,
			FeatureTiles featureTiles, int minZoom, int maxZoom) {
		super(geoPackage, tableName, minZoom, maxZoom);
		this.featureTiles = featureTiles;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected byte[] createTile(int z, long x, long y) {

		byte[] tileData = featureTiles.drawTileBytes((int) x, (int) y, z);

		return tileData;
	}

}
