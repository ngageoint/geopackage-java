package mil.nga.geopackage.tiles.features;

import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.nga.style.FeatureStyle;
import mil.nga.geopackage.extension.nga.style.IconRow;
import mil.nga.geopackage.extension.nga.style.StyleRow;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.sf.CircularString;
import mil.nga.sf.CompoundCurve;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryCollection;
import mil.nga.sf.GeometryType;
import mil.nga.sf.LineString;
import mil.nga.sf.MultiLineString;
import mil.nga.sf.MultiPoint;
import mil.nga.sf.MultiPolygon;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;
import mil.nga.sf.PolyhedralSurface;
import mil.nga.sf.TIN;
import mil.nga.sf.Triangle;
import mil.nga.sf.proj.GeometryTransform;

/**
 * Default Feature Tiles implementation using Java AWT to draw
 * 
 * @author osbornb
 * @since 1.1.2
 */
public class DefaultFeatureTiles extends FeatureTiles {

	/**
	 * Logger
	 */
	private static final Logger log = Logger
			.getLogger(DefaultFeatureTiles.class.getName());

	/**
	 * Default max number of feature geometries to retain in cache
	 *
	 * @since 3.3.0
	 */
	public static final int DEFAULT_GEOMETRY_CACHE_SIZE = 1000;

	/**
	 * Max geometry cache size
	 */
	protected int geometryCacheSize = DEFAULT_GEOMETRY_CACHE_SIZE;

	/**
	 * Geometry cache
	 */
	protected final Map<Long, GeoPackageGeometryData> geometryCache = new LinkedHashMap<Long, GeoPackageGeometryData>(
			geometryCacheSize, .75f, true) {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(
				Entry<Long, GeoPackageGeometryData> eldest) {
			return size() > geometryCacheSize;
		}
	};

	/**
	 * When true, geometries are cached. Default is true
	 */
	protected boolean cacheGeometries = true;

	/**
	 * Constructor
	 *
	 * @param featureDao
	 *            feature dao
	 */
	public DefaultFeatureTiles(FeatureDao featureDao) {
		super(featureDao);
	}

