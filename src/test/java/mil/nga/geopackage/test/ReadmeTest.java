package mil.nga.geopackage.test;

import java.io.File;
import java.util.List;

import org.junit.Test;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.extension.ExtensionsDao;
import mil.nga.geopackage.extension.metadata.MetadataDao;
import mil.nga.geopackage.extension.metadata.MetadataExtension;
import mil.nga.geopackage.extension.metadata.reference.MetadataReferenceDao;
import mil.nga.geopackage.extension.nga.index.FeatureTableIndex;
import mil.nga.geopackage.extension.schema.SchemaExtension;
import mil.nga.geopackage.extension.schema.columns.DataColumnsDao;
import mil.nga.geopackage.extension.schema.constraints.DataColumnConstraintsDao;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.tiles.matrix.TileMatrixDao;
import mil.nga.geopackage.tiles.matrixset.TileMatrixSetDao;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.tiles.user.TileResultSet;
import mil.nga.geopackage.tiles.user.TileRow;
import mil.nga.sf.Geometry;

/**
 * README example tests
 * 
 * @author osbornb
 */
public class ReadmeTest extends CreateGeoPackageTestCase {

	/**
	 * Test transform
	 */
	@Test
	public void testGeoPackage() {

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
	 */
	private void testGeoPackage(File newGeoPackage, File existingGeoPackage) {

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
		DataColumnsDao dao = schemaExtension.getDataColumnsDao();
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
		FeatureDao featureDao = geoPackage.getFeatureDao(features.get(0));
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
		TileDao tileDao = geoPackage.getTileDao(tiles.get(0));
		TileResultSet tileResultSet = tileDao.queryForAll();
		try {
			while (tileResultSet.moveToNext()) {
				TileRow tileRow = tileResultSet.getRow();
				byte[] tileBytes = tileRow.getTileData();
				// ...
			}
		} finally {
			tileResultSet.close();
		}

		// Index Features
		FeatureTableIndex indexer = new FeatureTableIndex(geoPackage,
				featureDao);
		int indexedCount = indexer.index();

		// Close database when done
		geoPackage.close();

	}

}
