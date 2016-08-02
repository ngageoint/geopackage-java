package mil.nga.geopackage.extension.elevation;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.ImageRectangle;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxJavaUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Tiled Gridded Elevation Data Extension
 * 
 * @author osbornb
 * @since 1.2.1
 */
public class ElevationTiles extends ElevationTilesCore {

	/**
	 * Tile DAO
	 */
	private final TileDao tileDao;

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param tileDao
	 *            tile dao
	 * @param width
	 *            elevation response width
	 * @param height
	 *            elevation response height
	 * @param requestProjection
	 *            request projection
	 */
	public ElevationTiles(GeoPackage geoPackage, TileDao tileDao,
			Integer width, Integer height, Projection requestProjection) {
		super(geoPackage, tileDao.getTileMatrixSet(), width, height,
				requestProjection);
		this.tileDao = tileDao;
	}

	/**
	 * Constructor, use the elevation tables pixel tile size as the request size
	 * width and height
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param tileDao
	 *            tile dao
	 */
	public ElevationTiles(GeoPackage geoPackage, TileDao tileDao) {
		this(geoPackage, tileDao, null, null, tileDao.getProjection());
	}

	/**
	 * Constructor, use the elevation tables pixel tile size as the request size
	 * width and height, request as the specified projection
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param tileDao
	 *            tile dao
	 * @param requestProjection
	 *            request projection
	 */
	public ElevationTiles(GeoPackage geoPackage, TileDao tileDao,
			Projection requestProjection) {
		this(geoPackage, tileDao, null, null, requestProjection);
	}

	/**
	 * Get the tile dao
	 * 
	 * @return tile dao
	 */
	public TileDao getTileDao() {
		return tileDao;
	}

	/**
	 * Get the elevation at the coordinate
	 * 
	 * @param latitude
	 *            latitude
	 * @param longitude
	 *            longitude
	 * @return elevation value
	 */
	public Double getElevation(double latitude, double longitude) {
		ElevationRequest request = new ElevationRequest(latitude, longitude);
		ElevationTileResults elevations = getElevations(request, 1, 1);
		Double elevation = null;
		if (elevations != null) {
			elevation = elevations.getElevations()[0][0];
		}
		return elevation;
	}

	/**
	 * Get the elevation values within the bounding box
	 * 
	 * @param requestBoundingBox
	 *            request bounding box
	 * @return elevation results
	 */
	public ElevationTileResults getElevations(BoundingBox requestBoundingBox) {
		ElevationRequest request = new ElevationRequest(requestBoundingBox);
		ElevationTileResults elevations = getElevations(request);
		return elevations;
	}

	/**
	 * Get the elevation values within the bounding box with the requested width
	 * and height result size
	 * 
	 * @param requestBoundingBox
	 *            request bounding box
	 * @param width
	 *            elevation request width
	 * @param height
	 *            elevation request height
	 * @return elevation results
	 */
	public ElevationTileResults getElevations(BoundingBox requestBoundingBox,
			Integer width, Integer height) {
		ElevationRequest request = new ElevationRequest(requestBoundingBox);
		ElevationTileResults elevations = getElevations(request, width, height);
		return elevations;
	}

	/**
	 * Get the requested elevation values
	 * 
	 * @param request
	 *            elevation request
	 * @return elevation results
	 */
	public ElevationTileResults getElevations(ElevationRequest request) {
		ElevationTileResults elevations = getElevations(request, width, height);
		return elevations;
	}

