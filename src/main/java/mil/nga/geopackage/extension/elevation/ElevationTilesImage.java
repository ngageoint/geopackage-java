package mil.nga.geopackage.extension.elevation;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Elevation Tiles extension image
 * 
 * @author osbornb
 * @since 1.2.1
 */
public class ElevationTilesImage implements ElevationImage {

	/**
	 * Buffered image
	 */
	private final BufferedImage image;

	/**
	 * Image writable raster
	 */
	private final WritableRaster raster;

	/**
	 * Constructor
	 * 
	 * @param tileRow
	 *            tile row
	 */
	public ElevationTilesImage(TileRow tileRow) {
		try {
			image = tileRow.getTileDataImage();
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed to get the Tile Row Data Image", e);
		}
		raster = image.getRaster();
	}

	/**
	 * Get the buffered image
	 * 
	 * @return buffered image
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * Get the image writable raster
	 * 
	 * @return raster
	 */
	public WritableRaster getRaster() {
		return raster;
	}

	/**
	 * Get the image width
	 */
	public int getWidth() {
		return raster.getWidth();
	}

	/**
	 * Get the image height
	 */
	public int getHeight() {
		return raster.getHeight();
	}

}
