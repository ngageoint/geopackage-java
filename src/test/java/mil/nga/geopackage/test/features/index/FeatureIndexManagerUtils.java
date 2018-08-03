package mil.nga.geopackage.test.features.index;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.core.srs.SpatialReferenceSystem;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.geopackage.features.index.FeatureIndexResults;
import mil.nga.geopackage.features.index.FeatureIndexType;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.schema.TableColumnKey;
import mil.nga.geopackage.test.TestUtils;
import mil.nga.geopackage.test.io.TestGeoPackageProgress;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.GeometryType;
import mil.nga.sf.Point;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;
import mil.nga.sf.proj.ProjectionTransform;
import mil.nga.sf.util.GeometryEnvelopeBuilder;

/**
 * Feature Index Manager Utility test methods
 *
 * @author osbornb
 */
public class FeatureIndexManagerUtils {

	/**
	 * Test index
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @throws SQLException
	 *             upon error
	 */
	public static void testIndex(GeoPackage geoPackage) throws SQLException {
		testIndex(geoPackage, FeatureIndexType.GEOPACKAGE, false);
		testIndex(geoPackage, FeatureIndexType.RTREE, true);
	}

	private static void testIndex(GeoPackage geoPackage, FeatureIndexType type,
			boolean includeEmpty) throws SQLException {

		// Test indexing each feature table
		List<String> featureTables = geoPackage.getFeatureTables();
		for (String featureTable : featureTables) {

			FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
			FeatureIndexManager featureIndexManager = new FeatureIndexManager(
					geoPackage, featureDao);
			featureIndexManager.setIndexLocation(type);
			featureIndexManager.deleteAllIndexes();

			// Determine how many features have geometry envelopes or geometries
			int expectedCount = 0;
			FeatureRow testFeatureRow = null;
			FeatureResultSet featureResultSet = featureDao.queryForAll();
			while (featureResultSet.moveToNext()) {
				FeatureRow featureRow = featureResultSet.getRow();
				if (featureRow.getGeometryEnvelope() != null) {
					expectedCount++;
					// Randomly choose a feature row with Geometry for testing
					// queries later
					if (testFeatureRow == null) {
						testFeatureRow = featureRow;
					} else if (Math.random() < (1.0 / featureResultSet
							.getCount())) {
						testFeatureRow = featureRow;
					}
				} else if (includeEmpty) {
					expectedCount++;
				}
			}
			featureResultSet.close();

			TestCase.assertFalse(featureIndexManager.isIndexed());
			TestCase.assertNull(featureIndexManager.getLastIndexed());
			Date currentDate = new Date();

			// Test indexing
			TestGeoPackageProgress progress = new TestGeoPackageProgress();
			featureIndexManager.setProgress(progress);
			int indexCount = featureIndexManager.index();
			TestCase.assertEquals(expectedCount, indexCount);
			TestCase.assertEquals(featureDao.count(), progress.getProgress());
			TestCase.assertNotNull(featureIndexManager.getLastIndexed());
			Date lastIndexed = featureIndexManager.getLastIndexed();
			TestCase.assertTrue(lastIndexed.getTime() > currentDate.getTime());

			TestCase.assertTrue(featureIndexManager.isIndexed());
			TestCase.assertEquals(expectedCount, featureIndexManager.count());

			// Test re-indexing, both ignored and forced
			TestCase.assertEquals(0, featureIndexManager.index());
			TestCase.assertEquals(expectedCount,
					featureIndexManager.index(true));
			TestCase.assertTrue(featureIndexManager.getLastIndexed().getTime() > lastIndexed
					.getTime());

			// Query for all indexed geometries
			int resultCount = 0;
			FeatureIndexResults featureIndexResults = featureIndexManager
					.query();
			for (FeatureRow featureRow : featureIndexResults) {
				validateFeatureRow(featureIndexManager, featureRow, null,
						includeEmpty);
				resultCount++;
			}
			featureIndexResults.close();
			TestCase.assertEquals(expectedCount, resultCount);

			// Test the query by envelope
			GeometryEnvelope envelope = testFeatureRow.getGeometryEnvelope();
			final double difference = .000001;
			envelope.setMinX(envelope.getMinX() - difference);
			envelope.setMaxX(envelope.getMaxX() + difference);
			envelope.setMinY(envelope.getMinY() - difference);
			envelope.setMaxY(envelope.getMaxY() + difference);
			if (envelope.hasZ()) {
				envelope.setMinZ(envelope.getMinZ() - difference);
				envelope.setMaxZ(envelope.getMaxZ() + difference);
			}
			if (envelope.hasM()) {
				envelope.setMinM(envelope.getMinM() - difference);
				envelope.setMaxM(envelope.getMaxM() + difference);
			}
			resultCount = 0;
			boolean featureFound = false;
			TestCase.assertTrue(featureIndexManager.count(envelope) >= 1);
			featureIndexResults = featureIndexManager.query(envelope);
			for (FeatureRow featureRow : featureIndexResults) {
				validateFeatureRow(featureIndexManager, featureRow, envelope,
						includeEmpty);
				if (featureRow.getId() == testFeatureRow.getId()) {
					featureFound = true;
				}
				resultCount++;
			}
			featureIndexResults.close();
			TestCase.assertTrue(featureFound);
			TestCase.assertTrue(resultCount >= 1);

			// Pick a projection different from the feature dao and project the
			// bounding box
			BoundingBox boundingBox = new BoundingBox(envelope.getMinX() - 1,
					envelope.getMinY() - 1, envelope.getMaxX() + 1,
					envelope.getMaxY() + 1);
			Projection projection = null;
			if (!featureDao.getProjection().equals(
					ProjectionConstants.AUTHORITY_EPSG,
					ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM)) {
				projection = ProjectionFactory
						.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
			} else {
				projection = ProjectionFactory
						.getProjection(ProjectionConstants.EPSG_WEB_MERCATOR);
			}
			ProjectionTransform transform = featureDao.getProjection()
					.getTransformation(projection);
			BoundingBox transformedBoundingBox = boundingBox
					.transform(transform);

			// Test the query by projected bounding box
			resultCount = 0;
			featureFound = false;
			TestCase.assertTrue(featureIndexManager.count(
					transformedBoundingBox, projection) >= 1);
			featureIndexResults = featureIndexManager.query(
					transformedBoundingBox, projection);
			for (FeatureRow featureRow : featureIndexResults) {
				validateFeatureRow(featureIndexManager, featureRow,
						boundingBox.buildEnvelope(), includeEmpty);
				if (featureRow.getId() == testFeatureRow.getId()) {
					featureFound = true;
				}
				resultCount++;
			}
			featureIndexResults.close();
			TestCase.assertTrue(featureFound);
			TestCase.assertTrue(resultCount >= 1);

			// Update a Geometry and update the index of a single feature row
			GeoPackageGeometryData geometryData = new GeoPackageGeometryData(
					featureDao.getGeometryColumns().getSrsId());
			Point point = new Point(5, 5);
			geometryData.setGeometry(point);
			testFeatureRow.setGeometry(geometryData);
			TestCase.assertEquals(1, featureDao.update(testFeatureRow));
			Date lastIndexedBefore = featureIndexManager.getLastIndexed();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
			TestCase.assertTrue(featureIndexManager.index(testFeatureRow));
			Date lastIndexedAfter = featureIndexManager.getLastIndexed();
			TestCase.assertTrue(lastIndexedAfter.after(lastIndexedBefore));

			// Verify the index was updated for the feature row
			envelope = GeometryEnvelopeBuilder.buildEnvelope(point);
			resultCount = 0;
			featureFound = false;
			TestCase.assertTrue(featureIndexManager.count(envelope) >= 1);
			featureIndexResults = featureIndexManager.query(envelope);
			for (FeatureRow featureRow : featureIndexResults) {
				validateFeatureRow(featureIndexManager, featureRow, envelope,
						includeEmpty);
				if (featureRow.getId() == testFeatureRow.getId()) {
					featureFound = true;
				}
				resultCount++;
			}
			featureIndexResults.close();
			TestCase.assertTrue(featureFound);
			TestCase.assertTrue(resultCount >= 1);

			featureIndexManager.close();
		}

		// Delete the extensions
		boolean everyOther = false;
		for (String featureTable : featureTables) {
			FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
			FeatureIndexManager featureIndexManager = new FeatureIndexManager(
					geoPackage, featureDao);
			featureIndexManager.setIndexLocation(type);
			TestCase.assertTrue(featureIndexManager.isIndexed());

			// Test deleting a single geometry index
			if (everyOther) {
				FeatureResultSet featureResultSet = featureDao.queryForAll();
				while (featureResultSet.moveToNext()) {
					FeatureRow featureRow = featureResultSet.getRow();
					if (featureRow.getGeometryEnvelope() != null) {
						featureResultSet.close();
						TestCase.assertTrue(featureIndexManager
								.deleteIndex(featureRow));
						break;
					}
				}
				featureResultSet.close();
			}

			featureIndexManager.deleteIndex();

			TestCase.assertFalse(featureIndexManager.isIndexed());
			everyOther = !everyOther;

			featureIndexManager.close();
		}

	}

