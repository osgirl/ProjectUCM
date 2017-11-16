package com.tcs.logutil;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class LogFileScannerApp {
	
	private static Properties config = new Properties();
	private static SimpleDateFormat searchDateFormat = new SimpleDateFormat(Constants.INPUT_SEARCH_DATE_FORMAT);
	static{
		try {
			config.load(LogFileScannerApp.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			System.out.println("File 'config.properties' is not available on classpath.");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception{
		String local_log_output_dir = config.getProperty(Constants.KEY_LOCAL_LOG_OUTPUT_DIR);
		String envType = config.getProperty(Constants.KEY_INPUT_ENV_TYPE);
		String regionType = config.getProperty(Constants.KEY_INPUT_REGION_TYPE);

		try{
			System.out.println("Clearing local log directory...");
			File dir = new File(local_log_output_dir+"/"+envType+"/"+regionType+"/");
			cleanLocalDirectory(dir);
			System.out.println("Local log directory cleared.");
		}catch(Exception e){
			System.out.println("Error occured during local log directory clean-up: "+e.getMessage());
		}

		File outputDir = new File(local_log_output_dir);
		if(!outputDir.exists()){
			outputDir.mkdir();
		}
		
		
        LogFileScannerApp frMain = new LogFileScannerApp();
		
		Date searchDate = searchDateFormat.parse(config.getProperty(Constants.KEY_INPUT_SEARCH_DATE));
		frMain.executeLinuxSearch(envType, regionType, searchDate);

	}
	private static void cleanLocalDirectory(File dir){
		if(dir.isDirectory()){
			for(File file:dir.listFiles()){
				if(file.isFile()){
					file.delete();
				}else if(file.isDirectory()){
					cleanLocalDirectory(file);
				}
			}
		}
		dir.delete();
	}
	private Date getServerDate(Session session){
		String cmd = "date +'%d/%m/%Y %H:%M'";
        Date serverDate = null;
		try{
			Channel channel = session.openChannel("exec");
            ChannelExec channelExec = (ChannelExec) channel;
			channelExec.setCommand(cmd);
			channelExec.setErrStream(System.err);
			channelExec.setInputStream(null);
			channelExec.connect();
	        InputStream in =channelExec.getInputStream();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	        String tmp;
	        if((tmp= reader.readLine()) != null) {
	        	serverDate = searchDateFormat.parse(tmp);
	        }
	        reader.close();
	        channelExec.disconnect();
		}catch(Exception e){
			System.out.println("Error in getting server date: "+e.getMessage());
		}
        return serverDate;
	}
	private List<String> fetchModifiedFileNames(Session session, Date searchDate, String logDir, String envType, String regionType, String serverType){
		List<String> fileNames = new ArrayList<String>();
		try{
			String countryCode = config.getProperty(Constants.KEY_INPUT_COUNTRY_CODE);

			String searchPatternCmd = config.getProperty(buildConfigKey(envType, regionType, serverType, Constants.KEY_SUFFIX_FIND_FILES_CMD));
			Integer modFilelogTimeBufferMins = Integer.parseInt(config.getProperty(Constants.KEY_MOD_FILES_LOG_TIME_AROUND_BUFFER_MINS));
			Date currDate = getServerDate(session);
			System.out.println("Server date: "+currDate);
			long dateTime = searchDate.getTime();
			long startLongTime = dateTime - (modFilelogTimeBufferMins*60*1000);
			long endLongTime = dateTime + (modFilelogTimeBufferMins*60*1000);
			
			long startTimeMins = (currDate.getTime()-startLongTime) / (60*1000);
			long endTimeMins = (currDate.getTime()-endLongTime) / (60*1000);
			if(endTimeMins<0){ endTimeMins = 0; }
			String cmd = searchPatternCmd.replace(Constants.KEYWORD_LOG_DIR, logDir)
						.replace(Constants.KEYWORD_TIME_1, String.valueOf(startTimeMins))
						.replace(Constants.KEYWORD_TIME_2, String.valueOf(endTimeMins))
						.replace(Constants.KEYWORD_COUNTRY_REGEX, countryCode);				
			
			
			
			Channel channel = session.openChannel("exec");
            ChannelExec channelExec = (ChannelExec) channel;
			channelExec.setCommand(cmd);
			channelExec.setErrStream(System.err);
			channelExec.setInputStream(null);
			channelExec.connect();
	        InputStream in =channelExec.getInputStream();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	        String filename;
	        int count=0;
	        while ((filename= reader.readLine()) != null) {
	        	count++;
	        	filename = filename.replace(logDir, "");
	        	fileNames.add(filename);
	        }
	        reader.close();
	        channelExec.disconnect();
			System.out.println("Executing find command to get the list of modified files: "+cmd);
			System.out.printf("Total %d files found.", count);
			System.out.println(fileNames);
		}catch(Exception e){
			System.out.println("Error in fetching log file names: "+e.getMessage());
		}
		return fileNames;
	}
	//entry point method
	public void executeLinuxSearch(String envType, String regionType, Date searchDate){
		try {
	        System.out.println("\n------------------------------------------------------------------------------------------");
			System.out.printf("Envrionment [%s, %s, %s]: \n", envType, regionType, Constants.SERVER_TYPE_WEBCACHE_1);
	        System.out.println("\n------------------------------------------------------------------------------------------");
	        Date tzSearchDate = getTimeZoneSearchDate(envType, regionType, Constants.SERVER_TYPE_WEBCACHE_1, searchDate);
			executeLinuxSearch(envType, regionType, Constants.SERVER_TYPE_WEBCACHE_1, tzSearchDate);
		} catch (Exception e) {
			System.out.printf("\nUnable to perform search: %s\n", e.getMessage());
		}
		try {
	        System.out.println("\n------------------------------------------------------------------------------------------");
			System.out.printf("Envrionment [%s, %s, %s]: \n", envType, regionType, Constants.SERVER_TYPE_WEBCACHE_2);
	        System.out.println("\n------------------------------------------------------------------------------------------");
	        Date tzSearchDate = getTimeZoneSearchDate(envType, regionType, Constants.SERVER_TYPE_WEBCACHE_2, searchDate);
			executeLinuxSearch(envType, regionType, Constants.SERVER_TYPE_WEBCACHE_2, tzSearchDate);
		} catch (Exception e) {
			System.out.printf("\nUnable to perform search: %s\n",  e.getMessage());
		}
		try {
	        System.out.println("\n------------------------------------------------------------------------------------------");
			System.out.printf("Envrionment [%s, %s, %s]: \n", envType, regionType, Constants.SERVER_TYPE_WEBSERVER_1);
	        System.out.println("\n------------------------------------------------------------------------------------------");
	        Date tzSearchDate = getTimeZoneSearchDate(envType, regionType, Constants.SERVER_TYPE_WEBSERVER_1, searchDate);
			executeLinuxSearch(envType, regionType, Constants.SERVER_TYPE_WEBSERVER_1, tzSearchDate);
		} catch (Exception e) {
			System.out.printf("\nUnable to perform search: %s\n", e.getMessage());
		}
		try {
	        System.out.println("\n------------------------------------------------------------------------------------------");
			System.out.printf("Envrionment [%s, %s, %s]: \n", envType, regionType, Constants.SERVER_TYPE_WEBSERVER_2);
	        System.out.println("\n------------------------------------------------------------------------------------------");
	        Date tzSearchDate = getTimeZoneSearchDate(envType, regionType, Constants.SERVER_TYPE_WEBSERVER_2, searchDate);
			executeLinuxSearch(envType, regionType, Constants.SERVER_TYPE_WEBSERVER_2, tzSearchDate);
		} catch (Exception e) {
			System.out.printf("\nUnable to perform search: %s\n", e.getMessage());
		}
		try {
	        System.out.println("\n------------------------------------------------------------------------------------------");
			System.out.printf("Envrionment [%s, %s, %s]: \n", envType, regionType, Constants.SERVER_TYPE_WLSERVER_1);
	        System.out.println("\n------------------------------------------------------------------------------------------");
	        Date tzSearchDate = getTimeZoneSearchDate(envType, regionType, Constants.SERVER_TYPE_WLSERVER_1, searchDate);
			executeLinuxSearch(envType, regionType, Constants.SERVER_TYPE_WLSERVER_1, tzSearchDate);
		} catch (Exception e) {
			System.out.printf("\nUnable to perform search: %s\n", e.getMessage());
		}
		try {
	        System.out.println("\n------------------------------------------------------------------------------------------");
			System.out.printf("Envrionment [%s, %s, %s]: \n", envType, regionType, Constants.SERVER_TYPE_WLSERVER_2);
	        System.out.println("\n------------------------------------------------------------------------------------------");
	        Date tzSearchDate = getTimeZoneSearchDate(envType, regionType, Constants.SERVER_TYPE_WLSERVER_2, searchDate);
			executeLinuxSearch(envType, regionType, Constants.SERVER_TYPE_WLSERVER_2, tzSearchDate);
		} catch (Exception e) {
			System.out.printf("\nUnable to perform search: %s\n", e.getMessage());
		}
	}
	private Date getTimeZoneSearchDate(String envType, String regionType, String serverType, Date searchDate){
		Date date = null;
		String key = buildConfigKey(envType, regionType, serverType, Constants.KEY_SUFFIX_TIME_ZONE_DIFF_MINS);
		try{
			System.out.println("Input search date: "+searchDateFormat.format(searchDate));
			String timeZoneDiffStr = config.getProperty(key);
			String minuteStr = timeZoneDiffStr.substring(1);
			int minutes = Integer.parseInt(minuteStr);
			if('-'==timeZoneDiffStr.charAt(0)){
				date = new Date(searchDate.getTime()-(minutes*60*1000));
			}else{
				date = new Date(searchDate.getTime()+(minutes*60*1000));
			}
			System.out.println("Search date after timezone difference: "+searchDateFormat.format(date));
		}catch(Exception e){
			date = searchDate;
			System.out.printf("\nInvalid value for key:[%s]: %s\n", key, e.getMessage());
		}
		return date;
	}
	private void executeLinuxSearch(String envType, String regionType, String serverType, Date searchDate)throws Exception{
		JSch jsch = new JSch();
        Session session = null;
        try {
        	String remoteHost = config.getProperty(buildConfigKey(envType, regionType, serverType, Constants.KEY_SUFFIX_REMOTE_HOST));
        	String remoteUser = config.getProperty(buildConfigKey(envType, regionType, serverType, Constants.KEY_SUFFIX_REMOTE_USER));
        	String remotePassword = config.getProperty(buildConfigKey(envType, regionType, serverType, Constants.KEY_SUFFIX_REMOTE_PASSWORD));

        	String remoteLogDir = config.getProperty(buildConfigKey(envType, regionType, serverType, Constants.KEY_SUFFIX_REMOTE_LOG_DIR));
        	
        	String localLogOutputDir = config.getProperty(Constants.KEY_LOCAL_LOG_OUTPUT_DIR);
        	System.out.println("Connecting to host... "+remoteHost);
            session = jsch.getSession(remoteUser, remoteHost, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(remotePassword);
            session.connect(); 
            
            System.out.println("Connected.....");
            //fetching the file names eligible for scanning
	    		List<String> fileNames = fetchModifiedFileNames(session, searchDate, remoteLogDir, envType, regionType, serverType);
	    		String[] logDateFormats = getLogDateFormatsArray(envType, regionType, serverType); 
	    		//below map holds the mapping of date patterns and files to be searched using this pattern.
	    		Map<String,String> dateFmt_logFile_map = new HashMap<String, String>();
	    		for(String logDateFormat : logDateFormats){
	   				String[] formatPair = logDateFormat.split("=");
	   				String logFilePattern = formatPair[0];
	   				String dateFormat = formatPair[1];
	   				String mapFile = dateFmt_logFile_map.get(dateFormat);
	   				if(mapFile==null){ mapFile = ""; }
	   				//iterating through the list of modified files
	   	    		for(int idx=0;idx<fileNames.size();idx++){
	   	    			String fileName = fileNames.get(idx);
	   	    			if(fileName.matches(logFilePattern)){
	   	    				if(mapFile!=null && mapFile.length()>0){
	   	    					mapFile += " ";//for multi file search with grep
	   	    				}
	   	    				mapFile += remoteLogDir+fileName;
	   	    			}
	   	    		}
	   	    		dateFmt_logFile_map.put(dateFormat, mapFile);
	    		}
	    		Iterator<String> mapKeyIter = dateFmt_logFile_map.keySet().iterator();
	    		Map<String, List<String>> final_dateFmt_logFile_map = new HashMap<String, List<String>>();//it will be used with sed command for actual search
	    		while(mapKeyIter.hasNext()){
		            String fileNameGrepCmd = "grep -l ";
	    			String _key = mapKeyIter.next();
	    			String dateFmt = _key;
	    			String _value = dateFmt_logFile_map.get(_key);
	    			if(_value!=null && _value.length()>0){
	    				String datePartFmt = getDatePartFromDateTimeFormat(dateFmt);
	    				String searchDateStr = new SimpleDateFormat(datePartFmt).format(searchDate);
	    				fileNameGrepCmd += "'"+searchDateStr+"' "+dateFmt_logFile_map.get(_key);
	    				System.out.println("Executing grep command: "+fileNameGrepCmd);
	    				List<String> files = executeGrepSearchCmd(session, fileNameGrepCmd);
	    				final_dateFmt_logFile_map.put(_key, files);
	    			}
	    		}
	
            //END fetching the file names eligible for scanning
            
//            String command = "sed -n '/05-May-2015 07:[42,43,44,45,46,47]/{:a;N;/^\\n/s/^\\n//;/05-May-2015 08:[16,17,18,19,20,21]/{p;s/.*//;};ba};' ";
			Iterator<String> finalLogFileMapIter = final_dateFmt_logFile_map.keySet().iterator();
			while(finalLogFileMapIter.hasNext()){
				String datePattern = finalLogFileMapIter.next();
				
				
//				String startDateStr = new SimpleDateFormat(datePattern).format(startCalendar.getTime());
//				String endDateStr = new SimpleDateFormat(datePattern).format(endCalendar.getTime());
				
				List<String> files = final_dateFmt_logFile_map.get(datePattern);
				for(String fileName : files){
					String command = buildSedSearchCommand(datePattern, fileName, searchDate);
					System.out.println("Executing awk command: "+command);
		            Channel channel = session.openChannel("exec");
		            ChannelExec execChannel = (ChannelExec) channel;
		            execChannel.setCommand(command);
		            execChannel.setErrStream(System.err);
		            execChannel.setInputStream(null);
		            execChannel.connect();
		            InputStream in =execChannel.getInputStream();
		            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		            String outputFileDir = localLogOutputDir+"/"+envType+"/"+regionType+"/"+serverType+"/";
		            File outputFile = new File(outputFileDir);
		            if(!outputFile.exists()){
		            	outputFile.mkdirs();
		            }
		            File outputFilePath = new File(outputFileDir+(fileName.replace(remoteLogDir, "")));
		            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath));
		            String tmp;
		            System.out.print("Copying log....");
		            int count=0;
		            while ((tmp= reader.readLine()) != null) {
		            	count++;
		            	writer.write(tmp+"\n");
		            }
		            writer.close();
		            reader.close();
		            if(count==0){
		            	outputFilePath.delete();
		            	System.out.println("No matching lines found.");
		            }else{
			            System.out.println(count+" lines copied.");
		            }
		            execChannel.disconnect();
				}
			}
            
        }finally{
        	if(session!=null)
        		session.disconnect();
        }
	}

	private String buildSedSearchCommand(String datePattern, String fileName, Date searchDate){
		long dateTime = searchDate.getTime();
		Integer logsLogTimeBufferMins = Integer.parseInt(config.getProperty(Constants.KEY_LOGS_LOG_TIME_AROUND_BUFFER_MINS));
		long startLongTime = dateTime - (logsLogTimeBufferMins*60*1000);
		long endLongTime = dateTime + (logsLogTimeBufferMins*60*1000);
	
		String command = "awk '/";
		
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		int minute = 60*1000;
		long currTime=startLongTime;
		for(;currTime<=dateTime;currTime+=minute){
			Date date = new Date(currTime);
			if(currTime>startLongTime){
				command += "|";
			}
			command += sdf.format(date).replaceAll("/", ".");
		}
		command += "/{flag=1}/";
		for(currTime=endLongTime;currTime>=dateTime;currTime-=minute){
			Date date = new Date(currTime);
			if(currTime<endLongTime){
				command += "|";
			}
			command += sdf.format(date).replaceAll("/", ".");
		}
		command += "/{print;flag=0}flag' "+fileName;
		return command;
	}
	private List<String> executeGrepSearchCmd(Session session, String cmd){
		List<String> fileNames = new ArrayList<String>();
		try{
			Channel channel = session.openChannel("exec");
            ChannelExec channelExec = (ChannelExec) channel;
			channelExec.setCommand(cmd);
			channelExec.setErrStream(System.err);
			channelExec.setInputStream(null);
			channelExec.connect();
	        InputStream in =channelExec.getInputStream();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	        String filename;
	        int count=0;
	        while ((filename= reader.readLine()) != null) {
	        	count++;
	        	fileNames.add(filename);
	        }
	        reader.close();
	        channelExec.disconnect();
			System.out.printf("Total %d files found.", count);
			System.out.println(fileNames);
		}catch(Exception e){
			System.out.println("Error in fetching log file names: "+e.getMessage());
		}
		return fileNames;
	}
	private String getDatePartFromDateTimeFormat(String dateTimeFormat){
		String datePart = "";
		int index = dateTimeFormat.indexOf("'T'");
		if(index==-1){
			index = dateTimeFormat.indexOf(" HH:");
		}
		if(index==-1){
			index = dateTimeFormat.indexOf(":HH");
		}
		if(index!=-1){
			datePart = dateTimeFormat.substring(0, index);
		}
		return datePart;
	}
	public String buildConfigKey(String envType, String regionType, String serverType, String keyName){
		StringBuilder keyBuilder = new StringBuilder().append(envType).append('.').append(regionType)
				.append('.').append(serverType).append('.').append(keyName);
		return keyBuilder.toString();
	}
	private String[] getLogDateFormatsArray(String envType, String regionType, String serverType){
		String key = buildConfigKey(envType, regionType, serverType, Constants.KEY_SUFFIX_DATE_PATTERN);
		String[] dateFormats = config.getProperty(key).split("#");
		return dateFormats;
	}
}
