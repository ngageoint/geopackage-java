package mil.nga.geopackage.tiles;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Draw tiles from a GeoPackage tile table for a x, y, z coordinate
 * 
 * @author osbornb
 */
public class TileDraw {

	/**
	 * Draw the tile for the x, y, and z
	 * 
	 * @param geoPackageFile
	 * @param tileTable
	 * @param imageFormat
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage drawTile(File geoPackageFile, String tileTable,
			String imageFormat, long x, long y, long z) throws IOException {

		BufferedImage image = null;

		GeoPackage geoPackage = GeoPackageManager.open(geoPackageFile);
		try {
			drawTile(geoPackage, tileTable, imageFormat, x, y, z);
		} finally {
			geoPackage.close();
		}

		return image;
	}

	/**
	 * Draw the tile for the x, y, and z
	 * 
	 * @param geoPackage
	 * @param tileTable
	 * @param imageFormat
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage drawTile(GeoPackage geoPackage,
			String tileTable, String imageFormat, long x, long y, long z)
			throws IOException {

		// Get a tile data access object for the tile table
		TileDao tileDao = geoPackage.getTileDao(tileTable);

		BufferedImage image = drawTile(tileDao, imageFormat, x, y, z);

		return image;
	}

	/**
	 * Draw the tile for the x, y, and z
	 * 
	 * @param tileDao
	 * @param tileMatrix
	 * @param imageFormat
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage drawTile(TileDao tileDao, String imageFormat,
			long x, long y, long z) throws IOException {

		// Get the tile matrix at this zoom level
		TileMatrix tileMatrix = tileDao.getTileMatrix(z);

		// Get the projection of the tile matrix set
		long epsg = tileDao.getTileMatrixSet().getSrs()
				.getOrganizationCoordsysId();
		Projection projection = ProjectionFactory.getProjection(epsg);

		// Get the transformation to web mercator
		Projection webMercator = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
		ProjectionTransform projectionToWebMercator = projection
				.getTransformation(webMercator);

		// Get the tile matrix set and bounding box
		TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();
		BoundingBox setProjectionBoundingBox = tileMatrixSet.getBoundingBox();
		BoundingBox setWebMercatorBoundingBox = projectionToWebMercator
				.transform(setProjectionBoundingBox);

		// Create the buffered image
		BufferedImage image = TileDraw.drawTile(tileDao, tileMatrix,
				imageFormat, setWebMercatorBoundingBox, x, y, z);

		return image;
	}

	/**
	 * Draw the tile for the x, y, and z
	 * 
	 * @param tileDao
	 * @param tileMatrix
	 * @param imageFormat
	 * @param setWebMercatorBoundingBox
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage drawTile(TileDao tileDao,
			TileMatrix tileMatrix, String imageFormat,
			BoundingBox setWebMercatorBoundingBox, long x, long y, long z)
			throws IOException {

		BufferedImage image = null;

		// Get the bounding box of the requested tile
		BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(x, y, (int) z);

		// Get the tile grid
		TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
				setWebMercatorBoundingBox, tileMatrix.getMatrixWidth(),
				tileMatrix.getMatrixHeight(), webMercatorBoundingBox);

		// Query for matching tiles in the tile grid
		TileResultSet tileResultSet = tileDao.queryByTileGrid(tileGrid, z);
		if (tileResultSet != null) {

			try {

				// Get the requested tile dimensions
				int tileWidth = (int) tileMatrix.getTileWidth();
				int tileHeight = (int) tileMatrix.getTileHeight();

				// Draw the resulting bitmap with the matching tiles
				Graphics graphics = null;
				while (tileResultSet.moveToNext()) {

					// Get the next tile
					TileRow tileRow = tileResultSet.getRow();

					if (tileRow != null) {

						// Get the image bytes
						byte[] tileData = tileRow.getTileData();

						if (tileData != null) {

							// Create the buffered image
							BufferedImage tileImage = ImageIO
									.read(new ByteArrayInputStream(tileData));

							// Get the bounding box of the tile
							BoundingBox tileWebMercatorBoundingBox = TileBoundingBoxUtils
									.getWebMercatorBoundingBox(
											setWebMercatorBoundingBox,
											tileMatrix,
											tileRow.getTileColumn(),
											tileRow.getTileRow());

							// Get the bounding box where the requested image
							// and tile overlap
							BoundingBox overlap = TileBoundingBoxUtils.overlap(
									webMercatorBoundingBox,
									tileWebMercatorBoundingBox);

							// If the tile overlaps with the requested box
							if (overlap != null) {

								// Get the rectangle of the tile image to draw
								ImageRectangleF src = TileBoundingBoxJavaUtils
										.getRectangle(
												tileMatrix.getTileWidth(),
												tileMatrix.getTileHeight(),
												tileWebMercatorBoundingBox,
												overlap);

								// Get the rectangle of where to draw the tile
								// in the resulting image
								ImageRectangleF dest = TileBoundingBoxJavaUtils
										.getRectangle(tileWidth, tileHeight,
												webMercatorBoundingBox, overlap);

								// Round the rectangles and make sure the bounds
								// are valid
								ImageRectangle srcRounded = src.round();
								ImageRectangle destRounded = dest.round();
								if (srcRounded.isValid()
										&& destRounded.isValid()) {

									// Create the image first time through
									if (image == null) {
										image = ImageUtils.createBufferedImage(
												tileWidth, tileHeight,
												imageFormat);
										graphics = image.getGraphics();
									}

									// Draw the tile to the image
									graphics.drawImage(tileImage,
											destRounded.getLeft(),
											destRounded.getTop(),
											destRounded.getRight(),
											destRounded.getBottom(),
											srcRounded.getLeft(),
											srcRounded.getTop(),
											srcRounded.getRight(),
											srcRounded.getBottom(), null);
								}
							}
						}
					}
				}
			} finally {
				tileResultSet.close();
			}

			// Check if the entire image is transparent
			if (image != null && ImageUtils.isFullyTransparent(image)) {
				image = null;
			}

		}

		return image;
	}

	/**
	 * Attempt to get a single raw tile that aligns with the x, y, z location
	 * 
	 * @param tileDao
	 * @param tileMatrix
	 * @param setWebMercatorBoundingBox
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static TileRow getRawTileRow(TileDao tileDao, TileMatrix tileMatrix,
			BoundingBox setWebMercatorBoundingBox, long x, long y, long z) {

		TileRow rawTileRow = null;

		// Get the bounding box of the requested tile
		BoundingBox webMercatorBoundingBox = TileBoundingBoxUtils
				.getWebMercatorBoundingBox(x, y, (int) z);

		// Get the tile grid
		TileGrid tileGrid = TileBoundingBoxUtils.getTileGrid(
				setWebMercatorBoundingBox, tileMatrix.getMatrixWidth(),
				tileMatrix.getMatrixHeight(), webMercatorBoundingBox);

		// Query for matching tiles in the tile grid
		TileResultSet tileResultSet = tileDao.queryByTileGrid(tileGrid, z);
		if (tileResultSet != null) {

			try {

				// Get the requested tile dimensions
				int tileWidth = (int) tileMatrix.getTileWidth();
				int tileHeight = (int) tileMatrix.getTileHeight();

				while (tileResultSet.moveToNext()) {

					// Get the next tile
					TileRow tileRow = tileResultSet.getRow();

					if (tileRow != null) {

						// Get the image bytes
						byte[] tileData = tileRow.getTileData();

						if (tileData != null) {

							// Get the bounding box of the tile
							BoundingBox tileWebMercatorBoundingBox = TileBoundingBoxUtils
									.getWebMercatorBoundingBox(
											setWebMercatorBoundingBox,
											tileMatrix,
											tileRow.getTileColumn(),
											tileRow.getTileRow());

							// Get the bounding box where the requested image
							// and tile overlap
							BoundingBox overlap = TileBoundingBoxUtils.overlap(
									webMercatorBoundingBox,
									tileWebMercatorBoundingBox);

							// If the tile overlaps with the requested box
							if (overlap != null) {

								// Get the rectangle of the tile image to draw
								ImageRectangleF src = TileBoundingBoxJavaUtils
										.getRectangle(
												tileMatrix.getTileWidth(),
												tileMatrix.getTileHeight(),
												tileWebMercatorBoundingBox,
												overlap);

								// Get the rectangle of where to draw the tile
								// in the resulting image
								ImageRectangleF dest = TileBoundingBoxJavaUtils
										.getRectangle(tileWidth, tileHeight,
												webMercatorBoundingBox, overlap);

								// Round the rectangles and make sure the bounds
								// are valid
								ImageRectangle srcRounded = src.round();
								ImageRectangle destRounded = dest.round();
								if (srcRounded.isValid()
										&& destRounded.isValid()) {

									// Verify only one image was found and
									// it lines up perfectly
									if (rawTileRow != null
											|| !srcRounded.equals(destRounded)) {
										throw new GeoPackageException(
												"Raw image only supported when the images are aligned with the tile format requiring no combining and cropping");
									}

									rawTileRow = tileRow;
								}
							}
						}
					}
				}
			} finally {
				tileResultSet.close();
			}

		}

		return rawTileRow;
	}
}
