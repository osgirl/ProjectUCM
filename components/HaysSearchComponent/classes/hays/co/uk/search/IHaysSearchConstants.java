package hays.co.uk.search;

import intradoc.shared.SharedObjects;

public interface IHaysSearchConstants {
	static final String PAYMENT_TYPES = "HDWMA";
	
	static final String PERMANENT = "P";
	static final String CONTRACT = "C";
	static final String TEMPORARY = "T";
	
	static final double KM_MILES_CONVERT = 0.621371;
	static final String DEFAULT = "0";
	static final String DEFAULT_RADIUS = SharedObjects.getEnvironmentValue("DefaultRadius");
	static final String DEFAULT_DISTANCE_UNIT = SharedObjects.getEnvironmentValue("DefaultDistanceUnit");
	static final String DISTANCE_UNITS = SharedObjects.getEnvironmentValue("DistanceUnits");
	static final boolean IS_THEASAURUS = SharedObjects.getEnvValueAsBoolean("isThesaurus", false);
	
	// parameters
	public static final String JOB_TITLE = "job_title";
	public static final String JOB_TITLE_FULL = "job_job_title";
	public static final String JOB_TITLE_FULL_DECR = "job_job_title_descr";
	public static final String JOB_TITLE_REF = "job_title_ref";
	public static final String JOB_INDUSTRY = "job_industry";
	public static final String JOB_KEYWORDS = "job_keywords";
	public static final String JOB_PERM = "job_permanent";
	public static final String JOB_CONTRACT = "job_contract";
	public static final String JOB_TEMP = "job_temporary";
	public static final String JOB_MIN_PERM = "job_min_permanent";
	public static final String JOB_MAX_PERM = "job_max_permanent";
	public static final String JOB_MIN_TEMP = "job_min_temporary";
	public static final String JOB_MAX_TEMP = "job_max_temporary";
	public static final String JOB_MIN_CONTRACT = "job_min_contract";
	public static final String JOB_MAX_CONTRACT = "job_max_contract";
	public static final String JOB_SELECT_CONTRACT = "job_select_contract";
	public static final String JOB_SELECT_TEMP = "job_select_temporary";
	public static final String JOB_SELECT_PERM = "job_select_permanent";
	public static final String JOB_CATEGORY = "job_category";
	public static final String LOCALE = "SiteLocale";
	public static final String micrositeCode = "MicrositeCode";
	
	public static final String JOB_RATE = "job_rate";
	public static final String JOB_MIN_SALARY = "job_min";
	public static final String JOB_MAX_SALARY = "job_max";
	public static final String LOCATION_ID = "location_id";
	
	// filter params
	public static final String JOB_TITLE_FILTER = "job_job_title_filter";;
	public static final String JOB_INDUSTRY_FILTER = "job_industry_filter";
	public static final String JOB_KEYWORDS_FILTER = "job_keywords_filter";
	public static final String JOB_TYPE_FILTER = "job_type_filter";
	public static final String JOB_PERM_FILTER = "job_permanent_filter";
	public static final String JOB_CONTRACT_FILTER = "job_contract_filter";
	public static final String JOB_TEMP_FILTER = "job_temporary_filter";
	public static final String JOB_MIN_PERM_FILTER = "job_min_permanent_filter";
	public static final String JOB_MAX_PERM_FILTER = "job_max_permanent_filter";
	public static final String JOB_MIN_TEMP_FILTER = "job_min_temporary_filter";
	public static final String JOB_MAX_TEMP_FILTER = "job_max_temporary_filter";
	public static final String JOB_MIN_CONTRACT_FILTER = "job_min_contract_filter";
	public static final String JOB_MAX_CONTRACT_FILTER = "job_max_contract_filter";
	public static final String JOB_SELECT_CONTRACT_FILTER = "job_select_contract_filter";
	public static final String JOB_SELECT_TEMP_FILTER = "job_select_temporary_filter";
	public static final String JOB_EXPERTISE_FILTER = "job_expertise_filter";
	public static final String JOB_CATEGORY_FILTER = "job_category_filter";
	//added for POSTED DATE
	public static final String JOB_POSTED_DATE_FILTER = "job_posteddate_filter";
	public static final String JOB_POSTED_DATE_EXCLUDE = "job_posteddate_exclude";
	//end for Posted Date
	
	
	public static final String CONTENT_TYPE = "contentType";
	public static final String SUB_CONTENT_TYPE = "subContentType";
	public static final String JOB_INTERN = "job_international";
	public static final String JOB_NONNATIONL = "job_nonEU";
	public static final String JOB_SPONSORED = "job_sponsored";
	public static final String RELEASE_DATE = "release_date";
	
