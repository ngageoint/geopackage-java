package mil.nga.geopackage.extension.coverage;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.coverage.CoverageDataImage;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Coverage Data PNG image, stores the tile row image and raster
 * 
 * @author osbornb
 * @since 2.0.1
 */
public class CoverageDataPngImage implements CoverageDataImage {

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
	public CoverageDataPngImage(TileRow tileRow) {
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
	 * Get the width
	 * 
	 * @return width
	 */
	public int getWidth() {
		return raster.getWidth();
	}

	/**
	 * Get the height
	 * 
	 * @return height
	 */
	public int getHeight() {
		return raster.getHeight();
	}

}
