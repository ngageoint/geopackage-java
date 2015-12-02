package mil.nga.geopackage.io;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tile Progress logger
 * 
 * @author osbornb
 * @since 1.1.2
 */
public class TileProgress implements GeoPackageProgress {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(TileProgress.class
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
	 * Log frequency
	 */
	private final int frequency;

	/**
	 * Local count between logs
	 */
	private int localCount = 0;

	/**
	 * Constructor
	 * 
	 * @param frequency
	 */
	public TileProgress(int frequency) {
		this.frequency = frequency;
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
		if (localCount >= frequency) {
			LOGGER.log(Level.INFO, this.progress + " of " + max);
			localCount = 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() {
		return active && (max == null || progress < max);
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
