/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ArchiveCollections extends DataResultSet
/*     */ {
/*  33 */   public static String m_tableName = "ArchiveCollections";
/*  34 */   protected static int m_maxID = 0;
/*  35 */   public static int m_idIndex = 1;
/*     */ 
/*  37 */   public static final String[] COLUMNS = { "IDC_ID", "IDC_Name", "aCollectionLocation", "aCollectionExportLocation", "aVaultDir", "aWeblayoutDir" };
/*     */ 
/*  42 */   protected Hashtable m_idMap = null;
/*  43 */   protected Hashtable m_nameMap = null;
/*     */ 
/*     */   public ArchiveCollections()
/*     */   {
/*  47 */     super(COLUMNS);
/*  48 */     this.m_idMap = new Hashtable();
/*  49 */     this.m_nameMap = new Hashtable();
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  55 */     DataResultSet rset = new ArchiveCollections();
/*  56 */     initShallow(rset);
/*     */ 
/*  58 */     return rset;
/*     */   }
/*     */ 
/*     */   public void initShallow(DataResultSet rset)
/*     */   {
/*  64 */     super.initShallow(rset);
/*  65 */     ArchiveCollections ac = (ArchiveCollections)rset;
/*  66 */     ac.m_idMap = this.m_idMap;
/*  67 */     ac.m_nameMap = this.m_nameMap;
/*     */   }
/*     */ 
/*     */   public boolean load(DataBinder data) throws DataException
/*     */   {
/*  72 */     String str = data.getLocal("MaxID");
/*     */ 
/*  74 */     if (str != null)
/*     */     {
/*     */       try
/*     */       {
/*  78 */         m_maxID = Integer.parseInt(str);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*  83 */         Report.appError("archiver", null, "!apArchiveCollectionsInvalidMaxId", e);
/*     */ 
/*  85 */         return false;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*  90 */       m_maxID = 0;
/*  91 */       data.putLocal("MaxID", "0");
/*     */     }
/*     */ 
/*  94 */     DataResultSet rset = (DataResultSet)data.getResultSet(m_tableName);
/*  95 */     boolean isChanged = load(rset, true);
/*     */ 
/*  97 */     data.addResultSet(m_tableName, this);
/*  98 */     SharedObjects.putTable(m_tableName, this);
/*     */ 
/* 100 */     return isChanged;
/*     */   }
/*     */ 
/*     */   public boolean load(DataResultSet rset, boolean isCreateDefault)
/*     */     throws DataException
/*     */   {
/* 106 */     this.m_values = createNewResultSetList(32);
/* 107 */     this.m_currentRow = 0;
/* 108 */     this.m_numRows = 0;
/*     */ 
/* 110 */     this.m_idMap = new Hashtable();
/* 111 */     this.m_nameMap = new Hashtable();
/*     */ 
/* 113 */     if (rset != null)
/*     */     {
/* 116 */       merge("IDC_ID", rset, false);
/*     */     }
/*     */ 
/* 119 */     boolean isChanged = checkAndUpgradeCollections(rset);
/* 120 */     if (isCreateDefault)
/*     */     {
/* 123 */       Vector values = new IdcVector();
/* 124 */       int size = this.m_fieldList.size();
/* 125 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 127 */         FieldInfo info = (FieldInfo)this.m_fieldList.get(i);
/* 128 */         String name = info.m_name;
/* 129 */         String val = "";
/* 130 */         if (name.equals(COLUMNS[0]))
/*     */         {
/* 132 */           val = "0";
/*     */         }
/*     */         else
/*     */         {
/* 136 */           String key = name;
/* 137 */           if (key.startsWith("a"))
/*     */           {
/* 139 */             key = info.m_name.substring(1);
/*     */           }
/* 141 */           val = SharedObjects.getEnvironmentValue(key);
/*     */         }
/*     */ 
/* 144 */         if (val == null)
/*     */         {
/* 146 */           String msg = LocaleUtils.encodeMessage("apUnableToFindTableColumn", null, "ArchiveCollections", name);
/*     */ 
/* 148 */           throw new DataException(msg);
/*     */         }
/* 150 */         values.addElement(val);
/*     */       }
/*     */ 
/* 154 */       Vector curValues = findRow(0, "0");
/* 155 */       if (curValues != null)
/*     */       {
/* 157 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 159 */           String v1 = (String)values.elementAt(i);
/* 160 */           String v2 = (String)curValues.elementAt(i);
/* 161 */           if (v1.equals(v2))
/*     */             continue;
/* 163 */           isChanged = true;
/* 164 */           deleteCurrentRow();
/* 165 */           break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 171 */       String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 172 */       int idcIndex = ResultSetUtils.getIndexMustExist(this, "IDC_Name");
/*     */ 
/* 174 */       Vector idcValues = findRow(idcIndex, idcName);
/* 175 */       while (idcValues != null)
/*     */       {
/* 177 */         deleteCurrentRow();
/* 178 */         isChanged = true;
/* 179 */         idcValues = findRow(idcIndex, idcName);
/*     */       }
/*     */ 
/* 182 */       if (rset == null)
/*     */       {
/* 184 */         isChanged = true;
/* 185 */         addRow(values);
/*     */       }
/* 187 */       else if (isChanged)
/*     */       {
/* 189 */         insertRowAt(values, 0);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 194 */     for (first(); isRowPresent(); next())
/*     */     {
/* 196 */       String idStr = getStringValue(0);
/* 197 */       int id = Integer.parseInt(idStr);
/* 198 */       if (id > m_maxID)
/*     */       {
/* 200 */         m_maxID = id;
/*     */       }
/*     */ 
/* 203 */       Vector elts = getCurrentRowValues();
/* 204 */       addRowInfo(id, elts);
/*     */     }
/*     */ 
/* 207 */     return isChanged;
/*     */   }
/*     */ 
/*     */   public boolean checkAndUpgradeCollections(DataResultSet drset) throws DataException
/*     */   {
/* 212 */     FieldInfo info = new FieldInfo();
/* 213 */     if ((drset == null) || (drset.getFieldInfo("aCollectionLocation", info)))
/*     */     {
/* 215 */       return false;
/*     */     }
/*     */ 
/* 219 */     int dirIndex = ResultSetUtils.getIndexMustExist(drset, "aCollectionDir");
/*     */ 
/* 221 */     info.m_name = "aCollectionLocation";
/* 222 */     info.m_type = 6;
/* 223 */     Vector infos = new IdcVector();
/* 224 */     infos.addElement(info);
/* 225 */     drset.mergeFieldsWithFlags(infos, 2);
/*     */ 
/* 227 */     int locIndex = info.m_index;
/* 228 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 230 */       String val = drset.getStringValue(dirIndex);
/* 231 */       drset.setCurrentValue(locIndex, val);
/*     */     }
/* 233 */     drset.removeFields(new String[] { "aCollectionDir" });
/*     */ 
/* 235 */     return true;
/*     */   }
/*     */ 
/*     */   public void addRowInfo(int id, Vector elts)
/*     */   {
/* 240 */     String name = (String)elts.elementAt(1);
/* 241 */     String collectionDir = (String)elts.elementAt(2);
/* 242 */     String exportDir = (String)elts.elementAt(3);
/* 243 */     String vaultDir = (String)elts.elementAt(4);
/* 244 */     String webDir = (String)elts.elementAt(5);
/*     */ 
/* 246 */     CollectionData data = new CollectionData(id, name, collectionDir, exportDir, vaultDir, webDir);
/* 247 */     this.m_idMap.put(String.valueOf(id), data);
/* 248 */     this.m_nameMap.put(name, data);
/*     */   }
/*     */ 
/*     */   public static synchronized int createID()
/*     */   {
/* 253 */     return ++m_maxID;
/*     */   }
/*     */ 
/*     */   public DataBinder makeData()
/*     */   {
/* 258 */     DataBinder binder = new DataBinder();
/*     */ 
/* 260 */     Properties props = new Properties();
/* 261 */     props.put("MaxID", String.valueOf(m_maxID));
/* 262 */     binder.setLocalData(props);
/*     */ 
/* 264 */     binder.addResultSet(m_tableName, this);
/*     */ 
/* 266 */     return binder;
/*     */   }
/*     */ 
/*     */   public void addRow(DataBinder binder) throws DataException
/*     */   {
/* 271 */     int id = createID();
/* 272 */     binder.putLocal("IDC_ID", String.valueOf(id));
/*     */ 
/* 274 */     Vector v = createRow(binder);
/* 275 */     super.addRow(v);
/*     */ 
/* 277 */     addRowInfo(id, v);
/*     */   }
/*     */ 
/*     */   public String getTableName()
/*     */   {
/* 285 */     return m_tableName;
/*     */   }
/*     */ 
/*     */   public CollectionData getCollectionData(int id)
/*     */   {
/* 290 */     return (CollectionData)this.m_idMap.get(String.valueOf(id));
/*     */   }
/*     */ 
/*     */   public CollectionData getCollectionData(String name)
/*     */   {
/* 295 */     return (CollectionData)this.m_nameMap.get(name);
/*     */   }
/*     */ 
/*     */   public String getLocation(String name)
/*     */   {
/* 300 */     CollectionData data = (CollectionData)this.m_nameMap.get(name);
/* 301 */     if (data == null)
/*     */     {
/* 303 */       String msg = LocaleUtils.encodeMessage("apUnableToFindCollection", null, name);
/*     */ 
/* 305 */       Report.appError("archiver", null, msg, null);
/* 306 */       return null;
/*     */     }
/*     */ 
/* 309 */     return data.m_location;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 314 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97046 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ArchiveCollections
 * JD-Core Version:    0.5.4
 */