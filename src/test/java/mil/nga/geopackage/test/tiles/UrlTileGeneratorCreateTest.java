package mil.nga.geopackage.test.tiles;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.test.CreateGeoPackageTestCase;

import org.junit.Test;

/**
 * Test URL Tile Generator from a created database
 * 
 * @author osbornb
 */
public class UrlTileGeneratorCreateTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public UrlTileGeneratorCreateTest() {

	}

	@Test
	public void testGenerateTiles() throws SQLException, IOException {

		UrlTileGeneratorUtils.testGenerateTiles(geoPackage);

	}

	@Test
	public void testGenerateTilesCompress() throws SQLException, IOException {

		UrlTileGeneratorUtils.testGenerateTilesCompress(geoPackage);

	}

	@Test
	public void testGenerateTilesCompressQuality() throws SQLException,
			IOException {

		UrlTileGeneratorUtils.testGenerateTilesCompressQuality(geoPackage);

	}

	@Test
	public void testGenerateTilesGoogle() throws SQLException, IOException {

		UrlTileGeneratorUtils.testGenerateTilesGoogle(geoPackage);

	}

	@Test
	public void testGenerateTilesBounded() throws SQLException, IOException {

		UrlTileGeneratorUtils.testGenerateTilesBounded(geoPackage);

	}

	@Test
	public void testGenerateTilesGoogleBounded() throws SQLException,
			IOException {

		UrlTileGeneratorUtils.testGenerateTilesGoogleBounded(geoPackage);

	}

	@Test
	public void testGenerateTilesRandom() throws SQLException, IOException {

		UrlTileGeneratorUtils.testGenerateTilesRandom(geoPackage);

	}

	@Test
	public void testGenerateTilesUnsupportedCompressQuality()
			throws SQLException, IOException {

		UrlTileGeneratorUtils
				.testGenerateTilesUnsupportedCompressQuality(geoPackage);

	}

}
