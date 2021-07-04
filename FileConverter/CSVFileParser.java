/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileConverter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import Database.DatabaseHandler;
import Model.CDRModel;
import Model.CDR_Struct;
import Model.CDR_Struct_List;
import Models.CDRStructureFields;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Al Badr
 */
public class CSVFileParser {

    BufferedReader bufferReader;
    FileReader fileReader;
    private List<String> keys;
    FileWriter fileWriter;
    private List<String> headerFields;
    private List<String> values;
    private List<List<String>> lines;

    public CSVFileParser() {
        keys = new ArrayList();
        keys.add("Called Number");
        keys.add("Calling Number");
        keys.add("Call Duration");
        keys.add("CDR ID");

    }

    public List<String> getHeaderFields() {
        return headerFields;
    }

    public List<String> getValues() {
        return values;
    }

    public List<List<String>> getLines() {
        return lines;
    }

    public List<String> getKeys() {
        return this.keys;

    }

    public boolean readFromCSV(String filePath) {
        int lineCounter = 0;
        String[] rows;
        String row;
        headerFields = new ArrayList();
        values = new ArrayList();
        lines = new ArrayList();
        Boolean validRead = false;
        try {
            fileReader = new FileReader(filePath);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("File was not found");
        }
        bufferReader = new BufferedReader(fileReader);

        try {
            while ((row = bufferReader.readLine()) != null) {
                lineCounter++;

                rows = row.split(",");
                if (lineCounter == 1) {
                    headerFields = Arrays.asList(rows);
                } else {
                    values = Arrays.asList(rows);
                    lines.add(values);
                }

            }
            validRead = true;
            fileReader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("A problem was encountered while reading the file");
        }

        return validRead;
    }

    public boolean writeToCSV(String filePath, CDR_Struct_List cdrStructList, int serverId) {

        boolean validWrite = true;
        List<String> cdrFieldsValue = new ArrayList();
        List<String> cdrFieldsName = new ArrayList();
        List<CDR_Struct> cdrFields = (List<CDR_Struct>) cdrStructList.getValue();
        String localCSVfileName = filePath.replace(".ber", ".csv");

        CDRStructureFields cdrsf = new CDRStructureFields();
        cdrsf = DatabaseHandler.getServerCDRStructure(serverId);
        StringTokenizer stFields = new StringTokenizer(cdrsf.getCdrStruct().replaceAll("[\\{\\}\"]", ""), ",");
        while (stFields.hasMoreTokens()) {
            cdrFieldsName.add(stFields.nextToken());
        }
        //setting table headers
        File file = new File(localCSVfileName);
        boolean appendMode = false;

        try {
            if (file.isFile() && !file.isDirectory()) {
                fileWriter = new FileWriter(localCSVfileName, true);
                appendMode = true;
            } else {
                fileWriter = new FileWriter(localCSVfileName);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("File was not found");
            validWrite = false;
        }

        if (!appendMode) {

            for (String fieldName : cdrFieldsName) {

                try {
                    fileWriter.append(fieldName);
                    fileWriter.append(",");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("A problem was encountered while writing to file");
                    validWrite = false;
                }
            }
            try {
                fileWriter.append("\n");
            } catch (IOException ex) {
                Logger.getLogger(CSVFileParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (int i = 0; i < cdrFields.size(); i++) {
            if (i % cdrFieldsName.size() == 0 && i != 0) {
                try {
                    fileWriter.append("\n");
                } catch (IOException ex) {
                    Logger.getLogger(CSVFileParser.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            try {
                fileWriter.append(cdrFields.get(i).getFieldValue());
                fileWriter.append(",");

            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("A problem was encountered while writing to file");
                validWrite = false;
            }
        }
        try {

            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(CSVFileParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        return validWrite;
    }

    public boolean writeToCSV(List<List<String>> validEntries, String filteredCSVPath, String rawASNPath, int serverId, List<Integer> detectedCDRIds) {
        File filteredCSVDir = new File(filteredCSVPath + "\\Filtered");
       
        File rawASNDir = new File(rawASNPath);
        File[] files = rawASNDir.listFiles();
        CDRStructureFields cdrsf = DatabaseHandler.getServerCDRStructure(serverId);
        List<String> cdrFieldNames = new ArrayList();
        boolean validWrite = false;
        List<String> filteredFileList = new ArrayList();
        StringTokenizer StTok;
        String id="";
        StringTokenizer st = new StringTokenizer(cdrsf.getCdrStruct().replaceAll("[\\{\\}\"]", ""), ",");

        while (st.hasMoreTokens()) {
            cdrFieldNames.add(st.nextToken());
        }

        if (!filteredCSVDir.exists()) {
            filteredCSVDir.mkdirs();
        }
     
        for (File file : files) {
            StTok = new StringTokenizer(file.getName().replace(".ber", ""),"_");
            while(StTok.hasMoreTokens()){
            id = StTok.nextToken();
            }
            for(Integer detectedId : detectedCDRIds){
            if(id.equals(Integer.toString(detectedId))){
                filteredFileList.add(file.getName());
            }
            }
        }
     
        for (int i = 0; i < validEntries.size(); i++) {

            try {
                fileWriter = new FileWriter(filteredCSVPath + "\\" + "Filtered" + "\\" + filteredFileList.get(i).replace(".ber", ".csv"));
                System.out.println("File: " + files[i].getName());
                for (String fieldName : cdrFieldNames) {
                    fileWriter.append(fieldName + ",");
                }
                fileWriter.append("\n");
                for (String record : validEntries.get(i)) {
                    fileWriter.append(record);
                    fileWriter.append("\n");
                }
                fileWriter.close();
                validWrite = true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CSVFileParser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CSVFileParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return validWrite;
    }
    public boolean writeToCSV(List<String> CDREntries,String filePath){
        
        List<String> headers = new ArrayList();
        boolean validWrite=false;
        for(int i=0; i<CDREntries.size(); i++){
            if(i%2 ==0){
            headers.add(CDREntries.get(i));
            }
        }
        for (String head: headers){
        CDREntries.remove(head);
        }
        try {
                fileWriter = new FileWriter(filePath);
                
                for (String fieldName : headers) {
                    fileWriter.append(fieldName + ",");
                }
                fileWriter.append("\n");
                for (String record : CDREntries) {
                    fileWriter.append(record+",");
                    fileWriter.append("\n");
                }
                fileWriter.close();
                validWrite = true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CSVFileParser.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CSVFileParser.class.getName()).log(Level.SEVERE, null, ex);
            }
    return validWrite;
    }

}
