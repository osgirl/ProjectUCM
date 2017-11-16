/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
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
/*     */ import intradoc.server.MailInfo;
/*     */ import intradoc.server.WorkQueueProcessor;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocumentOrientedSubscription extends IndexerStepImpl
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
/*  65 */       Report.error(null, "!csSubscriptionProcessError", e);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  69 */       Exception e = new Exception(t.getMessage());
/*  70 */       Report.error(null, "!csSubscriptionProcessError", e);
/*     */     }
/*     */ 
/*  73 */     return "Success";
/*     */   }
/*     */ 
/*     */   public void doSubscription() throws ServiceException
/*     */   {
/*  78 */     int size = this.m_changes.count('I');
/*  79 */     Object en = this.m_changes.first('I');
/*  80 */     DataBinder binder = new DataBinder();
/*  81 */     binder.m_blDateFormat = LocaleResources.m_bulkloadFormat;
/*  82 */     List extraSubList = new ArrayList();
/*     */ 
/*  84 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  86 */       if (i % 20 == 0)
/*     */       {
/*  88 */         this.m_data.reportProgress(0, "!csComputingSubscription", i, size);
/*     */       }
/*     */ 
/*  92 */       WebChange change = this.m_changes.nextChange(en);
/*  93 */       if (change == null) {
/*     */         continue;
/*     */       }
/*     */ 
/*  97 */       Properties props = (Properties)this.m_loadedProps.get(change.m_dID);
/*  98 */       if (props == null)
/*     */       {
/* 100 */         Report.error(null, LocaleUtils.encodeMessage("csSubscriptionUnableToLoadRevision", null, change.m_dID), null);
/*     */       }
/*     */       else
/*     */       {
/*     */         try
/*     */         {
/* 111 */           boolean skipDefault = false;
/* 112 */           Object[] params = { this, this.m_subscriptionTypes, change, props, new Boolean(skipDefault), extraSubList };
/*     */ 
/* 114 */           this.m_data.setCachedObject("checkSubscriptionValid:params", params);
/*     */ 
/* 116 */           if (PluginFilters.filter("checkSubscriptionValid", this.m_data.m_workspace, binder, this.m_data) != 0)
/*     */           {
/*     */             break label398;
/*     */           }
/*     */ 
/* 121 */           skipDefault = ((Boolean)params[4]).booleanValue();
/*     */ 
/* 123 */           if ((!skipDefault) && (change.m_dReleaseState != 'R'))
/*     */           {
/* 125 */             break label398:
/*     */           }
/*     */         }
/*     */         catch (DataException d)
/*     */         {
/* 130 */           throw new ServiceException(d);
/*     */         }
/*     */ 
/* 133 */         for (int j = 0; j < this.m_subscriptionTypes.length; ++j)
/*     */         {
/* 135 */           String[] type = this.m_subscriptionTypes[j];
/* 136 */           Vector v = new IdcVector();
/* 137 */           for (int k = 1; k < type.length; ++k)
/*     */           {
/* 139 */             String tmp = props.getProperty(type[k]);
/* 140 */             if ((tmp == null) || (tmp.length() == 0))
/*     */             {
/* 142 */               tmp = " ";
/*     */             }
/* 144 */             v.addElement(tmp);
/*     */           }
/*     */ 
/* 147 */           String id = StringUtils.createString(v, ',', '^');
/*     */ 
/* 150 */           props.put("dSubscriptionType", type[0]);
/* 151 */           props.put("dSubscriptionID", id);
/* 152 */           binder.setLocalData(props);
/*     */ 
/* 154 */           label398: doSubscription(binder, change);
/*     */         }
/*     */       }
/*     */     }
/* 158 */     for (int j = 0; j < extraSubList.size(); ++j)
/*     */     {
/* 160 */       Properties props = (Properties)extraSubList.get(j);
/* 161 */       binder.setLocalData(props);
/* 162 */       doSubscription(binder, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void doSubscription(DataBinder binder, WebChange change)
/*     */   {
/* 168 */     DataResultSet subs = new DataResultSet();
/*     */     try
/*     */     {
/* 171 */       ResultSet rset = this.m_data.m_workspace.createResultSet("QusersSubscribed", binder);
/*     */ 
/* 173 */       if ((rset == null) || (rset.isEmpty()))
/*     */       {
/*     */         try
/*     */         {
/* 179 */           if (PluginFilters.filter("runDifferentUsersSubscribedQuery", this.m_data.m_workspace, binder, this.m_data) != 0)
/*     */           {
/* 182 */             return;
/*     */           }
/*     */         }
/*     */         catch (DataException d)
/*     */         {
/* 187 */           throw new ServiceException(d);
/*     */         }
/*     */ 
/* 190 */         ResultSet usersScpSet = binder.getResultSet("usersScpSet");
/* 191 */         if ((usersScpSet != null) && (!usersScpSet.isEmpty()))
/*     */         {
/* 193 */           rset = usersScpSet;
/*     */         }
/* 195 */         binder.removeResultSet("usersScpSet");
/*     */       }
/*     */ 
/* 198 */       subs.copy(rset);
/* 199 */       this.m_data.releaseConnection(false);
/* 200 */       processSubscriptions(binder, subs);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 204 */       this.m_data.releaseConnection(false);
/*     */ 
/* 206 */       Report.error(null, LocaleUtils.encodeMessage("csSubscriptionUnableToAdd", null, binder.getAllowMissing("dDocName")), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void processSubscriptions(DataBinder binder, DataResultSet subs)
/*     */     throws DataException, ServiceException
/*     */   {
/* 214 */     String docID = binder.getLocal("dID");
/* 215 */     String docName = binder.getLocal("dDocName");
/*     */ 
/* 218 */     for (subs.first(); subs.isRowPresent(); subs.next())
/*     */     {
/* 220 */       boolean skipRow = false;
/* 221 */       FieldInfo[] subsInfo = ResultSetUtils.createInfoList(subs, MailInfo.FIELDS, true);
/*     */ 
/* 223 */       for (int i = 0; i < subsInfo.length; ++i)
/*     */       {
/* 225 */         String val = subs.getStringValue(subsInfo[i].m_index);
/*     */ 
/* 227 */         if (val == null)
/*     */         {
/* 229 */           if (i == 2)
/*     */           {
/* 231 */             val = " ";
/*     */           }
/*     */           else
/*     */           {
/* 235 */             Report.error(null, LocaleUtils.encodeMessage("csSubscriptionUnableToAdd2", null, docName, MailInfo.FIELDS[i]), null);
/*     */ 
/* 237 */             skipRow = true;
/* 238 */             break;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 243 */         binder.putLocal(MailInfo.FIELDS[i], val);
/*     */       }
/*     */ 
/* 247 */       binder.putLocal("isSubscriptionByDocument", "1");
/* 248 */       boolean[] isSendMail = { true };
/* 249 */       this.m_data.setCachedObject("isSendMail", isSendMail);
/* 250 */       binder.addResultSet("Subscriptions", subs);
/*     */ 
/* 252 */       if ((skipRow) || 
/* 254 */         (PluginFilters.filter("prepareSubscriptionMail", null, binder, this.m_data) == -1) || (isSendMail[0] == 0)) {
/*     */         continue;
/*     */       }
/* 257 */       WorkQueueProcessor.addTaskToWorkQueue(0, docID, binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 265 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98371 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.DocumentOrientedSubscription
 * JD-Core Version:    0.5.4
 */