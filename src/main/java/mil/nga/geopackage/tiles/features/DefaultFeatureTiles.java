package mil.nga.geopackage.tiles.features;

import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.index.GeometryIndex;
import mil.nga.geopackage.extension.style.FeatureStyle;
import mil.nga.geopackage.extension.style.IconRow;
import mil.nga.geopackage.extension.style.StyleRow;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.sf.CircularString;
import mil.nga.sf.CompoundCurve;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryCollection;
import mil.nga.sf.GeometryEnvelope;
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
import mil.nga.sf.proj.ProjectionTransform;

import com.j256.ormlite.dao.CloseableIterator;

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
	 * Constructor
	 *
	 * @param featureDao
	 *            feature dao
	 */
	public DefaultFeatureTiles(FeatureDao featureDao) {
		super(featureDao);
	}

	/**
	 * Constructor, auto creates the feature table index for indexed tables and
	 * feature styles for styled tables
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param featureDao
	 *            feature dao
	 * @since 3.1.1
	 */
	public DefaultFeatureTiles(GeoPackage geoPackage, FeatureDao featureDao) {
		super(geoPackage, featureDao);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage drawTile(int zoom, BoundingBox boundingBox,
			CloseableIterator<GeometryIndex> results) {

		FeatureTileGraphics graphics = new FeatureTileGraphics(tileWidth,
				tileHeight);

		// WGS84 to web mercator projection and google shape converter
		ProjectionTransform webMercatorTransform = getWebMercatorTransform();
		BoundingBox expandedBoundingBox = expandBoundingBox(boundingBox);

		boolean drawn = false;
		while (results.hasNext()) {
			GeometryIndex geometryIndex = results.next();
			FeatureRow featureRow = getFeatureIndex().getFeatureRow(
					geometryIndex);
			if (drawFeature(zoom, boundingBox, expandedBoundingBox,
					webMercatorTransform, graphics, featureRow)) {
				drawn = true;
			}
		}
		try {
			results.close();
		} catch (IOException e) {
			log.log(Level.WARNING, "Failed to close geometry index results", e);
		}

		BufferedImage image = null;
		if (drawn) {
			image = graphics.createImage();
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

		ProjectionTransform webMercatorTransform = getWebMercatorTransform();
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

		ProjectionTransform webMercatorTransform = getWebMercatorTransform();
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
	 *            projection transform
	 * @param graphics
	 *            graphics to draw on
	 * @param row
	 *            feature row
	 * @return true if at least one feature was drawn
	 */
	private boolean drawFeature(int zoom, BoundingBox boundingBox,
			BoundingBox expandedBoundingBox, ProjectionTransform transform,
			FeatureTileGraphics graphics, FeatureRow row) {

		boolean drawn = false;

		try {
			GeoPackageGeometryData geomData = row.getGeometry();
			if (geomData != null) {
				Geometry geometry = geomData.getGeometry();
				if (geometry != null) {

					GeometryEnvelope envelope = geomData.getOrBuildEnvelope();
					BoundingBox geometryBoundingBox = new BoundingBox(envelope);
					BoundingBox transformedBoundingBox = geometryBoundingBox
							.transform(transform);

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
	 *            projection transform
	 * @param graphics
	 *            feature tile graphics
	 * @param featureRow
	 *            feature row
	 * @param geometry
	 *            geometry
	 * @return true if drawn
	 */
	private boolean drawGeometry(double simplifyTolerance,
			BoundingBox boundingBox, ProjectionTransform transform,
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
	 *            projection transform
	 * @param graphics
	 *            feature tile graphics
	 * @param lineString
	 *            line string
	 * @param featureStyle
	 *            feature style
	 * @return true if drawn
	 */
	private boolean drawLineString(double simplifyTolerance,
			BoundingBox boundingBox, ProjectionTransform transform,
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
	 *            projection transform
	 * @param graphics
	 *            feature tile graphics
	 * @param polygon
	 *            polygon
	 * @param featureStyle
	 *            feature style
	 * @return true if drawn
	 */
	private boolean drawPolygon(double simplifyTolerance,
			BoundingBox boundingBox, ProjectionTransform transform,
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
			ProjectionTransform transform, LineString lineString) {

		Path2D path = null;

		// Try to simplify the number of points in the LineString
		List<Point> lineStringPoints = simplifyPoints(simplifyTolerance,
				lineString.getPoints());

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

		boolean drawn = lineGraphics.hit(new java.awt.Rectangle(tileWidth,
				tileHeight), line, true);
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
			ProjectionTransform transform, Polygon polygon) {

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

		boolean drawn = polygonGraphics.hit(new java.awt.Rectangle(tileWidth,
				tileHeight), polygon, true);
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
	 *            projection transform
	 * @param graphics
	 *            feature tile graphics
	 * @param point
	 *            point
	 * @param featureStyle
	 *            feature style
	 * @return true if drawn
	 */
	private boolean drawPoint(BoundingBox boundingBox,
			ProjectionTransform transform, FeatureTileGraphics graphics,
			Point point, FeatureStyle featureStyle) {

		boolean drawn = false;

		Point projectedPoint = transform.transform(point);

		float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
				projectedPoint.getX());
		float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
				projectedPoint.getY());

		if (featureStyle != null && featureStyle.hasIcon()) {

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

			if (x >= 0 - pointIcon.getWidth()
					&& x <= tileWidth + pointIcon.getWidth()
					&& y >= 0 - pointIcon.getHeight()
					&& y <= tileHeight + pointIcon.getHeight()) {
				int iconX = Math.round(x - pointIcon.getXOffset());
				int iconY = Math.round(y - pointIcon.getYOffset());
				Graphics2D iconGraphics = graphics.getIconGraphics();
				iconGraphics.drawImage(pointIcon.getIcon(), iconX, iconY, null);
				drawn = true;
			}

		} else {

			Float radius = null;
			if (featureStyle != null) {
				StyleRow styleRow = featureStyle.getStyle();
				if (styleRow != null) {
					radius = (float) (styleRow.getWidthOrDefault() / 2.0f);
				}
			}
			if (radius == null) {
				radius = pointRadius;
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
