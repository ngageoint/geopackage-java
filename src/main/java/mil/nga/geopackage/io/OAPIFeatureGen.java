package mil.nga.geopackage.io;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.features.OAPIFeatureGenerator;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionFactory;

/**
 * OGC API Feature Generator main method for command line feature generation
 * 
 * To run from command line, build with the standalone profile:
 * 
 * mvn clean install -Pstandalone
 * 
 * java -classpath geopackage-*-standalone.jar
 * mil.nga.geopackage.io.OAPIFeatureGen +usage_arguments
 * 
 * @author osbornb
 * @since 3.3.0
 */
public class OAPIFeatureGen {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(OAPIFeatureGen.class.getName());

	/**
	 * Log feature frequency for how often to log feature generation progress
	 */
	private static final int LOG_FEATURE_FREQUENCY = 1000;

	/**
	 * Log feature frequency in seconds for how often to log feature generation
	 * progress
	 */
	private static final int LOG_FEATURE_TIME_FREQUENCY = 60;

	/**
	 * Argument prefix
	 */
	public static final String ARGUMENT_PREFIX = "-";

	/**
	 * Limit argument
	 */
	public static final String ARGUMENT_LIMIT = "limit";

	/**
	 * Bounding box argument
	 */
	public static final String ARGUMENT_BOUNDING_BOX = "bbox";

	/**
	 * Bounding box projection argument
	 */
	public static final String ARGUMENT_BOUNDING_BOX_PROJECTION = "bbox-proj";

	/**
	 * Time argument
	 */
	public static final String ARGUMENT_TIME = "time";

	/**
	 * Projection argument
	 */
	public static final String ARGUMENT_PROJECTION = "proj";

	/**
	 * Total Limit argument
	 */
	public static final String ARGUMENT_TOTAL_LIMIT = "totalLimit";

	/**
	 * Transaction Limit argument
	 */
	public static final String ARGUMENT_TRANSACTION_LIMIT = "transactionLimit";

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
	 * Feature progress
	 */
	private static Progress progress = new Progress(
			"OGC API Feature Generation", "features", LOG_FEATURE_FREQUENCY,
			LOG_FEATURE_TIME_FREQUENCY);

	/**
	 * GeoPackage file
	 */
	private static File geoPackageFile = null;

	/**
	 * GeoPackage
	 */
	private static GeoPackage geoPackage = null;

	/**
	 * Feature Table name
	 */
	private static String tableName = null;

	/**
	 * Server URL
	 */
	private static String server = null;

	/**
	 * Collection identifier
	 */
	private static String id = null;

	/**
	 * Limit
	 */
	private static Integer limit = null;

	/**
	 * Bounding box
	 */
	private static BoundingBox boundingBox = null;

	/**
	 * Bounding box projection
	 */
	private static Projection boundingBoxProjection = null;

	/**
	 * Time
	 */
	private static String time = null;

	/**
	 * Projection
	 */
	private static Projection projection = null;

	/**
	 * Total Limit
	 */
	private static Integer totalLimit = null;

	/**
	 * Transaction Limit
	 */
	private static Integer transactionLimit = null;

