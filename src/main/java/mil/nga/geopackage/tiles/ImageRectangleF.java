package mil.nga.geopackage.tiles;

/**
 * Tile image rectangle with floating point dimensions
 * 
 * @author osbornb
 */
public class ImageRectangleF {

	/**
	 * Left pixel
	 */
	private float left;

	/**
	 * Right pixel
	 */
	private float right;

	/**
	 * Top pixel
	 */
	private float top;

	/**
	 * Bottom pixel
	 */
	private float bottom;

	/**
	 * Constructor
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public ImageRectangleF(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	/**
	 * Constructor
	 * 
	 * @param rectangle
	 * @since 1.2.0
	 */
	public ImageRectangleF(ImageRectangleF rectangle) {
		this(rectangle.getLeft(), rectangle.getTop(), rectangle.getRight(),
				rectangle.getBottom());
	}

	/**
	 * Constructor
	 * 
	 * @param rectangle
	 * @since 1.2.0
	 */
	public ImageRectangleF(ImageRectangle rectangle) {
		this(rectangle.getLeft(), rectangle.getTop(), rectangle.getRight(),
				rectangle.getBottom());
	}

	/**
	 * Get the left
	 * 
	 * @return left
	 */
	public float getLeft() {
		return left;
	}

	/**
	 * Get the right
	 * 
	 * @return right
	 */
	public float getRight() {
		return right;
	}

	/**
	 * Get the top
	 * 
	 * @return top
	 */
	public float getTop() {
		return top;
	}

	/**
	 * Get the bottom
	 * 
	 * @return bottom
	 */
	public float getBottom() {
		return bottom;
	}

	/**
	 * Round the floating point rectangle to an integer rectangle
	 * 
	 * @return image rectangle
	 */
	public ImageRectangle round() {
		return new ImageRectangle(Math.round(left), Math.round(top),
				Math.round(right), Math.round(bottom));
	}

	/**
	 * Check if the rectangle is valid
	 * 
	 * @return true if valid
	 */
	public boolean isValid() {
		return left < right && top < bottom;
	}

	/**
	 * Check if the rectangle is valid allowing empty ranges
	 * 
	 * @return valid
	 * @since 1.2.1
	 */
	public boolean isValidAllowEmpty() {
		return left <= right && top <= bottom;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(bottom);
		result = prime * result + Float.floatToIntBits(left);
		result = prime * result + Float.floatToIntBits(right);
		result = prime * result + Float.floatToIntBits(top);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageRectangleF other = (ImageRectangleF) obj;
		if (Float.floatToIntBits(bottom) != Float.floatToIntBits(other.bottom))
			return false;
		if (Float.floatToIntBits(left) != Float.floatToIntBits(other.left))
			return false;
		if (Float.floatToIntBits(right) != Float.floatToIntBits(other.right))
			return false;
		if (Float.floatToIntBits(top) != Float.floatToIntBits(other.top))
			return false;
		return true;
	}

}
