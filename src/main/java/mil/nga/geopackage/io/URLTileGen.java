package mil.nga.geopackage.io;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.locationtech.proj4j.units.Units;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.UrlTileGenerator;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;
import mil.nga.sf.proj.ProjectionTransform;

/**
 * URL Tile Generator main method for command line tile generation
 * 
 * To run from command line, build with the standalone profile:
 * 
 * mvn clean install -Pstandalone
 * 
 * java -classpath geopackage-*-standalone.jar mil.nga.geopackage.io.URLTileGen
 * +usage_arguments
 * 
 * @author osbornb
 * @since 1.1.2
 */
public class URLTileGen {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(URLTileGen.class.getName());

	/**
	 * Log tile frequency for how often to log tile generation progress
	 */
	private static final int LOG_TILE_FREQUENCY = 100;

	/**
	 * Log tile frequency in seconds for how often to log tile generation
	 * progress
	 */
	private static final int LOG_TILE_TIME_FREQUENCY = 60;

	/**
	 * Argument prefix
	 */
	public static final String ARGUMENT_PREFIX = "-";

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
	 * 
	 * @since 3.5.0
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
	 * URL EPSG argument
	 */
	public static final String ARGUMENT_URL_EPSG = "uepsg";

	/**
	 * TMS argument
	 */
	public static final String ARGUMENT_TMS = "tms";

