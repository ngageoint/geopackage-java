package mil.nga.geopackage.io;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Progress logger
 * 
 * @author osbornb
 * @since 1.1.2
 */
public class Progress implements GeoPackageZoomLevelProgress {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(Progress.class
			.getName());

	/**
	 * Decimal format
	 */
	private DecimalFormat decimalFormat = new DecimalFormat("0.00");

	/**
	 * Max number of tiles
	 */
	private Integer max = null;

	/**
	 * Zoom level max number of tiles
	 */
	private Map<Integer, Integer> zoomLevelMax = new HashMap<>();

	/**
	 * Total progress
	 */
	private int progress = 0;

	/**
	 * Zoom level progress
	 */
	private Map<Integer, Integer> zoomLevelProgress = new HashMap<>();

	/**
	 * Current zoom level
	 */
	private int currentZoom = -1;

	/**
	 * Active flag
	 */
	private boolean active = true;

	/**
	 * Log Title
	 */
	private final String title;

	/**
	 * Log count frequency
	 */
	private final int countFrequency;

	/**
	 * Log time frequency
	 */
	private final int timeFrequency;

	/**
	 * Local count between logs
	 */
	private int localCount = 0;

	/**
	 * Local time between logs
	 */
	private Date localTime = new Date();

	/**
	 * Constructor
	 * 
	 * @param title
	 * @param countFrequency
	 * @param timeFrequency
	 */
	public Progress(String title, int countFrequency, int timeFrequency) {
		this.title = title;
		this.countFrequency = countFrequency;
		this.timeFrequency = timeFrequency * 1000;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMax(int max) {
		this.max = max;
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
	public void addProgress(int progress) {
		this.progress += progress;
		localCount += progress;
		if (localCount >= countFrequency
				|| localTime.getTime() + timeFrequency <= (new Date())
						.getTime()) {
			int zoomCount = getZoomLevelProgress(currentZoom);
			int zoomTotal = getZoomLevelMax(currentZoom);
			LOGGER.log(Level.INFO, title + " - " + this.progress + " of " + max
					+ " (" + getPercentage(this.progress, max) + "), Zoom "
					+ currentZoom + " - " + zoomCount + " of " + zoomTotal
					+ " (" + getPercentage(zoomCount, zoomTotal) + ")");
			localCount = 0;
			localTime = new Date();
		}
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
			LOGGER.log(Level.INFO, title + " - Finished Zoom Level "
					+ currentZoom + ", Tiles: "
					+ getZoomLevelProgress(currentZoom));
		}
		currentZoom = zoomLevel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cleanupOnCancel() {
		return false;
	}

	/**
	 * Cancel the operation
	 */
	public void cancel() {
		active = false;
	}

	/**
	 * Get the max
	 * 
	 * @return max
	 */
	public Integer getMax() {
		return max;
	}

	/**
	 * Get the max at the zoom level
	 * 
	 * @param zoomLevel
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
	 * Get the total progress
	 * 
	 * @return progress
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * Get the total progress at the zoom level
	 * 
	 * @param zoomLevel
	 * @return progress
	 */
	public int getZoomLevelProgress(int zoomLevel) {
		Integer zoomProgress = zoomLevelProgress.get(zoomLevel);
		if (zoomProgress == null) {
			zoomProgress = 0;
		}
		return zoomProgress;
	}

	/**
	 * Get the string percentage of the count and total
	 * 
	 * @param count
	 * @param total
	 * @return
	 */
	private String getPercentage(int count, int total) {
		return decimalFormat.format((count / (double) total * 100.0f)) + "%";
	}

}
