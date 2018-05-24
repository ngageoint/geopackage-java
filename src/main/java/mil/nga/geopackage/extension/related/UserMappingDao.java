package mil.nga.geopackage.extension.related;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserDao;

/**
 * User Mapping DAO for reading user mapping data tables
 * 
 * @author osbornb
 * @since 3.0.1
 */
public class UserMappingDao
		extends
		UserDao<UserMappingColumn, UserMappingTable, UserMappingRow, UserMappingResultSet> {

	/**
	 * User Mapping connection
	 */
	private final UserMappingConnection userMappingDb;

	/**
	 * Constructor
	 * 
	 * @param database
	 * @param db
	 * @param userMappingDb
	 * @param table
	 */
	public UserMappingDao(String database, GeoPackageConnection db,
			UserMappingConnection userMappingDb, UserMappingTable table) {
		super(database, db, userMappingDb, table);

		this.userMappingDb = userMappingDb;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BoundingBox getBoundingBox() {
		throw new GeoPackageException(
				"Bounding Box not supported for User Mapping");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserMappingRow newRow() {
		return new UserMappingRow(getTable());
	}

	/**
	 * Get the User Mapping connection
	 * 
	 * @return user mapping connection
	 */
	public UserMappingConnection getUserMappingDb() {
		return userMappingDb;
	}

}
