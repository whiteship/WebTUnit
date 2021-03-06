package org.opensprout.webtest;

import java.util.List;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.opensprout.webtest.configuration.DataConfiguration;
import org.opensprout.webtest.configuration.WarConfiguration;
import org.opensprout.webtest.configuration.WebTest;
import org.opensprout.webtest.exception.TestDataInputException;
import org.opensprout.webtest.exception.WarDeployingException;
import org.opensprout.webtest.exception.WarPackagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebTestRunner extends BlockJUnit4ClassRunner {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private DataManager dataManager;
	private WarManager warManager;

	public WebTestRunner(Class<?> klass) throws InitializationError {
		super(klass);
		logger.debug("LOADING TEST CONFIGURATIONS....");
		WarConfiguration wc = klass.getAnnotation(WarConfiguration.class);
		if (wc == null)
			throw new IllegalStateException("WEB TEST MUST HAVE WAR CONFIGURATION!");
		logger.debug("SUCCESS! WAR CONFIGURATIONS LOADING....");

		DataConfiguration dc = klass.getAnnotation(DataConfiguration.class);
		if (dc != null) {
			logger.debug("SUCCESS! DATA CONFIGURATIONS LOADING....");
			dataManager = createDataManager(dc, klass);
		}

		warManager = createWarManager(wc);
	}

	private WarManager createWarManager(WarConfiguration wc) {
		return new DefaultWarManager(wc);
	}

	private DataManager createDataManager(DataConfiguration dc, Class<?> klass) {
		return new DefaultDataManager(dc, klass);
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		return getTestClass().getAnnotatedMethods(WebTest.class);
	}

	public DataManager getDataManager() {
		return dataManager;
	}

	public void setDataManager(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	public WarManager getWarManager() {
		return warManager;
	}

	public void setWarManager(WarManager warManager) {
		this.warManager = warManager;
	}

	@Override
	public void run(RunNotifier arg0) {
		if (hasDataManager())
			runWithDataManager(arg0);
		else
			runWithoutDataManager(arg0);
	}

	private boolean hasDataManager() {
		return dataManager != null;
	}

	private void runWithDataManager(RunNotifier arg0) {
		logger.debug("RUNNING WEB TEST WITH DATA MANAGER....");
		try {
			warManager.packaging();
			warManager.deploy();
			dataManager.insertTestData();
			super.run(arg0);
			dataManager.deleteTestData();
			warManager.undeploy();
		} catch (WarPackagingException e) {
			throw e;
		} catch (WarDeployingException e) {
			throw e;
		} catch (TestDataInputException e) {
			throw e;
		}catch (RuntimeException e) { // test, delete, undeploy
			try {
				logger.debug("TEST ERROR");
				dataManager.deleteTestData();
			} finally {
				warManager.undeploy();
			}
		}
	}

	private void runWithoutDataManager(RunNotifier arg0) {
		logger.debug("RUNNING WEB TEST WITHOUT DATA MANAGER....");
		try {
			warManager.packaging();
			warManager.deploy();
			super.run(arg0);
			warManager.undeploy();
		} catch (WarPackagingException e) {
			throw e;
		} catch (WarDeployingException e) {
			throw e;
		} catch (RuntimeException e) { // test, undeploy
			try{
				logger.debug("TEST ERROR");
			} finally {
				warManager.undeploy();
			}
		}
	}

}
