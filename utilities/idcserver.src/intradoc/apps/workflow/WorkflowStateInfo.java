/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WorkflowStateInfo
/*     */   implements Parameters
/*     */ {
/*  31 */   public String m_wfName = null;
/*  32 */   public String m_wfType = null;
/*  33 */   public DataBinder m_wfData = null;
/*     */ 
/*  35 */   public String m_nameColumn = "dWfName";
/*  36 */   public String m_typeColumn = "dWfType";
/*     */ 
/*     */   public WorkflowStateInfo(Properties props)
/*     */   {
/*  40 */     createData(props);
/*     */   }
/*     */ 
/*     */   public WorkflowStateInfo(Properties props, String nameClmn, String typeClmn)
/*     */   {
/*  45 */     this.m_nameColumn = nameClmn;
/*  46 */     this.m_typeColumn = typeClmn;
/*     */ 
/*  48 */     createData(props);
/*     */   }
/*     */ 
/*     */   public void createData(Properties props)
/*     */   {
/*  53 */     this.m_wfData = new DataBinder();
/*  54 */     updateData(props);
/*     */   }
/*     */ 
/*     */   public void updateData(Properties props)
/*     */   {
/*  59 */     this.m_wfData.setLocalData(props);
/*     */ 
/*  61 */     this.m_wfName = this.m_wfData.getLocal(this.m_nameColumn);
/*  62 */     this.m_wfType = this.m_wfData.getLocal(this.m_typeColumn);
/*     */   }
/*     */ 
/*     */   public void mergeData(Properties props)
/*     */   {
/*  67 */     DataBinder.mergeHashTables(this.m_wfData.getLocalData(), props);
/*     */ 
/*  69 */     this.m_wfName = this.m_wfData.getLocal(this.m_nameColumn);
/*  70 */     this.m_wfType = this.m_wfData.getLocal(this.m_typeColumn);
/*     */   }
/*     */ 
/*     */   public void createStepData(DataBinder binder) throws DataException
/*     */   {
/*  75 */     String stepName = this.m_wfName;
/*  76 */     String[][] events = WorkflowScriptUtils.COLUMN_EVENT_MAP;
/*  77 */     boolean isFound = false;
/*     */ 
/*  80 */     DataResultSet drset = (DataResultSet)binder.getResultSet("WorkflowStepEvents");
/*  81 */     if ((stepName != null) && (drset != null))
/*     */     {
/*  85 */       Vector row = drset.findRow(0, stepName);
/*  86 */       isFound = row != null;
/*  87 */       if (isFound)
/*     */       {
/*  89 */         Properties props = drset.getCurrentRowProps();
/*  90 */         DataBinder.mergeHashTables(this.m_wfData.getLocalData(), props);
/*  91 */         WorkflowScriptUtils.exchangeScriptStepInfo(stepName, binder, this.m_wfData, false, false);
/*     */       }
/*     */     }
/*     */ 
/*  95 */     if (isFound)
/*     */       return;
/*  97 */     for (int i = 0; i < events.length; ++i)
/*     */     {
/* 100 */       this.m_wfData.putLocal(events[i][0], "");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 107 */     this.m_wfData.removeLocal(this.m_nameColumn);
/* 108 */     this.m_wfData.removeLocal(this.m_typeColumn);
/*     */ 
/* 110 */     this.m_wfName = null;
/* 111 */     this.m_wfType = null;
/*     */   }
/*     */ 
/*     */   public void setWorkflowData(DataBinder data)
/*     */   {
/* 116 */     this.m_wfData = data;
/* 117 */     this.m_wfName = this.m_wfData.getLocal(this.m_nameColumn);
/* 118 */     this.m_wfType = this.m_wfData.getLocal(this.m_typeColumn);
/*     */   }
/*     */ 
/*     */   public DataBinder getWorkflowData()
/*     */   {
/* 123 */     return this.m_wfData;
/*     */   }
/*     */ 
/*     */   public String getWfName()
/*     */   {
/* 128 */     return this.m_wfName;
/*     */   }
/*     */ 
/*     */   public String getWfType()
/*     */   {
/* 133 */     return this.m_wfType;
/*     */   }
/*     */ 
/*     */   public String get(String key)
/*     */   {
/* 138 */     return this.m_wfData.getAllowMissing(key);
/*     */   }
/*     */ 
/*     */   public String getSystem(String key)
/*     */   {
/* 143 */     return this.m_wfData.getAllowMissing(key);
/*     */   }
/*     */ 
/*     */   public void setValue(String key, String value)
/*     */   {
/* 148 */     this.m_wfData.putLocal(key, value);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 153 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.WorkflowStateInfo
 * JD-Core Version:    0.5.4
 */