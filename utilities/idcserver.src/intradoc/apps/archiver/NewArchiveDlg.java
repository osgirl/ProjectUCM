/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class NewArchiveDlg
/*     */   implements ComponentBinder
/*     */ {
/*  58 */   protected SystemInterface m_systemInterface = null;
/*  59 */   protected ExecutionContext m_cxt = null;
/*  60 */   protected DialogHelper m_helper = null;
/*  61 */   protected CollectionContext m_collectionContext = null;
/*     */ 
/*  63 */   protected JButton m_browseBtn = null;
/*  64 */   protected JTextField m_copyPathCmp = null;
/*  65 */   protected String m_action = null;
/*     */ 
/*     */   public NewArchiveDlg(SystemInterface sys, String title, CollectionContext ctxt)
/*     */   {
/*  70 */     this.m_systemInterface = sys;
/*  71 */     this.m_cxt = sys.getExecutionContext();
/*  72 */     this.m_helper = new DialogHelper(sys, title, true);
/*  73 */     this.m_collectionContext = ctxt;
/*  74 */     this.m_action = "ADD_ARCHIVE";
/*     */   }
/*     */ 
/*     */   public void init(boolean isNew)
/*     */   {
/*  79 */     this.m_collectionContext.loadContext(this.m_helper.m_props);
/*  80 */     if (isNew)
/*     */     {
/*  83 */       this.m_helper.m_props.put("aArchiveName", "");
/*     */     }
/*     */     else
/*     */     {
/*  87 */       this.m_collectionContext.loadArchiveData(this.m_helper.m_props);
/*  88 */       this.m_action = "EDIT_ARCHIVE";
/*     */     }
/*     */ 
/*  91 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 101 */           AppLauncher.executeService(NewArchiveDlg.this.m_action, NewArchiveDlg.this.m_helper.m_props);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 105 */           NewArchiveDlg.this.m_collectionContext.reportError(exp);
/* 106 */           return false;
/*     */         }
/* 108 */         return true;
/*     */       }
/*     */     };
/* 112 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, DialogHelpTable.getHelpPage("AddNewArchive"));
/*     */ 
/* 115 */     initUI(mainPanel, isNew);
/*     */   }
/*     */ 
/*     */   protected void initUI(JPanel mainPanel, boolean isNew)
/*     */   {
/* 120 */     Component nameField = null;
/* 121 */     if (isNew)
/*     */     {
/* 123 */       nameField = new CustomTextField(20);
/*     */     }
/*     */     else
/*     */     {
/* 127 */       CustomLabel nameLabel = new CustomLabel();
/* 128 */       nameLabel.setMinWidth(20);
/* 129 */       nameField = nameLabel;
/*     */     }
/* 131 */     this.m_helper.addLabelFieldPairEx(mainPanel, LocaleResources.getString("apLabelArchiveName", this.m_cxt), nameField, "aArchiveName", false);
/*     */ 
/* 134 */     CustomLabel filler = new CustomLabel();
/* 135 */     filler.setMinWidth(20);
/* 136 */     this.m_helper.addLastComponentInRow(mainPanel, filler);
/*     */ 
/* 138 */     this.m_helper.addLabelEditPair(mainPanel, LocaleResources.getString("apLabelDescription", this.m_cxt), 40, "aArchiveDescription");
/*     */ 
/* 141 */     if ((!AppLauncher.getIsStandAlone()) || (!isNew))
/*     */       return;
/* 143 */     JCheckBox isCopyBox = new CustomCheckbox(LocaleResources.getString("apLabelCopyFrom", this.m_cxt));
/*     */ 
/* 145 */     this.m_helper.m_exchange.addComponent("IsCopyFrom", isCopyBox, null);
/* 146 */     this.m_helper.addLastComponentInRow(mainPanel, isCopyBox);
/*     */ 
/* 148 */     this.m_browseBtn = this.m_helper.addFilePathComponentEx(mainPanel, 40, LocaleResources.getString("apLabelCopyArchive", this.m_cxt), "CopyPath", "archive.hda", true);
/*     */ 
/* 151 */     Object[] pathInfo = this.m_helper.m_exchange.findComponent("CopyPath", false);
/* 152 */     this.m_copyPathCmp = ((JTextField)pathInfo[1]);
/*     */ 
/* 154 */     ItemListener listener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 158 */         int state = e.getStateChange();
/* 159 */         boolean isSelected = state == 1;
/* 160 */         NewArchiveDlg.this.enableDisable(isSelected);
/*     */       }
/*     */     };
/* 163 */     isCopyBox.addItemListener(listener);
/* 164 */     enableDisable(false);
/*     */   }
/*     */ 
/*     */   protected void enableDisable(boolean st)
/*     */   {
/* 170 */     if (this.m_browseBtn == null)
/*     */       return;
/* 172 */     this.m_browseBtn.setEnabled(st);
/* 173 */     this.m_copyPathCmp.setEnabled(st);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 179 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public String getArchiveName()
/*     */   {
/* 184 */     return this.m_helper.m_props.getProperty("aArchiveName");
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 195 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 196 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 201 */     String name = exchange.m_compName;
/* 202 */     String val = exchange.m_compValue;
/*     */ 
/* 204 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 210 */     if (name.equals("aArchiveName"))
/*     */     {
/* 212 */       int valResult = Validation.checkUrlFileSegment(val);
/* 213 */       IdcMessage errMsg = null;
/* 214 */       switch (valResult)
/*     */       {
/*     */       case 0:
/* 217 */         break;
/*     */       case -1:
/* 219 */         errMsg = IdcMessageFactory.lc("apSpecifyArchiveName", new Object[0]);
/* 220 */         break;
/*     */       case -2:
/* 222 */         errMsg = IdcMessageFactory.lc("apSpacesInArchiveName", new Object[0]);
/* 223 */         break;
/*     */       default:
/* 225 */         errMsg = IdcMessageFactory.lc("apIllegalCharsInArchiveName", new Object[0]);
/*     */       }
/*     */ 
/* 228 */       if (errMsg != null)
/*     */       {
/* 230 */         exchange.m_errorMessage = errMsg;
/* 231 */         return false;
/*     */       }
/*     */     }
/* 234 */     else if (name.equals("IsCopyFrom"))
/*     */     {
/* 236 */       boolean isCopy = StringUtils.convertToBool(val, false);
/* 237 */       IdcMessage errMsg = null;
/* 238 */       if (isCopy)
/*     */       {
/* 240 */         String path = this.m_copyPathCmp.getText();
/*     */ 
/* 242 */         int result = FileUtils.checkFile(path, true, false);
/* 243 */         switch (result)
/*     */         {
/*     */         case -16:
/* 246 */           errMsg = IdcMessageFactory.lc("apArchiveDefinitionNotFound", new Object[] { path });
/*     */         }
/*     */       }
/*     */ 
/* 250 */       if (errMsg != null)
/*     */       {
/* 252 */         exchange.m_errorMessage = errMsg;
/* 253 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 257 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 262 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.NewArchiveDlg
 * JD-Core Version:    0.5.4
 */