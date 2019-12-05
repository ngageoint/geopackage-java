package mil.nga.geopackage.io;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Progress logger
 * 
 * @author osbornb
 * @since 3.3.0
 */
public class ZoomLevelProgress extends Progress
		implements GeoPackageZoomLevelProgress {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(ZoomLevelProgress.class.getName());

	/**
	 * Zoom level max number of tiles
	 */
	private Map<Integer, Integer> zoomLevelMax = new HashMap<>();

	/**
	 * Zoom level progress
	 */
	private Map<Integer, Integer> zoomLevelProgress = new HashMap<>();

	/**
	 * Current zoom level
	 */
	private int currentZoom = -1;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            title
	 * @param countFrequency
	 *            count frequency
	 * @param timeFrequency
	 *            time frequency
	 */
	public ZoomLevelProgress(String title, int countFrequency,
			int timeFrequency) {
		super(title, countFrequency, timeFrequency);
	}

	/**
	 * Constructor
	 * 
	 * @param title
	 *            title
	 * @param unit
	 *            unit
	 * @param countFrequency
	 *            count frequency
	 * @param timeFrequency
	 *            time frequency in seconds
	 * @since 3.4.1
	 */
	public ZoomLevelProgress(String title, String unit, int countFrequency,
			int timeFrequency) {
		super(title, unit, countFrequency, timeFrequency);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setZoomLevelMax(int zoomLevel, int max) {
		zoomLevelMax.put(zoomLevel, max);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void logProgress() {
		int zoomCount = getZoomLevelProgress(currentZoom);
		int zoomTotal = getZoomLevelMax(currentZoom);
		LOGGER.log(Level.INFO, title + " - " + this.progress + " of " + max
				+ unit + " (" + getPercentage(this.progress, max) + "), Zoom "
				+ currentZoom + " - " + zoomCount + " of " + zoomTotal + unit
				+ " (" + getPercentage(zoomCount, zoomTotal) + ")");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addZoomLevelProgress(int zoomLevel, int progress) {
		Integer zoomProgress = getZoomLevelProgress(zoomLevel);
		zoomProgress += progress;
		zoomLevelProgress.put(zoomLevel, zoomProgress);
		if (currentZoom > -1 && currentZoom != zoomLevel) {
			LOGGER.log(Level.INFO,
					title + " - Finished Zoom Level " + currentZoom
							+ ", Tiles: " + getZoomLevelProgress(currentZoom));
		}
		currentZoom = zoomLevel;
	}

	/**
	 * Get the max at the zoom level
	 * 
	 * @param zoomLevel
	 *            zoom level
	 * @return max
	 */
	public Integer getZoomLevelMax(int zoomLevel) {
		Integer zoomMax = zoomLevelMax.get(zoomLevel);
		if (zoomMax == null) {
			zoomMax = 0;
		}
		return zoomMax;
	}

	/**
	 * Get the total progress at the zoom level
	 * 
	 * @param zoomLevel
	 *            zoom level
	 * @return progress
	 */
	public int getZoomLevelProgress(int zoomLevel) {
		Integer zoomProgress = zoomLevelProgress.get(zoomLevel);
		if (zoomProgress == null) {
			zoomProgress = 0;
		}
		return zoomProgress;
	}

}
