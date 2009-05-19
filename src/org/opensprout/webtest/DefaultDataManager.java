package org.opensprout.webtest;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.opensprout.webtest.configuration.DataConfiguration;
import org.opensprout.webtest.configuration.DataType;
import org.opensprout.webtest.exception.DataManagerSettingException;
import org.opensprout.webtest.exception.TestDataDeleteException;
import org.opensprout.webtest.exception.TestDataInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DefaultDataManager implements DataManager {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	IDataSet dataset;
	DatabaseConnection databaseConnection;

	DataSource dataSource;
	String testDataFileLocation;
	DataType dataType;

	public DefaultDataManager(DataConfiguration dc, Class<?> klass) {
		this(getFileName(dc), getPropertiesName(dc), klass);
	}

	private static String getFileName(DataConfiguration dc) {
		return (String) AnnotationUtils.getValue(dc, "fileName");
	}

	private static String getPropertiesName(DataConfiguration dc) {
		return (String) AnnotationUtils.getValue(dc, "value");
	}

	public DefaultDataManager(String fileName, String propertiesName, Class<?> klass) {
		try {
			this.dataType = makeDataType(fileName);
			testDataFileLocation = makeDefaultFileLocation(fileName, klass);
			dataSource = makeDataSource(propertiesName);
			dataset = makeDataset();
			databaseConnection = makeDBConnection();
		} catch (Exception e) {
			logger.debug("DATA MANAGER SETTING ERROR", e);
			throw new DataManagerSettingException(e);
		}
	}

	private DataType makeDataType(String fileName) {
		if (fileName.endsWith(".xml"))
			return DataType.XML;
		else if (fileName.endsWith(".xls"))
			return DataType.EXEL;
		throw new IllegalStateException("FILE NAME IS ILLEGAL");
	}

	private String makeDefaultFileLocation(String fileName, Class<?> klass) {
		String result = fileName;
		String defaultPackage = klass.getPackage().getName().replace(".", "/");

		if (result.contains("/"))
			return result;
		else
			return defaultPackage + "/" + result;
	}

	private DatabaseConnection makeDBConnection()
			throws CannotGetJdbcConnectionException, DatabaseUnitException {
		return new DatabaseConnection(DataSourceUtils.getConnection(dataSource));
	}

	private IDataSet makeDataset() throws IOException, DataSetException {
		InputStream sourceStream = new ClassPathResource(testDataFileLocation)
				.getInputStream();
		return makeDataSet(dataType, sourceStream);
	}

	private DataSource makeDataSource(String propertiesName) {
		Properties properties = new Properties();
		java.net.URL url = ClassLoader.getSystemResource(propertiesName);
		try {
			properties.load(url.openStream());
		} catch (IOException e) {
			logger.debug("PROPERTIES FILE IS ILLEGAL");
			throw new IllegalStateException("PROPERTIES FILE IS ILLEGAL", e);
		}

		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass(properties.getProperty("db.driver"));
			dataSource.setJdbcUrl(properties.getProperty("db.url"));
			dataSource.setUser(properties.getProperty("db.username"));
			dataSource.setPassword(properties.getProperty("db.password"));
		} catch (PropertyVetoException e) {
			logger.debug("PROPERTIES VALUE IS ILLEGAL");
			throw new IllegalArgumentException("PROPERTIES VALUE IS ILLEGAL", e);
		}

		return dataSource;
	}

	private IDataSet makeDataSet(DataType dataType, InputStream sourceStream)
			throws DataSetException, IOException {
		if (dataType == DataType.XML)
			return new FlatXmlDataSet(sourceStream);
		else if (dataType == DataType.EXEL)
			return new XlsDataSet(sourceStream);
		else
			throw new IllegalArgumentException();
	}

	public void insertTestData() {
		try {
			DatabaseOperation operation = DatabaseOperation.CLEAN_INSERT;
			operation.execute(databaseConnection, dataset);
			dataSource.getConnection().commit();
			logger.debug("TEST DATA INPUT OK....");
		} catch (Exception e) {
			logger.debug("TEST DATA INPUT ERROR", e);
			try {
				dataSource.getConnection().rollback();
				logger.debug("TEST DATA INPUT ROLLBACK OK....", e);
			} catch (SQLException e1) {
				logger.debug("TEST DATA INPUT ROLLBACK FAILED!", e);
				throw new TestDataInputException(
						"TEST DATA INPUT ROLLBACK FAILED");
			}
			throw new TestDataInputException("TEST DATA INPUT ERROR");
		}
	}

	public void deleteTestData() {
		try {
			DatabaseOperation operation = DatabaseOperation.DELETE_ALL;
			operation.execute(databaseConnection, dataset);
			dataSource.getConnection().commit();
			logger.debug("TEST DATA DELETE OK....");
		} catch (Exception e) {
			logger.debug("TEST DATA DELETE ERROR", e);
			try {
				dataSource.getConnection().rollback();
				logger.debug("TEST DATA DELETE ROLLBACK OK....", e);
			} catch (SQLException e1) {
				logger.debug("TEST DATA DELETE ROLLBACK FAILED!", e);
				throw new TestDataInputException(
						"TEST DATA DELETE ROLLBACK FAILED");
			}
			throw new TestDataDeleteException("TEST DATA DELETE ERROR");
		}
	}
}
