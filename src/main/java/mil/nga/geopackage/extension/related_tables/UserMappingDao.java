package mil.nga.geopackage.extension.related_tables;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserDao;

/**
 * DAO for reading user mapping tables
 * 
 * @author yutzlejp
 */
public class UserMappingDao extends
	UserDao<UserMappingColumn, UserMappingTable, UserMappingRow, UserMappingResultSet> {

	/**
	 * Constructor
	 * 
	 * @param database
	 * @param db
	 * @param tileDb
	 * @param tileMatrixSet
	 * @param tileMatrices
	 * @param table
	 */
	public UserMappingDao(String database, GeoPackageConnection db,
			UserMappingConnection umc, UserMappingTable table) {
		super(database, db, umc, table);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BoundingBox getBoundingBox() {
		return null;
	}

	@Override
	public UserMappingRow newRow() {
		return new UserMappingRow(getTable());
	}

}
