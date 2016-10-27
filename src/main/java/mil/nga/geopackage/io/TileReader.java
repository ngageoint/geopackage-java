package mil.nga.geopackage.io;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.io.TileDirectory.XDirectory;
import mil.nga.geopackage.io.TileDirectory.YFile;
import mil.nga.geopackage.io.TileDirectory.ZoomDirectory;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.ImageRectangle;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxJavaUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileColumn;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.tiles.user.TileTable;

/**
 * Read tiles from a file system directory into a GeoPackage file
 * 
 * To run from command line, build with the standalone profile:
 * 
 * mvn clean install -Pstandalone
 * 
 * java -classpath geopackage-*-standalone.jar mil.nga.geopackage.io.TileReader
 * +usage_arguments
 * 
 * @author osbornb
 */
public class TileReader {

	/**
	 * Argument prefix
	 */
	public static final String ARGUMENT_PREFIX = "-";

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
	private static final Logger LOGGER = Logger.getLogger(TileReader.class
			.getName());

	/**
	 * Progress log frequency within a zoom level
	 */
	private static final int ZOOM_PROGRESS_FREQUENCY = 100;

	/**
	 * Main method to read tiles from the file system into a GeoPackage
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		boolean valid = true;
		boolean requiredArguments = false;

		String imageFormat = null;
		boolean rawImage = false;
		File inputDirectory = null;
		TileFormatType tileType = null;
		File geoPackageFile = null;
		String tileTable = null;

		for (int i = 0; valid && i < args.length; i++) {

			String arg = args[i];

			// Handle optional arguments
			if (arg.startsWith(ARGUMENT_PREFIX)) {

				String argument = arg.substring(ARGUMENT_PREFIX.length());

				switch (argument) {

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
				if (inputDirectory == null) {
					inputDirectory = new File(arg);
				} else if (tileType == null) {
					tileType = TileFormatType.valueOf(arg.toUpperCase());
				} else if (geoPackageFile == null) {
					geoPackageFile = new File(arg);
				} else if (tileTable == null) {
					tileTable = arg;
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
			// Read the tiles
			try {
				readTiles(geoPackageFile, tileTable, inputDirectory,
						imageFormat, tileType, rawImage);
			} catch (Exception e) {
				printUsage();
				throw e;
			}
		}

	}

	/**
	 * Read the tiles in the directory into the GeoPackage file table
	 * 
	 * @param geoPackageFile
	 *            GeoPackage file
	 * @param tileTable
	 *            tile table
	 * @param directory
	 *            input directory
	 * @param imageFormat
	 *            image format
	 * @param tileType
	 *            tile type
	 * @param rawImage
	 *            use raw image flag
	 * @throws IOException
	 * @throws SQLException
	 */
	public static void readTiles(File geoPackageFile, String tileTable,
			File directory, String imageFormat, TileFormatType tileType,
			boolean rawImage) throws IOException, SQLException {

		// If the GeoPackage does not exist create it
		if (!geoPackageFile.exists()) {
			if (!GeoPackageManager.create(geoPackageFile)) {
				throw new GeoPackageException(
						"Failed to create GeoPackage file: "
								+ geoPackageFile.getAbsolutePath());
			}
		}

		// Open the GeoPackage
		GeoPackage geoPackage = GeoPackageManager.open(geoPackageFile);
		try {
			readTiles(geoPackage, tileTable, directory, imageFormat, tileType,
					rawImage);
		} finally {
			geoPackage.close();
		}
	}

