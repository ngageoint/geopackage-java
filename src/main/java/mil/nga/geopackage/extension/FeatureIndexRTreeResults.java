package mil.nga.geopackage.extension;

import java.util.Iterator;

import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.user.custom.UserCustomResultSet;

/**
 * Iterable Feature Index Results to iterate on feature rows retrieved from
 * RTree results
 *
 * @author osbornb
 * @since 3.0.3
 */
public class FeatureIndexRTreeResults implements FeatureIndexResults {

	/**
	 * RTree Index Table DAO
	 */
	private final RTreeIndexTableDao dao;

	/**
	 * Result Set
	 */
	private final UserCustomResultSet resultSet;

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            RTree Index Table DAO
	 * @param resultSet
	 *            result set
	 */
	public FeatureIndexRTreeResults(RTreeIndexTableDao dao,
			UserCustomResultSet resultSet) {
		this.dao = dao;
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
				return dao.getFeatureRow(resultSet);
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
