package mil.nga.geopackage.extension.rtree;

import java.sql.SQLException;

import org.sqlite.Function;

import mil.nga.geopackage.geom.GeoPackageGeometryData;

/**
 * Geometry Function for reading Geometry Data from a geometry column blob
 * 
 * @author osbornb
 * @since 2.0.1
 */
public abstract class GeometryFunction extends Function {

	/**
	 * Function name
	 */
	private String name;

	/**
	 * Constructor
	 *
	 * @param name
	 *            function name
	 * @since 6.0.0
	 */
	public GeometryFunction(String name) {
		this.name = name;
	}

	/**
	 * Get the function name
	 *
	 * @return name
	 * @since 6.0.0
	 */
	public String getName() {
		return name;
	}

	/**
	 * Execute the function
	 * 
	 * @param geometryData
	 *            geometry data
	 * @return function result
	 */
	public abstract Object execute(GeoPackageGeometryData geometryData);

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void xFunc() throws SQLException {

		int argCount = args();
		if (argCount != 1) {
			throw new SQLException(
					"Single argument is required. args: " + argCount);
		}

		byte[] bytes = value_blob(0);
		GeoPackageGeometryData geometryData = null;
		if (bytes != null && bytes.length > 0) {
			geometryData = GeoPackageGeometryData.create(bytes);
		}

		Object response = execute(geometryData);

		if (response == null) {
			result();
		} else if (response instanceof Double) {
			result((Double) response);
		} else if (response instanceof Integer) {
			result((Integer) response);
		} else {
			throw new SQLException("Unexpected response value: " + response
					+ ", of type: " + response.getClass().getSimpleName());
		}

	}

}
