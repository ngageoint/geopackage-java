package mil.nga.geopackage.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Result from {@link SQLExec} containing tables, columns, column widths, rows,
 * and values
 * 
 * @author osbornb
 * @since 3.3.0
 */
public class SQLExecResult {

	/**
	 * Result tables
	 */
	private Set<String> tables = new LinkedHashSet<>();

	/**
	 * Result columns
	 */
	private List<String> columns = new ArrayList<>();

	/**
	 * Max column width for any value, used when printing
	 */
	private Integer maxColumnWidth = null;

	/**
	 * Max number of printed lines per row, used when printing
	 */
	private Integer maxLinesPerRow = null;

	/**
	 * Result column formatting widths
	 */
	private List<Integer> columnWidths = new ArrayList<>();

	/**
	 * Rows with column values (as strings)
	 */
	private List<List<String>> rows = new ArrayList<>();

	/**
	 * Update count
	 */
	private Integer updateCount = null;

	/**
	 * Max rows queried
	 */
	private Integer maxRows = null;

	/**
	 * Print sides flag
	 */
	private boolean printSides = true;

	/**
	 * Constructor
	 */
	public SQLExecResult() {

	}

	/**
	 * Add a table
	 * 
	 * @param table
	 *            table name
	 */
	public void addTable(String table) {
		if (table != null && !table.trim().isEmpty()) {
			tables.add(table.trim());
		}
	}

	/**
	 * Get the tables
	 * 
	 * @return table names
	 */
	public List<String> getTables() {
		return new ArrayList<>(tables);
	}

	/**
	 * Add a column
	 * 
	 * @param column
	 *            column name
	 */
	public void addColumn(String column) {
		columns.add(column);
	}

	/**
	 * Add a column
	 * 
	 * @param index
	 *            column index
	 * @param column
	 *            column name
	 * @since 5.0.0
	 */
	public void addColumn(int index, String column) {
		columns.add(index, column);
	}

	/**
	 * Add columns
	 * 
	 * @param columns
	 *            column names
	 */
	public void addColumns(Collection<String> columns) {
		this.columns.addAll(columns);
	}

	/**
	 * Get the number of columns
	 * 
	 * @return column count
	 */
	public int numColumns() {
		return columns.size();
	}

	/**
	 * Get the columns
	 * 
	 * @return column names
	 */
	public List<String> getColumns() {
		return columns;
	}

	/**
	 * Get the column name at the index
	 * 
	 * @param index
	 *            column index
	 * @return column name
	 */
	public String getColumn(int index) {
		return columns.get(index);
	}

	/**
	 * Determine if the results have any columns
	 * 
	 * @return true if has columns
	 */
	public boolean hasColumns() {
		return numColumns() > 0;
	}

	/**
	 * Get the max column width
	 * 
	 * @return max column width
	 * @since 4.0.0
	 */
	public Integer getMaxColumnWidth() {
		return maxColumnWidth;
	}

	/**
	 * Set the max column width
	 * 
	 * @param maxColumnWidth
	 *            max column width
	 * @since 4.0.0
	 */
	public void setMaxColumnWidth(Integer maxColumnWidth) {
		if (maxColumnWidth != null && maxColumnWidth <= 0) {
			maxColumnWidth = null;
		}
		this.maxColumnWidth = maxColumnWidth;
		if (maxColumnWidth != null) {
			for (int i = 0; i < columnWidths.size(); i++) {
				Integer width = columnWidths.get(i);
				if (width != null && maxColumnWidth < width) {
					columnWidths.set(i, maxColumnWidth);
				}
			}
		}
	}

	/**
	 * Get the max lines per row
	 * 
	 * @return max lines per row
	 * @since 4.0.0
	 */
	public Integer getMaxLinesPerRow() {
		return maxLinesPerRow;
	}

	/**
	 * Set the max lines per row
	 * 
	 * @param maxLinesPerRow
	 *            max lines per row
	 * @since 4.0.0
	 */
	public void setMaxLinesPerRow(Integer maxLinesPerRow) {
		if (maxLinesPerRow != null && maxLinesPerRow <= 0) {
			maxLinesPerRow = null;
		}
		this.maxLinesPerRow = maxLinesPerRow;
	}

	/**
	 * Add a column width
	 * 
	 * @param width
	 *            column width
	 */
	public void addColumnWidth(int width) {
		if (maxColumnWidth != null) {
			width = Math.min(width, maxColumnWidth);
		}
		columnWidths.add(width);
	}

