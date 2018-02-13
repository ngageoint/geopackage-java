package mil.nga.geopackage.extension;

import java.sql.SQLException;

import mil.nga.geopackage.geom.GeoPackageGeometryData;

import org.sqlite.Function;

/**
 * Geometry Function for reading Geometry Data from a geometry column blob
 * 
 * @author osbornb
 * @since 2.0.1
 */
public abstract class GeometryFunction extends Function {

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
			throw new SQLException("Single argument is required. args: "
					+ argCount);
		}

		byte[] bytes = value_blob(0);
		GeoPackageGeometryData geometryData = null;
		if (bytes != null && bytes.length > 0) {
			geometryData = new GeoPackageGeometryData(bytes);
		}

		Object response = execute(geometryData);

		if (response == null) {
			result();
		} else if (response instanceof Double) {
			result((Double) response);
		} else if (response instanceof Boolean) {
			result(Boolean.compare((Boolean) response, false));
		} else {
			throw new SQLException("Unexpected response value: " + response
					+ ", of type: " + response.getClass().getSimpleName());
		}

	}

}
