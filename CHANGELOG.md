#Change Log
All notable changes to this project will be documented in this file.
Adheres to [Semantic Versioning](http://semver.org/).

---

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
