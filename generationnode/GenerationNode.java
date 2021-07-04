/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package generationnode;

import Database.DatabaseHandler;
import FileParser.CSVFileParser;
import Models.CDRLocationInfo;
import Models.CDRStructureFields;
import Models.ServerData;
import cdrgeneration.CDRStructureGenerator;
import com.iti.structure.CDR_Struct;
import com.iti.structure.CDR_Struct_List;
import com.iti.structure.CDR_Structure;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.IEncoder;

/**
 *
 * @author Al Badr
 */
public class GenerationNode {

    private static List<String> cdrFieldNames = new ArrayList();
    private static final String ASN_TYPE = "asn";
    private static final String CSV_TYPE = "csv";
    private static final String CSV_FILE_NAME = "New_CDR.csv";
    private static final String ASN_FILE_NAME = "New_CDR.ber";


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TimerTask generationTask = new TimerTask() {
            @Override
            public void run() {
                GenerationNode.generateCDR();
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(generationTask, 0, 20 * 1000);

    }

    public static int generateCDR() {
        //CDR generation
        CDR_Struct newCDREntry;
        List<ServerData> serversData = DatabaseHandler.getServersData();
        List<ServerData> filteredServersData = new ArrayList();
        CDRStructureFields cdrsf = new CDRStructureFields();
        String cdrField = "";
        String cdrType = "";
        int insertionStatus = 0;
        StringTokenizer stFields;
        StringTokenizer stTypes;
        List<CDR_Struct> generatedCDREntries;
        CDR_Struct_List structList;
        CDRLocationInfo cdrli;
        FTPClient ftpClient;
        String serverFileName = "";
        String localFileName = "";
        String GNStoragePath = "E:\\ITI\\GraduationProject\\Generation_Node\\GeneratedFiles";
        List<String> tollFreeNumbers = DatabaseHandler.getTollFreeNumbers();
        int serverId = 0;
        int replyCode = 0;
        boolean isTollFree;
        boolean isCalled;
        
        String generatedPhoneNum = "";
        for (ServerData serverData : serversData) {
            if (serverData.getServerDescription().equals("upstream_server")) {
                filteredServersData.add(serverData);
            }
        }
        for (ServerData filtServerData : filteredServersData) {
            System.out.println("Server IP: "+filtServerData.getServerIP());
            serverId = CDRStructureGenerator.generateCDRID();
            structList = new CDR_Struct_List();
            generatedCDREntries = new ArrayList();
            cdrli = new CDRLocationInfo();
            cdrli = DatabaseHandler.getCDRLocationInfo(Integer.parseInt(filtServerData.getServerID()));
            cdrsf = DatabaseHandler.getServerCDRStructure(Integer.parseInt(filtServerData.getServerID()));
            List<Integer> invalidIndexes = new ArrayList();
            boolean generateTollFree = false;
            for (int i = 0; i < 3; i++) {
                invalidIndexes.add(new Random().nextInt(CDRStructureGenerator.CDR_LENGTH));
            }
            for (int i = 0; i < CDRStructureGenerator.CDR_LENGTH; i++) {
                isTollFree = false;
                isCalled = false;
                stFields = new StringTokenizer(cdrsf.getCdrStruct().replaceAll("[\\{\\}\"]", ""), ",");
                stTypes = new StringTokenizer(cdrsf.getCdrStructType().replaceAll("[\\{\\}]", ""), ",");
                while (stFields.hasMoreTokens()) {
                    cdrField = stFields.nextToken();
                    cdrType = stTypes.nextToken();
                    newCDREntry = new CDR_Struct();
                    newCDREntry.setFieldName(cdrField);
                    if (cdrType.equals(CDRStructureGenerator.BUILTIN_PHONENUM_TYPE)) {
                        if (newCDREntry.getFieldName().equals("Called Number")) {
                            isCalled = true;
                        }
                        for (Integer invalidIndex : invalidIndexes) {
                            if (i == invalidIndex) {
                                isTollFree = true;
                            }
                        }
                        if (isCalled && isTollFree) {
                            generatedPhoneNum = CDRStructureGenerator.generateTollFreeNumber();
                        } else {
                            generatedPhoneNum = CDRStructureGenerator.generatePhoneNumber();
                        }
                        newCDREntry.setFieldValue(generatedPhoneNum);

                    } else if (cdrType.equals(CDRStructureGenerator.BUILTIN_ID_TYPE)) {
                        newCDREntry.setFieldValue(Integer.toString(i));
                    } else if (cdrType.equals(CDRStructureGenerator.BUILTIN_DURATION_TYPE)) {
                        newCDREntry.setFieldValue(CDRStructureGenerator.generateDuration());
                    } else if (cdrType.equals(CDRStructureGenerator.BUILTIN_TIMESTAMP_TYPE)) {
                        newCDREntry.setFieldValue(CDRStructureGenerator.generateTimeStamp());
                    } else if (cdrType.equals(CDRStructureGenerator.BUILTIN_TYPE_STATE)) {

                        newCDREntry.setFieldValue(CDRStructureGenerator.generateState(isTollFree));
                    }
                    generatedCDREntries.add(newCDREntry);
                }
            }

            structList.setValue(generatedCDREntries);

            //initiaing encoding
            if (cdrli.getType().equals(ASN_TYPE)) {

                localFileName = ASN_FILE_NAME;
                try {
                    IEncoder<CDR_Struct_List> encoder = CoderFactory.getInstance().newEncoder("BER");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    encoder.encode(structList, baos);
                    FileOutputStream fos = new FileOutputStream(GNStoragePath + "/" + filtServerData.getServerName() + "_" + localFileName);
                    baos.writeTo(fos);
                    fos.close();
                    baos.close();

                } catch (Exception ex) {
                    Logger.getLogger(GenerationNode.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (cdrli.getFilePath() == null || cdrli.getFilePath().isEmpty()) {

                    serverFileName = "/home/" + filtServerData.getServerUserName() + "/Generated_CDRs" + "/GN_" + filtServerData.getServerName() + "_" + Integer.toString(serverId) + ".ber";
                } else {
                    serverFileName = cdrli.getFilePath() + "/GN_" + filtServerData.getServerName() + "_" + Integer.toString(serverId) + ".ber";
                }
            }
//            else if(cdrli.getType().equals(CSV_TYPE)){
//            localFileName = CSV_FILE_NAME;
//            new CSVFileParser().writeToCSV(generatedCDREntries, GNStoragePath + "/" + filtServerData.getServerName() + "_" + localFileName);
//            
//            }

            //initiate server upload
            ftpClient = new FTPClient();
            FTPFile[] files;
            try {
                FileInputStream fis = new FileInputStream(GNStoragePath + "/" + filtServerData.getServerName() + "_" + localFileName);
                if (filtServerData.getServerProtocol().equals("FTP")) {
                    System.out.println(filtServerData.getServerIP()+filtServerData.getServerPort());
                    ftpClient.connect(filtServerData.getServerIP(), Integer.parseInt(filtServerData.getServerPort()));
                    ftpClient.login(filtServerData.getServerUserName(), filtServerData.getServerPassword());
                    ftpClient.mkd(cdrli.getFilePath());
                    ftpClient.storeFile(serverFileName, fis);
                    replyCode = ftpClient.getReplyCode();
                    ftpClient.disconnect();
                } else if (filtServerData.getServerProtocol().equals("SCP")) {
                    System.out.println(filtServerData.getServerIP()+filtServerData.getServerPort());
                    GNSCPClient.connectToServer(filtServerData.getServerIP(), Integer.parseInt(filtServerData.getServerPort()), filtServerData.getServerUserName(), filtServerData.getServerPassword());
                    GNSCPClient.uploadFile(GNStoragePath + "/" + filtServerData.getServerName() + "_" + localFileName, serverFileName);
                }
                fis.close();

            } catch (IOException ex) {
                Logger.getLogger(GenerationNode.class.getName()).log(Level.SEVERE, null, ex);
            }

            insertionStatus = DatabaseHandler.insertNewCDREntry(serverId, Integer.parseInt(cdrli.getCdrLocationInfoId()), false, serverFileName);
            List<CDR_Struct> list = new ArrayList();
            CDR_Struct_List cdrList = new CDR_Struct_List();

            try {
                IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
                FileInputStream fis = new FileInputStream(GNStoragePath + "/" + filtServerData.getServerName() + "_" + localFileName);
                cdrList = decoder.decode(fis, CDR_Struct_List.class);
                list = (List<CDR_Struct>) cdrList.getValue();
                for (CDR_Struct entry : list) {
                    System.out.println(entry.getFieldName());
                    System.out.println(entry.getFieldValue());
                }
            } catch (Exception ex) {
                Logger.getLogger(GenerationNode.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        return 0;
    }

}
