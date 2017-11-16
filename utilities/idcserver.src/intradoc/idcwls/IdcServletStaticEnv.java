/*     */ package intradoc.idcwls;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.DocumentPathUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcServletStaticEnv
/*     */ {
/*     */   public static boolean m_isInit;
/*  43 */   protected static String m_servletAppID = "unspecified";
/*     */   public static boolean m_forceSystemConfigPage;
/*     */   public static String m_relativeWebRoot;
/*     */   public static String m_lowerCaseWebRoot;
/*     */   public static String m_cgiPathRoot;
/*     */   public static String m_lowerCaseCgiPathRoot;
/*     */   public static String m_lowerCaseAuthPrefix;
/*     */   public static String m_internalAppExtensionWildcardTest;
/*     */   public static String m_executableExtensionWildcardTest;
/*     */   public static String[] m_lowerCaseSpecialAuthGroups;
/*     */   public static String[] m_applicationExecutableEnabledGroups;
/*     */   public static String[] m_lowerCaseProxiedFilesGroups;
/*     */   public static Workspace m_systemWorkspace;
/*     */   public static ThreadGroup m_servletThreadGroup;
/*     */   public static Map<String, String> m_defaultInitProps;
/*     */ 
/*     */   public static void init(IdcServletConfig servletConfig, String serverType)
/*     */     throws ServiceException
/*     */   {
/*  73 */     m_forceSystemConfigPage = SharedObjects.getEnvValueAsBoolean("ForceSystemConfigPage", false);
/*  74 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  75 */     m_servletAppID = idcName + ":" + serverType;
/*  76 */     String authPrefix = DocumentPathUtils.getAuthPathPrefix();
/*  77 */     m_lowerCaseAuthPrefix = authPrefix;
/*  78 */     String contextRoot = (String)servletConfig.getAttribute("context-root");
/*  79 */     contextRoot = FileUtils.directorySlashes(contextRoot);
/*  80 */     String relativeWebRoot = SharedObjects.getEnvironmentValue("HttpRelativeWebRoot");
/*  81 */     if (relativeWebRoot == null)
/*     */     {
/*  83 */       relativeWebRoot = contextRoot;
/*     */     }
/*     */     else
/*     */     {
/*  87 */       relativeWebRoot = FileUtils.directorySlashes(relativeWebRoot);
/*     */     }
/*  89 */     if (!relativeWebRoot.startsWith(contextRoot))
/*     */     {
/*  93 */       throw new ServiceException(null, "csServletFilterMismatchedContextRoot", new Object[] { contextRoot, m_relativeWebRoot });
/*     */     }
/*     */ 
/*  97 */     m_relativeWebRoot = relativeWebRoot;
/*  98 */     m_lowerCaseWebRoot = m_relativeWebRoot.toLowerCase();
/*  99 */     m_cgiPathRoot = m_relativeWebRoot + DirectoryLocator.getCgiFileName();
/* 100 */     m_lowerCaseCgiPathRoot = m_cgiPathRoot.toLowerCase();
/*     */ 
/* 102 */     String specialAuthGroups = SharedObjects.getEnvironmentValue("SpecialAuthGroups");
/* 103 */     if (specialAuthGroups != null)
/*     */     {
/* 105 */       m_lowerCaseSpecialAuthGroups = convertStringToLowerCaseArray(specialAuthGroups);
/*     */     }
/*     */ 
/* 108 */     String appEnabledGroups = SharedObjects.getEnvironmentValue("ApplicationEnabledGroups");
/* 109 */     if (appEnabledGroups == null)
/*     */     {
/* 111 */       appEnabledGroups = SharedObjects.getEnvironmentValue("JspEnabledGroups");
/*     */     }
/* 113 */     if (appEnabledGroups != null)
/*     */     {
/* 115 */       m_applicationExecutableEnabledGroups = convertStringToLowerCaseArray(appEnabledGroups);
/*     */     }
/*     */ 
/* 119 */     String delegatedFilesGroups = SharedObjects.getEnvironmentValue("ProxiedFilesGroups");
/* 120 */     String[] delegatedFilesGroupsList = null;
/* 121 */     if (delegatedFilesGroups != null)
/*     */     {
/* 123 */       delegatedFilesGroupsList = convertStringToLowerCaseArray(delegatedFilesGroups);
/*     */     }
/* 125 */     String saveAsGroups = SharedObjects.getEnvironmentValue("DocPromptSaveAsGroups");
/* 126 */     String[] saveAsGroupsList = null;
/* 127 */     if (saveAsGroups != null)
/*     */     {
/* 129 */       saveAsGroupsList = convertStringToLowerCaseArray(saveAsGroups);
/*     */     }
/* 131 */     m_lowerCaseProxiedFilesGroups = StringUtils.appendStringArrayNoDuplicates(delegatedFilesGroupsList, saveAsGroupsList, 0);
/*     */ 
/* 135 */     String internalAppFilesTest = SharedObjects.getEnvironmentValue("InternalAppExtensionWildcardTest");
/* 136 */     if (internalAppFilesTest == null)
/*     */     {
/* 138 */       if (SharedObjects.getEnvValueAsBoolean("IsJspServerEnabled", false))
/*     */       {
/* 140 */         internalAppFilesTest = "hcs?|xcs?|jsp*";
/*     */       }
/*     */       else
/*     */       {
/* 144 */         internalAppFilesTest = "hcs?|xcs?";
/*     */       }
/*     */     }
/* 147 */     m_internalAppExtensionWildcardTest = internalAppFilesTest;
/*     */ 
/* 150 */     String executableFilesTest = SharedObjects.getEnvironmentValue("ExecutableExtensionWildcardTest");
/* 151 */     if (executableFilesTest == null)
/*     */     {
/* 153 */       executableFilesTest = "jsp*";
/*     */     }
/* 155 */     m_executableExtensionWildcardTest = executableFilesTest;
/*     */ 
/* 157 */     m_servletThreadGroup = Thread.currentThread().getThreadGroup();
/* 158 */     Provider wsProvider = Providers.getProvider("SystemDatabase");
/* 159 */     if (wsProvider == null)
/*     */       return;
/* 161 */     m_systemWorkspace = (Workspace)wsProvider.getProvider();
/*     */   }
/*     */ 
/*     */   public static String[] convertStringToLowerCaseArray(String val)
/*     */   {
/* 170 */     String valLower = val.toLowerCase();
/* 171 */     List valList = StringUtils.makeListFromSequenceSimple(valLower);
/* 172 */     String[] retList = new String[valList.size()];
/* 173 */     retList = (String[])valList.toArray(retList);
/* 174 */     return retList;
/*     */   }
/*     */ 
/*     */   public static boolean isSpecialGroup(String lowerGroup)
/*     */   {
/* 179 */     if (m_lowerCaseSpecialAuthGroups == null)
/*     */     {
/* 181 */       return false;
/*     */     }
/* 183 */     return StringUtils.findStringIndex(m_lowerCaseSpecialAuthGroups, lowerGroup) >= 0;
/*     */   }
/*     */ 
/*     */   public static boolean isAppExecutableGroup(String lowerGroup)
/*     */   {
/* 188 */     if (m_applicationExecutableEnabledGroups == null)
/*     */     {
/* 190 */       return false;
/*     */     }
/* 192 */     return StringUtils.findStringIndex(m_applicationExecutableEnabledGroups, lowerGroup) >= 0;
/*     */   }
/*     */ 
/*     */   public static boolean isProxiedFileGroup(String lowerGroup)
/*     */   {
/* 197 */     return StringUtils.findStringIndex(m_lowerCaseProxiedFilesGroups, lowerGroup) >= 0;
/*     */   }
/*     */ 
/*     */   public static boolean isRealUser(String user)
/*     */   {
/* 202 */     return (user != null) && (user.length() > 0) && (!user.equals("anonymous"));
/*     */   }
/*     */ 
/*     */   public static String getStringValue(IdcServletConfig servletConfig, String key)
/*     */   {
/* 207 */     Object val = servletConfig.getAttribute(key);
/* 208 */     String response = null;
/* 209 */     if (val != null)
/*     */     {
/* 211 */       response = ScriptUtils.getDisplayString(val, null);
/*     */     }
/* 213 */     return response;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 218 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79712 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.IdcServletStaticEnv
 * JD-Core Version:    0.5.4
 */