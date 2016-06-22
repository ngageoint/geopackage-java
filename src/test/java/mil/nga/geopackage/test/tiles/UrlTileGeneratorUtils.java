package mil.nga.geopackage.test.tiles;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;

import junit.framework.TestCase;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.test.io.TestGeoPackageProgress;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGenerator;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.UrlTileGenerator;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.wkb.geom.Point;

/**
 * URL Tile Generator utils
 * 
 * @author osbornb
 */
public class UrlTileGeneratorUtils {

	private static final String TABLE_NAME = "generate_test";
	private static final String URL = "http://osm.geointservices.io/osm_tiles/{z}/{x}/{y}.png";

	/**
	 * Test generating tiles
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void testGenerateTiles(GeoPackage geoPackage)
			throws SQLException, IOException {

		UrlTileGenerator tileGenerator = new UrlTileGenerator(geoPackage,
				TABLE_NAME, URL, 1, 2, getBoundingBox(), getProjection());

		testGenerateTiles(tileGenerator);
	}

	/**
	 * Test generating tiles with jpeg compression
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void testGenerateTilesCompress(GeoPackage geoPackage)
			throws SQLException, IOException {

		UrlTileGenerator tileGenerator = new UrlTileGenerator(geoPackage,
				TABLE_NAME, URL, 2, 3, getBoundingBox(), getProjection());
		tileGenerator.setCompressFormat("jpeg");

		testGenerateTiles(tileGenerator);
	}

	/**
	 * Test generating tiles with jpeg compression and quality
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void testGenerateTilesCompressQuality(GeoPackage geoPackage)
			throws SQLException, IOException {

		UrlTileGenerator tileGenerator = new UrlTileGenerator(geoPackage,
				TABLE_NAME, URL, 0, 1, getBoundingBox(), getProjection());
		tileGenerator.setCompressFormat("jpeg");
		tileGenerator.setCompressQuality(.7f);

		testGenerateTiles(tileGenerator);
	}

	/**
	 * Test generating tiles in Google format
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void testGenerateTilesGoogle(GeoPackage geoPackage)
			throws SQLException, IOException {

		UrlTileGenerator tileGenerator = new UrlTileGenerator(geoPackage,
				TABLE_NAME, URL, 1, 3, getBoundingBox(), getProjection());
		tileGenerator.setGoogleTiles(true);

		testGenerateTiles(tileGenerator);
	}

	/**
	 * Test generating tiles with bounding box
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void testGenerateTilesBounded(GeoPackage geoPackage)
			throws SQLException, IOException {

		UrlTileGenerator tileGenerator = new UrlTileGenerator(geoPackage,
				TABLE_NAME, URL, 1, 2, new BoundingBox(-10, 10, -10, 10),
				getProjection());

		testGenerateTiles(tileGenerator);
	}

	/**
	 * Test generating tiles in Google format with bounding box
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void testGenerateTilesGoogleBounded(GeoPackage geoPackage)
			throws SQLException, IOException {

		UrlTileGenerator tileGenerator = new UrlTileGenerator(geoPackage,
				TABLE_NAME, URL, 1, 2, new BoundingBox(-10, 10, -10, 10),
				getProjection());
		tileGenerator.setGoogleTiles(true);

		testGenerateTiles(tileGenerator);
	}

	/**
	 * Test generating tiles with random bounds and zoomss
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void testGenerateTilesRandom(GeoPackage geoPackage)
			throws SQLException, IOException {

		for (int i = 0; i < 10; i++) {

			int minZoom = (int) (Math.random() * 3.0);
			int maxZoom = minZoom + ((int) (Math.random() * 3.0));
			Point point1 = TestUtils.createPoint(false, false);
			Point point2 = TestUtils.createPoint(false, false);
			BoundingBox boundingBox = new BoundingBox(Math.min(point1.getX(),
					point2.getX()), Math.max(point1.getX(), point2.getX()),
					Math.min(point1.getY(), point2.getY()), Math.max(
							point1.getY(), point2.getY()));
			UrlTileGenerator tileGenerator = new UrlTileGenerator(geoPackage,
					TABLE_NAME + i, URL, minZoom, maxZoom, boundingBox,
					getProjection());

			testGenerateTiles(tileGenerator);
		}
	}

	/**
	 * Test generating tiles with unsupported png compression and quality
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void testGenerateTilesUnsupportedCompressQuality(
			GeoPackage geoPackage) throws SQLException, IOException {

		UrlTileGenerator tileGenerator = new UrlTileGenerator(geoPackage,
				TABLE_NAME, URL, 0, 1, getBoundingBox(), getProjection());
		tileGenerator.setCompressFormat("png");
		tileGenerator.setCompressQuality(.7f);

		int count = tileGenerator.generateTiles();
		TestCase.assertEquals(0, count);
	}

	private static BoundingBox getBoundingBox() {
		BoundingBox boundingBox = new BoundingBox();
		boundingBox = getBoundingBox(boundingBox);
		return boundingBox;
	}

	private static BoundingBox getBoundingBox(BoundingBox boundingBox) {
		boundingBox = TileBoundingBoxUtils
				.boundWgs84BoundingBoxWithWebMercatorLimits(boundingBox);
		boundingBox = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)
				.getTransformation(ProjectionConstants.EPSG_WEB_MERCATOR)
				.transform(boundingBox);
		return boundingBox;
	}

	private static Projection getProjection() {
		return ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
	}

	/**
	 * Test generating tiles
	 * 
	 * @param tileGenerator
	 * @throws SQLException
	 * @throws IOException
	 */
	private static void testGenerateTiles(TileGenerator tileGenerator)
			throws SQLException, IOException {

		GeoPackage geoPackage = tileGenerator.getGeoPackage();
		String tableName = tileGenerator.getTableName();
		int minZoom = tileGenerator.getMinZoom();
		int maxZoom = tileGenerator.getMaxZoom();
		BoundingBox webMercatorBoundingBox = tileGenerator.getBoundingBox();

		TestGeoPackageProgress progress = new TestGeoPackageProgress();
		tileGenerator.setProgress(progress);

		int count = tileGenerator.generateTiles();

		long expected = expectedTiles(webMercatorBoundingBox, minZoom, maxZoom);
		TestCase.assertEquals(expected, count);
		TestCase.assertEquals(expected, progress.getProgress());

		TileDao tileDao = geoPackage.getTileDao(tableName);
		TestCase.assertEquals(expected, tileDao.count());
		TestCase.assertEquals(minZoom, tileDao.getMinZoom());
		TestCase.assertEquals(maxZoom, tileDao.getMaxZoom());

		BoundingBox tileMatrixSetBoundingBox = tileDao.getBoundingBox();

		for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
			TileGrid expectedTileGrid = TileBoundingBoxUtils.getTileGrid(
					webMercatorBoundingBox, zoom);
			BoundingBox expectedBoundingBox = TileBoundingBoxUtils
					.getWebMercatorBoundingBox(expectedTileGrid, zoom);
			BoundingBox zoomBoundingBox = tileDao.getBoundingBox(zoom);
			TestCase.assertEquals(expectedBoundingBox.getMinLongitude(),
					zoomBoundingBox.getMinLongitude(), .000001);
			TestCase.assertEquals(expectedBoundingBox.getMaxLongitude(),
					zoomBoundingBox.getMaxLongitude(), .000001);
			TestCase.assertEquals(expectedBoundingBox.getMinLatitude(),
					zoomBoundingBox.getMinLatitude(), .000001);
			TestCase.assertEquals(expectedBoundingBox.getMaxLatitude(),
					zoomBoundingBox.getMaxLatitude(), .000001);
			long expectedZoomTiles = expectedTiles(webMercatorBoundingBox, zoom);
			TestCase.assertEquals(expectedZoomTiles, tileDao.count(zoom));

			TileMatrix tileMatrix = tileDao.getTileMatrix(zoom);

			TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
					tileMatrixSetBoundingBox, tileMatrix.getMatrixWidth(),
					tileMatrix.getMatrixHeight(), zoomBoundingBox);

