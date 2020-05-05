package mil.nga.geopackage.io;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.locationtech.proj4j.units.Units;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.nga.index.FeatureTableIndex;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.features.DefaultFeatureTiles;
import mil.nga.geopackage.tiles.features.FeatureTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTilePointIcon;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;
import mil.nga.sf.proj.ProjectionTransform;

/**
 * Feature Tile Generator main method for command line feature to tile
 * generation. Generate tiles from a feature table.
 * 
 * To run from command line, build with the standalone profile:
 * 
 * mvn clean install -Pstandalone
 * 
 * java -classpath geopackage-*-standalone.jar
 * mil.nga.geopackage.io.FeatureTileGen +usage_arguments
 * 
 * @author osbornb
 * @since 1.1.2
 */
public class FeatureTileGen {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(FeatureTileGen.class.getName());

	/**
	 * Log index frequency for how often to log indexed features
	 */
	private static final int LOG_INDEX_FREQUENCY = 1000;

	/**
	 * Log index frequency in seconds for how often to log indexed features
	 */
	private static final int LOG_INDEX_TIME_FREQUENCY = 10;

	/**
	 * Log tile frequency for how often to log tile generation progress
	 */
	private static final int LOG_TILE_FREQUENCY = 1000;

	/**
	 * Log tile frequency in seconds for how often to log tile generation
	 * progress
	 */
	private static final int LOG_TILE_TIME_FREQUENCY = 60;

	/**
	 * Default max features per tile value
	 */
	private static final int DEFAULT_MAX_FEATURES_PER_TILE = 5000;

	/**
	 * Argument prefix
	 */
	public static final String ARGUMENT_PREFIX = "-";

	/**
	 * Max features per tile argument
	 */
	public static final String ARGUMENT_MAX_FEATURES_PER_TILE = "m";

	/**
	 * Compress format argument
	 */
	public static final String ARGUMENT_COMPRESS_FORMAT = "f";

	/**
	 * Compress quality argument
	 */
	public static final String ARGUMENT_COMPRESS_QUALITY = "q";

	/**
	 * XYZ Tiles format argument
	 */
	public static final String ARGUMENT_XYZ_TILES = "xyz";

	/**
	 * Bounding box argument
	 */
	public static final String ARGUMENT_BOUNDING_BOX = "bbox";

	/**
	 * EPSG argument
	 */
	public static final String ARGUMENT_EPSG = "epsg";

	/**
	 * Tile draw width argument
	 */
	public static final String ARGUMENT_TILE_WIDTH = "tileWidth";

	/**
	 * Tile draw height argument
	 */
	public static final String ARGUMENT_TILE_HEIGHT = "tileHeight";

	/**
	 * Tile scale factor argument
	 */
	public static final String ARGUMENT_TILE_SCALE = "tileScale";

	/**
	 * Point radius argument
	 */
	public static final String ARGUMENT_POINT_RADIUS = "pointRadius";

	/**
	 * Point color argument
	 */
	public static final String ARGUMENT_POINT_COLOR = "pointColor";

	/**
	 * Point icon argument
	 */
	public static final String ARGUMENT_POINT_ICON = "pointIcon";

	/**
	 * Point icon display width argument
	 */
	public static final String ARGUMENT_ICON_WIDTH = "iconWidth";

	/**
	 * Point icon display height argument
	 */
	public static final String ARGUMENT_ICON_HEIGHT = "iconHeight";

	/**
	 * Center icon argument
	 */
	public static final String ARGUMENT_POINT_CENTER_ICON = "centerIcon";

	/**
	 * Line stroke width argument
	 */
	public static final String ARGUMENT_LINE_STROKE_WIDTH = "lineStrokeWidth";

	/**
	 * Line color argument
	 */
	public static final String ARGUMENT_LINE_COLOR = "lineColor";

	/**
	 * Polygon stroke width argument
	 */
	public static final String ARGUMENT_POLYGON_STROKE_WIDTH = "polygonStrokeWidth";

	/**
	 * Polygon color argument
	 */
	public static final String ARGUMENT_POLYGON_COLOR = "polygonColor";

	/**
	 * Fill polygon argument
	 */
	public static final String ARGUMENT_FILL_POLYGON = "fillPolygon";

	/**
	 * Polygon fill color argument
	 */
	public static final String ARGUMENT_POLYGON_FILL_COLOR = "polygonFillColor";

	/**
	 * Simplify geometries argument
	 */
	public static final String ARGUMENT_SIMPLIFY_GEOMETRIES = "simplifyGeometries";

	/**
	 * Ignore GeoPackage styles argument
	 */
	public static final String ARGUMENT_IGNORE_GEOPACKAGE_STYLES = "ignoreGeoPackageStyles";

