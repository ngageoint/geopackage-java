package mil.nga.geopackage.test.extension.elevation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDataType;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.core.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.extension.ExtensionScopeType;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.ExtensionsDao;
import mil.nga.geopackage.extension.elevation.ElevationTiles;
import mil.nga.geopackage.extension.elevation.ElevationTilesCore;
import mil.nga.geopackage.extension.elevation.GriddedCoverage;
import mil.nga.geopackage.extension.elevation.GriddedCoverageDataType;
import mil.nga.geopackage.extension.elevation.GriddedTile;
import mil.nga.geopackage.projection.Projection;
import mil.nga.geopackage.projection.ProjectionConstants;
import mil.nga.geopackage.projection.ProjectionFactory;
import mil.nga.geopackage.projection.ProjectionTransform;
import mil.nga.geopackage.test.ImportElevationTilesGeoPackageTestCase;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSet;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.geopackage.tiles.user.TileTable;

import org.junit.Test;

/**
 * Elevation Tiles Extensions Tests
 * 
 * @author osbornb
 */
public class ElevationTilesTest extends ImportElevationTilesGeoPackageTestCase {

	/**
	 * Test the Extension creation
	 */
	@Test
	public void testExtension() throws Exception {

		TileMatrixSetDao dao = geoPackage.getTileMatrixSetDao();
		TestCase.assertTrue(dao.isTableExists());

		List<TileMatrixSet> results = dao.queryForAll();
		TestCase.assertFalse(results.isEmpty());

		// Verify non nulls
		for (TileMatrixSet tileMatrixSet : results) {

			// Test the tile matrix set
			TestCase.assertNotNull(tileMatrixSet.getTableName());
			TestCase.assertNotNull(tileMatrixSet.getId());
			TestCase.assertNotNull(tileMatrixSet.getSrsId());
			TestCase.assertNotNull(tileMatrixSet.getMinX());
			TestCase.assertNotNull(tileMatrixSet.getMinY());
			TestCase.assertNotNull(tileMatrixSet.getMaxX());
			TestCase.assertNotNull(tileMatrixSet.getMaxY());

			// Test the tile matrix set SRS
			SpatialReferenceSystem srs = tileMatrixSet.getSrs();
			TestCase.assertNotNull(srs);
			TestCase.assertNotNull(srs.getSrsName());
			TestCase.assertNotNull(srs.getSrsId());
			TestCase.assertTrue(srs.getOrganization().equalsIgnoreCase("epsg"));
			TestCase.assertNotNull(srs.getOrganizationCoordsysId());
			TestCase.assertNotNull(srs.getDefinition());

			// Test the contents
			Contents contents = tileMatrixSet.getContents();
			TestCase.assertNotNull(contents);
			TestCase.assertEquals(tileMatrixSet.getTableName(),
					contents.getTableName());
			TestCase.assertEquals(ContentsDataType.ELEVATION_TILES,
					contents.getDataType());
			TestCase.assertEquals(ContentsDataType.ELEVATION_TILES.getName(),
					contents.getDataTypeString());
			TestCase.assertNotNull(contents.getLastChange());

			// Test the contents SRS
			SpatialReferenceSystem contentsSrs = contents.getSrs();
			TestCase.assertNotNull(contentsSrs);
			TestCase.assertNotNull(contentsSrs.getSrsName());
			TestCase.assertNotNull(contentsSrs.getSrsId());
			TestCase.assertNotNull(contentsSrs.getOrganization());
			TestCase.assertNotNull(contentsSrs.getOrganizationCoordsysId());
			TestCase.assertNotNull(contentsSrs.getDefinition());

			// Test the elevation tiles extension is on
			TileDao tileDao = geoPackage.getTileDao(tileMatrixSet);
			ElevationTiles elevationTiles = new ElevationTiles(geoPackage,
					tileDao);
			TestCase.assertTrue(elevationTiles.has());

			// Test the 3 extension rows
			ExtensionsDao extensionsDao = geoPackage.getExtensionsDao();

			Extensions griddedCoverageExtension = extensionsDao
					.queryByExtension(ElevationTilesCore.EXTENSION_NAME,
							GriddedCoverage.TABLE_NAME, null);
			TestCase.assertNotNull(griddedCoverageExtension);
			TestCase.assertEquals(GriddedCoverage.TABLE_NAME,
					griddedCoverageExtension.getTableName());
			TestCase.assertNull(griddedCoverageExtension.getColumnName());
			TestCase.assertEquals(ElevationTilesCore.EXTENSION_NAME,
					griddedCoverageExtension.getExtensionName());
			TestCase.assertEquals(ElevationTilesCore.EXTENSION_DEFINITION,
					griddedCoverageExtension.getDefinition());
			TestCase.assertEquals(ExtensionScopeType.READ_WRITE,
					griddedCoverageExtension.getScope());

			Extensions griddedTileExtension = extensionsDao.queryByExtension(
					ElevationTilesCore.EXTENSION_NAME, GriddedTile.TABLE_NAME,
					null);
			TestCase.assertNotNull(griddedTileExtension);
			TestCase.assertEquals(GriddedTile.TABLE_NAME,
					griddedTileExtension.getTableName());
			TestCase.assertNull(griddedTileExtension.getColumnName());
			TestCase.assertEquals(ElevationTilesCore.EXTENSION_NAME,
					griddedTileExtension.getExtensionName());
			TestCase.assertEquals(ElevationTilesCore.EXTENSION_DEFINITION,
					griddedTileExtension.getDefinition());
			TestCase.assertEquals(ExtensionScopeType.READ_WRITE,
					griddedTileExtension.getScope());

			Extensions tileTableExtension = extensionsDao.queryByExtension(
					ElevationTilesCore.EXTENSION_NAME,
					tileMatrixSet.getTableName(), TileTable.COLUMN_TILE_DATA);
			TestCase.assertNotNull(tileTableExtension);
			TestCase.assertEquals(tileMatrixSet.getTableName(),
					tileTableExtension.getTableName());
			TestCase.assertEquals(TileTable.COLUMN_TILE_DATA,
					tileTableExtension.getColumnName());
			TestCase.assertEquals(ElevationTilesCore.EXTENSION_NAME,
					tileTableExtension.getExtensionName());
			TestCase.assertEquals(ElevationTilesCore.EXTENSION_DEFINITION,
					tileTableExtension.getDefinition());
			TestCase.assertEquals(ExtensionScopeType.READ_WRITE,
					tileTableExtension.getScope());

			// Test the Gridded Coverage
			List<GriddedCoverage> griddedCoverages = elevationTiles
					.getGriddedCoverage();
			TestCase.assertNotNull(griddedCoverages);
			TestCase.assertFalse(griddedCoverages.isEmpty());
			for (GriddedCoverage griddedCoverage : griddedCoverages) {
				TestCase.assertTrue(griddedCoverage.getId() >= 0);
				TestCase.assertNotNull(griddedCoverage.getTileMatrixSet());
				TestCase.assertEquals(tileMatrixSet.getTableName(),
						griddedCoverage.getTileMatrixSetName());
				TestCase.assertEquals(GriddedCoverageDataType.INTEGER,
						griddedCoverage.getDataType());
				TestCase.assertTrue(griddedCoverage.getScale() >= 0);
				TestCase.assertTrue(griddedCoverage.getOffset() >= 0);
				TestCase.assertTrue(griddedCoverage.getPrecision() >= 0);
				griddedCoverage.getDataNull();
				griddedCoverage.getDataMissing();
			}

			// Test the Gridded Tile
			List<GriddedTile> griddedTiles = elevationTiles.getGriddedTile();
			TestCase.assertNotNull(griddedTiles);
			TestCase.assertFalse(griddedTiles.isEmpty());
			for (GriddedTile griddedTile : griddedTiles) {
				TileRow tileRow = tileDao.queryForIdRow(griddedTile
						.getTableId());
				testTileRow(elevationTiles, tileMatrixSet, griddedTile, tileRow);
			}

			TileResultSet tileResultSet = tileDao.queryForAll();
			TestCase.assertNotNull(tileResultSet);
			TestCase.assertTrue(tileResultSet.getCount() > 0);
			while (tileResultSet.moveToNext()) {
				TileRow tileRow = tileResultSet.getRow();
				GriddedTile griddedTile = elevationTiles.getGriddedTile(tileRow
						.getId());
				testTileRow(elevationTiles, tileMatrixSet, griddedTile, tileRow);
			}
			tileResultSet.close();

		}

	}

