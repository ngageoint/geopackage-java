package mil.nga.geopackage.test.extension.properties;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.properties.PropertiesExtension;
import mil.nga.geopackage.extension.properties.PropertyNames;
import mil.nga.geopackage.test.CreateGeoPackageTestCase;

import org.junit.Test;

/**
 * Properties Extension Tests
 * 
 * @author osbornb
 */
public class PropertiesExtensionTest extends CreateGeoPackageTestCase {

	/**
	 * Test properties extension
	 */
	@Test
	public void testPropertiesExtension() {

		PropertiesExtension extension = new PropertiesExtension(geoPackage);
		TestCase.assertFalse(extension.has());
		TestCase.assertFalse(geoPackage.isTable(PropertiesExtension.TABLE_NAME));

		Extensions extensions = extension.getOrCreate();
		TestCase.assertNotNull(extensions);
		TestCase.assertTrue(extension.has());
		TestCase.assertTrue(geoPackage.isTable(PropertiesExtension.TABLE_NAME));

		TestCase.assertEquals(0, extension.numProperties());
		TestCase.assertTrue(extension.getProperties().isEmpty());
		TestCase.assertEquals(0, extension.numValues());
		TestCase.assertTrue(extension.getValues(PropertyNames.TITLE).isEmpty());
		TestCase.assertFalse(extension.hasSingleValue(PropertyNames.TITLE));
		TestCase.assertFalse(extension.hasValues(PropertyNames.TITLE));
		TestCase.assertEquals(0, extension.numValues(PropertyNames.TITLE));

		final String name = "My GeoPackage";
		TestCase.assertTrue(extension.addValue(PropertyNames.TITLE, name));
		TestCase.assertEquals(1, extension.numProperties());
		TestCase.assertEquals(1, extension.getProperties().size());
		TestCase.assertEquals(1, extension.numValues());
		TestCase.assertEquals(1, extension.getValues(PropertyNames.TITLE)
				.size());
		TestCase.assertTrue(extension.hasSingleValue(PropertyNames.TITLE));
		TestCase.assertTrue(extension.hasValues(PropertyNames.TITLE));
		TestCase.assertEquals(1, extension.numValues(PropertyNames.TITLE));
		TestCase.assertEquals(1, extension.numValues());
		TestCase.assertEquals(name, extension.getValue(PropertyNames.TITLE));
		TestCase.assertTrue(extension.hasValue(PropertyNames.TITLE, name));

		final String tag = "TAG";
		TestCase.assertTrue(extension.addValue(PropertyNames.TAG, tag + 1));
		TestCase.assertEquals(2, extension.numProperties());
		TestCase.assertEquals(2, extension.getProperties().size());
		TestCase.assertEquals(2, extension.numValues());
		TestCase.assertEquals(1, extension.getValues(PropertyNames.TAG).size());
		TestCase.assertTrue(extension.hasSingleValue(PropertyNames.TAG));
		TestCase.assertTrue(extension.hasValues(PropertyNames.TAG));
		TestCase.assertEquals(1, extension.numValues(PropertyNames.TAG));
		TestCase.assertTrue(extension.hasValue(PropertyNames.TAG, tag + 1));

		TestCase.assertTrue(extension.addValue(PropertyNames.TAG, tag + 2));
		TestCase.assertEquals(2, extension.numProperties());
		TestCase.assertEquals(2, extension.getProperties().size());
		TestCase.assertEquals(3, extension.numValues());
		TestCase.assertEquals(2, extension.getValues(PropertyNames.TAG).size());
		TestCase.assertFalse(extension.hasSingleValue(PropertyNames.TAG));
		TestCase.assertTrue(extension.hasValues(PropertyNames.TAG));
		TestCase.assertEquals(2, extension.numValues(PropertyNames.TAG));
		TestCase.assertTrue(extension.hasValue(PropertyNames.TAG, tag + 2));

		TestCase.assertTrue(extension.addValue(PropertyNames.TAG, tag + 3));
		TestCase.assertTrue(extension.addValue(PropertyNames.TAG, tag + 4));
		TestCase.assertFalse(extension.addValue(PropertyNames.TAG, tag + 4));

		Set<String> values = new HashSet<>(
				extension.getValues(PropertyNames.TAG));
		for (int i = 1; i <= 4; i++) {
			TestCase.assertTrue(values.contains(tag + i));
			TestCase.assertTrue(extension.hasValue(PropertyNames.TAG, tag + i));
		}

		TestCase.assertEquals(1,
				extension.deleteValue(PropertyNames.TAG, tag + 3));
		TestCase.assertEquals(3, extension.getValues(PropertyNames.TAG).size());
		TestCase.assertEquals(3, extension.numValues(PropertyNames.TAG));
		TestCase.assertFalse(extension.hasValue(PropertyNames.TAG, tag + 3));

		TestCase.assertEquals(3, extension.deleteProperty(PropertyNames.TAG));
		TestCase.assertEquals(1, extension.numProperties());
		TestCase.assertEquals(1, extension.getProperties().size());
		TestCase.assertEquals(1, extension.numValues());
		TestCase.assertTrue(extension.getValues(PropertyNames.TAG).isEmpty());
		TestCase.assertFalse(extension.hasSingleValue(PropertyNames.TAG));
		TestCase.assertFalse(extension.hasValues(PropertyNames.TAG));
		TestCase.assertEquals(0, extension.numValues(PropertyNames.TAG));

		extension.removeExtension();
		TestCase.assertFalse(extension.has());
		TestCase.assertFalse(geoPackage.isTable(PropertiesExtension.TABLE_NAME));

	}

}