	/**
	 * Log Frequency Count argument
	 * 
	 * @since 3.5.0
	 */
	public static final String ARGUMENT_LOG_COUNT = "logCount";

	/**
	 * Log Frequency Time argument
	 * 
	 * @since 3.5.0
	 */
	public static final String ARGUMENT_LOG_TIME = "logTime";

	/**
	 * Tile progress
	 */
	private static ZoomLevelProgress progress = new ZoomLevelProgress(
			"Feature Tile Generation", "tiles", LOG_TILE_FREQUENCY,
			LOG_TILE_TIME_FREQUENCY);

	/**
	 * Feature GeoPackage file
	 */
	private static File featureGeoPackageFile = null;

	/**
	 * Feature GeoPackage
	 */
	private static GeoPackage featureGeoPackage = null;

	/**
	 * Feature Table name
	 */
	private static String featureTable = null;

	/**
	 * Tile GeoPackage file
	 */
	private static File tileGeoPackageFile = null;

	/**
	 * Tile GeoPackage
	 */
	private static GeoPackage tileGeoPackage = null;

	/**
	 * Tile Table name
	 */
	private static String tileTable = null;

	/**
	 * Min Zoom
	 */
	private static Integer minZoom = null;

	/**
	 * Max Zoom
	 */
	private static Integer maxZoom = null;

	/**
	 * Max features per tile
	 */
	private static Integer maxFeaturesPerTile = DEFAULT_MAX_FEATURES_PER_TILE;

	/**
	 * Compress Format
	 */
	private static String compressFormat = null;

	/**
	 * Compress Quality
	 */
	private static Float compressQuality = null;

	/**
	 * XYZ tiles flag
	 */
	private static boolean xyzTiles = false;

	/**
	 * Bounding box
	 */
	private static BoundingBox boundingBox = null;

	/**
	 * Bounding Box EPSG
	 */
	private static Long epsg = null;

	/**
	 * Tile draw width
	 */
	private static Integer tileWidth = null;

	/**
	 * Tile draw height
	 */
	private static Integer tileHeight = null;

	/**
	 * Tile scale
	 */
	private static Float tileScale = null;

	/**
	 * Point radius
	 */
	private static Float pointRadius = null;

	/**
	 * Point color
	 */
	private static Color pointColor = null;

	/**
	 * Feature tile point icon
	 */
	private static FeatureTilePointIcon icon = null;

	/**
	 * Feature tile point icon display width
	 */
	private static Integer iconWidth = null;

	/**
	 * Feature tile point icon display height
	 */
	private static Integer iconHeight = null;

	/**
	 * Center icon flag
	 */
	private static boolean centerIcon = false;

	/**
	 * Line stroke width
	 */
	private static Float lineStrokeWidth = null;

	/**
	 * Line color
	 */
	private static Color lineColor = null;

	/**
	 * Polygon stroke width
	 */
	private static Float polygonStrokeWidth = null;

	/**
	 * Polygon color
	 */
	private static Color polygonColor = null;

	/**
	 * Fill polygon
	 */
	private static Boolean fillPolygon = null;

	/**
	 * Polygon fill color
	 */
	private static Color polygonFillColor = null;

	/**
	 * Simplify geometries
	 */
	private static boolean simplifyGeometries = true;

	/**
	 * Ignore styles saved within the GeoPackage
	 */
	private static boolean ignoreGeoPackageStyles = false;

