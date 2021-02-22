package mil.nga.geopackage.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.tiles.reproject.TileReprojection;
import mil.nga.geopackage.tiles.reproject.TileReprojectionOptimize;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionFactory;

/**
 * Tile Reproject main method for command line tile reprojections
 * 
 * To run from command line, build with the standalone profile:
 * 
 * mvn clean install -Pstandalone
 * 
 * java -classpath geopackage-*-standalone.jar
 * mil.nga.geopackage.io.TileReproject +usage_arguments
 * 
 * @author osbornb
 * @since 4.0.1
 */
public class TileReproject {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(TileReproject.class.getName());

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
	 * Projection argument
	 */
	public static final String ARGUMENT_PROJECTION = "proj";

	/**
	 * Optimize argument
	 */
	public static final String ARGUMENT_OPTIMIZE = "optimize";

	/**
	 * Web Mercator optimization argument
	 */
	public static final String ARGUMENT_OPTIMIZE_WEB_MERCATOR = "wm";

	/**
	 * Platte Carre (WGS84) optimization argument
	 */
	public static final String ARGUMENT_OPTIMIZE_PLATTE_CARRE = "pc";

	/**
	 * Web Mercator with world bounds optimization argument
	 */
	public static final String ARGUMENT_OPTIMIZE_WEB_MERCATOR_WORLD = ARGUMENT_OPTIMIZE_WEB_MERCATOR
			+ "w";

	/**
	 * Platte Carre (WGS84) with world bounds optimization argument
	 */
	public static final String ARGUMENT_OPTIMIZE_PLATTE_CARRE_WORLD = ARGUMENT_OPTIMIZE_PLATTE_CARRE
			+ "w";

	/**
	 * Tile draw width argument
	 */
	public static final String ARGUMENT_TILE_WIDTH = "width";

	/**
	 * Tile draw height argument
	 */
	public static final String ARGUMENT_TILE_HEIGHT = "height";

	/**
	 * Zoom levels argument
	 */
	public static final String ARGUMENT_ZOOM_LEVELS = "zoom";

	/**
	 * Log Frequency Count argument
	 */
	public static final String ARGUMENT_LOG_COUNT = "logCount";

	/**
	 * Log Frequency Time argument
	 */
	public static final String ARGUMENT_LOG_TIME = "logTime";

	/**
	 * Tile progress
	 */
	private static Progress progress = new Progress("Tile Reprojection",
			"tiles", LOG_TILE_FREQUENCY, LOG_TILE_TIME_FREQUENCY);

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
	private static String table = null;

	/**
	 * Reproject GeoPackage file
	 */
	private static File reprojectGeoPackageFile = null;

	/**
	 * Reproject GeoPackage
	 */
	private static GeoPackage reprojectGeoPackage = null;

	/**
	 * Reproject Tile Table name
	 */
	private static String reprojectTable = null;

	/**
	 * Projection
	 */
	private static Projection projection = null;

	/**
	 * Optimize
	 */
	private static TileReprojectionOptimize optimize = null;

	/**
	 * Tile width
	 */
	private static Long tileWidth = null;

	/**
	 * Tile height
	 */
	private static Long tileHeight = null;

	/**
	 * Zoom levels
	 */
	private static String zoomLevels = null;

	/**
	 * Zoom levels
	 */
	private static List<Long> zooms = null;

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

				case ARGUMENT_PROJECTION:
					if (i + 1 < args.length) {
						projection = ProjectionFactory.getProjection(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Projection argument '" + arg
								+ "' must be followed by a projection");
					}
					break;

				case ARGUMENT_OPTIMIZE:
					if (i + 1 < args.length) {
						String optimizeArg = args[++i];
						optimize = parseOptimize(optimizeArg);
						if (optimize == null) {
							valid = false;
							System.out.println("Error: Optimize argument '"
									+ arg
									+ "' must be followed by a valid optimization type. Invalid: "
									+ optimizeArg);
						}
					} else {
						valid = false;
						System.out.println("Error: Optimize argument '" + arg
								+ "' must be followed by an optimization type");
					}
					break;

