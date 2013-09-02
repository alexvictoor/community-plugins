package ext.deployit.community.plugin.lock;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import com.xebialabs.deployit.booter.local.LocalBooter;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.base.BaseContainer;

public class LockFileHelperTest {

	private ConfigurationItem ci;
	private ConfigurationItem ci2;
	private LockHelper lockHelper = new LockHelper();

	@Before
	public void createContainer() {
        LocalBooter.bootWithoutGlobalContext();
        this.ci = new BaseContainer();
		this.ci.setId("Infrastructure/lock/TestContainer");
		this.ci2 = new BaseContainer();
		this.ci2.setId("Infrastructure/lock/TestContainer2");
	}

	@Before
	public void clearLockDirectory() {
		lockHelper.clearLocks();
	}

	@Test
	public void shouldCorrectlyConvertCiIdToLockFileNameAndBack() throws FileNotFoundException {
		String lockFileName = lockHelper.ciIdToLockFileName(ci.getId());
		assertThat(ci.getId(), is(equalTo(lockHelper.lockFileNameToCiId(lockFileName))));
	}

	@Test
	public void shouldCorrectlyLockContainer() throws IOException {
		assertThat(lockHelper.isLocked(ci), is(equalTo(false)));
		
		assertThat(lockHelper.lock(ci), is(equalTo(true)));
		assertThat(lockHelper.isLocked(ci), is(equalTo(true)));
	}

	@Test
	public void shouldNotAllowLockIfAlreadyLocked() throws IOException {
		assertThat(lockHelper.isLocked(ci), is(equalTo(false)));
		
		assertThat(lockHelper.lock(ci), is(equalTo(true)));		
		assertThat(lockHelper.isLocked(ci), is(equalTo(true)));
		
		assertThat(lockHelper.lock(ci), is(equalTo(false)));
	}

	@Test
	public void shouldCorrectlyClearLocks() throws IOException {
		assertThat(lockHelper.lock(ci), is(equalTo(true)));		

		lockHelper.clearLocks();
		
		assertThat(lockHelper.isLocked(ci), is(equalTo(false)));

		assertThat(lockHelper.lock(ci), is(equalTo(true)));		
	}

	@Test
	public void shouldCorrectlyLockAllCis() throws IOException {
		assertThat(lockHelper.atomicallyLock(Lists.newArrayList(ci, ci2)), is(equalTo(true)));		

		assertThat(lockHelper.isLocked(ci), is(equalTo(true)));
		assertThat(lockHelper.isLocked(ci2), is(equalTo(true)));
	}
	
	@Test
	public void shouldReleaseLocksWhenAtomicLockingFails() throws IOException {
		assertThat(lockHelper.lock(ci2), is(equalTo(true)));		
		assertThat(lockHelper.atomicallyLock(Lists.newArrayList(ci, ci2)), is(equalTo(false)));		

		assertThat(lockHelper.isLocked(ci), is(equalTo(false)));
		assertThat(lockHelper.isLocked(ci2), is(equalTo(true)));
	}
}
