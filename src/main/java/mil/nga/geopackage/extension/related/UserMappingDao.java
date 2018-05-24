package mil.nga.geopackage.extension.related;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserDao;

/**
 * User Mapping DAO for reading user mapping data tables
 * 
 * @author osbornb
 * @since 3.0.1
 */
public class UserMappingDao
		extends
		UserDao<UserMappingColumn, UserMappingTable, UserMappingRow, UserMappingResultSet> {

	/**
	 * User Mapping connection
	 */
	private final UserMappingConnection userMappingDb;

	/**
	 * Constructor
	 * 
	 * @param database
	 * @param db
	 * @param userMappingDb
	 * @param table
	 */
	public UserMappingDao(String database, GeoPackageConnection db,
			UserMappingConnection userMappingDb, UserMappingTable table) {
		super(database, db, userMappingDb, table);

		this.userMappingDb = userMappingDb;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BoundingBox getBoundingBox() {
		throw new GeoPackageException(
				"Bounding Box not supported for User Mapping");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserMappingRow newRow() {
		return new UserMappingRow(getTable());
	}

	/**
	 * Get the User Mapping connection
	 * 
	 * @return user mapping connection
	 */
	public UserMappingConnection getUserMappingDb() {
		return userMappingDb;
	}

	/**
	 * Delete user mappings by base id
	 * 
	 * @param userMappingRow
	 *            user mapping row
	 * @return rows deleted
	 */
	public int deleteByBaseId(UserMappingRow userMappingRow) {
		return deleteByBaseId(userMappingRow.getBaseId());
	}

	/**
	 * Delete user mappings by base id
	 * 
	 * @param baseId
	 *            base id
	 * @return rows deleted
	 */
	public int deleteByBaseId(long baseId) {

		StringBuilder where = new StringBuilder();
		where.append(buildWhere(UserMappingTable.COLUMN_BASE_ID, baseId));

		String[] whereArgs = buildWhereArgs(new Object[] { baseId });

		int deleted = delete(where.toString(), whereArgs);

		return deleted;
	}

	/**
	 * Delete user mappings by related id
	 * 
	 * @param userMappingRow
	 *            user mapping row
	 * @return rows deleted
	 */
	public int deleteByRelatedId(UserMappingRow userMappingRow) {
		return deleteByRelatedId(userMappingRow.getRelatedId());
	}

	/**
	 * Delete user mappings by related id
	 * 
	 * @param relatedId
	 *            related id
	 * @return rows deleted
	 */
	public int deleteByRelatedId(long relatedId) {

		StringBuilder where = new StringBuilder();
		where.append(buildWhere(UserMappingTable.COLUMN_RELATED_ID, relatedId));

		String[] whereArgs = buildWhereArgs(new Object[] { relatedId });

		int deleted = delete(where.toString(), whereArgs);

		return deleted;
	}

	/**
	 * Delete user mappings by both base id and related id
	 * 
	 * @param userMappingRow
	 *            user mapping row
	 * @return rows deleted
	 */
	public int deleteByIds(UserMappingRow userMappingRow) {
		return deleteByIds(userMappingRow.getBaseId(),
				userMappingRow.getRelatedId());
	}

	/**
	 * Delete user mappings by both base id and related id
	 * 
	 * @param baseId
	 *            base id
	 * @param relatedId
	 *            related id
	 * @return rows deleted
	 */
	public int deleteByIds(long baseId, long relatedId) {

		StringBuilder where = new StringBuilder();
		where.append(buildWhere(UserMappingTable.COLUMN_BASE_ID, baseId));
		where.append(" AND ");
		where.append(buildWhere(UserMappingTable.COLUMN_RELATED_ID, relatedId));

		String[] whereArgs = buildWhereArgs(new Object[] { baseId, relatedId });

		int deleted = delete(where.toString(), whereArgs);

		return deleted;
	}

}
