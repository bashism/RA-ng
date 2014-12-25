package edu.duke.ra.core.db;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.io.PrintStream;

import edu.duke.ra.core.RAException;
import edu.duke.ra.core.result.IQueryResult;
import edu.duke.ra.core.result.StandardQueryResult;

public class DB {

    public static void printSQLExceptionDetails(SQLException sqle, PrintStream err, boolean verbose) {
        while (sqle != null) {
            err.println("Error message: " + sqle.getMessage());
            err.println("Error code: " + sqle.getErrorCode());
            err.println("SQL state: " + sqle.getSQLState());
            sqle = sqle.getNextException();
        }
        return;
    }

    protected Connection _conn = null;
    protected String _driverName = null;
    protected String _schema = null;

    static ArrayList<String> loadedDriverNames = new ArrayList<String>();
    static List<String> supportedDriverNames = Arrays.asList(
            "org.sqlite.JDBC",
            "org.postgresql.Driver",
            "com.mysql.jdbc.Driver",
            "com.ibm.db2.jcc.DB2Driver"
        );
    static {
        for (String driverName : supportedDriverNames) {
            try {
                Class.forName(driverName);
                loadedDriverNames.add(driverName);
            } catch (ClassNotFoundException e) {
                // Silently ignore and move on to another driver.
            }
        }
        if (loadedDriverNames.isEmpty()) {
            System.err.println("No JDBC driver found; tried the following:");
            for (String driverName : supportedDriverNames) {
                 System.err.println(driverName);
            }
            System.exit(1);
        }
    }

    public DB(String connURL, Properties connProperties) throws SQLException {
        _conn = DriverManager.getConnection(connURL, connProperties);
        _driverName = DriverManager.getDriver(connURL).getClass().getName();
        _schema = connProperties.getProperty("schema");
    }

    public String getDriverName() {
        return _driverName;
    }

    public void close()
        throws SQLException {
        _conn.close();
        _conn = null;
        _driverName = null;
        _schema = null;
        return;
    }

    public void execCommands(StringBuilder out, String commands)
        throws SQLException {
        Statement s = _conn.createStatement();
        int resultNum = 0;
        while (true) {
            resultNum++;
            boolean queryResult;
            try {
                if (resultNum == 1) {
                    queryResult = s.execute(commands);
                } else {
                    queryResult = s.getMoreResults();
                }
            } catch (SQLException e) {
                out.append("*** Result " + resultNum + " is an error: " + e.getMessage() + "\n");
                /*
                 * JDBC API for handling multiple results is a mess.
                 * 
                 * For SQL Server
                 * (http://blogs.msdn.com/b/jdbcteam/archive/2008/08/01/use-execute-and-getmoreresults-methods-for-those-pesky-complex-sql-queries.aspx),
                 * apparently when an exception is thrown here, it may
                 * just be that the current statement produced an
                 * error but subsequent statements might have
                 * succeeded, so we have to continue processing other
                 * results, with "continue;" here.  If we do "break;"
                 * instead, we will miss reporting some results.
                 * 
                 * PostgreSQL apparently supports multiple statements
                 * in one execute(), but any error will abort/rollback
                 * everything, and then getMoreResults() will
                 * subsequently return false and getUpdateCount() -1.
                 * So either "continue;" or "break;" is okay here.
                 * 
                 * SQLite apparently doesn't support multiple
                 * statements in one execute(); it just executes the
                 * first one and silently ignores others.
                 * Furthermore, if the statement fails to execute,
                 * getMoreResults() will throw an exception instead of
                 * returning false.  So we must use "break;" here.
                 * 
                 * Overall, let's just go with "break;" and document
                 * this behavior.
                 */
                break;
            }
            if (queryResult) {
                out.append("*** Result " + resultNum + " is a table:\n");
                ResultSet rs = s.getResultSet();
                List<RAException> errors = new ArrayList<>();
                StandardQueryResult qrs = new StandardQueryResult(rs, errors, "", commands);
                out.append(qrs.toRawString());
                rs.close();
            } else {
                int rowsAffected = s.getUpdateCount();
                if (rowsAffected == -1) {
                    resultNum--;
                    break;
                }
                out.append("*** Result " + resultNum + " is an update count of " + rowsAffected + "\n");
            }
        }
        s.close();
        return;
    }

    public IQueryResult executeQuery(String query, String raQueryString, boolean verbose) throws SQLException {
        List<RAException> errors = new ArrayList<>();
        ResultSet resultSet = null;
        Statement statement = _conn.createStatement();
        resultSet = statement.executeQuery(query);
        IQueryResult result = new StandardQueryResult(resultSet, errors, raQueryString, query);
        return result;
    }

    public ArrayList<String> getTables()
        throws SQLException {
        ArrayList<String> tableNames = new ArrayList<String>();
        DatabaseMetaData dbmd = _conn.getMetaData();
        ResultSet rs = dbmd.getTables(null, _schema, null, new String[] { "TABLE", "VIEW" });
        while (rs.next()) {
            String tableName = rs.getString(3);
            tableNames.add(tableName);
        }
        rs.close();
        return tableNames;
    }

    public TableSchema getTableSchema(String tableName)
        throws SQLException {
        TableSchema schema = getOutputSchema("SELECT * FROM " + tableName);
        return new TableSchema(tableName, schema.getColNames(), schema.getColTypes());
    }

    // The following implementation does not seem to work for
    // postgresql when tableName is a view:
    //
    // public TableSchema getTableSchema(String tableName)
    //     throws SQLException {
    //     ArrayList<String> colNames = new ArrayList<String>();
    //     ArrayList<String> colTypes = new ArrayList<String>();
    //     DatabaseMetaData dbmetadta = _conn.getMetaData();
    //     ResultSet rs = dbmetadta.getColumns(null, null, tableName, null);
    //     while (rs.next()) {
    //         String colName = rs.getString(4);
    //         String colType = rs.getString(6);
    //         colNames.add(colName);
    //         colTypes.add(colType);
    //     }
    //     rs.close();
    //     if (colNames.isEmpty()) // No such table!
    //         return null;
    //     return new TableSchema(tableName, colNames, colTypes);
    // }

    public TableSchema getOutputSchema(String query)
            throws SQLException {
            ArrayList<String> colNames = new ArrayList<String>();
            ArrayList<String> colTypes = new ArrayList<String>();
            Statement s = _conn.createStatement();
            ResultSet rs = s.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();
            int numCols = rsmd.getColumnCount();
            for (int i=1; i<=numCols; i++) {
                // Important: Use getColumnLabel() to get new column names specified
                // in AS or in CREATE VIEW.  For some JDBC drivers, getColumnName()
                // gives the original column names inside base tables.
                colNames.add(rsmd.getColumnLabel(i));
                colTypes.add(rsmd.getColumnTypeName(i));
            }
            rs.close();
            return new TableSchema(null, colNames, colTypes);
    }
}
