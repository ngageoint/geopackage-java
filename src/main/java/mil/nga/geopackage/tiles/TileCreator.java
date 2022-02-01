package mil.nga.geopackage.tiles;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.proj4j.ProjCoordinate;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.nga.scale.TileScaling;
import mil.nga.geopackage.extension.nga.scale.TileScalingType;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.proj.Projection;
import mil.nga.proj.ProjectionTransform;
import mil.nga.sf.proj.GeometryTransform;

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
	 * Flag indicating if the tile and request projections are the same
	 */
	private final boolean sameProjection;

	/**
	 * Flag indicating if the tile and request projection units are the same
	 */
	private final boolean sameUnit;

	/**
	 * Tile Scaling options
	 */
	private TileScaling scaling;

	/**
	 * Image format
	 */
	private final String imageFormat;

	/**
	 * Constructor
	 *
	 * @param tileDao
	 *            tile dao
	 * @param width
	 *            request width
	 * @param height
	 *            request height
	 * @param requestProjection
	 *            request projection
	 * @param imageFormat
	 *            image format
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
		tilesProjection = tileDao.getProjection();
		tileSetBoundingBox = tileMatrixSet.getBoundingBox();

		// Check if the projections are the same or have the same units
		sameProjection = requestProjection.equals(tilesProjection);
		sameUnit = (requestProjection.getUnit().name
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
	 *            tile dao
	 * @param imageFormat
	 *            image format
	 */
	public TileCreator(TileDao tileDao, String imageFormat) {
		this(tileDao, null, null, tileDao.getProjection(), imageFormat);
	}

	/**
	 * Constructor
	 *
	 * @param tileDao
	 *            tile dao
	 * @param width
	 *            request width
	 * @param height
	 *            request height
	 * @param imageFormat
	 *            image format
	 * @since 5.0.0
	 */
	public TileCreator(TileDao tileDao, Integer width, Integer height,
			String imageFormat) {
		this(tileDao, width, height, tileDao.getProjection(), imageFormat);
	}

	/**
	 * Constructor, use the tile tables image width and height as request size
	 * and request as the specified projection
	 *
	 * @param tileDao
	 *            tile dao
	 * @param requestProjection
	 *            request projection
	 * @param imageFormat
	 *            image format
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
	 *            tile dao
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
	 * Is the request and tile projection the same unit
	 *
	 * @return true if the same
	 * @since 4.0.0
	 */
	public boolean isSameUnit() {
		return sameUnit;
	}

	/**
	 * Get the tile scaling options
	 *
	 * @return tile scaling options
	 * @since 2.0.2
	 */
	public TileScaling getScaling() {
		return scaling;
	}

	/**
	 * Set the tile scaling options
	 *
	 * @param scaling
	 *            tile scaling options
	 * @since 2.0.2
	 */
	public void setScaling(TileScaling scaling) {
		this.scaling = scaling;
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
		GeometryTransform transformRequestToTiles = GeometryTransform
				.create(requestProjection, tilesProjection);
		BoundingBox tilesBoundingBox = requestBoundingBox
				.transform(transformRequestToTiles);

		List<TileMatrix> tileMatrices = getTileMatrices(tilesBoundingBox);

		for (int i = 0; !hasTile && i < tileMatrices.size(); i++) {

			TileMatrix tileMatrix = tileMatrices.get(i);

			TileResultSet tileResults = retrieveTileResults(tilesBoundingBox,
					tileMatrix);
			if (tileResults != null) {

				try {
					hasTile = tileResults.getCount() > 0;
				} finally {
					tileResults.close();
				}
			}
		}

		return hasTile;
	}

	/**
	 * Get the tile from the request bounding box in the request projection
	 *
	 * @param requestBoundingBox
	 *            request bounding box in the request projection
	 *
	 * @return tile
	 */
	public GeoPackageTile getTile(BoundingBox requestBoundingBox) {
		return getTile(requestBoundingBox, null);
	}

	/**
	 * Get the tile from the request bounding box in the request projection,
	 * only from the zoom level
	 *
	 * @param requestBoundingBox
	 *            request bounding box in the request projection
	 * @param zoomLevel
	 *            zoom level
	 *
	 * @return tile
	 * @since 5.0.0
	 */
	public GeoPackageTile getTile(BoundingBox requestBoundingBox,
			long zoomLevel) {
		GeoPackageTile tile = null;
		TileMatrix tileMatrix = tileDao.getTileMatrix(zoomLevel);
		if (tileMatrix != null) {
			List<TileMatrix> tileMatrices = new ArrayList<>();
			tileMatrices.add(tileMatrix);
			tile = getTile(requestBoundingBox, tileMatrices);
		}
		return tile;
	}

	/**
	 * Get the tile from the request bounding box in the request projection
	 *
	 * @param requestBoundingBox
	 *            request bounding box in the request projection
	 * @param tileMatrices
	 *            tile matrices
	 *
	 * @return tile
	 */
	private GeoPackageTile getTile(BoundingBox requestBoundingBox,
			List<TileMatrix> tileMatrices) {

		GeoPackageTile tile = null;

		// Transform to the projection of the tiles
		GeometryTransform transformRequestToTiles = GeometryTransform
				.create(requestProjection, tilesProjection);
		BoundingBox tilesBoundingBox = requestBoundingBox
				.transform(transformRequestToTiles);

		if (tileMatrices == null) {
			tileMatrices = getTileMatrices(tilesBoundingBox);
		}

		for (int i = 0; tile == null && i < tileMatrices.size(); i++) {

			TileMatrix tileMatrix = tileMatrices.get(i);

			TileResultSet tileResults = retrieveTileResults(tilesBoundingBox,
					tileMatrix);
			if (tileResults != null) {

				try {

					if (tileResults.getCount() > 0) {

						// Determine the tile dimensions
						int[] tileDimensions = tileDimensions(
								requestBoundingBox, tilesBoundingBox,
								tileMatrix);
						int requestedTileWidth = tileDimensions[0];
						int requestedTileHeight = tileDimensions[1];

						// Determine the size of the tile to initially draw
						int tileWidth = requestedTileWidth;
						int tileHeight = requestedTileHeight;
						if (!sameUnit) {
							tileWidth = (int) Math.round((tilesBoundingBox
									.getMaxLongitude()
									- tilesBoundingBox.getMinLongitude())
									/ tileMatrix.getPixelXSize());
							tileHeight = (int) Math
									.round((tilesBoundingBox.getMaxLatitude()
											- tilesBoundingBox.getMinLatitude())
											/ tileMatrix.getPixelYSize());
						}

						// Draw the resulting bitmap with the matching tiles
						GeoPackageTile geoPackageTile = drawTile(tileMatrix,
								tileResults, tilesBoundingBox, tileWidth,
								tileHeight);

						// Create the tile
						if (geoPackageTile != null) {

							// Project the tile if needed
							if (!sameProjection
									&& geoPackageTile.getImage() != null) {
								BufferedImage reprojectTile = reprojectTile(
										geoPackageTile.getImage(),
										requestedTileWidth, requestedTileHeight,
										requestBoundingBox,
										transformRequestToTiles,
										tilesBoundingBox);
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
		}

		return tile;
	}

	/**
	 * Determine the tile dimensions. Specified width and/or height values are
	 * used. When only one of width or height is specified, other is determined
	 * as a request ratio. When neither width or height is specified, determine
	 * from the tile matrix as a request ratio.
	 * 
	 * @param requestBoundingBox
	 *            request bounding box
	 * @param tilesBoundingBox
	 *            tiles bounding box
	 * @param tileMatrix
	 *            tile matrix
	 * @return tile dimensions array of size 2 [width, height]
	 */
	private int[] tileDimensions(BoundingBox requestBoundingBox,
			BoundingBox tilesBoundingBox, TileMatrix tileMatrix) {

		// Determine the tile dimensions
		int requestedTileWidth;
		int requestedTileHeight;
		if (width != null && height != null) {

			// Requested dimensions
			requestedTileWidth = width;
			requestedTileHeight = height;

		} else if (width == null && height == null) {

			// Determine dimensions from a single tile matrix
			// tile as a ratio to the requested bounds
			double requestLonRange = requestBoundingBox.getLongitudeRange();
			double requestLatRange = requestBoundingBox.getLatitudeRange();
			double pixelXSize = tileMatrix.getPixelXSize();
			double pixelYSize = tileMatrix.getPixelYSize();
			if (requestProjection.isUnit(tilesProjection.getUnit())) {
				// Same unit, use the pixel x and y size
				requestedTileWidth = (int) Math
						.round(requestLonRange / pixelXSize);
				requestedTileHeight = (int) Math
						.round(requestLatRange / pixelYSize);
			} else {
				// Use the max tile pixel length and adjust to
				// the sides to the requested bounds ratio
				double tileWidth = tilesBoundingBox.getLongitudeRange()
						/ pixelXSize;
				double tileHeight = tilesBoundingBox.getLatitudeRange()
						/ pixelYSize;
				if (requestLonRange < requestLatRange) {
					requestedTileWidth = (int) Math.round(
							tileHeight * (requestLonRange / requestLatRange));
					requestedTileHeight = (int) Math.round(tileHeight);
				} else if (requestLatRange < requestLonRange) {
					requestedTileWidth = (int) Math.round(tileWidth);
					requestedTileHeight = (int) Math.round(
							tileWidth * (requestLatRange / requestLonRange));
				} else {
					requestedTileWidth = (int) Math
							.round(Math.max(tileWidth, tileHeight));
					requestedTileHeight = requestedTileWidth;
				}
			}

		} else if (width == null) {

			// Requested height, determine width as a ratio from
			// the requested bounds
			requestedTileHeight = height;
			requestedTileWidth = (int) Math
					.round(height * (requestBoundingBox.getLongitudeRange()
							/ requestBoundingBox.getLatitudeRange()));

		} else {

			// Requested width, determine height as a ratio from
			// the requested bounds
			requestedTileWidth = width;
			requestedTileHeight = (int) Math
					.round(width * (requestBoundingBox.getLatitudeRange()
							/ requestBoundingBox.getLongitudeRange()));

		}

		return new int[] { requestedTileWidth, requestedTileHeight };
	}

	/**
	 * Draw the tile from the tile results
	 *
	 * @param tileMatrix
	 *            tile matrix
	 * @param tileResults
	 *            tile results
	 * @param requestBoundingBox
	 *            projected request bounding box
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return GeoPackage Tile
	 */
	private GeoPackageTile drawTile(TileMatrix tileMatrix,
			TileResultSet tileResults, BoundingBox requestBoundingBox,
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
			BoundingBox overlap = requestBoundingBox.overlap(tileBoundingBox);

			// If the tile overlaps with the requested box
			if (overlap != null) {

				// Get the rectangle of the tile image to draw
				ImageRectangle src = TileBoundingBoxJavaUtils.getRectangle(
						tileMatrix.getTileWidth(), tileMatrix.getTileHeight(),
						tileBoundingBox, overlap);

				// Get the rectangle of where to draw the tile in
				// the resulting image
				ImageRectangle dest = TileBoundingBoxJavaUtils.getRectangle(
						tileWidth, tileHeight, requestBoundingBox, overlap);

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

				int xPixel = (int) Math.round(((projectedLongitude
						- tilesBoundingBox.getMinLongitude())
						/ tilesDistanceWidth) * width);
				int yPixel = (int) Math.round(
						((tilesBoundingBox.getMaxLatitude() - projectedLatitude)
								/ tilesDistanceHeight) * height);

				xPixel = Math.max(0, xPixel);
				xPixel = Math.min(width - 1, xPixel);

				yPixel = Math.max(0, yPixel);
				yPixel = Math.min(height - 1, yPixel);

				int color = pixels[(yPixel * width) + xPixel];
				projectedPixels[(y * requestedTileWidth) + x] = color;
			}
		}

		// Draw the new image
		BufferedImage projectedTileImage = new BufferedImage(requestedTileWidth,
				requestedTileHeight, tile.getType());
		projectedTileImage.setRGB(0, 0, requestedTileWidth, requestedTileHeight,
				projectedPixels, 0, requestedTileWidth);

		return projectedTileImage;
	}

	/**
	 * Get the tile matrices that may contain the tiles for the bounding box,
	 * matches against the bounding box and zoom level options
	 *
	 * @param projectedRequestBoundingBox
	 *            bounding box projected to the tiles
	 * @return tile matrices
	 */
	private List<TileMatrix> getTileMatrices(
			BoundingBox projectedRequestBoundingBox) {

		List<TileMatrix> tileMatrices = new ArrayList<>();

		// Check if the request overlaps the tile matrix set
		if (!tileDao.getTileMatrices().isEmpty()
				&& projectedRequestBoundingBox.intersects(tileSetBoundingBox)) {

			// Get the tile distance
			double distanceWidth = projectedRequestBoundingBox.getMaxLongitude()
					- projectedRequestBoundingBox.getMinLongitude();
			double distanceHeight = projectedRequestBoundingBox.getMaxLatitude()
					- projectedRequestBoundingBox.getMinLatitude();

			// Get the zoom level to request based upon the tile size
			Long requestZoomLevel = null;
			if (scaling != null) {
				// When options are provided, get the approximate zoom level
				// regardless of whether a tile level exists
				requestZoomLevel = tileDao
						.getApproximateZoomLevel(distanceWidth, distanceHeight);
			} else {
				// Get the closest existing zoom level
				requestZoomLevel = tileDao.getZoomLevel(distanceWidth,
						distanceHeight);
			}

			// If there is a matching zoom level
			if (requestZoomLevel != null) {

				List<Long> zoomLevels = null;

				// If options are configured, build the possible zoom levels in
				// order to request
				if (scaling != null && scaling.getScalingType() != null) {

					// Find zoom in levels
					List<Long> zoomInLevels = new ArrayList<>();
					if (scaling.isZoomIn()) {
						long zoomIn = scaling.getZoomIn() != null
								? requestZoomLevel + scaling.getZoomIn()
								: tileDao.getMaxZoom();
						for (long zoomLevel = requestZoomLevel
								+ 1; zoomLevel <= zoomIn; zoomLevel++) {
							zoomInLevels.add(zoomLevel);
						}
					}

					// Find zoom out levels
					List<Long> zoomOutLevels = new ArrayList<>();
					if (scaling.isZoomOut()) {
						long zoomOut = scaling.getZoomOut() != null
								? requestZoomLevel - scaling.getZoomOut()
								: tileDao.getMinZoom();
						for (long zoomLevel = requestZoomLevel
								- 1; zoomLevel >= zoomOut; zoomLevel--) {
							zoomOutLevels.add(zoomLevel);
						}
					}

					if (zoomInLevels.isEmpty()) {
						// Only zooming out
						zoomLevels = zoomOutLevels;
					} else if (zoomOutLevels.isEmpty()) {
						// Only zooming in
						zoomLevels = zoomInLevels;
					} else {
						// Determine how to order the zoom in and zoom out
						// levels
						TileScalingType type = scaling.getScalingType();
						switch (type) {
						case IN:
						case IN_OUT:
							// Order zoom in levels before zoom out levels
							zoomLevels = zoomInLevels;
							zoomLevels.addAll(zoomOutLevels);
							break;
						case OUT:
						case OUT_IN:
							// Order zoom out levels before zoom in levels
							zoomLevels = zoomOutLevels;
							zoomLevels.addAll(zoomInLevels);
							break;
						case CLOSEST_IN_OUT:
						case CLOSEST_OUT_IN:
							// Alternate the zoom in and out levels

							List<Long> firstLevels;
							List<Long> secondLevels;
							if (type == TileScalingType.CLOSEST_IN_OUT) {
								// Alternate starting with zoom in
								firstLevels = zoomInLevels;
								secondLevels = zoomOutLevels;
							} else {
								// Alternate starting with zoom out
								firstLevels = zoomOutLevels;
								secondLevels = zoomInLevels;
							}

							zoomLevels = new ArrayList<>();
							int maxLevels = Math.max(firstLevels.size(),
									secondLevels.size());
							for (int i = 0; i < maxLevels; i++) {
								if (i < firstLevels.size()) {
									zoomLevels.add(firstLevels.get(i));
								}
								if (i < secondLevels.size()) {
									zoomLevels.add(secondLevels.get(i));
								}
							}

							break;
						default:
							throw new GeoPackageException("Unsupported "
									+ TileScalingType.class.getSimpleName()
									+ ": " + type);
						}
					}
				} else {
					zoomLevels = new ArrayList<>();
				}

				// Always check the request zoom level first
				zoomLevels.add(0, requestZoomLevel);

				// Build a list of tile matrices that exist for the zoom levels
				for (long zoomLevel : zoomLevels) {
					TileMatrix tileMatrix = tileDao.getTileMatrix(zoomLevel);
					if (tileMatrix != null) {
						tileMatrices.add(tileMatrix);
					}
				}

			}
		}

		return tileMatrices;
	}

	/**
	 * Get the tile row results of tiles needed to draw the requested bounding
	 * box tile
	 *
	 * @param projectedRequestBoundingBox
	 *            bounding box projected to the tiles
	 * @param tileMatrix
	 * @return tile result set or null
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
