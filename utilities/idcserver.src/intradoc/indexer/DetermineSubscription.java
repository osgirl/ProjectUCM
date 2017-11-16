/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DetermineSubscription extends IndexerStepImpl
/*     */ {
/*     */   protected IndexerWorkObject m_data;
/*     */   protected Hashtable m_loadedProps;
/*     */   protected WebChanges m_changes;
/*     */   protected IndexerState m_state;
/*     */   protected String m_dbSymbol;
/*     */   protected boolean m_hasReloadedRevisionData;
/*     */   protected int m_subscriptionCount;
/*     */   protected int m_docCount;
/*     */   protected String[][] m_subscriptionTypes;
/*     */ 
/*     */   public String doWork(String step, IndexerWorkObject data, boolean restart)
/*     */     throws ServiceException
/*     */   {
/*  52 */     this.m_data = data;
/*  53 */     this.m_changes = ((WebChanges)this.m_data.getCachedObject("WebChanges"));
/*  54 */     this.m_state = ((IndexerState)this.m_data.getCachedObject("IndexerState"));
/*  55 */     this.m_dbSymbol = this.m_state.getFinishedSymbol();
/*  56 */     this.m_hasReloadedRevisionData = false;
/*     */     try
/*     */     {
/*  60 */       if (PluginFilters.filter("updateSubscriptionInfo", this.m_data.m_workspace, null, null) != 0)
/*     */       {
/*  63 */         return "Continue";
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  68 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  73 */       this.m_subscriptionTypes = loadSubscriptionTypes();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  77 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/*  80 */     if ((this.m_subscriptionTypes == null) || (this.m_subscriptionTypes.length == 0))
/*     */     {
/*  82 */       return "Continue";
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  87 */       ResultSet rset = this.m_data.m_workspace.createResultSetSQL("SELECT count(*) from Subscription");
/*  88 */       if (rset.isRowPresent())
/*     */       {
/*  90 */         String ttl = rset.getStringValue(0);
/*  91 */         this.m_subscriptionCount = Integer.parseInt(ttl);
/*     */       }
/*  93 */       rset = null;
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  97 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 100 */     if (this.m_subscriptionCount == 0)
/*     */     {
/* 102 */       return "Continue";
/*     */     }
/*     */ 
/* 105 */     this.m_docCount = this.m_changes.count('I');
/*     */ 
/* 107 */     this.m_loadedProps = ((Hashtable)this.m_data.getCachedObject("LoadedProps"));
/* 108 */     if (this.m_loadedProps == null)
/*     */     {
/* 110 */       this.m_loadedProps = new Hashtable();
/* 111 */       this.m_data.setCachedObject("LoadedProps", this.m_loadedProps);
/* 112 */       loadRevisionData();
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 118 */       if (PluginFilters.filter("updateLoadedProperties", this.m_data.m_workspace, null, this.m_data) != 0)
/*     */       {
/* 121 */         return "Continue";
/*     */       }
/*     */     }
/*     */     catch (DataException d)
/*     */     {
/* 126 */       throw new ServiceException(d);
/*     */     }
/*     */ 
/* 130 */     Hashtable scpInfo = new Hashtable();
/* 131 */     this.m_data.setCachedObject("SubscriptionInfo", scpInfo);
/* 132 */     scpInfo.put("SubscriptionCount", new Integer(this.m_subscriptionCount));
/* 133 */     scpInfo.put("SubscriptionTypes", this.m_subscriptionTypes);
/*     */ 
/* 135 */     return determineSubscriptionMethod();
/*     */   }
/*     */ 
/*     */   public String determineSubscriptionMethod()
/*     */   {
/* 140 */     String rc = SharedObjects.getEnvironmentValue("SubscriptionMethod");
/* 141 */     if (rc != null)
/*     */     {
/* 143 */       return rc;
/*     */     }
/* 145 */     rc = "SubscriptionOriented";
/* 146 */     int minCount = SharedObjects.getEnvironmentInt("BySubscriptionMin", 5);
/* 147 */     int factor = SharedObjects.getEnvironmentInt("BySubscriptionFactor", 50);
/*     */ 
/* 149 */     if ((minCount > this.m_docCount) || (this.m_subscriptionCount > this.m_docCount * factor * this.m_subscriptionTypes.length))
/*     */     {
/* 152 */       rc = "DocumentOriented";
/*     */     }
/*     */ 
/* 155 */     return rc;
/*     */   }
/*     */ 
/*     */   public void loadRevisionData() throws ServiceException
/*     */   {
/* 160 */     Report.trace("indexer", "loadRevisionData -- Loading all revision data for doing subscription", null);
/* 161 */     DataBinder binder = new DataBinder();
/* 162 */     binder.m_blDateFormat = LocaleResources.m_bulkloadFormat;
/* 163 */     binder.putLocal("dIndexerState", this.m_dbSymbol);
/*     */     try
/*     */     {
/* 167 */       ResultSet rset = this.m_data.m_workspace.createResultSet("INDEXER-QUERY-ALL", binder);
/* 168 */       rset.setDateFormat(LocaleResources.m_bulkloadFormat);
/*     */ 
/* 171 */       FieldInfo[] infos = ResultSetUtils.createInfoList(rset, new String[] { "dID", "dRevClassID" }, true);
/*     */ 
/* 173 */       int ID_INDEX = infos[0].m_index;
/* 174 */       int REVCLASS_INDEX = infos[1].m_index;
/*     */ 
/* 176 */       infos = new FieldInfo[rset.getNumFields()];
/* 177 */       for (int i = 0; i < infos.length; ++i)
/*     */       {
/* 179 */         infos[i] = new FieldInfo();
/* 180 */         rset.getIndexFieldInfo(i, infos[i]);
/*     */       }
/*     */ 
/* 183 */       int count = 0;
/* 184 */       int total = this.m_changes.count('I');
/* 185 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/* 187 */         if (count % 100 == 0)
/*     */         {
/* 189 */           this.m_data.reportProgress(1, "!csLoadingRevisionData", count, total);
/*     */         }
/*     */ 
/* 192 */         ++count;
/*     */ 
/* 194 */         String id = rset.getStringValue(ID_INDEX);
/* 195 */         String revClassId = rset.getStringValue(REVCLASS_INDEX);
/* 196 */         WebChange change = this.m_changes.find(revClassId);
/* 197 */         if ((change == null) || (change.m_change != 'I') || (!id.equals(change.m_dID)))
/*     */         {
/* 199 */           ++total;
/*     */         }
/*     */         else
/*     */         {
/* 203 */           Properties props = new Properties();
/* 204 */           for (int i = 0; i < infos.length; ++i)
/*     */           {
/* 206 */             String value = rset.getStringValue(i);
/* 207 */             props.put(infos[i].m_name, value);
/*     */           }
/* 209 */           this.m_loadedProps.put(id, props);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 233 */       Report.error(null, "!csSubscriptionUnableToLoad", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String[][] loadSubscriptionTypes() throws DataException
/*     */   {
/* 239 */     DataResultSet drset = SharedObjects.getTable("SubscriptionTypes");
/* 240 */     if (drset == null)
/*     */     {
/* 242 */       return (String[][])null;
/*     */     }
/*     */ 
/* 245 */     Vector v = new IdcVector();
/*     */ 
/* 247 */     FieldInfo[] infos = ResultSetUtils.createInfoList(drset, new String[] { "scpFields", "scpEnabled", "scpType" }, true);
/*     */ 
/* 249 */     int FIELDS = infos[0].m_index;
/* 250 */     int ENABLED = infos[1].m_index;
/* 251 */     int TYPE = infos[2].m_index;
/*     */ 
/* 254 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 256 */       String enabled = drset.getStringValue(ENABLED);
/* 257 */       if (!StringUtils.convertToBool(enabled, false)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 261 */       String fields = drset.getStringValue(FIELDS);
/* 262 */       Vector tmp = StringUtils.parseArray(fields, ',', ',');
/* 263 */       int size = tmp.size();
/* 264 */       if (size == 0)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 269 */       String[] fieldList = new String[size + 1];
/* 270 */       fieldList[0] = drset.getStringValue(TYPE);
/* 271 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 273 */         fieldList[(i + 1)] = ((String)tmp.elementAt(i));
/*     */       }
/* 275 */       v.addElement(fieldList);
/*     */     }
/*     */ 
/* 278 */     int size = v.size();
/* 279 */     String[][] types = new String[size][];
/* 280 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 282 */       types[i] = ((String[])(String[])v.elementAt(i));
/*     */     }
/*     */ 
/* 285 */     return types;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 290 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98371 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.DetermineSubscription
 * JD-Core Version:    0.5.4
 */