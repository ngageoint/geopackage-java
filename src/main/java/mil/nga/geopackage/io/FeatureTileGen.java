package mil.nga.geopackage.io;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.index.FeatureTableIndex;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.tiles.features.DefaultFeatureTiles;
import mil.nga.geopackage.tiles.features.FeatureTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;

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
 */
public class FeatureTileGen {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(FeatureTileGen.class
			.getName());

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
	private static final int DEFAULT_MAX_FEATURES_PER_TILE = 1000;

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
	 * Google Tiles format argument
	 */
	public static final String ARGUMENT_GOOGLE_TILES = "g";

	/**
	 * Bounding box argument
	 */
	public static final String ARGUMENT_BOUNDING_BOX = "bbox";

	/**
	 * EPSG argument
	 */
	public static final String ARGUMENT_EPSG = "epsg";

	/**
	 * Tile progress
	 */
	private static Progress progress = new Progress("Feature Tile Generation",
			LOG_TILE_FREQUENCY, LOG_TILE_TIME_FREQUENCY);

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
	 * Google tiles flag
	 */
	private static boolean googleTiles = false;

	/**
	 * Bounding box
	 */
	private static BoundingBox boundingBox = null;

	/**
	 * Bounding Box EPSG
	 */
	private static Long epsg = null;

