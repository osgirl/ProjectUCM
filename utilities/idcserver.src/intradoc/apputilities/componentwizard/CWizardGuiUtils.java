/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.Help;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemListener;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ 
/*     */ public class CWizardGuiUtils
/*     */ {
/*  60 */   public static final String[][] LIST_COMMANDS = { { "csCompWizCommandAdd", "add" }, { "csCompWizCommandEdit", "edit" }, { "csCompWizCommandDelete2", "delete" } };
/*     */ 
/*  67 */   public static final String[][] LIST_INFO_COMMANDS = { { "csCompWizCommandInfo", "info" } };
/*     */ 
/*     */   public static void launchEditor(SystemInterface sys, String filename)
/*     */   {
/*  74 */     String editorPath = SharedObjects.getEnvironmentValue("HTMLEditorPath");
/*     */     try
/*     */     {
/*  77 */       if ((editorPath == null) || (editorPath.length() == 0))
/*     */       {
/*  79 */         throw new ServiceException("!csCompWizHTMLEditorPathLongError");
/*     */       }
/*  81 */       CWizardUtils.launchExe(editorPath, filename);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  85 */       reportError(sys, e.getMessage());
/*  86 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static JPanel addButton(JButton btn, ActionListener listener, boolean isEditable)
/*     */   {
/*  92 */     JPanel btnPanel = new PanePanel();
/*  93 */     btnPanel.setLayout(new BorderLayout());
/*  94 */     if (listener != null)
/*     */     {
/*  96 */       btn.addActionListener(listener);
/*     */     }
/*  98 */     btnPanel.add("West", btn);
/*  99 */     btn.setEnabled(isEditable);
/* 100 */     return btnPanel;
/*     */   }
/*     */ 
/*     */   public static void launchHelp(SystemInterface sys, String dlgName)
/*     */   {
/* 105 */     String errMsg = null;
/*     */ 
/* 107 */     if ((dlgName != null) && (dlgName.length() > 0))
/*     */     {
/* 109 */       String helpPage = DialogHelpTable.getHelpPage(dlgName);
/*     */       try
/*     */       {
/* 113 */         Help.display(helpPage, sys.getExecutionContext());
/*     */       }
/*     */       catch (ServiceException exp)
/*     */       {
/* 117 */         errMsg = LocaleUtils.encodeMessage("csCompWizHelpError", exp.getMessage(), helpPage);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 122 */       errMsg = "!csCompWizHelpNotFound";
/*     */     }
/*     */ 
/* 125 */     if (errMsg == null)
/*     */       return;
/* 127 */     reportError(sys, errMsg);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static int doMessage(SystemInterface sys, String msg, int type)
/*     */   {
/* 135 */     String title = LocaleResources.getString("csCompWizTitle", null);
/* 136 */     if (sys != null)
/*     */     {
/* 138 */       title = sys.getAppName();
/*     */     }
/* 140 */     return doMessage(sys, title, msg, type);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static int doMessage(SystemInterface sys, String title, String msg, int type)
/*     */   {
/* 147 */     DialogHelper dlg = new DialogHelper(sys, title, true);
/* 148 */     return MessageBox.showMessage(dlg, msg, type);
/*     */   }
/*     */ 
/*     */   public static int doMessage(SystemInterface sys, IdcMessage title, IdcMessage msg, int type)
/*     */   {
/* 153 */     if (title == null)
/*     */     {
/* 155 */       title = IdcMessageFactory.lc();
/* 156 */       if (sys != null)
/*     */       {
/* 158 */         title.m_msgLocalized = sys.getAppName();
/*     */       }
/*     */       else
/*     */       {
/* 162 */         title.m_msgLocalized = LocaleResources.getString("csCompWizTitle", null);
/*     */       }
/*     */     }
/* 165 */     DialogHelper dlg = new DialogHelper(sys, sys.localizeMessage(title), true);
/* 166 */     return MessageBox.showMessage(dlg, msg, type);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void reportError(SystemInterface sys, String msg)
/*     */   {
/* 173 */     String title = LocaleResources.getString("csCompWizErrorTitleDefault", null);
/* 174 */     if (sys != null)
/*     */     {
/* 176 */       title = sys.getAppName();
/*     */     }
/* 178 */     doMessage(sys, title, msg, 1);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void reportError(SystemInterface sys, Exception e, String msg)
/*     */   {
/* 185 */     IdcMessage idcmsg = null;
/* 186 */     if (msg != null)
/*     */     {
/* 188 */       idcmsg = IdcMessageFactory.lc();
/* 189 */       idcmsg.m_msgEncoded = msg;
/*     */     }
/* 191 */     reportError(sys, e, idcmsg);
/*     */   }
/*     */ 
/*     */   public static void reportError(SystemInterface sys, Exception e, IdcMessage msg)
/*     */   {
/* 196 */     if (SystemUtils.m_verbose)
/*     */     {
/* 198 */       Report.debug("componentwizard", e, msg);
/*     */     }
/* 200 */     if (e != null)
/*     */     {
/* 202 */       if (msg == null)
/*     */       {
/* 204 */         msg = IdcMessageFactory.lc(e);
/*     */       }
/*     */       else
/*     */       {
/* 208 */         msg.m_prior = IdcMessageFactory.lc(e);
/*     */       }
/*     */     }
/* 211 */     doMessage(sys, null, msg, 1);
/*     */   }
/*     */ 
/*     */   public static UdlPanel createUdlPanel(String listTitle, int width, int height, String resultSetName, boolean setVisibleColumns, String[][] columnMap, String idColumn, boolean forceVertical)
/*     */   {
/* 217 */     UdlPanel pnl = new UdlPanel(LocaleResources.localizeMessage(listTitle, null), null, width, height, resultSetName, setVisibleColumns);
/*     */ 
/* 220 */     if (forceVertical)
/*     */     {
/* 222 */       pnl.m_list.m_scrollPane.setVerticalScrollBarPolicy(22);
/*     */     }
/*     */ 
/* 226 */     pnl.init();
/* 227 */     pnl.useDefaultListener();
/*     */ 
/* 229 */     String displayColumns = "";
/* 230 */     if (columnMap != null)
/*     */     {
/* 232 */       int size = columnMap.length;
/*     */ 
/* 234 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 236 */         String column = columnMap[i][0];
/* 237 */         ColumnInfo info = new ColumnInfo(LocaleResources.localizeMessage(columnMap[i][1], null), column, Integer.parseInt(columnMap[i][2]));
/*     */ 
/* 239 */         pnl.setColumnInfo(info);
/* 240 */         if (!setVisibleColumns)
/*     */           continue;
/* 242 */         displayColumns = displayColumns + column;
/* 243 */         if (i >= size - 1)
/*     */           continue;
/* 245 */         displayColumns = displayColumns + ",";
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 252 */       displayColumns = idColumn;
/*     */     }
/*     */ 
/* 255 */     if ((displayColumns != null) && (displayColumns.length() > 0))
/*     */     {
/* 257 */       pnl.setVisibleColumns(displayColumns);
/*     */     }
/*     */ 
/* 260 */     if ((idColumn != null) && (idColumn.length() > 0))
/*     */     {
/* 262 */       pnl.setIDColumn(idColumn);
/*     */     }
/*     */ 
/* 265 */     return pnl;
/*     */   }
/*     */ 
/*     */   public static JPanel addUdlPanelCommandButtons(ContainerHelper helper, UdlPanel list, ActionListener listener, boolean isInfo)
/*     */   {
/* 271 */     JPanel btnPanel = new PanePanel();
/* 272 */     helper.makePanelGridBag(btnPanel, 1);
/*     */ 
/* 274 */     Insets insets = new Insets(5, 5, 5, 5);
/* 275 */     GridBagHelper gbh = helper.m_gridHelper;
/* 276 */     Insets oldInsets = gbh.m_gc.insets;
/*     */ 
/* 278 */     String[][] cmdInfo = LIST_COMMANDS;
/* 279 */     if (isInfo)
/*     */     {
/* 281 */       cmdInfo = LIST_INFO_COMMANDS;
/*     */     }
/*     */ 
/* 284 */     for (int i = 0; i < cmdInfo.length; ++i)
/*     */     {
/* 286 */       String command = cmdInfo[i][1];
/* 287 */       boolean flag = true;
/*     */ 
/* 289 */       if (command.equals("add"))
/*     */       {
/* 291 */         flag = false;
/*     */       }
/* 293 */       JButton btn = list.addButton(LocaleResources.getString(cmdInfo[i][0], null), flag);
/* 294 */       btn.setActionCommand(command);
/* 295 */       btn.addActionListener(listener);
/* 296 */       gbh.m_gc.insets = insets;
/* 297 */       gbh.prepareAddLastRowElement();
/* 298 */       helper.addComponent(btnPanel, btn);
/*     */     }
/*     */ 
/* 301 */     list.m_list.addActionListener(listener);
/* 302 */     gbh.m_gc.insets = oldInsets;
/* 303 */     return btnPanel;
/*     */   }
/*     */ 
/*     */   public static JPanel addWrapperPanel(ContainerHelper helper, boolean isCustom)
/*     */   {
/* 309 */     JPanel wrapper = null;
/* 310 */     if (isCustom)
/*     */     {
/* 312 */       wrapper = new CustomPanel();
/*     */     }
/*     */     else
/*     */     {
/* 316 */       wrapper = new PanePanel();
/*     */     }
/* 318 */     helper.makePanelGridBag(wrapper, 1);
/* 319 */     helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 320 */     helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 321 */     return wrapper;
/*     */   }
/*     */ 
/*     */   public static void addDescription(ContainerHelper helper, JPanel panel, String text) {
/* 325 */     GridBagHelper gbh = helper.m_gridHelper;
/* 326 */     GridBagConstraints gbc = gbh.m_gc;
/* 327 */     int gbcLeft = gbc.insets.left;
/* 328 */     int anchor = gbc.anchor;
/* 329 */     gbh.prepareAddLastRowElement();
/* 330 */     gbc.insets.left = 0;
/* 331 */     gbc.fill = 0;
/* 332 */     gbc.anchor = 17;
/* 333 */     helper.addComponent(panel, new CustomText(text, 100));
/* 334 */     gbc.fill = 1;
/* 335 */     gbc.insets.left = gbcLeft;
/* 336 */     gbc.anchor = anchor;
/*     */   }
/*     */ 
/*     */   public static void addCheckbox(ContainerHelper helper, JPanel panel, String label, String name, boolean isEditable, ItemListener listener, boolean isLastRow)
/*     */   {
/* 342 */     GridBagHelper gbh = helper.m_gridHelper;
/* 343 */     GridBagConstraints gbc = gbh.m_gc;
/* 344 */     JCheckBox checkbox = new CustomCheckbox(LocaleResources.getString(label, null));
/* 345 */     if (isLastRow)
/*     */     {
/* 347 */       gbh.prepareAddLastRowElement();
/*     */     }
/*     */     else
/*     */     {
/* 351 */       gbh.prepareAddRowElement();
/*     */     }
/* 353 */     gbc.fill = 0;
/* 354 */     gbc.anchor = 17;
/* 355 */     helper.addExchangeComponent(panel, checkbox, name);
/* 356 */     gbc.fill = 1;
/* 357 */     if (!isEditable)
/*     */     {
/* 359 */       checkbox.setEnabled(false);
/*     */     }
/* 361 */     if (listener == null)
/*     */       return;
/* 363 */     checkbox.addItemListener(listener);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 369 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80969 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.CWizardGuiUtils
 * JD-Core Version:    0.5.4
 */