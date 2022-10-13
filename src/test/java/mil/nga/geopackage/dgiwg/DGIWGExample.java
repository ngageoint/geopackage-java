package mil.nga.geopackage.dgiwg;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.TestConstants;
import mil.nga.geopackage.TestUtils;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.sf.GeometryType;
import mil.nga.sf.LineString;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;

/**
 * Creates an example DGIWG GeoPackage file
 * 
 * @author osbornb
 */
public class DGIWGExample {

	private static final boolean FEATURES = true;
	private static final boolean TILES = true;

	private static final String PRODUCER = "NGA";
	private static final String DATA_PRODUCT = "DGIWG-Example";
	private static final String GEOGRAPHIC_COVERAGE_AREA = "USA";
	private static final int MIN_ZOOM = 15;
	private static final int MAX_ZOOM = 16;
	private static final int MAJOR_VERSION = 1;
	private static final int MINOR_VERSION = 0;

	/**
	 * Main method to create the GeoPackage example file
	 * 
	 * @param args
	 *            arguments
	 * @throws IOException
	 *             upon error
	 */
	public static void main(String[] args) throws IOException {

		DGIWGExampleCreate create = DGIWGExampleCreate.base();
		create.features = FEATURES;
		create.tiles = TILES;

		create(getFileName(), create);
	}

	/**
	 * Get the file name
	 * 
	 * @return file name
	 */
	public static GeoPackageFileName getFileName() {

		GeoPackageFileName fileName = new GeoPackageFileName();

		fileName.setProducer(PRODUCER);
		fileName.setDataProduct(DATA_PRODUCT);
		fileName.setGeographicCoverageArea(GEOGRAPHIC_COVERAGE_AREA);
		fileName.setZoomLevelRange(MIN_ZOOM, MAX_ZOOM);
		fileName.setVersion(MAJOR_VERSION, MINOR_VERSION);
		fileName.setCreationDate(new Date());

		return fileName;
	}

	/**
	 * Test making the base GeoPackage example
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testExampleBase() throws IOException {
		testExample(DGIWGExampleCreate.base());
	}

	/**
	 * Test making the GeoPackage example with all parts
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testExample() throws IOException {
		testExample(DGIWGExampleCreate.all());
	}

	/**
	 * Test making the GeoPackage example with features
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testExampleFeatures() throws IOException {
		testExample(DGIWGExampleCreate.features());
	}

	/**
	 * Test making the GeoPackage example with tiles
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testExampleTiles() throws IOException {
		testExample(DGIWGExampleCreate.tiles());
	}

	/**
	 * Test making the GeoPackage example
	 * 
	 * @param create
	 *            create parts
	 * @throws IOException
	 *             upon error
	 */
	private void testExample(DGIWGExampleCreate create) throws IOException {

		GeoPackageFileName fileName = getFileName();

		GeoPackageFile file = create(fileName, create);

		GeoPackage geoPackage = DGIWGGeoPackageManager.open(file);
		TestCase.assertNotNull(geoPackage);
		geoPackage.close();

		TestCase.assertTrue(file.getFile().delete());
	}

	/**
	 * Create the GeoPackage example file
	 * 
	 * @param fileName
	 *            file name
	 * @param create
	 *            create parts
	 * @return GeoPackage file
	 * @throws IOException
	 *             upon error
	 */
	private static GeoPackageFile create(GeoPackageFileName fileName,
			DGIWGExampleCreate create) throws IOException {

		System.out.println("Creating: " + fileName.getName());
		DGIWGGeoPackage geoPackage = createGeoPackage(fileName);

		System.out.println("Features: " + create.features);
		if (create.features) {

			createFeatures(geoPackage);

		}

		System.out.println("Tiles: " + create.tiles);
		if (create.tiles) {

			createTiles(geoPackage);

		}

		DGIWGValidationErrors errors = geoPackage.validate();
		if (errors.hasErrors()) {
			System.out.println(errors);
		}
		assertTrue(geoPackage.isValid());

		geoPackage.close();
		System.out.println("Created: " + geoPackage.getPath());

		return geoPackage.getFile();
	}

	/**
	 * Create the GeoPackage
	 * 
	 * @param fileName
	 *            file name
	 * @return GeoPackage
	 * @throws IOException
	 *             upon error
	 */
	private static DGIWGGeoPackage createGeoPackage(GeoPackageFileName fileName)
			throws IOException {

		File file = fileName.getFile();
		if (file.exists()) {
			file.delete();
		}

		GeoPackageFile geoPackageFile = DGIWGGeoPackageManager.create(fileName,
				getMetadata());

		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager
				.open(geoPackageFile);

		return geoPackage;
	}

	/**
	 * Get the example metadata
	 * 
	 * @return metadata
	 * @throws IOException
	 *             upon error
	 */
	public static String getMetadata() throws IOException {
		File metadataFile = TestUtils.getTestFile(TestConstants.DGIWG_METADATA);
		String metadata = GeoPackageIOUtils.fileString(metadataFile);
		return metadata;
	}

