package mil.nga.geopackage.test.tiles.reproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.test.io.TestGeoPackageProgress;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.reproject.TileReprojection;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;

/**
 * Tile Reprojection Utility test methods
 * 
 * @author osbornb
 */
public class TileReprojectionUtils {

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public static void testReproject(GeoPackage geoPackage) {

		for (String table : randomTileTables(geoPackage)) {

			String reprojectTable = table + "_reproject";
			Projection projection = geoPackage.getProjection(table);
			Projection reprojectProjection = alternateProjection(projection);

			TileDao tileDao = geoPackage.getTileDao(table);
			int count = tileDao.count();
			Map<Long, Integer> counts = zoomCounts(tileDao);

			int tiles = TileReprojection.reproject(geoPackage, table,
					reprojectTable, reprojectProjection);

			assertEquals(count > 0, tiles > 0);

			assertTrue(projection.equals(geoPackage.getProjection(table)));
			assertTrue(reprojectProjection
					.equals(geoPackage.getProjection(reprojectTable)));

			tileDao = geoPackage.getTileDao(table);
			compareZoomCounts(count, counts, tileDao);

			TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
			checkZoomCounts(count, counts, reprojectTileDao, tiles);

			Set<Long> zoomLevels = new HashSet<>(tileDao.getZoomLevels());
			Set<Long> reprojectZoomLevels = reprojectTileDao.getZoomLevels();
			zoomLevels.removeAll(reprojectZoomLevels);
			assertEquals(0, zoomLevels.size());

			compareBoundingBox(
					geoPackage.getBoundingBox(reprojectProjection, table),
					geoPackage.getContentsBoundingBox(reprojectTable),
					.0000001);
		}

	}

	/**
	 * Test reproject replacing the table
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public static void testReprojectReplace(GeoPackage geoPackage) {

		for (String table : randomTileTables(geoPackage)) {

			Projection projection = geoPackage.getProjection(table);
			Projection reprojectProjection = alternateProjection(projection);

			BoundingBox boundingBox = geoPackage
					.getBoundingBox(reprojectProjection, table);

			TileDao tileDao = geoPackage.getTileDao(table);
			Set<Long> zoomLevels = new HashSet<>(tileDao.getZoomLevels());
			int count = tileDao.count();
			Map<Long, Integer> counts = zoomCounts(tileDao);

			int tiles = TileReprojection.reproject(geoPackage, table,
					reprojectProjection);

			assertEquals(count > 0, tiles > 0);

			assertTrue(reprojectProjection
					.equals(geoPackage.getProjection(table)));

			TileDao reprojectTileDao = geoPackage.getTileDao(table);
			checkZoomCounts(count, counts, reprojectTileDao, tiles);

			Set<Long> reprojectZoomLevels = reprojectTileDao.getZoomLevels();
			zoomLevels.removeAll(reprojectZoomLevels);
			assertEquals(0, zoomLevels.size());

			compareBoundingBox(boundingBox,
					geoPackage.getContentsBoundingBox(table), .0000001);
		}

	}

	/**
	 * Test reproject of individual zoom levels
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public static void testReprojectZoomLevels(GeoPackage geoPackage) {

		for (String table : randomTileTables(geoPackage)) {

			String reprojectTable = table + "_reproject";
			Projection projection = geoPackage.getProjection(table);
			Projection reprojectProjection = alternateProjection(projection);

			TileDao tileDao = geoPackage.getTileDao(table);
			Map<Long, Integer> counts = zoomCounts(tileDao);

			TileReprojection tileReprojection = TileReprojection.create(
					geoPackage, table, reprojectTable, reprojectProjection);

			for (long zoom : counts.keySet()) {

				int tiles = tileReprojection.reproject(zoom);
				assertEquals(counts.get(zoom) > 0, tiles > 0);
			}

			assertTrue(projection.equals(geoPackage.getProjection(table)));
			assertTrue(reprojectProjection
					.equals(geoPackage.getProjection(reprojectTable)));

			Set<Long> zoomLevels = new HashSet<>(tileDao.getZoomLevels());
			TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
			Set<Long> reprojectZoomLevels = reprojectTileDao.getZoomLevels();
			zoomLevels.removeAll(reprojectZoomLevels);
			assertEquals(0, zoomLevels.size());

			compareBoundingBox(
					geoPackage.getBoundingBox(reprojectProjection, table),
					geoPackage.getContentsBoundingBox(reprojectTable),
					.0000001);
		}

	}

	/**
	 * Test reproject of overwriting a zoom level
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @throws IOException
	 *             upon error
	 */
	public static void testReprojectZoomOverwrite(GeoPackage geoPackage)
			throws IOException {

		for (String table : randomTileTables(geoPackage)) {

			String reprojectTable = table + "_reproject";
			Projection projection = geoPackage.getProjection(table);
			Projection reprojectProjection = alternateProjection(projection);

			TileDao tileDao = geoPackage.getTileDao(table);
			Map<Long, Integer> counts = zoomCounts(tileDao);

			long zoom = (new ArrayList<>(counts.keySet()))
					.get((int) (Math.random() * counts.size()));
			TileMatrix tileMatrix = tileDao.getTileMatrix(zoom);

			TileReprojection tileReprojection = TileReprojection.create(
					geoPackage, table, reprojectTable, reprojectProjection);

			int tiles = tileReprojection.reproject(zoom);
			assertEquals(counts.get(zoom) > 0, tiles > 0);

			tileReprojection = TileReprojection.create(geoPackage, table,
					reprojectTable, reprojectProjection);

			int tiles2 = tileReprojection.reproject(zoom);
			assertEquals(tiles, tiles2);

			tileReprojection = TileReprojection.create(geoPackage, table,
					reprojectTable, reprojectProjection);
			tileReprojection.setTileWidth(tileMatrix.getTileWidth() * 2);
			tileReprojection.setTileHeight(tileMatrix.getTileHeight() * 2);

			try {
				tileReprojection.reproject(zoom);
				fail("Reprojection of existing zoom level with new geographic properties did not fail");
			} catch (Exception e) {
				// expected
			}

			tileReprojection.setOverwrite(true);
			int tiles3 = tileReprojection.reproject(zoom);
			assertEquals(tiles, tiles3);

			Set<Long> zoomLevels = new HashSet<>(tileDao.getZoomLevels());
			TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
			Set<Long> reprojectZoomLevels = reprojectTileDao.getZoomLevels();
			assertTrue(zoomLevels.contains(zoom));
			assertEquals(1, reprojectZoomLevels.size());
			assertTrue(reprojectZoomLevels.contains(zoom));

			TileMatrix reprojectTileMatrix = reprojectTileDao
					.getTileMatrix(zoom);
			assertEquals(zoom, reprojectTileMatrix.getZoomLevel());
			assertEquals(tileReprojection.getTileWidth().longValue(),
					reprojectTileMatrix.getTileWidth());
			assertEquals(tileReprojection.getTileHeight().longValue(),
					reprojectTileMatrix.getTileHeight());
			assertEquals(tileMatrix.getMatrixWidth(),
					reprojectTileMatrix.getMatrixWidth());
			assertEquals(tileMatrix.getMatrixHeight(),
					reprojectTileMatrix.getMatrixHeight());

			TileResultSet tileResults = reprojectTileDao.queryForAll();
			assertTrue(tileResults.moveToNext());
			TileRow tileRow = tileResults.getRow();
			BufferedImage tileImage = tileRow.getTileDataImage();
			assertEquals(tileReprojection.getTileWidth().intValue(),
					tileImage.getWidth());
			assertEquals(tileReprojection.getTileHeight().intValue(),
					tileImage.getHeight());
			tileResults.close();

			compareBoundingBox(
					geoPackage.getBoundingBox(reprojectProjection, table),
					geoPackage.getContentsBoundingBox(reprojectTable),
					.0000001);
		}

	}

