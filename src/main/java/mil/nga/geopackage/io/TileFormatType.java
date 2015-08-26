package mil.nga.geopackage.io;

/**
 * Tile Format Type specifying the z/x/y folder structure of tiles
 * 
 * @author osbornb
 */
public enum TileFormatType {

	/**
	 * x and y coordinates created using tile matrix width and height
	 */
	GEOPACKAGE,

	/**
	 * origin is upper left
	 */
	STANDARD,

	/**
	 * Tile Map Service specification, origin is lower left
	 */
	TMS;

}
