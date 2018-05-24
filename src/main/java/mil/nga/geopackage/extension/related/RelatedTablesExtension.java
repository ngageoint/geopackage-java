package mil.nga.geopackage.extension.related;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserCoreTableReader;

/**
 * Related Tables extension
 * 
 * @author jyutzler
 * @since 3.0.1
 */
public class RelatedTablesExtension extends RelatedTablesCoreExtension {

	/**
	 * GeoPackage connection
	 */
	private GeoPackageConnection connection;

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * 
	 */
	public RelatedTablesExtension(GeoPackage geoPackage) {
		super(geoPackage);
		connection = geoPackage.getConnection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPrimaryKeyColumnName(String tableName) {
		String result = null;
		String sql = "PRAGMA table_info(" + CoreSQLUtils.quoteWrap(tableName)
				+ ")";
		ResultSet resultSet = connection.query(sql, null);
		try {
			while (resultSet.next()) {
				if (resultSet.getInt(UserCoreTableReader.PK) == 1) {
					result = resultSet.getString(UserCoreTableReader.NAME);
					break;
				}
			}
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to query for the "
					+ " primary key for table " + tableName, e);
		} finally {
			try {
				resultSet.close();
			} catch (SQLException e) {
				throw new GeoPackageException("Failed to close ResultSet", e);
			}
		}
		if (result == null) {
			throw new GeoPackageException("Found no primary key for table "
					+ tableName);
		}
		return result;
	}

	/**
	 * Get an User Mapping DAO from a table name
	 * 
	 * @param tableName
	 *            table name
	 * @return user mapping dao
	 */
	public UserMappingDao getUserMappingDao(String tableName) {

		if (tableName == null) {
			throw new GeoPackageException(
					"Non null table name is required to create "
							+ UserMappingDao.class.getSimpleName());
		}

		// Read the existing table and create the dao
		UserMappingTableReader tableReader = new UserMappingTableReader(
				tableName);
		UserMappingConnection userDb = new UserMappingConnection(connection);
		final UserMappingTable userMappingTable = tableReader.readTable(userDb);
		userDb.setTable(userMappingTable);
		UserMappingDao dao = new UserMappingDao(getGeoPackage().getName(),
				connection, userDb, userMappingTable);

		return dao;
	}

	/**
	 * 
	 * @param extendedRelation
	 * @param baseId
	 * @return an array of IDs representing the matching related IDs
	 */
	public long[] getMappingsForBase(ExtendedRelation extendedRelation,
			long baseId) {
		Collection<Long> relatedIds = new HashSet<Long>();

		String sql = "select "
				+ CoreSQLUtils.quoteWrap(UserMappingTable.COLUMN_RELATED_ID)
				+ " from "
				+ CoreSQLUtils
						.quoteWrap(extendedRelation.getMappingTableName())
				+ " where "
				+ CoreSQLUtils.quoteWrap(UserMappingTable.COLUMN_BASE_ID)
				+ " = ?";

		ResultSet resultSet = connection.query(sql,
				new String[] { Long.toString(baseId) });
		try {
			while (resultSet.next()) {
				relatedIds.add(Long.valueOf(resultSet.getLong(extendedRelation
						.getMappingTable().getRelatedIdIndex())));
			}
			resultSet.close();
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get mappings for relationship '"
							+ extendedRelation.getMappingTableName()
							+ "' between "
							+ extendedRelation.getBaseTableName() + " and "
							+ extendedRelation.getRelatedTableName(), e);
		}

		long[] result = new long[relatedIds.size()];
		Iterator<Long> iter = relatedIds.iterator();
		int inx = 0;
		while (iter.hasNext()) {
			result[inx++] = iter.next().longValue();
		}
		return result;
	}

	public long[] getMappingsForRelated(ExtendedRelation extendedRelation,
			long relatedId) {
		Collection<Long> baseIds = new HashSet<Long>();

		String sql = "select "
				+ CoreSQLUtils.quoteWrap(UserMappingTable.COLUMN_BASE_ID)
				+ " from "
				+ CoreSQLUtils
						.quoteWrap(extendedRelation.getMappingTableName())
				+ " where "
				+ CoreSQLUtils.quoteWrap(UserMappingTable.COLUMN_RELATED_ID)
				+ " = ?";

		ResultSet resultSet = connection.query(sql,
				new String[] { Long.toString(relatedId) });
		try {
			while (resultSet.next()) {
				baseIds.add(Long.valueOf(resultSet.getLong(extendedRelation
						.getMappingTable().getRelatedIdIndex())));
			}
			resultSet.close();
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to get reverse mappings for relationship '"
							+ extendedRelation.getMappingTableName()
							+ "' between "
							+ extendedRelation.getBaseTableName() + " and "
							+ extendedRelation.getRelatedTableName(), e);
		}

		long[] result = new long[baseIds.size()];
		Iterator<Long> iter = baseIds.iterator();
		int inx = 0;
		while (iter.hasNext()) {
			result[inx++] = iter.next().longValue();
		}
		return result;
	}

}
