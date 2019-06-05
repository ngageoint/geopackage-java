package mil.nga.geopackage.io;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.SQLUtils;
import mil.nga.geopackage.extension.RTreeIndexExtension;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.validate.GeoPackageValidate;

/**
 * Executes SQL on a SQLite database
 * 
 * To run from command line, build with the standalone profile:
 * 
 * mvn clean install -Pstandalone
 * 
 * java -jar name.jar +usage_arguments
 * 
 * java -classpath name.jar mil.nga.geopackage.io.SQLExec +usage_arguments
 * 
 * @author osbornb
 * @since 3.3.0
 */
public class SQLExec {

	/**
	 * Argument prefix
	 */
	public static final String ARGUMENT_PREFIX = "-";

	/**
	 * Max Rows argument
	 */
	public static final String ARGUMENT_MAX_ROWS = "m";

	/**
	 * Default max rows
	 */
	public static final int DEFAULT_MAX_ROWS = 100;

	/**
	 * History pattern
	 */
	public static final Pattern HISTORY_PATTERN = Pattern.compile("^!-?\\d+$");

	/**
	 * Command prompt
	 */
	public static final String COMMAND_PROMPT = "sql> ";

	/**
	 * Help command
	 */
	public static final String COMMAND_HELP = "help";

	/**
	 * Tables command
	 */
	public static final String COMMAND_TABLES = "tables";

	/**
	 * Tables command SQL
	 */
	public static final String COMMAND_TABLES_SQL = "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name;";

	/**
	 * Tables command max rows
	 */
	public static final int COMMAND_TABLES_MAX_ROWS = 2147483646;

	/**
	 * History command
	 */
	public static final String COMMAND_HISTORY = "history";

	/**
	 * Previous command
	 */
	public static final String COMMAND_PREVIOUS = "!!";

	/**
	 * Blob display value
	 */
	public static final String BLOB_DISPLAY_VALUE = "BLOB";

	/**
	 * Main method to execute SQL in a SQLite database
	 * 
	 * @param args
	 *            arguments
	 * @throws Exception
	 *             upon failure
	 */
	public static void main(String[] args) throws Exception {

		boolean valid = true;
		boolean requiredArguments = false;

		File sqliteFile = null;
		Integer maxRows = null;
		StringBuilder sql = null;

		for (int i = 0; valid && i < args.length; i++) {

			String arg = args[i];

			// Handle optional arguments
			if (arg.startsWith(ARGUMENT_PREFIX)) {

				String argument = arg.substring(ARGUMENT_PREFIX.length());

				switch (argument) {
				case ARGUMENT_MAX_ROWS:
					if (i < args.length) {
						String maxRowsString = args[++i];
						try {
							maxRows = Integer.valueOf(maxRowsString);
						} catch (NumberFormatException e) {
							valid = false;
							System.out.println("Error: Max Rows argument '"
									+ arg
									+ "' must be followed by a valid number. Invalid: "
									+ maxRowsString);
						}
					} else {
						valid = false;
						System.out.println("Error: Max Rows argument '" + arg
								+ "' must be followed by a valid number");
					}
					break;

				default:
					valid = false;
					System.out.println("Error: Unsupported arg: '" + arg + "'");
				}

			} else {
				// Set required arguments in order
				if (sqliteFile == null) {
					sqliteFile = new File(arg);
					requiredArguments = true;
				} else if (sql == null) {
					sql = new StringBuilder(arg);
				} else {
					sql.append(" ").append(arg);
				}
			}
		}

		if (!valid || !requiredArguments) {
			printUsage();
		} else {

			GeoPackage database = GeoPackageManager.open(sqliteFile, false);
			try {

				if (GeoPackageValidate.hasMinimumTables(database)) {
					System.out.print("GeoPackage");
				} else {
					System.out.print("Database");
				}
				System.out.println(": " + database.getName());
				System.out.println("Path: " + database.getPath());
				System.out.println("Max Rows: "
						+ (maxRows != null ? maxRows : DEFAULT_MAX_ROWS));

				if (sql != null) {

					try {
						SQLExecResult result = executeSQL(database,
								sql.toString(), maxRows);
						result.printResults();
					} catch (Exception e) {
						System.out.println(e);
					}

				} else {

					commandPrompt(database, maxRows);

				}

			} finally {
				database.close();
			}
		}

	}

