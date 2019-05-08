package mil.nga.geopackage.extension;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sqlite.Function;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.user.custom.UserCustomDao;
import mil.nga.geopackage.user.custom.UserCustomTable;
import mil.nga.sf.GeometryEnvelope;

/**
 * RTree Index Extension
 * 
 * @author osbornb
 * @since 2.0.1
 */
public class RTreeIndexExtension extends RTreeIndexCoreExtension {

	/**
	 * Logger
	 */
	private static final Logger log = Logger
			.getLogger(RTreeIndexExtension.class.getName());

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public RTreeIndexExtension(GeoPackage geoPackage) {
		super(geoPackage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GeoPackage getGeoPackage() {
		return (GeoPackage) super.getGeoPackage();
	}

	/**
	 * Get a RTree Index Table DAO for the feature table
	 * 
	 * @param featureTable
	 *            feature table
	 * @return RTree Index Table DAO
	 * @since 3.1.0
	 */
	public RTreeIndexTableDao getTableDao(String featureTable) {
		return getTableDao(getGeoPackage().getFeatureDao(featureTable));
	}

	/**
	 * Get a RTree Index Table DAO for the feature dao
	 * 
	 * @param featureDao
	 *            feature DAO
	 * @return RTree Index Table DAO
	 * @since 3.1.0
	 */
	public RTreeIndexTableDao getTableDao(FeatureDao featureDao) {

		GeoPackageConnection connection = getGeoPackage().getConnection();
		UserCustomTable userCustomTable = getRTreeTable(featureDao.getTable());
		UserCustomDao userCustomDao = new UserCustomDao(geoPackage.getName(),
				connection, userCustomTable);

		return new RTreeIndexTableDao(this, userCustomDao, featureDao);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createMinXFunction() {
		createFunction(MIN_X_FUNCTION, new GeometryFunction() {
			@Override
			public Object execute(GeoPackageGeometryData data) {
				Object value = null;
				GeometryEnvelope envelope = getEnvelope(data);
				if (envelope != null) {
					value = envelope.getMinX();
				}
				return value;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createMaxXFunction() {
		createFunction(MAX_X_FUNCTION, new GeometryFunction() {
			@Override
			public Object execute(GeoPackageGeometryData data) {
				Object value = null;
				GeometryEnvelope envelope = getEnvelope(data);
				if (envelope != null) {
					value = envelope.getMaxX();
				}
				return value;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createMinYFunction() {
		createFunction(MIN_Y_FUNCTION, new GeometryFunction() {
			@Override
			public Object execute(GeoPackageGeometryData data) {
				Object value = null;
				GeometryEnvelope envelope = getEnvelope(data);
				if (envelope != null) {
					value = envelope.getMinY();
				}
				return value;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createMaxYFunction() {
		createFunction(MAX_Y_FUNCTION, new GeometryFunction() {
			@Override
			public Object execute(GeoPackageGeometryData data) {
				Object value = null;
				GeometryEnvelope envelope = getEnvelope(data);
				if (envelope != null) {
					value = envelope.getMaxY();
				}
				return value;
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createIsEmptyFunction() {
		createFunction(IS_EMPTY_FUNCTION, new GeometryFunction() {
			@Override
			public Object execute(GeoPackageGeometryData data) {
				return data == null || data.isEmpty()
						|| data.getGeometry() == null;
			}
		});
	}

	/**
	 * Create the function for the connection
	 * 
	 * @param name
	 *            function name
	 * @param function
	 *            geometry function
	 */
	private void createFunction(String name, GeometryFunction function) {
		try {
			Function.create(getGeoPackage().getConnection().getConnection(),
					name, function);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to create function: " + name, e);
		}
	}

}
