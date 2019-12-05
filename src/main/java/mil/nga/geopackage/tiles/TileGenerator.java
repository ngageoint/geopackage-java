package mil.nga.geopackage.tiles;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.locationtech.proj4j.units.Units;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.extension.scale.TileScaling;
import mil.nga.geopackage.extension.scale.TileTableScaling;
import mil.nga.geopackage.io.GeoPackageZoomLevelProgress;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrix.TileMatrixKey;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.tiles.user.TileTable;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;
import mil.nga.sf.proj.ProjectionTransform;

/**
 * Creates a set of tiles within a GeoPackage
 *
 * @author osbornb
 * @since 1.1.2
 */
public abstract class TileGenerator {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(TileGenerator.class.getName());

	/**
	 * GeoPackage
	 */
	private final GeoPackage geoPackage;

	/**
	 * Table Name
	 */
	private final String tableName;

	/**
	 * Min zoom level
	 */
	private final int minZoom;

	/**
	 * Max zoom level
	 */
	private final int maxZoom;

	/**
	 * Tiles projection
	 */
	protected Projection projection;

	/**
	 * Total tile count
	 */
	private Integer tileCount;

	/**
	 * Tile grids by zoom level
	 */
	private final Map<Integer, TileGrid> tileGrids = new HashMap<>();

	/**
	 * Tile bounding boxes by zoom level
	 */
	private final Map<Integer, BoundingBox> tileBounds = new HashMap<>();

	/**
	 * Tile bounding box
	 */
	protected BoundingBox boundingBox;

	/**
	 * Compress format
	 */
	private String compressFormat;

	/**
	 * Compress quality
	 */
	private Float compressQuality = null;

	/**
	 * GeoPackage zoom level progress
	 */
	private GeoPackageZoomLevelProgress progress;

	/**
	 * True when generating tiles in XYZ tile format, false when generating
	 * GeoPackage format where rows and columns do not match the XYZ row &
	 * column coordinates
	 */
	private boolean xyzTiles = false;

	/**
	 * Tile grid bounding box
	 */
	private BoundingBox tileGridBoundingBox;

	/**
	 * Matrix height when GeoPackage tile format
	 */
	private long matrixHeight = 0;

	/**
	 * Matrix width when GeoPackage tile format
	 */
	private long matrixWidth = 0;

	/**
	 * Tile scaling settings
	 */
	private TileScaling scaling = null;

	/**
	 * Skip existing tiles
	 */
	private boolean skipExisting = false;

	/**
	 * Constructor
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param tableName
	 *            table name
	 * @param minZoom
	 *            min zoom
	 * @param maxZoom
	 *            max zoom
	 * @param boundingBox
	 *            tiles bounding box
	 * @param projection
	 *            tiles projection
	 * @since 1.2.0
	 */
	public TileGenerator(GeoPackage geoPackage, String tableName, int minZoom,
			int maxZoom, BoundingBox boundingBox, Projection projection) {
		geoPackage.verifyWritable();
		this.geoPackage = geoPackage;
		this.tableName = tableName;

		this.minZoom = minZoom;
		this.maxZoom = maxZoom;

		this.boundingBox = boundingBox;
		this.projection = projection;
	}

	/**
	 * Get the GeoPackage
	 * 
	 * @return GeoPackage
	 */
	public GeoPackage getGeoPackage() {
		return geoPackage;
	}

	/**
	 * Get the table name
	 * 
	 * @return table name
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Get the min zoom
	 * 
	 * @return min zoom
	 */
	public int getMinZoom() {
		return minZoom;
	}

	/**
	 * Get the max zoom
	 * 
	 * @return max zoom
	 */
	public int getMaxZoom() {
		return maxZoom;
	}

	/**
	 * Get bounding box
	 * 
	 * @return bounding box
	 */
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	/**
	 * Get the bounding box, possibly expanded for the zoom level
	 *
	 * @param zoom
	 *            zoom level
	 * @return original or expanded bounding box
	 * @since 3.2.0
	 */
	public BoundingBox getBoundingBox(int zoom) {
		return boundingBox;
	}

	/**
	 * Set the compress format
	 *
	 * @param compressFormat
	 *            compression format
	 */
	public void setCompressFormat(String compressFormat) {
		this.compressFormat = compressFormat;
	}

