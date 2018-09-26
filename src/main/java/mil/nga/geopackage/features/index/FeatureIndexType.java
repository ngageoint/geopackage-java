package mil.nga.geopackage.features.index;

/**
 * Feature Index type enumeration of index location
 *
 * @author osbornb
 * @since 3.1.0
 */
public enum FeatureIndexType {

	/**
	 * GeoPackage extension tables
	 */
	GEOPACKAGE,

	/**
	 * RTree Index extension
	 */
	RTREE,

	/**
	 * No index
	 */
	NONE;

}
