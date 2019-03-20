package mil.nga.geopackage.tiles.features;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.index.FeatureTableIndex;
import mil.nga.geopackage.extension.index.GeometryIndex;
import mil.nga.geopackage.extension.style.FeatureStyle;
import mil.nga.geopackage.extension.style.FeatureTableStyles;
import mil.nga.geopackage.extension.style.IconCache;
import mil.nga.geopackage.extension.style.IconDao;
import mil.nga.geopackage.extension.style.IconRow;
import mil.nga.geopackage.extension.style.StyleDao;
import mil.nga.geopackage.extension.style.StyleRow;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.property.GeoPackageJavaProperties;
import mil.nga.geopackage.property.JavaPropertyConstants;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileUtils;
import mil.nga.sf.GeometryType;
import mil.nga.sf.Point;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;
import mil.nga.sf.proj.ProjectionTransform;
import mil.nga.sf.util.GeometryUtils;

import org.locationtech.proj4j.units.Units;

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
	 * Feature DAO Projection
	 */
	protected Projection projection;

	/**
	 * When not null, features are retrieved using a feature index
	 */
	protected FeatureTableIndex featureIndex;

	/**
	 * Feature Style extension
	 */
	protected FeatureTableStyles featureTableStyles;

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
	 * Point paint
	 */
	protected Paint pointPaint = new Paint();

	/**
	 * Optional point icon in place of a drawn circle
	 */
	protected FeatureTilePointIcon pointIcon;

	/**
	 * Line paint
	 */
	protected Paint linePaint = new Paint();

	/**
	 * Line stroke width
	 */
	protected float lineStrokeWidth;

	/**
	 * Polygon paint
	 */
	protected Paint polygonPaint = new Paint();

	/**
	 * Polygon stroke width
	 */
	protected float polygonStrokeWidth;

	/**
	 * Fill polygon flag
	 */
	protected boolean fillPolygon;

	/**
	 * Polygon fill paint
	 */
	protected Paint polygonFillPaint = new Paint();

	/**
	 * Feature paint cache
	 */
	private FeaturePaintCache featurePaintCache = new FeaturePaintCache();

	/**
	 * Icon Cache
	 */
	private IconCache iconCache = new IconCache();

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
	 * When true, geometries are simplified before being drawn. Default is true
	 */
	protected boolean simplifyGeometries = true;

	/**
	 * Scale factor
	 */
	protected float scale = 1.0f;

	/**
	 * Constructor
	 *
	 * @param featureDao
	 *            feature dao
	 */
	public FeatureTiles(FeatureDao featureDao) {
		this(null, featureDao);
	}

	/**
	 * Constructor
	 *
	 * @param featureDao
	 *            feature dao
	 * @param scale
	 *            scale factor
	 * @since 3.2.0
	 */
	public FeatureTiles(FeatureDao featureDao, float scale) {
		this(null, featureDao, scale);
	}

	/**
	 * Constructor
	 *
	 * @param featureDao
	 *            feature dao
	 * @param width
	 *            drawn tile width
	 * @param height
	 *            drawn tile height
	 * @since 3.2.0
	 */
	public FeatureTiles(FeatureDao featureDao, int width, int height) {
		this(null, featureDao, width, height);
	}

	/**
	 * Constructor, auto creates the index manager for indexed tables and
	 * feature styles for styled tables
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param featureDao
	 *            feature dao
	 * @since 3.2.0
	 */
	public FeatureTiles(GeoPackage geoPackage, FeatureDao featureDao) {
		this(geoPackage, featureDao, TileUtils.TILE_PIXELS_HIGH,
				TileUtils.TILE_PIXELS_HIGH);
	}

	/**
	 * Constructor, auto creates the index manager for indexed tables and
	 * feature styles for styled tables
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param featureDao
	 *            feature dao
	 * @param scale
	 *            scale factor
	 * @since 3.2.0
	 */
	public FeatureTiles(GeoPackage geoPackage, FeatureDao featureDao,
			float scale) {
		this(geoPackage, featureDao, scale, TileUtils.tileLength(scale),
				TileUtils.tileLength(scale));
	}

	/**
	 * Constructor, auto creates the index manager for indexed tables and
	 * feature styles for styled tables
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param featureDao
	 *            feature dao
	 * @param width
	 *            drawn tile width
	 * @param height
	 *            drawn tile height
	 * @since 3.2.0
	 */
	public FeatureTiles(GeoPackage geoPackage, FeatureDao featureDao,
			int width, int height) {
		this(geoPackage, featureDao, TileUtils.tileScale(width, height), width,
				height);
	}

	/**
	 * Constructor, auto creates the index manager for indexed tables and
	 * feature styles for styled tables
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param featureDao
	 *            feature dao
	 * @param scale
	 *            scale factor
	 * @param width
	 *            drawn tile width
	 * @param height
	 *            drawn tile height
	 * @since 3.2.0
	 */
	public FeatureTiles(GeoPackage geoPackage, FeatureDao featureDao,
			float scale, int width, int height) {

		this.featureDao = featureDao;
		if (featureDao != null) {
			this.projection = featureDao.getProjection();
		}

		this.scale = scale;

		tileWidth = width;
		tileHeight = height;

		compressFormat = GeoPackageJavaProperties.getProperty(
				JavaPropertyConstants.FEATURE_TILES,
				JavaPropertyConstants.FEATURE_TILES_COMPRESS_FORMAT);

		pointRadius = GeoPackageJavaProperties.getFloatProperty(
				JavaPropertyConstants.FEATURE_TILES_POINT,
				JavaPropertyConstants.FEATURE_TILES_RADIUS);
		pointPaint.setColor(GeoPackageJavaProperties.getColorProperty(
				JavaPropertyConstants.FEATURE_TILES_POINT,
				JavaPropertyConstants.FEATURE_TILES_COLOR));

		lineStrokeWidth = GeoPackageJavaProperties.getFloatProperty(
				JavaPropertyConstants.FEATURE_TILES_LINE,
				JavaPropertyConstants.FEATURE_TILES_STROKE_WIDTH);
		linePaint.setStrokeWidth(this.scale * lineStrokeWidth);
		linePaint.setColor(GeoPackageJavaProperties.getColorProperty(
				JavaPropertyConstants.FEATURE_TILES_LINE,
				JavaPropertyConstants.FEATURE_TILES_COLOR));

		polygonStrokeWidth = GeoPackageJavaProperties.getFloatProperty(
				JavaPropertyConstants.FEATURE_TILES_POLYGON,
				JavaPropertyConstants.FEATURE_TILES_STROKE_WIDTH);
		polygonPaint.setStrokeWidth(this.scale * polygonStrokeWidth);
		polygonPaint.setColor(GeoPackageJavaProperties.getColorProperty(
				JavaPropertyConstants.FEATURE_TILES_POLYGON,
				JavaPropertyConstants.FEATURE_TILES_COLOR));

		fillPolygon = GeoPackageJavaProperties
				.getBooleanProperty(JavaPropertyConstants.FEATURE_TILES_POLYGON_FILL);
		polygonFillPaint.setColor(GeoPackageJavaProperties.getColorProperty(
				JavaPropertyConstants.FEATURE_TILES_POLYGON_FILL,
				JavaPropertyConstants.FEATURE_TILES_COLOR));

		if (geoPackage != null) {

			featureIndex = new FeatureTableIndex(geoPackage, featureDao);
			if (!featureIndex.isIndexed()) {
				featureIndex.close();
				featureIndex = null;
			}

			featureTableStyles = new FeatureTableStyles(geoPackage,
					featureDao.getTable());
			if (!featureTableStyles.has()) {
				featureTableStyles = null;
			}

		}

		calculateDrawOverlap();
	}

	/**
	 * Call after making changes to the point icon, point radius, or paint
	 * stroke widths. Determines the pixel overlap between tiles
	 */
	public void calculateDrawOverlap() {

		if (pointIcon != null) {
			heightOverlap = this.scale * pointIcon.getHeight();
			widthOverlap = this.scale * pointIcon.getWidth();
		} else {
			heightOverlap = this.scale * pointRadius;
			widthOverlap = this.scale * pointRadius;
		}

		float linePaintHalfStroke = this.scale * lineStrokeWidth / 2.0f;
		heightOverlap = Math.max(heightOverlap, linePaintHalfStroke);
		widthOverlap = Math.max(widthOverlap, linePaintHalfStroke);

		float polygonPaintHalfStroke = this.scale * polygonStrokeWidth / 2.0f;
		heightOverlap = Math.max(heightOverlap, polygonPaintHalfStroke);
		widthOverlap = Math.max(widthOverlap, polygonPaintHalfStroke);

		if (featureTableStyles != null && featureTableStyles.has()) {

			// Style Rows
			Set<Long> styleRowIds = new HashSet<>();
			List<Long> tableStyleIds = featureTableStyles.getAllTableStyleIds();
			if (tableStyleIds != null) {
				styleRowIds.addAll(tableStyleIds);
			}
			List<Long> styleIds = featureTableStyles.getAllStyleIds();
			if (styleIds != null) {
				styleRowIds.addAll(styleIds);
			}

			StyleDao styleDao = featureTableStyles.getStyleDao();
			for (long styleRowId : styleRowIds) {
				StyleRow styleRow = styleDao.getRow(styleDao
						.queryForIdRow(styleRowId));
				float styleHalfWidth = this.scale
						* (float) (styleRow.getWidthOrDefault() / 2.0f);
				widthOverlap = Math.max(widthOverlap, styleHalfWidth);
				heightOverlap = Math.max(heightOverlap, styleHalfWidth);
			}

			// Icon Rows
			Set<Long> iconRowIds = new HashSet<>();
			List<Long> tableIconIds = featureTableStyles.getAllTableIconIds();
			if (tableIconIds != null) {
				iconRowIds.addAll(tableIconIds);
			}
			List<Long> iconIds = featureTableStyles.getAllIconIds();
			if (iconIds != null) {
				iconRowIds.addAll(iconIds);
			}

			IconDao iconDao = featureTableStyles.getIconDao();
			for (long iconRowId : iconRowIds) {
				IconRow iconRow = iconDao.getRow(iconDao
						.queryForIdRow(iconRowId));
				double[] iconDimensions = iconRow.getDerivedDimensions();
				float iconWidth = this.scale
						* (float) Math.ceil(iconDimensions[0]);
				float iconHeight = this.scale
						* (float) Math.ceil(iconDimensions[1]);
				widthOverlap = Math.max(widthOverlap, iconWidth);
				heightOverlap = Math.max(heightOverlap, iconHeight);
			}

		}

	}

	/**
	 * Set the scale
	 * 
	 * @param scale
	 *            scale factor
	 * @since 3.2.0
	 */
	public void setScale(float scale) {
		this.scale = scale;
		linePaint.setStrokeWidth(scale * lineStrokeWidth);
		polygonPaint.setStrokeWidth(scale * polygonStrokeWidth);
		featurePaintCache.clear();
	}

	/**
	 * Get the scale
	 * 
	 * @return scale factor
	 * @since 3.2.0
	 */
	public float getScale() {
		return scale;
	}

	/**
	 * Manually set the width and height draw overlap
	 *
	 * @param pixels
	 *            pixels
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
	 *            pixels
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
	 *            pixels
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
	 *            feature index
	 */
	public void setFeatureIndex(FeatureTableIndex featureIndex) {
		this.featureIndex = featureIndex;
	}

	/**
	 * Get the feature table styles
	 *
	 * @return feature table styles
	 * @since 3.2.0
	 */
	public FeatureTableStyles getFeatureTableStyles() {
		return featureTableStyles;
	}

	/**
	 * Set the feature table styles
	 *
	 * @param featureTableStyles
	 *            feature table styles
	 * @since 3.2.0
	 */
	public void setFeatureTableStyles(FeatureTableStyles featureTableStyles) {
		this.featureTableStyles = featureTableStyles;
	}

	/**
	 * Ignore the feature table styles within the GeoPackage
	 * 
	 * @since 3.2.0
	 */
	public void ignoreFeatureTableStyles() {
		setFeatureTableStyles(null);
		calculateDrawOverlap();
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
	 *            tile width
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
	 *            tile height
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
	 *            compress format
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
	 *            point radius
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
		return pointPaint.getColor();
	}

	/**
	 * Set point color
	 * 
	 * @param pointColor
	 *            point color
	 */
	public void setPointColor(Color pointColor) {
		pointPaint.setColor(pointColor);
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
	 *            point icon
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
	 *            line stroke width
	 */
	public void setLineStrokeWidth(float lineStrokeWidth) {
		this.lineStrokeWidth = lineStrokeWidth;
		linePaint.setStrokeWidth(this.scale * lineStrokeWidth);
	}

	/**
	 * Get line color
	 * 
	 * @return color
	 */
	public Color getLineColor() {
		return linePaint.getColor();
	}

	/**
	 * Set line color
	 * 
	 * @param lineColor
	 *            line color
	 */
	public void setLineColor(Color lineColor) {
		linePaint.setColor(lineColor);
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
	 *            polygon stroke width
	 */
	public void setPolygonStrokeWidth(float polygonStrokeWidth) {
		this.polygonStrokeWidth = polygonStrokeWidth;
		polygonPaint.setStrokeWidth(this.scale * polygonStrokeWidth);
	}

	/**
	 * Get polygon color
	 * 
	 * @return color
	 */
	public Color getPolygonColor() {
		return polygonPaint.getColor();
	}

	/**
	 * Set polygon color
	 * 
	 * @param polygonColor
	 *            polygon color
	 */
	public void setPolygonColor(Color polygonColor) {
		polygonPaint.setColor(polygonColor);
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
	 *            fill polygon
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
		return polygonFillPaint.getColor();
	}

	/**
	 * Set polygon fill color
	 * 
	 * @param polygonFillColor
	 *            polygon fill color
	 */
	public void setPolygonFillColor(Color polygonFillColor) {
		polygonFillPaint.setColor(polygonFillColor);
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
	 *            max features per tile
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
	 *            max features tile draw
	 * @see CustomFeaturesTile
	 * @see mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile custom
	 *      features tile implementation
	 */
	public void setMaxFeaturesTileDraw(CustomFeaturesTile maxFeaturesTileDraw) {
		this.maxFeaturesTileDraw = maxFeaturesTileDraw;
	}

	/**
	 * Is the simplify geometries flag set? Default is true
	 * 
	 * @return simplify geometries flag
	 * @since 2.0.0
	 */
	public boolean isSimplifyGeometries() {
		return simplifyGeometries;
	}

	/**
	 * Set the simplify geometries flag
	 * 
	 * @param simplifyGeometries
	 *            simplify geometries flag
	 * @since 2.0.0
	 */
	public void setSimplifyGeometries(boolean simplifyGeometries) {
		this.simplifyGeometries = simplifyGeometries;
	}

	/**
	 * Draw the tile and get the bytes from the x, y, and zoom level
	 *
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param zoom
	 *            zoom level
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
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param zoom
	 *            zoom level
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
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param zoom
	 *            zoom level
	 * @return drawn image, or null
	 */
	public BufferedImage drawTileQueryIndex(int x, int y, int zoom) {

		// Get the web mercator bounding box
		BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(x, y, zoom);

		BufferedImage image = null;

		// Query for the geometry count matching the bounds in the index
		long tileCount = queryIndexedFeaturesCount(webMercatorBoundingBox);

		// Draw if at least one geometry exists
		if (tileCount > 0) {

			// Query for geometries matching the bounds in the index
			CloseableIterator<GeometryIndex> results = queryIndexedFeatures(webMercatorBoundingBox);

			try {

				if (maxFeaturesPerTile == null
						|| tileCount <= maxFeaturesPerTile.longValue()) {

					// Draw the tile image
					image = drawTile(zoom, webMercatorBoundingBox, results);

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
		}

		return image;
	}

	/**
	 * Query for feature result count in the x, y, and zoom
	 *
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param zoom
	 *            zoom level
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
	 *            web mercator bounding box
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
	 * Query for feature results in the x, y, and zoom
	 *
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param zoom
	 *            zoom level
	 * @return feature count
	 * @since 3.2.0
	 */
	public CloseableIterator<GeometryIndex> queryIndexedFeatures(int x, int y,
			int zoom) {

		// Get the web mercator bounding box
		BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(x, y, zoom);

		// Query for the geometries matching the bounds in the index
		return queryIndexedFeatures(webMercatorBoundingBox);
	}

	/**
	 * Query for feature results in the bounding box
	 *
	 * @param webMercatorBoundingBox
	 *            web mercator bounding box
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
	 * @param boundingBox
	 *            bounding box
	 * @param projection
	 *            bounding box projection
	 * @return bounding box
	 * @since 3.2.0
	 */
	public BoundingBox expandBoundingBox(BoundingBox boundingBox,
			Projection projection) {

		BoundingBox expandedBoundingBox = boundingBox;

		ProjectionTransform toWebMercator = projection
				.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR);
		if (!toWebMercator.isSameProjection()) {
			expandedBoundingBox = expandedBoundingBox.transform(toWebMercator);
		}

		expandedBoundingBox = expandBoundingBox(expandedBoundingBox);

		if (!toWebMercator.isSameProjection()) {
			ProjectionTransform fromWebMercator = toWebMercator
					.getInverseTransformation();
			expandedBoundingBox = expandedBoundingBox
					.transform(fromWebMercator);
		}

		return expandedBoundingBox;
	}

	/**
	 * Create an expanded bounding box to handle features outside the tile that
	 * overlap
	 *
	 * @param webMercatorBoundingBox
	 *            web mercator bounding box
	 * @return bounding box
	 * @since 3.2.0
	 */
	public BoundingBox expandBoundingBox(BoundingBox webMercatorBoundingBox) {
		return expandBoundingBox(webMercatorBoundingBox, webMercatorBoundingBox);
	}

	/**
	 * Create an expanded bounding box to handle features outside the tile that
	 * overlap
	 *
	 * @param webMercatorBoundingBox
	 *            web mercator bounding box
	 * @param tileWebMercatorBoundingBox
	 *            tile web mercator bounding box
	 * @return bounding box
	 * @since 3.2.0
	 */
	public BoundingBox expandBoundingBox(BoundingBox webMercatorBoundingBox,
			BoundingBox tileWebMercatorBoundingBox) {

		// Create an expanded bounding box to handle features outside the tile
		// that overlap
		double minLongitude = TileBoundingBoxUtils.getLongitudeFromPixel(
				tileWidth, webMercatorBoundingBox, tileWebMercatorBoundingBox,
				0 - widthOverlap);
		double maxLongitude = TileBoundingBoxUtils.getLongitudeFromPixel(
				tileWidth, webMercatorBoundingBox, tileWebMercatorBoundingBox,
				tileWidth + widthOverlap);
		double maxLatitude = TileBoundingBoxUtils.getLatitudeFromPixel(
				tileHeight, webMercatorBoundingBox, tileWebMercatorBoundingBox,
				0 - heightOverlap);
		double minLatitude = TileBoundingBoxUtils.getLatitudeFromPixel(
				tileHeight, webMercatorBoundingBox, tileWebMercatorBoundingBox,
				tileHeight + heightOverlap);

		// Choose the most expanded longitudes and latitudes
		minLongitude = Math.min(minLongitude,
				webMercatorBoundingBox.getMinLongitude());
		maxLongitude = Math.max(maxLongitude,
				webMercatorBoundingBox.getMaxLongitude());
		minLatitude = Math.min(minLatitude,
				webMercatorBoundingBox.getMinLatitude());
		maxLatitude = Math.max(maxLatitude,
				webMercatorBoundingBox.getMaxLatitude());

		// Bound with the web mercator limits
		minLongitude = Math.max(minLongitude, -1
				* ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH);
		maxLongitude = Math.min(maxLongitude,
				ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH);
		minLatitude = Math.max(minLatitude, -1
				* ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH);
		maxLatitude = Math.min(maxLatitude,
				ProjectionConstants.WEB_MERCATOR_HALF_WORLD_WIDTH);

		BoundingBox expandedBoundingBox = new BoundingBox(minLongitude,
				minLatitude, maxLongitude, maxLatitude);

		return expandedBoundingBox;
	}

	/**
	 * Draw a tile image from the x, y, and zoom level by querying all features.
	 * This could be very slow if there are a lot of features
	 *
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param zoom
	 *            zoom level
	 * @return drawn image, or null
	 */
	public BufferedImage drawTileQueryAll(int x, int y, int zoom) {

		BoundingBox boundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(x, y, zoom);

		BufferedImage image = null;

		// Query for all features
		FeatureResultSet resultSet = featureDao.queryForAll();

		try {

			int totalCount = resultSet.getCount();

			// Draw if at least one geometry exists
			if (totalCount > 0) {

				if (maxFeaturesPerTile == null
						|| totalCount <= maxFeaturesPerTile) {

					// Draw the tile image
					image = drawTile(zoom, boundingBox, resultSet);

				} else if (maxFeaturesTileDraw != null) {

					// Draw the unindexed max features tile
					image = maxFeaturesTileDraw.drawUnindexedTile(tileWidth,
							tileHeight, totalCount, resultSet);
				}

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
	 * Get a graphics for the image
	 * 
	 * @param image
	 *            buffered image
	 * @return graphics
	 */
	protected Graphics2D getGraphics(BufferedImage image) {
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		return graphics;
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
	 * When the simplify tolerance is set, simplify the points to a similar
	 * curve with fewer points.
	 * 
	 * @param simplifyTolerance
	 *            simplify tolerance in meters
	 * @param points
	 *            ordered points
	 * @return simplified points
	 * @since 2.0.0
	 */
	protected List<Point> simplifyPoints(double simplifyTolerance,
			List<Point> points) {

		List<Point> simplifiedPoints = null;
		if (simplifyGeometries) {

			// Reproject to web mercator if not in meters
			if (projection != null && !projection.isUnit(Units.METRES)) {
				ProjectionTransform toWebMercator = projection
						.getTransformation(WEB_MERCATOR_PROJECTION);
				points = toWebMercator.transform(points);
			}

			// Simplify the points
			simplifiedPoints = GeometryUtils.simplifyPoints(points,
					simplifyTolerance);

			// Reproject back to the original projection
			if (projection != null && !projection.isUnit(Units.METRES)) {
				ProjectionTransform fromWebMercator = WEB_MERCATOR_PROJECTION
						.getTransformation(projection);
				simplifiedPoints = fromWebMercator.transform(simplifiedPoints);
			}
		} else {
			simplifiedPoints = points;
		}

		return simplifiedPoints;
	}

	/**
	 * Get the feature style for the feature row and geometry type
	 *
	 * @param featureRow
	 *            feature row
	 * @return feature style
	 */
	protected FeatureStyle getFeatureStyle(FeatureRow featureRow) {
		FeatureStyle featureStyle = null;
		if (featureTableStyles != null) {
			featureStyle = featureTableStyles.getFeatureStyle(featureRow);
		}
		return featureStyle;
	}

	/**
	 * Get the feature style for the feature row and geometry type
	 *
	 * @param featureRow
	 *            feature row
	 * @param geometryType
	 *            geometry type
	 * @return feature style
	 */
	protected FeatureStyle getFeatureStyle(FeatureRow featureRow,
			GeometryType geometryType) {
		FeatureStyle featureStyle = null;
		if (featureTableStyles != null) {
			featureStyle = featureTableStyles.getFeatureStyle(featureRow,
					geometryType);
		}
		return featureStyle;
	}

	/**
	 * Get the icon image from the icon row
	 *
	 * @param iconRow
	 *            icon row
	 * @return icon image
	 */
	protected BufferedImage getIcon(IconRow iconRow) {
		return iconCache.createIcon(iconRow, scale);
	}

	/**
	 * Get the point paint for the feature style, or return the default paint
	 *
	 * @param featureStyle
	 *            feature style
	 * @return paint
	 */
	protected Paint getPointPaint(FeatureStyle featureStyle) {

		Paint paint = getFeatureStylePaint(featureStyle, FeatureDrawType.CIRCLE);

		if (paint == null) {
			paint = pointPaint;
		}

		return paint;
	}

	/**
	 * Get the line paint for the feature style, or return the default paint
	 *
	 * @param featureStyle
	 *            feature style
	 * @return paint
	 */
	protected Paint getLinePaint(FeatureStyle featureStyle) {

		Paint paint = getFeatureStylePaint(featureStyle, FeatureDrawType.STROKE);

		if (paint == null) {
			paint = linePaint;
		}

		return paint;
	}

	/**
	 * Get the polygon paint for the feature style, or return the default paint
	 *
	 * @param featureStyle
	 *            feature style
	 * @return paint
	 */
	protected Paint getPolygonPaint(FeatureStyle featureStyle) {

		Paint paint = getFeatureStylePaint(featureStyle, FeatureDrawType.STROKE);

		if (paint == null) {
			paint = polygonPaint;
		}

		return paint;
	}

	/**
	 * Get the polygon fill paint for the feature style, or return the default
	 * paint
	 *
	 * @param featureStyle
	 *            feature style
	 * @return paint
	 */
	protected Paint getPolygonFillPaint(FeatureStyle featureStyle) {

		Paint paint = null;

		boolean hasStyleColor = false;

		if (featureStyle != null) {

			StyleRow style = featureStyle.getStyle();

			if (style != null) {

				if (style.hasFillColor()) {
					paint = getStylePaint(style, FeatureDrawType.FILL);
				} else {
					hasStyleColor = style.hasColor();
				}

			}

		}

		if (paint == null && !hasStyleColor && fillPolygon) {
			paint = polygonFillPaint;
		}

		return paint;
	}

	/**
	 * Get the feature style paint from cache, or create and cache it
	 *
	 * @param featureStyle
	 *            feature style
	 * @param drawType
	 *            draw type
	 * @return feature style paint
	 */
	private Paint getFeatureStylePaint(FeatureStyle featureStyle,
			FeatureDrawType drawType) {

		Paint paint = null;

		if (featureStyle != null) {

			StyleRow style = featureStyle.getStyle();

			if (style != null && style.hasColor()) {

				paint = getStylePaint(style, drawType);

			}
		}

		return paint;
	}

	/**
	 * Get the style paint from cache, or create and cache it
	 *
	 * @param style
	 *            style row
	 * @param drawType
	 *            draw type
	 * @return paint
	 */
	private Paint getStylePaint(StyleRow style, FeatureDrawType drawType) {

		Paint paint = featurePaintCache.getPaint(style, drawType);

		if (paint == null) {

			mil.nga.geopackage.style.Color color = null;
			Float strokeWidth = null;

			switch (drawType) {
			case CIRCLE:
				color = style.getColorOrDefault();
				break;
			case STROKE:
				color = style.getColorOrDefault();
				strokeWidth = this.scale * (float) style.getWidthOrDefault();
				break;
			case FILL:
				color = style.getFillColor();
				strokeWidth = this.scale * (float) style.getWidthOrDefault();
				break;
			default:
				throw new GeoPackageException("Unsupported Draw Type: "
						+ drawType);
			}

			Paint stylePaint = new Paint();
			stylePaint.setColor(new Color(color.getColorWithAlpha(), true));
			if (strokeWidth != null) {
				stylePaint.setStrokeWidth(strokeWidth);
			}

			synchronized (featurePaintCache) {

				paint = featurePaintCache.getPaint(style, drawType);

				if (paint == null) {
					featurePaintCache.setPaint(style, drawType, stylePaint);
					paint = stylePaint;
				}

			}
		}

		return paint;
	}

	/**
	 * Draw a tile image from geometry index results
	 *
	 * @param zoom
	 *            zoom level
	 * @param webMercatorBoundingBox
	 *            web mercator bounding box
	 * @param results
	 *            results
	 * @return image
	 * @since 2.0.0
	 */
	public abstract BufferedImage drawTile(int zoom,
			BoundingBox webMercatorBoundingBox,
			CloseableIterator<GeometryIndex> results);

	/**
	 * Draw a tile image from feature geometries in the provided cursor
	 *
	 * @param zoom
	 *            zoom level
	 * @param webMercatorBoundingBox
	 *            web mercator bounding box
	 * @param resultSet
	 *            feature result set
	 * @return image
	 * @since 2.0.0
	 */
	public abstract BufferedImage drawTile(int zoom,
			BoundingBox webMercatorBoundingBox, FeatureResultSet resultSet);

	/**
	 * Draw a tile image from the feature rows
	 *
	 * @param zoom
	 *            zoom level
	 * @param webMercatorBoundingBox
	 *            web mercator bounding box
	 * @param featureRow
	 *            feature row
	 * @return image
	 * @since 2.0.0
	 */
	public abstract BufferedImage drawTile(int zoom,
			BoundingBox webMercatorBoundingBox, List<FeatureRow> featureRow);

}
