/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdrgeneration;

import Database.DatabaseHandler;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Al Badr
 */
public class CDRStructureGenerator {

    public static final String BUILTIN_PHONENUM_TYPE = "PhoneNumber";
    public static final String BUILTIN_DURATION_TYPE = "Duration";
    public static final String BUILTIN_ID_TYPE = "Integer";
    public static final String BUILTIN_TIMESTAMP_TYPE = "TimeStamp";
    public static final String BUILTIN_TYPE_STATE = "State";
    public static final int CDR_LENGTH = 10;
    public static final String BUILTIN_CDREntryNo_TYPE = "CDR_Entry_No";
    private static int CDR_ID = 0;
    private final static String[] types = {"VT", "VO"};
    private static final String[] calledNoTypes = {"toll_free", "regular"};
    private static List<String> tollFreeNumbers=DatabaseHandler.getTollFreeNumbers();

    public CDRStructureGenerator() {
        

    }

    public String[] getCalledNoTypes() {
        return calledNoTypes;
    }



    public List<String> getTollFreeNumbers() {
        return tollFreeNumbers;
    }

    public void setTollFreeNumbers(List<String> tollFreeNumbers) {
        this.tollFreeNumbers = tollFreeNumbers;
    }

    public static int getCDR_ID() {
        return CDR_ID;
    }

    public static void setCDR_ID(int CDR_ID) {
        CDRStructureGenerator.CDR_ID = CDR_ID;
    }

    public static String generatePhoneNumber() {
        Random randCalledType = new Random();
        String generatedType="";
        

           
        Random rand = new Random();
        int digit = 0;
        String phoneNumber = "011";
        for (int i = 0; i < 9; i++) {
            digit = rand.nextInt(10);
            phoneNumber += Integer.toString(digit);
        
    }
        return phoneNumber;
    }
    public static String generateTollFreeNumber(){
         
        return tollFreeNumbers.get(new Random().nextInt(tollFreeNumbers.size()));
       
    }

    public static String generateDuration() {

        Random rand_mins = new Random();
        Random rand_secs = new Random();

        int minutes = 0;
        int seconds = 0;
        String duration = "";
        for (int i = 0; i < 5; i++) {
            if (i == 1) {
                duration += ":";
            } else if (i == 0) {
                minutes = rand_mins.nextInt(60);
                duration += Integer.toString(minutes);
            } else if (i == 2) {

                seconds = rand_secs.nextInt(60);
                duration += Integer.toString(seconds);

            }

        }
        
        return duration;
    }

    public static String generateTimeStamp() {
        LocalDateTime ldt = LocalDateTime.now();
        return ldt.toString();
    }

    public static String generateState(boolean type) {
        if(type){
        return types[1];
        }
        return types[new Random().nextInt(types.length)];
    }

    public static Integer generateCDRID() {
        int id = 0;
        id = DatabaseHandler.getLastInsertedId();
        id++;
        return id;
    }

}
