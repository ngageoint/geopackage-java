package mil.nga.geopackage.tiles.features.custom;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.index.GeometryIndex;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.property.GeoPackageJavaProperties;
import mil.nga.geopackage.property.JavaPropertyConstants;
import mil.nga.geopackage.tiles.features.CustomFeaturesTile;

import com.j256.ormlite.dao.CloseableIterator;

/**
 * Draws a tile indicating the number of features that exist within the tile,
 * visible when zoomed in closer. The number is drawn in the center of the tile
 * and by default is surrounded by a colored circle with border. By default a
 * tile border is drawn and the tile is colored (transparently most likely). The
 * paint objects for each draw type can be modified to or set to null (except
 * for the text paint object).
 * 
 * @author osbornb
 * @since 1.1.2
 */
public class NumberFeaturesTile implements CustomFeaturesTile {

	/**
	 * Text size
	 */
	protected int textSize;

	/**
	 * Text font
	 */
	protected String textFont;

	/**
	 * Text color
	 */
	protected Color textColor;

	/**
	 * Circle stroke width
	 */
	protected float circleStrokeWidth;

	/**
	 * Circle color
	 */
	protected Color circleColor;

	/**
	 * Circle fill color
	 */
	protected Color circleFillColor;

	/**
	 * Tile Border stroke width
	 */
	protected float tileBorderStrokeWidth;

	/**
	 * Tile Border color
	 */
	protected Color tileBorderColor;

	/**
	 * Tile fill color
	 */
	protected Color tileFillColor;

	/**
	 * The percentage of border to include around the edges of the text in the
	 * circle
	 */
	private float circlePaddingPercentage;

	/**
	 * Flag indicating whether tiles should be drawn for feature tables that are
	 * not indexed
	 */
	private boolean drawUnindexedTiles;

	/**
	 * Constructor
	 */
	public NumberFeaturesTile() {

		// Set the default text values
		textSize = GeoPackageJavaProperties.getIntegerProperty(
				JavaPropertyConstants.NUMBER_FEATURES_TILE_TEXT,
				JavaPropertyConstants.NUMBER_FEATURES_TILE_TEXT_SIZE);
		textFont = GeoPackageJavaProperties.getProperty(
				JavaPropertyConstants.NUMBER_FEATURES_TILE_TEXT,
				JavaPropertyConstants.NUMBER_FEATURES_TILE_TEXT_FONT);
		textColor = GeoPackageJavaProperties.getColorProperty(
				JavaPropertyConstants.NUMBER_FEATURES_TILE_TEXT,
				JavaPropertyConstants.NUMBER_FEATURES_TILE_COLOR);

		// Set the default circle paint values
		if (GeoPackageJavaProperties
				.getBooleanProperty(JavaPropertyConstants.NUMBER_FEATURES_TILE_CIRCLE_DRAW)) {
			circleStrokeWidth = GeoPackageJavaProperties.getFloatProperty(
					JavaPropertyConstants.NUMBER_FEATURES_TILE_CIRCLE_DRAW,
					JavaPropertyConstants.NUMBER_FEATURES_TILE_STROKE_WIDTH);
			circleColor = GeoPackageJavaProperties.getColorProperty(
					JavaPropertyConstants.NUMBER_FEATURES_TILE_CIRCLE_DRAW,
					JavaPropertyConstants.NUMBER_FEATURES_TILE_COLOR);
		}

		// Set the default circle fill paint values
		if (GeoPackageJavaProperties
				.getBooleanProperty(JavaPropertyConstants.NUMBER_FEATURES_TILE_CIRCLE_FILL)) {
			circleFillColor = GeoPackageJavaProperties.getColorProperty(
					JavaPropertyConstants.NUMBER_FEATURES_TILE_CIRCLE_FILL,
					JavaPropertyConstants.NUMBER_FEATURES_TILE_COLOR);
		}

		// Set the default tile border paint values
		if (GeoPackageJavaProperties
				.getBooleanProperty(JavaPropertyConstants.NUMBER_FEATURES_TILE_BORDER)) {
			tileBorderStrokeWidth = GeoPackageJavaProperties.getFloatProperty(
					JavaPropertyConstants.NUMBER_FEATURES_TILE_BORDER,
					JavaPropertyConstants.NUMBER_FEATURES_TILE_STROKE_WIDTH);
			tileBorderColor = GeoPackageJavaProperties.getColorProperty(
					JavaPropertyConstants.NUMBER_FEATURES_TILE_BORDER,
					JavaPropertyConstants.NUMBER_FEATURES_TILE_COLOR);
		}

		// Set the default tile fill paint values
		if (GeoPackageJavaProperties
				.getBooleanProperty(JavaPropertyConstants.NUMBER_FEATURES_TILE_FILL)) {
			tileFillColor = GeoPackageJavaProperties.getColorProperty(
					JavaPropertyConstants.NUMBER_FEATURES_TILE_FILL,
					JavaPropertyConstants.NUMBER_FEATURES_TILE_COLOR);
		}

		// Set the default circle padding percentage
		circlePaddingPercentage = GeoPackageJavaProperties
				.getFloatProperty(
						JavaPropertyConstants.NUMBER_FEATURES_TILE,
						JavaPropertyConstants.NUMBER_FEATURES_TILE_CIRCLE_PADDING_PERCENTAGE);

		// Set the default draw unindexed tiles value
		drawUnindexedTiles = GeoPackageJavaProperties.getBooleanProperty(
				JavaPropertyConstants.NUMBER_FEATURES_TILE,
				JavaPropertyConstants.NUMBER_FEATURES_TILE_UNINDEXED_DRAW);
	}

