package mil.nga.geopackage.user.custom;

import mil.nga.geopackage.db.Pagination;
import mil.nga.geopackage.user.UserPaginatedResults;

/**
 * User Custom Paginated Results for iterating and querying through user customs
 * in chunks
 * 
 * @author osbornb
 * @since 6.2.0
 */
public class UserCustomPaginatedResults extends
		UserPaginatedResults<UserCustomColumn, UserCustomTable, UserCustomRow, UserCustomResultSet> {

	/**
	 * Determine if the result set is paginated
	 * 
	 * @param resultSet
	 *            user custom result set
	 * @return true if paginated
	 */
	public static boolean isPaginated(UserCustomResultSet resultSet) {
		return getPagination(resultSet) != null;
	}

	/**
	 * Get the pagination offset and limit
	 * 
	 * @param resultSet
	 *            user custom result set
	 * @return pagination or null if not paginated
	 */
	public static Pagination getPagination(UserCustomResultSet resultSet) {
		return Pagination.find(resultSet.getSql());
	}

	/**
	 * Create paginated results
	 * 
	 * @param dao
	 *            user custom dao
	 * @param results
	 *            user custom result set
	 * @return user custom paginated results
	 */
	public static UserCustomPaginatedResults create(UserCustomDao dao,
			UserCustomResultSet results) {
		return new UserCustomPaginatedResults(dao, results);
	}

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            user custom dao
	 * @param results
	 *            user custom result set
	 */
	public UserCustomPaginatedResults(UserCustomDao dao,
			UserCustomResultSet results) {
		super(dao, results);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserCustomDao getDao() {
		return (UserCustomDao) super.getDao();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserCustomResultSet getResults() {
		return (UserCustomResultSet) super.getResults();
	}

}
