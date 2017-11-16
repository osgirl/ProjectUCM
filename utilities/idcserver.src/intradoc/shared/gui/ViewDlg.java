/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.HashVector;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.CustomDialog;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.StatusBar;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Dialog;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.Window;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ViewDlg
/*     */   implements SystemInterface, RefreshView
/*     */ {
/*  66 */   protected DialogHelper m_helper = null;
/*  67 */   protected SystemInterface m_systemInterface = null;
/*  68 */   protected SharedContext m_context = null;
/*     */ 
/*  70 */   protected ViewData m_viewData = null;
/*  71 */   protected BaseView m_view = null;
/*  72 */   protected StatusBar m_statusBar = null;
/*     */ 
/*     */   public ViewDlg(Window parent, SystemInterface sys, String title, SharedContext shContext, String helpPage)
/*     */   {
/*  77 */     if (parent == null)
/*     */     {
/*  79 */       parent = sys.getMainWindow();
/*     */     }
/*     */ 
/*  83 */     this.m_helper = new DialogHelper();
/*  84 */     this.m_helper.m_title = title;
/*  85 */     this.m_helper.m_parent = parent;
/*  86 */     this.m_helper.m_isModal = true;
/*  87 */     this.m_helper.m_helpPage = helpPage;
/*  88 */     this.m_helper.m_exitOnClose = false;
/*     */ 
/*  91 */     this.m_systemInterface = sys;
/*  92 */     this.m_context = shContext;
/*     */   }
/*     */ 
/*     */   public void init(ViewData viewData, Hashtable ignoreMap)
/*     */   {
/* 101 */     init(viewData, ignoreMap, new Properties());
/*     */   }
/*     */ 
/*     */   public void init(ViewData viewData, Hashtable ignoreMap, Properties props)
/*     */   {
/* 106 */     this.m_viewData = viewData;
/* 107 */     this.m_helper.m_props = props;
/*     */ 
/* 109 */     initUI();
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/*     */     try
/*     */     {
/* 116 */       this.m_view.refreshView();
/* 117 */       return this.m_helper.prompt();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 121 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apUnableToDisplayView", new Object[0]));
/* 122 */       this.m_helper.close();
/*     */     }
/*     */ 
/* 125 */     return 0;
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/* 133 */     JDialog dlg = null;
/* 134 */     if (this.m_helper.m_parent instanceof JFrame)
/*     */     {
/* 136 */       dlg = new CustomDialog((JFrame)this.m_helper.m_parent, this.m_helper.m_title, true);
/*     */     }
/*     */     else
/*     */     {
/* 140 */       dlg = new CustomDialog((Dialog)this.m_helper.m_parent, this.m_helper.m_title, true);
/*     */     }
/* 142 */     this.m_helper.attachToWindow(dlg, this, this.m_helper.m_props);
/* 143 */     this.m_helper.m_dialog = dlg;
/*     */ 
/* 145 */     this.m_helper.m_toolbar = new PanePanel();
/* 146 */     this.m_helper.m_toolbar.setLayout(new FlowLayout());
/*     */ 
/* 148 */     JPanel viewAreaPanel = new PanePanel();
/* 149 */     viewAreaPanel.setLayout(new BorderLayout());
/* 150 */     viewAreaPanel.add("Center", this.m_helper.m_mainPanel = new PanePanel());
/* 151 */     viewAreaPanel.add("South", this.m_helper.m_toolbar);
/*     */ 
/* 153 */     dlg.setLayout(new BorderLayout());
/* 154 */     dlg.add("Center", viewAreaPanel);
/* 155 */     dlg.add("South", this.m_statusBar = new StatusBar());
/*     */ 
/* 157 */     JPanel header = null;
/* 158 */     if (this.m_viewData.m_hasExtraWhereClause)
/*     */     {
/* 161 */       header = new PanePanel();
/* 162 */       header.setLayout(new BorderLayout());
/* 163 */       header.add("West", new CustomLabel(this.m_viewData.m_extraLabel, 1));
/* 164 */       header.add("Center", new CustomText(this.m_viewData.m_extraWhereClause, 75));
/*     */     }
/*     */ 
/* 168 */     JPanel viewPanel = new PanePanel();
/* 169 */     initView(viewPanel);
/*     */ 
/* 172 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 173 */     mainPanel.setLayout(new BorderLayout());
/* 174 */     if (header != null)
/*     */     {
/* 176 */       mainPanel.add("North", header);
/*     */     }
/* 178 */     mainPanel.add("Center", viewPanel);
/*     */ 
/* 181 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 186 */         if (!ViewDlg.this.m_viewData.m_isViewOnly)
/*     */         {
/* 188 */           int[] selectedObjs = ViewDlg.this.getSelectedIndexes();
/* 189 */           if ((selectedObjs == null) || (selectedObjs.length == 0))
/*     */           {
/* 191 */             this.m_errorMessage = IdcMessageFactory.lc("apMustSelect_" + ViewDlg.this.m_viewData.m_msg, new Object[0]);
/* 192 */             return false;
/*     */           }
/*     */         }
/* 195 */         return true;
/*     */       }
/*     */     };
/* 199 */     if (this.m_viewData.m_isViewOnly)
/*     */     {
/* 202 */       String label = LocaleResources.getString("apLabelClose", this.m_systemInterface.getExecutionContext());
/*     */ 
/* 204 */       this.m_helper.m_ok = this.m_helper.addCommandButton(label, this.m_helper);
/* 205 */       this.m_helper.m_okCallback = okCallback;
/*     */     }
/*     */     else
/*     */     {
/* 209 */       this.m_helper.addOK(okCallback);
/* 210 */       this.m_helper.addCancel(null);
/*     */     }
/*     */ 
/* 213 */     this.m_helper.addHelp(null);
/*     */   }
/*     */ 
/*     */   public void initView(JPanel viewPanel)
/*     */   {
/* 218 */     switch (this.m_viewData.m_viewType)
/*     */     {
/*     */     case 1:
/* 221 */       this.m_view = new DocView(this.m_helper, this, null);
/* 222 */       break;
/*     */     case 2:
/* 225 */       this.m_view = new UserView(this.m_helper, this, null);
/* 226 */       break;
/*     */     case 3:
/* 229 */       this.m_view = new TableView(this.m_helper, this, null);
/* 230 */       break;
/*     */     case 4:
/* 233 */       this.m_view = new SchemaView(this.m_helper, this, null);
/*     */     }
/*     */ 
/* 237 */     this.m_view.init(this.m_viewData);
/* 238 */     this.m_view.initUI(this.m_viewData, viewPanel);
/*     */   }
/*     */ 
/*     */   public int[] getSelectedIndexes()
/*     */   {
/* 246 */     return this.m_view.getSelectedIndexes();
/*     */   }
/*     */ 
/*     */   public String[] getSelectedObjs()
/*     */   {
/* 252 */     return this.m_view.getSelectedObjs();
/*     */   }
/*     */ 
/*     */   public Vector computeSelectedValues(String key, boolean isUnique)
/*     */   {
/* 257 */     int[] indexes = this.m_view.getSelectedIndexes();
/* 258 */     int num = indexes.length;
/*     */ 
/* 260 */     Vector objs = null;
/* 261 */     if (isUnique)
/*     */     {
/* 263 */       HashVector objMap = new HashVector();
/* 264 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 266 */         Properties props = this.m_view.getDataAt(indexes[i]);
/* 267 */         String value = props.getProperty(key);
/* 268 */         objMap.addValue(value);
/*     */       }
/* 270 */       objs = objMap.m_values;
/*     */     }
/*     */     else
/*     */     {
/* 274 */       objs = new IdcVector();
/* 275 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 277 */         Properties props = this.m_view.getDataAt(indexes[i]);
/* 278 */         String value = props.getProperty(key);
/* 279 */         objs.addElement(value);
/*     */       }
/*     */     }
/* 282 */     return objs;
/*     */   }
/*     */ 
/*     */   public String computeSelectedValuesString(String key, boolean isUnique)
/*     */   {
/* 287 */     Vector objs = computeSelectedValues(key, isUnique);
/* 288 */     String str = StringUtils.createString(objs, '\t', '^');
/* 289 */     return str;
/*     */   }
/*     */ 
/*     */   public Properties getProperties()
/*     */   {
/* 294 */     return this.m_helper.m_props;
/*     */   }
/*     */ 
/*     */   public JFrame getMainWindow()
/*     */   {
/* 302 */     return this.m_systemInterface.getMainWindow();
/*     */   }
/*     */ 
/*     */   public void displayStatus(String str)
/*     */   {
/* 307 */     this.m_statusBar.setText(str);
/*     */   }
/*     */ 
/*     */   public void displayStatus(IdcMessage msg)
/*     */   {
/* 312 */     String str = localizeMessage(msg);
/* 313 */     this.m_statusBar.setText(str);
/*     */   }
/*     */ 
/*     */   public String getAppName()
/*     */   {
/* 318 */     return this.m_systemInterface.getAppName();
/*     */   }
/*     */ 
/*     */   public DataBinder refresh(String rsetName, Vector filterData, DataResultSet defSet)
/*     */     throws ServiceException
/*     */   {
/* 326 */     DataBinder binder = buildBinder(rsetName, filterData);
/*     */ 
/* 328 */     String viewName = this.m_viewData.m_viewName;
/* 329 */     if ((viewName != null) && (viewName.length() > 0))
/*     */     {
/* 331 */       String filterName = viewName + ":filter";
/* 332 */       FilterUtils.createTopicEdits(filterName, binder, defSet);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 337 */       this.m_context.executeService(this.m_viewData.m_action, binder, false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 341 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 346 */     return binder;
/*     */   }
/*     */ 
/*     */   public SharedContext getSharedContext()
/*     */   {
/* 351 */     return this.m_context;
/*     */   }
/*     */ 
/*     */   public DataResultSet getMetaData()
/*     */   {
/* 356 */     return SharedObjects.getTable("DocMetaDefinition");
/*     */   }
/*     */ 
/*     */   public DataBinder buildBinder(String rsetName, Vector filter)
/*     */     throws ServiceException
/*     */   {
/* 362 */     StringBuffer queryBuff = new StringBuffer();
/* 363 */     String extraWhereClause = this.m_viewData.m_extraWhereClause;
/* 364 */     if ((extraWhereClause != null) && (extraWhereClause.length() > 0))
/*     */     {
/* 366 */       queryBuff.append(extraWhereClause);
/*     */     }
/*     */ 
/* 369 */     String whereClause = this.m_view.buildSQL(filter);
/* 370 */     if ((whereClause != null) && (whereClause.length() > 0))
/*     */     {
/* 372 */       if (queryBuff.length() > 0)
/*     */       {
/* 374 */         queryBuff.append(" AND ");
/*     */       }
/* 376 */       queryBuff.append("(");
/* 377 */       queryBuff.append(whereClause);
/* 378 */       queryBuff.append(")");
/*     */     }
/*     */ 
/* 381 */     DataBinder binder = new DataBinder();
/* 382 */     binder.setLocalData(this.m_helper.m_props);
/*     */ 
/* 384 */     binder.putLocal("whereClause", queryBuff.toString());
/* 385 */     binder.putLocal("resultName", rsetName);
/* 386 */     binder.putLocal("dataSource", this.m_viewData.m_dataSource);
/*     */ 
/* 388 */     if (this.m_viewData.m_maxRows > 0)
/*     */     {
/* 390 */       binder.putLocal("MaxQueryRows", Integer.toString(this.m_viewData.m_maxRows));
/*     */     }
/*     */ 
/* 393 */     if (this.m_viewData.m_orderClause != null)
/*     */     {
/* 395 */       binder.putLocal("orderClause", this.m_viewData.m_orderClause);
/*     */     }
/*     */ 
/* 398 */     if (this.m_viewData.m_colNames != null)
/*     */     {
/* 400 */       binder.putLocal("collectionNames", this.m_viewData.m_colNames);
/*     */     }
/* 402 */     return binder;
/*     */   }
/*     */ 
/*     */   public void checkSelection()
/*     */   {
/*     */   }
/*     */ 
/*     */   public ExecutionContext getExecutionContext()
/*     */   {
/* 412 */     return this.m_systemInterface.getExecutionContext();
/*     */   }
/*     */ 
/*     */   public String localizeMessage(String msg)
/*     */   {
/* 417 */     return this.m_systemInterface.localizeMessage(msg);
/*     */   }
/*     */ 
/*     */   public String localizeMessage(IdcMessage msg)
/*     */   {
/* 422 */     return this.m_systemInterface.localizeMessage(msg);
/*     */   }
/*     */ 
/*     */   public String localizeCaption(String msg)
/*     */   {
/* 427 */     msg = LocaleUtils.encodeMessage("syCaptionWrapper", null, msg);
/*     */ 
/* 429 */     msg = this.m_systemInterface.localizeMessage(msg);
/* 430 */     return msg;
/*     */   }
/*     */ 
/*     */   public String getString(String str)
/*     */   {
/* 435 */     return this.m_systemInterface.getString(str);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String getValidationErrorMessage(String fieldName, String value)
/*     */   {
/* 442 */     String msg = this.m_systemInterface.getValidationErrorMessage(fieldName, value);
/*     */ 
/* 444 */     return msg;
/*     */   }
/*     */ 
/*     */   public IdcMessage getValidationErrorMessageObject(String fieldName, String value, Map options)
/*     */   {
/* 449 */     IdcMessage msg = this.m_systemInterface.getValidationErrorMessageObject(fieldName, value, options);
/*     */ 
/* 451 */     return msg;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 456 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78892 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.ViewDlg
 * JD-Core Version:    0.5.4
 */