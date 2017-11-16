/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class CopyArchiveDlg
/*     */   implements ComponentBinder
/*     */ {
/*  52 */   protected SystemInterface m_systemInterface = null;
/*  53 */   protected ExecutionContext m_cxt = null;
/*  54 */   protected DialogHelper m_helper = null;
/*  55 */   protected CollectionContext m_collectionContext = null;
/*     */ 
/*     */   public CopyArchiveDlg(SystemInterface sys, String title, CollectionContext ctxt)
/*     */   {
/*  59 */     this.m_systemInterface = sys;
/*  60 */     this.m_cxt = sys.getExecutionContext();
/*  61 */     this.m_helper = new DialogHelper(sys, title, true);
/*  62 */     this.m_collectionContext = ctxt;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  67 */     this.m_collectionContext.loadContext(this.m_helper.m_props);
/*  68 */     String archiveName = this.m_helper.m_props.getProperty("aArchiveName");
/*  69 */     this.m_helper.m_props.put("CopyName", archiveName);
/*     */ 
/*  71 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/*  81 */           AppLauncher.executeService("COPY_ARCHIVE", CopyArchiveDlg.this.m_helper.m_props);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/*  85 */           MessageBox.reportError(CopyArchiveDlg.this.m_systemInterface, exp, IdcMessageFactory.lc("apErrorCopyingArchive", new Object[0]));
/*     */ 
/*  87 */           return false;
/*     */         }
/*  89 */         return true;
/*     */       }
/*     */     };
/*  93 */     initUI(okCallback);
/*     */   }
/*     */ 
/*     */   protected void initUI(DialogCallback okCallback)
/*     */   {
/*  98 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, DialogHelpTable.getHelpPage("CopyArchive"));
/*     */ 
/* 101 */     JTextField textField = new CustomTextField(20);
/*     */ 
/* 103 */     this.m_helper.addLabelFieldPairEx(mainPanel, LocaleResources.getString("apTitleName", this.m_cxt), textField, "CopyName", false);
/*     */ 
/* 106 */     CustomLabel filler = new CustomLabel();
/* 107 */     filler.setMinWidth(20);
/* 108 */     this.m_helper.addLastComponentInRow(mainPanel, filler);
/*     */ 
/* 113 */     this.m_helper.addLabelEditPair(mainPanel, LocaleResources.getString("apTitleCopyArchiveToDirectory", this.m_cxt), 40, "CopyPath");
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 123 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 134 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 135 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 140 */     String name = exchange.m_compName;
/* 141 */     String val = exchange.m_compValue;
/*     */ 
/* 143 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 149 */     if (name.equals("CopyName"))
/*     */     {
/* 151 */       int valResult = Validation.checkUrlFileSegment(val);
/* 152 */       IdcMessage errMsg = null;
/* 153 */       switch (valResult)
/*     */       {
/*     */       case 0:
/* 156 */         break;
/*     */       case -1:
/* 158 */         errMsg = IdcMessageFactory.lc("apSpecifyArchiveName", new Object[0]);
/* 159 */         break;
/*     */       case -2:
/* 161 */         errMsg = IdcMessageFactory.lc("apSpacesInArchiveName", new Object[0]);
/* 162 */         break;
/*     */       default:
/* 164 */         errMsg = IdcMessageFactory.lc("apIllegalCharsInArchiveName", new Object[0]);
/*     */       }
/*     */ 
/* 167 */       if (errMsg != null)
/*     */       {
/* 169 */         exchange.m_errorMessage = errMsg;
/* 170 */         return false;
/*     */       }
/*     */     }
/* 173 */     else if (name.equals("CopyPath"))
/*     */     {
/* 176 */       IdcMessage errMsg = null;
/* 177 */       val = FileUtils.directorySlashes(val);
/* 178 */       String dir = FileUtils.getDirectory(val);
/* 179 */       if (dir == null)
/*     */       {
/* 181 */         errMsg = IdcMessageFactory.lc("apInvalidCopyPath", new Object[] { val });
/*     */       }
/*     */       else
/*     */       {
/* 185 */         int result = FileUtils.checkFile(dir, false, true);
/* 186 */         switch (result)
/*     */         {
/*     */         case -24:
/* 189 */           errMsg = IdcMessageFactory.lc("apDirNotDir", new Object[] { dir });
/* 190 */           break;
/*     */         case -19:
/* 193 */           break;
/*     */         case -16:
/* 196 */           errMsg = IdcMessageFactory.lc("apDirNotFound", new Object[] { val });
/*     */         }
/*     */       }
/*     */ 
/* 200 */       if (errMsg != null)
/*     */       {
/* 202 */         exchange.m_errorMessage = errMsg;
/* 203 */         return false;
/*     */       }
/*     */     }
/* 206 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 211 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.CopyArchiveDlg
 * JD-Core Version:    0.5.4
 */