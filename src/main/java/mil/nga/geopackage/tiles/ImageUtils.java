package mil.nga.geopackage.tiles;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import mil.nga.geopackage.GeoPackageException;

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
	 * tiff image format
	 * 
	 * @since 1.2.1
	 */
	public static final String IMAGE_FORMAT_TIFF = "tiff";

	/**
	 * Create a buffered image for the dimensions and image format
	 * 
	 * @param width
	 * @param height
	 * @param imageFormat
	 * @return image
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
	 * @return true if fully transparent
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
	 * @return true if transparent
	 */
	public static boolean isTransparent(BufferedImage image, int x, int y) {
		int pixel = image.getRGB(x, y);
		boolean transparent = (pixel >> 24) == 0x00;
		return transparent;
	}

	/**
	 * Get a buffered image of the image bytes
	 * 
	 * @param imageBytes
	 * @return buffered image or null
	 * @throws IOException
	 * @since 1.1.2
	 */
	public static BufferedImage getImage(byte[] imageBytes) throws IOException {

		BufferedImage image = null;

		if (imageBytes != null) {
			ByteArrayInputStream stream = new ByteArrayInputStream(imageBytes);
			image = ImageIO.read(stream);
			stream.close();
		}

		return image;
	}

	/**
	 * Write the image to bytes in the provided format and optional quality
	 * 
	 * @param image
	 *            buffered image
	 * @param formatName
	 *            image format name
	 * @param quality
	 *            null or quality between 0.0 and 1.0
	 * @return image bytes
	 * @throws IOException
	 * @since 1.1.2
	 */
	public static byte[] writeImageToBytes(BufferedImage image,
			String formatName, Float quality) throws IOException {
		byte[] bytes = null;
		if (quality != null) {
			bytes = compressAndWriteImageToBytes(image, formatName, quality);
		} else {
			bytes = writeImageToBytes(image, formatName);
		}
		return bytes;
	}

	/**
	 * Write the image to bytes in the provided format
	 * 
	 * @param image
	 *            buffered image
	 * @param formatName
	 *            image format name
	 * @return image bytes
	 * @throws IOException
	 * @since 1.1.2
	 */
	public static byte[] writeImageToBytes(BufferedImage image,
			String formatName) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ImageIO.write(image, formatName, stream);
		stream.flush();
		byte[] bytes = stream.toByteArray();
		stream.close();
		return bytes;
	}

	/**
	 * Compress and write the image to bytes in the provided format and quality
	 * 
	 * @param image
	 *            buffered image
	 * @param formatName
	 *            image format name
	 * @param quality
	 *            quality between 0.0 and 1.0
	 * @return compressed image bytes
	 * @since 1.1.2
	 */
	public static byte[] compressAndWriteImageToBytes(BufferedImage image,
			String formatName, float quality) {

		byte[] bytes = null;

		Iterator<ImageWriter> writers = ImageIO
				.getImageWritersByFormatName(formatName);
		if (writers == null || !writers.hasNext()) {
			throw new GeoPackageException(
					"No Image Writer to compress format: " + formatName);
		}
		ImageWriter writer = writers.next();
		ImageWriteParam writeParam = writer.getDefaultWriteParam();
		writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		writeParam.setCompressionQuality(quality);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageOutputStream ios = null;
		try {
			ios = ImageIO.createImageOutputStream(baos);
			writer.setOutput(ios);
			writer.write(null, new IIOImage(image, null, null), writeParam);
			writer.dispose();

			bytes = baos.toByteArray();

		} catch (IOException e) {
			throw new GeoPackageException(
					"Failed to compress image to format: " + formatName
							+ ", with quality: " + quality, e);
		} finally {
			closeQuietly(ios);
			closeQuietly(baos);
		}

		return bytes;
	}

	/**
	 * Close quietly
	 * 
	 * @param closeable
	 * @since 1.1.2
	 */
	public static void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// Eat
			}
		}
	}

}
