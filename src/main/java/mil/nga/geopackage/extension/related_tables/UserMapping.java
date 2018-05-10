package mil.nga.geopackage.extension.related_tables;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Provides the mapping between the rows in the base table 
 * and the related data table
 * 
 * @author jyutzler
 */
@DatabaseTable(daoClass = BaseDaoImpl.class)
public class UserMapping {

	
	/**
	 * Base ID field name
	 */
	public static final String COLUMN_BASE_ID = "base_id";

	/**
	 * Related ID field name
	 */
	public static final String COLUMN_RELATED_ID = "related_id";

	/**
	 * Base ID
	 */
	@DatabaseField(columnName = COLUMN_BASE_ID, id = true, canBeNull = false)
	private long baseId;

	/**
	 * Related ID
	 */
	@DatabaseField(columnName = COLUMN_RELATED_ID, canBeNull = false)
	private long relatedId;

	public long getBaseId() {
		return baseId;
	}

	public void setBaseId(long baseId) {
		this.baseId = baseId;
	}

	public long getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(long relatedId) {
		this.relatedId = relatedId;
	}

	/**
	 * Default Constructor
	 */
	public UserMapping() {
		super();
	}
}