	/**
	 * Main method to generate tiles in a GeoPackage
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {

		// Add a shutdown hook
		final Thread mainThread = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				progress.cancel();
				try {
					mainThread.join();
				} catch (InterruptedException e) {
					LOGGER.log(Level.WARNING,
							"Failed to wait for the main thread to finish", e);
				}
			}
		});

		boolean valid = true;
		boolean requiredArguments = false;

		for (int i = 0; valid && i < args.length; i++) {

			String arg = args[i];

			// Handle optional arguments
			if (arg.startsWith(ARGUMENT_PREFIX)) {

				String argument = arg.substring(ARGUMENT_PREFIX.length());

				switch (argument) {

				case ARGUMENT_MAX_FEATURES_PER_TILE:
					if (i < args.length) {
						int max = Integer.valueOf(args[++i]);
						if (max >= 0) {
							maxFeaturesPerTile = max;
						} else {
							maxFeaturesPerTile = null;
						}
					} else {
						valid = false;
						System.out.println(
								"Error: Max Features Per Tile argument '" + arg
										+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_COMPRESS_FORMAT:
					if (i < args.length) {
						compressFormat = args[++i];
					} else {
						valid = false;
						System.out.println("Error: Compress Format argument '"
								+ arg
								+ "' must be followed by an image format");
					}
					break;

				case ARGUMENT_COMPRESS_QUALITY:
					if (i < args.length) {
						compressQuality = Float.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Compress Quality argument '"
								+ arg
								+ "' must be followed by a value between 0.0 and 1.0");
					}
					break;

				case ARGUMENT_XYZ_TILES:
					xyzTiles = true;
					break;

				case ARGUMENT_BOUNDING_BOX:
					if (i < args.length) {
						String bbox = args[++i];
						String[] bboxParts = bbox.split(",");
						if (bboxParts.length != 4) {
							valid = false;
							System.out.println("Error: Bounding Box argument '"
									+ arg
									+ "' value must be in the format: minLon,minLat,maxLon,maxLat");
						} else {
							double minLon = Double.valueOf(bboxParts[0]);
							double minLat = Double.valueOf(bboxParts[1]);
							double maxLon = Double.valueOf(bboxParts[2]);
							double maxLat = Double.valueOf(bboxParts[3]);
							boundingBox = new BoundingBox(minLon, minLat,
									maxLon, maxLat);
						}
					} else {
						valid = false;
						System.out.println("Error: Bounding Box argument '"
								+ arg
								+ "' must be followed by bbox values: minLon,minLat,maxLon,maxLat");
					}
					break;

				case ARGUMENT_EPSG:
					if (i < args.length) {
						epsg = Long.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: EPSG argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_TILE_WIDTH:
					if (i < args.length) {
						tileWidth = Integer.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Tile Width argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_TILE_HEIGHT:
					if (i < args.length) {
						tileHeight = Integer.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Tile Height argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_TILE_SCALE:
					if (i < args.length) {
						tileScale = Float.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Tile Scale argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_POINT_RADIUS:
					if (i < args.length) {
						pointRadius = Float.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Point Radius argument '"
								+ arg + "' must be followed by a value");
					}
					break;

				case ARGUMENT_POINT_COLOR:
					if (i < args.length) {
						pointColor = getColor(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Point Color argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_POINT_ICON:
					if (i < args.length) {
						File pointIconFile = new File(args[++i]);
						try {
							BufferedImage iconImage = ImageIO
									.read(pointIconFile);
							icon = new FeatureTilePointIcon(iconImage);
						} catch (IOException e) {
							throw new GeoPackageException(
									"Failed to create point icon from image file: "
											+ pointIconFile.getAbsolutePath(),
									e);
						}
					} else {
						valid = false;
						System.out.println("Error: Point Icon argument '" + arg
								+ "' must be followed by a point image file");
					}
					break;

				case ARGUMENT_ICON_WIDTH:
					if (i < args.length) {
						iconWidth = Integer.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Icon Width argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_ICON_HEIGHT:
					if (i < args.length) {
						iconHeight = Integer.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Icon Height argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_POINT_CENTER_ICON:
					centerIcon = true;
					break;

				case ARGUMENT_LINE_STROKE_WIDTH:
					if (i < args.length) {
						lineStrokeWidth = Float.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Line Stroke Width argument '"
								+ arg + "' must be followed by a value");
					}
					break;

				case ARGUMENT_LINE_COLOR:
					if (i < args.length) {
						lineColor = getColor(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Line Color argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_POLYGON_STROKE_WIDTH:
					if (i < args.length) {
						polygonStrokeWidth = Float.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println(
								"Error: Polygon Stroke Width argument '" + arg
										+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_POLYGON_COLOR:
					if (i < args.length) {
						polygonColor = getColor(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Polygon Color argument '"
								+ arg + "' must be followed by a value");
					}
					break;

				case ARGUMENT_FILL_POLYGON:
					fillPolygon = true;
					break;

				case ARGUMENT_POLYGON_FILL_COLOR:
					if (i < args.length) {
						polygonFillColor = getColor(args[++i]);
					} else {
						valid = false;
						System.out.println(
								"Error: Polygon Fill Color argument '" + arg
										+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_SIMPLIFY_GEOMETRIES:
					if (i < args.length) {
						simplifyGeometries = Boolean.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println(
								"Error: Simplify Geometries argument '" + arg
										+ "' must be followed by a boolean value");
					}
					break;

				case ARGUMENT_IGNORE_GEOPACKAGE_STYLES:
					if (i < args.length) {
						ignoreGeoPackageStyles = Boolean.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println(
								"Error: Ignore GeoPackage Styles argument '"
										+ arg
										+ "' must be followed by a boolean value");
					}
					break;

				case ARGUMENT_LOG_COUNT:
					if (i < args.length) {
						progress.setCountFrequency(Integer.valueOf(args[++i]));
					} else {
						valid = false;
						System.out.println("Error: Log Count argument '" + arg
								+ "' must be followed by a frequency count value");
					}
					break;

				case ARGUMENT_LOG_TIME:
					if (i < args.length) {
						progress.setTimeFrequency(Integer.valueOf(args[++i]));
					} else {
						valid = false;
						System.out.println("Error: Log Time argument '" + arg
								+ "' must be followed by a frequency time value in seconds");
					}
					break;

				default:
					valid = false;
					System.out.println("Error: Unsupported arg: '" + arg + "'");
				}

			} else {
				// Set required arguments in order
				if (featureGeoPackageFile == null) {
					featureGeoPackageFile = new File(arg);
				} else if (featureTable == null) {
					featureTable = arg;
				} else if (tileGeoPackageFile == null) {
					tileGeoPackageFile = new File(arg);
				} else if (tileTable == null) {
					tileTable = arg;
				} else if (minZoom == null) {
					minZoom = Integer.valueOf(arg);
				} else if (maxZoom == null) {
					maxZoom = Integer.valueOf(arg);
					requiredArguments = true;
				} else {
					valid = false;
					System.out.println(
							"Error: Unsupported extra argument: " + arg);
				}
			}
		}

		if (compressFormat == null && compressQuality != null) {
			System.out.println(
					"Error: Compress quality requires a compress format");
			valid = false;
		} else if (boundingBox == null && epsg != null) {
			System.out.println("Error: EPSG requires a bounding box");
			valid = false;
		}

		if ((pointRadius != null || pointColor != null) && icon != null) {
			System.out.println(
					"Error: Point radius and/or color can not be specified together with a point icon");
			valid = false;
		}

		if (iconWidth != null || iconHeight != null) {
			if (icon == null) {
				System.out.println(
						"Error: Point icon file must be specified when attempting to set an icon width or height");
				valid = false;
			} else {

				if (iconWidth == null) {
					iconWidth = Math.round(icon.getWidth()
							* (iconHeight / (float) icon.getHeight()));
				} else if (iconHeight == null) {
					iconHeight = Math.round(icon.getHeight()
							* (iconWidth / (float) icon.getWidth()));
				}

				icon.setWidth(iconWidth);
				icon.setHeight(iconHeight);
			}
		}

		if (centerIcon) {
			if (icon == null) {
				System.out.println(
						"Error: Point icon file must be specified when attempting to center it");
				valid = false;
			} else {
				icon.centerIcon();
			}
		}

		if ((fillPolygon == null || !fillPolygon) && polygonFillColor != null) {
			System.out.println(
					"Error: Polygon Fill Color can only be specified when Fill Polygon is enabled");
			valid = false;
		}

		if (!valid || !requiredArguments) {
			printUsage();
		} else {
			// Read the tiles
			try {
				generate();
			} catch (Exception e) {
				printUsage();
				throw e;
			}
		}
	}

	/**
	 * Get the color from the string
	 * 
	 * @param colorString
	 * @return
	 */
	private static Color getColor(String colorString) {

		Color color = null;

		String[] colorParts = colorString.split(",");

		try {

			switch (colorParts.length) {

			case 1:
				Field field = Color.class.getField(colorString);
				color = (Color) field.get(null);
				break;

			case 3:
				color = new Color(Integer.parseInt(colorParts[0]),
						Integer.parseInt(colorParts[1]),
						Integer.parseInt(colorParts[2]));
				break;

			case 4:
				color = new Color(Integer.parseInt(colorParts[0]),
						Integer.parseInt(colorParts[1]),
						Integer.parseInt(colorParts[2]),
						Integer.parseInt(colorParts[3]));
				break;

			default:
				throw new GeoPackageException("Unexpected color arguments: "
						+ colorParts.length + ", color: " + colorString);
			}

		} catch (Exception e) {
			throw new GeoPackageException("Invalid color: " + colorString
					+ ", Allowable Formats: colorName | r,g,b | r,g,b,a", e);
		}

		return color;
	}

