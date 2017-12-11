package mil.nga.geopackage.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesColumn;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesRow;
import mil.nga.geopackage.attributes.AttributesTable;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.index.FeatureTableIndex;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.schema.columns.DataColumns;
import mil.nga.geopackage.schema.columns.DataColumnsDao;
import mil.nga.geopackage.schema.constraints.DataColumnConstraintType;
import mil.nga.geopackage.schema.constraints.DataColumnConstraints;
import mil.nga.geopackage.schema.constraints.DataColumnConstraintsDao;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGenerator;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.features.DefaultFeatureTiles;
import mil.nga.geopackage.tiles.features.FeatureTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.tiles.user.TileTable;
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
	private static final boolean TILES = true;
	private static final boolean ATTRIBUTES = true;
	private static final boolean SCHEMA_EXTENSION = true;
	private static final boolean GEOMETRY_INDEX_EXTENSION = true;
	private static final boolean FEATURE_TILE_LINK_EXTENSION = true;

	private static final String ID_COLUMN = "id";
	private static final String GEOMETRY_COLUMN = "geometry";
	private static final String TEXT_COLUMN = "text";
	private static final String REAL_COLUMN = "real";
	private static final String BOOLEAN_COLUMN = "boolean";
	private static final String BLOB_COLUMN = "blob";
	private static final String INTEGER_COLUMN = "integer";
	private static final String TEXT_LIMITED_COLUMN = "text_limited";
	private static final String BLOB_LIMITED_COLUMN = "blob_limited";
	private static final String DATE_COLUMN = "date";
	private static final String DATETIME_COLUMN = "datetime";

	public static void main(String[] args) throws SQLException, IOException {

		System.out.println("Creating: " + GEOPACKAGE_FILE);
		GeoPackage geoPackage = createGeoPackage();

		System.out.println("Features: " + FEATURES);
		if (FEATURES) {

			createFeatures(geoPackage);

			System.out.println("Schema Extension: " + SCHEMA_EXTENSION);
			if (SCHEMA_EXTENSION) {
				createSchemaExtension(geoPackage);
			}

			System.out.println("Geometry Index Extension: "
					+ GEOMETRY_INDEX_EXTENSION);
			if (GEOMETRY_INDEX_EXTENSION) {
				createGeometryIndexExtension(geoPackage);
			}

			System.out.println("Feature Tile Link Extension: "
					+ FEATURE_TILE_LINK_EXTENSION);
			if (FEATURE_TILE_LINK_EXTENSION) {
				createFeatureTileLinkExtension(geoPackage);
			}
		} else {
			System.out.println("Schema Extension: " + FEATURES);
			System.out.println("Geometry Index Extension: " + FEATURES);
			System.out.println("Feature Tile Link Extension: " + FEATURES);
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

		SpatialReferenceSystem srs = srsDao.getOrCreateCode(
				ProjectionConstants.AUTHORITY_EPSG,
				(long) ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		geoPackage.createGeometryColumnsTable();

		Point point1 = new Point(-104.801918, 39.720014);
		String point1Name = "BIT Systems";

		createFeatures(geoPackage, srs, "point1", GeometryType.POINT, point1,
				point1Name);

		Point point2 = new Point(-77.196736, 38.753370);
		String point2Name = "NGA";

		createFeatures(geoPackage, srs, "point2", GeometryType.POINT, point2,
				point2Name);

		LineString line1 = new LineString();
		String line1Name = "East Lockheed Drive";
		line1.addPoint(new Point(-104.800614, 39.720721));
		line1.addPoint(new Point(-104.802174, 39.720726));
		line1.addPoint(new Point(-104.802584, 39.720660));
		line1.addPoint(new Point(-104.803088, 39.720477));
		line1.addPoint(new Point(-104.803474, 39.720209));

		createFeatures(geoPackage, srs, "line1", GeometryType.LINESTRING,
				line1, line1Name);

		LineString line2 = new LineString();
		String line2Name = "NGA";
		line2.addPoint(new Point(-77.196650, 38.756501));
		line2.addPoint(new Point(-77.196414, 38.755979));
		line2.addPoint(new Point(-77.195518, 38.755208));
		line2.addPoint(new Point(-77.195303, 38.755272));
		line2.addPoint(new Point(-77.195351, 38.755459));
		line2.addPoint(new Point(-77.195863, 38.755697));
		line2.addPoint(new Point(-77.196328, 38.756069));
		line2.addPoint(new Point(-77.196568, 38.756526));

		createFeatures(geoPackage, srs, "line2", GeometryType.LINESTRING,
				line2, line2Name);

		Polygon polygon1 = new Polygon();
		String polygon1Name = "BIT Systems";
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

		createFeatures(geoPackage, srs, "polygon1", GeometryType.POLYGON,
				polygon1, polygon1Name);

		Polygon polygon2 = new Polygon();
		String polygon2Name = "NGA Visitor Center";
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

		createFeatures(geoPackage, srs, "polygon2", GeometryType.POLYGON,
				polygon2, polygon2Name);

		List<Geometry> geometries1 = new ArrayList<>();
		List<String> geometries1Names = new ArrayList<>();
		geometries1.add(point1);
		geometries1Names.add(point1Name);
		geometries1.add(line1);
		geometries1Names.add(line1Name);
		geometries1.add(polygon1);
		geometries1Names.add(polygon1Name);

		createFeatures(geoPackage, srs, "geometry1", GeometryType.GEOMETRY,
				geometries1, geometries1Names);

		List<Geometry> geometries2 = new ArrayList<>();
		List<String> geometries2Names = new ArrayList<>();
		geometries2.add(point2);
		geometries2Names.add(point2Name);
		geometries2.add(line2);
		geometries2Names.add(line2Name);
		geometries2.add(polygon2);
		geometries2Names.add(polygon2Name);

		createFeatures(geoPackage, srs, "geometry2", GeometryType.GEOMETRY,
				geometries2, geometries2Names);

	}

	private static void createFeatures(GeoPackage geoPackage,
			SpatialReferenceSystem srs, String tableName, GeometryType type,
			Geometry geometry, String name) throws SQLException {

		List<Geometry> geometries = new ArrayList<>();
		geometries.add(geometry);
		List<String> names = new ArrayList<>();
		names.add(name);

		createFeatures(geoPackage, srs, tableName, type, geometries, names);
	}

	private static void createFeatures(GeoPackage geoPackage,
			SpatialReferenceSystem srs, String tableName, GeometryType type,
			List<Geometry> geometries, List<String> names) throws SQLException {

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

		int columnNumber = 0;
		columns.add(FeatureColumn.createPrimaryKeyColumn(columnNumber++,
				ID_COLUMN));
		columns.add(FeatureColumn.createGeometryColumn(columnNumber++,
				GEOMETRY_COLUMN, type, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, TEXT_COLUMN,
				GeoPackageDataType.TEXT, false, ""));
		columns.add(FeatureColumn.createColumn(columnNumber++, REAL_COLUMN,
				GeoPackageDataType.REAL, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, BOOLEAN_COLUMN,
				GeoPackageDataType.BOOLEAN, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, BLOB_COLUMN,
				GeoPackageDataType.BLOB, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, INTEGER_COLUMN,
				GeoPackageDataType.INTEGER, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++,
				TEXT_LIMITED_COLUMN, GeoPackageDataType.TEXT, (long) UUID
						.randomUUID().toString().length(), false, null));
		columns.add(FeatureColumn
				.createColumn(columnNumber++, BLOB_LIMITED_COLUMN,
						GeoPackageDataType.BLOB, (long) UUID.randomUUID()
								.toString().getBytes().length, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, DATE_COLUMN,
				GeoPackageDataType.DATE, false, null));
		columns.add(FeatureColumn.createColumn(columnNumber++, DATETIME_COLUMN,
				GeoPackageDataType.DATETIME, false, null));

		FeatureTable table = new FeatureTable(tableName, columns);
		geoPackage.createFeatureTable(table);

		contentsDao.create(contents);

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();

		GeometryColumns geometryColumns = new GeometryColumns();
		geometryColumns.setContents(contents);
		geometryColumns.setColumnName(GEOMETRY_COLUMN);
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

			newRow.setValue(TEXT_COLUMN, name);
			newRow.setValue(REAL_COLUMN, Math.random() * 5000.0);
			newRow.setValue(BOOLEAN_COLUMN, Math.random() < .5 ? false : true);
			newRow.setValue(BLOB_COLUMN, UUID.randomUUID().toString()
					.getBytes());
			newRow.setValue(INTEGER_COLUMN, (int) (Math.random() * 500));
			newRow.setValue(TEXT_LIMITED_COLUMN, UUID.randomUUID().toString());
			newRow.setValue(BLOB_LIMITED_COLUMN, UUID.randomUUID().toString()
					.getBytes());
			newRow.setValue(DATE_COLUMN, new Date());
			newRow.setValue(DATETIME_COLUMN, new Date());

			dao.create(newRow);

		}

	}

	private static void createTiles(GeoPackage geoPackage) throws IOException,
			SQLException {

		geoPackage.createTileMatrixSetTable();
		geoPackage.createTileMatrixTable();

		BoundingBox bitsBoundingBox = new BoundingBox(-11667347.997449303,
				4824705.2253603265, -11666125.00499674, 4825928.217812888);
		createTiles(geoPackage, "bit_systems", bitsBoundingBox, 15, 17, "png");

		BoundingBox ngaBoundingBox = new BoundingBox(-8593967.964158937,
				4685284.085768163, -8592744.971706374, 4687730.070673289);
		createTiles(geoPackage, "nga", ngaBoundingBox, 15, 16, "png");

	}

	private static void createTiles(GeoPackage geoPackage, String name,
			BoundingBox boundingBox, int minZoomLevel, int maxZoomLevel,
			String extension) throws SQLException, IOException {

		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();
		SpatialReferenceSystem srs = srsDao.getOrCreateCode(
				ProjectionConstants.AUTHORITY_EPSG,
				(long) ProjectionConstants.EPSG_WEB_MERCATOR);

		TileGrid totalTileGrid = TileBoundingBoxUtils.getTileGrid(boundingBox,
				minZoomLevel);
		BoundingBox totalBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(totalTileGrid, minZoomLevel);

		ContentsDao contentsDao = geoPackage.getContentsDao();

		Contents contents = new Contents();
		contents.setTableName(name);
		contents.setDataType(ContentsDataType.TILES);
		contents.setIdentifier(name);
		contents.setDescription(name);
		contents.setMinX(totalBoundingBox.getMinLongitude());
		contents.setMinY(totalBoundingBox.getMinLatitude());
		contents.setMaxX(totalBoundingBox.getMaxLongitude());
		contents.setMaxY(totalBoundingBox.getMaxLatitude());
		contents.setSrs(srs);

		TileTable tileTable = TestUtils.buildTileTable(contents.getTableName());
		geoPackage.createTileTable(tileTable);

		contentsDao.create(contents);

		TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();

		TileMatrixSet tileMatrixSet = new TileMatrixSet();
		tileMatrixSet.setContents(contents);
		tileMatrixSet.setSrs(contents.getSrs());
		tileMatrixSet.setMinX(contents.getMinX());
		tileMatrixSet.setMinY(contents.getMinY());
		tileMatrixSet.setMaxX(contents.getMaxX());
		tileMatrixSet.setMaxY(contents.getMaxY());
		tileMatrixSetDao.create(tileMatrixSet);

		TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();

		final String tilesPath = "tiles/";

		for (int zoom = minZoomLevel; zoom <= maxZoomLevel; zoom++) {

			final String zoomPath = tilesPath + zoom + "/";

			Integer tileWidth = null;
			Integer tileHeight = null;

			TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
					totalBoundingBox, zoom);
			TileDao dao = geoPackage.getTileDao(tileMatrixSet);

			for (long x = tileGrid.getMinX(); x <= tileGrid.getMaxX(); x++) {

				final String xPath = zoomPath + x + "/";

				for (long y = tileGrid.getMinY(); y <= tileGrid.getMaxY(); y++) {

					final String yPath = xPath + y + "." + extension;

					if (TestUtils.class.getResource("/" + yPath) != null) {

						File tileFile = TestUtils.getTestFile(yPath);

						byte[] tileBytes = GeoPackageIOUtils
								.fileBytes(tileFile);

						if (tileWidth == null || tileHeight == null) {
							BufferedImage tileImage = ImageIO.read(tileFile);
							tileHeight = tileImage.getHeight();
							tileWidth = tileImage.getWidth();
						}

						TileRow newRow = dao.newRow();

						newRow.setZoomLevel(zoom);
						newRow.setTileColumn(x - tileGrid.getMinX());
						newRow.setTileRow(y - tileGrid.getMinY());
						newRow.setTileData(tileBytes);

						dao.create(newRow);

					}
				}
			}

			long matrixWidth = tileGrid.getMaxX() - tileGrid.getMinX() + 1;
			long matrixHeight = tileGrid.getMaxY() - tileGrid.getMinY() + 1;
			double pixelXSize = (tileMatrixSet.getMaxX() - tileMatrixSet
					.getMinX()) / (matrixWidth * tileWidth);
			double pixelYSize = (tileMatrixSet.getMaxY() - tileMatrixSet
					.getMinY()) / (matrixHeight * tileHeight);

			TileMatrix tileMatrix = new TileMatrix();
			tileMatrix.setContents(contents);
			tileMatrix.setZoomLevel(zoom);
			tileMatrix.setMatrixWidth(matrixWidth);
			tileMatrix.setMatrixHeight(matrixHeight);
			tileMatrix.setTileWidth(tileWidth);
			tileMatrix.setTileHeight(tileHeight);
			tileMatrix.setPixelXSize(pixelXSize);
			tileMatrix.setPixelYSize(pixelYSize);
			tileMatrixDao.create(tileMatrix);

		}

	}

	private static void createAttributes(GeoPackage geoPackage) {

		List<AttributesColumn> columns = new ArrayList<AttributesColumn>();

		int columnNumber = 1;
		columns.add(AttributesColumn.createColumn(columnNumber++, TEXT_COLUMN,
				GeoPackageDataType.TEXT, false, ""));
		columns.add(AttributesColumn.createColumn(columnNumber++, REAL_COLUMN,
				GeoPackageDataType.REAL, false, null));
		columns.add(AttributesColumn.createColumn(columnNumber++,
				BOOLEAN_COLUMN, GeoPackageDataType.BOOLEAN, false, null));
		columns.add(AttributesColumn.createColumn(columnNumber++, BLOB_COLUMN,
				GeoPackageDataType.BLOB, false, null));
		columns.add(AttributesColumn.createColumn(columnNumber++,
				INTEGER_COLUMN, GeoPackageDataType.INTEGER, false, null));
		columns.add(AttributesColumn.createColumn(columnNumber++,
				TEXT_LIMITED_COLUMN, GeoPackageDataType.TEXT, (long) UUID
						.randomUUID().toString().length(), false, null));
		columns.add(AttributesColumn
				.createColumn(columnNumber++, BLOB_LIMITED_COLUMN,
						GeoPackageDataType.BLOB, (long) UUID.randomUUID()
								.toString().getBytes().length, false, null));
		columns.add(AttributesColumn.createColumn(columnNumber++, DATE_COLUMN,
				GeoPackageDataType.DATE, false, null));
		columns.add(AttributesColumn.createColumn(columnNumber++,
				DATETIME_COLUMN, GeoPackageDataType.DATETIME, false, null));

		AttributesTable attributesTable = geoPackage
				.createAttributesTableWithId("attributes", columns);

		AttributesDao attributesDao = geoPackage
				.getAttributesDao(attributesTable.getTableName());

		for (int i = 0; i < 10; i++) {

			AttributesRow newRow = attributesDao.newRow();

			newRow.setValue(TEXT_COLUMN, UUID.randomUUID().toString());
			newRow.setValue(REAL_COLUMN, Math.random() * 5000.0);
			newRow.setValue(BOOLEAN_COLUMN, Math.random() < .5 ? false : true);
			newRow.setValue(BLOB_COLUMN, UUID.randomUUID().toString()
					.getBytes());
			newRow.setValue(INTEGER_COLUMN, (int) (Math.random() * 500));
			newRow.setValue(TEXT_LIMITED_COLUMN, UUID.randomUUID().toString());
			newRow.setValue(BLOB_LIMITED_COLUMN, UUID.randomUUID().toString()
					.getBytes());
			newRow.setValue(DATE_COLUMN, new Date());
			newRow.setValue(DATETIME_COLUMN, new Date());

			attributesDao.create(newRow);

		}
	}

	private static void createGeometryIndexExtension(GeoPackage geoPackage) {

		List<String> featureTables = geoPackage.getFeatureTables();
		for (String featureTable : featureTables) {

			FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
			FeatureTableIndex featureTableIndex = new FeatureTableIndex(
					geoPackage, featureDao);
			featureTableIndex.index();
		}

	}

	private static void createFeatureTileLinkExtension(GeoPackage geoPackage)
			throws SQLException, IOException {

		List<String> featureTables = geoPackage.getFeatureTables();
		for (String featureTable : featureTables) {

			FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
			FeatureTiles featureTiles = new DefaultFeatureTiles(featureDao);

			FeatureTableIndex featureIndex = new FeatureTableIndex(geoPackage,
					featureDao);
			featureTiles.setFeatureIndex(featureIndex);

			BoundingBox boundingBox = featureDao.getBoundingBox();
			Projection projection = featureDao.getProjection();

			Projection requestProjection = ProjectionFactory
					.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
			ProjectionTransform transform = projection
					.getTransformation(requestProjection);
			BoundingBox requestBoundingBox = transform.transform(boundingBox);

			int zoomLevel = TileBoundingBoxUtils
					.getZoomLevel(requestBoundingBox);
			zoomLevel = Math.min(zoomLevel, 19);

			int minZoom = zoomLevel - 2;
			int maxZoom = zoomLevel + 2;

			TileGenerator tileGenerator = new FeatureTileGenerator(geoPackage,
					featureTable + "_tiles", featureTiles, minZoom, maxZoom,
					requestBoundingBox, requestProjection);

			tileGenerator.generateTiles();
		}
	}

	private static int DATA_COLUMN_CONSTRAINT_INDEX = 0;

	private static void createSchemaExtension(GeoPackage geoPackage)
			throws SQLException {

		// TODO make the feature column values fall within the constraints

		geoPackage.createDataColumnConstraintsTable();

		DataColumnConstraintsDao dao = geoPackage.getDataColumnConstraintsDao();

		DataColumnConstraints sampleRange = new DataColumnConstraints();
		sampleRange.setConstraintName("sampleRange");
		sampleRange.setConstraintType(DataColumnConstraintType.RANGE);
		sampleRange.setMin(BigDecimal.ONE);
		sampleRange.setMinIsInclusive(true);
		sampleRange.setMax(BigDecimal.TEN);
		sampleRange.setMaxIsInclusive(true);
		sampleRange.setDescription("sampleRange description");
		dao.create(sampleRange);

		DataColumnConstraints sampleEnum1 = new DataColumnConstraints();
		sampleEnum1.setConstraintName("sampleEnum");
		sampleEnum1.setConstraintType(DataColumnConstraintType.ENUM);
		sampleEnum1.setValue("1");
		sampleEnum1.setDescription("sampleEnum description");
		dao.create(sampleEnum1);

		DataColumnConstraints sampleEnum3 = new DataColumnConstraints();
		sampleEnum3.setConstraintName(sampleEnum1.getConstraintName());
		sampleEnum3.setConstraintType(DataColumnConstraintType.ENUM);
		sampleEnum3.setValue("3");
		sampleEnum3.setDescription("sampleEnum description");
		dao.create(sampleEnum3);

		DataColumnConstraints sampleEnum5 = new DataColumnConstraints();
		sampleEnum5.setConstraintName(sampleEnum1.getConstraintName());
		sampleEnum5.setConstraintType(DataColumnConstraintType.ENUM);
		sampleEnum5.setValue("5");
		sampleEnum5.setDescription("sampleEnum description");
		dao.create(sampleEnum5);

		DataColumnConstraints sampleEnum7 = new DataColumnConstraints();
		sampleEnum7.setConstraintName(sampleEnum1.getConstraintName());
		sampleEnum7.setConstraintType(DataColumnConstraintType.ENUM);
		sampleEnum7.setValue("7");
		sampleEnum7.setDescription("sampleEnum description");
		dao.create(sampleEnum7);

		DataColumnConstraints sampleEnum9 = new DataColumnConstraints();
		sampleEnum9.setConstraintName(sampleEnum1.getConstraintName());
		sampleEnum9.setConstraintType(DataColumnConstraintType.ENUM);
		sampleEnum9.setValue("9");
		sampleEnum9.setDescription("sampleEnum description");
		dao.create(sampleEnum9);

		DataColumnConstraints sampleGlob = new DataColumnConstraints();
		sampleGlob.setConstraintName("sampleGlob");
		sampleGlob.setConstraintType(DataColumnConstraintType.GLOB);
		sampleGlob.setValue("[1-2][0-9][0-9][0-9]");
		sampleGlob.setDescription("sampleGlob description");
		dao.create(sampleGlob);

		geoPackage.createDataColumnsTable();

		DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();

		List<String> featureTables = geoPackage.getFeatureTables();
		for (String featureTable : featureTables) {

			FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);

			FeatureTable table = featureDao.getTable();
			for (FeatureColumn column : table.getColumns()) {

				if (!column.isPrimaryKey()
						&& column.getDataType() == GeoPackageDataType.INTEGER) {

					DataColumns dataColumns = new DataColumns();
					dataColumns.setContents(featureDao.getGeometryColumns()
							.getContents());
					dataColumns.setColumnName(column.getName());
					dataColumns.setName(featureTable);
					dataColumns.setTitle("TEST_TITLE");
					dataColumns.setDescription("TEST_DESCRIPTION");
					dataColumns.setMimeType("TEST_MIME_TYPE");

					DataColumnConstraintType constraintType = DataColumnConstraintType
							.values()[DATA_COLUMN_CONSTRAINT_INDEX];
					DATA_COLUMN_CONSTRAINT_INDEX++;
					if (DATA_COLUMN_CONSTRAINT_INDEX >= DataColumnConstraintType
							.values().length) {
						DATA_COLUMN_CONSTRAINT_INDEX = 0;
					}

					String contraintName = null;
					switch (constraintType) {
					case RANGE:
						contraintName = sampleRange.getConstraintName();
						break;
					case ENUM:
						contraintName = sampleEnum1.getConstraintName();
						break;
					case GLOB:
						contraintName = sampleGlob.getConstraintName();
						break;
					}
					dataColumns.setConstraintName(contraintName);

					dataColumnsDao.create(dataColumns);

					break;
				}
			}
		}
	}

}
