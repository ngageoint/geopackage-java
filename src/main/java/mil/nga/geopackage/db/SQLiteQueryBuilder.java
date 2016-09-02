package mil.nga.geopackage.db;

import java.util.regex.Pattern;

/**
 * Sections taken from android.database.sqlite.SQLiteQueryBuilder in the Android
 * Open Source Project and slightly modified.
 *
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
public class SQLiteQueryBuilder {

	private static final Pattern sLimitPattern = Pattern
			.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");

	/**
	 * Build an SQL query string from the given clauses.
	 *
	 * @param distinct
	 *            true if you want each row to be unique, false otherwise.
	 * @param table
	 *            The table name to compile the query against.
	 * @param columns
	 *            A list of which columns to return. Passing null will return
	 *            all columns, which is discouraged to prevent reading data from
	 *            storage that isn't going to be used.
	 * @param where
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause (excluding the WHERE itself). Passing null will
	 *            return all rows for the given URL.
	 * @param groupBy
	 *            A filter declaring how to group rows, formatted as an SQL
	 *            GROUP BY clause (excluding the GROUP BY itself). Passing null
	 *            will cause the rows to not be grouped.
	 * @param having
	 *            A filter declare which row groups to include in the cursor, if
	 *            row grouping is being used, formatted as an SQL HAVING clause
	 *            (excluding the HAVING itself). Passing null will cause all row
	 *            groups to be included, and is required when row grouping is
	 *            not being used.
	 * @param orderBy
	 *            How to order the rows, formatted as an SQL ORDER BY clause
	 *            (excluding the ORDER BY itself). Passing null will use the
	 *            default sort order, which may be unordered.
	 * @param limit
	 *            Limits the number of rows returned by the query, formatted as
	 *            LIMIT clause. Passing null denotes no LIMIT clause.
	 * @return the SQL query string
	 */
	public static String buildQueryString(boolean distinct, String table,
			String[] columns, String where, String groupBy, String having,
			String orderBy, String limit) {

		return buildQueryString(distinct, new String[] { table }, columns,
				where, groupBy, having, orderBy, limit);
	}

	/**
	 * Build an SQL query string from the given clauses.
	 *
	 * @param distinct
	 *            true if you want each row to be unique, false otherwise.
	 * @param tables
	 *            The table names to compile the query against.
	 * @param columns
	 *            A list of which columns to return. Passing null will return
	 *            all columns, which is discouraged to prevent reading data from
	 *            storage that isn't going to be used.
	 * @param where
	 *            A filter declaring which rows to return, formatted as an SQL
	 *            WHERE clause (excluding the WHERE itself). Passing null will
	 *            return all rows for the given URL.
	 * @param groupBy
	 *            A filter declaring how to group rows, formatted as an SQL
	 *            GROUP BY clause (excluding the GROUP BY itself). Passing null
	 *            will cause the rows to not be grouped.
	 * @param having
	 *            A filter declare which row groups to include in the cursor, if
	 *            row grouping is being used, formatted as an SQL HAVING clause
	 *            (excluding the HAVING itself). Passing null will cause all row
	 *            groups to be included, and is required when row grouping is
	 *            not being used.
	 * @param orderBy
	 *            How to order the rows, formatted as an SQL ORDER BY clause
	 *            (excluding the ORDER BY itself). Passing null will use the
	 *            default sort order, which may be unordered.
	 * @param limit
	 *            Limits the number of rows returned by the query, formatted as
	 *            LIMIT clause. Passing null denotes no LIMIT clause.
	 * @return the SQL query string
	 * @since 1.2.1
	 */
	public static String buildQueryString(boolean distinct, String[] tables,
			String[] columns, String where, String groupBy, String having,
			String orderBy, String limit) {
		if (isEmpty(groupBy) && !isEmpty(having)) {
			throw new IllegalArgumentException(
					"HAVING clauses are only permitted when using a groupBy clause");
		}
		if (!isEmpty(limit) && !sLimitPattern.matcher(limit).matches()) {
			throw new IllegalArgumentException("invalid LIMIT clauses:" + limit);
		}

		StringBuilder query = new StringBuilder(120);

		query.append("SELECT ");
		if (distinct) {
			query.append("DISTINCT ");
		}
		if (columns != null && columns.length != 0) {
			appendColumns(query, columns);
		} else {
			query.append("* ");
		}
		query.append("FROM ");
		for (int i = 0; i < tables.length; i++) {
			String table = tables[i];
			if (i > 0) {
				query.append(", ");
			}
			query.append(CoreSQLUtils.quoteWrap(table));
		}
		appendClause(query, " WHERE ", where);
		appendClause(query, " GROUP BY ", groupBy);
		appendClause(query, " HAVING ", having);
		appendClause(query, " ORDER BY ", orderBy);
		appendClause(query, " LIMIT ", limit);

		return query.toString();
	}

	private static void appendClause(StringBuilder s, String name, String clause) {
		if (!isEmpty(clause)) {
			s.append(name);
			s.append(clause);
		}
	}

	/**
	 * Add the names that are non-null in columns to s, separating them with
	 * commas.
	 */
	public static void appendColumns(StringBuilder s, String[] columns) {
		int n = columns.length;

		for (int i = 0; i < n; i++) {
			String column = columns[i];

			if (column != null) {
				if (i > 0) {
					s.append(", ");
				}
				s.append(CoreSQLUtils.quoteWrap(column));
			}
		}
		s.append(' ');
	}

	private static boolean isEmpty(CharSequence str) {
		if (str == null || str.length() == 0)
			return true;
		else
			return false;
	}

}
