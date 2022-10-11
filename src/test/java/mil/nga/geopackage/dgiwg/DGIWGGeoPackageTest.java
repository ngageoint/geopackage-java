package mil.nga.geopackage.dgiwg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import mil.nga.crs.geo.GeoDatums;
import mil.nga.geopackage.BaseTestCase;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.TestConstants;
import mil.nga.geopackage.TestUtils;
import mil.nga.geopackage.contents.ContentsDataType;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.sf.GeometryType;

/**
 * Test DGIWG GeoPackage methods
 * 
 * @author osbornb
 */
public class DGIWGGeoPackageTest extends BaseTestCase {

	/**
	 * Test creating tiles with maximum CRS bounds
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateTiles() throws IOException {

		final String table = "dgiwg_tiles";
		final String identifier = "dgiwg identifier";
		final String description = "dgiwg description";
		final BoundingBox informativeBounds = new BoundingBox(-8922952, 4539748,
				-8453324, 4696291);

		final CoordinateReferenceSystem crs = CoordinateReferenceSystem.EPSG_3395;
		final long minZoom = 8;
		final long maxZoom = 10;
		final long matrixWidth = TileBoundingBoxUtils.tilesPerSide(minZoom);
		final long matrixHeight = matrixWidth;

		File dbFile = new File(folder.newFolder(), GeoPackageManager
				.addExtension(DGIWGGeoPackageManagerTest.FILE_NAME));
		GeoPackageFile file = DGIWGGeoPackageManager.create(dbFile,
				getMetadata());
		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager.open(file);

		TileMatrixSet tileMatrixSet = geoPackage.createTiles(table, identifier,
				description, informativeBounds, crs);

		geoPackage.createTileMatrices(tileMatrixSet, minZoom, maxZoom,
				matrixWidth, matrixHeight);

		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

		BoundingBox bounds = tileMatrixSet.getBoundingBox();
		TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(bounds,
				matrixWidth, matrixHeight, informativeBounds);
		BoundingBox tileBounds = TileBoundingBoxUtils.getBoundingBox(bounds,
				matrixWidth, matrixHeight, tileGrid);

		for (long zoom = minZoom; zoom <= maxZoom; zoom++) {

			for (long row = tileGrid.getMinY(); row <= tileGrid
					.getMaxY(); row++) {

				for (long column = tileGrid.getMinX(); column <= tileGrid
						.getMaxX(); column++) {

					TileRow tile = tileDao.newRow();

					tile.setZoomLevel(zoom);
					tile.setTileColumn(column);
					tile.setTileRow(row);
					tile.setTileData(TestUtils.getTileBytes());

					tileDao.create(tile);

				}

			}

			tileGrid = TileBoundingBoxUtils.tileGridZoomIncrease(tileGrid, 1);

		}

		for (long zoom = minZoom; zoom <= maxZoom; zoom++) {
			assertEquals(tileBounds, tileDao.getBoundingBox(zoom));
		}

		DGIWGValidationErrors errors = geoPackage.validate();
		if (errors.hasErrors()) {
			System.out.println(errors);
		}
		assertTrue(geoPackage.isValid());

		geoPackage.close();

	}

	/**
	 * Test creating tiles with a subset of CRS bounds
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateTilesSubsetBounds() throws IOException {

		final String table = "dgiwg_tiles";

		final CoordinateReferenceSystem crs = CoordinateReferenceSystem.EPSG_3395;
		final long minZoom = 8;
		final long maxZoom = 10;
		long matrixWidth = 3;
		long matrixHeight = 2;

		int tiles = TileBoundingBoxUtils.tilesPerSide(minZoom);
		TileGrid tileGrid = new TileGrid(71, 105, 73, 106);

		BoundingBox tileBounds = TileBoundingBoxUtils
				.getBoundingBox(crs.getBounds(), tiles, tiles, tileGrid);

		File dbFile = new File(folder.newFolder(), GeoPackageManager
				.addExtension(DGIWGGeoPackageManagerTest.FILE_NAME));
		GeoPackageFile file = DGIWGGeoPackageManager.create(dbFile,
				getMetadata());
		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager.open(file);

		TileMatrixSet tileMatrixSet = geoPackage.createTiles(table, crs,
				tileBounds);

		geoPackage.createTileMatrices(tileMatrixSet, minZoom, maxZoom,
				matrixWidth, matrixHeight);

		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

		for (long zoom = minZoom; zoom <= maxZoom; zoom++) {

			for (long row = 0; row < matrixHeight; row++) {

				for (long column = 0; column < matrixWidth; column++) {

					TileRow tile = tileDao.newRow();

					tile.setZoomLevel(zoom);
					tile.setTileColumn(column);
					tile.setTileRow(row);
					tile.setTileData(TestUtils.getTileBytes());

					tileDao.create(tile);

				}

			}

			matrixHeight *= 2;
			matrixWidth *= 2;

		}

		for (long zoom = minZoom; zoom <= maxZoom; zoom++) {
			assertEquals(tileBounds, tileDao.getBoundingBox(zoom));
		}

		DGIWGValidationErrors errors = geoPackage.validate();
		if (errors.hasErrors()) {
			System.out.println(errors);
		}
		assertTrue(geoPackage.isValid());

		geoPackage.close();

	}

	/**
	 * Test creating features
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateFeatures() throws IOException {

		final String table = "dgiwg_features";

		final CoordinateReferenceSystem crs = CoordinateReferenceSystem.EPSG_4326;

		File dbFile = new File(folder.newFolder(), GeoPackageManager
				.addExtension(DGIWGGeoPackageManagerTest.FILE_NAME));
		GeoPackageFile file = DGIWGGeoPackageManager.create(dbFile,
				getMetadata());
		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager.open(file);

		GeometryColumns geometryColumns = geoPackage.createFeatures(table,
				GeometryType.GEOMETRY, crs);
		long srsId = geometryColumns.getSrsId();

		FeatureDao featureDao = geoPackage.getFeatureDao(geometryColumns);

		FeatureRow featureRow = featureDao.newRow();
		featureRow.setGeometry(GeoPackageGeometryData.create(srsId,
				TestUtils.createPoint(false, false)));
		featureDao.insert(featureRow);

		featureRow = featureDao.newRow();
		featureRow.setGeometry(GeoPackageGeometryData.create(srsId,
				TestUtils.createLineString(false, false, false)));
		featureDao.insert(featureRow);

		featureRow = featureDao.newRow();
		featureRow.setGeometry(GeoPackageGeometryData.create(srsId,
				TestUtils.createPolygon(false, false)));
		featureDao.insert(featureRow);

		assertEquals(3, featureDao.count());

		DGIWGValidationErrors errors = geoPackage.validate();
		if (errors.hasErrors()) {
			System.out.println(errors);
		}
		assertTrue(geoPackage.isValid());

		geoPackage.close();

	}

	/**
	 * Test creating tiles with a Lambert Conic Conformal CRS
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateTilesLambert() throws IOException {

		final String table = "dgiwg_tiles";

		long epsg = 3978;
		String name = "NAD83 / Canada Atlas Lambert";
		GeoDatums datum = GeoDatums.NAD83;
		double standardParallel1 = 49;
		double standardParallel2 = 77;
		double latitudeOfOrigin = 49;
		double centralMeridian = -95;
		double falseEasting = 0;
		double falseNorthing = 0;

		BoundingBox boundingBox = new BoundingBox(-7786476.885838887,
				-5153821.09213678, 7148753.233541353, 7928343.534071138);

		SpatialReferenceSystem srs = CoordinateReferenceSystem
				.createLambertConicConformal2SP(epsg, name, datum,
						standardParallel1, standardParallel2, latitudeOfOrigin,
						centralMeridian, falseEasting, falseNorthing);

		final long minZoom = 2;
		final long maxZoom = 5;
		long matrixWidth = TileBoundingBoxUtils.tilesPerSide(minZoom);
		long matrixHeight = matrixWidth;

		File dbFile = new File(folder.newFolder(), GeoPackageManager
				.addExtension(DGIWGGeoPackageManagerTest.FILE_NAME));
		GeoPackageFile file = DGIWGGeoPackageManager.create(dbFile,
				getMetadata());
		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager.open(file);

		TileMatrixSet tileMatrixSet = geoPackage.createTiles(table, srs,
				boundingBox);

		geoPackage.createTileMatrices(tileMatrixSet, minZoom, maxZoom,
				matrixWidth, matrixHeight);

		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

		for (long zoom = minZoom; zoom <= maxZoom; zoom++) {

			for (long row = 0; row < matrixHeight; row++) {

				for (long column = 0; column < matrixWidth; column++) {

					TileRow tile = tileDao.newRow();

					tile.setZoomLevel(zoom);
					tile.setTileColumn(column);
					tile.setTileRow(row);
					tile.setTileData(TestUtils.getTileBytes());

					tileDao.create(tile);

				}

			}

			matrixHeight *= 2;
			matrixWidth *= 2;

		}

		for (long zoom = minZoom; zoom <= maxZoom; zoom++) {
			assertEquals(boundingBox, tileDao.getBoundingBox(zoom));
		}

		DGIWGValidationErrors errors = geoPackage.validate();
		if (errors.hasErrors()) {
			System.out.println(errors);
		}
		assertTrue(geoPackage.isValid());

		geoPackage.close();

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
	 * Test creating tiles from CRS
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateCRSTiles() throws IOException {

		File dbFile = new File(folder.newFolder(), GeoPackageManager
				.addExtension(DGIWGGeoPackageManagerTest.FILE_NAME));
		GeoPackageFile file = DGIWGGeoPackageManager.create(dbFile,
				getMetadata());
		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager.open(file);

		final long minZoom = 0;
		final long maxZoom = 1;

		for (CoordinateReferenceSystem crs : CoordinateReferenceSystem
				.getCoordinateReferenceSystems(ContentsDataType.TILES)) {

			final String table = crs.getAuthority() + "_" + crs.getCode();

			long matrixWidth = TileBoundingBoxUtils.tilesPerSide(minZoom);
			long matrixHeight = matrixWidth;

			TileMatrixSet tileMatrixSet = geoPackage.createTiles(table, crs);

			geoPackage.createTileMatrices(tileMatrixSet, minZoom, maxZoom,
					matrixWidth, matrixHeight);

			TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

			for (long zoom = minZoom; zoom <= maxZoom; zoom++) {

				for (long row = 0; row < matrixHeight; row++) {

					for (long column = 0; column < matrixWidth; column++) {

						TileRow tile = tileDao.newRow();

						tile.setZoomLevel(zoom);
						tile.setTileColumn(column);
						tile.setTileRow(row);
						tile.setTileData(TestUtils.getTileBytes());

						tileDao.create(tile);

					}

				}

				matrixHeight *= 2;
				matrixWidth *= 2;

			}

			BoundingBox bounds = crs.getBounds();

			for (long zoom = minZoom; zoom <= maxZoom; zoom++) {
				BoundingBox zoomBounds = tileDao.getBoundingBox(zoom);
				assertEquals(bounds.getMinLongitude(),
						zoomBounds.getMinLongitude(), 0.00000001);
				assertEquals(bounds.getMinLatitude(),
						zoomBounds.getMinLatitude(), 0.00000001);
				assertEquals(bounds.getMaxLongitude(),
						zoomBounds.getMaxLongitude(), 0.00000001);
				assertEquals(bounds.getMaxLatitude(),
						zoomBounds.getMaxLatitude(), 0.00000001);
			}

			DGIWGValidationErrors errors = geoPackage.validate(table);
			if (errors.hasErrors()) {
				System.out.println(errors);
			}
			assertTrue(errors.isValid());

		}

		DGIWGValidationErrors errors = geoPackage.validate();
		if (errors.hasErrors()) {
			System.out.println(errors);
		}
		assertTrue(geoPackage.isValid());

		geoPackage.close();

	}

	/**
	 * Test creating features from CRS
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testCreateCRSFeatures() throws IOException {

		File dbFile = new File(folder.newFolder(), GeoPackageManager
				.addExtension(DGIWGGeoPackageManagerTest.FILE_NAME));
		GeoPackageFile file = DGIWGGeoPackageManager.create(dbFile,
				getMetadata());
		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager.open(file);

		for (CoordinateReferenceSystem crs : CoordinateReferenceSystem
				.getCoordinateReferenceSystems(ContentsDataType.FEATURES)) {

			final String table = crs.getAuthority() + "_" + crs.getCode();

			GeometryColumns geometryColumns = geoPackage.createFeatures(table,
					GeometryType.GEOMETRY, crs);
			long srsId = geometryColumns.getSrsId();

			FeatureDao featureDao = geoPackage.getFeatureDao(geometryColumns);

			FeatureRow featureRow = featureDao.newRow();
			featureRow.setGeometry(GeoPackageGeometryData.create(srsId,
					TestUtils.createPoint(false, false)));
			featureDao.insert(featureRow);

			featureRow = featureDao.newRow();
			featureRow.setGeometry(GeoPackageGeometryData.create(srsId,
					TestUtils.createLineString(false, false, false)));
			featureDao.insert(featureRow);

			featureRow = featureDao.newRow();
			featureRow.setGeometry(GeoPackageGeometryData.create(srsId,
					TestUtils.createPolygon(false, false)));
			featureDao.insert(featureRow);

			assertEquals(3, featureDao.count());

			DGIWGValidationErrors errors = geoPackage.validate(table);
			if (errors.hasErrors()) {
				System.out.println(errors);
			}
			assertTrue(errors.isValid());

		}

		DGIWGValidationErrors errors = geoPackage.validate();
		if (errors.hasErrors()) {
			System.out.println(errors);
		}
		assertTrue(geoPackage.isValid());

		geoPackage.close();

	}

}
