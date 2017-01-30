package mil.nga.geopackage.tiles.features;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.extension.index.FeatureTableIndex;
import mil.nga.geopackage.extension.index.GeometryIndex;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.property.GeoPackageJavaProperties;
import mil.nga.geopackage.property.JavaPropertyConstants;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;

import com.j256.ormlite.dao.CloseableIterator;

/**
 * Tiles generated from features
 *
 * @author osbornb
 * @since 1.1.2
 */
public abstract class FeatureTiles {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(FeatureTiles.class
			.getName());

	/**
	 * WGS84 Projection
	 */
	protected static final Projection WGS_84_PROJECTION = ProjectionFactory
			.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

	/**
	 * Web Mercator Projection
	 */
	protected static final Projection WEB_MERCATOR_PROJECTION = ProjectionFactory
			.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);

	/**
	 * Tile data access object
	 */
	protected final FeatureDao featureDao;

	/**
	 * When not null, features are retrieved using a feature index
	 */
	protected FeatureTableIndex featureIndex;

	/**
	 * Tile height
	 */
	protected int tileWidth;

	/**
	 * Tile height
	 */
	protected int tileHeight;

	/**
	 * Compress format
	 */
	protected String compressFormat;

	/**
	 * Point radius
	 */
	protected float pointRadius;

	/**
	 * Point color
	 */
	protected Color pointColor;

	/**
	 * Optional point icon in place of a drawn circle
	 */
	protected FeatureTilePointIcon pointIcon;

	/**
	 * Line stroke width
	 */
	protected float lineStrokeWidth;

	/**
	 * Line color
	 */
	protected Color lineColor;

	/**
	 * Polygon stroke width
	 */
	protected float polygonStrokeWidth;

	/**
	 * Polygon color
	 */
	protected Color polygonColor;

	/**
	 * Fill polygon flag
	 */
	protected boolean fillPolygon;

	/**
	 * Polygon fill color
	 */
	protected Color polygonFillColor;

	/**
	 * Height overlapping pixels between tile images
	 */
	protected float heightOverlap;

	/**
	 * Width overlapping pixels between tile images
	 */
	protected float widthOverlap;

	/**
	 * Optional max features per tile. When more features than this value exist
	 * for creating a single tile, the tile is not created
	 */
	protected Integer maxFeaturesPerTile;

	/**
	 * When not null and the number of features is greater than the max features
	 * per tile, used to draw tiles for those tiles with more features than the
	 * max
	 *
	 * @see CustomFeaturesTile
	 * @see mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile custom
	 *      features tile implementation
	 */
	protected CustomFeaturesTile maxFeaturesTileDraw;

	/**
	 * Constructor
	 *
	 * @param featureDao
	 */
	public FeatureTiles(FeatureDao featureDao) {
		this.featureDao = featureDao;

		tileWidth = GeoPackageJavaProperties.getIntegerProperty(
				JavaPropertyConstants.FEATURE_TILES,
				JavaPropertyConstants.FEATURE_TILES_TILE_WIDTH);
		tileHeight = GeoPackageJavaProperties.getIntegerProperty(
				JavaPropertyConstants.FEATURE_TILES,
				JavaPropertyConstants.FEATURE_TILES_TILE_HEIGHT);

		compressFormat = GeoPackageJavaProperties.getProperty(
				JavaPropertyConstants.FEATURE_TILES,
				JavaPropertyConstants.FEATURE_TILES_COMPRESS_FORMAT);

		pointRadius = GeoPackageJavaProperties.getFloatProperty(
				JavaPropertyConstants.FEATURE_TILES_POINT,
				JavaPropertyConstants.FEATURE_TILES_RADIUS);
		pointColor = GeoPackageJavaProperties.getColorProperty(
				JavaPropertyConstants.FEATURE_TILES_POINT,
				JavaPropertyConstants.FEATURE_TILES_COLOR);

		lineStrokeWidth = GeoPackageJavaProperties.getFloatProperty(
				JavaPropertyConstants.FEATURE_TILES_LINE,
				JavaPropertyConstants.FEATURE_TILES_STROKE_WIDTH);
		lineColor = GeoPackageJavaProperties.getColorProperty(
				JavaPropertyConstants.FEATURE_TILES_LINE,
				JavaPropertyConstants.FEATURE_TILES_COLOR);

		polygonStrokeWidth = GeoPackageJavaProperties.getFloatProperty(
				JavaPropertyConstants.FEATURE_TILES_POLYGON,
				JavaPropertyConstants.FEATURE_TILES_STROKE_WIDTH);
		polygonColor = GeoPackageJavaProperties.getColorProperty(
				JavaPropertyConstants.FEATURE_TILES_POLYGON,
				JavaPropertyConstants.FEATURE_TILES_COLOR);

		fillPolygon = GeoPackageJavaProperties
				.getBooleanProperty(JavaPropertyConstants.FEATURE_TILES_POLYGON_FILL);
		polygonFillColor = GeoPackageJavaProperties.getColorProperty(
				JavaPropertyConstants.FEATURE_TILES_POLYGON_FILL,
				JavaPropertyConstants.FEATURE_TILES_COLOR);

		calculateDrawOverlap();
	}

	/**
	 * Call after making changes to the point icon, point radius, or paint
	 * stroke widths. Determines the pixel overlap between tiles
	 */
	public void calculateDrawOverlap() {
		if (pointIcon != null) {
			heightOverlap = pointIcon.getHeight();
			widthOverlap = pointIcon.getWidth();
		} else {
			heightOverlap = pointRadius;
			widthOverlap = pointRadius;
		}

		float linePaintHalfStroke = lineStrokeWidth / 2.0f;
		heightOverlap = Math.max(heightOverlap, linePaintHalfStroke);
		widthOverlap = Math.max(widthOverlap, linePaintHalfStroke);

		float polygonPaintHalfStroke = polygonStrokeWidth / 2.0f;
		heightOverlap = Math.max(heightOverlap, polygonPaintHalfStroke);
		widthOverlap = Math.max(widthOverlap, polygonPaintHalfStroke);
	}

	/**
	 * Manually set the width and height draw overlap
	 *
	 * @param pixels
	 */
	public void setDrawOverlap(float pixels) {
		setWidthDrawOverlap(pixels);
		setHeightDrawOverlap(pixels);
	}

	/**
	 * Get the width draw overlap
	 *
	 * @return width draw overlap
	 */
	public float getWidthDrawOverlap() {
		return widthOverlap;
	}

	/**
	 * Manually set the width draw overlap
	 *
	 * @param pixels
	 */
	public void setWidthDrawOverlap(float pixels) {
		widthOverlap = pixels;
	}

	/**
	 * Get the height draw overlap
	 *
	 * @return height draw overlap
	 */
	public float getHeightDrawOverlap() {
		return heightOverlap;
	}

	/**
	 * Manually set the height draw overlap
	 *
	 * @param pixels
	 */
	public void setHeightDrawOverlap(float pixels) {
		heightOverlap = pixels;
	}

	/**
	 * Get the feature DAO
	 *
	 * @return feature dao
	 */
	public FeatureDao getFeatureDao() {
		return featureDao;
	}

	/**
	 * Is index query
	 *
	 * @return true if an index query
	 */
	public boolean isIndexQuery() {
		return featureIndex != null && featureIndex.isIndexed();
	}

	/**
	 * Get the feature index
	 *
	 * @return feature index or null
	 */
	public FeatureTableIndex getFeatureIndex() {
		return featureIndex;
	}

	/**
	 * Set the feature index
	 *
	 * @param featureIndex
	 */
	public void setFeatureIndex(FeatureTableIndex featureIndex) {
		this.featureIndex = featureIndex;
	}

	/**
	 * Get the tile width
	 *
	 * @return tile width
	 */
	public int getTileWidth() {
		return tileWidth;
	}

	/**
	 * Set the tile width
	 *
	 * @param tileWidth
	 */
	public void setTileWidth(int tileWidth) {
		this.tileWidth = tileWidth;
	}

	/**
	 * Get the tile height
	 *
	 * @return tile height
	 */
	public int getTileHeight() {
		return tileHeight;
	}

	/**
	 * Set the tile height
	 *
	 * @param tileHeight
	 */
	public void setTileHeight(int tileHeight) {
		this.tileHeight = tileHeight;
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
	 * Set the compress format
	 *
	 * @param compressFormat
	 */
	public void setCompressFormat(String compressFormat) {
		this.compressFormat = compressFormat;
	}

	/**
	 * Get the point radius
	 *
	 * @return radius
	 */
	public float getPointRadius() {
		return pointRadius;
	}

	/**
	 * Set the point radius
	 *
	 * @param pointRadius
	 */
	public void setPointRadius(float pointRadius) {
		this.pointRadius = pointRadius;
	}

	/**
	 * Get point color
	 * 
	 * @return color
	 */
	public Color getPointColor() {
		return pointColor;
	}

	/**
	 * Set point color
	 * 
	 * @param pointColor
	 */
	public void setPointColor(Color pointColor) {
		this.pointColor = pointColor;
	}

	/**
	 * Get the point icon
	 *
	 * @return icon
	 */
	public FeatureTilePointIcon getPointIcon() {
		return pointIcon;
	}

	/**
	 * Set the point icon
	 *
	 * @param pointIcon
	 */
	public void setPointIcon(FeatureTilePointIcon pointIcon) {
		this.pointIcon = pointIcon;
	}

	/**
	 * Get line stroke width
	 * 
	 * @return width
	 */
	public float getLineStrokeWidth() {
		return lineStrokeWidth;
	}

	/**
	 * Set line stroke width
	 * 
	 * @param lineStrokeWidth
	 */
	public void setLineStrokeWidth(float lineStrokeWidth) {
		this.lineStrokeWidth = lineStrokeWidth;
	}

	/**
	 * Get line color
	 * 
	 * @return color
	 */
	public Color getLineColor() {
		return lineColor;
	}

	/**
	 * Set line color
	 * 
	 * @param lineColor
	 */
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	/**
	 * Get polygon stroke width
	 * 
	 * @return width
	 */
	public float getPolygonStrokeWidth() {
		return polygonStrokeWidth;
	}

	/**
	 * Set polygon stroke width
	 * 
	 * @param polygonStrokeWidth
	 */
	public void setPolygonStrokeWidth(float polygonStrokeWidth) {
		this.polygonStrokeWidth = polygonStrokeWidth;
	}

	/**
	 * Get polygon color
	 * 
	 * @return color
	 */
	public Color getPolygonColor() {
		return polygonColor;
	}

	/**
	 * Set polygon color
	 * 
	 * @param polygonColor
	 */
	public void setPolygonColor(Color polygonColor) {
		this.polygonColor = polygonColor;
	}

	/**
	 * Is fill polygon
	 *
	 * @return true if fill polygon
	 */
	public boolean isFillPolygon() {
		return fillPolygon;
	}

	/**
	 * Set the fill polygon
	 *
	 * @param fillPolygon
	 */
	public void setFillPolygon(boolean fillPolygon) {
		this.fillPolygon = fillPolygon;
	}

	/**
	 * Get polygon fill color
	 * 
	 * @return color
	 */
	public Color getPolygonFillColor() {
		return polygonFillColor;
	}

	/**
	 * Set polygon fill color
	 * 
	 * @param polygonFillColor
	 */
	public void setPolygonFillColor(Color polygonFillColor) {
		this.polygonFillColor = polygonFillColor;
	}

	/**
	 * Get the max features per tile
	 *
	 * @return max features per tile or null
	 */
	public Integer getMaxFeaturesPerTile() {
		return maxFeaturesPerTile;
	}

	/**
	 * Set the max features per tile. When more features are returned in a query
	 * to create a single tile, the tile is not created.
	 *
	 * @param maxFeaturesPerTile
	 */
	public void setMaxFeaturesPerTile(Integer maxFeaturesPerTile) {
		this.maxFeaturesPerTile = maxFeaturesPerTile;
	}

	/**
	 * Get the max features tile draw, the custom tile drawing implementation
	 * for tiles with more features than the max at #getMaxFeaturesPerTile
	 *
	 * @return max features tile draw or null
	 * @see CustomFeaturesTile
	 * @see mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile custom
	 *      features tile implementation
	 */
	public CustomFeaturesTile getMaxFeaturesTileDraw() {
		return maxFeaturesTileDraw;
	}

	/**
	 * Set the max features tile draw, used to draw tiles when more features for
	 * a single tile than the max at #getMaxFeaturesPerTile exist
	 *
	 * @param maxFeaturesTileDraw
	 * @see CustomFeaturesTile
	 * @see mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile custom
	 *      features tile implementation
	 */
	public void setMaxFeaturesTileDraw(CustomFeaturesTile maxFeaturesTileDraw) {
		this.maxFeaturesTileDraw = maxFeaturesTileDraw;
	}

	/**
	 * Draw the tile and get the bytes from the x, y, and zoom level
	 *
	 * @param x
	 * @param y
	 * @param zoom
	 * @return tile bytes, or null
	 */
	public byte[] drawTileBytes(int x, int y, int zoom) {

		BufferedImage image = drawTile(x, y, zoom);

		byte[] tileData = null;

		// Convert the image to bytes
		if (image != null) {
			try {
				tileData = ImageUtils.writeImageToBytes(image, compressFormat);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Failed to create tile. x: " + x
						+ ", y: " + y + ", zoom: " + zoom, e);
			}
		}

		return tileData;
	}

	/**
	 * Draw a tile image from the x, y, and zoom level
	 *
	 * @param x
	 * @param y
	 * @param zoom
	 * @return tile image, or null
	 */
	public BufferedImage drawTile(int x, int y, int zoom) {
		BufferedImage image;
		if (isIndexQuery()) {
			image = drawTileQueryIndex(x, y, zoom);
		} else {
			image = drawTileQueryAll(x, y, zoom);
		}
		return image;
	}

	/**
	 * Draw a tile image from the x, y, and zoom level by querying features in
	 * the tile location
	 *
	 * @param x
	 * @param y
	 * @param zoom
	 * @return drawn image, or null
	 */
	public BufferedImage drawTileQueryIndex(int x, int y, int zoom) {

		// Get the web mercator bounding box
		BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(x, y, zoom);

		BufferedImage image = null;

		// Query for geometries matching the bounds in the index
		CloseableIterator<GeometryIndex> results = queryIndexedFeatures(webMercatorBoundingBox);

		try {

			Long tileCount = null;
			if (maxFeaturesPerTile != null) {
				tileCount = queryIndexedFeaturesCount(webMercatorBoundingBox);
			}

			if (maxFeaturesPerTile == null
					|| tileCount <= maxFeaturesPerTile.longValue()) {

				// Draw the tile bitmap
				image = drawTile(webMercatorBoundingBox, results);

			} else if (maxFeaturesTileDraw != null) {

				// Draw the max features tile
				image = maxFeaturesTileDraw.drawTile(tileWidth, tileHeight,
						tileCount, results);
			}
		} finally {
			try {
				results.close();
			} catch (IOException e) {
				LOGGER.log(Level.WARNING,
						"Failed to close result set for query on x: " + x
								+ ", y: " + y + ", zoom: " + zoom, e);
			}
		}

		return image;
	}

	/**
	 * Query for feature result count in the x, y, and zoom
	 *
	 * @param x
	 * @param y
	 * @param zoom
	 * @return feature count
	 */
	public long queryIndexedFeaturesCount(int x, int y, int zoom) {

		// Get the web mercator bounding box
		BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(x, y, zoom);

		// Query for the count of geometries matching the bounds in the index
		long count = queryIndexedFeaturesCount(webMercatorBoundingBox);

		return count;
	}

	/**
	 * Query for feature result count in the bounding box
	 * 
	 * @param webMercatorBoundingBox
	 * @return count
	 */
	public long queryIndexedFeaturesCount(BoundingBox webMercatorBoundingBox) {

		// Create an expanded bounding box to handle features outside the tile
		// that overlap
		BoundingBox expandedQueryBoundingBox = expandBoundingBox(webMercatorBoundingBox);

		// Query for the count of geometries matching the bounds in the index
		long count = featureIndex.count(expandedQueryBoundingBox,
				WEB_MERCATOR_PROJECTION);

		return count;
	}

	/**
	 * Query for feature results in the bounding box
	 *
	 * @param webMercatorBoundingBox
	 * @return geometry index results
	 */
	public CloseableIterator<GeometryIndex> queryIndexedFeatures(
			BoundingBox webMercatorBoundingBox) {

		// Create an expanded bounding box to handle features outside the tile
		// that overlap
		BoundingBox expandedQueryBoundingBox = expandBoundingBox(webMercatorBoundingBox);

		// Query for geometries matching the bounds in the index
		CloseableIterator<GeometryIndex> results = featureIndex.query(
				expandedQueryBoundingBox, WEB_MERCATOR_PROJECTION);

		return results;
	}

	/**
	 * Create an expanded bounding box to handle features outside the tile that
	 * overlap
	 * 
	 * @param webMercatorBoundingBox
	 * @return
	 */
	private BoundingBox expandBoundingBox(BoundingBox webMercatorBoundingBox) {

		// Create an expanded bounding box to handle features outside the tile
		// that overlap
		double minLongitude = TileBoundingBoxUtils.getLongitudeFromPixel(
				tileWidth, webMercatorBoundingBox, 0 - widthOverlap);
		double maxLongitude = TileBoundingBoxUtils.getLongitudeFromPixel(
				tileWidth, webMercatorBoundingBox, tileWidth + widthOverlap);
		double maxLatitude = TileBoundingBoxUtils.getLatitudeFromPixel(
				tileHeight, webMercatorBoundingBox, 0 - heightOverlap);
		double minLatitude = TileBoundingBoxUtils.getLatitudeFromPixel(
				tileHeight, webMercatorBoundingBox, tileHeight + heightOverlap);
		BoundingBox expandedQueryBoundingBox = new BoundingBox(minLongitude,
				maxLongitude, minLatitude, maxLatitude);

		return expandedQueryBoundingBox;
	}

	/**
	 * Draw a tile image from the x, y, and zoom level by querying all features.
	 * This could be very slow if there are a lot of features
	 *
	 * @param x
	 * @param y
	 * @param zoom
	 * @return drawn image, or null
	 */
	public BufferedImage drawTileQueryAll(int x, int y, int zoom) {

		BoundingBox boundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(x, y, zoom);

		BufferedImage image = null;

		// Query for all features
		FeatureResultSet resultSet = featureDao.queryForAll();

		try {

			Integer totalCount = null;
			if (maxFeaturesPerTile != null) {
				totalCount = resultSet.getCount();
			}

			if (maxFeaturesPerTile == null || totalCount <= maxFeaturesPerTile) {

				// Draw the tile bitmap
				image = drawTile(boundingBox, resultSet);

			} else if (maxFeaturesTileDraw != null) {

				// Draw the unindexed max features tile
				image = maxFeaturesTileDraw.drawUnindexedTile(tileWidth,
						tileHeight, totalCount, resultSet);
			}
		} finally {
			resultSet.close();
		}

		return image;
	}

	/**
	 * Create a new empty image
	 *
	 * @return image
	 */
	protected BufferedImage createNewImage() {
		return new BufferedImage(tileWidth, tileHeight,
				BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Create a projection transformation from the feature dao projection to Web
	 * Mercator
	 *
	 * @return transform
	 */
	protected ProjectionTransform getWebMercatorTransform() {
		return this.featureDao.getProjection().getTransformation(
				ProjectionConstants.EPSG_WEB_MERCATOR);
	}

	/**
	 * Draw a tile image from geometry index results
	 *
	 * @param webMercatorBoundingBox
	 * @param results
	 * @return image
	 */
	public abstract BufferedImage drawTile(BoundingBox webMercatorBoundingBox,
			CloseableIterator<GeometryIndex> results);

	/**
	 * Draw a tile image from feature geometries in the provided cursor
	 *
	 * @param webMercatorBoundingBox
	 * @param resultSet
	 * @return image
	 */
	public abstract BufferedImage drawTile(BoundingBox webMercatorBoundingBox,
			FeatureResultSet resultSet);

	/**
	 * Draw a tile image from the feature rows
	 *
	 * @param webMercatorBoundingBox
	 * @param featureRow
	 * @return image
	 */
	public abstract BufferedImage drawTile(BoundingBox webMercatorBoundingBox,
			List<FeatureRow> featureRow);

}
