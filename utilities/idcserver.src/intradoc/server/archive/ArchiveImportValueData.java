/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.ServiceException;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ArchiveImportValueData
/*     */ {
/*  34 */   public Properties m_mappedValues = new Properties();
/*  35 */   public Properties m_isValueIdocScript = new Properties();
/*  36 */   public boolean m_isAll = false;
/*  37 */   public boolean m_isDefaultIdocScript = false;
/*  38 */   public String m_defaultOutput = null;
/*     */ 
/*  40 */   public DynamicHtmlMerger m_htmlMerger = null;
/*     */ 
/*     */   public void addMap(boolean isAll, String input, String output)
/*     */   {
/*  49 */     this.m_isAll = isAll;
/*     */ 
/*  52 */     boolean isIdocScript = (output.indexOf("<$") >= 0) && (output.indexOf("$>") >= 2);
/*     */ 
/*  54 */     if (isAll)
/*     */     {
/*  56 */       this.m_defaultOutput = output;
/*  57 */       this.m_isDefaultIdocScript = isIdocScript;
/*     */     }
/*     */     else
/*     */     {
/*  61 */       this.m_mappedValues.put(input.toLowerCase(), output);
/*  62 */       if (!isIdocScript)
/*     */         return;
/*  64 */       this.m_isValueIdocScript.put(input.toLowerCase(), output);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getMappedValue(String field, String value)
/*     */     throws ServiceException
/*     */   {
/*  74 */     String lookupStr = value.toLowerCase();
/*  75 */     if (this.m_isAll)
/*     */     {
/*  77 */       String output = null;
/*  78 */       if (this.m_isDefaultIdocScript)
/*     */       {
/*     */         try
/*     */         {
/*  82 */           output = this.m_htmlMerger.evaluateScriptReportError(this.m_defaultOutput);
/*     */         }
/*     */         catch (ParseSyntaxException e)
/*     */         {
/*  86 */           String msg = LocaleUtils.encodeMessage("csUnableToEvalScriptForField", null, field);
/*     */ 
/*  88 */           throw new ServiceException(msg, e);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/*  92 */           String priorMsg = null;
/*  93 */           if (e instanceof IllegalArgumentException)
/*     */           {
/*  95 */             priorMsg = e.getMessage();
/*     */           }
/*  97 */           String msg = LocaleUtils.encodeMessage("csUnableToEvalScriptForField2", priorMsg, field, this.m_defaultOutput);
/*     */ 
/*  99 */           if (priorMsg != null)
/*     */           {
/* 101 */             throw new ServiceException(msg);
/*     */           }
/* 103 */           throw new ServiceException(msg, e);
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 108 */         output = this.m_defaultOutput;
/*     */       }
/* 110 */       return output;
/*     */     }
/*     */ 
/* 113 */     String toValue = this.m_mappedValues.getProperty(lookupStr);
/* 114 */     if (this.m_isValueIdocScript.get(lookupStr) != null)
/*     */     {
/*     */       try
/*     */       {
/* 118 */         if (this.m_htmlMerger.m_isReportErrorStack)
/*     */         {
/* 120 */           String msg = LocaleUtils.encodeMessage("csDynHTMLEvalVariableInMethod", null, "ArchiveImportValueData.getMappedValue", field);
/* 121 */           this.m_htmlMerger.pushStackMessage(msg);
/*     */         }
/* 123 */         toValue = this.m_htmlMerger.evaluateScript(toValue);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 127 */         String priorMsg = null;
/* 128 */         if (e instanceof IllegalArgumentException)
/*     */         {
/* 130 */           priorMsg = e.getMessage();
/*     */         }
/* 132 */         String msg = LocaleUtils.encodeMessage("csUnableToEvalScript", priorMsg, toValue);
/*     */ 
/* 138 */         throw new ServiceException(msg, e);
/*     */       }
/*     */       finally
/*     */       {
/* 142 */         if (this.m_htmlMerger.m_isReportErrorStack)
/*     */         {
/* 144 */           this.m_htmlMerger.popStack();
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 149 */     if (toValue == null)
/*     */     {
/* 151 */       return value;
/*     */     }
/* 153 */     return toValue;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 158 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiveImportValueData
 * JD-Core Version:    0.5.4
 */