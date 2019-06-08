package mil.nga.geopackage.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.SQLUtils;
import mil.nga.geopackage.db.table.TableColumn;
import mil.nga.geopackage.db.table.TableInfo;
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
	 * Write blobs command
	 */
	public static final String COMMAND_WRITE_BLOBS = "write blobs";

	/**
	 * Blob display value
	 */
	public static final String BLOB_DISPLAY_VALUE = "BLOB";

	/**
	 * Default write directory for blobs
	 */
	public static final String BLOBS_WRITE_DEFAULT_DIRECTORY = "blobs";

	/**
	 * Blobs extension argument
	 */
	public static final String BLOBS_ARGUMENT_EXTENSION = "e";

	/**
	 * Blobs directory argument
	 */
	public static final String BLOBS_ARGUMENT_DIRECTORY = "d";

	/**
	 * Blobs pattern argument
	 */
	public static final String BLOBS_ARGUMENT_PATTERN = "p";

	/**
	 * Blobs column start regex
	 */
	public static final String BLOBS_COLUMN_START_REGEX = "\\(";

	/**
	 * Blobs column end regex
	 */
	public static final String BLOBS_COLUMN_END_REGEX = "\\)";

	/**
	 * Blobs column pattern
	 */
	public static final Pattern BLOBS_COLUMN_PATTERN = Pattern
			.compile(BLOBS_COLUMN_START_REGEX + "([^" + BLOBS_COLUMN_END_REGEX
					+ "]+)" + BLOBS_COLUMN_END_REGEX);

	/**
	 * Blobs column pattern group
	 */
	public static final int BLOBS_COLUMN_PATTERN_GROUP = 1;

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

						} else if (sqlLine.equalsIgnoreCase(COMMAND_TABLES)) {

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

						} else if (sqlLine.toLowerCase()
								.startsWith(COMMAND_WRITE_BLOBS)) {

							writeBlobs(database, sqlBuilder, maxRows, history,
									sqlLine.substring(
											COMMAND_WRITE_BLOBS.length()));

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
				+ "      - re-execute the previous successful sql command");
		System.out.println(
				"\t!n      - re-execute a sql statement by history id n");
		System.out.println(
				"\t!-n     - re-execute a sql statement n commands back in history");
		System.out.println("\t" + COMMAND_WRITE_BLOBS + " [" + ARGUMENT_PREFIX
				+ BLOBS_ARGUMENT_EXTENSION + " file_extension] ["
				+ ARGUMENT_PREFIX + BLOBS_ARGUMENT_DIRECTORY + " directory] ["
				+ ARGUMENT_PREFIX + BLOBS_ARGUMENT_PATTERN + " pattern]");
		System.out.println(
				"\t        - write blobs from the previous successful sql command to the file system");
		System.out.println(
				"\t              ([directory]|blobs)/table_name/column_name/(pk_values|result_index|[pattern])[.file_extension]");
		System.out.println(
				"\t           file_extension - file extension added to each saved blob file");
		System.out.println(
				"\t           directory      - base directory to save table_name/column_name/blobs (default is ./"
						+ BLOBS_WRITE_DEFAULT_DIRECTORY + ")");
		System.out.println(
				"\t           pattern        - file directory and/or name pattern consisting of column names in parentheses");
		System.out.println(
				"\t                             (column_name)-(column_name2)");
		System.out.println(
				"\t                             (column_name)/(column_name2)");
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
	 * Write blobs from the query
	 * 
	 * @param database
	 *            database
	 * @param sqlBuilder
	 *            SQL builder
	 * @param maxRows
	 *            max rows
	 * @param history
	 *            history
	 * @param args
	 *            write blob arguments
	 * @throws SQLException
	 *             upon error
	 * @throws IOException
	 *             upon error
	 */
	private static void writeBlobs(GeoPackage database,
			StringBuilder sqlBuilder, Integer maxRows, List<String> history,
			String args) throws SQLException, IOException {

		if (history.isEmpty()) {
			System.out.println("No previous query with blobs");
		} else {

			boolean valid = true;

			String extension = null;
			String directory = null;
			String pattern = null;
			List<String> patternColumns = new ArrayList<>();

			if (args != null && !args.isEmpty()) {

				String[] argParts = args.trim().split("\\s+");

				for (int i = 0; valid && i < argParts.length; i++) {

					String arg = argParts[i];

					if (arg.startsWith(ARGUMENT_PREFIX)) {

						String argument = arg
								.substring(ARGUMENT_PREFIX.length());

						switch (argument) {

						case BLOBS_ARGUMENT_EXTENSION:
							if (i < argParts.length) {
								extension = argParts[++i];
							} else {
								valid = false;
								System.out.println(
										"Error: Blobs extension argument '"
												+ arg
												+ "' must be followed by a file extension");
							}
							break;

						case BLOBS_ARGUMENT_DIRECTORY:
							if (i < argParts.length) {
								directory = argParts[++i];
							} else {
								valid = false;
								System.out.println(
										"Error: Blobs directory argument '"
												+ arg
												+ "' must be followed by a directory location");
							}
							break;

						case BLOBS_ARGUMENT_PATTERN:
							if (i < argParts.length) {
								pattern = argParts[++i];
								Matcher matcher = BLOBS_COLUMN_PATTERN
										.matcher(pattern);
								while (matcher.find()) {
									String columnName = matcher
											.group(BLOBS_COLUMN_PATTERN_GROUP);
									patternColumns.add(columnName);
								}
								if (patternColumns.isEmpty()) {
									valid = false;
									System.out.println(
											"Error: Blobs pattern argument '"
													+ arg
													+ "' must be followed by a save pattern with at least one column surrounded by parentheses");
								}
							} else {
								valid = false;
								System.out.println(
										"Error: Blobs pattern argument '" + arg
												+ "' must be followed by a save pattern");
							}
							break;

						default:
							valid = false;
							System.out.println(
									"Error: Unsupported arg: '" + arg + "'");
						}

					} else {
						valid = false;
						System.out.println(
								"Error: Unsupported arg: '" + arg + "'");
					}
				}
			}

			if (valid) {

				String sql = history.get(history.size() - 1);

				if (maxRows == null) {
					maxRows = DEFAULT_MAX_ROWS;
				}

				Set<String> blobsWritten = new LinkedHashSet<>();
				int blobsWrittenCount = 0;

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

						List<Integer> blobColumns = new ArrayList<>();
						List<String> tables = new ArrayList<>();
						List<String> columnNames = new ArrayList<>();
						Map<String, List<Integer>> tableNameColumns = new HashMap<>();

						Map<String, Integer> columnNameIndexes = new HashMap<>();
						for (int col = 1; col <= numColumns; col++) {
							columnNameIndexes.put(metadata.getColumnName(col),
									col);
						}

						for (int col = 1; col <= numColumns; col++) {
							if (metadata.getColumnType(col) == Types.BLOB) {
								blobColumns.add(col);
								String tableName = metadata.getTableName(col);
								List<Integer> nameColumns = tableNameColumns
										.get(tableName);
								if (nameColumns == null) {
									nameColumns = new ArrayList<>();
									TableInfo tableInfo = TableInfo.info(
											database.getConnection(),
											tableName);
									List<String> nameColumnNames = null;
									if (pattern != null) {
										nameColumnNames = patternColumns;
									} else if (tableInfo.hasPrimaryKey()) {
										nameColumnNames = new ArrayList<>();
										for (TableColumn tableColumn : tableInfo
												.getPrimaryKeys()) {
											nameColumnNames
													.add(tableColumn.getName());
										}
									}
									if (nameColumnNames != null) {
										for (String columnName : nameColumnNames) {
											Integer columnIndex = columnNameIndexes
													.get(columnName);
											if (columnIndex == null
													&& pattern != null) {
												throw new IllegalArgumentException(
														"Pattern column not found in query: "
																+ columnName);
											}
											nameColumns.add(columnIndex);
										}
									}
									tableNameColumns.put(tableName,
											nameColumns);
								}
								tables.add(tableName);
								columnNames.add(metadata.getColumnName(col));
							}
						}

						if (!blobColumns.isEmpty()) {

							if (extension != null
									&& !extension.startsWith(".")) {
								extension = "." + extension;
							}

							if (directory == null) {
								directory = BLOBS_WRITE_DEFAULT_DIRECTORY;
							}
							File blobsDirectory = new File(directory);

							int resultCount = 0;
							while (resultSet.next()) {

								resultCount++;

								for (int i = 0; i < blobColumns.size(); i++) {
									int col = blobColumns.get(i);

									byte[] blobBytes = resultSet.getBytes(col);

									if (blobBytes != null) {

										String tableName = tables.get(i);

										File tableDirectory = new File(
												blobsDirectory, tableName);
										File columnDirectory = new File(
												tableDirectory,
												columnNames.get(i));

										String name = null;

										if (pattern != null) {
											name = pattern;
										}

										List<Integer> nameColumns = tableNameColumns
												.get(tableName);
										if (!nameColumns.isEmpty()) {
											for (int j = 0; j < nameColumns
													.size(); j++) {
												Integer nameColumn = nameColumns
														.get(j);
												if (nameColumn != null) {
													String columnValue = resultSet
															.getString(
																	nameColumn);
													if (columnValue != null) {
														if (pattern != null) {
															String columnName = patternColumns
																	.get(j);
															name = name
																	.replaceAll(
																			BLOBS_COLUMN_START_REGEX
																					+ columnName
																					+ BLOBS_COLUMN_END_REGEX,
																			columnValue);
														} else if (name == null) {
															name = columnValue;
														} else {
															name += "-"
																	+ columnValue;
														}
													}
												}
											}
										}

										if (name == null) {
											name = String.valueOf(resultCount);
										}

										if (extension != null) {
											name += extension;
										}

										File blobFile = new File(
												columnDirectory, name);
										blobFile.getParentFile().mkdirs();

										FileOutputStream fos = new FileOutputStream(
												blobFile);
										fos.write(blobBytes);
										fos.close();

										blobsWrittenCount++;
										blobsWritten.add(columnDirectory
												.getAbsolutePath());
									}
								}

							}

						}

					}

				} finally {
					SQLUtils.closeStatement(statement, sql);
				}

				if (blobsWrittenCount <= 0) {
					System.out.println("No Blobs in previous query: " + sql);
				} else {
					System.out
							.println(blobsWrittenCount + " Blobs written to:");
					for (String location : blobsWritten) {
						System.out.println(location);
					}
				}
			}

		}

		resetCommandPrompt(sqlBuilder);
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
