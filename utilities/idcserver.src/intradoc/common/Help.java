/*     */ package intradoc.common;
/*     */ 
/*     */ import java.net.URL;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Help
/*     */ {
/*  29 */   protected static URL m_helpURL = null;
/*  30 */   protected static String m_helpDir = null;
/*  31 */   protected static Vector m_validHelpLangs = null;
/*     */ 
/*  34 */   protected static boolean m_systemHelpIsLocalized = false;
/*  35 */   protected static boolean m_useBuiltInHelpSystem = false;
/*     */ 
/*     */   public static void setPaths(String helpDir)
/*     */   {
/*  39 */     m_helpDir = FileUtils.directorySlashes(helpDir);
/*     */   }
/*     */ 
/*     */   public static String getHelpDir()
/*     */   {
/*  44 */     return m_helpDir;
/*     */   }
/*     */ 
/*     */   public static void setHelpUrlBase(URL helpURL)
/*     */   {
/*  49 */     m_helpURL = helpURL;
/*     */   }
/*     */ 
/*     */   public static boolean getIsSystemHelpLocalized()
/*     */   {
/*  54 */     return m_systemHelpIsLocalized;
/*     */   }
/*     */ 
/*     */   public static void setIsSystemHelpLocalized(boolean systemHelpIsLocalized)
/*     */   {
/*  59 */     m_systemHelpIsLocalized = systemHelpIsLocalized;
/*     */   }
/*     */ 
/*     */   public static boolean getUseBuiltInHelpSystem()
/*     */   {
/*  64 */     return m_useBuiltInHelpSystem;
/*     */   }
/*     */ 
/*     */   public static void setUseBuiltInHelpSystem(boolean useBuiltInHelpSystem)
/*     */   {
/*  69 */     m_useBuiltInHelpSystem = useBuiltInHelpSystem;
/*     */   }
/*     */ 
/*     */   public static void display(String page) throws ServiceException
/*     */   {
/*  74 */     display(page, m_helpDir, m_helpURL);
/*     */   }
/*     */ 
/*     */   protected static void display(String page, String helpDir, URL helpUrl)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/*  82 */       Browser.showDocumentEx("IdcHelp", helpDir, m_helpURL, page);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  86 */       throw new ServiceException("!syBrowserNoHelpAccess", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void display(String page, ExecutionContext ctxt)
/*     */     throws ServiceException
/*     */   {
/*  97 */     String directHelpPath = null;
/*  98 */     if (m_useBuiltInHelpSystem)
/*     */     {
/* 100 */       String dir = "";
/* 101 */       if (m_systemHelpIsLocalized)
/*     */       {
/* 103 */         dir = computeLangHelpDirSuffix(ctxt);
/*     */       }
/* 105 */       directHelpPath = m_helpDir + dir;
/*     */     }
/* 107 */     display(page, directHelpPath, m_helpURL);
/*     */   }
/*     */ 
/*     */   public static String computeLangHelpId(ExecutionContext ctxt)
/*     */   {
/* 112 */     String langId = null;
/* 113 */     String sysLangId = LocaleResources.getSystemLocale().m_languageId;
/* 114 */     if (ctxt != null)
/*     */     {
/* 116 */       String cxtLangId = (String)ctxt.getLocaleResource(1);
/* 117 */       if (cxtLangId != null)
/*     */       {
/* 119 */         langId = cxtLangId;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 124 */     boolean determinedId = false;
/* 125 */     if (langId != null)
/*     */     {
/* 127 */       if (langId.equals("en"))
/*     */       {
/* 129 */         determinedId = true;
/*     */       }
/* 131 */       else if (isValidHelpLangId(langId))
/*     */       {
/* 133 */         determinedId = true;
/*     */       }
/*     */     }
/* 136 */     if ((!determinedId) && (isValidHelpLangId(sysLangId)))
/*     */     {
/* 138 */       langId = sysLangId;
/*     */     }
/*     */ 
/* 141 */     return langId;
/*     */   }
/*     */ 
/*     */   public static String computeLangHelpDirSuffix(ExecutionContext ctxt)
/*     */   {
/* 146 */     String langId = computeLangHelpId(ctxt);
/* 147 */     String dir = "";
/* 148 */     if ((langId != null) && (langId.length() > 0))
/*     */     {
/* 150 */       dir = langId + "/";
/*     */     }
/*     */ 
/* 153 */     return dir;
/*     */   }
/*     */ 
/*     */   public static void setValidHelpLangs(Vector validHelpLangs)
/*     */   {
/* 158 */     m_validHelpLangs = validHelpLangs;
/*     */   }
/*     */ 
/*     */   public static List getValidHelpLangs()
/*     */   {
/* 163 */     return m_validHelpLangs;
/*     */   }
/*     */ 
/*     */   public static boolean isValidHelpLangId(String languageId)
/*     */   {
/* 171 */     if ((languageId == null) || (m_validHelpLangs == null))
/*     */     {
/* 173 */       return false;
/*     */     }
/*     */ 
/* 176 */     return m_validHelpLangs.indexOf(languageId) >= 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 182 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80969 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.Help
 * JD-Core Version:    0.5.4
 */