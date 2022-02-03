package mil.nga.geopackage;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.db.TableColumnKey;
import mil.nga.geopackage.db.master.SQLiteMaster;
import mil.nga.geopackage.db.master.SQLiteMasterType;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTableMetadata;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.proj.ProjectionConstants;
import mil.nga.sf.Geometry;
import mil.nga.sf.LineString;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;

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
	private static final boolean CHUNK_LOGGING = true;

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
		if (CHUNK_LOGGING) {
			LOGGER.log(Level.INFO, "Log Chunk: " + LOG_CHUNK);
		}

		GeoPackageManager.create(file);

		GeoPackage geoPackage = GeoPackageManager.open(file);

		Geometry geometry = createGeometry();

		SpatialReferenceSystem srs = geoPackage.getSpatialReferenceSystemDao()
				.getOrCreateCode(ProjectionConstants.AUTHORITY_EPSG,
						ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);

		GeometryColumns geometryColumns = new GeometryColumns();
		geometryColumns.setId(new TableColumnKey(TABLE_NAME, COLUMN_NAME));
		geometryColumns.setGeometryType(geometry.getGeometryType());
		geometryColumns.setZ((byte) 0);
		geometryColumns.setM((byte) 0);
		geometryColumns.setSrs(srs);

		BoundingBox boundingBox = new BoundingBox(geometry);

		geoPackage.createFeatureTable(
				FeatureTableMetadata.create(geometryColumns, boundingBox));

		SQLiteMaster table = SQLiteMaster.queryByType(geoPackage.getDatabase(),
				SQLiteMasterType.TABLE, TABLE_NAME);
		LOGGER.log(Level.INFO, table.getSql(0));

		GeoPackageGeometryData geometryData = GeoPackageGeometryData
				.create(srs.getSrsId(), geometry);

		FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);

		if (AUTO_COMMIT) {
			dao.getConnection().setAutoCommit(true);
		} else {
			dao.beginTransaction();
		}
		// Connection connection = dao.getConnection();
		// connection.setAutoCommit(AUTO_COMMIT);

		int count = 1;

		Duration chunkInsertDuration = null;
		Instant chunkStartTime = null;

		Duration insertDuration = Duration.ofMillis(0);
		Instant startTime = Instant.now();

		if (CHUNK_LOGGING) {
			chunkInsertDuration = insertDuration;
			chunkStartTime = startTime;
		}

		try {

			for (; count <= CREATE_COUNT; count++) {

				FeatureRow newRow = dao.newRow();
				newRow.setGeometry(geometryData);

				Instant beforeInsert = Instant.now();
				dao.create(newRow);
				Duration insert = Duration.between(beforeInsert, Instant.now());
				insertDuration = insertDuration.plus(insert);
				if (CHUNK_LOGGING) {
					chunkInsertDuration = chunkInsertDuration.plus(insert);
				}

				if (!AUTO_COMMIT && count % COMMIT_CHUNK == 0) {
					beforeInsert = Instant.now();
					dao.commit();
					insert = Duration.between(beforeInsert, Instant.now());
					insertDuration = insertDuration.plus(insert);
					if (CHUNK_LOGGING) {
						chunkInsertDuration = chunkInsertDuration.plus(insert);
					}
				}

				if (CHUNK_LOGGING && count % LOG_CHUNK == 0) {
					Instant time = Instant.now();
					LOGGER.log(Level.INFO, "Total Count: " + count);
					Duration duration = Duration.between(chunkStartTime, time);
					LOGGER.log(Level.INFO,
							"Chunk Time: " + duration.toString().substring(2));
					LOGGER.log(Level.INFO,
							"Chunk Average: "
									+ (duration.toMillis() / (float) LOG_CHUNK)
									+ " ms");
					LOGGER.log(Level.INFO,
							"Chunk Insert Average: "
									+ (chunkInsertDuration.toMillis()
											/ (float) LOG_CHUNK)
									+ " ms");
					Duration totalDuration = Duration.between(startTime, time);
					LOGGER.log(Level.INFO, "Total Time: "
							+ totalDuration.toString().substring(2));
					LOGGER.log(Level.INFO,
							"Feature Average: "
									+ (totalDuration.toMillis() / (float) count)
									+ " ms");
					LOGGER.log(Level.INFO, "Feature Insert Average: "
							+ (insertDuration.toMillis() / (float) count)
							+ " ms");
					chunkInsertDuration = Duration.ofMillis(0);
					chunkStartTime = time;
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

		Duration totalDuration = Duration.between(startTime, Instant.now());
		LOGGER.log(Level.INFO,
				"Final Total Time: " + totalDuration.toString().substring(2));
		LOGGER.log(Level.INFO, "Final Feature Average: "
				+ (totalDuration.toMillis() / (float) count) + " ms");
		LOGGER.log(Level.INFO, "Final Feature Insert Average: "
				+ (insertDuration.toMillis() / (float) count) + " ms");

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
