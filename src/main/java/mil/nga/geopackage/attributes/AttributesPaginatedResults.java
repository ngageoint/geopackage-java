package mil.nga.geopackage.attributes;

import mil.nga.geopackage.db.Pagination;
import mil.nga.geopackage.user.UserPaginatedResults;

/**
 * Attributes Paginated Results for iterating and querying through attributes in
 * chunks
 * 
 * @author osbornb
 * @since 6.1.3
 */
public class AttributesPaginatedResults extends
		UserPaginatedResults<AttributesColumn, AttributesTable, AttributesRow, AttributesResultSet> {

	/**
	 * Determine if the result set is paginated
	 * 
	 * @param resultSet
	 *            attributes result set
	 * @return true if paginated
	 */
	public static boolean isPaginated(AttributesResultSet resultSet) {
		return getPagination(resultSet) != null;
	}

	/**
	 * Get the pagination offset and limit
	 * 
	 * @param resultSet
	 *            attributes result set
	 * @return pagination or null if not paginated
	 */
	public static Pagination getPagination(AttributesResultSet resultSet) {
		return Pagination.find(resultSet.getSql());
	}

	/**
	 * Create paginated results
	 * 
	 * @param dao
	 *            attributes dao
	 * @param results
	 *            attributes result set
	 * @return attributes paginated results
	 */
	public static AttributesPaginatedResults create(AttributesDao dao,
			AttributesResultSet results) {
		return new AttributesPaginatedResults(dao, results);
	}

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            attributes dao
	 * @param results
	 *            attributes result set
	 */
	public AttributesPaginatedResults(AttributesDao dao,
			AttributesResultSet results) {
		super(dao, results);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttributesDao getDao() {
		return (AttributesDao) super.getDao();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttributesResultSet getResults() {
		return (AttributesResultSet) super.getResults();
	}

}
