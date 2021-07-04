/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

/**
 *
 * @author Al Badr
 */
public class CDRStructureFields {
        private String serverID;
    private String cdrStruct;
    private String cdrStructType;

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getCdrStruct() {
        return cdrStruct;
    }

    public void setCdrStruct(String cdrStruct) {
        this.cdrStruct = cdrStruct;
    }

    public String getCdrStructType() {
        return cdrStructType;
    }

    public void setCdrStructType(String cdrStructType) {
        this.cdrStructType = cdrStructType;
    }

    
}
