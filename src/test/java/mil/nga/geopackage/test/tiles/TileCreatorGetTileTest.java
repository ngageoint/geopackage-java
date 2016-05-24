package mil.nga.geopackage.test.tiles;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;

import junit.framework.TestCase;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.test.TestConstants;
import mil.nga.geopackage.test.TilesGeoPackageTestCase;
import mil.nga.geopackage.tiles.GeoPackageTile;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileCreator;
import mil.nga.geopackage.tiles.user.TileDao;

import org.junit.Test;

/**
 * Test Tile Creator from a GeoPackage with tiles
 * 
 * @author osbornb
 */
public class TileCreatorGetTileTest extends TilesGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public TileCreatorGetTileTest() {
		super(TestConstants.TILES_DB_FILE_NAME);
	}

	/**
	 * Test get tile
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void testGetTile() throws SQLException, IOException {

		TileDao tileDao = geoPackage
				.getTileDao(TestConstants.TILES_DB_TABLE_NAME);
		TestCase.assertEquals(tileDao.getProjection().getEpsg(),
				ProjectionConstants.EPSG_WEB_MERCATOR);

		tileDao.adjustTileMatrixLengths();

		Projection wgs84 = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		int width = 256;
		int height = 140;
		TileCreator tileCreator = new TileCreator(tileDao, width, height,
				wgs84, "png");

		BoundingBox boundingBox = new BoundingBox();
		boundingBox = TileBoundingBoxUtils
				.boundWgs84BoundingBoxWithWebMercatorLimits(boundingBox);
		TestCase.assertFalse(tileCreator.hasTile(boundingBox));

		boundingBox = new BoundingBox(-180.0, 0.0, 0.0, 90.0);
		boundingBox = TileBoundingBoxUtils
				.boundWgs84BoundingBoxWithWebMercatorLimits(boundingBox);
		TestCase.assertTrue(tileCreator.hasTile(boundingBox));

		GeoPackageTile tile = tileCreator.getTile(boundingBox);

		TestCase.assertNotNull(tile);
		TestCase.assertEquals(width, tile.getWidth());
		TestCase.assertEquals(height, tile.getHeight());

		BufferedImage image = tile.getImage();
		TestCase.assertNotNull(image);

		TestCase.assertEquals(width, image.getWidth());
		TestCase.assertEquals(height, image.getHeight());
		validateImage(image);

		boundingBox = new BoundingBox(-90.0, 0.0, 0.0, 45.0);
		TestCase.assertTrue(tileCreator.hasTile(boundingBox));

		tile = tileCreator.getTile(boundingBox);

		TestCase.assertNotNull(tile);
		TestCase.assertEquals(width, tile.getWidth());
		TestCase.assertEquals(height, tile.getHeight());

		image = tile.getImage();
		TestCase.assertNotNull(image);

		TestCase.assertEquals(width, image.getWidth());
		TestCase.assertEquals(height, image.getHeight());
		validateImage(image);
	}

	/**
	 * Test get raw tile
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void testGetRawTile() throws SQLException, IOException {

		TileDao tileDao = geoPackage
				.getTileDao(TestConstants.TILES_DB_TABLE_NAME);
		TestCase.assertEquals(tileDao.getProjection().getEpsg(),
				ProjectionConstants.EPSG_WEB_MERCATOR);

		tileDao.adjustTileMatrixLengths();

		int width = 256;
		int height = 256;
		TileCreator tileCreator = new TileCreator(tileDao);

		BoundingBox boundingBox = new BoundingBox();
		boundingBox = TileBoundingBoxUtils
				.boundWgs84BoundingBoxWithWebMercatorLimits(boundingBox);
		TestCase.assertFalse(tileCreator.hasTile(boundingBox));

		boundingBox = new BoundingBox(-180.0, 0.0, 0.0, 90.0);
		boundingBox = TileBoundingBoxUtils.toWebMercator(boundingBox);
		TestCase.assertTrue(tileCreator.hasTile(boundingBox));

		GeoPackageTile tile = tileCreator.getTile(boundingBox);

		TestCase.assertNotNull(tile);
		TestCase.assertEquals(width, tile.getWidth());
		TestCase.assertEquals(height, tile.getHeight());

		byte[] tileBytes = tile.getData();
		TestCase.assertNotNull(tileBytes);
		BufferedImage image = ImageUtils.getImage(tileBytes);

		TestCase.assertEquals(width, image.getWidth());
		TestCase.assertEquals(height, image.getHeight());
		validateImage(image);

		boundingBox = new BoundingBox(-10018754.171394622, 0.0, 0.0, 10018754.17139462);
		TestCase.assertTrue(tileCreator.hasTile(boundingBox));

		tile = tileCreator.getTile(boundingBox);

		TestCase.assertNotNull(tile);
		TestCase.assertEquals(width, tile.getWidth());
		TestCase.assertEquals(height, tile.getHeight());

		tileBytes = tile.getData();
		TestCase.assertNotNull(tileBytes);
		image = ImageUtils.getImage(tileBytes);

		TestCase.assertEquals(width, image.getWidth());
		TestCase.assertEquals(height, image.getHeight());
		validateImage(image);

		// Test a raw image request when the bounds do not line up
		boundingBox = new BoundingBox(-10018754.171394622, 0.0, 0.0, 5009377.085697312);
		TestCase.assertTrue(tileCreator.hasTile(boundingBox));

		try {
			tileCreator.getTile(boundingBox);
			TestCase.fail("A raw image request without exact image bounds passed");
		} catch (Exception e) {
			// expected
		}
	}

	/**
	 * Validate that the image has no transparency
	 *
	 * @param image
	 */
	private void validateImage(BufferedImage image) {

		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				TestCase.assertTrue(image.getRGB(x, y) != 0);
			}
		}

	}

}
