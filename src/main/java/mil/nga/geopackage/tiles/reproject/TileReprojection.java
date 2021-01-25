package mil.nga.geopackage.tiles.reproject;

import java.sql.SQLException;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.sf.proj.Projection;

/**
 * 
 * Tile Reprojection for reprojecting an existing tile table
 * 
 * @author osbornb
 * @since 4.0.1
 */
public class TileReprojection extends TileReprojectionCore {

	/**
	 * Constructor, reproject a tile table to a new tile table in a specified
	 * GeoPackage
	 *
	 * @param tileDao
	 *            tile DAO
	 * @param geoPackage
	 *            GeoPackage for reprojected tile table
	 * @param table
	 *            new reprojected tile table
	 * @param projection
	 *            desired projection
	 */
	public TileReprojection(TileDao tileDao, GeoPackage geoPackage,
			String table, Projection projection) {
		super(tileDao, geoPackage, table, projection);
	}

	/**
	 * Constructor, reproject a GeoPackage tile table to a new tile table
	 *
	 * @param tileDao
	 *            tile DAO
	 * @param reprojectTileDao
	 *            reprojection tile DAO
	 */
	public TileReprojection(TileDao tileDao, TileDao reprojectTileDao) {
		super(tileDao, reprojectTileDao);
	}

	/**
	 * Constructor, reproject a GeoPackage tile table to a new tile table
	 * 
	 * @param tileDao
	 *            tile DAO
	 * @param geoPackage
	 *            GeoPackage for reprojected tile table
	 * @param reprojectTileDao
	 *            reprojection tile DAO
	 */
	public TileReprojection(TileDao tileDao, GeoPackage geoPackage,
			TileDao reprojectTileDao) {
		super(tileDao, geoPackage, reprojectTileDao);
	}

	/**
	 * Get the tile DAO
	 * 
	 * @return tile DAO
	 */
	public TileDao getTileDao() {
		return (TileDao) super.tileDao;
	}

	/**
	 * Get the GeoPackage
	 * 
	 * @return GeoPackage
	 */
	public GeoPackage getGeoPackage() {
		return (GeoPackage) super.geoPackage;
	}

	/**
	 * Get the reprojection tile DAO
	 * 
	 * @return reprojection tile DAO
	 */
	public TileDao getReprojectTileDao() {
		return (TileDao) super.reprojectTileDao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long getOptimizeZoom() {
		TileDao tileDao = getTileDao();
		return tileDao.getMapZoom(tileDao.getTileMatrixAtMinZoom());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TileDao createReprojectTileDao(String table) {
		return getGeoPackage().getTileDao(table);
	}

	/**
	 * Get the corresponding tile dao
	 * 
	 * @param reproject
	 *            true for reprojection
	 * @return tile dao
	 */
	public TileDao getTileDao(boolean reproject) {
		TileDao tileDao = null;
		if (reproject) {
			tileDao = getReprojectTileDao();
		} else {
			tileDao = getTileDao();
		}
		return tileDao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TileMatrixSet getTileMatrixSet(boolean reproject) {
		return getTileDao(reproject).getTileMatrixSet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<TileMatrix> getTileMatrices(boolean reproject) {
		return getTileDao(reproject).getTileMatrices();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TileMatrix getTileMatrix(boolean reproject, long zoom) {
		return getTileDao(reproject).getTileMatrix(zoom);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void deleteTileMatrices(boolean reproject, String table) {
		try {
			getTileDao(reproject).getTileMatrixDao().deleteByTableName(table);
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to delete tile matrices for tile table. GeoPackage: "
							+ reprojectTileDao.getDatabase() + ", Tile Table: "
							+ table,
					e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected long getMapZoom(boolean reproject, TileMatrix tileMatrix) {
		return getTileDao(reproject).getMapZoom(tileMatrix);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createTileMatrix(TileMatrix tileMatrix) {
		try {
			getReprojectTileDao().getTileMatrixDao().createOrUpdate(tileMatrix);
		} catch (SQLException e) {
			throw new GeoPackageException(
					"Failed to create tile matrix. GeoPackage: "
							+ reprojectTileDao.getDatabase() + ", Tile Table: "
							+ tileMatrix.getTableName(),
					e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int reproject(long zoom) {

		int tiles = 0;

		// TODO
		
//	    GPKGBoundingBox *zoomBounds = [_tileDao boundingBoxWithZoomLevel:zoom inProjection:_reprojectTileDao.projection];
//	    GPKGTileGrid *tileGrid = [GPKGTileBoundingBoxUtils tileGridWithTotalBoundingBox:boundingBox andMatrixWidth:[matrixWidth intValue] andMatrixHeight:[matrixHeight intValue] andBoundingBox:zoomBounds];
//	    
//	    GPKGTileCreator *tileCreator = [[GPKGTileCreator alloc] initWithTileDao:_tileDao andWidth:tileWidth andHeight:tileHeight andProjection:_reprojectTileDao.projection];
//	    
//	    for(int tileRow = tileGrid.minY; tileRow <= tileGrid.maxY; tileRow++){
//	        
//	        double tileMaxLatitude = maxLatitude - ((tileRow / [matrixHeight doubleValue]) * latitudeRange);
//	        double tileMinLatitude = maxLatitude - (((tileRow + 1) / [matrixHeight doubleValue]) * latitudeRange);
//	        
//	        for(int tileColumn = tileGrid.minX; [self isActive] && tileColumn <= tileGrid.maxX; tileColumn++){
//	            
//	            double tileMinLongitude = minLongitude + ((tileColumn / [matrixWidth doubleValue]) * longitudeRange);
//	            double tileMaxLongitude = minLongitude + (((tileColumn + 1) / [matrixWidth doubleValue]) * longitudeRange);
//	            
//	            GPKGBoundingBox *tileBounds = [[GPKGBoundingBox alloc] initWithMinLongitudeDouble:tileMinLongitude andMinLatitudeDouble:tileMinLatitude andMaxLongitudeDouble:tileMaxLongitude andMaxLatitudeDouble:tileMaxLatitude];
//	            
//	            GPKGGeoPackageTile *tile = [tileCreator tileWithBoundingBox:tileBounds andZoom:zoom];
//	            
//	            if(tile != nil){
//	                
//	                GPKGTileRow *row = [_reprojectTileDao queryForTileWithColumn:tileColumn andRow:tileRow andZoomLevel:toZoom];
//	                
//	                if(row == nil){
//	                    row = [_reprojectTileDao newRow];
//	                    [row setTileColumn:tileColumn];
//	                    [row setTileRow:tileRow];
//	                    [row setZoomLevel:toZoom];
//	                }
//	                
//	                [row setTileData:tile.data];
//	                
//	                [_reprojectTileDao createOrUpdate:row];
//	                tiles++;
//	                
//	                if(_progress != nil){
//	                    [_progress addProgress:1];
//	                }
//	            }
//	        }
//	        
//	    }

		return tiles;
	}

}
