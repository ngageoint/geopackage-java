package mil.nga.geopackage;

import java.io.File;

import mil.nga.geopackage.manager.GeoPackageManager;

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
		GeoPackage geoPackage = get(name);
		if (geoPackage == null) {
			geoPackage = GeoPackageManager.open(name, file);
			add(geoPackage);
		}
		return geoPackage;
	}

}
