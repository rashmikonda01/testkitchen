package org.dankinsley.testkitchen;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.ext.mysql.MySqlConnection;
import org.dbunit.database.IDatabaseConnection;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
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

	protected static void openConnections(Map<String,String> JNDI_NAMES) throws NamingException, SQLException, DatabaseUnitException {
		if(connections == null){
			connections = new HashMap<String, IDatabaseConnection>();
			
			for(Map.Entry<String, String> entry : JNDI_NAMES.entrySet()){
					String jndiname = entry.getKey();
					String schema = entry.getValue();
					String dbType = jndiname.contains(":") ? jndiname.split(":")[0].toUpperCase() : null;
					jndiname = jndiname.contains(":") ? jndiname.split(":")[1] : jndiname;
					
					Connection conn = getJndiConnection(jndiname);
					IDatabaseConnection databaseConnection = null;
					
					if ("MYSQL".equals(dbType)){
						databaseConnection = new MySqlConnection(conn,schema);
					} else {
						databaseConnection = new DatabaseConnection(conn, schema);
					}

					connections.put(jndiname,databaseConnection );
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
    	// Init with "false" parameter so it doesn't try to initialize simple-jndi (since we are doing it ourselves)
    	KettleEnvironment.init(false);
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
    	// Init with "false" parameter so it doesn't try to initialize simple-jndi (since we are doing it ourselves)		
		KettleEnvironment.init(false);
		JobMeta jobMeta = new JobMeta(filename, null);
		
		Job job = new Job(null,jobMeta);
		
		job.run();
		job.waitUntilFinished();
		
	    if ( job.getErrors() > 0 )
	    {
	      throw new KettleException( "There were errors during job execution.");
	    }		
	}	
}
