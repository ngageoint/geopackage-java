package mil.nga.giat.geopackage.manager;

import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.db.GeoPackageConnection;
import mil.nga.giat.geopackage.db.GeoPackageTableCreator;
import mil.nga.giat.geopackage.factory.GeoPackageCoreImpl;

/**
 * GeoPackage implementation
 * 
 * @author osbornb
 */
class GeoPackageImpl extends GeoPackageCoreImpl implements GeoPackage {

	/**
	 * Constructor
	 *
	 * @param name
	 * @param database
	 * @param tableCreator
	 */
	GeoPackageImpl(String name, GeoPackageConnection database,
			GeoPackageTableCreator tableCreator) {
		super(name, database, tableCreator);
	}

}
