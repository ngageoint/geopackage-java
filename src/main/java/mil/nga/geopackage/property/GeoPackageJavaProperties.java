package mil.nga.geopackage.property;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GeoPackage Java property loader
 * 
 * @author osbornb
 * @since 1.1.2
 */
public class GeoPackageJavaProperties {

	/**
	 * Logger
	 */
	private static final Logger log = Logger
			.getLogger(GeoPackageJavaProperties.class.getName());

	/**
	 * Properties
	 */
	private static Properties mProperties;

	/**
	 * Get a required property by key
	 * 
	 * @param key
	 * @return property value
	 */
	public static String getProperty(String key) {
		return getProperty(key, true);
	}

	/**
	 * Get a property by key
	 * 
	 * @param key
	 * @param required
	 * @return property value
	 */
	public static synchronized String getProperty(String key, boolean required) {
		if (mProperties == null) {
			mProperties = initializeConfigurationProperties();
		}
		String value = mProperties.getProperty(key);
		if (value == null && required) {
			throw new RuntimeException("Property not found: " + key);
		}
		return value;
	}

	/**
	 * Get a required property by base property and property name
	 * 
	 * @param base
	 * @param property
	 * @return property value
	 */
	public static String getProperty(String base, String property) {
		return getProperty(base, property, true);
	}

	/**
	 * Get a property by base property and property name
	 * 
	 * @param base
	 * @param property
	 * @param required
	 * @return property value
	 */
	public static synchronized String getProperty(String base, String property,
			boolean required) {
		return getProperty(base + JavaPropertyConstants.PROPERTY_DIVIDER
				+ property, required);
	}

	/**
	 * Get a required integer property by key
	 * 
	 * @param key
	 * @return property value
	 */
	public static int getIntegerProperty(String key) {
		return getIntegerProperty(key, true);
	}

	/**
	 * Get an integer property by key
	 * 
	 * @param key
	 * @param required
	 * @return property value
	 */
	public static Integer getIntegerProperty(String key, boolean required) {
		Integer value = null;
		String stringValue = getProperty(key, required);
		if (stringValue != null) {
			value = Integer.valueOf(stringValue);
		}
		return value;
	}

	/**
	 * Get a required integer property by base property and property name
	 * 
	 * @param base
	 * @param property
	 * @return property value
	 */
	public static int getIntegerProperty(String base, String property) {
		return getIntegerProperty(base, property, true);
	}

	/**
	 * Get an integer property by base property and property name
	 * 
	 * @param base
	 * @param property
	 * @param required
	 * @return property value
	 */
	public static Integer getIntegerProperty(String base, String property,
			boolean required) {
		return getIntegerProperty(base + JavaPropertyConstants.PROPERTY_DIVIDER
				+ property, required);
	}

	/**
	 * Get a required float by key
	 * 
	 * @param key
	 * @return property value
	 */
	public static float getFloatProperty(String key) {
		return getFloatProperty(key, true);
	}

	/**
	 * Get a float by key
	 * 
	 * @param key
	 * @param required
	 * @return property value
	 */
	public static Float getFloatProperty(String key, boolean required) {
		Float value = null;
		String stringValue = getProperty(key, required);
		if (stringValue != null) {
			value = Float.valueOf(stringValue);
		}
		return value;
	}

	/**
	 * Get a required float property by base property and property name
	 * 
	 * @param base
	 * @param property
	 * @return property value
	 */
	public static float getFloatProperty(String base, String property) {
		return getFloatProperty(base, property, true);
	}

	/**
	 * Get a float property by base property and property name
	 * 
	 * @param base
	 * @param property
	 * @param required
	 * @return property value
	 */
	public static Float getFloatProperty(String base, String property,
			boolean required) {
		return getFloatProperty(base + JavaPropertyConstants.PROPERTY_DIVIDER
				+ property, required);
	}

