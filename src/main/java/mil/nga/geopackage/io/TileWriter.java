package mil.nga.geopackage.io;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileDraw;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Writes the tiles from a GeoPackage tile table to a file system directory
 * 
 * To run from command line, build with the standalone profile:
 * 
 * mvn clean install -Pstandalone
 * 
 * java -classpath geopackage-*-standalone.jar mil.nga.geopackage.io.TileWriter
 * +usage_arguments
 * 
 * @author osbornb
 */
public class TileWriter {

	/**
	 * Argument prefix
	 */
	public static final String ARGUMENT_PREFIX = "-";

	/**
	 * Tile Type argument
	 */
	public static final String ARGUMENT_TILE_TYPE = "t";

	/**
	 * Image Format argument
	 */
	public static final String ARGUMENT_IMAGE_FORMAT = "i";

	/**
	 * Raw image argument
	 */
	public static final String ARGUMENT_RAW_IMAGE = "r";

	/**
	 * Default tile type
	 */
	public static final TileFormatType DEFAULT_TILE_TYPE = TileFormatType.STANDARD;

	/**
	 * Default image format
	 */
	public static final String DEFAULT_IMAGE_FORMAT = ImageUtils.IMAGE_FORMAT_PNG;

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(TileWriter.class
			.getName());

	/**
	 * Progress log frequency within a zoom level
	 */
	private static final int ZOOM_PROGRESS_FREQUENCY = 100;

	/**
	 * Main method to write tiles from a GeoPackage
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		boolean valid = true;
		boolean requiredArguments = false;

		TileFormatType tileType = null;
		String imageFormat = null;
		boolean rawImage = false;
		File geoPackageFile = null;
		String tileTable = null;
		File outputDirectory = null;

		for (int i = 0; valid && i < args.length; i++) {

			String arg = args[i];

			// Handle optional arguments
			if (arg.startsWith(ARGUMENT_PREFIX)) {

				String argument = arg.substring(ARGUMENT_PREFIX.length());

				switch (argument) {
				case ARGUMENT_TILE_TYPE:
					if (i < args.length) {
						tileType = TileFormatType.valueOf(args[++i]
								.toUpperCase());
					} else {
						valid = false;
						System.out
								.println("Error: Tile Type argument '"
										+ arg
										+ "' must be followed by a tile type (geopackage, standard, tms)");
					}
					break;

				case ARGUMENT_IMAGE_FORMAT:
					if (i < args.length) {
						imageFormat = args[++i];
					} else {
						valid = false;
						System.out.println("Error: Image Format argument '"
								+ arg + "' must be followed by a image format");
					}
					break;

				case ARGUMENT_RAW_IMAGE:
					rawImage = true;
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
				} else if (outputDirectory == null) {
					outputDirectory = new File(arg);
					requiredArguments = true;
				} else {
					valid = false;
					System.out.println("Error: Unsupported extra argument: "
							+ arg);
				}
			}
		}

		if (!valid || !requiredArguments) {
			printUsage();
		} else {
			// Write the tiles
			try {
				writeTiles(geoPackageFile, tileTable, outputDirectory,
						imageFormat, tileType, rawImage);
			} catch (Exception e) {
				printUsage();
				throw e;
			}
		}

	}

	/**
	 * Write the tile table tile image set within the GeoPackage file to the
	 * provided directory
	 * 
	 * @param geoPackageFile
	 *            GeoPackage file
	 * @param tileTable
	 *            tile table
	 * @param directory
	 *            output directory
	 * @param imageFormat
	 *            image format
	 * @param tileType
	 *            tile type
	 * @param rawImage
	 *            use raw image flag
	 * @throws IOException
	 */
	public static void writeTiles(File geoPackageFile, String tileTable,
			File directory, String imageFormat, TileFormatType tileType,
			boolean rawImage) throws IOException {

		GeoPackage geoPackage = GeoPackageManager.open(geoPackageFile);
		try {
			writeTiles(geoPackage, tileTable, directory, imageFormat, tileType,
					rawImage);
		} finally {
			geoPackage.close();
		}
	}