	/**
	 * Create features
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	private static void createFeatures(DGIWGGeoPackage geoPackage) {

		String table = "nga_features";
		String identifier = "NGA Features";
		String description = "DGIWG Features example";
		String nameColumn = "name";

		CoordinateReferenceSystem crs = CoordinateReferenceSystem.EPSG_4326;

		List<FeatureColumn> columns = new ArrayList<>();
		columns.add(FeatureColumn.createColumn(nameColumn,
				GeoPackageDataType.TEXT));
		GeometryColumns geometryColumns = geoPackage.createFeatures(table,
				identifier, description, GeometryType.GEOMETRY, columns, crs);
		long srsId = geometryColumns.getSrsId();

		FeatureDao featureDao = geoPackage.getFeatureDao(geometryColumns);

		Point point = new Point(-77.196736, 38.753370);
		FeatureRow pointRow = featureDao.newRow();
		pointRow.setGeometry(GeoPackageGeometryData.create(srsId, point));
		pointRow.setValue(nameColumn, "NGA");
		featureDao.insert(pointRow);

		LineString line = new LineString();
		line.addPoint(new Point(-77.196650, 38.756501));
		line.addPoint(new Point(-77.196414, 38.755979));
		line.addPoint(new Point(-77.195518, 38.755208));
		line.addPoint(new Point(-77.195303, 38.755272));
		line.addPoint(new Point(-77.195351, 38.755459));
		line.addPoint(new Point(-77.195863, 38.755697));
		line.addPoint(new Point(-77.196328, 38.756069));
		line.addPoint(new Point(-77.196568, 38.756526));
		FeatureRow lineRow = featureDao.newRow();
		lineRow.setGeometry(GeoPackageGeometryData.create(srsId, line));
		lineRow.setValue(nameColumn, "NGA Visitor Center Road");
		featureDao.insert(lineRow);

		Polygon polygon = new Polygon();
		LineString ring = new LineString();
		ring.addPoint(new Point(-77.195299, 38.755159));
		ring.addPoint(new Point(-77.195203, 38.755080));
		ring.addPoint(new Point(-77.195410, 38.754930));
		ring.addPoint(new Point(-77.195350, 38.754884));
		ring.addPoint(new Point(-77.195228, 38.754966));
		ring.addPoint(new Point(-77.195135, 38.754889));
		ring.addPoint(new Point(-77.195048, 38.754956));
		ring.addPoint(new Point(-77.194986, 38.754906));
		ring.addPoint(new Point(-77.194897, 38.754976));
		ring.addPoint(new Point(-77.194953, 38.755025));
		ring.addPoint(new Point(-77.194763, 38.755173));
		ring.addPoint(new Point(-77.194827, 38.755224));
		ring.addPoint(new Point(-77.195012, 38.755082));
		ring.addPoint(new Point(-77.195041, 38.755104));
		ring.addPoint(new Point(-77.195028, 38.755116));
		ring.addPoint(new Point(-77.195090, 38.755167));
		ring.addPoint(new Point(-77.195106, 38.755154));
		ring.addPoint(new Point(-77.195205, 38.755233));
		ring.addPoint(new Point(-77.195299, 38.755159));
		polygon.addRing(ring);
		FeatureRow polygonRow = featureDao.newRow();
		polygonRow.setGeometry(GeoPackageGeometryData.create(srsId, polygon));
		polygonRow.setValue(nameColumn, "NGA Visitor Center");
		featureDao.insert(polygonRow);

	}

	/**
	 * Create tiles
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @throws IOException
	 *             upon error
	 */
	private static void createTiles(DGIWGGeoPackage geoPackage)
			throws IOException {

		String table = "nga_tiles";
		String identifier = "NGA Tiles";
		String description = "DGIWG Tiles example";

		CoordinateReferenceSystem crs = CoordinateReferenceSystem.EPSG_3857;

		BoundingBox informativeBounds = new BoundingBox(-8593967, 4685285,
				-8592745, 4687730);

		TileGrid totalTileGrid = TileBoundingBoxUtils
				.getTileGrid(informativeBounds, MIN_ZOOM);
		BoundingBox extentBounds = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(totalTileGrid, MIN_ZOOM);

		TileMatrixSet tileMatrixSet = geoPackage.createTiles(table, identifier,
				description, informativeBounds, crs, extentBounds);

		long matrixWidth = totalTileGrid.getWidth();
		long matrixHeight = totalTileGrid.getHeight();

		geoPackage.createTileMatrices(tileMatrixSet, MIN_ZOOM, MAX_ZOOM,
				matrixWidth, matrixHeight);

		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

		TileGrid tileGrid = totalTileGrid;

		final String tilesPath = "tiles/";

		for (int zoom = MIN_ZOOM; zoom <= MAX_ZOOM; zoom++) {

			final String zoomPath = tilesPath + zoom + "/";

			for (long x = tileGrid.getMinX(); x <= tileGrid.getMaxX(); x++) {

				final String xPath = zoomPath + x + "/";

				for (long y = tileGrid.getMinY(); y <= tileGrid
						.getMaxY(); y++) {

					final String yPath = xPath + y + "."
							+ ImageUtils.IMAGE_FORMAT_PNG;

					if (TestUtils.class.getResource("/" + yPath) != null) {

						File tileFile = TestUtils.getTestFile(yPath);

						byte[] tileBytes = GeoPackageIOUtils
								.fileBytes(tileFile);

						TileRow newRow = tileDao.newRow();

						newRow.setZoomLevel(zoom);
						newRow.setTileColumn(x - tileGrid.getMinX());
						newRow.setTileRow(y - tileGrid.getMinY());
						newRow.setTileData(tileBytes);

						tileDao.create(newRow);

					}
				}
			}

			tileGrid = TileBoundingBoxUtils.tileGridZoomIncrease(tileGrid, 1);
		}

	}

}