	/**
	 * Generate the tiles
	 */
	public static void generate() {

		// Open the GeoPackage with the feature table
		featureGeoPackage = GeoPackageManager.open(featureGeoPackageFile);

		// Check if the tile table is to be created in the same GeoPackage file
		if (featureGeoPackageFile.equals(tileGeoPackageFile)) {
			tileGeoPackage = featureGeoPackage;
		} else {

			// If the GeoPackage does not exist create it
			if (!tileGeoPackageFile.exists()) {
				if (!GeoPackageManager.create(tileGeoPackageFile)) {
					throw new GeoPackageException(
							"Failed to create GeoPackage file: "
									+ tileGeoPackageFile.getAbsolutePath());
				}
			}

			// Open the GeoPackage
			tileGeoPackage = GeoPackageManager.open(tileGeoPackageFile);
		}

		FeatureDao featureDao = featureGeoPackage.getFeatureDao(featureTable);

		// Index the feature table if needed
		FeatureTableIndex featureIndex = new FeatureTableIndex(
				featureGeoPackage, featureDao);
		if (!featureIndex.isIndexed()) {
			int numFeatures = featureDao.count();
			LOGGER.log(Level.INFO,
					"Indexing GeoPackage '" + featureGeoPackage.getName()
							+ "' feature table '" + featureTable + "' with "
							+ numFeatures + " features");
			ZoomLevelProgress indexProgress = new ZoomLevelProgress(
					"Feature Indexer", "features", LOG_INDEX_FREQUENCY,
					LOG_INDEX_TIME_FREQUENCY);
			indexProgress.setMax(numFeatures);
			featureIndex.setProgress(indexProgress);
			int indexed = featureIndex.index();
			LOGGER.log(Level.INFO,
					"Indexed GeoPackage '" + featureGeoPackage.getName()
							+ "' feature table '" + featureTable + "', "
							+ indexed + " features");
		}
		featureIndex.close();

		// Create the feature tiles
		FeatureTiles featureTiles = new DefaultFeatureTiles(featureGeoPackage,
				featureDao);
		if (ignoreGeoPackageStyles) {
			featureTiles.ignoreFeatureTableStyles();
		}
		if (maxFeaturesPerTile != null) {
			featureTiles.setMaxFeaturesPerTile(maxFeaturesPerTile);
			featureTiles.setMaxFeaturesTileDraw(new NumberFeaturesTile());
		}

		// Set the tile styles
		if (tileWidth != null) {
			featureTiles.setTileWidth(tileWidth);
		}
		if (tileHeight != null) {
			featureTiles.setTileHeight(tileHeight);
		}
		if (tileScale != null) {
			featureTiles.setScale(tileScale);
		}
		if (pointRadius != null) {
			featureTiles.setPointRadius(pointRadius);
		}
		if (pointColor != null) {
			featureTiles.setPointColor(pointColor);
		}
		if (icon != null) {
			featureTiles.setPointIcon(icon);
		}
		if (lineStrokeWidth != null) {
			featureTiles.setLineStrokeWidth(lineStrokeWidth);
		}
		if (lineColor != null) {
			featureTiles.setLineColor(lineColor);
		}
		if (polygonStrokeWidth != null) {
			featureTiles.setPolygonStrokeWidth(polygonStrokeWidth);
		}
		if (polygonColor != null) {
			featureTiles.setPolygonColor(polygonColor);
		}
		if (fillPolygon != null) {
			featureTiles.setFillPolygon(fillPolygon);
		}
		if (polygonFillColor != null) {
			featureTiles.setPolygonFillColor(polygonFillColor);
		}
		featureTiles.setSimplifyGeometries(simplifyGeometries);

		// Calculate the tile overlap with the new settings
		featureTiles.calculateDrawOverlap();

		// Default the EPSG
		if (epsg == null) {
			epsg = new Long(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
		}

		BoundingBox webMercatorBoundingBox = null;
		Projection webMercatorProjection = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);

		// Set the projection and default bounding box as needed
		Projection projection = null;
		if (boundingBox != null) {

			projection = ProjectionFactory.getProjection(epsg);

			// Bound WGS84 tiles to Web Mercator limits
			if (projection.isUnit(Units.DEGREES)) {
				boundingBox = TileBoundingBoxUtils
						.boundDegreesBoundingBoxWithWebMercatorLimits(
								boundingBox);
			}

			// Transform to a Web Mercator bounding box
			ProjectionTransform transform = projection
					.getTransformation(webMercatorProjection);
			webMercatorBoundingBox = boundingBox.transform(transform);

		} else {
			projection = ProjectionFactory.getProjection(
					ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
		}

		// Create the tile generator
		FeatureTileGenerator tileGenerator = new FeatureTileGenerator(
				tileGeoPackage, tileTable, featureTiles, featureGeoPackage,
				minZoom, maxZoom, webMercatorBoundingBox,
				webMercatorProjection);

		if (compressFormat != null) {
			tileGenerator.setCompressFormat(compressFormat);
			if (compressQuality != null) {
				tileGenerator.setCompressQuality(compressQuality);
			}
		}

		if (xyzTiles) {
			tileGenerator.setXYZTiles(true);
		}

		int count = tileGenerator.getTileCount();

		System.out.println();
		System.out
				.println("Feature GeoPackage: " + featureGeoPackage.getName());
		System.out.println("Feature Table: " + featureTable);
		System.out.println("Tile GeoPackage: " + tileGeoPackage.getName());
		System.out.println("Tile Table: " + tileTable);
		System.out.println("Min Zoom: " + minZoom);
		System.out.println("Max Zoom: " + maxZoom);
		if (maxFeaturesPerTile != null) {
			System.out.println("Max Features Per Tile: " + maxFeaturesPerTile);
		}
		if (compressFormat != null) {
			System.out.println("Compress Format: " + compressFormat);
		}
		if (compressQuality != null) {
			System.out.println("Compress Quality: " + compressQuality);
		}
		if (xyzTiles) {
			System.out.println("Save as XYZ Tiles: true");
		}
		if (boundingBox != null) {
			System.out.println("Bounding Box:");
			System.out.println("\tMin Lon: " + boundingBox.getMinLongitude());
			System.out.println("\tMin Lat: " + boundingBox.getMinLatitude());
			System.out.println("\tMax Lon: " + boundingBox.getMaxLongitude());
			System.out.println("\tMax Lat: " + boundingBox.getMaxLatitude());
		}
		if (epsg != null) {
			System.out.println("EPSG: " + epsg);
		}
		System.out.println("Log Count Frequency: "
				+ progress.getCountFrequency() + " tiles");
		System.out.println("Log Time Frequency: " + progress.getTimeFrequency()
				+ " seconds");
		System.out.println("Expected Tile Count: " + count);
		System.out.println();

		StringBuilder tileStyle = new StringBuilder();
		if (tileWidth != null) {
			tileStyle.append(", Width: ").append(tileWidth);
		}
		if (tileHeight != null) {
			tileStyle.append(", Height: ").append(tileHeight);
		}
		if (pointRadius != null) {
			tileStyle.append(", Point Radius: ").append(pointRadius);
		}
		if (pointColor != null) {
			tileStyle.append(", Point Color: ").append(colorString(pointColor));
		}
		if (icon != null) {
			tileStyle.append(", Point Icon (height=").append(icon.getHeight())
					.append(", width=").append(icon.getWidth())
					.append(", xoffset=").append(icon.getXOffset())
					.append(", yoffset=").append(icon.getYOffset()).append(")");
		}
		if (lineStrokeWidth != null) {
			tileStyle.append(", Line Stroke Width: ").append(lineStrokeWidth);
		}
		if (lineColor != null) {
			tileStyle.append(", Line Color: ").append(colorString(lineColor));
		}
		if (polygonStrokeWidth != null) {
			tileStyle.append(", Polygon Stroke Width: ")
					.append(polygonStrokeWidth);
		}
		if (polygonColor != null) {
			tileStyle.append(", Polygon Color: ")
					.append(colorString(polygonColor));
		}
		if (fillPolygon != null && fillPolygon) {
			tileStyle.append(", Fill Polygon");
		}
		if (polygonFillColor != null) {
			tileStyle.append(", Polygon Fill Color: ")
					.append(colorString(polygonFillColor));
		}
		if (simplifyGeometries) {
			tileStyle.append(", Simplify Geometries");
		}
		if (tileStyle.length() == 0) {
			tileStyle.append(", Default Settings");
		}
		LOGGER.log(Level.INFO, "Tile Attributes" + tileStyle);

		tileGenerator.setProgress(progress);

		LOGGER.log(Level.INFO, "Generating Tiles...");

		try {
			tileGenerator.generateTiles();
		} catch (IOException | SQLException e) {
			throw new GeoPackageException("Exception while generating tiles",
					e);
		}

		finish();
	}

	/**
	 * Finish tile generation
	 */
	private static void finish() {

		if (progress.getMax() != null) {

			StringBuilder output = new StringBuilder();
			output.append("\nTile Generation: ").append(progress.getProgress())
					.append(" of ").append(progress.getMax());

			if (tileGeoPackage != null) {
				try {
					GeoPackageTextOutput textOutput = new GeoPackageTextOutput(
							tileGeoPackage);
					output.append("\n\n");
					output.append(textOutput.header());
					output.append("\n\n");
					output.append(textOutput.tileTable(tileTable));

				} finally {
					tileGeoPackage.close();
				}
			}

			if (featureGeoPackage != null) {
				featureGeoPackage.close();
			}

			System.out.println(output.toString());
		}
	}

	/**
	 * Print usage for the main method
	 */
	private static void printUsage() {
		FeatureTiles featureTiles = new DefaultFeatureTiles(null);
		System.out.println();
		System.out.println("USAGE");
		System.out.println();
		System.out.println("\t[" + ARGUMENT_PREFIX
				+ ARGUMENT_MAX_FEATURES_PER_TILE + " max_features_per_tile] ["
				+ ARGUMENT_PREFIX + ARGUMENT_COMPRESS_FORMAT
				+ " compress_format] [" + ARGUMENT_PREFIX
				+ ARGUMENT_COMPRESS_QUALITY + " compress_quality] ["
				+ ARGUMENT_PREFIX + ARGUMENT_XYZ_TILES + "] [" + ARGUMENT_PREFIX
				+ ARGUMENT_BOUNDING_BOX + " minLon,minLat,maxLon,maxLat] ["
				+ ARGUMENT_PREFIX + ARGUMENT_EPSG + " epsg] [" + ARGUMENT_PREFIX
				+ ARGUMENT_TILE_WIDTH + " width] [" + ARGUMENT_PREFIX
				+ ARGUMENT_TILE_HEIGHT + " height] [" + ARGUMENT_PREFIX
				+ ARGUMENT_TILE_SCALE + " scale] [" + ARGUMENT_PREFIX
				+ ARGUMENT_POINT_RADIUS + " radius] [" + ARGUMENT_PREFIX
				+ ARGUMENT_POINT_COLOR + " color] [" + ARGUMENT_PREFIX
				+ ARGUMENT_POINT_ICON + " image_file] [" + ARGUMENT_PREFIX
				+ ARGUMENT_ICON_WIDTH + " width] [" + ARGUMENT_PREFIX
				+ ARGUMENT_ICON_HEIGHT + " height] [" + ARGUMENT_PREFIX
				+ ARGUMENT_POINT_CENTER_ICON + "] [" + ARGUMENT_PREFIX
				+ ARGUMENT_LINE_STROKE_WIDTH + " stroke_width] ["
				+ ARGUMENT_PREFIX + ARGUMENT_LINE_COLOR + " color] ["
				+ ARGUMENT_PREFIX + ARGUMENT_POLYGON_STROKE_WIDTH
				+ " stroke_width] [" + ARGUMENT_PREFIX + ARGUMENT_POLYGON_COLOR
				+ " color] [" + ARGUMENT_PREFIX + ARGUMENT_FILL_POLYGON + "] ["
				+ ARGUMENT_PREFIX + ARGUMENT_POLYGON_FILL_COLOR + " color] ["
				+ ARGUMENT_PREFIX + ARGUMENT_SIMPLIFY_GEOMETRIES
				+ " true|false] [" + ARGUMENT_PREFIX
				+ ARGUMENT_IGNORE_GEOPACKAGE_STYLES + " true|false] ["
				+ ARGUMENT_PREFIX + ARGUMENT_LOG_COUNT + " count] ["
				+ ARGUMENT_PREFIX + ARGUMENT_LOG_TIME
				+ " time] feature_geopackage_file feature_table tile_geopackage_file tile_table min_zoom max_zoom");
		System.out.println();
		System.out.println("DESCRIPTION");
		System.out.println();
		System.out.println(
				"\tGenerates tiles from a GeoPackage feature table into a tile table");
		System.out.println();
		System.out.println("ARGUMENTS");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX
				+ ARGUMENT_MAX_FEATURES_PER_TILE + " max_features_per_tile");
		System.out.println(
				"\t\tMax features to generate into a tile before generating a numbered feature count tile (default is "
						+ DEFAULT_MAX_FEATURES_PER_TILE
						+ ", use -1 for no max)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_COMPRESS_FORMAT
				+ " compress_format");
		System.out.println(
				"\t\tTile compression image format: png, jpg, jpeg (default is no compression, native format)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_COMPRESS_QUALITY
				+ " compress_quality");
		System.out.println(
				"\t\tTile compression image quality between 0.0 and 1.0 (not valid for png, default is 1.0)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_XYZ_TILES);
		System.out.println(
				"\t\tGenerate tiles in XYZ tile format (default is GeoPackage format with minimum bounds)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_BOUNDING_BOX
				+ " minLon,minLat,maxLon,maxLat");
		System.out.println(
				"\t\tOnly tiles overlapping the bounding box are requested (default is the world)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_EPSG + " epsg");
		System.out.println(
				"\t\tEPSG number of the provided bounding box (default is 4326, WGS 84)");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_TILE_WIDTH + " width");
		System.out.println("\t\tWidth used when creating each tile (default is "
				+ featureTiles.getTileWidth() + ")");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_TILE_HEIGHT + " height");
		System.out
				.println("\t\tHeight used when creating each tile (default is "
						+ featureTiles.getTileHeight() + ")");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_TILE_SCALE + " scale");
		System.out.println(
				"\t\tScale factor used when creating each tile (default is "
						+ featureTiles.getScale() + ")");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_POINT_RADIUS + " radius");
		System.out.println(
				"\t\tFloating point circle radius used when drawing points (default is "
						+ featureTiles.getPointRadius() + ")");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_POINT_COLOR + " color");
		System.out.println(
				"\t\tColor used when drawing points formatted as one of: [ name | r,g,b | r,g,b,a ] (default is "
						+ colorString(featureTiles.getPointColor()) + ")");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_POINT_ICON + " image_file");
		System.out.println(
				"\t\tImage file containing image to use when drawing points in place of a drawn circle");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_ICON_WIDTH + " width");
		System.out.println(
				"\t\tPoint icon display width (default is actual icon width)");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_ICON_HEIGHT + " height");
		System.out.println(
				"\t\tPoint icon display height (default is actual icon height)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_POINT_CENTER_ICON);
		System.out.println(
				"\t\tDraw point icons by centering the icon image to the location (default is pinning to bottom center)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_LINE_STROKE_WIDTH
				+ " stroke_width");
		System.out.println(
				"\t\tFloating point stroke width when drawing lines (default is "
						+ featureTiles.getLineStrokeWidth() + ")");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_LINE_COLOR + " color");
		System.out.println(
				"\t\tColor used when drawing lines formatted as one of: [ name | r,g,b | r,g,b,a ] (default is "
						+ colorString(featureTiles.getLineColor()) + ")");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX
				+ ARGUMENT_POLYGON_STROKE_WIDTH + " stroke_width");
		System.out.println(
				"\t\tFloating point stroke width when drawing polygons (default is "
						+ featureTiles.getPolygonStrokeWidth() + ")");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_POLYGON_COLOR + " color");
		System.out.println(
				"\t\tColor used when drawing polygons formatted as one of: [ name | r,g,b | r,g,b,a ] (default is "
						+ colorString(featureTiles.getPolygonColor()) + ")");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_FILL_POLYGON);
		System.out.println("\t\tFill polygons with color (default is "
				+ featureTiles.isFillPolygon() + ")");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_POLYGON_FILL_COLOR
				+ " color");
		System.out.println(
				"\t\tColor used when filling polygons formatted as one of: [ name | r,g,b | r,g,b,a ] (default is "
						+ colorString(featureTiles.getPolygonFillColor())
						+ ")");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_SIMPLIFY_GEOMETRIES
				+ " true|false");
		System.out.println(
				"\t\tFlag indicating whether geometries should be simplified with a similar curve with fewer points before drawn (default is true)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX
				+ ARGUMENT_IGNORE_GEOPACKAGE_STYLES + " true|false");
		System.out.println(
				"\t\tFlag indicating whether styles saved within the GeoPackage should be ignored (default is false)");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_LOG_COUNT + " count");
		System.out.println(
				"\t\tLog frequency count of generated tiles (default is "
						+ LOG_TILE_FREQUENCY + ")");
		System.out.println();
		System.out
				.println("\t" + ARGUMENT_PREFIX + ARGUMENT_LOG_TIME + " time");
		System.out.println("\t\tLog frequency time in seconds (default is "
				+ LOG_TILE_TIME_FREQUENCY + ")");
		System.out.println();
		System.out.println("\tfeature_geopackage_file");
		System.out.println(
				"\t\tpath to the GeoPackage file containing the feature table to generate tiles from");
		System.out.println();
		System.out.println("\tfeature_table");
		System.out.println(
				"\t\tfeature table name within the GeoPackage file to generate tiles from");
		System.out.println();
		System.out.println("\ttile_geopackage_file");
		System.out.println(
				"\t\tpath to the GeoPackage file to create with tiles, or existing file to update");
		System.out.println();
		System.out.println("\ttile_table");
		System.out.println(
				"\t\ttile table name within the GeoPackage file to create or update");
		System.out.println();
		System.out.println("\tmin_zoom");
		System.out.println("\t\tMinimum zoom level to request tiles for");
		System.out.println();
		System.out.println("\tmax_zoom");
		System.out.println("\t\tMaximum zoom level to request tiles for");
		System.out.println();
	}

	/**
	 * Get a r,g,b,a color string from the color
	 * 
	 * @param color
	 * @return color string
	 */
	private static String colorString(Color color) {
		return color.getRed() + "," + color.getGreen() + "," + color.getBlue()
				+ "," + color.getAlpha();
	}

}
