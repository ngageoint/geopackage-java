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
		TestCase.assertFalse(rte.has());
		
		Extensions extension = rte.getOrCreate();
		
		TestCase.assertTrue(rte.has());
		
		ExtendedRelationsDao extendedRelationsDao = geoPackage
				.createDao(ExtendedRelations.class);
		TestCase.assertFalse(extendedRelationsDao.isTableExists());
		GeoPackageTableCreator tableCreator = new GeoPackageTableCreator(
				geoPackage.getConnection());
		tableCreator.createExtendedRelations();
		TestCase.assertTrue(extendedRelationsDao.isTableExists());
		geoPackage.dropTable(ExtendedRelations.TABLE_NAME);
		TestCase.assertFalse(extendedRelationsDao.isTableExists());
		rte.getExtensionsDao().delete(extension);
		TestCase.assertFalse(rte.has());
		
		extension.toString();
	}
}
