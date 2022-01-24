package mil.nga.geopackage.features.user;

import mil.nga.geopackage.db.Pagination;
import mil.nga.geopackage.user.UserPaginatedResults;

/**
 * Feature Paginated Results for iterating and querying through features in
 * chunks
 * 
 * @author osbornb
 * @since 6.1.3
 */
public class FeaturePaginatedResults extends
		UserPaginatedResults<FeatureColumn, FeatureTable, FeatureRow, FeatureResultSet> {

	/**
	 * Determine if the result set is paginated
	 * 
	 * @param resultSet
	 *            feature result set
	 * @return true if paginated
	 */
	public static boolean isPaginated(FeatureResultSet resultSet) {
		return getPagination(resultSet) != null;
	}

	/**
	 * Get the pagination offset and limit
	 * 
	 * @param resultSet
	 *            feature result set
	 * @return pagination or null if not paginated
	 */
	public static Pagination getPagination(FeatureResultSet resultSet) {
		return Pagination.find(resultSet.getSql());
	}

	/**
	 * Create paginated results
	 * 
	 * @param dao
	 *            feature dao
	 * @param results
	 *            feature result set
	 * @return feature paginated results
	 */
	public static FeaturePaginatedResults create(FeatureDao dao,
			FeatureResultSet results) {
		return new FeaturePaginatedResults(dao, results);
	}

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            feature dao
	 * @param results
	 *            feature result set
	 */
	public FeaturePaginatedResults(FeatureDao dao, FeatureResultSet results) {
		super(dao, results);
	}

}
