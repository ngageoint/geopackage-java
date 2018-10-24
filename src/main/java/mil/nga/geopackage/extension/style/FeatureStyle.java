package mil.nga.geopackage.extension.style;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.extension.related.RelatedTablesExtension;

/**
 * Feature Style extension for styling features
 * 
 * @author osbornb
 * @since 3.1.1
 */
public class FeatureStyle extends FeatureCoreStyle {

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public FeatureStyle(GeoPackage geoPackage) {
		super(geoPackage, new RelatedTablesExtension(geoPackage));
	}

}
