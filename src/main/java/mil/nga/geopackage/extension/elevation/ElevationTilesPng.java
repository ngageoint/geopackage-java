package mil.nga.geopackage.extension.elevation;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.IOException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Tiled Gridded Elevation, PNG Encoding, Data Extension
 * 
 * @author osbornb
 * @since 1.2.1
 */
public class ElevationTilesPng extends ElevationTilesCommon<ElevationPngImage> {

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param tileDao
	 *            tile dao
	 * @param width
	 *            elevation response width
	 * @param height
	 *            elevation response height
	 * @param requestProjection
	 *            request projection
	 */
	public ElevationTilesPng(GeoPackage geoPackage, TileDao tileDao,
			Integer width, Integer height, Projection requestProjection) {
		super(geoPackage, tileDao, width, height, requestProjection);
	}

	/**
	 * Constructor, use the elevation tables pixel tile size as the request size
	 * width and height
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param tileDao
	 *            tile dao
	 */
	public ElevationTilesPng(GeoPackage geoPackage, TileDao tileDao) {
		this(geoPackage, tileDao, null, null, tileDao.getProjection());
	}

	/**
	 * Constructor, use the elevation tables pixel tile size as the request size
	 * width and height, request as the specified projection
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param tileDao
	 *            tile dao
	 * @param requestProjection
	 *            request projection
	 */
	public ElevationTilesPng(GeoPackage geoPackage, TileDao tileDao,
			Projection requestProjection) {
		this(geoPackage, tileDao, null, null, requestProjection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ElevationPngImage createElevationImage(TileRow tileRow) {
		return new ElevationPngImage(tileRow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getElevationValue(GriddedTile griddedTile, TileRow tileRow,
			int x, int y) {
		BufferedImage image = null;
		try {
			image = tileRow.getTileDataImage();
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed to get the Tile Row Data Image", e);
		}
		double elevation = getElevationValue(griddedTile, image, x, y);
		return elevation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double getElevationValue(GriddedTile griddedTile,
			ElevationPngImage image, int x, int y) {
		return getElevationValue(griddedTile, image.getRaster(), x, y);
	}

	/**
	 * Get the pixel value as an "unsigned short"
	 * 
	 * @param image
	 *            tile image
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return "unsigned short" pixel value
	 */
	public short getPixelValue(BufferedImage image, int x, int y) {
		validateImageType(image);
		WritableRaster raster = image.getRaster();
		short pixelValue = getPixelValue(raster, x, y);
		return pixelValue;
	}

	/**
	 * Get the pixel value as a 16 bit unsigned integer value
	 * 
	 * @param image
	 *            tile image
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return unsigned integer pixel value
	 */
	public int getUnsignedPixelValue(BufferedImage image, int x, int y) {
		short pixelValue = getPixelValue(image, x, y);
		int unsignedPixelValue = getUnsignedPixelValue(pixelValue);
		return unsignedPixelValue;
	}

	/**
	 * Get the pixel value as an "unsigned short" from the raster and the
	 * coordinate
	 * 
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return "unsigned short" pixel value
	 */
	public short getPixelValue(WritableRaster raster, int x, int y) {
		Object pixelData = raster.getDataElements(x, y, null);
		short sdata[] = (short[]) pixelData;
		if (sdata.length != 1) {
			throw new UnsupportedOperationException(
					"This method is not supported by this color model");
		}
		short pixelValue = sdata[0];

		return pixelValue;
	}

	/**
	 * Get the pixel value as a 16 bit unsigned integer value
	 * 
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return unsigned integer pixel value
	 */
	public int getUnsignedPixelValue(WritableRaster raster, int x, int y) {
		short pixelValue = getPixelValue(raster, x, y);
		int unsignedPixelValue = getUnsignedPixelValue(pixelValue);
		return unsignedPixelValue;
	}

	/**
	 * Get the pixel values of the buffered image as "unsigned shorts"
	 * 
	 * @param image
	 *            tile image
	 * @return "unsigned short" pixel values
	 */
	public short[] getPixelValues(BufferedImage image) {
		validateImageType(image);
		WritableRaster raster = image.getRaster();
		short[] pixelValues = getPixelValues(raster);
		return pixelValues;
	}

	/**
	 * Get the pixel values of the buffered image as 16 bit unsigned integer
	 * values
	 * 
	 * @param image
	 *            tile image
	 * @return unsigned integer pixel values
	 */
	public int[] getUnsignedPixelValues(BufferedImage image) {
		short[] pixelValues = getPixelValues(image);
		int[] unsignedPixelValues = getUnsignedPixelValues(pixelValues);
		return unsignedPixelValues;
	}

	/**
	 * Get the pixel values of the raster as "unsigned shorts"
	 * 
	 * @param raster
	 *            image raster
	 * @return "unsigned short" pixel values
	 */
	public short[] getPixelValues(WritableRaster raster) {
		DataBufferUShort buffer = (DataBufferUShort) raster.getDataBuffer();
		short[] pixelValues = buffer.getData();
		return pixelValues;
	}

	/**
	 * Get the pixel values of the raster as 16 bit unsigned integer values
	 * 
	 * @param raster
	 *            image raster
	 * @return unsigned integer pixel values
	 */
	public int[] getUnsignedPixelValues(WritableRaster raster) {
		short[] pixelValues = getPixelValues(raster);
		int[] unsignedPixelValues = getUnsignedPixelValues(pixelValues);
		return unsignedPixelValues;
	}

	/**
	 * Validate that the image type is an unsigned short
	 * 
	 * @param image
	 *            tile image
	 */
	public void validateImageType(BufferedImage image) {
		if (image == null) {
			throw new GeoPackageException("The image is null");
		}
		if (image.getColorModel().getTransferType() != DataBuffer.TYPE_USHORT) {
			throw new GeoPackageException(
					"The elevation tile is expected to be a 16 bit unsigned short, actual: "
							+ image.getColorModel().getTransferType());
		}
	}

	/**
	 * Get the elevation value
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param image
	 *            tile image
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return elevation value
	 */
	public Double getElevationValue(GriddedTile griddedTile,
			BufferedImage image, int x, int y) {
		short pixelValue = getPixelValue(image, x, y);
		Double elevation = getElevationValue(griddedTile, pixelValue);
		return elevation;
	}

	/**
	 * Get the elevation value
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return elevation value
	 */
	public Double getElevationValue(GriddedTile griddedTile,
			WritableRaster raster, int x, int y) {
		short pixelValue = getPixelValue(raster, x, y);
		Double elevation = getElevationValue(griddedTile, pixelValue);
		return elevation;
	}

	/**
	 * Get the elevation values
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param image
	 *            tile image
	 * @return elevation values
	 */
	public Double[] getElevationValues(GriddedTile griddedTile,
			BufferedImage image) {
		short[] pixelValues = getPixelValues(image);
		Double[] elevations = getElevationValues(griddedTile, pixelValues);
		return elevations;
	}

	/**
	 * Get the elevation values
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param raster
	 *            raster image
	 * @return elevation values
	 */
	public Double[] getElevationValues(GriddedTile griddedTile,
			WritableRaster raster) {
		short[] pixelValues = getPixelValues(raster);
		Double[] elevations = getElevationValues(griddedTile, pixelValues);
		return elevations;
	}

	/**
	 * Draw an elevation image tile from the flat array of "unsigned short"
	 * pixel values of length tileWidth * tileHeight where each pixel is at: (y
	 * * tileWidth) + x
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(short[] pixelValues, int tileWidth,
			int tileHeight) {

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				short pixelValue = pixelValues[(y * tileWidth) + x];
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as PNG bytes from the flat array
	 * of "unsigned short" pixel values of length tileWidth * tileHeight where
	 * each pixel is at: (y * tileWidth) + x
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(short[] pixelValues, int tileWidth,
			int tileHeight) {
		BufferedImage image = drawTile(pixelValues, tileWidth, tileHeight);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw an elevation tile from the double array of "unsigned short" pixel
	 * values formatted as short[row][width]
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values as [row][width]
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(short[][] pixelValues) {

		int tileWidth = pixelValues[0].length;
		int tileHeight = pixelValues.length;

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				short pixelValue = pixelValues[y][x];
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation tile and format as PNG bytes from the double array of
	 * "unsigned short" pixel values formatted as short[row][width]
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values as [row][width]
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(short[][] pixelValues) {
		BufferedImage image = drawTile(pixelValues);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw an elevation image tile from the flat array of unsigned 16 bit
	 * integer pixel values of length tileWidth * tileHeight where each pixel is
	 * at: (y * tileWidth) + x
	 * 
	 * @param unsignedPixelValues
	 *            unsigned 16 bit integer pixel values of length tileWidth *
	 *            tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(int[] unsignedPixelValues, int tileWidth,
			int tileHeight) {

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				int unsignedPixelValue = unsignedPixelValues[(y * tileWidth)
						+ x];
				setPixelValue(raster, x, y, unsignedPixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as PNG bytes from the flat array
	 * of unsigned 16 bit integer pixel values of length tileWidth * tileHeight
	 * where each pixel is at: (y * tileWidth) + x
	 * 
	 * @param unsignedPixelValues
	 *            unsigned 16 bit integer pixel values of length tileWidth *
	 *            tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(int[] unsignedPixelValues, int tileWidth,
			int tileHeight) {
		BufferedImage image = drawTile(unsignedPixelValues, tileWidth,
				tileHeight);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw an elevation image tile from the double array of unsigned 16 bit
	 * integer pixel values formatted as int[row][width]
	 * 
	 * @param unsignedPixelValues
	 *            unsigned 16 bit integer pixel values as [row][width]
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(int[][] unsignedPixelValues) {

		int tileWidth = unsignedPixelValues[0].length;
		int tileHeight = unsignedPixelValues.length;

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				int unsignedPixelValue = unsignedPixelValues[y][x];
				setPixelValue(raster, x, y, unsignedPixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as PNG bytes from the double
	 * array of unsigned 16 bit integer pixel values formatted as
	 * int[row][width]
	 * 
	 * @param unsignedPixelValues
	 *            unsigned 16 bit integer pixel values as [row][width]
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(int[][] unsignedPixelValues) {
		BufferedImage image = drawTile(unsignedPixelValues);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw an elevation image tile from the flat array of elevations of length
	 * tileWidth * tileHeight where each elevation is at: (y * tileWidth) + x
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param elevations
	 *            elevations of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(GriddedTile griddedTile, Double[] elevations,
			int tileWidth, int tileHeight) {

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				Double elevation = elevations[(y * tileWidth) + x];
				short pixelValue = getPixelValue(griddedTile, elevation);
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as PNG bytes from the flat array
	 * of elevations of length tileWidth * tileHeight where each elevation is
	 * at: (y * tileWidth) + x
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param elevations
	 *            elevations of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(GriddedTile griddedTile, Double[] elevations,
			int tileWidth, int tileHeight) {
		BufferedImage image = drawTile(griddedTile, elevations, tileWidth,
				tileHeight);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw an elevation image tile from the double array of unsigned elevations
	 * formatted as Double[row][width]
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param elevations
	 *            elevations as [row][width]
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(GriddedTile griddedTile, Double[][] elevations) {

		int tileWidth = elevations[0].length;
		int tileHeight = elevations.length;

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				Double elevation = elevations[y][x];
				short pixelValue = getPixelValue(griddedTile, elevation);
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as PNG bytes from the double
	 * array of unsigned elevations formatted as Double[row][width]
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param elevations
	 *            elevations as [row][width]
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(GriddedTile griddedTile, Double[][] elevations) {
		BufferedImage image = drawTile(griddedTile, elevations);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Create a new unsigned 16 bit short grayscale image
	 * 
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return image
	 */
	public BufferedImage createImage(int tileWidth, int tileHeight) {
		return new BufferedImage(tileWidth, tileHeight,
				BufferedImage.TYPE_USHORT_GRAY);
	}

	/**
	 * Get the image as PNG bytes
	 * 
	 * @param image
	 *            buffered image
	 * @return image bytes
	 */
	public byte[] getImageBytes(BufferedImage image) {
		byte[] bytes = null;
		try {
			bytes = ImageUtils.writeImageToBytes(image,
					ImageUtils.IMAGE_FORMAT_PNG);
		} catch (IOException e) {
			throw new GeoPackageException("Failed to write image to "
					+ ImageUtils.IMAGE_FORMAT_PNG + " bytes", e);
		}
		return bytes;
	}

	/**
	 * Set the "unsigned short" pixel value into the image raster
	 * 
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param pixelValue
	 *            "unsigned short" pixel value
	 */
	public void setPixelValue(WritableRaster raster, int x, int y,
			short pixelValue) {
		short data[] = new short[] { pixelValue };
		raster.setDataElements(x, y, data);
	}

	/**
	 * Set the unsigned 16 bit integer pixel value into the image raster
	 * 
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param unsignedPixelValue
	 *            unsigned 16 bit integer pixel value
	 */
	public void setPixelValue(WritableRaster raster, int x, int y,
			int unsignedPixelValue) {
		short pixelValue = getPixelValue(unsignedPixelValue);
		setPixelValue(raster, x, y, pixelValue);
	}

	/**
	 * Create the elevation tile table with metadata and extension
	 * 
	 * @param geoPackage
	 * @param tableName
	 * @param contentsBoundingBox
	 * @param contentsSrsId
	 * @param tileMatrixSetBoundingBox
	 * @param tileMatrixSetSrsId
	 * @return elevation tiles
	 */
	public static ElevationTilesPng createTileTableWithMetadata(
			GeoPackage geoPackage, String tableName,
			BoundingBox contentsBoundingBox, long contentsSrsId,
			BoundingBox tileMatrixSetBoundingBox, long tileMatrixSetSrsId) {

		TileMatrixSet tileMatrixSet = ElevationTilesCore
				.createTileTableWithMetadata(geoPackage, tableName,
						contentsBoundingBox, contentsSrsId,
						tileMatrixSetBoundingBox, tileMatrixSetSrsId);
		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
		ElevationTilesPng elevationTiles = new ElevationTilesPng(geoPackage,
				tileDao);
		elevationTiles.getOrCreate();

		return elevationTiles;
	}

}
