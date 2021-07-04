/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FileParser;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import Database.DatabaseHandler;

import Models.CDRStructureFields;
import com.iti.structure.CDR_Struct;
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
