/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mediationnode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 *
 * @author Al Badr
 */
public class MediationFTPClient extends FTP {

    private static FTPClient ftpClient;

    public static int connectToServer(String hostName, int port, String userName, String password) {

        ftpClient = new FTPClient();
        int validConnection = 0;

        try {
            ftpClient.connect(hostName, port);
            ftpClient.login(userName, password);
            validConnection = 1;
        } catch (IOException ex) {
            Logger.getLogger(MediationFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validConnection;
    }

    public static List<FTPFile> listServerFiles(String pathName) {

        List<FTPFile> ftpFiles = new ArrayList();
        try {
            FTPFile[] files = ftpClient.listFiles(pathName);
            ftpFiles = Arrays.asList(files);
        } catch (IOException ex) {
            Logger.getLogger(MediationFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ftpFiles;
    }

    public static int uploadFile(String filePath, FileInputStream fis) {
        int uploadStatus = 0;
        FTPFile[] directories;
        StringTokenizer st;
        String destinationDirectory = "";
        String token = "";
        boolean exists = false;
        try {
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            directories = ftpClient.listDirectories();
            for (FTPFile directory : directories) {
                if (filePath.contains(directory.getName())) {
                    exists = true;
                    
                }

            }
            if(!exists){
            st = new StringTokenizer(filePath, "/");
            while (st.hasMoreTokens()) {
                token = st.nextToken();
                if (!token.contains(".csv")) {
                    destinationDirectory += "/" + token;
                }
                
            }
              System.out.println(destinationDirectory);
            ftpClient.mkd(destinationDirectory);
            }
          
            ftpClient.storeFile(filePath, fis);
           

            fis.close();
            uploadStatus = 1;
        } catch (IOException ex) {
            Logger.getLogger(MediationFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return uploadStatus;
    }

    public static ByteArrayOutputStream downloadFile(String fileName) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {

            ftpClient.retrieveFile(fileName, bos);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MediationFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bos;
    }

    public static void disconnectFromServer() {
        try {
            ftpClient.disconnect();
        } catch (IOException ex) {
            Logger.getLogger(MediationFTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static FTPClient getFTPClientInstance (){
        return ftpClient;
    }

}
