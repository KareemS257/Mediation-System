/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Database;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Al Badr
 */
public class DatabaseConnector {

    private static final String url = "jdbc:postgresql:mediation_db";
    private static final String user = "postgres";
    private final static String password = "new";
    private static Connection connection;
    
    public static void initiateDBConnection(){
        
    try {
            
            connection = DriverManager.getConnection(url, user, password);
            
            
        }
        catch(SQLException e){
        e.printStackTrace();
        }
    }
    public static Connection getDBConnection(){
        return connection;
    }

}

