/*     */ package intradoc.apputilities.idcanalyze;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class IdcAnalyzeReport
/*     */   implements IdcAnalyzeTask
/*     */ {
/*     */   public IdcAnalyzeApp m_analyzer;
/*     */   public Properties m_environment;
/*     */   public Workspace m_workspace;
/*     */ 
/*     */   public void init(IdcAnalyzeApp analyzer, Properties environment, Workspace ws)
/*     */   {
/*  46 */     this.m_analyzer = analyzer;
/*  47 */     this.m_environment = environment;
/*  48 */     this.m_workspace = ws;
/*     */   }
/*     */ 
/*     */   public int getErrorCount()
/*     */   {
/*  53 */     return 0;
/*     */   }
/*     */ 
/*     */   public boolean doTask() throws DataException
/*     */   {
/*  58 */     String[] params = { "dReleaseState", "dStatus", "dProcessingState", "dStatus, dReleaseState" };
/*     */ 
/*  63 */     TableFields tables = new TableFields();
/*  64 */     Properties props = new Properties();
/*  65 */     Hashtable tableNames = new Hashtable();
/*  66 */     tableNames.put("dReleaseState", "ReleaseStateList");
/*  67 */     tableNames.put("dStatus", "StatusList");
/*  68 */     tableNames.put("dProcessingState", "ProcessingStateList");
/*     */ 
/*  71 */     this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/*  72 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeReport", new Object[0]));
/*  73 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeLineBreak", new Object[0]));
/*  74 */     for (int i = 0; i < params.length; ++i)
/*     */     {
/*  76 */       this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeItemsGroupedByCount", new Object[] { params[i] }));
/*     */ 
/*  78 */       this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeSeparator", new Object[0]));
/*     */ 
/*  80 */       props.put("columns", params[i]);
/*  81 */       PropParameters args = new PropParameters(props);
/*  82 */       ResultSet rset = this.m_workspace.createResultSet("QIDCAnalyzeReportQuery", args);
/*  83 */       for (; rset.isRowPresent(); rset.next())
/*     */       {
/*  85 */         String msg = "";
/*  86 */         String output = "";
/*  87 */         for (int j = 0; j < rset.getNumFields() - 1; ++j)
/*     */         {
/*  90 */           FieldInfo finfo = new FieldInfo();
/*  91 */           rset.getIndexFieldInfo(j, finfo);
/*  92 */           output = rset.getStringValue(j);
/*  93 */           this.m_analyzer.debug("Report rset output: " + output);
/*  94 */           this.m_analyzer.debug("Report finfo.m_name: " + finfo.m_name);
/*  95 */           if (j > 0)
/*     */           {
/*  97 */             msg = LocaleUtils.appendMessage(msg, "!csIDCAnalyzeReportAnd");
/*     */           }
/*  99 */           if (finfo.m_name.length() <= 0)
/*     */             continue;
/* 101 */           String[][] display = tables.getDisplayMap((String)tableNames.get(finfo.m_name));
/* 102 */           String presStr = "!" + StringUtils.getPresentationString(display, output);
/* 103 */           msg = LocaleUtils.appendMessage(msg, presStr);
/*     */         }
/*     */ 
/* 108 */         msg = LocaleUtils.appendMessage("!csIDCAnalyzeSemicolon", msg);
/* 109 */         String tmpMsg = LocaleResources.localizeMessage(msg, null);
/* 110 */         int len = tmpMsg.length();
/* 111 */         int numSpaces = 40 - len;
/* 112 */         for (int k = 0; k < numSpaces; ++k)
/*     */         {
/* 114 */           msg = LocaleUtils.appendMessage(" ", msg);
/*     */         }
/* 116 */         String num = rset.getStringValue(rset.getNumFields() - 1);
/*     */ 
/* 118 */         msg = LocaleUtils.appendMessage(num + "", msg);
/* 119 */         IdcMessage logmsg = IdcMessageFactory.lc();
/* 120 */         logmsg.m_msgEncoded = msg;
/* 121 */         this.m_analyzer.log(logmsg);
/*     */       }
/* 123 */       this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/*     */     }
/* 125 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 130 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idcanalyze.IdcAnalyzeReport
 * JD-Core Version:    0.5.4
 */