package mil.nga.geopackage.test.tiles;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import junit.framework.TestCase;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.test.TestConstants;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.test.TilesGeoPackageTestCase;
import mil.nga.geopackage.tiles.GeoPackageTile;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileCreator;
import mil.nga.geopackage.tiles.user.TileDao;

import org.junit.Test;

/**
 * Test Tile Creator image accuracy from a GeoPackage with tiles
 *
 * @author osbornb
 */
public class TileCreatorImageTest extends TilesGeoPackageTestCase {

	private final int COLOR_TOLERANCE = 0;

	/**
	 * Constructor
	 */
	public TileCreatorImageTest() {
		super(TestConstants.TILES2_DB_FILE_NAME);
	}

	/**
	 * Test get tile image
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void testTileImage() throws SQLException, IOException {

		TileDao tileDao = geoPackage
				.getTileDao(TestConstants.TILES2_DB_TABLE_NAME);
		TestCase.assertEquals(tileDao.getProjection().getEpsg(),
				ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		Projection webMercator = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
		Projection wgs84 = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		int width = 256;
		int height = 256;
		TileCreator webMeractorTileCreator = new TileCreator(tileDao, width,
				height, webMercator, "png");
		TileCreator wgs84TileCreator = new TileCreator(tileDao, width, height,
				wgs84, "png");

		BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(0, 4, 4);
		BoundingBox wgs84BoundingBox = webMercator.getTransformation(wgs84)
				.transform(webMercatorBoundingBox);

		TestCase.assertTrue(webMeractorTileCreator
				.hasTile(webMercatorBoundingBox));
		TestCase.assertTrue(wgs84TileCreator.hasTile(wgs84BoundingBox));

		GeoPackageTile webMercatorTile = webMeractorTileCreator
				.getTile(webMercatorBoundingBox);
		GeoPackageTile wgs84Tile = wgs84TileCreator.getTile(wgs84BoundingBox);

		TestCase.assertNotNull(webMercatorTile);
		TestCase.assertEquals(width, webMercatorTile.getWidth());
		TestCase.assertEquals(height, webMercatorTile.getHeight());

		TestCase.assertNotNull(wgs84Tile);
		TestCase.assertEquals(width, wgs84Tile.getWidth());
		TestCase.assertEquals(height, wgs84Tile.getHeight());

		BufferedImage webMercatorImage = webMercatorTile.getImage();
		TestCase.assertNotNull(webMercatorImage);

		BufferedImage wgs84Image = wgs84Tile.getImage();
		TestCase.assertNotNull(wgs84Image);

		TestCase.assertEquals(width, webMercatorImage.getWidth());
		TestCase.assertEquals(height, webMercatorImage.getHeight());
		validateNoTransparency(webMercatorImage);

		TestCase.assertEquals(width, wgs84Image.getWidth());
		TestCase.assertEquals(height, wgs84Image.getHeight());
		validateNoTransparency(wgs84Image);

		File webMercatorTestFile = TestUtils
				.getTestFile(TestConstants.TILES2_WEB_MERCATOR_TEST_IMAGE);
		BufferedImage webMercatorTestImage = ImageIO.read(webMercatorTestFile);

		File wgs84TestFile = TestUtils
				.getTestFile(TestConstants.TILES2_WGS84_TEST_IMAGE);
		BufferedImage wgs84TestImage = ImageIO.read(wgs84TestFile);

		int redDiff = 0;
		int greenDiff = 0;
		int blueDiff = 0;

		// Compare the image pixels with the expected test image pixels
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				int webMercatorPixel = webMercatorImage.getRGB(x, y);
				int webMercatorTestPixel = webMercatorTestImage.getRGB(x, y);

				Color webMercatorPixelColor = new Color(webMercatorPixel);
				Color webMercatorTestPixelColor = new Color(
						webMercatorTestPixel);

				int webMercatorRed = webMercatorPixelColor.getRed();
				int webMercatorGreen = webMercatorPixelColor.getGreen();
				int webMercatorBlue = webMercatorPixelColor.getBlue();
				int webMercatorAlpha = webMercatorPixelColor.getAlpha();

				int webMercatorTestRed = webMercatorTestPixelColor.getRed();
				int webMercatorTestGreen = webMercatorTestPixelColor.getGreen();
				int webMercatorTestBlue = webMercatorTestPixelColor.getBlue();
				int webMercatorTestAlpha = webMercatorTestPixelColor.getAlpha();

				// Colors differ between phones and emulators, try to validate
				// within a tolerance range the colors are as expected
				TestCase.assertTrue(
						"Web Meractor Red pixel " + webMercatorRed
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test red pixel "
								+ webMercatorTestRed,
						Math.abs(webMercatorRed - webMercatorTestRed) <= COLOR_TOLERANCE);
				TestCase.assertTrue(
						"Web Meractor Green pixel " + webMercatorGreen
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test green pixel "
								+ webMercatorTestGreen,
						Math.abs(webMercatorGreen - webMercatorTestGreen) <= COLOR_TOLERANCE);
				TestCase.assertTrue(
						"Web Meractor Blue pixel " + webMercatorBlue
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test blue pixel "
								+ webMercatorTestBlue,
						Math.abs(webMercatorBlue - webMercatorTestBlue) <= COLOR_TOLERANCE);
				TestCase.assertTrue(
						"Web Meractor Alpha pixel " + webMercatorAlpha
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test alpha pixel "
								+ webMercatorTestAlpha,
						Math.abs(webMercatorAlpha - webMercatorTestAlpha) <= COLOR_TOLERANCE);

				int wgs84Pixel = wgs84Image.getRGB(x, y);
				int wgs84TestPixel = wgs84TestImage.getRGB(x, y);

				Color wgs84PixelColor = new Color(wgs84Pixel);
				Color wgs84TestPixelColor = new Color(wgs84TestPixel);

				int wgs84Red = wgs84PixelColor.getRed();
				int wgs84Green = wgs84PixelColor.getGreen();
				int wgs84Blue = wgs84PixelColor.getBlue();
				int wgs84Alpha = wgs84PixelColor.getAlpha();

				int wgs84TestRed = wgs84TestPixelColor.getRed();
				int wgs84TestGreen = wgs84TestPixelColor.getGreen();
				int wgs84TestBlue = wgs84TestPixelColor.getBlue();
				int wgs84TestAlpha = wgs84TestPixelColor.getAlpha();

				// Colors differ between phones and emulators, try to validate
				// within a tolerance range the colors are as expected
				TestCase.assertTrue("Web Meractor Red pixel " + wgs84Red
						+ " is not within the " + COLOR_TOLERANCE
						+ " range of test red pixel " + wgs84TestRed,
						Math.abs(wgs84Red - wgs84TestRed) <= COLOR_TOLERANCE);
				TestCase.assertTrue(
						"Web Meractor Green pixel " + wgs84Green
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test green pixel "
								+ wgs84TestGreen,
						Math.abs(wgs84Green - wgs84TestGreen) <= COLOR_TOLERANCE);
				TestCase.assertTrue("Web Meractor Blue pixel " + wgs84Blue
						+ " is not within the " + COLOR_TOLERANCE
						+ " range of test blue pixel " + wgs84TestBlue,
						Math.abs(wgs84Blue - wgs84TestBlue) <= COLOR_TOLERANCE);
				TestCase.assertTrue(
						"Web Meractor Alpha pixel " + wgs84Alpha
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test alpha pixel "
								+ wgs84TestAlpha,
						Math.abs(wgs84Alpha - wgs84TestAlpha) <= COLOR_TOLERANCE);

				redDiff = Math
						.max(redDiff, Math.abs(webMercatorRed - wgs84Red));
				greenDiff = Math.max(greenDiff,
						Math.abs(webMercatorGreen - wgs84Green));
				blueDiff = Math.max(blueDiff,
						Math.abs(webMercatorBlue - wgs84Blue));
			}

		}

        //  To write the images if the test images needs to change
		/*
        try {
        	File webMercatorWriteFile = new File(".", TestConstants.TILES2_WEB_MERCATOR_TEST_IMAGE);
        	ImageIO.write(webMercatorImage, "png", webMercatorWriteFile);
        	System.out.println("Wrote test image: " + webMercatorWriteFile.getAbsolutePath());
        	File wgs84WriteFile = new File(".", TestConstants.TILES2_WGS84_TEST_IMAGE);
        	ImageIO.write(wgs84Image, "png", wgs84WriteFile);
        	System.out.println("Wrote test image: " + wgs84WriteFile.getAbsolutePath());
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        */

	}

	/**
	 * Test raw tile image
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void testRawTileImage() throws SQLException, IOException {

		TileDao tileDao = geoPackage
				.getTileDao(TestConstants.TILES2_DB_TABLE_NAME);
		TestCase.assertEquals(tileDao.getProjection().getEpsg(),
				ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		int width = 450;
		int height = 450;
		TileCreator wgs84TileCreator = new TileCreator(tileDao);

		Projection webMercator = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
		try {
			new TileCreator(tileDao,
					webMercator, null);
			TestCase.fail("Tile Creator was created for raw images in a different projection");
		} catch (Exception e) {
			// expected
		}

		BoundingBox wgs84BoundingBox = new BoundingBox(-180, -157.5, 45, 67.5);

		TestCase.assertTrue(wgs84TileCreator.hasTile(wgs84BoundingBox));

		GeoPackageTile wgs84Tile = wgs84TileCreator.getTile(wgs84BoundingBox);

		TestCase.assertNotNull(wgs84Tile);
		TestCase.assertEquals(width, wgs84Tile.getWidth());
		TestCase.assertEquals(height, wgs84Tile.getHeight());

		byte[] wgs84TileBytes = wgs84Tile.getData();
		TestCase.assertNotNull(wgs84TileBytes);
		BufferedImage wgs84Image = ImageUtils.getImage(wgs84TileBytes);

		TestCase.assertEquals(width, wgs84Image.getWidth());
		TestCase.assertEquals(height, wgs84Image.getHeight());
		validateNoTransparency(wgs84Image);

		File wgs84TestFile = TestUtils
				.getTestFile(TestConstants.TILES2_WGS84_TEST_RAW_IMAGE);
		BufferedImage wgs84TestImage = ImageIO.read(wgs84TestFile);

		// Compare the image pixels with the expected test image pixels
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				int wgs84Pixel = wgs84Image.getRGB(x, y);
				int wgs84TestPixel = wgs84TestImage.getRGB(x, y);

				Color wgs84PixelColor = new Color(wgs84Pixel);
				Color wgs84TestPixelColor = new Color(wgs84TestPixel);

				int wgs84Red = wgs84PixelColor.getRed();
				int wgs84Green = wgs84PixelColor.getGreen();
				int wgs84Blue = wgs84PixelColor.getBlue();
				int wgs84Alpha = wgs84PixelColor.getAlpha();

				int wgs84TestRed = wgs84TestPixelColor.getRed();
				int wgs84TestGreen = wgs84TestPixelColor.getGreen();
				int wgs84TestBlue = wgs84TestPixelColor.getBlue();
				int wgs84TestAlpha = wgs84TestPixelColor.getAlpha();

				// Colors differ between phones and emulators, try to validate
				// within a tolerance range the colors are as expected
				TestCase.assertTrue("Web Meractor Red pixel " + wgs84Red
						+ " is not within the " + COLOR_TOLERANCE
						+ " range of test red pixel " + wgs84TestRed,
						Math.abs(wgs84Red - wgs84TestRed) <= COLOR_TOLERANCE);
				TestCase.assertTrue(
						"Web Meractor Green pixel " + wgs84Green
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test green pixel "
								+ wgs84TestGreen,
						Math.abs(wgs84Green - wgs84TestGreen) <= COLOR_TOLERANCE);
				TestCase.assertTrue("Web Meractor Blue pixel " + wgs84Blue
						+ " is not within the " + COLOR_TOLERANCE
						+ " range of test blue pixel " + wgs84TestBlue,
						Math.abs(wgs84Blue - wgs84TestBlue) <= COLOR_TOLERANCE);
				TestCase.assertTrue(
						"Web Meractor Alpha pixel " + wgs84Alpha
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test alpha pixel "
								+ wgs84TestAlpha,
						Math.abs(wgs84Alpha - wgs84TestAlpha) <= COLOR_TOLERANCE);
			}

		}

		// To write the image if the test image needs to change
		/*
		try {
			File wgs84WriteFile = new File(".",
					TestConstants.TILES2_WGS84_TEST_RAW_IMAGE);
			ImageIO.write(wgs84Image, "png", wgs84WriteFile);
			System.out.println("Wrote test image: "
					+ wgs84WriteFile.getAbsolutePath());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		*/

	}

	/**
	 * Validate that the image has no transparency
	 *
	 * @param image
	 */
	private void validateNoTransparency(BufferedImage image) {

		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				TestCase.assertTrue(image.getRGB(x, y) != 0);
			}
		}

	}

}