	/**
	 * Get the requested elevation values with the requested width and height
	 * 
	 * @param request
	 *            elevation request
	 * @param width
	 *            elevation request width
	 * @param height
	 *            elevation request height
	 * @return elevation results
	 */
	public ElevationTileResults getElevations(ElevationRequest request,
			Integer width, Integer height) {

		ElevationTileResults elevationResults = null;

		// Transform to the projection of the elevation tiles
		ProjectionTransform transformRequestToElevation = requestProjection
				.getTransformation(elevationProjection);
		BoundingBox requestProjectedBoundingBox = transformRequestToElevation
				.transform(request.getBoundingBox());
		request.setProjectedBoundingBox(requestProjectedBoundingBox);

		// Find the tile matrix and results
		ElevationTileMatrixResults results = getResults(request,
				requestProjectedBoundingBox);

		if (results != null) {

			TileMatrix tileMatrix = results.getTileMatrix();
			TileResultSet tileResults = results.getTileResults();

			try {

				// Determine the requested elevation dimensions, or use the
				// dimensions of a single tile matrix elevation tile
				int requestedElevationsWidth = width != null ? width
						: (int) tileMatrix.getTileWidth();
				int requestedElevationsHeight = height != null ? height
						: (int) tileMatrix.getTileHeight();

				// Determine the size of the non projected elevation results
				int tileWidth = requestedElevationsWidth;
				int tileHeight = requestedElevationsHeight;
				if (!sameProjection) {
					int projectedWidth = (int) Math
							.round((requestProjectedBoundingBox
									.getMaxLongitude() - requestProjectedBoundingBox
									.getMinLongitude())
									/ tileMatrix.getPixelXSize());
					if (projectedWidth > 0) {
						tileWidth = projectedWidth;
					}
					int projectedHeight = (int) Math
							.round((requestProjectedBoundingBox
									.getMaxLatitude() - requestProjectedBoundingBox
									.getMinLatitude())
									/ tileMatrix.getPixelYSize());
					if (projectedHeight > 0) {
						tileHeight = projectedHeight;
					}
				}

				// Retrieve the elevations from the results
				Double[][] elevations = getElevations(tileMatrix, tileResults,
						request, tileWidth, tileHeight);

				// Project the elevations if needed
				if (elevations != null && !sameProjection && !request.isPoint()) {
					elevations = reprojectElevations(elevations,
							requestedElevationsWidth,
							requestedElevationsHeight,
							request.getBoundingBox(),
							transformRequestToElevation,
							requestProjectedBoundingBox);
				}

				// Create the results
				if (elevations != null) {
					elevationResults = new ElevationTileResults(elevations,
							tileMatrix);
				}
			} finally {
				tileResults.close();
			}
		}

		return elevationResults;
	}

	/**
	 * Get the unbounded elevation values within the bounding box. Unbounded
	 * results retrieves and returns each elevation pixel. The results size
	 * equals the width and height of all matching pixels.
	 * 
	 * @param requestBoundingBox
	 *            request bounding box
	 * @return elevation results
	 */
	public ElevationTileResults getElevationsUnbounded(
			BoundingBox requestBoundingBox) {
		ElevationRequest request = new ElevationRequest(requestBoundingBox);
		return getElevationsUnbounded(request);
	}

	/**
	 * Get the requested unbounded elevation values. Unbounded results retrieves
	 * and returns each elevation pixel. The results size equals the width and
	 * height of all matching pixels.
	 * 
	 * @param request
	 *            elevation request
	 * @return elevation results
	 */
	public ElevationTileResults getElevationsUnbounded(ElevationRequest request) {

		ElevationTileResults elevationResults = null;

		// Transform to the projection of the elevation tiles
		ProjectionTransform transformRequestToElevation = requestProjection
				.getTransformation(elevationProjection);
		BoundingBox requestProjectedBoundingBox = transformRequestToElevation
				.transform(request.getBoundingBox());
		request.setProjectedBoundingBox(requestProjectedBoundingBox);

		// Find the tile matrix and results
		ElevationTileMatrixResults results = getResults(request,
				requestProjectedBoundingBox);

		if (results != null) {

			TileMatrix tileMatrix = results.getTileMatrix();
			TileResultSet tileResults = results.getTileResults();

			try {

				// Retrieve the elevations from the results
				Double[][] elevations = getElevationsUnbounded(tileMatrix,
						tileResults, request);

				// Project the elevations if needed
				if (elevations != null && !sameProjection && !request.isPoint()) {
					elevations = reprojectElevations(elevations,
							elevations[0].length, elevations.length,
							request.getBoundingBox(),
							transformRequestToElevation,
							requestProjectedBoundingBox);
				}

				// Create the results
				if (elevations != null) {
					elevationResults = new ElevationTileResults(elevations,
							tileMatrix);
				}

			} finally {
				tileResults.close();
			}
		}

		return elevationResults;
	}