	/**
	 * Get the compress format
	 * 
	 * @return compress format
	 */
	public String getCompressFormat() {
		return compressFormat;
	}

	/**
	 * Set the compress quality (0.0 to 1.0). The Compress format must be set
	 * for this to be used.
	 *
	 * @param compressQuality
	 *            compression quality
	 */
	public void setCompressQuality(Float compressQuality) {
		if (compressQuality != null
				&& (compressQuality < 0.0 || compressQuality > 1.0)) {
			throw new GeoPackageException(
					"Compress quality must be between 0.0 and 1.0, not: "
							+ compressQuality);
		}
		this.compressQuality = compressQuality;
	}

	/**
	 * Get the compress quality
	 * 
	 * @return compress quality or null
	 */
	public Float getCompressQuality() {
		return compressQuality;
	}

	/**
	 * Set the progress tracker
	 *
	 * @param progress
	 *            progress tracker
	 */
	public void setProgress(GeoPackageZoomLevelProgress progress) {
		this.progress = progress;
	}

	/**
	 * Get the progress tracker
	 * 
	 * @return progress
	 */
	public GeoPackageZoomLevelProgress getProgress() {
		return progress;
	}

	/**
	 * Set the XYZ Tiles flag to true to generate XYZ tile format tiles. Default
	 * is false
	 *
	 * @param xyzTiles
	 *            XYZ Tiles flag
	 */
	public void setXYZTiles(boolean xyzTiles) {
		this.xyzTiles = xyzTiles;
	}

	/**
	 * Is the XYZ Tiles flag set to generate XYZ tile format tiles.
	 * 
	 * @return true if XYZ Tiles format, false if GeoPackage
	 */
	public boolean isXYZTiles() {
		return xyzTiles;
	}

	/**
	 * Get the tile scaling settings
	 *
	 * @return tile scaling
	 * @since 2.0.2
	 */
	public TileScaling getScaling() {
		return scaling;
	}

	/**
	 * Set the tile scaling settings
	 *
	 * @param scaling
	 *            tile scaling
	 * @since 2.0.2
	 */
	public void setScaling(TileScaling scaling) {
		this.scaling = scaling;
	}

	/**
	 * Is skip existing tiles on
	 * 
	 * @return true if skipping existing tiles
	 * @since 3.4.1
	 */
	public boolean isSkipExisting() {
		return skipExisting;
	}

	/**
	 * Set the skip existing tiles flag
	 * 
	 * @param skipExisting
	 *            true to skip existing tiles
	 * @since 3.4.1
	 */
	public void setSkipExisting(boolean skipExisting) {
		this.skipExisting = skipExisting;
	}

	/**
	 * Get the tile count of tiles to be generated
	 *
	 * @return tile count
	 */
	public int getTileCount() {
		if (tileCount == null) {
			int count = 0;

			boolean degrees = projection.isUnit(Units.DEGREES);
			ProjectionTransform transformToWebMercator = null;
			if (!degrees) {
				transformToWebMercator = projection.getTransformation(
						ProjectionConstants.EPSG_WEB_MERCATOR);
			}

			for (int zoom = minZoom; zoom <= maxZoom; zoom++) {

				BoundingBox expandedBoundingBox = getBoundingBox(zoom);

				// Get the tile grid that includes the entire bounding box
				TileGrid tileGrid = null;
				if (degrees) {
					tileGrid = TileBoundingBoxUtils
							.getTileGridWGS84(expandedBoundingBox, zoom);
				} else {
					tileGrid = TileBoundingBoxUtils
							.getTileGrid(expandedBoundingBox
									.transform(transformToWebMercator), zoom);
				}

				count += tileGrid.count();
				tileGrids.put(zoom, tileGrid);
				tileBounds.put(zoom, expandedBoundingBox);
			}

			tileCount = count;
		}
		return tileCount;
	}

