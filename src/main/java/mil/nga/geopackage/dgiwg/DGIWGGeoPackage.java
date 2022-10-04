package mil.nga.geopackage.dgiwg;

import java.io.File;
import java.util.Collection;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageImpl;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;

/**
 * DGIWG (Defence Geospatial Information Working Group) GeoPackage
 * implementation
 * 
 * @author osbornb
 * @since 6.5.1
 */
public class DGIWGGeoPackage extends GeoPackageImpl {

	/**
	 * DGIWG File Name
	 */
	private GeoPackageFileName fileName;

	/**
	 * Validate errors when validated
	 */
	private DGIWGValidationErrors errors;

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
	 * Is the GeoPackage valid according to the DGIWG GeoPackage Profile
	 * 
	 * @return true if valid
	 */
	public boolean isValid() {
		return validate().isValid();
	}

	/**
	 * Validate the GeoPackage against the DGIWG GeoPackage Profile
	 * 
	 * @return validation errors
	 */
	public DGIWGValidationErrors validate() {
		errors = DGIWGValidate.validate(this);
		return errors;
	}

	/**
	 * Get the most recent {@link #validate()} results
	 * 
	 * @return validation errors, null if not yet validated
	 */
	public DGIWGValidationErrors getErrors() {
		return errors;
	}

