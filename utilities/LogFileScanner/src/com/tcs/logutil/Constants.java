package com.tcs.logutil;

public interface Constants {
	String KEY_LOCAL_LOG_OUTPUT_DIR = "localLog.outputDir";
	String KEY_MOD_FILES_LOG_TIME_AROUND_BUFFER_MINS = "ModifiedFiles.logTimeAroundBufferMinutes";
	String KEY_LOGS_LOG_TIME_AROUND_BUFFER_MINS = "logs.logTimeAroundBufferMinutes";
	
	String KEY_INPUT_SEARCH_DATE = "inputSearchDate";
	String KEY_INPUT_ENV_TYPE = "inputSearchEnvType";
	String KEY_INPUT_REGION_TYPE = "inputSearchRegionType";
	String KEY_INPUT_COUNTRY_CODE = "inputSearchCountryCode";

	String INPUT_SEARCH_DATE_FORMAT = "dd/MM/yyyy HH:mm";
	
	String KEY_SUFFIX_REMOTE_HOST = "host";
	String KEY_SUFFIX_REMOTE_USER = "remoteUser";
	String KEY_SUFFIX_REMOTE_PASSWORD = "remotePassword";
	String KEY_SUFFIX_REMOTE_LOG_DIR = "logDir";
	String KEY_SUFFIX_DATE_PATTERN = "logDatePatterns";
	String KEY_SUFFIX_FIND_FILES_CMD = "findFilesCommand";
	String KEY_SUFFIX_TIME_ZONE_DIFF_MINS = "timeZoneDiffMinutes";
	
	String KEYWORD_LOG_DIR = "#log_dir#";
	String KEYWORD_TIME_1 = "#time1#";
	String KEYWORD_TIME_2 = "#time2#";
	String KEYWORD_COUNTRY_REGEX = "#country#";

	String ENV_TYPE_OAT = "OAT";
	String ENV_TYPE_PROD = "PROD";
	String ENV_TYPE_DEV = "DEV";
	
	String REGION_TYPE_UK = "uk";
	String REGION_TYPE_APAC = "apac";
	
	String SERVER_TYPE_WEBCACHE_1 = "webcache.1";
	String SERVER_TYPE_WEBCACHE_2 = "webcache.2";
	String SERVER_TYPE_WEBSERVER_1 = "webserver.1";
	String SERVER_TYPE_WEBSERVER_2 = "webserver.2";
	String SERVER_TYPE_WLSERVER_1 = "wlserver.1";
	String SERVER_TYPE_WLSERVER_2 = "wlserver.2";
	
}
