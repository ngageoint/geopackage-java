package mil.nga.geopackage.extension.related_tables;

import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.user.UserColumn;

/**
 * User Mapping column
 * 
 * @author yutzlejp
 */
public class UserMappingColumn extends UserColumn {

	/**
	 * Create a base id column
	 * 
	 * @param index
	 *            index
	 * @return tile column
	 */
	public static UserMappingColumn createBaseIdColumn(int baseId) {
		return new UserMappingColumn(baseId, UserMappingTable.COLUMN_BASE_ID,
				GeoPackageDataType.INTEGER, null, false, null, true);
	}

	/**
	 * Create a zoom level column
	 * 
	 * @param index
	 *            index
	 * @return tile column
	 */
	public static UserMappingColumn createRelatedIdColumn(int relatedId) {
		return new UserMappingColumn(relatedId, UserMappingTable.COLUMN_RELATED_ID,
				GeoPackageDataType.INTEGER, null, true, 0, false);
	}

	/**
	 * Create a new column
	 * 
	 * @param index
	 *            index
	 * @param name
	 *            name
	 * @param type
	 *            type
	 * @param notNull
	 *            not null flag
	 * @param defaultValue
	 *            default value
	 * @return tile column
	 */
	public static UserMappingColumn createColumn(int index, String name,
			GeoPackageDataType type, boolean notNull, Object defaultValue) {
		return createColumn(index, name, type, null, notNull, defaultValue);
	}

	/**
	 * Create a new column
	 * 
	 * @param index
	 *            index
	 * @param name
	 *            name
	 * @param type
	 *            type
	 * @param max
	 *            max value
	 * @param notNull
	 *            not null flag
	 * @param defaultValue
	 *            default value
	 * @return tile column
	 */
	public static UserMappingColumn createColumn(int index, String name,
			GeoPackageDataType type, Long max, boolean notNull,
			Object defaultValue) {
		return new UserMappingColumn(index, name, type, max, notNull, defaultValue,
				false);
	}

	/**
	 * Constructor
	 * 
	 * @param index
	 *            index
	 * @param name
	 *            name
	 * @param dataType
	 *            data type
	 * @param max
	 *            max value
	 * @param notNull
	 *            not null flag
	 * @param defaultValue
	 *            default value
	 * @param primaryKey
	 *            primary key
	 */
	UserMappingColumn(int index, String name, GeoPackageDataType dataType, Long max,
			boolean notNull, Object defaultValue, boolean primaryKey) {
		super(index, name, dataType, max, notNull, defaultValue, primaryKey);
		if (dataType == null) {
			throw new GeoPackageException(
					"Data Type is required to create column: " + name);
		}
	}

}
