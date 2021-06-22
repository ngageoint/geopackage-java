package mil.nga.geopackage.user.custom;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.user.UserDao;
import mil.nga.proj.Projection;

/**
 * User Custom DAO for reading user custom data tables
 * 
 * @author osbornb
 * @since 3.0.1
 */
public class UserCustomDao extends
		UserDao<UserCustomColumn, UserCustomTable, UserCustomRow, UserCustomResultSet> {

	/**
	 * User Custom connection
	 */
	protected final UserCustomConnection userDb;

	/**
	 * Constructor
	 * 
	 * @param database
	 *            database name
	 * @param db
	 *            database connection
	 * @param table
	 *            user custom table
	 */
	public UserCustomDao(String database, GeoPackageConnection db,
			UserCustomTable table) {
		super(database, db, new UserCustomConnection(db), table);

		this.userDb = (UserCustomConnection) getUserDb();
	}

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            user custom data access object
	 */
	public UserCustomDao(UserCustomDao dao) {
		this(dao, dao.getTable());
	}

	/**
	 * Constructor
	 * 
	 * @param dao
	 *            user custom data access object
	 * @param userCustomTable
	 *            user custom table
	 */
	public UserCustomDao(UserCustomDao dao, UserCustomTable userCustomTable) {
		this(dao.getDatabase(), dao.getDb(), userCustomTable);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BoundingBox getBoundingBox() {
		throw new GeoPackageException(
				"Bounding Box not supported for User Custom");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BoundingBox getBoundingBox(Projection projection) {
		throw new GeoPackageException(
				"Bounding Box not supported for User Custom");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserCustomRow newRow() {
		return new UserCustomRow(getTable());
	}

	/**
	 * Get the User Custom connection
	 * 
	 * @return user custom connection
	 */
	public UserCustomConnection getUserDb() {
		return userDb;
	}

	/**
	 * Get the count of the result set and close it
	 * 
	 * @param resultSet
	 *            result set
	 * @return count
	 */
	protected int count(UserCustomResultSet resultSet) {
		int count = 0;
		try {
			count = resultSet.getCount();
		} finally {
			resultSet.close();
		}
		return count;
	}

	/**
	 * Read the database table and create a DAO
	 * 
	 * @param database
	 *            database name
	 * @param connection
	 *            db connection
	 * @param tableName
	 *            table name
	 * @return user custom DAO
	 */
	public static UserCustomDao readTable(String database,
			GeoPackageConnection connection, String tableName) {

		UserCustomTable userCustomTable = UserCustomTableReader
				.readTable(connection, tableName);
		UserCustomDao dao = new UserCustomDao(database, connection,
				userCustomTable);

		return dao;
	}

}
