package mil.nga.geopackage.dgiwg;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import junit.framework.TestCase;
import mil.nga.geopackage.BaseTestCase;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.TestUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileGrid;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Test DGIWG GeoPackage methods
 * 
 * @author osbornb
 */
public class DGIWGGeoPackageTest extends BaseTestCase {

	/**
	 * Test creating tiles
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
		GeoPackageFile file = DGIWGGeoPackageManager.create(dbFile);
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
			TestCase.assertEquals(tileBounds, tileDao.getBoundingBox(zoom));
		}

		geoPackage.validate();

		geoPackage.close();

		// TODO
		// GeoPackageIOUtils.copyFile(dbFile, new File("temp.gpkg"));

	}

}