	/**
	 * Get the elevation tile results by finding the tile matrix with values
	 * 
	 * @param request
	 *            elevation request
	 * @param requestProjectedBoundingBox
	 *            request projected bounding box
	 * @return tile matrix results
	 */
	private ElevationTileMatrixResults getResults(ElevationRequest request,
			BoundingBox requestProjectedBoundingBox) {
		// Try to get the elevation from the current zoom level
		TileMatrix tileMatrix = getTileMatrix(request);
		ElevationTileMatrixResults results = null;
		if (tileMatrix != null) {
			results = getResults(requestProjectedBoundingBox, tileMatrix);

			// Try to zoom in or out to find a matching elevation
			if (results == null) {
				results = getResultsZoom(requestProjectedBoundingBox,
						tileMatrix);
			}
		}
		return results;
	}

	/**
	 * Get the elevation tile results for a specified tile matrix
	 * 
	 * @param requestProjectedBoundingBox
	 *            request projected bounding box
	 * @param tileMatrix
	 *            tile matrix
	 * @return tile matrix results
	 */
	private ElevationTileMatrixResults getResults(
			BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix) {
		ElevationTileMatrixResults results = null;
		TileResultSet tileResults = retrieveTileResults(
				requestProjectedBoundingBox, tileMatrix);
		if (tileResults != null) {
			if (tileResults.getCount() > 0) {
				results = new ElevationTileMatrixResults(tileMatrix,
						tileResults);
			} else {
				tileResults.close();
			}
		}
		return results;
	}

	/**
	 * Get the elevation tile results by zooming in or out as needed from the
	 * provided tile matrix to find values
	 * 
	 * @param requestProjectedBoundingBox
	 *            request projected bounding box
	 * @param tileMatrix
	 *            tile matrix
	 * @return tile matrix results
	 */
	private ElevationTileMatrixResults getResultsZoom(
			BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix) {

		ElevationTileMatrixResults results = null;

		if (zoomIn && zoomInBeforeOut) {
			results = getResultsZoomIn(requestProjectedBoundingBox, tileMatrix);
		}
		if (results == null && zoomOut) {
			results = getResultsZoomOut(requestProjectedBoundingBox, tileMatrix);
		}
		if (results == null && zoomIn && !zoomInBeforeOut) {
			results = getResultsZoomIn(requestProjectedBoundingBox, tileMatrix);
		}

		return results;
	}

	/**
	 * Get the elevation tile results by zooming in from the provided tile
	 * matrix
	 * 
	 * @param requestProjectedBoundingBox
	 *            request projected bounding box
	 * @param tileMatrix
	 *            tile matrix
	 * @return tile matrix results
	 */
	private ElevationTileMatrixResults getResultsZoomIn(
			BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix) {

		ElevationTileMatrixResults results = null;
		for (long zoomLevel = tileMatrix.getZoomLevel() + 1; zoomLevel <= tileDao
				.getMaxZoom(); zoomLevel++) {
			TileMatrix zoomTileMatrix = tileDao.getTileMatrix(zoomLevel);
			if (zoomTileMatrix != null) {
				results = getResults(requestProjectedBoundingBox,
						zoomTileMatrix);
				if (results != null) {
					break;
				}
			}
		}
		return results;
	}

	/**
	 * Get the elevation tile results by zooming out from the provided tile
	 * matrix
	 * 
	 * @param requestProjectedBoundingBox
	 *            request projected bounding box
	 * @param tileMatrix
	 *            tile matrix
	 * @return tile matrix results
	 */
	private ElevationTileMatrixResults getResultsZoomOut(
			BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix) {

		ElevationTileMatrixResults results = null;
		for (long zoomLevel = tileMatrix.getZoomLevel() - 1; zoomLevel >= tileDao
				.getMinZoom(); zoomLevel--) {
			TileMatrix zoomTileMatrix = tileDao.getTileMatrix(zoomLevel);
			if (zoomTileMatrix != null) {
				results = getResults(requestProjectedBoundingBox,
						zoomTileMatrix);
				if (results != null) {
					break;
				}
			}
		}
		return results;
	}

