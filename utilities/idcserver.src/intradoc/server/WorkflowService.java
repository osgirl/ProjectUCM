/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.workflow.WfCompanionData;
/*     */ import intradoc.server.workflow.WfCompanionManager;
/*     */ import intradoc.server.workflow.WorkflowUtils;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.workflow.WfStepData;
/*     */ import java.io.IOException;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WorkflowService extends Service
/*     */ {
/*     */   protected String m_curGroup;
/*     */   protected boolean m_groupChanged;
/*     */   protected boolean m_skipGroupCheck;
/*     */   protected boolean m_isWorkflowsTerminated;
/*  45 */   protected WfStepData m_stepData = null;
/*     */ 
/*     */   public WorkflowService()
/*     */   {
/*  49 */     this.m_curGroup = null;
/*  50 */     this.m_groupChanged = false;
/*  51 */     this.m_skipGroupCheck = false;
/*  52 */     this.m_isWorkflowsTerminated = false;
/*     */   }
/*     */ 
/*     */   public void createHandlersForService()
/*     */     throws ServiceException, DataException
/*     */   {
/*  58 */     super.createHandlersForService();
/*  59 */     createHandlers("WorkflowService");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void checkWorkflowAdminAccess() throws ServiceException
/*     */   {
/*  65 */     if (setPromptForLoginIfAnonymous())
/*     */     {
/*  67 */       createServiceException(null, "!csSystemNeedsLogin");
/*     */     }
/*     */ 
/*  70 */     Vector groups = SecurityUtils.getUserGroupsWithPrivilege(this.m_userData, 8);
/*  71 */     if ((groups != null) && (groups.size() != 0))
/*     */       return;
/*  73 */     createServiceException(null, "!csNoSecurityGroupAccess");
/*     */   }
/*     */ 
/*     */   public boolean computeValue(String variable, String[] val)
/*     */     throws IOException
/*     */   {
/*  82 */     String value = null;
/*  83 */     if (this.m_stepData != null)
/*     */     {
/*  85 */       value = ResultSetUtils.getValue(this.m_stepData, variable);
/*     */     }
/*     */ 
/*  88 */     if (value != null)
/*     */     {
/*  90 */       val[0] = value;
/*  91 */       return true;
/*     */     }
/*     */ 
/*  94 */     return super.computeValue(variable, val);
/*     */   }
/*     */ 
/*     */   public boolean testForNextRow(String rsetName, boolean[] retVal)
/*     */     throws IOException
/*     */   {
/* 100 */     boolean isWorkflows = rsetName.equals("Workflows");
/* 101 */     boolean isGroupedWorkflows = rsetName.equals("GroupedWorkflows");
/* 102 */     if ((!isWorkflows) && (!isGroupedWorkflows))
/*     */     {
/* 104 */       if (rsetName.equals("WfDocuments"))
/*     */       {
/* 107 */         retVal[0] = false;
/* 108 */         while (this.m_binder.nextRow(rsetName))
/*     */         {
/*     */           try
/*     */           {
/* 112 */             boolean isOK = checkAccess(this.m_binder, this.m_serviceData.m_accessLevel);
/* 113 */             if (isOK)
/*     */             {
/* 115 */               retVal[0] = true;
/* 116 */               break label95:
/*     */             }
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 121 */             throw new IOException(e.getMessage());
/*     */           }
/*     */         }
/* 124 */         label95: return true;
/*     */       }
/*     */ 
/* 127 */       return super.testForNextRow(rsetName, retVal);
/*     */     }
/*     */ 
/* 130 */     if ((isWorkflows) && (this.m_groupChanged))
/*     */     {
/* 136 */       this.m_groupChanged = false;
/* 137 */       retVal[0] = true;
/* 138 */       return true;
/*     */     }
/*     */ 
/* 141 */     boolean advanceRow = false;
/* 142 */     retVal[0] = false;
/* 143 */     if (isGroupedWorkflows)
/*     */     {
/* 147 */       if (!this.m_isWorkflowsTerminated)
/*     */       {
/* 149 */         this.m_groupChanged = true;
/* 150 */         if (this.m_skipGroupCheck)
/*     */         {
/* 155 */           this.m_skipGroupCheck = false;
/* 156 */           retVal[0] = true;
/*     */         }
/*     */         else
/*     */         {
/* 160 */           advanceRow = true;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 166 */       advanceRow = true;
/*     */     }
/*     */ 
/* 169 */     if (!advanceRow)
/*     */     {
/* 172 */       return true;
/*     */     }
/*     */ 
/* 176 */     retVal[0] = this.m_binder.nextRow("Workflows");
/* 177 */     if (retVal[0] == 0)
/*     */     {
/* 179 */       if (isWorkflows)
/*     */       {
/* 181 */         this.m_isWorkflowsTerminated = true;
/*     */       }
/* 183 */       return true;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 188 */       String nextGroup = this.m_binder.get("dSecurityGroup");
/*     */ 
/* 190 */       if (isWorkflows)
/*     */       {
/* 192 */         retVal[0] = (((this.m_curGroup != null) && (this.m_curGroup.equals(nextGroup))) ? 1 : false);
/* 193 */         if (retVal[0] == 0)
/*     */         {
/* 196 */           this.m_skipGroupCheck = true;
/*     */         }
/*     */       }
/* 199 */       this.m_curGroup = nextGroup;
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 203 */       throw new IOException("!csWorkflowWithoutSecurityGroup");
/*     */     }
/*     */ 
/* 206 */     return true;
/*     */   }
/*     */ 
/*     */   public void notifyNextRow(String rsetName, boolean hasNext)
/*     */     throws IOException
/*     */   {
/* 213 */     if ((hasNext) && 
/* 215 */       (rsetName.equals("WfDocuments")))
/*     */     {
/*     */       try
/*     */       {
/* 219 */         computeDocStepInfo();
/*     */ 
/* 221 */         String docTitle = this.m_binder.getActiveAllowMissing("dDocTitle");
/* 222 */         setConditionVar("dDocTitle", (docTitle != null) && (docTitle.length() > 0));
/*     */ 
/* 224 */         DocCommonHandler docHandler = (DocCommonHandler)getHandler("DocCommonHandler");
/* 225 */         if (docHandler != null)
/*     */         {
/* 227 */           docHandler.getURL(true, true);
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 232 */         IOException ioe = new IOException("!csWorkflowCannotComputeActions");
/* 233 */         ioe.initCause(e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 238 */     super.notifyNextRow(rsetName, hasNext);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void computeDocStepInfo() throws DataException, ServiceException
/*     */   {
/* 244 */     if (this.m_stepData == null)
/*     */     {
/* 246 */       this.m_stepData = ((WfStepData)this.m_binder.getResultSet("WorkflowSteps"));
/* 247 */       if (this.m_stepData == null)
/*     */       {
/* 249 */         throw new DataException("!csUnableToFindWorkflowStepInfo");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 255 */     String docName = this.m_binder.getActiveValue("dDocName");
/* 256 */     String subDir = this.m_binder.get("dWfDirectory");
/* 257 */     WfCompanionData wfCompanionData = WfCompanionManager.getOrCreateCompanionData(docName, subDir, this.m_workspace, this.m_binder);
/*     */ 
/* 259 */     setCachedObject("WorkflowCompanionData", wfCompanionData);
/*     */ 
/* 261 */     WorkflowUtils.computeDocStepInfo(this.m_stepData, this.m_binder, this, this.m_workspace);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 266 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70705 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.WorkflowService
 * JD-Core Version:    0.5.4
 */