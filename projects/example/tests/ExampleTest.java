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

import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.exception.*;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobMeta;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ExampleTest extends KitchenTestCase  {
	
	@BeforeClass 
	public static void initialize() throws Exception
    {
		String[] jndi_names = {"Source", "Warehouse"};
		openConnections(jndi_names);
   
    	// Load Mock Data into Source DB
    	IDataSet sourceDataset = new FlatXmlDataSetBuilder().build(new FileInputStream("projects/example/datasets/mock_data.xml"));
    	DatabaseOperation.CLEAN_INSERT.execute(connections.get("Source"), sourceDataset);    	
    	
    	// Load Mock Existing Data into Warehouse
    	IDataSet dimensionsDataset = new FlatXmlDataSetBuilder().build(new FileInputStream("projects/example/datasets/base_data.xml"));
    	DatabaseOperation.CLEAN_INSERT.execute(connections.get("Warehouse"), dimensionsDataset);
           
    	runJob("ETL/example/example_dimension_update.kjb");
    }
	
    @Test 
    public void testUsers() throws Exception
    {
    	IDataSet goldenDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream("projects/example/datasets/golden_data.xml"));     
    	
    	ITable expected = goldenDataSet.getTable("dim_example_users");
    	
    	String actualQuery="SELECT * FROM dim_example_users WHERE user_id <> 1 ORDER BY user_id, version";
    	ITable actual = connections.get("Warehouse").createQueryTable("actual",actualQuery); 
    	ITable filteredActual = DefaultColumnFilter.includedColumnsTable(actual, expected.getTableMetaData().getColumns());
        
        Assertion.assertEquals(expected,filteredActual);
    }

}
