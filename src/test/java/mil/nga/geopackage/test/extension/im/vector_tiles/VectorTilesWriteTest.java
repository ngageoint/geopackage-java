package mil.nga.geopackage.test.extension.im.vector_tiles;

import junit.framework.TestCase;
import mil.nga.geopackage.extension.im.vector_tiles.VectorTilesExtension;
import mil.nga.geopackage.extension.im.vector_tiles.VectorTilesGeoJSONExtension;
import mil.nga.geopackage.extension.im.vector_tiles.VectorTilesMapboxExtension;
import mil.nga.geopackage.test.LoadGeoPackageTestCase;
import mil.nga.geopackage.test.TestConstants;
import org.junit.Test;

import java.sql.SQLException;

public class VectorTilesWriteTest extends LoadGeoPackageTestCase {

    /**
     * Constructor
     */
    public VectorTilesWriteTest() {
        super(TestConstants.IMPORT_DB_FILE_NAME);
    }

    @Test
    public void testWriteVectorTiles() throws SQLException {
        VectorTilesExtension vte = new VectorTilesExtension(geoPackage);
        VectorTilesMapboxExtension vtme = new VectorTilesMapboxExtension(geoPackage);
        VectorTilesGeoJSONExtension vtge = new VectorTilesGeoJSONExtension(geoPackage);

        if (vte.has()) {
            vte.removeExtension();
        }

        // 1. Has extension
        TestCase.assertFalse(vte.has());

        // 2. Add extension
        vte.getOrCreate();
        vte.createUserVectorTilesTable("myvt1", vtme);
        vte.createUserVectorTilesTable("myvt2", vtge);
        TestCase.assertTrue(vte.has());
        TestCase.assertTrue(vtme.has());
        TestCase.assertTrue(vtge.has());
        TestCase.assertEquals(2, geoPackage.getExtensionsDao().
                queryByExtension(VectorTilesExtension.getName()).size());
        TestCase.assertEquals(1, geoPackage.getExtensionsDao().
                queryByExtension(vtme.getName()).size());
        TestCase.assertEquals(1, geoPackage.getExtensionsDao().
                queryByExtension(vtme.getName()).size());
    }
}
