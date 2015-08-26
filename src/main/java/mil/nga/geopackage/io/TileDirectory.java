package mil.nga.geopackage.io;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a z,x,y directory structure
 * 
 * @author osbornb
 */
public class TileDirectory {

	public File directory;
	public int minZoom = Integer.MAX_VALUE;
	public int maxZoom = Integer.MIN_VALUE;
	public Map<Integer, ZoomDirectory> zooms = new TreeMap<>();

	/**
	 * Zoom level directory
	 */
	public class ZoomDirectory {

		public File directory;
		public int zoom;
		public int minX = Integer.MAX_VALUE;
		public int maxX = Integer.MIN_VALUE;
		public int minY = Integer.MAX_VALUE;
		public int maxY = Integer.MIN_VALUE;
		public Map<Integer, XDirectory> xValues = new TreeMap<>();

	}

	/**
	 * X level directory
	 */
	public class XDirectory {

		public File directory;
		public int x;
		public int minY = Integer.MAX_VALUE;
		public int maxY = Integer.MIN_VALUE;
		public Map<Integer, YFile> yValues = new TreeMap<>();

	}

	/**
	 * Y level file
	 */
	public class YFile {

		public File file;
		public int y;

	}

}