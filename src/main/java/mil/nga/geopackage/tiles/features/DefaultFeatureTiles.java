package mil.nga.geopackage.tiles.features;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.index.GeometryIndex;
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
import mil.nga.sf.util.GeometryEnvelopeBuilder;

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
	 */
	public DefaultFeatureTiles(FeatureDao featureDao) {
		super(featureDao);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage drawTile(int zoom, BoundingBox boundingBox,
			CloseableIterator<GeometryIndex> results) {

		// Create image and graphics
		BufferedImage image = createNewImage();
		Graphics2D graphics = getGraphics(image);

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

		if (!drawn) {
			image.flush();
			image = null;
		}

		return image;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage drawTile(int zoom, BoundingBox boundingBox,
			FeatureResultSet resultSet) {

		BufferedImage image = createNewImage();
		Graphics2D graphics = getGraphics(image);

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
		if (!drawn) {
			image.flush();
			image = null;
		}

		return image;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage drawTile(int zoom, BoundingBox boundingBox,
			List<FeatureRow> featureRow) {

		BufferedImage image = createNewImage();
		Graphics2D graphics = getGraphics(image);

		ProjectionTransform webMercatorTransform = getWebMercatorTransform();
		BoundingBox expandedBoundingBox = expandBoundingBox(boundingBox);

		boolean drawn = false;
		for (FeatureRow row : featureRow) {
			if (drawFeature(zoom, boundingBox, expandedBoundingBox,
					webMercatorTransform, graphics, row)) {
				drawn = true;
			}
		}

		if (!drawn) {
			image.flush();
			image = null;
		}

		return image;
	}

	/**
	 * Get a graphics for the image
	 * 
	 * @param image
	 * @return graphics
	 */
	private Graphics2D getGraphics(BufferedImage image) {
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		return graphics;
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
			Graphics2D graphics, FeatureRow row) {

		boolean drawn = false;

		try {
			GeoPackageGeometryData geomData = row.getGeometry();
			if (geomData != null) {
				Geometry geometry = geomData.getGeometry();
				if (geometry != null) {

					GeometryEnvelope envelope = geomData.getEnvelope();
					if (envelope == null) {
						envelope = GeometryEnvelopeBuilder
								.buildEnvelope(geometry);
					}
					BoundingBox geometryBoundingBox = new BoundingBox(envelope);
					BoundingBox transformedBoundingBox = geometryBoundingBox
							.transform(transform);

					if (TileBoundingBoxUtils.overlap(expandedBoundingBox,
							transformedBoundingBox, true) != null) {

						double simplifyTolerance = TileBoundingBoxUtils
								.toleranceDistance(zoom, tileWidth, tileHeight);
						drawGeometry(simplifyTolerance, boundingBox, transform,
								graphics, geometry);

						drawn = true;
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
	 * @param transform
	 * @param graphics
	 * @param geometry
	 */
	private void drawGeometry(double simplifyTolerance,
			BoundingBox boundingBox, ProjectionTransform transform,
			Graphics2D graphics, Geometry geometry) {

		switch (geometry.getGeometryType()) {

		case POINT:
			Point point = (Point) geometry;
			drawPoint(boundingBox, transform, graphics, point);
			break;
		case LINESTRING:
			LineString lineString = (LineString) geometry;
			drawLineString(simplifyTolerance, boundingBox, transform, graphics,
					lineString);
			break;
		case POLYGON:
			Polygon polygon = (Polygon) geometry;
			drawPolygon(simplifyTolerance, boundingBox, transform, graphics,
					polygon);
			break;
		case MULTIPOINT:
			MultiPoint multiPoint = (MultiPoint) geometry;
			for (Point p : multiPoint.getPoints()) {
				drawPoint(boundingBox, transform, graphics, p);
			}
			break;
		case MULTILINESTRING:
			MultiLineString multiLineString = (MultiLineString) geometry;
			for (LineString ls : multiLineString.getLineStrings()) {
				drawLineString(simplifyTolerance, boundingBox, transform,
						graphics, ls);
			}
			break;
		case MULTIPOLYGON:
			MultiPolygon multiPolygon = (MultiPolygon) geometry;
			for (Polygon p : multiPolygon.getPolygons()) {
				drawPolygon(simplifyTolerance, boundingBox, transform,
						graphics, p);
			}
			break;
		case CIRCULARSTRING:
			CircularString circularString = (CircularString) geometry;
			drawLineString(simplifyTolerance, boundingBox, transform, graphics,
					circularString);
			break;
		case COMPOUNDCURVE:
			CompoundCurve compoundCurve = (CompoundCurve) geometry;
			for (LineString ls : compoundCurve.getLineStrings()) {
				drawLineString(simplifyTolerance, boundingBox, transform,
						graphics, ls);
			}
			break;
		case POLYHEDRALSURFACE:
			PolyhedralSurface polyhedralSurface = (PolyhedralSurface) geometry;
			for (Polygon p : polyhedralSurface.getPolygons()) {
				drawPolygon(simplifyTolerance, boundingBox, transform,
						graphics, p);
			}
			break;
		case TIN:
			TIN tin = (TIN) geometry;
			for (Polygon p : tin.getPolygons()) {
				drawPolygon(simplifyTolerance, boundingBox, transform,
						graphics, p);
			}
			break;
		case TRIANGLE:
			Triangle triangle = (Triangle) geometry;
			drawPolygon(simplifyTolerance, boundingBox, transform, graphics,
					triangle);
			break;
		case GEOMETRYCOLLECTION:
			@SuppressWarnings("unchecked")
			GeometryCollection<Geometry> geometryCollection = (GeometryCollection<Geometry>) geometry;
			for (Geometry g : geometryCollection.getGeometries()) {
				drawGeometry(simplifyTolerance, boundingBox, transform,
						graphics, g);
			}
			break;
		default:
			throw new GeoPackageException("Unsupported Geometry Type: "
					+ geometry.getGeometryType().getName());
		}

	}

	/**
	 * Draw a LineString
	 * 
	 * @param simplifyTolerance
	 *            simplify tolerance in meters
	 * @param boundingBox
	 * @param transform
	 * @param graphics
	 * @param lineString
	 */
	private void drawLineString(double simplifyTolerance,
			BoundingBox boundingBox, ProjectionTransform transform,
			Graphics2D graphics, LineString lineString) {
		Path2D path = getPath(simplifyTolerance, boundingBox, transform,
				lineString);
		drawLine(graphics, path);
	}

	/**
	 * Draw a Polygon
	 * 
	 * @param simplifyTolerance
	 *            simplify tolerance in meters
	 * @param boundingBox
	 * @param transform
	 * @param graphics
	 * @param polygon
	 */
	private void drawPolygon(double simplifyTolerance, BoundingBox boundingBox,
			ProjectionTransform transform, Graphics2D graphics, Polygon polygon) {
		Area polygonArea = getArea(simplifyTolerance, boundingBox, transform,
				polygon);
		drawPolygon(graphics, polygonArea);
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
	 * @param line
	 */
	private void drawLine(Graphics2D graphics, Path2D line) {
		graphics.setColor(lineColor);
		graphics.setStroke(new BasicStroke(lineStrokeWidth));
		graphics.draw(line);
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
	 * @param polygon
	 */
	private void drawPolygon(Graphics2D graphics, Area polygon) {

		if (fillPolygon) {
			graphics.setColor(polygonFillColor);
			graphics.fill(polygon);
		}

		graphics.setColor(polygonColor);
		graphics.setStroke(new BasicStroke(polygonStrokeWidth));
		graphics.draw(polygon);

	}

	/**
	 * Draw the point
	 *
	 * @param boundingBox
	 * @param transform
	 * @param graphics
	 * @param point
	 */
	private void drawPoint(BoundingBox boundingBox,
			ProjectionTransform transform, Graphics2D graphics, Point point) {

		Point projectedPoint = transform.transform(point);

		float x = TileBoundingBoxUtils.getXPixel(tileWidth, boundingBox,
				projectedPoint.getX());
		float y = TileBoundingBoxUtils.getYPixel(tileHeight, boundingBox,
				projectedPoint.getY());

		if (pointIcon != null) {
			if (x >= 0 - pointIcon.getWidth()
					&& x <= tileWidth + pointIcon.getWidth()
					&& y >= 0 - pointIcon.getHeight()
					&& y <= tileHeight + pointIcon.getHeight()) {
				int iconX = Math.round(x - pointIcon.getXOffset());
				int iconY = Math.round(y - pointIcon.getYOffset());
				graphics.drawImage(pointIcon.getIcon(), iconX, iconY, null);
			}
		} else {
			if (x >= 0 - pointRadius && x <= tileWidth + pointRadius
					&& y >= 0 - pointRadius && y <= tileHeight + pointRadius) {
				int diameter = Math.round(pointRadius * 2);
				graphics.setColor(pointColor);
				int circleX = Math.round(x - pointRadius);
				int circleY = Math.round(y - pointRadius);
				graphics.fillOval(circleX, circleY, diameter, diameter);
			}
		}

	}

}
