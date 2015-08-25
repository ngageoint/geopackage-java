package mil.nga.geopackage.tiles;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.tiles.TileBoundingBoxUtils;

/**
 * Tile Bounding Box utility methods for the Java specific library
 * 
 * @author osbornb
 */
public class TileBoundingBoxJavaUtils {

	/**
	 * Get a rectangle with floating point boundaries using the tile width,
	 * height, bounding box, and the bounding box section within the outer box
	 * to build the rectangle from
	 * 
	 * @param width
	 * @param height
	 * @param boundingBox
	 * @param boundingBoxSection
	 * @return
	 */
	public static ImageRectangleF getRectangle(long width, long height,
			BoundingBox boundingBox, BoundingBox boundingBoxSection) {

		float left = TileBoundingBoxUtils.getXPixel(width, boundingBox,
				boundingBoxSection.getMinLongitude());
		float right = TileBoundingBoxUtils.getXPixel(width, boundingBox,
				boundingBoxSection.getMaxLongitude());
		float top = TileBoundingBoxUtils.getYPixel(height, boundingBox,
				boundingBoxSection.getMaxLatitude());
		float bottom = TileBoundingBoxUtils.getYPixel(height, boundingBox,
				boundingBoxSection.getMinLatitude());

		ImageRectangleF rect = new ImageRectangleF(left, top, right, bottom);

		return rect;
	}

}
