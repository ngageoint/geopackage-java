package mil.nga.geopackage.tiles.features;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import mil.nga.geopackage.extension.nga.style.StyleRow;

/**
 * Feature Paint Cache of Feature Paint objects for each feature id and draw
 * type
 *
 * @author osbornb
 * @since 3.2.0
 */
public class FeaturePaintCache {

	/**
	 * Default max number of feature style paints to maintain
	 */
	public static final int DEFAULT_STYLE_PAINT_CACHE_SIZE = 100;

	/**
	 * Feature paint cache
	 */
	private final Map<Long, FeaturePaint> paintCache;

	/**
	 * Max cache size
	 */
	private int cacheSize;

	/**
	 * Constructor
	 */
	public FeaturePaintCache() {
		this(DEFAULT_STYLE_PAINT_CACHE_SIZE);
	}

	/**
	 * Constructor
	 * 
	 * @param size
	 *            max paint objects to retain in the cache
	 */
	public FeaturePaintCache(int size) {
		cacheSize = size;
		paintCache = new LinkedHashMap<Long, FeaturePaint>(size, .75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Entry<Long, FeaturePaint> eldest) {
				return size() > cacheSize;
			}
		};
	}

	/**
	 * Clear the cache
	 */
	public void clear() {
		paintCache.clear();
	}

	/**
	 * Resize the cache
	 *
	 * @param maxSize
	 *            max size
	 */
	public void resize(int maxSize) {
		cacheSize = maxSize;
		if (paintCache.size() > maxSize) {
			int count = 0;
			Iterator<Long> rowIds = paintCache.keySet().iterator();
			while (rowIds.hasNext()) {
				rowIds.next();
				if (++count > maxSize) {
					rowIds.remove();
				}
			}
		}
	}

	/**
	 * Get the feature paint for the style row
	 *
	 * @param styleRow
	 *            style row
	 * @return feature paint
	 */
	public FeaturePaint getFeaturePaint(StyleRow styleRow) {
		return getFeaturePaint(styleRow.getId());
	}

	/**
	 * Get the feature paint for the style row id
	 *
	 * @param styleId
	 *            style row id
	 * @return feature paint
	 */
	public FeaturePaint getFeaturePaint(long styleId) {
		return paintCache.get(styleId);
	}

	/**
	 * Get the paint for the style row and draw type
	 *
	 * @param styleRow
	 *            style row
	 * @param type
	 *            feature draw type
	 * @return paint
	 */
	public Paint getPaint(StyleRow styleRow, FeatureDrawType type) {
		return getPaint(styleRow.getId(), type);
	}

	/**
	 * Get the paint for the style row id and draw type
	 *
	 * @param styleId
	 *            style row id
	 * @param type
	 *            feature draw type
	 * @return paint
	 */
	public Paint getPaint(long styleId, FeatureDrawType type) {
		Paint paint = null;
		FeaturePaint featurePaint = getFeaturePaint(styleId);
		if (featurePaint != null) {
			paint = featurePaint.getPaint(type);
		}
		return paint;
	}

	/**
	 * Set the paint for the style id and draw type
	 *
	 * @param styleRow
	 *            style row
	 * @param type
	 *            feature draw type
	 * @param paint
	 *            paint
	 */
	public void setPaint(StyleRow styleRow, FeatureDrawType type, Paint paint) {
		setPaint(styleRow.getId(), type, paint);
	}

	/**
	 * Set the paint for the style id and draw type
	 *
	 * @param styleId
	 *            style row id
	 * @param type
	 *            feature draw type
	 * @param paint
	 *            paint
	 */
	public void setPaint(long styleId, FeatureDrawType type, Paint paint) {
		FeaturePaint featurePaint = getFeaturePaint(styleId);
		if (featurePaint == null) {
			featurePaint = new FeaturePaint();
			paintCache.put(styleId, featurePaint);
		}
		featurePaint.setPaint(type, paint);
	}

}