	/**
	 * Test reproject of overwriting a table
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @throws SQLException
	 *             upon error
	 */
	public static void testReprojectOverwrite(GeoPackage geoPackage)
			throws SQLException {

		for (String table : randomTileTables(geoPackage)) {

			String reprojectTable = table + "_reproject";
			Projection projection = geoPackage.getProjection(table);
			Projection reprojectProjection = alternateProjection(projection);

			TileDao tileDao = geoPackage.getTileDao(table);
			int count = tileDao.count();
			Map<Long, Integer> counts = zoomCounts(tileDao);

			int tiles = TileReprojection.reproject(geoPackage, table,
					reprojectTable, reprojectProjection);

			assertEquals(count > 0, tiles > 0);

			assertTrue(projection.equals(geoPackage.getProjection(table)));
			assertTrue(reprojectProjection
					.equals(geoPackage.getProjection(reprojectTable)));

			tileDao = geoPackage.getTileDao(table);
			compareZoomCounts(count, counts, tileDao);

			TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
			checkZoomCounts(count, counts, reprojectTileDao, tiles);

			int tiles2 = TileReprojection.reproject(geoPackage, table,
					reprojectTable, reprojectProjection);

			assertEquals(tiles, tiles2);

			TileMatrixSet tileMatrixSet = reprojectTileDao.getTileMatrixSet();
			double multiplier = 0.5;
			tileMatrixSet.setMinX(tileMatrixSet.getMinX() * multiplier);
			tileMatrixSet.setMinY(tileMatrixSet.getMinY() * multiplier);
			tileMatrixSet.setMaxX(tileMatrixSet.getMaxX() * multiplier);
			tileMatrixSet.setMaxY(tileMatrixSet.getMaxY() * multiplier);
			reprojectTileDao.getTileMatrixSetDao().update(tileMatrixSet);

			TileReprojection tileReprojection = TileReprojection.create(
					geoPackage, table, reprojectTable, reprojectProjection);

			try {
				tileReprojection.reproject();
				fail("Reprojection of existing table with new geographic properties did not fail");
			} catch (Exception e) {
				// expected
			}

			tileReprojection.setOverwrite(true);
			tileReprojection.reproject();

			tileMatrixSet.setMinX(tileMatrixSet.getMinX() / multiplier);
			tileMatrixSet.setMinY(tileMatrixSet.getMinY() / multiplier);
			tileMatrixSet.setMaxX(tileMatrixSet.getMaxX() / multiplier);
			tileMatrixSet.setMaxY(tileMatrixSet.getMaxY() / multiplier);
			reprojectTileDao.getTileMatrixSetDao().update(tileMatrixSet);

			tileReprojection = TileReprojection.create(geoPackage, table,
					reprojectTable, reprojectProjection);

			try {
				tileReprojection.reproject();
				fail("Reprojection of existing table with new geographic properties did not fail");
			} catch (Exception e) {
				// expected
			}

			tileReprojection.setOverwrite(true);
			TestGeoPackageProgress progress = new TestGeoPackageProgress();
			tileReprojection.setProgress(progress);
			int tiles3 = tileReprojection.reproject();
			assertEquals(tiles3, progress.getProgress());
			assertEquals(tiles, tiles3);

			Set<Long> zoomLevels = new HashSet<>(tileDao.getZoomLevels());
			Set<Long> reprojectZoomLevels = reprojectTileDao.getZoomLevels();
			zoomLevels.removeAll(reprojectZoomLevels);
			assertEquals(0, zoomLevels.size());

			compareBoundingBox(
					geoPackage.getBoundingBox(reprojectProjection, table),
					geoPackage.getContentsBoundingBox(reprojectTable),
					.0000001);
		}

	}

