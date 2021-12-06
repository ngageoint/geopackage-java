package mil.nga.geopackage.dgiwg;

import java.io.File;
import java.sql.SQLException;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageImpl;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.contents.ContentsDataType;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileTable;

/**
 * DGIWG (Defence Geospatial Information Working Group) GeoPackage
 * implementation
 * 
 * @author osbornb
 * @since 6.1.2
 */
public class DGIWGGeoPackage extends GeoPackageImpl {

	/**
	 * DGIWG File Name
	 */
	private GeoPackageFileName fileName;

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public DGIWGGeoPackage(GeoPackage geoPackage) {
		super(geoPackage.getName(), geoPackage.getPath(),
				geoPackage.getConnection());
		this.fileName = new GeoPackageFileName(geoPackage.getPath());
	}

	/**
	 * Constructor
	 * 
	 * @param fileName
	 *            DGIWG file name
	 * @param geoPackage
	 *            GeoPackage
	 */
	public DGIWGGeoPackage(GeoPackageFileName fileName, GeoPackage geoPackage) {
		super(geoPackage.getName(), geoPackage.getPath(),
				geoPackage.getConnection());
		this.fileName = fileName;
	}

	/**
	 * Constructor
	 *
	 * @param name
	 *            GeoPackage name
	 * @param file
	 *            GeoPackage file
	 * @param database
	 *            connection
	 */
	protected DGIWGGeoPackage(String name, File file,
			GeoPackageConnection database) {
		super(name, file, database);
		this.fileName = new GeoPackageFileName(file);
	}

	/**
	 * Get the DGIWG file name
	 * 
	 * @return DGIWG file name
	 */
	public GeoPackageFileName getFileName() {
		return fileName;
	}

	/**
	 * Create tiles table
	 * 
	 * @param table
	 *            table name
	 * @param crs
	 *            coordinate reference system
	 * @return created tile table
	 */
	public TileTable createTiles(String table, CoordinateReferenceSystem crs) {
		return createTiles(table, table, table, crs);
	}

	/**
	 * Create tiles table
	 * 
	 * @param table
	 *            table name
	 * @param identifier
	 *            contents identifier
	 * @param description
	 *            contents description
	 * @param crs
	 *            coordinate reference system
	 * @return created tile table
	 */
	public TileTable createTiles(String table, String identifier,
			String description, CoordinateReferenceSystem crs) {
		return createTiles(table, identifier, description, crs.getBounds(),
				crs);
	}

	/**
	 * Create tiles table
	 * 
	 * @param table
	 *            table name
	 * @param identifier
	 *            contents identifier
	 * @param description
	 *            contents description
	 * @param informativeBounds
	 *            informative contents bounds
	 * @param crs
	 *            coordinate reference system
	 * @return created tile table
	 */
	public TileTable createTiles(String table, String identifier,
			String description, BoundingBox informativeBounds,
			CoordinateReferenceSystem crs) {
		return createTiles(table, identifier, description, informativeBounds,
				crs.createSpatialReferenceSystem(), crs.getBounds());
	}

	/**
	 * Create tiles table
	 * 
	 * @param table
	 *            table name
	 * @param crs
	 *            coordinate reference system
	 * @param extentBounds
	 *            crs extent bounds
	 * @return created tile table
	 */
	public TileTable createTiles(String table, CoordinateReferenceSystem crs,
			BoundingBox extentBounds) {
		return createTiles(table, table, table, crs, extentBounds);
	}

	/**
	 * Create tiles table
	 * 
	 * @param table
	 *            table name
	 * @param identifier
	 *            contents identifier
	 * @param description
	 *            contents description
	 * @param crs
	 *            coordinate reference system
	 * @param extentBounds
	 *            crs extent bounds
	 * @return created tile table
	 */
	public TileTable createTiles(String table, String identifier,
			String description, CoordinateReferenceSystem crs,
			BoundingBox extentBounds) {
		return createTiles(table, identifier, description, extentBounds, crs,
				extentBounds);
	}