	/**
	 * Validate a Feature Row result
	 *
	 * @param featureIndexManager
	 * @param featureRow
	 * @param queryEnvelope
	 */
	private static void validateFeatureRow(
			FeatureIndexManager featureIndexManager, FeatureRow featureRow,
			GeometryEnvelope queryEnvelope, boolean includeEmpty) {
		TestCase.assertNotNull(featureRow);
		GeometryEnvelope envelope = featureRow.getGeometryEnvelope();

		if (!includeEmpty) {
			TestCase.assertNotNull(envelope);

			if (queryEnvelope != null) {
				TestCase.assertTrue(envelope.getMinX() <= queryEnvelope
						.getMaxX());
				TestCase.assertTrue(envelope.getMaxX() >= queryEnvelope
						.getMinX());
				TestCase.assertTrue(envelope.getMinY() <= queryEnvelope
						.getMaxY());
				TestCase.assertTrue(envelope.getMaxY() >= queryEnvelope
						.getMinY());
				if (envelope.isHasZ()) {
					if (queryEnvelope.hasZ()) {
						TestCase.assertTrue(envelope.getMinZ() <= queryEnvelope
								.getMaxZ());
						TestCase.assertTrue(envelope.getMaxZ() >= queryEnvelope
								.getMinZ());
					}
				}
				if (envelope.isHasM()) {
					if (queryEnvelope.hasM()) {
						TestCase.assertTrue(envelope.getMinM() <= queryEnvelope
								.getMaxM());
						TestCase.assertTrue(envelope.getMaxM() >= queryEnvelope
								.getMinM());
					}
				}
			}
		}
	}

