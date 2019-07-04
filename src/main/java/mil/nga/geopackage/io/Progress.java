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
	 * Log count frequency
	 */
	protected final int countFrequency;

	/**
	 * Log time frequency
	 */
	protected final int timeFrequency;

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
	 *            time frequency
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
						+ (max != null ? " of " + max + " ("
								+ getPercentage(this.progress, max) + ")"
								: ""));
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

	/**
	 * Get the string percentage of the count and total
	 * 
	 * @param count
	 * @param total
	 * @return
	 */
	protected String getPercentage(int count, int total) {
		return decimalFormat.format((count / (double) total * 100.0f)) + "%";
	}

}
