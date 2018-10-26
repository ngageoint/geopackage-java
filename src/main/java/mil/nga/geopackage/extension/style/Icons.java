package mil.nga.geopackage.extension.style;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.nga.sf.GeometryType;

/**
 * Icons for a single feature geometry or feature table default
 * 
 * @author osbornb
 * @since 3.1.1
 */
public class Icons {

	/**
	 * Default icon
	 */
	private IconRow defaultIcon;

	/**
	 * Geometry type to icon mapping
	 */
	private Map<GeometryType, IconRow> icons = new HashMap<>();

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

		if (geometryType != null && !icons.isEmpty()) {
			List<GeometryType> geometryTypes = FeatureStyleExtension
					.getGeometryTypeInheritance(geometryType);
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

		if (iconRow == null && geometryType == null && icons.size() == 1) {
			iconRow = icons.values().iterator().next();
		}

		return iconRow;
	}

	/**
	 * Determine if this icons is empty
	 * 
	 * @return true if empty, false if at least one icon
	 */
	public boolean isEmpty() {
		return defaultIcon == null && icons.isEmpty();
	}

	/**
	 * Determine if there is a default icon
	 * 
	 * @return true if default icon exists
	 */
	public boolean hasDefault() {
		return defaultIcon != null;
	}

}