	/**
	 * Test reproject with zoom level mappings
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public static void testReprojectToZoom(GeoPackage geoPackage) {

		for (String table : randomTileTables(geoPackage)) {

			String reprojectTable = table + "_reproject";
			Projection projection = geoPackage.getProjection(table);
			Projection reprojectProjection = alternateProjection(projection);

			TileDao tileDao = geoPackage.getTileDao(table);
			int count = tileDao.count();
			Map<Long, Integer> counts = zoomCounts(tileDao);

			TileReprojection tileReprojection = TileReprojection.create(
					geoPackage, table, reprojectTable, reprojectProjection);

			Map<Long, Long> zoomMap = new HashMap<>();

			List<Long> zoomLevels = new ArrayList<>(tileDao.getZoomLevels());
			long fromZoom = zoomLevels.get(0);
			long toZoom = Math.max(fromZoom - 2, 0);
			tileReprojection.setToZoom(fromZoom, toZoom);
			zoomMap.put(toZoom, fromZoom);

			for (int i = 1; i < zoomLevels.size(); i++) {
				fromZoom = zoomLevels.get(i);
				toZoom += 2;
				tileReprojection.setToZoom(fromZoom, toZoom);
				zoomMap.put(toZoom, fromZoom);
			}

			int tiles = tileReprojection.reproject();

			assertEquals(count > 0, tiles > 0);

			assertTrue(projection.equals(geoPackage.getProjection(table)));
			assertTrue(reprojectProjection
					.equals(geoPackage.getProjection(reprojectTable)));

			tileDao = geoPackage.getTileDao(table);
			compareZoomCounts(count, counts, tileDao);

			TileDao reprojectTileDao = geoPackage.getTileDao(reprojectTable);
			assertEquals(count > 0, reprojectTileDao.count() > 0);
			assertEquals(tiles, reprojectTileDao.count());
			Map<Long, Integer> countsAfter = zoomCounts(reprojectTileDao);
			assertEquals(counts.size(), countsAfter.size());
			for (long toZoomLevel : reprojectTileDao.getZoomLevels()) {
				long fromZoomLevel = zoomMap.get(toZoomLevel);
				assertEquals(counts.get(fromZoomLevel) > 0,
						countsAfter.get(toZoomLevel) > 0);
			}

			List<Long> fromZoomLevels = new ArrayList<>(
					tileDao.getZoomLevels());
			List<Long> reprojectZoomLevels = new ArrayList<>(
					reprojectTileDao.getZoomLevels());
			assertEquals(fromZoomLevels.size(), reprojectZoomLevels.size());
			fromZoomLevels.removeAll(zoomMap.values());
			reprojectZoomLevels.removeAll(zoomMap.keySet());
			assertEquals(0, fromZoomLevels.size());
			assertEquals(0, reprojectZoomLevels.size());

			compareBoundingBox(
					geoPackage.getBoundingBox(reprojectProjection, table),
					geoPackage.getContentsBoundingBox(reprojectTable),
					.0000001);
		}

	}

	/**
	 * Test reproject with zoom level matrix and tile length configurations
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public static void testReprojectMatrixAndTileLengths(
			GeoPackage geoPackage) {
		// TODO
	}

	/**
	 * Test reproject with tile optimization
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param world
	 *            world bounds
	 */
	public static void testReprojectOptimize(GeoPackage geoPackage,
			boolean world) {
		// TODO
	}

