package mil.nga.geopackage.test.extension.related_tables;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;
import mil.nga.geopackage.attributes.AttributesColumn;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesResultSet;
import mil.nga.geopackage.db.GeoPackageCoreConnection;
import mil.nga.geopackage.db.GeoPackageTableCreator;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.RelatedTablesExtension;
import mil.nga.geopackage.extension.related_tables.ExtendedRelations;
import mil.nga.geopackage.extension.related_tables.ExtendedRelationsDao;
import mil.nga.geopackage.extension.related_tables.UserMappingConnection;
import mil.nga.geopackage.extension.related_tables.UserMappingDao;
import mil.nga.geopackage.extension.related_tables.UserMappingResultSet;
import mil.nga.geopackage.extension.related_tables.UserMappingTable;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.test.LoadGeoPackageTestCase;
import mil.nga.geopackage.test.TestConstants;

/**
 * Test Tile Creator from a GeoPackage with tiles
 * 
 * @author osbornb
 */
public class RelatedTablesWriteTest extends LoadGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public RelatedTablesWriteTest() {
		super(TestConstants.IMPORT_DB_FILE_NAME);
	}

	/**
	 * Test get tile
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void testGetRelationships() throws SQLException, IOException {

		RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);

		// 1. Has extension
		TestCase.assertFalse(rte.has());
		
		// 2. Add extension
		Extensions extension = rte.getOrCreate();
		TestCase.assertTrue(rte.has());
		ExtendedRelationsDao extendedRelationsDao = geoPackage
				.createDao(ExtendedRelations.class);
		TestCase.assertFalse(extendedRelationsDao.isTableExists());
		GeoPackageTableCreator tableCreator = new GeoPackageTableCreator(
				geoPackage.getConnection());
		tableCreator.createExtendedRelations();
		TestCase.assertTrue(extendedRelationsDao.isTableExists());

		// 4. Get relationships
		Collection<ExtendedRelations> extendedRelations = extendedRelationsDao.queryForAll();
		TestCase.assertTrue(extendedRelations.size() == 0);
		
		// 5. Add relationship between "geometry2d" and "geometry3d"
		// 5a. Create mapping table
		final String mappingTableName = "g2d_3d";
		UserMappingTable umt = new UserMappingTable(mappingTableName);
		tableCreator.createTable(umt);
		// How do we check that the table was actually created?
//		TestCase.assertTrue(geoPackage.isTable(mappingTableName));
		
		// 5b. create relationship
		final String baseTableName = "geometry2d";
		final String relatedTableName = "geometry3d";
		FeatureDao baseDao = geoPackage.getFeatureDao(baseTableName);
		FeatureDao relatedDao = geoPackage.getFeatureDao(relatedTableName);
		ExtendedRelations extendedRelation = new ExtendedRelations();
		extendedRelation.setBaseTableName(baseTableName);
		extendedRelation.setBasePrimaryColumn(baseDao.getTable().getPkColumn().getName());
		extendedRelation.setRelatedTableName(relatedTableName);
		extendedRelation.setRelatedPrimaryColumn(relatedDao.getTable().getPkColumn().getName());
		extendedRelation.setMappingTableName(mappingTableName);
		extendedRelation.setRelationName("ggggg");
		extendedRelationsDao.create(extendedRelation);
		extendedRelations = extendedRelationsDao.queryForAll();
		TestCase.assertTrue(extendedRelations.size() == 1);
		
		// 7. Add mapping
//		UserMappingDao mappingDao = new UserMappingDao(mappingTableName, geoPackage.getConnection(), new UserMappingConnection(geoPackage.getConnection()), umt);
//		mappingDao.

		// 12. Remove mapping

		// 6. Remove relationship
		extendedRelationsDao.delete(extendedRelation);
		extendedRelations = extendedRelationsDao.queryForAll();
		TestCase.assertTrue(extendedRelations.size() == 0);
		geoPackage.dropTable(mappingTableName);

		// 3. Remove extension
		geoPackage.dropTable(ExtendedRelations.TABLE_NAME);
		TestCase.assertFalse(extendedRelationsDao.isTableExists());		
		rte.getExtensionsDao().delete(extension);
		TestCase.assertFalse(rte.has());
	}
}