	/**
	 * Get the elevation values from the tile results scaled to the provided
	 * dimensions
	 * 
	 * @param tileMatrix
	 *            tile matrix
	 * @param tileResults
	 *            tile results
	 * @param request
	 *            elevation request
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation values
	 */
	private Double[][] getElevations(TileMatrix tileMatrix,
			TileResultSet tileResults, ElevationRequest request, int tileWidth,
			int tileHeight) {

		Double[][] elevations = null;

		// Process each elevation tile
		while (tileResults.moveToNext()) {

			// Get the next elevation tile
			TileRow tileRow = tileResults.getRow();

			// Get the bounding box of the elevation
			BoundingBox tileBoundingBox = TileBoundingBoxUtils.getBoundingBox(
					elevationBoundingBox, tileMatrix, tileRow.getTileColumn(),
					tileRow.getTileRow());

			// Get the bounding box where the request and elevation tile overlap
			BoundingBox overlap = request.overlap(tileBoundingBox);

			// If the tile overlaps with the requested box
			if (overlap != null) {

				// Get the rectangle of the tile elevation with matching values
				ImageRectangle src = TileBoundingBoxJavaUtils.getRectangle(
						tileMatrix.getTileWidth(), tileMatrix.getTileHeight(),
						tileBoundingBox, overlap);

				// Get the rectangle of where to store the results
				ImageRectangle dest = TileBoundingBoxJavaUtils.getRectangle(
						tileWidth, tileHeight,
						request.getProjectedBoundingBox(), overlap);

				if (src.isValidAllowEmpty() && dest.isValidAllowEmpty()) {

					// Create the elevations array first time through
					if (elevations == null) {
						elevations = new Double[tileHeight][tileWidth];
					}

					// Get the destination dimensions
					int destTop = Math.min(dest.getTop(), tileHeight - 1);
					int destBottom = Math.min(dest.getBottom(), tileHeight - 1);
					int destLeft = Math.min(dest.getLeft(), tileWidth - 1);
					int destRight = Math.min(dest.getRight(), tileWidth - 1);

					int destWidth = destRight - destLeft + 1;
					int destHeight = destBottom - destTop + 1;

					// Get the source dimensions
					int srcTop = Math.min(src.getTop(),
							(int) tileMatrix.getTileHeight() - 1);
					int srcBottom = Math.min(src.getBottom(),
							(int) tileMatrix.getTileHeight() - 1);
					int srcLeft = Math.min(src.getLeft(),
							(int) tileMatrix.getTileWidth() - 1);
					int srcRight = Math.min(src.getRight(),
							(int) tileMatrix.getTileWidth() - 1);

					int srcWidth = srcRight - srcLeft + 1;
					int srcHeight = srcBottom - srcTop + 1;

					// Determine the source to destination ratio
					float widthRatio = srcWidth / destWidth;
					float heightRatio = srcHeight / destHeight;

					// Get the gridded tile value for the tile
					GriddedTile griddedTile = getGriddedTile(tileRow.getId());

					// Get the elevation tile image
					BufferedImage image = null;
					try {
						image = tileRow.getTileDataImage();
					} catch (IOException e) {
						throw new GeoPackageException(
								"Failed to get the Tile Row Data Image", e);
					}
					WritableRaster raster = image.getRaster();

					// Read and set the elevation values
					for (int y = destTop; y <= destBottom; y++) {
						for (int x = destLeft; x <= destRight; x++) {

							// Determine which source pixel to use
							float middleOfXDestPixel = (x - destLeft) + 0.5f;
							float xSourcePixel = middleOfXDestPixel
									* widthRatio;
							int xSource = srcLeft
									+ (int) Math.floor(xSourcePixel);

							float middleOfYDestPixel = (y - destTop) + 0.5f;
							float ySourcePixel = middleOfYDestPixel
									* heightRatio;
							int ySource = srcTop
									+ (int) Math.floor(ySourcePixel);

							// Get the elevation value from the source pixel
							Double elevation = getElevationValue(griddedTile,
									raster, xSource, ySource);

							elevations[y][x] = elevation;
						}
					}
				}
			}
		}

		return elevations;
	}

