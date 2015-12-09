package mil.nga.geopackage.io;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Progress logger
 * 
 * @author osbornb
 * @since 1.1.2
 */
public class Progress implements GeoPackageProgress {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(Progress.class
			.getName());

	/**
	 * Max number of tiles
	 */
	private Integer max = null;

	/**
	 * Total progress
	 */
	private int progress = 0;

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
	public void addProgress(int progress) {
		this.progress += progress;
		localCount += progress;
		if (localCount >= countFrequency
				|| localTime.getTime() + timeFrequency <= (new Date())
						.getTime()) {
			LOGGER.log(Level.INFO, title + " - " + this.progress + " of " + max);
			localCount = 0;
			localTime = new Date();
		}
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
	 * Get the total progress
	 * 
	 * @return progress
	 */
	public int getProgress() {
		return progress;
	}

}