			TestCase.assertTrue(tileGrid.getMinX() >= 0);
			TestCase.assertTrue(tileGrid.getMaxX() < tileMatrix
					.getMatrixWidth());
			TestCase.assertTrue(tileGrid.getMinY() >= 0);
			TestCase.assertTrue(tileGrid.getMaxY() < tileMatrix
					.getMatrixHeight());

			TileResultSet resultSet = tileDao.queryForTile(zoom);
			TestCase.assertEquals(expectedZoomTiles, resultSet.getCount());
			int resultCount = 0;
			while (resultSet.moveToNext()) {
				TileRow tileRow = resultSet.getRow();
				resultCount++;
				byte[] tileData = tileRow.getTileData();
				TestCase.assertNotNull(tileData);
				BufferedImage image = tileRow.getTileDataImage();
				TestCase.assertNotNull(image);
				TestCase.assertEquals(tileMatrix.getTileWidth(),
						image.getWidth());
				TestCase.assertEquals(tileMatrix.getTileHeight(),
						image.getHeight());
			}
			TestCase.assertEquals(expectedZoomTiles, resultCount);
		}

	}

	/**
	 * Expected number of XYZ tiles between zoom range and bounding box
	 * 
	 * @param webMercatorBoundingBox
	 * @param minZoom
	 * @param maxZoom
	 * @return
	 */
	private static long expectedTiles(BoundingBox webMercatorBoundingBox,
			int minZoom, int maxZoom) {
		long tiles = 0;
		for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
			tiles += expectedTiles(webMercatorBoundingBox, zoom);
		}
		return tiles;
	}

	/**
	 * Expected number of XYZ tiles at zoom and bounding box
	 * 
	 * @param webMercatorBoundingBox
	 * @param zoom
	 * @return
	 */
	private static long expectedTiles(BoundingBox webMercatorBoundingBox,
			int zoom) {
		TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
				webMercatorBoundingBox, zoom);
		return tileGrid.count();
	}

}
