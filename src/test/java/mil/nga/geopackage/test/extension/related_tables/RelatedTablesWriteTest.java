package mil.nga.geopackage.test.extension.related_tables;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.junit.Test;

import junit.framework.TestCase;
import mil.nga.geopackage.db.GeoPackageTableCreator;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.RelatedTablesExtension;
import mil.nga.geopackage.extension.related_tables.ExtendedRelations;
import mil.nga.geopackage.extension.related_tables.ExtendedRelationsDao;
import mil.nga.geopackage.extension.related_tables.UserMappingConnection;
import mil.nga.geopackage.extension.related_tables.UserMappingDao;
import mil.nga.geopackage.extension.related_tables.UserMappingResultSet;
import mil.nga.geopackage.extension.related_tables.UserMappingRow;
import mil.nga.geopackage.extension.related_tables.UserMappingTable;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
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
		TestCase.assertEquals(0, extendedRelations.size());
		
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
		TestCase.assertEquals(1, extendedRelations.size());
		
		// 7. Add mappings
		UserMappingDao mappingDao = new UserMappingDao(geoPackage.getConnection(), new UserMappingConnection(geoPackage.getConnection()), umt);
		FeatureResultSet baseFrs = baseDao.queryForAll();
		int baseCount = baseFrs.getCount();
		long[] baseIds = new long[baseCount]; 
		int inx=0;
		while(baseFrs.moveToNext()) {
			baseIds[inx++] = baseFrs.getRow().getId();
		}
		FeatureResultSet relatedFrs = relatedDao.queryForAll();
		int relatedCount = relatedFrs.getCount();
		long[] relatedIds = new long[relatedCount]; 
		inx=0;
		while(relatedFrs.moveToNext()) {
			relatedIds[inx++] = relatedFrs.getRow().getId();
		}
		UserMappingRow umr = null;
		for (inx = 0; inx < 10; inx++){
			umr = new UserMappingRow(umt);
			umr.setBaseId(baseIds[(int)Math.floor(Math.random() * baseCount)]);
			umr.setRelatedId(relatedIds[(int)Math.floor(Math.random() * relatedCount)]);
			mappingDao.insert(umr);
		}
		UserMappingResultSet mappings = mappingDao.queryForAll();
		TestCase.assertEquals(10, mappings.getCount());
		mappings.close();

		// 12. Remove mappings (note: it is plausible and allowed 
		// to have duplicate entries)
		int deleted = mappingDao.delete(umr);
		TestCase.assertFalse(deleted == 0);

		// 6. Remove relationship
		extendedRelationsDao.delete(extendedRelation);
		extendedRelations = extendedRelationsDao.queryForAll();
		TestCase.assertEquals(0, extendedRelations.size());
		geoPackage.dropTable(mappingTableName);

		// 3. Remove extension
		geoPackage.dropTable(ExtendedRelations.TABLE_NAME);
		TestCase.assertFalse(extendedRelationsDao.isTableExists());		
		rte.getExtensionsDao().delete(extension);
		TestCase.assertFalse(rte.has());
	}
}