/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.ServiceException;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ValueMapData
/*     */ {
/*  32 */   public Hashtable m_scriptMaps = new Hashtable();
/*  33 */   public Hashtable m_valueMaps = new Hashtable();
/*  34 */   public Properties m_defaultProps = new Properties();
/*     */ 
/*  36 */   public DynamicHtmlMerger m_htmlMerger = null;
/*     */   public static final int VALUE = 0;
/*     */   public static final int SCRIPT = 1;
/*     */ 
/*     */   public void addMap(String field, boolean isAll, String input, String output)
/*     */   {
/*  48 */     boolean isIdocScript = (output.indexOf("<$") >= 0) && (output.indexOf("$>") >= 2);
/*     */ 
/*  50 */     if (isAll)
/*     */     {
/*  52 */       this.m_defaultProps.put(field, "1");
/*  53 */       this.m_defaultProps.put(field + "_output", output);
/*  54 */       this.m_defaultProps.put(field + "_IsScript", (isIdocScript) ? "1" : "");
/*     */     }
/*     */     else
/*     */     {
/*  58 */       this.m_defaultProps.put(field, "0");
/*  59 */       if (isIdocScript)
/*     */       {
/*  61 */         Properties scriptMap = getMapForField(field, 1, true);
/*  62 */         scriptMap.put(input.toLowerCase(), output);
/*     */       }
/*     */       else
/*     */       {
/*  66 */         Properties valueMap = getMapForField(field, 0, true);
/*  67 */         valueMap.put(input.toLowerCase(), output);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getMappedValue(String field, String value)
/*     */     throws ServiceException
/*     */   {
/*  77 */     String lookupStr = value.toLowerCase();
/*  78 */     String defaultAll = this.m_defaultProps.getProperty(field);
/*  79 */     if ((defaultAll != null) && (defaultAll.equals("1")))
/*     */     {
/*  81 */       String output = this.m_defaultProps.getProperty(field + "_output");
/*  82 */       String isScript = this.m_defaultProps.getProperty(field + "_IsScript");
/*  83 */       if ((isScript != null) && (isScript.length() != 0))
/*     */       {
/*     */         try
/*     */         {
/*  87 */           output = this.m_htmlMerger.evaluateScriptReportError(this.m_defaultProps.getProperty(field + "_output"));
/*     */         }
/*     */         catch (ParseSyntaxException e)
/*     */         {
/*     */           String msg;
/*  94 */           throw new ServiceException(msg, e);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/*  98 */           String priorMsg = null;
/*  99 */           if (e instanceof IllegalArgumentException)
/*     */           {
/* 101 */             priorMsg = e.getMessage();
/*     */           }
/* 103 */           String msg = LocaleUtils.encodeMessage("csUnableToEvalScriptForField2", priorMsg, field, output);
/*     */ 
/* 105 */           if (priorMsg != null);
/* 109 */           throw new ServiceException(msg, e);
/*     */         }
/*     */         finally
/*     */         {
/* 113 */           this.m_htmlMerger.releaseAllTemporary();
/*     */         }
/*     */       }
/* 116 */       return output;
/*     */     }
/*     */ 
/* 119 */     String toValue = getValue(field, lookupStr, 1);
/* 120 */     if (toValue != null)
/*     */     {
/*     */       try
/*     */       {
/* 124 */         if (this.m_htmlMerger.m_isReportErrorStack)
/*     */         {
/* 126 */           String msg = LocaleUtils.encodeMessage("csDynHTMLEvalVariableInMethod", null, "ValueMapData.getMappedValue", field);
/*     */ 
/* 128 */           this.m_htmlMerger.pushStackMessage(msg);
/*     */         }
/* 130 */         toValue = this.m_htmlMerger.evaluateScript(toValue);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 134 */         String priorMsg = null;
/* 135 */         if (e instanceof IllegalArgumentException)
/*     */         {
/* 137 */           priorMsg = e.getMessage();
/*     */         }
/* 139 */         String msg = LocaleUtils.encodeMessage("csUnableToEvalScript", priorMsg, toValue);
/*     */ 
/* 145 */         throw new ServiceException(msg, e);
/*     */       }
/*     */       finally
/*     */       {
/* 149 */         if (this.m_htmlMerger.m_isReportErrorStack)
/*     */         {
/* 151 */           this.m_htmlMerger.popStack();
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 157 */       toValue = getValue(field, lookupStr, 0);
/*     */     }
/*     */ 
/* 160 */     if (toValue == null)
/*     */     {
/* 162 */       return value;
/*     */     }
/* 164 */     return toValue;
/*     */   }
/*     */ 
/*     */   protected Properties getMapForField(String field, int type, boolean isCreate)
/*     */   {
/* 169 */     Hashtable map = null;
/* 170 */     switch (type)
/*     */     {
/*     */     case 1:
/* 173 */       map = this.m_scriptMaps;
/* 174 */       break;
/*     */     case 0:
/*     */     default:
/* 177 */       map = this.m_valueMaps;
/*     */     }
/*     */ 
/* 180 */     Properties prop = (Properties)map.get(field);
/* 181 */     if ((isCreate) && (prop == null))
/*     */     {
/* 183 */       prop = new Properties();
/* 184 */       map.put(field, prop);
/*     */     }
/* 186 */     return prop;
/*     */   }
/*     */ 
/*     */   protected String getValue(String field, String lookupStr, int type)
/*     */   {
/* 191 */     String value = null;
/* 192 */     Properties prop = getMapForField(field, type, false);
/* 193 */     if (prop != null)
/*     */     {
/* 195 */       value = prop.getProperty(lookupStr);
/*     */     }
/* 197 */     return value;
/*     */   }
/*     */ 
/*     */   public void setDynamicHtmlMerger(DynamicHtmlMerger merger)
/*     */   {
/* 202 */     this.m_htmlMerger = merger;
/*     */   }
/*     */ 
/*     */   public DynamicHtmlMerger getDynamicHtmlMerger()
/*     */   {
/* 207 */     return this.m_htmlMerger;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 212 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ValueMapData
 * JD-Core Version:    0.5.4
 */