package org.opensprout.webtest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.opensprout.webtest.configuration.WarConfiguration;
import org.opensprout.webtest.configuration.WebTest;
import org.opensprout.webtest.exception.WarDeployingException;
import org.opensprout.webtest.exception.WarPackagingException;
import org.opensprout.webtest.exception.WarUnDeployingException;
import org.opensprout.webtest.exception.WebTestException;

@WarConfiguration("springsprout")
public class WebTestRunnerTest {

	WebTestRunnerStub runner;
	WarManager mockWarManager;
	DataManager mockDataManager;

	@Before
	public void setUp() throws InitializationError {
		runner = new WebTestRunnerStub(this.getClass());
		mockWarManager = mock(WarManager.class);
		mockDataManager = mock(DataManager.class);
		runner.setWarManager(mockWarManager);
		runner.setDataManager(mockDataManager);
	}

	@Test
	public void testB() {
	}

	@WebTest
	public void webTestA() {
		System.out.println("webTestA");
	}

	@WebTest
	public void webTestB() {
		System.out.println("webTestB");
	}

	@WebTest
	@Ignore
	public void webTestC() {
		System.out.println("webTestC");
	}

	@Test
	public void findWebTest() throws Exception {
		runner.computeTestMethods();
		assertTrue(runner.getTestMethodName().contains("webTestA"));
		assertTrue(runner.getTestMethodName().contains("webTestB"));
		assertFalse(runner.getTestMethodName().contains("testB"));
	}

	// with DataManager

	@Test
	public void bestSinarioWithDataManager() throws Exception {
		RunNotifier mockRN = mock(RunNotifier.class);
		runner.run(mockRN);

		verify(mockWarManager).packaging();
		verify(mockWarManager).deploy();
		verify(mockDataManager).insertTestData();
		// running tests
		verify(mockDataManager).deleteTestData();
		verify(mockWarManager).undeploy();
	}

	@Test(expected = WarPackagingException.class)
	public void packingFailWithDataManager() throws Exception {
		doThrow(new WarPackagingException()).when(mockWarManager).packaging();

		RunNotifier mockRN = mock(RunNotifier.class);
		runner.run(mockRN);

		verify(mockWarManager).packaging();
		verify(mockWarManager, never()).deploy();
		verify(mockWarManager, never()).undeploy();
		verifyNoMoreInteractions(mockDataManager);
	}

	@Test(expected = WarDeployingException.class)
	public void deployFailWithDataManager() throws Exception {
		doThrow(new WarDeployingException()).when(mockWarManager).deploy();

		RunNotifier mockRN = mock(RunNotifier.class);
		runner.run(mockRN);

		verify(mockWarManager).packaging();
		verify(mockWarManager).deploy();
		verify(mockWarManager, never()).undeploy();
		verifyNoMoreInteractions(mockDataManager);
	}

	@Test
	public void insertDataFailWithDataManager() throws Exception {
		doThrow(new WebTestException()).when(mockDataManager).insertTestData();

		RunNotifier mockRN = mock(RunNotifier.class);
		runner.run(mockRN);

		verify(mockWarManager).packaging();
		verify(mockWarManager).deploy();
		verify(mockDataManager).insertTestData();
		verify(mockDataManager).deleteTestData();
		verify(mockWarManager).undeploy();
	}

	@Test(expected = WebTestException.class)
	public void deleteDataFailWithDataManager() throws Exception {
		doThrow(new WebTestException()).when(mockDataManager).deleteTestData();

		RunNotifier mockRN = mock(RunNotifier.class);
		runner.run(mockRN);

		verify(mockWarManager).packaging();
		verify(mockWarManager).deploy();
		verify(mockDataManager).insertTestData();
		// running tests
		verify(mockDataManager).deleteTestData();
		verify(mockWarManager).undeploy();
	}

	@Test(expected = WarUnDeployingException.class)
	public void undeployFailWithDataManager() throws Exception {
		doThrow(new WarUnDeployingException()).when(mockWarManager).undeploy();

		RunNotifier mockRN = mock(RunNotifier.class);
		runner.run(mockRN);

		verify(mockWarManager).packaging();
		verify(mockWarManager).deploy();
		verify(mockDataManager).insertTestData();
		// running tests
		verify(mockDataManager).deleteTestData();
		verify(mockWarManager).undeploy();
	}

	// without DataManager

	@Test
	public void bestSinarioWithoutDataManager() throws Exception {
		runner.setDataManager(null);

		RunNotifier mockRN = mock(RunNotifier.class);
		runner.run(mockRN);

		verify(mockWarManager).packaging();
		verify(mockWarManager).deploy();
		// running tests
		verify(mockWarManager).undeploy();
	}

	@Test(expected = WarPackagingException.class)
	public void packingFailWithoutDataManager() throws Exception {
		runner.setDataManager(null);

		doThrow(new WarPackagingException()).when(mockWarManager).packaging();

		RunNotifier mockRN = mock(RunNotifier.class);
		runner.run(mockRN);

		verify(mockWarManager).packaging();
		verify(mockWarManager, never()).deploy();
		verify(mockWarManager, never()).undeploy();
		verifyNoMoreInteractions(mockDataManager);
	}

	@Test(expected = WarDeployingException.class)
	public void deployingFailWithoutDataManager() throws Exception {
		runner.setDataManager(null);

		doThrow(new WarDeployingException()).when(mockWarManager).deploy();

		RunNotifier mockRN = mock(RunNotifier.class);
		runner.run(mockRN);

		verify(mockWarManager).packaging();
		verify(mockWarManager).deploy();
		verify(mockWarManager, never()).undeploy();
		verifyNoMoreInteractions(mockDataManager);
	}

	@Test(expected = WarUnDeployingException.class)
	public void undeployingFailWithoutDataManager() throws Exception {
		runner.setDataManager(null);

		doThrow(new WarUnDeployingException()).when(mockWarManager).undeploy();

		RunNotifier mockRN = mock(RunNotifier.class);
		runner.run(mockRN);

		verify(mockWarManager).packaging();
		verify(mockWarManager).deploy();
		verify(mockWarManager).undeploy();
		verifyNoMoreInteractions(mockDataManager);
	}



}
