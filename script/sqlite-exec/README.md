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

    ./sqlite-exec.sh [-m max_rows] sqlite_file [sql]

### Jar

    java -jar sqlite-exec.jar [-m max_rows] sqlite_file [sql]

### Alias

Add an alias to your shell to run from any location

    alias sql="~/sqlite-exec/sqlite-exec.sh"

And run

    sql sqlite_file [sql]

## Examples:

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
