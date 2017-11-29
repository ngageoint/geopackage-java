package mil.nga.geopackage.extension.coverage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.IOException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.coverage.CoverageDataCore;
import mil.nga.geopackage.extension.coverage.GriddedTile;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Tiled Gridded Coverage Data, PNG Encoding, Extension
 * 
 * @author osbornb
 * @since 2.0.1
 */
public class CoverageDataPng extends CoverageDataCommon<CoverageDataPngImage> {

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param tileDao
	 *            tile dao
	 * @param width
	 *            coverage data response width
	 * @param height
	 *            coverage data response height
	 * @param requestProjection
	 *            request projection
	 */
	public CoverageDataPng(GeoPackage geoPackage, TileDao tileDao,
			Integer width, Integer height, Projection requestProjection) {
		super(geoPackage, tileDao, width, height, requestProjection);
	}

	/**
	 * Constructor, use the coverage data tables pixel tile size as the request
	 * size width and height
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param tileDao
	 *            tile dao
	 */
	public CoverageDataPng(GeoPackage geoPackage, TileDao tileDao) {
		this(geoPackage, tileDao, null, null, tileDao.getProjection());
	}

	/**
	 * Constructor, use the coverage data tables pixel tile size as the request
	 * size width and height, request as the specified projection
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @param tileDao
	 *            tile dao
	 * @param requestProjection
	 *            request projection
	 */
	public CoverageDataPng(GeoPackage geoPackage, TileDao tileDao,
			Projection requestProjection) {
		this(geoPackage, tileDao, null, null, requestProjection);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CoverageDataPngImage createImage(TileRow tileRow) {
		return new CoverageDataPngImage(tileRow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getValue(GriddedTile griddedTile, TileRow tileRow, int x,
			int y) {
		BufferedImage image = null;
		try {
			image = tileRow.getTileDataImage();
		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed to get the Tile Row Data Image", e);
		}
		double value = getValue(griddedTile, image, x, y);
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double getValue(GriddedTile griddedTile, CoverageDataPngImage image,
			int x, int y) {
		return getValue(griddedTile, image.getRaster(), x, y);
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
					"The coverage data tile is expected to be a 16 bit unsigned short, actual: "
							+ image.getColorModel().getTransferType());
		}
	}

	/**
	 * Get the coverage data value
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param image
	 *            tile image
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return coverage data value
	 */
	public Double getValue(GriddedTile griddedTile, BufferedImage image, int x,
			int y) {
		short pixelValue = getPixelValue(image, x, y);
		Double value = getValue(griddedTile, pixelValue);
		return value;
	}

	/**
	 * Get the coverage data value
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return coverage data value
	 */
	public Double getValue(GriddedTile griddedTile, WritableRaster raster,
			int x, int y) {
		short pixelValue = getPixelValue(raster, x, y);
		Double value = getValue(griddedTile, pixelValue);
		return value;
	}

	/**
	 * Get the coverage data values
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param image
	 *            tile image
	 * @return coverage data values
	 */
	public Double[] getValues(GriddedTile griddedTile, BufferedImage image) {
		short[] pixelValues = getPixelValues(image);
		Double[] values = getValues(griddedTile, pixelValues);
		return values;
	}

	/**
	 * Get the coverage data values
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param raster
	 *            raster image
	 * @return coverage data values
	 */
	public Double[] getValues(GriddedTile griddedTile, WritableRaster raster) {
		short[] pixelValues = getPixelValues(raster);
		Double[] values = getValues(griddedTile, pixelValues);
		return values;
	}

	/**
	 * Draw a coverage data image tile from the flat array of "unsigned short"
	 * pixel values of length tileWidth * tileHeight where each pixel is at: (y
	 * * tileWidth) + x
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return coverage data image tile
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
	 * Draw a coverage data image tile and format as PNG bytes from the flat
	 * array of "unsigned short" pixel values of length tileWidth * tileHeight
	 * where each pixel is at: (y * tileWidth) + x
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return coverage data image tile bytes
	 */
	public byte[] drawTileData(short[] pixelValues, int tileWidth,
			int tileHeight) {
		BufferedImage image = drawTile(pixelValues, tileWidth, tileHeight);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw a coverage data tile from the double array of "unsigned short" pixel
	 * values formatted as short[row][width]
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values as [row][width]
	 * @return coverage data image tile
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
	 * Draw a coverage data tile and format as PNG bytes from the double array
	 * of "unsigned short" pixel values formatted as short[row][width]
	 * 
	 * @param pixelValues
	 *            "unsigned short" pixel values as [row][width]
	 * @return coverage data image tile bytes
	 */
	public byte[] drawTileData(short[][] pixelValues) {
		BufferedImage image = drawTile(pixelValues);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw a coverage data image tile from the flat array of unsigned 16 bit
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
	 * @return coverage data image tile
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
	 * Draw a coverage data image tile and format as PNG bytes from the flat
	 * array of unsigned 16 bit integer pixel values of length tileWidth *
	 * tileHeight where each pixel is at: (y * tileWidth) + x
	 * 
	 * @param unsignedPixelValues
	 *            unsigned 16 bit integer pixel values of length tileWidth *
	 *            tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return coverage data image tile bytes
	 */
	public byte[] drawTileData(int[] unsignedPixelValues, int tileWidth,
			int tileHeight) {
		BufferedImage image = drawTile(unsignedPixelValues, tileWidth,
				tileHeight);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw a coverage data image tile from the double array of unsigned 16 bit
	 * integer pixel values formatted as int[row][width]
	 * 
	 * @param unsignedPixelValues
	 *            unsigned 16 bit integer pixel values as [row][width]
	 * @return coverage data image tile
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
	 * Draw a coverage data image tile and format as PNG bytes from the double
	 * array of unsigned 16 bit integer pixel values formatted as
	 * int[row][width]
	 * 
	 * @param unsignedPixelValues
	 *            unsigned 16 bit integer pixel values as [row][width]
	 * @return coverage data image tile bytes
	 */
	public byte[] drawTileData(int[][] unsignedPixelValues) {
		BufferedImage image = drawTile(unsignedPixelValues);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw a coverage data image tile from the flat array of coverage data
	 * values of length tileWidth * tileHeight where each coverage data value is
	 * at: (y * tileWidth) + x
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param values
	 *            coverage data values of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return coverage data image tile
	 */
	public BufferedImage drawTile(GriddedTile griddedTile, Double[] values,
			int tileWidth, int tileHeight) {

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				Double value = values[(y * tileWidth) + x];
				short pixelValue = getPixelValue(griddedTile, value);
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw a coverage data image tile and format as PNG bytes from the flat
	 * array of coverage data values of length tileWidth * tileHeight where each
	 * coverage data value is at: (y * tileWidth) + x
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param values
	 *            coverage data values of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return coverage data image tile bytes
	 */
	public byte[] drawTileData(GriddedTile griddedTile, Double[] values,
			int tileWidth, int tileHeight) {
		BufferedImage image = drawTile(griddedTile, values, tileWidth,
				tileHeight);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw a coverage data image tile from the double array of unsigned
	 * coverage data values formatted as Double[row][width]
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param values
	 *            coverage data values as [row][width]
	 * @return coverage data image tile
	 */
	public BufferedImage drawTile(GriddedTile griddedTile, Double[][] values) {

		int tileWidth = values[0].length;
		int tileHeight = values.length;

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				Double value = values[y][x];
				short pixelValue = getPixelValue(griddedTile, value);
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw a coverage data image tile and format as PNG bytes from the double
	 * array of unsigned coverage data values formatted as Double[row][width]
	 * 
	 * @param griddedTile
	 *            gridded tile
	 * @param values
	 *            coverage data values as [row][width]
	 * @return coverage data image tile bytes
	 */
	public byte[] drawTileData(GriddedTile griddedTile, Double[][] values) {
		BufferedImage image = drawTile(griddedTile, values);
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
	 * Create the coverage data tile table with metadata and extension
	 * 
	 * @param geoPackage
	 * @param tableName
	 * @param contentsBoundingBox
	 * @param contentsSrsId
	 * @param tileMatrixSetBoundingBox
	 * @param tileMatrixSetSrsId
	 * @return coverage data
	 */
	public static CoverageDataPng createTileTableWithMetadata(
			GeoPackage geoPackage, String tableName,
			BoundingBox contentsBoundingBox, long contentsSrsId,
			BoundingBox tileMatrixSetBoundingBox, long tileMatrixSetSrsId) {

		TileMatrixSet tileMatrixSet = CoverageDataCore
				.createTileTableWithMetadata(geoPackage, tableName,
						contentsBoundingBox, contentsSrsId,
						tileMatrixSetBoundingBox, tileMatrixSetSrsId);
		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
		CoverageDataPng coverageData = new CoverageDataPng(geoPackage, tileDao);
		coverageData.getOrCreate();

		return coverageData;
	}

}
