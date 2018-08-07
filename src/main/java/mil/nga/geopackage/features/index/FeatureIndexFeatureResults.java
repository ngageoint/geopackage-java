package mil.nga.geopackage.features.index;

import java.util.Iterator;

import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * Iterable Feature Index Results to iterate on feature results from a feature
 * DAO
 *
 * @author osbornb
 * @since 3.0.3
 */
public class FeatureIndexFeatureResults implements FeatureIndexResults {

	/**
	 * Result Set
	 */
	private final FeatureResultSet resultSet;

	/**
	 * Constructor
	 * 
	 * @param resultSet
	 *            result set
	 */
	public FeatureIndexFeatureResults(FeatureResultSet resultSet) {
		this.resultSet = resultSet;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<FeatureRow> iterator() {
		Iterator<FeatureRow> iterator = new Iterator<FeatureRow>() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean hasNext() {
				return resultSet.moveToNext();
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public FeatureRow next() {
				return resultSet.getRow();
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		return iterator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long count() {
		return resultSet.getCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		resultSet.close();
	}

}