	/**
	 * Generate the tiles
	 *
	 * @return tiles created
	 * @throws java.sql.SQLException
	 *             upon failure
	 * @throws java.io.IOException
	 *             upon failure
	 */
	public int generateTiles() throws SQLException, IOException {

		int totalCount = getTileCount();

		// Set the max progress count
		if (progress != null) {
			progress.setMax(totalCount);
			for (Map.Entry<Integer, TileGrid> zoomGrid : tileGrids.entrySet()) {
				progress.setZoomLevelMax(zoomGrid.getKey(),
						(int) zoomGrid.getValue().count());
			}
		}

		int count = 0;
		boolean update = false;

		// Adjust the tile matrix set and bounds
		BoundingBox minZoomBoundingBox = tileBounds.get(minZoom);
		adjustBounds(minZoomBoundingBox, minZoom);

		// Create a new tile matrix or update an existing
		TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
		TileMatrixSet tileMatrixSet = null;
		if (!tileMatrixSetDao.isTableExists()
				|| !tileMatrixSetDao.idExists(tableName)) {
			// Create the srs if needed
			SpatialReferenceSystemDao srsDao = geoPackage
					.getSpatialReferenceSystemDao();
			SpatialReferenceSystem srs = srsDao.getOrCreateCode(
					projection.getAuthority(),
					Long.parseLong(projection.getCode()));
			// Create the tile table
			tileMatrixSet = geoPackage.createTileTableWithMetadata(tableName,
					boundingBox, srs.getSrsId(), tileGridBoundingBox,
					srs.getSrsId());
		} else {
			update = true;
			// Query to get the Tile Matrix Set
			tileMatrixSet = tileMatrixSetDao.queryForId(tableName);

			// Update the tile bounds between the existing and this request
			updateTileBounds(tileMatrixSet);
		}

		preTileGeneration();

		// If tile scaling is set, create the tile scaling extension entry
		if (scaling != null) {
			TileTableScaling tileTableScaling = new TileTableScaling(geoPackage,
					tileMatrixSet);
			tileTableScaling.createOrUpdate(scaling);
		}

		// Create the tiles
		try {
			Contents contents = tileMatrixSet.getContents();
			TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
			TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

			// Create the new matrix tiles
			for (int zoom = minZoom; zoom <= maxZoom
					&& (progress == null || progress.isActive()); zoom++) {

				TileGrid localTileGrid = null;

				// Determine the matrix width and height for XYZ format
				if (xyzTiles) {
					matrixWidth = TileBoundingBoxUtils.tilesPerSide(zoom);
					matrixHeight = matrixWidth;
				}
				// Get the local tile grid for GeoPackage format of where the
				// tiles belong
				else {
					BoundingBox zoomBoundingBox = tileBounds.get(zoom);
					localTileGrid = TileBoundingBoxUtils.getTileGrid(
							tileGridBoundingBox, matrixWidth, matrixHeight,
							zoomBoundingBox);
				}

				// Generate the tiles for the zoom level
				TileGrid tileGrid = tileGrids.get(zoom);
				count += generateTiles(tileMatrixDao, tileDao, contents, zoom,
						tileGrid, localTileGrid, matrixWidth, matrixHeight,
						update);

				if (!xyzTiles) {
					// Double the matrix width and height for the next level
					matrixWidth *= 2;
					matrixHeight *= 2;
				}
			}

			// Delete the table if cancelled
			if (progress != null && !progress.isActive()
					&& progress.cleanupOnCancel()) {
				geoPackage.deleteTableQuietly(tableName);
				count = 0;
			} else {
				// Update the contents last modified date
				contents.setLastChange(new Date());
				ContentsDao contentsDao = geoPackage.getContentsDao();
				contentsDao.update(contents);
			}
		} catch (RuntimeException e) {
			geoPackage.deleteTableQuietly(tableName);
			throw e;
		} catch (SQLException e) {
			geoPackage.deleteTableQuietly(tableName);
			throw e;
		} catch (IOException e) {
			geoPackage.deleteTableQuietly(tableName);
			throw e;
		}

		return count;
	}

	/**
	 * Adjust the tile matrix set and bounds
	 *
	 * @param boundingBox
	 *            bounding box
	 * @param zoom
	 *            zoom
	 */
	private void adjustBounds(BoundingBox boundingBox, int zoom) {
		// XYZ Tile Format
		if (xyzTiles) {
			adjustXYZBounds();
		} else if (projection.isUnit(Units.DEGREES)) {
			adjustGeoPackageBoundsWGS84(boundingBox, zoom);
		} else {
			adjustGeoPackageBounds(boundingBox, zoom);
		}
	}

