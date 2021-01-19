package mil.nga.geopackage.tiles.reproject;

import mil.nga.geopackage.GeoPackage;
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
	 * @return tile reprojection
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
	 * @return tile reprojection
	 */
	public TileReprojection(TileDao tileDao, TileDao reprojectTileDao) {
		super(tileDao, reprojectTileDao);
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
	protected long getOptimizeZoom() {
		TileDao tileDao = getTileDao();
		return tileDao.getMapZoom(tileDao.getTileMatrixAtMinZoom());
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initialize() {
		if (reprojectTileDao == null) {
			super.initialize();
		}
	}

}
