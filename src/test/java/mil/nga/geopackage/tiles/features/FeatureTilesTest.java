package mil.nga.geopackage.tiles.features;

import java.awt.image.BufferedImage;
import java.sql.SQLException;

import org.junit.Test;

import junit.framework.TestCase;
import mil.nga.geopackage.CreateGeoPackageTestCase;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;

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

		FeatureTiles featureTiles = FeatureTileUtils
				.createFeatureTiles(geoPackage, featureDao, useIcon);

		try {

			FeatureIndexManager indexManager = new FeatureIndexManager(
					geoPackage, featureDao);
			featureTiles.setIndexManager(indexManager);

			indexManager.setIndexLocation(FeatureIndexType.GEOPACKAGE);
			int indexed = indexManager.index();
			TestCase.assertEquals(num, indexed);

			createTiles(featureTiles, 0, 3);
		} finally {
			featureTiles.close();
		}
	}

	private void createTiles(FeatureTiles featureTiles, int minZoom,
			int maxZoom) {
		for (int i = minZoom; i <= maxZoom; i++) {
			createTiles(featureTiles, i);
		}
	}

	private void createTiles(FeatureTiles featureTiles, int zoom) {
		int tilesPerSide = TileBoundingBoxUtils.tilesPerSide(zoom);
		for (int i = 0; i < tilesPerSide; i++) {
			for (int j = 0; j < tilesPerSide; j++) {
				BufferedImage image = featureTiles.drawTile(i, j, zoom);
				if (image != null) {
					long count = featureTiles.queryIndexedFeaturesCount(i, j,
							zoom);
					TestCase.assertTrue(count > 0);
					TestCase.assertEquals(featureTiles.getTileWidth(),
							image.getWidth());
					TestCase.assertEquals(featureTiles.getTileHeight(),
							image.getHeight());
				}
			}
		}
	}

}
