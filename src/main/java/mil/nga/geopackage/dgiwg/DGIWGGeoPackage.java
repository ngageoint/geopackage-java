package mil.nga.geopackage.dgiwg;

import java.io.File;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageImpl;
import mil.nga.geopackage.db.GeoPackageConnection;

/**
 * DGIWG (Defence Geospatial Information Working Group) GeoPackage
 * implementation
 * 
 * @author osbornb
 */
public class DGIWGGeoPackage extends GeoPackageImpl {

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public DGIWGGeoPackage(GeoPackage geoPackage) {
		super(geoPackage.getName(), geoPackage.getPath(),
				geoPackage.getConnection());
	}

	/**
	 * Constructor
	 *
	 * @param name
	 *            GeoPackage name
	 * @param file
	 *            GeoPackage file
	 * @param database
	 *            connection
	 */
	protected DGIWGGeoPackage(String name, File file,
			GeoPackageConnection database) {
		super(name, file, database);
	}

}
