package mil.nga.geopackage;

import java.awt.image.BufferedImage;
import java.sql.ResultSet;

import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomTable;

/**
 * GeoPackage database connection
 * 
 * @author osbornb
 */
public interface GeoPackage extends GeoPackageCore {

	/**
	 * Get a Feature DAO from Geometry Columns
	 *
	 * @param geometryColumns
	 *            geometry columns
	 * @return feature dao
	 */
	public FeatureDao getFeatureDao(GeometryColumns geometryColumns);

	/**
	 * Get a Feature DAO from Contents
	 *
	 * @param contents
	 *            contents
	 * @return feature dao
	 */
	public FeatureDao getFeatureDao(Contents contents);

	/**
	 * Get a Feature DAO from a table name
	 *
	 * @param tableName
	 *            table name
	 * @return feature dao
	 */
	public FeatureDao getFeatureDao(String tableName);

	/**
	 * Get a Tile DAO from Tile Matrix Set
	 *
	 * @param tileMatrixSet
	 *            tile matrix set
	 * @return tile dao
	 */
	public TileDao getTileDao(TileMatrixSet tileMatrixSet);

	/**
	 * Get a Tile DAO from Contents
	 *
	 * @param contents
	 *            contents
	 * @return tile dao
	 */
	public TileDao getTileDao(Contents contents);

	/**
	 * Get a Tile DAO from a table name
	 *
	 * @param tableName
	 *            table name
	 * @return tile dao
	 */
	public TileDao getTileDao(String tableName);

	/**
	 * Get an Attributes DAO from Contents
	 * 
	 * @param contents
	 *            contents
	 * @return attributes dao
	 * @since 1.2.1
	 */
	public AttributesDao getAttributesDao(Contents contents);

	/**
	 * Get an Attributes DAO from a table name
	 * 
	 * @param tableName
	 *            table name
	 * @return attributes dao
	 * @since 1.2.1
	 */
	public AttributesDao getAttributesDao(String tableName);

	/**
	 * Get a User Custom DAO from a table name
	 * 
	 * @param tableName
	 *            table name
	 * @return user custom dao
	 * @since 3.3.0
	 */
	public UserCustomDao getUserCustomDao(String tableName);

	/**
	 * Get a User Custom DAO from a table
	 *
	 * @param table
	 *            table
	 * @return user custom dao
	 * @since 3.4.0
	 */
	public UserCustomDao getUserCustomDao(UserCustomTable table);

	/**
	 * Perform a query on the database
	 *
	 * @param sql
	 *            sql statement
	 * @param args
	 *            arguments
	 * @return result set
	 * @since 1.1.2
	 */
	public ResultSet query(String sql, String[] args);

	/**
	 * Get the GeoPackage connection
	 * 
	 * @return GeoPackage connection
	 * @since 2.0.1
	 */
	public GeoPackageConnection getConnection();

	/**
	 * Perform a foreign key check on the database
	 *
	 * @return null if check passed, open result set with results if failed
	 * @since 1.1.2
	 */
	public ResultSet foreignKeyCheck();

	/**
	 * Perform a foreign key check on the database table
	 *
	 * @param tableName
	 *            table name
	 * @return null if check passed, open result set with results if failed
	 * @since 3.3.0
	 */
	public ResultSet foreignKeyCheck(String tableName);

	/**
	 * Perform an integrity check on the database
	 *
	 * @return null if check passed, open result set with results if failed
	 * @since 1.1.2
	 */
	public ResultSet integrityCheck();

	/**
	 * Perform a quick integrity check on the database
	 *
	 * @return null if check passed, open result set with results if failed
	 * @since 1.1.2
	 */
	public ResultSet quickCheck();

	/**
	 * Draw a feature preview image from all features in a feature table
	 * 
	 * @param table
	 *            feature table name
	 * @param manual
	 *            manually query for a bounding box if not indexed and no
	 *            contents bounds, false to instead return no image
	 * @param bufferPercentage
	 *            image empty edge buffer percentage (>= 0.0 && < 0.5), 0.0 for
	 *            no buffer, 0.1 for 10% edge buffer
	 * @return preview image or null
	 * @since 3.5.0
	 */
	public BufferedImage drawFeaturePreview(String table, boolean manual,
			double bufferPercentage);

	// TODO Tile sizes
	
	/**
	 * Draw a feature preview image from all features up to the limit in a
	 * feature table
	 * 
	 * @param table
	 *            feature table name
	 * @param manual
	 *            manually query for a bounding box if not indexed and no
	 *            contents bounds, false to instead return no image
	 * @param bufferPercentage
	 *            image empty edge buffer percentage (>= 0.0 && < 0.5), 0.0 for
	 *            no buffer, 0.1 for 10% edge buffer
	 * @param limit
	 *            query result limit
	 * @return preview image or null
	 * @since 3.5.0
	 */
	public BufferedImage drawFeaturePreview(String table, boolean manual,
			double bufferPercentage, Integer limit);

}
