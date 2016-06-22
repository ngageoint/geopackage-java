package mil.nga.geopackage.tiles;

import mil.nga.geopackage.BoundingBox;

/**
 * Tile Bounding Box utility methods for the Java specific library
 * 
 * @author osbornb
 */
public class TileBoundingBoxJavaUtils {

	/**
	 * Get a rectangle using the tile width, height, bounding box, and the
	 * bounding box section within the outer box to build the rectangle from
	 * 
	 * @param width
	 *            width
	 * @param height
	 *            height
	 * @param boundingBox
	 *            full bounding box
	 * @param boundingBoxSection
	 *            rectangle bounding box section
	 * @return rectangle
	 * @since 1.2.0
	 */
	public static ImageRectangle getRectangle(long width, long height,
			BoundingBox boundingBox, BoundingBox boundingBoxSection) {

		ImageRectangleF rectF = getFloatRectangle(width, height, boundingBox,
				boundingBoxSection);

		ImageRectangle rect = rectF.round();

		return rect;
	}

	/**
	 * Get a rectangle with rounded floating point boundaries using the tile
	 * width, height, bounding box, and the bounding box section within the
	 * outer box to build the rectangle from
	 * 
	 * @param width
	 *            width
	 * @param height
	 *            height
	 * @param boundingBox
	 *            full bounding box
	 * @param boundingBoxSection
	 *            rectangle bounding box section
	 * @return floating point rectangle
	 * @since 1.2.0
	 */
	public static ImageRectangleF getRoundedFloatRectangle(long width,
			long height, BoundingBox boundingBox, BoundingBox boundingBoxSection) {

		ImageRectangle rect = getRectangle(width, height, boundingBox,
				boundingBoxSection);

		ImageRectangleF rectF = new ImageRectangleF(rect);

		return rectF;
	}

	/**
	 * Get a rectangle with floating point boundaries using the tile width,
	 * height, bounding box, and the bounding box section within the outer box
	 * to build the rectangle from
	 * 
	 * @param width
	 *            width
	 * @param height
	 *            height
	 * @param boundingBox
	 *            full bounding box
	 * @param boundingBoxSection
	 *            rectangle bounding box section
	 * @return floating point rectangle
	 * @since 1.2.0
	 */
	public static ImageRectangleF getFloatRectangle(long width, long height,
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
