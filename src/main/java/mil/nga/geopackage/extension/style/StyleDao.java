package mil.nga.geopackage.extension.style;

import mil.nga.geopackage.extension.related.simple.SimpleAttributesDao;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomResultSet;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * Style DAO for reading style tables
 * 
 * @author osbornb
 * @since 3.1.1
 */
public class StyleDao extends SimpleAttributesDao {

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            user custom data access object
	 */
	public StyleDao(UserCustomDao dao) {
		super(dao, new StyleTable(dao.getTable()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StyleTable getTable() {
		return (StyleTable) super.getTable();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StyleRow newRow() {
		return new StyleRow(getTable());
	}

	/**
	 * Get the style row from the current result set location
	 * 
	 * @param resultSet
	 *            result set
	 * @return style row
	 */
	public StyleRow getRow(UserCustomResultSet resultSet) {
		return getRow(resultSet.getRow());
	}

	/**
	 * Get a style row from the user custom row
	 * 
	 * @param row
	 *            custom row
	 * @return style row
	 */
	public StyleRow getRow(UserCustomRow row) {
		return new StyleRow(row);
	}

}
