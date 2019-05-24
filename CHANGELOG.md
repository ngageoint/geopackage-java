# Change Log
All notable changes to this project will be documented in this file.
Adheres to [Semantic Versioning](http://semver.org/).

---

## 3.3.0 (TBD)

* geopackage-core version 3.3.0
* Transaction shortcut methods for the GeoPackages, connections, and User DAOs
* Feature Tiles geometry caching
* GeoPackage User Custom DAO retrieval
* Table Readers moved to geopackage-core

## [3.2.0](https://github.com/ngageoint/geopackage-java/releases/tag/3.2.0) (04-02-2019)

* geopackage-core version 3.2.0
* tiff version 2.0.1
* sqlite-jdbc version 3.25.2
* NGA [Contents Id](http://ngageoint.github.io/GeoPackage/docs/extensions/contents-id.html) Extension
* NGA [Feature Style](http://ngageoint.github.io/GeoPackage/docs/extensions/feature-style.html) Extension
* OGC [Related Tables](http://www.geopackage.org/18-000.html) Extension improvements
* Feature Tile drawing and generator improvements including scaling, styles, and layering
* FeatureIndexResults id iteration option in place of reading full feature rows
* Feature Cache for memory caching feature rows in a single table
* Feature Cache Tables for memory caching feature rows from multiple single GeoPackage tables
* Feature Row geometry type accessor
* FeatureTileGen argument support for tile scale, icon dimensions, and ignoring styles
* GeoPackage creation example updates
* Eclipse project cleanup

## [3.1.0](https://github.com/ngageoint/geopackage-java/releases/tag/3.1.0) (10-04-2018)

* geopackage-core version 3.1.0
* User Table DAO projected bounding box methods
* GeoPackage Connection and SQLUtils query improvements
* ResultSet Result interface implementation, utilized by UserResultSet
* RTree improvements for querying geometries
* Feature Table Index chunked limit queries when indexing
* Feature Index Manager to manage and query multiple index types
* Feature Index Results interface and implementations
* Feature Row geometry value and envelope methods
* Feature Table Reader ignore case of geometry column name
* Manual Feature Queries for unindexed geometries
* Tile Reader directory checking shortcut when processing raw images

## [3.0.2](https://github.com/ngageoint/geopackage-java/releases/tag/3.0.2) (07-27-2018)

* geopackage-core version 3.0.2
* Properties Extension for saving GeoPackage metadata in the file
* Properties Manager for using the Properties Extension on multiple open GeoPackages
* Additional SQL Utils query methods and null query result method fixes
* Additional GeoPackage Connection query methods
* GeoPackageCache implementation
* GeoPackageManager automatically adds extension as needed

## [3.0.1](https://github.com/ngageoint/geopackage-java/releases/tag/3.0.1) (07-13-2018)

* geopackage-core version 3.0.1
* Related Tables Extension support (DRAFT version 0.1)
* User row copy methods
* User DAO support for id-less schemas
* Custom User connection, DAO, result set, row, and table reader implementations
* Javadoc warning fixes
* ormlite-jdbc version 5.1
* sqlite-jdbc version 3.23.1
* maven and sonatype plugin version updates

## [3.0.0](https://github.com/ngageoint/geopackage-java/releases/tag/3.0.0) (05-17-2018)

* geopackage-core version updated to 3.0.0
* [GeoPackage Core](https://github.com/ngageoint/geopackage-core-java) new WKB dependency on [Simple Features WKB library](https://github.com/ngageoint/simple-features-wkb-java)
  * Package names in dependent classes must be updated
  * GeometryType code calls must be replaced using GeometryCodes
* [GeoPackage Core](https://github.com/ngageoint/geopackage-core-java) new projection dependency on [Simple Features Projections library](https://github.com/ngageoint/simple-features-proj-java)
  * Package names in dependent classes must be updated
  * ProjectionFactory SRS calls must be replaced using SpatialReferenceSystem projection method
  * ProjectionTransform bounding box calls must be replaced using BoundingBox transform method

## [2.0.2](https://github.com/ngageoint/geopackage-java/releases/tag/2.0.2) (03-20-2018)

* Tile Scaling extension for generating missing tiles using nearby zoom levels
* Skip tiles drawn from features when no features overlap the tile
* Tile DAO approximate zoom level methods
* Tile Generator fix to save updated bounds in the Tile Matrix Set
* Tile Generator projection transformations only when projections differ
* geopackage-core version updated to 2.0.2

## [2.0.1](https://github.com/ngageoint/geopackage-java/releases/tag/2.0.1) (02-13-2018)

* Coverage Data extension (previously Elevation Extension)
* RTree Index Extension support
* Tile Generator contents bounding box fix to use the requested bounds
* GeoPackage creation example
* geopackage-core version updated to 2.0.1
* SQLite JDBC version updated to 3.21.0.1

## [2.0.0](https://github.com/ngageoint/geopackage-java/releases/tag/2.0.0) (11-20-2017)

* WARNING - BoundingBox.java (geopackage-core) coordinate constructor arguments order changed to (min lon, min lat, max lon, max lat)
  Pre-existing calls to BoundingBox coordinate constructor should swap the min lat and max lon values
* WARNING - TileGrid.java (geopackage-core) constructor arguments order changed to (minX, minY, maxX, maxY)
  Pre-existing calls to TileGrid constructor should swap the minY and maxX values
* geopackage-core version updated to 2.0.0
* Attribute, Feature, and Tile User Row Sync implementations
* Query support for "columns as"
* Feature Table Index row syncing
* Improved feature row geometry blob handling
* Feature Tiles and Feature Tile Gen geometry simplifications
* Feature Tile Gen increased default max features per tile to 5000
* Tile Reader creates contents with the same SRS as the Tile Matrix Set
* tiff version updated to 2.0.0
* maven-gpg-plugin version 1.6

## [1.3.1](https://github.com/ngageoint/geopackage-java/releases/tag/1.3.1) (07-13-2017)

* geopackage-core version updated to 1.3.1
* Bounding of degree projected boxes before Web Mercator transformations

## [1.3.0](https://github.com/ngageoint/geopackage-java/releases/tag/1.3.0) (06-27-2017)

* geopackage-core version updated to 1.3.0
* tiff version updated to 1.0.3
* Copy constructors for user table (features, tiles, attributes) row objects
* Improved date column support for user tables (features, tiles, attributes)

## [1.2.2](https://github.com/ngageoint/geopackage-java/releases/tag/1.2.2) (06-12-2017)

* geopackage-core version updated to 1.2.2
* tiff version updated to 1.0.2
* Elevation Extension scale and offset columns changed to be non nullable
* URL Tile Generator handle URL redirects

## [1.2.1](https://github.com/ngageoint/geopackage-java/releases/tag/1.2.1) (02-02-2017)

* Elevation Extension support (PNG & TIFF)
* geopackage-core version updated to 1.2.1
* User Attributes table support
* tiff-java dependency for TIFF support
* Elevation query algorithms including Nearest Neighbor, Bilinear, and Bicubic
* Elevation unbounded results elevation queries
* Table and column name SQL quotations to allow uncommon but valid names
* Zoom level determination using width and height
* GeoPackage application id and user version
* OrmLite JDBC version updated to 5.0
* SQLite JDBC version updated to 3.16.1

## [1.2.0](https://github.com/ngageoint/geopackage-java/releases/tag/1.2.0) (06-22-2016)

* Tile Reader (creates a GeoPackage from tile image files) updated to support image reprojections
* Tile Writer (writes tile image files from a GeoPackage) updated to support image reprojections
* Tile Writer user specified tile width and height arguments
* Tile Retriever providing common XYZ tile retrieval functionality
* Tile Creator providing common tile generation functionality
* Removal of TileDraw, replaced by Tile Retriever and Tile Creator
* Tile DAO changed to work with any projection units
* Tile Generator support for multiple projections, such as WGS84 in addition to Web Mercator
* URL Tile Generator changed to use provided projection in place of parsing URL

## [1.1.9](https://github.com/ngageoint/geopackage-java/releases/tag/1.1.9) (05-10-2016)

* GeoPackage 1.1.0 spec updates
* geopackage-core version updated to 1.1.8
* GeoPackage Connection column exists and query single result method implementations
* Use updated projection calls by passing Spatial Reference Systems

## [1.1.8](https://github.com/ngageoint/geopackage-java/releases/tag/1.1.8) (04-18-2016)

* geopackage-core version updated to 1.1.7

## [1.1.7](https://github.com/ngageoint/geopackage-java/releases/tag/1.1.7) (02-19-2016)

* geopackage-core version updated to 1.1.6
* Feature Tile Table Linker implementation with methods for retrieving data access objects

## [1.1.6](https://github.com/ngageoint/geopackage-java/releases/tag/1.1.6) (02-02-2016)

* geopackage-core version updated to 1.1.5
* Feature Tile Generator linking between feature and tile tables

## [1.1.5](https://github.com/ngageoint/geopackage-java/releases/tag/1.1.5) (01-20-2016)

* Standalone tile generator fix to save tile progress from current zoom level when canceled
* Standalone tile generator zoom level specific progress logging
* URL Tile Generator multiple tile download attempts upon failure

## [1.1.4](https://github.com/ngageoint/geopackage-java/releases/tag/1.1.4) (01-15-2016)

* geopackage-core version updated to 1.1.4 for proj4j dependency location change

## [1.1.3](https://github.com/ngageoint/geopackage-java/releases/tag/1.1.3) (12-16-2015)

* geopackage-core version updated to 1.1.3 for Geometry projection transformations
* Tile Draw image bytes shortcut methods

## [1.1.2](https://github.com/ngageoint/geopackage-java/releases/tag/1.1.2) (12-14-2015)

* geopackage-core version updated to 1.1.2 - [Core Issue #14](https://github.com/ngageoint/geopackage-core-java/issues/14)
* GeoPackage methods: execute SQL, query, foreign key check, integrity check, quick integrity check
* Tile Generator with URL and Feature Tile Generator implementations - [Issue #13](https://github.com/ngageoint/geopackage-java/issues/13)
* URL and Feature Tile Generator command line implementations
* ORMLite log level changed from debug to info
* Tile Writer performance improvements when writing sparse GeoPackage tile tables
* Added org.xerial.thirdparty nestedvm dependency which was removed in 3.8 versions of xerial sqlite-jdbc to suppress invalid warning logs

## [1.1.1](https://github.com/ngageoint/geopackage-java/releases/tag/1.1.1) (11-20-2015)

* Javadoc project links to geopackage-core and wkb
* Project Feature DAO bounding box when not in the same projection
* geopackage-core version updated to 1.1.1 - [Issue #11](https://github.com/ngageoint/geopackage-java/issues/11)
* min and max column query methods - [Issue #9](https://github.com/ngageoint/geopackage-java/issues/9)
* TileDao methods, query for tile grid or bounding box at zoom level - [Issue #10](https://github.com/ngageoint/geopackage-java/issues/10)

## [1.1.0](https://github.com/ngageoint/geopackage-java/releases/tag/1.1.0) (10-08-2015)

* NGA Table Index Extension implementation - http://ngageoint.github.io/GeoPackage/docs/extensions/geometry-index.html
* Feature and Tile DAO get bounding box method

## [1.0.1](https://github.com/ngageoint/geopackage-java/releases/tag/1.0.1) (09-23-2015)

* Upgrading geopackage-core version to 1.0.1 to get added GeoPackageCache functionality

## [1.0.0](https://github.com/ngageoint/geopackage-java/releases/tag/1.0.0) (09-15-2015)

* Initial Release
