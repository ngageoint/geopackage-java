package mil.nga.geopackage.test;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Abstract Base Test Case
 * 
 * @author osbornb
 */
public abstract class BaseTestCase {

	/**
	 * Constructor
	 */
	public BaseTestCase() {
	}

	@Before
	public void baseSetUp() throws Exception {

	}

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
}
