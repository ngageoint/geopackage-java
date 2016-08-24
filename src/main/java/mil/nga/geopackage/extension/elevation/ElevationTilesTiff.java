package mil.nga.geopackage.extension.elevation;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.io.IOException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.property.GeoPackageProperties;
import mil.nga.geopackage.property.PropertyConstants;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileRow;

/**
 * Tiled Gridded Elevation Data, TIFF Encoding, Extension
 * 
 * @author osbornb
 * @since 1.2.1
 */
public class ElevationTilesTiff extends ElevationTilesCommon {

	/**
	 * Extension author
	 */
	public static final String EXTENSION_AUTHOR = GeoPackageConstants.GEO_PACKAGE_EXTENSION_AUTHOR;

	/**
	 * Extension name without the author
	 */
	public static final String EXTENSION_NAME_NO_AUTHOR = "elevation_tiles_tiff";

	/**
	 * Extension, with author and name
	 */
	public static final String EXTENSION_NAME = Extensions.buildExtensionName(
			EXTENSION_AUTHOR, EXTENSION_NAME_NO_AUTHOR);

	/**
	 * Extension definition URL
	 */
	public static final String EXTENSION_DEFINITION = GeoPackageProperties
			.getProperty(PropertyConstants.EXTENSIONS, EXTENSION_NAME_NO_AUTHOR);

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
	public ElevationTilesTiff(GeoPackage geoPackage, TileDao tileDao,
			Integer width, Integer height, Projection requestProjection) {
		super(geoPackage, EXTENSION_NAME, EXTENSION_DEFINITION, tileDao, width,
				height, requestProjection);
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
	public ElevationTilesTiff(GeoPackage geoPackage, TileDao tileDao) {
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
	public ElevationTilesTiff(GeoPackage geoPackage, TileDao tileDao,
			Projection requestProjection) {
		this(geoPackage, tileDao, null, null, requestProjection);
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
			ElevationImage image, int x, int y) {
		return getElevationValue(griddedTile, image.getRaster(), x, y);
	}

	/**
	 * Get the pixel value as a float
	 * 
	 * @param image
	 *            tile image
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return float pixel value
	 */
	public float getPixelValue(BufferedImage image, int x, int y) {
		validateImageType(image);
		WritableRaster raster = image.getRaster();
		float pixelValue = getPixelValue(raster, x, y);
		return pixelValue;
	}

	/**
	 * Get the pixel value as a float from the raster and the coordinate
	 * 
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @return float pixel value
	 */
	public float getPixelValue(WritableRaster raster, int x, int y) {
		Object pixelData = raster.getDataElements(x, y, null);
		float sdata[] = (float[]) pixelData;
		if (sdata.length != 1) {
			throw new UnsupportedOperationException(
					"This method is not supported by this color model");
		}
		float pixelValue = sdata[0];

		return pixelValue;
	}

	/**
	 * Get the pixel values of the buffered image as floats
	 * 
	 * @param image
	 *            tile image
	 * @return float pixel values
	 */
	public float[] getPixelValues(BufferedImage image) {
		validateImageType(image);
		WritableRaster raster = image.getRaster();
		float[] pixelValues = getPixelValues(raster);
		return pixelValues;
	}

	/**
	 * Get the pixel values of the raster as floats
	 * 
	 * @param raster
	 *            image raster
	 * @return float pixel values
	 */
	public float[] getPixelValues(WritableRaster raster) {
		DataBufferFloat buffer = (DataBufferFloat) raster.getDataBuffer();
		float[] pixelValues = buffer.getData();
		return pixelValues;
	}

	/**
	 * Validate that the image type is float
	 * 
	 * @param image
	 *            tile image
	 */
	public void validateImageType(BufferedImage image) {
		if (image == null) {
			throw new GeoPackageException("The image is null");
		}
		if (image.getColorModel().getTransferType() != DataBuffer.TYPE_FLOAT) {
			throw new GeoPackageException(
					"The elevation tile is expected to be a 32 bit float, actual: "
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
		float pixelValue = getPixelValue(image, x, y);
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
		float pixelValue = getPixelValue(raster, x, y);
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
		float[] pixelValues = getPixelValues(image);
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
		float[] pixelValues = getPixelValues(raster);
		Double[] elevations = getElevationValues(griddedTile, pixelValues);
		return elevations;
	}

	/**
	 * Draw an elevation image tile from the flat array of float pixel values of
	 * length tileWidth * tileHeight where each pixel is at: (y * tileWidth) + x
	 * 
	 * @param pixelValues
	 *            float pixel values of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(float[] pixelValues, int tileWidth,
			int tileHeight) {

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				float pixelValue = pixelValues[(y * tileWidth) + x];
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as TIFF bytes from the flat array
	 * of float pixel values of length tileWidth * tileHeight where each pixel
	 * is at: (y * tileWidth) + x
	 * 
	 * @param pixelValues
	 *            float pixel values of length tileWidth * tileHeight
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(float[] pixelValues, int tileWidth,
			int tileHeight) {
		BufferedImage image = drawTile(pixelValues, tileWidth, tileHeight);
		byte[] bytes = getImageBytes(image);
		return bytes;
	}

	/**
	 * Draw an elevation image tile from the double array of float pixel values
	 * formatted as float[row][width]
	 * 
	 * @param pixelValues
	 *            float pixel values as [row][width]
	 * @return elevation image tile
	 */
	public BufferedImage drawTile(float[][] pixelValues) {

		int tileWidth = pixelValues[0].length;
		int tileHeight = pixelValues.length;

		BufferedImage image = createImage(tileWidth, tileHeight);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < tileWidth; x++) {
			for (int y = 0; y < tileHeight; y++) {
				float pixelValue = pixelValues[y][x];
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as TIFF bytes from the double
	 * array of float pixel values formatted as float[row][width]
	 * 
	 * @param pixelValues
	 *            float pixel values as [row][width]
	 * @return elevation image tile bytes
	 */
	public byte[] drawTileData(float[][] pixelValues) {
		BufferedImage image = drawTile(pixelValues);
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
				float pixelValue = getPixelValue(griddedTile, elevation);
				setPixelValue(raster, x, y, pixelValue);
			}
		}

		return image;
	}

	/**
	 * Draw an elevation image tile and format as TIFF bytes from the flat array
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
	 * Draw an elevation image tile from the double array of elevations
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
	 * Draw an elevation image tile and format as TIFF bytes from the double
	 * array of elevations formatted as Double[row][width]
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
	 * Create a new image
	 * 
	 * @param tileWidth
	 *            tile width
	 * @param tileHeight
	 *            tile height
	 * @return image
	 */
	public BufferedImage createImage(int tileWidth, int tileHeight) {
		return new BufferedImage(tileWidth, tileHeight,
				BufferedImage.TYPE_USHORT_GRAY); // TODO
	}

	/**
	 * Get the image as TIFF bytes
	 * 
	 * @param image
	 *            buffered image
	 * @return image bytes
	 */
	public byte[] getImageBytes(BufferedImage image) {
		byte[] bytes = null;
		try {
			bytes = ImageUtils.writeImageToBytes(image,
					ImageUtils.IMAGE_FORMAT_TIFF);
		} catch (IOException e) {
			throw new GeoPackageException("Failed to write image to "
					+ ImageUtils.IMAGE_FORMAT_TIFF + " bytes", e);
		}
		return bytes;
	}

	/**
	 * Set the pixel value into the image raster
	 * 
	 * @param raster
	 *            image raster
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coordinate
	 * @param pixelValue
	 *            pixel value
	 */
	public void setPixelValue(WritableRaster raster, int x, int y,
			float pixelValue) {
		float data[] = new float[] { pixelValue };
		raster.setDataElements(x, y, data);
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
	public static ElevationTilesTiff createTileTableWithMetadata(
			GeoPackage geoPackage, String tableName,
			BoundingBox contentsBoundingBox, long contentsSrsId,
			BoundingBox tileMatrixSetBoundingBox, long tileMatrixSetSrsId) {

		TileMatrixSet tileMatrixSet = ElevationTilesCore
				.createTileTableWithMetadata(geoPackage, tableName,
						contentsBoundingBox, contentsSrsId,
						tileMatrixSetBoundingBox, tileMatrixSetSrsId);
		TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
		ElevationTilesTiff elevationTiles = new ElevationTilesTiff(geoPackage,
				tileDao);
		elevationTiles.getOrCreate();

		return elevationTiles;
	}

}
