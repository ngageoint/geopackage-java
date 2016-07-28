package mil.nga.geopackage.extension.elevation;

import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileResultSet;

public class ElevationTileMatrixResults {

	private TileMatrix tileMatrix;
	private TileResultSet tileResults;

	public ElevationTileMatrixResults(TileMatrix tileMatrix,
			TileResultSet tileResults) {
		this.tileMatrix = tileMatrix;
		this.tileResults = tileResults;
	}

	public TileMatrix getTileMatrix() {
		return tileMatrix;
	}

	public TileResultSet getTileResults() {
		return tileResults;
	}

}
