package mil.nga.geopackage.tiles;

/**
 * Tile image rectangle with integer dimensions
 * 
 * @author osbornb
 */
public class ImageRectangle {

	/**
	 * Left pixel
	 */
	private int left;

	/**
	 * Right pixel
	 */
	private int right;

	/**
	 * Top pixel
	 */
	private int top;

	/**
	 * Bottom pixel
	 */
	private int bottom;

	/**
	 * Constructor
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public ImageRectangle(int left, int top, int right, int bottom) {
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
	public ImageRectangle(ImageRectangle rectangle) {
		this(rectangle.getLeft(), rectangle.getTop(), rectangle.getRight(),
				rectangle.getBottom());
	}

	/**
	 * Get the left
	 * 
	 * @return left
	 */
	public int getLeft() {
		return left;
	}

	/**
	 * Get the right
	 * 
	 * @return right
	 */
	public int getRight() {
		return right;
	}

	/**
	 * Get the top
	 * 
	 * @return top
	 */
	public int getTop() {
		return top;
	}

	/**
	 * Get the bottom
	 * 
	 * @return bottom
	 */
	public int getBottom() {
		return bottom;
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
		result = prime * result + bottom;
		result = prime * result + left;
		result = prime * result + right;
		result = prime * result + top;
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
		ImageRectangle other = (ImageRectangle) obj;
		if (bottom != other.bottom)
			return false;
		if (left != other.left)
			return false;
		if (right != other.right)
			return false;
		if (top != other.top)
			return false;
		return true;
	}

}
