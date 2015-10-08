# GeoPackage Java

#### GeoPackage Java Lib ####

The [GeoPackage Libraries](http://ngageoint.github.io/GeoPackage/) were developed at the [National Geospatial-Intelligence Agency (NGA)](http://www.nga.mil/) in collaboration with [BIT Systems](http://www.bit-sys.com/). The government has "unlimited rights" and is releasing this software to increase the impact of government investments by providing developers with the opportunity to take things in new directions. The software use, modification, and distribution rights are stipulated within the [MIT license](http://choosealicense.com/licenses/mit/).

### Pull Requests ###
If you'd like to contribute to this project, please make a pull request. We'll review the pull request and discuss the changes. All pull request contributions to this project will be released under the MIT license.

Software source code previously released under an open source license and then modified by NGA staff is considered a "joint work" (see 17 USC § 101); it is partially copyrighted, partially public domain, and as a whole is protected by the copyrights of the non-government authors and must be released according to the terms of the original open source license.

### About ###

[GeoPackage](http://ngageoint.github.io/geopackage-java/) is a [GeoPackage Library](http://ngageoint.github.io/GeoPackage/) Java implementation of the Open Geospatial Consortium [GeoPackage](http://www.geopackage.org/) [spec](http://www.geopackage.org/spec/).  It is listed as an [OGC GeoPackage Implementation](http://www.geopackage.org/#implementations) by the National Geospatial-Intelligence Agency.

The GeoPackage Java library provides the ability to read, create, and edit GeoPackage files. GeoPackage tiles can be imported from or exported to a file system folder structure.

### Usage ###

View the latest [Javadoc](http://ngageoint.github.io/geopackage-java/docs/api/)

    // File newGeoPackage = ...;
    // File existingGeoPackage = ...;
    
    // Create a new GeoPackage
    boolean created = GeoPackageManager.create(newGeoPackage)
    
    // Open a GeoPackage
    GeoPackage geoPackage = GeoPackageManager.open(existingGeoPackage)
    
    // GeoPackage Table DAOs
    SpatialReferenceSystemDao srsDao = getSpatialReferenceSystemDao();
    ContentsDao contentsDao = geoPackage.getContentsDao();
    GeometryColumnsDao geomColumnsDao = geoPackage.getGeometryColumnsDao();
    TileMatrixSetDao tileMatrixSetDao = geoPackage.getTileMatrixSetDao();
    TileMatrixDao tileMatrixDao = geoPackage.getTileMatrixDao();
    DataColumnsDao dataColumnsDao = geoPackage.getDataColumnsDao();
    DataColumnConstraintsDao dataColumnConstraintsDao = geoPackage.getDataColumnConstraintsDao();
    MetadataDao metadataDao = geoPackage.getMetadataDao();
    MetadataReferenceDao metadataReferenceDao = geoPackage.getMetadataReferenceDao();
    ExtensionsDao extensionsDao = geoPackage.getExtensionsDao();
    
    // Feature and tile tables
    List<String> features = geoPackage.getFeatureTables();
    List<String> tiles = geoPackage.getTileTables();
    
    // Query Features
    FeatureDao featureDao = geoPackage.getFeatureDao(features.get(0));
    FeatureResultSet featureResultSet = featureDao.queryForAll();
    try{
        while(featureResultSet.moveToNext()){
            FeatureRow featureRow = featureResultSet.getRow();
            GeoPackageGeometryData geometryData = featureRow.getGeometry();
            Geometry geometry = geometryData.getGeometry();
            // ...
        }
    }finally{
        featureResultSet.close();
    }
    
    // Query Tiles
    TileDao tileDao = geoPackage.getTileDao(tiles.get(0));
    TileResultSet tileResultSet = tileDao.queryForAll();
    try{
        while(tileResultSet.moveToNext()){
            TileRow tileRow = tileResultSet.getRow();
            byte[] tileBytes = tileRow.getTileData();
            // ...
        }
    }finally{
        tileResultSet.close();
    }
    
    // Close database when done
    geoPackage.close();

### Installation ###

Pull from the [Maven Central Repository](http://search.maven.org/#artifactdetails|mil.nga.geopackage|geopackage|1.1.0|jar) (JAR, POM, Source, Javadoc)

    <dependency>
        <groupId>mil.nga.geopackage</groupId>
        <artifactId>geopackage</artifactId>
        <version>1.1.0</version>
    </dependency>

### Build ###

Build this repository using Eclipse and/or Maven:

    mvn clean install

### Stand Alone Utilities ###

The jar can be built as standalone (or combined with required dependency jars) to run utilities from the command line.

To build the jar into a standalone jar that includes all dependencies:

    mvn clean install -Pstandalone

#### Tile Writer ####

The tile writer writes tiles from a GeoPackage tile table to the file system.  Images are saved as raw bytes or as a specified format in a z/x/y.ext folder & file structure formatted as GeoPackage, Standard (Google), or TMS (Tile Map Service).  The GeoPackage format writes a tiles.properties file in the base imagery directory.  The mil.nga.geopackage.io.TileWriter functionality is invokable through code or command line.

To run against the jar:

    java -classpath geopackage-*standalone.jar mil.nga.geopackage.io.TileWriter [-t tile_type] [-i image_format] [-r] geopackage_file tile_table output_directory

Example:

    java -classpath geopackage-*standalone.jar mil.nga.geopackage.io.TileWriter -t tms /path/geopackage.gpkg mytiletable /path/tiles/mytiles

#### Tile Reader ####

The tile reader reads tile images from the file system and saves them into a new or existing GeoPackage in a new tile table. Images structured in a z/x/y.ext folder & file structure formatted as GeoPackage, Standard (Google), or TMS (Tile Map Service) are saved as raw bytes or as a specified format in a GeoPackage.  The GeoPackage format requires a tiles.properties file in the base imagery directory.  The mil.nga.geopackage.io.TileReader functionality is invokable through code or command line.

To run against the jar:

    java -classpath geopackage-*standalone.jar mil.nga.geopackage.io.TileReader [-i image_format] [-r] input_directory tile_type geopackage_file tile_table

Example:

    java -classpath geopackage-*standalone.jar mil.nga.geopackage.io.TileReader -i png /path/tiles/mytiles standard /path/geopackage.gpkg mytiletable

GeoPackage Format tiles.properties:

    epsg=
    min_x=
    max_x=
    min_y=
    max_y=
    # Zoom Level Properties:
    # If the file strucThe GeoPackage format requires a tiles.properties file in the base imagery directory.ture is fully populated and represents the matrix width and height, the properties can be omitted
    # If a non top zoom level matrix width and height increase by a factor of 2 with each zoom level, the properties can be omitted for those zoom levels
    zoom_level.{zoom}.matrix_width=
    zoom_level.{zoom}.matrix_height=

### Dependencies ###

#### Remote ####

* [GeoPackage Core Java](https://github.com/ngageoint/geopackage-core-java) (The MIT License (MIT)) - GeoPackage Library
* [WKB](https://github.com/ngageoint/geopackage-wkb-java) (The MIT License (MIT)) - GeoPackage Well Known Binary Lib
* [OrmLite](http://ormlite.com/) (Open Source License) - Object Relational Mapping (ORM) Library
* [SQLite JDBC](https://bitbucket.org/xerial/sqlite-jdbc) (Apache License, Version 2.0) - SQLiteJDBC library

#### Embedded ####

* [The Android Open Source Project](https://source.android.com/) (Apache License, Version 2.0) - Slightly modified subset of SQLiteQueryBuilder