	private void testTileRow(ElevationTiles elevationTiles,
			TileMatrixSet tileMatrixSet, GriddedTile griddedTile,
			TileRow tileRow) throws IOException, SQLException {
		TestCase.assertNotNull(griddedTile);
		TestCase.assertTrue(griddedTile.getId() >= 0);
		TestCase.assertNotNull(griddedTile.getContents());
		TestCase.assertEquals(tileMatrixSet.getTableName(),
				griddedTile.getTableName());
		long tableId = griddedTile.getTableId();
		TestCase.assertTrue(tableId >= 0);
		TestCase.assertTrue(griddedTile.getScale() >= 0);
		TestCase.assertTrue(griddedTile.getOffset() >= 0);
		griddedTile.getMin();
		griddedTile.getMax();
		griddedTile.getMean();
		griddedTile.getStandardDeviation();
		TestCase.assertNotNull(tileRow);

		TestCase.assertNotNull(tileRow);
		byte[] tileData = tileRow.getTileData();
		TestCase.assertTrue(tileData.length > 0);
		BufferedImage image = tileRow.getTileDataImage();

		// Get all the pixel values of the image
		short[] pixelValues = elevationTiles.getPixelValues(image);

		int width = image.getWidth();
		int height = image.getHeight();

		// Get each individual image pixel value
		List<Short> pixelValuesList = new ArrayList<>();
		for (int y = 0; y < width; y++) {
			for (int x = 0; x < height; x++) {
				short pixelValue = elevationTiles.getPixelValue(image, x, y);
				pixelValuesList.add(pixelValue);

				// Test getting the pixel value from the pixel values
				// array
				short pixelValue2 = elevationTiles.getPixelValue(pixelValues,
						width, x, y);
				TestCase.assertEquals(pixelValue, pixelValue2);

				// Test getting the elevation value
				double elevationValue = elevationTiles.getElevationValue(
						griddedTile, pixelValue);
				GriddedCoverage griddedCoverage = elevationTiles
						.getGriddedCoverage().get(0);
				TestCase.assertEquals(
						(pixelValue * griddedTile.getScale() + griddedTile
								.getOffset())
								* griddedCoverage.getScale()
								+ griddedCoverage.getOffset(), elevationValue);
			}
		}

		// Test the individually built list of pixel values vs the full
		// returned array
		TestCase.assertEquals(pixelValuesList.size(), pixelValues.length);
		for (int i = 0; i < pixelValuesList.size(); i++) {
			TestCase.assertEquals((short) pixelValuesList.get(i),
					pixelValues[i]);
		}

		// Determine an alternate projection
		BoundingBox boundingBox = tileMatrixSet.getBoundingBox();
		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();
		long srsId = tileMatrixSet.getSrsId();
		SpatialReferenceSystem srs = srsDao.getOrCreate(srsId);

		long epsg = srs.getOrganizationCoordsysId();
		Projection projection = ProjectionFactory.getProjection(srs);
		long requestEpsg = -1;
		if (epsg == ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM) {
			requestEpsg = ProjectionConstants.EPSG_WEB_MERCATOR;
		} else {
			requestEpsg = ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM;
		}
		Projection requestProjection = ProjectionFactory
				.getProjection(requestEpsg);
		ProjectionTransform elevationToRequest = projection
				.getTransformation(requestProjection);
		BoundingBox projectedBoundingBox = elevationToRequest
				.transform(boundingBox);

		// Get a random coordinate
		double latitude = (projectedBoundingBox.getMaxLatitude() - projectedBoundingBox
				.getMinLatitude())
				* Math.random()
				+ projectedBoundingBox.getMinLatitude();
		double longitude = (projectedBoundingBox.getMaxLongitude() - projectedBoundingBox
				.getMinLongitude())
				* Math.random()
				+ projectedBoundingBox.getMinLongitude();

		// Test getting the elevation of a single coordinate
		ElevationTiles elevationTiles2 = new ElevationTiles(geoPackage,
				elevationTiles.getTileDao(), requestProjection);
		Double elevation = elevationTiles2.getElevation(latitude, longitude);
		TestCase.assertNotNull(elevation);

		// Build a random bounding box
		double minLatitude = (projectedBoundingBox.getMaxLatitude() - projectedBoundingBox
				.getMinLatitude())
				* Math.random()
				+ projectedBoundingBox.getMinLatitude();
		double minLongitude = (projectedBoundingBox.getMaxLongitude() - projectedBoundingBox
				.getMinLongitude())
				* Math.random()
				+ projectedBoundingBox.getMinLongitude();
		double maxLatitude = (projectedBoundingBox.getMaxLatitude() - minLatitude)
				* Math.random() + minLatitude;
		double maxLongitude = (projectedBoundingBox.getMaxLongitude() - minLongitude)
				* Math.random() + minLongitude;

		BoundingBox requestBoundingBox = new BoundingBox(minLongitude,
				maxLongitude, minLatitude, maxLatitude);
		Double[][] elevations = elevationTiles2
				.getElevation(requestBoundingBox);
		TestCase.assertNotNull(elevations);
		for (int y = 0; y < elevations.length; y++) {
			for (int x = 0; x < elevations[y].length; x++) {
				TestCase.assertNotNull(elevations[y][x]);
			}
		}

		int specifiedWidth = 50;
		int specifiedHeight = 100;
		elevationTiles2.setWidth(specifiedWidth);
		elevationTiles2.setHeight(specifiedHeight);

		elevations = elevationTiles2.getElevation(requestBoundingBox);
		TestCase.assertNotNull(elevations);
		TestCase.assertEquals(specifiedHeight, elevations.length);
		TestCase.assertEquals(specifiedWidth, elevations[0].length);
		for (int y = 0; y < specifiedHeight; y++) {
			for (int x = 0; x < specifiedWidth; x++) {
				TestCase.assertNotNull(elevations[y][x]);
			}
		}

	}

}
