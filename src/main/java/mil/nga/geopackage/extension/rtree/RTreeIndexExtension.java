package mil.nga.geopackage.extension.rtree;

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
 * <p>
 * <a href=
 * "https://www.geopackage.org/spec/#extension_rtree">https://www.geopackage.org/spec/#extension_rtree</a>
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
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param geodesic
	 *            index using geodesic bounds
	 * @since 6.6.5
	 */
	public RTreeIndexExtension(GeoPackage geoPackage, boolean geodesic) {
		super(geoPackage, geodesic);
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
		createFunction(new GeometryFunction(MIN_X_FUNCTION) {
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
		createFunction(new GeometryFunction(MAX_X_FUNCTION) {
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
		createFunction(new GeometryFunction(MIN_Y_FUNCTION) {
			@Override
			public Object execute(GeoPackageGeometryData data) {
				Object value = null;
				GeometryEnvelope envelope = getEnvelope(data);
				if (envelope != null) {
					int srsId = data.getSrsId();
					if (srsId > 0) {
						envelope = geodesicEnvelope(envelope, srsId);
					}
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
		createFunction(new GeometryFunction(MAX_Y_FUNCTION) {
			@Override
			public Object execute(GeoPackageGeometryData data) {
				Object value = null;
				GeometryEnvelope envelope = getEnvelope(data);
				if (envelope != null) {
					int srsId = data.getSrsId();
					if (srsId > 0) {
						envelope = geodesicEnvelope(envelope, srsId);
					}
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
		createFunction(new GeometryFunction(IS_EMPTY_FUNCTION) {
			@Override
			public Object execute(GeoPackageGeometryData data) {
				Object value = null;
				if (data != null) {
					if (data.isEmpty() || data.getGeometry() == null) {
						value = 1;
					} else {
						value = 0;
					}
				}
				return value;
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
	private void createFunction(GeometryFunction function) {
		try {
			Function.create(getGeoPackage().getConnection().getConnection(),
					function.getName(), function);
		} catch (SQLException e) {
			log.log(Level.SEVERE,
					"Failed to create function: " + function.getName(), e);
		}
	}

}