	public static final String JOB_LOCATION = "job_location";
	public static final String NE_LONGITUDE = "ne_longitude";
	public static final String NE_LATITUDE = "ne_latitude";
	public static final String SW_LONGITUDE = "sw_longitude";
	public static final String SW_LATITUDE = "sw_latitude";
	public static final String RADIUS = "radius";
	public static final String LEVEL = "level";
	public static final String SORTFIELD = "SortField";
	public static final String SORTORDER = "SortOrder";
	public static final String NE_LONGITUDE_FILTER = "ne_longitude_filter";
	public static final String NE_LATITUDE_FILTER = "ne_latitude_filter";
	public static final String RADIUS_FILTER = "radius_filter";
	public static final String LEVEL_FILTER = "level_filter";
	public static final String DISTANCE_UNIT = "distance_unit";
	
	public static final String EXCLUDE = "exclude";
	
	//POC Constants
	public static final String IS_FUZZY_SEARCH = "isFuzzy";
	public static final String IS_ONLY_JOB_TITLE = "isOnlyJobTitle";
	//POC Constants
	public static final String specialCharacters = "[\\]\\[_\\\\{}\\^\\$\\,\\.\\|\\?\\*\\+\\(\\)/%\\- #]+";
	public static final String fuzzySpecialCharacters = "[\\]\\[_\\\\{}\\^\\$\\.\\|\\?\\*\\+\\(\\)/%\\- #]+";
	public static final String specialCharactersForKeywords = "[\\]\\[_\\\\{}\\^\\$\\.\\|\\?\\*\\+\\(\\)/%\\- #]+";
	public static final String reservedKeyWordsForOracleTextSearch = SharedObjects.getEnvironmentValue("reservedKeyWordsForOracleTextSearch");
	public static final String specialKeyWordsForHaysSearch = SharedObjects.getEnvironmentValue("specialKeyWordsForHaysSearch");
	public static final String[] specialKeyWordsForHaysSearchArr = specialKeyWordsForHaysSearch.split(",");
	public static final String IS_ADVANCE_SEARCH = "isAdvanceSearch";
	public static final String IS_HOME_SEARCH = "isHomePageSearch";
	public static final String REGISTERED_DATE = "registered_date";
	public static final String AlertProfileID = "AlertProfileID";
	public static final String JOB_POST_CODE = "job_postcode"; // added for r7 alert form requirement
	
	//Added for Release 7.0 Job Type
	public static final String JOB_MIN_PERM_SLIIDER = "job_min_permanent_slider";
	public static final String JOB_MAX_PERM_SLIIDER = "job_max_permanent_slider";
	public static final String JOB_MIN_TEMP_SLIIDER = "job_min_temporary_slider";
	public static final String JOB_MAX_TEMP_SLIIDER = "job_max_temporary_slider";
	public static final String JOB_MIN_CONT_SLIIDER = "job_min_contract_slider";
	public static final String JOB_MAX_CONT_SLIIDER = "job_max_contract_slider";
	public static final String JOB_PERM_SLIDER = "job_permanent_slider";
	public static final String JOB_TEMP_SLIDER = "job_temporary_slider";
	public static final String JOB_CONT_SLIDER = "job_contract_slider";
	public static final String JOB_SELECT_PERM_SLIDER = "job_select_perm_slider";
	public static final String JOB_SELECT_TEMP_SLIDER = "job_select_temp_slider";
	public static final String JOB_SELECT_CONT_SLIDER = "job_select_cont_slider";
	 

}