	/**
	 * Add a column width
	 * 
	 * @param index
	 *            column index
	 * @param width
	 *            column width
	 * @since 5.0.0
	 */
	public void addColumnWidth(int index, int width) {
		if (maxColumnWidth != null) {
			width = Math.min(width, maxColumnWidth);
		}
		columnWidths.add(index, width);
	}

	/**
	 * Add the column widths
	 * 
	 * @param widths
	 *            column widths
	 */
	public void addColumnWidths(int[] widths) {
		for (int width : widths) {
			addColumnWidth(width);
		}
	}

	/**
	 * Get the column widths
	 * 
	 * @return column widths
	 */
	public List<Integer> getColumnWidths() {
		return columnWidths;
	}

	/**
	 * Get the column width at the index
	 * 
	 * @param index
	 *            column index
	 * @return column width
	 */
	public int getColumnWidth(int index) {
		return columnWidths.get(index);
	}

	/**
	 * Add a row
	 * 
	 * @param row
	 *            result row
	 */
	public void addRow(List<String> row) {
		rows.add(row);
	}

	/**
	 * Get the number of rows
	 * 
	 * @return number of rows
	 */
	public int numRows() {
		return rows.size();
	}

	/**
	 * Determine if the results have any rows
	 * 
	 * @return true if has rows
	 */
	public boolean hasRows() {
		return numRows() > 0;
	}

	/**
	 * Get the rows
	 * 
	 * @return result rows
	 */
	public List<List<String>> getRows() {
		return rows;
	}

	/**
	 * Get the row at the row index
	 * 
	 * @param index
	 *            row index
	 * @return result row
	 */
	public List<String> getRow(int index) {
		return rows.get(index);
	}

	/**
	 * Add a row value
	 * 
	 * @param index
	 *            row index
	 * @param value
	 *            value
	 * @since 5.0.0
	 */
	public void addRowValue(int index, String value) {
		rows.get(index).add(value);
	}

	/**
	 * Add a row value
	 * 
	 * @param index
	 *            row index
	 * @param columnIndex
	 *            column index
	 * @param value
	 *            value
	 * @since 5.0.0
	 */
	public void addRowValue(int index, int columnIndex, String value) {
		rows.get(index).add(columnIndex, value);
	}

	/**
	 * Get the value at the row and column index
	 * 
	 * @param rowIndex
	 *            row index
	 * @param columnIndex
	 *            column index
	 * @return value as a string
	 */
	public String getValue(int rowIndex, int columnIndex) {
		return getRow(rowIndex).get(columnIndex);
	}

	/**
	 * Set the update count
	 * 
	 * @param updateCount
	 *            update count
	 */
	public void setUpdateCount(Integer updateCount) {
		this.updateCount = updateCount;
	}

	/**
	 * Get the update count
	 * 
	 * @return update count
	 */
	public Integer getUpdateCount() {
		return updateCount;
	}

	/**
	 * Check if has an update count
	 * 
	 * @return true if has an update count
	 */
	public boolean hasUpdateCount() {
		return getUpdateCount() != null;
	}

	/**
	 * Set the max rows queried
	 * 
	 * @param maxRows
	 *            max rows
	 */
	public void setMaxRows(Integer maxRows) {
		if (maxRows != null && maxRows <= 0) {
			maxRows = null;
		}
		this.maxRows = maxRows;
	}

	/**
	 * Get the max rows queried
	 * 
	 * @return max rows
	 */
	public Integer getMaxRows() {
		return maxRows;
	}

	/**
	 * Determine if the result has the max number of results
	 * 
	 * @return true if max results
	 */
	public boolean hasMaxResults() {
		return maxRows != null && numRows() >= maxRows;
	}

	/**
	 * Is the print sides flag enabled
	 * 
	 * @return true to print sides
	 */
	public boolean isPrintSides() {
		return printSides;
	}

	/**
	 * Set the print sides flag
	 * 
	 * @param printSides
	 *            true to print sides
	 */
	public void setPrintSides(boolean printSides) {
		this.printSides = printSides;
	}

	/**
	 * Print the results using {@link System#out}
	 */
	public void printResults() {

		System.out.println();

		if (hasColumns()) {

			int width = 0;

			for (int columnWidth : columnWidths) {
				width += columnWidth;
			}

			// Add dividers
			width += numColumns() - 1;
			if (printSides) {
				width += 2;
			}

			// Add space buffers
			width += (2 * (numColumns() - 1));
			if (printSides) {
				width += 2;
			}

			printTables();

			printHorizontalDivider(width);

			printColumns();

			printHorizontalDivider(width);

			printRows(width);

			printHorizontalDivider(width);

			printRowCount();

		} else if (hasUpdateCount()) {
			System.out.println("Update Count: " + getUpdateCount());
		} else {
			System.out.println("No Results");
		}

	}

