/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class VerityIndexUtils
/*     */ {
/*     */   public static Vector prepareCommandLine(String commandTableName, boolean needNewProcess, DataResultSet conditions, IndexerConfig config)
/*     */   {
/*  31 */     boolean reportCommand = true;
/*  32 */     Vector cmdLine = new IdcVector();
/*  33 */     if (needNewProcess)
/*     */     {
/*  35 */       if (EnvUtils.isFamily("unix"))
/*     */       {
/*  37 */         String userEnvPath = config.getValue("IndexerUserEnvPath");
/*  38 */         if ((userEnvPath != null) && (userEnvPath.length() > 0))
/*     */         {
/*  40 */           cmdLine.addElement(userEnvPath);
/*     */         }
/*  42 */         String envName = config.getValue("IndexerEnvName");
/*  43 */         String vdkPath = config.getScriptValue("IndexerEnvPath");
/*  44 */         if ((envName != null) && (envName.length() > 0) && (vdkPath != null) && (vdkPath.length() > 0))
/*     */         {
/*  46 */           cmdLine.addElement(envName + "=" + vdkPath);
/*     */         }
/*     */       }
/*  49 */       cmdLine.addElement(config.getValue("IndexerPath"));
/*     */     }
/*     */ 
/*  56 */     DataResultSet drset = config.getTable(commandTableName);
/*     */ 
/*  58 */     for (; conditions.isRowPresent(); conditions.next())
/*     */     {
/*  60 */       String key = ResultSetUtils.getValue(conditions, "key");
/*  61 */       String value = ResultSetUtils.getValue(conditions, "value");
/*  62 */       config.setValue(key, value);
/*     */     }
/*     */ 
/*  65 */     if (drset != null)
/*     */     {
/*  67 */       drset.first();
/*     */     }
/*  69 */     for (; drset.isRowPresent(); drset.next())
/*     */     {
/*  71 */       String name = config.parseScriptValue(drset.getStringValue(0));
/*  72 */       if ((name == null) || (name.length() == 0)) {
/*     */         continue;
/*     */       }
/*  75 */       if (name.indexOf(32) > 0)
/*     */       {
/*  77 */         Vector additionalParam = StringUtils.parseArray(name, ' ', '%');
/*  78 */         int size = additionalParam.size();
/*  79 */         for (int j = 0; j < size; ++j)
/*     */         {
/*  81 */           cmdLine.addElement(additionalParam.elementAt(j));
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/*  86 */         String value = drset.getStringValue(1);
/*  87 */         value = config.parseScriptValue(value);
/*     */ 
/*  89 */         boolean hasWhiteSpace = StringUtils.convertToBool(drset.getStringValue(2), false);
/*     */ 
/*  91 */         if ((hasWhiteSpace) && (EnvUtils.isMicrosoftVM()))
/*     */         {
/*  93 */           value = FileUtils.windowsSlashes(value);
/*  94 */           value = "\"" + value + "\"";
/*     */         }
/*  96 */         cmdLine.addElement(name);
/*  97 */         if ((value == null) || ((value.length() == 0) && (!StringUtils.convertToBool(drset.getStringValue(3), false)))) {
/*     */           continue;
/*     */         }
/* 100 */         cmdLine.addElement(value);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 106 */     int size = cmdLine.size();
/* 107 */     String[] cmdBuff = new String[size];
/* 108 */     String reportLine = null;
/* 109 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 111 */       cmdBuff[i] = ((String)cmdLine.elementAt(i));
/* 112 */       if (!reportCommand)
/*     */         continue;
/* 114 */       if (reportLine != null)
/*     */       {
/* 116 */         reportLine = reportLine + " " + cmdBuff[i];
/*     */       }
/*     */       else
/*     */       {
/* 120 */         reportLine = cmdBuff[i];
/*     */       }
/*     */     }
/*     */ 
/* 124 */     if (reportLine != null)
/*     */     {
/* 126 */       Report.trace("indexer", reportLine, null);
/*     */     }
/*     */ 
/* 129 */     for (conditions.first(); conditions.isRowPresent(); conditions.next())
/*     */     {
/* 131 */       String needRemove = ResultSetUtils.getValue(conditions, "removeAfterward");
/* 132 */       if (!StringUtils.convertToBool(needRemove, false))
/*     */         continue;
/* 134 */       String key = ResultSetUtils.getValue(conditions, "key");
/* 135 */       config.removeValue(key);
/*     */     }
/*     */ 
/* 138 */     return cmdLine;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 143 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.VerityIndexUtils
 * JD-Core Version:    0.5.4
 */