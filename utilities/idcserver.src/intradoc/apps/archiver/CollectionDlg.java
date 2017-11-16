/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class CollectionDlg
/*     */   implements ComponentBinder
/*     */ {
/*  52 */   protected SystemInterface m_systemInterface = null;
/*  53 */   protected ExecutionContext m_cxt = null;
/*  54 */   protected DialogHelper m_helper = null;
/*     */ 
/*  56 */   protected boolean m_isCreate = true;
/*  57 */   protected String m_filename = null;
/*     */ 
/*     */   public CollectionDlg(SystemInterface sys, String title)
/*     */   {
/*  61 */     this.m_helper = new DialogHelper(sys, title, true);
/*  62 */     this.m_systemInterface = sys;
/*  63 */     this.m_cxt = sys.getExecutionContext();
/*     */   }
/*     */ 
/*     */   public int init(String idcName, String filename, boolean isLocal)
/*     */   {
/*  69 */     this.m_filename = filename;
/*  70 */     String collectionDir = FileUtils.getDirectory(filename);
/*     */ 
/*  72 */     String intradocDir = "";
/*  73 */     String exportDir = collectionDir;
/*  74 */     String webDir = "";
/*  75 */     String vaultDir = "";
/*     */ 
/*  78 */     int index = collectionDir.indexOf("archives");
/*  79 */     if ((isLocal) && (index > 0))
/*     */     {
/*  81 */       intradocDir = collectionDir.substring(0, index);
/*     */ 
/*  83 */       vaultDir = intradocDir + "vault";
/*  84 */       webDir = intradocDir + "weblayout";
/*     */     }
/*     */ 
/*  88 */     if (idcName != null)
/*     */     {
/*  90 */       this.m_isCreate = false;
/*  91 */       this.m_helper.m_props.put("IDC_Name", idcName);
/*     */     }
/*  93 */     this.m_helper.m_props.put("aCollectionLocation", collectionDir);
/*  94 */     this.m_helper.m_props.put("aCollectionExportLocation", exportDir);
/*  95 */     this.m_helper.m_props.put("aWeblayoutDir", webDir);
/*  96 */     this.m_helper.m_props.put("aVaultDir", vaultDir);
/*     */ 
/*  99 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 106 */           Properties props = CollectionDlg.this.m_helper.m_props;
/* 107 */           if (CollectionDlg.this.m_isCreate)
/*     */           {
/* 109 */             props.put("IsCreate", "1");
/*     */           }
/* 111 */           AppLauncher.executeService("ADD_COLLECTION", CollectionDlg.this.m_helper.m_props);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 115 */           MessageBox.reportError(CollectionDlg.this.m_systemInterface, exp, IdcMessageFactory.lc("apErrorCreatingCollection", new Object[0]));
/*     */ 
/* 117 */           return false;
/*     */         }
/* 119 */         return true;
/*     */       }
/*     */     };
/* 124 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 0, true, DialogHelpTable.getHelpPage("AddArchiverCollection"));
/*     */ 
/* 127 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 128 */     CustomTextField comp = new CustomTextField(idcName, 20);
/* 129 */     comp.setEnabled(this.m_isCreate);
/* 130 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apTitleName", this.m_cxt), comp, "IDC_Name");
/*     */ 
/* 133 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 5, 5, 5);
/* 134 */     comp = new CustomTextField(collectionDir, 40);
/* 135 */     comp.setEnabled(false);
/* 136 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apTitleLocation", this.m_cxt), comp, "aCollectionLocation");
/*     */ 
/* 139 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 5, 5, 5);
/* 140 */     comp = new CustomTextField(exportDir, 40);
/* 141 */     comp.setEnabled(!isLocal);
/* 142 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apTitleExportLocation", this.m_cxt), comp, "aCollectionExportLocation");
/*     */ 
/* 145 */     if (isLocal)
/*     */     {
/* 147 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 5, 5, 5);
/* 148 */       this.m_helper.m_gridHelper.prepareAddRowElement(13);
/* 149 */       this.m_helper.addLabelEditPair(mainPanel, LocaleResources.getString("apTitleWebDirectory", this.m_cxt), 40, "aWeblayoutDir");
/*     */ 
/* 152 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 5, 10, 5);
/* 153 */       this.m_helper.m_gridHelper.prepareAddRowElement(13);
/* 154 */       this.m_helper.addLabelEditPair(mainPanel, LocaleResources.getString("apTitleVaultDirectory", this.m_cxt), 40, "aVaultDir");
/*     */     }
/*     */ 
/* 158 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public String getIdcName()
/*     */   {
/* 163 */     return this.m_helper.m_props.getProperty("IDC_Name");
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 174 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 175 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 180 */     String name = exchange.m_compName;
/* 181 */     String val = exchange.m_compValue;
/*     */ 
/* 183 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 189 */     IdcMessage errMsg = null;
/* 190 */     if (name.equals("aCollectionExportLocation"))
/*     */     {
/* 193 */       val = FileUtils.directorySlashes(val);
/* 194 */       File dir = new File(val);
/* 195 */       int result = FileUtils.checkFile(dir, false, true);
/* 196 */       switch (result)
/*     */       {
/*     */       case -24:
/* 199 */         errMsg = IdcMessageFactory.lc("apExportDirNotDir", new Object[] { val });
/* 200 */         break;
/*     */       case -19:
/* 203 */         break;
/*     */       case -16:
/* 206 */         errMsg = IdcMessageFactory.lc("apExportDirNotFound", new Object[] { val });
/*     */       }
/*     */ 
/*     */     }
/* 210 */     else if (name.equals("aVaultDir"))
/*     */     {
/* 213 */       val = FileUtils.directorySlashes(val);
/* 214 */       File dir = new File(val);
/* 215 */       int result = FileUtils.checkFile(dir, false, true);
/* 216 */       switch (result)
/*     */       {
/*     */       case -24:
/* 219 */         errMsg = IdcMessageFactory.lc("apVaultDirNotDir", new Object[] { val });
/* 220 */         break;
/*     */       case -19:
/* 223 */         break;
/*     */       case -16:
/* 226 */         errMsg = IdcMessageFactory.lc("apVaultDirNotFound", new Object[] { val });
/*     */       }
/*     */ 
/*     */     }
/* 230 */     else if (name.equals("aWeblayoutDir"))
/*     */     {
/* 233 */       val = FileUtils.directorySlashes(val);
/* 234 */       File dir = new File(val);
/* 235 */       int result = FileUtils.checkFile(dir, false, true);
/* 236 */       switch (result)
/*     */       {
/*     */       case -24:
/* 239 */         errMsg = IdcMessageFactory.lc("apWebDirNotDir", new Object[] { val });
/* 240 */         break;
/*     */       case -19:
/* 243 */         break;
/*     */       case -16:
/* 246 */         errMsg = IdcMessageFactory.lc("apWebDirNotFound", new Object[] { val });
/*     */       }
/*     */ 
/*     */     }
/* 250 */     else if ((name.equals("IDC_Name")) && ((
/* 252 */       (val == null) || (val.trim().length() == 0))))
/*     */     {
/* 254 */       errMsg = IdcMessageFactory.lc("apSpecifyName", new Object[0]);
/*     */     }
/*     */ 
/* 257 */     if (errMsg != null)
/*     */     {
/* 259 */       exchange.m_errorMessage = errMsg;
/* 260 */       return false;
/*     */     }
/* 262 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 267 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97779 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.CollectionDlg
 * JD-Core Version:    0.5.4
 */