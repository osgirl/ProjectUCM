/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class CommonDialogs
/*     */ {
/*     */   public static int promptForLargeText(SystemInterface sys, String title, String msg, Properties props, String labelStr, String fieldName)
/*     */   {
/*  41 */     DialogHelper helper = new DialogHelper(sys, title, true);
/*     */ 
/*  43 */     JPanel wrapper = helper.initStandard(null, null, 2, false, null);
/*     */ 
/*  45 */     helper.m_gridHelper.m_gc.insets = new Insets(10, 20, 10, 20);
/*  46 */     JPanel mainPanel = new PanePanel();
/*  47 */     helper.addComponent(wrapper, mainPanel);
/*     */ 
/*  49 */     helper.makePanelGridBag(mainPanel, 2);
/*  50 */     helper.m_props = props;
/*     */ 
/*  52 */     helper.addLastComponentInRow(mainPanel, new CustomText(msg, 50));
/*  53 */     helper.addLastComponentInRow(mainPanel, new CustomLabel(labelStr));
/*  54 */     helper.addExchangeComponent(mainPanel, new CustomTextArea(5, 20), fieldName);
/*     */ 
/*  56 */     return helper.prompt();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static int promptMultiselect(SystemInterface sys, String title, String msg, String[] result, String labelStr, Component list)
/*     */   {
/*  64 */     IdcMessage idcmsg = IdcMessageFactory.lc();
/*  65 */     idcmsg.m_msgEncoded = msg;
/*  66 */     return promptMultiselect(sys, title, idcmsg, result, labelStr, list);
/*     */   }
/*     */ 
/*     */   public static int promptMultiselect(SystemInterface sys, String title, IdcMessage msg, String[] result, String labelStr, Component list)
/*     */   {
/*  72 */     DialogHelper helper = new DialogHelper(sys, title, true);
/*  73 */     JPanel panel = helper.initStandard(null, null, 1, false, null);
/*     */ 
/*  75 */     helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  76 */     helper.m_gridHelper.m_gc.weighty = 2.0D;
/*  77 */     helper.m_gridHelper.m_gc.insets = new Insets(10, 20, 10, 20);
/*     */ 
/*  79 */     helper.addLastComponentInRow(panel, list);
/*     */ 
/*  81 */     if (result != null)
/*     */     {
/*  83 */       helper.m_gridHelper.m_gc.weighty = 0.0D;
/*  84 */       helper.addLastComponentInRow(panel, new CustomText(sys.localizeMessage(msg), 80));
/*  85 */       helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  86 */       helper.addExchangeComponent(panel, new CustomTextArea(5, 50), "messageText");
/*     */     }
/*     */ 
/*  89 */     int rc = helper.prompt();
/*     */ 
/*  91 */     if (result != null)
/*     */     {
/*  93 */       result[0] = ((String)helper.m_props.get("messageText"));
/*     */     }
/*     */ 
/*  96 */     return rc;
/*     */   }
/*     */ 
/*     */   public static void showUrlMessage(SystemInterface sys, String title, String urlPath, String msg)
/*     */   {
/* 101 */     DialogHelper dlg = new DialogHelper(sys, title, true);
/* 102 */     dlg.initDialogType(1);
/*     */ 
/* 104 */     JPanel mainPanel = dlg.m_mainPanel;
/* 105 */     dlg.m_gridHelper.useGridBag(mainPanel);
/* 106 */     dlg.m_gridHelper.m_gc.anchor = 17;
/*     */ 
/* 108 */     if (msg == null)
/*     */     {
/* 110 */       msg = GuiText.m_unknownError;
/*     */     }
/*     */ 
/* 113 */     CustomText ct = new CustomText(msg, 120);
/* 114 */     dlg.addLastComponentInRow(mainPanel, ct);
/* 115 */     CustomTextField textField = new CustomTextField(urlPath, 80);
/* 116 */     textField.setEditable(false);
/* 117 */     dlg.m_gridHelper.m_gc.fill = 2;
/* 118 */     dlg.m_gridHelper.m_gc.weightx = 1.0D;
/* 119 */     dlg.addLastComponentInRow(mainPanel, textField);
/*     */ 
/* 121 */     dlg.prompt();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 126 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CommonDialogs
 * JD-Core Version:    0.5.4
 */