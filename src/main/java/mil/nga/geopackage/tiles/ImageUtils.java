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

	/**
	 * Check if the image is fully transparent, meaning it contains only
	 * transparent pixels as an empty image
	 * 
	 * @param image
	 * @return
	 */
	public static boolean isFullyTransparent(BufferedImage image) {
		boolean transparent = true;
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				transparent = isTransparent(image, x, y);
				if (!transparent) {
					break;
				}
			}
			if (!transparent) {
				break;
			}
		}
		return transparent;
	}

	/**
	 * Check if the pixel in the image at the x and y is transparent
	 * 
	 * @param image
	 * @param x
	 * @param y
	 * @return
	 */
	public static boolean isTransparent(BufferedImage image, int x, int y) {
		int pixel = image.getRGB(x, y);
		boolean transparent = (pixel >> 24) == 0x00;
		return transparent;
	}

}
