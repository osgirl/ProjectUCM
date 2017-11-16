/*     */ package intradoc.shared;
/*     */ 
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class SharedPageMergerData
/*     */ {
/*  31 */   static final String[][] m_sharedTablesSearch = { { "dGif", "DocTypes", "dDocType", "dDocType" }, { "DocTypes.dDescription", "DocTypes", "dDocType", "dDocType" }, { "dFullName", "Users", "dDocAuthor", "dName" }, { "dEmail", "Users", "dDocAuthor", "dName" } };
/*     */ 
/*  44 */   static final String[][] m_optionListMapTable = { { "docAuthors", "dUser" }, { "securityGroups", "dSecurityGroup" }, { "docTypes", "dDocType" }, { "docAccounts", "dDocAccount" } };
/*     */ 
/*  52 */   public static HashMap m_specialLookupKeys = new HashMap();
/*  53 */   public static Properties m_optionsListMap = new Properties();
/*  54 */   public static Hashtable m_pageInfo = new Hashtable();
/*  55 */   public static Hashtable m_resultPageInfo = new Hashtable();
/*  56 */   public static final String[] TEMPLATE_VARIABLES = { "TemplateName", "TemplateFilePath", "TemplateClass", "TemplateType" };
/*     */ 
/*     */   public static void init()
/*     */   {
/*  64 */     m_specialLookupKeys = new HashMap();
/*  65 */     for (int i = 0; i < m_sharedTablesSearch.length; ++i)
/*     */     {
/*  67 */       m_specialLookupKeys.put(m_sharedTablesSearch[i][0], m_sharedTablesSearch[i]);
/*     */     }
/*     */ 
/*  70 */     m_optionsListMap = new Properties();
/*  71 */     for (i = 0; i < m_optionListMapTable.length; ++i)
/*     */     {
/*  73 */       m_optionsListMap.put(m_optionListMapTable[i][0], m_optionListMapTable[i][1]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void addTemplateInfo(String name, String filename, String templateclass, String templatetype)
/*     */   {
/*  80 */     String[] data = { name, filename, templateclass, templatetype };
/*  81 */     m_pageInfo.put(name, data);
/*     */   }
/*     */ 
/*     */   public static void loadTemplateData(String name, Properties props)
/*     */   {
/*  86 */     String[] data = (String[])(String[])m_pageInfo.get(name);
/*  87 */     if (data == null)
/*     */       return;
/*  89 */     for (int i = 0; i < TEMPLATE_VARIABLES.length; ++i)
/*     */     {
/*  91 */       props.put(TEMPLATE_VARIABLES[i], data[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void addResultTemplateInfo(String name, Properties resultPageInfo)
/*     */   {
/* 101 */     ResultData resultData = new ResultData();
/* 102 */     resultData.setValues(resultPageInfo);
/* 103 */     Properties props = resultData.getProps();
/* 104 */     m_resultPageInfo.put(name, props);
/*     */   }
/*     */ 
/*     */   public static boolean loadResultTemplateData(String name, Properties props)
/*     */   {
/* 109 */     Properties resultProps = (Properties)m_resultPageInfo.get(name);
/* 110 */     if (resultProps != null)
/*     */     {
/* 112 */       for (Enumeration e = resultProps.propertyNames(); e.hasMoreElements(); )
/*     */       {
/* 114 */         String key = (String)e.nextElement();
/* 115 */         props.put(key, resultProps.getProperty(key));
/*     */       }
/* 117 */       return true;
/*     */     }
/*     */ 
/* 120 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 125 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72682 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SharedPageMergerData
 * JD-Core Version:    0.5.4
 */