	/**
	 * Get a required boolean by key
	 * 
	 * @param key
	 * @return property value
	 */
	public static boolean getBooleanProperty(String key) {
		return getBooleanProperty(key, true);
	}

	/**
	 * Get a boolean by key
	 * 
	 * @param key
	 * @param required
	 * @return property value
	 */
	public static Boolean getBooleanProperty(String key, boolean required) {
		Boolean value = null;
		String stringValue = getProperty(key, required);
		if (stringValue != null) {
			value = Boolean.valueOf(stringValue);
		}
		return value;
	}

	/**
	 * Get a required boolean property by base property and property name
	 * 
	 * @param base
	 * @param property
	 * @return property value
	 */
	public static boolean getBooleanProperty(String base, String property) {
		return getBooleanProperty(base, property, true);
	}

	/**
	 * Get a boolean property by base property and property name
	 * 
	 * @param base
	 * @param property
	 * @param required
	 * @return property value
	 */
	public static Boolean getBooleanProperty(String base, String property,
			boolean required) {
		return getBooleanProperty(base + JavaPropertyConstants.PROPERTY_DIVIDER
				+ property, required);
	}

	/**
	 * Get a required color by key
	 * 
	 * @param key
	 * @return property value
	 */
	public static Color getColorProperty(String key) {
		return getColorProperty(key, true);
	}

	/**
	 * Get a color by key
	 * 
	 * @param key
	 * @param required
	 * @return property value
	 */
	public static Color getColorProperty(String key, boolean required) {
		Color value = null;

		String redProperty = key + JavaPropertyConstants.PROPERTY_DIVIDER
				+ JavaPropertyConstants.COLOR_RED;
		String greenProperty = key + JavaPropertyConstants.PROPERTY_DIVIDER
				+ JavaPropertyConstants.COLOR_GREEN;
		String blueProperty = key + JavaPropertyConstants.PROPERTY_DIVIDER
				+ JavaPropertyConstants.COLOR_BLUE;
		String alphaProperty = key + JavaPropertyConstants.PROPERTY_DIVIDER
				+ JavaPropertyConstants.COLOR_ALPHA;

		Integer red = getIntegerProperty(redProperty, required);
		Integer green = getIntegerProperty(greenProperty, required);
		Integer blue = getIntegerProperty(blueProperty, required);
		Integer alpha = getIntegerProperty(alphaProperty, required);

		if (red != null && green != null && blue != null && alpha != null) {
			value = new Color(red, green, blue, alpha);
		}
		return value;
	}

	/**
	 * Get a required color property by base property and property name
	 * 
	 * @param base
	 * @param property
	 * @return property value
	 */
	public static Color getColorProperty(String base, String property) {
		return getColorProperty(base, property, true);
	}

	/**
	 * Get a float property by base property and property name
	 * 
	 * @param base
	 * @param property
	 * @param required
	 * @return property value
	 */
	public static Color getColorProperty(String base, String property,
			boolean required) {
		return getColorProperty(base + JavaPropertyConstants.PROPERTY_DIVIDER
				+ property, required);
	}

	/**
	 * Initialize the configuration properties
	 * 
	 * @return
	 */
	private static Properties initializeConfigurationProperties() {
		Properties properties = new Properties();

		InputStream in = GeoPackageJavaProperties.class.getResourceAsStream("/"
				+ JavaPropertyConstants.PROPERTIES_FILE);
		if (in != null) {
			try {
				properties.load(in);
			} catch (Exception e) {
				log.log(Level.SEVERE, "Failed to load properties file: "
						+ JavaPropertyConstants.PROPERTIES_FILE, e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					log.log(Level.WARNING, "Failed to close properties file: "
							+ JavaPropertyConstants.PROPERTIES_FILE, e);
				}
			}
		} else {
			log.log(Level.SEVERE, "Failed to load properties, file not found: "
					+ JavaPropertyConstants.PROPERTIES_FILE);
		}

		return properties;
	}
}
