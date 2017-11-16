/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.TableSerializationCallback;
/*     */ import intradoc.server.utils.TableModHistoryUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.Date;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ArchiveTableCallBack
/*     */   implements TableSerializationCallback
/*     */ {
/*     */   protected String[] m_timeStamps;
/*     */   protected String m_sourceID;
/*     */   protected boolean m_hasLimitedTimeResolution;
/*     */   protected int[] m_srcTimeIndexes;
/*     */   protected int[] m_dstTimeIndexes;
/*     */   protected int m_dstIDIndex;
/*     */   protected int m_srcIDIndex;
/*     */   protected boolean m_indexInitialized;
/*     */   protected boolean m_isInitTSErrorReported;
/*     */   protected boolean m_isInitIDErrorReported;
/*     */   protected String m_table;
/*     */   protected String m_constraints;
/*     */   protected Vector m_constraintKeys;
/*     */   protected String m_deletePKTypes;
/*     */   protected ExecutionContext m_context;
/*     */   protected Workspace m_workspace;
/*     */   protected Vector m_filters;
/*     */ 
/*     */   public ArchiveTableCallBack()
/*     */   {
/*  33 */     this.m_timeStamps = null;
/*  34 */     this.m_sourceID = null;
/*  35 */     this.m_hasLimitedTimeResolution = false;
/*     */ 
/*  37 */     this.m_srcTimeIndexes = null;
/*  38 */     this.m_dstTimeIndexes = null;
/*  39 */     this.m_dstIDIndex = -1;
/*  40 */     this.m_srcIDIndex = -1;
/*     */ 
/*  42 */     this.m_indexInitialized = false;
/*  43 */     this.m_isInitTSErrorReported = false;
/*  44 */     this.m_isInitIDErrorReported = false;
/*  45 */     this.m_table = null;
/*     */ 
/*  47 */     this.m_constraints = "";
/*  48 */     this.m_constraintKeys = null;
/*  49 */     this.m_deletePKTypes = null;
/*     */ 
/*  51 */     this.m_context = null;
/*  52 */     this.m_workspace = null;
/*     */ 
/*  54 */     this.m_filters = new IdcVector();
/*     */   }
/*     */ 
/*     */   public void init(String table, String[] timeStamps, String srcIDCol, ExecutionContext cxt, Workspace ws, boolean hasLimitedTimeResolution)
/*     */   {
/*  60 */     this.m_table = table;
/*  61 */     this.m_timeStamps = timeStamps;
/*  62 */     this.m_sourceID = srcIDCol;
/*  63 */     this.m_indexInitialized = false;
/*     */ 
/*  65 */     this.m_context = cxt;
/*  66 */     this.m_workspace = ws;
/*  67 */     this.m_hasLimitedTimeResolution = hasLimitedTimeResolution;
/*     */   }
/*     */ 
/*     */   public void addFilter(Vector row)
/*     */   {
/*  72 */     this.m_filters.addElement(row);
/*     */   }
/*     */ 
/*     */   public boolean allowModifyAndDelete(ResultSet srcRset, ResultSet dstRset, boolean isDelete)
/*     */   {
/*  77 */     boolean allowModifyAndDelete = false;
/*  78 */     boolean continueCheck = true;
/*  79 */     if (!this.m_indexInitialized)
/*     */     {
/*  81 */       continueCheck = initIndexes(srcRset, dstRset);
/*     */     }
/*  83 */     if (continueCheck)
/*     */     {
/*  85 */       boolean timeCheckOk = checkTimeStamps(srcRset, dstRset);
/*  86 */       boolean sourceCheckOk = checkSourceID(srcRset, dstRset);
/*  87 */       allowModifyAndDelete = (timeCheckOk) && (sourceCheckOk);
/*  88 */       if ((allowModifyAndDelete) && (isDelete) && (this.m_filters.size() > 0))
/*     */       {
/*  90 */         allowModifyAndDelete = checkFilters(srcRset, dstRset);
/*     */       }
/*     */     }
/*  93 */     if ((!allowModifyAndDelete) && (srcRset instanceof ImportTableDataResultSet))
/*     */     {
/*  95 */       ((ImportTableDataResultSet)srcRset).markRowSkipped();
/*     */     }
/*  97 */     if (!allowModifyAndDelete)
/*     */     {
/* 100 */       Report.appInfo("archiver", null, "!Skip importing Row: " + ((DataResultSet)srcRset).getCurrentRowValues() + " in Table " + this.m_table + ".", null);
/*     */     }
/*     */ 
/* 105 */     return allowModifyAndDelete;
/*     */   }
/*     */ 
/*     */   protected boolean checkTimeStamps(ResultSet srcRset, ResultSet dstRset)
/*     */   {
/* 110 */     if (this.m_timeStamps == null)
/*     */     {
/* 112 */       return true;
/*     */     }
/* 114 */     Date latestDate = null;
/* 115 */     boolean isNewerRecord = false;
/* 116 */     for (int i = 0; i < this.m_timeStamps.length; ++i)
/*     */     {
/* 118 */       if (this.m_srcTimeIndexes[i] == -1) continue; if (this.m_dstTimeIndexes[i] == -1)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 125 */         Date srcDate = srcRset.getDateValue(this.m_srcTimeIndexes[i]);
/* 126 */         Date dstDate = dstRset.getDateValue(this.m_dstTimeIndexes[i]);
/* 127 */         Timestamp tsDstDate = new Timestamp(dstDate.getTime());
/* 128 */         int latestDateNanos = 0;
/* 129 */         if (latestDate == null)
/*     */         {
/* 131 */           latestDate = dstDate;
/* 132 */           latestDateNanos = tsDstDate.getNanos();
/*     */         }
/* 134 */         int dstNanos = tsDstDate.getNanos();
/* 135 */         if ((dstDate.getTime() + dstNanos > latestDate.getTime() + latestDateNanos) && (((!this.m_hasLimitedTimeResolution) || (dstDate.getTime() - latestDate.getTime() > 2L))))
/*     */         {
/* 138 */           latestDate = dstDate;
/* 139 */           isNewerRecord = false;
/*     */         }
/* 141 */         if ((srcDate.getTime() > latestDate.getTime() + latestDateNanos) && (((!this.m_hasLimitedTimeResolution) || (srcDate.getTime() - latestDate.getTime() > 2L))))
/*     */         {
/* 144 */           latestDate = srcDate;
/* 145 */           isNewerRecord = true;
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 150 */         Report.trace("archiver", null, e);
/* 151 */         return false;
/*     */       }
/*     */     }
/* 154 */     if (!isNewerRecord)
/*     */     {
/* 156 */       Report.trace("archiver", "Skipping current row, not a new record. " + srcRset.getFieldName(0) + " = " + srcRset.getStringValue(0), null);
/*     */     }
/*     */ 
/* 159 */     return isNewerRecord;
/*     */   }
/*     */ 
/*     */   protected boolean checkSourceID(ResultSet srcRset, ResultSet dstRset)
/*     */   {
/* 165 */     if ((this.m_sourceID == null) || (this.m_sourceID.length() == 0))
/*     */     {
/* 167 */       return true;
/*     */     }
/* 169 */     boolean failed = false;
/* 170 */     String dstID = dstRset.getStringValue(this.m_dstIDIndex);
/* 171 */     String srcID = srcRset.getStringValue(this.m_srcIDIndex);
/* 172 */     if ((dstID == null) || (srcID == null) || (!srcID.equalsIgnoreCase(dstID)))
/*     */     {
/* 174 */       failed = true;
/*     */     }
/*     */ 
/* 177 */     if (failed)
/*     */     {
/* 179 */       Report.trace("archiver", "Skipping current row. Current Row's sourceID: " + dstID + " does not match importing sourceID: " + srcID + ". " + srcRset.getFieldName(0) + " = " + srcRset.getStringValue(0), null);
/*     */ 
/* 182 */       return false;
/*     */     }
/* 184 */     return true;
/*     */   }
/*     */ 
/*     */   protected boolean checkFilters(ResultSet srcRset, ResultSet dstRset)
/*     */   {
/* 193 */     int size = this.m_filters.size();
/*     */ 
/* 195 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 197 */       Vector row = (Vector)this.m_filters.elementAt(i);
/*     */ 
/* 199 */       String src = (String)row.elementAt(0);
/* 200 */       int dot = src.indexOf(46);
/* 201 */       String srcCol = src.substring(dot + 1);
/* 202 */       FieldInfo fi = new FieldInfo();
/* 203 */       srcRset.getFieldInfo(srcCol, fi);
/* 204 */       if (fi.m_index < 0)
/*     */       {
/* 206 */         Report.trace("archiver", "Skipping current row. Column " + srcCol + " doesn't exist", null);
/*     */ 
/* 208 */         return false;
/*     */       }
/*     */ 
/* 211 */       String value = srcRset.getStringValue(fi.m_index);
/*     */ 
/* 213 */       String childCol = (String)row.elementAt(1);
/* 214 */       dot = childCol.indexOf(46);
/* 215 */       String table = childCol.substring(0, dot);
/* 216 */       String query = "SELECT * FROM " + table + " WHERE " + childCol + " = '" + value + "'";
/*     */       try
/*     */       {
/* 221 */         ResultSet rset = this.m_workspace.createResultSetSQL(query);
/* 222 */         if (rset.isRowPresent())
/*     */         {
/* 224 */           Report.trace("archiver", "Skipping current row. Child table " + table + " contains row with " + childCol + " = '" + value + "'", null);
/*     */ 
/* 226 */           return false;
/*     */         }
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 231 */         Report.trace("archiver", "Error in checkFilters:" + e.getMessage(), null);
/* 232 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 236 */     return true;
/*     */   }
/*     */ 
/*     */   public void postModification(Workspace ws, String table, ResultSet drset, String constraintKeys, long results, boolean isDelete)
/*     */     throws DataException
/*     */   {
/* 242 */     if ((constraintKeys == null) || (constraintKeys.length() == 0) || (!isDelete) || (results == 0L))
/*     */     {
/* 245 */       return;
/*     */     }
/*     */ 
/* 249 */     FieldInfo fi = new FieldInfo();
/*     */ 
/* 251 */     int timeIndex = -1;
/* 252 */     if (this.m_srcTimeIndexes != null)
/*     */     {
/* 254 */       timeIndex = this.m_srcTimeIndexes[1];
/* 255 */       if (timeIndex < 0)
/*     */       {
/* 257 */         timeIndex = this.m_srcTimeIndexes[0];
/*     */       }
/*     */     }
/*     */ 
/* 261 */     int srcId = -1;
/* 262 */     if (this.m_srcIDIndex > -1)
/*     */     {
/* 264 */       srcId = this.m_srcIDIndex;
/*     */     }
/* 266 */     else if (drset.getFieldInfo("schSourceID", fi))
/*     */     {
/* 268 */       srcId = fi.m_index;
/*     */     }
/*     */ 
/* 271 */     TableModHistoryUtils.insertEntry(ws, table, (DataResultSet)drset, constraintKeys, srcId, timeIndex, true, true);
/*     */   }
/*     */ 
/*     */   protected boolean initIndexes(ResultSet srcRset, ResultSet dstRset)
/*     */   {
/* 279 */     this.m_indexInitialized = true;
/* 280 */     if (this.m_timeStamps != null)
/*     */     {
/* 282 */       this.m_srcTimeIndexes = new int[this.m_timeStamps.length];
/* 283 */       this.m_dstTimeIndexes = new int[this.m_timeStamps.length];
/* 284 */       for (int i = 0; i < this.m_timeStamps.length; ++i)
/*     */       {
/* 286 */         FieldInfo fi = new FieldInfo();
/* 287 */         srcRset.getFieldInfo(this.m_timeStamps[i], fi);
/* 288 */         this.m_srcTimeIndexes[i] = fi.m_index;
/* 289 */         dstRset.getFieldInfo(this.m_timeStamps[i], fi);
/* 290 */         this.m_dstTimeIndexes[i] = fi.m_index;
/* 291 */         if ((this.m_srcTimeIndexes[i] >= 0) && (this.m_dstTimeIndexes[i] >= 0)) {
/*     */           continue;
/*     */         }
/* 294 */         if (!this.m_isInitTSErrorReported)
/*     */         {
/* 296 */           this.m_isInitTSErrorReported = true;
/* 297 */           Report.trace("archiver", "Skipping current Row. Unable to initialize index for " + this.m_timeStamps[i], null);
/*     */         }
/*     */ 
/* 300 */         this.m_indexInitialized = false;
/*     */       }
/*     */     }
/*     */ 
/* 304 */     if ((this.m_sourceID != null) && (this.m_sourceID.length() != 0))
/*     */     {
/* 306 */       FieldInfo fi = new FieldInfo();
/* 307 */       dstRset.getFieldInfo(this.m_sourceID, fi);
/* 308 */       this.m_dstIDIndex = fi.m_index;
/* 309 */       fi = new FieldInfo();
/* 310 */       srcRset.getFieldInfo(this.m_sourceID, fi);
/* 311 */       this.m_srcIDIndex = fi.m_index;
/* 312 */       if ((this.m_dstIDIndex < 0) || (this.m_srcIDIndex < 0))
/*     */       {
/* 314 */         if (!this.m_isInitIDErrorReported)
/*     */         {
/* 316 */           this.m_isInitIDErrorReported = true;
/* 317 */           Report.trace("archiver", "Skipping current Row. Unable to initialize index for " + this.m_sourceID, null);
/*     */         }
/*     */ 
/* 320 */         this.m_indexInitialized = false;
/*     */       }
/*     */     }
/* 323 */     return this.m_indexInitialized;
/*     */   }
/*     */ 
/*     */   public void handleImportError(Workspace ws, String table, ResultSet drset, boolean isDelete, Throwable t)
/*     */   {
/* 328 */     Report.trace("archiver", "Handling import error for current row. Error:" + t.getMessage(), null);
/* 329 */     if (!drset instanceof ImportTableDataResultSet)
/*     */       return;
/* 331 */     ((ImportTableDataResultSet)drset).markRowSkipped();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 337 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96399 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiveTableCallBack
 * JD-Core Version:    0.5.4
 */