package mil.nga.geopackage.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.db.AlterTable;
import mil.nga.geopackage.db.CoreSQLUtils;

/**
 * Execute special Alter Table statement cases including unsupported, non
 * compliant, and those requiring additional modifications
 * 
 * @author osbornb
 * @since 3.3.0
 */
public class SQLExecAlterTable {

	/**
	 * Regex prefix for ignoring: case insensitive, dotall mode (match line
	 * terminators), and start of line
	 */
	private static final String REGEX_PREFIX = "(?i)(?s)^";

	/**
	 * Match a table or column name, with or without quotes
	 */
	private static final String REGEX_NAME = "(\".+\"|\\S+)";

	/**
	 * Alter table prefix regex with table name
	 */
	private static final String REGEX_ALTER_TABLE = REGEX_PREFIX
			+ "ALTER\\s+TABLE\\s+" + REGEX_NAME + "\\s+";

	/**
	 * Alter table pattern
	 */
	public static final Pattern ALTER_TABLE_PATTERN = Pattern
			.compile(REGEX_ALTER_TABLE + ".+");

	/**
	 * Drop column pattern
	 */
	public static final Pattern DROP_COLUMN_PATTERN = Pattern
			.compile(REGEX_ALTER_TABLE + "DROP(\\s+COLUMN)?\\s+" + REGEX_NAME);

	/**
	 * Rename table pattern
	 */
	public static final Pattern RENAME_TABLE_PATTERN = Pattern
			.compile(REGEX_ALTER_TABLE + "RENAME\\s+TO\\s+" + REGEX_NAME);

	/**
	 * Copy table pattern
	 */
	public static final Pattern COPY_TABLE_PATTERN = Pattern
			.compile(REGEX_ALTER_TABLE + "COPY\\s+TO\\s+" + REGEX_NAME);

	/**
	 * Table name pattern group
	 */
	public static final int TABLE_NAME_GROUP = 1;

	/**
	 * Column name pattern group
	 */
	public static final int COLUMN_NAME_GROUP = 3;

	/**
	 * New table name pattern group
	 */
	public static final int NEW_TABLE_NAME_GROUP = 2;

	/**
	 * Drop table pattern
	 */
	public static final Pattern DROP_TABLE_PATTERN = Pattern
			.compile(REGEX_PREFIX + "DROP\\s+TABLE\\s+" + REGEX_NAME);

	/**
	 * Handle alter table statements that are unsupported, non spec compliant,
	 * or require additional modifications
	 * 
	 * @param database
	 *            open database
	 * @param sql
	 *            SQL statement
	 * @return results if handled, null if not a special case
	 */
	public static SQLExecResult alterTable(GeoPackage database, String sql) {

		SQLExecResult result = null;

		sql = sql.trim();
		if (sql.endsWith(";")) {
			sql = sql.substring(0, sql.length() - 1);
		}

		if (ALTER_TABLE_PATTERN.matcher(sql).matches()) {

			// ALTER TABLE table_name DROP column_name
			// ALTER TABLE table_name DROP COLUMN column_name
			result = dropColumn(database, sql);

			if (result == null) {

				// ALTER TABLE table_name RENAME TO new_table_name
				result = renameTable(database, sql);

				if (result == null) {

					// ALTER TABLE table_name COPY TO new_table_name
					result = copyTable(database, sql);

				}

			}

		} else {

			// DROP TABLE table_name
			result = dropTable(database, sql);

		}

		return result;
	}

	/**
	 * Check for a drop column statement and execute
	 * 
	 * @param database
	 *            database
	 * @param sql
	 *            SQL statement
	 * @return result if dropped column, null if not
	 */
	private static SQLExecResult dropColumn(GeoPackage database, String sql) {

		SQLExecResult result = null;

		Matcher matcher = DROP_COLUMN_PATTERN.matcher(sql);
		if (matcher.find()) {
			String tableName = CoreSQLUtils
					.quoteUnwrap(matcher.group(TABLE_NAME_GROUP));
			if (tableName != null) {
				tableName = tableName.trim();
			}
			String columnName = CoreSQLUtils
					.quoteUnwrap(matcher.group(COLUMN_NAME_GROUP));
			if (columnName != null) {
				columnName = columnName.trim();
			}

			if (tableName != null && columnName != null) {
				AlterTable.dropColumn(database.getDatabase(), tableName,
						columnName);
				result = new SQLExecResult();
			}

		}

		return result;
	}

	/**
	 * Check for a rename table statement and execute
	 * 
	 * @param database
	 *            database
	 * @param sql
	 *            SQL statement
	 * @return result if renamed table, null if not
	 */
	private static SQLExecResult renameTable(GeoPackage database, String sql) {

		SQLExecResult result = null;

		Matcher matcher = RENAME_TABLE_PATTERN.matcher(sql);
		if (matcher.find() && SQLExec.isGeoPackage(database)) {
			String tableName = CoreSQLUtils
					.quoteUnwrap(matcher.group(TABLE_NAME_GROUP));
			if (tableName != null) {
				tableName = tableName.trim();
			}
			String newTableName = CoreSQLUtils
					.quoteUnwrap(matcher.group(NEW_TABLE_NAME_GROUP));
			if (newTableName != null) {
				newTableName = newTableName.trim();
			}

			if (tableName != null && newTableName != null
					&& database.getTableDataType(tableName) != null) {
				database.renameTable(tableName, newTableName);
				result = new SQLExecResult();
			}

		}

		return result;
	}

	/**
	 * Check for a copy table statement and execute
	 * 
	 * @param database
	 *            database
	 * @param sql
	 *            SQL statement
	 * @return result if copied table, null if not
	 */
	private static SQLExecResult copyTable(GeoPackage database, String sql) {

		SQLExecResult result = null;

		Matcher matcher = COPY_TABLE_PATTERN.matcher(sql);
		if (matcher.find()) {
			String tableName = CoreSQLUtils
					.quoteUnwrap(matcher.group(TABLE_NAME_GROUP));
			if (tableName != null) {
				tableName = tableName.trim();
			}
			String newTableName = CoreSQLUtils
					.quoteUnwrap(matcher.group(NEW_TABLE_NAME_GROUP));
			if (newTableName != null) {
				newTableName = newTableName.trim();
			}

			if (tableName != null && newTableName != null) {

				if (SQLExec.isGeoPackage(database)) {
					database.copyTable(tableName, newTableName);
				} else {
					AlterTable.copyTable(database.getDatabase(), tableName,
							newTableName);
				}

				result = new SQLExecResult();

			}

		}

		return result;
	}

	/**
	 * Check for a drop table statement and execute
	 * 
	 * @param database
	 *            database
	 * @param sql
	 *            SQL statement
	 * @return result if dropped table, null if not
	 */
	private static SQLExecResult dropTable(GeoPackage database, String sql) {

		SQLExecResult result = null;

		Matcher matcher = DROP_TABLE_PATTERN.matcher(sql);
		if (matcher.matches() && SQLExec.isGeoPackage(database)) {
			String tableName = CoreSQLUtils
					.quoteUnwrap(matcher.group(TABLE_NAME_GROUP));
			if (tableName != null) {
				tableName = tableName.trim();
				if (!database.isTable(tableName)) {
					throw new GeoPackageException(
							"Table does not exist: " + tableName);
				}
				database.deleteTable(tableName.trim());
				result = new SQLExecResult();
			}
		}

		return result;
	}

}
