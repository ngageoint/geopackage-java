package mil.nga.geopackage.extension.im.portrayal;

import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.Test;

import junit.framework.TestCase;
import mil.nga.geopackage.LoadGeoPackageTestCase;
import mil.nga.geopackage.TestConstants;

public class PortrayalExtensionTest extends LoadGeoPackageTestCase {
    /**
     * Constructor
     */
    public PortrayalExtensionTest() {
        super(TestConstants.IMPORT_DB_FILE_NAME);
    }

    @Test
    public void testExtension() {
        PortrayalExtension portExt = new PortrayalExtension(geoPackage);

        if (portExt.has()) {
            portExt.removeExtension();
        }

        // 1. Has extension
        TestCase.assertFalse(portExt.has());

        // 2. Add extension
        portExt.getOrCreate();
        TestCase.assertTrue(portExt.has());
        try {
            TestCase.assertEquals(5, geoPackage.getExtensionsDao().
                    queryByExtension(PortrayalExtension.EXTENSION_NAME).size());
        } catch (SQLException e) {
            e.printStackTrace();
            fail();
        }
    }
}