	/**
	 * Constructor
	 *
	 * @param featureDao
	 *            feature dao
	 * @param geodesic
	 *            draw geometries using geodesic lines
	 * @since 6.6.5
	 */
	public DefaultFeatureTiles(FeatureDao featureDao, boolean geodesic) {
		super(featureDao, geodesic);
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
	public DefaultFeatureTiles(FeatureDao featureDao, float scale) {
		super(featureDao, scale);
	}

	/**
	 * Constructor
	 *
	 * @param featureDao
	 *            feature dao
	 * @param scale
	 *            scale factor
	 * @param geodesic
	 *            draw geometries using geodesic lines
	 * @since 6.6.5
	 */
	public DefaultFeatureTiles(FeatureDao featureDao, float scale,
			boolean geodesic) {
		super(featureDao, scale, geodesic);
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
	public DefaultFeatureTiles(FeatureDao featureDao, int width, int height) {
		super(featureDao, width, height);
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
	 * @param geodesic
	 *            draw geometries using geodesic lines
	 * @since 6.6.5
	 */
	public DefaultFeatureTiles(FeatureDao featureDao, int width, int height,
			boolean geodesic) {
		super(featureDao, width, height, geodesic);
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
	public DefaultFeatureTiles(GeoPackage geoPackage, FeatureDao featureDao) {
		super(geoPackage, featureDao);
	}

	/**
	 * Constructor, auto creates the index manager for indexed tables and
	 * feature styles for styled tables
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param featureDao
	 *            feature dao
	 * @param geodesic
	 *            draw geometries using geodesic lines
	 * @since 6.6.5
	 */
	public DefaultFeatureTiles(GeoPackage geoPackage, FeatureDao featureDao,
			boolean geodesic) {
		super(geoPackage, featureDao, geodesic);
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
	public DefaultFeatureTiles(GeoPackage geoPackage, FeatureDao featureDao,
			float scale) {
		super(geoPackage, featureDao, scale);
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
	 * @param geodesic
	 *            draw geometries using geodesic lines
	 * @since 6.6.5
	 */
	public DefaultFeatureTiles(GeoPackage geoPackage, FeatureDao featureDao,
			float scale, boolean geodesic) {
		super(geoPackage, featureDao, scale, geodesic);
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
	public DefaultFeatureTiles(GeoPackage geoPackage, FeatureDao featureDao,
			int width, int height) {
		super(geoPackage, featureDao, width, height);
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
	 * @param geodesic
	 *            draw geometries using geodesic lines
	 * @since 6.6.5
	 */
	public DefaultFeatureTiles(GeoPackage geoPackage, FeatureDao featureDao,
			int width, int height, boolean geodesic) {
		super(geoPackage, featureDao, width, height, geodesic);
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
	public DefaultFeatureTiles(GeoPackage geoPackage, FeatureDao featureDao,
			float scale, int width, int height) {
		super(geoPackage, featureDao, scale, width, height);
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
	 * @param geodesic
	 *            draw geometries using geodesic lines
	 * @since 6.6.5
	 */
	public DefaultFeatureTiles(GeoPackage geoPackage, FeatureDao featureDao,
			float scale, int width, int height, boolean geodesic) {
		super(geoPackage, featureDao, scale, width, height, geodesic);
	}

	/**
	 * Is caching geometries enabled?
	 *
	 * @return true if caching geometries
	 * @since 3.3.0
	 */
	public boolean isCacheGeometries() {
		return cacheGeometries;
	}

	/**
	 * Set the cache geometries flag
	 *
	 * @param cacheGeometries
	 *            true to cache geometries
	 * @since 3.3.0
	 */
	public void setCacheGeometries(boolean cacheGeometries) {
		this.cacheGeometries = cacheGeometries;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearCache() {
		super.clearCache();
		clearGeometryCache();
	}

	/**
	 * Clear the geometry cache
	 *
	 * @since 3.3.0
	 */
	public void clearGeometryCache() {
		geometryCache.clear();
	}

	/**
	 * Set / resize the geometry cache size
	 *
	 * @param size
	 *            new size
	 * @since 3.3.0
	 */
	public void setGeometryCacheSize(int size) {
		geometryCacheSize = size;
		if (geometryCache.size() > size) {
			int count = 0;
			Iterator<Long> rowIds = geometryCache.keySet().iterator();
			while (rowIds.hasNext()) {
				rowIds.next();
				if (++count > size) {
					rowIds.remove();
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage drawTile(int zoom, BoundingBox boundingBox,
			FeatureIndexResults results) {

		FeatureTileGraphics graphics = new FeatureTileGraphics(tileWidth,
				tileHeight);

		// Feature projection to web mercator projection
		GeometryTransform webMercatorTransform = getWebMercatorTransform();
		BoundingBox expandedBoundingBox = expandBoundingBox(boundingBox);

		boolean drawn = false;
		for (FeatureRow featureRow : results) {
			if (drawFeature(zoom, boundingBox, expandedBoundingBox,
					webMercatorTransform, graphics, featureRow)) {
				drawn = true;
			}
		}
		results.close();

		BufferedImage image = null;
		if (drawn) {
			image = graphics.createImage();
			image = checkIfDrawn(image);
		} else {
			graphics.dispose();
		}

		return image;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage drawTile(int zoom, BoundingBox boundingBox,
			FeatureResultSet resultSet) {

		FeatureTileGraphics graphics = new FeatureTileGraphics(tileWidth,
				tileHeight);

		GeometryTransform webMercatorTransform = getWebMercatorTransform();
		BoundingBox expandedBoundingBox = expandBoundingBox(boundingBox);

		boolean drawn = false;
		while (resultSet.moveToNext()) {
			FeatureRow row = resultSet.getRow();
			if (drawFeature(zoom, boundingBox, expandedBoundingBox,
					webMercatorTransform, graphics, row)) {
				drawn = true;
			}
		}
		resultSet.close();

		BufferedImage image = null;
		if (drawn) {
			image = graphics.createImage();
			image = checkIfDrawn(image);
		} else {
			graphics.dispose();
		}

		return image;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage drawTile(int zoom, BoundingBox boundingBox,
			List<FeatureRow> featureRow) {

		FeatureTileGraphics graphics = new FeatureTileGraphics(tileWidth,
				tileHeight);

		GeometryTransform webMercatorTransform = getWebMercatorTransform();
		BoundingBox expandedBoundingBox = expandBoundingBox(boundingBox);

		boolean drawn = false;
		for (FeatureRow row : featureRow) {
			if (drawFeature(zoom, boundingBox, expandedBoundingBox,
					webMercatorTransform, graphics, row)) {
				drawn = true;
			}
		}

		BufferedImage image = null;
		if (drawn) {
			image = graphics.createImage();
			image = checkIfDrawn(image);
		} else {
			graphics.dispose();
		}

		return image;
	}

	/**
	 * Draw the feature
	 *
	 * @param zoom
	 *            zoom level
	 * @param boundingBox
	 *            bounding box
	 * @param expandedBoundingBox
	 *            expanded bounding box
	 * @param transform
	 *            geometry transform
	 * @param graphics
	 *            graphics to draw on
	 * @param row
	 *            feature row
	 * @return true if at least one feature was drawn
	 */
	private boolean drawFeature(int zoom, BoundingBox boundingBox,
			BoundingBox expandedBoundingBox, GeometryTransform transform,
			FeatureTileGraphics graphics, FeatureRow row) {

		boolean drawn = false;

		try {

			GeoPackageGeometryData geomData = null;
			BoundingBox transformedBoundingBox = null;
			long rowId = -1;

			// Check the cache for the geometry data
			if (cacheGeometries) {
				rowId = row.getId();
				geomData = geometryCache.get(rowId);
				if (geomData != null) {
					transformedBoundingBox = geomData.getBoundingBox();
				}
			}

			if (geomData == null) {
				// Read the geometry
				geomData = row.getGeometry();
			}

			if (geomData != null) {
				Geometry geometry = geomData.getGeometry();
				if (geometry != null) {

					if (transformedBoundingBox == null) {
						BoundingBox geometryBoundingBox = geomData
								.getOrBuildBoundingBox();
						transformedBoundingBox = geometryBoundingBox
								.transform(transform);

						if (cacheGeometries) {
							// Set the geometry envelope to the transformed
							// bounding box
							geomData.setEnvelope(
									transformedBoundingBox.buildEnvelope());
						}
					}

					if (cacheGeometries) {
						// Cache the geometry
						geometryCache.put(rowId, geomData);
					}

					if (expandedBoundingBox.intersects(transformedBoundingBox,
							true)) {

						double simplifyTolerance = TileBoundingBoxUtils
								.toleranceDistance(zoom, tileWidth, tileHeight);
						drawn = drawGeometry(simplifyTolerance, boundingBox,
								transform, graphics, row, geometry);

					}
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to draw feature in tile. Table: "
					+ featureDao.getTableName(), e);
		}

		return drawn;
	}

	/**
	 * Draw the geometry
	 *
	 * @param simplifyTolerance
	 *            simplify tolerance in meters
	 * @param boundingBox
	 *            bounding box
	 * @param transform
	 *            geometry transform
	 * @param graphics
	 *            feature tile graphics
	 * @param featureRow
	 *            feature row
	 * @param geometry
	 *            geometry
	 * @return true if drawn
	 */
	private boolean drawGeometry(double simplifyTolerance,
			BoundingBox boundingBox, GeometryTransform transform,
			FeatureTileGraphics graphics, FeatureRow featureRow,
			Geometry geometry) {

		boolean drawn = false;

		GeometryType geometryType = geometry.getGeometryType();
		FeatureStyle featureStyle = getFeatureStyle(featureRow, geometryType);

		switch (geometryType) {

		case POINT:
			Point point = (Point) geometry;
			drawn = drawPoint(boundingBox, transform, graphics, point,
					featureStyle);
			break;
		case LINESTRING:
			LineString lineString = (LineString) geometry;
			drawn = drawLineString(simplifyTolerance, boundingBox, transform,
					graphics, lineString, featureStyle);
			break;
		case POLYGON:
			Polygon polygon = (Polygon) geometry;
			drawn = drawPolygon(simplifyTolerance, boundingBox, transform,
					graphics, polygon, featureStyle);
			break;
		case MULTIPOINT:
			MultiPoint multiPoint = (MultiPoint) geometry;
			for (Point p : multiPoint.getPoints()) {
				drawn = drawPoint(boundingBox, transform, graphics, p,
						featureStyle) || drawn;
			}
			break;
		case MULTILINESTRING:
			MultiLineString multiLineString = (MultiLineString) geometry;
			for (LineString ls : multiLineString.getLineStrings()) {
				drawn = drawLineString(simplifyTolerance, boundingBox,
						transform, graphics, ls, featureStyle) || drawn;
			}
			break;
		case MULTIPOLYGON:
			MultiPolygon multiPolygon = (MultiPolygon) geometry;
			for (Polygon p : multiPolygon.getPolygons()) {
				drawn = drawPolygon(simplifyTolerance, boundingBox, transform,
						graphics, p, featureStyle) || drawn;
			}
			break;
		case CIRCULARSTRING:
			CircularString circularString = (CircularString) geometry;
			drawn = drawLineString(simplifyTolerance, boundingBox, transform,
					graphics, circularString, featureStyle);
			break;
		case COMPOUNDCURVE:
			CompoundCurve compoundCurve = (CompoundCurve) geometry;
			for (LineString ls : compoundCurve.getLineStrings()) {
				drawn = drawLineString(simplifyTolerance, boundingBox,
						transform, graphics, ls, featureStyle) || drawn;
			}
			break;
		case POLYHEDRALSURFACE:
			PolyhedralSurface polyhedralSurface = (PolyhedralSurface) geometry;
			for (Polygon p : polyhedralSurface.getPolygons()) {
				drawn = drawPolygon(simplifyTolerance, boundingBox, transform,
						graphics, p, featureStyle) || drawn;
			}
			break;
		case TIN:
			TIN tin = (TIN) geometry;
			for (Polygon p : tin.getPolygons()) {
				drawn = drawPolygon(simplifyTolerance, boundingBox, transform,
						graphics, p, featureStyle) || drawn;
			}
			break;
		case TRIANGLE:
			Triangle triangle = (Triangle) geometry;
			drawn = drawPolygon(simplifyTolerance, boundingBox, transform,
					graphics, triangle, featureStyle);
			break;
		case GEOMETRYCOLLECTION:
			@SuppressWarnings("unchecked")
			GeometryCollection<Geometry> geometryCollection = (GeometryCollection<Geometry>) geometry;
			for (Geometry g : geometryCollection.getGeometries()) {
				drawn = drawGeometry(simplifyTolerance, boundingBox, transform,
						graphics, featureRow, g) || drawn;
			}
			break;
		default:
			throw new GeoPackageException("Unsupported Geometry Type: "
					+ geometry.getGeometryType().getName());
		}

		return drawn;
	}

	/**
	 * Draw a LineString
	 * 
	 * @param simplifyTolerance
	 *            simplify tolerance in meters
	 * @param boundingBox
	 *            bounding box
	 * @param transform
	 *            geometry transform
	 * @param graphics
	 *            feature tile graphics
	 * @param lineString
	 *            line string
	 * @param featureStyle
	 *            feature style
	 * @return true if drawn
	 */
	private boolean drawLineString(double simplifyTolerance,
			BoundingBox boundingBox, GeometryTransform transform,
			FeatureTileGraphics graphics, LineString lineString,
			FeatureStyle featureStyle) {
		Path2D path = getPath(simplifyTolerance, boundingBox, transform,
				lineString);
		return drawLine(graphics, path, featureStyle);
	}

	/**
	 * Draw a Polygon
	 * 
	 * @param simplifyTolerance
	 *            simplify tolerance in meters
	 * @param boundingBox
	 *            bounding box
	 * @param transform
	 *            geometry transform
	 * @param graphics
	 *            feature tile graphics
	 * @param polygon
	 *            polygon
	 * @param featureStyle
	 *            feature style
	 * @return true if drawn
	 */
	private boolean drawPolygon(double simplifyTolerance,
			BoundingBox boundingBox, GeometryTransform transform,
			FeatureTileGraphics graphics, Polygon polygon,
			FeatureStyle featureStyle) {
		Area polygonArea = getArea(simplifyTolerance, boundingBox, transform,
				polygon);
		return drawPolygon(graphics, polygonArea, featureStyle);
	}

	/**
	 * Get the path of the line string
	 *
	 * @param simplifyTolerance
	 *            simplify tolerance in meters
	 * @param boundingBox
	 * @param transform
	 * @param lineString
	 */
	private Path2D getPath(double simplifyTolerance, BoundingBox boundingBox,
			GeometryTransform transform, LineString lineString) {

		Path2D path = null;

		// Try to simplify the number of points in the LineString
		List<Point> lineStringPoints = simplifyPoints(simplifyTolerance,
				lineString.getPoints());

		// Create a geodesic path of points if needed
		lineStringPoints = geodesicPath(simplifyTolerance, lineStringPoints);

		for (Point point : lineStringPoints) {

			Point projectedPoint = transform.transform(point);

			float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
					projectedPoint.getX());
			float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
					projectedPoint.getY());

			if (path == null) {
				path = new Path2D.Double();
				path.moveTo(x, y);
			} else {
				path.lineTo(x, y);
			}

		}

		return path;
	}

	/**
	 * Draw the line
	 * 
	 * @param graphics
	 *            feature tile graphics
	 * @param line
	 *            line path
	 * @param featureStyle
	 *            feature style
	 * @return true if drawn
	 */
	private boolean drawLine(FeatureTileGraphics graphics, Path2D line,
			FeatureStyle featureStyle) {

		Graphics2D lineGraphics = graphics.getLineGraphics();

		Paint paint = getLinePaint(featureStyle);
		lineGraphics.setColor(paint.getColor());
		lineGraphics.setStroke(paint.getStroke());

		boolean drawn = lineGraphics
				.hit(new java.awt.Rectangle(tileWidth, tileHeight), line, true);
		if (drawn) {
			lineGraphics.draw(line);
		}

		return drawn;
	}

	/**
	 * Get the area of the polygon
	 *
	 * @param simplifyTolerance
	 *            simplify tolerance in meters
	 * @param boundingBox
	 * @param transform
	 * @param lineString
	 */
	private Area getArea(double simplifyTolerance, BoundingBox boundingBox,
			GeometryTransform transform, Polygon polygon) {

		Area area = null;

		for (LineString ring : polygon.getRings()) {

			Path2D path = getPath(simplifyTolerance, boundingBox, transform,
					ring);
			Area ringArea = new Area(path);

			if (area == null) {
				area = ringArea;
			} else {
				area.subtract(ringArea);
			}

		}

		return area;
	}

	/**
	 * Draw the polygon
	 * 
	 * @param graphics
	 *            feature tile graphics
	 * @param polygon
	 *            polygon area
	 * @param featureStyle
	 *            feature style
	 * @return true if drawn
	 */
	private boolean drawPolygon(FeatureTileGraphics graphics, Area polygon,
			FeatureStyle featureStyle) {

		Graphics2D polygonGraphics = graphics.getPolygonGraphics();

		Paint fillPaint = getPolygonFillPaint(featureStyle);
		if (fillPaint != null) {

			polygonGraphics.setColor(fillPaint.getColor());
			polygonGraphics.fill(polygon);

		}

		Paint paint = getPolygonPaint(featureStyle);
		polygonGraphics.setColor(paint.getColor());
		polygonGraphics.setStroke(paint.getStroke());

		boolean drawn = polygonGraphics.hit(
				new java.awt.Rectangle(tileWidth, tileHeight), polygon, true);
		if (drawn) {
			polygonGraphics.draw(polygon);
		}

		return drawn;
	}

	/**
	 * Draw the point
	 *
	 * @param boundingBox
	 *            bounding box
	 * @param transform
	 *            geometry transform
	 * @param graphics
	 *            feature tile graphics
	 * @param point
	 *            point
	 * @param featureStyle
	 *            feature style
	 * @return true if drawn
	 */
	private boolean drawPoint(BoundingBox boundingBox,
			GeometryTransform transform, FeatureTileGraphics graphics,
			Point point, FeatureStyle featureStyle) {

		boolean drawn = false;

		Point projectedPoint = transform.transform(point);

		float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
				projectedPoint.getX());
		float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
				projectedPoint.getY());

		if (featureStyle != null && featureStyle.useIcon()) {

			IconRow iconRow = featureStyle.getIcon();
			BufferedImage icon = getIcon(iconRow);

			int width = icon.getWidth();
			int height = icon.getHeight();

			if (x >= 0 - width && x <= tileWidth + width && y >= 0 - height
					&& y <= tileHeight + height) {

				float anchorU = (float) iconRow.getAnchorUOrDefault();
				float anchorV = (float) iconRow.getAnchorVOrDefault();

				int iconX = Math.round(x - (anchorU * width));
				int iconY = Math.round(y - (anchorV * height));

				Graphics2D iconGraphics = graphics.getIconGraphics();
				iconGraphics.drawImage(icon, iconX, iconY, null);
				drawn = true;
			}

		} else if (pointIcon != null) {

			int width = Math.round(this.scale * pointIcon.getWidth());
			int height = Math.round(this.scale * pointIcon.getHeight());
			if (x >= 0 - width && x <= tileWidth + width && y >= 0 - height
					&& y <= tileHeight + height) {
				int iconX = Math.round(x - this.scale * pointIcon.getXOffset());
				int iconY = Math.round(y - this.scale * pointIcon.getYOffset());
				Graphics2D iconGraphics = graphics.getIconGraphics();
				iconGraphics.drawImage(pointIcon.getIcon(), iconX, iconY, width,
						height, null);
				drawn = true;
			}

		} else {

			Float radius = null;
			if (featureStyle != null) {
				StyleRow styleRow = featureStyle.getStyle();
				if (styleRow != null) {
					radius = this.scale
							* (float) (styleRow.getWidthOrDefault() / 2.0f);
				}
			}
			if (radius == null) {
				radius = this.scale * pointRadius;
			}
			if (x >= 0 - radius && x <= tileWidth + radius && y >= 0 - radius
					&& y <= tileHeight + radius) {

				Graphics2D pointGraphics = graphics.getPointGraphics();
				Paint pointPaint = getPointPaint(featureStyle);
				pointGraphics.setColor(pointPaint.getColor());

				int circleX = Math.round(x - radius);
				int circleY = Math.round(y - radius);
				int diameter = Math.round(radius * 2);
				pointGraphics.fillOval(circleX, circleY, diameter, diameter);
				drawn = true;
			}

		}

		return drawn;
	}

}
