package mil.nga.geopackage.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import junit.framework.TestCase;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.extension.elevation.ElevationTiles;
import mil.nga.geopackage.extension.elevation.ElevationTilesCore;
import mil.nga.geopackage.extension.elevation.GriddedCoverage;
import mil.nga.geopackage.extension.elevation.GriddedCoverageDao;
import mil.nga.geopackage.extension.elevation.GriddedCoverageDataType;
import mil.nga.geopackage.extension.elevation.GriddedTile;
import mil.nga.geopackage.extension.elevation.GriddedTileDao;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

import org.junit.After;

/**
 * Abstract Test Case for Imported Elevation Tiles GeoPackages
 * 
 * @author osbornb
 */
public abstract class CreateElevationTilesGeoPackageTestCase extends
		GeoPackageTestCase {

	/**
	 * Constructor
	 */
	public CreateElevationTilesGeoPackageTestCase() {

	}

	@Override
	protected GeoPackage getGeoPackage() throws Exception {

		File testFolder = folder.newFolder();
		File dbFile = new File(testFolder,
				TestConstants.CREATE_ELEVATION_TILES_DB_FILE_NAME);

		// Create
		assertTrue("Database failed to create",
				GeoPackageManager.create(dbFile));

		// Open
		GeoPackage geoPackage = GeoPackageManager.open(dbFile);
		if (geoPackage == null) {
			throw new GeoPackageException("Failed to open database");
		}

		double minLongitude = -180.0 + (360.0 * Math.random());
		double maxLongitude = minLongitude
				+ ((180.0 - minLongitude) * Math.random());
		double minLatitude = ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE
				+ ((ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE - ProjectionConstants.WEB_MERCATOR_MIN_LAT_RANGE) * Math
						.random());
		double maxLatitude = minLatitude
				+ ((ProjectionConstants.WEB_MERCATOR_MAX_LAT_RANGE - minLatitude) * Math
						.random());

		BoundingBox bbox = new BoundingBox(minLongitude, maxLongitude,
				minLatitude, maxLatitude);

		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();
		SpatialReferenceSystem contentsSrs = srsDao
				.getOrCreateFromEpsg(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM_GEOGRAPHICAL_3D);
		SpatialReferenceSystem tileMatrixSrs = srsDao
				.getOrCreateFromEpsg(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		TileMatrixSet tileMatrixSet = ElevationTilesCore
				.createTileTableWithMetadata(geoPackage,
						TestConstants.CREATE_ELEVATION_TILES_DB_TABLE_NAME,
						bbox, contentsSrs.getId(), bbox, tileMatrixSrs.getId());
		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
		ElevationTiles elevationTiles = new ElevationTiles(geoPackage, tileDao);

		GriddedCoverageDao griddedCoverageDao = elevationTiles
				.getGriddedCoverageDao();

		GriddedCoverage griddedCoverage = new GriddedCoverage();
		griddedCoverage.setTileMatrixSet(tileMatrixSet);
		griddedCoverage.setDataType(GriddedCoverageDataType.INTEGER);
		boolean defaultScale = true;
		if (Math.random() < .5) {
			griddedCoverage.setScale(100.0 * Math.random());
			defaultScale = false;
		}
		boolean defaultOffset = true;
		if (Math.random() < .5) {
			griddedCoverage.setOffset(100.0 * Math.random());
			defaultOffset = false;
		}
		boolean defaultPrecision = true;
		if (Math.random() < .5) {
			griddedCoverage.setPrecision(10.0 * Math.random());
			defaultPrecision = false;
		}
		griddedCoverage.setDataNull(-1.0);
		griddedCoverage.setDataMissing(-2.0);
		long gcId = griddedCoverageDao.create(griddedCoverage);

		griddedCoverage = griddedCoverageDao.queryForId(gcId);
		TestCase.assertNotNull(griddedCoverage);
		if (defaultScale) {
			TestCase.assertEquals(1.0, griddedCoverage.getScale());
		} else {
			TestCase.assertTrue(griddedCoverage.getScale() >= 0.0
					&& griddedCoverage.getScale() <= 100.0);
		}
		if (defaultOffset) {
			TestCase.assertEquals(0.0, griddedCoverage.getOffset());
		} else {
			TestCase.assertTrue(griddedCoverage.getOffset() >= 0.0
					&& griddedCoverage.getOffset() <= 100.0);
		}
		if (defaultPrecision) {
			TestCase.assertEquals(1.0, griddedCoverage.getPrecision());
		} else {
			TestCase.assertTrue(griddedCoverage.getPrecision() >= 0.0
					&& griddedCoverage.getPrecision() <= 10.0);
		}

		GriddedTileDao griddedTileDao = elevationTiles.getGriddedTileDao();

		int width = 1 + (int) Math.floor((Math.random() * 10.0));
		int height = 1 + (int) Math.floor((Math.random() * 10.0));
		int tileWidth = 1 + (int) Math.floor((Math.random() * 512.0));
		int tileHeight = 1 + (int) Math.floor((Math.random() * 512.0));
		int minZoomLevel = (int) Math.floor(Math.random() * 22.0);
		int maxZoomLevel = minZoomLevel + (int) Math.floor(Math.random() * 5.0);

		TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();

		for (int zoomLevel = minZoomLevel; zoomLevel <= maxZoomLevel; zoomLevel++) {

			TileMatrix tileMatrix = new TileMatrix();
			tileMatrix.setContents(tileMatrixSet.getContents());
			tileMatrix.setMatrixHeight(height);
			tileMatrix.setMatrixWidth(width);
			tileMatrix.setTileHeight(tileHeight);
			tileMatrix.setTileWidth(tileWidth);
			tileMatrix.setPixelXSize((bbox.getMaxLongitude() - bbox
					.getMinLongitude()) / width / tileWidth);
			tileMatrix.setPixelYSize((bbox.getMaxLatitude() - bbox
					.getMinLatitude()) / height / tileHeight);
			tileMatrix.setZoomLevel(zoomLevel);
			TestCase.assertTrue(tileMatrixDao.create(tileMatrix) >= 0);

			for (int row = 0; row < height; row++) {
				for (int column = 0; column < width; column++) {

					TileRow tileRow = tileDao.newRow();
					tileRow.setTileColumn(column);
					tileRow.setTileRow(row);
					tileRow.setZoomLevel(zoomLevel);
					tileRow.setTileData(new byte[1]); // TODO elevation tile

					long tileId = tileDao.create(tileRow);
					TestCase.assertTrue(tileId >= 0);

					GriddedTile griddedTile = new GriddedTile();
					griddedTile.setContents(tileMatrixSet.getContents());
					griddedTile.setTableId(tileId);
					// TODO set more gridded tile values
					TestCase.assertTrue(griddedTileDao.create(griddedTile) >= 0);
				}
			}
			height *= 2;
			width *= 2;
		}

		// TODO

		return geoPackage;
	}

	@After
	public void tearDown() throws Exception {

		// Close
		if (geoPackage != null) {
			geoPackage.close();
		}

	}

}
