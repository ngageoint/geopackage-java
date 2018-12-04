package mil.nga.geopackage.tiles.features;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Feature Tile Graphics for creating layered tiles to draw ordered features.
 * Draw Order: polygons, lines, points, icons
 *
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureTileGraphics {

	/**
	 * Polygon layer index
	 */
	private static final int POLYGON_LAYER = 0;

	/**
	 * Line layer index
	 */
	private static final int LINE_LAYER = 1;

	/**
	 * Point layer index
	 */
	private static final int POINT_LAYER = 2;

	/**
	 * Icon layer index
	 */
	private static final int ICON_LAYER = 3;

	/**
	 * Tile width
	 */
	private final int tileWidth;

	/**
	 * Tile height
	 */
	private final int tileHeight;

	/**
	 * Layered image
	 */
	private final BufferedImage[] layeredImage = new BufferedImage[4];

	/**
	 * Layered graphics
	 */
	private final Graphics2D[] layeredGraphics = new Graphics2D[4];

	/**
	 * Constructor
	 *
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 */
	public FeatureTileGraphics(int tileWidth, int tileHeight) {
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	/**
	 * Get the polygon image
	 *
	 * @return polygon image
	 */
	public BufferedImage getPolygonImage() {
		return getImage(POLYGON_LAYER);
	}

	/**
	 * Get the polygon graphics
	 *
	 * @return polygon graphics
	 */
	public Graphics2D getPolygonGraphics() {
		return getGraphics(POLYGON_LAYER);
	}

	/**
	 * Get the line image
	 *
	 * @return line image
	 */
	public BufferedImage getLineImage() {
		return getImage(LINE_LAYER);
	}

	/**
	 * Get the line graphics
	 *
	 * @return line graphics
	 */
	public Graphics2D getLineGraphics() {
		return getGraphics(LINE_LAYER);
	}

	/**
	 * Get the point image
	 *
	 * @return point image
	 */
	public BufferedImage getPointImage() {
		return getImage(POINT_LAYER);
	}

	/**
	 * Get the point graphics
	 *
	 * @return point graphics
	 */
	public Graphics2D getPointGraphics() {
		return getGraphics(POINT_LAYER);
	}

	/**
	 * Get the icon image
	 *
	 * @return icon image
	 */
	public BufferedImage getIconImage() {
		return getImage(ICON_LAYER);
	}

	/**
	 * Get the icon canvas
	 *
	 * @return icon canvas
	 */
	public Graphics2D getIconGraphics() {
		return getGraphics(ICON_LAYER);
	}

	/**
	 * Create the final image from the layers, resets the layers
	 *
	 * @return image
	 */
	public BufferedImage createImage() {

		BufferedImage image = null;
		Graphics2D graphics = null;

		for (int layer = 0; layer < 4; layer++) {

			BufferedImage layerImage = layeredImage[layer];

			if (layerImage != null) {

				if (image == null) {
					image = layerImage;
					graphics = layeredGraphics[layer];
				} else {
					graphics.drawImage(layerImage, 0, 0, null);
					layeredGraphics[layer].dispose();
					layerImage.flush();
				}

				layeredImage[layer] = null;
				layeredGraphics[layer] = null;
			}
		}

		if (graphics != null) {
			graphics.dispose();
		}

		return image;
	}

	/**
	 * Dispose of the layered graphics and images
	 */
	public void dispose() {
		for (int layer = 0; layer < 4; layer++) {
			Graphics2D graphics = layeredGraphics[layer];
			if (graphics != null) {
				graphics.dispose();
				layeredGraphics[layer] = null;
			}
			BufferedImage image = layeredImage[layer];
			if (image != null) {
				image.flush();
				layeredImage[layer] = null;
			}
		}
	}

	/**
	 * Get the bitmap for the layer index
	 *
	 * @param layer
	 *            layer index
	 * @return bitmap
	 */
	private BufferedImage getImage(int layer) {
		BufferedImage image = layeredImage[layer];
		if (image == null) {
			createImageAndGraphics(layer);
			image = layeredImage[layer];
		}
		return image;
	}

	/**
	 * Get the graphics for the layer index
	 *
	 * @param layer
	 *            layer index
	 * @return graphics
	 */
	private Graphics2D getGraphics(int layer) {
		Graphics2D graphics = layeredGraphics[layer];
		if (graphics == null) {
			createImageAndGraphics(layer);
			graphics = layeredGraphics[layer];
		}
		return graphics;
	}

	/**
	 * Create a new empty Image and Graphics
	 *
	 * @param layer
	 *            layer index
	 */
	private void createImageAndGraphics(int layer) {
		layeredImage[layer] = new BufferedImage(tileWidth, tileHeight,
				BufferedImage.TYPE_INT_ARGB);
		layeredGraphics[layer] = layeredImage[layer].createGraphics();
		layeredGraphics[layer].setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
	}

}
