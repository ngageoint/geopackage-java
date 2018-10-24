package mil.nga.geopackage.extension.style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.sf.GeometryType;

/**
 * Style for a single feature geometry or feature table default
 * 
 * @author osbornb
 * @since 3.1.1
 */
public class Style {

	/**
	 * Default style
	 */
	private StyleRow defaultStyle;

	/**
	 * Geometry type to style mapping
	 */
	private Map<GeometryType, StyleRow> styles = new HashMap<>();

	/**
	 * Default icon
	 */
	private IconRow defaultIcon;

	/**
	 * Geometry type to icon mapping
	 */
	private Map<GeometryType, IconRow> icons = new HashMap<>();

	/**
	 * Set the default style icon
	 * 
	 * @param styleRow
	 *            default style
	 */
	public void setDefaultStyle(StyleRow styleRow) {
		setStyle(styleRow, null);
	}

	/**
	 * Set the style for the geometry type
	 * 
	 * @param styleRow
	 *            style row
	 * @param geometryType
	 *            geometry type
	 */
	public void setStyle(StyleRow styleRow, GeometryType geometryType) {
		if (geometryType != null) {
			if (styleRow != null) {
				styles.put(geometryType, styleRow);
			} else {
				styles.remove(geometryType);
			}
		} else {
			defaultStyle = styleRow;
		}
	}

	/**
	 * Set the default icon
	 * 
	 * @param iconRow
	 *            default icon
	 */
	public void setDefaultIcon(IconRow iconRow) {
		setIcon(iconRow, null);
	}

	/**
	 * Set the icon for the geometry type
	 * 
	 * @param iconRow
	 *            icon row
	 * @param geometryType
	 *            geometry type
	 */
	public void setIcon(IconRow iconRow, GeometryType geometryType) {
		if (geometryType != null) {
			if (iconRow != null) {
				icons.put(geometryType, iconRow);
			} else {
				icons.remove(geometryType);
			}
		} else {
			defaultIcon = iconRow;
		}
	}

	/**
	 * Default style
	 * 
	 * @return default style
	 */
	public StyleRow getDefaultStyle() {
		return defaultStyle;
	}

	/**
	 * Get an unmodifiable mapping between specific geometry types and styles
	 * 
	 * @return geometry types to style mapping
	 */
	public Map<GeometryType, StyleRow> getStyles() {
		return Collections.unmodifiableMap(styles);
	}

	/**
	 * Get the default icon
	 * 
	 * @return default icon
	 */
	public IconRow getDefaultIcon() {
		return defaultIcon;
	}

	/**
	 * Get an unmodifiable mapping between specific geometry types and icons
	 * 
	 * @return geometry types to icon mapping
	 */
	public Map<GeometryType, IconRow> getIcons() {
		return Collections.unmodifiableMap(icons);
	}

	/**
	 * Get the style, either the default or single geometry type style
	 * 
	 * @return style
	 */
	public StyleRow getStyle() {
		return getStyle(null);
	}

	/**
	 * Get the style for the geometry type
	 * 
	 * @param geometryType
	 *            geometry type
	 * @return style
	 */
	public StyleRow getStyle(GeometryType geometryType) {

		StyleRow styleRow = null;

		if (geometryType != null) {
			List<GeometryType> geometryTypes = getGeometryTypeInheritance(geometryType);
			for (GeometryType type : geometryTypes) {
				styleRow = styles.get(type);
				if (styleRow != null) {
					break;
				}
			}
		}

		if (styleRow == null) {
			styleRow = defaultStyle;
		}

		if (styleRow == null && geometryType == null && styles.size() == 1) {
			styleRow = styles.values().iterator().next();
		}

		return styleRow;
	}

	/**
	 * Get the icon, either the default or single geometry type icon
	 * 
	 * @return style
	 */
	public IconRow getIcon() {
		return getIcon(null);
	}

	/**
	 * Get the icon for the geometry type
	 * 
	 * @param geometryType
	 *            geometry type
	 * @return icon
	 */
	public IconRow getIcon(GeometryType geometryType) {

		IconRow iconRow = null;

		if (geometryType != null) {
			List<GeometryType> geometryTypes = getGeometryTypeInheritance(geometryType);
			for (GeometryType type : geometryTypes) {
				iconRow = icons.get(type);
				if (iconRow != null) {
					break;
				}
			}
		}

		if (iconRow == null) {
			iconRow = defaultIcon;
		}

		if (iconRow == null && geometryType == null && styles.size() == 1) {
			iconRow = icons.values().iterator().next();
		}

		return iconRow;
	}

	/**
	 * Geometry geometry type inheritance starting with the provided geometry
	 * type followed by parent types
	 * 
	 * @param geometryType
	 *            geometry type
	 * @return geometry types
	 */
	private List<GeometryType> getGeometryTypeInheritance(
			GeometryType geometryType) {
		List<GeometryType> geometryTypes = new ArrayList<>();
		if (geometryType != null) {
			geometryTypes.add(geometryType);
			// TODO add parent types
		}
		return geometryTypes;
	}

}
