package mil.nga.geopackage.extension.style;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.extension.related.UserMappingDao;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomResultSet;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * Style Mapping DAO for reading style mapping data tables
 * 
 * @author osbornb
 * @since 3.1.1
 */
public class StyleMappingDao extends UserMappingDao {

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            user custom data access object
	 */
	public StyleMappingDao(UserCustomDao dao) {
		super(dao, new StyleMappingTable(dao.getTable()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StyleMappingTable getTable() {
		return (StyleMappingTable) super.getTable();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StyleMappingRow newRow() {
		return new StyleMappingRow(getTable());
	}

	/**
	 * Get the style mapping row from the current result set location
	 * 
	 * @param resultSet
	 *            result set
	 * @return style mapping row
	 */
	public StyleMappingRow getRow(UserCustomResultSet resultSet) {
		return getRow(resultSet.getRow());
	}

	/**
	 * Get a style mapping row from the user custom row
	 * 
	 * @param row
	 *            custom row
	 * @return style mapping row
	 */
	public StyleMappingRow getRow(UserCustomRow row) {
		return new StyleMappingRow(row);
	}

	/**
	 * Query for style mappings by base id
	 * 
	 * @param id
	 *            base id, feature contents id or feature geometry id
	 * @return style mappings rows
	 */
	public List<StyleMappingRow> queryByBaseFeatureId(long id) {
		List<StyleMappingRow> rows = new ArrayList<>();
		UserCustomResultSet resultSet = queryByBaseId(id);
		try {
			while (resultSet.moveToNext()) {
				rows.add(getRow(resultSet));
			}
		} finally {
			resultSet.close();
		}
		return rows;
	}

}
