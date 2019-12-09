package mil.nga.geopackage.io;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Progress logger
 * 
 * @author osbornb
 * @since 3.3.0
 */
public class Progress implements GeoPackageProgress {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(Progress.class.getName());

	/**
	 * Decimal format
	 */
	protected DecimalFormat decimalFormat = new DecimalFormat("0.00");

	/**
	 * Max number
	 */
	protected Integer max = null;

	/**
	 * Total progress
	 */
	protected int progress = 0;

	/**
	 * Active flag
	 */
	protected boolean active = true;

	/**
	 * Log Title
	 */
	protected final String title;

	/**
	 * Log Unit
	 */
	protected final String unit;

	/**
	 * Log count frequency
	 */
	protected int countFrequency;

	/**
	 * Log time frequency, stored in milliseconds
	 */
	protected int timeFrequency;

	/**
	 * Local count between logs
	 */
	protected int localCount = 0;

	/**
	 * Local time between logs
	 */
	protected Date localTime = new Date();

	/**
	 * Constructor
	 * 
	 * @param title
	 *            title
	 * @param countFrequency
	 *            count frequency
	 * @param timeFrequency
	 *            time frequency in seconds
	 */
	public Progress(String title, int countFrequency, int timeFrequency) {
		this(title, null, countFrequency, timeFrequency);
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
	 * @since 3.5.0
	 */
	public Progress(String title, String unit, int countFrequency,
			int timeFrequency) {
		this.title = title;
		this.unit = unit != null ? " " + unit : "";
		setCountFrequency(countFrequency);
		setTimeFrequency(timeFrequency);
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
				|| localTime.getTime() + timeFrequency <= (new Date()).getTime()
				|| (max != null && max == this.progress)) {
			logProgress();
			localCount = 0;
			localTime = new Date();
		}
	}

	/**
	 * Log the progress
	 */
	protected void logProgress() {
		LOGGER.log(Level.INFO,
				title + " - " + this.progress
						+ (max != null ? " of " + max + unit + " ("
								+ getPercentage(this.progress, max) + ")"
								: unit));
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
	 * Get the count frequency
	 * 
	 * @return count frequency
	 * @since 3.5.0
	 */
	public int getCountFrequency() {
		return countFrequency;
	}

	/**
	 * Set the count frequency
	 * 
	 * @param countFrequency
	 *            count frequency
	 * @since 3.5.0
	 */
	public void setCountFrequency(int countFrequency) {
		this.countFrequency = countFrequency;
	}

	/**
	 * Get the time frequency in seconds
	 * 
	 * @return time frequency in seconds
	 * @since 3.5.0
	 */
	public int getTimeFrequency() {
		return timeFrequency / 1000;
	}

	/**
	 * Set the time frequency in seconds
	 * 
	 * @param timeFrequency
	 *            time frequency in seconds
	 * @since 3.5.0
	 */
	public void setTimeFrequency(int timeFrequency) {
		this.timeFrequency = timeFrequency * 1000;
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
	 * Get the string percentage of the count and total
	 * 
	 * @param count
	 *            current count
	 * @param total
	 *            total count
	 * @return string percentage
	 */
	protected String getPercentage(int count, int total) {
		return decimalFormat.format((count / (double) total * 100.0f)) + "%";
	}

}
