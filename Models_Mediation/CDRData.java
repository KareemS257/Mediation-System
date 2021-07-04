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
public class CDRData {

    private int cdr_id;
    private int cdr_li_id;
    private boolean isProcessed;
    private String cdrTimeStamp;
    private String originalFilePath;
    private String convertedFilePath;
    private String remoteFilePath;

    public int getCdr_id() {
        return cdr_id;
    }

    public void setCdr_id(int cdr_id) {
        this.cdr_id = cdr_id;
    }

    public int getCdr_li_id() {
        return cdr_li_id;
    }

    public void setCdr_li_id(int cdr_li_id) {
        this.cdr_li_id = cdr_li_id;
    }

    public boolean isIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public String getCdrTimeStamp() {
        return cdrTimeStamp;
    }

    public void setCdrTimeStamp(String cdrTimeStamp) {
        this.cdrTimeStamp = cdrTimeStamp;
    }

    public String getOriginalFilePath() {
        return originalFilePath;
    }

    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }

    public String getConvertedFilePath() {
        return convertedFilePath;
    }

    public void setConvertedFilePath(String convertedFilePath) {
        this.convertedFilePath = convertedFilePath;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
    }

}
