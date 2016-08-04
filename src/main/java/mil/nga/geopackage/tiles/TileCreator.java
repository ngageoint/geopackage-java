package mil.nga.geopackage.tiles;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;

import org.osgeo.proj4j.ProjCoordinate;

/**
 * Tile Creator, creates a tile from a tile matrix to the desired projection
 * 
 * @author osbornb
 * @since 1.2.0
 */
public class TileCreator {

	/**
	 * Tile DAO
	 */
	private final TileDao tileDao;

	/**
	 * Tile width
	 */
	private final Integer width;

	/**
	 * Tile height
	 */
	private final Integer height;

	/**
	 * Tile Matrix Set
	 */
	private final TileMatrixSet tileMatrixSet;

	/**
	 * Projection of the requests
	 */
	private final Projection requestProjection;

	/**
	 * Projection of the tiles
	 */
	private final Projection tilesProjection;

	/**
	 * Tile Set bounding box
	 */
	private final BoundingBox tileSetBoundingBox;

	/**
	 * Flag indicating the the tile and request projections are the same
	 */
	private final boolean sameProjection;

	/**
	 * Image format
	 */
	private final String imageFormat;

	/**
	 * Constructor
	 *
	 * @param tileDao
	 * @param width
	 * @param height
	 * @param requestProjection
	 * @param imageFormat
	 */
	public TileCreator(TileDao tileDao, Integer width, Integer height,
			Projection requestProjection, String imageFormat) {
		this.tileDao = tileDao;
		this.width = width;
		this.height = height;
		this.requestProjection = requestProjection;
		this.imageFormat = imageFormat;

		if (imageFormat == null && (width != null || height != null)) {
			throw new GeoPackageException(
					"The width and height request size can not be specified when requesting raw tiles (no image format specified)");
		}

		tileMatrixSet = tileDao.getTileMatrixSet();
		tilesProjection = ProjectionFactory.getProjection(tileDao
				.getTileMatrixSet().getSrs());
		tileSetBoundingBox = tileMatrixSet.getBoundingBox();

		// Check if the projections have the same units
		sameProjection = (requestProjection.getUnit().name
				.equals(tilesProjection.getUnit().name));

		if (imageFormat == null && !sameProjection) {
			throw new GeoPackageException(
					"The requested projection must be the same as the stored tiles when requesting raw tiles (no image format specified)");
		}
	}

	/**
	 * Constructor, use the tile tables image width and height as request size
	 *
	 * @param tileDao
	 * @param imageFormat
	 */
	public TileCreator(TileDao tileDao, String imageFormat) {
		this(tileDao, null, null, tileDao.getProjection(), imageFormat);
	}

	/**
	 * Constructor, use the tile tables image width and height as request size
	 * and request as the specified projection
	 *
	 * @param tileDao
	 * @param requestProjection
	 * @param imageFormat
	 */
	public TileCreator(TileDao tileDao, Projection requestProjection,
			String imageFormat) {
		this(tileDao, null, null, requestProjection, imageFormat);
	}

