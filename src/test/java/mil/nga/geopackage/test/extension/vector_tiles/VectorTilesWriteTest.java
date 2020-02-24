package mil.nga.geopackage.test.extension.vector_tiles;

import junit.framework.TestCase;
import mil.nga.geopackage.extension.vector_tiles.VectorTilesExtension;
import mil.nga.geopackage.extension.vector_tiles.VectorTilesGeoJSONExtension;
import mil.nga.geopackage.extension.vector_tiles.VectorTilesMapboxExtension;
import mil.nga.geopackage.test.LoadGeoPackageTestCase;
import mil.nga.geopackage.test.TestConstants;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.fail;

public class VectorTilesWriteTest extends LoadGeoPackageTestCase {

    private final String TABLE_NAME_1 = "myvt1";
    private final String TABLE_NAME_2 = "myvt2";
    /**
     * Constructor
     */
    public VectorTilesWriteTest() {
        super(TestConstants.IMPORT_DB_FILE_NAME);
    }

    @Test
    public void testWriteVectorTiles() {
        VectorTilesExtension vte = new VectorTilesExtension(geoPackage);

        if (vte.has()) {
            vte.removeExtension();
        }

        // 1. Has extension
        TestCase.assertFalse(vte.has());

        // 2. Add extension
        vte.getOrCreate();
        vte.createUserVectorTilesTable(TABLE_NAME_1, new VectorTilesMapboxExtension());
        vte.createUserVectorTilesTable(TABLE_NAME_2, new VectorTilesGeoJSONExtension());
        TestCase.assertTrue(vte.has());
        try {
            TestCase.assertEquals(2, geoPackage.getExtensionsDao().
                    queryByExtension(VectorTilesExtension.EXTENSION_NAME).size());
            TestCase.assertEquals(1, geoPackage.getExtensionsDao().
                    queryByExtension(VectorTilesMapboxExtension.EXTENSION_NAME).size());
            TestCase.assertEquals(1, geoPackage.getExtensionsDao().
                    queryByExtension(VectorTilesGeoJSONExtension.EXTENSION_NAME).size());
        } catch (SQLException e) {
            e.printStackTrace();
            fail();
        }
    }
}
