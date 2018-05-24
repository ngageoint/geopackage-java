package mil.nga.geopackage.extension.related;

import mil.nga.geopackage.user.UserRowSync;

/**
 * User Mapping Row Sync to support reading a single user mapping row copy when
 * multiple near simultaneous asynchronous requests are made
 *
 * @author osbornb
 * @since 3.0.1
 */
public class UserMappingRowSync extends
		UserRowSync<UserMappingColumn, UserMappingTable, UserMappingRow> {

	/**
	 * Constructor
	 */
	public UserMappingRowSync() {

	}

}
