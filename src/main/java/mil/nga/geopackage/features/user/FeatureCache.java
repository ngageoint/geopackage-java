package mil.nga.geopackage.features.user;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Feature Row Cache for a single feature table
 *
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureCache {

	/**
	 * Default max number of feature rows to retain in cache
	 */
	public static final int DEFAULT_CACHE_MAX_SIZE = 1000;

	/**
	 * Feature Row cache
	 */
	private final Map<Long, FeatureRow> cache;

	/**
	 * Max cache size
	 */
	private int maxSize;

	/**
	 * Constructor, created with cache max size of
	 * {@link #DEFAULT_CACHE_MAX_SIZE}
	 */
	public FeatureCache() {
		this(DEFAULT_CACHE_MAX_SIZE);
	}

	/**
	 * Constructor
	 *
	 * @param size
	 *            max feature rows to retain in the cache
	 */
	public FeatureCache(int size) {
		maxSize = size;
		cache = new LinkedHashMap<Long, FeatureRow>(maxSize, .75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Entry<Long, FeatureRow> eldest) {
				return size() > maxSize;
			}
		};
	}

	/**
	 * Get the cache max size
	 *
	 * @return max size
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * Get the current cache size, number of feature rows cached
	 *
	 * @return cache size
	 */
	public int getSize() {
		return cache.size();
	}

	/**
	 * Get the cached feature row by feature id
	 *
	 * @param featureId
	 *            feature row id
	 * @return feature row or null
	 */
	public FeatureRow get(long featureId) {
		return cache.get(featureId);
	}

	/**
	 * Cache the feature row
	 *
	 * @param featureRow
	 *            feature row
	 * @return previous cached feature row or null
	 */
	public FeatureRow put(FeatureRow featureRow) {
		return cache.put(featureRow.getId(), featureRow);
	}

	/**
	 * Remove the cached feature row
	 *
	 * @param featureRow
	 *            feature row
	 * @return removed feature row or null
	 */
	public FeatureRow remove(FeatureRow featureRow) {
		return remove(featureRow.getId());
	}

	/**
	 * Remove the cached feature row by id
	 *
	 * @param featureId
	 *            feature row id
	 * @return removed feature row or null
	 */
	public FeatureRow remove(long featureId) {
		return cache.remove(featureId);
	}

	/**
	 * Clear the cache
	 */
	public void clear() {
		cache.clear();
	}

	/**
	 * Resize the cache
	 *
	 * @param maxSize
	 *            max size
	 */
	public void resize(int maxSize) {
		this.maxSize = maxSize;
		if (cache.size() > maxSize) {
			int count = 0;
			Iterator<Long> rowIds = cache.keySet().iterator();
			while (rowIds.hasNext()) {
				rowIds.next();
				if (++count > maxSize) {
					rowIds.remove();
				}
			}
		}
	}

	/**
	 * Clear and resize the cache
	 *
	 * @param maxSize
	 *            max size of the cache
	 */
	public void clearAndResize(int maxSize) {
		clear();
		resize(maxSize);
	}

}
