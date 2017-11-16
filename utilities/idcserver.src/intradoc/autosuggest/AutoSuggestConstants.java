/*    */ package intradoc.autosuggest;
/*    */ 
/*    */ import intradoc.server.DirectoryLocator;
/*    */ 
/*    */ public class AutoSuggestConstants
/*    */ {
/* 24 */   public static short DEFAULT_MIN_GRAM_LENGTH = 2;
/* 25 */   public static short DEFAULT_MAX_GRAM_LENGTH = 3;
/* 26 */   public static double DEFAULT_PROXIMITY_CUT_OFF = 0.1D;
/* 27 */   public static int OCCURRENCE_BUCKET_SIZE = 250;
/*    */   public static final String KEY_TERMS_PROCESSED_PER_GRAM = "AutoSuggesterPerGramTermsProcessedLimit";
/*    */   public static final String KEY_MIN_GRAM_LENGTH = "AutoSuggestMinGramLength";
/*    */   public static final String KEY_MAX_GRAM_LENGTH = "AutoSuggestMaxGramLength";
/*    */   public static final String KEY_PROXIMITY_CUT_OFF = "AutoSuggestProximityCutOff";
/* 32 */   public static long AUTO_SUGGESTER_THREAD_TIMEOUT = 120000L;
/* 33 */   public static long AUTO_SUGGESTER_PER_GRAM_THREAD_TIMEOUT = 60000L;
/*    */ 
/* 37 */   public static String AUTO_SUGGEST_META = "autosuggestmeta";
/* 38 */   public static String AUTO_SUGGEST_PRIMARY_INDEX = "autosuggestindexprimary";
/* 39 */   public static String AUTO_SUGGEST_SECONDARY_INDEX = "autosuggestindexsecondary";
/* 40 */   public static String AUTO_SUGGEST_INDEXER_THREAD_NAME = "Auto-Suggest index update";
/* 41 */   public static String AUTO_SUGGESTER_CHILD_THREAD_NAME = "Auto-Suggester-Child";
/* 42 */   public static String AUTO_SUGGESTER_PARENT_THREAD_NAME = "Auto-Suggester-Parent";
/*    */ 
/* 44 */   public static String AUTO_SUGGEST_DIR = DirectoryLocator.getSystemBaseDirectory("data") + "/AutoSuggest";
/* 45 */   public static String AUTO_SUGGEST_CONTEXTS_FILE = "contexts.hda";
/* 46 */   public static String AUTO_SUGGEST_LOCK_DIR = AUTO_SUGGEST_DIR + "/lock";
/*    */ 
/* 50 */   public static String FIELD_AUTOSUGGEST_IDENTIFIER = "AutoSuggestIdentifier";
/* 51 */   public static String FIELD_AUTOSUGGEST_SECURITYGROUP_ID = "AutoSuggestSecurityGroupID";
/* 52 */   public static String FIELD_AUTOSUGGEST_OWNER = "AutoSuggestOwner";
/* 53 */   public static String FIELD_AUTOSUGGEST_ACCOUNT_ID = "AutoSuggestAccountID";
/* 54 */   public static String FIELD_AUTOSUGGEST_USERS = "AutoSuggestUsers";
/* 55 */   public static String FIELD_AUTOSUGGEST_GROUPS = "AutoSuggestGroups";
/* 56 */   public static String FIELD_AUTOSUGGEST_ROLES = "AutoSuggestRoles";
/* 57 */   public static String FIELD_AUTOSUGGEST_GUID = "AutoSuggestGUID";
/* 58 */   public static String FIELD_AUTOSUGGEST_PARTITION = "AutoSuggestPartition";
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 62 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105661 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.AutoSuggestConstants
 * JD-Core Version:    0.5.4
 */