	/**
	 * Create tiles table
	 * 
	 * @param table
	 *            table name
	 * @param identifier
	 *            contents identifier
	 * @param description
	 *            contents description
	 * @param informativeBounds
	 *            informative contents bounds
	 * @param crs
	 *            coordinate reference system
	 * @param extentBounds
	 *            crs extent bounds
	 * @return created tile table
	 */
	public TileTable createTiles(String table, String identifier,
			String description, BoundingBox informativeBounds,
			CoordinateReferenceSystem crs, BoundingBox extentBounds) {
		return createTiles(table, identifier, description, informativeBounds,
				crs.createSpatialReferenceSystem(), extentBounds);
	}

	/**
	 * Create tiles table
	 * 
	 * @param table
	 *            table name
	 * @param srs
	 *            spatial reference system
	 * @param extentBounds
	 *            crs extent bounds
	 * @return created tile table
	 */
	public TileTable createTiles(String table, SpatialReferenceSystem srs,
			BoundingBox extentBounds) {
		return createTiles(table, table, table, srs, extentBounds);
	}

	/**
	 * Create tiles table
	 * 
	 * @param table
	 *            table name
	 * @param identifier
	 *            contents identifier
	 * @param description
	 *            contents description
	 * @param srs
	 *            spatial reference system
	 * @param extentBounds
	 *            crs extent bounds
	 * @return created tile table
	 */
	public TileTable createTiles(String table, String identifier,
			String description, SpatialReferenceSystem srs,
			BoundingBox extentBounds) {
		return createTiles(table, identifier, description, extentBounds, srs,
				extentBounds);
	}

	/**
	 * Create tiles table
	 * 
	 * @param table
	 *            table name
	 * @param identifier
	 *            contents identifier
	 * @param description
	 *            contents description
	 * @param informativeBounds
	 *            informative contents bounds
	 * @param srs
	 *            spatial reference system
	 * @param extentBounds
	 *            crs extent bounds
	 * @return created tile table
	 */
	public TileTable createTiles(String table, String identifier,
			String description, BoundingBox informativeBounds,
			SpatialReferenceSystem srs, BoundingBox extentBounds) {

		createTileMatrixSetTable();
		createTileMatrixTable();

		SpatialReferenceSystemDao srsDao = getSpatialReferenceSystemDao();
		try {
			srs = srsDao.createIfNotExists(srs);
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to create Spatial Reference System: "
							+ srs.getSrsName(),
					e);
		}

		Contents contents = new Contents();
		contents.setTableName(table);
		contents.setDataType(ContentsDataType.TILES);
		contents.setIdentifier(identifier);
		contents.setDescription(description);
		contents.setMinX(informativeBounds.getMinLongitude());
		contents.setMinY(informativeBounds.getMinLatitude());
		contents.setMaxX(informativeBounds.getMaxLongitude());
		contents.setMaxY(informativeBounds.getMaxLatitude());
		contents.setSrs(srs);

		TileTable tileTable = new TileTable(table);
		createTileTable(tileTable);

		try {
			getContentsDao().create(contents);
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to create Contents: " + contents.getTableName(), e);
		}

		TileMatrixSet tileMatrixSet = new TileMatrixSet();
		tileMatrixSet.setContents(contents);
		tileMatrixSet.setSrs(contents.getSrs());
		tileMatrixSet.setMinX(extentBounds.getMinLongitude());
		tileMatrixSet.setMinY(extentBounds.getMinLatitude());
		tileMatrixSet.setMaxX(extentBounds.getMaxLongitude());
		tileMatrixSet.setMaxY(extentBounds.getMaxLatitude());

		try {
			getTileMatrixSetDao().create(tileMatrixSet);
		} catch (SQLException e) {
			throw new GeoPackageException("Failed to create Tile Matrix Set: "
					+ tileMatrixSet.getTableName(), e);
		}

		return tileTable;
	}

}
