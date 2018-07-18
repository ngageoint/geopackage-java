package mil.nga.geopackage.test.extension;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.PropertiesExtension;
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
		TestCase.assertTrue(extension.getValues(
				PropertiesExtension.PROPERTY_NAME).isEmpty());
		TestCase.assertFalse(extension
				.hasSingleValue(PropertiesExtension.PROPERTY_NAME));
		TestCase.assertFalse(extension
				.hasValues(PropertiesExtension.PROPERTY_NAME));
		TestCase.assertEquals(0,
				extension.numValues(PropertiesExtension.PROPERTY_NAME));

		final String name = "My GeoPackage";
		TestCase.assertTrue(extension.addValue(
				PropertiesExtension.PROPERTY_NAME, name));
		TestCase.assertEquals(1, extension.numProperties());
		TestCase.assertEquals(1, extension.getProperties().size());
		TestCase.assertEquals(1, extension.numValues());
		TestCase.assertEquals(1,
				extension.getValues(PropertiesExtension.PROPERTY_NAME).size());
		TestCase.assertTrue(extension
				.hasSingleValue(PropertiesExtension.PROPERTY_NAME));
		TestCase.assertTrue(extension
				.hasValues(PropertiesExtension.PROPERTY_NAME));
		TestCase.assertEquals(1,
				extension.numValues(PropertiesExtension.PROPERTY_NAME));
		TestCase.assertEquals(1, extension.numValues());
		TestCase.assertEquals(name,
				extension.getValue(PropertiesExtension.PROPERTY_NAME));

		TestCase.assertTrue(extension.addValue(
				PropertiesExtension.PROPERTY_TAG, "TAG1"));
		TestCase.assertEquals(2, extension.numProperties());
		TestCase.assertEquals(2, extension.getProperties().size());
		TestCase.assertEquals(2, extension.numValues());
		TestCase.assertEquals(1,
				extension.getValues(PropertiesExtension.PROPERTY_TAG).size());
		TestCase.assertTrue(extension
				.hasSingleValue(PropertiesExtension.PROPERTY_TAG));
		TestCase.assertTrue(extension
				.hasValues(PropertiesExtension.PROPERTY_TAG));
		TestCase.assertEquals(1,
				extension.numValues(PropertiesExtension.PROPERTY_TAG));

		TestCase.assertTrue(extension.addValue(
				PropertiesExtension.PROPERTY_TAG, "TAG2"));
		TestCase.assertEquals(2, extension.numProperties());
		TestCase.assertEquals(2, extension.getProperties().size());
		TestCase.assertEquals(3, extension.numValues());
		TestCase.assertEquals(2,
				extension.getValues(PropertiesExtension.PROPERTY_TAG).size());
		TestCase.assertFalse(extension
				.hasSingleValue(PropertiesExtension.PROPERTY_TAG));
		TestCase.assertTrue(extension
				.hasValues(PropertiesExtension.PROPERTY_TAG));
		TestCase.assertEquals(2,
				extension.numValues(PropertiesExtension.PROPERTY_TAG));

		TestCase.assertTrue(extension.addValue(
				PropertiesExtension.PROPERTY_TAG, "TAG3"));
		TestCase.assertTrue(extension.addValue(
				PropertiesExtension.PROPERTY_TAG, "TAG4"));
		TestCase.assertFalse(extension.addValue(
				PropertiesExtension.PROPERTY_TAG, "TAG4"));

		Set<String> values = new HashSet<>(
				extension.getValues(PropertiesExtension.PROPERTY_TAG));
		for (int i = 1; i <= 4; i++) {
			String tag = "TAG" + i;
			TestCase.assertTrue(values.contains(tag));
			TestCase.assertTrue(extension.hasValue(
					PropertiesExtension.PROPERTY_TAG, tag));
		}

		TestCase.assertEquals(1,
				extension.deleteValue(PropertiesExtension.PROPERTY_TAG, "TAG3"));
		TestCase.assertEquals(3,
				extension.getValues(PropertiesExtension.PROPERTY_TAG).size());
		TestCase.assertEquals(3,
				extension.numValues(PropertiesExtension.PROPERTY_TAG));

		TestCase.assertEquals(3,
				extension.deleteProperty(PropertiesExtension.PROPERTY_TAG));
		TestCase.assertEquals(1, extension.numProperties());
		TestCase.assertEquals(1, extension.getProperties().size());
		TestCase.assertEquals(1, extension.numValues());
		TestCase.assertTrue(extension.getValues(
				PropertiesExtension.PROPERTY_TAG).isEmpty());
		TestCase.assertFalse(extension
				.hasSingleValue(PropertiesExtension.PROPERTY_TAG));
		TestCase.assertFalse(extension
				.hasValues(PropertiesExtension.PROPERTY_TAG));
		TestCase.assertEquals(0,
				extension.numValues(PropertiesExtension.PROPERTY_TAG));

		extension.removeExtension();
		TestCase.assertFalse(extension.has());
		TestCase.assertFalse(geoPackage.isTable(PropertiesExtension.TABLE_NAME));

	}
}
