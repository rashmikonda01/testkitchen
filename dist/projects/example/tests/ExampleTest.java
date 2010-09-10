package resources.projects.example.tests;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

import java.sql.*;

import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

import org.testkitchen.KitchenTestCase;

import junit.framework.TestCase;
import junit.*;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import org.dbunit.Assertion;

import org.dbunit.DatabaseTestCase;
import org.dbunit.DatabaseUnitException;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSet;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.QueryDataSet;

import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.IncludeTableFilter;
import org.dbunit.dataset.filter.DefaultColumnFilter;

import org.dbunit.operation.DatabaseOperation;

import org.apache.commons.digester.Rule;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.vfs.FileSystemException;
import org.apache.log4j.Layout;

import ognl.OgnlException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ExampleTest extends KitchenTestCase{
	
	@BeforeClass 
	public static void initialize() throws Exception
    {
    	openConnections(new String[]{"Warehouse","Staging"});
    	connections.get("Warehouse").createQueryTable("actual","SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO'");

    	// Load Mock Data into Staging
    	IDataSet agileDataset = new FlatXmlDataSetBuilder().build(new FileInputStream("projects/test/datasets/mock_test_data.xml"));
    	DatabaseOperation.CLEAN_INSERT.execute(connections.get("Staging"), agileDataset);  
    	
    	// Load Mock Existing Data into Warehouse
 	    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
 	    builder.setColumnSensing(true);
 	    IDataSet dimensionsDataset = builder.build(new FileInputStream("projects/tests/datasets/base_data.xml"));        	
    	DatabaseOperation.CLEAN_INSERT.execute(connections.get("Warehouse"), dimensionsDataset);
           
    	runJob("ETL/test/example_transformation.ktr");
    }

    @Test 
    public void testSomething() throws Exception
    {
    	// Load Golden Data so we can compare the transformation results with what we are expecting
 	    FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
 	    builder.setColumnSensing(true);
 	    IDataSet goldenDataSet = builder.build(new FileInputStream("projects/example/datasets/golden_dimensions.xml"));    	

    	ITable expected = goldenDataSet.getTable("dim_voip_extensions");
    	
    	String actualQuery="SELECT * FROM some_table ORDER BY id desc";
    	ITable actual = connections.get("Warehouse").createQueryTable("actual",actualQuery); 
    	ITable filteredActual = DefaultColumnFilter.includedColumnsTable(actual, expected.getTableMetaData().getColumns());
        
        Assertion.assertEquals(expected,filteredActual);
    }
}