	/**
	 * Get the text size
	 * 
	 * @return text size
	 */
	public int getTextSize() {
		return textSize;
	}

	/**
	 * Set the text size
	 * 
	 * @param textSize
	 *            text size
	 */
	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}

	/**
	 * Get the text color
	 * 
	 * @return text color
	 */
	public Color getTextColor() {
		return textColor;
	}

	/**
	 * Set the text color
	 * 
	 * @param textColor
	 *            text color
	 */
	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	/**
	 * Get the circle stroke width
	 * 
	 * @return circle stroke width
	 */
	public float getCircleStrokeWidth() {
		return circleStrokeWidth;
	}

	/**
	 * Set the circle stroke width
	 * 
	 * @param circleStrokeWidth
	 *            circle stroke width
	 */
	public void setCircleStrokeWidth(float circleStrokeWidth) {
		this.circleStrokeWidth = circleStrokeWidth;
	}

	/**
	 * Get the circle color
	 * 
	 * @return circle color
	 */
	public Color getCircleColor() {
		return circleColor;
	}

	/**
	 * Set the circle color
	 * 
	 * @param circleColor
	 *            circle color
	 */
	public void setCircleColor(Color circleColor) {
		this.circleColor = circleColor;
	}

	/**
	 * Get the circle fill color
	 * 
	 * @return circle fill color
	 */
	public Color getCircleFillColor() {
		return circleFillColor;
	}

	/**
	 * Set the circle fill color
	 * 
	 * @param circleFillColor
	 *            circle fill color
	 */
	public void setCircleFillColor(Color circleFillColor) {
		this.circleFillColor = circleFillColor;
	}

	/**
	 * Get the circle padding percentage around the text
	 *
	 * @return circle padding percentage, 0.0 to 1.0
	 */
	public float getCirclePaddingPercentage() {
		return circlePaddingPercentage;
	}

	/**
	 * Set the circle padding percentage to pad around the text, value between
	 * 0.0 and 1.0
	 *
	 * @param circlePaddingPercentage
	 */
	public void setCirclePaddingPercentage(float circlePaddingPercentage) {
		if (circlePaddingPercentage < 0.0 || circlePaddingPercentage > 1.0) {
			throw new GeoPackageException(
					"Circle padding percentage must be between 0.0 and 1.0: "
							+ circlePaddingPercentage);
		}
		this.circlePaddingPercentage = circlePaddingPercentage;
	}

	/**
	 * Get the tile border stroke width
	 * 
	 * @return tile border stroke width
	 */
	public float getTileBorderStrokeWidth() {
		return tileBorderStrokeWidth;
	}

	/**
	 * Set the tile border stroke width
	 * 
	 * @param tileBorderStrokeWidth
	 *            tile border stroke width
	 */
	public void setTileBorderStrokeWidth(float tileBorderStrokeWidth) {
		this.tileBorderStrokeWidth = tileBorderStrokeWidth;
	}

	/**
	 * Get the tile border color
	 * 
	 * @return tile border color
	 */
	public Color getTileBorderColor() {
		return tileBorderColor;
	}

	/**
	 * Set the tile border color
	 * 
	 * @param tileBorderColor
	 *            tile border color
	 */
	public void setTileBorderColor(Color tileBorderColor) {
		this.tileBorderColor = tileBorderColor;
	}

	/**
	 * Get the tile fill color
	 * 
	 * @return tile fill color
	 */
	public Color getTileFillColor() {
		return tileFillColor;
	}

	/**
	 * Set the tile fill color
	 * 
	 * @param tileFillColor
	 *            tile fill color
	 */
	public void setTileFillColor(Color tileFillColor) {
		this.tileFillColor = tileFillColor;
	}

	/**
	 * Is the draw unindexed tiles option enabled
	 *
	 * @return true if drawing unindexed tiles
	 */
	public boolean isDrawUnindexedTiles() {
		return drawUnindexedTiles;
	}

	/**
	 * Set the draw unindexed tiles option
	 *
	 * @param drawUnindexedTiles
	 */
	public void setDrawUnindexedTiles(boolean drawUnindexedTiles) {
		this.drawUnindexedTiles = drawUnindexedTiles;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage drawTile(int tileWidth, int tileHeight,
			long tileFeatureCount,
			CloseableIterator<GeometryIndex> geometryIndexResults) {

		String featureText = String.valueOf(tileFeatureCount);
		BufferedImage image = drawTile(tileWidth, tileHeight, featureText);

		return image;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage drawUnindexedTile(int tileWidth, int tileHeight,
			long totalFeatureCount, FeatureResultSet allFeatureResults) {

		BufferedImage image = null;

		if (drawUnindexedTiles) {
			// Draw a tile indicating we have no idea if there are features
			// inside.
			// The table is not indexed and more features exist than the max
			// feature count set.
			image = drawTile(tileWidth, tileHeight, "?");
		}

		return image;
	}

	/**
	 * Draw a tile with the provided text label in the middle
	 *
	 * @param tileWidth
	 * @param tileHeight
	 * @param text
	 * @return
	 */
	private BufferedImage drawTile(int tileWidth, int tileHeight, String text) {

		// Create the image and graphics
		BufferedImage image = new BufferedImage(tileWidth, tileHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// Draw the tile fill paint
		if (tileFillColor != null) {
			graphics.setColor(tileFillColor);
			graphics.fillRect(0, 0, tileWidth, tileHeight);
		}

		// Draw the tile border
		if (tileBorderColor != null) {
			graphics.setColor(tileBorderColor);
			graphics.setStroke(new BasicStroke(tileBorderStrokeWidth));
			graphics.drawRect(0, 0, tileWidth, tileHeight);
		}

		// Determine the text bounds
		graphics.setFont(new Font(textFont, Font.PLAIN, textSize));
		FontMetrics fontMetrics = graphics.getFontMetrics();
		int textWidth = fontMetrics.stringWidth(text);
		int textHeight = fontMetrics.getAscent();

		// Determine the center of the tile
		int centerX = (int) (image.getWidth() / 2.0f);
		int centerY = (int) (image.getHeight() / 2.0f);

		// Draw the circle
		if (circleColor != null || circleFillColor != null) {
			int diameter = Math.max(textWidth, textHeight);
			float radius = diameter / 2.0f;
			radius = radius + (diameter * circlePaddingPercentage);
			int paddedDiameter = Math.round(radius * 2);

			int circleX = Math.round(centerX - radius);
			int circleY = Math.round(centerY - radius);

			// Draw the filled circle
			if (circleFillColor != null) {
				graphics.setColor(circleFillColor);
				graphics.setStroke(new BasicStroke(circleStrokeWidth));
				graphics.fillOval(circleX, circleY, paddedDiameter,
						paddedDiameter);
			}

			// Draw the circle
			if (circleColor != null) {
				graphics.setColor(circleColor);
				graphics.setStroke(new BasicStroke(circleStrokeWidth));
				graphics.drawOval(circleX, circleY, paddedDiameter,
						paddedDiameter);
			}

		}

		// Draw the text
		float textX = centerX - (textWidth / 2.0f);
		float textY = centerY + (textHeight / 2.0f);
		graphics.setColor(textColor);
		graphics.drawString(text, textX, textY);

		return image;
	}

}
