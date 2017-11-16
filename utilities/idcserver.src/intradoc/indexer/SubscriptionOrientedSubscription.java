/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.WorkQueueProcessor;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SubscriptionOrientedSubscription extends IndexerStepImpl
/*     */ {
/*     */   protected IndexerWorkObject m_data;
/*     */   protected int m_subscriptionCount;
/*     */   protected String[][] m_subscriptionTypes;
/*     */   protected WebChanges m_changes;
/*     */   protected Hashtable m_loadedProps;
/*     */ 
/*     */   public String doWork(String step, IndexerWorkObject data, boolean restart)
/*     */     throws ServiceException
/*     */   {
/*  44 */     this.m_data = data;
/*  45 */     this.m_changes = ((WebChanges)this.m_data.getCachedObject("WebChanges"));
/*  46 */     this.m_loadedProps = ((Hashtable)this.m_data.getCachedObject("LoadedProps"));
/*     */ 
/*  48 */     Hashtable scpInfo = (Hashtable)this.m_data.getCachedObject("SubscriptionInfo");
/*  49 */     if (scpInfo == null)
/*     */     {
/*  51 */       return "RecomputeSubscription";
/*     */     }
/*  53 */     Object tmp = scpInfo.get("SubscriptionCount");
/*  54 */     this.m_subscriptionCount = ((Integer)tmp).intValue();
/*  55 */     this.m_subscriptionTypes = ((String[][])(String[][])scpInfo.get("SubscriptionTypes"));
/*     */     try
/*     */     {
/*  59 */       doSubscription();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  65 */       Report.error(null, "!csSubscriptionFailed", e);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  69 */       String msg = LocaleUtils.encodeMessage("csSubscriptionFailed", t.getMessage());
/*  70 */       Report.error(null, msg, null);
/*     */     }
/*     */ 
/*  73 */     return "Success";
/*     */   }
/*     */ 
/*     */   public void doSubscription() throws DataException, ServiceException, IOException
/*     */   {
/*  78 */     Hashtable map = loadSubscriptionMap();
/*     */ 
/*  80 */     DataBinder binder = new DataBinder();
/*  81 */     binder.m_blDateFormat = LocaleResources.m_bulkloadFormat;
/*  82 */     ResultSet rset = this.m_data.m_workspace.createResultSet("QallSubscriptions", binder);
/*  83 */     FieldInfo[] infos = ResultSetUtils.createInfoList(rset, new String[] { "dSubscriptionType", "dSubscriptionID", "dSubscriptionAlias", "dSubscriptionAliasType", "dSubscriptionEmail" }, true);
/*     */ 
/*  86 */     int TYPE = infos[0].m_index;
/*  87 */     int ID = infos[1].m_index;
/*  88 */     binder.addResultSet("Subscriptions", rset);
/*     */ 
/*  95 */     int count = 0;
/*  96 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/*  98 */       if (count++ % 50 == 0)
/*     */       {
/* 100 */         this.m_data.reportProgress(0, "!csProcessingSubscriptions", count, this.m_subscriptionCount);
/*     */       }
/*     */ 
/* 103 */       String type = rset.getStringValue(TYPE);
/* 104 */       String id = rset.getStringValue(ID);
/* 105 */       String fullId = type + "," + id;
/*     */ 
/* 107 */       Vector v = (Vector)map.get(fullId);
/* 108 */       if (v == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 113 */       int size = v.size();
/* 114 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 116 */         Properties props = (Properties)v.elementAt(i);
/* 117 */         String dID = props.getProperty("dID");
/* 118 */         binder.setLocalData(props);
/*     */ 
/* 121 */         binder.putLocal("isSubscriptionBySubscription", "1");
/* 122 */         boolean[] isSendMail = { true };
/* 123 */         this.m_data.setCachedObject("isSendMail", isSendMail);
/*     */ 
/* 125 */         if ((PluginFilters.filter("prepareSubscriptionMail", null, binder, this.m_data) == -1) || (isSendMail[0] == 0)) {
/*     */           continue;
/*     */         }
/*     */ 
/* 129 */         WorkQueueProcessor.addTaskToWorkQueue(0, dID, binder);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public Hashtable loadSubscriptionMap()
/*     */     throws ServiceException
/*     */   {
/* 137 */     Hashtable map = new Hashtable();
/* 138 */     int size = this.m_changes.count('I');
/* 139 */     int scpCount = this.m_subscriptionTypes.length;
/* 140 */     Object en = this.m_changes.first('I');
/*     */ 
/* 142 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 144 */       WebChange change = this.m_changes.nextChange(en);
/* 145 */       if (change == null) continue; if (change.m_dReleaseState != 'R') {
/*     */         continue;
/*     */       }
/*     */ 
/* 149 */       Properties props = (Properties)this.m_loadedProps.get(change.m_dID);
/* 150 */       if (props == null)
/*     */       {
/* 152 */         String msg = LocaleUtils.encodeMessage("csSubscriptionUnableToLoadRevision", null, change.m_dID);
/*     */ 
/* 154 */         Report.error(null, msg, null);
/*     */       }
/*     */       else
/*     */       {
/* 158 */         for (int j = 0; j < scpCount; ++j)
/*     */         {
/* 160 */           Vector v = new IdcVector();
/* 161 */           String[] fields = this.m_subscriptionTypes[j];
/* 162 */           v.addElement(fields[0]);
/* 163 */           for (int k = 1; k < fields.length; ++k)
/*     */           {
/* 165 */             String value = props.getProperty(fields[k]);
/* 166 */             if (value == null)
/*     */             {
/* 168 */               value = "";
/*     */             }
/* 170 */             v.addElement(value);
/*     */           }
/*     */ 
/* 173 */           String key = StringUtils.createString(v, ',', '^');
/* 174 */           Vector list = (Vector)map.get(key);
/* 175 */           if (list == null)
/*     */           {
/* 177 */             list = new IdcVector();
/* 178 */             map.put(key, list);
/*     */           }
/* 180 */           list.addElement(props);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 185 */     this.m_data.setCachedObject("SubscriptionMap", map);
/*     */     try
/*     */     {
/* 188 */       if (PluginFilters.filter("postLoadSubscriptionMap", this.m_data.m_workspace, null, this.m_data) != 0)
/*     */       {
/* 191 */         map = (Hashtable)this.m_data.getCachedObject("SubscriptionMap");
/*     */       }
/*     */     }
/*     */     catch (DataException d)
/*     */     {
/* 196 */       throw new ServiceException(d);
/*     */     }
/*     */ 
/* 199 */     return map;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 204 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99306 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.SubscriptionOrientedSubscription
 * JD-Core Version:    0.5.4
 */