package mil.nga.geopackage.attributes;

import java.sql.ResultSet;

import mil.nga.geopackage.user.UserResultSet;

/**
 * Attributes Result Set to wrap a database ResultSet for attributes queries
 * 
 * @author osbornb
 * @since 1.2.1
 */
public class AttributesResultSet extends
		UserResultSet<AttributesColumn, AttributesTable, AttributesRow> {

	/**
	 * Constructor
	 * 
	 * @param table
	 *            attributes table
	 * @param resultSet
	 *            result set
	 * @param count
	 *            result count
	 */
	public AttributesResultSet(AttributesTable table, ResultSet resultSet,
			int count) {
		super(table, resultSet, count);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttributesRow getRow(int[] columnTypes, Object[] values) {
		AttributesRow row = new AttributesRow(getTable(), columnTypes, values);
		return row;
	}

}
