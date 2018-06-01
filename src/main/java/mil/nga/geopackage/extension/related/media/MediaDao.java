package mil.nga.geopackage.extension.related.media;

import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomResultSet;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * User Media DAO for reading user media data tables
 * 
 * @author osbornb
 * @since 3.0.1
 */
public class MediaDao extends UserCustomDao {

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            user custom data access object
	 */
	public MediaDao(UserCustomDao dao) {
		super(dao, new MediaTable(dao.getTable()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MediaTable getTable() {
		return (MediaTable) super.getTable();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MediaRow newRow() {
		return new MediaRow(getTable());
	}

	/**
	 * Get the media row from the current result set location
	 * 
	 * @param resultSet
	 *            result set
	 * @return media row
	 */
	public MediaRow getRow(UserCustomResultSet resultSet) {
		return getRow(resultSet.getRow());
	}

	/**
	 * Get a media row from the user custom row
	 * 
	 * @param row
	 *            custom row
	 * @return media row
	 */
	public MediaRow getRow(UserCustomRow row) {
		return new MediaRow(row);
	}

}