	/**
	 * Read the tiles in the directory into the GeoPackage file table
	 * 
	 * @param geoPackage
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
	 * @throws SQLException
	 */
	public static void readTiles(GeoPackage geoPackage, String tileTable,
			File directory, String imageFormat, TileFormatType tileType,
			boolean rawImage) throws IOException, SQLException {

		// If no format, use the default
		if (imageFormat == null) {
			imageFormat = DEFAULT_IMAGE_FORMAT;
		} else if (rawImage) {
			throw new GeoPackageException(
					"Image format is not used when raw images are used. Choose either image format or raw images.");
		}

		// Build the tile directory structure
		TileDirectory tileDirectory = buildTileDirectory(directory);

		LOGGER.log(Level.INFO,
				"GeoPackage: "
						+ geoPackage.getName()
						+ ", Tile Table: "
						+ tileTable
						+ ", Input Directory: "
						+ directory
						+ (rawImage ? ", Raw Images" : ", Image Format: "
								+ imageFormat) + ", Tiles Type: " + tileType
						+ ", Zoom Range: " + tileDirectory.minZoom + " - "
						+ tileDirectory.maxZoom);

		int totalCount = 0;

		switch (tileType) {

		case GEOPACKAGE:
			totalCount = readGeoPackageFormatTiles(geoPackage, tileTable,
					imageFormat, rawImage, tileDirectory);
			break;

		case STANDARD:
		case TMS:
			totalCount = readFormatTiles(geoPackage, tileTable, imageFormat,
					tileType, rawImage, tileDirectory);
			break;

		default:
			throw new UnsupportedOperationException("Tile Type Not Supported: "
					+ tileType);
		}

		LOGGER.log(Level.INFO, "Total Tiles: " + totalCount);

	}

