package mil.nga.geopackage.io;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
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
 * java -classpath geopackage-*-standalone.jar
 * mil.nga.geopackage.io.GeoPackageTileWriter geopackage_file tile_table
 * output_directory tile_type [image_format]
 * 
 * @author osbornb
 */
public class GeoPackageTileWriter {

	/**
	 * Supported tile types
	 */
	public enum TileType {

		/**
		 * x and y coordinates created using tile matrix width and height
		 * location
		 */
		GEOPACKAGE,

		/**
		 * 0,0 is upper left
		 */
		STANDARD,

		/**
		 * Tile Map Service specification, 0,0 is lower left
		 */
		TMS
	}

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
	 * Default tile type
	 */
	public static final TileType DEFAULT_TILE_TYPE = TileType.STANDARD;

	/**
	 * Default image format
	 */
	public static final String DEFAULT_IMAGE_FORMAT = "png";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(GeoPackageTileWriter.class.getName());

	/**
	 * Main method to write tiles from a GeoPackage
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		boolean valid = true;
		boolean requiredArguments = false;

		TileType tileType = null;
		String imageFormat = null;
		File geoPackageFile = null;
		String tileTable = null;
		File outputDirectory = null;

		for (int i = 0; valid && i < args.length; i++) {

			String arg = args[i];

			if (arg.startsWith(ARGUMENT_PREFIX)) {

				String argument = arg.substring(ARGUMENT_PREFIX.length());

				switch (argument) {
				case ARGUMENT_TILE_TYPE:
					if (i < args.length) {
						tileType = TileType.valueOf(args[++i].toUpperCase());
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
				default:
					valid = false;
					System.out.println("Error: Unsupported arg: '" + arg + "'");
				}

			} else {
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
			System.out.println();
			System.out.println("USAGE");
			System.out.println();
			System.out
					.println("\t["
							+ ARGUMENT_PREFIX
							+ ARGUMENT_TILE_TYPE
							+ " tile_type] ["
							+ ARGUMENT_PREFIX
							+ ARGUMENT_IMAGE_FORMAT
							+ " image_format] geopackage_file tile_table output_directory");
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
					.println("\t\tType of tiles to output: geopackage, standard, tms. Default is "
							+ DEFAULT_TILE_TYPE.toString().toLowerCase()
							+ ". GeoPackage x and y represent width and height columns, Standard starts from top left (i.e. Google), Tile Map Service starts from bottom left");
			System.out.println();
			System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_IMAGE_FORMAT
					+ " image_format");
			System.out.println("\t\tOutput image format (default is '"
					+ DEFAULT_IMAGE_FORMAT + "')");
			System.out.println();
			System.out.println("\tgeopackage_file");
			System.out
					.println("\t\tpath to the GeoPackage file containing the tiles");
			System.out.println();
			System.out.println("\ttile_table");
			System.out
					.println("\t\ttile table name within the GeoPackage file");
			System.out.println();
			System.out.println("\toutput_directory");
			System.out
					.println("\t\toutput directory to write the tile images to");
			System.out.println();
		} else {
			writeTiles(geoPackageFile, tileTable, outputDirectory, imageFormat,
					tileType);
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
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void writeTiles(File geoPackageFile, String tileTable,
			File directory, String imageFormat, TileType tileType)
			throws SQLException, IOException {

		GeoPackage geoPackage = GeoPackageManager.open(geoPackageFile);
		try {
			writeTiles(geoPackage, tileTable, directory, imageFormat, tileType);
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
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void writeTiles(GeoPackage geoPackage, String tileTable,
			File directory, String imageFormat, TileType tileType)
			throws SQLException, IOException {

		// Get a tile data access object for the tile table
		TileDao tileDao = geoPackage.getTileDao(tileTable);

		// If no format, use the default
		if (imageFormat == null) {
			imageFormat = DEFAULT_IMAGE_FORMAT;
		}

		// If not tiles type, use the default
		if (tileType == null) {
			tileType = DEFAULT_TILE_TYPE;
		}

		LOGGER.log(Level.INFO,
				"GeoPackage: " + geoPackage.getName() + ", Tile Table: "
						+ tileTable + ", Output Directory: " + directory
						+ ", Image Format: " + imageFormat + ", Tiles Type: "
						+ tileType + ", Zoom Range: " + tileDao.getMinZoom()
						+ " - " + tileDao.getMaxZoom());

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
			if (tileType == TileType.GEOPACKAGE) {
				zoomCount = writeGeoPackageFormatTiles(tileDao, zoomLevel,
						tileMatrix, zDirectory, imageFormat);
			} else {
				zoomCount = writeFormatTiles(tileDao, zoomLevel, tileMatrix,
						zDirectory, imageFormat, tileType);
			}

			LOGGER.log(Level.INFO, "Zoom " + zoomLevel + " Tiles: " + zoomCount);

			totalCount += zoomCount;
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
	 * @return
	 * @throws IOException
	 */
	private static int writeGeoPackageFormatTiles(TileDao tileDao,
			long zoomLevel, TileMatrix tileMatrix, File zDirectory,
			String imageFormat) throws IOException {

		int tileCount = 0;

		// Go through each x in the width
		for (int x = 0; x < tileMatrix.getMatrixWidth(); x++) {

			File xDirectory = new File(zDirectory, String.valueOf(x));

			// Go through each y in the height
			for (int y = 0; y < tileMatrix.getMatrixHeight(); y++) {

				// Query for a tile at the x, y, z
				TileRow tileRow = tileDao.queryForTile(x, y, zoomLevel);

				if (tileRow != null) {

					// Get the image bytes
					byte[] tileData = tileRow.getTileData();

					if (tileData != null) {

						// Make any needed directories for the image
						xDirectory.mkdirs();

						// Create the buffered image
						BufferedImage image = ImageIO
								.read(new ByteArrayInputStream(tileData));

						File imageFile = new File(xDirectory, String.valueOf(y)
								+ "." + imageFormat);

						// Write the image to the file
						ImageIO.write(image, imageFormat, imageFile);
						tileCount++;
					}
				}
			}
		}

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
	 * @return
	 * @throws IOException
	 */
	private static int writeFormatTiles(TileDao tileDao, long zoomLevel,
			TileMatrix tileMatrix, File zDirectory, String imageFormat,
			TileType tileType) throws IOException {

		int tileCount = 0;

		Projection webMercator = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);

		long epsg = tileDao.getTileMatrixSet().getSrs()
				.getOrganizationCoordsysId();
		Projection projection = ProjectionFactory.getProjection(epsg);

		ProjectionTransform projectionToWebMercator = projection
				.getTransformation(webMercator);

		TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();
		BoundingBox setProjectionBoundingBox = tileMatrixSet.getBoundingBox();
		BoundingBox setWebMercatorBoundingBox = projectionToWebMercator
				.transform(setProjectionBoundingBox);

		TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
				setWebMercatorBoundingBox, (int) zoomLevel);