	/**
	 * Command prompt accepting SQL statements
	 * 
	 * @param database
	 *            open database
	 */
	private static void commandPrompt(GeoPackage database, Integer maxRows) {

		printHelp(database);

		List<String> history = new ArrayList<>();
		Scanner scanner = new Scanner(System.in);
		try {
			StringBuilder sqlBuilder = new StringBuilder();
			resetCommandPrompt(sqlBuilder);

			while (scanner.hasNextLine()) {
				try {
					String sqlLine = scanner.nextLine().trim();

					int semicolon = sqlLine.indexOf(";");
					boolean executeSql = semicolon >= 0;
					if (executeSql) {
						sqlLine = sqlLine.substring(0, semicolon + 1);
					}

					boolean singleLine = sqlBuilder.length() == 0;
					if (!sqlLine.isEmpty()) {
						if (!singleLine) {
							sqlBuilder.append(" ");
						}
						sqlBuilder.append(sqlLine);
					}

					if (executeSql) {

						executeSQL(database, sqlBuilder, sqlBuilder.toString(),
								maxRows, history);

					} else if (singleLine) {

						if (sqlLine.isEmpty()) {

							break;

						} else if (sqlLine.equalsIgnoreCase(COMMAND_HELP)) {

							printHelp(database);

							resetCommandPrompt(sqlBuilder);

						} else if (sqlLine.equals(COMMAND_TABLES)) {

							executeSQL(database, sqlBuilder, COMMAND_TABLES_SQL,
									COMMAND_TABLES_MAX_ROWS, history);

						} else if (sqlLine.equalsIgnoreCase(COMMAND_HISTORY)) {

							for (int i = 0; i < history.size(); i++) {
								System.out.println(
										" " + String.format("%4d", i + 1) + "  "
												+ history.get(i));
							}

							resetCommandPrompt(sqlBuilder);

						} else if (sqlLine.equals(COMMAND_PREVIOUS)) {

							executeSQL(database, sqlBuilder, history.size(),
									maxRows, history);

						} else if (HISTORY_PATTERN.matcher(sqlLine).matches()) {

							int historyNumber = Integer.parseInt(
									sqlLine.substring(1, sqlLine.length()));

							executeSQL(database, sqlBuilder, historyNumber,
									maxRows, history);

						}

					}

				} catch (Exception e) {
					System.out.println(e);
					resetCommandPrompt(sqlBuilder);
				}
			}
		} finally {
			scanner.close();
		}

	}

	/**
	 * Print the command prompt help
	 * 
	 * @param database
	 *            database
	 */
	private static void printHelp(GeoPackage database) {
		System.out.println();
		System.out.println("- Supports most SQLite statements including:");
		System.out.println(
				"\tSELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, PRAGMA, VACUUM, etc");
		System.out.println("- Terminate SQL statements with a ;");
		System.out.println("- Exit with a single empty line");
		System.out.println();
		System.out.println("Commands:");
		System.out.println();
		System.out.println(
				"\t" + COMMAND_HELP + "    - print this help information");
		System.out.println("\t" + COMMAND_TABLES + "  - list database tables");
		System.out.println("\t" + COMMAND_HISTORY
				+ " - list successfully executed sql commands");
		System.out.println("\t" + COMMAND_PREVIOUS
				+ "      - re-execute the last successful sql command");
		System.out.println(
				"\t!n      - re-execute a sql statement by history id n");
		System.out.println(
				"\t!-n     - re-execute a sql statement n commands back in history");
		System.out.println();
		System.out.println("Special Supported Cases:");
		System.out.println();
		System.out.println("\tDrop Column  - Not natively supported in SQLite");
		System.out.println(
				"\t                  * ALTER TABLE table_name DROP column_name");
		System.out.println(
				"\t                  * ALTER TABLE table_name DROP COLUMN column_name");
		System.out.println("\tCopy Table   - Not a traditional SQL statment");
		System.out.println(
				"\t                  * ALTER TABLE table_name COPY TO new_table_name");
		if (GeoPackageValidate.hasMinimumTables(database)) {
			System.out.println(
					"\tRename Table - User tables are updated throughout the GeoPackage");
			System.out.println(
					"\t                  * ALTER TABLE table_name RENAME TO new_table_name");
			System.out.println(
					"\tDrop Table   - User tables are dropped throughout the GeoPackage");
			System.out.println("\t                  * DROP TABLE table_name");
		}
	}

