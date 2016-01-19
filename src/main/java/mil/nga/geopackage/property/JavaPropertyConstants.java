package mil.nga.geopackage.property;

/**
 * GeoPackage Java property constants
 * 
 * @author osbornb
 * @since 1.1.2
 */
public class JavaPropertyConstants {

	/**
	 * Property file name
	 */
	public static final String PROPERTIES_FILE = "geopackage-java.properties";

	/**
	 * Property part divider
	 */
	public static final String PROPERTY_DIVIDER = ".";

	public static final String GEO_PACKAGE = "geopackage";

	public static final String TILE_GENERATOR = GEO_PACKAGE + PROPERTY_DIVIDER
			+ "tile_generator";

	public static final String TILE_GENERATOR_VARIABLE = TILE_GENERATOR
			+ PROPERTY_DIVIDER + "variable";

	public static final String TILE_GENERATOR_VARIABLE_Z = "z";

	public static final String TILE_GENERATOR_VARIABLE_X = "x";

	public static final String TILE_GENERATOR_VARIABLE_Y = "y";

	public static final String TILE_GENERATOR_VARIABLE_MIN_LAT = "min_lat";

	public static final String TILE_GENERATOR_VARIABLE_MAX_LAT = "max_lat";

	public static final String TILE_GENERATOR_VARIABLE_MIN_LON = "min_lon";

	public static final String TILE_GENERATOR_VARIABLE_MAX_LON = "max_lon";

	public static final String TILE_GENERATOR_DOWNLOAD_ATTEMPTS = "downloadAttempts";
	
	public static final String COLOR_RED = "red";

	public static final String COLOR_GREEN = "green";

	public static final String COLOR_BLUE = "blue";

	public static final String COLOR_ALPHA = "alpha";

	public static final String FEATURE_TILES = GEO_PACKAGE + PROPERTY_DIVIDER
			+ "feature_tiles";

	public static final String FEATURE_TILES_TILE_WIDTH = "tile_width";

	public static final String FEATURE_TILES_TILE_HEIGHT = "tile_height";

	public static final String FEATURE_TILES_COMPRESS_FORMAT = "compress_format";

	public static final String FEATURE_TILES_POINT = FEATURE_TILES
			+ PROPERTY_DIVIDER + "point";

	public static final String FEATURE_TILES_LINE = FEATURE_TILES
			+ PROPERTY_DIVIDER + "line";

	public static final String FEATURE_TILES_POLYGON = FEATURE_TILES
			+ PROPERTY_DIVIDER + "polygon";

	public static final String FEATURE_TILES_POLYGON_FILL = FEATURE_TILES
			+ PROPERTY_DIVIDER + "polygon_fill";

	public static final String FEATURE_TILES_RADIUS = "radius";

	public static final String FEATURE_TILES_COLOR = "color";

	public static final String FEATURE_TILES_STROKE_WIDTH = "stroke_width";

	public static final String NUMBER_FEATURES_TILE = GEO_PACKAGE
			+ PROPERTY_DIVIDER + "number_features_tile";

	public static final String NUMBER_FEATURES_TILE_TEXT = NUMBER_FEATURES_TILE
			+ PROPERTY_DIVIDER + "text";

	public static final String NUMBER_FEATURES_TILE_TEXT_SIZE = "size";

	public static final String NUMBER_FEATURES_TILE_TEXT_FONT = "font";

	public static final String NUMBER_FEATURES_TILE_COLOR = "color";

	public static final String NUMBER_FEATURES_TILE_STROKE_WIDTH = "stroke_width";

	public static final String NUMBER_FEATURES_TILE_CIRCLE_DRAW = NUMBER_FEATURES_TILE
			+ PROPERTY_DIVIDER + "circle_draw";

	public static final String NUMBER_FEATURES_TILE_CIRCLE_FILL = NUMBER_FEATURES_TILE
			+ PROPERTY_DIVIDER + "circle_fill";

	public static final String NUMBER_FEATURES_TILE_BORDER = NUMBER_FEATURES_TILE
			+ PROPERTY_DIVIDER + "tile_border";

	public static final String NUMBER_FEATURES_TILE_FILL = NUMBER_FEATURES_TILE
			+ PROPERTY_DIVIDER + "tile_fill";

	public static final String NUMBER_FEATURES_TILE_CIRCLE_PADDING_PERCENTAGE = "circle_padding_percentage";

	public static final String NUMBER_FEATURES_TILE_UNINDEXED_DRAW = "tile_unindexed_draw";

}