				case ARGUMENT_TILE_WIDTH:
					if (i + 1 < args.length) {
						tileWidth = Long.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Tile Width argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_TILE_HEIGHT:
					if (i + 1 < args.length) {
						tileHeight = Long.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Tile Height argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_ZOOM_LEVELS:
					if (i + 1 < args.length) {
						zoomLevels = args[++i];
						zooms = parseZoomLevels(zoomLevels);
						if (zooms == null) {
							valid = false;
							System.out.println("Error: Zoom Levels argument '"
									+ arg
									+ "' must be followed by a valid single zoom or zoom range. Invalid: "
									+ zoomLevels);
						}
					} else {
						valid = false;
						System.out.println("Error: Zoom Levels argument '" + arg
								+ "' must be followed by a single zoom or zoom range");
					}
					break;

				case ARGUMENT_LOG_COUNT:
					if (i + 1 < args.length) {
						progress.setCountFrequency(Integer.valueOf(args[++i]));
					} else {
						valid = false;
						System.out.println("Error: Log Count argument '" + arg
								+ "' must be followed by a frequency count value");
					}
					break;

				case ARGUMENT_LOG_TIME:
					if (i + 1 < args.length) {
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
				// Set required + optional arguments in order
				if (geoPackageFile == null) {
					geoPackageFile = new File(arg);
				} else if (table == null) {
					table = arg;
					requiredArguments = true;
				} else if (reprojectTable == null) {
					reprojectTable = arg;
				} else if (reprojectGeoPackageFile == null) {
					reprojectGeoPackageFile = new File(reprojectTable);
					reprojectTable = arg;
				} else {
					valid = false;
					System.out.println(
							"Error: Unsupported extra argument: " + arg);
				}
			}
		}

		if (optimize != null) {
			if (projection != null
					&& !projection.equals(optimize.getProjection())) {
				System.out.println(
						"Error: Projection is not compatible with optimization. Projection: "
								+ projection + ", Optimization: "
								+ optimize.getClass().getSimpleName());
				valid = false;
			}
		} else if (projection == null) {
			System.out
					.println("Error: A projection or optimization is required");
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

		// Open the GeoPackage
		geoPackage = GeoPackageManager.open(geoPackageFile);

		System.out.println();
		System.out.println("GeoPackage: " + geoPackage.getName());
		System.out.println("Tile Table: " + table);

		if (reprojectGeoPackageFile != null) {

			// If the reproject GeoPackage does not exist create it
			if (!reprojectGeoPackageFile.exists()) {
				GeoPackageManager.create(reprojectGeoPackageFile);
			}

			// Open the reproject GeoPackage
			reprojectGeoPackage = GeoPackageManager
					.open(reprojectGeoPackageFile);

			System.out.println("Reprojection GeoPackage: "
					+ reprojectGeoPackage.getName());
		} else {
			// Reproject into the same GeoPackage
			reprojectGeoPackage = geoPackage;
		}

		if (reprojectTable == null) {
			reprojectTable = table;
		} else {
			System.out.println("Reprojection Tile Table: " + reprojectTable);
		}

		if (projection == null) {
			projection = optimize.getProjection();
		}
		System.out.println("Projection: " + projection);

		TileReprojection tileReprojection = TileReprojection.create(geoPackage,
				table, reprojectGeoPackage, reprojectTable, projection);
		tileReprojection.setOverwrite(true);
		tileReprojection.setProgress(progress);

		if (optimize != null) {
			tileReprojection.setOptimize(optimize);
			System.out.println(
					"Optimization: " + optimize.getClass().getSimpleName()
							+ (optimize.isWorld() ? ", world bounds" : ""));
		}
		if (tileWidth != null) {
			tileReprojection.setTileWidth(tileWidth);
			System.out.println("Tile Width: " + tileWidth);
		}
		if (tileHeight != null) {
			tileReprojection.setTileHeight(tileHeight);
			System.out.println("Tile Height: " + tileHeight);
		}
		if (zoomLevels != null) {
			System.out.println("Zoom Levels: " + zoomLevels);
		}

		System.out.println("Log Count Frequency: "
				+ progress.getCountFrequency() + " tiles");
		System.out.println("Log Time Frequency: " + progress.getTimeFrequency()
				+ " seconds");
		System.out.println();

		LOGGER.log(Level.INFO, "Reprojecting Tiles...");

		int count = 0;
		if (zooms != null) {
			count = tileReprojection.reproject(zooms);
		} else {
			count = tileReprojection.reproject();
		}

		finish(count);
	}

	/**
	 * Parse the reprojection optimize argument
	 * 
	 * @param optimizeArg
	 *            optimize argument
	 * @return optimize or null
	 */
	public static TileReprojectionOptimize parseOptimize(String optimizeArg) {

		TileReprojectionOptimize optimize = null;

		switch (optimizeArg.toLowerCase()) {
		case ARGUMENT_OPTIMIZE_WEB_MERCATOR:
			optimize = TileReprojectionOptimize.webMercator();
			break;
		case ARGUMENT_OPTIMIZE_PLATTE_CARRE:
			optimize = TileReprojectionOptimize.platteCarre();
			break;
		case ARGUMENT_OPTIMIZE_WEB_MERCATOR_WORLD:
			optimize = TileReprojectionOptimize.webMercatorWorld();
			break;
		case ARGUMENT_OPTIMIZE_PLATTE_CARRE_WORLD:
			optimize = TileReprojectionOptimize.platteCarreWorld();
			break;
		}

		return optimize;
	}

	/**
	 * Parse the zoom levels argument
	 * 
	 * @param zoomLevels
	 *            zoom levels
	 * @return zoom levels or null
	 */
	public static List<Long> parseZoomLevels(String zoomLevels) {
		List<Long> zooms = new ArrayList<>();
		if (zoomLevels.contains(",")) {
			String[] zoomParts = zoomLevels.split(",");
			for (String zoom : zoomParts) {
				zooms.add(Long.valueOf(zoom));
			}
		} else if (zoomLevels.contains("-")) {
			String[] zoomParts = zoomLevels.split("-");
			if (zoomParts.length == 2) {
				long minZoom = Long.valueOf(zoomParts[0]);
				long maxZoom = Long.valueOf(zoomParts[1]);
				for (long zoom = minZoom; zoom <= maxZoom; zoom++) {
					zooms.add(zoom);
				}
			}
		} else {
			zooms.add(Long.valueOf(zoomLevels));
		}
		return zooms.isEmpty() ? null : zooms;
	}

	/**
	 * Finish tile generation
	 * 
	 * @param count
	 *            generated count
	 */
	private static void finish(int count) {

		StringBuilder output = new StringBuilder();
		output.append("\nTile Reprojection: ").append(progress.getProgress())
				.append(" tiles");

		if (reprojectGeoPackage != null) {
			try {
				GeoPackageTextOutput textOutput = new GeoPackageTextOutput(
						reprojectGeoPackage);
				output.append("\n\n");
				output.append(textOutput.header());
				output.append("\n\n");
				output.append(textOutput.tileTable(reprojectTable));

			} finally {
				reprojectGeoPackage.close();
			}
		}
		if (geoPackage != null) {
			geoPackage.close();
		}
		output.append("\n");

		System.out.println(output.toString());
	}

	/**
	 * Print usage for the main method
	 */
	private static void printUsage() {
		System.out.println();
		System.out.println("USAGE");
		System.out.println();
		System.out.println("\t[" + ARGUMENT_PREFIX + ARGUMENT_PROJECTION
				+ " projection] [" + ARGUMENT_PREFIX + ARGUMENT_OPTIMIZE
				+ " optimization] [" + ARGUMENT_PREFIX + ARGUMENT_TILE_WIDTH
				+ " tile_width] [" + ARGUMENT_PREFIX + ARGUMENT_TILE_HEIGHT
				+ " tile_height] [" + ARGUMENT_PREFIX + ARGUMENT_ZOOM_LEVELS
				+ " zoom_levels] [" + ARGUMENT_PREFIX + ARGUMENT_LOG_COUNT
				+ " count] [" + ARGUMENT_PREFIX + ARGUMENT_LOG_TIME
				+ " time] geopackage_file tile_table [[reprojection_geopackage_file] reprojection_tile_table]");
		System.out.println();
		System.out.println("DESCRIPTION");
		System.out.println();
		System.out.println(
				"\tReprojects Geopackage tiles to an alternate projection");
		System.out.println();
		System.out.println("ARGUMENTS");
		System.out.println();
		System.out.println("\t* A '" + ARGUMENT_PREFIX + ARGUMENT_PROJECTION
				+ "' OR '" + ARGUMENT_PREFIX + ARGUMENT_OPTIMIZE
				+ "' argument is required");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_PROJECTION + " projection");
		System.out.println(
				"\t\tProjection specified as 'authority:code' or 'epsg_code'");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_OPTIMIZE + " optimization");
		System.out.println(
				"\t\tReprojection optimization with included projection, specified as one of:");
		System.out.println("\t\t\t" + ARGUMENT_OPTIMIZE_WEB_MERCATOR
				+ " - Web Mercator optimization, minimally tile bounded");
		System.out.println("\t\t\t" + ARGUMENT_OPTIMIZE_PLATTE_CARRE
				+ " - Platte Carre (WGS84) optimization, minimally tile bounded");
		System.out.println("\t\t\t" + ARGUMENT_OPTIMIZE_WEB_MERCATOR_WORLD
				+ " - Web Mercator optimization, world bounded with XYZ tile coordinates");
		System.out.println("\t\t\t" + ARGUMENT_OPTIMIZE_PLATTE_CARRE_WORLD
				+ " - Platte Carre (WGS84) optimization, world bounded with XYZ tile coordinates");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_TILE_WIDTH + " tile_width");
		System.out.println(
				"\t\tWidth of reprojected tiles (default is existing width)");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_TILE_HEIGHT + " tile_height");
		System.out.println(
				"\t\tHeight of reprojected tiles (default is existing height)");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_ZOOM_LEVELS + " zoom_levels");
		System.out.println(
				"\t\tTile zoom levels to reproject, specified as 'z', 'zmin-zmax', or 'z1,z2,...', (default is all levels)");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_LOG_COUNT + " count");
		System.out.println(
				"\t\tLog frequency count of reprojected tiles (default is "
						+ LOG_TILE_FREQUENCY + ")");
		System.out.println();
		System.out
				.println("\t" + ARGUMENT_PREFIX + ARGUMENT_LOG_TIME + " time");
		System.out.println("\t\tLog frequency time in seconds (default is "
				+ LOG_TILE_TIME_FREQUENCY + ")");
		System.out.println();
		System.out.println("\tgeopackage_file");
		System.out.println(
				"\t\tPath to the source GeoPackage file with tiles to reproject and default reprojection GeoPackage destination");
		System.out.println();
		System.out.println("\ttile_table");
		System.out.println(
				"\t\tSource tile table name to reproject within the GeoPackage file and default reprojection tile table destination");
		System.out.println();
		System.out.println("\treprojection_geopackage_file");
		System.out.println(
				"\t\tPath to the destination GeoPackage to save the reprojected tiles");
		System.out.println();
		System.out.println("\treprojection_tile_table");
		System.out.println(
				"\t\tName of the destination tile table to save reprojected tiles");
		System.out.println();
	}

}
