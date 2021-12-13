package mil.nga.geopackage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.extension.ExtensionsDao;
import mil.nga.geopackage.extension.metadata.MetadataDao;
import mil.nga.geopackage.extension.metadata.MetadataExtension;
import mil.nga.geopackage.extension.metadata.reference.MetadataReferenceDao;
import mil.nga.geopackage.extension.schema.SchemaExtension;
import mil.nga.geopackage.extension.schema.columns.DataColumnsDao;
import mil.nga.geopackage.extension.schema.constraints.DataColumnConstraintsDao;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.tiles.GeoPackageTile;
import mil.nga.geopackage.tiles.GeoPackageTileRetriever;
import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.tiles.TileCreator;
import mil.nga.geopackage.tiles.TileGenerator;
import mil.nga.geopackage.tiles.UrlTileGenerator;
import mil.nga.geopackage.tiles.features.DefaultFeatureTiles;
import mil.nga.geopackage.tiles.features.FeatureTileGenerator;
import mil.nga.geopackage.tiles.features.FeatureTiles;
import mil.nga.geopackage.tiles.features.custom.NumberFeaturesTile;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.proj.Projection;
import mil.nga.proj.ProjectionConstants;
import mil.nga.proj.ProjectionFactory;
import mil.nga.sf.Geometry;

/**
 * README example tests
 * 
 * @author osbornb
 */
public class ReadmeTest extends CreateGeoPackageTestCase {

	/**
	 * Test transform
	 * 
	 * @throws IOException
	 *             upon error
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testGeoPackage() throws SQLException, IOException {

		File newGeoPackage = new File("new.gpkg");
		newGeoPackage.delete();

		geoPackage.close();
		File existingGeoPackage = new File(geoPackage.getPath());

		try {
			testGeoPackage(newGeoPackage, existingGeoPackage);
		} finally {
			newGeoPackage.delete();
		}

	}

	/**
	 * Test GeoPackage
	 * 
	 * @param newGeoPackage
	 *            new GeoPackage
	 * @param existingGeoPackage
	 *            existing GeoPackage
	 * @throws IOException
	 *             upon error
	 * @throws SQLException
	 *             upon error
	 */
	private void testGeoPackage(File newGeoPackage, File existingGeoPackage)
			throws SQLException, IOException {

		// File newGeoPackage = ...;
		// File existingGeoPackage = ...;

		// Create a new GeoPackage
		File createdGeoPackage = GeoPackageManager.create(newGeoPackage);

		// Open a GeoPackage
		GeoPackage geoPackage = GeoPackageManager.open(existingGeoPackage);

		// GeoPackage Table DAOs
		SpatialReferenceSystemDao srsDao = geoPackage
				.getSpatialReferenceSystemDao();
		ContentsDao contentsDao = geoPackage.getContentsDao();
		GeometryColumnsDao geomColumnsDao = geoPackage.getGeometryColumnsDao();
		TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
		TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
		SchemaExtension schemaExtension = new SchemaExtension(geoPackage);
		DataColumnsDao dataColumnsDao = schemaExtension.getDataColumnsDao();
		DataColumnConstraintsDao dataColumnConstraintsDao = schemaExtension
				.getDataColumnConstraintsDao();
		MetadataExtension metadataExtension = new MetadataExtension(geoPackage);
		MetadataDao metadataDao = metadataExtension.getMetadataDao();
		MetadataReferenceDao metadataReferenceDao = metadataExtension
				.getMetadataReferenceDao();
		ExtensionsDao extensionsDao = geoPackage.getExtensionsDao();

		// Feature and tile tables
		List<String> features = geoPackage.getFeatureTables();
		List<String> tiles = geoPackage.getTileTables();

		// Query Features
		String featureTable = features.get(0);
		FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
		FeatureResultSet featureResultSet = featureDao.queryForAll();
		try {
			while (featureResultSet.moveToNext()) {
				FeatureRow featureRow = featureResultSet.getRow();
				GeoPackageGeometryData geometryData = featureRow.getGeometry();
				if (geometryData != null && !geometryData.isEmpty()) {
					Geometry geometry = geometryData.getGeometry();
					// ...
				}
			}
		} finally {
			featureResultSet.close();
		}

		// Query Tiles
		String tileTable = tiles.get(0);
		TileDao tileDao = geoPackage.getTileDao(tileTable);
		TileResultSet tileResultSet = tileDao.queryForAll();
		try {
			while (tileResultSet.moveToNext()) {
				TileRow tileRow = tileResultSet.getRow();
				byte[] tileBytes = tileRow.getTileData();
				BufferedImage tileImage = tileRow.getTileDataImage();
				// ...
			}
		} finally {
			tileResultSet.close();
		}

		// Retrieve Tiles by XYZ
		GeoPackageTileRetriever retriever = new GeoPackageTileRetriever(tileDao,
				ImageUtils.IMAGE_FORMAT_PNG);
		GeoPackageTile geoPackageTile = retriever.getTile(2, 2, 2);
		if (geoPackageTile != null) {
			byte[] tileBytes = geoPackageTile.getData();
			BufferedImage tileImage = geoPackageTile.getImage();
			// ...
		}

		// Retrieve Tiles by Bounding Box
		TileCreator tileCreator = new TileCreator(tileDao,
				ProjectionFactory.getProjection(
						ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM),
				ImageUtils.IMAGE_FORMAT_PNG);
		GeoPackageTile geoPackageTile2 = tileCreator
				.getTile(new BoundingBox(-90.0, 0.0, 0.0, 66.513260));
		if (geoPackageTile2 != null) {
			byte[] tileBytes = geoPackageTile2.getData();
			BufferedImage tileImage = geoPackageTile2.getImage();
			// ...
		}

		// Index Features
		FeatureIndexManager indexer = new FeatureIndexManager(geoPackage,
				featureDao);
		indexer.setIndexLocation(FeatureIndexType.GEOPACKAGE);
		int indexedCount = indexer.index();

		// Draw tiles from features
		FeatureTiles featureTiles = new DefaultFeatureTiles(featureDao);
		// Set max features to draw per tile
		featureTiles.setMaxFeaturesPerTile(1000);
		// Custom feature tile implementation
		NumberFeaturesTile numberFeaturesTile = new NumberFeaturesTile();
		// Draw feature count tiles when max features passed
		featureTiles.setMaxFeaturesTileDraw(numberFeaturesTile);
		// Set index manager to query feature indices
		featureTiles.setIndexManager(indexer);
		BufferedImage tile = featureTiles.drawTile(2, 2, 2);

		BoundingBox boundingBox = BoundingBox.worldWebMercator();
		Projection projection = ProjectionFactory
				.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);

		// URL Tile Generator (generate tiles from a URL)
		TileGenerator urlTileGenerator = new UrlTileGenerator(geoPackage,
				"url_tile_table", "http://url/{z}/{x}/{y}.png", 0, 0,
				boundingBox, projection);
		int urlTileCount = urlTileGenerator.generateTiles();

		// Feature Tile Generator (generate tiles from features)
		TileGenerator featureTileGenerator = new FeatureTileGenerator(
				geoPackage, "tiles_" + featureTable, featureTiles, 1, 2,
				boundingBox, projection);
		int featureTileCount = featureTileGenerator.generateTiles();

		// Close feature tiles (and indexer)
		featureTiles.close();

		// Close database when done
		geoPackage.close();

	}

}