	/**
	 * Adjust the tile matrix set and web mercator bounds for XYZ tile format
	 */
	private void adjustXYZBounds() {
		// Set the tile matrix set bounding box to be the world
		BoundingBox standardWgs84Box = new BoundingBox(
				-ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH,
				ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE,
				ProjectionConstants.WGS84_HALF_WORLD_LON_WIDTH,
				ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE);
		ProjectionTransform wgs84ToWebMercatorTransform = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
				.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
		tileGridBoundingBox = standardWgs84Box
				.transform(wgs84ToWebMercatorTransform);
	}

	/**
	 * Adjust the tile matrix set and WGS84 bounds for GeoPackage format.
	 * Determine the tile grid width and height
	 *
	 * @param boundingBox
	 * @param zoom
	 */
	private void adjustGeoPackageBoundsWGS84(BoundingBox boundingBox,
			int zoom) {
		// Get the fitting tile grid and determine the bounding box that fits it
		TileGrid tileGrid = TileBoundingBoxUtils.getTileGridWGS84(boundingBox,
				zoom);
		tileGridBoundingBox = TileBoundingBoxUtils.getWGS84BoundingBox(tileGrid,
				zoom);
		matrixWidth = tileGrid.getMaxX() + 1 - tileGrid.getMinX();
		matrixHeight = tileGrid.getMaxY() + 1 - tileGrid.getMinY();
	}

	/**
	 * Adjust the tile matrix set and web mercator bounds for GeoPackage format.
	 * Determine the tile grid width and height
	 *
	 * @param requestWebMercatorBoundingBox
	 * @param zoom
	 */
	private void adjustGeoPackageBounds(
			BoundingBox requestWebMercatorBoundingBox, int zoom) {
		// Get the fitting tile grid and determine the bounding box that
		// fits it
		TileGrid tileGrid = TileBoundingBoxUtils
				.getTileGrid(requestWebMercatorBoundingBox, zoom);
		tileGridBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(tileGrid, zoom);
		matrixWidth = tileGrid.getMaxX() + 1 - tileGrid.getMinX();
		matrixHeight = tileGrid.getMaxY() + 1 - tileGrid.getMinY();
	}

