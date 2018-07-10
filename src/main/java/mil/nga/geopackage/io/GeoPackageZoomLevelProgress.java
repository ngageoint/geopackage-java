package mil.nga.geopackage.io;

/**
 * GeoPackage Progress interface for receiving zoom level specific progress
 * information and handling cancellations
 * 
 * @author osbornb
 */
public interface GeoPackageZoomLevelProgress extends GeoPackageProgress {

	/**
	 * Set the max progress value for the zoom level
	 * 
	 * @param zoomLevel
	 *            zoom level
	 * @param max
	 *            max
	 */
	public void setZoomLevelMax(int zoomLevel, int max);

	/**
	 * Add to the total progress at the zoom level
	 * 
	 * @param zoomLevel
	 *            zoom level
	 * @param progress
	 *            progress
	 */
	public void addZoomLevelProgress(int zoomLevel, int progress);

}