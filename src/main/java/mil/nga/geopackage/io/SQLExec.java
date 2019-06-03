package mil.nga.geopackage.io;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	 * Logger
	 */
	private static final Logger LOGGER = Logger
			.getLogger(SQLExec.class.getName());

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
				} else if (sql == null) {
					sql = new StringBuilder(arg);
					requiredArguments = true;
				} else {
					sql.append(" ").append(arg);
				}
			}
		}

		if (!valid || !requiredArguments) {
			printUsage();
		} else {
			SQLExecResult result = executeSQL(geoPackageFile, sql.toString(),
					maxRows);
			result.printResults();
		}

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

		LOGGER.log(Level.INFO, "GeoPackage: " + geoPackage.getName());
		LOGGER.log(Level.INFO, "GeoPackage Path: " + geoPackage.getPath());
		LOGGER.log(Level.INFO, "SQL: " + sql);
		LOGGER.log(Level.INFO, "Max Rows: " + maxRows);

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

			ResultSet resultSet = statement.executeQuery();

			LOGGER.log(Level.INFO, "Successfully Executed: " + sql);

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