	/**
	 * Update the Content and Tile Matrix Set bounds
	 *
	 * @param tileMatrixSet
	 * @throws java.sql.SQLException
	 */
	private void updateTileBounds(TileMatrixSet tileMatrixSet)
			throws SQLException {

		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

		if (tileDao.isXYZTiles()) {
			if (!xyzTiles) {
				// If adding GeoPackage tiles to a XYZ Tile format, add them
				// as XYZ tiles
				xyzTiles = true;
				adjustXYZBounds();
			}
		} else if (xyzTiles) {
			// Can't add XYZ formatted tiles to GeoPackage tiles
			throw new GeoPackageException("Can not add XYZ formatted tiles to "
					+ tableName
					+ " which already contains GeoPackage formatted tiles");
		}

		Projection tileMatrixProjection = tileMatrixSet.getSrs()
				.getProjection();
		if (!tileMatrixProjection.equals(projection)) {
			throw new GeoPackageException("Can not update tiles projected at "
					+ tileMatrixProjection.getCode()
					+ " with tiles projected at " + projection.getCode());
		}

		Contents contents = tileMatrixSet.getContents();

		// Combine the existing content and request bounding boxes
		BoundingBox previousContentsBoundingBox = contents.getBoundingBox();
		if (previousContentsBoundingBox != null) {
			ProjectionTransform transformProjectionToContents = projection
					.getTransformation(contents.getProjection());
			BoundingBox contentsBoundingBox = boundingBox;
			if (!transformProjectionToContents.isSameProjection()) {
				contentsBoundingBox = contentsBoundingBox
						.transform(transformProjectionToContents);
			}
			contentsBoundingBox = contentsBoundingBox
					.union(previousContentsBoundingBox);

			// Update the contents if modified
			if (!contentsBoundingBox.equals(previousContentsBoundingBox)) {
				contents.setBoundingBox(contentsBoundingBox);
				ContentsDao contentsDao = geoPackage.getContentsDao();
				contentsDao.update(contents);
			}
		}

		// If updating GeoPackage format tiles, all existing metadata and tile
		// rows needs to be adjusted
		if (!xyzTiles) {

			BoundingBox previousTileMatrixSetBoundingBox = tileMatrixSet
					.getBoundingBox();

			// Adjust the bounds to include the request and existing bounds
			ProjectionTransform transformProjectionToTileMatrixSet = projection
					.getTransformation(tileMatrixProjection);
			boolean sameProjection = transformProjectionToTileMatrixSet
					.isSameProjection();
			BoundingBox updateBoundingBox = tileBounds.get(minZoom);
			if (!sameProjection) {
				updateBoundingBox = updateBoundingBox
						.transform(transformProjectionToTileMatrixSet);
			}
			int minNewOrUpdateZoom = Math.min(minZoom,
					(int) tileDao.getMinZoom());
			adjustBounds(updateBoundingBox, minNewOrUpdateZoom);

			// Update the tile matrix set if modified
			BoundingBox updateTileGridBoundingBox = tileGridBoundingBox;
			if (!sameProjection) {
				updateTileGridBoundingBox = updateTileGridBoundingBox
						.transform(transformProjectionToTileMatrixSet);
			}
			if (!previousTileMatrixSetBoundingBox
					.equals(updateTileGridBoundingBox)) {
				updateTileGridBoundingBox = updateTileGridBoundingBox
						.union(previousTileMatrixSetBoundingBox);
				adjustBounds(updateTileGridBoundingBox, minNewOrUpdateZoom);
				updateTileGridBoundingBox = tileGridBoundingBox;
				if (!sameProjection) {
					updateTileGridBoundingBox = updateTileGridBoundingBox
							.transform(transformProjectionToTileMatrixSet);
				}
				tileMatrixSet.setBoundingBox(updateTileGridBoundingBox);
				TileMatrixSetDao tileMatrixSetDao = geoPackage
						.getTileMatrixSetDao();
				tileMatrixSetDao.update(tileMatrixSet);
			}

			TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();

			// Adjust the tile matrix metadata and tile rows at each existing
			// zoom level
			for (long zoom = tileDao.getMinZoom(); zoom <= tileDao
					.getMaxZoom(); zoom++) {
				TileMatrix tileMatrix = tileDao.getTileMatrix(zoom);
				if (tileMatrix != null) {

					// Determine the new width and height at this level
					long adjustment = (long) Math.pow(2,
							zoom - minNewOrUpdateZoom);
					long zoomMatrixWidth = matrixWidth * adjustment;
					long zoomMatrixHeight = matrixHeight * adjustment;

					// Get the zoom level tile rows, starting with highest rows
					// and columns so when updating we avoid constraint
					// violations
					TileResultSet tileResultSet = tileDao
							.queryForTileDescending(zoom);
					try {
						// Update each tile row at this zoom level
						while (tileResultSet.moveToNext()) {
							TileRow tileRow = tileResultSet.getRow();

							// Get the bounding box of the existing tile
							BoundingBox tileBoundingBox = TileBoundingBoxUtils
									.getBoundingBox(
											previousTileMatrixSetBoundingBox,
											tileMatrix, tileRow.getTileColumn(),
											tileRow.getTileRow());

							// Get the mid lat and lon to find the new tile row
							// and column
							double midLatitude = tileBoundingBox
									.getMinLatitude()
									+ ((tileBoundingBox.getMaxLatitude()
											- tileBoundingBox.getMinLatitude())
											/ 2.0);
							double midLongitude = tileBoundingBox
									.getMinLongitude()
									+ ((tileBoundingBox.getMaxLongitude()
											- tileBoundingBox.getMinLongitude())
											/ 2.0);

							// Get the new tile row and column with regards to
							// the new bounding box
							long newTileRow = TileBoundingBoxUtils.getTileRow(
									tileGridBoundingBox, zoomMatrixHeight,
									midLatitude);
							long newTileColumn = TileBoundingBoxUtils
									.getTileColumn(tileGridBoundingBox,
											zoomMatrixWidth, midLongitude);

							// Update the tile row
							if (tileRow.getTileRow() != newTileRow || tileRow
									.getTileColumn() != newTileColumn) {
								tileRow.setTileRow(newTileRow);
								tileRow.setTileColumn(newTileColumn);
								tileDao.update(tileRow);
							}
						}
					} finally {
						tileResultSet.close();
					}

					// Calculate the pixel size
					double pixelXSize = (tileGridBoundingBox.getMaxLongitude()
							- tileGridBoundingBox.getMinLongitude())
							/ zoomMatrixWidth / tileMatrix.getTileWidth();
					double pixelYSize = (tileGridBoundingBox.getMaxLatitude()
							- tileGridBoundingBox.getMinLatitude())
							/ zoomMatrixHeight / tileMatrix.getTileHeight();

					// Update the tile matrix
					tileMatrix.setMatrixWidth(zoomMatrixWidth);
					tileMatrix.setMatrixHeight(zoomMatrixHeight);
					tileMatrix.setPixelXSize(pixelXSize);
					tileMatrix.setPixelYSize(pixelYSize);

					tileMatrixDao.update(tileMatrix);
				}
			}

			// Adjust the width and height to the min zoom level of the
			// request
			if (minNewOrUpdateZoom < minZoom) {
				long adjustment = (long) Math.pow(2,
						minZoom - minNewOrUpdateZoom);
				matrixWidth *= adjustment;
				matrixHeight *= adjustment;
			}

		}
	}

