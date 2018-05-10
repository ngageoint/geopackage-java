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
public class RelatedTablesReadTest extends LoadGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public RelatedTablesReadTest() {
		super(TestConstants.RTE_DB_FILE_NAME);
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
		TestCase.assertTrue(rte.has());
		
		ExtendedRelationsDao extendedRelationsDao = geoPackage
				.createDao(ExtendedRelations.class);
		Collection<ExtendedRelations> extendedRelations = extendedRelationsDao.queryForAll();
		
		for (ExtendedRelations extendedRelation : extendedRelations) {
			String mappingTableName = extendedRelation.getMappingTableName();
			UserMappingDao mappingDao = new UserMappingDao(mappingTableName, geoPackage.getConnection(), new UserMappingConnection(geoPackage.getConnection()), new UserMappingTable(mappingTableName));
			UserMappingResultSet mappings = mappingDao.queryForAll();
			AttributesDao attributesDao = geoPackage.getAttributesDao(extendedRelation.getRelatedTableName());
			List<AttributesColumn> attributesColumns = attributesDao.getTable().getColumns();
			int relatedIdIndex = mappings.getColumnIndex(UserMappingTable.COLUMN_RELATED_ID);
			while (mappings.moveToNext()) {
				int relatedId = mappings.getInt(relatedIdIndex);
				AttributesResultSet ars = attributesDao.queryForId(relatedId);
				for (AttributesColumn attributesColumn : attributesColumns){
					Object obj = ars.getValue(attributesColumn);
					obj.toString();
				}
			}
		}
	}
}