	/**
	 * Read GeoPackage formatted tiles into a GeoPackage
	 * 
	 * @param geoPackage
	 * @param tileTable
	 * @param imageFormat
	 * @param rawImage
	 * @param tileDirectory
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private static int readGeoPackageFormatTiles(GeoPackage geoPackage,
			String tileTable, String imageFormat, boolean rawImage,
			TileDirectory tileDirectory) throws SQLException, IOException {

		int created = 0;

		TileProperties properties = new TileProperties(tileDirectory.directory);
		properties.load();

		int epsg = properties.getIntegerProperty(
				TileProperties.GEOPACKAGE_PROPERTIES_EPSG, true);
		double minX = properties.getDoubleProperty(
				TileProperties.GEOPACKAGE_PROPERTIES_MIN_X, true);
		double maxX = properties.getDoubleProperty(
				TileProperties.GEOPACKAGE_PROPERTIES_MAX_X, true);
		double minY = properties.getDoubleProperty(
				TileProperties.GEOPACKAGE_PROPERTIES_MIN_Y, true);
		double maxY = properties.getDoubleProperty(
				TileProperties.GEOPACKAGE_PROPERTIES_MAX_Y, true);

		// Create the user tile table
		List<TileColumn> columns = TileTable.createRequiredColumns();
		TileTable table = new TileTable(tileTable, columns);
		geoPackage.createTileTable(table);

		// Get SRS value
		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();
		SpatialReferenceSystem srs = srsDao.getOrCreateFromEpsg(epsg);

		// Create the Tile Matrix Set and Tile Matrix tables
		geoPackage.createTileMatrixSetTable();
		geoPackage.createTileMatrixTable();

		// Create new Contents
		ContentsDao contentsDao = geoPackage.getContentsDao();

		Contents contents = new Contents();
		contents.setTableName(tileTable);
		contents.setDataType(ContentsDataType.TILES);
		contents.setIdentifier(tileTable);
		// contents.setDescription("");
		contents.setLastChange(new Date());
		contents.setMinX(minX);
		contents.setMinY(minY);
		contents.setMaxX(maxX);
		contents.setMaxY(maxY);
		contents.setSrs(srs);

		// Create the contents
		contentsDao.create(contents);

		// Create new Tile Matrix Set
		TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();

		TileMatrixSet tileMatrixSet = new TileMatrixSet();
		tileMatrixSet.setContents(contents);
		tileMatrixSet.setSrs(srs);
		tileMatrixSet.setMinX(minX);
		tileMatrixSet.setMinY(minY);
		tileMatrixSet.setMaxX(maxX);
		tileMatrixSet.setMaxY(maxY);
		tileMatrixSetDao.create(tileMatrixSet);

		// Create new Tile Matrix and tile table rows by going through each zoom
		// level
		TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

		Integer lastZoom = null;
		Integer lastMatrixWidth = null;
		Integer lastMatrixHeight = null;

		for (ZoomDirectory zoomDirectory : tileDirectory.zooms.values()) {

			int zoomCount = 0;

			Integer tileWidth = null;
			Integer tileHeight = null;

			// Determine the matrix width and height
			Integer matrixWidth = properties.getIntegerProperty(
					TileProperties.getMatrixWidthProperty(zoomDirectory.zoom),
					false);
			Integer matrixHeight = properties.getIntegerProperty(
					TileProperties.getMatrixHeightProperty(zoomDirectory.zoom),
					false);

			// If the matrix width is not configured as a property, try to
			// determine it
			if (matrixWidth == null) {
				if (lastZoom != null) {
					// Determine the width by a factor of 2 for each zoom level
					matrixWidth = lastMatrixWidth;
					for (int i = lastZoom; i < zoomDirectory.zoom; i++) {
						matrixWidth *= 2;
					}
				} else {
					// Assume the max x is the width
					matrixWidth = zoomDirectory.maxX + 1;
				}
			}

			// If the matrix height is not configured as a property, try to
			// determine it
			if (matrixHeight == null) {
				if (lastZoom != null) {
					// Determine the height by a factor of 2 for each zoom level
					matrixHeight = lastMatrixHeight;
					for (int i = lastZoom; i < zoomDirectory.zoom; i++) {
						matrixHeight *= 2;
					}
				} else {
					// Assume the max y is the height
					matrixHeight = zoomDirectory.maxY + 1;
				}
			}

			// Set values for the next zoom level
			lastZoom = zoomDirectory.zoom;
			lastMatrixWidth = matrixWidth;
			lastMatrixHeight = matrixHeight;

			LOGGER.log(Level.INFO, "Zoom Level: " + zoomDirectory.zoom
					+ ", Width: " + matrixWidth + ", Height: " + matrixHeight
					+ ", Max Tiles: " + (matrixWidth * matrixHeight));

			for (XDirectory xDirectory : zoomDirectory.xValues.values()) {

				for (YFile yFile : xDirectory.yValues.values()) {

					BufferedImage image = null;

					// Set the tile width and height
					if (tileWidth == null || tileHeight == null) {
						image = ImageIO.read(yFile.file);
						tileWidth = image.getWidth();
						tileHeight = image.getHeight();
					}

					TileRow newRow = tileDao.newRow();

					newRow.setZoomLevel(zoomDirectory.zoom);
					newRow.setTileColumn(xDirectory.x);
					newRow.setTileRow(yFile.y);
					if (rawImage) {
						byte[] rawImageBytes = GeoPackageIOUtils
								.fileBytes(yFile.file);
						newRow.setTileData(rawImageBytes);
					} else {
						if (image == null) {
							image = ImageIO.read(yFile.file);
						}
						newRow.setTileData(image, imageFormat);
					}

					tileDao.create(newRow);

					zoomCount++;

					if (zoomCount % ZOOM_PROGRESS_FREQUENCY == 0) {
						LOGGER.log(Level.INFO, "Zoom " + zoomDirectory.zoom
								+ " Tile Progress... " + zoomCount);
					}
				}
			}

			LOGGER.log(Level.INFO, "Zoom " + zoomDirectory.zoom + " Tiles: "
					+ zoomCount);

			// If tiles were saved for the zoom level, create the tile matrix
			// row
			if (zoomCount > 0) {

				double pixelXSize = (maxX - minX) / matrixWidth / tileWidth;
				double pixelYSize = (maxY - minY) / matrixHeight / tileHeight;

				TileMatrix tileMatrix = new TileMatrix();
				tileMatrix.setContents(contents);
				tileMatrix.setZoomLevel(zoomDirectory.zoom);
				tileMatrix.setMatrixWidth(matrixWidth);
				tileMatrix.setMatrixHeight(matrixHeight);
				tileMatrix.setTileWidth(tileWidth);
				tileMatrix.setTileHeight(tileHeight);
				tileMatrix.setPixelXSize(pixelXSize);
				tileMatrix.setPixelYSize(pixelYSize);
				tileMatrixDao.create(tileMatrix);

				created += zoomCount;
			}

		}

		return created;
	}

	/**
	 * Read formatted tiles into a GeoPackage
	 * 
	 * @param geoPackage
	 * @param tileTable
	 * @param imageFormat
	 * @param tileType
	 * @param rawImage
	 * @param tileDirectory
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	private static int readFormatTiles(GeoPackage geoPackage, String tileTable,
			String imageFormat, TileFormatType tileType, boolean rawImage,
			TileDirectory tileDirectory) throws IOException, SQLException {

		int created = 0;

		// Find the bounding box that includes all the zoom levels
		BoundingBox webMercatorBoundingBox = null;

		for (ZoomDirectory zoomDirectory : tileDirectory.zooms.values()) {

			int minY = zoomDirectory.minY;
			int maxY = zoomDirectory.maxY;

			// If TMS format, flip the y values
			if (tileType == TileFormatType.TMS) {
				int tempMaxY = TileBoundingBoxUtils.getYAsOppositeTileFormat(
						zoomDirectory.zoom, minY);
				minY = TileBoundingBoxUtils.getYAsOppositeTileFormat(
						zoomDirectory.zoom, maxY);
				maxY = tempMaxY;
			}

			// Get the bounding box at the zoom level
			TileGrid tileGrid = new TileGrid(zoomDirectory.minX,
					zoomDirectory.maxX, minY, maxY);
			BoundingBox zoomBoundingBox = TileBoundingBoxUtils
					.getWebMercatorBoundingBox(tileGrid, zoomDirectory.zoom);

			// Set or expand the bounding box
			if (webMercatorBoundingBox == null) {
				webMercatorBoundingBox = zoomBoundingBox;
			} else {
				webMercatorBoundingBox = TileBoundingBoxUtils.union(
						webMercatorBoundingBox, zoomBoundingBox);
			}
		}

		// Get the bounding box that includes all zoom levels at the min zoom
		// level
		TileGrid totalTileGrid = TileBoundingBoxUtils.getTileGrid(
				webMercatorBoundingBox, tileDirectory.minZoom);
		BoundingBox totalWebMercatorBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(totalTileGrid, tileDirectory.minZoom);

		// Create the user tile table
		List<TileColumn> columns = TileTable.createRequiredColumns();
		TileTable table = new TileTable(tileTable, columns);
		geoPackage.createTileTable(table);

		// Get SRS values
		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();
		SpatialReferenceSystem srsWgs84 = srsDao
				.getOrCreateFromEpsg(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
		SpatialReferenceSystem srsWebMercator = srsDao
				.getOrCreateFromEpsg(ProjectionConstants.EPSG_WEB_MERCATOR);

		// Get the transformation from web mercator to wgs84
		Projection wgs84Projection = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
		Projection webMercator = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
		ProjectionTransform webMercatorToWgs84 = webMercator
				.getTransformation(wgs84Projection);

		// Get the WGS 84 bounding box
		BoundingBox totalWgs84BoundingBox = webMercatorToWgs84
				.transform(totalWebMercatorBoundingBox);

		// Create the Tile Matrix Set and Tile Matrix tables
		geoPackage.createTileMatrixSetTable();
		geoPackage.createTileMatrixTable();

		// Create new Contents
		ContentsDao contentsDao = geoPackage.getContentsDao();

		Contents contents = new Contents();
		contents.setTableName(tileTable);
		contents.setDataType(ContentsDataType.TILES);
		contents.setIdentifier(tileTable);
		// contents.setDescription("");
		contents.setLastChange(new Date());
		contents.setMinX(totalWgs84BoundingBox.getMinLongitude());
		contents.setMinY(totalWgs84BoundingBox.getMinLatitude());
		contents.setMaxX(totalWgs84BoundingBox.getMaxLongitude());
		contents.setMaxY(totalWgs84BoundingBox.getMaxLatitude());
		contents.setSrs(srsWgs84);

		// Create the contents
		contentsDao.create(contents);

		// Create new Tile Matrix Set
		TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();

		TileMatrixSet tileMatrixSet = new TileMatrixSet();
		tileMatrixSet.setContents(contents);
		tileMatrixSet.setSrs(srsWebMercator);
		tileMatrixSet.setMinX(totalWebMercatorBoundingBox.getMinLongitude());
		tileMatrixSet.setMinY(totalWebMercatorBoundingBox.getMinLatitude());
		tileMatrixSet.setMaxX(totalWebMercatorBoundingBox.getMaxLongitude());
		tileMatrixSet.setMaxY(totalWebMercatorBoundingBox.getMaxLatitude());
		tileMatrixSetDao.create(tileMatrixSet);

		// Create new Tile Matrix and tile table rows by going through each zoom
		// level
		TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
		for (ZoomDirectory zoomDirectory : tileDirectory.zooms.values()) {

			int zoomCount = 0;

			Integer tileWidth = null;
			Integer tileHeight = null;

			// Determine the matrix width and height
			TileGrid zoomTotalTileGrid = TileBoundingBoxUtils.getTileGrid(
					totalWebMercatorBoundingBox, zoomDirectory.zoom);
			long matrixWidth = zoomTotalTileGrid.getMaxX()
					- zoomTotalTileGrid.getMinX() + 1;
			long matrixHeight = zoomTotalTileGrid.getMaxY()
					- zoomTotalTileGrid.getMinY() + 1;

			LOGGER.log(Level.INFO, "Zoom Level: " + zoomDirectory.zoom
					+ ", Width: " + matrixWidth + ", Height: " + matrixHeight
					+ ", Max Tiles: " + (matrixWidth * matrixHeight));

			// Create the image for each column and row combination
			for (int column = 0; column < matrixWidth; column++) {

				for (int row = 0; row < matrixHeight; row++) {

					// Image to draw for the column and row
					BufferedImage image = null;
					Graphics graphics = null;
					byte[] rawImageBytes = null;

					// Determine the bounding box of this column and row
					BoundingBox tileMatrixBoundingBox = TileBoundingBoxUtils
							.getBoundingBox(totalWebMercatorBoundingBox,
									matrixWidth, matrixHeight, column, row);

					// Get the x and y tile grid of the bounding box at the zoom
					// level
					TileGrid tileMatrixGrid = TileBoundingBoxUtils.getTileGrid(
							tileMatrixBoundingBox, zoomDirectory.zoom);

					// Build the column and row image from images in the
					// matching x and y locations
					for (int x = (int) tileMatrixGrid.getMinX(); x <= tileMatrixGrid
							.getMaxX(); x++) {

						// Check if the x directory exists and contains images
						XDirectory xDirectory = zoomDirectory.xValues.get(x);
						if (xDirectory != null) {

							for (int y = (int) tileMatrixGrid.getMinY(); y <= tileMatrixGrid
									.getMaxY(); y++) {

								// If TMS file format, change the y value to TMS
								int yLocation = (int) y;
								if (tileType == TileFormatType.TMS) {
									yLocation = TileBoundingBoxUtils
											.getYAsOppositeTileFormat(
													zoomDirectory.zoom,
													yLocation);
								}

								// Check if the y directory exists and contains
								// images
								YFile yFile = xDirectory.yValues.get(yLocation);
								if (yFile != null) {

									// Get the bounding box of the x, y, z image
									BoundingBox imageBoundingBox = TileBoundingBoxUtils
											.getWebMercatorBoundingBox(x, y,
													zoomDirectory.zoom);

									// Get the bounding box overlap between the
									// column/row image and the x,y,z image
									BoundingBox overlap = TileBoundingBoxUtils
											.overlap(tileMatrixBoundingBox,
													imageBoundingBox);

									// If the tile overlaps
									if (overlap != null) {

										BufferedImage zxyImage = null;

										// Set the tile width and height
										if (tileWidth == null
												|| tileHeight == null) {
											zxyImage = ImageIO.read(yFile.file);
											tileWidth = zxyImage.getWidth();
											tileHeight = zxyImage.getHeight();
										}

										// Get the rectangle of the source image
										ImageRectangle src = TileBoundingBoxJavaUtils
												.getRectangle(tileWidth,
														tileHeight,
														imageBoundingBox,
														overlap);

										// Get the rectangle of where to draw
										// the tile in the resulting image
										ImageRectangle dest = TileBoundingBoxJavaUtils
												.getRectangle(tileWidth,
														tileHeight,
														tileMatrixBoundingBox,
														overlap);

										// Round the rectangles and make sure
										// the bounds are valid
										if (src.isValid() && dest.isValid()) {

											// Save off raw bytes
											if (rawImage) {

												// Verify only one image was
												// found and it lines up
												// perfectly
												if (rawImageBytes != null
														|| !src.equals(dest)) {
													throw new GeoPackageException(
															"Raw image only supported when the images are aligned with the tile format requiring no combining and cropping");
												}

												// Read the file bytes
												rawImageBytes = GeoPackageIOUtils
														.fileBytes(yFile.file);
											} else {

												// Create the image first time
												// through
												if (image == null) {
													image = ImageUtils
															.createBufferedImage(
																	tileWidth,
																	tileHeight,
																	imageFormat);
													graphics = image
															.getGraphics();
												}

												if (zxyImage == null) {
													zxyImage = ImageIO
															.read(yFile.file);
												}

												// Draw the tile to the image
												graphics.drawImage(zxyImage,
														dest.getLeft(),
														dest.getTop(),
														dest.getRight(),
														dest.getBottom(),
														src.getLeft(),
														src.getTop(),
														src.getRight(),
														src.getBottom(), null);
											}

										}
									}
								}
							}
						}
					}

					// If an image was drawn and is not fully transparent,
					// create the tile row
					if ((image != null && !ImageUtils.isFullyTransparent(image))
							|| rawImageBytes != null) {
						TileRow newRow = tileDao.newRow();

						newRow.setZoomLevel(zoomDirectory.zoom);
						newRow.setTileColumn(column);
						newRow.setTileRow(row);
						if (rawImage) {
							newRow.setTileData(rawImageBytes);
						} else {
							newRow.setTileData(image, imageFormat);
						}

						tileDao.create(newRow);

						zoomCount++;

						if (zoomCount % ZOOM_PROGRESS_FREQUENCY == 0) {
							LOGGER.log(Level.INFO, "Zoom " + zoomDirectory.zoom
									+ " Tile Progress... " + zoomCount);
						}
					}
				}

			}

			LOGGER.log(Level.INFO, "Zoom " + zoomDirectory.zoom + " Tiles: "
					+ zoomCount);

			// If tiles were saved for the zoom level, create the tile matrix
			// row
			if (zoomCount > 0) {
				double pixelXSize = TileBoundingBoxUtils.getPixelXSize(
						totalWebMercatorBoundingBox, matrixWidth, tileWidth);
				double pixelYSize = TileBoundingBoxUtils.getPixelYSize(
						totalWebMercatorBoundingBox, matrixHeight, tileHeight);

				TileMatrix tileMatrix = new TileMatrix();
				tileMatrix.setContents(contents);
				tileMatrix.setZoomLevel(zoomDirectory.zoom);
				tileMatrix.setMatrixWidth(matrixWidth);
				tileMatrix.setMatrixHeight(matrixHeight);
				tileMatrix.setTileWidth(tileWidth);
				tileMatrix.setTileHeight(tileHeight);
				tileMatrix.setPixelXSize(pixelXSize);
				tileMatrix.setPixelYSize(pixelYSize);
				tileMatrixDao.create(tileMatrix);

				created += zoomCount;
			}

		}

		return created;
	}

	/**
	 * Determine and build the directory structure of images
	 * 
	 * @param directory
	 * @return
	 */
	private static TileDirectory buildTileDirectory(File directory) {

		TileDirectory tileDirectory = new TileDirectory();
		tileDirectory.directory = directory;

		// Search for zoom level directories
		for (File zoomDirectory : directory.listFiles()) {
			if (zoomDirectory.isDirectory()) {
				try {
					Integer zoomLevel = Integer
							.valueOf(zoomDirectory.getName());
					ZoomDirectory zoom = tileDirectory.new ZoomDirectory();
					zoom.directory = new File(tileDirectory.directory,
							zoomDirectory.getName());
					zoom.zoom = zoomLevel;
					tileDirectory.zooms.put(zoomLevel, zoom);
					tileDirectory.minZoom = Math.min(tileDirectory.minZoom,
							zoomLevel);
					tileDirectory.maxZoom = Math.max(tileDirectory.maxZoom,
							zoomLevel);

					// Search for x level directories
					for (File xDirectory : zoomDirectory.listFiles()) {
						if (xDirectory.isDirectory()) {
							try {
								Integer xValue = Integer.valueOf(xDirectory
										.getName());
								XDirectory x = tileDirectory.new XDirectory();
								x.directory = new File(zoom.directory,
										xDirectory.getName());
								x.x = xValue;
								zoom.xValues.put(xValue, x);
								zoom.minX = Math.min(zoom.minX, xValue);
								zoom.maxX = Math.max(zoom.maxX, xValue);

								// Search for y level directories
								for (File yImage : xDirectory.listFiles()) {
									if (yImage.isFile()) {
										try {
											String yImageName = yImage
													.getName();
											int extensionLocation = yImageName
													.lastIndexOf(".");
											if (extensionLocation >= 0) {
												yImageName = yImageName
														.substring(0,
																extensionLocation);
											}
											YFile y = tileDirectory.new YFile();
											y.y = Integer.valueOf(yImageName);
											y.file = new File(x.directory,
													yImage.getName());

											x.yValues.put(y.y, y);
											x.minY = Math.min(x.minY, y.y);
											x.maxY = Math.max(x.maxY, y.y);
										} catch (NumberFormatException e) {
											LOGGER.log(
													Level.INFO,
													"Skipping file: "
															+ yImage.getAbsolutePath());
										}

									} else {
										LOGGER.log(
												Level.INFO,
												"Skipping directory: "
														+ yImage.getAbsolutePath());
									}
								}

								zoom.minY = Math.min(zoom.minY, x.minY);
								zoom.maxY = Math.max(zoom.maxY, x.maxY);

							} catch (NumberFormatException e) {
								LOGGER.log(Level.INFO, "Skipping directory: "
										+ xDirectory.getAbsolutePath());
							}
						} else {
							LOGGER.log(Level.INFO, "Skipping file: "
									+ xDirectory.getAbsolutePath());
						}
					}
				} catch (NumberFormatException e) {
					LOGGER.log(Level.INFO, "Skipping directory: "
							+ zoomDirectory.getAbsolutePath());
				}
			} else {
				LOGGER.log(Level.INFO,
						"Skipping file: " + zoomDirectory.getAbsolutePath());
			}
		}

		return tileDirectory;
	}

