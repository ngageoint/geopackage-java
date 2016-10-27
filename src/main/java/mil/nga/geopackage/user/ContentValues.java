package mil.nga.geopackage.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Content Values mapping between columns and values. This is a simplified
 * implementation of the Android counter part android.content.ContentValues
 * 
 * @author osbornb
 */
public class ContentValues {

	/**
	 * Mapping between columns and values
	 */
	private Map<String, Object> values = new HashMap<String, Object>();

	/**
	 * Put a key value pair
	 * 
	 * @param key
	 * @param value
	 */
	public void put(String key, Object value) {
		values.put(key, value);
	}

	/**
	 * Put a key null value
	 * 
	 * @param key
	 */
	public void putNull(String key) {
		values.put(key, null);
	}

	/**
	 * Get the number of value mappings
	 * 
	 * @return size
	 */
	public int size() {
		return values.size();
	}

	/**
	 * Get the value of the key
	 * 
	 * @param key
	 * @return value
	 */
	public Object get(String key) {
		return values.get(key);
	}

	/**
	 * Get a value set of the mappings
	 * 
	 * @return value set
	 */
	public Set<Map.Entry<String, Object>> valueSet() {
		return values.entrySet();
	}

	/**
	 * Get a field key set
	 * 
	 * @return field key set
	 */
	public Set<String> keySet() {
		return values.keySet();
	}

	/**
	 * Get the key value as a string
	 * 
	 * @param key
	 * @return string value
	 */
	public String getAsString(String key) {
		Object value = values.get(key);
		return value != null ? value.toString() : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String name : values.keySet()) {
			String value = getAsString(name);
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(name + "=" + value);
		}
		return sb.toString();
	}

}
