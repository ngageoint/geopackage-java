package mil.nga.geopackage.io;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.user.TileDao;

/**
 * Builds readable text output from a GeoPackage
 * 
 * @author osbornb
 * @since 1.1.2
 */
public class GeoPackageTextOutput {

	/**
	 * GeoPackage
	 */
	private final GeoPackage geoPackage;

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 */
	public GeoPackageTextOutput(GeoPackage geoPackage) {
		this.geoPackage = geoPackage;
	}

	/**
	 * Get the GeoPackage file and name header text
	 * 
	 * @return text
	 */
	public String header() {
		StringBuilder output = new StringBuilder();
		output.append("GeoPackage File: " + geoPackage.getPath());
		output.append("\nGeoPackage Name: " + geoPackage.getName());
		return output.toString();
	}

	/**
	 * Build text from a tile table
	 * 
	 * @param table
	 *            tile table
	 * @return text
	 */
	public String tileTable(String table) {

		StringBuilder output = new StringBuilder();
		TileDao tileDao = geoPackage.getTileDao(table);
		output.append("Table Name: " + tileDao.getTableName());
		long minZoom = tileDao.getMinZoom();
		long maxZoom = tileDao.getMaxZoom();
		output.append("\nMin Zoom: " + minZoom);
		output.append("\nMax Zoom: " + maxZoom);
		output.append("\nTiles: " + tileDao.count());

		TileMatrixSet tileMatrixSet = tileDao.getTileMatrixSet();

		output.append("\n\nContents\n\n").append(
				textOutput(tileMatrixSet.getContents()));

		output.append("\n\nTile Matrix Set\n\n").append(
				textOutput(tileMatrixSet));

		output.append("\n\n Tile Matrices");

		for (long zoom = minZoom; zoom <= maxZoom; zoom++) {
			TileMatrix tileMatrix = tileDao.getTileMatrix(zoom);
			if (tileMatrix != null) {
				output.append("\n\n").append(textOutput(tileMatrix));
				output.append("\n\tTiles: " + tileDao.count(zoom));
				BoundingBox boundingBox = tileDao.getBoundingBox(zoom);
				output.append("\n\tTile Bounds: \n").append(
						textOutput(boundingBox));
			}
		}

		return output.toString();
	}

	/**
	 * Text output from a SRS
	 * 
	 * @param srs
	 *            spatial reference system
	 * @return text
	 */
	public String textOutput(SpatialReferenceSystem srs) {
		StringBuilder output = new StringBuilder();
		output.append("\tSRS " + SpatialReferenceSystem.COLUMN_ORGANIZATION
				+ ": " + srs.getOrganization());
		output.append("\n\tSRS "
				+ SpatialReferenceSystem.COLUMN_ORGANIZATION_COORDSYS_ID + ": "
				+ srs.getOrganizationCoordsysId());
		output.append("\n\tSRS " + SpatialReferenceSystem.COLUMN_DEFINITION
				+ ": " + srs.getDefinition());
		return output.toString();
	}

	/**
	 * Text output from a Contents
	 * 
	 * @param contents
	 *            contents
	 * @return text
	 */
	public String textOutput(Contents contents) {
		StringBuilder output = new StringBuilder();
		output.append("\t" + Contents.COLUMN_TABLE_NAME + ": "
				+ contents.getTableName());
		output.append("\n\t" + Contents.COLUMN_DATA_TYPE + ": "
				+ contents.getDataType());
		output.append("\n\t" + Contents.COLUMN_IDENTIFIER + ": "
				+ contents.getIdentifier());
		output.append("\n\t" + Contents.COLUMN_DESCRIPTION + ": "
				+ contents.getDescription());
		output.append("\n\t" + Contents.COLUMN_LAST_CHANGE + ": "
				+ contents.getLastChange());
		output.append("\n\t" + Contents.COLUMN_MIN_X + ": "
				+ contents.getMinX());
		output.append("\n\t" + Contents.COLUMN_MIN_Y + ": "
				+ contents.getMinY());
		output.append("\n\t" + Contents.COLUMN_MAX_X + ": "
				+ contents.getMaxX());
		output.append("\n\t" + Contents.COLUMN_MAX_Y + ": "
				+ contents.getMaxY());
		output.append("\n" + textOutput(contents.getSrs()));
		return output.toString();
	}

	/**
	 * Text output from a TileMatrixSet
	 * 
	 * @param tileMatrixSet
	 *            tile matrix set
	 * @return text
	 */
	public String textOutput(TileMatrixSet tileMatrixSet) {
		StringBuilder output = new StringBuilder();
		output.append("\t" + TileMatrixSet.COLUMN_TABLE_NAME + ": "
				+ tileMatrixSet.getTableName());
		output.append("\n" + textOutput(tileMatrixSet.getSrs()));
		output.append("\n\t" + TileMatrixSet.COLUMN_MIN_X + ": "
				+ tileMatrixSet.getMinX());
		output.append("\n\t" + TileMatrixSet.COLUMN_MIN_Y + ": "
				+ tileMatrixSet.getMinY());
		output.append("\n\t" + TileMatrixSet.COLUMN_MAX_X + ": "
				+ tileMatrixSet.getMaxX());
		output.append("\n\t" + TileMatrixSet.COLUMN_MAX_Y + ": "
				+ tileMatrixSet.getMaxY());
		return output.toString();
	}

	/**
	 * Text output from a Tile Matrix
	 * 
	 * @param tileMatrix
	 *            tile matrix
	 * @return text
	 */
	public String textOutput(TileMatrix tileMatrix) {
		StringBuilder output = new StringBuilder();
		output.append("\t" + TileMatrix.COLUMN_TABLE_NAME + ": "
				+ tileMatrix.getTableName());
		output.append("\n\t" + TileMatrix.COLUMN_ZOOM_LEVEL + ": "
				+ tileMatrix.getZoomLevel());
		output.append("\n\t" + TileMatrix.COLUMN_MATRIX_WIDTH + ": "
				+ tileMatrix.getMatrixWidth());
		output.append("\n\t" + TileMatrix.COLUMN_MATRIX_HEIGHT + ": "
				+ tileMatrix.getMatrixHeight());
		output.append("\n\t" + TileMatrix.COLUMN_TILE_WIDTH + ": "
				+ tileMatrix.getTileWidth());
		output.append("\n\t" + TileMatrix.COLUMN_TILE_HEIGHT + ": "
				+ tileMatrix.getTileHeight());
		output.append("\n\t" + TileMatrix.COLUMN_PIXEL_X_SIZE + ": "
				+ tileMatrix.getPixelXSize());
		output.append("\n\t" + TileMatrix.COLUMN_PIXEL_Y_SIZE + ": "
				+ tileMatrix.getPixelYSize());
		return output.toString();
	}

	/**
	 * Text output from a bounding box
	 * 
	 * @param boundingBox
	 *            bounding box
	 * @return text
	 */
	public String textOutput(BoundingBox boundingBox) {
		StringBuilder output = new StringBuilder();
		output.append("\tMin Longitude: " + boundingBox.getMinLongitude());
		output.append("\n\tMin Latitude: " + boundingBox.getMinLatitude());
		output.append("\n\tMax Longitude: " + boundingBox.getMaxLongitude());
		output.append("\n\tMax Latitude: " + boundingBox.getMaxLatitude());
		return output.toString();
	}

}
