/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.indexer.IndexerState;
/*     */ import intradoc.indexer.IndexerStepImpl;
/*     */ import intradoc.indexer.IndexerWorkObject;
/*     */ import intradoc.indexer.WebChanges;
/*     */ import intradoc.server.archive.ArchiveHandler;
/*     */ import intradoc.server.archive.ArchiveUtils;
/*     */ import intradoc.server.archive.ReplicationData;
/*     */ import intradoc.shared.ArchiveCollections;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IndexerReplication extends IndexerStepImpl
/*     */   implements ReportProgress
/*     */ {
/*     */   protected IndexerWorkObject m_data;
/*     */   protected IndexerState m_state;
/*     */   protected boolean m_isRestart;
/*     */   protected WebChanges m_changes;
/*     */   protected static final String m_filterName = "preIndexingStep";
/*  42 */   protected static final String[] m_dataSources = { "IndexerExportDelete", "IndexerExportInsert" };
/*     */ 
/*  48 */   protected static final String[] m_archiveState = { "AutoDelete", "AutoInsert" };
/*     */ 
/*  54 */   protected static final String[] m_isDeleteExport = { "1", "0" };
/*     */   protected String m_archiveName;
/*     */   protected String m_collectionName;
/*     */   protected CollectionData m_collectionData;
/*     */   protected String m_finishedState;
/*     */ 
/*     */   public IndexerReplication()
/*     */   {
/*  60 */     this.m_archiveName = null;
/*  61 */     this.m_collectionName = null;
/*  62 */     this.m_collectionData = null;
/*  63 */     this.m_finishedState = " ";
/*     */   }
/*     */ 
/*     */   protected void init(IndexerWorkObject data) throws ServiceException {
/*  67 */     if (this.m_data == null)
/*     */     {
/*  69 */       this.m_data = data;
/*  70 */       this.m_state = ((IndexerState)this.m_data.getCachedObject("IndexerState"));
/*  71 */       this.m_finishedState = this.m_state.getFinishedSymbol();
/*     */     }
/*  73 */     this.m_changes = ((WebChanges)data.getCachedObject("WebChanges"));
/*  74 */     this.m_changes.init(this.m_data);
/*     */   }
/*     */ 
/*     */   public void cleanUp(IndexerWorkObject data)
/*     */     throws ServiceException
/*     */   {
/*  82 */     init(data);
/*  83 */     this.m_state.freeFinishedSymbol("Replicated");
/*     */   }
/*     */ 
/*     */   public String doWork(String step, IndexerWorkObject data, boolean restart)
/*     */     throws ServiceException
/*     */   {
/*  90 */     init(data);
/*  91 */     this.m_isRestart = restart;
/*     */     try
/*     */     {
/*  95 */       doReplication();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  99 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 102 */     return "Success";
/*     */   }
/*     */ 
/*     */   public void doReplication()
/*     */     throws DataException, ServiceException
/*     */   {
/* 124 */     DataResultSet rset = SharedObjects.getTable(ReplicationData.m_tableNames[0]);
/* 125 */     if (rset == null)
/*     */     {
/* 127 */       String msg = LocaleUtils.encodeMessage("csUnableToFindTable", null, "RegisteredExporters");
/*     */ 
/* 129 */       createException(msg);
/*     */     }
/*     */ 
/* 132 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 133 */     if (idcName == null)
/*     */     {
/* 136 */       createException("!csInstanceNameNotDefined");
/*     */     }
/*     */ 
/* 139 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 141 */       String archiveLocation = rset.getStringValue(0);
/*     */ 
/* 144 */       int index = archiveLocation.indexOf(47);
/* 145 */       if (index < 0)
/*     */       {
/* 149 */         String msg = LocaleUtils.encodeMessage("csReplicationArchiveWrongFormat", null, archiveLocation);
/*     */ 
/* 151 */         createException(msg);
/* 152 */         return;
/*     */       }
/*     */ 
/* 155 */       String[] locData = ArchiveUtils.parseLocation(archiveLocation);
/* 156 */       this.m_collectionName = locData[0];
/* 157 */       this.m_archiveName = locData[1];
/*     */ 
/* 159 */       DataBinder archiveData = readArchiveProperties(this.m_collectionName, this.m_archiveName);
/* 160 */       if (archiveData == null)
/*     */       {
/* 163 */         String msg = LocaleUtils.encodeMessage("csReplicationUnableToRetrieve", null, archiveLocation);
/*     */ 
/* 165 */         Report.appError("archiver", null, msg, null);
/* 166 */         ReplicationData.registerExporter(this.m_collectionName, this.m_archiveName, false);
/*     */       }
/*     */       else
/*     */       {
/* 170 */         String expStr = archiveData.getLocal("aRegisteredExporters");
/* 171 */         Vector exporters = StringUtils.parseArray(expStr, ',', ',');
/* 172 */         doExport(archiveData, idcName, exporters);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean doExport(DataBinder binder, String idcName, Vector exporters) throws ServiceException
/*     */   {
/* 179 */     boolean isAuto = StringUtils.convertToBool(binder.getLocal("aIsAutomatedExport"), false);
/* 180 */     boolean isFound = false;
/* 181 */     int num = exporters.size();
/* 182 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 184 */       String exporter = (String)exporters.elementAt(i);
/* 185 */       if (!exporter.equals(idcName))
/*     */         continue;
/* 187 */       isFound = true;
/* 188 */       break;
/*     */     }
/*     */ 
/* 194 */     if ((!isAuto) || (!isFound))
/*     */     {
/* 197 */       String msg = LocaleUtils.encodeMessage("csReplicationInstanceRemoved", null, this.m_collectionName, this.m_archiveName);
/*     */ 
/* 199 */       Report.appError("archiver", null, msg, null);
/* 200 */       ReplicationData.registerExporter(this.m_collectionName, this.m_archiveName, false);
/* 201 */       return false;
/*     */     }
/*     */ 
/* 204 */     if (!isAuto)
/*     */     {
/* 206 */       return false;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 218 */       Object obj = ComponentClassFactory.createClassInstance("ArchiveHandler", "intradoc.server.archive.ArchiveHandler", "!csArchiveHandlerError");
/*     */ 
/* 220 */       ArchiveHandler archiver = (ArchiveHandler)obj;
/* 221 */       archiver.initObjects(this.m_data.m_workspace, this, null);
/*     */ 
/* 223 */       String extraWhereClause = "dIndexerState = '" + this.m_finishedState + "'";
/* 224 */       for (int i = 0; i < m_dataSources.length; ++i)
/*     */       {
/* 232 */         binder.putLocal("IDC_Name", this.m_collectionData.m_name);
/*     */ 
/* 235 */         binder.putLocal("aArchiveName", this.m_archiveName);
/*     */ 
/* 249 */         binder.putLocal("aDoReplace", "0");
/*     */ 
/* 252 */         binder.putLocal("IsExport", "1");
/*     */ 
/* 255 */         binder.putLocal("aState", m_archiveState[i]);
/* 256 */         binder.putLocal("aIsDeleteExport", m_isDeleteExport[i]);
/* 257 */         binder.putLocal("dataSource", m_dataSources[i]);
/* 258 */         binder.putLocal("extraWhereClause", extraWhereClause);
/* 259 */         archiver.doArchiving(binder, true);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 264 */       throw new ServiceException("!csReplicationError", e);
/*     */     }
/* 266 */     return true;
/*     */   }
/*     */ 
/*     */   public DataBinder readArchiveProperties(String collectionName, String archiveName)
/*     */     throws ServiceException
/*     */   {
/* 272 */     ArchiveCollections colls = (ArchiveCollections)SharedObjects.getTable("ArchiveCollections");
/*     */ 
/* 275 */     this.m_collectionData = colls.getCollectionData(this.m_collectionName);
/* 276 */     if (this.m_collectionData == null)
/*     */     {
/* 279 */       return null;
/*     */     }
/*     */ 
/* 282 */     return ArchiveUtils.readArchiveFile(this.m_collectionData.m_location, this.m_archiveName, true);
/*     */   }
/*     */ 
/*     */   public void createException(String msg)
/*     */     throws ServiceException
/*     */   {
/* 288 */     msg = LocaleUtils.encodeMessage("csReplicationError", msg);
/* 289 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public WebChanges getWebChanges()
/*     */   {
/* 294 */     return this.m_changes;
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, float amtDone, float max)
/*     */   {
/* 302 */     if ((this.m_data == null) || (max < 0.0F))
/*     */       return;
/* 304 */     this.m_data.reportProgress(type, "!csReplicating", amtDone, max);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 310 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IndexerReplication
 * JD-Core Version:    0.5.4
 */