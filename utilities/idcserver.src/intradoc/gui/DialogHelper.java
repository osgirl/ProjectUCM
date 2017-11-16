/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.Help;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import java.awt.Container;
/*     */ import java.awt.Dialog;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Point;
/*     */ import java.awt.Toolkit;
/*     */ import java.awt.Window;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ 
/*     */ public class DialogHelper extends WindowHelper
/*     */   implements PromptHandler, ActionListener
/*     */ {
/*  49 */   public boolean m_isModal = true;
/*     */   public JDialog m_dialog;
/*     */   public String m_title;
/*     */   public Window m_parent;
/*     */   public JPanel m_toolbar;
/*     */   public String m_helpPage;
/*     */   public JScrollPane m_scrollPane;
/*  57 */   public int m_defaultResult = 0;
/*  58 */   public int m_result = 0;
/*     */   public JButton m_ok;
/*     */   public JButton m_cancel;
/*     */   public JButton m_reset;
/*     */   public JButton m_yes;
/*     */   public JButton m_yesAll;
/*     */   public JButton m_no;
/*     */   public JButton m_noAll;
/*     */   public JButton m_abort;
/*     */   public JButton m_retry;
/*     */   public JButton m_ignore;
/*     */   public JButton m_ignoreAll;
/*     */   public JButton m_help;
/*     */   public DialogCallback m_okCallback;
/*     */   public DialogCallback m_cancelCallback;
/*     */   public DialogCallback m_helpCallback;
/*     */   public DialogCallback m_resetCallback;
/*     */   public static final String APPLICATION_MODAL = "APPLICATION_MODAL";
/*     */   public static final String DOCUMENT_MODAL = "DOCUMENT_MODAL";
/*     */   public static final String MODELESS = "MODELESS";
/*     */   public static final String TOOLKIT_MODAL = "TOOLKIT_MODAL";
/*     */ 
/*     */   public DialogHelper()
/*     */   {
/*     */   }
/*     */ 
/*     */   public DialogHelper(SystemInterface sys, String title, boolean isModal)
/*     */   {
/*  91 */     init(sys, title, isModal, false);
/*     */   }
/*     */ 
/*     */   public DialogHelper(SystemInterface sys, String title, boolean isModal, boolean isScroll)
/*     */   {
/*  96 */     init(sys, title, isModal, isScroll);
/*     */   }
/*     */ 
/*     */   public DialogHelper(SystemInterface sys, Window parent, String title, boolean isModal, boolean isScroll)
/*     */   {
/* 101 */     this.m_parent = parent;
/* 102 */     init(sys, title, isModal, isScroll);
/*     */   }
/*     */ 
/*     */   protected void init(SystemInterface sys, String title, boolean isModal, boolean isScroll)
/*     */   {
/* 107 */     if ((sys != null) && (this.m_parent == null))
/*     */     {
/* 109 */       this.m_parent = sys.getMainWindow();
/*     */     }
/* 111 */     this.m_title = title;
/* 112 */     this.m_isModal = isModal;
/*     */ 
/* 114 */     JDialog dlg = null;
/* 115 */     if ((this.m_parent instanceof JFrame) || (this.m_parent == null))
/*     */     {
/* 117 */       dlg = new JDialog((JFrame)this.m_parent, title, isModal);
/*     */     }
/* 119 */     else if (this.m_parent instanceof Dialog)
/*     */     {
/* 121 */       dlg = new JDialog((Dialog)this.m_parent, title, isModal);
/*     */     }
/*     */ 
/* 124 */     Properties props = new Properties();
/* 125 */     attachToDialogEx(dlg, sys, props, isScroll);
/*     */   }
/*     */ 
/*     */   public void attachToDialog(JDialog dlg, SystemInterface sys, Properties props)
/*     */   {
/* 130 */     attachToDialogEx(dlg, sys, props, false);
/*     */   }
/*     */ 
/*     */   public void attachToDialogEx(JDialog dlg, SystemInterface sys, Properties props, boolean isScroll)
/*     */   {
/* 136 */     if ((this.m_parent != null) && (sys != null))
/*     */     {
/* 138 */       this.m_parent = sys.getMainWindow();
/*     */     }
/* 140 */     if (dlg == null)
/*     */     {
/* 142 */       if (this.m_parent instanceof JFrame)
/*     */       {
/* 144 */         dlg = new JDialog((JFrame)this.m_parent, this.m_isModal);
/*     */       }
/* 146 */       else if (this.m_parent instanceof Dialog)
/*     */       {
/* 148 */         dlg = new JDialog((Dialog)this.m_parent, this.m_isModal);
/*     */       }
/*     */     }
/* 151 */     if (this.m_title != null)
/*     */     {
/* 153 */       dlg.setTitle(this.m_title);
/*     */     }
/*     */ 
/* 156 */     attachToWindow(dlg, sys, props);
/* 157 */     this.m_dialog = dlg;
/* 158 */     this.m_exitOnClose = false;
/*     */ 
/* 160 */     if (this.m_isModal)
/*     */     {
/* 162 */       setModalityType("DOCUMENT_MODAL");
/*     */     }
/*     */ 
/* 165 */     this.m_toolbar = new PanePanel();
/* 166 */     this.m_mainPanel = new PanePanel();
/* 167 */     Container pnl = this.m_mainPanel;
/* 168 */     if (isScroll)
/*     */     {
/* 170 */       this.m_scrollPane = new JScrollPane(this.m_mainPanel);
/* 171 */       pnl = this.m_scrollPane;
/*     */     }
/* 173 */     this.m_dialog.add("Center", pnl);
/* 174 */     this.m_dialog.add("South", this.m_toolbar);
/*     */   }
/*     */ 
/*     */   public JPanel initStandard(ComponentBinder cmptBinder, DialogCallback callback, int fill, boolean enableHelp, String helpPage)
/*     */   {
/* 180 */     if (callback != null)
/*     */     {
/* 182 */       this.m_okCallback = callback;
/* 183 */       this.m_okCallback.m_dlgHelper = this;
/*     */     }
/*     */ 
/* 186 */     addOK(this.m_okCallback);
/* 187 */     addCancel(null);
/*     */ 
/* 190 */     if (enableHelp)
/*     */     {
/* 192 */       addHelpInfo(helpPage);
/*     */     }
/*     */ 
/* 195 */     if (cmptBinder != null)
/*     */     {
/* 197 */       this.m_componentBinder = cmptBinder;
/*     */     }
/* 199 */     makePanelGridBag(this.m_mainPanel, fill);
/*     */ 
/* 201 */     return this.m_mainPanel;
/*     */   }
/*     */ 
/*     */   public JButton addCommandButton(String label, ActionListener onClick)
/*     */   {
/* 206 */     JButton btn = new JButton(label);
/* 207 */     return addCommandButtonEx(btn, onClick);
/*     */   }
/*     */ 
/*     */   public JButton addCommandButtonEx(JButton btn, ActionListener onClick)
/*     */   {
/* 212 */     if (onClick != null)
/*     */     {
/* 214 */       btn.addActionListener(onClick);
/*     */     }
/* 216 */     this.m_toolbar.add(btn);
/*     */ 
/* 218 */     return btn;
/*     */   }
/*     */ 
/*     */   public void show()
/*     */   {
/* 223 */     this.m_dialog.pack();
/*     */ 
/* 226 */     if (this.m_scrollPane != null)
/*     */     {
/* 228 */       Dimension mainDim = this.m_mainPanel.getPreferredSize();
/* 229 */       mainDim.width = (((mainDim.width > 660) ? 660 : mainDim.width) + 30);
/* 230 */       mainDim.height = (((mainDim.height > 460) ? 460 : mainDim.height) + 20);
/*     */ 
/* 232 */       this.m_scrollPane.setSize(mainDim);
/*     */ 
/* 235 */       this.m_mainPanel.invalidate();
/*     */     }
/*     */ 
/* 238 */     Dimension dim = this.m_dialog.getPreferredSize();
/* 239 */     if ((this.m_parent != null) && (this.m_parent.isVisible()))
/*     */     {
/* 241 */       Point parentL = this.m_parent.getLocationOnScreen();
/* 242 */       Dimension parentS = this.m_parent.getSize();
/* 243 */       int width = ((dim.width > 1000) ? 1000 : dim.width) + 20;
/* 244 */       int height = ((dim.height > 660) ? 660 : dim.height) + 30;
/* 245 */       this.m_dialog.setBounds(parentL.x + (parentS.width - width) / 2, parentL.y + (parentS.height - height) / 2, width, height);
/*     */     }
/*     */     else
/*     */     {
/* 250 */       Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
/* 251 */       int width = (d.width - 800 > 0) ? (d.width - 800) / 2 : 10;
/* 252 */       int height = (d.height - 600 > 0) ? (d.height - 600) / 2 : 10;
/* 253 */       this.m_dialog.setBounds(width, height, dim.width, dim.height);
/*     */     }
/* 255 */     this.m_dialog.setVisible(true);
/*     */   }
/*     */ 
/*     */   public void addOK(DialogCallback callback)
/*     */   {
/* 260 */     this.m_okCallback = callback;
/* 261 */     this.m_ok = addCommandButton(GuiText.m_okLabel, this);
/*     */   }
/*     */ 
/*     */   public void addCancel(DialogCallback callback)
/*     */   {
/* 266 */     this.m_cancelCallback = callback;
/* 267 */     this.m_cancel = addCommandButton(GuiText.m_cancelLabel, this);
/*     */   }
/*     */ 
/*     */   public void addReset(DialogCallback callback)
/*     */   {
/* 272 */     this.m_resetCallback = callback;
/* 273 */     this.m_reset = addCommandButton(GuiText.m_resetLabel, this);
/*     */   }
/*     */ 
/*     */   public void addHelp(DialogCallback callback)
/*     */   {
/* 278 */     this.m_helpCallback = callback;
/* 279 */     this.m_help = addCommandButton(GuiText.m_helpLabel, this);
/*     */   }
/*     */ 
/*     */   public void addHelpInfo(String helpPage)
/*     */   {
/* 284 */     addHelp(null);
/* 285 */     this.m_helpPage = helpPage;
/* 286 */     if (this.m_helpPage != null)
/*     */       return;
/* 288 */     this.m_help.setEnabled(false);
/*     */   }
/*     */ 
/*     */   public void addClose(DialogCallback callback)
/*     */   {
/* 294 */     this.m_okCallback = callback;
/* 295 */     this.m_ok = addCommandButton(GuiText.m_closeLabel, this);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 300 */     loadComponentValues();
/* 301 */     show();
/* 302 */     return this.m_result;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 307 */     Object obj = e.getSource();
/* 308 */     DialogCallback callback = this.m_cancelCallback;
/* 309 */     this.m_result = this.m_defaultResult;
/* 310 */     if (obj == this.m_ok)
/*     */     {
/* 312 */       this.m_result = 1;
/* 313 */       callback = this.m_okCallback;
/* 314 */       if (!retrieveComponentValues())
/*     */       {
/* 316 */         return;
/*     */       }
/*     */     }
/* 319 */     else if (obj == this.m_yes)
/*     */     {
/* 321 */       this.m_result = 2;
/*     */     }
/* 323 */     else if (obj == this.m_yesAll)
/*     */     {
/* 325 */       this.m_result = 9;
/*     */     }
/* 327 */     else if (obj == this.m_no)
/*     */     {
/* 329 */       this.m_result = 3;
/*     */     }
/* 331 */     else if (obj == this.m_noAll)
/*     */     {
/* 333 */       this.m_result = 10;
/*     */     }
/* 335 */     else if (obj == this.m_abort)
/*     */     {
/* 337 */       this.m_result = 4;
/*     */     }
/* 339 */     else if (obj == this.m_retry)
/*     */     {
/* 341 */       this.m_result = 5;
/*     */     }
/* 343 */     else if (obj == this.m_ignore)
/*     */     {
/* 345 */       this.m_result = 6;
/*     */     }
/* 347 */     else if (obj == this.m_ignoreAll)
/*     */     {
/* 349 */       this.m_result = 11;
/*     */     }
/* 351 */     else if (obj == this.m_help)
/*     */     {
/* 353 */       this.m_result = 7;
/* 354 */       callback = this.m_helpCallback;
/*     */     }
/* 356 */     else if (obj == this.m_reset)
/*     */     {
/* 358 */       this.m_result = 8;
/* 359 */       callback = this.m_resetCallback;
/*     */     }
/*     */ 
/* 362 */     if (callback != null)
/*     */     {
/* 364 */       callback.m_promptResult = this.m_result;
/* 365 */       if (!callback.handleDialogEvent(e))
/*     */       {
/* 369 */         this.m_result = this.m_defaultResult;
/* 370 */         if (callback.m_errorMessage != null)
/*     */         {
/* 372 */           MessageBox.reportError(this.m_exchange.m_sysInterface, null, callback.m_errorMessage);
/*     */         }
/* 374 */         this.m_result = 0;
/* 375 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 379 */     if (this.m_result == 7)
/*     */     {
/* 381 */       if (this.m_helpPage != null)
/*     */       {
/*     */         try
/*     */         {
/* 385 */           ExecutionContext cxt = null;
/* 386 */           if ((this.m_exchange != null) && (this.m_exchange.m_sysInterface != null))
/*     */           {
/* 388 */             cxt = this.m_exchange.m_sysInterface.getExecutionContext();
/*     */           }
/* 390 */           Help.display(this.m_helpPage, cxt);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 394 */           Report.trace(null, "Error in launching.", exp);
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 399 */         MessageBox.reportError(this.m_exchange.m_sysInterface, null, GuiText.m_helpPageMissing);
/*     */       }
/*     */ 
/* 403 */       return;
/*     */     }
/*     */ 
/* 406 */     if (this.m_result == 8)
/*     */     {
/* 408 */       return;
/*     */     }
/*     */ 
/* 411 */     close();
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/* 416 */     this.m_dialog.setVisible(false);
/* 417 */     this.m_dialog.dispose();
/*     */   }
/*     */ 
/*     */   public void initDialogType(int type)
/*     */   {
/* 422 */     switch (type)
/*     */     {
/*     */     case 1:
/* 425 */       this.m_ok = addCommandButton(GuiText.m_okLabel, this);
/* 426 */       break;
/*     */     case 2:
/* 429 */       this.m_ok = addCommandButton(GuiText.m_okLabel, this);
/* 430 */       this.m_cancel = addCommandButton(GuiText.m_cancelLabel, this);
/* 431 */       break;
/*     */     case 64:
/* 434 */       this.m_ok = addCommandButton(GuiText.m_okLabel, this);
/* 435 */       this.m_cancel = addCommandButton(GuiText.m_cancelLabel, this);
/* 436 */       this.m_reset = addCommandButton(GuiText.m_resetLabel, this);
/* 437 */       break;
/*     */     case 4:
/* 440 */       this.m_yes = addCommandButton(GuiText.m_yesLabel, this);
/* 441 */       this.m_no = addCommandButton(GuiText.m_noLabel, this);
/* 442 */       break;
/*     */     case 8:
/* 445 */       this.m_yes = addCommandButton(GuiText.m_yesLabel, this);
/* 446 */       this.m_no = addCommandButton(GuiText.m_noLabel, this);
/* 447 */       this.m_cancel = addCommandButton(GuiText.m_cancelLabel, this);
/* 448 */       break;
/*     */     case 16:
/* 451 */       this.m_abort = addCommandButton(GuiText.m_abortLabel, this);
/* 452 */       this.m_retry = addCommandButton(GuiText.m_retryLabel, this);
/* 453 */       this.m_ignore = addCommandButton(GuiText.m_ignoreLabel, this);
/* 454 */       break;
/*     */     case 32:
/* 457 */       this.m_retry = addCommandButton(GuiText.m_retryLabel, this);
/* 458 */       this.m_cancel = addCommandButton(GuiText.m_cancelLabel, this);
/* 459 */       break;
/*     */     case 128:
/* 462 */       this.m_yes = addCommandButton(GuiText.m_yesLabel, this);
/* 463 */       this.m_yesAll = addCommandButton(GuiText.m_yesAllLabel, this);
/* 464 */       this.m_no = addCommandButton(GuiText.m_noLabel, this);
/* 465 */       this.m_noAll = addCommandButton(GuiText.m_noAllLabel, this);
/* 466 */       this.m_cancel = addCommandButton(GuiText.m_cancelLabel, this);
/* 467 */       break;
/*     */     case 256:
/* 470 */       this.m_ignore = addCommandButton(GuiText.m_ignoreLabel, this);
/* 471 */       this.m_ignoreAll = addCommandButton(GuiText.m_ignoreAllLabel, this);
/* 472 */       this.m_cancel = addCommandButton(GuiText.m_cancelLabel, this);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setModalityType(String modalityType)
/*     */   {
/*     */     try
/*     */     {
/* 481 */       Class dialogClass = this.m_dialog.getClass();
/* 482 */       Class modalityTypeClass = Class.forName("java.awt.Dialog$ModalityType");
/* 483 */       Enum e = Enum.valueOf(modalityTypeClass, modalityType);
/* 484 */       Method m = dialogClass.getMethod("setModalityType", new Class[] { modalityTypeClass });
/* 485 */       m.invoke(this.m_dialog, new Object[] { e });
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 489 */       if (modalityType.equals("MODELESS"))
/*     */       {
/* 491 */         this.m_dialog.setModal(false);
/*     */       }
/*     */       else
/*     */       {
/* 495 */         this.m_dialog.setModal(true);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 502 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80969 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.DialogHelper
 * JD-Core Version:    0.5.4
 */