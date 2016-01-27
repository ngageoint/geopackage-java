package mil.nga.geopackage.tiles.features;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.link.FeatureTileTableLinker;
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
	 * Flag indicating whether the feature and tile tables should be linked
	 */
	private boolean linkTables = true;

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
	 * Is the feature table going to be linked with the tile table? Defaults to
	 * true.
	 * 
	 * @return true if tables will be linked upon generation
	 * @since 1.1.6
	 */
	public boolean isLinkTables() {
		return linkTables;
	}

	/**
	 * Set the link tables flag
	 * 
	 * @param linkTables
	 * @since 1.1.6
	 */
	public void setLinkTables(boolean linkTables) {
		this.linkTables = linkTables;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preTileGeneration() {

		// Link the feature and tile table
		if (linkTables) {
			FeatureTileTableLinker linker = new FeatureTileTableLinker(
					getGeoPackage());
			linker.link(featureTiles.getFeatureDao().getTableName(),
					getTableName());
		}

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
