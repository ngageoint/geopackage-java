package mil.nga.geopackage.test;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryEnvelope;
import mil.nga.wkb.geom.GeometryType;
import mil.nga.wkb.geom.LineString;
import mil.nga.wkb.geom.Point;
import mil.nga.wkb.geom.Polygon;
import mil.nga.wkb.util.GeometryEnvelopeBuilder;

/**
 * Creates an example GeoPackage file
 * 
 * @author osbornb
 */
public class GeoPackageExample {

	private static final String GEOPACKAGE_FILE = "example.gpkg";

	private static final boolean FEATURES = true;
	private static final boolean TILES = false;
	private static final boolean ATTRIBUTES = false;

	public static void main(String[] args) throws SQLException {

		System.out.println("Creating: " + GEOPACKAGE_FILE);
		GeoPackage geoPackage = createGeoPackage();

		System.out.println("Features: " + FEATURES);
		if (FEATURES) {
			createFeatures(geoPackage);
		}

		System.out.println("Tiles: " + TILES);
		if (TILES) {
			createTiles(geoPackage);
		}

		System.out.println("Attributes: " + ATTRIBUTES);
		if (ATTRIBUTES) {
			createAttributes(geoPackage);
		}

		System.out.println("Created: " + geoPackage.getPath());
	}

	private static GeoPackage createGeoPackage() {

		File file = new File(GEOPACKAGE_FILE);
		if (file.exists()) {
			file.delete();
		}

		GeoPackageManager.create(file);

		GeoPackage geoPackage = GeoPackageManager.open(file);

		return geoPackage;
	}

