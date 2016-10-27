package mil.nga.geopackage.tiles;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.io.GeoPackageIOUtils;
import mil.nga.geopackage.io.TileFormatType;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.property.GeoPackageJavaProperties;
import mil.nga.geopackage.property.JavaPropertyConstants;

/**
 * Creates a set of tiles within a GeoPackage by downloading the tiles from a
 * URL
 * 
 * @author osbornb
 * @since 1.1.2
 */
public class UrlTileGenerator extends TileGenerator {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(UrlTileGenerator.class.getName());

	/**
	 * URL Z Variable
	 */
	private static final String Z_VARIABLE = GeoPackageJavaProperties
			.getProperty(JavaPropertyConstants.TILE_GENERATOR_VARIABLE,
					JavaPropertyConstants.TILE_GENERATOR_VARIABLE_Z);

	/**
	 * URL X Variable
	 */
	private static final String X_VARIABLE = GeoPackageJavaProperties
			.getProperty(JavaPropertyConstants.TILE_GENERATOR_VARIABLE,
					JavaPropertyConstants.TILE_GENERATOR_VARIABLE_X);

	/**
	 * URL Y Variable
	 */
	private static final String Y_VARIABLE = GeoPackageJavaProperties
			.getProperty(JavaPropertyConstants.TILE_GENERATOR_VARIABLE,
					JavaPropertyConstants.TILE_GENERATOR_VARIABLE_Y);

	/**
	 * URL Min Lat Variable
	 */
	private static final String MIN_LAT_VARIABLE = GeoPackageJavaProperties
			.getProperty(JavaPropertyConstants.TILE_GENERATOR_VARIABLE,
					JavaPropertyConstants.TILE_GENERATOR_VARIABLE_MIN_LAT);

	/**
	 * URL Max Lat Variable
	 */
	private static final String MAX_LAT_VARIABLE = GeoPackageJavaProperties
			.getProperty(JavaPropertyConstants.TILE_GENERATOR_VARIABLE,
					JavaPropertyConstants.TILE_GENERATOR_VARIABLE_MAX_LAT);

	/**
	 * URL Min Lon Variable
	 */
	private static final String MIN_LON_VARIABLE = GeoPackageJavaProperties
			.getProperty(JavaPropertyConstants.TILE_GENERATOR_VARIABLE,
					JavaPropertyConstants.TILE_GENERATOR_VARIABLE_MIN_LON);

	/**
	 * URL Max Lon Variable
	 */
	private static final String MAX_LON_VARIABLE = GeoPackageJavaProperties
			.getProperty(JavaPropertyConstants.TILE_GENERATOR_VARIABLE,
					JavaPropertyConstants.TILE_GENERATOR_VARIABLE_MAX_LON);

	/**
	 * Tile URL
	 */
	private final String tileUrl;

	/**
	 * True if the URL has x, y, or z variables
	 */
	private final boolean urlHasXYZ;

	/**
	 * True if the URL has bounding box variables
	 */
	private final boolean urlHasBoundingBox;

	/**
	 * Tile Format when downloading tiles with x, y, and z values
	 */
	private TileFormatType tileFormat = TileFormatType.STANDARD;

