package com.bonitasoft.utils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by pablo on 16/06/2017.
 */
public class LoggerReader {
    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.FRANCE);
    private Map<String, Map<String,Serializable>> tasks = new HashMap<String, Map<String, Serializable>>();
    private Map<String, Map<String,Serializable>> connectors = new HashMap<String, Map<String, Serializable>>();
    private Map<Long, Long> cases = new HashMap<Long, Long>();

    private static Map<Long, List<String>> caseLines = new HashMap<Long, List<String>>();
    //CONNECTORS
    private Map<String, Map<String,Serializable>> avgTimeByConnType = new HashMap<String, Map<String, Serializable>>();
    private Map<String, Map<String,Serializable>> avgTimeByTaskName = new HashMap<String, Map<String, Serializable>>();
    private Map<String, Map<String,Serializable>> avgTimeByConnector = new HashMap<String, Map<String, Serializable>>();

    //TASKS
    private Map<String, Map<String,Serializable>> avgTaskTime = new HashMap<String, Map<String, Serializable>>();

    //CASES
    private Map<String, Map<String,Serializable>> avgCaseTime = new HashMap<String, Map<String, Serializable>>();

    private String path;

    private LoggerReader(String path){
        super();
        this.path = path;
    }
    public static void main(String[] args) throws Exception {

        //String path = "C:\\BonitaBPM\\SERVERS\\BonitaBPMSubscription-6.5.3-Tomcat-7.0.55\\logs\\tracker.2017-06-19.log";


          String path = args[0];
        if(path!= null && path.length()>0) {
            LoggerReader l = new LoggerReader(path);
            l.analizeLogs();
        }else{
            System.out.println("You need specify the path as parameter.");
        }
        boolean exit = false;
        Scanner in = new Scanner(System.in);
        while(!exit) {
            System.out.println("");
/*
            System.out.println("Do you want to exit? (y or n)");

            String s = in.nextLine();

             */
            System.out.println("Introduce the case number: ");

            long i = in.nextLong();
            printCaseInfo(i);

        }
    }

    private static void printCaseInfo(long i) {
        List<String> lines = caseLines.get(i);
        for( String caseLine : lines  ) {
            System.out.println(caseLine);
        }
    }

    public void analizeLogs() throws Exception{


            BufferedReader br = new BufferedReader(new FileReader(path));
            try {
                String line = br.readLine();
                while (line != null) {
                    if(line.contains("TASK TRACKING")){
                        readTaskTracking(line);
                    }else{
                        if(line.contains("CONNECTOR TRACKING")) {
                            readConnectorTracking(line);
                        }else{
                            if(line.contains("PROCESS TRACKING")){
                                readProcessTracking(line);
                            }
                        }
                    }
                    line = br.readLine();
                }

            } finally {
                br.close();
            }

        printPrcessStats();

        generateConnectorStats();
        printConnectorStats();

        generateTasksStats();
        printTasksStats();


    }

    private void printPrcessStats() {
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("----------------                   PROCESS REPORT                 -----------------");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("----------------               AVERAGE TIME OF CASES (s)          -----------------");
        System.out.println("-----------------------------------------------------------------------------------");
        int numCases = cases.size();
        long sumTime = 0L;
        long maxCaseId = 0L;
        long max = 0L;
        long min = 99999999999L;
        for(Map.Entry<Long,Long> e : cases.entrySet()){
            long v = e.getValue();
            if(v > max){
                max = v;
                maxCaseId = e.getKey();
            }
            if(v < min){
                min = v;
            }
            sumTime += v;
        }
        Long avg = sumTime / numCases;
        System.out.println("CASES: | avg(s): "+ avg +" | min(s): " + min+" | max(s): "+ max+" | count: "+ numCases + " | longestTaskCaseId: " + maxCaseId);

    }


    private void readProcessTracking(String line) {

        int pos = line.indexOf("PROCESS TRACKING - END - MainProcess:") + "PROCESS TRACKING - END - MainProcess:".length();
        StringTokenizer st = new StringTokenizer(line.substring(pos), " - ");
        Long caseId = new Long(0);
        boolean exit = false;
        if(line.contains("PROCESS TRACKING - END - MainProcess:")) {
            while (st.hasMoreElements() && !exit) {
                String s = st.nextToken();
                switch (s) {
                    case "caseId:":
                        caseId = new Long(st.nextToken());
                        exit = true;
                        break;
                }
            }

            pos = line.indexOf("- took: 0 ") + "- took: 0 ".length();
            String time = line.substring(pos).trim();

            st = new StringTokenizer(time, ":");

            Long timeLong = (new Long(st.nextToken() )* 60 * 60 )+ (new Long(st.nextToken()) * 60) + (new Long(st.nextToken()));
            cases.put(caseId, timeLong);
        }
    }

    private void printTasksStats() {

        Map<String,Serializable> m;
        /*
        avgTimeByConnType
        avgTimeByTaskName
        avgTimeByConnector

        */
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("----------------                 TASK REPORT                 -----------------");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("----------------          AVERAGE TIME BY TASK NAME          -----------------");
        System.out.println("-----------------------------------------------------------------------------------");

        for(String key:avgTaskTime.keySet()){
            m = avgTaskTime.get(key);
            Long avg = ((Long)m.get("sum")) / ((Integer)m.get("count"));
            System.out.println("Task Name:" +key + " | avg(Ms): "+ avg +" | min(Ms): " + m.get("min")+" | max(Ms): "+ m.get("max")+" | count: "+ m.get("count") + " | longestTaskCaseId: " + m.get("maxId"));
        }
        System.out.println();
        System.out.println();
    }

    private void generateTasksStats() {
        final Map<String,Serializable> example = new HashMap<String,Serializable>();
        example.put("sum",0L);
        example.put("count",0);
        example.put("min",1000000L);
        example.put("max",0L);
        example.put("maxId",0L);
        //System.out.println(connectors);
        for(Map<String,Serializable> task :tasks.values()){
            Map<String,Serializable> byTaskName = avgTaskTime.get(task.get("taskName"));
            if(byTaskName == null){
                byTaskName = new HashMap<String,Serializable>();
                byTaskName.putAll(example);
            }

            Long timeConsumed = (Long)task.get("timeConsumedMs");
            if( timeConsumed == null){
                continue;
            }
            byTaskName.put("sum",(Long)byTaskName.get("sum")+timeConsumed);
            byTaskName.put("count",(Integer)byTaskName.get("count")+1);
            if((Long)byTaskName.get("min") > timeConsumed ){
                byTaskName.put("min",timeConsumed);
            }
            if((Long)byTaskName.get("max") < timeConsumed ){
                byTaskName.put("max",timeConsumed);
                byTaskName.put("maxId",task.get("caseId"));
            }


            avgTaskTime.put((String)task.get("taskName"),byTaskName);
        }
    }

    private void printConnectorStats() {
        Map<String,Serializable> m;
        /*
        avgTimeByConnType
        avgTimeByTaskName
        avgTimeByConnector

        */
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("----------------                 CONNECTOR REPORT                 -----------------");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("----------------          AVERAGE TIME BY CONNECTOR TYPE          -----------------");
        System.out.println("-----------------------------------------------------------------------------------");

        for(String key:avgTimeByConnType.keySet()){
            m = avgTimeByConnType.get(key);
            Long avg = ((Long)m.get("sum")) / ((Integer)m.get("count"));
            System.out.println("Connector Type:" +key + " | avg(Ms): "+ avg +" | min(Ms): " + m.get("min")+" | max(Ms): "+ m.get("max")+" | count: "+ m.get("count") + " | longestConnectorCaseId: " + m.get("maxId"));
        }
        System.out.println();
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("----------------           AVERAGE TIME BY TASK NAME              -----------------");
        System.out.println("-----------------------------------------------------------------------------------");

        for(String key:avgTimeByTaskName.keySet()){
            m = avgTimeByTaskName.get(key);
            Long avg = ((Long)m.get("sum")) / ((Integer)m.get("count"));
            System.out.println("Task name:" +key + " | avg(Ms): "+ avg +" | min(Ms): " + m.get("min")+" | max(Ms): "+ m.get("max")+" | count: "+ m.get("count") + " | longestConnectorCaseId: " + m.get("maxId"));
        }

        System.out.println();
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("----------------         AVERAGE TIME BY CONNECTOR NAME           -----------------");
        System.out.println("-----------------------------------------------------------------------------------");

        for(String key:avgTimeByConnector.keySet()){
            m = avgTimeByConnector.get(key);
            Long avg = ((Long)m.get("sum")) / ((Integer)m.get("count"));
            System.out.println("Connector name:" +key + " | avg(Ms): "+ avg +" | min(Ms): " + m.get("min")+" | max(Ms): "+ m.get("max")+" | count: "+ m.get("count") + " | longestConnectorCaseId: " + m.get("maxId"));
        }
    }

    private void generateConnectorStats() {
        final Map<String,Serializable> example = new HashMap<String,Serializable>();
        example.put("sum",0L);
        example.put("count",0);
        example.put("min",1000000L);
        example.put("max",0L);
        example.put("maxId",0L);
        //System.out.println(connectors);
        for(Map<String,Serializable> connector :connectors.values()){
            Map<String,Serializable> byCType = avgTimeByConnType.get(connector.get("type"));
            if(byCType == null){
                byCType = new HashMap<String,Serializable>();
                byCType.putAll(example);
            }
            Map<String,Serializable> byTName = avgTimeByTaskName.get(connector.get("taskName"));
            if(byTName == null){
                byTName = new HashMap<String,Serializable>();
                byTName.putAll(example);
            }
            Map<String,Serializable> byC = avgTimeByConnector.get(connector.get("connector"));
            if(byC == null){
                byC = new HashMap<String,Serializable>();
                byC.putAll(example);
            }
            /*
            Map<String,Serializable> byCId = avgTimeByConnId.get(connector.get("connectorId"));
            if(byCId == null){
                byCId = new HashMap<String,Serializable>();
                byCId.putAll(example);
            }
            */
            Long timeConsumed = (Long)connector.get("timeConsumedMs");
            if( timeConsumed == null){
                continue;
            }
            byCType.put("sum",(Long)byCType.get("sum")+timeConsumed);
            byCType.put("count",(Integer)byCType.get("count")+1);
            if((Long)byCType.get("min") > timeConsumed ){
                byCType.put("min",timeConsumed);
            }
            if((Long)byCType.get("max") < timeConsumed ){
                byCType.put("max",timeConsumed);
                byCType.put("maxId",connector.get("caseId"));
            }



            byTName.put("sum",(Long)byTName.get("sum")+timeConsumed);
            byTName.put("count",(Integer)byTName.get("count")+1);
            if((Long)byTName.get("min") > timeConsumed ){
                byTName.put("min",timeConsumed);
            }
            if((Long)byTName.get("max") < timeConsumed ){
                byTName.put("max",timeConsumed);
                byTName.put("maxId",connector.get("caseId"));
            }



            byC.put("sum",(Long)byC.get("sum")+timeConsumed);
            byC.put("count",(Integer)byC.get("count")+1);
            if((Long)byC.get("min") > timeConsumed ){
                byC.put("min",timeConsumed);
            }
            if((Long)byC.get("max") < timeConsumed ){
                byC.put("max",timeConsumed);
                byC.put("maxId",connector.get("caseId"));
            }
/*
            byCId.put("sum",new Integer((String)byCId.get("sum"))+timeConsumed);
            byCId.put("count",new Integer((String)byCId.get("count"))+1);
            if(new Integer((String)byCId.get("min")) > timeConsumed ){
                byCId.put("min",timeConsumed);
            }
            if(new Integer((String)byCId.get("max")) < timeConsumed ){
                byCId.put("max",timeConsumed);
            }
*/

            avgTimeByConnType.put((String)connector.get("type"),byCType);
            avgTimeByTaskName.put((String)connector.get("taskName"),byTName);
            avgTimeByConnector.put((String)connector.get("connector"),byC);
        }

    }

    private void readTaskTracking(String line) throws Exception {
        Map<String, Serializable> task = new HashMap<String, Serializable>();
        boolean omit = false;
        try {

            String data;
            int pos = line.indexOf("-  caseId:") + 3;
            boolean noExit = true;
            String taskId = "";
            StringTokenizer st = new StringTokenizer(line.substring(pos), " - ");
            while (st.hasMoreElements() && noExit) {
                String s = st.nextToken();
                switch (s) {
                    case "caseId:":
                        task.put("caseId", st.nextToken());
                        break;
                    case "taskId:":
                        taskId = st.nextToken();
                        task.put("taskId", taskId);
                        noExit = false;
                        break;
                }
            }
            if (line.contains("TASK TRACKING - START -")) {
                pos = line.indexOf("- taskName:") + "-  taskName:".length();
                int pos2 = line.indexOf("- date:");
                task.put("taskName", line.substring(pos, pos2).trim());
                task.put("dateStart", line.substring(pos2 + "- date:".length()));

            } else {
                if (line.contains("TASK TRACKING - END -")) {
                    pos = line.indexOf("- date:");
                    int pos2 = line.indexOf("- Current State:");
                    task = tasks.get(taskId);
                    if(task == null || task.get("dateStart") == null){
//                        System.out.println("Start Task Event with id "+taskId +" is not present on this file");
                        omit = true;
                    }else {
                        task.put("dateEnd", line.substring(pos + "- date:".length(), pos2).trim());
                        Long diff = format.parse((String) task.get("dateEnd")).getTime() - format.parse((String) task.get("dateStart")).getTime();
                        task.put("timeConsumed", getDateFromMsec(diff));
                        task.put("timeConsumedMs", diff);


                        List<String> caseLine = caseLines.get(new Long((String)task.get("caseId")));
                        if(caseLine == null){
                            caseLine = new ArrayList<String>();
                        }
                        caseLine.add(taskToString(task));
                        caseLines.put(new Long((String)task.get("caseId")), caseLine);
                    }
                }else{
                    omit = true;
                }

            }
            if(!omit) {
                tasks.put(taskId, task);

            }
            omit = false;
        }catch(Exception e){
            System.out.println(line);
            System.out.println(task);
            e.printStackTrace();
            throw new Exception(e);
        }

    }

    private void readConnectorTracking(String line) throws Exception {

        Map<String,Serializable> connector = new HashMap<String,Serializable>();
        boolean omit = false;
        try {
            String data;
            int pos = line.indexOf("-  caseId:") + 3;
            boolean noExit = true;
            String taskId = "";
            String connectorId = "";
            StringTokenizer st = new StringTokenizer(line.substring(pos), " - ");
            while (st.hasMoreElements() && noExit) {
                String s = st.nextToken();
                switch (s) {
                    case "caseId:":
                        connector.put("caseId", st.nextToken());
                        break;
                    case "taskId:":
                        taskId = st.nextToken();
                        connector.put("taskId", taskId);
                        break;
                    case "connectorId:":
                        connectorId = st.nextToken();
                        connector.put("connectorId", connectorId);
                        break;
                    case "connector:":
                        connector.put("connector", st.nextToken());
                        break;
                    case "type:":
                        connector.put("type", st.nextToken());
                        noExit = false;
                        break;
                }
            }
            if (line.contains("CONNECTOR TRACKING - EXECUTING -")) {
                pos = line.indexOf("- taskName:") + "-  taskName:".length();
                int pos2 = line.indexOf("- connectorId:");
                connector.put("taskName", line.substring(pos, pos2).trim());
                pos2 = line.indexOf("- date:");
                connector.put("dateStart", line.substring(pos2 + "- date:".length()));

            } else {
                pos = line.indexOf("- date:");
                connector = connectors.get(connectorId);
                if(connector == null){
                       //System.out.println("Start Connector Event with id "+connectorId +" is not present on this file");
                    omit = true;
                }else {
                    connector.put("dateEnd", line.substring(pos + "- date:".length()).trim());
                    Long diff = format.parse((String) connector.get("dateEnd")).getTime() - format.parse((String) connector.get("dateStart")).getTime();
                    connector.put("timeConsumed", getDateFromMsec(diff));
                    connector.put("timeConsumedMs", diff);

                    List<String> caseLine = caseLines.get(new Long((String)connector.get("caseId")));
                    if(caseLine == null){
                        caseLine = new ArrayList<String>();
                    }
                    caseLine.add(connectorToString(connector));
                    caseLines.put(new Long((String)connector.get("caseId")), caseLine);

                }

            }
            if(!omit) {
                connectors.put(connectorId, connector);
            }
            omit = false;
        }catch (Exception e){
            System.out.println("ERROR");
            //System.out.println(connector);
            System.out.println(line);
            e.printStackTrace();
            throw new Exception(e);
        }
    }
    // to convert Milliseconds into DD HH:MM:SS format.
    private String getDateFromMsec(long diffMSec) {
        int left = 0;
        int ss = 0;
        int mm = 0;
        int hh = 0;
        int dd = 0;
        left = (int) (diffMSec / 1000);
        ss = left % 60;
        left = (int) left / 60;
        if (left > 0) {
            mm = left % 60;
            left = (int) left / 60;
            if (left > 0) {
                hh = left % 24;
                left = (int) left / 24;
                if (left > 0) {
                    dd = left;
                }
            }
        }
        String diff = Integer.toString(dd) + " " + Integer.toString(hh) + ":"
                + Integer.toString(mm) + ":" + Integer.toString(ss);
        return diff;

    }

    private String connectorToString(Map<String, Serializable> connector){
        String s = "CONNECTOR "+connector.get("connector") + " - " + connector.get("type") + " took: " + connector.get("timeConsumedMs");
        return s;
    }
    private String taskToString(Map<String, Serializable> task){
        String s = "TASK "+task.get("taskName") + " " + " took: " + task.get("timeConsumedMs");
        return s;
    }
}
