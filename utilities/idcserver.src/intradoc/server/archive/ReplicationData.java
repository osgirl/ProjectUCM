/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ReplicationData
/*     */ {
/*  34 */   protected static long m_lastModified = -2L;
/*  35 */   public static Hashtable[] m_mappedLookup = null;
/*     */   public static final int EXPORTER = 0;
/*     */   public static final int IMPORTER = 1;
/*     */   public static final int TRANSFER = 2;
/*     */   public static final int QUEUED_IMPORT = 3;
/*  43 */   public static final String[] m_tableNames = { "RegisteredExporters", "RegisteredImporters", "AutomatedTransfers", "QueuedImporters" };
/*     */ 
/*  52 */   public static final String[] m_typeNames = { "exporter", "importer", "transfer", "queuedimport" };
/*     */ 
/*  60 */   public static final String[] COLUMNS = { "aArchiveLocation" };
/*     */ 
/*  65 */   public static final String[] TRANSFER_COLUMNS = { "aArchiveLocation", "aTargetArchive" };
/*     */ 
/*  71 */   public static final String[] QUEUED_COLUMNS = { "aArchiveLocation", "aImportLogonUser" };
/*     */ 
/*     */   public static void checkInit()
/*     */   {
/*  79 */     if (m_mappedLookup != null)
/*     */       return;
/*  81 */     m_mappedLookup = new Hashtable[m_tableNames.length];
/*  82 */     for (int i = 0; i < m_mappedLookup.length; ++i)
/*     */     {
/*  84 */       m_mappedLookup[i] = new Hashtable();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static int determineType(String typeName)
/*     */   {
/*  91 */     return StringUtils.findStringIndex(m_typeNames, typeName);
/*     */   }
/*     */ 
/*     */   public static Hashtable getAutomatedLookup(int type)
/*     */   {
/*  96 */     checkInit();
/*  97 */     return m_mappedLookup[type];
/*     */   }
/*     */ 
/*     */   public static boolean isQueued(int type)
/*     */   {
/* 102 */     return type == 3;
/*     */   }
/*     */ 
/*     */   public static boolean isTransfer(int type) {
/* 106 */     return type == 2;
/*     */   }
/*     */ 
/*     */   public static boolean isArchiverMonitorAutomated(int type)
/*     */   {
/* 112 */     return type != 0;
/*     */   }
/*     */ 
/*     */   public static void loadFromFile() throws ServiceException
/*     */   {
/* 117 */     checkInit();
/* 118 */     long ts = ArchiveUtils.checkReplicationFile();
/* 119 */     if (ts == m_lastModified)
/*     */       return;
/* 121 */     DataBinder binder = ArchiveUtils.readReplicationFile(true);
/* 122 */     loadFromDataBinder(binder);
/* 123 */     m_lastModified = ts;
/*     */   }
/*     */ 
/*     */   public static void loadFromDataBinder(DataBinder binder)
/*     */   {
/* 129 */     DataResultSet rset = null;
/* 130 */     for (int i = 0; i < m_tableNames.length; ++i)
/*     */     {
/* 132 */       String tableName = m_tableNames[i];
/* 133 */       rset = (DataResultSet)binder.getResultSet(m_tableNames[i]);
/* 134 */       if (rset == null)
/*     */       {
/* 136 */         rset = createAndAddTable(i, null);
/*     */       }
/* 138 */       SharedObjects.putTable(m_tableNames[i], rset);
/*     */ 
/* 140 */       if (tableName.equals("RegisteredExporters"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 146 */       Hashtable newAutomaters = new Hashtable();
/* 147 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/* 149 */         Properties props = rset.getCurrentRowProps();
/* 150 */         String loc = props.getProperty("aArchiveLocation");
/*     */ 
/* 152 */         AutomatedArchiveData data = (AutomatedArchiveData)newAutomaters.get(loc);
/* 153 */         if (data == null)
/*     */         {
/* 155 */           data = new AutomatedArchiveData(loc);
/*     */         }
/* 157 */         data.m_props = props;
/* 158 */         newAutomaters.put(loc, data);
/*     */       }
/*     */ 
/* 161 */       m_mappedLookup[i] = newAutomaters;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static DataResultSet createAndAddTable(int type, DataBinder binder)
/*     */   {
/* 167 */     DataResultSet rset = null;
/* 168 */     if (isTransfer(type))
/*     */     {
/* 170 */       rset = new DataResultSet(TRANSFER_COLUMNS);
/*     */     }
/* 172 */     else if (isQueued(type))
/*     */     {
/* 174 */       rset = new DataResultSet(QUEUED_COLUMNS);
/*     */     }
/*     */     else
/*     */     {
/* 178 */       rset = new DataResultSet(COLUMNS);
/*     */     }
/* 180 */     if (binder != null)
/*     */     {
/* 182 */       binder.addResultSet(m_tableNames[type], rset);
/*     */     }
/*     */ 
/* 185 */     return rset;
/*     */   }
/*     */ 
/*     */   public static void registerExporter(String collName, String archiveName, boolean isAdd)
/*     */     throws ServiceException
/*     */   {
/* 191 */     changeTable(0, collName, archiveName, isAdd);
/*     */   }
/*     */ 
/*     */   public static void registerImporter(String collName, String archiveName, boolean isAdd)
/*     */     throws ServiceException
/*     */   {
/* 202 */     changeTable(1, collName, archiveName, isAdd);
/*     */   }
/*     */ 
/*     */   public static void registerTranfer(String collName, String archiveName, String target, boolean isAdd)
/*     */     throws ServiceException
/*     */   {
/* 213 */     Vector v = new IdcVector();
/* 214 */     String archiveLocation = ArchiveUtils.buildLocation(collName, archiveName);
/* 215 */     v.addElement(archiveLocation);
/* 216 */     v.addElement(target);
/*     */ 
/* 218 */     Vector rows = new IdcVector();
/* 219 */     rows.addElement(v);
/* 220 */     changeTable(2, rows, isAdd);
/*     */   }
/*     */ 
/*     */   public static void registerQueuedImport(String collName, String archiveName, String loginName, boolean isAdd)
/*     */     throws ServiceException
/*     */   {
/* 226 */     Vector v = new IdcVector();
/* 227 */     String archiveLocation = ArchiveUtils.buildLocation(collName, archiveName);
/* 228 */     v.addElement(archiveLocation);
/* 229 */     v.addElement(loginName);
/*     */ 
/* 231 */     Vector rows = new IdcVector();
/* 232 */     rows.addElement(v);
/* 233 */     changeTable(3, rows, isAdd);
/*     */   }
/*     */ 
/*     */   public static void changeTable(int type, String idcName, String archiveName, boolean isAdd)
/*     */     throws ServiceException
/*     */   {
/* 239 */     Vector v = new IdcVector();
/* 240 */     String archiveLocation = ArchiveUtils.buildLocation(idcName, archiveName);
/* 241 */     v.addElement(archiveLocation);
/*     */ 
/* 243 */     Vector rows = new IdcVector();
/* 244 */     rows.addElement(v);
/* 245 */     changeTable(type, rows, isAdd);
/*     */   }
/*     */ 
/*     */   public static void changeTable(int type, Vector values, boolean isAdd)
/*     */     throws ServiceException
/*     */   {
/* 251 */     if ((values == null) || (values.size() == 0))
/*     */     {
/* 254 */       return;
/*     */     }
/*     */ 
/* 258 */     String dir = LegacyDirectoryLocator.getCollectionsDirectory();
/* 259 */     FileUtils.reserveDirectory(dir);
/*     */     try
/*     */     {
/* 262 */       DataBinder binder = ArchiveUtils.readReplicationFile(false);
/* 263 */       DataResultSet rset = (DataResultSet)binder.getResultSet(m_tableNames[type]);
/* 264 */       if (rset == null)
/*     */       {
/* 266 */         rset = createAndAddTable(type, binder);
/*     */       }
/* 268 */       boolean isSave = addOrDeleteRows(rset, values, isAdd);
/* 269 */       if (isSave)
/*     */       {
/* 271 */         ArchiveUtils.writeReplicationFile(binder);
/*     */       }
/* 273 */       loadFromDataBinder(binder);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 277 */       Report.appError("archive", null, "!csArchiverAutoDefFileError", e);
/*     */     }
/*     */     finally
/*     */     {
/* 281 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean addOrDeleteRows(DataResultSet rset, Vector rows, boolean isAdd)
/*     */     throws ServiceException
/*     */   {
/* 288 */     boolean hasChanged = false;
/* 289 */     int size = rows.size();
/* 290 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 292 */       Vector row = (Vector)rows.elementAt(i);
/* 293 */       String archiveLocation = (String)row.elementAt(i);
/* 294 */       Vector v = rset.findRow(0, archiveLocation);
/* 295 */       if (isAdd)
/*     */       {
/* 298 */         if (v == null)
/*     */         {
/* 300 */           rset.addRow(row);
/*     */         }
/*     */         else
/*     */         {
/* 304 */           rset.setRowValues(row, rset.getCurrentRow());
/*     */         }
/* 306 */         hasChanged = true;
/*     */       }
/*     */       else
/*     */       {
/* 310 */         if (v == null)
/*     */           continue;
/* 312 */         boolean isSave = rset.deleteCurrentRow();
/* 313 */         if (hasChanged)
/*     */           continue;
/* 315 */         hasChanged = isSave;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 321 */     return hasChanged;
/*     */   }
/*     */ 
/*     */   public static AutomatedArchiveData getReplicationData(String loc, int type)
/*     */   {
/* 326 */     return (AutomatedArchiveData)m_mappedLookup[type].get(loc);
/*     */   }
/*     */ 
/*     */   public static Vector[] getReplicatedArchives(String collName)
/*     */   {
/* 331 */     Vector[] v = new IdcVector[2];
/* 332 */     for (int i = 0; i < 2; ++i)
/*     */     {
/* 334 */       v[i] = new IdcVector();
/* 335 */       DataResultSet rset = SharedObjects.getTable(m_tableNames[i]);
/* 336 */       if (rset == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 340 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/* 342 */         String location = rset.getStringValue(0);
/* 343 */         String[] locData = ArchiveUtils.parseLocation(location);
/* 344 */         if (!locData[0].equalsIgnoreCase(collName))
/*     */           continue;
/* 346 */         v[i].addElement(locData[1]);
/*     */       }
/*     */     }
/*     */ 
/* 350 */     return v;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 355 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ReplicationData
 * JD-Core Version:    0.5.4
 */