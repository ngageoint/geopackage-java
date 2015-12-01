package mil.nga.geopackage.test.tiles;

import java.io.IOException;
import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import org.junit.Test;

/**
 * Test URL Tile Generator from an imported database
 * 
 * @author osbornb
 */
public class UrlTileGeneratorImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public UrlTileGeneratorImportTest() {

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
