package mil.nga.geopackage.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.contents.ContentsDataType;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.db.SQLUtils;
import mil.nga.geopackage.db.master.SQLiteMaster;
import mil.nga.geopackage.db.master.SQLiteMasterColumn;
import mil.nga.geopackage.db.master.SQLiteMasterQuery;
import mil.nga.geopackage.db.master.SQLiteMasterType;
import mil.nga.geopackage.db.table.TableColumn;
import mil.nga.geopackage.db.table.TableInfo;
import mil.nga.geopackage.extension.coverage.CoverageData;
import mil.nga.geopackage.extension.rtree.RTreeIndexExtension;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.tiles.matrix.TileMatrix;
import mil.nga.geopackage.tiles.user.TileDao;
import mil.nga.geopackage.validate.GeoPackageValidate;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionFactory;
import mil.nga.sf.proj.ProjectionTransform;
import mil.nga.sf.util.GeometryEnvelopeBuilder;

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
	 * Max column width argument
	 */
	public static final String ARGUMENT_MAX_COLUMN_WIDTH = "w";

	/**
	 * Default max column width
	 */
	public static final Integer DEFAULT_MAX_COLUMN_WIDTH = 120;

	/**
	 * Max lines per row argument
	 */
	public static final String ARGUMENT_MAX_LINES_PER_ROW = "l";

	/**
	 * Default max lines per row
	 */
	public static final Integer DEFAULT_MAX_LINES_PER_ROW = null;

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
	 * Indexes command
	 */
	public static final String COMMAND_INDEXES = "indexes";

	/**
	 * Views command
	 */
	public static final String COMMAND_VIEWS = "views";

	/**
	 * Triggers command
	 */
	public static final String COMMAND_TRIGGERS = "triggers";

	/**
	 * Command with all rows
	 */
	public static final int COMMAND_ALL_ROWS = 2147483646;

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
	public static final String COMMAND_WRITE_BLOBS = "blobs";

	/**
	 * Max rows command
	 */
	public static final String COMMAND_MAX_ROWS = "rows";

	/**
	 * Max column width command
	 */
	public static final String COMMAND_MAX_COLUMN_WIDTH = "width";

	/**
	 * Max lines per row command
	 */
	public static final String COMMAND_MAX_LINES_PER_ROW = "lines";

	/**
	 * Table Info command
	 */
	public static final String COMMAND_TABLE_INFO = "info";

	/**
	 * VACUUM command
	 */
	public static final String COMMAND_VACUUM = "vacuum";

	/**
	 * Foreign Keys command
	 */
	public static final String COMMAND_FOREIGN_KEYS = "fk";

	/**
	 * SQLite Master command
	 */
	public static final String COMMAND_FOREIGN_KEY_CHECK = "fkc";

	/**
	 * SQLite Master command
	 */
	public static final String COMMAND_INTEGRITY_CHECK = "integrity";

	/**
	 * Quick Check command
	 */
	public static final String COMMAND_QUICK_CHECK = "quick";

	/**
	 * SQLite Master command
	 */
	public static final String COMMAND_SQLITE_MASTER = "sqlite_master";

	/**
	 * GeoPackage contents command
	 */
	public static final String COMMAND_CONTENTS = "contents";

	/**
	 * GeoPackage Info command
	 */
	public static final String COMMAND_GEOPACKAGE_INFO = "ginfo";

	/**
	 * GeoPackage extensions command
	 */
	public static final String COMMAND_EXTENSIONS = "extensions";

	/**
	 * GeoPackage geometry command
	 */
	public static final String COMMAND_GEOMETRY = "geometry";

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
	 * GeoPackage contents bounds command
	 */
	public static final String COMMAND_CONTENTS_BOUNDS = "cbounds";

	/**
	 * GeoPackage bounds command
	 */
	public static final String COMMAND_BOUNDS = "bounds";

	/**
	 * GeoPackage table bounds command
	 */
	public static final String COMMAND_TABLE_BOUNDS = "tbounds";

	/**
	 * Bounds projection argument
	 */
	public static final String ARGUMENT_PROJECTION = "p";

	/**
	 * Bounds manual argument
	 */
	public static final String BOUNDS_ARGUMENT_MANUAL = "m";

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
	 * Bounds query type
	 */
	private static enum BoundsType {
		CONTENTS, ALL, TABLE;
	}

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
		Integer maxRows = DEFAULT_MAX_ROWS;
		Integer maxColumnWidth = DEFAULT_MAX_COLUMN_WIDTH;
		Integer maxLinesPerRow = DEFAULT_MAX_LINES_PER_ROW;
		StringBuilder sql = null;

		for (int i = 0; valid && i < args.length; i++) {

			String arg = args[i];

			// Handle optional arguments
			if (arg.startsWith(ARGUMENT_PREFIX)) {

				String argument = arg.substring(ARGUMENT_PREFIX.length());

				switch (argument) {

				case ARGUMENT_MAX_ROWS:
					if (i + 1 < args.length) {
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
						maxRows = Math.max(0, maxRows);
					} else {
						valid = false;
						System.out.println("Error: Max Rows argument '" + arg
								+ "' must be followed by a valid number");
					}
					break;

				case ARGUMENT_MAX_COLUMN_WIDTH:
					if (i + 1 < args.length) {
						String maxColumnWidthString = args[++i];
						try {
							maxColumnWidth = Integer
									.valueOf(maxColumnWidthString);
						} catch (NumberFormatException e) {
							valid = false;
							System.out.println(
									"Error: Max Column Width argument '" + arg
											+ "' must be followed by a valid number. Invalid: "
											+ maxColumnWidthString);
						}
						maxColumnWidth = Math.max(0, maxColumnWidth);
					} else {
						valid = false;
						System.out.println("Error: Max Column Width argument '"
								+ arg + "' must be followed by a valid number");
					}
					break;

				case ARGUMENT_MAX_LINES_PER_ROW:
					if (i + 1 < args.length) {
						String maxLinesPerRowString = args[++i];
						try {
							maxLinesPerRow = Integer
									.valueOf(maxLinesPerRowString);
						} catch (NumberFormatException e) {
							valid = false;
							System.out.println(
									"Error: Max Lines Per Row argument '" + arg
											+ "' must be followed by a valid number. Invalid: "
											+ maxLinesPerRowString);
						}
						maxLinesPerRow = Math.max(0, maxLinesPerRow);
					} else {
						valid = false;
						System.out.println("Error: Max Lines Per Row argument '"
								+ arg + "' must be followed by a valid number");
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

			// Create the file if it does not exist
			if (!GeoPackageManager.exists(sqliteFile)) {
				sqliteFile = GeoPackageManager.create(sqliteFile, false);
			}

			GeoPackage database = GeoPackageManager.open(sqliteFile, false);
			try {

				printInfo(database, maxRows, maxColumnWidth, maxLinesPerRow);

				if (sql != null) {

					try {
						SQLExecResult result = executeSQL(database,
								sql.toString(), maxRows);
						setPrintOptions(result, maxColumnWidth, maxLinesPerRow);
						result.printResults();
					} catch (Exception e) {
						System.out.println(e);
					}

				} else {

					commandPrompt(database, maxRows, maxColumnWidth,
							maxLinesPerRow);

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
	 * @param maxRows
	 *            max rows
	 * @param maxColumnWidth
	 *            max column width
	 * @param maxLinesPerRow
	 *            max lines per row
	 */
	private static void commandPrompt(GeoPackage database, Integer maxRows,
			Integer maxColumnWidth, Integer maxLinesPerRow) {

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

					if (singleLine) {

						if (executeSql) {
							sqlLine = sqlLine.substring(0, sqlLine.length() - 1)
									.trim();
						}

						boolean command = true;

						String sqlLineLower = sqlLine.toLowerCase();

						if (sqlLine.isEmpty()) {

							break;

						} else if (sqlLine.equalsIgnoreCase(COMMAND_HELP)) {

							printHelp(database);

							resetCommandPrompt(sqlBuilder);

						} else if (sqlLineLower.startsWith(COMMAND_TABLES)) {

							String name = sqlLine
									.substring(COMMAND_TABLES.length(),
											sqlLine.length())
									.trim();
							String sql = buildSqlMasterQuery(false,
									SQLiteMasterType.TABLE, name);
							executeSQL(database, sqlBuilder, sql,
									COMMAND_ALL_ROWS, maxColumnWidth,
									maxLinesPerRow, history);

						} else if (sqlLineLower.startsWith(COMMAND_INDEXES)) {

							String name = sqlLine
									.substring(COMMAND_INDEXES.length(),
											sqlLine.length())
									.trim();
							String sql = buildSqlMasterQuery(true,
									SQLiteMasterType.INDEX, name);
							executeSQL(database, sqlBuilder, sql,
									COMMAND_ALL_ROWS, maxColumnWidth,
									maxLinesPerRow, history);

						} else if (sqlLineLower.startsWith(COMMAND_VIEWS)) {

							String name = sqlLine
									.substring(COMMAND_VIEWS.length(),
											sqlLine.length())
									.trim();
							String sql = buildSqlMasterQuery(false,
									SQLiteMasterType.VIEW, name);
							executeSQL(database, sqlBuilder, sql,
									COMMAND_ALL_ROWS, maxColumnWidth,
									maxLinesPerRow, history);

						} else if (sqlLineLower.startsWith(COMMAND_TRIGGERS)) {

							String name = sqlLine
									.substring(COMMAND_TRIGGERS.length(),
											sqlLine.length())
									.trim();
							String sql = buildSqlMasterQuery(true,
									SQLiteMasterType.TRIGGER, name);
							executeSQL(database, sqlBuilder, sql,
									COMMAND_ALL_ROWS, maxColumnWidth,
									maxLinesPerRow, history);

						} else if (sqlLine.equalsIgnoreCase(COMMAND_HISTORY)) {

							for (int i = 0; i < history.size(); i++) {
								System.out.println(
										" " + String.format("%4d", i + 1) + "  "
												+ history.get(i));
							}

							resetCommandPrompt(sqlBuilder);

						} else if (sqlLine.equalsIgnoreCase(COMMAND_PREVIOUS)) {

							executeSQL(database, sqlBuilder, history.size(),
									maxRows, maxColumnWidth, maxLinesPerRow,
									history);

						} else if (sqlLineLower
								.startsWith(COMMAND_WRITE_BLOBS)) {

							writeBlobs(database, sqlBuilder, maxRows, history,
									sqlLine.substring(
											COMMAND_WRITE_BLOBS.length()));

						} else if (HISTORY_PATTERN.matcher(sqlLine).matches()) {

							int historyNumber = Integer.parseInt(
									sqlLine.substring(1, sqlLine.length()));

							executeSQL(database, sqlBuilder, historyNumber,
									maxRows, maxColumnWidth, maxLinesPerRow,
									history);

						} else if (sqlLineLower.startsWith(COMMAND_MAX_ROWS)) {

							String maxRowsString = sqlLine
									.substring(COMMAND_MAX_ROWS.length(),
											sqlLine.length())
									.trim();
							if (!maxRowsString.isEmpty()) {
								maxRows = Integer.parseInt(maxRowsString);
								maxRows = Math.max(0, maxRows);
							}
							System.out.println(
									"Max Rows: " + printableValue(maxRows));
							resetCommandPrompt(sqlBuilder);

						} else if (sqlLineLower
								.startsWith(COMMAND_MAX_COLUMN_WIDTH)) {

							String maxColumnWidthString = sqlLine.substring(
									COMMAND_MAX_COLUMN_WIDTH.length(),
									sqlLine.length()).trim();
							if (!maxColumnWidthString.isEmpty()) {
								maxColumnWidth = Integer
										.parseInt(maxColumnWidthString);
								maxColumnWidth = Math.max(0, maxColumnWidth);
							}
							System.out.println("Max Column Width: "
									+ printableValue(maxColumnWidth));
							resetCommandPrompt(sqlBuilder);

						} else if (sqlLineLower
								.startsWith(COMMAND_MAX_LINES_PER_ROW)) {

							String maxLinesPerRowString = sqlLine.substring(
									COMMAND_MAX_LINES_PER_ROW.length(),
									sqlLine.length()).trim();
							if (!maxLinesPerRowString.isEmpty()) {
								maxLinesPerRow = Integer
										.parseInt(maxLinesPerRowString);
								maxLinesPerRow = Math.max(0, maxLinesPerRow);
							}
							System.out.println("Max Lines Per Row: "
									+ printableValue(maxLinesPerRow));
							resetCommandPrompt(sqlBuilder);

						} else if (sqlLineLower
								.startsWith(COMMAND_TABLE_INFO)) {

							String tableName = sqlLine
									.substring(COMMAND_TABLE_INFO.length(),
											sqlLine.length())
									.trim();
							if (!tableName.isEmpty()) {
								executeSQL(database, sqlBuilder,
										"PRAGMA table_info(\"" + tableName
												+ "\");",
										COMMAND_ALL_ROWS, maxColumnWidth,
										maxLinesPerRow, history);
							} else {
								printInfo(database, maxRows, maxColumnWidth,
										maxLinesPerRow);
								resetCommandPrompt(sqlBuilder);
							}

						} else if (sqlLine
								.equalsIgnoreCase(COMMAND_SQLITE_MASTER)
								|| SQLiteMaster.count(database.getDatabase(),
										new SQLiteMasterType[] {
												SQLiteMasterType.TABLE,
												SQLiteMasterType.VIEW },
										SQLiteMasterQuery.create(
												SQLiteMasterColumn.NAME,
												sqlLine)) > 0) {

							executeSQL(database, sqlBuilder,
									"SELECT * FROM \"" + sqlLine + "\";",
									maxRows, maxColumnWidth, maxLinesPerRow,
									history);

						} else if (sqlLineLower.startsWith(COMMAND_VACUUM)) {
							executeShortcutSQL(database, sqlBuilder, sqlLine,
									maxRows, maxColumnWidth, maxLinesPerRow,
									history, COMMAND_VACUUM, "VACUUM");
						} else if (sqlLineLower
								.startsWith(COMMAND_FOREIGN_KEY_CHECK)) {
							executeShortcutSQL(database, sqlBuilder, sqlLine,
									maxRows, maxColumnWidth, maxLinesPerRow,
									history, COMMAND_FOREIGN_KEY_CHECK,
									"PRAGMA foreign_key_check");
						} else if (sqlLineLower
								.startsWith(COMMAND_FOREIGN_KEYS)) {
							executeShortcutSQL(database, sqlBuilder, sqlLine,
									maxRows, maxColumnWidth, maxLinesPerRow,
									history, COMMAND_FOREIGN_KEYS,
									"PRAGMA foreign_keys");
						} else if (sqlLineLower
								.startsWith(COMMAND_INTEGRITY_CHECK)) {
							executeShortcutSQL(database, sqlBuilder, sqlLine,
									maxRows, maxColumnWidth, maxLinesPerRow,
									history, COMMAND_INTEGRITY_CHECK,
									"PRAGMA integrity_check");
						} else if (sqlLineLower
								.startsWith(COMMAND_QUICK_CHECK)) {
							executeShortcutSQL(database, sqlBuilder, sqlLine,
									maxRows, maxColumnWidth, maxLinesPerRow,
									history, COMMAND_QUICK_CHECK,
									"PRAGMA quick_check");
						} else if (isGeoPackage(database)) {

							if (sqlLineLower.startsWith(COMMAND_CONTENTS)) {

								String tableName = sqlLine
										.substring(COMMAND_CONTENTS.length(),
												sqlLine.length())
										.trim();
								StringBuilder sql = new StringBuilder(
										"SELECT table_name, data_type FROM gpkg_contents");
								if (!tableName.isEmpty()) {
									sql.append(" WHERE table_name LIKE ");
									sql.append(
											CoreSQLUtils.quoteWrap(tableName));
								}
								sql.append(" ORDER BY table_name;");
								executeSQL(database, sqlBuilder, sql.toString(),
										COMMAND_ALL_ROWS, maxColumnWidth,
										maxLinesPerRow, history);

							} else if (sqlLineLower
									.startsWith(COMMAND_GEOPACKAGE_INFO)) {

								String tableName = sqlLine.substring(
										COMMAND_GEOPACKAGE_INFO.length(),
										sqlLine.length()).trim();

								if (!tableName.isEmpty()) {

									executeSQL(database, sqlBuilder,
											"SELECT * FROM gpkg_contents WHERE LOWER(table_name) = '"
													+ tableName.toLowerCase()
													+ "';",
											COMMAND_ALL_ROWS, maxColumnWidth,
											maxLinesPerRow, history, false);

									String tableType = database
											.getTableType(tableName);
									if (tableType != null) {
										switch (tableType) {
										case CoverageData.GRIDDED_COVERAGE:
											executeSQL(database, sqlBuilder,
													"SELECT * FROM gpkg_2d_gridded_coverage_ancillary WHERE tile_matrix_set_name = '"
															+ tableName + "';",
													COMMAND_ALL_ROWS,
													maxColumnWidth,
													maxLinesPerRow, history,
													false);
											executeSQL(database, sqlBuilder,
													"SELECT * FROM gpkg_2d_gridded_tile_ancillary WHERE tpudt_name = '"
															+ tableName + "';",
													COMMAND_ALL_ROWS,
													maxColumnWidth,
													maxLinesPerRow, history,
													false);
											break;
										}
									}

									ContentsDataType dataType = database
											.getTableDataType(tableName);
									if (dataType != null) {
										switch (dataType) {
										case ATTRIBUTES:

											break;
										case FEATURES:
											executeSQL(database, sqlBuilder,
													"SELECT * FROM gpkg_geometry_columns WHERE table_name = '"
															+ tableName + "';",
													COMMAND_ALL_ROWS,
													maxColumnWidth,
													maxLinesPerRow, history,
													false);
											break;
										case TILES:
											executeSQL(database, sqlBuilder,
													"SELECT * FROM gpkg_tile_matrix_set WHERE table_name = '"
															+ tableName + "';",
													COMMAND_ALL_ROWS,
													maxColumnWidth,
													maxLinesPerRow, history,
													false);
											tileMatrix(database, sqlBuilder,
													maxColumnWidth,
													maxLinesPerRow, history,
													tableName);
											break;
										}
									}

									executeSQL(database, sqlBuilder,
											"PRAGMA table_info(\"" + tableName
													+ "\");",
											COMMAND_ALL_ROWS, maxColumnWidth,
											maxLinesPerRow, history, false);
								}

								resetCommandPrompt(sqlBuilder);

							} else if (sqlLineLower
									.startsWith(COMMAND_CONTENTS_BOUNDS)
									|| sqlLineLower.startsWith(COMMAND_BOUNDS)
									|| sqlLineLower
											.startsWith(COMMAND_TABLE_BOUNDS)) {

								BoundsType type = null;
								int commandLength;
								if (sqlLineLower
										.startsWith(COMMAND_CONTENTS_BOUNDS)) {
									type = BoundsType.CONTENTS;
									commandLength = COMMAND_CONTENTS_BOUNDS
											.length();
								} else if (sqlLineLower
										.startsWith(COMMAND_TABLE_BOUNDS)) {
									type = BoundsType.TABLE;
									commandLength = COMMAND_TABLE_BOUNDS
											.length();
								} else {
									type = BoundsType.ALL;
									commandLength = COMMAND_BOUNDS.length();
								}

								bounds(database, sqlBuilder, type,
										sqlLine.substring(commandLength));

							} else if (sqlLineLower
									.startsWith(COMMAND_EXTENSIONS)) {

								String tableName = sqlLine
										.substring(COMMAND_EXTENSIONS.length(),
												sqlLine.length())
										.trim();
								StringBuilder sql = new StringBuilder(
										"SELECT table_name, column_name, extension_name, definition FROM gpkg_extensions");
								if (!tableName.isEmpty()) {
									sql.append(
											" WHERE LOWER(table_name) LIKE ");
									sql.append(CoreSQLUtils.quoteWrap(
											tableName.toLowerCase()));
								}
								sql.append(";");

								executeSQL(database, sqlBuilder, sql.toString(),
										COMMAND_ALL_ROWS, maxColumnWidth,
										maxLinesPerRow, history);

							} else if (sqlLineLower
									.startsWith(COMMAND_GEOMETRY)) {

								geometries(database, sqlBuilder, maxRows,
										history,
										sqlLine.substring(
												COMMAND_GEOMETRY.length(),
												sqlLine.length()));

							} else {

								String[] parts = sqlLine.split("\\s+");
								String dataType = parts[0];

								if (ContentsDataType.fromName(
										dataType.toLowerCase()) != null
										|| !database
												.getTables(
														dataType.toLowerCase())
												.isEmpty()
										|| !database.getTables(dataType)
												.isEmpty()) {

									StringBuilder sql = new StringBuilder(
											"SELECT table_name FROM gpkg_contents WHERE LOWER(data_type) = '");
									sql.append(dataType.toLowerCase());
									sql.append("'");
									if (parts.length > 0) {
										String tableName = sqlLine
												.substring(dataType.length(),
														sqlLine.length())
												.trim();
										if (!tableName.isEmpty()) {
											sql.append(" AND table_name LIKE ");
											sql.append(CoreSQLUtils
													.quoteWrap(tableName));
										}
									}
									sql.append(" ORDER BY table_name;");

									executeSQL(database, sqlBuilder,
											sql.toString(), COMMAND_ALL_ROWS,
											maxColumnWidth, maxLinesPerRow,
											history);

								} else {
									command = false;
								}

							}

						} else {

							command = false;
						}

						if (command) {
							executeSql = false;
						}
					}

					if (executeSql) {

						executeSQL(database, sqlBuilder, sqlBuilder.toString(),
								maxRows, maxColumnWidth, maxLinesPerRow,
								history);

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
	 * Build a SQLite Master table query
	 * 
	 * @param tableName
	 *            true to include table name
	 * @param type
	 *            SQLite Master type
	 * @param name
	 *            name LIKE value
	 * @return SQL
	 */
	private static String buildSqlMasterQuery(boolean tableName,
			SQLiteMasterType type, String name) {

		StringBuilder sql = new StringBuilder("SELECT ");
		sql.append(SQLiteMasterColumn.NAME.name().toLowerCase());
		if (tableName) {
			sql.append(", ");
			sql.append(SQLiteMasterColumn.TBL_NAME.name().toLowerCase());
		}
		sql.append(" FROM ");
		sql.append(SQLiteMaster.TABLE_NAME);
		sql.append(" WHERE ");
		sql.append(SQLiteMasterColumn.TYPE.name().toLowerCase());
		sql.append(" = '");
		sql.append(type.name().toLowerCase());
		sql.append("' AND ");
		sql.append(SQLiteMasterColumn.NAME.name().toLowerCase());
		sql.append(" NOT LIKE 'sqlite_%'");

		if (name != null) {
			name = name.trim();
			if (!name.isEmpty()) {
				sql.append(" AND ");
				sql.append(SQLiteMasterColumn.NAME.name().toLowerCase());
				sql.append(" LIKE ");
				sql.append(CoreSQLUtils.quoteWrap(name));
			}
		}

		sql.append(" ORDER BY ");
		sql.append(SQLiteMasterColumn.NAME.name().toLowerCase());
		sql.append(";");

		return sql.toString();
	}

	/**
	 * Print header information
	 * 
	 * @param database
	 *            database
	 * @param maxRows
	 *            max rows
	 * @param maxColumnWidth
	 *            max column width
	 * @param maxLinesPerRow
	 *            max lines per row
	 */
	private static void printInfo(GeoPackage database, Integer maxRows,
			Integer maxColumnWidth, Integer maxLinesPerRow) {

		boolean isGeoPackage = isGeoPackage(database);

		System.out.println();
		if (isGeoPackage) {
			System.out.print("GeoPackage");
		} else {
			System.out.print("Database");
		}
		System.out.println(": " + database.getName());
		System.out.println("Path: " + database.getPath());
		System.out.println("Size: " + database.readableSize() + " ("
				+ database.size() + " bytes)");
		Integer applicationId = database.getApplicationIdInteger();
		if (applicationId != null) {
			System.out.println("Application ID: " + applicationId + ", "
					+ database.getApplicationIdHex() + ", "
					+ database.getApplicationId());
		}
		Integer userVersion = database.getUserVersion();
		if (userVersion != null) {
			System.out.println("User Version: " + userVersion);
		}
		System.out.println("Max Rows: " + printableValue(maxRows));
		System.out
				.println("Max Column Width: " + printableValue(maxColumnWidth));
		System.out.println(
				"Max Lines Per Row: " + printableValue(maxLinesPerRow));

	}

	/**
	 * Print the command prompt help
	 * 
	 * @param database
	 *            database
	 */
	private static void printHelp(GeoPackage database) {

		boolean isGeoPackage = isGeoPackage(database);

		System.out.println();
		System.out.println("- Supports most SQLite statements including:");
		System.out.println(
				"\tSELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, PRAGMA, VACUUM, etc");
		System.out.println("- Terminate SQL statements with a ;");
		System.out.println("- Exit with a single empty line");
		System.out.println();
		System.out.println("Commands:");
		System.out.println();
		System.out.println("\t" + COMMAND_TABLE_INFO + "              - "
				+ (isGeoPackage ? "GeoPackage" : "Database") + " information");
		System.out.println("\t" + COMMAND_HELP
				+ "              - print this help information");
		System.out.println("\t" + COMMAND_TABLES
				+ " [name]     - list database tables (all or LIKE table name)");
		System.out.println("\t" + COMMAND_INDEXES
				+ " [name]    - list database indexes (all or LIKE index name)");
		System.out.println("\t" + COMMAND_VIEWS
				+ " [name]      - list database views (all or LIKE view name)");
		System.out.println("\t" + COMMAND_TRIGGERS
				+ " [name]   - list database triggers (all or LIKE trigger name)");
		System.out.println("\t" + COMMAND_MAX_ROWS
				+ " [n]          - display or set the max rows per query");
		System.out.println("\t" + COMMAND_MAX_COLUMN_WIDTH
				+ " [n]         - display or set the max width (in characters) per column");
		System.out.println("\t" + COMMAND_MAX_LINES_PER_ROW
				+ " [n]         - display or set the max lines per row");
		System.out.println("\t" + COMMAND_HISTORY
				+ "           - list successfully executed sql commands");
		System.out.println("\t" + COMMAND_PREVIOUS
				+ "                - re-execute the previous successful sql command");
		System.out.println(
				"\t!n                - re-execute a sql statement by history id n");
		System.out.println(
				"\t!-n               - re-execute a sql statement n commands back in history");
		System.out.println("\t" + COMMAND_WRITE_BLOBS + " [" + ARGUMENT_PREFIX
				+ BLOBS_ARGUMENT_EXTENSION + " file_extension] ["
				+ ARGUMENT_PREFIX + BLOBS_ARGUMENT_DIRECTORY + " directory] ["
				+ ARGUMENT_PREFIX + BLOBS_ARGUMENT_PATTERN + " pattern]");
		System.out.println(
				"\t                  - write blobs from the previous successful sql command to the file system");
		System.out.println(
				"\t                        ([directory]|blobs)/table_name/column_name/(pk_values|result_index|[pattern])[.file_extension]");
		System.out.println(
				"\t                     file_extension - file extension added to each saved blob file");
		System.out.println(
				"\t                     directory      - base directory to save table_name/column_name/blobs (default is ./"
						+ BLOBS_WRITE_DEFAULT_DIRECTORY + ")");
		System.out.println(
				"\t                     pattern        - file directory and/or name pattern consisting of column names in parentheses");
		System.out.println(
				"\t                                       (column_name)-(column_name2)");
		System.out.println(
				"\t                                       (column_name)/(column_name2)");
		System.out.println("\t" + COMMAND_TABLE_INFO
				+ " <name>       - PRAGMA table_info(<name>);");
		System.out.println("\t" + COMMAND_SQLITE_MASTER
				+ "     - SELECT * FROM " + COMMAND_SQLITE_MASTER + ";");
		System.out.println("\t<name>            - SELECT * FROM <name>;");
		System.out.println("\t" + COMMAND_VACUUM
				+ "            - VACUUM [INTO 'filename'];");
		System.out.println("\t" + COMMAND_FOREIGN_KEYS
				+ "                - PRAGMA foreign_keys [= boolean];");
		System.out.println("\t" + COMMAND_FOREIGN_KEY_CHECK
				+ "               - PRAGMA foreign_key_check[(<table-name>)];");
		System.out.println("\t" + COMMAND_INTEGRITY_CHECK
				+ "         - PRAGMA integrity_check[(N)];");
		System.out.println("\t" + COMMAND_QUICK_CHECK
				+ "             - PRAGMA quick_check[(N)];");
		if (isGeoPackage) {
			System.out.println("\t" + COMMAND_CONTENTS
					+ " [name]   - List GeoPackage contents (all or LIKE table name)");
			System.out.println("\t" + ContentsDataType.ATTRIBUTES.getName()
					+ " [name] - List GeoPackage attributes tables (all or LIKE table name)");
			System.out.println("\t" + ContentsDataType.FEATURES.getName()
					+ " [name]   - List GeoPackage feature tables (all or LIKE table name)");
			System.out.println("\t" + ContentsDataType.TILES.getName()
					+ " [name]      - List GeoPackage tile tables (all or LIKE table name)");
			System.out.println("\t" + COMMAND_GEOPACKAGE_INFO
					+ " <name>      - Query GeoPackage metadata for the table name");
			System.out.println(
					"\t" + COMMAND_CONTENTS_BOUNDS + " [" + ARGUMENT_PREFIX
							+ ARGUMENT_PROJECTION + " projection] [name]");
			System.out.println(
					"\t                  - Determine the bounds (using only the contents) of the entire GeoPackage or single table name");
			System.out.println(
					"\t                     projection     - desired projection as 'authority:code' or 'epsg_code'");
			System.out.println("\t" + COMMAND_BOUNDS + " [" + ARGUMENT_PREFIX
					+ ARGUMENT_PROJECTION + " projection] [" + ARGUMENT_PREFIX
					+ BOUNDS_ARGUMENT_MANUAL + "] [name]");
			System.out.println(
					"\t                  - Determine the bounds of the entire GeoPackage or single table name");
			System.out.println(
					"\t                     projection     - desired projection as 'authority:code' or 'epsg_code'");
			System.out.println("\t                     "
					+ BOUNDS_ARGUMENT_MANUAL
					+ "              - manually query unindexed tables");
			System.out.println("\t" + COMMAND_TABLE_BOUNDS + " ["
					+ ARGUMENT_PREFIX + ARGUMENT_PROJECTION + " projection] ["
					+ ARGUMENT_PREFIX + BOUNDS_ARGUMENT_MANUAL + "] [name]");
			System.out.println(
					"\t                  - Determine the bounds (using only table metadata) of the entire GeoPackage or single table name");
			System.out.println(
					"\t                     projection     - desired projection as 'authority:code' or 'epsg_code'");
			System.out.println("\t                     "
					+ BOUNDS_ARGUMENT_MANUAL
					+ "              - manually query unindexed tables");
			System.out.println("\t" + COMMAND_EXTENSIONS
					+ " [name] - List GeoPackage extensions (all or LIKE table name)");
			System.out.println(
					"\t" + COMMAND_GEOMETRY + " <name> [-p projection] [ids]");
			System.out.println(
					"\t                  - Display feature table geometries as Well-Known Text");
			System.out.println(
					"\t                     projection     - desired display projection as 'authority:code' or 'epsg_code'");
			System.out.println(
					"\t                     ids            - single or comma delimited feature table row ids");
			System.out.println("\t" + COMMAND_GEOMETRY
					+ " <name> [-p projection] <id> <wkt>");
			System.out.println(
					"\t                  - Update or insert a feature table geometry with Well-Known Text");
			System.out.println(
					"\t                     projection     - Well-Known Text projection as 'authority:code' or 'epsg_code'");
			System.out.println(
					"\t                     id             - single feature table row id to update or -1 to insert a new row");
			System.out.println(
					"\t                     wkt            - Well-Known Text");
		}
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
		if (isGeoPackage) {
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
	 * @param maxColumnWidth
	 *            max column width
	 * @param maxLinesPerRow
	 *            max lines per row
	 * @param history
	 *            history
	 * @throws SQLException
	 *             upon error
	 */
	private static void executeSQL(GeoPackage database,
			StringBuilder sqlBuilder, int historyNumber, Integer maxRows,
			Integer maxColumnWidth, Integer maxLinesPerRow,
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
			executeSQL(database, sqlBuilder, sql, maxRows, maxColumnWidth,
					maxLinesPerRow, history);

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
	 * @param maxColumnWidth
	 *            max column width
	 * @param maxLinesPerRow
	 *            max lines per row
	 * @param history
	 *            history
	 * @throws SQLException
	 *             upon error
	 */
	private static void executeSQL(GeoPackage database,
			StringBuilder sqlBuilder, String sql, Integer maxRows,
			Integer maxColumnWidth, Integer maxLinesPerRow,
			List<String> history) throws SQLException {
		executeSQL(database, sqlBuilder, sql, maxRows, maxColumnWidth,
				maxLinesPerRow, history, true);
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
	 * @param maxColumnWidth
	 *            max column width
	 * @param maxLinesPerRow
	 *            max lines per row
	 * @param history
	 *            history
	 * @param resetCommandPrompt
	 *            reset command prompt
	 * @throws SQLException
	 *             upon error
	 */
	private static void executeSQL(GeoPackage database,
			StringBuilder sqlBuilder, String sql, Integer maxRows,
			Integer maxColumnWidth, Integer maxLinesPerRow,
			List<String> history, boolean resetCommandPrompt)
			throws SQLException {

		executeSQL(database, sqlBuilder, sql, maxRows, maxColumnWidth,
				maxLinesPerRow, history, resetCommandPrompt, true);

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
	 * @param maxColumnWidth
	 *            max column width
	 * @param maxLinesPerRow
	 *            max lines per row
	 * @param history
	 *            history
	 * @param resetCommandPrompt
	 *            reset command prompt
	 * @param printSides
	 *            true to print table sides
	 * @throws SQLException
	 *             upon error
	 */
	private static void executeSQL(GeoPackage database,
			StringBuilder sqlBuilder, String sql, Integer maxRows,
			Integer maxColumnWidth, Integer maxLinesPerRow,
			List<String> history, boolean resetCommandPrompt,
			boolean printSides) throws SQLException {
		executeSQL(database, sqlBuilder, sql, null, maxRows, maxColumnWidth,
				maxLinesPerRow, history, resetCommandPrompt, printSides);
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
	 * @param projection
	 *            desired projection
	 * @param maxRows
	 *            max rows
	 * @param maxColumnWidth
	 *            max column width
	 * @param maxLinesPerRow
	 *            max lines per row
	 * @param history
	 *            history
	 * @param resetCommandPrompt
	 *            reset command prompt
	 * @param printSides
	 *            true to print table sides
	 * @throws SQLException
	 *             upon error
	 */
	private static void executeSQL(GeoPackage database,
			StringBuilder sqlBuilder, String sql, Projection projection,
			Integer maxRows, Integer maxColumnWidth, Integer maxLinesPerRow,
			List<String> history, boolean resetCommandPrompt,
			boolean printSides) throws SQLException {

		SQLExecResult result = executeSQL(database, sql, projection, maxRows);

		printResult(result, sqlBuilder, sql, maxColumnWidth, maxLinesPerRow,
				history, resetCommandPrompt, printSides);

	}

	/**
	 * Print the result
	 * 
	 * @param result
	 *            result
	 * @param sqlBuilder
	 *            SQL builder
	 * @param sql
	 *            SQL statement
	 * @param maxColumnWidth
	 *            max column width
	 * @param maxLinesPerRow
	 *            max lines per row
	 * @param history
	 *            history
	 * @param resetCommandPrompt
	 *            reset command prompt
	 * @param printSides
	 *            true to print table sides
	 */
	private static void printResult(SQLExecResult result,
			StringBuilder sqlBuilder, String sql, Integer maxColumnWidth,
			Integer maxLinesPerRow, List<String> history,
			boolean resetCommandPrompt, boolean printSides) {

		setPrintOptions(result, maxColumnWidth, maxLinesPerRow);
		result.setPrintSides(printSides);
		result.printResults();

		history.add(sql);

		if (resetCommandPrompt) {
			resetCommandPrompt(sqlBuilder);
		}

	}

	/**
	 * Set print formatting options
	 * 
	 * @param result
	 *            result
	 * @param maxColumnWidth
	 *            max column width
	 * @param maxLinesPerRow
	 *            max lines per row
	 */
	private static void setPrintOptions(SQLExecResult result,
			Integer maxColumnWidth, Integer maxLinesPerRow) {

		// If no max column width, use the default
		if (maxColumnWidth == null) {
			maxColumnWidth = DEFAULT_MAX_COLUMN_WIDTH;
		}

		// If no max lines per row, use the default
		if (maxLinesPerRow == null) {
			maxLinesPerRow = DEFAULT_MAX_LINES_PER_ROW;
		}

		result.setMaxColumnWidth(maxColumnWidth);
		result.setMaxLinesPerRow(maxLinesPerRow);
	}

	/**
	 * Execute the SQL on the database
	 * 
	 * @param database
	 *            database
	 * @param sqlBuilder
	 *            SQL builder
	 * @param sql
	 *            SQL statement
	 * @param maxRows
	 *            max rows
	 * @param maxColumnWidth
	 *            max column width
	 * @param maxLinesPerRow
	 *            max lines per row
	 * @param history
	 *            history
	 * @param shortcut
	 *            shortcut command
	 * @param replacement
	 *            shortcut replacement command
	 * @throws SQLException
	 *             upon SQL error
	 */
	private static void executeShortcutSQL(GeoPackage database,
			StringBuilder sqlBuilder, String sql, Integer maxRows,
			Integer maxColumnWidth, Integer maxLinesPerRow,
			List<String> history, String shortcut, String replacement)
			throws SQLException {
		String replacedSql = replacement + sql.substring(shortcut.length());
		executeSQL(database, sqlBuilder, replacedSql, maxRows, maxColumnWidth,
				maxLinesPerRow, history);
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
	 * Execute the SQL on the GeoPackage database
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
		return executeSQL(database, sql, null, maxRows);
	}

	/**
	 * Execute the SQL on the GeoPackage database
	 * 
	 * @param database
	 *            open database
	 * @param sql
	 *            SQL statement
	 * @param projection
	 *            desired projection
	 * @param maxRows
	 *            max rows
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	public static SQLExecResult executeSQL(GeoPackage database, String sql,
			Projection projection, Integer maxRows) throws SQLException {

		sql = sql.trim();

		RTreeIndexExtension rtree = new RTreeIndexExtension(database);
		if (rtree.has()) {
			rtree.createAllFunctions();
		}

		SQLExecResult result = SQLExecAlterTable.alterTable(database, sql);
		if (result == null) {
			result = executeQuery(database, sql, projection, maxRows);
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
	 * @param projection
	 *            desired projection
	 * @param maxRows
	 *            max rows
	 * @return results
	 * @throws SQLException
	 *             upon SQL error
	 */
	private static SQLExecResult executeQuery(GeoPackage database, String sql,
			Projection projection, Integer maxRows) throws SQLException {

		SQLExecResult result = new SQLExecResult();

		if (!sql.equals(";")) {

			PreparedStatement statement = null;
			try {

				statement = database.getConnection().getConnection()
						.prepareStatement(sql);

				if (maxRows != null) {
					statement.setMaxRows(maxRows);
					result.setMaxRows(maxRows);
				}

				boolean hasResultSet = statement.execute();

				if (hasResultSet) {

					ResultSet resultSet = statement.getResultSet();

					ResultSetMetaData metadata = resultSet.getMetaData();
					int numColumns = metadata.getColumnCount();

					int[] columnWidths = new int[numColumns];
					int[] columnTypes = new int[numColumns];

					boolean isGeoPackage = isGeoPackage(database);
					Map<String, GeometryColumns> tableGeometryColumns = new HashMap<>();
					Map<Integer, ProjectionTransform> geometryColumns = new HashMap<>();

					for (int col = 1; col <= numColumns; col++) {
						String tableName = metadata.getTableName(col);
						result.addTable(tableName);
						String columnName = metadata.getColumnName(col);
						result.addColumn(columnName);
						columnTypes[col - 1] = metadata.getColumnType(col);
						columnWidths[col - 1] = columnName.length();

						// Determine if the column is a GeoPackage geometry
						if (isGeoPackage) {
							GeometryColumns geometryColumn = null;
							if (tableGeometryColumns.containsKey(tableName)) {
								geometryColumn = tableGeometryColumns
										.get(tableName);
							} else {
								if (database.isFeatureTable(tableName)) {
									geometryColumn = database
											.getGeometryColumnsDao()
											.queryForTableName(tableName);
								}
								tableGeometryColumns.put(tableName,
										geometryColumn);
							}
							if (geometryColumn != null
									&& geometryColumn.getColumnName()
											.equalsIgnoreCase(columnName)) {
								ProjectionTransform transform = null;
								if (projection != null) {
									transform = geometryColumn.getProjection()
											.getTransformation(projection);
								}
								geometryColumns.put(col, transform);
							}
						}
					}

					while (resultSet.next()) {

						List<String> row = new ArrayList<>();
						result.addRow(row);
						for (int col = 1; col <= numColumns; col++) {

							String stringValue = null;
							Object value = resultSet.getObject(col);

							if (value != null) {

								switch (columnTypes[col - 1]) {
								case Types.BLOB:
									stringValue = BLOB_DISPLAY_VALUE;
									if (geometryColumns.containsKey(col)) {
										ProjectionTransform transform = geometryColumns
												.get(col);
										byte[] bytes = (byte[]) value;
										try {
											if (transform == null) {
												stringValue = GeoPackageGeometryData
														.wkt(bytes);
											} else {
												GeoPackageGeometryData geometryData = GeoPackageGeometryData
														.create(bytes);
												stringValue = geometryData
														.transform(transform)
														.getWkt();
											}
										} catch (IOException e) {
										}
									}
									break;
								default:
									stringValue = value.toString().replaceAll(
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
							if (i + 1 < argParts.length) {
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
							if (i + 1 < argParts.length) {
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
							if (i + 1 < argParts.length) {
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

				Set<String> blobsWritten = new LinkedHashSet<>();
				int blobsWrittenCount = 0;

				PreparedStatement statement = null;
				try {

					statement = database.getConnection().getConnection()
							.prepareStatement(sql);
					if (maxRows != null) {
						statement.setMaxRows(maxRows);
					}

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
	 * Print tile matrix info for a single table
	 * 
	 * @param database
	 *            database
	 * @param sqlBuilder
	 *            sql builder
	 * @param maxColumnWidth
	 *            max column width
	 * @param maxLinesPerRow
	 *            max lines per row
	 * @param history
	 *            history
	 * @param tableName
	 *            table name
	 * @throws SQLException
	 *             upon error
	 */
	private static void tileMatrix(GeoPackage database,
			StringBuilder sqlBuilder, Integer maxColumnWidth,
			Integer maxLinesPerRow, List<String> history, String tableName)
			throws SQLException {

		String sql = "SELECT * FROM gpkg_tile_matrix WHERE table_name = '"
				+ tableName + "';";

		SQLExecResult result = executeSQL(database, sql, COMMAND_ALL_ROWS);

		// Add map zoom levels
		TileDao tileDao = database.getTileDao(tableName);
		int zoomColumn = result.getColumns()
				.indexOf(TileMatrix.COLUMN_ZOOM_LEVEL);
		int mapZoomColumn = zoomColumn + 1;
		String mapZoomColumnName = "map_zoom_level";
		int mapZoomColumnWidth = mapZoomColumnName.length();
		for (int i = 0; i < result.numRows(); i++) {
			long zoom = Long.parseLong(result.getValue(i, zoomColumn));
			String mapZoom = Long.toString(tileDao.getMapZoom(zoom));
			mapZoomColumnWidth = Math.max(mapZoomColumnWidth, mapZoom.length());
			result.addRowValue(i, mapZoomColumn, mapZoom);
		}
		result.addColumn(mapZoomColumn, mapZoomColumnName);
		result.addColumnWidth(mapZoomColumn, mapZoomColumnWidth);

		printResult(result, sqlBuilder, sql, maxColumnWidth, maxLinesPerRow,
				history, false, true);
	}

	/**
	 * Print or update geometries
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
	private static void geometries(GeoPackage database,
			StringBuilder sqlBuilder, Integer maxRows, List<String> history,
			String args) throws SQLException, IOException {

		String[] parts = args.trim().split("\\s+");

		boolean valid = true;
		String tableName = null;
		String ids = null;
		Long singleId = null;
		Projection projection = null;
		StringBuilder wktBuilder = null;

		for (int i = 0; valid && i < parts.length; i++) {

			String arg = parts[i];

			if (arg.startsWith(ARGUMENT_PREFIX) && !arg.equals("-1")) {

				if (wktBuilder != null) {
					valid = false;
					System.out.println("Error: Unexpected argument '" + arg
							+ "' after expected Well-Known Text: "
							+ wktBuilder.toString());
				} else {

					String argument = arg.substring(ARGUMENT_PREFIX.length());

					switch (argument) {

					case ARGUMENT_PROJECTION:
						if (i + 1 < parts.length) {
							String projectionArugment = parts[++i];
							projection = getProjection(projectionArugment);
							if (projection == null) {
								valid = false;
							}
						} else {
							valid = false;
							System.out.println("Error: Projection argument '"
									+ arg
									+ "' must be followed by 'authority:code' or 'epsg_code'");
						}
						break;

					default:
						valid = false;
						System.out.println(
								"Error: Unsupported arg: '" + arg + "'");
					}
				}

			} else {
				if (tableName == null) {
					tableName = arg;
					if (!database.isFeatureTable(tableName)) {
						valid = false;
						if (tableName.isEmpty()) {
							System.out.println("Error: Feature table required");
						} else {
							System.out.println("Error: '" + tableName
									+ "' is not a feature table");
						}
					}
				} else if (ids == null) {
					ids = arg;
					if (ids.indexOf(",") == -1) {
						try {
							singleId = Long.parseLong(ids);
						} catch (NumberFormatException e) {
							valid = false;
							System.out.println("Error: Invalid single row id '"
									+ ids + "'");
						}
					}
				} else if (singleId == null) {
					valid = false;
					System.out.println("Error: Unexpected additional argument '"
							+ arg + "' for multiple id query");
				} else {
					if (wktBuilder == null) {
						wktBuilder = new StringBuilder();
					} else {
						wktBuilder.append(" ");
					}
					wktBuilder.append(arg);
				}
			}
		}

		if (tableName == null) {
			valid = false;
			System.out.println("Error: Feature table required");
		}

		if (valid) {

			FeatureDao featureDao = database.getFeatureDao(tableName);

			if (wktBuilder != null) {

				String wkt = wktBuilder.toString().trim();

				if (wkt.startsWith("'") || wkt.startsWith("\"")) {
					wkt = wkt.substring(1);
				}
				if (wkt.endsWith("'") || wkt.endsWith("\"")) {
					wkt = wkt.substring(0, wkt.length() - 1);
				}

				GeoPackageGeometryData geometryData = GeoPackageGeometryData
						.createFromWktAndBuildEnvelope(featureDao.getSrsId(),
								wkt);

				if (projection != null) {
					ProjectionTransform transform = projection
							.getTransformation(featureDao.getProjection());
					if (!transform.isSameProjection()) {
						geometryData = geometryData.transform(transform);
					}
				}

				FeatureRow featureRow = null;

				if (singleId == -1) {

					featureRow = featureDao.newRow();
					featureRow.setGeometry(geometryData);
					singleId = featureDao.insert(featureRow);
					System.out.println();
					System.out.println("Inserted Row Id: " + singleId);

				} else {

					featureRow = featureDao.queryForIdRow(singleId);

					if (featureRow != null) {
						featureRow.setGeometry(geometryData);
						featureDao.update(featureRow);
						System.out.println();
						System.out.println("Updated Row Id: " + singleId);
					} else {
						System.out.println(
								"Error: No row found for feature table '"
										+ tableName + "' with id '" + singleId
										+ "'");
					}

				}

				if (featureRow != null) {
					featureRow = featureDao.queryForIdRow(singleId);
					if (featureRow != null) {
						printGeometryData(database, featureDao,
								featureRow.getGeometry());
					}
				}

			} else {

				StringBuilder sql = new StringBuilder("SELECT "
						+ CoreSQLUtils
								.quoteWrap(featureDao.getGeometryColumnName())
						+ " FROM " + CoreSQLUtils.quoteWrap(tableName));

				if (ids != null) {

					sql.append(" WHERE "
							+ CoreSQLUtils
									.quoteWrap(featureDao.getIdColumnName())
							+ " IN (" + ids + ")");
				}

				GeoPackageGeometryData geometryData = null;

				if (singleId != null) {

					FeatureRow featureRow = featureDao.queryForIdRow(singleId);
					if (featureRow != null) {

						geometryData = featureRow.getGeometry();

						if (projection != null) {
							ProjectionTransform transform = featureDao
									.getProjection()
									.getTransformation(projection);
							if (!transform.isSameProjection()) {
								geometryData = geometryData
										.transform(transform);
							}
						}

					}

				}

				printGeometryDataHeader(database, featureDao, geometryData,
						projection);

				executeSQL(database, sqlBuilder, sql.toString(), projection,
						COMMAND_ALL_ROWS, 0, 0, history, false, false);
			}
		}

		resetCommandPrompt(sqlBuilder);
	}

	/**
	 * Print the geometry data
	 * 
	 * @param geometryData
	 *            geometry data
	 * @throws SQLException
	 *             upon error
	 */
	private static void printGeometryData(GeoPackage database,
			FeatureDao featureDao, GeoPackageGeometryData geometryData)
			throws SQLException {
		printGeometryDataHeader(database, featureDao, geometryData, null);
		System.out.println();
		System.out.println(geometryData.getWkt());
	}

	/**
	 * Print the geometry data header
	 * 
	 * @param database
	 *            database
	 * @param featureDao
	 *            feature dao
	 * @param geometryData
	 *            geometry data
	 * @param projection
	 *            transformed projection
	 * @throws SQLException
	 *             upon error
	 */
	private static void printGeometryDataHeader(GeoPackage database,
			FeatureDao featureDao, GeoPackageGeometryData geometryData,
			Projection projection) throws SQLException {
		System.out.println();

		if (projection == null && geometryData != null) {
			int srsId = geometryData.getSrsId();
			SpatialReferenceSystem geometrySrs = database
					.getSpatialReferenceSystemDao().queryForId((long) srsId);
			if (geometrySrs != null) {
				projection = geometrySrs.getProjection();
			}
		}

		if (projection != null) {
			System.out.println("Projection: " + projection);
		}

		Projection featureProjection = featureDao.getProjection();
		if (projection == null || !projection.equals(featureProjection)) {
			if (projection != null) {
				System.out.print("Table ");
			}
			System.out.println("Projection: " + featureProjection);
		}

		if (geometryData != null) {

			GeometryEnvelope envelope = geometryData.getEnvelope();
			if (envelope != null) {
				System.out.print("Geometry ");
				printGeometryEnvelope(envelope);
			}

			Geometry geometry = geometryData.getGeometry();
			GeometryEnvelope builtEnvelope = GeometryEnvelopeBuilder
					.buildEnvelope(geometry);
			if (builtEnvelope != null
					&& (envelope == null || !envelope.equals(builtEnvelope))) {
				System.out.print("Calculated ");
				printGeometryEnvelope(builtEnvelope);
			}

		}

	}

	/**
	 * Print the geometry envelope
	 * 
	 * @param envelope
	 *            geometry envelope
	 */
	private static void printGeometryEnvelope(GeometryEnvelope envelope) {
		System.out.println("Envelope");
		System.out.println("   min x: " + envelope.getMinX());
		System.out.println("   min y: " + envelope.getMinY());
		System.out.println("   max x: " + envelope.getMaxX());
		System.out.println("   max y: " + envelope.getMaxY());
		if (envelope.hasZ()) {
			System.out.println("   min z: " + envelope.getMinZ());
			System.out.println("   max z: " + envelope.getMaxZ());
		}
		if (envelope.hasM()) {
			System.out.println("   min m: " + envelope.getMinM());
			System.out.println("   max m: " + envelope.getMaxM());
		}
	}

	/**
	 * Get the projection from the projection authority:code or epsg_code
	 * argument
	 * 
	 * @param argument
	 *            projection argument
	 * @return projection or null
	 */
	private static Projection getProjection(String argument) {

		Projection projection = null;

		String authority = null;
		String code = null;

		String[] projectionParts = argument.split(":");

		switch (projectionParts.length) {
		case 1:
			authority = ProjectionConstants.AUTHORITY_EPSG;
			code = projectionParts[0];
			break;
		case 2:
			authority = projectionParts[0];
			code = projectionParts[1];
			break;
		default:
			System.out.println("Error: Invalid projection argument '" + argument
					+ "', expected 'authority:code' or 'epsg_code'");
		}

		if (authority != null) {
			projection = ProjectionFactory.getProjection(authority, code);
		}

		return projection;
	}

	/**
	 * Determine the bounds
	 * 
	 * @param database
	 *            database
	 * @param sqlBuilder
	 *            SQL builder
	 * @param type
	 *            bounds type
	 * @param args
	 *            bounds arguments
	 */
	private static void bounds(GeoPackage database, StringBuilder sqlBuilder,
			BoundsType type, String args) {

		boolean valid = true;

		String table = null;
		Projection projection = null;
		boolean manual = false;

		if (args != null && !args.isEmpty()) {

			String[] argParts = args.trim().split("\\s+");

			for (int i = 0; valid && i < argParts.length; i++) {

				String arg = argParts[i];

				if (arg.startsWith(ARGUMENT_PREFIX)) {

					String argument = arg.substring(ARGUMENT_PREFIX.length());

					switch (argument) {

					case ARGUMENT_PROJECTION:
						if (i + 1 < argParts.length) {
							String projectionArugment = argParts[++i];
							projection = getProjection(projectionArugment);
							if (projection == null) {
								valid = false;
							}
						} else {
							valid = false;
							System.out.println("Error: Projection argument '"
									+ arg
									+ "' must be followed by 'authority:code' or 'epsg_code'");
						}
						break;

					case BOUNDS_ARGUMENT_MANUAL:
						if (type == BoundsType.CONTENTS) {
							valid = false;
							System.out.println(
									"Error: Unsupported arg: '" + arg + "'");
						} else {
							manual = true;
						}
						break;

					default:
						valid = false;
						System.out.println(
								"Error: Unsupported arg: '" + arg + "'");
					}

				} else {
					if (table == null) {
						table = arg;
						if (!database.isContentsTable(table)) {
							valid = false;
							System.out.println("Error: Not a contents table: '"
									+ table + "'");
						}
					} else {
						valid = false;
						System.out.println(
								"Error: Unsupported arg: '" + arg + "'");
					}
				}
			}
		}

		if (valid) {

			BoundingBox bounds = null;

			if (table != null) {

				switch (type) {
				case CONTENTS:
					if (projection != null) {
						bounds = database.getContentsBoundingBox(projection,
								table);
					} else {
						bounds = database.getContentsBoundingBox(table);
						projection = database.getContentsProjection(table);
					}
					break;
				case ALL:
					if (projection != null) {
						bounds = database.getBoundingBox(projection, table,
								manual);
					} else {
						bounds = database.getBoundingBox(table, manual);
						projection = database.getProjection(table);
					}
					break;
				case TABLE:
					if (projection != null) {
						bounds = database.getTableBoundingBox(projection, table,
								manual);
					} else {
						bounds = database.getTableBoundingBox(table, manual);
						projection = database.getProjection(table);
					}
					break;
				default:
					throw new GeoPackageException("Unsupported type: " + type);
				}

			} else {

				if (projection == null) {
					projection = ProjectionFactory.getProjection(
							ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
				}

				switch (type) {
				case CONTENTS:
					bounds = database.getContentsBoundingBox(projection);
					break;
				case ALL:
					bounds = database.getBoundingBox(projection, manual);
					break;
				case TABLE:
					bounds = database.getTableBoundingBox(projection, manual);
					break;
				default:
					throw new GeoPackageException("Unsupported type: " + type);
				}

			}

			printBounds(table, projection, bounds);

		}

		resetCommandPrompt(sqlBuilder);
	}

	/**
	 * Print table bounds
	 * 
	 * @param table
	 *            table name
	 * @param projection
	 *            bounds projection
	 * @param bounds
	 *            bounding box bounds
	 */
	private static void printBounds(String table, Projection projection,
			BoundingBox bounds) {

		SQLExecResult result = new SQLExecResult();
		result.addTable(table);

		List<String> columns = new ArrayList<>();
		columns.add("Projection");
		columns.add("Min Longitude");
		columns.add("Min Latitude");
		columns.add("Max Longitude");
		columns.add("Max Latitude");

		result.addColumns(columns);

		int[] columnWidths = new int[columns.size()];
		for (int i = 0; i < columnWidths.length; i++) {
			columnWidths[i] = columns.get(i).length();
		}

		String proj = null;
		if (projection != null) {
			proj = projection.getAuthority() + ":" + projection.getCode();
		}

		String minLon = null;
		String minLat = null;
		String maxLon = null;
		String maxLat = null;

		if (bounds != null) {
			DecimalFormat df = new DecimalFormat("0",
					DecimalFormatSymbols.getInstance(Locale.ENGLISH));
			df.setMaximumFractionDigits(340);
			minLon = df.format(bounds.getMinLongitude());
			minLat = df.format(bounds.getMinLatitude());
			maxLon = df.format(bounds.getMaxLongitude());
			maxLat = df.format(bounds.getMaxLatitude());
		}

		List<String> row = new ArrayList<>();
		row.add(proj);
		row.add(minLon);
		row.add(minLat);
		row.add(maxLon);
		row.add(maxLat);

		result.addRow(row);

		for (int i = 0; i < columnWidths.length; i++) {
			String value = row.get(i);
			if (value != null) {
				columnWidths[i] = Math.max(columnWidths[i], value.length());
			}
		}

		result.addColumnWidths(columnWidths);

		result.printResults();
	}

	/**
	 * Print usage for the main method
	 */
	private static void printUsage() {
		System.out.println();
		System.out.println("USAGE");
		System.out.println();
		System.out.println("\t[" + ARGUMENT_PREFIX + ARGUMENT_MAX_ROWS
				+ " max_rows] [" + ARGUMENT_PREFIX + ARGUMENT_MAX_COLUMN_WIDTH
				+ " max_column_width] [" + ARGUMENT_PREFIX
				+ ARGUMENT_MAX_LINES_PER_ROW
				+ " max_lines_per_row] sqlite_file [sql]");
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
		System.out.println("\t\tMax rows per query" + " (Default is "
				+ printableValue(DEFAULT_MAX_ROWS) + ")");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_MAX_COLUMN_WIDTH
				+ " max_column_width");
		System.out.println(
				"\t\tMax width (in characters) per column" + " (Default is "
						+ printableValue(DEFAULT_MAX_COLUMN_WIDTH) + ")");
		System.out.println();
		System.out.println("\t" + ARGUMENT_PREFIX + ARGUMENT_MAX_LINES_PER_ROW
				+ " max_lines_per_row");
		System.out.println("\t\tMax lines per row" + " (Default is "
				+ printableValue(DEFAULT_MAX_LINES_PER_ROW) + ")");
		System.out.println();
		System.out.println("\tsqlite_file");
		System.out.println("\t\tpath to the SQLite database file");
		System.out.println();
		System.out.println("\tsql");
		System.out.println("\t\tSQL statement to execute");
		System.out.println();
	}

	/**
	 * Get a printable string for the integer value
	 * 
	 * @param value
	 *            value
	 * @return string
	 */
	private static String printableValue(Integer value) {
		return (value != null && value > 0) ? value.toString() : "0 = none";
	}

	/**
	 * Check if the SQLite database is a GeoPackage
	 * 
	 * @param database
	 *            SQLite database
	 * @return true if a GeoPackage
	 */
	public static boolean isGeoPackage(GeoPackage database) {
		return GeoPackageValidate.hasMinimumTables(database);
	}

}
