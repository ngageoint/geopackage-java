package mil.nga.geopackage.test.tiles;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import org.junit.Test;

import junit.framework.TestCase;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.test.LoadGeoPackageTestCase;
import mil.nga.geopackage.test.TestConstants;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.tiles.GeoPackageTile;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.TileCreator;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;
import mil.nga.sf.proj.ProjectionTransform;

/**
 * Test Tile Creator image accuracy from a GeoPackage with tiles
 *
 * @author osbornb
 */
public class TileCreatorImageTest extends LoadGeoPackageTestCase {

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
		TestCase.assertEquals(tileDao.getProjection().getAuthority(),
				ProjectionConstants.AUTHORITY_EPSG);
		TestCase.assertEquals(Long.parseLong(tileDao.getProjection().getCode()),
				ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		Projection webMercator = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
		Projection wgs84 = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		int width = 256;
		int height = 256;
		TileCreator webMercatorTileCreator = new TileCreator(tileDao, width,
				height, webMercator, "png");
		TileCreator wgs84TileCreator = new TileCreator(tileDao, width, height,
				wgs84, "png");

		BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(0, 4, 4);
		BoundingBox wgs84BoundingBox = webMercatorBoundingBox
				.transform(webMercator.getTransformation(wgs84));

		TestCase.assertTrue(
				webMercatorTileCreator.hasTile(webMercatorBoundingBox));
		TestCase.assertTrue(wgs84TileCreator.hasTile(wgs84BoundingBox));

		GeoPackageTile webMercatorTile = webMercatorTileCreator
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
				TestCase.assertTrue("Web Mercator Red pixel " + webMercatorRed
						+ " is not within the " + COLOR_TOLERANCE
						+ " range of test red pixel " + webMercatorTestRed,
						Math.abs(webMercatorRed
								- webMercatorTestRed) <= COLOR_TOLERANCE);
				TestCase.assertTrue(
						"Web Mercator Green pixel " + webMercatorGreen
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test green pixel "
								+ webMercatorTestGreen,
						Math.abs(webMercatorGreen
								- webMercatorTestGreen) <= COLOR_TOLERANCE);
				TestCase.assertTrue("Web Mercator Blue pixel " + webMercatorBlue
						+ " is not within the " + COLOR_TOLERANCE
						+ " range of test blue pixel " + webMercatorTestBlue,
						Math.abs(webMercatorBlue
								- webMercatorTestBlue) <= COLOR_TOLERANCE);
				TestCase.assertTrue(
						"Web Mercator Alpha pixel " + webMercatorAlpha
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test alpha pixel "
								+ webMercatorTestAlpha,
						Math.abs(webMercatorAlpha
								- webMercatorTestAlpha) <= COLOR_TOLERANCE);

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
				TestCase.assertTrue(
						"Web Mercator Red pixel " + wgs84Red
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test red pixel " + wgs84TestRed,
						Math.abs(wgs84Red - wgs84TestRed) <= COLOR_TOLERANCE);
				TestCase.assertTrue("Web Mercator Green pixel " + wgs84Green
						+ " is not within the " + COLOR_TOLERANCE
						+ " range of test green pixel " + wgs84TestGreen,
						Math.abs(wgs84Green
								- wgs84TestGreen) <= COLOR_TOLERANCE);
				TestCase.assertTrue(
						"Web Mercator Blue pixel " + wgs84Blue
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test blue pixel " + wgs84TestBlue,
						Math.abs(wgs84Blue - wgs84TestBlue) <= COLOR_TOLERANCE);
				TestCase.assertTrue("Web Mercator Alpha pixel " + wgs84Alpha
						+ " is not within the " + COLOR_TOLERANCE
						+ " range of test alpha pixel " + wgs84TestAlpha,
						Math.abs(wgs84Alpha
								- wgs84TestAlpha) <= COLOR_TOLERANCE);

				redDiff = Math.max(redDiff,
						Math.abs(webMercatorRed - wgs84Red));
				greenDiff = Math.max(greenDiff,
						Math.abs(webMercatorGreen - wgs84Green));
				blueDiff = Math.max(blueDiff,
						Math.abs(webMercatorBlue - wgs84Blue));
			}

		}

		// To write the images if the test images needs to change
		/*
		try {
			File webMercatorWriteFile = new File(".",
					TestConstants.TILES2_WEB_MERCATOR_TEST_IMAGE);
			ImageIO.write(webMercatorImage, "png", webMercatorWriteFile);
			System.out.println("Wrote test image: "
					+ webMercatorWriteFile.getAbsolutePath());
			File wgs84WriteFile = new File(".",
					TestConstants.TILES2_WGS84_TEST_IMAGE);
			ImageIO.write(wgs84Image, "png", wgs84WriteFile);
			System.out.println("Wrote test image: "
					+ wgs84WriteFile.getAbsolutePath());
		} catch (Exception e) {
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
		TestCase.assertEquals(tileDao.getProjection().getAuthority(),
				ProjectionConstants.AUTHORITY_EPSG);
		TestCase.assertEquals(Long.parseLong(tileDao.getProjection().getCode()),
				ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		int width = 450;
		int height = 450;
		TileCreator wgs84TileCreator = new TileCreator(tileDao);

		Projection webMercator = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
		try {
			new TileCreator(tileDao, webMercator, null);
			TestCase.fail(
					"Tile Creator was created for raw images in a different projection");
		} catch (Exception e) {
			// expected
		}

		BoundingBox wgs84BoundingBox = new BoundingBox(-180, 45, -157.5, 67.5);

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
				TestCase.assertTrue(
						"Web Mercator Red pixel " + wgs84Red
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test red pixel " + wgs84TestRed,
						Math.abs(wgs84Red - wgs84TestRed) <= COLOR_TOLERANCE);
				TestCase.assertTrue("Web Mercator Green pixel " + wgs84Green
						+ " is not within the " + COLOR_TOLERANCE
						+ " range of test green pixel " + wgs84TestGreen,
						Math.abs(wgs84Green
								- wgs84TestGreen) <= COLOR_TOLERANCE);
				TestCase.assertTrue(
						"Web Mercator Blue pixel " + wgs84Blue
								+ " is not within the " + COLOR_TOLERANCE
								+ " range of test blue pixel " + wgs84TestBlue,
						Math.abs(wgs84Blue - wgs84TestBlue) <= COLOR_TOLERANCE);
				TestCase.assertTrue("Web Mercator Alpha pixel " + wgs84Alpha
						+ " is not within the " + COLOR_TOLERANCE
						+ " range of test alpha pixel " + wgs84TestAlpha,
						Math.abs(wgs84Alpha
								- wgs84TestAlpha) <= COLOR_TOLERANCE);
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

	/**
	 * Test tile image projections
	 * 
	 * @throws IOException
	 *             upon error
	 */
	@Test
	public void testTileImageProjections() throws IOException {

		TileDao tileDao = geoPackage
				.getTileDao(TestConstants.TILES2_DB_TABLE_NAME);
		BoundingBox boundingBox = geoPackage
				.getBoundingBox(TestConstants.TILES2_DB_TABLE_NAME);

		Projection wgs84 = geoPackage
				.getProjection(TestConstants.TILES2_DB_TABLE_NAME);
		Projection webMercator = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
		ProjectionTransform toWebMercator = wgs84
				.getTransformation(webMercator);
		ProjectionTransform toWGS84 = toWebMercator.getInverseTransformation();

		BoundingBox webMercatorBoundingBox = boundingBox
				.transform(toWebMercator);

		int width = 256;
		int height = 256;
		TileCreator webMercatorTileCreator = new TileCreator(tileDao, width,
				height, webMercator, ImageUtils.IMAGE_FORMAT_PNG);
		TileCreator wgs84TileCreator = new TileCreator(tileDao, width, height,
				ImageUtils.IMAGE_FORMAT_PNG);

		double minLongitude = webMercatorBoundingBox.getMinLongitude();
		double maxLongitude = webMercatorBoundingBox.getMaxLongitude();
		double midLongitude = minLongitude
				+ ((maxLongitude - minLongitude) / 2);

		double minLatitude = webMercatorBoundingBox.getMinLatitude();
		double maxLatitude = webMercatorBoundingBox.getMaxLatitude();
		double midLatitude = minLatitude + ((maxLatitude - minLatitude) / 2);

		double minWGS84Longitude = boundingBox.getMinLongitude();
		double maxWGS84Longitude = boundingBox.getMaxLongitude();
		double midWGS84Longitude = minWGS84Longitude
				+ ((maxWGS84Longitude - minWGS84Longitude) / 2);

		double minWGS84Latitude = boundingBox.getMinLatitude();
		double maxWGS84Latitude = boundingBox.getMaxLatitude();
		double midWGS84Latitude = minWGS84Latitude
				+ ((maxWGS84Latitude - minWGS84Latitude) / 2);

		BoundingBox topLeft = new BoundingBox(minLongitude, midLatitude,
				midLongitude, maxLatitude);
		BoundingBox topLeftWGS84 = new BoundingBox(minWGS84Longitude,
				midWGS84Latitude, midWGS84Longitude, maxWGS84Latitude);
		createTiles(webMercatorTileCreator, topLeft, wgs84TileCreator,
				topLeftWGS84);

		BoundingBox topRight = new BoundingBox(midLongitude, midLatitude,
				maxLongitude, maxLatitude);
		BoundingBox topRightWGS84 = new BoundingBox(midWGS84Longitude,
				midWGS84Latitude, maxWGS84Longitude, maxWGS84Latitude);
		createTiles(webMercatorTileCreator, topRight, wgs84TileCreator,
				topRightWGS84);

		BoundingBox bottomLeft = new BoundingBox(minLongitude, minLatitude,
				midLongitude, midLatitude);
		BoundingBox bottomLeftWGS84 = new BoundingBox(minWGS84Longitude,
				minWGS84Latitude, midWGS84Longitude, midWGS84Latitude);
		createTiles(webMercatorTileCreator, bottomLeft, wgs84TileCreator,
				bottomLeftWGS84);

		BoundingBox bottomRight = new BoundingBox(midLongitude, minLatitude,
				maxLongitude, midLatitude);
		BoundingBox bottomRightWGS84 = new BoundingBox(midWGS84Longitude,
				minWGS84Latitude, maxWGS84Longitude, midWGS84Latitude);
		createTiles(webMercatorTileCreator, bottomRight, wgs84TileCreator,
				bottomRightWGS84);

		double pixelXSize = (maxLongitude - minLongitude) / (2.0 * width);
		double pixelYSize = (maxLatitude - minLatitude) / (2.0 * height);

		double pixelXSizeWGS84 = (maxWGS84Longitude - minWGS84Longitude)
				/ (2.0 * width);
		double pixelYSizeWGS84 = (maxWGS84Latitude - minWGS84Latitude)
				/ (2.0 * height);

	}

	/**
	 * Create tiles
	 * 
	 * @param webMercatorCreator
	 *            web mercator tile creator
	 * @param webMercator
	 *            web mercator bounding box
	 * @param wgs84Creator
	 *            wgs84 tile creator
	 * @param wgs84
	 *            wgs84 bounding box
	 * @throws IOException
	 *             upon error
	 */
	private void createTiles(TileCreator webMercatorCreator,
			BoundingBox webMercator, TileCreator wgs84Creator,
			BoundingBox wgs84) throws IOException {

		Projection wgs84Projection = geoPackage
				.getProjection(TestConstants.TILES2_DB_TABLE_NAME);
		Projection webMercatorProjection = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
		ProjectionTransform toWebMercator = wgs84Projection
				.getTransformation(webMercatorProjection);
		ProjectionTransform toWGS84 = toWebMercator.getInverseTransformation();

		BoundingBox wgs84WebMercator = webMercator.transform(toWGS84);
		double pixelXSize = (wgs84WebMercator.getMaxLongitude()
				- wgs84WebMercator.getMinLongitude())
				/ (1.0 * wgs84Creator.getWidth().doubleValue());
		double pixelYSize = (wgs84WebMercator.getMaxLatitude()
				- wgs84WebMercator.getMinLatitude())
				/ (1.0 * wgs84Creator.getHeight().doubleValue());

		GeoPackageTile tile = webMercatorCreator.getTile(webMercator);
		GeoPackageTile wgs84WebMercatorTile = wgs84Creator
				.getTile(wgs84WebMercator);
		GeoPackageTile wgs84Tile = wgs84Creator.getTile(wgs84);

		BufferedImage image = tile.getImage();
		BufferedImage wgs84WebMercatorImage = wgs84WebMercatorTile.getImage();
		BufferedImage wgs84Image = wgs84Tile.getImage();

		TestCase.assertEquals(image.getWidth(),
				wgs84WebMercatorImage.getWidth());
		TestCase.assertEquals(image.getHeight(),
				wgs84WebMercatorImage.getHeight());
		TestCase.assertEquals(image.getWidth(), wgs84Image.getWidth());
		TestCase.assertEquals(image.getHeight(), wgs84Image.getHeight());

	}

}
