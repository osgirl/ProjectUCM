/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.QueryUtils;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.LegacyDocumentPathBuilder;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.HashMap;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DatabaseSearchImplementor extends CommonSearchAdaptor
/*     */ {
/*     */   protected Workspace m_workspace;
/*     */   protected String m_result;
/*     */   protected DataBinder m_resultBinder;
/*     */ 
/*     */   public String doQuery(DataBinder binder)
/*     */   {
/*  40 */     String query = binder.getLocal("QueryAssembly");
/*  41 */     int startRow = Integer.parseInt(binder.getLocal("StartRow"));
/*  42 */     int resultCount = Integer.parseInt(binder.getLocal("ResultCount"));
/*     */ 
/*  44 */     String doubleQuery = binder.getAllowMissing("DisableTotalItemsSearchQuery");
/*  45 */     boolean disableDoubleQuery = StringUtils.convertToBool(doubleQuery, false);
/*  46 */     String querySelection = binder.getLocal("QuerySelection");
/*  47 */     String sortSpec = binder.getLocal("SortSpec");
/*  48 */     int index = 0;
/*  49 */     if (!disableDoubleQuery)
/*     */     {
/*  51 */       if ((querySelection == null) || (querySelection.length() == 0))
/*     */       {
/*  53 */         String queryTmp = query.toLowerCase();
/*  54 */         while ((index = queryTmp.indexOf("from", index)) >= 0)
/*     */         {
/*  56 */           if ((index < queryTmp.length() - 5) && (Validation.isSpace(queryTmp.charAt(index - 1))) && (Validation.isSpace(queryTmp.charAt(index + 4))))
/*     */           {
/*  60 */             --index;
/*  61 */             break;
/*     */           }
/*  63 */           ++index;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/*  68 */         index = query.indexOf(querySelection) + querySelection.length();
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/*  73 */       resultCount += 1;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  78 */       if ((sortSpec != null) && (sortSpec.trim().length() > 0) && (!QueryUtils.validateQuerySortClause(sortSpec)))
/*     */       {
/*  81 */         String msg = LocaleUtils.encodeMessage("csSearchInvalidOrderByClause", null, sortSpec);
/*  82 */         throw new DataException(msg);
/*     */       }
/*  84 */       ResultSet rset = this.m_workspace.createResultSetSQL(query);
/*  85 */       rset.setDateFormat(LocaleResources.m_iso8601Format);
/*  86 */       DataResultSet drset = new DataResultSet();
/*     */ 
/*  88 */       drset.copyFieldInfoWithFlags(rset, 0);
/*  89 */       binder = new DataBinder();
/*     */ 
/*  91 */       int skippedRow = rset.skip(startRow - 1);
/*  92 */       if ((startRow == 0) || (skippedRow == startRow - 1))
/*     */       {
/*  94 */         drset.mergeEx(null, rset, false, resultCount);
/*  95 */         processResult(drset);
/*     */       }
/*     */ 
/*  98 */       if ((!disableDoubleQuery) && (index > 0))
/*     */       {
/* 100 */         String countQuery = "SELECT COUNT(*) " + query.substring(index);
/* 101 */         if ((sortSpec != null) && (sortSpec.length() != 0))
/*     */         {
/* 103 */           index = countQuery.length() - sortSpec.length();
/*     */         }
/*     */         else
/*     */         {
/* 107 */           String queryTmp = countQuery.toLowerCase();
/* 108 */           while ((index = queryTmp.indexOf("order", index)) >= 0)
/*     */           {
/* 111 */             if ((index < queryTmp.length() - 6) && (Validation.isSpace(queryTmp.charAt(index - 1))) && (Validation.isSpace(queryTmp.charAt(index + 5))))
/*     */             {
/* 115 */               String tmp = queryTmp.substring(index + 6).trim();
/* 116 */               if ((tmp.startsWith("by")) && (Validation.isSpace(tmp.charAt(3))) && (tmp.indexOf('\'') < 0) && (tmp.indexOf('"') < 0))
/*     */               {
/* 120 */                 --index;
/* 121 */                 break;
/*     */               }
/*     */             }
/*     */ 
/* 125 */             ++index;
/*     */           }
/*     */         }
/* 128 */         if (index > 0)
/*     */         {
/* 130 */           countQuery = countQuery.substring(0, index);
/*     */         }
/*     */ 
/* 133 */         rset = this.m_workspace.createResultSetSQL(countQuery);
/* 134 */         if (rset.isRowPresent())
/*     */         {
/* 136 */           String totalRows = rset.getStringValue(0);
/* 137 */           binder.putLocal("TotalRows", totalRows);
/*     */         }
/*     */       }
/* 140 */       else if (drset.isCopyAborted())
/*     */       {
/* 142 */         drset.last();
/* 143 */         drset.deleteCurrentRow();
/* 144 */         binder.putLocal("HasMoreRows", "1");
/*     */       }
/* 146 */       binder.addResultSet("SearchResults", drset);
/*     */ 
/* 148 */       this.m_resultBinder = binder;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 152 */       this.m_context.setCachedObject("SearchException", e);
/* 153 */       return CommonSearchConnection.createErrorMsg(e, "csSearchUnableToRetrieveSearchResult");
/*     */     }
/* 155 */     return null;
/*     */   }
/*     */ 
/*     */   public String getResult()
/*     */   {
/* 162 */     if (this.m_result == null)
/*     */     {
/* 164 */       this.m_result = getString(this.m_resultBinder);
/*     */     }
/* 166 */     return this.m_result;
/*     */   }
/*     */ 
/*     */   public DataBinder getResultAsBinder()
/*     */   {
/* 172 */     return this.m_resultBinder;
/*     */   }
/*     */ 
/*     */   public boolean prepareUse(ExecutionContext ctxt)
/*     */   {
/* 178 */     super.prepareUse(ctxt);
/* 179 */     Workspace ws = (Workspace)ctxt.getCachedObject("Workspace");
/* 180 */     if (ws == null)
/*     */     {
/* 182 */       if (ctxt instanceof Service)
/*     */       {
/* 184 */         ws = ((Service)ctxt).getWorkspace();
/*     */       }
/* 186 */       if (ws == null)
/*     */       {
/* 188 */         return false;
/*     */       }
/*     */     }
/* 191 */     this.m_workspace = ws;
/* 192 */     return true;
/*     */   }
/*     */ 
/*     */   public String getString(DataBinder binder)
/*     */   {
/* 197 */     String result = null;
/*     */     try
/*     */     {
/* 200 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/* 201 */       binder.send(sw);
/* 202 */       result = sw.toStringRelease();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 207 */       Report.trace("search", null, e);
/*     */     }
/* 209 */     return result;
/*     */   }
/*     */ 
/*     */   protected void processResult(DataResultSet drset) throws DataException
/*     */   {
/* 214 */     DataBinder binder = new DataBinder();
/* 215 */     binder.addResultSet("SearchResults", drset);
/*     */ 
/* 217 */     FieldInfo fi = new FieldInfo();
/* 218 */     fi.m_name = "URL";
/* 219 */     fi.m_type = 6;
/* 220 */     Vector v = new IdcVector();
/* 221 */     v.addElement(fi);
/* 222 */     drset.mergeFieldsWithFlags(v, 2);
/*     */ 
/* 224 */     FileStoreProvider fileStore = null;
/*     */     try
/*     */     {
/* 227 */       fileStore = FileStoreProviderLoader.initFileStore(this.m_context);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 231 */       DataException de = new DataException("");
/* 232 */       SystemUtils.setExceptionCause(de, e);
/* 233 */       throw de;
/*     */     }
/*     */ 
/* 236 */     IdcDateFormat blDateFormat = drset.getDateFormat();
/* 237 */     Object dateObj = this.m_context.getLocaleResource(3);
/* 238 */     this.m_context.setCachedObject("UserDateFormat", blDateFormat);
/* 239 */     binder.m_blDateFormat = blDateFormat;
/*     */     try
/*     */     {
/* 242 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 244 */         String url = null;
/*     */         try
/*     */         {
/* 247 */           HashMap args = new HashMap();
/* 248 */           args.put("isLocationOnly", "1");
/* 249 */           binder.putLocal("RenditionId", "webViewableFile");
/* 250 */           IdcFileDescriptor d = fileStore.createDescriptor(binder, args, this.m_context);
/*     */ 
/* 252 */           args.put("useAbsolute", "0");
/* 253 */           url = fileStore.getClientURL(d, null, args, this.m_context);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 257 */           Report.warning("search", e, "csSearchUrlError", new Object[] { binder.getAllowMissing("dDocName") });
/*     */         }
/* 259 */         if (url == null)
/*     */         {
/* 262 */           url = LegacyDocumentPathBuilder.computeWebUrlDir(binder, false);
/*     */ 
/* 264 */           String fileName = binder.get("dDocName").toLowerCase();
/* 265 */           String webExt = binder.get("dWebExtension");
/* 266 */           if ((webExt != null) && (webExt.length() != 0))
/*     */           {
/* 268 */             fileName = fileName + "." + webExt;
/*     */           }
/*     */ 
/* 271 */           url = url + fileName;
/*     */         }
/* 273 */         drset.setCurrentValue(fi.m_index, url);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 278 */       this.m_context.setCachedObject("UserDateFormat", dateObj);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 284 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86052 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.DatabaseSearchImplementor
 * JD-Core Version:    0.5.4
 */