	/**
	 * Download attempts per tile
	 */
	private int downloadAttempts = GeoPackageJavaProperties.getIntegerProperty(
			JavaPropertyConstants.TILE_GENERATOR,
			JavaPropertyConstants.TILE_GENERATOR_DOWNLOAD_ATTEMPTS);

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param tableName
	 *            table name
	 * @param tileUrl
	 *            tile url
	 * @param minZoom
	 *            min zoom
	 * @param maxZoom
	 *            max zoom
	 * @param boundingBox
	 *            tiles bounding box
	 * @param projection
	 *            tiles projection
	 * @since 1.2.0
	 */
	public UrlTileGenerator(GeoPackage geoPackage, String tableName,
			String tileUrl, int minZoom, int maxZoom, BoundingBox boundingBox,
			Projection projection) {
		super(geoPackage, tableName, minZoom, maxZoom, boundingBox, projection);

		try {
			this.tileUrl = URLDecoder.decode(tileUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new GeoPackageException("Failed to decode tile url: "
					+ tileUrl, e);
		}

		this.urlHasXYZ = hasXYZ(tileUrl);
		this.urlHasBoundingBox = hasBoundingBox(tileUrl);

		if (!this.urlHasXYZ && !this.urlHasBoundingBox) {
			throw new GeoPackageException(
					"URL does not contain x,y,z or bounding box variables: "
							+ tileUrl);
		}
	}

	/**
	 * Get the tile format
	 * 
	 * @return tile format
	 */
	public TileFormatType getTileFormat() {
		return tileFormat;
	}

	/**
	 * Set the tile format
	 * 
	 * @param tileFormat
	 *            tile format
	 */
	public void setTileFormat(TileFormatType tileFormat) {
		if (tileFormat == null) {
			tileFormat = TileFormatType.STANDARD;
		} else {
			switch (tileFormat) {
			case STANDARD:
			case TMS:
				this.tileFormat = tileFormat;
				break;
			default:
				throw new GeoPackageException(
						"Unsupported Tile Format Type for URL Tile Generation: "
								+ tileFormat);
			}
		}
	}

	/**
	 * Get the number of download attempts per tile
	 * 
	 * @return download attempts
	 */
	public int getDownloadAttempts() {
		return downloadAttempts;
	}

	/**
	 * Set the number of download attempts per tile
	 * 
	 * @param downloadAttempts
	 */
	public void setDownloadAttempts(int downloadAttempts) {
		this.downloadAttempts = downloadAttempts;
	}

	/**
	 * Determine if the url has bounding box variables
	 * 
	 * @param url
	 * @return
	 */
	private boolean hasBoundingBox(String url) {

		String replacedUrl = replaceBoundingBox(url, boundingBox);
		boolean hasBoundingBox = !replacedUrl.equals(url);

		return hasBoundingBox;
	}

	/**
	 * Replace x, y, and z in the url
	 * 
	 * @param url
	 * @param z
	 * @param x
	 * @param y
	 * @return
	 */
	private String replaceXYZ(String url, int z, long x, long y) {

		url = url.replaceAll(Z_VARIABLE, String.valueOf(z));
		url = url.replaceAll(X_VARIABLE, String.valueOf(x));
		url = url.replaceAll(Y_VARIABLE, String.valueOf(y));
		return url;
	}

	/**
	 * Determine if the url has x, y, or z variables
	 * 
	 * @param url
	 * @return
	 */
	private boolean hasXYZ(String url) {

		String replacedUrl = replaceXYZ(url, 0, 0, 0);
		boolean hasXYZ = !replacedUrl.equals(url);

		return hasXYZ;
	}

	/**
	 * Replace the bounding box coordinates in the url
	 * 
	 * @param url
	 * @param z
	 * @param x
	 * @param y
	 * @return
	 */
	private String replaceBoundingBox(String url, int z, long x, long y) {

		BoundingBox boundingBox = TileBoundingBoxUtils.getProjectedBoundingBox(
				projection, x, y, z);

		url = replaceBoundingBox(url, boundingBox);

		return url;
	}

	/**
	 * Replace the url parts with the bounding box
	 * 
	 * @param url
	 * @param boundingBox
	 * @return
	 */
	private String replaceBoundingBox(String url, BoundingBox boundingBox) {

		url = url.replaceAll(MIN_LAT_VARIABLE,
				String.valueOf(boundingBox.getMinLatitude()));
		url = url.replaceAll(MAX_LAT_VARIABLE,
				String.valueOf(boundingBox.getMaxLatitude()));
		url = url.replaceAll(MIN_LON_VARIABLE,
				String.valueOf(boundingBox.getMinLongitude()));
		url = url.replaceAll(MAX_LON_VARIABLE,
				String.valueOf(boundingBox.getMaxLongitude()));

		return url;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preTileGeneration() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected byte[] createTile(int z, long x, long y) {

		byte[] bytes = null;

		String zoomUrl = tileUrl;

		// Replace x, y, and z
		if (urlHasXYZ) {
			long yRequest = y;

			// If TMS, flip the y value
			if (tileFormat == TileFormatType.TMS) {
				yRequest = TileBoundingBoxUtils.getYAsOppositeTileFormat(z,
						(int) y);
			}

			zoomUrl = replaceXYZ(zoomUrl, z, x, yRequest);
		}

		// Replace bounding box
		if (urlHasBoundingBox) {
			zoomUrl = replaceBoundingBox(zoomUrl, z, x, y);
		}

		URL url;
		try {
			url = new URL(zoomUrl);
		} catch (MalformedURLException e) {
			throw new GeoPackageException("Failed to download tile. URL: "
					+ zoomUrl + ", z=" + z + ", x=" + x + ", y=" + y, e);
		}

		int attempt = 1;
		while (true) {
			try {
				bytes = downloadTile(zoomUrl, url, z, x, y);
				break;
			} catch (Exception e) {
				if (attempt < downloadAttempts) {
					LOGGER.log(Level.WARNING,
							"Failed to download tile after attempt " + attempt
									+ " of " + downloadAttempts + ". URL: "
									+ zoomUrl + ", z=" + z + ", x=" + x
									+ ", y=" + y, e);
					attempt++;
				} else {
					throw new GeoPackageException(
							"Failed to download tile after " + downloadAttempts
									+ " attempts. URL: " + zoomUrl + ", z=" + z
									+ ", x=" + x + ", y=" + y, e);
				}
			}
		}

		return bytes;
	}

	/**
	 * Download the tile from the URL
	 * 
	 * @param zoomUrl
	 * @param url
	 * @param z
	 * @param x
	 * @param y
	 * @return tile bytes
	 */
	private byte[] downloadTile(String zoomUrl, URL url, int z, long x, long y) {

		byte[] bytes = null;

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new GeoPackageException("Failed to download tile. URL: "
						+ zoomUrl + ", z=" + z + ", x=" + x + ", y=" + y
						+ ", Response Code: " + connection.getResponseCode()
						+ ", Response Message: "
						+ connection.getResponseMessage());
			}

			InputStream geoPackageStream = connection.getInputStream();
			bytes = GeoPackageIOUtils.streamBytes(geoPackageStream);

		} catch (IOException e) {
			throw new GeoPackageException("Failed to download tile. URL: "
					+ zoomUrl + ", z=" + z + ", x=" + x + ", y=" + y, e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return bytes;
	}

}
