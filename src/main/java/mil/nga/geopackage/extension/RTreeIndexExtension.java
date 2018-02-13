package mil.nga.geopackage.extension;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageConnection;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.wkb.geom.Geometry;
import mil.nga.wkb.geom.GeometryEnvelope;
import mil.nga.wkb.util.GeometryEnvelopeBuilder;

import org.sqlite.Function;

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
	 * GeoPackage connection
	 */
	private GeoPackageConnection connection;

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public RTreeIndexExtension(GeoPackage geoPackage) {
		super(geoPackage);
		connection = geoPackage.getConnection();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createMinXFunction() {
		createFunction(MIN_X_FUNCTION, new GeometryFunction() {
			@Override
			public Object execute(GeoPackageGeometryData data) {
				return getEnvelope(data).getMinX();
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
				return getEnvelope(data).getMaxX();
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
				return getEnvelope(data).getMinY();
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
				return getEnvelope(data).getMaxY();
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
	 * Get or build a geometry envelope from the Geometry Data
	 * 
	 * @param data
	 *            geometry data
	 * @return geometry envelope
	 */
	private GeometryEnvelope getEnvelope(GeoPackageGeometryData data) {
		GeometryEnvelope envelope = null;
		if (data != null) {
			envelope = data.getEnvelope();
			if (envelope == null) {
				Geometry geometry = data.getGeometry();
				if (geometry != null) {
					envelope = GeometryEnvelopeBuilder.buildEnvelope(geometry);
				}
			}
		}
		if (envelope == null) {
			envelope = new GeometryEnvelope();
		}
		return envelope;
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
			Function.create(connection.getConnection(), name, function);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to create function: " + name, e);
		}
	}

}
