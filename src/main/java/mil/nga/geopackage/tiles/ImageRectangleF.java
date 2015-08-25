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
	 * Get the left
	 * 
	 * @return
	 */
	public float getLeft() {
		return left;
	}

	/**
	 * Get the right
	 * 
	 * @return
	 */
	public float getRight() {
		return right;
	}

	/**
	 * Get the top
	 * 
	 * @return
	 */
	public float getTop() {
		return top;
	}

	/**
	 * Get the bottom
	 * 
	 * @return
	 */
	public float getBottom() {
		return bottom;
	}

	/**
	 * Round the floating point rectangle to an integer rectangle
	 * 
	 * @return
	 */
	public ImageRectangle round() {
		return new ImageRectangle(Math.round(left), Math.round(top),
				Math.round(right), Math.round(bottom));
	}

	/**
	 * Check if the rectangle is valid
	 * 
	 * @return
	 */
	public boolean isValid() {
		return left < right && top < bottom;
	}

}
