package org.opensprout.webtest;

import static org.junit.Assert.*;

import org.junit.Test;


public class DefaultDataManagerTest {

	class WebTest{};

	@Test
	public void findTestDataFileLocation() throws Exception {
		DefaultDataManager dm = new DefaultDataManager("testData.xml", WebTest.class);
		assertEquals("org/opensprout/webtest/testData.xml", dm.testDataFileLocation);
		assertNotNull(dm.dataset);
		assertNotNull(dm.dataSource);
		assertNotNull(dm.databaseConnection);
	}

}
