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
	private Styles styles;

	/**
	 * Icon
	 */
	private Icons icons;

	/**
	 * Constructor
	 */
	public FeatureStyle() {

	}

	/**
	 * Constructor
	 * 
	 * @param styles
	 *            styles
	 */
	public FeatureStyle(Styles styles) {
		this(styles, null);
	}

	/**
	 * Constructor
	 * 
	 * @param icons
	 *            icons
	 */
	public FeatureStyle(Icons icons) {
		this(null, icons);
	}

	/**
	 * Constructor
	 * 
	 * @param styles
	 *            styles
	 * @param icons
	 *            icons
	 */
	public FeatureStyle(Styles styles, Icons icons) {
		this.styles = styles;
		this.icons = icons;
	}

	/**
	 * Get the styles
	 * 
	 * @return styles or null
	 */
	public Styles getStyles() {
		return styles;
	}

	/**
	 * Set the styles
	 * 
	 * @param styles
	 *            styles
	 */
	public void setStyles(Styles styles) {
		this.styles = styles;
	}

	/**
	 * Get the icons
	 * 
	 * @return icons or null
	 */
	public Icons getIcons() {
		return icons;
	}

	/**
	 * Set the icons
	 * 
	 * @param icons
	 *            icons
	 */
	public void setIcon(Icons icons) {
		this.icons = icons;
	}

}
