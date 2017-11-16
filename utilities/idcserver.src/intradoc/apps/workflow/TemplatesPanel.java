/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ import java.util.Properties;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class TemplatesPanel extends WfBasePanel
/*     */   implements Observer
/*     */ {
/*     */   protected UdlPanel m_templateList;
/*     */ 
/*     */   public TemplatesPanel()
/*     */   {
/*  57 */     this.m_templateList = null;
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys, WorkflowContext ctxt) throws ServiceException
/*     */   {
/*  62 */     super.init(sys, ctxt);
/*     */ 
/*  64 */     refreshList(null);
/*     */ 
/*  66 */     AppLauncher.addSubjectObserver("wftemplates", this);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  72 */     JPanel tmpPanel = initTemplateList();
/*     */ 
/*  74 */     this.m_helper.makePanelGridBag(this, 1);
/*  75 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(3, 7, 3, 7);
/*  76 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  77 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  78 */     this.m_helper.addComponent(this, tmpPanel);
/*     */ 
/*  81 */     this.m_templateList.enableDisable(false);
/*     */   }
/*     */ 
/*     */   protected JPanel initTemplateList()
/*     */   {
/*  86 */     this.m_templateList = new UdlPanel(LocaleResources.getString("apTitleTemplates", this.m_cxt), null, 300, 20, "WfTemplates", true);
/*     */ 
/*  88 */     this.m_templateList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleName", this.m_cxt), "dWfTemplateName", 10.0D));
/*     */ 
/*  90 */     this.m_templateList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleDescription", this.m_cxt), "dWfTemplateDescription", 20.0D));
/*     */ 
/*  92 */     this.m_templateList.setVisibleColumns("dWfTemplateName,dWfTemplateDescription");
/*     */ 
/*  94 */     this.m_templateList.init();
/*  95 */     this.m_templateList.useDefaultListener();
/*     */ 
/*  98 */     JPanel btnPanel = new PanePanel();
/*     */ 
/* 100 */     JButton addBtn = this.m_templateList.addButton(LocaleResources.getString("apDlgButtonAdd", this.m_cxt), false);
/* 101 */     addBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apTitleAddTemplate", this.m_cxt));
/* 102 */     addBtn.setActionCommand("add");
/* 103 */     btnPanel.add(addBtn);
/*     */ 
/* 105 */     JButton editBtn = this.m_templateList.addButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt), true);
/* 106 */     editBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableButtonEditTemplate", this.m_cxt));
/* 107 */     editBtn.setActionCommand("edit");
/* 108 */     btnPanel.add(editBtn);
/*     */ 
/* 110 */     JButton deleteBtn = this.m_templateList.addButton(LocaleResources.getString("apLabelDelete", this.m_cxt), true);
/* 111 */     deleteBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableButtonDeleteTemplate", this.m_cxt));
/* 112 */     deleteBtn.setActionCommand("delete");
/* 113 */     btnPanel.add(deleteBtn);
/*     */ 
/* 115 */     this.m_templateList.add("South", btnPanel);
/*     */ 
/* 118 */     ActionListener addEditListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 122 */         String cmd = e.getActionCommand();
/* 123 */         boolean isAdd = false;
/* 124 */         if (cmd.equals("add"))
/*     */         {
/* 126 */           isAdd = true;
/*     */         }
/* 128 */         TemplatesPanel.this.addOrEditTemplate(isAdd);
/*     */       }
/*     */     };
/* 131 */     addBtn.addActionListener(addEditListener);
/* 132 */     editBtn.addActionListener(addEditListener);
/* 133 */     this.m_templateList.m_list.addActionListener(addEditListener);
/*     */ 
/* 135 */     ActionListener deleteListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 139 */         int index = TemplatesPanel.this.m_templateList.getSelectedIndex();
/* 140 */         if (index < 0)
/*     */         {
/* 143 */           TemplatesPanel.this.reportError(IdcMessageFactory.lc("apSelectTemplateToDelete", new Object[0]));
/* 144 */           return;
/*     */         }
/*     */ 
/* 147 */         Properties props = TemplatesPanel.this.m_templateList.getDataAt(index);
/* 148 */         String name = props.getProperty("dWfTemplateName");
/* 149 */         IdcMessage msg = IdcMessageFactory.lc("apVerifyWorkflowTemplateDelete", new Object[] { name });
/*     */ 
/* 151 */         if (MessageBox.doMessage(TemplatesPanel.this.m_systemInterface, msg, 4) != 2) {
/*     */           return;
/*     */         }
/*     */         try
/*     */         {
/* 156 */           DataBinder binder = new DataBinder();
/* 157 */           binder.setLocalData(props);
/*     */ 
/* 159 */           SharedContext shContext = TemplatesPanel.this.m_context.getSharedContext();
/* 160 */           AppContextUtils.executeService(shContext, "DELETE_WF_TEMPLATE", binder);
/* 161 */           TemplatesPanel.this.refreshList(null);
/* 162 */           TemplatesPanel.this.m_templateList.enableDisable(false);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 166 */           TemplatesPanel.this.reportError(exp);
/*     */         }
/*     */       }
/*     */     };
/* 171 */     deleteBtn.addActionListener(deleteListener);
/*     */ 
/* 174 */     JPanel wrapper = new PanePanel();
/* 175 */     this.m_helper.makePanelGridBag(wrapper, 1);
/* 176 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 177 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 178 */     this.m_helper.addComponent(wrapper, this.m_templateList);
/*     */ 
/* 180 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected void addOrEditTemplate(boolean isAdd)
/*     */   {
/* 185 */     String title = LocaleResources.getString("apTitleAddTemplate", this.m_cxt);
/* 186 */     String name = null;
/* 187 */     DataBinder data = null;
/* 188 */     String helpPage = "AddNewWorkflowTemplate";
/* 189 */     if (!isAdd)
/*     */     {
/* 191 */       int index = this.m_templateList.getSelectedIndex();
/* 192 */       if (index < 0)
/*     */       {
/* 194 */         return;
/*     */       }
/* 196 */       name = this.m_templateList.getSelectedObj();
/* 197 */       title = LocaleResources.getString("apTitleEditTemplate", this.m_cxt, name);
/* 198 */       helpPage = "EditWorkflowTemplate";
/*     */ 
/* 200 */       Properties props = this.m_templateList.getDataAt(index);
/* 201 */       data = getTemplateData(name);
/*     */ 
/* 205 */       DataBinder.mergeHashTables(data.getLocalData(), props);
/*     */     }
/*     */ 
/* 208 */     if (data == null)
/*     */     {
/* 210 */       data = new DataBinder();
/*     */     }
/*     */ 
/* 214 */     EditTemplateDlg dlg = new EditTemplateDlg(this.m_systemInterface, title, this.m_context, DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 216 */     if (dlg.init(data, isAdd) != 1)
/*     */       return;
/* 218 */     if (name == null)
/*     */     {
/* 221 */       name = data.getLocal("dWfTemplateName");
/*     */     }
/*     */ 
/* 224 */     refreshList(name);
/*     */   }
/*     */ 
/*     */   public void refreshList(String selObject)
/*     */   {
/* 230 */     DataResultSet templates = SharedObjects.getTable("WfTemplates");
/* 231 */     this.m_templateList.refreshList(templates, selObject);
/*     */   }
/*     */ 
/*     */   public DataBinder getTemplateData(String name)
/*     */   {
/* 236 */     DataBinder data = new DataBinder(true);
/*     */     try
/*     */     {
/* 239 */       data.putLocal("dWfTemplateName", name);
/*     */ 
/* 241 */       SharedContext shContext = this.m_context.getSharedContext();
/* 242 */       shContext.executeService("GET_WF_TEMPLATE", data, false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 246 */       reportError(e, IdcMessageFactory.lc("apUnableToGetTemplateData", new Object[0]));
/*     */     }
/*     */ 
/* 249 */     return data;
/*     */   }
/*     */ 
/*     */   public void update(Observable obs, Object arg)
/*     */   {
/* 257 */     String name = this.m_templateList.getSelectedObj();
/* 258 */     refreshList(name);
/*     */   }
/*     */ 
/*     */   public void removeNotify()
/*     */   {
/* 264 */     AppLauncher.removeSubjectObserver("wftemplates", this);
/* 265 */     super.removeNotify();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 270 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87480 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.TemplatesPanel
 * JD-Core Version:    0.5.4
 */