	/**
	 * Test read
	 *
	 * @param geoPackage
	 *            GeoPackage
	 * @throws SQLException
	 *             upon error
	 */
	public static void testLargeIndex(GeoPackage geoPackage)
			throws SQLException {

		GeometryColumns geometryColumns = new GeometryColumns();
		geometryColumns.setId(new TableColumnKey("large_index", "geom"));
		geometryColumns.setGeometryType(GeometryType.POLYGON);
		geometryColumns.setZ((byte) 0);
		geometryColumns.setM((byte) 0);

		BoundingBox boundingBox = new BoundingBox(-180, -90, 180, 90);

		SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
				.getOrCreateCode(ProjectionConstants.AUTHORITY_EPSG,
						ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
		geometryColumns = geoPackage.createFeatureTableWithMetadata(
				geometryColumns, boundingBox, srs.getId());

		FeatureDao featureDao = geoPackage.getFeatureDao(geometryColumns);

		int numFeatures = 5000; // TODO
		System.out.println("Features: " + numFeatures);
		TestUtils.addRowsToFeatureTable(geoPackage, geometryColumns,
				featureDao.getTable(), numFeatures, false, false, false);

		GeometryEnvelope envelope = null;
		FeatureResultSet resultSet = featureDao.queryForAll();
		while (resultSet.moveToNext()) {
			FeatureRow featureRow = resultSet.getRow();
			GeometryEnvelope rowEnvelope = featureRow.getGeometryEnvelope();
			if (envelope == null) {
				envelope = rowEnvelope;
			} else if (rowEnvelope != null) {
				envelope = envelope.union(rowEnvelope);
			}
		}
		resultSet.close();

		testLargeIndex(geoPackage, FeatureIndexType.GEOPACKAGE, featureDao,
				envelope);
		testLargeIndex(geoPackage, FeatureIndexType.RTREE, featureDao, envelope);
	}

	private static void testLargeIndex(GeoPackage geoPackage,
			FeatureIndexType type, FeatureDao featureDao,
			GeometryEnvelope envelope) {

		System.out.println();
		System.out.println("-------------------------------------");
		System.out.println("Type: " + type);
		System.out.println("-------------------------------------");
		System.out.println();

		int featureCount = featureDao.count();

		FeatureIndexManager featureIndexManager = new FeatureIndexManager(
				geoPackage, featureDao);
		featureIndexManager.setIndexLocation(type);
		featureIndexManager.deleteAllIndexes();

		Date before = new Date();
		int indexCount = featureIndexManager.index();
		System.out.println("Index: "
				+ (new Date().getTime() - before.getTime()) + " ms");
		TestCase.assertEquals(featureCount, indexCount);

		TestCase.assertTrue(featureIndexManager.isIndexed());
		before = new Date();
		TestCase.assertEquals(featureCount, featureIndexManager.count());
		System.out.println("Count: "
				+ (new Date().getTime() - before.getTime()) + " ms");

		before = new Date();
		long fullCount = featureIndexManager.count(envelope);
		System.out.println("Full Envelope Count: "
				+ (new Date().getTime() - before.getTime()) + " ms");
		TestCase.assertEquals(featureCount, fullCount);

		before = new Date();
		FeatureIndexResults results = featureIndexManager.query(envelope);
		System.out.println("Full Envelope Query: "
				+ (new Date().getTime() - before.getTime()) + " ms");
		TestCase.assertEquals(featureCount, results.count());
		results.close();

		// TODO

	}

}