	/**
	 * Create tiles table
	 * 
	 * @param table
	 *            table name
	 * @param crs
	 *            coordinate reference system
	 * @return created tile matrix set
	 */
	public TileMatrixSet createTiles(String table,
			CoordinateReferenceSystem crs) {
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
	 * @return created tile matrix set
	 */
	public TileMatrixSet createTiles(String table, String identifier,
			String description, CoordinateReferenceSystem crs) {
		return createTiles(table, identifier, description, null, crs);
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
	 * @return created tile matrix set
	 */
	public TileMatrixSet createTiles(String table, String identifier,
			String description, BoundingBox informativeBounds,
			CoordinateReferenceSystem crs) {
		SpatialReferenceSystem srs = crs.createSpatialReferenceSystem();
		return createTiles(table, identifier, description, informativeBounds,
				srs, crs.getBounds(srs));
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
	 * @return created tile matrix set
	 */
	public TileMatrixSet createTiles(String table,
			CoordinateReferenceSystem crs, BoundingBox extentBounds) {
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
	 * @return created tile matrix set
	 */
	public TileMatrixSet createTiles(String table, String identifier,
			String description, CoordinateReferenceSystem crs,
			BoundingBox extentBounds) {
		return createTiles(table, identifier, description, null, crs,
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
	 * @return created tile matrix set
	 */
	public TileMatrixSet createTiles(String table, String identifier,
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
	 * @return created tile matrix set
	 */
	public TileMatrixSet createTiles(String table, SpatialReferenceSystem srs,
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
	 * @return created tile matrix set
	 */
	public TileMatrixSet createTiles(String table, String identifier,
			String description, SpatialReferenceSystem srs,
			BoundingBox extentBounds) {
		return createTiles(table, identifier, description, null, srs,
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
	 * @return created tile matrix set
	 */
	public TileMatrixSet createTiles(String table, String identifier,
			String description, BoundingBox informativeBounds,
			SpatialReferenceSystem srs, BoundingBox extentBounds) {
		return DGIWGGeoPackageUtils.createTiles(this, table, identifier,
				description, informativeBounds, srs, extentBounds);
	}

	/**
	 * Create a tile matrix for a zoom level
	 * 
	 * @param tileMatrixSet
	 *            tile matrix set
	 * @param minZoom
	 *            min zoom level
	 * @param maxZoom
	 *            max zoom level
	 * @param matrixWidth
	 *            matrix width
	 * @param matrixHeight
	 *            matrix height
	 */
	public void createTileMatrices(TileMatrixSet tileMatrixSet, long minZoom,
			long maxZoom, long matrixWidth, long matrixHeight) {
		createTileMatrices(tileMatrixSet.getTableName(),
				tileMatrixSet.getBoundingBox(), minZoom, maxZoom, matrixWidth,
				matrixHeight);
	}

	/**
	 * Create a tile matrix for a zoom level
	 * 
	 * @param table
	 *            table name
	 * @param boundingBox
	 *            bounding box
	 * @param minZoom
	 *            min zoom level
	 * @param maxZoom
	 *            max zoom level
	 * @param matrixWidth
	 *            matrix width
	 * @param matrixHeight
	 *            matrix height
	 */
	public void createTileMatrices(String table, BoundingBox boundingBox,
			long minZoom, long maxZoom, long matrixWidth, long matrixHeight) {
		DGIWGGeoPackageUtils.createTileMatrices(this, table, boundingBox,
				minZoom, maxZoom, matrixWidth, matrixHeight);
	}

	/**
	 * Create a tile matrix for a zoom level
	 * 
	 * @param tileMatrixSet
	 *            tile matrix set
	 * @param zoomLevels
	 *            zoom levels
	 * @param matrixWidth
	 *            matrix width
	 * @param matrixHeight
	 *            matrix height
	 */
	public void createTileMatrices(TileMatrixSet tileMatrixSet,
			Collection<Long> zoomLevels, long matrixWidth, long matrixHeight) {
		createTileMatrices(tileMatrixSet.getTableName(),
				tileMatrixSet.getBoundingBox(), zoomLevels, matrixWidth,
				matrixHeight);
	}

	/**
	 * Create a tile matrix for a zoom level
	 * 
	 * @param table
	 *            table name
	 * @param boundingBox
	 *            bounding box
	 * @param zoomLevels
	 *            zoom levels
	 * @param matrixWidth
	 *            matrix width
	 * @param matrixHeight
	 *            matrix height
	 */
	public void createTileMatrices(String table, BoundingBox boundingBox,
			Collection<Long> zoomLevels, long matrixWidth, long matrixHeight) {
		DGIWGGeoPackageUtils.createTileMatrices(this, table, boundingBox,
				zoomLevels, matrixWidth, matrixHeight);
	}

	/**
	 * Create a tile matrix for a zoom level
	 * 
	 * @param tileMatrixSet
	 *            tile matrix set
	 * @param zoom
	 *            zoom level
	 * @param matrixWidth
	 *            matrix width
	 * @param matrixHeight
	 *            matrix height
	 */
	public void createTileMatrices(TileMatrixSet tileMatrixSet, long zoom,
			long matrixWidth, long matrixHeight) {
		createTileMatrices(tileMatrixSet.getTableName(),
				tileMatrixSet.getBoundingBox(), zoom, matrixWidth,
				matrixHeight);
	}

	/**
	 * Create a tile matrix for a zoom level
	 * 
	 * @param table
	 *            table name
	 * @param boundingBox
	 *            bounding box
	 * @param zoom
	 *            zoom level
	 * @param matrixWidth
	 *            matrix width
	 * @param matrixHeight
	 *            matrix height
	 */
	public void createTileMatrices(String table, BoundingBox boundingBox,
			long zoom, long matrixWidth, long matrixHeight) {
		DGIWGGeoPackageUtils.createTileMatrices(this, table, boundingBox, zoom,
				matrixWidth, matrixHeight);
	}

	/**
	 * Create a tile matrix for a zoom level
	 * 
	 * @param table
	 *            table name
	 * @param zoom
	 *            zoom level
	 * @param matrixWidth
	 *            matrix width
	 * @param matrixHeight
	 *            matrix height
	 * @param pixelXSize
	 *            pixel x size
	 * @param pixelYSize
	 *            pixel y size
	 */
	public void createTileMatrix(String table, long zoom, long matrixWidth,
			long matrixHeight, double pixelXSize, double pixelYSize) {
		DGIWGGeoPackageUtils.createTileMatrix(this, table, zoom, matrixWidth,
				matrixHeight, pixelXSize, pixelYSize);
	}

}
