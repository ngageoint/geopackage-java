package mil.nga.geopackage.io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Writes the tiles from a GeoPackage tile table to a file system directory
 * 
 * To run from command line, build with the standalone profile ( mvn clean install -Pstandalone): 
 *    java -classpath geopackage-*-standalone.jar mil.nga.geopackage.io.GeoPackageTileWriter geopackage_file tile_table output_directory [image_format]
 * 
 * @author osbornb
 */
public class GeoPackageTileWriter {

	/**
	 * Default image format
	 */
	public static final String DEFAULT_FORMAT = "png";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(GeoPackageTileWriter.class.getName());

	/**
	 * Main method to write tiles from a GeoPackage
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 3 || args.length > 4) {
			System.out.println();
			System.out
					.println("Usage: geopackage_file tile_table output_directory [image_format]");
			System.out.println();
			System.out
					.println("\tgeopackage_file - path to the GeoPackage file containing the tiles");
			System.out
					.println("\ttile_table - tile table name within the GeoPackage file");
			System.out
					.println("\toutput_directory - output directory to write the tile images to");
			System.out
					.println("\timage_format - output image format, such as 'png' (default) or 'jpg'");
			System.out.println();
		} else {
			File geoPackageFile = new File(args[0]);
			String tileTable = args[1];
			File outputDirectory = new File(args[2]);
			String imageFormat = null;
			if (args.length == 4) {
				imageFormat = args[3];
			}
			writeTiles(geoPackageFile, tileTable, outputDirectory, imageFormat);
		}

	}

	/**
	 * Write the tile table tile image set within the GeoPackage file to the
	 * provided directory
	 * 
	 * @param geoPackageFile
	 *            GeoPackage file
	 * @param tileTable
	 *            tile table
	 * @param directory
	 *            output directory
	 * @param format
	 *            image format
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void writeTiles(File geoPackageFile, String tileTable,
			File directory, String format) throws SQLException, IOException {

		GeoPackage geoPackage = GeoPackageManager.open(geoPackageFile);
		try {
			writeTiles(geoPackage, tileTable, directory, format);
		} finally {
			geoPackage.close();
		}
	}

	/**
	 * Write the tile table tile image set within the GeoPackage file to the
	 * provided directory
	 * 
	 * @param geoPackageFile
	 *            open GeoPackage
	 * @param tileTable
	 *            tile table
	 * @param directory
	 *            output directory
	 * @param format
	 *            image format
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void writeTiles(GeoPackage geoPackage, String tileTable,
			File directory, String format) throws SQLException, IOException {

		// Get a tile data access object for the tile table
		TileDao tileDao = geoPackage.getTileDao(tileTable);

		// If no format, use the deafult
		if (format == null) {
			format = DEFAULT_FORMAT;
		}

		LOGGER.log(Level.INFO, "Zoom Range: " + tileDao.getMinZoom() + " - "
				+ tileDao.getMaxZoom());

		int totalCount = 0;

		// Go through each zoom level
		for (long zoomLevel = tileDao.getMinZoom(); zoomLevel <= tileDao
				.getMaxZoom(); zoomLevel++) {

			// Get the tile matrix at this zoom level
			TileMatrix tileMatrix = tileDao.getTileMatrix(zoomLevel);

			int zoomCount = 0;

			LOGGER.log(
					Level.INFO,
					"Zoom Level: "
							+ zoomLevel
							+ ", Width: "
							+ tileMatrix.getMatrixWidth()
							+ ", Height: "
							+ tileMatrix.getMatrixHeight()
							+ ", Max Tiles: "
							+ (tileMatrix.getMatrixWidth() * tileMatrix
									.getMatrixHeight()));

			File zDirectory = new File(directory, String.valueOf(zoomLevel));

			// Go through each x in the width
			for (int x = 0; x < tileMatrix.getMatrixWidth(); x++) {

				File xDirectory = new File(zDirectory, String.valueOf(x));

				// Go through each y in the height
				for (int y = 0; y < tileMatrix.getMatrixHeight(); y++) {

					File imageFile = new File(xDirectory, String.valueOf(y)
							+ "." + format);

					// Query for a tile at the x, y, z
					TileRow tileRow = tileDao.queryForTile(x, y, zoomLevel);

					if (tileRow != null) {

						// Get the image bytes
						byte[] tileData = tileRow.getTileData();

						if (tileData != null) {

							// Make any needed directories for the image
							xDirectory.mkdirs();

							// Create the buffered image
							BufferedImage image = ImageIO
									.read(new ByteArrayInputStream(tileData));

							// Write the image to the file
							ImageIO.write(image, format, imageFile);
							zoomCount++;
						}
					}
				}
			}

			LOGGER.log(Level.INFO, "Zoom " + zoomLevel + " Tiles: " + zoomCount);

			totalCount += zoomCount;
		}

		LOGGER.log(Level.INFO, "Total Tiles: " + totalCount);
	}

}
