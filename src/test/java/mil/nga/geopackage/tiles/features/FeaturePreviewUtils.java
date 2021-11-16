package mil.nga.geopackage.tiles.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.TileUtils;
import mil.nga.proj.ProjectionConstants;

/**
 * Test feature preview
 * 
 * @author osbornb
 */
public class FeaturePreviewUtils {

	/**
	 * Write images to disk for viewing / debugging during each feature table
	 * pass
	 */
	private static final boolean writeImages = false;

	/**
	 * Test the GeoPackage draw feature preview
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @throws IOException
	 *             upon error
	 */
	public static void testDraw(GeoPackage geoPackage) throws IOException {

		for (String featureTable : geoPackage.getFeatureTables()) {

			FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
			int count = featureDao.count(
					CoreSQLUtils.quoteWrap(featureDao.getGeometryColumnName())
							+ " IS NOT NULL");

			BoundingBox contentsBoundingBox = geoPackage
					.getContentsBoundingBox(featureTable);
			BoundingBox indexedBoundingBox = geoPackage
					.getBoundingBox(featureTable);
			boolean expectImage = (contentsBoundingBox != null
					|| indexedBoundingBox != null) && count > 0;
			boolean epsg = featureDao.getProjection().getAuthority()
					.equalsIgnoreCase(ProjectionConstants.AUTHORITY_EPSG);

			FeaturePreview preview = new FeaturePreview(geoPackage, featureDao);

			BufferedImage image = preview.draw();
			if (epsg) {
				assertEquals(expectImage, image != null);
			}
			if (writeImages) {
				ImageIO.write(image, "png", new File("image.png"));
			}

			preview.setBufferPercentage(0.4);
			preview.setLimit((int) Math.ceil(count / 2.0));
			BufferedImage imageLimit = preview.draw();
			if (epsg) {
				assertEquals(expectImage, imageLimit != null);
			}
			if (writeImages) {
				ImageIO.write(imageLimit, "png", new File("image_limit.png"));
			}

			preview.setManual(true);
			preview.setBufferPercentage(0.05);
			preview.setLimit(null);
			FeatureTiles featureTiles = preview.getFeatureTiles();
			featureTiles.setTileWidth(TileUtils.TILE_PIXELS_DEFAULT);
			featureTiles.setTileHeight(TileUtils.TILE_PIXELS_DEFAULT);
			featureTiles.setScale(
					TileUtils.tileScale(TileUtils.TILE_PIXELS_DEFAULT));
			featureTiles.clearIconCache();
			BufferedImage imageManual = preview.draw();
			if (epsg) {
				assertNotNull(imageManual);
			}
			if (writeImages) {
				ImageIO.write(imageManual, "png", new File("image_manual.png"));
			}

			preview.setBufferPercentage(0.35);
			preview.setLimit(Math.max(count - 1, 1));
			BufferedImage imageManualLimit = preview.draw();
			if (epsg) {
				assertNotNull(imageManualLimit);
			}
			if (writeImages) {
				ImageIO.write(imageManualLimit, "png",
						new File("image_manual_limit.png"));
			}

			preview.setBufferPercentage(0.15);
			preview.setLimit(null);
			preview.appendWhere(
					CoreSQLUtils.quoteWrap(featureDao.getIdColumnName()) + " > "
							+ ((int) Math.floor(count / 2.0)));
			BufferedImage imageManualWhere = preview.draw();
			if (epsg) {
				assertNotNull(imageManualWhere);
			}
			if (writeImages) {
				ImageIO.write(imageManualWhere, "png",
						new File("image_manual_where.png"));
				System.out.println("Breakpoint here");
			}

		}

	}

}
