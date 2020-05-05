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
		this(table, null, resultSet, count);
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            attributes table
	 * @param columns
	 *            columns
	 * @param resultSet
	 *            result set
	 * @param count
	 *            result count
	 * @since 3.5.0
	 */
	public AttributesResultSet(AttributesTable table, String[] columns,
			ResultSet resultSet, int count) {
		super(table, columns, resultSet, count);
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            attributes table
	 * @param resultSet
	 *            result set
	 * @param sql
	 *            SQL statement
	 * @param selectionArgs
	 *            selection arguments
	 * @since 4.0.0
	 */
	public AttributesResultSet(AttributesTable table, ResultSet resultSet,
			String sql, String[] selectionArgs) {
		this(table, null, resultSet, sql, selectionArgs);
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            attributes table
	 * @param columns
	 *            columns
	 * @param resultSet
	 *            result set
	 * @param sql
	 *            SQL statement
	 * @param selectionArgs
	 *            selection arguments
	 * @since 4.0.0
	 */
	public AttributesResultSet(AttributesTable table, String[] columns,
			ResultSet resultSet, String sql, String[] selectionArgs) {
		super(table, columns, resultSet, sql, selectionArgs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttributesRow getRow(int[] columnTypes, Object[] values) {
		return new AttributesRow(getTable(), getColumns(), columnTypes, values);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttributesColumns getColumns() {
		return (AttributesColumns) super.getColumns();
	}

}
