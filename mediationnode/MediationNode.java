/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mediationnode;

import Database.DatabaseHandler;
import FileConverter.ASNFileParser;

import FileConverter.CSVFileParser;
import Model.CDRModel;
import Model.CDR_Struct;
import Models.CDRLocationInfo;
import Models.ServerData;
import Model.CDR_Struct_List;
import Models.CDRStructureFields;
import Models.Rule;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.bn.CoderFactory;
import org.bn.IDecoder;

/**
 *
 * @author Al Badr
 */
public class MediationNode {

    private static final String LOCAL_ASN_ARCHIVE_PATH = "E:\\ITI\\GraduationProject\\Mediation_Node\\Archive";
    private static final String LOCAL_CSV_ARCHIVE_PATH = "E:\\ITI\\GraduationProject\\Mediation_Node\\Archive_CSV";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        MediationNode mediationNode = new MediationNode();
        TimerTask mediationTask = new TimerTask() {
            @Override
            public void run() {
                mediationNode.startMediationProcess();
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(mediationTask, 0, 10 * 1000);
    }

    public void startMediationProcess() {
        List<Integer> detectedCDRIDs;
        CDRLocationInfo cdrli;
        ServerData serverData = new ServerData();
        CDRStructureFields cdrsf = new CDRStructureFields();
        boolean validConversion = false;
        boolean validFilteration = false;
        boolean isUploaded = false;
        StringTokenizer st;
        String fileName = "";
        List<String> processedFilePaths = new ArrayList();
        List<List<String>> validRecords = new ArrayList();
        List<String> extractedIds = new ArrayList();
        String singleID = "";
        StringTokenizer stid;
        List<Integer> locationInfoIds = DatabaseHandler.getUnprocessedCDRsLocInfo();
        List<String> remoteFilesPaths = new ArrayList();

        if (locationInfoIds.size() > 0) {

            int isConnected = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileOutputStream fos;
            first:
            for (Integer locationInfoId : locationInfoIds) {

                remoteFilesPaths = DatabaseHandler.getCDrRemotePath(locationInfoId);
                System.out.println("Found " + remoteFilesPaths.size() + " unprocessed CDRS");
                detectedCDRIDs = new ArrayList();
                detectedCDRIDs = DatabaseHandler.getCDRIds(locationInfoId);

                cdrli = new CDRLocationInfo();
                cdrli = DatabaseHandler.getCDRLocationInfo(locationInfoId);
                serverData = DatabaseHandler.getServerData(Integer.parseInt(cdrli.getServerID()));

                for (String file : remoteFilesPaths) {
                    st = new StringTokenizer(file, "/");
                    while (st.hasMoreTokens()) {
                        fileName = st.nextToken();
                        if (fileName.matches("\\*.ber")) {
                            break;
                        }
                        stid = new StringTokenizer(fileName, "_");
                        while (stid.hasMoreTokens()) {
                            singleID = stid.nextToken();
                        }
                        extractedIds.add(singleID);
                    }

                    if (cdrli.getType().equals("asn")) {

                        File asnArchive = new File(LOCAL_ASN_ARCHIVE_PATH + "\\" + serverData.getServerName() + "\\Raw");
                        if (!asnArchive.exists()) {
                            asnArchive.mkdirs();

                        }
                    } else if (cdrli.getType().equals("csv")) {
                        File csvArchive = new File(LOCAL_CSV_ARCHIVE_PATH + "\\" + serverData.getServerName() + "\\Raw");
                        if (!csvArchive.exists()) {
                            csvArchive.mkdirs();
                        }
                    }

                    if (serverData.getServerProtocol().equals("FTP")) {
                        FTPClient ftpClient = new FTPClient();
                        isConnected = MediationFTPClient.connectToServer(serverData.getServerIP(), Integer.parseInt(serverData.getServerPort()), serverData.getServerUserName(), serverData.getServerPassword());
                        ftpClient = MediationFTPClient.getFTPClientInstance();
                        baos = MediationFTPClient.downloadFile(file);
                        MediationFTPClient.disconnectFromServer();
                        try {
                            fos = new FileOutputStream(LOCAL_ASN_ARCHIVE_PATH + "\\" + serverData.getServerName() + "\\" + "Raw" + "\\" + LocalDate.now().toString() + "_" + fileName);
                            baos.writeTo(fos);

                        } catch (FileNotFoundException ex) {
                            System.out.print("There was a problem writing to the file");
                            Logger.getLogger(MediationNode.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(MediationNode.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(MediationNode.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (serverData.getServerProtocol().equals("SCP")) {

                        MediationSCPClient.connectToServer(serverData.getServerIP(), Integer.parseInt(serverData.getServerPort()), serverData.getServerUserName(), serverData.getServerPassword());
                        MediationSCPClient.downloadFile(LOCAL_ASN_ARCHIVE_PATH + "\\" + serverData.getServerName() + "\\" + "Raw" + "\\" + LocalDate.now().toString() + "_" + fileName, file);
                    }

                }

                //get valid entries in a data collection
                validRecords = new MediationNode().filterCDRs(LOCAL_ASN_ARCHIVE_PATH + "\\" + serverData.getServerName() + "\\Raw", detectedCDRIDs);
                //send the valid entries to be written to csv files
                validFilteration = new CSVFileParser().writeToCSV(validRecords, LOCAL_CSV_ARCHIVE_PATH + "\\" + serverData.getServerName(), LOCAL_ASN_ARCHIVE_PATH + "\\" + serverData.getServerName() + "\\Raw", Integer.parseInt(serverData.getServerID()), detectedCDRIDs);
                isUploaded = new MediationNode().initiateUpload(locationInfoIds, LOCAL_CSV_ARCHIVE_PATH);

                if (isUploaded) {
                    new MediationNode().setProcessedCDRs(detectedCDRIDs, LOCAL_ASN_ARCHIVE_PATH + "\\" + serverData.getServerName() + "\\Raw", LOCAL_CSV_ARCHIVE_PATH + "\\" + serverData.getServerName() + "\\Filtered", locationInfoId);
                }
                System.out.println("Full Cycle");
            }

        } else {
            System.out.println("No new CDRs Found");
        }

    }

    public boolean fromRawASNToCSV(String localArchivePath, ServerData serverData, String fileName) {
        CSVFileParser csvfp = new CSVFileParser();
        File[] files;
        CDR_Struct_List cdrStructList;
        List<CDR_Struct> decodedList = new ArrayList();
        File archiveDirectory = new File(localArchivePath + "\\" + serverData.getServerName());
        File csvArchiveDirectory = new File(LOCAL_CSV_ARCHIVE_PATH + "\\" + serverData.getServerName());
        if (!csvArchiveDirectory.exists()) {
            archiveDirectory.mkdir();
        }
        files = archiveDirectory.listFiles();
        boolean validConversion = false;
        CDR_Struct cdrStruct;

        if (files.length > 0) {

            for (File file : files) {

                System.out.println(file.getName());
                cdrStructList = new CDR_Struct_List();
                cdrStruct = new CDR_Struct();
                try {
                    FileInputStream fis = new FileInputStream(localArchivePath + "\\" + serverData.getServerName() + "\\" + file.getName());

                    IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
                    cdrStructList = decoder.decode(fis, CDR_Struct_List.class);
                    decodedList = (List<CDR_Struct>) cdrStructList.getValue();
                    fis.close();
                    validConversion = csvfp.writeToCSV(LOCAL_CSV_ARCHIVE_PATH + "\\" + serverData.getServerName() + "\\" + file.getName(), cdrStructList, Integer.parseInt(serverData.getServerID()));
                } catch (Exception ex) {
                    Logger.getLogger(MediationNode.class.getName()).log(Level.SEVERE, null, ex);
                }
                decodedList.clear();
            }
        }
        return validConversion;
    }

    public List<List<String>> filterCDRs(String rawCDRSDirectory, List<Integer> detectedCDRIds) {
        List<CDRModel> validCDREntries = new ArrayList();
        List<CDR_Struct> allCDREntries = new ArrayList();
        CDRModel cdrModel = new CDRModel();
        CDR_Struct_List cdrsl = new CDR_Struct_List();
        File[] files;
        File rawCDRsDir = new File(rawCDRSDirectory);
        List<List<String>> allRecords = new ArrayList();
        List<String> singleFileRecord = new ArrayList();
        String record = "";
        List<String> invalidEntries = new ArrayList();
        List<String> tollFreeNumbers = DatabaseHandler.getTollFreeNumbers();
        StringTokenizer st;
        List<Rule> allRules = new ArrayList();
        if (!rawCDRsDir.exists()) {
            rawCDRsDir.mkdirs();
        }
        files = rawCDRsDir.listFiles();
        boolean isIdMatching = false;
        String extractedIdString = "";
        int extractedIdInt = 0;

        if (files.length > 0) {

            for (File file : files) {
                st = new StringTokenizer(file.getName().replace(".ber", ""), "_");
                while (st.hasMoreTokens()) {
                    extractedIdString = st.nextToken();

                }
                extractedIdInt = Integer.parseInt(extractedIdString);
                for (Integer cdrId : detectedCDRIds) {
                    if (extractedIdInt == cdrId) {
                        System.out.println(extractedIdInt);
                        isIdMatching = true;
                    }
                }
                if (isIdMatching) {

                    try {

                        FileInputStream fis = new FileInputStream(rawCDRSDirectory + "\\" + file.getName());

                        IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
                        cdrsl = decoder.decode(fis, CDR_Struct_List.class);
                        allCDREntries = (List<CDR_Struct>) cdrsl.getValue();

                        for (CDR_Struct cdrStruct : allCDREntries) {

                            if (cdrStruct.getFieldName().equals("Entry_No")) {

                                if (cdrStruct.getFieldValue().equals("0")) {

                                    if (record.isEmpty()) {
                                        record += cdrStruct.getFieldValue() + ",";

                                    } else {

                                        singleFileRecord.add(record);
                                        record = "";
                                        record += cdrStruct.getFieldValue() + ",";

                                    }
                                } else {

                                    singleFileRecord.add(record);
                                    record = "";
                                    record += cdrStruct.getFieldValue() + ",";

                                }

                            } else {

                                record += cdrStruct.getFieldValue() + ",";

                            }

                        }
                        fis.close();
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(MediationNode.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(MediationNode.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    singleFileRecord.add(record);
                    record = "";
                    for (String entry : singleFileRecord) {
                        if (entry.contains("VT") || entry.contains("0:0")) {

                            invalidEntries.add(entry);
                        }
                        for (String tollFreeNumber : tollFreeNumbers) {
                            if (entry.contains(tollFreeNumber)) {
                                invalidEntries.add(entry);

                            }
                        }

                    }

                    for (String invalidEntry : invalidEntries) {
                        System.out.println(invalidEntry);
                        singleFileRecord.remove(invalidEntry);

                    }

                    if (singleFileRecord.size() != 0) {

                        allRecords.add(singleFileRecord);
                    }

                    invalidEntries.clear();
                    singleFileRecord = new ArrayList();
                    isIdMatching = false;
                }
            }
        }

        return allRecords;
    }

    public boolean initiateUpload(List<Integer> locationInfoIds, String localCSVArchivePath) {

        String serverName = "";
        ServerData serverData = new ServerData();
        Rule rule = new Rule();
        File[] files;
        CDRLocationInfo cdrli = new CDRLocationInfo();
        File csvArchiveDir = new File(localCSVArchivePath);
        File filteredCSVDir;
        boolean isUploaded = false;
        int uploadStatus = 0;
        List<Rule> rules = new ArrayList();

        if (!csvArchiveDir.exists()) {
            csvArchiveDir.mkdirs();

        }

        for (Integer id : locationInfoIds) {
            cdrli = DatabaseHandler.getCDRLocationInfo(id);
            serverData = DatabaseHandler.getServerData(Integer.parseInt(cdrli.getServerID()));
            System.out.println("ServerName: " + serverData.getServerName());
            filteredCSVDir = new File(localCSVArchivePath + "\\" + serverData.getServerName() + "\\" + "Filtered");
            if (!filteredCSVDir.exists()) {
                filteredCSVDir.mkdirs();

            }
            files = filteredCSVDir.listFiles();
            rules = DatabaseHandler.getAllRules(id);
            for (File localFile : files) {

                for (Rule singleRule : rules) {

                    cdrli = DatabaseHandler.getCDRLocationInfo(singleRule.getSource_cdr_li_id());

                    serverData = DatabaseHandler.getServerData(Integer.parseInt(cdrli.getServerID()));

                    try {

                        FileInputStream fis = new FileInputStream(localCSVArchivePath + "\\" + serverData.getServerName() + "\\" + "Filtered" + "\\" + localFile.getName());
                        cdrli = DatabaseHandler.getCDRLocationInfo(singleRule.getDestination_cdr_li_id());
                        serverData = DatabaseHandler.getServerData(Integer.parseInt(cdrli.getServerID()));
                        if (serverData.getServerProtocol().equals("FTP")) {
                            MediationFTPClient.connectToServer(serverData.getServerIP(), Integer.parseInt(serverData.getServerPort()), serverData.getServerUserName(), serverData.getServerPassword());
                            uploadStatus = MediationFTPClient.uploadFile(cdrli.getFilePath() + "/" + serverData.getServerName() + '/' + localFile.getName(), fis);
                            MediationFTPClient.disconnectFromServer();
                            isUploaded = true;
                        } else if (serverData.getServerProtocol().equals("SCP")) {
                            MediationSCPClient.connectToServer(serverData.getServerIP(), Integer.parseInt(serverData.getServerPort()), serverData.getServerUserName(), serverData.getServerPassword());
                            uploadStatus = MediationSCPClient.uploadFile(filteredCSVDir + "\\" + localFile.getName(), cdrli.getFilePath() + "/" + serverData.getServerName() + '/' + localFile.getName());
                            isUploaded = true;

                        }

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(MediationNode.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(MediationNode.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }
        return isUploaded;
    }

    public boolean setProcessedCDRs(List<Integer> detectedIds, String localASNArchiveDir, String localCSVArchiveDir, int locationInfoId) {

        Rule rule = new Rule();
        CDRLocationInfo cdrli = new CDRLocationInfo();
        rule = DatabaseHandler.getRule(locationInfoId);
        String remoteFilePath = "";
        cdrli = DatabaseHandler.getCDRLocationInfo(rule.getDestination_cdr_li_id());
        remoteFilePath = cdrli.getFilePath();
        File originalASNDir = new File(localASNArchiveDir);
        File convertedCSVDir = new File(localCSVArchiveDir);
        File[] originalFiles;
        File[] convertedFiles;
        String singleId = "";
        List<Integer> convertedCSVIds = new ArrayList();
        StringTokenizer st;
        List<Integer> convertedCDRIds = new ArrayList();

        boolean isSet = false;
        if (originalASNDir.exists()) {
            originalFiles = originalASNDir.listFiles();

            for (File file : originalFiles) {
                st = new StringTokenizer(file.getName().replace(".ber", ""), "_");
                while (st.hasMoreTokens()) {
                    singleId = st.nextToken();
                }

                convertedCDRIds.add(Integer.parseInt(singleId));

            }

            for (int i = 0; i < detectedIds.size(); i++) {
                for (int c = 0; c < convertedCDRIds.size(); c++) {

                    if (detectedIds.get(i) == convertedCDRIds.get(c)) {

                        DatabaseHandler.setProcessedCDRs(convertedCDRIds.get(c), localASNArchiveDir + "/" + originalFiles[c].getName(), true);
                    }
                }
            }

        }
        if (convertedCSVDir.exists()) {

            convertedFiles = convertedCSVDir.listFiles();
            for (File file : convertedFiles) {
                st = new StringTokenizer(file.getName().replace(".csv", ""), "_");
                while (st.hasMoreTokens()) {
                    singleId = st.nextToken();
                }

                convertedCSVIds.add(Integer.parseInt(singleId));

            }

            for (int i = 0; i < detectedIds.size(); i++) {
                for (int c = 0; c < convertedCSVIds.size(); c++) {

                    if (detectedIds.get(i) == convertedCSVIds.get(c)) {

                        DatabaseHandler.setProcessedCDRs(convertedCSVIds.get(c), cdrli.getFilePath() + "/" + convertedFiles[c].getName(), false);
                    }
                }
            }

            isSet = true;

        }

        return isSet;
    }
}
