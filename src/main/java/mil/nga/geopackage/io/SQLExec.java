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

/**
 * Executes SQL on a GeoPackage
 * 
 * To run from command line, build with the standalone profile:
 * 
 * mvn clean install -Pstandalone
 * 
 * java -classpath geopackage-*-standalone.jar mil.nga.geopackage.io.SQLExec
 * +usage_arguments
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
	 * Main method to execute SQL in a GeoPackage
	 * 
	 * @param args
	 *            arguments
	 * @throws Exception
	 *             upon failure
	 */
	public static void main(String[] args) throws Exception {

		boolean valid = true;
		boolean requiredArguments = false;

		File geoPackageFile = null;
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
				if (geoPackageFile == null) {
					geoPackageFile = new File(arg);
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

			GeoPackage geoPackage = GeoPackageManager.open(geoPackageFile);
			try {

				System.out.println("GeoPackage: " + geoPackage.getName());
				System.out.println("Path: " + geoPackage.getPath());
				System.out.println("Max Rows: "
						+ (maxRows != null ? maxRows : DEFAULT_MAX_ROWS));

				if (sql != null) {

					try {
						SQLExecResult result = executeSQL(geoPackage,
								sql.toString(), maxRows);
						result.printResults();
					} catch (Exception e) {
						System.out.println(e);
					}

				} else {

					commandPrompt(geoPackage, maxRows);

				}

			} finally {
				geoPackage.close();
			}
		}

	}

	/**
	 * Command prompt accepting SQL statements
	 * 
	 * @param geoPackage
	 *            open GeoPackage
	 */
	private static void commandPrompt(GeoPackage geoPackage, Integer maxRows) {

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

						executeSQL(geoPackage, sqlBuilder,
								sqlBuilder.toString(), maxRows, history);

					} else if (singleLine) {

						if (sqlLine.isEmpty()) {

							break;

						} else if (sqlLine.equalsIgnoreCase("history")) {

							for (int i = 0; i < history.size(); i++) {
								System.out.println(
										" " + String.format("%4d", i + 1) + "  "
												+ history.get(i));
							}

							resetCommandPrompt(sqlBuilder);

						} else if (sqlLine.equals("!!")) {

							executeSQL(geoPackage, sqlBuilder, history.size(),
									maxRows, history);

						} else if (HISTORY_PATTERN.matcher(sqlLine).matches()) {

							int historyNumber = Integer.parseInt(
									sqlLine.substring(1, sqlLine.length()));

							executeSQL(geoPackage, sqlBuilder, historyNumber,
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
	 * Execute the SQL
	 * 
	 * @param geoPackage
	 *            GeoPackage
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
	private static void executeSQL(GeoPackage geoPackage,
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
			executeSQL(geoPackage, sqlBuilder, sql, maxRows, history);

		} else {
			System.out.println("No History at " + historyNumber);
			resetCommandPrompt(sqlBuilder);
		}

	}

	/**
	 * Execute the SQL
	 * 
	 * @param geoPackage
	 *            GeoPackage
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
	private static void executeSQL(GeoPackage geoPackage,
			StringBuilder sqlBuilder, String sql, Integer maxRows,
			List<String> history) throws SQLException {

		SQLExecResult result = executeSQL(geoPackage, sql, maxRows);
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
		System.out.print("sql> ");
	}

	/**
	 * Execute the SQL on the GeoPackage
	 * 
	 * @param geoPackageFile
	 *            GeoPackage file
	 * @param sql
	 *            SQL statement
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	public static SQLExecResult executeSQL(File geoPackageFile, String sql)
			throws SQLException {
		return executeSQL(geoPackageFile, sql, null);
	}

	/**
	 * Execute the SQL on the GeoPackage
	 * 
	 * @param geoPackageFile
	 *            GeoPackage file
	 * @param sql
	 *            SQL statement
	 * @param maxRows
	 *            max rows
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	public static SQLExecResult executeSQL(File geoPackageFile, String sql,
			Integer maxRows) throws SQLException {

		SQLExecResult result = null;

		GeoPackage geoPackage = GeoPackageManager.open(geoPackageFile);
		try {
			result = executeSQL(geoPackage, sql, maxRows);
		} finally {
			geoPackage.close();
		}

		return result;
	}

	/**
	 * Execute the SQL on the GeoPackage
	 * 
	 * @param geoPackage
	 *            open GeoPackage
	 * @param sql
	 *            SQL statement
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	public static SQLExecResult executeSQL(GeoPackage geoPackage, String sql)
			throws SQLException {
		return executeSQL(geoPackage, sql, null);
	}

	/**
	 * Execute the SQL on the GeoPackage
	 * 
	 * @param geoPackage
	 *            open GeoPackage
	 * @param sql
	 *            SQL statement
	 * @param maxRows
	 *            max rows
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	public static SQLExecResult executeSQL(GeoPackage geoPackage, String sql,
			Integer maxRows) throws SQLException {

		// If no max number of results, use the default
		if (maxRows == null) {
			maxRows = DEFAULT_MAX_ROWS;
		}

		sql = sql.trim();

		RTreeIndexExtension rtree = new RTreeIndexExtension(geoPackage);
		if (rtree.has()) {
			rtree.createAllFunctions();
		}

		SQLExecResult result = SQLExecAlterTable.alterTable(geoPackage, sql);
		if (result == null) {
			result = executeQuery(geoPackage, sql, maxRows);
		}

		return result;
	}

	/**
	 * Execute the query against the GeoPackage
	 * 
	 * @param geoPackage
	 *            open GeoPackage
	 * @param sql
	 *            SQL statement
	 * @param maxRows
	 *            max rows
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	private static SQLExecResult executeQuery(GeoPackage geoPackage, String sql,
			int maxRows) throws SQLException {

		SQLExecResult result = new SQLExecResult();

		PreparedStatement statement = null;
		try {

			statement = geoPackage.getConnection().getConnection()
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
								stringValue = "BLOB";
								break;
							default:
								stringValue = stringValue
										.replaceAll("\\s*[\\r\\n]+\\s*", " ");
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
				+ " max_rows] geopackage_file sql");
		System.out.println();
		System.out.println("DESCRIPTION");
		System.out.println();
		System.out.println("\tExecutes SQL on a GeoPackage");
		System.out.println();
		System.out.println("ARGUMENTS");
		System.out.println();
		System.out.println(
				"\t" + ARGUMENT_PREFIX + ARGUMENT_MAX_ROWS + " max_rows");
		System.out.println("\t\tMax rows to query and display" + " (Default is "
				+ DEFAULT_MAX_ROWS + ")");
		System.out.println();
		System.out.println("\tgeopackage_file");
		System.out.println(
				"\t\tpath to the GeoPackage file containing the tiles");
		System.out.println();
		System.out.println("\tsql");
		System.out.println("\t\tSQL statement to execute");
		System.out.println();
	}

}
