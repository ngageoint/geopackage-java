package mil.nga.geopackage.extension.style;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import mil.nga.geopackage.GeoPackageException;

/**
 * Icon Cache of icon images
 *
 * @author osbornb
 * @since 3.1.1
 */
public class IconCache {

	/**
	 * Default max number of icon images to retain in cache
	 */
	public static final int DEFAULT_CACHE_SIZE = 100;

	/**
	 * Icon image cache
	 */
	private final Map<Long, BufferedImage> iconCache;

	/**
	 * Max cache size
	 */
	private int cacheSize;

	/**
	 * Constructor, created with cache size of {@link #DEFAULT_CACHE_SIZE}
	 */
	public IconCache() {
		this(DEFAULT_CACHE_SIZE);
	}

	/**
	 * Constructor
	 *
	 * @param size
	 *            max icon images to retain in the cache
	 */
	public IconCache(int size) {
		cacheSize = size;
		iconCache = new LinkedHashMap<Long, BufferedImage>(size, .75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(
					Entry<Long, BufferedImage> eldest) {
				return size() > cacheSize;
			}
		};
	}

	/**
	 * Get the cached image for the icon row or null if not cached
	 *
	 * @param iconRow
	 *            icon row
	 * @return icon image or null
	 */
	public BufferedImage get(IconRow iconRow) {
		return get(iconRow.getId());
	}

	/**
	 * Get the cached image for the icon row id or null if not cached
	 *
	 * @param iconRowId
	 *            icon row id
	 * @return icon image or null
	 */
	public BufferedImage get(long iconRowId) {
		return iconCache.get(iconRowId);
	}

	/**
	 * Cache the icon image for the icon row
	 *
	 * @param iconRow
	 *            icon row
	 * @param image
	 *            icon image
	 * @return previous cached icon image or null
	 */
	public BufferedImage put(IconRow iconRow, BufferedImage image) {
		return put(iconRow.getId(), image);
	}

	/**
	 * Cache the icon image for the icon row id
	 *
	 * @param iconRowId
	 *            icon row id
	 * @param image
	 *            icon image
	 * @return previous cached icon image or null
	 */
	public BufferedImage put(long iconRowId, BufferedImage image) {
		return iconCache.put(iconRowId, image);
	}

	/**
	 * Remove the cached image for the icon row
	 *
	 * @param iconRow
	 *            icon row
	 * @return removed icon image or null
	 */
	public BufferedImage remove(IconRow iconRow) {
		return remove(iconRow.getId());
	}

	/**
	 * Remove the cached image for the icon row id
	 *
	 * @param iconRowId
	 *            icon row id
	 * @return removed icon image or null
	 */
	public BufferedImage remove(long iconRowId) {
		return iconCache.remove(iconRowId);
	}

	/**
	 * Clear the cache
	 */
	public void clear() {
		iconCache.clear();
	}

	/**
	 * Resize the cache
	 *
	 * @param maxSize
	 *            max size
	 */
	public void resize(int maxSize) {
		cacheSize = maxSize;
		if (iconCache.size() > maxSize) {
			int count = 0;
			Iterator<Long> rowIds = iconCache.keySet().iterator();
			while (rowIds.hasNext()) {
				rowIds.next();
				if (++count > maxSize) {
					rowIds.remove();
				}
			}
		}
	}

	/**
	 * Create or retrieve from cache an icon image for the icon row
	 *
	 * @param icon
	 *            icon row
	 * @return icon image
	 */
	public BufferedImage createIcon(IconRow icon) {
		return createIcon(icon, this);
	}

	/**
	 * Create an icon image for the icon row without caching
	 *
	 * @param icon
	 *            icon row
	 * @return icon image
	 */
	public static BufferedImage createIconNoCache(IconRow icon) {
		return createIcon(icon, null);
	}

	/**
	 * Create or retrieve from cache an icon image for the icon row
	 *
	 * @param icon
	 *            icon row
	 * @param iconCache
	 *            icon cache
	 * @return icon image
	 */
	public static BufferedImage createIcon(IconRow icon, IconCache iconCache) {

		BufferedImage iconImage = null;

		if (icon != null) {

			if (iconCache != null) {
				iconImage = iconCache.get(icon.getId());
			}

			if (iconImage == null) {

				try {
					iconImage = icon.getDataImage();
				} catch (IOException e) {
					throw new GeoPackageException(
							"Failed to get the Icon Row image. Id: "
									+ icon.getId() + ", Name: "
									+ icon.getName(), e);
				}

				Double iconWidth = icon.getWidth();
				Double iconHeight = icon.getHeight();

				if (iconWidth != null || iconHeight != null) {

					int dataWidth = iconImage.getWidth();
					int dataHeight = iconImage.getHeight();

					if (iconWidth == null) {
						iconWidth = dataWidth * (iconHeight / dataHeight);
					} else if (iconHeight == null) {
						iconHeight = dataHeight * (iconWidth / dataWidth);
					}

					int scaledWidth = Math.round(iconWidth.floatValue());
					int scaledHeight = Math.round(iconHeight.floatValue());

					if (scaledWidth != dataWidth || scaledHeight != dataHeight) {

						Image scaledImage = iconImage.getScaledInstance(
								scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
						iconImage = new BufferedImage(scaledWidth,
								scaledHeight, BufferedImage.TYPE_INT_ARGB);

						Graphics2D graphics = iconImage.createGraphics();
						graphics.drawImage(scaledImage, 0, 0, null);
						graphics.dispose();

					}

				}

				if (iconCache != null) {
					iconCache.put(icon.getId(), iconImage);
				}
			}

		}

		return iconImage;
	}

}
