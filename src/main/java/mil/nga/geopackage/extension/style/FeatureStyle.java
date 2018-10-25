package mil.nga.geopackage.extension.style;

/**
 * Feature Style, including styles and icons, for a single feature geometry or
 * feature table default
 * 
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureStyle {

	/**
	 * Style
	 */
	private Style style;

	/**
	 * Icon
	 */
	private Icon icon;

	/**
	 * Constructor
	 */
	public FeatureStyle() {

	}

	/**
	 * Constructor
	 * 
	 * @param style
	 *            style
	 */
	public FeatureStyle(Style style) {
		this(style, null);
	}

	/**
	 * Constructor
	 * 
	 * @param icon
	 *            icon
	 */
	public FeatureStyle(Icon icon) {
		this(null, icon);
	}

	/**
	 * Constructor
	 * 
	 * @param style
	 *            style
	 * @param icon
	 *            icon
	 */
	public FeatureStyle(Style style, Icon icon) {
		this.style = style;
		this.icon = icon;
	}

	/**
	 * Get the style
	 * 
	 * @return style or null
	 */
	public Style getStyle() {
		return style;
	}

	/**
	 * Set the style
	 * 
	 * @param style
	 *            style
	 */
	public void setStyle(Style style) {
		this.style = style;
	}

	/**
	 * Get the icon
	 * 
	 * @return icon or null
	 */
	public Icon getIcon() {
		return icon;
	}

	/**
	 * Set the icon
	 * 
	 * @param icon
	 *            icon
	 */
	public void setIcon(Icon icon) {
		this.icon = icon;
	}

}