	/**
	 * Get the elevation values from the tile results unbounded in result size
	 * 
	 * @param tileMatrix
	 *            tile matrix
	 * @param tileResults
	 *            tile results
	 * @param request
	 *            elevation request
	 * @return elevation values
	 */
	private Double[][] getElevationsUnbounded(TileMatrix tileMatrix,
			TileResultSet tileResults, ElevationRequest request) {

		// Build a map of rows to maps of columns and values
		Map<Long, Map<Long, Double[][]>> rowsMap = new TreeMap<>();

		// Track the min and max row and column
		Long minRow = null;
		Long maxRow = null;
		Long minColumn = null;
		Long maxColumn = null;

		// Track count of tiles involved in the results
		int tileCount = 0;

		// Process each elevation tile row
		while (tileResults.moveToNext()) {

			// Get the next elevation tile
			TileRow tileRow = tileResults.getRow();

			// Get the bounding box of the elevation
			BoundingBox tileBoundingBox = TileBoundingBoxUtils.getBoundingBox(
					elevationBoundingBox, tileMatrix, tileRow.getTileColumn(),
					tileRow.getTileRow());

			// Get the bounding box where the request and elevation tile overlap
			BoundingBox overlap = request.overlap(tileBoundingBox);

			// If the elevation tile overlaps with the requested box
			if (overlap != null) {

				// Get the rectangle of the tile elevation with matching values
				ImageRectangle src = TileBoundingBoxJavaUtils.getRectangle(
						tileMatrix.getTileWidth(), tileMatrix.getTileHeight(),
						tileBoundingBox, overlap);

				if (src.isValidAllowEmpty()) {

					// Get the source dimensions
					int srcTop = Math.min(src.getTop(),
							(int) tileMatrix.getTileHeight() - 1);
					int srcBottom = Math.min(src.getBottom(),
							(int) tileMatrix.getTileHeight() - 1);
					int srcLeft = Math.min(src.getLeft(),
							(int) tileMatrix.getTileWidth() - 1);
					int srcRight = Math.min(src.getRight(),
							(int) tileMatrix.getTileWidth() - 1);

					// Get the gridded tile value for the tile
					GriddedTile griddedTile = getGriddedTile(tileRow.getId());

					// Get the elevation tile image
					BufferedImage image = null;
					try {
						image = tileRow.getTileDataImage();
					} catch (IOException e) {
						throw new GeoPackageException(
								"Failed to get the Tile Row Data Image", e);
					}
					WritableRaster raster = image.getRaster();

					// Create the elevation results for this tile
					Double[][] elevations = new Double[srcBottom - srcTop + 1][srcRight
							- srcLeft + 1];

					// Get or add the columns map to the rows map
					Map<Long, Double[][]> columnsMap = rowsMap.get(tileRow
							.getTileRow());
					if (columnsMap == null) {
						columnsMap = new TreeMap<Long, Double[][]>();
						rowsMap.put(tileRow.getTileRow(), columnsMap);
					}

					// Read and set the elevation values
					for (int y = srcTop; y <= srcBottom; y++) {

						for (int x = srcLeft; x <= srcRight; x++) {

							// Get the elevation value from the source pixel
							Double elevation = getElevationValue(griddedTile,
									raster, x, y);

							elevations[y - srcTop][x - srcLeft] = elevation;
						}
					}

					// Set the elevations in the results map
					columnsMap.put(tileRow.getTileColumn(), elevations);

					// Increase the contributing tiles count
					tileCount++;

					// Track the min and max row and column
					minRow = minRow == null ? tileRow.getTileRow() : Math.min(
							minRow, tileRow.getTileRow());
					maxRow = maxRow == null ? tileRow.getTileRow() : Math.max(
							maxRow, tileRow.getTileRow());
					minColumn = minColumn == null ? tileRow.getTileColumn()
							: Math.min(minColumn, tileRow.getTileColumn());
					maxColumn = maxColumn == null ? tileRow.getTileColumn()
							: Math.max(maxColumn, tileRow.getTileColumn());
				}
			}
		}

		// Handle formatting the results
		Double[][] elevations = formatUnboundedResults(tileMatrix, rowsMap,
				tileCount, minRow, maxRow, minColumn, maxColumn);

		return elevations;
	}

	/**
	 * Get the tile matrix for the zoom level as defined by the area of the
	 * request
	 *
	 * @param request
	 *            elevation request
	 * @return tile matrix or null
	 */
	private TileMatrix getTileMatrix(ElevationRequest request) {

		TileMatrix tileMatrix = null;

		// Check if the request overlaps elevation bounding box
		if (request.overlap(elevationBoundingBox) != null) {

			// Get the tile distance
			BoundingBox projectedBoundingBox = request
					.getProjectedBoundingBox();
			double distance = projectedBoundingBox.getMaxLongitude()
					- projectedBoundingBox.getMinLongitude();

			// Get the zoom level to request based upon the tile size
			Long zoomLevel = tileDao.getClosestZoomLevel(distance);

			// If there is a matching zoom level
			if (zoomLevel != null) {
				tileMatrix = tileDao.getTileMatrix(zoomLevel);
			}
		}

		return tileMatrix;
	}

