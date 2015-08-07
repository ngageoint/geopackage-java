# GeoPackage Java

#### GeoPackage Java Lib ####

The GeoPackage Libraries were developed at the National Geospatial-Intelligence Agency (NGA) in collaboration with [BIT Systems](https://www.bit-sys.com/index.jsp). The government has "unlimited rights" and is releasing this software to increase the impact of government investments by providing developers with the opportunity to take things in new directions. The software use, modification, and distribution rights are stipulated within the [MIT license](http://choosealicense.com/licenses/mit/).

### Pull Requests ###
If you'd like to contribute to this project, please make a pull request. We'll review the pull request and discuss the changes. All pull request contributions to this project will be released under the MIT license.

Software source code previously released under an open source license and then modified by NGA staff is considered a "joint work" (see 17 USC § 101); it is partially copyrighted, partially public domain, and as a whole is protected by the copyrights of the non-government authors and must be released according to the terms of the original open source license.

### About ###

GeoPackage is a Java implementation of the Open Geospatial Consortium [GeoPackage](http://www.geopackage.org/) [spec](http://www.geopackage.org/spec/).

The GeoPackage Java library provides the ability to read, create, and edit GeoPackage files.

### Usage ###

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

### Build ###

The following repositories must be built first (Central Repository Artifacts Coming Soon):
* [GeoPackage WKB Java] (https://github.com/ngageoint/geopackage-wkb-java)
* [GeoPackage Core Java] (https://github.com/ngageoint/geopackage-core-java)

Build this repository using Maven:

    mvn clean install

### Dependencies ###

#### Remote ####

* [GeoPackage Core Java](https://github.com/ngageoint/geopackage-core-java) (The MIT License (MIT)) - GeoPackage Library
* [OrmLite](http://ormlite.com/) (Open Source License) - Object Relational Mapping (ORM) Library
* [SQLite JDBC](https://bitbucket.org/xerial/sqlite-jdbc) (Apache License, Version 2.0) - SQLiteJDBC library

#### Embedded ####

* [The Android Open Source Project](https://source.android.com/) (Apache License, Version 2.0) - Slightly modified subset of SQLiteQueryBuilder
