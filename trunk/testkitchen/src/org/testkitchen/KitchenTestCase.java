package org.testkitchen;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.junit.AfterClass;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryLoader;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public abstract class KitchenTestCase {
	protected static Map<String, IDatabaseConnection> connections;
	
	protected static void openConnections(String[] JNDI_NAMES) throws NamingException, SQLException, DatabaseUnitException {
		if(connections == null){
			connections = new HashMap<String, IDatabaseConnection>();
			for(String name : JNDI_NAMES){
					Connection conn = getJndiConnection(name);
					IDatabaseConnection databaseConnection = new DatabaseConnection(conn);
					connections.put(name,databaseConnection );
			} 
		}
	}

	public static void closeConnections() throws SQLException{
		if(connections != null){
		    Iterator<IDatabaseConnection> it = connections.values().iterator();
		    while (it.hasNext()) {
	    		it.next().close();
		    }
		}
	}
	
    protected static Connection getJndiConnection(String name) throws NamingException, SQLException{
    	InitialContext context = new InitialContext();
		DataSource dataSource = (DataSource) context.lookup("jdbc/"+name);
		if(dataSource == null){
			throw new NamingException("Error opening connection for "+name);
		}
		
		Connection connection = dataSource.getConnection();

    	return connection;
    }       
    protected static void runTransformation(String filename) throws KettleException {
    	runTransformation(filename, null);
    }
    
    protected static void runTransformation(String filename, String[] args) throws KettleException {
	    StepLoader.init();
	    EnvUtil.environmentInit(); 
	    TransMeta transMeta = new TransMeta(filename);
	    Trans trans = new Trans(transMeta);

	    trans.execute(null); // You can pass arguments instead of null.
	    trans.waitUntilFinished();
	    
	    if ( trans.getErrors() > 0 )
	    {
	      throw new KettleException( "There were errors during transformation execution.");
	    }
	}	

    protected static void runJob(String filename) throws KettleException, KettleXMLException {
    	runJob(filename, null);
    }	
	
    protected static void runJob(String filename,String[] args) throws KettleException, KettleXMLException{
		LogWriter log = LogWriter.getInstance(LogWriter.LOG_LEVEL_BASIC);

		StepLoader.init();
		StepLoader stepLoader = StepLoader.getInstance();
		JobEntryLoader.init();
		EnvUtil.environmentInit(); 
		
		JobMeta jobMeta = new JobMeta(log, filename, null);
		Job job = new Job(log, stepLoader, null,jobMeta);
		
		job.execute();
		job.waitUntilFinished();
		
	    if ( job.getErrors() > 0 )
	    {
	      throw new KettleException( "There were errors during job execution.");
	    }		
	}	
}