	/**
	 * Write the tile table tile image set within the GeoPackage file to the
	 * provided directory
	 * 
	 * @param geoPackageFile
	 *            open GeoPackage
	 * @param tileTable
	 *            tile table
	 * @param directory
	 *            output directory
	 * @param imageFormat
	 *            image format
	 * @param tileType
	 *            tile type
	 * @param rawImage
	 *            use raw image flag
	 * @throws IOException
	 */
	public static void writeTiles(GeoPackage geoPackage, String tileTable,
			File directory, String imageFormat, TileFormatType tileType,
			boolean rawImage) throws IOException {

		// Get a tile data access object for the tile table
		TileDao tileDao = geoPackage.getTileDao(tileTable);

		// If no format, use the default
		if (imageFormat == null) {
			imageFormat = DEFAULT_IMAGE_FORMAT;
		}

		// If no tiles type, use the default
		if (tileType == null) {
			tileType = DEFAULT_TILE_TYPE;
		}

		LOGGER.log(Level.INFO, "GeoPackage: " + geoPackage.getName()
				+ ", Tile Table: " + tileTable + ", Output Directory: "
				+ directory + (rawImage ? ", Raw Images" : "")
				+ ", Image Format: " + imageFormat + ", Tiles Type: "
				+ tileType + ", Zoom Range: " + tileDao.getMinZoom() + " - "
				+ tileDao.getMaxZoom());

		int totalCount = 0;

		// Go through each zoom level
		for (long zoomLevel = tileDao.getMinZoom(); zoomLevel <= tileDao
				.getMaxZoom(); zoomLevel++) {

			// Get the tile matrix at this zoom level
			TileMatrix tileMatrix = tileDao.getTileMatrix(zoomLevel);

			LOGGER.log(
					Level.INFO,
					"Zoom Level: "
							+ zoomLevel
							+ ", Width: "
							+ tileMatrix.getMatrixWidth()
							+ ", Height: "
							+ tileMatrix.getMatrixHeight()
							+ ", Max Tiles: "
							+ (tileMatrix.getMatrixWidth() * tileMatrix
									.getMatrixHeight()));

			File zDirectory = new File(directory, String.valueOf(zoomLevel));

			int zoomCount = 0;
			switch (tileType) {

			case GEOPACKAGE:
				zoomCount = writeGeoPackageFormatTiles(tileDao, zoomLevel,
						tileMatrix, zDirectory, imageFormat, rawImage);
				break;

			case STANDARD:
			case TMS:
				zoomCount = writeFormatTiles(tileDao, zoomLevel, tileMatrix,
						zDirectory, imageFormat, tileType, rawImage);
				break;

			default:
				throw new UnsupportedOperationException(
						"Tile Type Not Supported: " + tileType);
			}

			LOGGER.log(Level.INFO, "Zoom " + zoomLevel + " Tiles: " + zoomCount);

			totalCount += zoomCount;
		}

		// If GeoPackage format, write a properties file
		if (tileType == TileFormatType.GEOPACKAGE) {
			tileDao = geoPackage.getTileDao(tileTable);
			TileProperties tileProperties = new TileProperties(directory);
			tileProperties.writeFile(tileDao);
		}

		LOGGER.log(Level.INFO, "Total Tiles: " + totalCount);
	}

	/**
	 * Write GeoPackage formatted tiles
	 * 
	 * @param tileDao
	 * @param zoomLevel
	 * @param tileMatrix
	 * @param zDirectory
	 * @param imageFormat
	 * @param rawImage
	 * @return
	 * @throws IOException
	 */
	private static int writeGeoPackageFormatTiles(TileDao tileDao,
			long zoomLevel, TileMatrix tileMatrix, File zDirectory,
			String imageFormat, boolean rawImage) throws IOException {

		int tileCount = 0;

		// Query for all tiles at the zoom level
		TileResultSet tileResultSet = tileDao.queryForTile(zoomLevel);

		while (tileResultSet.moveToNext()) {

			TileRow tileRow = tileResultSet.getRow();

			if (tileRow != null) {

				// Get the image bytes
				byte[] tileData = tileRow.getTileData();

				if (tileData != null) {

					// Make any needed directories for the image
					File xDirectory = new File(zDirectory,
							String.valueOf(tileRow.getTileColumn()));
					xDirectory.mkdirs();

					File imageFile = new File(xDirectory,
							String.valueOf(tileRow.getTileRow()) + "."
									+ imageFormat);

					if (rawImage) {

						// Write the raw image bytes to the file
						FileOutputStream fos = new FileOutputStream(imageFile);
						fos.write(tileData);
						fos.close();

					} else {

						// Read the tile image
						BufferedImage tileImage = tileRow.getTileDataImage();

						// Create the new image in the image format
						BufferedImage image = ImageUtils.createBufferedImage(
								tileImage.getWidth(), tileImage.getHeight(),
								imageFormat);
						Graphics graphics = image.getGraphics();

						// Draw the image
						graphics.drawImage(tileImage, 0, 0, null);

						// Write the image to the file
						ImageIO.write(image, imageFormat, imageFile);
					}

					tileCount++;

					if (tileCount % ZOOM_PROGRESS_FREQUENCY == 0) {
						LOGGER.log(Level.INFO, "Zoom " + zoomLevel
								+ " Tile Progress... " + tileCount);
					}
				}
			}
		}
		tileResultSet.close();

		return tileCount;
	}