	/**
	 * Print the row count
	 */
	private void printRowCount() {
		System.out.println(
				"Rows: " + numRows() + (hasMaxResults() ? " (max)" : ""));
	}

	/**
	 * Print the table header
	 */
	private void printTables() {
		List<String> tables = getTables();
		if (!tables.isEmpty()) {
			System.out.print("Table");
			if (tables.size() > 1) {
				System.out.print("s");
			}
			System.out.print(": ");
			for (int i = 0; i < tables.size(); i++) {
				if (i > 0) {
					System.out.print(", ");
				}
				System.out.print(tables.get(i));
			}
			System.out.println();
		}
	}

	/**
	 * Print the column header
	 */
	private void printColumns() {
		for (int col = 0; col < numColumns(); col++) {
			if (col > 0 || printSides) {
				printVerticalDivider();
				printSpace();
			}
			String column = getColumn(col);
			System.out.print(column);
			if (col + 1 < numColumns() || printSides) {
				int width = getColumnWidth(col);
				printSpace(width - column.length());
				printSpace();
			}
		}
		if (printSides) {
			printVerticalDivider();
		}
		System.out.println();
	}

	/**
	 * Print the result rows
	 * 
	 * @param width
	 *            table width
	 */
	private void printRows(int width) {
		if (hasRows()) {
			for (int row = 0; row < numRows(); row++) {
				printRow(row);
			}
		} else if (printSides) {
			printVerticalDivider();
			printSpace(width - 2);
			printVerticalDivider();
			System.out.println();
		}
	}

	/**
	 * Print the row at the row index
	 * 
	 * @param index
	 *            row index
	 */
	private void printRow(int index) {
		printRowValues(getRow(index), 1);
	}

	/**
	 * Print a single line for row values
	 * 
	 * @param values
	 *            row values
	 * @param line
	 *            number within the current row
	 */
	private void printRowValues(List<String> values, int line) {

		List<String> nextLine = null;

		int numColumns = numColumns();
		for (int col = 0; col < numColumns; col++) {

			if (col > 0 || printSides) {
				if (col > 0) {
					printSpace();
				}
				printVerticalDivider();
				printSpace();
			}

			String value = values.get(col);

			// Check if the value is longer than the max column width
			if (value != null && maxColumnWidth != null
					&& value.length() > maxColumnWidth) {

				if (maxLinesPerRow == null || line < maxLinesPerRow) {

					String nextLineValue = value.substring(maxColumnWidth);
					value = value.substring(0, maxColumnWidth);

					if (nextLine == null) {
						nextLine = new ArrayList<String>();
						for (int i = 0; i < values.size(); i++) {
							nextLine.add(null);
						}
					}

					nextLine.set(col, nextLineValue);

				} else {
					// end with HORIZONTAL ELLIPSIS
					value = value.substring(0, maxColumnWidth - 1) + "\u2026";
				}

			}

			int width = getColumnWidth(col);
			int valueLength = 0;
			if (value != null) {
				System.out.print(value);
				valueLength = value.length();
			}
			if (printSides || numColumns > 1) {
				printSpace(width - valueLength);
			}

		}

		if (printSides) {
			printSpace();
			printVerticalDivider();
		}
		System.out.println();

		if (nextLine != null) {
			printRowValues(nextLine, line + 1);
		}

	}

	/**
	 * Print a horizontal divider
	 * 
	 * @param width
	 *            divider width
	 */
	private void printHorizontalDivider(int width) {
		for (int i = 0; i < width; i++) {
			printHorizontalDivider();
		}
		System.out.println();
	}

	/**
	 * Print a single horizontal divider character
	 */
	private void printHorizontalDivider() {
		System.out.print("-");
	}

	/**
	 * Print a vertical divider character
	 */
	private void printVerticalDivider() {
		System.out.print("|");
	}

	/**
	 * Print spacing
	 * 
	 * @param width
	 *            spacing width
	 */
	private void printSpace(int width) {
		for (int i = 0; i < width; i++) {
			printSpace();
		}
	}

	/**
	 * Print a single character space
	 */
	private void printSpace() {
		System.out.print(" ");
	}

}
