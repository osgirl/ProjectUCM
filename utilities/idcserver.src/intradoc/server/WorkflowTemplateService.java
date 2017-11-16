/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.workflow.WorkflowUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.workflow.WorkflowTemplates;
/*     */ import java.io.File;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WorkflowTemplateService extends Service
/*     */ {
/*  37 */   protected String m_workflowDir = null;
/*     */ 
/*     */   public WorkflowTemplateService()
/*     */   {
/*  41 */     this.m_workflowDir = LegacyDirectoryLocator.getWorkflowDirectory();
/*     */   }
/*     */ 
/*     */   public void createHandlersForService()
/*     */     throws ServiceException, DataException
/*     */   {
/*  47 */     super.createHandlersForService();
/*  48 */     createHandlers("WorkflowTemplateService");
/*     */   }
/*     */ 
/*     */   public void preActions()
/*     */     throws ServiceException
/*     */   {
/*  56 */     super.preActions();
/*  57 */     FileUtils.reserveDirectory(this.m_workflowDir, true);
/*     */   }
/*     */ 
/*     */   public void cleanUp(boolean isError)
/*     */   {
/*  63 */     super.cleanUp(isError);
/*  64 */     FileUtils.releaseDirectory(this.m_workflowDir, true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getTemplates()
/*     */   {
/*  73 */     DataResultSet templateSet = SharedObjects.getTable("WfTemplates");
/*  74 */     if (templateSet == null)
/*     */     {
/*  77 */       return;
/*     */     }
/*     */ 
/*  80 */     this.m_binder.addResultSet("WfTemplates", templateSet);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getTemplate() throws DataException, ServiceException
/*     */   {
/*  86 */     String name = this.m_binder.get("dWfTemplateName");
/*  87 */     if (name == null)
/*     */     {
/*  89 */       throw new DataException("!csTemplateNotDefined");
/*     */     }
/*     */ 
/*  92 */     DataBinder binder = ResourceUtils.readDataBinder(this.m_workflowDir, name.toLowerCase() + ".hda");
/*     */ 
/*  95 */     this.m_binder.merge(binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void cacheTemplates() throws DataException, ServiceException
/*     */   {
/* 101 */     ResultSet rset = WorkflowUtils.cacheTemplates(false);
/* 102 */     this.m_binder.addResultSet("WfTemplates", rset);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addTemplate() throws DataException, ServiceException
/*     */   {
/* 108 */     addOrEditTemplate(true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editTemplate() throws DataException, ServiceException
/*     */   {
/* 114 */     addOrEditTemplate(false);
/*     */   }
/*     */ 
/*     */   public void addOrEditTemplate(boolean isNew)
/*     */     throws DataException, ServiceException
/*     */   {
/* 121 */     String name = this.m_binder.get("dWfTemplateName");
/* 122 */     String filename = name.toLowerCase() + ".hda";
/*     */ 
/* 124 */     WorkflowUtils.writeTemplate(this.m_workflowDir, filename, this.m_binder);
/*     */ 
/* 128 */     DataBinder binder = WorkflowUtils.readTemplates();
/*     */ 
/* 130 */     DataResultSet rset = (DataResultSet)binder.getResultSet("WfTemplates");
/* 131 */     if (rset == null)
/*     */     {
/* 134 */       rset = new DataResultSet(WorkflowTemplates.COLUMNS);
/* 135 */       binder.addResultSet("WfTemplates", rset);
/*     */     }
/*     */ 
/* 139 */     Vector values = rset.findRow(0, name);
/* 140 */     if ((isNew) && (values != null))
/*     */     {
/* 142 */       createServiceException(null, "!csTemplateNotUnique");
/*     */     }
/*     */ 
/* 145 */     Vector row = rset.createRow(this.m_binder);
/* 146 */     if (values == null)
/*     */     {
/* 148 */       rset.addRow(row);
/*     */     }
/*     */     else
/*     */     {
/* 152 */       int index = rset.getCurrentRow();
/* 153 */       rset.setRowValues(row, index);
/*     */     }
/*     */ 
/* 157 */     WorkflowUtils.writeTemplates(this.m_workflowDir, binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteTemplate()
/*     */     throws ServiceException
/*     */   {
/* 164 */     DataBinder binder = WorkflowUtils.readTemplates();
/*     */ 
/* 166 */     DataResultSet rset = (DataResultSet)binder.getResultSet("WfTemplates");
/* 167 */     if (rset == null)
/*     */     {
/* 171 */       return;
/*     */     }
/*     */ 
/* 174 */     String name = this.m_binder.getLocal("dWfTemplateName");
/* 175 */     Vector row = rset.findRow(0, name);
/*     */ 
/* 177 */     if (row == null)
/*     */     {
/* 180 */       return;
/*     */     }
/*     */ 
/* 183 */     rset.deleteCurrentRow();
/*     */ 
/* 186 */     WorkflowUtils.writeTemplates(this.m_workflowDir, binder);
/*     */ 
/* 189 */     String filename = name.toLowerCase() + ".hda";
/* 190 */     File template = FileUtilsCfgBuilder.getCfgFile(this.m_workflowDir + filename, "Workflow", false);
/*     */     try
/*     */     {
/* 194 */       if (template.exists())
/*     */       {
/* 196 */         template.delete();
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 201 */       String msg = LocaleUtils.encodeMessage("csUnableToDeleteForCleanup", null, filename);
/*     */ 
/* 203 */       createServiceException(e, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 209 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98955 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.WorkflowTemplateService
 * JD-Core Version:    0.5.4
 */