	/**
	 * Print usage for the main method
	 */
	private static void printUsage() {
		System.out.println();
		System.out.println("USAGE");
		System.out.println();
		System.out.println("\t[" + ARGUMENT_PREFIX + ARGUMENT_IMAGE_FORMAT
				+ " image_format] [" + ARGUMENT_PREFIX + ARGUMENT_RAW_IMAGE
				+ "] input_directory tile_type geopackage_file tile_table");
		System.out.println();
		System.out.println("DESCRIPTION");
		System.out.println();
		System.out
				.println("\tReads a tile set from the file system in a z/x/y folder system into a new table in a new or existing GeoPackage file");
		System.out.println();
		System.out.println("ARGUMENTS");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_IMAGE_FORMAT
				+ " image_format");
		System.out
				.println("\t\tStorage image format in the GeoPackage: png, jpg, jpeg (default is '"
						+ DEFAULT_IMAGE_FORMAT + "')");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_RAW_IMAGE);
		System.out
				.println("\t\tUse the raw image bytes, only works when combining and cropping is not required. Not compatible with image_format");
		System.out.println();
		System.out.println("\tinput_directory");
		System.out
				.println("\t\tinput directory containing the tile image set used to create the GeoPakage tiles");
		System.out.println();
		System.out.println("\ttile_type");
		System.out
				.println("\t\tTile input format specifying z/x/y folder organization: "
						+ TileFormatType.GEOPACKAGE.name().toLowerCase()
						+ ", "
						+ TileFormatType.STANDARD.name().toLowerCase()
						+ ", "
						+ TileFormatType.TMS.name().toLowerCase());
		System.out
				.println("\t\t\t"
						+ TileFormatType.GEOPACKAGE.name().toLowerCase()
						+ " - x and y represent GeoPackage Tile Matrix width and height. Requires a input_directory/"
						+ TileProperties.GEOPACKAGE_PROPERTIES_FILE + " file");
		System.out.println("\t\t\t"
				+ TileFormatType.STANDARD.name().toLowerCase()
				+ " - x and y origin is top left (Google format)");
		System.out.println("\t\t\t" + TileFormatType.TMS.name().toLowerCase()
				+ " - (Tile Map Service) x and y origin is bottom left");
		System.out.println();
		System.out.println("\tgeopackage_file");
		System.out
				.println("\t\tpath to the GeoPackage file to create or add a new tile table to");
		System.out.println();
		System.out.println("\ttile_table");
		System.out
				.println("\t\tnew tile table name to create within the GeoPackage file");
		System.out.println();
		System.out.println("GEOPACKAGE PROPERTIES");
		System.out.println();
		System.out
				.println("\tReading tiles with a tile_type of geopackage requires a properties file located at: input_directory/"
						+ TileProperties.GEOPACKAGE_PROPERTIES_FILE);
		System.out.println();
		System.out.println("\tRequired Properties:");
		System.out.println();
		System.out.println("\t\t" + TileProperties.GEOPACKAGE_PROPERTIES_EPSG
				+ "=");
		System.out.println("\t\t" + TileProperties.GEOPACKAGE_PROPERTIES_MIN_X
				+ "=");
		System.out.println("\t\t" + TileProperties.GEOPACKAGE_PROPERTIES_MAX_X
				+ "=");
		System.out.println("\t\t" + TileProperties.GEOPACKAGE_PROPERTIES_MIN_Y
				+ "=");
		System.out.println("\t\t" + TileProperties.GEOPACKAGE_PROPERTIES_MAX_Y
				+ "=");
		System.out.println();
		System.out.println("\tOptional Properties:");
		System.out.println();
		System.out
				.println("\t\tIf the file structure is fully populated and represents the matrix width and height, the properties can be omitted.");
		System.out
				.println("\t\tIf a non top zoom level matrix width and height increase by a factor of 2 with each zoom level, the properties can be omitted for those zoom levels.");
		System.out.println();
		System.out.println("\t\t"
				+ TileProperties.getMatrixWidthProperty("{zoom}") + "=");
		System.out.println("\t\t"
				+ TileProperties.getMatrixHeightProperty("{zoom}") + "=");
		System.out.println();

	}
}
