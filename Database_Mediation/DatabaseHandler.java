/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Database;

import Models.CDRData;
import Models.CDRLocationInfo;
import Models.CDRStructureFields;
import Models.Rule;
import Models.ServerData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Al Badr
 */
public class DatabaseHandler {

    public static List<Integer> getUnprocessedCDRsLocInfo() {
        List<Integer> locationInfoIDs = new ArrayList();
        Connection con;
        ResultSet result;
        PreparedStatement pst;
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();

        try {
            pst = con.prepareStatement("select DISTINCT cdr_li_id from cdr where processed = ? ");
            pst.setBoolean(1, false);
            result = pst.executeQuery();
            while (result.next()) {

                locationInfoIDs.add(result.getInt("cdr_li_id"));
            }
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return locationInfoIDs;
    }

    public static ServerData getServerData(int serverID) {
        Connection con;
        ResultSet result;
        PreparedStatement pst;
        ServerData sd = new ServerData();
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        try {
            pst = con.prepareStatement("select server_id,description,ip,port,protocol,username,password,name from server where server_id=?");
            pst.setInt(1, serverID);
            result = pst.executeQuery();
            while (result.next()) {

                sd.setServerID(result.getString("server_id"));
                sd.setServerDescription(result.getString("description"));
                sd.setServerIP(result.getString("ip"));
                sd.setServerPort(result.getString("port"));
                sd.setServerProtocol(result.getString("protocol"));
                sd.setServerUserName(result.getString("username"));
                sd.setServerPassword(result.getString("password"));
                sd.setServerName(result.getString("name"));

            }
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return sd;
    }

    public static CDRLocationInfo getCDRLocationInfo(int cdr_li_id) {
        List<CDRLocationInfo> cdrLocationInfoList = new ArrayList();
        Connection con;
        ResultSet result;
        PreparedStatement pst;
        CDRLocationInfo cdrLocationInfo = new CDRLocationInfo();;
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        try {
            pst = con.prepareStatement("select cdr_li_id,filepath,server_id,type from cdr_location_info where cdr_li_id=?");
            pst.setInt(1, cdr_li_id);

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

    public static Rule getRule(int cdrLocationInfoId) {

        Connection con;
        ResultSet result;
        PreparedStatement pst;
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        Rule rule = new Rule();
        try {
            pst = con.prepareStatement("select * from rules where source_cdr_li_id=?");
            pst.setInt(1, cdrLocationInfoId);
            result = pst.executeQuery();
            while (result.next()) {
                rule.setDestination_cdr_li_id(result.getInt("destination_cdr_li_id"));
                rule.setSource_cdr_li_id(result.getInt("source_cdr_li_id"));
 
                rule.setRule_id(result.getInt("rules_id"));

            }
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return rule;

    }

    public static List<String> getCDrRemotePath(Integer locationInfoId) {
        Connection con;
        ResultSet result;
        PreparedStatement pst;
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        List<String> remoteFilesPath = new ArrayList();
        try {
            pst = con.prepareStatement("select remote_file_path from cdr where processed = ?  and cdr_li_id = ?");
            pst.setBoolean(1, false);
            pst.setInt(2, locationInfoId);
            result = pst.executeQuery();
            while (result.next()) {
                remoteFilesPath.add(result.getString("remote_file_path"));
            }
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return remoteFilesPath;

    }

    public static List<String> getTollFreeNumbers() {

        Connection con;
        PreparedStatement pst;
        ResultSet result;
        List<String> tollFreeNumbers = new ArrayList();
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

    public static boolean setProcessedCDRs(Integer cdrId, String originalFilePath, boolean isOriginal) {
        Connection con;
        PreparedStatement pst;
        ResultSet result;
        int updateStatus = 0;
        boolean isUpdated = false;
        String filePathField = "";
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        if (isOriginal) {
            filePathField = "original_file_path";
        } else {
            filePathField = "converted_file_path";
        }

        try {

            pst = con.prepareStatement("update cdr set processed = ?," + filePathField + "=? where cdr_id = ? ");
            pst.setBoolean(1, true);
            pst.setString(2, originalFilePath);
            pst.setInt(3, cdrId);
            updateStatus = pst.executeUpdate();
            if (updateStatus == 1) {
                isUpdated = true;
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return isUpdated;
    }

    public static List<Integer> getCDRIds(int cdrLocationInfo) {
        Connection con;
        PreparedStatement pst;
        ResultSet result;
        List<Integer> cdrIds = new ArrayList();
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        try {
            pst = con.prepareStatement("select cdr_id from cdr where cdr_li_id=? and processed=false");
            pst.setInt(1, cdrLocationInfo);
            result = pst.executeQuery();
            while (result.next()) {
                cdrIds.add(result.getInt("cdr_id"));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return cdrIds;
    }

    public static List<Rule> getAllRules(int sourceCDRLiId) {
        Connection con;
        PreparedStatement pst;
        ResultSet result;
        List<Rule> rules = new ArrayList();
        Rule rule;
        DatabaseConnector.initiateDBConnection();
        con = DatabaseConnector.getDBConnection();
        try {
            pst = con.prepareStatement("select * from rules where source_cdr_li_id =?");
            pst.setInt(1, sourceCDRLiId);
            result = pst.executeQuery();
            while (result.next()) {
                rule = new Rule();
                rule.setRule_id(result.getInt("rules_id"));
                rule.setSource_cdr_li_id(result.getInt("source_cdr_li_id"));
                rule.setDestination_cdr_li_id(result.getInt("destination_cdr_li_id"));
      
                rules.add(rule);
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return rules;
    }

}