	/**
	 * Test reproject with web mercator tile optimization
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param world
	 *            world bounds
	 */
	public static void testReprojectWebMercator(GeoPackage geoPackage,
			boolean world) {
		// TODO
	}

	/**
	 * Test reproject with platte carre tile optimization
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param world
	 *            world bounds
	 */
	public static void testReprojectPlatteCarre(GeoPackage geoPackage,
			boolean world) {
		// TODO
	}

	/**
	 * Test reproject cancel
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public static void testReprojectCancel(GeoPackage geoPackage) {
		// TODO
	}

	private static void compareBoundingBox(BoundingBox boundingBox1,
			BoundingBox boundingBox2, TileMatrix tileMatrix) {
		double longitudeDelta = tileMatrix.getPixelXSize();
		double latitudeDelta = tileMatrix.getPixelYSize();
		compareBoundingBox(boundingBox1, boundingBox2, longitudeDelta,
				latitudeDelta);
	}

	private static void compareBoundingBox(BoundingBox boundingBox1,
			BoundingBox boundingBox2, double delta) {
		compareBoundingBox(boundingBox1, boundingBox2, delta, delta);
	}

	private static void compareBoundingBox(BoundingBox boundingBox1,
			BoundingBox boundingBox2, double longitudeDelta,
			double latitudeDelta) {
		assertEquals(boundingBox1.getMinLongitude(),
				boundingBox2.getMinLongitude(), longitudeDelta);
		assertEquals(boundingBox1.getMinLatitude(),
				boundingBox2.getMinLatitude(), latitudeDelta);
		assertEquals(boundingBox1.getMaxLongitude(),
				boundingBox2.getMaxLongitude(), longitudeDelta);
		assertEquals(boundingBox1.getMaxLatitude(),
				boundingBox2.getMaxLatitude(), latitudeDelta);
	}

	private static List<String> randomTileTables(GeoPackage geoPackage) {
		List<String> tileTables = null;
		List<String> allTileTables = geoPackage.getTileTables();
		allTileTables.remove("webp_tiles");
		int count = allTileTables.size();
		if (count <= 2) {
			tileTables = allTileTables;
		} else {
			int index1 = (int) (Math.random() * count);
			int index2 = (int) (Math.random() * count);
			if (index1 == index2) {
				if (++index2 >= count) {
					index2 = 0;
				}
			}
			tileTables = new ArrayList<>();
			tileTables.add(allTileTables.get(index1));
			tileTables.add(allTileTables.get(index2));
		}
		return tileTables;
	}

	private static Projection alternateProjection(Projection projection) {
		Projection alternate = null;
		if (projection.equals(ProjectionConstants.AUTHORITY_EPSG,
				ProjectionConstants.EPSG_WEB_MERCATOR)) {
			alternate = ProjectionFactory.getProjection(
					ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
		} else {
			alternate = ProjectionFactory
					.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
		}
		return alternate;
	}

	private static Map<Long, Integer> zoomCounts(TileDao tileDao) {
		Map<Long, Integer> counts = new HashMap<>();
		for (long zoomLevel : tileDao.getZoomLevels()) {
			int zoomCount = tileDao.count(zoomLevel);
			counts.put(zoomLevel, zoomCount);
		}
		return counts;
	}

	private static void compareZoomCounts(int count, Map<Long, Integer> counts,
			TileDao tileDao) {
		assertEquals(count, tileDao.count());
		Map<Long, Integer> countsAfter = zoomCounts(tileDao);
		assertEquals(counts.size(), countsAfter.size());
		for (long zoomLevel : tileDao.getZoomLevels()) {
			assertEquals(counts.get(zoomLevel), countsAfter.get(zoomLevel));
		}
	}

	private static void checkZoomCounts(int count, Map<Long, Integer> counts,
			TileDao tileDao, int tiles) {
		assertEquals(count > 0, tileDao.count() > 0);
		assertEquals(tiles, tileDao.count());
		Map<Long, Integer> countsAfter = zoomCounts(tileDao);
		assertEquals(counts.size(), countsAfter.size());
		for (long zoomLevel : tileDao.getZoomLevels()) {
			assertEquals(counts.get(zoomLevel) > 0,
					countsAfter.get(zoomLevel) > 0);
		}
	}

}
