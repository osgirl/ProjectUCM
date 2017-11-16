/*     */ package intradoc.apps.useradmin.irmintg.util;
/*     */ 
/*     */ public final class IRMConstants
/*     */ {
/*     */   public static final String UCM_IRM_INTEGRATION_COMPONENT_NAME = "IRMUCMIntegration";
/*     */   public static final String FIELD_COMPONENT_NAME = "name";
/*     */   public static final String RS_ENABLED_COMPONENTS = "EnabledComponents";
/*     */   public static final String FIELD_GROUP_NAME = "dGroupName";
/*     */   public static final String FIELD_ACCOUNT_NAME = "dDocAccount";
/*     */   public static final String FIELD_ROLE_NAME = "dRoleName";
/*     */   public static final String FIELD_RS_FEATURE_MAP = "rsFeatureMap";
/*     */   public static final String RS_CONSTRAINTS = "rsConstraints";
/*     */   public static final String RS_CONFIG = "rsConfig";
/*     */   public static final String FIELD_RS_DEFAULT_FEATURE_MAP = "rsDefaultFeatureMap";
/*     */   public static final String IRM_DESKTOP_SUFFIX = "/irm_desktop";
/*     */   public static final String IRM_SEALING_SERVICES_SUFFIX = "/irm_sealing/sealing_services";
/*     */   public static final String IRM_CONTENT_TYPE_OPS_SUFFIX = "/irm_sealing/content_type_operations";
/*     */   public static final String IRM_FEATURE_OPS_SUFFIX = "/irm_services/feature_operations";
/*     */   public static final String IRM_DOMAIN_OPS_SUFFIX = "/irm_services/domain_operations";
/*     */   public static final String FIELD_IS_ACCOUNT = "isAccount";
/*     */   public static final String HAS_OVERRIDDEN_FEATURES = "hasOverriddenFeatures";
/*     */   public static final int SIZE_TEXT_FIELD = 10;
/*     */   public static final String NO_FEATURE = "EmptyFeature";
/*     */   public static final String IRM_PASSWORD = "IRMPassword";
/*     */   public static final char APPLET_RIGHTS_SECURITY_GROUP_START_CHAR = '#';
/*     */   public static final String FIELD_DPRIVILEGE = "dPrivilege";
/*     */   public static final String FIELD_FEATURE = "IRMFeature";
/*     */   public static final String IRM_FEATURE_LIST = "IRMFeatureList";
/*     */   public static final String FIELD_AUDIT_OFFLINE_USE = "AuditOfflineUse";
/*     */   public static final String FIELD_ENABLE_IRM = "EnableIRM";
/*     */   public static final String FIELD_UNSEAL_TYPES = "UnsealTypes";
/*     */   public static final String FIELD_NON_CONTROLLED_SEALED_CONTENT = "NonControlSealedContent";
/*     */   public static final String FIELD_ALLOW_OFFLINE = "AllowOffline";
/*     */   public static final String FIELD_REFRESH_PERIOD_AMOUNT = "RefreshPeriodAmount";
/*     */   public static final String FIELD_REFRESH_PERIOD_UNITS = "RefreshPeriodUnits";
/*     */   public static final String ALLOW_OFFLINE_FALSE = "0";
/*     */   public static final String ALLOW_OFFLINE_TRUE = "1";
/*     */   public static final String AUDIT_OFFLINE_FALSE = "0";
/*     */   public static final String AUDIT_OFFLINE_TRUE = "1";
/*     */   public static final String REFRESHPERIOD_UNITS_SEPARATOR = " ";
/*     */   public static final String FIELD_DESKTOP_URI = "DesktopURI";
/*     */   public static final String FIELD_SEALING_URI = "SealingURI";
/*     */   public static final String FIELD_HTTP_URI = "HttpURI";
/*     */   public static final String FIELD_SEALING_UNAME = "SealingUName";
/*     */   public static final String FIELD_SEALING_PWD = "SealingPwd";
/*     */   public static final String FEATURE_ID = "featureID";
/*     */   public static final String FEATURE_LABEL = "featureLabel";
/*     */   public static final String FEATURE_DESCRIPTION = "featureDescription";
/*     */   public static final String SERVICE_GET_COMPONENTS_CONFIG_INFO = "CONFIG_INFO";
/*     */   public static final String SERVICE_SET_PROTECTION = "IRM_SET_PROTECTION";
/*     */   public static final String SERVICE_GET_PROTECTION = "IRM_GET_PROTECTION";
/*     */   public static final String SERVICE_GET_FEATURE_MAPPING_FOR_ROLE = "IRM_GET_FEATURE_MAPPING_FOR_ROLE";
/*     */   public static final String SERVICE_EDIT_IRM_MAPPING = "EDIT_IRM_MAPPING";
/*     */   public static final String SERVICE_GET_IRM_FEATURES = "IRM_GET_FEATURES";
/*     */   public static final String SERVICE_GET_DEFAULT_MAPPING = "GET_IRM_DEFAULT_MAPPING";
/*     */   public static final String SERVICE_GET_REFRESH_PERIODS = "IRM_GET_REFRESH_PERIODS";
/*     */   public static final String REFRESH_PERIODS = "refreshPeriodsList";
/*     */   public static final String SERVICE_GET_ROLE_CONSTRAINTS = "GET_IRM_CONSTRAINTS";
/*     */   public static final String SERVICE_EDIT_CONSTRAINTS = "EDIT_IRM_CONSTRAINTS";
/*     */   public static final String SERVICE_GET_CONFIG = "IRM_GET_CONFIG";
/*     */   public static final String SERVICE_UPDATE_CONFIG = "IRM_UPDATE_CONFIG";
/*     */   public static final String SERVICE_GET_IRM_MAPPING = "GET_RIGHT_IRMFEATURE_MAPPING";
/*     */   public static final String REFRESH_PERIOD_DELIMITER = " ";
/*     */   public static final String IRM_INPUT_REGEX = "[\\p{L}\\p{N}]+[[^[:punct:]--[.,\"';:#$%/@^_-]][\\p{L}\\p{N}]+]*";
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 380 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94362 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.util.IRMConstants
 * JD-Core Version:    0.5.4
 */