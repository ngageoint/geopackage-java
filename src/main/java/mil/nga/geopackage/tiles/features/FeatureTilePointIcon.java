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
	private int width;

	/**
	 * Point icon height
	 */
	private int height;

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
	 *            icon image
	 */
	public FeatureTilePointIcon(BufferedImage icon) {
		this(icon, icon.getWidth(), icon.getHeight());
	}

	/**
	 * Constructor
	 *
	 * @param icon
	 *            icon image
	 * @param width
	 *            icon display width
	 * @param height
	 *            icon display height
	 * @since 3.2.0
	 */
	public FeatureTilePointIcon(BufferedImage icon, int width, int height) {
		this.icon = icon;
		this.width = width;
		this.height = height;
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
	 * Set the display width and adjust the x offset
	 * 
	 * @param width
	 *            icon display width
	 * @since 3.2.0
	 */
	public void setWidth(int width) {
		xOffset = xOffset / this.width * width;
		this.width = width;
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
	 * Set the display height and adjust the y offset
	 * 
	 * @param height
	 *            icon display height
	 * @since 3.2.0
	 */
	public void setHeight(int height) {
		yOffset = yOffset / this.height * height;
		this.height = height;
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
	 *            x offset
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
	 *            y offset
	 */
	public void setYOffset(float yOffset) {
		this.yOffset = yOffset;
	}

}
