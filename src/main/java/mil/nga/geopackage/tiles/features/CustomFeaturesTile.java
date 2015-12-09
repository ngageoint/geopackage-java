package mil.nga.geopackage.tiles.features;

import java.awt.image.BufferedImage;

import mil.nga.geopackage.extension.index.GeometryIndex;
import mil.nga.geopackage.features.user.FeatureResultSet;

import com.j256.ormlite.dao.CloseableIterator;

/**
 * Interface defining custom feature tile drawing. The tile drawn will be used
 * instead of drawing all of the features.
 * 
 * @author osbornb
 * @since 1.1.2
 */
public interface CustomFeaturesTile {

	/**
	 * Draw a custom tile
	 *
	 * @param tileWidth
	 *            tile width to draw
	 * @param tileHeight
	 *            tile height to draw
	 * @param tileFeatureCount
	 *            count of features in the requested tile
	 * @param geometryIndexResults
	 *            results as geometry index results
	 * @return custom image, or null
	 */
	public BufferedImage drawTile(int tileWidth, int tileHeight,
			long tileFeatureCount,
			CloseableIterator<GeometryIndex> geometryIndexResults);

	/**
	 * Draw a custom tile when the number of features within the tile is
	 * unknown. This is called when a feature table is not indexed and more
	 * total features exist than the max per tile.
	 *
	 * @param tileWidth
	 *            tile width to draw
	 * @param tileHeight
	 *            tile height to draw
	 * @param totalFeatureCount
	 *            count of total features in the feature table
	 * @param allFeatureResults
	 *            results in a feature cursor
	 * @return custom image, or null
	 */
	public BufferedImage drawUnindexedTile(int tileWidth, int tileHeight,
			long totalFeatureCount, FeatureResultSet allFeatureResults);

}
