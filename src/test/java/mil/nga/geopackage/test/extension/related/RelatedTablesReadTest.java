package mil.nga.geopackage.test.extension.related;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import mil.nga.geopackage.attributes.AttributesColumn;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesResultSet;
import mil.nga.geopackage.extension.related.ExtendedRelation;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.test.LoadGeoPackageTestCase;
import mil.nga.geopackage.test.TestConstants;

import org.junit.Test;

/**
 * Test Related Tables Extension reading
 * 
 * @author jyutzler
 */
public class RelatedTablesReadTest extends LoadGeoPackageTestCase {

	private static final Logger log = Logger.getLogger(LoadGeoPackageTestCase.class
			.getName());
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

		// 1. has
		RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);
		TestCase.assertTrue(rte.has());

		// 4. get relationships
		Collection<ExtendedRelation> extendedRelations = rte.getRelationships();
		TestCase.assertEquals(1, extendedRelations.size());
		
		for (ExtendedRelation extendedRelation : extendedRelations) {
			
			// 9. get mappings by base ID
			FeatureDao baseDao = geoPackage.getFeatureDao(extendedRelation.getBaseTableName());
			FeatureColumn pkColumn = baseDao.getTable().getPkColumn();
			FeatureResultSet frs = baseDao.queryForAll();
			while(frs.moveToNext()){
				long baseId = frs.getLong(pkColumn.getIndex());
				long[] relatedIds = rte.getMappingsForBase(extendedRelation, baseId);
				log.log(Level.INFO, String.format("Found ids for %s: %s", baseId, Arrays.toString(relatedIds)));
			}
			frs.close();

			// 10. get mappings by related ID
			AttributesDao relatedDao = geoPackage.getAttributesDao(extendedRelation.getRelatedTableName());
			AttributesColumn pkColumn2 = relatedDao.getTable().getPkColumn();
			AttributesResultSet ars = relatedDao.queryForAll();
			while(ars.moveToNext()){
				long relatedId = ars.getLong(pkColumn2.getIndex());
				long[] baseIds = rte.getMappingsForRelated(extendedRelation, relatedId);
				log.log(Level.INFO, String.format("Found ids for %s: %s", relatedId, Arrays.toString(baseIds)));
			}
			ars.close();
		}
	}
}
