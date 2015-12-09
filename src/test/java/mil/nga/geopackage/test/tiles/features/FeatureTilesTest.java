package mil.nga.geopackage.test.tiles.features;

import java.awt.image.BufferedImage;
import java.sql.SQLException;

import junit.framework.TestCase;
import mil.nga.geopackage.extension.index.FeatureTableIndex;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.features.FeatureTiles;

import org.junit.Test;

/**
 * Test GeoPackage Feature Tiles, tiles created from features
 *
 * @author osbornb
 */
public class FeatureTilesTest extends CreateGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public FeatureTilesTest() {

	}

	/**
	 * Test feature tiles
	 *
	 * @throws java.sql.SQLException
	 */
	@Test
	public void testFeatureTiles() throws SQLException {
		testFeatureTiles(false);
	}

	/**
	 * Test feature tiles
	 *
	 * @throws java.sql.SQLException
	 */
	@Test
	public void testFeatureTilesWithIcon() throws SQLException {
		testFeatureTiles(true);
	}

	/**
	 * Test feature tiles
	 *
	 * @param useIcon
	 *
	 * @throws java.sql.SQLException
	 */
	public void testFeatureTiles(boolean useIcon) throws SQLException {

		FeatureDao featureDao = FeatureTileUtils.createFeatureDao(geoPackage);

		int num = FeatureTileUtils.insertFeatures(geoPackage, featureDao);

		FeatureTiles featureTiles = FeatureTileUtils.createFeatureTiles(
				geoPackage, featureDao, useIcon);

		FeatureTableIndex featureIndex = new FeatureTableIndex(geoPackage,
				featureDao);
		int indexed = featureIndex.index();
		TestCase.assertEquals(num, indexed);

		createTiles(featureTiles, 0, 2);

	}

	private void createTiles(FeatureTiles featureTiles, int minZoom, int maxZoom) {
		for (int i = minZoom; i <= maxZoom; i++) {
			createTiles(featureTiles, i);
		}
	}

	private void createTiles(FeatureTiles featureTiles, int zoom) {
		int tilesPerSide = TileBoundingBoxUtils.tilesPerSide(zoom);
		for (int i = 0; i < tilesPerSide; i++) {
			for (int j = 0; j < tilesPerSide; j++) {
				BufferedImage image = featureTiles.drawTile(i, j, zoom);
				TestCase.assertEquals(featureTiles.getTileWidth(),
						image.getWidth());
				TestCase.assertEquals(featureTiles.getTileHeight(),
						image.getHeight());
			}
		}
	}

}
