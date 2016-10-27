package mil.nga.geopackage.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * Tile properties for GeoPackage formatted tile property files
 * 
 * @author osbornb
 */
public class TileProperties {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(TileProperties.class
			.getName());

	/**
	 * GeoPackage tiles properties file
	 */
	public static final String GEOPACKAGE_PROPERTIES_FILE = "tiles.properties";

	/**
	 * SRS ID property
	 */
	public static final String GEOPACKAGE_PROPERTIES_EPSG = "epsg";

	/**
	 * MIN X property
	 */
	public static final String GEOPACKAGE_PROPERTIES_MIN_X = "min_x";

	/**
	 * MAX X property
	 */
	public static final String GEOPACKAGE_PROPERTIES_MAX_X = "max_x";

	/**
	 * MIN Y property
	 */
	public static final String GEOPACKAGE_PROPERTIES_MIN_Y = "min_y";

	/**
	 * MAX Y property
	 */
	public static final String GEOPACKAGE_PROPERTIES_MAX_Y = "max_y";

	/**
	 * Zoom Level property
	 */
	public static final String GEOPACKAGE_PROPERTIES_ZOOM_LEVEL = "zoom_level";

	/**
	 * Matrix Width property
	 */
	public static final String GEOPACKAGE_PROPERTIES_MATRIX_WIDTH = "matrix_width";

	/**
	 * Matrix Height property
	 */
	public static final String GEOPACKAGE_PROPERTIES_MATRIX_HEIGHT = "matrix_height";

	/**
	 * Properties file to read or write
	 */
	private File propertiesFile;

	/**
	 * Loaded Properties
	 */
	private Properties properties;

	/**
	 * Constructor
	 * 
	 * @param directory
	 */
	public TileProperties(File directory) {
		this.propertiesFile = new File(directory, GEOPACKAGE_PROPERTIES_FILE);
	}

	/**
	 * Load the properties
	 */
	public void load() {
		properties = new Properties();

		try {
			InputStream in = new FileInputStream(propertiesFile);

			try {
				properties.load(in);
			} catch (Exception e) {
				throw new GeoPackageException(
						"Failed to load properties file for GeoPackage file format located at: "
								+ propertiesFile, e);
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.log(Level.WARNING,
							"Failed to close properties file: "
									+ propertiesFile, e);
				}
			}
		} catch (FileNotFoundException e) {
			throw new GeoPackageException(
					"GeoPackage file format requires a properties file located at: "
							+ propertiesFile, e);
		}
	}

	/**
	 * Get the Integer property
	 * 
	 * @param property
	 * @param required
	 * @return integer property
	 */
	public Integer getIntegerProperty(String property, boolean required) {
		Integer integerValue = null;
		String value = getProperty(property, required);
		if (value != null) {
			try {
				integerValue = Integer.valueOf(value);
			} catch (NumberFormatException e) {
				throw new GeoPackageException(GEOPACKAGE_PROPERTIES_FILE
						+ " property file property '" + property
						+ "' must be an integer");
			}
		}
		return integerValue;
	}

	/**
	 * Get the Double property
	 * 
	 * @param property
	 * @param required
	 * @return double property
	 */
	public Double getDoubleProperty(String property, boolean required) {
		Double doubleValue = null;
		String value = getProperty(property, required);
		if (value != null) {
			try {
				doubleValue = Double.valueOf(value);
			} catch (NumberFormatException e) {
				throw new GeoPackageException(GEOPACKAGE_PROPERTIES_FILE
						+ " property file property '" + property
						+ "' must be a double");
			}
		}
		return doubleValue;
	}

	/**
	 * Get the String property
	 * 
	 * @param property
	 * @param required
	 * @return string property
	 */
	public String getProperty(String property, boolean required) {

		if (properties == null) {
			throw new GeoPackageException(
					"Properties must be loaded before reading");
		}

		String value = properties.getProperty(property);
		if (value == null && required) {
			throw new GeoPackageException(GEOPACKAGE_PROPERTIES_FILE
					+ " property file missing required property: " + property);
		}
		return value;
	}

	/**
	 * Write the properties file using the tile dao
	 * 
	 * @param tileDao
	 */
	public void writeFile(TileDao tileDao) {
		try {
			PrintWriter pw = new PrintWriter(propertiesFile);

			TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();
			pw.println(GEOPACKAGE_PROPERTIES_EPSG + "="
					+ tileMatrixSet.getSrs().getOrganizationCoordsysId());
			pw.println(GEOPACKAGE_PROPERTIES_MIN_X + "="
					+ tileMatrixSet.getMinX());
			pw.println(GEOPACKAGE_PROPERTIES_MAX_X + "="
					+ tileMatrixSet.getMaxX());
			pw.println(GEOPACKAGE_PROPERTIES_MIN_Y + "="
					+ tileMatrixSet.getMinY());
			pw.println(GEOPACKAGE_PROPERTIES_MAX_Y + "="
					+ tileMatrixSet.getMaxY());

			for (TileMatrix tileMatrix : tileDao.getTileMatrices()) {
				long zoom = tileMatrix.getZoomLevel();
				pw.println(getMatrixWidthProperty(zoom) + "="
						+ tileMatrix.getMatrixWidth());
				pw.println(getMatrixHeightProperty(zoom) + "="
						+ tileMatrix.getMatrixHeight());
			}

			pw.close();

		} catch (FileNotFoundException e) {
			throw new GeoPackageException(
					"GeoPackage file format properties file could not be created: "
							+ propertiesFile, e);
		}
	}

	/**
	 * Get the matrix width property for the zoom level
	 * 
	 * @param zoom
	 * @return matrix width property
	 */
	public static String getMatrixWidthProperty(long zoom) {
		return getMatrixWidthProperty(String.valueOf(zoom));
	}

	/**
	 * Get the matrix width property for the zoom level
	 * 
	 * @param zoom
	 * @return matrix width property
	 */
	public static String getMatrixWidthProperty(String zoom) {
		return GEOPACKAGE_PROPERTIES_ZOOM_LEVEL + "." + zoom + "."
				+ GEOPACKAGE_PROPERTIES_MATRIX_WIDTH;
	}

	/**
	 * Get the matrix height property for the zoom level
	 * 
	 * @param zoom
	 * @return matrix height property
	 */
	public static String getMatrixHeightProperty(long zoom) {
		return getMatrixHeightProperty(String.valueOf(zoom));
	}

	/**
	 * Get the matrix height property for the zoom level
	 * 
	 * @param zoom
	 * @return matrix height property
	 */
	public static String getMatrixHeightProperty(String zoom) {
		return GEOPACKAGE_PROPERTIES_ZOOM_LEVEL + "." + zoom + "."
				+ GEOPACKAGE_PROPERTIES_MATRIX_HEIGHT;
	}

}