	/**
	 * Execute the SQL
	 * 
	 * @param database
	 *            database
	 * @param sqlBuilder
	 *            SQL builder
	 * @param historyNumber
	 *            history number
	 * @param maxRows
	 *            max rows
	 * @param history
	 *            history
	 * @throws SQLException
	 *             upon error
	 */
	private static void executeSQL(GeoPackage database,
			StringBuilder sqlBuilder, int historyNumber, Integer maxRows,
			List<String> history) throws SQLException {

		int number = historyNumber;

		if (number < 0) {
			number += history.size();
		} else {
			number--;
		}

		if (number >= 0 && number < history.size()) {

			String sql = history.get(number);
			System.out.println(sql);
			executeSQL(database, sqlBuilder, sql, maxRows, history);

		} else {
			System.out.println("No History at " + historyNumber);
			resetCommandPrompt(sqlBuilder);
		}

	}

	/**
	 * Execute the SQL
	 * 
	 * @param database
	 *            database
	 * @param sqlBuilder
	 *            SQL builder
	 * @param sql
	 *            SQL statement
	 * @param maxRows
	 *            max rows
	 * @param history
	 *            history
	 * @throws SQLException
	 *             upon error
	 */
	private static void executeSQL(GeoPackage database,
			StringBuilder sqlBuilder, String sql, Integer maxRows,
			List<String> history) throws SQLException {

		SQLExecResult result = executeSQL(database, sql, maxRows);
		result.printResults();

		history.add(sql);

		resetCommandPrompt(sqlBuilder);
	}

	/**
	 * Reset the command prompt
	 * 
	 * @param sqlBuilder
	 *            sql builder
	 */
	private static void resetCommandPrompt(StringBuilder sqlBuilder) {
		sqlBuilder.setLength(0);
		System.out.println();
		System.out.print(COMMAND_PROMPT);
	}

