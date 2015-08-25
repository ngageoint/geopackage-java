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
	 * Get the left
	 * 
	 * @return
	 */
	public int getLeft() {
		return left;
	}

	/**
	 * Get the right
	 * 
	 * @return
	 */
	public int getRight() {
		return right;
	}

	/**
	 * Get the top
	 * 
	 * @return
	 */
	public int getTop() {
		return top;
	}

	/**
	 * Get the bottom
	 * 
	 * @return
	 */
	public int getBottom() {
		return bottom;
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
