/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.data.DataResultSet;
/*     */ 
/*     */ public class DialogHelpTable
/*     */ {
/*  29 */   public static boolean m_useOhsHelpSystem = false;
/*  30 */   public static boolean m_isInit = false;
/*     */ 
/*     */   public static void checkInit()
/*     */   {
/*  39 */     if (m_isInit)
/*     */       return;
/*  41 */     m_useOhsHelpSystem = SharedObjects.getEnvValueAsBoolean("UseOhsHelpSystem", true);
/*  42 */     m_isInit = true;
/*     */   }
/*     */ 
/*     */   public static String getHelpPage(String digName)
/*     */   {
/*  55 */     checkInit();
/*     */ 
/*  57 */     String[] tableNames = { "ServerHelpPages", "AppletHelpPages" };
/*  58 */     String[] tableFieldPrefixes = { "shp", "ahp" };
/*     */ 
/*  60 */     for (int i = 0; i < tableNames.length; ++i)
/*     */     {
/*  62 */       DataResultSet helpStrings = SharedObjects.getTable(tableNames[i]);
/*  63 */       String fieldNamePrefix = tableFieldPrefixes[i];
/*  64 */       if (helpStrings == null)
/*     */         continue;
/*  66 */       String primaryKeyName = fieldNamePrefix + "Key";
/*  67 */       String stdHelpPageName = fieldNamePrefix + "HelpPage";
/*  68 */       String stdHelpTopicName = fieldNamePrefix + "HelpTopic";
/*  69 */       int primaryKeyIndex = helpStrings.getFieldInfoIndex(primaryKeyName);
/*  70 */       int stdHelpPageIndex = helpStrings.getFieldInfoIndex(stdHelpPageName);
/*  71 */       int stdHelpTopicIndex = helpStrings.getFieldInfoIndex(stdHelpTopicName);
/*  72 */       if (helpStrings.findRow(primaryKeyIndex, digName) == null)
/*     */         continue;
/*  74 */       String result = null;
/*  75 */       if ((m_useOhsHelpSystem) && (stdHelpTopicIndex >= 0))
/*     */       {
/*  77 */         result = helpStrings.getStringValue(stdHelpTopicIndex);
/*     */       }
/*  79 */       if ((result == null) || (result.length() == 0))
/*     */       {
/*  81 */         result = helpStrings.getStringValue(stdHelpPageIndex);
/*  82 */         if ((m_useOhsHelpSystem) && (result != null) && (result.length() > 0))
/*     */         {
/*  84 */           result = FileUtils.getName(result);
/*     */         }
/*     */       }
/*  87 */       if ((result != null) && (result.length() == 0))
/*     */       {
/*  90 */         result = "";
/*     */       }
/*     */ 
/*  93 */       return result;
/*     */     }
/*     */ 
/*  98 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 103 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71864 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.DialogHelpTable
 * JD-Core Version:    0.5.4
 */