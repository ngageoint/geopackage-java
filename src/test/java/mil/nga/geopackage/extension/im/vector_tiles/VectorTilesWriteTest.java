package mil.nga.geopackage.extension.im.vector_tiles;

import java.sql.SQLException;

import org.junit.Test;

import junit.framework.TestCase;
import mil.nga.geopackage.LoadGeoPackageTestCase;
import mil.nga.geopackage.TestConstants;

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
