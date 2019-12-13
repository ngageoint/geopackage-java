package mil.nga.geopackage.extension.related.media;

import java.awt.image.BufferedImage;
import java.io.IOException;

import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.user.custom.UserCustomColumn;
import mil.nga.geopackage.user.custom.UserCustomRow;

/**
 * User Media Row containing the values from a single result set row
 * 
 * @author osbornb
 * @since 3.0.1
 */
public class MediaRow extends UserCustomRow {

	/**
	 * Constructor to create an empty row
	 * 
	 * @param table
	 *            media table
	 */
	protected MediaRow(MediaTable table) {
		super(table);
	}

	/**
	 * Constructor
	 * 
	 * @param userCustomRow
	 *            user custom row
	 */
	public MediaRow(UserCustomRow userCustomRow) {
		super(userCustomRow.getTable(), userCustomRow.getColumns(),
				userCustomRow.getRowColumnTypes(), userCustomRow.getValues());
	}

	/**
	 * Copy Constructor
	 * 
	 * @param mediaRow
	 *            media row to copy
	 */
	public MediaRow(MediaRow mediaRow) {
		super(mediaRow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MediaTable getTable() {
		return (MediaTable) super.getTable();
	}

	/**
	 * Get the id column index
	 * 
	 * @return id column index
	 */
	public int getIdColumnIndex() {
		return getColumns().getPkColumnIndex();
	}

	/**
	 * Get the id column
	 * 
	 * @return id column
	 */
	public UserCustomColumn getIdColumn() {
		return getColumns().getPkColumn();
	}

	/**
	 * Get the id
	 * 
	 * @return id
	 */
	public long getId() {
		return ((Number) getValue(getIdColumnIndex())).longValue();
	}

	/**
	 * Get the data column index
	 * 
	 * @return data column index
	 */
	public int getDataColumnIndex() {
		return getColumns().getColumnIndex(MediaTable.COLUMN_DATA);
	}

	/**
	 * Get the data column
	 * 
	 * @return data column
	 */
	public UserCustomColumn getDataColumn() {
		return getColumns().getColumn(MediaTable.COLUMN_DATA);
	}

	/**
	 * Get the data
	 * 
	 * @return data
	 */
	public byte[] getData() {
		return (byte[]) getValue(getDataColumnIndex());
	}

	/**
	 * Set the data
	 * 
	 * @param data
	 *            data
	 */
	public void setData(byte[] data) {
		setValue(getDataColumnIndex(), data);
	}

	/**
	 * Get the data image
	 * 
	 * @return image
	 * @throws IOException
	 *             upon failure
	 * @since 3.2.0
	 */
	public BufferedImage getDataImage() throws IOException {
		return ImageUtils.getImage(getData());
	}

	/**
	 * Set the data from an image
	 * 
	 * @param image
	 *            image
	 * @param imageFormat
	 *            image format
	 * @throws IOException
	 *             upon failure
	 * @since 3.2.0
	 */
	public void setData(BufferedImage image, String imageFormat)
			throws IOException {
		setData(image, imageFormat, null);
	}

	/**
	 * Set the data from an image with optional quality
	 * 
	 * @param image
	 *            image
	 * @param imageFormat
	 *            image format
	 * @param quality
	 *            null or quality between 0.0 and 1.0
	 * @throws IOException
	 *             upon failure
	 * @since 3.2.0
	 */
	public void setData(BufferedImage image, String imageFormat, Float quality)
			throws IOException {
		setData(ImageUtils.writeImageToBytes(image, imageFormat, quality));
	}

	/**
	 * Get the content type column index
	 * 
	 * @return content type column index
	 */
	public int getContentTypeColumnIndex() {
		return getColumns().getColumnIndex(MediaTable.COLUMN_CONTENT_TYPE);
	}

	/**
	 * Get the content type column
	 * 
	 * @return content type column
	 */
	public UserCustomColumn getContentTypeColumn() {
		return getColumns().getColumn(MediaTable.COLUMN_CONTENT_TYPE);
	}

	/**
	 * Get the content type
	 * 
	 * @return content type
	 */
	public String getContentType() {
		return getValue(getContentTypeColumnIndex()).toString();
	}

	/**
	 * Set the content type
	 * 
	 * @param contentType
	 *            content type
	 */
	public void setContentType(String contentType) {
		setValue(getContentTypeColumnIndex(), contentType);
	}

	/**
	 * Copy the row
	 * 
	 * @return row copy
	 */
	public MediaRow copy() {
		return new MediaRow(this);
	}

}