	/**
	 * Close the GeoPackage
	 */
	public void close() {
		if (geoPackage != null) {
			geoPackage.close();
		}
	}

	/**
	 * Generate the tiles for the zoom level
	 *
	 * @param tileMatrixDao
	 * @param tileDao
	 * @param contents
	 * @param zoomLevel
	 * @param tileGrid
	 * @param localTileGrid
	 * @param matrixWidth
	 * @param matrixHeight
	 * @param update
	 * @return tile count
	 * @throws java.sql.SQLException
	 * @throws java.io.IOException
	 */
	private int generateTiles(TileMatrixDao tileMatrixDao, TileDao tileDao,
			Contents contents, int zoomLevel, TileGrid tileGrid,
			TileGrid localTileGrid, long matrixWidth, long matrixHeight,
			boolean update) throws SQLException, IOException {

		int count = 0;

		Integer tileWidth = null;
		Integer tileHeight = null;

		Map<Long, Set<Long>> existingTiles = null;
		if (update && skipExisting) {
			existingTiles = new HashMap<>();
			TileResultSet tileResultSet = tileDao.queryForTile(zoomLevel);
			try {
				while (tileResultSet.moveToNext()) {
					long column = ((Number) tileResultSet
							.getValue(TileTable.COLUMN_TILE_COLUMN))
									.longValue();
					long row = ((Number) tileResultSet
							.getValue(TileTable.COLUMN_TILE_ROW)).longValue();
					Set<Long> columnRows = existingTiles.get(column);
					if (columnRows == null) {
						columnRows = new HashSet<>();
						existingTiles.put(column, columnRows);
					}
					columnRows.add(row);
				}
			} finally {
				tileResultSet.close();
			}
			if (existingTiles.isEmpty()) {
				existingTiles = null;
			}
		}

		// Download and create the tile and each coordinate
		for (long x = tileGrid.getMinX(); x <= tileGrid.getMaxX(); x++) {

			// Check if the progress has been cancelled
			if (progress != null && !progress.isActive()) {
				break;
			}

			long tileColumn = x;
			// Update the column to the local tile grid location
			if (localTileGrid != null) {
				tileColumn = (x - tileGrid.getMinX()) + localTileGrid.getMinX();
			}

			Set<Long> existingColumnRows = null;
			if (existingTiles != null) {
				existingColumnRows = existingTiles.get(tileColumn);
			}

			for (long y = tileGrid.getMinY(); y <= tileGrid.getMaxY(); y++) {

				// Check if the progress has been cancelled
				if (progress != null && !progress.isActive()) {
					break;
				}

				long tileRow = y;
				// Update the row to the local tile grid location
				if (localTileGrid != null) {
					tileRow = (y - tileGrid.getMinY())
							+ localTileGrid.getMinY();
				}

				boolean createTile = true;
				if (existingColumnRows != null) {
					createTile = !existingColumnRows.contains(tileRow);
				}

				if (createTile) {
					try {

						// Create the tile
						byte[] tileBytes = createTile(zoomLevel, x, y);

						if (tileBytes != null) {

							BufferedImage image = null;

							// Compress the image
							if (compressFormat != null) {
								image = ImageUtils.getImage(tileBytes);
								if (image != null) {
									tileBytes = ImageUtils.writeImageToBytes(
											image, compressFormat,
											compressQuality);
								}
							}

							// Create a new tile row
							TileRow newRow = tileDao.newRow();
							newRow.setZoomLevel(zoomLevel);

							// If an update, delete an existing row
							if (update) {
								tileDao.deleteTile(tileColumn, tileRow,
										zoomLevel);
							}

							newRow.setTileColumn(tileColumn);
							newRow.setTileRow(tileRow);
							newRow.setTileData(tileBytes);
							tileDao.create(newRow);

							count++;

							// Determine the tile width and height
							if (tileWidth == null) {
								if (image == null) {
									image = ImageUtils.getImage(tileBytes);
								}
								if (image != null) {
									tileWidth = image.getWidth();
									tileHeight = image.getHeight();
								}
							}
						}
					} catch (Exception e) {
						LOGGER.log(
								Level.WARNING, "Failed to create tile. Zoom: "
										+ zoomLevel + ", x: " + x + ", y: " + y,
								e);
						// Skip this tile, don't increase count
					}
				}

				// Update the progress count, even on failures
				if (progress != null) {
					progress.addZoomLevelProgress(zoomLevel, 1);
					progress.addProgress(1);
				}

			}

		}

		// If none of the tiles were translated into a bitmap with dimensions,
		// delete them
		if ((tileWidth == null || tileHeight == null)
				&& existingTiles == null) {
			count = 0;

			StringBuilder where = new StringBuilder();

			where.append(
					tileDao.buildWhere(TileTable.COLUMN_ZOOM_LEVEL, zoomLevel));

			where.append(" AND ");
			where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_COLUMN,
					tileGrid.getMinX(), ">="));