	private static void createFeatures(GeoPackage geoPackage)
			throws SQLException {

		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();

		SpatialReferenceSystem srs = srsDao.queryForOrganizationCoordsysId(
				ProjectionConstants.AUTHORITY_EPSG,
				(long) ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		geoPackage.createGeometryColumnsTable();

		List<Geometry> points = new ArrayList<>();
		List<String> pointNames = new ArrayList<>();

		Point point1 = new Point(-104.802223, 39.719994);
		points.add(point1);
		pointNames.add("BIT Systems");

		Point point2 = new Point(-77.196736, 38.753370);
		points.add(point2);
		pointNames.add("NGA");

		createFeatures(geoPackage, srs, GeometryType.POINT, points, pointNames);

		List<Geometry> lines = new ArrayList<>();
		List<String> lineNames = new ArrayList<>();

		LineString line1 = new LineString();
		line1.addPoint(new Point(-104.800614, 39.720721));
		line1.addPoint(new Point(-104.802174, 39.720726));
		line1.addPoint(new Point(-104.802584, 39.720660));
		line1.addPoint(new Point(-104.803088, 39.720477));
		line1.addPoint(new Point(-104.803474, 39.720209));
		lines.add(line1);
		lineNames.add("Laredo St");

		LineString line2 = new LineString();
		line2.addPoint(new Point(-77.196650, 38.756501));
		line2.addPoint(new Point(-77.196414, 38.755979));
		line2.addPoint(new Point(-77.195518, 38.755208));
		line2.addPoint(new Point(-77.195303, 38.755272));
		line2.addPoint(new Point(-77.195351, 38.755459));
		line2.addPoint(new Point(-77.195863, 38.755697));
		line2.addPoint(new Point(-77.196328, 38.756069));
		line2.addPoint(new Point(-77.196568, 38.756526));
		lines.add(line2);
		lineNames.add("NGA");

		createFeatures(geoPackage, srs, GeometryType.LINESTRING, lines,
				lineNames);

		List<Geometry> polygons = new ArrayList<>();
		List<String> polygonNames = new ArrayList<>();

		Polygon polygon1 = new Polygon();
		LineString ring1 = new LineString();
		ring1.addPoint(new Point(-104.802246, 39.720343));
		ring1.addPoint(new Point(-104.802246, 39.719753));
		ring1.addPoint(new Point(-104.802183, 39.719754));
		ring1.addPoint(new Point(-104.802184, 39.719719));
		ring1.addPoint(new Point(-104.802138, 39.719694));
		ring1.addPoint(new Point(-104.802097, 39.719691));
		ring1.addPoint(new Point(-104.802096, 39.719648));
		ring1.addPoint(new Point(-104.801646, 39.719648));
		ring1.addPoint(new Point(-104.801644, 39.719722));
		ring1.addPoint(new Point(-104.801550, 39.719723));
		ring1.addPoint(new Point(-104.801549, 39.720207));
		ring1.addPoint(new Point(-104.801648, 39.720207));
		ring1.addPoint(new Point(-104.801648, 39.720341));
		ring1.addPoint(new Point(-104.802246, 39.720343));
		polygon1.addRing(ring1);
		polygons.add(polygon1);
		polygonNames.add("BIT Systems");

		Polygon polygon2 = new Polygon();
		LineString ring2 = new LineString();
		ring2.addPoint(new Point(-77.195299, 38.755159));
		ring2.addPoint(new Point(-77.195203, 38.755080));
		ring2.addPoint(new Point(-77.195410, 38.754930));
		ring2.addPoint(new Point(-77.195350, 38.754884));
		ring2.addPoint(new Point(-77.195228, 38.754966));
		ring2.addPoint(new Point(-77.195135, 38.754889));
		ring2.addPoint(new Point(-77.195048, 38.754956));
		ring2.addPoint(new Point(-77.194986, 38.754906));
		ring2.addPoint(new Point(-77.194897, 38.754976));
		ring2.addPoint(new Point(-77.194953, 38.755025));
		ring2.addPoint(new Point(-77.194763, 38.755173));
		ring2.addPoint(new Point(-77.194827, 38.755224));
		ring2.addPoint(new Point(-77.195012, 38.755082));
		ring2.addPoint(new Point(-77.195041, 38.755104));
		ring2.addPoint(new Point(-77.195028, 38.755116));
		ring2.addPoint(new Point(-77.195090, 38.755167));
		ring2.addPoint(new Point(-77.195106, 38.755154));
		ring2.addPoint(new Point(-77.195205, 38.755233));
		ring2.addPoint(new Point(-77.195299, 38.755159));
		polygon2.addRing(ring2);
		polygons.add(polygon2);
		polygonNames.add("NGA Visitor Center");

		createFeatures(geoPackage, srs, GeometryType.POLYGON, polygons,
				polygonNames);

		List<Geometry> geometries = new ArrayList<>();
		List<String> geometryNames = new ArrayList<>();
		geometries.addAll(points);
		geometryNames.addAll(pointNames);
		geometries.addAll(lines);
		geometryNames.addAll(lineNames);
		geometries.addAll(polygons);
		geometryNames.addAll(polygonNames);

		createFeatures(geoPackage, srs, GeometryType.GEOMETRY, geometries,
				geometryNames);
	}

	private static void createFeatures(GeoPackage geoPackage,
			SpatialReferenceSystem srs, GeometryType type,
			List<Geometry> geometries, List<String> names) throws SQLException {

		String tableName = type.name().toLowerCase();

		GeometryEnvelope envelope = null;
		for (Geometry geometry : geometries) {
			if (envelope == null) {
				envelope = GeometryEnvelopeBuilder.buildEnvelope(geometry);
			} else {
				GeometryEnvelopeBuilder.buildEnvelope(geometry, envelope);
			}
		}

		ContentsDao contentsDao = geoPackage.getContentsDao();

		Contents contents = new Contents();
		contents.setTableName(tableName);
		contents.setDataType(ContentsDataType.FEATURES);
		contents.setIdentifier(tableName);
		contents.setDescription("example: " + tableName);
		contents.setMinX(envelope.getMinX());
		contents.setMinY(envelope.getMinY());
		contents.setMaxX(envelope.getMaxX());
		contents.setMaxY(envelope.getMaxY());
		contents.setSrs(srs);

		List<FeatureColumn> columns = new ArrayList<FeatureColumn>();

		final String idColumn = "id";
		final String geometryColumn = "geometry";
		final String textColumn = "name";
		final String realColumn = "test_real";
		final String booleanColumn = "test_boolean";
		final String blobColumn = "test_blob";
		final String integerColumn = "test_integer";
		final String textLimitedColumn = "test_text_limited";
		final String blobLimitedColumn = "test_blob_limited";
		final String dateColumn = "test_date";
		final String datetimeColumn = "test_datetime";

		int columnNumber = 0;
		columns.add(FeatureColumn.createPrimaryKeyColumn(columnNumber++,
				idColumn));
		columns.add(FeatureColumn.createGeometryColumn(columnNumber++,
				geometryColumn, type, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, textColumn,
				GeoPackageDataType.TEXT, false, ""));
		columns.add(FeatureColumn.createColumn(columnNumber++, realColumn,
				GeoPackageDataType.REAL, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, booleanColumn,
				GeoPackageDataType.BOOLEAN, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, blobColumn,
				GeoPackageDataType.BLOB, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, integerColumn,
				GeoPackageDataType.INTEGER, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++,
				textLimitedColumn, GeoPackageDataType.TEXT, (long) UUID
						.randomUUID().toString().length(), false, null));
		columns.add(FeatureColumn
				.createColumn(columnNumber++, blobLimitedColumn,
						GeoPackageDataType.BLOB, (long) UUID.randomUUID()
								.toString().getBytes().length, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, dateColumn,
				GeoPackageDataType.DATE, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, datetimeColumn,
				GeoPackageDataType.DATETIME, false, null));

		FeatureTable table = new FeatureTable(tableName, columns);
		geoPackage.createFeatureTable(table);

		contentsDao.create(contents);

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();

		GeometryColumns geometryColumns = new GeometryColumns();
		geometryColumns.setContents(contents);
		geometryColumns.setColumnName(geometryColumn);
		geometryColumns.setGeometryType(type);
		geometryColumns.setSrs(srs);
		geometryColumns.setZ((byte) 0);
		geometryColumns.setM((byte) 0);
		geometryColumnsDao.create(geometryColumns);

		FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);

		for (int i = 0; i < geometries.size(); i++) {

			Geometry geometry = geometries.get(i);
			String name;
			if (names != null) {
				name = names.get(i);
			} else {
				name = UUID.randomUUID().toString();
			}

			FeatureRow newRow = dao.newRow();

			GeoPackageGeometryData geometryData = new GeoPackageGeometryData(
					geometryColumns.getSrsId());
			geometryData.setGeometry(geometry);
			newRow.setGeometry(geometryData);

			newRow.setValue(textColumn, name);
			newRow.setValue(realColumn, Math.random() * 5000.0);
			newRow.setValue(booleanColumn, Math.random() < .5 ? false : true);
			newRow.setValue(blobColumn, UUID.randomUUID().toString().getBytes());
			newRow.setValue(integerColumn, (int) (Math.random() * 500));
			newRow.setValue(textLimitedColumn, UUID.randomUUID().toString());
			newRow.setValue(blobLimitedColumn, UUID.randomUUID().toString()
					.getBytes());
			newRow.setValue(dateColumn, new Date());
			newRow.setValue(datetimeColumn, new Date());

			dao.create(newRow);

		}

	}

	private static void createTiles(GeoPackage geoPackage) {
		// TODO
	}

	private static void createAttributes(GeoPackage geoPackage) {
		// TODO
	}

}