		// Go through each tile in the tile grid
		for (long x = tileGrid.getMinX(); x <= tileGrid.getMaxX(); x++) {

			File xDirectory = new File(zDirectory, String.valueOf(x));

			for (long y = tileGrid.getMinY(); y <= tileGrid.getMaxY(); y++) {

				long yFileName = y;
				if (tileType == TileType.TMS) {
					yFileName = TileBoundingBoxUtils.getYAsTMS((int) zoomLevel,
							(int) y);
				}

				// Create the buffered image
				BufferedImage image = drawTile(tileDao, tileMatrix,
						setWebMercatorBoundingBox, x, y, zoomLevel);

				if (image != null) {

					// Make any needed directories for the image
					xDirectory.mkdirs();

					File imageFile = new File(xDirectory,
							String.valueOf(yFileName) + "." + imageFormat);

					// Write the image to the file
					ImageIO.write(image, imageFormat, imageFile);
					tileCount++;
				}

			}

		}

		return tileCount;
	}

	/**
	 * Draw the tile for the x, y, and z
	 * 
	 * @param tileDao
	 * @param tileMatrix
	 * @param setWebMercatorBoundingBox
	 * @param x
	 * @param y
	 * @param zoomLevel
	 * @return
	 * @throws IOException
	 */
	private static BufferedImage drawTile(TileDao tileDao,
			TileMatrix tileMatrix, BoundingBox setWebMercatorBoundingBox,
			long x, long y, long zoomLevel) throws IOException {

		BufferedImage image = null;

		// Get the bounding box of the requested tile
		BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(x, y, (int) zoomLevel);

		// Get the tile grid
		TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
				setWebMercatorBoundingBox, tileMatrix.getMatrixWidth(),
				tileMatrix.getMatrixHeight(), webMercatorBoundingBox);

		// Query for matching tiles in the tile grid
		TileResultSet tileResultSet = tileDao.queryByTileGrid(tileGrid,
				zoomLevel);
		if (tileResultSet != null) {

			try {

				// Get the requested tile dimensions
				int tileWidth = (int) tileMatrix.getTileWidth();
				int tileHeight = (int) tileMatrix.getTileHeight();

				// Draw the resulting bitmap with the matching tiles
				Graphics graphics = null;
				while (tileResultSet.moveToNext()) {

					// Get the next tile
					TileRow tileRow = tileResultSet.getRow();

					if (tileRow != null) {

						// Get the image bytes
						byte[] tileData = tileRow.getTileData();

						if (tileData != null) {

							// Create the buffered image
							BufferedImage tileImage = ImageIO
									.read(new ByteArrayInputStream(tileData));

							// Get the bounding box of the tile
							BoundingBox tileWebMercatorBoundingBox = TileBoundingBoxUtils
									.getWebMercatorBoundingBox(
											setWebMercatorBoundingBox,
											tileMatrix,
											tileRow.getTileColumn(),
											tileRow.getTileRow());

							// Get the bounding box where the requested image
							// and tile overlap
							BoundingBox overlap = TileBoundingBoxUtils.overlap(
									webMercatorBoundingBox,
									tileWebMercatorBoundingBox);

							// If the tile overlaps with the requested box
							if (overlap != null) {

								// Get the rectangle of the tile image to draw
								ImageRectangleF src = getRectangle(
										tileMatrix.getTileWidth(),
										tileMatrix.getTileHeight(),
										tileWebMercatorBoundingBox, overlap);

								// Get the rectangle of where to draw the tile
								// in the resulting image
								ImageRectangleF dest = getRectangle(tileWidth,
										tileHeight, webMercatorBoundingBox,
										overlap);

								// Round the rectangles and make sure the bounds
								// are valid
								ImageRectangle srcRounded = src.round();
								ImageRectangle destRounded = dest.round();
								if (srcRounded.isValid()
										&& destRounded.isValid()) {

									// Create the image first time through
									if (image == null) {
										image = new BufferedImage(tileWidth,
												tileHeight,
												BufferedImage.TYPE_INT_ARGB);
										graphics = image.getGraphics();
									}

									// Draw the tile to the image
									graphics.drawImage(tileImage,
											destRounded.getLeft(),
											destRounded.getTop(),
											destRounded.getRight(),
											destRounded.getBottom(),
											srcRounded.getLeft(),
											srcRounded.getTop(),
											srcRounded.getRight(),
											srcRounded.getBottom(), null);
								}
							}
						}
					}
				}
			} finally {
				tileResultSet.close();
			}

		}

		return image;
	}

	/**
	 * Get a rectangle with floating point boundaries using the tile width,
	 * height, bounding box, and the bounding box section within the outer box
	 * to build the rectangle from
	 * 
	 * @param width
	 * @param height
	 * @param boundingBox
	 * @param boundingBoxSection
	 * @return
	 */
	public static ImageRectangleF getRectangle(long width, long height,
			BoundingBox boundingBox, BoundingBox boundingBoxSection) {

		float left = TileBoundingBoxUtils.getXPixel(width, boundingBox,
				boundingBoxSection.getMinLongitude());
		float right = TileBoundingBoxUtils.getXPixel(width, boundingBox,
				boundingBoxSection.getMaxLongitude());
		float top = TileBoundingBoxUtils.getYPixel(height, boundingBox,
				boundingBoxSection.getMaxLatitude());
		float bottom = TileBoundingBoxUtils.getYPixel(height, boundingBox,
				boundingBoxSection.getMinLatitude());

		ImageRectangleF rect = new ImageRectangleF(left, top, right, bottom);

		return rect;
	}

}
