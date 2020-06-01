package mil.nga.geopackage;

import java.io.File;

/**
 * GeoPackage Cache
 *
 * @author osbornb
 * @since 3.0.2
 */
public class GeoPackageCache extends GeoPackageCoreCache<GeoPackage> {

	/**
	 * Constructor
	 */
	public GeoPackageCache() {

	}

	/**
	 * Get the cached GeoPackage or open and cache the GeoPackage file
	 *
	 * @param file
	 *            GeoPackage file
	 * @return GeoPackage
	 */
	public GeoPackage getOrOpen(File file) {
		return getOrOpen(file.getName(), file);
	}

	/**
	 * Get the cached GeoPackage or open and cache the GeoPackage file
	 *
	 * @param name
	 *            GeoPackage name
	 * @param file
	 *            GeoPackage file
	 * @return GeoPackage
	 */
	public GeoPackage getOrOpen(String name, File file) {
		return getOrOpen(name, file, true);
	}

	/**
	 * Get the cached GeoPackage or open the GeoPackage file without caching it
	 *
	 * @param file
	 *            GeoPackage file
	 * @return GeoPackage
	 * @since 3.1.0
	 */
	public GeoPackage getOrNoCacheOpen(File file) {
		return getOrNoCacheOpen(file.getName(), file);
	}

	/**
	 * Get the cached GeoPackage or open the GeoPackage file without caching it
	 *
	 * @param name
	 *            GeoPackage name
	 * @param file
	 *            GeoPackage file
	 * @return GeoPackage
	 * @since 3.1.0
	 */
	public GeoPackage getOrNoCacheOpen(String name, File file) {
		return getOrOpen(name, file, false);
	}

	/**
	 * Get the cached GeoPackage or open the GeoPackage file without caching it
	 *
	 * @param name
	 *            GeoPackage name
	 * @param file
	 *            GeoPackage file
	 * @param cache
	 *            true to cache opened GeoPackages
	 * @return GeoPackage
	 */
	private GeoPackage getOrOpen(String name, File file, boolean cache) {
		GeoPackage geoPackage = get(name);
		if (geoPackage == null) {
			geoPackage = GeoPackageManager.open(name, file);
			if (cache) {
				add(geoPackage);
			}
		}
		return geoPackage;
	}

}
