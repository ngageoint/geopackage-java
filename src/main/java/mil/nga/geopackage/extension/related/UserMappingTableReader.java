package mil.nga.geopackage.extension.related;

import java.util.List;

import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.user.UserTableReader;

/**
 * Reads the metadata from an existing user mapping table
 * 
 * @author osbornb
 * @since 3.0.1
 */
public class UserMappingTableReader
		extends
		UserTableReader<UserMappingColumn, UserMappingTable, UserMappingRow, UserMappingResultSet> {

	/**
	 * Constructor
	 * 
	 * @param tableName
	 *            table name
	 */
	public UserMappingTableReader(String tableName) {
		super(tableName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected UserMappingTable createTable(String tableName,
			List<UserMappingColumn> columnList) {
		return new UserMappingTable(tableName, columnList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected UserMappingColumn createColumn(UserMappingResultSet result,
			int index, String name, String type, Long max, boolean notNull,
			int defaultValueIndex, boolean primaryKey) {

		GeoPackageDataType dataType = GeoPackageDataType.fromName(type);

		Object defaultValue = result.getValue(defaultValueIndex, dataType);

		UserMappingColumn column = new UserMappingColumn(index, name, dataType,
				max, notNull, defaultValue, primaryKey);

		return column;
	}

}
