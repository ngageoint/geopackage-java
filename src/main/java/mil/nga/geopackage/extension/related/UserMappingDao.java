package mil.nga.geopackage.extension.related;

import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomResultSet;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * User Mapping DAO for reading user mapping data tables
 * 
 * @author osbornb
 * @since 3.0.1
 */
public class UserMappingDao extends UserCustomDao {

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            user custom data access object
	 */
	public UserMappingDao(UserCustomDao dao) {
		super(dao);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserMappingRow newRow() {
		return new UserMappingRow(getTable());
	}

	/**
	 * Get the user mapping row from the current result set location
	 * 
	 * @param resultSet
	 *            result set
	 * @return user mapping row
	 */
	public UserMappingRow getRow(UserCustomResultSet resultSet) {
		return getRow(resultSet.getRow());
	}

	/**
	 * Get a user mapping row from the user custom row
	 * 
	 * @param row
	 *            custom row
	 * @return user mapping row
	 */
	public UserMappingRow getRow(UserCustomRow row) {
		return new UserMappingRow(row);
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
