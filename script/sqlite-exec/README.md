# SQLite Exec

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

## Alias

Add an alias to run from any location

    alias sql="~/sqlite-exec/sqlite-exec.sh"

    sql sqlite_file [sql]
