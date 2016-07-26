package mil.nga.geopackage.extension.elevation;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.ImageRectangle;
import mil.nga.geopackage.tiles.TileBoundingBoxJavaUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;

import org.osgeo.proj4j.ProjCoordinate;

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
	 * Elevation results width
	 */
	private Integer width;

	/**
	 * Elevation results height
	 */
	private Integer height;

	/**
	 * Projection of the requests
	 */
	private final Projection requestProjection;

	/**
	 * Projection of the elevations
	 */
	private final Projection elevationProjection;

	/**
	 * Elevations bounding box
	 */
	private final BoundingBox elevationBoundingBox;

	/**
	 * Flag indicating the elevation and request projections are the same
	 */
	private final boolean sameProjection;

	private boolean zoomIn = true;

	private boolean zoomOut = true;

	private boolean zoomInBeforeOut = true;

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
		super(geoPackage, tileDao.getTileMatrixSet());
		this.tileDao = tileDao;
		this.width = width;
		this.height = height;
		this.requestProjection = requestProjection;
		elevationProjection = ProjectionFactory.getProjection(tileDao
				.getTileMatrixSet().getSrs());
		elevationBoundingBox = tileDao.getTileMatrixSet().getBoundingBox();

		// Check if the projections have the same units
		sameProjection = (requestProjection.getUnit().name
				.equals(elevationProjection.getUnit().name));
	}

	/**
	 * Constructor, use the elevation tables actual elevation pixels as the
	 * request size width and height
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
	 * Constructor, use the elevation tables actual elevation pixels as the
	 * request size width and height, request as the specified projection
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
	 * Get the requested elevation width
	 * 
	 * @return width
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * Set the requested elevation width
	 * 
	 * @param width
	 *            requested elevation width
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}

	/**
	 * Get the requested elevation height
	 * 
	 * @return height
	 */
	public Integer getHeight() {
		return height;
	}

	/**
	 * Set the requested elevation height
	 * 
	 * @param height
	 *            requested elevation height
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}

	/**
	 * Get the request projection
	 * 
	 * @return request projection
	 */
	public Projection getRequestProjection() {
		return requestProjection;
	}

	/**
	 * Get the elevation projection
	 * 
	 * @return elevation projection
	 */
	public Projection getElevationProjection() {
		return elevationProjection;
	}

	/**
	 * Get the elevation bounding box
	 * 
	 * @return elevation bounding box
	 */
	public BoundingBox getElevationBoundingBox() {
		return elevationBoundingBox;
	}

	/**
	 * Is the request and elevation projection the same
	 * 
	 * @return true if the same
	 */
	public boolean isSameProjection() {
		return sameProjection;
	}

	public boolean isZoomIn() {
		return zoomIn;
	}

	public void setZoomIn(boolean zoomIn) {
		this.zoomIn = zoomIn;
	}

	public boolean isZoomOut() {
		return zoomOut;
	}

	public void setZoomOut(boolean zoomOut) {
		this.zoomOut = zoomOut;
	}

	public boolean isZoomInBeforeOut() {
		return zoomInBeforeOut;
	}

	public void setZoomInBeforeOut(boolean zoomInBeforeOut) {
		this.zoomInBeforeOut = zoomInBeforeOut;
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
		Double[][] elevations = getElevation(request, 1, 1);
		Double elevation = null;
		if (elevations != null) {
			elevation = elevations[0][0];
		}
		return elevation;
	}

	/**
	 * Get the elevation values within the bounding box
	 * 
	 * @param requestBoundingBox
	 *            request bounding box
	 * @return elevations
	 */
	public Double[][] getElevation(BoundingBox requestBoundingBox) {
		ElevationRequest request = new ElevationRequest(requestBoundingBox);
		return getElevation(request);
	}

	/**
	 * Get the elevation values within the bounding box with the requested width
	 * and height
	 * 
	 * @param requestBoundingBox
	 *            request bounding box
	 * @param width
	 *            elevation request width
	 * @param height
	 *            elevation request height
	 * @return elevations
	 */
	public Double[][] getElevation(BoundingBox requestBoundingBox,
			Integer width, Integer height) {
		ElevationRequest request = new ElevationRequest(requestBoundingBox);
		return getElevation(request, width, height);
	}

	/**
	 * Get the elevation values within the request
	 * 
	 * @param request
	 *            elevation request
	 * @return elevations
	 */
	public Double[][] getElevation(ElevationRequest request) {
		Double[][] elevations = getElevation(request, width, height);
		return elevations;
	}

	/**
	 * Get the elevation values within the request with the requested width and
	 * height
	 * 
	 * @param request
	 *            elevation request
	 * @param width
	 *            elevation request width
	 * @param height
	 *            elevation request height
	 * @return elevations
	 */
	public Double[][] getElevation(ElevationRequest request, Integer width,
			Integer height) {

		Double[][] elevations = null;

		// Transform to the projection of the tiles
		ProjectionTransform transformRequestToElevation = requestProjection
				.getTransformation(elevationProjection);
		BoundingBox requestProjectedBoundingBox = transformRequestToElevation
				.transform(request.getBoundingBox());
		request.setProjectedBoundingBox(requestProjectedBoundingBox);

		// Try to get the elevation from the current zoom level
		TileMatrix tileMatrix = getTileMatrix(request);
		TileMatrixResults results = null;
		if (tileMatrix != null) {
			results = getResults(requestProjectedBoundingBox, tileMatrix);

			// Try to zoom in or out to find a matching elevation
			if (results == null) {
				results = getResultsZoom(requestProjectedBoundingBox,
						tileMatrix);
			}
		}

		if (results != null) {

			tileMatrix = results.tileMatrix;
			TileResultSet tileResults = results.tileResults;

			try {

				// Determine the requested tile dimensions, or use the
				// dimensions of a single tile matrix tile
				int requestedTileWidth = width != null ? width
						: (int) tileMatrix.getTileWidth();
				int requestedTileHeight = height != null ? height
						: (int) tileMatrix.getTileHeight();

				// Determine the size of the tile to initially draw
				int tileWidth = requestedTileWidth;
				int tileHeight = requestedTileHeight;
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

				// Draw the resulting bitmap with the matching tiles
				elevations = getElevation(tileMatrix, tileResults, request,
						tileWidth, tileHeight);

				// Project the elevations if needed
				if (elevations != null && !sameProjection && !request.isPoint()) {
					elevations = reprojectElevations(elevations,
							requestedTileWidth, requestedTileHeight,
							request.getBoundingBox(),
							transformRequestToElevation,
							requestProjectedBoundingBox);
				}

			} finally {
				tileResults.close();
			}
		}

		return elevations;
	}

	private class TileMatrixResults {
		TileMatrix tileMatrix;
		TileResultSet tileResults;

		TileMatrixResults(TileMatrix tileMatrix, TileResultSet tileResults) {
			this.tileMatrix = tileMatrix;
			this.tileResults = tileResults;
		}
	}

	private TileMatrixResults getResults(
			BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix) {
		TileMatrixResults results = null;
		TileResultSet tileResults = retrieveTileResults(
				requestProjectedBoundingBox, tileMatrix);
		if (tileResults != null) {
			if (tileResults.getCount() > 0) {
				results = new TileMatrixResults(tileMatrix, tileResults);
			} else {
				tileResults.close();
			}
		}
		return results;
	}

	private TileMatrixResults getResultsZoom(
			BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix) {

		TileMatrixResults results = null;

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

	private TileMatrixResults getResultsZoomIn(
			BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix) {

		TileMatrixResults results = null;
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

	private TileMatrixResults getResultsZoomOut(
			BoundingBox requestProjectedBoundingBox, TileMatrix tileMatrix) {

		TileMatrixResults results = null;
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

	private Double[][] getElevation(TileMatrix tileMatrix,
			TileResultSet tileResults, ElevationRequest request, int tileWidth,
			int tileHeight) {

		Double[][] elevations = null;

		while (tileResults.moveToNext()) {

			// Get the next tile
			TileRow tileRow = tileResults.getRow();

			// Get the bounding box of the elevation
			BoundingBox tileBoundingBox = TileBoundingBoxUtils.getBoundingBox(
					elevationBoundingBox, tileMatrix, tileRow.getTileColumn(),
					tileRow.getTileRow());

			// Get the bounding box where the requested image and
			// tile overlap
			BoundingBox overlap = request.overlap(tileBoundingBox);

			// If the tile overlaps with the requested box
			if (overlap != null) {

				// Get the rectangle of the tile image to draw
				ImageRectangle src = TileBoundingBoxJavaUtils.getRectangle(
						tileMatrix.getTileWidth(), tileMatrix.getTileHeight(),
						tileBoundingBox, overlap);

				// Get the rectangle of where to draw the tile in
				// the resulting image
				ImageRectangle dest = TileBoundingBoxJavaUtils.getRectangle(
						tileWidth, tileHeight,
						request.getProjectedBoundingBox(), overlap);

				if (src.isValidAllowEmpty() && dest.isValidAllowEmpty()) {

					// Create the elevations array first time through
					if (elevations == null) {
						elevations = new Double[tileHeight][tileWidth];
					}

					int destTop = Math.min(dest.getTop(), tileHeight - 1);
					int destBottom = Math.min(dest.getBottom(), tileHeight - 1);
					int destLeft = Math.min(dest.getLeft(), tileWidth - 1);
					int destRight = Math.min(dest.getRight(), tileWidth - 1);

					int destWidth = destRight - destLeft + 1;
					int destHeight = destBottom - destTop + 1;

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

					float widthRatio = srcWidth / destWidth;
					float heightRatio = srcHeight / destHeight;

					GriddedTile griddedTile = getGriddedTile(tileRow.getId());

					BufferedImage image = null;
					try {
						image = tileRow.getTileDataImage();
					} catch (IOException e) {
						throw new GeoPackageException(
								"Failed to get the Tile Row Data Image", e);
					}
					WritableRaster raster = image.getRaster();

					for (int y = destTop; y <= destBottom; y++) {
						for (int x = destLeft; x <= destRight; x++) {

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

							double elevation = getElevationValue(griddedTile,
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
	 * Reproject the elevations to the requested projection
	 *
	 * @param elevations
	 *            elevations
	 * @param requestedTileWidth
	 *            requested tile width
	 * @param requestedTileHeight
	 *            requested tile height
	 * @param requestBoundingBox
	 *            request bounding box in the request projection
	 * @param transformRequestToElevation
	 *            transformation from request to elevations
	 * @param elevationBoundingBox
	 *            elevations bounding box
	 * @return projected elevations
	 */
	private Double[][] reprojectElevations(Double[][] elevations,
			int requestedTileWidth, int requestedTileHeight,
			BoundingBox requestBoundingBox,
			ProjectionTransform transformRequestToElevation,
			BoundingBox elevationBoundingBox) {

		final double requestedWidthUnitsPerPixel = (requestBoundingBox
				.getMaxLongitude() - requestBoundingBox.getMinLongitude())
				/ requestedTileWidth;
		final double requestedHeightUnitsPerPixel = (requestBoundingBox
				.getMaxLatitude() - requestBoundingBox.getMinLatitude())
				/ requestedTileHeight;

		final double tilesDistanceWidth = elevationBoundingBox
				.getMaxLongitude() - elevationBoundingBox.getMinLongitude();
		final double tilesDistanceHeight = elevationBoundingBox
				.getMaxLatitude() - elevationBoundingBox.getMinLatitude();

		final int width = elevations[0].length;
		final int height = elevations.length;

		Double[][] projectedElevations = new Double[requestedTileHeight][requestedTileWidth];

		// Retrieve each elevation in the unprojected elevations
		for (int y = 0; y < requestedTileHeight; y++) {
			for (int x = 0; x < requestedTileWidth; x++) {

				double longitude = requestBoundingBox.getMinLongitude()
						+ (x * requestedWidthUnitsPerPixel);
				double latitude = requestBoundingBox.getMaxLatitude()
						- (y * requestedHeightUnitsPerPixel);
				ProjCoordinate fromCoord = new ProjCoordinate(longitude,
						latitude);
				ProjCoordinate toCoord = transformRequestToElevation
						.transform(fromCoord);
				double projectedLongitude = toCoord.x;
				double projectedLatitude = toCoord.y;

				int xPixel = (int) Math
						.round(((projectedLongitude - elevationBoundingBox
								.getMinLongitude()) / tilesDistanceWidth)
								* width);
				int yPixel = (int) Math
						.round(((elevationBoundingBox.getMaxLatitude() - projectedLatitude) / tilesDistanceHeight)
								* height);

				xPixel = Math.max(0, xPixel);
				xPixel = Math.min(width - 1, xPixel);

				yPixel = Math.max(0, yPixel);
				yPixel = Math.min(height - 1, yPixel);

				Double elevation = elevations[yPixel][xPixel];
				projectedElevations[y][x] = elevation;
			}
		}

		return projectedElevations;
	}

	/**
	 * Get the tile matrix that contains the tiles for the bounding box, matches
	 * against the bounding box and zoom level
	 *
	 * @param projectedRequestBoundingBox
	 *            bounding box projected to the tiles
	 * @return tile matrix or null
	 */
	private TileMatrix getTileMatrix(ElevationRequest request) {

		TileMatrix tileMatrix = null;

		// Check if the request overlaps the tile matrix set
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
	 * Get the tile row results of tiles needed to draw the requested bounding
	 * box tile
	 *
	 * @param projectedRequestBoundingBox
	 *            bounding box projected to the tiles
	 * @param tileMatrix
	 * @return tile cursor results or null
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
	 * Get the elevation value
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
	 * Get the elevation value
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

}