	/**
	 * Main method to generate features in a GeoPackage
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

				case ARGUMENT_LIMIT:
					if (i < args.length) {
						limit = Integer.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Limit argument '" + arg
								+ "' must be followed by a value");
					}
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

				case ARGUMENT_BOUNDING_BOX_PROJECTION:
					if (i < args.length) {
						String proj = args[++i];
						String[] projParts = proj.split(",");
						if (projParts.length != 2) {
							valid = false;
							System.out.println(
									"Error: Bounding Box Projection argument '"
											+ arg
											+ "' value must be in the format: authority,code");
						} else {
							boundingBoxProjection = ProjectionFactory
									.getProjection(projParts[0], projParts[1]);
						}
					} else {
						valid = false;
						System.out.println(
								"Error: Bounding Box Projection argument '"
										+ arg
										+ "' must be followed by projection values: authority,code");
					}
					break;

				case ARGUMENT_TIME:
					if (i < args.length) {
						time = args[++i];
					} else {
						valid = false;
						System.out.println("Error: Time argument '" + arg
								+ "' must be followed by a date-time or a period string that adheres to RFC 3339");
					}
					break;

				case ARGUMENT_PROJECTION:
					if (i < args.length) {
						String proj = args[++i];
						String[] projParts = proj.split(",");
						if (projParts.length != 2) {
							valid = false;
							System.out.println("Error: Projection argument '"
									+ arg
									+ "' value must be in the format: authority,code");
						} else {
							projection = ProjectionFactory
									.getProjection(projParts[0], projParts[1]);
						}
					} else {
						valid = false;
						System.out.println("Error: Projection argument '" + arg
								+ "' must be followed by projection values: authority,code");
					}
					break;

				case ARGUMENT_TOTAL_LIMIT:
					if (i < args.length) {
						totalLimit = Integer.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Total Limit argument '" + arg
								+ "' must be followed by a value");
					}
					break;

				case ARGUMENT_TRANSACTION_LIMIT:
					if (i < args.length) {
						transactionLimit = Integer.valueOf(args[++i]);
					} else {
						valid = false;
						System.out.println("Error: Transaction Limit argument '"
								+ arg + "' must be followed by a value");
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
				if (geoPackageFile == null) {
					geoPackageFile = new File(arg);
				} else if (tableName == null) {
					tableName = arg;
				} else if (server == null) {
					server = arg;
				} else if (id == null) {
					id = arg;
					requiredArguments = true;
				} else {
					valid = false;
					System.out.println(
							"Error: Unsupported extra argument: " + arg);
				}
			}
		}

		if (!valid || !requiredArguments) {
			printUsage();
		} else {
			// Read the features
			try {
				generate();
			} catch (Exception e) {
				printUsage();
				throw e;
			}
		}
	}

	/**
	 * Generate the features
	 */
	public static void generate() {

		// If the GeoPackage does not exist create it
		if (!geoPackageFile.exists()) {
			if (!GeoPackageManager.create(geoPackageFile)) {
				throw new GeoPackageException(
						"Failed to create GeoPackage file: "
								+ geoPackageFile.getAbsolutePath());
			}
		}

		// Open the GeoPackage
		geoPackage = GeoPackageManager.open(geoPackageFile);

		OAPIFeatureGenerator featureGenerator = new OAPIFeatureGenerator(
				geoPackage, tableName, server, id);

		featureGenerator.setLimit(limit);
		featureGenerator.setBoundingBox(boundingBox);
		featureGenerator.setBoundingBoxProjection(boundingBoxProjection);
		featureGenerator.setTime(time);
		featureGenerator.setProjection(projection);
		featureGenerator.setTotalLimit(totalLimit);
		if (transactionLimit != null) {
			featureGenerator.setTransactionLimit(transactionLimit);
		}

		System.out.println();
		System.out.println("GeoPackage: " + geoPackage.getName());
		System.out.println("Feature Table: " + tableName);
		System.out.println("Server: " + server);
		System.out.println("Collection Id: " + id);
		if (limit != null) {
			System.out.println("Limit: " + limit);
		}
		if (boundingBox != null) {
			System.out.println("Bounding Box:");
			System.out.println("\tMin Lon: " + boundingBox.getMinLongitude());
			System.out.println("\tMin Lat: " + boundingBox.getMinLatitude());
			System.out.println("\tMax Lon: " + boundingBox.getMaxLongitude());
			System.out.println("\tMax Lat: " + boundingBox.getMaxLatitude());
		}
		if (boundingBoxProjection != null) {
			System.out.println("Bounding Box Projection: "
					+ boundingBoxProjection.getAuthority() + ","
					+ boundingBoxProjection.getCode());

		}
		if (time != null) {
			System.out.println("Time: " + time);
		}
		if (projection != null) {
			System.out.println("Projection: " + projection.getAuthority() + ","
					+ projection.getCode());
		}
		if (totalLimit != null) {
			System.out.println("Total Limit: " + totalLimit);
		}
		if (transactionLimit != null) {
			System.out.println("Transaction Limit: " + transactionLimit);
		}
		System.out.println("Log Count Frequency: "
				+ progress.getCountFrequency() + " features");
		System.out.println("Log Time Frequency: " + progress.getTimeFrequency()
				+ " seconds");
		System.out.println();

		featureGenerator.setProgress(progress);

		LOGGER.log(Level.INFO, "Generating Features...");

		try {
			featureGenerator.generateFeatures();
		} catch (SQLException e) {
			throw new GeoPackageException("Exception while generating features",
					e);
		}

		finish();
	}