	/**
	 * Main method to generate tiles in a GeoPackage
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Add a shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				finish();
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
						System.out
								.println("Error: Max Features Per Tile argument '"
										+ arg + "' must be followed by a value");
					}
					break;

				case ARGUMENT_COMPRESS_FORMAT:
					if (i < args.length) {
						compressFormat = args[++i];
					} else {
						valid = false;
						System.out
								.println("Error: Compress Format argument '"
										+ arg
										+ "' must be followed by an image format");
					}
					break;

				case ARGUMENT_COMPRESS_QUALITY:
					if (i < args.length) {
						compressQuality = Float.valueOf(args[++i]);
					} else {
						valid = false;
						System.out
								.println("Error: Compress Quality argument '"
										+ arg
										+ "' must be followed by a value between 0.0 and 1.0");
					}
					break;

				case ARGUMENT_GOOGLE_TILES:
					googleTiles = true;
					break;

				case ARGUMENT_BOUNDING_BOX:
					if (i < args.length) {
						String bbox = args[++i];
						String[] bboxParts = bbox.split(",");
						if (bboxParts.length != 4) {
							valid = false;
							System.out
									.println("Error: Bounding Box argument '"
											+ arg
											+ "' value must be in the format: minLon,minLat,maxLon,maxLat");
						} else {
							double minLon = Double.valueOf(bboxParts[0]);
							double minLat = Double.valueOf(bboxParts[1]);
							double maxLon = Double.valueOf(bboxParts[2]);
							double maxLat = Double.valueOf(bboxParts[3]);
							boundingBox = new BoundingBox(minLon, maxLon,
									minLat, maxLat);
						}
					} else {
						valid = false;
						System.out
								.println("Error: Bounding Box argument '"
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
					System.out.println("Error: Unsupported extra argument: "
							+ arg);
				}
			}
		}

		if (compressFormat == null && compressQuality != null) {
			System.out
					.println("Error: Compress quality requires a compress format");
			valid = false;
		} else if (boundingBox == null && epsg != null) {
			System.out.println("Error: EPSG requires a bounding box");
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
			Progress indexProgress = new Progress("Feature Indexer",
					LOG_INDEX_FREQUENCY, LOG_INDEX_TIME_FREQUENCY);
			indexProgress.setMax(numFeatures);
			featureIndex.setProgress(indexProgress);
			int indexed = featureIndex.index();
			LOGGER.log(Level.INFO,
					"Indexed GeoPackage '" + featureGeoPackage.getName()
							+ "' feature table '" + featureTable + "', "
							+ indexed + " features");
		}

		// Create the feature tiles
		FeatureTiles featureTiles = new DefaultFeatureTiles(featureDao);
		featureTiles.setFeatureIndex(featureIndex);
		if (maxFeaturesPerTile != null) {
			featureTiles.setMaxFeaturesPerTile(maxFeaturesPerTile);
			featureTiles.setMaxFeaturesTileDraw(new NumberFeaturesTile());
		}

		// Create the tile generator
		FeatureTileGenerator tileGenerator = new FeatureTileGenerator(
				tileGeoPackage, tileTable, featureTiles, minZoom, maxZoom);

		if (compressFormat != null) {
			tileGenerator.setCompressFormat(compressFormat);
			if (compressQuality != null) {
				tileGenerator.setCompressQuality(compressQuality);
			}
		}

		if (googleTiles) {
			tileGenerator.setGoogleTiles(true);
		}

		if (boundingBox != null) {
			Projection projection = null;
			if (epsg != null) {
				projection = ProjectionFactory.getProjection(epsg);
			}
			tileGenerator.setTileBoundingBox(boundingBox, projection);
		}

		int count = tileGenerator.getTileCount();

		LOGGER.log(
				Level.INFO,
				"Feature GeoPackage: "
						+ featureGeoPackage.getName()
						+ ", Feature Table: "
						+ featureTable
						+ ", Tile GeoPackage: "
						+ tileGeoPackage.getName()
						+ ", Tile Table: "
						+ tileTable
						+ ", Min Zoom: "
						+ minZoom
						+ ", Max Zoom: "
						+ maxZoom
						+ (maxFeaturesPerTile != null ? ", Max Features Per Tile: "
								+ maxFeaturesPerTile
								: "")
						+ (compressFormat != null ? ", Compress Format: "
								+ compressFormat : "")
						+ (compressQuality != null ? ", Compress Quality: "
								+ compressQuality : "")
						+ ", "
						+ (googleTiles ? "Google Tiles" : "")
						+ (boundingBox != null ? ", Min Lon: "
								+ boundingBox.getMinLongitude() + ", Min Lat: "
								+ boundingBox.getMinLatitude() + ", Max Lon: "
								+ boundingBox.getMaxLongitude() + ", Max Lat: "
								+ boundingBox.getMaxLatitude() : "")
						+ (epsg != null ? ", EPSG: " + epsg : "")
						+ ", Expected Tile Count: " + count);

		tileGenerator.setProgress(progress);

		LOGGER.log(Level.INFO, "Generating Tiles...");

		try {
			tileGenerator.generateTiles();
		} catch (IOException | SQLException e) {
			throw new GeoPackageException("Exception while generating tiles", e);
		}
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
		System.out.println();
		System.out.println("USAGE");
		System.out.println();
		System.out
				.println("\t["
						+ ARGUMENT_PREFIX
						+ ARGUMENT_MAX_FEATURES_PER_TILE
						+ " max_features_per_tile] ["
						+ ARGUMENT_PREFIX
						+ ARGUMENT_COMPRESS_FORMAT
						+ " compress_format] ["
						+ ARGUMENT_PREFIX
						+ ARGUMENT_COMPRESS_QUALITY
						+ " compress_quality] ["
						+ ARGUMENT_PREFIX
						+ ARGUMENT_GOOGLE_TILES
						+ "] ["
						+ ARGUMENT_PREFIX
						+ ARGUMENT_BOUNDING_BOX
						+ " minLon,minLat,maxLon,maxLat] ["
						+ ARGUMENT_PREFIX
						+ ARGUMENT_EPSG
						+ " epsg] feature_geopackage_file feature_table tile_geopackage_file tile_table min_zoom max_zoom");
		System.out.println();
		System.out.println("DESCRIPTION");
		System.out.println();
		System.out
				.println("\tGenerates tiles from a GeoPackage feature table into a tile table");
		System.out.println();
		System.out.println("ARGUMENTS");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX
				+ ARGUMENT_MAX_FEATURES_PER_TILE + " max_features_per_tile");
		System.out
				.println("\t\tMax features to generate into a tile before generating a numbered feature count tile (default is "
						+ DEFAULT_MAX_FEATURES_PER_TILE
						+ ", use -1 for no max)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_COMPRESS_FORMAT
				+ " compress_format");
		System.out
				.println("\t\tTile compression image format: png, jpg, jpeg (default is no compression, native format)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_COMPRESS_QUALITY
				+ " compress_quality");
		System.out
				.println("\t\tTile compression image quality between 0.0 and 1.0 (not valid for png, default is 1.0)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_GOOGLE_TILES);
		System.out
				.println("\t\tGenerate tiles in Google tile format (default is GeoPackage format with minimum bounds)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_BOUNDING_BOX
				+ " minLon,minLat,maxLon,maxLat");
		System.out
				.println("\t\tOnly tiles overlapping the bounding box are requested (default is the world)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_EPSG + " epsg");
		System.out
				.println("\t\tEPSG number of the provided bounding box (default is 4326, WGS 84)");
		System.out.println();
		System.out.println("\tfeature_geopackage_file");
		System.out
				.println("\t\tpath to the GeoPackage file containing the feature table to generate tiles from");
		System.out.println();
		System.out.println("\tfeature_table");
		System.out
				.println("\t\tfeature table name within the GeoPackage file to generate tiles from");
		System.out.println();
		System.out.println("\ttile_geopackage_file");
		System.out
				.println("\t\tpath to the GeoPackage file to create with tiles, or existing file to update");
		System.out.println();
		System.out.println("\ttile_table");
		System.out
				.println("\t\ttile table name within the GeoPackage file to create or update");
		System.out.println();
		System.out.println("\tmin_zoom");
		System.out.println("\t\tMinimum zoom level to request tiles for");
		System.out.println();
		System.out.println("\tmax_zoom");
		System.out.println("\t\tMaximum zoom level to request tiles for");
		System.out.println();
	}

}