	/**
	 * Write formatted tiles
	 * 
	 * @param tileDao
	 * @param zoomLevel
	 * @param tileMatrix
	 * @param zDirectory
	 * @param imageFormat
	 * @param tileType
	 * @param rawImage
	 * @return
	 * @throws IOException
	 */
	private static int writeFormatTiles(TileDao tileDao, long zoomLevel,
			TileMatrix tileMatrix, File zDirectory, String imageFormat,
			TileFormatType tileType, boolean rawImage) throws IOException {

		int tileCount = 0;

		// Get the projection of the tile matrix set
		long epsg = tileDao.getTileMatrixSet().getSrs()
				.getOrganizationCoordsysId();
		Projection projection = ProjectionFactory.getProjection(epsg);

		// Get the transformation to web mercator
		Projection webMercator = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
		ProjectionTransform projectionToWebMercator = projection
				.getTransformation(webMercator);

		// Get the tile matrix set and bounding box
		TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();
		BoundingBox setProjectionBoundingBox = tileMatrixSet.getBoundingBox();
		BoundingBox setWebMercatorBoundingBox = projectionToWebMercator
				.transform(setProjectionBoundingBox);

		// Get the bounding box of actual tiles at the zoom level
		BoundingBox zoomBoundingBox = tileDao.getBoundingBox(zoomLevel);
		BoundingBox zoomWebMercatorBoundingBox = projectionToWebMercator
				.transform(zoomBoundingBox);

		// Determine the tile grid in the world the tiles cover
		TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
				zoomWebMercatorBoundingBox, (int) zoomLevel);

		// Go through each tile in the tile grid
		for (long x = tileGrid.getMinX(); x <= tileGrid.getMaxX(); x++) {

			// Build the z/x directory
			File xDirectory = new File(zDirectory, String.valueOf(x));

			for (long y = tileGrid.getMinY(); y <= tileGrid.getMaxY(); y++) {

				// Get the y file name for the specified format
				long yFileName = y;
				if (tileType == TileFormatType.TMS) {
					yFileName = TileBoundingBoxUtils.getYAsOppositeTileFormat(
							(int) zoomLevel, (int) y);
				}

				File imageFile = new File(xDirectory, String.valueOf(yFileName)
						+ "." + imageFormat);

				TileRow tileRow = null;
				BufferedImage image = null;
				if (rawImage) {
					tileRow = TileDraw.getRawTileRow(tileDao, tileMatrix,
							setWebMercatorBoundingBox, x, y, zoomLevel);
				} else {
					// Create the buffered image
					image = TileDraw.drawTile(tileDao, tileMatrix, imageFormat,
							setWebMercatorBoundingBox, x, y, zoomLevel);
				}

				if (tileRow != null || image != null) {

					// Make any needed directories for the image
					xDirectory.mkdirs();

					if (rawImage) {
						// Write the raw image bytes to the file
						FileOutputStream fos = new FileOutputStream(imageFile);
						fos.write(tileRow.getTileData());
						fos.close();
					} else {
						// Write the image to the file
						ImageIO.write(image, imageFormat, imageFile);
					}

					tileCount++;

					if (tileCount % ZOOM_PROGRESS_FREQUENCY == 0) {
						LOGGER.log(Level.INFO, "Zoom " + zoomLevel
								+ " Tile Progress... " + tileCount);
					}
				}

			}
		}

		return tileCount;
	}

	/**
	 * Print usage for the main method
	 */
	private static void printUsage() {
		System.out.println();
		System.out.println("USAGE");
		System.out.println();
		System.out.println("\t[" + ARGUMENT_PREFIX + ARGUMENT_TILE_TYPE
				+ " tile_type] [" + ARGUMENT_PREFIX + ARGUMENT_IMAGE_FORMAT
				+ " image_format] [" + ARGUMENT_PREFIX + ARGUMENT_RAW_IMAGE
				+ "] geopackage_file tile_table output_directory");
		System.out.println();
		System.out.println("DESCRIPTION");
		System.out.println();
		System.out
				.println("\tWrites a tile set from within a GeoPackage tile table to the file system in a z/x/y folder system according to the specified tile type");
		System.out.println();
		System.out.println("ARGUMENTS");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_TILE_TYPE
				+ " tile_type");
		System.out
				.println("\t\tTile output format specifying z/x/y folder organization: "
						+ TileFormatType.GEOPACKAGE.name().toLowerCase()
						+ ", "
						+ TileFormatType.STANDARD.name().toLowerCase()
						+ ", "
						+ TileFormatType.TMS.name().toLowerCase()
						+ " (Default is "
						+ DEFAULT_TILE_TYPE.name().toLowerCase() + ")");
		System.out
				.println("\t\t\t"
						+ TileFormatType.GEOPACKAGE.name().toLowerCase()
						+ " - x and y represent GeoPackage Tile Matrix width and height");
		System.out.println("\t\t\t"
				+ TileFormatType.STANDARD.name().toLowerCase()
				+ " - x and y origin is top left (Google format)");
		System.out.println("\t\t\t" + TileFormatType.TMS.name().toLowerCase()
				+ " - (Tile Map Service) x and y origin is bottom left");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_IMAGE_FORMAT
				+ " image_format");
		System.out
				.println("\t\tOutput image format: png, jpg, jpeg (default is '"
						+ DEFAULT_IMAGE_FORMAT + "')");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_RAW_IMAGE);
		System.out
				.println("\t\tUse the raw image bytes, only works when combining and cropping is not required");
		System.out.println();
		System.out.println("\tgeopackage_file");
		System.out
				.println("\t\tpath to the GeoPackage file containing the tiles");
		System.out.println();
		System.out.println("\ttile_table");
		System.out.println("\t\ttile table name within the GeoPackage file");
		System.out.println();
		System.out.println("\toutput_directory");
		System.out.println("\t\toutput directory to write the tile images to");
		System.out.println();
	}

}
