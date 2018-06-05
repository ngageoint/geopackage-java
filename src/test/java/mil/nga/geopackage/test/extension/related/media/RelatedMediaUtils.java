package mil.nga.geopackage.test.extension.related.media;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.contents.Contents;
import mil.nga.geopackage.core.contents.ContentsDao;
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
import mil.nga.geopackage.test.geom.GeoPackageGeometryDataUtils;
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

		// Create a related tables extension
		RelatedTablesExtension rte = new RelatedTablesExtension(geoPackage);
		TestCase.assertFalse(rte.has());
		TestCase.assertTrue(rte.getRelationships().isEmpty());

		// Choose a random feature table
		List<String> featureTables = geoPackage.getFeatureTables();
		final String baseTableName = featureTables
				.get((int) (Math.random() * featureTables.size()));

		// Populate and validate a media table
		List<UserCustomColumn> additionalMediaColumns = RelatedTablesUtils
				.createAdditionalUserColumns(MediaTable.numRequiredColumns());
		MediaTable mediaTable = MediaTable.create("media_table",
				additionalMediaColumns);
		String[] mediaColumns = mediaTable.getColumnNames();
		TestCase.assertEquals(MediaTable.numRequiredColumns()
				+ additionalMediaColumns.size(), mediaColumns.length);
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

		// Create and validate a mapping table
		List<UserCustomColumn> additionalMappingColumns = RelatedTablesUtils
				.createAdditionalUserColumns(UserMappingTable
						.numRequiredColumns());
		final String mappingTableName = "features_media";
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

		// Create the media table, content row, and relationship between the
		// feature table and media table
		ContentsDao contentsDao = geoPackage.getContentsDao();
		TestCase.assertFalse(contentsDao.getTables().contains(
				mediaTable.getTableName()));
		ExtendedRelation extendedRelation = rte.createRelationship(
				baseTableName, mediaTable, userMappingTable);
		validateContents(mediaTable, mediaTable.getContents());
		TestCase.assertTrue(rte.has());
		TestCase.assertTrue(rte.has(userMappingTable.getTableName()));
		TestCase.assertNotNull(extendedRelation);
		List<ExtendedRelation> extendedRelations = rte.getRelationships();
		TestCase.assertEquals(1, extendedRelations.size());
		TestCase.assertTrue(geoPackage.getDatabase().tableExists(
				mappingTableName));
		TestCase.assertTrue(geoPackage.getDatabase().tableExists(
				mediaTable.getTableName()));
		TestCase.assertTrue(contentsDao.getTables().contains(
				mediaTable.getTableName()));
		validateContents(mediaTable,
				contentsDao.queryForId(mediaTable.getTableName()));

		// Validate the media DAO
		MediaDao mediaDao = rte.getMediaDao(mediaTable);
		TestCase.assertNotNull(mediaDao);
		mediaTable = mediaDao.getTable();
		TestCase.assertNotNull(mediaTable);
		validateContents(mediaTable, mediaTable.getContents());

		// Insert media table rows
		byte[] data = TestUtils.getTileBytes();
		String contentType = "image/png";
		int mediaCount = 2 + (int) (Math.random() * 9);
		for (int i = 0; i < mediaCount; i++) {
			MediaRow mediaRow = mediaDao.newRow();
			mediaRow.setData(data);
			mediaRow.setContentType(contentType);
			RelatedTablesUtils.populateUserRow(mediaTable, mediaRow,
					MediaTable.requiredColumns());
			TestCase.assertTrue(mediaDao.create(mediaRow) > 0);
		}
		TestCase.assertEquals(mediaCount, mediaDao.count());

		// Build the Feature ids
		FeatureDao featureDao = geoPackage.getFeatureDao(baseTableName);
		FeatureResultSet featureResultSet = featureDao.queryForAll();
		int featureCount = featureResultSet.getCount();
		List<Long> featureIds = new ArrayList<>();
		while (featureResultSet.moveToNext()) {
			featureIds.add(featureResultSet.getRow().getId());
		}
		featureResultSet.close();

		// Build the Media ids
		UserCustomResultSet mediaResultSet = mediaDao.queryForAll();
		mediaCount = mediaResultSet.getCount();
		List<Long> mediaIds = new ArrayList<>();
		while (mediaResultSet.moveToNext()) {
			mediaIds.add(mediaResultSet.getRow().getId());
		}
		mediaResultSet.close();

		// Insert user mapping rows between feature ids and media ids
		UserMappingDao dao = rte.getMappingDao(mappingTableName);
		UserMappingRow userMappingRow = null;
		for (int i = 0; i < 10; i++) {
			userMappingRow = dao.newRow();
			userMappingRow
					.setBaseId(featureIds.get((int) (Math.random() * featureCount)));
			userMappingRow
					.setRelatedId(mediaIds.get((int) (Math.random() * mediaCount)));
			RelatedTablesUtils.populateUserRow(userMappingTable,
					userMappingRow, UserMappingTable.requiredColumns());
			TestCase.assertTrue(dao.create(userMappingRow) > 0);
		}
		TestCase.assertEquals(10, dao.count());

		// Validate the user mapping rows
		userMappingTable = dao.getTable();
		String[] mappingColumns = userMappingTable.getColumnNames();
		UserCustomResultSet resultSet = dao.queryForAll();
		int count = resultSet.getCount();
		TestCase.assertEquals(10, count);
		int manualCount = 0;
		while (resultSet.moveToNext()) {

			UserMappingRow resultRow = dao.getRow(resultSet);
			TestCase.assertFalse(resultRow.hasId());
			TestCase.assertTrue(featureIds.contains(resultRow.getBaseId()));
			TestCase.assertTrue(mediaIds.contains(resultRow.getRelatedId()));
			RelatedTablesUtils.validateUserRow(mappingColumns, resultRow);

			manualCount++;
		}
		TestCase.assertEquals(count, manualCount);
		resultSet.close();

		ExtendedRelationsDao extendedRelationsDao = rte
				.getExtendedRelationsDao();

		// Get the relations starting from the feature table
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

		// Test the feature table relations
		for (ExtendedRelation featureRelation : featureExtendedRelations) {

			// Test the relation
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

			// Test the user mappings from the relation
			UserMappingDao userMappingDao = rte.getMappingDao(featureRelation);
			int totalMappedCount = userMappingDao.count();
			UserCustomResultSet mappingResultSet = userMappingDao.queryForAll();
			while (mappingResultSet.moveToNext()) {
				userMappingRow = userMappingDao.getRow(mappingResultSet);
				TestCase.assertTrue(featureIds.contains(userMappingRow
						.getBaseId()));
				TestCase.assertTrue(mediaIds.contains(userMappingRow
						.getRelatedId()));
				RelatedTablesUtils.validateUserRow(mappingColumns,
						userMappingRow);
			}
			mappingResultSet.close();

			// Get and test the media DAO
			mediaDao = rte.getMediaDao(featureRelation);
			TestCase.assertNotNull(mediaDao);
			mediaTable = mediaDao.getTable();
			TestCase.assertNotNull(mediaTable);
			validateContents(mediaTable, mediaTable.getContents());

			// Get and test the Media Rows mapped to each Feature Row
			featureResultSet = featureDao.queryForAll();
			int totalMapped = 0;
			while (featureResultSet.moveToNext()) {
				FeatureRow featureRow = featureResultSet.getRow();
				List<Long> mappedIds = rte.getMappingsForBase(featureRelation,
						featureRow.getId());
				List<MediaRow> mediaRows = mediaDao.getRows(mappedIds);
				TestCase.assertEquals(mappedIds.size(), mediaRows.size());

				for (MediaRow mediaRow : mediaRows) {
					TestCase.assertTrue(mediaRow.hasId());
					TestCase.assertTrue(mediaRow.getId() >= 0);
					TestCase.assertTrue(mediaIds.contains(mediaRow.getId()));
					TestCase.assertTrue(mappedIds.contains(mediaRow.getId()));
					GeoPackageGeometryDataUtils.compareByteArrays(data,
							mediaRow.getData());
					TestCase.assertEquals(contentType,
							mediaRow.getContentType());
					RelatedTablesUtils.validateUserRow(mediaColumns, mediaRow);
				}

				totalMapped += mappedIds.size();
			}
			featureResultSet.close();
			TestCase.assertEquals(totalMappedCount, totalMapped);
		}

		// Delete a single mapping
		int countOfIds = dao.countByIds(userMappingRow);
		TestCase.assertEquals(countOfIds, dao.deleteByIds(userMappingRow));
		TestCase.assertEquals(10 - countOfIds, dao.count());

		// Delete the relationship and user mapping table
		rte.removeRelationship(extendedRelation);
		TestCase.assertFalse(rte.has(userMappingTable.getTableName()));
		extendedRelations = rte.getRelationships();
		TestCase.assertEquals(0, extendedRelations.size());
		TestCase.assertFalse(geoPackage.getDatabase().tableExists(
				mappingTableName));

		// Delete the media table and contents row
		TestCase.assertTrue(geoPackage.isTable(mediaTable.getTableName()));
		TestCase.assertNotNull(contentsDao.queryForId(mediaTable.getTableName()));
		geoPackage.deleteTable(mediaTable.getTableName());
		TestCase.assertFalse(geoPackage.isTable(mediaTable.getTableName()));
		TestCase.assertNull(contentsDao.queryForId(mediaTable.getTableName()));

		// Delete the related tables extension
		rte.removeExtension();
		TestCase.assertFalse(rte.has());

	}

	/**
	 * Validate contents
	 * 
	 * @param mediaTable
	 *            media table
	 * @param contents
	 *            contents
	 */
	private static void validateContents(MediaTable mediaTable,
			Contents contents) {
		TestCase.assertNotNull(contents);
		TestCase.assertNull(contents.getDataType());
		TestCase.assertEquals(MediaTable.RELATION_TYPE.getName(),
				contents.getDataTypeString());
		TestCase.assertEquals(mediaTable.getTableName(),
				contents.getTableName());
		TestCase.assertNotNull(contents.getLastChange());
	}

}
