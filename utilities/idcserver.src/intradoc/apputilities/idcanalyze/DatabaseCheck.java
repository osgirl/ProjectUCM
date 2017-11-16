/*     */ package intradoc.apputilities.idcanalyze;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.lang.Queue;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DatabaseCheck
/*     */   implements IdcAnalyzeTask
/*     */ {
/*     */   protected Hashtable m_docNameHash;
/*     */   protected Hashtable m_revClassHash;
/*     */   protected Properties m_context;
/*     */   protected Workspace m_workspace;
/*     */   protected IdcAnalyzeApp m_analyzer;
/*     */   protected int m_errors;
/*     */   protected ExecutionContext m_executionContext;
/*     */ 
/*     */   public void init(IdcAnalyzeApp analyzer, Properties environment, Workspace ws)
/*     */     throws ServiceException, DataException
/*     */   {
/*  51 */     this.m_executionContext = new ExecutionContextAdaptor();
/*     */ 
/*  53 */     this.m_docNameHash = new Hashtable();
/*  54 */     this.m_revClassHash = new Hashtable();
/*     */ 
/*  56 */     this.m_analyzer = analyzer;
/*     */ 
/*  58 */     this.m_context = environment;
/*     */ 
/*  60 */     this.m_workspace = ws;
/*     */   }
/*     */ 
/*     */   public int getErrorCount()
/*     */   {
/*  65 */     return this.m_errors;
/*     */   }
/*     */ 
/*     */   public boolean doTask()
/*     */     throws DataException, ServiceException
/*     */   {
/*  77 */     this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/*  78 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeDBCheckMsg", new Object[0]));
/*  79 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeLineBreak", new Object[0]));
/*     */ 
/*  81 */     String batchNumStr = (String)this.m_context.get("DbBatchSize");
/*  82 */     int batchNum = NumberUtils.parseInteger(batchNumStr, 1000);
/*  83 */     Queue msgQueue = new Queue();
/*     */ 
/*  85 */     int startVal = 0;
/*  86 */     int maxDid = -1;
/*  87 */     int didCount = -1;
/*  88 */     int endVal = -1;
/*  89 */     boolean isBoundedCheck = false;
/*  90 */     boolean isRangeCheck = false;
/*     */ 
/*  92 */     String startRange = (String)this.m_context.get("StartID");
/*  93 */     if (startRange != null)
/*     */     {
/*  95 */       isRangeCheck = true;
/*  96 */       startVal = NumberUtils.parseInteger(startRange, 0);
/*     */     }
/*  98 */     String endRange = (String)this.m_context.get("EndID");
/*  99 */     if (endRange != null)
/*     */     {
/* 101 */       maxDid = NumberUtils.parseInteger(endRange, -1);
/*     */ 
/* 104 */       if (maxDid > 0)
/*     */       {
/* 106 */         ++maxDid;
/*     */       }
/* 108 */       didCount = maxDid - startVal;
/* 109 */       isBoundedCheck = true;
/*     */     }
/*     */ 
/* 112 */     if (maxDid < 0)
/*     */     {
/* 114 */       maxDid = IdcAnalyzeUtils.getMaxDid(this.m_workspace);
/* 115 */       didCount = IdcAnalyzeUtils.getRevisionCount(this.m_workspace);
/*     */     }
/*     */ 
/* 119 */     Properties props = new Properties();
/* 120 */     Hashtable revMap = null;
/* 121 */     Hashtable metaMap = null;
/* 122 */     Hashtable docMap = null;
/*     */ 
/* 124 */     int iteration = didCount / 80 + 1;
/* 125 */     endVal = (batchNum > didCount) ? maxDid : startVal + batchNum;
/*     */ 
/* 127 */     didCount = 0;
/* 128 */     int fixed = 0;
/* 129 */     int curDid = startVal;
/*     */ 
/* 133 */     for (int count = 0; curDid < maxDid; ++count)
/*     */     {
/* 135 */       boolean isBad = false;
/* 136 */       String errMsg = "";
/* 137 */       String dID = curDid + "";
/* 138 */       ++curDid;
/*     */ 
/* 140 */       if (count % batchNum == 0)
/*     */       {
/* 142 */         props.put("startID", startVal + "");
/* 143 */         props.put("endID", endVal + "");
/*     */ 
/* 146 */         props.put("columns", "dID, dDocName, dDocType, dRevClassID, dReleaseState, dStatus");
/* 147 */         props.put("table", "Revisions");
/* 148 */         revMap = IdcAnalyzeUtils.makeMap("IDCAnalyzeGenericSource", props, this.m_workspace, "dID", false, false);
/*     */ 
/* 151 */         props.put("columns", "dID");
/* 152 */         props.put("table", "DocMeta");
/* 153 */         metaMap = IdcAnalyzeUtils.makeMap("IDCAnalyzeGenericSource", props, this.m_workspace, "dID", false, false);
/*     */ 
/* 156 */         props.put("columns", "dID, dIsWebFormat");
/* 157 */         props.put("table", "Documents");
/* 158 */         docMap = IdcAnalyzeUtils.makeMap("IDCAnalyzeGenericSource", props, this.m_workspace, "dID", false, true);
/*     */ 
/* 161 */         startVal = endVal;
/* 162 */         endVal += batchNum;
/* 163 */         if ((isBoundedCheck) && (endVal > maxDid))
/*     */         {
/* 165 */           endVal = maxDid;
/*     */         }
/*     */       }
/*     */ 
/* 169 */       Vector revRow = (Vector)revMap.get(dID);
/* 170 */       Vector metaRow = (Vector)metaMap.get(dID);
/* 171 */       Vector docRows = (Vector)docMap.get(dID);
/*     */ 
/* 173 */       if ((revRow == null) && (metaRow == null) && (docRows == null))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 180 */       ++didCount;
/* 181 */       if (didCount % iteration == 1)
/*     */       {
/* 183 */         this.m_analyzer.incProgress();
/*     */       }
/*     */ 
/* 187 */       if (revRow == null)
/*     */       {
/* 189 */         errMsg = "!csIDCAnalyzeMissingRevisionsEntry";
/* 190 */         isBad = true;
/*     */       }
/* 192 */       if (metaRow == null)
/*     */       {
/* 194 */         errMsg = "!csIDCAnalyzeMissingDocMetaEntry";
/* 195 */         isBad = true;
/*     */       }
/* 197 */       if (docRows == null)
/*     */       {
/* 199 */         errMsg = "!csIDCAnalyzeMissingDocumentsEntry";
/* 200 */         isBad = true;
/*     */       }
/*     */ 
/* 207 */       if (!isBad)
/*     */       {
/* 209 */         isBad = true;
/*     */ 
/* 211 */         errMsg = "!csIDCAnalyzeMissingDocumentsEntry";
/* 212 */         for (int i = 0; i < docRows.size(); ++i)
/*     */         {
/* 214 */           Vector row = (Vector)docRows.elementAt(i);
/* 215 */           String value = (String)row.elementAt(1);
/*     */ 
/* 217 */           if (!StringUtils.convertToBool(value, false))
/*     */             continue;
/* 219 */           isBad = false;
/* 220 */           break;
/*     */         }
/*     */ 
/* 223 */         if (isBad == true)
/*     */         {
/* 226 */           String val = (String)revRow.elementAt(4);
/* 227 */           if (val.equalsIgnoreCase("E"))
/*     */           {
/* 229 */             String docType = (String)revRow.elementAt(2);
/* 230 */             if ((docType == null) || (docType.length() == 0))
/*     */             {
/* 232 */               isBad = false;
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 240 */       if (isBad)
/*     */       {
/* 242 */         this.m_errors += 1;
/* 243 */         if (StringUtils.convertToBool((String)this.m_context.get("CleanDatabase"), false))
/*     */         {
/* 246 */           deleteFromDb("Revisions", dID);
/* 247 */           deleteFromDb("DocMeta", dID);
/* 248 */           deleteFromDb("Documents", dID);
/* 249 */           ++fixed;
/*     */         }
/*     */         else
/*     */         {
/* 253 */           msgQueue.insert(LocaleUtils.encodeMessage("csIDCAnalyzeDocEntryCorrupt", null, dID) + errMsg);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 258 */         if ((!isRangeCheck) && (!StringUtils.convertToBool((String)this.m_context.get("UseLegacyRevclassCheck"), false)))
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 263 */         Vector row = (Vector)revMap.get(dID);
/* 264 */         String dDocName = (String)row.elementAt(1);
/*     */ 
/* 266 */         if (!StringUtils.convertToBool((String)this.m_context.get("CheckRevClassID"), false)) {
/*     */           continue;
/*     */         }
/* 269 */         String dRevClassID = (String)row.elementAt(3);
/*     */         try
/*     */         {
/* 272 */           checkRevClass(dDocName, dRevClassID);
/*     */         }
/*     */         catch (ServiceException s)
/*     */         {
/* 276 */           this.m_errors += 1;
/* 277 */           msgQueue.insert(s.getMessage());
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 284 */     if ((StringUtils.convertToBool((String)this.m_context.get("CheckRevClassID"), false)) && 
/* 287 */       (!isRangeCheck) && (!StringUtils.convertToBool((String)this.m_context.get("UseLegacyRevclassCheck"), false)))
/*     */     {
/* 291 */       this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeRevclassQueryWarning", new Object[0]));
/*     */ 
/* 293 */       ResultSet rset = null;
/* 294 */       rset = this.m_workspace.createResultSet("QIDCAnalyzeCheckRevClass", null);
/*     */ 
/* 296 */       int numFields = rset.getNumFields();
/* 297 */       Object[] objs = new Object[numFields];
/* 298 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/* 300 */         for (int i = 0; i < numFields; ++i)
/*     */         {
/* 302 */           objs[i] = rset.getStringValue(i);
/*     */         }
/*     */ 
/* 305 */         msgQueue.insert(LocaleUtils.encodeMessage("csIDCAnalyzeRevClassError", null, objs));
/*     */ 
/* 307 */         this.m_errors += 1;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 313 */     while (!msgQueue.empty())
/*     */     {
/* 315 */       this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/* 316 */       IdcMessage msg = IdcMessageFactory.lc();
/* 317 */       msg.m_msgEncoded = msgQueue.remove().toString();
/* 318 */       this.m_analyzer.logStraight(msg);
/*     */     }
/*     */ 
/* 321 */     this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/*     */ 
/* 329 */     if (isBoundedCheck)
/*     */     {
/* 332 */       checkMaxDid("Revisions", maxDid);
/* 333 */       checkMaxDid("DocMeta", maxDid);
/* 334 */       checkMaxDid("Documents", maxDid);
/*     */     }
/*     */     else
/*     */     {
/* 338 */       this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeMaxDidCheckSkippedForRange", new Object[0]));
/*     */     }
/*     */ 
/* 341 */     this.m_analyzer.setProgress(80);
/* 342 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeEntriesChecked", new Object[] { Integer.valueOf(didCount) }));
/* 343 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeErrorCount", new Object[] { Integer.valueOf(this.m_errors) }));
/* 344 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeErrorsFixed", new Object[] { Integer.valueOf(fixed) }));
/*     */ 
/* 346 */     return true;
/*     */   }
/*     */ 
/*     */   public void checkRevClass(String dDocName, String dRevClassID)
/*     */     throws DataException, ServiceException
/*     */   {
/* 353 */     checkItem("dDocName", dDocName, "dRevClassID", dRevClassID);
/*     */ 
/* 355 */     checkItem("dRevClassID", dRevClassID, "dDocName", dDocName);
/*     */   }
/*     */ 
/*     */   protected void checkItem(String lookupCol, String lookup, String verifyCol, String check)
/*     */     throws DataException, ServiceException
/*     */   {
/* 361 */     lookup = lookup.toLowerCase();
/*     */ 
/* 364 */     Hashtable lookupHash = null;
/* 365 */     if (lookupCol.equals("dDocName"))
/*     */     {
/* 367 */       lookupHash = this.m_docNameHash;
/*     */     }
/* 369 */     else if (lookupCol.equals("dRevClassID"))
/*     */     {
/* 371 */       lookupHash = this.m_revClassHash;
/*     */     }
/*     */     else
/*     */     {
/* 375 */       throw new ServiceException(LocaleUtils.encodeMessage("csIDCAnalyzeLookupColumnError", null, lookupCol));
/*     */     }
/*     */ 
/* 379 */     String verifiedStr = (String)lookupHash.get(lookup);
/* 380 */     if (verifiedStr == null)
/*     */     {
/* 383 */       lookupHash.put(lookup, check);
/*     */     }
/*     */     else
/*     */     {
/* 391 */       if (verifiedStr.equals(check))
/*     */         return;
/* 393 */       Object[] args = { lookupCol, lookup, verifyCol, check, verifiedStr };
/*     */ 
/* 396 */       throw new ServiceException(LocaleUtils.encodeMessage("csIDCAnalyzeLookupCheckError", null, args));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteFromDb(String tableName, String did)
/*     */     throws DataException
/*     */   {
/* 403 */     Properties props = new Properties();
/* 404 */     props.put("tableName", tableName);
/* 405 */     props.put("dID", did);
/* 406 */     PropParameters args = new PropParameters(props);
/*     */ 
/* 408 */     long result = 0L;
/* 409 */     result = this.m_workspace.execute("DIDCAnalyzeFixup", args);
/* 410 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeDocDeleted", new Object[] { did, tableName, Long.valueOf(result) }));
/*     */   }
/*     */ 
/*     */   protected void checkMaxDid(String table, int maxDid)
/*     */     throws DataException
/*     */   {
/* 416 */     Properties props = new Properties();
/* 417 */     props.put("tableName", table);
/* 418 */     props.put("maxdID", maxDid + "");
/* 419 */     PropParameters args = new PropParameters(props);
/*     */ 
/* 422 */     ResultSet rset = this.m_workspace.createResultSet("QIDCAnalyzeMaxTableDid", args);
/* 423 */     for (; rset.isRowPresent(); this.m_errors += 1)
/*     */     {
/* 425 */       String dID = rset.getStringValue(ResultSetUtils.getIndexMustExist(rset, "dID"));
/* 426 */       this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeTableEntryIDCorrupt", new Object[] { dID, Integer.valueOf(maxDid) }));
/*     */ 
/* 423 */       rset.next();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 432 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102048 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idcanalyze.DatabaseCheck
 * JD-Core Version:    0.5.4
 */