	/**
	 * Constructor, request raw tile images directly from the tile table in
	 * their original size
	 *
	 * @param tileDao
	 */
	public TileCreator(TileDao tileDao) {
		this(tileDao, null, null, tileDao.getProjection(), null);
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
	 * Get the requested tile width
	 * 
	 * @return width
	 */
	public Integer getWidth() {
		return width;
	}

	/**
	 * Get the requested tile height
	 * 
	 * @return height
	 */
	public Integer getHeight() {
		return height;
	}

	/**
	 * Get the tile matrix set
	 * 
	 * @return tile matrix set
	 */
	public TileMatrixSet getTileMatrixSet() {
		return tileMatrixSet;
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
	 * Get the tiles projection
	 * 
	 * @return tiles projection
	 */
	public Projection getTilesProjection() {
		return tilesProjection;
	}

	/**
	 * Get the tile set bounding box
	 * 
	 * @return tile set bounding box
	 */
	public BoundingBox getTileSetBoundingBox() {
		return tileSetBoundingBox;
	}

	/**
	 * Is the request and tile projection the same
	 * 
	 * @return true if the same
	 */
	public boolean isSameProjection() {
		return sameProjection;
	}

	/**
	 * Get the requested image format
	 * 
	 * @return image format
	 */
	public String getImageFormat() {
		return imageFormat;
	}

	/**
	 * Check if the tile table contains a tile for the request bounding box
	 *
	 * @param requestBoundingBox
	 *            request bounding box in the request projection
	 * @return true if a tile exists
	 */
	public boolean hasTile(BoundingBox requestBoundingBox) {

		boolean hasTile = false;

		// Transform to the projection of the tiles
		ProjectionTransform transformRequestToTiles = requestProjection
				.getTransformation(tilesProjection);
		BoundingBox tilesBoundingBox = transformRequestToTiles
				.transform(requestBoundingBox);

		TileMatrix tileMatrix = getTileMatrix(tilesBoundingBox);

		TileResultSet tileResults = retrieveTileResults(tilesBoundingBox,
				tileMatrix);
		if (tileResults != null) {

			try {
				hasTile = tileResults.getCount() > 0;
			} finally {
				tileResults.close();
			}
		}

		return hasTile;
	}

	/**
	 * Get the tile from the request bounding box in the request projection
	 *
	 * @param requestBoundingBox
	 *            request bounding box in the request projection
	 * @return image
	 */
	public GeoPackageTile getTile(BoundingBox requestBoundingBox) {

		GeoPackageTile tile = null;

		// Transform to the projection of the tiles
		ProjectionTransform transformRequestToTiles = requestProjection
				.getTransformation(tilesProjection);
		BoundingBox tilesBoundingBox = transformRequestToTiles
				.transform(requestBoundingBox);

		TileMatrix tileMatrix = getTileMatrix(tilesBoundingBox);

		TileResultSet tileResults = retrieveTileResults(tilesBoundingBox,
				tileMatrix);
		if (tileResults != null) {

			try {

				if (tileResults.getCount() > 0) {

					BoundingBox requestProjectedBoundingBox = transformRequestToTiles
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
					GeoPackageTile geoPackageTile = drawTile(tileMatrix,
							tileResults, requestProjectedBoundingBox,
							tileWidth, tileHeight);

					// Create the tile
					if (geoPackageTile != null) {

						// Project the tile if needed
						if (!sameProjection
								&& geoPackageTile.getImage() != null) {
							BufferedImage reprojectTile = reprojectTile(
									geoPackageTile.getImage(),
									requestedTileWidth, requestedTileHeight,
									requestBoundingBox,
									transformRequestToTiles, tilesBoundingBox);
							geoPackageTile = new GeoPackageTile(
									requestedTileWidth, requestedTileHeight,
									reprojectTile);
						}

						tile = geoPackageTile;
					}

				}
			} finally {
				tileResults.close();
			}
		}

		return tile;
	}

	/**
	 * Draw the tile from the tile results
	 *
	 * @param tileMatrix
	 * @param tileResults
	 * @param requestProjectedBoundingBox
	 * @param tileWidth
	 * @param tileHeight
	 * @return tile bitmap
	 */
	private GeoPackageTile drawTile(TileMatrix tileMatrix,
			TileResultSet tileResults, BoundingBox requestProjectedBoundingBox,
			int tileWidth, int tileHeight) {

		// Draw the resulting bitmap with the matching tiles
		GeoPackageTile geoPackageTile = null;
		Graphics graphics = null;
		while (tileResults.moveToNext()) {

			// Get the next tile
			TileRow tileRow = tileResults.getRow();
			BufferedImage tileDataImage;
			try {
				tileDataImage = tileRow.getTileDataImage();
			} catch (IOException e) {
				throw new GeoPackageException(
						"Failed to read the tile row image data", e);
			}

			// Get the bounding box of the tile
			BoundingBox tileBoundingBox = TileBoundingBoxUtils.getBoundingBox(
					tileSetBoundingBox, tileMatrix, tileRow.getTileColumn(),
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

					if (imageFormat != null) {

						// Create the bitmap first time through
						if (geoPackageTile == null) {
							BufferedImage bufferedImage = ImageUtils
									.createBufferedImage(tileWidth, tileHeight,
											imageFormat);
							graphics = bufferedImage.getGraphics();
							geoPackageTile = new GeoPackageTile(tileWidth,
									tileHeight, bufferedImage);
						}

						// Draw the tile to the image
						graphics.drawImage(tileDataImage, dest.getLeft(),
								dest.getTop(), dest.getRight(),
								dest.getBottom(), src.getLeft(), src.getTop(),
								src.getRight(), src.getBottom(), null);
					} else {

						// Verify only one image was found and
						// it lines up perfectly
						if (geoPackageTile != null || !src.equals(dest)) {
							throw new GeoPackageException(
									"Raw image only supported when the images are aligned with the tile format requiring no combining and cropping");
						}

						geoPackageTile = new GeoPackageTile(tileWidth,
								tileHeight, tileRow.getTileData());
					}
				}
			}
		}

		// Check if the entire image is transparent
		if (geoPackageTile != null && geoPackageTile.getImage() != null
				&& ImageUtils.isFullyTransparent(geoPackageTile.getImage())) {
			geoPackageTile = null;
		}

		return geoPackageTile;
	}

	/**
	 * Reproject the tile to the requested projection
	 *
	 * @param tile
	 *            tile in the tile matrix projection
	 * @param requestedTileWidth
	 *            requested tile width
	 * @param requestedTileHeight
	 *            requested tile height
	 * @param requestBoundingBox
	 *            request bounding box in the request projection
	 * @param transformRequestToTiles
	 *            transformation from request to tiles
	 * @param tilesBoundingBox
	 *            request bounding box in the tile matrix projection
	 * @return projected tile
	 */
	private BufferedImage reprojectTile(BufferedImage tile,
			int requestedTileWidth, int requestedTileHeight,
			BoundingBox requestBoundingBox,
			ProjectionTransform transformRequestToTiles,
			BoundingBox tilesBoundingBox) {

		final double requestedWidthUnitsPerPixel = (requestBoundingBox
				.getMaxLongitude() - requestBoundingBox.getMinLongitude())
				/ requestedTileWidth;
		final double requestedHeightUnitsPerPixel = (requestBoundingBox
				.getMaxLatitude() - requestBoundingBox.getMinLatitude())
				/ requestedTileHeight;

		final double tilesDistanceWidth = tilesBoundingBox.getMaxLongitude()
				- tilesBoundingBox.getMinLongitude();
		final double tilesDistanceHeight = tilesBoundingBox.getMaxLatitude()
				- tilesBoundingBox.getMinLatitude();

		final int width = tile.getWidth();
		final int height = tile.getHeight();

		// Tile pixels of the tile matrix tiles
		int[] pixels = new int[width * height];
		tile.getRGB(0, 0, width, height, pixels, 0, width);

		// Projected tile pixels to draw the reprojected tile
		int[] projectedPixels = new int[requestedTileWidth
				* requestedTileHeight];

		// Retrieve each pixel in the new tile from the unprojected tile
		for (int y = 0; y < requestedTileHeight; y++) {
			for (int x = 0; x < requestedTileWidth; x++) {

				double longitude = requestBoundingBox.getMinLongitude()
						+ (x * requestedWidthUnitsPerPixel);
				double latitude = requestBoundingBox.getMaxLatitude()
						- (y * requestedHeightUnitsPerPixel);
				ProjCoordinate fromCoord = new ProjCoordinate(longitude,
						latitude);
				ProjCoordinate toCoord = transformRequestToTiles
						.transform(fromCoord);
				double projectedLongitude = toCoord.x;
				double projectedLatitude = toCoord.y;

				int xPixel = (int) Math
						.round(((projectedLongitude - tilesBoundingBox
								.getMinLongitude()) / tilesDistanceWidth)
								* width);
				int yPixel = (int) Math
						.round(((tilesBoundingBox.getMaxLatitude() - projectedLatitude) / tilesDistanceHeight)
								* height);

				xPixel = Math.max(0, xPixel);
				xPixel = Math.min(width - 1, xPixel);

				yPixel = Math.max(0, yPixel);
				yPixel = Math.min(height - 1, yPixel);

				int color = pixels[(yPixel * width) + xPixel];
				projectedPixels[(y * requestedTileWidth) + x] = color;
			}
		}

		// Draw the new image
		BufferedImage projectedTileImage = new BufferedImage(
				requestedTileWidth, requestedTileHeight, tile.getType());
		projectedTileImage.setRGB(0, 0, requestedTileWidth,
				requestedTileHeight, projectedPixels, 0, requestedTileWidth);

		return projectedTileImage;
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
				tileSetBoundingBox) != null) {

			// Get the tile distance
			double distanceWidth = projectedRequestBoundingBox
					.getMaxLongitude()
					- projectedRequestBoundingBox.getMinLongitude();
			double distanceHeight = projectedRequestBoundingBox
					.getMaxLatitude()
					- projectedRequestBoundingBox.getMinLatitude();

			// Get the zoom level to request based upon the tile size
			Long zoomLevel = tileDao
					.getZoomLevel(distanceWidth, distanceHeight);

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
					tileSetBoundingBox, tileMatrix.getMatrixWidth(),
					tileMatrix.getMatrixHeight(), projectedRequestBoundingBox);

			// Query for matching tiles in the tile grid
			tileResults = tileDao.queryByTileGrid(tileGrid,
					tileMatrix.getZoomLevel());

		}

		return tileResults;
	}

}
