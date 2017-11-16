/*     */ package intradoc.shared.workflow;
/*     */ 
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WorkflowData extends DataResultSet
/*     */ {
/*  34 */   protected Hashtable m_workflowInfo = null;
/*     */ 
/*  37 */   protected DataResultSet m_criteriaSet = null;
/*  38 */   protected Hashtable m_criteriaMap = null;
/*     */ 
/*  41 */   public static String m_tableName = "Workflows";
/*  42 */   public static String m_queryName = "Qworkflows";
/*  43 */   public static String m_criteriaTableName = "QworkflowCriteria";
/*     */   protected String m_curTable;
/*     */ 
/*     */   public WorkflowData()
/*     */   {
/*  50 */     this.m_curTable = m_tableName;
/*     */   }
/*     */ 
/*     */   public String getTableName()
/*     */   {
/*  55 */     return m_tableName;
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  61 */     DataResultSet rset = new WorkflowData();
/*  62 */     initShallow(rset);
/*     */ 
/*  64 */     return rset;
/*     */   }
/*     */ 
/*     */   public void initShallow(DataResultSet rset)
/*     */   {
/*  70 */     super.initShallow(rset);
/*  71 */     WorkflowData data = (WorkflowData)rset;
/*  72 */     data.m_workflowInfo = this.m_workflowInfo;
/*  73 */     data.m_criteriaSet = this.m_criteriaSet;
/*  74 */     data.m_criteriaMap = this.m_criteriaMap;
/*     */   }
/*     */ 
/*     */   public void load(DataBinder binder) throws DataException
/*     */   {
/*  79 */     ResultSet rset = binder.getResultSet(m_tableName);
/*  80 */     loadWorkflows(rset);
/*     */ 
/*  82 */     rset = binder.getResultSet(m_criteriaTableName);
/*  83 */     ResultSet sset = binder.getResultSet("QworkflowSubs");
/*  84 */     loadCriteria(rset, sset);
/*     */   }
/*     */ 
/*     */   public void loadWorkflows(ResultSet rSet) throws DataException
/*     */   {
/*  89 */     if (rSet == null)
/*     */     {
/*  91 */       throw new DataException("!csWfInfoError");
/*     */     }
/*     */ 
/*  94 */     copy(rSet);
/*     */ 
/*  97 */     this.m_workflowInfo = new Hashtable();
/*  98 */     for (; isRowPresent(); next())
/*     */     {
/* 100 */       Properties props = getCurrentRowProps();
/* 101 */       String name = props.getProperty("dWfName").toLowerCase();
/*     */ 
/* 103 */       WorkflowInfo wfInfo = new WorkflowInfo(props);
/* 104 */       this.m_workflowInfo.put(name, wfInfo);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadCriteria(ResultSet critSet, ResultSet subWfSet) throws DataException
/*     */   {
/* 110 */     if (critSet == null)
/*     */     {
/* 112 */       throw new DataException("!csWfCriteriaInfoError");
/*     */     }
/*     */ 
/* 115 */     if (subWfSet == null)
/*     */     {
/* 117 */       throw new DataException("!csWfSubWorkflowInfoError");
/*     */     }
/*     */ 
/* 120 */     if (critSet instanceof DataResultSet)
/*     */     {
/* 122 */       this.m_criteriaSet = ((DataResultSet)critSet);
/*     */     }
/*     */     else
/*     */     {
/* 126 */       this.m_criteriaSet = new DataResultSet();
/* 127 */       this.m_criteriaSet.copy(critSet);
/*     */     }
/*     */ 
/* 130 */     this.m_criteriaSet.merge("dWfID", subWfSet, false);
/*     */ 
/* 132 */     SharedObjects.putTable(m_criteriaTableName, this.m_criteriaSet);
/* 133 */     this.m_criteriaMap = new Hashtable();
/*     */ 
/* 135 */     for (; this.m_criteriaSet.isRowPresent(); this.m_criteriaSet.next())
/*     */     {
/* 137 */       Properties props = this.m_criteriaSet.getCurrentRowProps();
/*     */ 
/* 139 */       String name = props.getProperty("dWfName").toLowerCase();
/* 140 */       WorkflowInfo info = (WorkflowInfo)this.m_workflowInfo.get(name);
/* 141 */       if (info == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 148 */       info.addCriteriaInfo(props);
/*     */ 
/* 151 */       String key = props.getProperty("dSecurityGroup").toLowerCase();
/* 152 */       String projectID = props.getProperty("dProjectID");
/* 153 */       if ((projectID != null) && (projectID.trim().length() > 0))
/*     */       {
/* 156 */         key = key + "@" + projectID;
/*     */       }
/* 158 */       if ((info.m_isCollaboration) && (info.m_isCriteria))
/*     */       {
/* 160 */         key = key + "@prj";
/*     */       }
/*     */ 
/* 163 */       key = key.toLowerCase();
/*     */ 
/* 165 */       String type = props.getProperty("dWfType");
/* 166 */       if (!type.equalsIgnoreCase("Criteria"))
/*     */         continue;
/* 168 */       Vector criteria = (Vector)this.m_criteriaMap.get(key);
/* 169 */       if (criteria == null)
/*     */       {
/* 171 */         criteria = new IdcVector();
/* 172 */         this.m_criteriaMap.put(key, criteria);
/*     */       }
/* 174 */       criteria.addElement(info);
/*     */     }
/*     */   }
/*     */ 
/*     */   public WorkflowInfo getWorkflowInfo(String wfName)
/*     */   {
/* 181 */     return (WorkflowInfo)this.m_workflowInfo.get(wfName.trim().toLowerCase());
/*     */   }
/*     */ 
/*     */   public String getWorkflowState(String wfName)
/*     */   {
/* 186 */     WorkflowInfo info = (WorkflowInfo)this.m_workflowInfo.get(wfName.trim().toLowerCase());
/* 187 */     if (info == null)
/*     */     {
/* 189 */       return null;
/*     */     }
/* 191 */     return info.m_properties.getProperty("dWfStatus");
/*     */   }
/*     */ 
/*     */   public DataResultSet getCriteriaWorkflows()
/*     */   {
/* 196 */     return this.m_criteriaSet;
/*     */   }
/*     */ 
/*     */   public DataResultSet getBasicWorkflows()
/*     */   {
/* 201 */     DataResultSet rset = new DataResultSet();
/* 202 */     rset.copySimpleFiltered(this, "dWfType", "Basic");
/*     */ 
/* 204 */     return rset;
/*     */   }
/*     */ 
/*     */   public Vector getCriteriaForSecurityGroup(String group, String extraKey)
/*     */   {
/* 209 */     String key = group;
/* 210 */     if (extraKey != null)
/*     */     {
/* 212 */       key = key + "@" + extraKey;
/*     */     }
/* 214 */     key = key.toLowerCase();
/*     */ 
/* 216 */     return (Vector)this.m_criteriaMap.get(key);
/*     */   }
/*     */ 
/*     */   public void getCriteriaInfo(Properties props)
/*     */   {
/* 221 */     if (props == null)
/*     */     {
/* 223 */       return;
/*     */     }
/*     */ 
/* 226 */     String wfID = props.getProperty("dWfID");
/* 227 */     if (wfID == null)
/*     */     {
/* 230 */       return;
/*     */     }
/*     */ 
/* 233 */     Vector values = this.m_criteriaSet.findRow(0, wfID);
/* 234 */     if (values == null)
/*     */     {
/* 236 */       return;
/*     */     }
/*     */ 
/* 239 */     int num = this.m_criteriaSet.getNumFields();
/* 240 */     FieldInfo info = new FieldInfo();
/* 241 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 243 */       this.m_criteriaSet.getIndexFieldInfo(i, info);
/* 244 */       props.put(info.m_name, values.elementAt(i));
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 250 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.workflow.WorkflowData
 * JD-Core Version:    0.5.4
 */