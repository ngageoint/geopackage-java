package mil.nga.geopackage.test;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.TableColumnKey;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.sf.Geometry;
import mil.nga.sf.LineString;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.util.GeometryEnvelopeBuilder;

/**
 * For testing performance of feature inserts through duration logging and
 * profiling
 * 
 * @author osbornb
 */
public class GeoPackagePerformance {

	private static final Logger LOGGER = Logger
			.getLogger(GeoPackagePerformance.class.getName());
	private static final String GEOPACKAGE_FILE = "performance.gpkg";
	private static final String TABLE_NAME = "features";
	private static final String COLUMN_NAME = "geom";
	private static final int CREATE_COUNT = 50000000;
	private static final boolean AUTO_COMMIT = false;
	private static final int COMMIT_CHUNK = 1000;
	private static final int LOG_CHUNK = 1000000;

	/**
	 * Test feature inserts
	 * 
	 * @param args
	 *            arguments
	 * @throws SQLException
	 *             upon failure
	 */
	public static void main(String[] args) throws SQLException {

		File file = new File(GEOPACKAGE_FILE);
		if (file.exists()) {
			file.delete();
		}

		LOGGER.log(Level.INFO, "File: " + file.getAbsolutePath());
		LOGGER.log(Level.INFO, "Table Name: " + TABLE_NAME);
		LOGGER.log(Level.INFO, "Column Name: " + COLUMN_NAME);
		LOGGER.log(Level.INFO, "Features: " + CREATE_COUNT);
		LOGGER.log(Level.INFO, "Auto Commit: " + AUTO_COMMIT);
		if (!AUTO_COMMIT) {
			LOGGER.log(Level.INFO, "Commit Chunk: " + COMMIT_CHUNK);
		}
		if (LOG_CHUNK > 0) {
			LOGGER.log(Level.INFO, "Log Chunk: " + LOG_CHUNK);
		}

		GeoPackageManager.create(file);

		GeoPackage geoPackage = GeoPackageManager.open(file);

		Geometry geometry = createGeometry();

		GeometryColumns geometryColumns = new GeometryColumns();
		geometryColumns.setId(new TableColumnKey(TABLE_NAME, COLUMN_NAME));
		geometryColumns.setGeometryType(geometry.getGeometryType());
		geometryColumns.setZ((byte) 0);
		geometryColumns.setM((byte) 0);

		BoundingBox boundingBox = new BoundingBox(
				GeometryEnvelopeBuilder.buildEnvelope(geometry));

		SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
				.getOrCreateCode(ProjectionConstants.AUTHORITY_EPSG,
						ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		geoPackage.createFeatureTableWithMetadata(geometryColumns, boundingBox,
				srs.getId());

		GeoPackageGeometryData geometryData = new GeoPackageGeometryData(
				srs.getSrsId());
		geometryData.setGeometry(geometry);

		FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);

		if (AUTO_COMMIT) {
			dao.getConnection().setAutoCommit(true);
		} else {
			dao.beginTransaction();
		}
		// Connection connection = dao.getConnection();
		// connection.setAutoCommit(AUTO_COMMIT);

		try {

			Instant startTime = Instant.now();
			Instant logTime = Instant.now();

			for (int count = 1; count <= CREATE_COUNT; count++) {

				FeatureRow newRow = dao.newRow();
				newRow.setGeometry(geometryData);

				dao.create(newRow);

				if (!AUTO_COMMIT && count % COMMIT_CHUNK == 0) {
					dao.commit();
				}

				if (LOG_CHUNK > 0 && count % LOG_CHUNK == 0) {
					Instant time = Instant.now();
					LOGGER.log(Level.INFO, "Total Count: " + count);
					Duration duration = Duration.between(logTime, time);
					LOGGER.log(Level.INFO, "Chunk Time: "
							+ duration.toString().substring(2));
					LOGGER.log(Level.INFO,
							"Chunk Average: "
									+ (duration.toMillis() / (float) LOG_CHUNK)
									+ " ms");
					Duration totalDuration = Duration.between(startTime, time);
					LOGGER.log(Level.INFO, "Total Time: "
							+ totalDuration.toString().substring(2));
					LOGGER.log(
							Level.INFO,
							"Feature Average: "
									+ (totalDuration.toMillis() / (float) count)
									+ " ms");
					logTime = time;
				}

			}

			if (!AUTO_COMMIT) {
				dao.endTransaction();
			}

		} catch (Exception e) {
			if (!AUTO_COMMIT) {
				dao.failTransaction();
			}
			throw e;
		}

		geoPackage.close();

		geoPackage = GeoPackageManager.open(file);
		dao = geoPackage.getFeatureDao(TABLE_NAME);
		LOGGER.log(Level.INFO, "Final Count: " + dao.count());
		geoPackage.close();

	}

	private static Geometry createGeometry() {

		Polygon polygon = new Polygon();
		LineString ring = new LineString();
		ring.addPoint(new Point(-104.802246, 39.720343));
		ring.addPoint(new Point(-104.802246, 39.719753));
		ring.addPoint(new Point(-104.802183, 39.719754));
		ring.addPoint(new Point(-104.802184, 39.719719));
		ring.addPoint(new Point(-104.802138, 39.719694));
		ring.addPoint(new Point(-104.802097, 39.719691));
		ring.addPoint(new Point(-104.802096, 39.719648));
		ring.addPoint(new Point(-104.801646, 39.719648));
		ring.addPoint(new Point(-104.801644, 39.719722));
		ring.addPoint(new Point(-104.801550, 39.719723));
		ring.addPoint(new Point(-104.801549, 39.720207));
		ring.addPoint(new Point(-104.801648, 39.720207));
		ring.addPoint(new Point(-104.801648, 39.720341));
		ring.addPoint(new Point(-104.802246, 39.720343));
		polygon.addRing(ring);

		return polygon;
	}

}
