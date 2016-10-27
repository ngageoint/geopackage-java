package mil.nga.geopackage.tiles;

import java.awt.image.BufferedImage;

/**
 * GeoPackage tile wrapper containing tile dimensions and the image or raw image
 * bytes
 * 
 * @author osbornb
 * @since 1.2.0
 */
public class GeoPackageTile {

	/**
	 * Tile width
	 */
	public final int width;

	/**
	 * Tile height
	 */
	public final int height;

	/**
	 * Image
	 */
	private BufferedImage image;

	/**
	 * Image bytes
	 */
	private byte[] data;

	/**
	 * Constructor
	 *
	 * @param width
	 * @param height
	 * @param image
	 */
	public GeoPackageTile(int width, int height, BufferedImage image) {
		this.width = width;
		this.height = height;
		this.image = image;
	}

	/**
	 * Constructor
	 *
	 * @param width
	 * @param height
	 * @param data
	 */
	public GeoPackageTile(int width, int height, byte[] data) {
		this.width = width;
		this.height = height;
		this.data = data;
	}

	/**
	 * Get width
	 *
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get height
	 *
	 * @return height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Get the image
	 * 
	 * @return image
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * Get image data
	 *
	 * @return image data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Set the image
	 * 
	 * @param image
	 *            buffered image
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
	}

	/**
	 * Set the image data
	 * 
	 * @param data
	 *            image data
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

}
