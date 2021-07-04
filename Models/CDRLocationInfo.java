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
public class CDRLocationInfo {

    private String cdrLocationInfoId;
    private String serverID;
    private String filePath;
    private String type;

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCdrLocationInfoId() {
        return cdrLocationInfoId;
    }

    public void setCdrLocationInfoId(String cdrLocationInfoId) {
        this.cdrLocationInfoId = cdrLocationInfoId;
    }

}
