/*     */ package intradoc.apputilities.idcanalyze;
/*     */ 
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.IdcCounterUtils;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.FileStoreProviderHelper;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IdcAnalyzeUtils
/*     */ {
/*     */   public static Hashtable makeMap(String sourceName, Properties params, Workspace ws, String colKey, boolean isProps, boolean isVector)
/*     */     throws DataException, ServiceException
/*     */   {
/*  39 */     DataBinder binder = new DataBinder(params);
/*     */ 
/*  42 */     DataResultSet sqlSet = SharedObjects.getTable("IdcAnalyzeDataSources");
/*  43 */     if ((sqlSet == null) || (sqlSet.findRow(0, sourceName) == null))
/*     */     {
/*  45 */       String msg = LocaleUtils.encodeMessage("csDataSourceScriptError", null, sourceName);
/*     */ 
/*  47 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/*  50 */     if (binder.getAllowMissing("primaryTable") == null)
/*     */     {
/*  52 */       String primaryTable = binder.get("table");
/*  53 */       int index = primaryTable.indexOf(",");
/*  54 */       if (index > 0)
/*     */       {
/*  56 */         primaryTable = primaryTable.substring(0, index);
/*     */       }
/*  58 */       binder.putLocal("primaryTable", primaryTable);
/*     */     }
/*     */ 
/*  61 */     String sql = ResultSetUtils.getValue(sqlSet, "dataSource");
/*     */ 
/*  64 */     PageMerger pageMerger = null;
/*     */     try
/*     */     {
/*  67 */       ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/*  68 */       pageMerger = new PageMerger(binder, cxt);
/*  69 */       sql = pageMerger.evaluateScript(sql);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       String msg;
/*  79 */       if (pageMerger != null)
/*     */       {
/*  81 */         pageMerger.releaseAllTemporary();
/*     */       }
/*     */     }
/*     */ 
/*  85 */     ResultSet rset = ws.createResultSetSQL(sql);
/*  86 */     return makeMap(rset, colKey, isProps, isVector);
/*     */   }
/*     */ 
/*     */   public static Hashtable makeMap(ResultSet rset, String colKey, boolean isProps, boolean isVector)
/*     */     throws DataException
/*     */   {
/*  93 */     int index = -1;
/*     */ 
/*  95 */     if (rset == null)
/*     */     {
/*  97 */       return null;
/*     */     }
/*  99 */     if (colKey == null)
/*     */     {
/* 101 */       return null;
/*     */     }
/*     */ 
/* 104 */     index = ResultSetUtils.getIndexMustExist(rset, colKey);
/* 105 */     int numCols = rset.getNumFields();
/*     */ 
/* 107 */     Hashtable newMap = new Hashtable();
/* 108 */     for (; rset.isRowPresent(); rset.next())
/*     */     {
/* 110 */       String colName = rset.getStringValue(index);
/* 111 */       Properties newProps = null;
/* 112 */       Vector newRow = null;
/*     */ 
/* 114 */       if (isProps)
/*     */       {
/* 116 */         newProps = new Properties();
/* 117 */         for (int i = 0; i < numCols; ++i)
/*     */         {
/* 120 */           FieldInfo finfo = new FieldInfo();
/* 121 */           rset.getIndexFieldInfo(i, finfo);
/* 122 */           String data = rset.getStringValue(i);
/* 123 */           newProps.put(finfo.m_name, data);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 128 */         newRow = new IdcVector();
/* 129 */         for (int i = 0; i < numCols; ++i)
/*     */         {
/* 131 */           newRow.addElement(rset.getStringValue(i));
/*     */         }
/*     */       }
/*     */ 
/* 135 */       if (isVector)
/*     */       {
/* 137 */         Vector v = (Vector)newMap.get(colName);
/* 138 */         if (v == null)
/*     */         {
/* 140 */           v = new IdcVector();
/* 141 */           newMap.put(colName, v);
/*     */         }
/* 143 */         if (isProps)
/*     */         {
/* 145 */           v.addElement(newProps);
/*     */         }
/*     */         else
/*     */         {
/* 149 */           v.addElement(newRow);
/*     */         }
/*     */ 
/*     */       }
/* 154 */       else if (isProps)
/*     */       {
/* 156 */         newMap.put(colName, newProps);
/*     */       }
/*     */       else
/*     */       {
/* 160 */         newMap.put(colName, newRow);
/*     */       }
/*     */     }
/*     */ 
/* 164 */     return newMap;
/*     */   }
/*     */ 
/*     */   public static int getMaxDid(Workspace ws)
/*     */     throws DataException
/*     */   {
/* 170 */     return (int)IdcCounterUtils.currentValue(ws, "RevID");
/*     */   }
/*     */ 
/*     */   public static int getRevisionCount(Workspace ws)
/*     */     throws DataException
/*     */   {
/* 176 */     ResultSet rset = ws.createResultSet("QIDCAnalyzeRevCount", null);
/* 177 */     return NumberUtils.parseInteger(rset.getStringValue(0), 0);
/*     */   }
/*     */ 
/*     */   public static boolean parsePropertiesFromPath(String path, Properties props, boolean isVault, FileStoreProviderHelper fileHelper)
/*     */   {
/* 183 */     int index = path.lastIndexOf("/");
/* 184 */     if (index < 0)
/*     */     {
/* 186 */       return false;
/*     */     }
/* 188 */     String fileName = path.substring(index + 1);
/* 189 */     props.put("FileName", fileName);
/*     */ 
/* 191 */     boolean result = true;
/* 192 */     if (!isVault)
/*     */     {
/*     */       try
/*     */       {
/* 196 */         result = fileHelper.parseDocInfoFromInternalPath(path, props, fileHelper.m_context);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 200 */         Report.trace("cmu", "IdcAnalyzeUtils.parsePropertiesFromPath: Error parsing " + path, e);
/*     */ 
/* 202 */         result = false;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 208 */       String r = path;
/*     */ 
/* 210 */       int curIndex = 0;
/*     */ 
/* 213 */       index = r.lastIndexOf('/');
/* 214 */       boolean hasDocInfo = false;
/* 215 */       curIndex = index;
/* 216 */       fileName = r.substring(index + 1);
/* 217 */       if (fileName.indexOf('/') < 0)
/*     */       {
/* 220 */         String ext = "";
/* 221 */         String docNamePart = fileName;
/* 222 */         index = fileName.lastIndexOf('.');
/* 223 */         if (index >= 0)
/*     */         {
/* 225 */           hasDocInfo = true;
/* 226 */           ext = fileName.substring(index + 1);
/* 227 */           docNamePart = fileName.substring(0, index);
/*     */         }
/* 229 */         props.put("dID", docNamePart);
/* 230 */         props.put("dExtension", ext);
/*     */       }
/*     */ 
/* 237 */       int offset = curIndex;
/* 238 */       String account = "";
/* 239 */       while (offset >= 0)
/*     */       {
/* 241 */         offset = r.lastIndexOf("/@", curIndex - 1);
/* 242 */         if (offset < 0)
/*     */           continue;
/* 244 */         String newAcct = r.substring(offset + 2, curIndex);
/* 245 */         if (account.length() > 0)
/*     */         {
/* 247 */           account = '/' + account;
/*     */         }
/* 249 */         account = newAcct + account;
/*     */ 
/* 251 */         curIndex = offset;
/*     */       }
/*     */ 
/* 254 */       props.put("dDocAccount", account);
/*     */ 
/* 258 */       index = r.lastIndexOf('/', curIndex - 1);
/* 259 */       if (index < 0)
/*     */       {
/* 261 */         return false;
/*     */       }
/* 263 */       String group = r.substring(index + 1, curIndex);
/* 264 */       props.put("dDocType", group);
/*     */ 
/* 266 */       props.put("hasDocInfo", (hasDocInfo) ? "1" : "");
/*     */     }
/*     */ 
/* 269 */     return result;
/*     */   }
/*     */ 
/*     */   public static String computeWeblayoutDir(Properties context)
/*     */   {
/* 274 */     String dir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/* 275 */     if (dir == null);
/* 279 */     return FileUtils.directorySlashes(dir);
/*     */   }
/*     */ 
/*     */   public static String computeVaultDir()
/*     */   {
/* 284 */     return LegacyDirectoryLocator.getVaultDirectory();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 289 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 67337 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idcanalyze.IdcAnalyzeUtils
 * JD-Core Version:    0.5.4
 */