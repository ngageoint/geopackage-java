package mil.nga.geopackage.tiles.features;

import java.awt.image.BufferedImage;

/**
 * Point icon in place of a drawn circle
 *
 * @author osbornb
 * @since 1.1.2
 */
public class FeatureTilePointIcon {

	/**
	 * Image
	 */
	private final BufferedImage icon;

	/**
	 * Point icon width
	 */
	private final int width;

	/**
	 * Point icon height
	 */
	private final int height;

	/**
	 * X pixel offset
	 */
	private float xOffset = 0;

	/**
	 * Y pixel offset
	 */
	private float yOffset = 0;

	/**
	 * Constructor
	 *
	 * @param icon
	 */
	public FeatureTilePointIcon(BufferedImage icon) {
		this.icon = icon;
		this.width = icon.getWidth();
		this.height = icon.getHeight();
		pinIcon();
	}

	/**
	 * Pin the icon to the point, lower middle on the point
	 */
	public void pinIcon() {
		xOffset = width / 2.0f;
		yOffset = height;
	}

	/**
	 * Center the icon on the point
	 */
	public void centerIcon() {
		xOffset = width / 2.0f;
		yOffset = height / 2.0f;
	}

	/**
	 * Get the icon
	 *
	 * @return image
	 */
	public BufferedImage getIcon() {
		return icon;
	}

	/**
	 * Get the width
	 *
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the height
	 *
	 * @return height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Get the x offset
	 *
	 * @return x offset
	 */
	public float getXOffset() {
		return xOffset;
	}

	/**
	 * Set the x offset
	 *
	 * @param xOffset
	 */
	public void setXOffset(float xOffset) {
		this.xOffset = xOffset;
	}

	/**
	 * Get the y offset
	 *
	 * @return y offset
	 */
	public float getYOffset() {
		return yOffset;
	}

	/**
	 * Set the y offset
	 *
	 * @param yOffset
	 */
	public void setYOffset(float yOffset) {
		this.yOffset = yOffset;
	}

}