	/**
	 * Replace Existing Tiles argument
	 * 
	 * @since 3.5.0
	 */
	public static final String ARGUMENT_REPLACE_EXISTING = "replace";

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
			"URL Tile Generation", "tiles", LOG_TILE_FREQUENCY,
			LOG_TILE_TIME_FREQUENCY);

	/**
	 * GeoPackage file
	 */
	private static File geoPackageFile = null;

	/**
	 * GeoPackage
	 */
	private static GeoPackage geoPackage = null;

	/**
	 * Tile Table name
	 */
	private static String tileTable = null;

	/**
	 * URL
	 */
	private static String url = null;

	/**
	 * Min Zoom
	 */
	private static Integer minZoom = null;

	/**
	 * Max Zoom
	 */
	private static Integer maxZoom = null;

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
	 * URL EPSG
	 */
	private static long urlEpsg = ProjectionConstants.EPSG_WEB_MERCATOR;

	/**
	 * TMS URL flag
	 */
	private static boolean tms = false;

	/**
	 * Replace Existing tiles flag
	 */
	private static boolean replaceExisting = false;

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

				case ARGUMENT_URL_EPSG:
					if (i < args.length) {
						urlEpsg = Long.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: URL EPSG argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_TMS:
					tms = true;
					break;

				case ARGUMENT_REPLACE_EXISTING:
					replaceExisting = true;
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
				if (geoPackageFile == null) {
					geoPackageFile = new File(arg);
				} else if (tileTable == null) {
					tileTable = arg;
				} else if (url == null) {
					url = arg;
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

		// If the GeoPackage does not exist create it
		if (!geoPackageFile.exists()) {
			GeoPackageManager.create(geoPackageFile);
		}

		// Open the GeoPackage
		geoPackage = GeoPackageManager.open(geoPackageFile);

		// Default the EPSG
		if (epsg == null) {
			epsg = new Long(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
		}

		// Set the projection and default bounding box as needed
		Projection bboxProjection = null;
		if (boundingBox != null) {
			bboxProjection = ProjectionFactory.getProjection(epsg);
		} else {
			boundingBox = new BoundingBox();
			bboxProjection = ProjectionFactory.getProjection(
					ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
		}

		// Bound degree tiles to Web Mercator limits
		if (bboxProjection.isUnit(Units.DEGREES)) {
			boundingBox = TileBoundingBoxUtils
					.boundDegreesBoundingBoxWithWebMercatorLimits(boundingBox);
		}

		// Transform to the URL projection bounding box
		Projection urlProjection = ProjectionFactory.getProjection(urlEpsg);
		ProjectionTransform transform = bboxProjection
				.getTransformation(urlProjection);
		BoundingBox urlBoundingBox = boundingBox.transform(transform);

		UrlTileGenerator tileGenerator = new UrlTileGenerator(geoPackage,
				tileTable, url, minZoom, maxZoom, urlBoundingBox,
				urlProjection);

		if (compressFormat != null) {
			tileGenerator.setCompressFormat(compressFormat);
			if (compressQuality != null) {
				tileGenerator.setCompressQuality(compressQuality);
			}
		}

		if (xyzTiles) {
			tileGenerator.setXYZTiles(true);
		}

		if (tms) {
			tileGenerator.setTileFormat(TileFormatType.TMS);
		}

		tileGenerator.setSkipExisting(!replaceExisting);

		int count = tileGenerator.getTileCount();

		System.out.println();
		System.out.println("GeoPackage: " + geoPackage.getName());
		System.out.println("Tile Table: " + tileTable);
		System.out.println("URL: " + url);
		System.out.println("Min Zoom: " + minZoom);
		System.out.println("Max Zoom: " + maxZoom);
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
		System.out.println("URL EPSG: " + urlEpsg);
		if (tms) {
			System.out.println("URL TMS Tiles: true");
		}
		System.out.println(
				"Existing Tiles: " + (replaceExisting ? "replace" : "skip"));
		System.out.println("Log Count Frequency: "
				+ progress.getCountFrequency() + " tiles");
		System.out.println("Log Time Frequency: " + progress.getTimeFrequency()
				+ " seconds");
		System.out.println("Expected Tile Count: " + count);
		System.out.println();

		tileGenerator.setProgress(progress);

		LOGGER.log(Level.INFO, "Generating Tiles...");

		int generatedCount = 0;
		try {
			generatedCount = tileGenerator.generateTiles();
		} catch (IOException | SQLException e) {
			throw new GeoPackageException("Exception while generating tiles",
					e);
		}

		finish(generatedCount);
	}

	/**
	 * Finish tile generation
	 * 
	 * @param count
	 *            generated count
	 */
	private static void finish(int count) {

		if (progress.getMax() != null) {

			StringBuilder output = new StringBuilder();
			int progressCount = progress.getProgress();
			output.append("\nTile Generation: ").append(progressCount)
					.append(" of ").append(progress.getMax()).append(" tiles");
			if (count != progressCount) {
				output.append(" (").append(count).append(" new, ")
						.append(progressCount - count).append(" skipped)");
			}

			if (geoPackage != null) {
				try {
					GeoPackageTextOutput textOutput = new GeoPackageTextOutput(
							geoPackage);
					output.append("\n\n");
					output.append(textOutput.header());
					output.append("\n\n");
					output.append(textOutput.tileTable(tileTable));

				} finally {
					geoPackage.close();
				}
			}
			output.append("\n");

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
		System.out.println("\t[" + ARGUMENT_PREFIX + ARGUMENT_COMPRESS_FORMAT
				+ " compress_format] [" + ARGUMENT_PREFIX
				+ ARGUMENT_COMPRESS_QUALITY + " compress_quality] ["
				+ ARGUMENT_PREFIX + ARGUMENT_XYZ_TILES + "] [" + ARGUMENT_PREFIX
				+ ARGUMENT_BOUNDING_BOX + " minLon,minLat,maxLon,maxLat] ["
				+ ARGUMENT_PREFIX + ARGUMENT_EPSG + " epsg] [" + ARGUMENT_PREFIX
				+ ARGUMENT_URL_EPSG + " url_epsg] [" + ARGUMENT_PREFIX
				+ ARGUMENT_TMS + "] [" + ARGUMENT_PREFIX
				+ ARGUMENT_REPLACE_EXISTING + "] [" + ARGUMENT_PREFIX
				+ ARGUMENT_LOG_COUNT + " count] [" + ARGUMENT_PREFIX
				+ ARGUMENT_LOG_TIME
				+ " time] geopackage_file tile_table url min_zoom max_zoom");
		System.out.println();
		System.out.println("DESCRIPTION");
		System.out.println();
		System.out.println(
				"\tGenerates tiles into a GeoPackage tile table by requesting them from a URL");
		System.out.println();
		System.out.println("ARGUMENTS");
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
				"\t" + ARGUMENT_PREFIX + ARGUMENT_URL_EPSG + " url_epsg");
		System.out.println(
				"\t\tEPSG number of the tiles provided by the URL (default is 3857, Web Mercator");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_TMS);
		System.out.println(
				"\t\tRequest URL for x,y,z coordinates is in TMS format (default is standard XYZ)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_REPLACE_EXISTING);
		System.out.println(
				"\t\tReplace tiles when updating an existing GeoPackage with new requested tiles (default is to skip existing tiles)");
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
		System.out.println("\tgeopackage_file");
		System.out.println(
				"\t\tpath to the GeoPackage file to create, or existing file to update");
		System.out.println();
		System.out.println("\ttile_table");
		System.out.println(
				"\t\ttile table name within the GeoPackage file to create or update");
		System.out.println();
		System.out.println("\turl");
		System.out.println(
				"\t\tURL with substitution variables for requesting tiles");
		System.out.println();
		System.out.println("\t\t{z}");
		System.out.println("\t\t\tz URL substitution variable for zoom level");
		System.out.println();
		System.out.println("\t\t{x}");
		System.out.println("\t\t\tx URL substitution variable");
		System.out.println();
		System.out.println("\t\t{y}");
		System.out.println("\t\t\ty URL substitution variable");
		System.out.println();
		System.out.println("\t\t{minLon}");
		System.out.println("\t\t\tMinimum longitude URL substitution variable");
		System.out.println();
		System.out.println("\t\t{minLat}");
		System.out.println("\t\t\tMinimum latitude URL substitution variable");
		System.out.println();
		System.out.println("\t\t{maxLon}");
		System.out.println("\t\t\tMaximum longitude URL substitution variable");
		System.out.println();
		System.out.println("\t\t{maxLat}");
		System.out.println("\t\t\tMaximum latitude URL substitution variable");
		System.out.println();
		System.out.println("\tmin_zoom");
		System.out.println("\t\tMinimum zoom level to request tiles for");
		System.out.println();
		System.out.println("\tmax_zoom");
		System.out.println("\t\tMaximum zoom level to request tiles for");
		System.out.println();
	}

}
