package ext.deployit.community.importer.composite;

import com.xebialabs.deployit.booter.local.LocalBooter;
import com.xebialabs.deployit.plugin.api.udm.Version;

import ext.deployit.community.importer.composite.CompositeApplicationDescriptor;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static com.google.common.io.Resources.getResource;
import static junit.framework.Assert.assertEquals;

public class CompositeApplicationDescriptorTest {

	private CompositeApplicationDescriptor descriptor;


	@Before
	public void setUp() throws Exception {
        LocalBooter.bootWithoutGlobalContext();
		descriptor = new CompositeApplicationDescriptor(new File(getResource("composite-application-1.cad").toURI()));
	}

	@Test
	public void testGetApplication() throws Exception {
		assertEquals("PetCompositeApp", descriptor.getApplication());
	}

	@Test
	public void testGetVersion() throws Exception {
		assertEquals("3.4", descriptor.getVersion());
	}

	@Test
	public void testGetCIProperties() throws Exception {
		assertEquals(2, descriptor.getProperties().size());
		assertEquals("value1", descriptor.getProperties().get("prop1"));
		assertEquals("value2", descriptor.getProperties().get("prop2"));
	}

	@Test
	public void testGetVersions() throws Exception {
		final List<Version> versions = descriptor.getVersions();
		assertEquals("[Applications/PetClinic-Ear/1.0, Applications/PetClinic-Ear/2.0]", versions.toString());
	}
}
