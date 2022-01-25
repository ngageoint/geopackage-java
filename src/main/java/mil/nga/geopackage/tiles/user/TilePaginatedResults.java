package mil.nga.geopackage.tiles.user;

import mil.nga.geopackage.db.Pagination;
import mil.nga.geopackage.user.UserPaginatedResults;

/**
 * Tile Paginated Results for iterating and querying through tiles in chunks
 * 
 * @author osbornb
 * @since 6.1.3
 */
public class TilePaginatedResults extends
		UserPaginatedResults<TileColumn, TileTable, TileRow, TileResultSet> {

	/**
	 * Determine if the result set is paginated
	 * 
	 * @param resultSet
	 *            tile result set
	 * @return true if paginated
	 */
	public static boolean isPaginated(TileResultSet resultSet) {
		return getPagination(resultSet) != null;
	}

	/**
	 * Get the pagination offset and limit
	 * 
	 * @param resultSet
	 *            tile result set
	 * @return pagination or null if not paginated
	 */
	public static Pagination getPagination(TileResultSet resultSet) {
		return Pagination.find(resultSet.getSql());
	}

	/**
	 * Create paginated results
	 * 
	 * @param dao
	 *            tile dao
	 * @param results
	 *            tile result set
	 * @return tile paginated results
	 */
	public static TilePaginatedResults create(TileDao dao,
			TileResultSet results) {
		return new TilePaginatedResults(dao, results);
	}

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            tile dao
	 * @param results
	 *            tile result set
	 */
	public TilePaginatedResults(TileDao dao, TileResultSet results) {
		super(dao, results);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileDao getDao() {
		return (TileDao) super.getDao();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TileResultSet getResults() {
		return (TileResultSet) super.getResults();
	}

}
