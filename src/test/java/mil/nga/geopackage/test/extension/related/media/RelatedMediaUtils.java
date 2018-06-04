package mil.nga.geopackage.test.extension.related.media;

import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.extension.related.ExtendedRelation;
import mil.nga.geopackage.extension.related.ExtendedRelationsDao;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;
import mil.nga.geopackage.extension.related.UserMappingDao;
import mil.nga.geopackage.extension.related.UserMappingRow;
import mil.nga.geopackage.extension.related.UserMappingTable;
import mil.nga.geopackage.extension.related.media.MediaDao;
import mil.nga.geopackage.extension.related.media.MediaRow;
import mil.nga.geopackage.extension.related.media.MediaTable;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.test.extension.related.RelatedTablesUtils;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomResultSet;

public class RelatedMediaUtils {

	/**
	 * Test related media tables
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testMedia(GeoPackage geoPackage) throws SQLException {

		RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);

		TestCase.assertFalse(rte.has());

		List<ExtendedRelation> extendedRelations = rte.getRelationships();
		TestCase.assertTrue(extendedRelations.isEmpty());

		List<String> featureTables = geoPackage.getFeatureTables();
		final String baseTableName = featureTables
				.get((int) (Math.random() * featureTables.size()));
		final String mappingTableName = "features_media";

		List<UserCustomColumn> additionalMediaColumns = RelatedTablesUtils
				.createAdditionalUserColumns(MediaTable.numRequiredColumns());
		MediaTable mediaTable = MediaTable.create("media_table",
				additionalMediaColumns);
		TestCase.assertEquals(MediaTable.numRequiredColumns()
				+ additionalMediaColumns.size(), mediaTable.getColumns().size());
		UserCustomColumn idColumn = mediaTable.getIdColumn();
		TestCase.assertNotNull(idColumn);
		TestCase.assertTrue(idColumn.isNamed(MediaTable.COLUMN_ID));
		TestCase.assertEquals(GeoPackageDataType.INTEGER,
				idColumn.getDataType());
		TestCase.assertTrue(idColumn.isNotNull());
		TestCase.assertTrue(idColumn.isPrimaryKey());
		UserCustomColumn dataColumn = mediaTable.getDataColumn();
		TestCase.assertNotNull(dataColumn);
		TestCase.assertTrue(dataColumn.isNamed(MediaTable.COLUMN_DATA));
		TestCase.assertEquals(GeoPackageDataType.BLOB, dataColumn.getDataType());
		TestCase.assertTrue(dataColumn.isNotNull());
		TestCase.assertFalse(dataColumn.isPrimaryKey());
		UserCustomColumn contentTypeColumn = mediaTable.getContentTypeColumn();
		TestCase.assertNotNull(contentTypeColumn);
		TestCase.assertTrue(contentTypeColumn
				.isNamed(MediaTable.COLUMN_CONTENT_TYPE));
		TestCase.assertEquals(GeoPackageDataType.TEXT,
				contentTypeColumn.getDataType());
		TestCase.assertTrue(contentTypeColumn.isNotNull());
		TestCase.assertFalse(contentTypeColumn.isPrimaryKey());

		List<UserCustomColumn> additionalMappingColumns = RelatedTablesUtils
				.createAdditionalUserColumns(UserMappingTable
						.numRequiredColumns());
		UserMappingTable userMappingTable = UserMappingTable.create(
				mappingTableName, additionalMappingColumns);
		TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
		TestCase.assertEquals(UserMappingTable.numRequiredColumns()
				+ additionalMappingColumns.size(), userMappingTable
				.getColumns().size());
		UserCustomColumn baseIdColumn = userMappingTable.getBaseIdColumn();
		TestCase.assertNotNull(baseIdColumn);
		TestCase.assertTrue(baseIdColumn
				.isNamed(UserMappingTable.COLUMN_BASE_ID));
		TestCase.assertEquals(GeoPackageDataType.INTEGER,
				baseIdColumn.getDataType());
		TestCase.assertTrue(baseIdColumn.isNotNull());
		TestCase.assertFalse(baseIdColumn.isPrimaryKey());
		UserCustomColumn relatedIdColumn = userMappingTable
				.getRelatedIdColumn();
		TestCase.assertNotNull(relatedIdColumn);
		TestCase.assertTrue(relatedIdColumn
				.isNamed(UserMappingTable.COLUMN_RELATED_ID));
		TestCase.assertEquals(GeoPackageDataType.INTEGER,
				relatedIdColumn.getDataType());
		TestCase.assertTrue(relatedIdColumn.isNotNull());
		TestCase.assertFalse(relatedIdColumn.isPrimaryKey());

		TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
		ExtendedRelation extendedRelation = rte.createRelationship(
				baseTableName, mediaTable, userMappingTable);
		TestCase.assertTrue(rte.has());
		TestCase.assertTrue(rte.has(userMappingTable.getTableName()));
		TestCase.assertNotNull(extendedRelation);
		extendedRelations = rte.getRelationships();
		TestCase.assertEquals(1, extendedRelations.size());
		TestCase.assertTrue(geoPackage.getDatabase().tableExists(
				mappingTableName));
		TestCase.assertTrue(geoPackage.getDatabase().tableExists(
				mediaTable.getTableName()));

		FeatureDao featureDao = geoPackage.getFeatureDao(baseTableName);
		MediaDao mediaDao = MediaDao.getDao(rte, mediaTable);

		byte[] data = TestUtils.getTileBytes();
		int mediaCount = 2 + (int) (Math.random() * 9);
		for (int i = 0; i < mediaCount; i++) {
			MediaRow mediaRow = mediaDao.newRow();
			mediaRow.setData(data);
			mediaRow.setContentType("image/png");
			RelatedTablesUtils.populateUserRow(mediaTable, mediaRow,
					MediaTable.requiredColumns());
			TestCase.assertTrue(mediaDao.create(mediaRow) > 0);
		}
		TestCase.assertEquals(mediaCount, mediaDao.count());

		FeatureResultSet featureResultSet = featureDao.queryForAll();
		int featureCount = featureResultSet.getCount();
		long[] baseIds = new long[featureCount];
		int index = 0;
		while (featureResultSet.moveToNext()) {
			baseIds[index++] = featureResultSet.getRow().getId();
		}
		featureResultSet.close();

		UserCustomResultSet relatedResultSet = mediaDao.queryForAll();
		int relatedCount = relatedResultSet.getCount();
		long[] relatedIds = new long[relatedCount];
		index = 0;
		while (relatedResultSet.moveToNext()) {
			relatedIds[index++] = relatedResultSet.getRow().getId();
		}
		relatedResultSet.close();

		UserMappingDao dao = rte.getUserMappingDao(mappingTableName);
		UserMappingRow userMappingRow = null;
		for (int i = 0; i < 10; i++) {
			userMappingRow = dao.newRow();
			userMappingRow
					.setBaseId(baseIds[(int) Math.random() * featureCount]);
			userMappingRow.setRelatedId(relatedIds[(int) Math.random()
					* relatedCount]);
			RelatedTablesUtils.populateUserRow(userMappingTable,
					userMappingRow, UserMappingTable.requiredColumns());
			TestCase.assertTrue(dao.create(userMappingRow) > 0);
		}
		TestCase.assertEquals(10, dao.count());

		userMappingTable = dao.getTable();
		String[] columns = userMappingTable.getColumnNames();
		UserCustomResultSet resultSet = dao.queryForAll();
		int count = resultSet.getCount();
		TestCase.assertEquals(10, count);
		int manualCount = 0;
		while (resultSet.moveToNext()) {

			UserMappingRow resultRow = dao.getRow(resultSet);
			TestCase.assertFalse(resultRow.hasId());
			RelatedTablesUtils.validateUserRow(columns, resultRow);

			manualCount++;
		}
		TestCase.assertEquals(count, manualCount);
		resultSet.close();

		ExtendedRelationsDao extendedRelationsDao = rte
				.getExtendedRelationsDao();
		List<ExtendedRelation> featureExtendedRelations = extendedRelationsDao
				.getBaseTableRelations(featureDao.getTableName());
		List<ExtendedRelation> featureExtendedRelations2 = extendedRelationsDao
				.getTableRelations(featureDao.getTableName());
		TestCase.assertEquals(1, featureExtendedRelations.size());
		TestCase.assertEquals(1, featureExtendedRelations2.size());
		TestCase.assertEquals(featureExtendedRelations.get(0).getId(),
				featureExtendedRelations2.get(0).getId());
		TestCase.assertTrue(extendedRelationsDao.getRelatedTableRelations(
				featureDao.getTableName()).isEmpty());

		for (ExtendedRelation featureRelation : featureExtendedRelations) {

			TestCase.assertTrue(featureRelation.getId() >= 0);
			TestCase.assertEquals(featureDao.getTableName(),
					featureRelation.getBaseTableName());
			TestCase.assertEquals(
					featureDao.getTable().getPkColumn().getName(),
					featureRelation.getBasePrimaryColumn());
			TestCase.assertEquals(mediaDao.getTableName(),
					featureRelation.getRelatedTableName());
			TestCase.assertEquals(mediaDao.getTable().getPkColumn().getName(),
					featureRelation.getRelatedPrimaryColumn());
			TestCase.assertEquals(MediaTable.RELATION_TYPE.getName(),
					featureRelation.getRelationName());
			TestCase.assertEquals(mappingTableName,
					featureRelation.getMappingTableName());

			UserMappingDao userMappingDao = rte
					.getUserMappingDao(featureRelation);
			int totalMappedCount = userMappingDao.count();
			UserCustomResultSet mappingResultSet = userMappingDao.queryForAll();
			while (mappingResultSet.moveToNext()) {
				userMappingRow = userMappingDao.getRow(mappingResultSet);
				// TODO validate mapping rows
			}
			mappingResultSet.close();

			// TODO try to dynamically handle the relation table type

			featureResultSet = featureDao.queryForAll();
			int totalMapped = 0;
			while (featureResultSet.moveToNext()) {
				FeatureRow featureRow = featureResultSet.getRow();
				List<Long> mappedIds = rte.getMappingsForBase(featureRelation,
						featureRow.getId());
				totalMapped += mappedIds.size();
			}
			featureResultSet.close();
			TestCase.assertEquals(totalMappedCount, totalMapped);
		}

		TestCase.assertTrue(dao.deleteByIds(userMappingRow) > 0);

		rte.removeRelationship(extendedRelation);
		TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
		extendedRelations = rte.getRelationships();
		TestCase.assertEquals(0, extendedRelations.size());
		TestCase.assertFalse(geoPackage.getDatabase().tableExists(
				mappingTableName));

		rte.removeExtension();
		TestCase.assertFalse(rte.has());

	}

}
