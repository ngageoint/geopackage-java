# SQLite Exec

[sqlite-exec.zip](https://github.com/ngageoint/geopackage-java/releases/latest/download/sqlite-exec.zip)

Executes SQL statements on a SQLite database, including GeoPackages.  Most SQLite statements are supported including: SELECT, INSERT, DELETE, CREATE, ALTER, DROP, PRAGMA, VACUUM, and more.  Providing SQL on the command line executes the single statement. Omitting SQL on the command line starts an interactive SQL shell with additional command options.  Handles special SQLite and GeoPackage cases and statements including:
 * Dropping columns (not natively supported in SQLite)
 * Copying a table and all dependencies (not a standard SQL alter table command)
 * For GeoPackages
   * Renaming a user table also updates dependencies throughout the GeoPackage
   * Dropping a table also removes dependencies throughout the GeoPackage

## Run

### Script

    ./sqlite-exec.sh [-m max_rows] [-w max_column_width] [-l max_lines_per_row] sqlite_file [sql]

### Jar

    java -jar sqlite-exec.jar [-m max_rows] [-w max_column_width] [-l max_lines_per_row] sqlite_file [sql]

### Alias

Add an alias to your shell to run from any location

    alias sql="~/sqlite-exec/sqlite-exec.sh"

And run

    sql sqlite_file [sql]

## Examples

Run using the script, Jar, or alias.

    sql /path/geopackage.gpkg "SELECT * FROM gpkg_contents"

    sql /path/geopackage.gpkg
    sql> PRAGMA integrity_check;
    sql> PRAGMA table_info(table_name);
    sql> SELECT * FROM table_name;
    sql> ALTER TABLE table_name COPY TO table_name_copy;
    sql> ALTER TABLE table_name RENAME TO new_table_name;
    sql> ALTER TABLE table_name DROP COLUMN column_name;
    sql> DROP TABLE table_name_copy;
    sql> SELECT * from sqlite_master;
    sql> VACUUM;

## Help

```
USAGE

	[-m max_rows] [-w max_column_width] [-l max_lines_per_row] sqlite_file [sql]

DESCRIPTION

	Executes SQL on a SQLite database

	Provide the SQL to execute a single statement. Omit to start an interactive session.

ARGUMENTS

	-m max_rows
		Max rows per query (Default is 100)

	-w max_column_width
		Max width (in characters) per column (Default is 120)

	-l max_lines_per_row
		Max lines per row (Default is 0 = none)

	sqlite_file
		path to the SQLite database file

	sql
		SQL statement to execute
```

### Interactive Session

```
- Supports most SQLite statements including:
	SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, PRAGMA, VACUUM, etc
- Terminate SQL statements with a ;
- Exit with a single empty line

Commands:

	info              - GeoPackage information
	help              - print this help information
	tables [name]     - list database tables (all or LIKE table name)
	indexes [name]    - list database indexes (all or LIKE index name)
	views [name]      - list database views (all or LIKE view name)
	triggers [name]   - list database triggers (all or LIKE trigger name)
	rows [n]          - display or set the max rows per query
	width [n]         - display or set the max width (in characters) per column
	lines [n]         - display or set the max lines per row
	history           - list successfully executed sql commands
	!!                - re-execute the previous successful sql command
	!n                - re-execute a sql statement by history id n
	!-n               - re-execute a sql statement n commands back in history
	blobs [-e file_extension] [-d directory] [-p pattern]
	                  - write blobs from the previous successful sql command to the file system
	                        ([directory]|blobs)/table_name/column_name/(pk_values|result_index|[pattern])[.file_extension]
	                     file_extension - file extension added to each saved blob file
	                     directory      - base directory to save table_name/column_name/blobs (default is ./blobs)
	                     pattern        - file directory and/or name pattern consisting of column names in parentheses
	                                       (column_name)-(column_name2)
	                                       (column_name)/(column_name2)
	info <name>       - PRAGMA table_info(<name>);
	sqlite_master     - SELECT * FROM sqlite_master;
	<name>            - SELECT * FROM <name>;
	vacuum            - VACUUM [INTO 'filename'];
	fk                - PRAGMA foreign_keys [= boolean];
	fkc               - PRAGMA foreign_key_check[(<table-name>)];
	integrity         - PRAGMA integrity_check[(N)];
	quick             - PRAGMA quick_check[(N)];
	contents [name]   - List GeoPackage contents (all or LIKE table name)
	attributes [name] - List GeoPackage attributes tables (all or LIKE table name)
	features [name]   - List GeoPackage feature tables (all or LIKE table name)
	tiles [name]      - List GeoPackage tile tables (all or LIKE table name)
	ginfo <name>      - Query GeoPackage metadata for the table name
	cbounds [-p projection] [name]
	                  - Determine the bounds (using only the contents) of the entire GeoPackage or single table name
	                     projection     - desired projection as 'authority:code' or 'epsg_code'
	bounds [-p projection] [-m] [name]
	                  - Determine the bounds of the entire GeoPackage or single table name
	                     projection     - desired projection as 'authority:code' or 'epsg_code'
	                     m              - manually query unindexed tables
	tbounds [-p projection] [-m] [name]
	                  - Determine the bounds (using only table metadata) of the entire GeoPackage or single table name
	                     projection     - desired projection as 'authority:code' or 'epsg_code'
	                     m              - manually query unindexed tables
	extensions [name] - List GeoPackage extensions (all or LIKE table name)
	geometry <name> [-p projection] [ids]
	                  - Display feature table geometries as Well-Known Text
	                     projection     - desired display projection as 'authority:code' or 'epsg_code'
	                     ids            - single or comma delimited feature table row ids
	geometry <name> [-p projection] <id> <wkt>
	                  - Update or insert a feature table geometry with Well-Known Text
	                     projection     - Well-Known Text projection as 'authority:code' or 'epsg_code'
	                     id             - single feature table row id to update or -1 to insert a new row
	                     wkt            - Well-Known Text

Special Supported Cases:

	Drop Column  - Not natively supported in SQLite
	                  * ALTER TABLE table_name DROP column_name
	                  * ALTER TABLE table_name DROP COLUMN column_name
	Copy Table   - Not a traditional SQL statment
	                  * ALTER TABLE table_name COPY TO new_table_name
	Rename Table - User tables are updated throughout the GeoPackage
	                  * ALTER TABLE table_name RENAME TO new_table_name
	Drop Table   - User tables are dropped throughout the GeoPackage
	                  * DROP TABLE table_name
```