	/**
	 * Execute the SQL on the database
	 * 
	 * @param databaseFile
	 *            database file
	 * @param sql
	 *            SQL statement
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	public static SQLExecResult executeSQL(File databaseFile, String sql)
			throws SQLException {
		return executeSQL(databaseFile, sql, null);
	}

	/**
	 * Execute the SQL on the database
	 * 
	 * @param databaseFile
	 *            database file
	 * @param sql
	 *            SQL statement
	 * @param maxRows
	 *            max rows
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	public static SQLExecResult executeSQL(File databaseFile, String sql,
			Integer maxRows) throws SQLException {

		SQLExecResult result = null;

		GeoPackage database = GeoPackageManager.open(databaseFile);
		try {
			result = executeSQL(database, sql, maxRows);
		} finally {
			database.close();
		}

		return result;
	}

	/**
	 * Execute the SQL on the database
	 * 
	 * @param database
	 *            open database
	 * @param sql
	 *            SQL statement
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	public static SQLExecResult executeSQL(GeoPackage database, String sql)
			throws SQLException {
		return executeSQL(database, sql, null);
	}

	/**
	 * Execute the SQL on the GeoPadatabaseckage
	 * 
	 * @param database
	 *            open database
	 * @param sql
	 *            SQL statement
	 * @param maxRows
	 *            max rows
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	public static SQLExecResult executeSQL(GeoPackage database, String sql,
			Integer maxRows) throws SQLException {

		// If no max number of results, use the default
		if (maxRows == null) {
			maxRows = DEFAULT_MAX_ROWS;
		}

		sql = sql.trim();

		RTreeIndexExtension rtree = new RTreeIndexExtension(database);
		if (rtree.has()) {
			rtree.createAllFunctions();
		}

		SQLExecResult result = SQLExecAlterTable.alterTable(database, sql);
		if (result == null) {
			result = executeQuery(database, sql, maxRows);
		}

		return result;
	}

	/**
	 * Execute the query against the database
	 * 
	 * @param database
	 *            open database
	 * @param sql
	 *            SQL statement
	 * @param maxRows
	 *            max rows
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	private static SQLExecResult executeQuery(GeoPackage database, String sql,
			int maxRows) throws SQLException {

		SQLExecResult result = new SQLExecResult();

		if (!sql.equals(";")) {

			PreparedStatement statement = null;
			try {

				statement = database.getConnection().getConnection()
						.prepareStatement(sql);
				statement.setMaxRows(maxRows);

				boolean hasResultSet = statement.execute();

				if (hasResultSet) {

					ResultSet resultSet = statement.getResultSet();

					ResultSetMetaData metadata = resultSet.getMetaData();
					int numColumns = metadata.getColumnCount();

					int[] columnWidths = new int[numColumns];
					int[] columnTypes = new int[numColumns];

					for (int col = 1; col <= numColumns; col++) {
						result.addTable(metadata.getTableName(col));
						String columnName = metadata.getColumnName(col);
						result.addColumn(columnName);
						columnTypes[col - 1] = metadata.getColumnType(col);
						columnWidths[col - 1] = columnName.length();
					}

					while (resultSet.next()) {

						List<String> row = new ArrayList<>();
						result.addRow(row);
						for (int col = 1; col <= numColumns; col++) {

							String stringValue = resultSet.getString(col);

							if (stringValue != null) {

								switch (columnTypes[col - 1]) {
								case Types.BLOB:
									stringValue = BLOB_DISPLAY_VALUE;
									break;
								default:
									stringValue = stringValue.replaceAll(
											"\\s*[\\r\\n]+\\s*", " ");
								}

								int valueLength = stringValue.length();
								if (valueLength > columnWidths[col - 1]) {
									columnWidths[col - 1] = valueLength;
								}

							}

							row.add(stringValue);
						}

					}

					result.addColumnWidths(columnWidths);

				} else {

					int updateCount = statement.getUpdateCount();
					if (updateCount >= 0) {
						result.setUpdateCount(updateCount);
					}

				}

			} finally {
				SQLUtils.closeStatement(statement, sql);
			}
		}

		return result;
	}

	/**
	 * Print usage for the main method
	 */
	private static void printUsage() {
		System.out.println();
		System.out.println("USAGE");
		System.out.println();
		System.out.println("\t[" + ARGUMENT_PREFIX + ARGUMENT_MAX_ROWS
				+ " max_rows] sqlite_file [sql]");
		System.out.println();
		System.out.println("DESCRIPTION");
		System.out.println();
		System.out.println("\tExecutes SQL on a SQLite database");
		System.out.println();
		System.out.println(
				"\tProvide the SQL to execute a single statement. Omit to start an interactive session.");
		System.out.println();
		System.out.println("ARGUMENTS");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_MAX_ROWS + " max_rows");
		System.out.println("\t\tMax rows to query and display" + " (Default is "
				+ DEFAULT_MAX_ROWS + ")");
		System.out.println();
		System.out.println("\tsqlite_file");
		System.out.println("\t\tpath to the SQLite database file");
		System.out.println();
		System.out.println("\tsql");
		System.out.println("\t\tSQL statement to execute");
		System.out.println();
	}

}
