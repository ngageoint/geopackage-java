package mil.nga.geopackage.extension.elevation;

import java.awt.image.BufferedImage;
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

	/**
	 * Get the elevation at the coordinate
	 * 
	 * @param latitude
	 *            latitude
	 * @param longitude
	 *            longitude
	 * @return elevation value
	 */
	public Float getElevation(double latitude, double longitude) {
		BoundingBox requestBoundingBox = new BoundingBox(longitude, longitude,
				latitude, latitude);
		Float[][] elevations = getElevation(requestBoundingBox, 1, 1);
		Float elevation = null;
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
	public Float[][] getElevation(BoundingBox requestBoundingBox) {
		Float[][] elevations = getElevation(requestBoundingBox, width, height);
		return elevations;
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
	public Float[][] getElevation(BoundingBox requestBoundingBox,
			Integer width, Integer height) {

		Float[][] elevations = null;

		// Transform to the projection of the tiles
		ProjectionTransform transformRequestToElevation = requestProjection
				.getTransformation(elevationProjection);
		BoundingBox elevationBoundingBox = transformRequestToElevation
				.transform(requestBoundingBox);

		TileMatrix tileMatrix = getTileMatrix(elevationBoundingBox);

		TileResultSet tileResults = retrieveTileResults(elevationBoundingBox,
				tileMatrix);
		if (tileResults != null) {

			try {

				if (tileResults.getCount() > 0) {

					BoundingBox requestProjectedBoundingBox = transformRequestToElevation
							.transform(requestBoundingBox);

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
						tileWidth = (int) Math
								.round((requestProjectedBoundingBox
										.getMaxLongitude() - requestProjectedBoundingBox
										.getMinLongitude())
										/ tileMatrix.getPixelXSize());
						tileHeight = (int) Math
								.round((requestProjectedBoundingBox
										.getMaxLatitude() - requestProjectedBoundingBox
										.getMinLatitude())
										/ tileMatrix.getPixelYSize());
					}

					// Draw the resulting bitmap with the matching tiles
					elevations = getElevation(tileMatrix, tileResults,
							requestProjectedBoundingBox, tileWidth, tileHeight);

					if (elevations != null) {

						// Project the elevations if needed
						if (!sameProjection) {
							// TODO reproject the elevations
						}

					}

				}
			} finally {
				tileResults.close();
			}
		}

		return elevations;
	}

	private Float[][] getElevation(TileMatrix tileMatrix,
			TileResultSet tileResults, BoundingBox requestProjectedBoundingBox,
			int tileWidth, int tileHeight) {

		Float[][] elevations = null;

		while (tileResults.moveToNext()) {

			// Get the next tile
			TileRow tileRow = tileResults.getRow();

			// Get the bounding box of the tile
			BoundingBox tileBoundingBox = TileBoundingBoxUtils.getBoundingBox(
					elevationBoundingBox, tileMatrix, tileRow.getTileColumn(),
					tileRow.getTileRow());

			// Get the bounding box where the requested image and
			// tile overlap
			BoundingBox overlap = TileBoundingBoxUtils.overlap(
					requestProjectedBoundingBox, tileBoundingBox);

			// If the tile overlaps with the requested box
			if (overlap != null) {

				// Get the rectangle of the tile image to draw
				ImageRectangle src = TileBoundingBoxJavaUtils.getRectangle(
						tileMatrix.getTileWidth(), tileMatrix.getTileHeight(),
						tileBoundingBox, overlap);

				// Get the rectangle of where to draw the tile in
				// the resulting image
				ImageRectangle dest = TileBoundingBoxJavaUtils.getRectangle(
						tileWidth, tileHeight, requestProjectedBoundingBox,
						overlap);

				if (src.isValid() && dest.isValid()) {

					// Create the elevations array first time through
					if (elevations == null) {
						elevations = new Float[tileHeight][tileWidth];
					}

					int destTop = dest.getTop();
					int destBottom = dest.getBottom();
					int destLeft = dest.getLeft();
					int destRight = dest.getRight();

					int destWidth = destRight - destLeft + 1;
					int destHeight = destBottom - destTop + 1;

					int srcTop = src.getTop();
					int srcBottom = src.getBottom();
					int srcLeft = src.getLeft();
					int srcRight = src.getRight();

					int srcWidth = srcRight - srcLeft + 1;
					int srcHeight = srcBottom - srcTop + 1;

					float widthRatio = srcWidth / destWidth;
					float heightRatio = srcHeight / destHeight;

					GriddedTile griddedTile = getGriddedTile(tileRow.getId());

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

							float elevation = getElevationValue(griddedTile,
									tileRow, xSource, ySource);

							elevations[y][x] = elevation;
						}
					}

				}
			}
		}

		return elevations;
	}

	/**
	 * Get the tile matrix that contains the tiles for the bounding box, matches
	 * against the bounding box and zoom level
	 *
	 * @param projectedRequestBoundingBox
	 *            bounding box projected to the tiles
	 * @return tile matrix or null
	 */
	private TileMatrix getTileMatrix(BoundingBox projectedRequestBoundingBox) {

		TileMatrix tileMatrix = null;

		// Check if the request overlaps the tile matrix set
		if (TileBoundingBoxUtils.overlap(projectedRequestBoundingBox,
				elevationBoundingBox) != null) {

			// Get the tile distance
			double distance = projectedRequestBoundingBox.getMaxLongitude()
					- projectedRequestBoundingBox.getMinLongitude();

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
	public float getElevationValue(GriddedTile griddedTile, TileRow tileRow,
			int x, int y) {
		BufferedImage image = null;
		try {
			image = tileRow.getTileDataImage();
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed to get the Tile Row Data Image", e);
		}
		float elevation = getElevationValue(griddedTile, image, x, y);
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
	public float getElevationValue(TileRow tileRow, int x, int y) {
		GriddedTile griddedTile = getGriddedTile(tileRow.getId());
		float elevation = getElevationValue(griddedTile, tileRow, x, y);
		return elevation;
	}

}
