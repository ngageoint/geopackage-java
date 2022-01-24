package mil.nga.geopackage.features.index;

import java.util.Iterator;

import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * Iterable Feature Index Results to iterate on feature results from a feature
 * DAO
 *
 * @author osbornb
 * @since 3.1.0
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
		return resultSet.iterator();
	}

	/**
	 * Get the result set
	 * 
	 * @return feature result set
	 * @since 6.1.3
	 */
	public FeatureResultSet getResultSet() {
		return resultSet;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<Long> ids() {
		return new Iterable<Long>() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public Iterator<Long> iterator() {
				return new Iterator<Long>() {

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
					public Long next() {
						return resultSet.getId();
					}

				};
			}
		};
	}

}
