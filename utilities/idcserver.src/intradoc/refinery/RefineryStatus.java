/*     */ package intradoc.refinery;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StackTrace;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.provider.Provider;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class RefineryStatus
/*     */ {
/*     */   public static final int F_UNKNOWN_STATS = -1;
/*     */   public String m_name;
/*     */   public Provider m_prov;
/*     */   public DataBinder m_provData;
/*     */   public boolean m_isAvailable;
/*     */   public boolean m_isReadOnly;
/*     */   public String m_lastStatusCode;
/*     */   public String m_lastStatusMsg;
/*     */   public long m_nextAttempt;
/*     */   public long m_nextUpdate;
/*     */   public HashMap m_convertibleTypes;
/*     */   public DataResultSet m_postConvertedJobs;
/*     */   protected DataBinder m_statusBinder;
/*     */ 
/*     */   public void init(String provName, Provider prov, boolean isAvailable, boolean isReadOnly)
/*     */   {
/*  58 */     this.m_name = provName;
/*  59 */     this.m_prov = prov;
/*  60 */     this.m_provData = this.m_prov.getProviderData();
/*  61 */     this.m_isAvailable = isAvailable;
/*  62 */     this.m_isReadOnly = isReadOnly;
/*  63 */     this.m_lastStatusCode = new String();
/*  64 */     this.m_lastStatusMsg = new String();
/*  65 */     this.m_nextAttempt = -1L;
/*  66 */     this.m_nextUpdate = -1L;
/*  67 */     this.m_convertibleTypes = new HashMap();
/*  68 */     this.m_statusBinder = null;
/*  69 */     this.m_postConvertedJobs = null;
/*     */   }
/*     */ 
/*     */   public DataBinder getCurrentStatusBinder()
/*     */   {
/*  74 */     return this.m_statusBinder;
/*     */   }
/*     */ 
/*     */   public boolean updateStatusBinder(DataBinder statusBinder) throws DataException
/*     */   {
/*  79 */     boolean doSort = false;
/*  80 */     boolean isQueueLoadDataPresent = false;
/*  81 */     if (statusBinder != null)
/*     */     {
/*  83 */       isQueueLoadDataPresent = DataBinderUtils.getBoolean(statusBinder, "IsQueueLoadDataPresent", false);
/*  84 */       if (isQueueLoadDataPresent)
/*     */       {
/*  86 */         doSort = computeIsSortNeeded(statusBinder);
/*  87 */         this.m_statusBinder = statusBinder;
/*  88 */         updateAllowedConversionTypes();
/*  89 */         this.m_postConvertedJobs = ((DataResultSet)this.m_statusBinder.getResultSet(RefineryProviderManager.m_agentName + "PostConvertedJobs"));
/*  90 */         IdcStringBuilder msg = new IdcStringBuilder("IBR " + this.m_name + " status updated");
/*  91 */         if (this.m_postConvertedJobs != null)
/*     */         {
/*  93 */           msg.append("; jobs waiting: " + this.m_postConvertedJobs.getNumRows());
/*     */         }
/*  95 */         Report.trace("ibrstatus", msg.toString(), null);
/*     */       }
/*     */     }
/*  98 */     if (!isQueueLoadDataPresent)
/*     */     {
/* 100 */       Report.trace("ibrstatus", "RefineryStatus was passed a binder that did not have status info.", new StackTrace());
/*     */     }
/* 102 */     return doSort;
/*     */   }
/*     */ 
/*     */   public boolean removeJobFromConvertedJobs(String dConvJobID)
/*     */   {
/* 107 */     boolean isRemoved = false;
/* 108 */     List row = this.m_postConvertedJobs.findRow(0, dConvJobID, 0, 0);
/* 109 */     if (row != null)
/*     */     {
/* 111 */       isRemoved = this.m_postConvertedJobs.deleteCurrentRow();
/*     */     }
/* 113 */     Report.trace("ibrstatus", "Job: " + dConvJobID + "; removed from status table: " + isRemoved, null);
/* 114 */     return isRemoved;
/*     */   }
/*     */ 
/*     */   protected boolean computeIsSortNeeded(DataBinder newStatus)
/*     */   {
/* 119 */     int curPreSize = -1;
/* 120 */     int updtPreSize = getQueueSize(RefineryProviderManager.m_agentName, "PreConvertedQueueSize", newStatus);
/* 121 */     boolean doSort = false;
/* 122 */     if (this.m_statusBinder == null)
/*     */     {
/* 124 */       Report.trace("ibrstatus", "IBR status first load.", null);
/* 125 */       doSort = true;
/*     */     }
/*     */     else
/*     */     {
/* 129 */       curPreSize = getPreConvertedQueueSize(RefineryProviderManager.m_agentName);
/* 130 */       if (curPreSize != updtPreSize)
/*     */       {
/* 132 */         doSort = true;
/*     */       }
/*     */     }
/* 135 */     Report.trace("ibrstatus", "IBR status sort needed: " + doSort, null);
/* 136 */     if (doSort)
/*     */     {
/* 138 */       RefineryProviderManager.updateRefineryLoadRS(this.m_name, updtPreSize);
/*     */     }
/* 140 */     return doSort;
/*     */   }
/*     */ 
/*     */   protected void updateAllowedConversionTypes()
/*     */     throws DataException
/*     */   {
/* 146 */     this.m_convertibleTypes = new HashMap();
/*     */ 
/* 148 */     DataResultSet convTypes = (DataResultSet)this.m_statusBinder.getResultSet("DocumentConversions");
/* 149 */     if (convTypes == null)
/*     */     {
/* 151 */       return;
/*     */     }
/*     */ 
/* 154 */     ResultSet allowedConv = this.m_statusBinder.getResultSet("AllowedConversions");
/* 155 */     if (allowedConv != null)
/*     */     {
/* 157 */       convTypes.mergeFields(allowedConv);
/* 158 */       convTypes.merge("drConversion", allowedConv, false);
/*     */     }
/* 160 */     for (convTypes.first(); convTypes.isRowPresent(); convTypes.next())
/*     */     {
/* 162 */       String conv = ResultSetUtils.getValue(convTypes, "drConversion");
/* 163 */       String isEnabled = ResultSetUtils.getValue(convTypes, "drIsEnabledFlag");
/* 164 */       if (!StringUtils.convertToBool(isEnabled, false)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 168 */       boolean isAllowed = StringUtils.convertToBool(ResultSetUtils.getValue(convTypes, "JobIsAccepted"), true);
/*     */ 
/* 170 */       if (SystemUtils.m_verbose)
/*     */       {
/* 172 */         Report.trace("ibrstatus", "conversion: '" + conv + "' -- accepted: " + isAllowed, null);
/*     */       }
/* 174 */       if (!isAllowed)
/*     */         continue;
/* 176 */       this.m_convertibleTypes.put(conv, "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getPreConvertedQueueSize(String agentName)
/*     */   {
/* 183 */     return getQueueSize(agentName, "PreConvertedQueueSize", this.m_statusBinder);
/*     */   }
/*     */ 
/*     */   public int getPostConvertedQueueSize(String agentName)
/*     */   {
/* 188 */     return getQueueSize(agentName, "PostConvertedQueueSize", this.m_statusBinder);
/*     */   }
/*     */ 
/*     */   protected int getQueueSize(String agentName, String qName, DataBinder status)
/*     */   {
/* 193 */     DataResultSet agentsQueuedJobs = (DataResultSet)status.getResultSet("AgentsQueuedJobs");
/* 194 */     if (agentsQueuedJobs != null)
/*     */     {
/* 196 */       List row = agentsQueuedJobs.findRow(0, agentName, 0, 0);
/* 197 */       if (row != null)
/*     */       {
/* 199 */         Map map = agentsQueuedJobs.getCurrentRowMap();
/* 200 */         String s = (String)map.get(qName);
/* 201 */         return Integer.parseInt(s);
/*     */       }
/*     */     }
/* 204 */     return -1;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 209 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97828 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.RefineryStatus
 * JD-Core Version:    0.5.4
 */