	/**
	 * Finish feature generation
	 */
	private static void finish() {

		if (progress.getMax() != null) {

			StringBuilder output = new StringBuilder();
			output.append("\nFeature Generation: ")
					.append(progress.getProgress()).append(" of ")
					.append(progress.getMax());

			if (geoPackage != null) {
				try {
					GeoPackageTextOutput textOutput = new GeoPackageTextOutput(
							geoPackage);
					output.append("\n\n");
					output.append(textOutput.header());
					output.append("\n\n");
					output.append(textOutput.featureTable(tableName));

				} finally {
					geoPackage.close();
				}
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
		System.out.println("\t[" + ARGUMENT_PREFIX + ARGUMENT_LIMIT
				+ " limit] [" + ARGUMENT_PREFIX + ARGUMENT_BOUNDING_BOX
				+ " minLon,minLat,maxLon,maxLat] [" + ARGUMENT_PREFIX
				+ ARGUMENT_BOUNDING_BOX_PROJECTION + " authority,code] ["
				+ ARGUMENT_PREFIX + ARGUMENT_TIME + " time] [" + ARGUMENT_PREFIX
				+ ARGUMENT_PROJECTION + " authority,code] [" + ARGUMENT_PREFIX
				+ ARGUMENT_TOTAL_LIMIT + " total_limit] [" + ARGUMENT_PREFIX
				+ ARGUMENT_TRANSACTION_LIMIT + " transaction_limit] ["
				+ ARGUMENT_PREFIX + ARGUMENT_LOG_COUNT + " count] ["
				+ ARGUMENT_PREFIX + ARGUMENT_LOG_TIME
				+ " time] geopackage_file table_name server_url collection_id");
		System.out.println();
		System.out.println("DESCRIPTION");
		System.out.println();
		System.out.println(
				"\tGenerates features into a GeoPackage feature table by requesting them from an OGC API Features URL");
		System.out.println();
		System.out.println("ARGUMENTS");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_LIMIT + " limit");
		System.out.println(
				"\t\tLimits the number of items per single server response");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_BOUNDING_BOX
				+ " minLon,minLat,maxLon,maxLat");
		System.out.println(
				"\t\tRequest bounding box for features with intersecting geometries (default is the world)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX
				+ ARGUMENT_BOUNDING_BOX_PROJECTION + " authority,code");
		System.out.println("\t\tProjection of the request bounding box");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_TIME + " time");
		System.out.println(
				"\t\tA date-time or a period string that adheres to RFC3339 (examples: 2018-02-12T23:20:50Z, 2018-02-12T00:00:00Z/2018-03-18T12:31:12Z, 2018-02-12T00:00:00Z/P1M6DT12H31M12S)");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_PROJECTION
				+ " authority,code");
		System.out.println("\t\tRequest projection");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_TOTAL_LIMIT + " total_limit");
		System.out.println(
				"\t\tTotal limit or max number of features to request from the server");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_TRANSACTION_LIMIT
				+ " transaction_limit");
		System.out.println(
				"\t\tLimit on number of features to insert into the GeoPackage in a single transaction");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_LOG_COUNT + " count");
		System.out.println(
				"\t\tLog frequency count of generated features (default is "
						+ LOG_FEATURE_FREQUENCY + ")");
		System.out.println();
		System.out
				.println("\t" + ARGUMENT_PREFIX + ARGUMENT_LOG_TIME + " time");
		System.out.println("\t\tLog frequency time in seconds (default is "
				+ LOG_FEATURE_TIME_FREQUENCY + ")");
		System.out.println();
		System.out.println("\tgeopackage_file");
		System.out.println(
				"\t\tpath to the GeoPackage file to create, or existing file to update");
		System.out.println();
		System.out.println("\ttable_name");
		System.out.println(
				"\t\tfeature table name within the GeoPackage file to create or update");
		System.out.println();
		System.out.println("\tserver_url");
		System.out.println("\t\tOGC API Features base server URL");
		System.out.println();
		System.out.println("\tcollection_id");
		System.out.println("\t\tOGC API Features collection identifier");
		System.out.println();
	}

}
