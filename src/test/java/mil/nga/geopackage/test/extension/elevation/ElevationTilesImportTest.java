package mil.nga.geopackage.test.extension.elevation;

import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.extension.elevation.ElevationTileResults;
import mil.nga.geopackage.extension.elevation.ElevationTiles;
import mil.nga.geopackage.extension.elevation.ElevationTilesAlgorithm;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.test.ImportElevationTilesGeoPackageTestCase;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;

import org.junit.Test;

/**
 * Elevation Tiles Extensions Tests from an imported GeoPackage
 * 
 * @author osbornb
 */
public class ElevationTilesImportTest extends
		ImportElevationTilesGeoPackageTestCase {

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Nearest Neighbor Algorithm
	 */
	@Test
	public void testElevationsNearestNeighbor() throws Exception {

		ElevationTilesTestUtils.testElevations(geoPackage, null,
				ElevationTilesAlgorithm.NEAREST_NEIGHBOR, false);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bilinear Algorithm
	 */
	@Test
	public void testElevationsBilinear() throws Exception {

		ElevationTilesTestUtils.testElevations(geoPackage, null,
				ElevationTilesAlgorithm.BILINEAR, false);

	}

	/**
	 * Test the elevation extension with a newly created GeoPackage using the
	 * Bicubic Algorithm
	 */
	@Test
	public void testElevationsBicubic() throws Exception {

		ElevationTilesTestUtils.testElevations(geoPackage, null,
				ElevationTilesAlgorithm.BICUBIC, false);

	}

	/**
	 * Test a random bounding box using the Nearest Neighbor Algorithm
	 */
	@Test
	public void testRandomBoundingBoxNearestNeighbor() throws Exception {

		ElevationTilesTestUtils.testRandomBoundingBox(geoPackage, null,
				ElevationTilesAlgorithm.NEAREST_NEIGHBOR, true);

	}

	/**
	 * Test a random bounding box using the Bilinear Algorithm
	 */
	@Test
	public void testRandomBoundingBoxBilinear() throws Exception {

		ElevationTilesTestUtils.testRandomBoundingBox(geoPackage, null,
				ElevationTilesAlgorithm.BILINEAR, true);

	}

	/**
	 * Test a random bounding box using the Bicubic Algorithm
	 */
	@Test
	public void testRandomBoundingBoxBicubic() throws Exception {

		ElevationTilesTestUtils.testRandomBoundingBox(geoPackage, null,
				ElevationTilesAlgorithm.BICUBIC, true);

	}

	@Test
	public void printLocation() throws Exception {

		double latitude = 61.57941522271581;
		double longitude = -148.96174115565339;

		testLocation(latitude, longitude);
	}

	@Test
	public void printRandomLocations() throws Exception {

		BoundingBox projectedBoundingBox = null;

		List<String> elevationTables = ElevationTiles.getTables(geoPackage);
		TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();

		for (String elevationTable : elevationTables) {

			TileMatrixSet tileMatrixSet = dao.queryForId(elevationTable);

			BoundingBox boundingBox = tileMatrixSet.getBoundingBox();
			System.out.println("Min Latitude: " + boundingBox.getMinLatitude());
			System.out.println("Max Latitude: " + boundingBox.getMaxLatitude());
			System.out.println("Min Longitude: "
					+ boundingBox.getMinLongitude());
			System.out.println("Max Longitude: "
					+ boundingBox.getMaxLongitude());
			System.out.println();
			SpatialReferenceSystemDao srsDao = geoPackage
					.getSpatialReferenceSystemDao();
			long srsId = tileMatrixSet.getSrsId();
			SpatialReferenceSystem srs = srsDao.getOrCreate(srsId);
			Projection projection = ProjectionFactory.getProjection(srs);
			Projection requestProjection = ProjectionFactory
					.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
			ProjectionTransform elevationToRequest = projection
					.getTransformation(requestProjection);
			projectedBoundingBox = elevationToRequest.transform(boundingBox);

		}
		System.out.println("Min Latitude: "
				+ projectedBoundingBox.getMinLatitude());
		System.out.println("Max Latitude: "
				+ projectedBoundingBox.getMaxLatitude());
		System.out.println("Min Longitude: "
				+ projectedBoundingBox.getMinLongitude());
		System.out.println("Max Longitude: "
				+ projectedBoundingBox.getMaxLongitude());
		System.out.println();

		for (int i = 0; i < 10; i++) {

			// Get a random coordinate
			double latitude = (projectedBoundingBox.getMaxLatitude() - projectedBoundingBox
					.getMinLatitude())
					* Math.random()
					+ projectedBoundingBox.getMinLatitude();
			double longitude = (projectedBoundingBox.getMaxLongitude() - projectedBoundingBox
					.getMinLongitude())
					* Math.random()
					+ projectedBoundingBox.getMinLongitude();
			testLocation(latitude, longitude);
			System.out.println();
		}
	}

	@Test
	public void printBoundingBox() throws Exception {

		long geoPackageEpsg = ProjectionConstants.EPSG_WEB_MERCATOR;

		double widthPixelDistance = 1000;
		double heightPixelDistance = 1000;
		int width = 10;
		int height = 6;
		double minLongitude = -16586000;
		double maxLongitude = minLongitude + (width * widthPixelDistance);
		double minLatitude = 8760000;
		double maxLatitude = minLatitude + (height * heightPixelDistance);

		BoundingBox boundingBox = new BoundingBox(minLongitude, maxLongitude,
				minLatitude, maxLatitude);

		Projection projection = ProjectionFactory.getProjection(geoPackageEpsg);
		Projection printProjection = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
		ProjectionTransform wgs84Transform = projection
				.getTransformation(printProjection);

		System.out.println("REQUEST");
		System.out.println();
		System.out.println("   Min Lat: " + boundingBox.getMinLatitude());
		System.out.println("   Max Lat: " + boundingBox.getMaxLatitude());
		System.out.println("   Min Lon: " + boundingBox.getMinLongitude());
		System.out.println("   Max Lon: " + boundingBox.getMaxLongitude());
		System.out.println("   Result Width: " + width);
		System.out.println("   Result Height: " + height);

		System.out.println();
		System.out.println();
		System.out.println("WGS84 REQUEST");
		System.out.println();
		BoundingBox wgs84BoundingBox = wgs84Transform.transform(boundingBox);
		System.out.println("   Min Lat: " + wgs84BoundingBox.getMinLatitude());
		System.out.println("   Max Lat: " + wgs84BoundingBox.getMaxLatitude());
		System.out.println("   Min Lon: " + wgs84BoundingBox.getMinLongitude());
		System.out.println("   Max Lon: " + wgs84BoundingBox.getMaxLongitude());

		System.out.println();
		System.out.println();
		System.out.println("WGS84 LOCATIONS");
		for (double lat = maxLatitude - (heightPixelDistance * .5); lat >= minLatitude; lat -= heightPixelDistance) {
			System.out.println();
			for (double lon = minLongitude + (widthPixelDistance * .5); lon <= maxLongitude; lon += widthPixelDistance) {
				double[] point = wgs84Transform.transform(lon, lat);
				System.out.print("   (" + point[1] + "," + point[0] + ")");
			}
		}

		for (ElevationTilesAlgorithm algorithm : ElevationTilesAlgorithm
				.values()) {

			System.out.println();
			System.out.println();
			System.out.println(algorithm.name() + " SINGLE ELEVATIONS");
			for (double lat = maxLatitude - (heightPixelDistance * .5); lat >= minLatitude; lat -= heightPixelDistance) {
				System.out.println();
				for (double lon = minLongitude + (widthPixelDistance * .5); lon <= maxLongitude; lon += widthPixelDistance) {
					System.out.print("   "
							+ ElevationTilesTestUtils.getElevation(geoPackage,
									algorithm, lat, lon, geoPackageEpsg));
				}
			}

			ElevationTileResults results = ElevationTilesTestUtils
					.getElevations(geoPackage, algorithm, boundingBox, width,
							height, geoPackageEpsg);
			System.out.println();
			System.out.println();
			System.out.println(algorithm.name());
			Double[][] elevations = results.getElevations();
			for (int y = 0; y < elevations.length; y++) {
				System.out.println();
				for (int x = 0; x < elevations[0].length; x++) {
					System.out.print("   " + elevations[y][x]);
				}
			}
		}

		List<String> elevationTables = ElevationTiles.getTables(geoPackage);
		TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();

		for (String elevationTable : elevationTables) {

			TileMatrixSet tileMatrixSet = dao.queryForId(elevationTable);

			boundingBox = tileMatrixSet.getBoundingBox();

			minLongitude = boundingBox.getMinLongitude();
			maxLongitude = boundingBox.getMaxLongitude();
			minLatitude = boundingBox.getMinLatitude();
			maxLatitude = boundingBox.getMaxLatitude();

			widthPixelDistance = (maxLongitude - minLongitude) / width;
			heightPixelDistance = (maxLatitude - minLatitude) / height;

			for (ElevationTilesAlgorithm algorithm : ElevationTilesAlgorithm
					.values()) {

				System.out.println();
				System.out.println();
				System.out.println(algorithm.name()
						+ " SINGLE ELEVATIONS Full Bounding Box");
				for (double lat = maxLatitude; lat >= minLatitude; lat -= heightPixelDistance) {
					System.out.println();
					for (double lon = minLongitude; lon <= maxLongitude; lon += widthPixelDistance) {
						System.out.print("   "
								+ ElevationTilesTestUtils.getElevation(
										geoPackage, algorithm, lat, lon,
										geoPackageEpsg));
					}
					System.out.print("   "
							+ ElevationTilesTestUtils.getElevation(
									geoPackage, algorithm, lat, maxLongitude,
									geoPackageEpsg));
				}
				System.out.println();
				for (double lon = minLongitude; lon <= maxLongitude; lon += widthPixelDistance) {
					System.out.print("   "
							+ ElevationTilesTestUtils.getElevation(
									geoPackage, algorithm, minLatitude, lon,
									geoPackageEpsg));
				}
				System.out.print("   "
						+ ElevationTilesTestUtils.getElevation(
								geoPackage, algorithm, minLatitude, maxLongitude,
								geoPackageEpsg));

				ElevationTileResults results = ElevationTilesTestUtils
						.getElevations(geoPackage, algorithm, boundingBox,
								width, height, geoPackageEpsg);
				System.out.println();
				System.out.println();
				System.out.println(algorithm.name() + " Full Bounding Box");
				Double[][] elevations = results.getElevations();
				for (int y = 0; y < elevations.length; y++) {
					System.out.println();
					for (int x = 0; x < elevations[0].length; x++) {
						System.out.print("   " + elevations[y][x]);
					}
				}

				TileMatrix tileMatrix = results.getTileMatrix();
				for (int row = 0; row < tileMatrix.getMatrixHeight(); row++) {
					for (int column = 0; column < tileMatrix.getMatrixWidth(); column++) {

						BoundingBox boundingBox2 = TileBoundingBoxUtils
								.getBoundingBox(boundingBox, tileMatrix,
										column, row);

						double minLongitude2 = boundingBox2.getMinLongitude();
						double maxLongitude2 = boundingBox2.getMaxLongitude();
						double minLatitude2 = boundingBox2.getMinLatitude();
						double maxLatitude2 = boundingBox2.getMaxLatitude();

						System.out.println();
						System.out.println();
						System.out.println(algorithm.name()
								+ " SINGLE ELEVATIONS Tile row = " + row
								+ ", column = " + column);
						System.out.println();
						System.out.print("   "
								+ ElevationTilesTestUtils.getElevation(
										geoPackage, algorithm, maxLatitude2,
										minLongitude2, geoPackageEpsg));
						System.out.println("   "
								+ ElevationTilesTestUtils.getElevation(
										geoPackage, algorithm, maxLatitude2,
										maxLongitude2, geoPackageEpsg));
						System.out.print("   "
								+ ElevationTilesTestUtils.getElevation(
										geoPackage, algorithm, minLatitude2,
										minLongitude2, geoPackageEpsg));
						System.out.println("   "
								+ ElevationTilesTestUtils.getElevation(
										geoPackage, algorithm, minLatitude2,
										maxLongitude2, geoPackageEpsg));

						results = ElevationTilesTestUtils.getElevations(
								geoPackage, algorithm, boundingBox2, width,
								height, geoPackageEpsg);
						System.out.println();
						System.out.println();
						System.out.println(algorithm.name() + " Tile row = "
								+ row + ", column = " + column);
						if (results == null) {
							System.out.println();
							System.out.print("null results");
						} else {
							elevations = results.getElevations();
							for (int y = 0; y < elevations.length; y++) {
								System.out.println();
								for (int x = 0; x < elevations[0].length; x++) {
									System.out.print("   " + elevations[y][x]);
								}
							}
						}

					}
				}
			}
		}

	}

	public void testLocation(double latitude, double longitude)
			throws Exception {

		System.out.println("Latitude: " + latitude);
		System.out.println("Longitude: " + longitude);

		for (ElevationTilesAlgorithm algorithm : ElevationTilesAlgorithm
				.values()) {
			Double elevation = ElevationTilesTestUtils.getElevation(geoPackage,
					algorithm, latitude, longitude,
					ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
			System.out.println(algorithm.name() + ": " + elevation);
		}
	}

}
