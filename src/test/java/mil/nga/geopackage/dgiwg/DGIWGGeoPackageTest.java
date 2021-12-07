package mil.nga.geopackage.dgiwg;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import mil.nga.geopackage.BaseTestCase;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;

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
		final BoundingBox informativeBounds = null; // TODO
		final CoordinateReferenceSystem crs = CoordinateReferenceSystem.EPSG_3395;
		final long minZoom = 8;
		final long maxZoom = 10;
		final long matrixWidth = 3;
		final long matrixHeight = 2;

		File dbFile = new File(folder.newFolder(), GeoPackageManager
				.addExtension(DGIWGGeoPackageManagerTest.FILE_NAME));
		GeoPackageFile file = DGIWGGeoPackageManager.create(dbFile);
		DGIWGGeoPackage geoPackage = DGIWGGeoPackageManager.open(file);

		TileMatrixSet tileMatrixSet = geoPackage.createTiles(table, identifier,
				description, informativeBounds, crs);

		geoPackage.createTileMatrices(tileMatrixSet, minZoom, maxZoom,
				matrixWidth, matrixHeight);

		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);

		geoPackage.close();

	}

}
