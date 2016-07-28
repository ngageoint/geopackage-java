package mil.nga.geopackage.extension.elevation;

import mil.nga.geopackage.tiles.matrix.TileMatrix;

public class ElevationTileResults {

	private final Double[][] elevations;

	private final TileMatrix tileMatrix;

	private int height;

	private int width;

	public ElevationTileResults(Double[][] elevations, TileMatrix tileMatrix) {
		this.elevations = elevations;
		this.tileMatrix = tileMatrix;
		height = elevations.length;
		width = elevations[0].length;
	}

	public Double[][] getElevations() {
		return elevations;
	}

	public TileMatrix getTileMatrix() {
		return tileMatrix;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public Double getElevation(int height, int width) {
		return elevations[height][width];
	}

	public long getZoomLevel() {
		return tileMatrix.getZoomLevel();
	}

}
