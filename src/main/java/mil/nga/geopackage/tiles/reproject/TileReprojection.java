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
	 * Tile DAO
	 */
	private TileDao tileDao;

	/**
	 * Reprojection Tile DAO
	 */
	private TileDao reprojectTileDao;

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
		super(geoPackage, table, projection);
		this.tileDao = tileDao;
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
		super();
		this.tileDao = tileDao;
		this.reprojectTileDao = reprojectTileDao;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void initialize() {
		if (reprojectTileDao == null) {
			// TODO
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void finish() {
		// TODO
	}

}
