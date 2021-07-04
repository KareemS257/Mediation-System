/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Database;

import Models.CDRLocationInfo;
import Models.CDRStructureFields;
import Models.ServerData;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ntp.TimeStamp;

/**
 *
 * @author Al Badr
 */
public class DatabaseHandler {

    public static int setCDRStructure(List<String> cdrStruct) {
        Connection con;
        PreparedStatement pst;
        int insertionStatus = 0;
        String[] cdrStructArr = new String[cdrStruct.size()];
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        for (int i = 0; i < cdrStruct.size(); i++) {
            cdrStructArr[i] = cdrStruct.get(i);
        }
        try {
            pst = con.prepareStatement("insert into CDR_Structure (server_id,cdr_struct) values (?,?)");
            pst.setInt(1, 1);
            pst.setObject(2, cdrStructArr);
            insertionStatus = pst.executeUpdate();

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return insertionStatus;

    }

    public static CDRStructureFields getServerCDRStructure(int serverID) {
        Connection con;
        ResultSet result;

        String cdrFieldsStr = "";
        PreparedStatement pst;
        CDRStructureFields cdrSF = new CDRStructureFields();
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();

        try {
            pst = con.prepareStatement("select server_id,cdr_struct,cdr_struct_type from CDR_Structure where server_id =?");
            pst.setInt(1, serverID);
            result = pst.executeQuery();
            while (result.next()) {
                cdrSF.setServerID(result.getString("server_id"));
                cdrSF.setCdrStruct(result.getString("cdr_struct"));
                cdrSF.setCdrStructType(result.getString("cdr_struct_type"));
            }
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return cdrSF;
    }

    public static List<ServerData> getServersData() {
        List<ServerData> serverData = new ArrayList();
        Connection con;
        ResultSet result;
        PreparedStatement pst;
        ServerData sd;
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        try {
            pst = con.prepareStatement("select server_id,description,ip,port,protocol,username,password,name from server");
            result = pst.executeQuery();
            while (result.next()) {
                sd = new ServerData();
                sd.setServerID(result.getString("server_id"));
                sd.setServerDescription(result.getString("description"));
                sd.setServerIP(result.getString("ip"));
                sd.setServerPort(result.getString("port"));
                sd.setServerProtocol(result.getString("protocol"));
                sd.setServerUserName(result.getString("username"));
                sd.setServerPassword(result.getString("password"));
                sd.setServerName(result.getString("name"));
                serverData.add(sd);
            }
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return serverData;
    }

    public static CDRLocationInfo getCDRLocationInfo(int serverID) {
        List<CDRLocationInfo> cdrLocationInfoList = new ArrayList();
        Connection con;
        ResultSet result;
        PreparedStatement pst;
        CDRLocationInfo cdrLocationInfo = new CDRLocationInfo();;
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        try {
            pst = con.prepareStatement("select cdr_li_id,filepath,server_id,type from cdR_location_info where server_id=?");
            pst.setInt(1, serverID);

            result = pst.executeQuery();
            while (result.next()) {
                cdrLocationInfo.setCdrLocationInfoId(result.getString("cdr_li_id"));
                cdrLocationInfo.setServerID(result.getString("server_id"));
                cdrLocationInfo.setFilePath(result.getString("filepath"));
                cdrLocationInfo.setType(result.getString("type"));

            }
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return cdrLocationInfo;
    }

    public static int insertNewCDREntry(int cdrID, int cdrLocationID, boolean isProcessed, String remoteFilePath) {
        Connection con;
        PreparedStatement pst;
        int insertionStatus = 0;
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();

        try {
            pst = con.prepareStatement("insert into cdr (cdr_id,cdr_li_id,processed,cdr_timestamp,remote_file_path) values (?,?,?,?,?)");
            pst.setInt(1, cdrID);
            pst.setInt(2, cdrLocationID);
            pst.setBoolean(3, isProcessed);
            pst.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            pst.setString(5, remoteFilePath);
            insertionStatus = pst.executeUpdate();

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return insertionStatus;
    }

    public static int getLastInsertedId() {
        Connection con;
        PreparedStatement pst;
        int lastInsertedId = 0;
        ResultSet result;
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        try {
            pst = con.prepareStatement("select MAX(cdr_id) from cdr");
            result = pst.executeQuery();
            while (result.next()) {
                lastInsertedId = result.getInt(1);
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lastInsertedId;
    }

    public static List<String> getTollFreeNumbers() {

        Connection con;
        PreparedStatement pst;
        ResultSet result;
        List<String> tollFreeNumbers = new  ArrayList();
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        try {
            pst = con.prepareStatement("select number from toll_free_numbers");
            result = pst.executeQuery();
            while (result.next()) {
                tollFreeNumbers.add(result.getString("number"));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return tollFreeNumbers;
    }

}