			where.append(" AND ");
			where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_COLUMN,
					tileGrid.getMaxX(), "<="));

			where.append(" AND ");
			where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_ROW,
					tileGrid.getMinY(), ">="));

			where.append(" AND ");
			where.append(tileDao.buildWhere(TileTable.COLUMN_TILE_ROW,
					tileGrid.getMaxY(), "<="));

			String[] whereArgs = tileDao.buildWhereArgs(new Object[] {
					zoomLevel, tileGrid.getMinX(), tileGrid.getMaxX(),
					tileGrid.getMinY(), tileGrid.getMaxY() });

			tileDao.delete(where.toString(), whereArgs);

		} else {

			// Check if the tile matrix already exists
			boolean create = true;
			if (update) {
				create = !tileMatrixDao
						.idExists(new TileMatrixKey(tableName, zoomLevel));
			}

			// Create the tile matrix
			if (create) {

				// Calculate meters per pixel
				double pixelXSize = (tileGridBoundingBox.getMaxLongitude()
						- tileGridBoundingBox.getMinLongitude()) / matrixWidth
						/ tileWidth;
				double pixelYSize = (tileGridBoundingBox.getMaxLatitude()
						- tileGridBoundingBox.getMinLatitude()) / matrixHeight
						/ tileHeight;

				// Create the tile matrix for this zoom level
				TileMatrix tileMatrix = new TileMatrix();
				tileMatrix.setContents(contents);
				tileMatrix.setZoomLevel(zoomLevel);
				tileMatrix.setMatrixWidth(matrixWidth);
				tileMatrix.setMatrixHeight(matrixHeight);
				tileMatrix.setTileWidth(tileWidth);
				tileMatrix.setTileHeight(tileHeight);
				tileMatrix.setPixelXSize(pixelXSize);
				tileMatrix.setPixelYSize(pixelYSize);
				tileMatrixDao.create(tileMatrix);
			}
		}

		return count;
	}

	/**
	 * Called after set up and right before tile generation starts for the first
	 * zoom level
	 */
	protected abstract void preTileGeneration();

	/**
	 * Create the tile
	 *
	 * @param z
	 *            zoom level
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return tile bytes
	 */
	protected abstract byte[] createTile(int z, long x, long y);

}
