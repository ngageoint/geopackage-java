package mil.nga.geopackage.io;

public class ImageRectangleF {

	private float left;
	private float right;
	private float top;
	private float bottom;

	public ImageRectangleF(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public float getLeft() {
		return left;
	}

	public float getRight() {
		return right;
	}

	public float getTop() {
		return top;
	}

	public float getBottom() {
		return bottom;
	}

	public ImageRectangle round() {
		return new ImageRectangle(Math.round(left), Math.round(top),
				Math.round(right), Math.round(bottom));
	}

	public boolean isValid() {
		return left < right && top < bottom;
	}

}
