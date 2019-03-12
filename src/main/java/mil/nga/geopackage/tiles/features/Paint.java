package mil.nga.geopackage.tiles.features;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

/**
 * Feature Tile drawing paint object
 * 
 * @author osbornb
 * @since 3.2.0
 */
public class Paint {

	/**
	 * Color
	 */
	private Color color;

	/**
	 * Stroke width
	 */
	private float strokeWidth = 1.0f;

	/**
	 * Stroke
	 */
	private Stroke stroke;

	/**
	 * Constructor
	 */
	public Paint() {

	}

	/**
	 * Constructor
	 * 
	 * @param color
	 *            color
	 */
	public Paint(Color color) {
		this.color = color;
	}

	/**
	 * Constructor
	 * 
	 * @param color
	 *            color
	 * @param strokeWidth
	 *            stroke width
	 */
	public Paint(Color color, float strokeWidth) {
		this(color);
		this.strokeWidth = strokeWidth;
	}

	/**
	 * Get the color
	 * 
	 * @return color
	 */
	public Color getColor() {
		return color != null ? color : new Color(0);
	}

	/**
	 * Set the color
	 * 
	 * @param color
	 *            color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Get the stroke width
	 * 
	 * @return stroke width
	 */
	public float getStrokeWidth() {
		return strokeWidth;
	}

	/**
	 * Set the stroke width
	 * 
	 * @param strokeWidth
	 *            stroke width
	 */
	public void setStrokeWidth(Float strokeWidth) {
		this.strokeWidth = strokeWidth;
		stroke = null;
	}

	/**
	 * Get the stroke created from the stroke width
	 * 
	 * @return stroke
	 */
	public Stroke getStroke() {
		Stroke theStroke = stroke;
		if (theStroke == null) {
			theStroke = new BasicStroke(strokeWidth);
			stroke = theStroke;
		}
		return theStroke;
	}

}
