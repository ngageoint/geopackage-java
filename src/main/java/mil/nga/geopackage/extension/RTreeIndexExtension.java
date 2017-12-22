package mil.nga.geopackage.extension;

import java.io.IOException;
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

public class RTreeIndexExtension extends RTreeIndexCoreExtension {

	/**
	 * Logger
	 */
	private static final Logger log = Logger
			.getLogger(RTreeIndexExtension.class.getName());

	private GeoPackageConnection connection;

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * 
	 */
	public RTreeIndexExtension(GeoPackage geoPackage) {
		super(geoPackage);
		connection = geoPackage.getConnection();
	}

	@Override
	protected void createMinXFunction(String name) {
		createFunction(name, new GeometryFunction() {
			@Override
			public Object execute(GeoPackageGeometryData data)
					throws IOException {
				GeometryEnvelope envelope = getEnvelope(data);
				return envelope.getMinX();
			}
		});
	}

	@Override
	protected void createMaxXFunction(String name) {
		createFunction(name, new GeometryFunction() {
			@Override
			public Object execute(GeoPackageGeometryData data)
					throws IOException {
				GeometryEnvelope envelope = getEnvelope(data);
				return envelope.getMaxX();
			}
		});
	}

	@Override
	protected void createMinYFunction(String name) {
		createFunction(name, new GeometryFunction() {
			@Override
			public Object execute(GeoPackageGeometryData data)
					throws IOException {
				GeometryEnvelope envelope = getEnvelope(data);
				return envelope.getMinY();
			}
		});
	}

	@Override
	protected void createMaxYFunction(String name) {
		createFunction(name, new GeometryFunction() {
			@Override
			public Object execute(GeoPackageGeometryData data)
					throws IOException {
				GeometryEnvelope envelope = getEnvelope(data);
				return envelope.getMaxY();
			}
		});
	}

	@Override
	protected void createIsEmptyFunction(String name) {
		createFunction(name, new GeometryFunction() {
			@Override
			public Object execute(GeoPackageGeometryData data)
					throws IOException {
				return data == null || data.isEmpty()
						|| data.getGeometry() == null;
			}
		});
	}

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

	private void createFunction(String name, GeometryFunction function) {
		try {
			Function.create(connection.getConnection(), name, function);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to create function: " + name, e);
		}
	}

}
