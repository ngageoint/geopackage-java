package mil.nga.geopackage.tiles;

import java.awt.image.BufferedImage;

/**
 * Image utility methods
 * 
 * @author osbornb
 */
public class ImageUtils {

	/**
	 * png image format
	 */
	public static final String IMAGE_FORMAT_PNG = "png";

	/**
	 * jpg image format
	 */
	public static final String IMAGE_FORMAT_JPG = "jpg";

	/**
	 * jpeg image format
	 */
	public static final String IMAGE_FORMAT_JPEG = "jpeg";

	/**
	 * Create a buffered image for the dimensions and image format
	 * 
	 * @param width
	 * @param height
	 * @param imageFormat
	 * @return
	 */
	public static BufferedImage createBufferedImage(int width, int height,
			String imageFormat) {

		int imageType;

		switch (imageFormat.toLowerCase()) {
		case IMAGE_FORMAT_JPG:
		case IMAGE_FORMAT_JPEG:
			imageType = BufferedImage.TYPE_INT_RGB;
			break;
		default:
			imageType = BufferedImage.TYPE_INT_ARGB;
		}

		BufferedImage image = new BufferedImage(width, height, imageType);

		return image;
	}

}
