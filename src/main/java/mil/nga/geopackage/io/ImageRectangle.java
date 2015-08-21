package mil.nga.geopackage.io;

public class ImageRectangle {

	private int left;
	private int right;
	private int top;
	private int bottom;

	public ImageRectangle(int left, int top, int right, int bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public int getLeft() {
		return left;
	}

	public int getRight() {
		return right;
	}

	public int getTop() {
		return top;
	}

	public int getBottom() {
		return bottom;
	}

	public boolean isValid() {
		return left < right && top < bottom;
	}

}