	/**
	 * Get the tile row results of elevation tiles needed to create the
	 * requested bounding box elevations
	 *
	 * @param projectedRequestBoundingBox
	 *            bounding box projected to the elevations
	 * @param tileMatrix
	 *            tile matrix
	 * @return tile results or null
	 */
	private TileResultSet retrieveTileResults(
			BoundingBox projectedRequestBoundingBox, TileMatrix tileMatrix) {

		TileResultSet tileResults = null;

		if (tileMatrix != null) {

			// Get the tile grid
			TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
					elevationBoundingBox, tileMatrix.getMatrixWidth(),
					tileMatrix.getMatrixHeight(), projectedRequestBoundingBox);

			// Query for matching tiles in the tile grid
			tileResults = tileDao.queryByTileGrid(tileGrid,
					tileMatrix.getZoomLevel());

		}

		return tileResults;
	}

	/**
	 * Get the elevation value of the pixel in the tile row image
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param tileRow
	 *            tile row
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return elevation value
	 */
	public double getElevationValue(GriddedTile griddedTile, TileRow tileRow,
			int x, int y) {
		BufferedImage image = null;
		try {
			image = tileRow.getTileDataImage();
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed to get the Tile Row Data Image", e);
		}
		double elevation = getElevationValue(griddedTile, image, x, y);
		return elevation;
	}

	/**
	 * Get the elevation value of the pixel in the tile row image
	 * 
	 * @param tileRow
	 *            tile row
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return elevation value
	 */
	public double getElevationValue(TileRow tileRow, int x, int y) {
		GriddedTile griddedTile = getGriddedTile(tileRow.getId());
		double elevation = getElevationValue(griddedTile, tileRow, x, y);
		return elevation;
	}

	/**
	 * Get the pixel value as an "unsigned short"
	 * 
	 * @param image
	 *            tile image
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return "unsigned short" pixel value
	 */
	public short getPixelValue(BufferedImage image, int x, int y) {
		validateImageType(image);
		WritableRaster raster = image.getRaster();
		short pixelValue = getPixelValue(raster, x, y);
		return pixelValue;
	}

	/**
	 * Get the pixel value as a 16 bit unsigned integer value
	 * 
	 * @param image
	 *            tile image
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return unsigned integer pixel value
	 */
	public int getUnsignedPixelValue(BufferedImage image, int x, int y) {
		short pixelValue = getPixelValue(image, x, y);
		int unsignedPixelValue = getUnsignedPixelValue(pixelValue);
		return unsignedPixelValue;
	}

	/**
	 * Get the pixel value as an "unsigned short" from the raster and the
	 * coordinate
	 * 
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return "unsigned short" pixel value
	 */
	public short getPixelValue(WritableRaster raster, int x, int y) {
		Object pixelData = raster.getDataElements(x, y, null);
		short sdata[] = (short[]) pixelData;
		if (sdata.length != 1) {
			throw new UnsupportedOperationException(
					"This method is not supported by this color model");
		}
		short pixelValue = sdata[0];

		return pixelValue;
	}

	/**
	 * Get the pixel value as a 16 bit unsigned integer value
	 * 
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return unsigned integer pixel value
	 */
	public int getUnsignedPixelValue(WritableRaster raster, int x, int y) {
		short pixelValue = getPixelValue(raster, x, y);
		int unsignedPixelValue = getUnsignedPixelValue(pixelValue);
		return unsignedPixelValue;
	}

	/**
	 * Get the pixel values of the buffered image as "unsigned shorts"
	 * 
	 * @param image
	 *            tile image
	 * @return "unsigned short" pixel values
	 */
	public short[] getPixelValues(BufferedImage image) {
		validateImageType(image);
		WritableRaster raster = image.getRaster();
		short[] pixelValues = getPixelValues(raster);
		return pixelValues;
	}

	/**
	 * Get the pixel values of the buffered image as 16 bit unsigned integer
	 * values
	 * 
	 * @param image
	 *            tile image
	 * @return unsigned integer pixel values
	 */
	public int[] getUnsignedPixelValues(BufferedImage image) {
		short[] pixelValues = getPixelValues(image);
		int[] unsignedPixelValues = getUnsignedPixelValues(pixelValues);
		return unsignedPixelValues;
	}

	/**
	 * Get the pixel values of the raster as "unsigned shorts"
	 * 
	 * @param raster
	 *            image raster
	 * @return "unsigned short" pixel values
	 */
	public short[] getPixelValues(WritableRaster raster) {
		DataBufferUShort buffer = (DataBufferUShort) raster.getDataBuffer();
		short[] pixelValues = buffer.getData();
		return pixelValues;
	}

	/**
	 * Get the pixel values of the raster as 16 bit unsigned integer values
	 * 
	 * @param raster
	 *            image raster
	 * @return unsigned integer pixel values
	 */
	public int[] getUnsignedPixelValues(WritableRaster raster) {
		short[] pixelValues = getPixelValues(raster);
		int[] unsignedPixelValues = getUnsignedPixelValues(pixelValues);
		return unsignedPixelValues;
	}

	/**
	 * Validate that the image type is an unsigned short
	 * 
	 * @param image
	 *            tile image
	 */
	public void validateImageType(BufferedImage image) {
		if (image == null) {
			throw new GeoPackageException("The image is null");
		}
		if (image.getColorModel().getTransferType() != DataBuffer.TYPE_USHORT) {
			throw new GeoPackageException(
					"The elevation tile is expected to be a 16 bit unsigned short, actual: "
							+ image.getColorModel().getTransferType());
		}
	}

	/**
	 * Get the elevation value
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param image
	 *            tile image
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return elevation value
	 */
	public Double getElevationValue(GriddedTile griddedTile,
			BufferedImage image, int x, int y) {
		short pixelValue = getPixelValue(image, x, y);
		Double elevation = getElevationValue(griddedTile, pixelValue);
		return elevation;
	}

	/**
	 * Get the elevation value
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return elevation value
	 */
	public Double getElevationValue(GriddedTile griddedTile,
			WritableRaster raster, int x, int y) {
		short pixelValue = getPixelValue(raster, x, y);
		Double elevation = getElevationValue(griddedTile, pixelValue);
		return elevation;
	}

	/**
	 * Get the elevation values
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param image
	 *            tile image
	 * @return elevation values
	 */
	public Double[] getElevationValues(GriddedTile griddedTile,
			BufferedImage image) {
		short[] pixelValues = getPixelValues(image);
		Double[] elevations = getElevationValues(griddedTile, pixelValues);
		return elevations;
	}

	/**
	 * Get the elevation values
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param raster
	 *            raster image
	 * @return elevation values
	 */
	public Double[] getElevationValues(GriddedTile griddedTile,
			WritableRaster raster) {
		short[] pixelValues = getPixelValues(raster);
		Double[] elevations = getElevationValues(griddedTile, pixelValues);
		return elevations;
	}

	/**
	 * Draw an elevation image tile from the flat array of "unsigned short"
	 * pixel values of length tileWidth * tileHeight where each pixel is at: (y
	 * * tileWidth) + x
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(short[] pixelValues, int tileWidth,
			int tileHeight) {

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				short pixelValue = pixelValues[(y * tileWidth) + x];
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as PNG bytes from the flat array
	 * of "unsigned short" pixel values of length tileWidth * tileHeight where
	 * each pixel is at: (y * tileWidth) + x
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(short[] pixelValues, int tileWidth,
			int tileHeight) {
		BufferedImage image = drawTile(pixelValues, tileWidth, tileHeight);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw an elevation tile from the double array of "unsigned short" pixel
	 * values formatted as short[row][width]
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values as [row][width]
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(short[][] pixelValues) {

		int tileWidth = pixelValues[0].length;
		int tileHeight = pixelValues.length;

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				short pixelValue = pixelValues[y][x];
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation tile and format as PNG bytes from the double array of
	 * "unsigned short" pixel values formatted as short[row][width]
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values as [row][width]
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(short[][] pixelValues) {
		BufferedImage image = drawTile(pixelValues);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw an elevation image tile from the flat array of unsigned 16 bit
	 * integer pixel values of length tileWidth * tileHeight where each pixel is
	 * at: (y * tileWidth) + x
	 * 
	 * @param unsignedPixelValues
	 *            unsigned 16 bit integer pixel values of length tileWidth *
	 *            tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(int[] unsignedPixelValues, int tileWidth,
			int tileHeight) {

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				int unsignedPixelValue = unsignedPixelValues[(y * tileWidth)
						+ x];
				setPixelValue(raster, x, y, unsignedPixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as PNG bytes from the flat array
	 * of unsigned 16 bit integer pixel values of length tileWidth * tileHeight
	 * where each pixel is at: (y * tileWidth) + x
	 * 
	 * @param unsignedPixelValues
	 *            unsigned 16 bit integer pixel values of length tileWidth *
	 *            tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(int[] unsignedPixelValues, int tileWidth,
			int tileHeight) {
		BufferedImage image = drawTile(unsignedPixelValues, tileWidth,
				tileHeight);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw an elevation image tile from the flat array of unsigned 16 bit
	 * integer pixel values formatted as short[row][width]
	 * 
	 * @param unsignedPixelValues
	 *            unsigned 16 bit integer pixel values as [row][width]
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(int[][] unsignedPixelValues) {

		int tileWidth = unsignedPixelValues[0].length;
		int tileHeight = unsignedPixelValues.length;

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				int unsignedPixelValue = unsignedPixelValues[y][x];
				setPixelValue(raster, x, y, unsignedPixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as PNG bytes from the flat array
	 * of unsigned 16 bit integer pixel values formatted as short[row][width]
	 * 
	 * @param unsignedPixelValues
	 *            unsigned 16 bit integer pixel values as [row][width]
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(int[][] unsignedPixelValues) {
		BufferedImage image = drawTile(unsignedPixelValues);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw an elevation image tile from the flat array of elevations of length
	 * tileWidth * tileHeight where each elevation is at: (y * tileWidth) + x
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param elevations
	 *            elevations of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(GriddedTile griddedTile, Double[] elevations,
			int tileWidth, int tileHeight) {

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				Double elevation = elevations[(y * tileWidth) + x];
				short pixelValue = getPixelValue(griddedTile, elevation);
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as PNG bytes from the flat array
	 * of elevations of length tileWidth * tileHeight where each elevation is
	 * at: (y * tileWidth) + x
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param elevations
	 *            elevations of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(GriddedTile griddedTile, Double[] elevations,
			int tileWidth, int tileHeight) {
		BufferedImage image = drawTile(griddedTile, elevations, tileWidth,
				tileHeight);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw an elevation image tile from the flat array of unsigned elevations
	 * formatted as short[row][width]
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param elevations
	 *            elevations as [row][width]
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(GriddedTile griddedTile, Double[][] elevations) {

		int tileWidth = elevations[0].length;
		int tileHeight = elevations.length;

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				Double elevation = elevations[y][x];
				short pixelValue = getPixelValue(griddedTile, elevation);
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as PNG bytes from the flat array
	 * of unsigned elevations formatted as short[row][width]
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param elevations
	 *            elevations as [row][width]
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(GriddedTile griddedTile, Double[][] elevations) {
		BufferedImage image = drawTile(griddedTile, elevations);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Create a new unsigned 16 bit short grayscale image
	 * 
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return image
	 */
	public BufferedImage createImage(int tileWidth, int tileHeight) {
		return new BufferedImage(tileWidth, tileHeight,
				BufferedImage.TYPE_USHORT_GRAY);
	}

	/**
	 * Get the image as PNG bytes
	 * 
	 * @param image
	 *            buffered image
	 * @return image bytes
	 */
	public byte[] getImageBytes(BufferedImage image) {
		byte[] bytes = null;
		try {
			bytes = ImageUtils.writeImageToBytes(image,
					ImageUtils.IMAGE_FORMAT_PNG);
		} catch (IOException e) {
			throw new GeoPackageException("Failed to write image to "
					+ ImageUtils.IMAGE_FORMAT_PNG + " bytes", e);
		}
		return bytes;
	}

	/**
	 * Set the "unsigned short" pixel value into the image raster
	 * 
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param pixelValue
	 *            "unsigned short" pixel value
	 */
	public void setPixelValue(WritableRaster raster, int x, int y,
			short pixelValue) {
		short data[] = new short[] { pixelValue };
		raster.setDataElements(x, y, data);
	}

	/**
	 * Set the unsigned 16 bit integer pixel value into the image raster
	 * 
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param unsignedPixelValue
	 *            unsigned 16 bit integer pixel value
	 */
	public void setPixelValue(WritableRaster raster, int x, int y,
			int unsignedPixelValue) {
		short pixelValue = getPixelValue(unsignedPixelValue);
		setPixelValue(raster, x, y, pixelValue);
	}

}
