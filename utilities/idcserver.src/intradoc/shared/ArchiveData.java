/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.StringReader;
/*     */ import java.util.Hashtable;
/*     */ 
/*     */ public class ArchiveData extends DataResultSet
/*     */ {
/*  31 */   protected String m_tableName = "ArchiveData";
/*  32 */   public static String[] COLUMNS = { "aArchiveName", "aArchiveDescription", "aArchiveData" };
/*     */ 
/*  37 */   protected Hashtable m_archiveData = null;
/*     */ 
/*     */   public ArchiveData()
/*     */   {
/*  42 */     super(COLUMNS);
/*     */ 
/*  44 */     this.m_archiveData = new Hashtable();
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  50 */     DataResultSet rset = new ArchiveData();
/*  51 */     initShallow(rset);
/*     */ 
/*  53 */     return rset;
/*     */   }
/*     */ 
/*     */   public void initShallow(DataResultSet rset)
/*     */   {
/*  59 */     super.initShallow(rset);
/*  60 */     ArchiveData aData = (ArchiveData)rset;
/*  61 */     aData.m_archiveData = this.m_archiveData;
/*     */   }
/*     */ 
/*     */   public void load(DataResultSet rset)
/*     */     throws DataException, IOException
/*     */   {
/*  67 */     loadEx(rset, true);
/*     */   }
/*     */ 
/*     */   public void loadEx(DataResultSet rset, boolean isGlobal)
/*     */     throws DataException, IOException
/*     */   {
/*  77 */     clear();
/*     */ 
/*  79 */     if (rset == null)
/*     */     {
/*  81 */       return;
/*     */     }
/*     */ 
/*  85 */     String[] keys = { "aArchiveName", "aArchiveData" };
/*  86 */     FieldInfo[] fields = ResultSetUtils.createInfoList(rset, keys, true);
/*     */ 
/*  89 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/*  91 */       String name = rset.getStringValue(fields[0].m_index);
/*  92 */       String data = rset.getStringValue(fields[1].m_index);
/*     */ 
/*  94 */       DataBinder binder = new DataBinder(true);
/*  95 */       BufferedReader br = new BufferedReader(new StringReader(data));
/*  96 */       binder.receive(br);
/*     */ 
/*  98 */       this.m_archiveData.put(name, binder);
/*  99 */       addRow(rset.getCurrentRowValues());
/*     */     }
/*     */ 
/* 102 */     if (!isGlobal)
/*     */       return;
/* 104 */     SharedObjects.putTable(this.m_tableName, this);
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 110 */     this.m_values = createNewResultSetList(32);
/* 111 */     this.m_currentRow = 0;
/* 112 */     this.m_numRows = 0;
/*     */ 
/* 114 */     this.m_archiveData = new Hashtable();
/*     */   }
/*     */ 
/*     */   public DataBinder getArchiveData(String name)
/*     */   {
/* 119 */     return (DataBinder)this.m_archiveData.get(name);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 124 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ArchiveData
 * JD-Core Version:    0.5.4
 */