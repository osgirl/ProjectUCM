/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Window;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class MessageBox
/*     */ {
/*     */   public static final int OK_TYPE = 1;
/*     */   public static final int OKCANCEL_TYPE = 2;
/*     */   public static final int YESNO_TYPE = 4;
/*     */   public static final int YESNOCANCEL_TYPE = 8;
/*     */   public static final int ABORTRETRYIGNORE_TYPE = 16;
/*     */   public static final int RETRYCANCEL_TYPE = 32;
/*     */   public static final int OKCANCELRESET_TYPE = 64;
/*     */   public static final int YESNOALLCANCEL_TYPE = 128;
/*     */   public static final int IGNOREALLCANCEL_TYPE = 256;
/*     */   public static final int MAX_CUSTOM_TEXT_LINE_WIDTH = 80;
/*     */   public static final int MAX_LINE_SCROLLING_WIDTH = 120;
/*     */   public static final int MAX_NUM_LINES_SCROLLING_HEIGHT = 30;
/*     */ 
/*     */   public static int doMessage(SystemInterface sys, String msg, int type)
/*     */   {
/*  62 */     String title = GuiText.m_defaultTitle;
/*  63 */     if (sys != null)
/*     */     {
/*  65 */       title = sys.getAppName();
/*     */     }
/*  67 */     return doMessage(sys, title, msg, type);
/*     */   }
/*     */ 
/*     */   public static int doMessage(SystemInterface sys, String title, String msg, int type)
/*     */   {
/*  72 */     boolean doScrolling = testNeedScrolling(msg);
/*  73 */     DialogHelper dlg = new DialogHelper(sys, title, true, doScrolling);
/*  74 */     return showMessage(dlg, msg, type);
/*     */   }
/*     */ 
/*     */   public static int doMessage(SystemInterface sys, IdcMessage msg, int type)
/*     */   {
/*  79 */     IdcMessage titleMsg = IdcMessageFactory.lc();
/*  80 */     titleMsg.m_msgLocalized = GuiText.m_defaultTitle;
/*  81 */     if (sys != null)
/*     */     {
/*  83 */       String title = sys.getAppName();
/*  84 */       if (title != null)
/*     */       {
/*  86 */         titleMsg.m_msgLocalized = title;
/*     */       }
/*     */     }
/*  89 */     return doMessage(sys, titleMsg, msg, type);
/*     */   }
/*     */ 
/*     */   public static int doMessage(SystemInterface sys, IdcMessage title, IdcMessage msg, int type)
/*     */   {
/*  94 */     String msgText = localizeMessageSafe(sys, msg);
/*  95 */     String titleText = localizeMessageSafe(sys, title);
/*  96 */     boolean doScrolling = testNeedScrolling(msgText);
/*  97 */     DialogHelper dlg = new DialogHelper(sys, titleText, true, doScrolling);
/*  98 */     return showMessageInternal(dlg, msgText, type);
/*     */   }
/*     */ 
/*     */   public static int doMessage(JDialog dlg, String msg, int type)
/*     */   {
/* 103 */     boolean doScrolling = testNeedScrolling(msg);
/* 104 */     DialogHelper dlgHelper = new DialogHelper();
/* 105 */     dlgHelper.attachToDialogEx(dlg, null, null, doScrolling);
/* 106 */     return showMessage(dlgHelper, msg, type);
/*     */   }
/*     */ 
/*     */   public static boolean testNeedScrolling(String msg)
/*     */   {
/* 111 */     boolean doScrolling = false;
/*     */ 
/* 114 */     int lastNewLineIndex = 0;
/* 115 */     int prevWhiteSpaceIndex = 0;
/* 116 */     int newLineCount = 0;
/* 117 */     for (int i = 0; i < msg.length(); ++i)
/*     */     {
/* 119 */       char ch = msg.charAt(i);
/* 120 */       if (ch > ' ')
/*     */         continue;
/* 122 */       if (i - prevWhiteSpaceIndex > 120)
/*     */       {
/* 124 */         doScrolling = true;
/* 125 */         break;
/*     */       }
/* 127 */       prevWhiteSpaceIndex = i;
/* 128 */       if (ch != '\n')
/*     */         continue;
/* 130 */       newLineCount += 1 + (i - lastNewLineIndex) / 80;
/* 131 */       if (++newLineCount > 30)
/*     */       {
/* 133 */         doScrolling = true;
/* 134 */         break;
/*     */       }
/* 136 */       lastNewLineIndex = i;
/*     */     }
/*     */ 
/* 140 */     if (!doScrolling)
/*     */     {
/* 142 */       newLineCount += 1 + (msg.length() - lastNewLineIndex) / 80;
/* 143 */       if (newLineCount > 30)
/*     */       {
/* 145 */         doScrolling = true;
/*     */       }
/*     */     }
/* 148 */     if ((!doScrolling) && (msg.length() - prevWhiteSpaceIndex > 120))
/*     */     {
/* 150 */       doScrolling = true;
/*     */     }
/* 152 */     return doScrolling;
/*     */   }
/*     */ 
/*     */   public static int showMessage(DialogHelper dlg, String msgString, int type)
/*     */   {
/*     */     String msgText;
/*     */     String msgText;
/* 158 */     if (msgString == null)
/*     */     {
/* 160 */       Exception e = new Exception("A null message has been passed to MessageBox.showMessage()");
/*     */ 
/* 162 */       Report.trace("applet", null, e);
/* 163 */       msgText = GuiText.m_unknownError;
/*     */     }
/*     */     else
/*     */     {
/*     */       String msgText;
/* 168 */       if (dlg.m_exchange.m_sysInterface != null)
/*     */       {
/* 170 */         msgText = dlg.m_exchange.m_sysInterface.localizeMessage(msgString);
/*     */       }
/*     */       else
/*     */       {
/* 174 */         msgText = LocaleResources.localizeMessage(msgString, null);
/*     */       }
/*     */     }
/* 177 */     return showMessageInternal(dlg, msgText, type);
/*     */   }
/*     */ 
/*     */   public static int showMessage(DialogHelper dlg, IdcMessage msg, int type)
/*     */   {
/* 183 */     String msgText = localizeMessageSafe(dlg.m_exchange.m_sysInterface, msg);
/* 184 */     return showMessageInternal(dlg, msgText, type);
/*     */   }
/*     */ 
/*     */   public static int showMessageInternal(DialogHelper dlg, String text, int type)
/*     */   {
/* 189 */     dlg.initDialogType(type);
/*     */ 
/* 191 */     JPanel mainPanel = dlg.m_mainPanel;
/* 192 */     mainPanel.setLayout(new GridLayout(0, 1));
/*     */ 
/* 194 */     CustomText ct = new CustomText(text, 80);
/* 195 */     mainPanel.add(ct);
/*     */ 
/* 197 */     return dlg.prompt();
/*     */   }
/*     */ 
/*     */   public static void reportError(SystemInterface sys, Exception e)
/*     */   {
/* 202 */     Report.trace("applet", null, e);
/* 203 */     IdcMessage msg = LocaleUtils.createMessageListFromThrowable(e);
/* 204 */     reportError(sys, msg);
/*     */   }
/*     */ 
/*     */   public static int reportErrorEx(SystemInterface sys, Exception e, int type)
/*     */   {
/* 209 */     String title = GuiText.m_errorTitle;
/* 210 */     if (sys != null)
/*     */     {
/* 212 */       title = sys.getAppName();
/*     */     }
/*     */ 
/* 215 */     Report.trace("applet", null, e);
/* 216 */     IdcMessage msg = IdcMessageFactory.lc(e);
/* 217 */     return doMessage(sys, title, LocaleUtils.encodeMessage(msg), type);
/*     */   }
/*     */ 
/*     */   public static void reportError(SystemInterface sys, Exception e, String msg)
/*     */   {
/* 222 */     String outMsg = msg;
/* 223 */     if (e != null)
/*     */     {
/* 225 */       Report.trace("applet", null, e);
/* 226 */       IdcMessage idcMsg = IdcMessageFactory.lc(e);
/* 227 */       outMsg = LocaleUtils.appendMessage(LocaleUtils.encodeMessage(idcMsg), outMsg);
/*     */     }
/* 229 */     reportError(sys, outMsg);
/*     */   }
/*     */ 
/*     */   public static void reportError(SystemInterface sys, Exception e, IdcMessage msg)
/*     */   {
/* 234 */     if (e != null)
/*     */     {
/* 236 */       Report.trace("applet", null, e);
/* 237 */       IdcMessage tmp = msg;
/* 238 */       while (tmp.m_prior != null)
/*     */       {
/* 240 */         tmp = tmp.m_prior;
/*     */       }
/* 242 */       tmp.m_prior = LocaleUtils.createMessageListFromThrowable(e);
/*     */     }
/* 244 */     reportError(sys, msg);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void reportError(SystemInterface sys, String msg)
/*     */   {
/* 251 */     String title = GuiText.m_errorTitle;
/* 252 */     if (sys != null)
/*     */     {
/* 254 */       title = sys.getAppName();
/*     */     }
/* 256 */     doMessage(sys, title, msg, 1);
/*     */   }
/*     */ 
/*     */   public static void reportError(SystemInterface sys, IdcMessage msg)
/*     */   {
/* 261 */     IdcMessage titleMsg = IdcMessageFactory.lc();
/* 262 */     String title = GuiText.m_errorTitle;
/* 263 */     titleMsg.m_msgLocalized = title;
/* 264 */     if (sys != null)
/*     */     {
/* 266 */       title = sys.getAppName();
/* 267 */       if (title != null)
/*     */       {
/* 269 */         titleMsg.m_msgLocalized = title;
/*     */       }
/*     */     }
/* 272 */     doMessage(sys, titleMsg, msg, 1);
/*     */   }
/*     */ 
/*     */   public static void reportError(JFrame parent, String msg, String title)
/*     */   {
/* 277 */     JDialog dlg = new CustomDialog(parent, title, true);
/* 278 */     doMessage(dlg, msg, 1);
/*     */   }
/*     */ 
/*     */   public static void reportError(SystemInterface sys, Window parent, IdcMessage msg, IdcMessage title)
/*     */   {
/* 283 */     String localizedTitle = localizeMessageSafe(sys, title);
/* 284 */     String localizedMsg = localizeMessageSafe(sys, msg);
/* 285 */     DialogHelper dlg = new DialogHelper(sys, parent, localizedTitle, true, false);
/* 286 */     showMessageInternal(dlg, localizedMsg, 1);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void reportFatal(SystemInterface sys, Exception e, String msg)
/*     */   {
/* 293 */     reportError(sys, e, msg);
/* 294 */     if (sys != null)
/*     */     {
/* 296 */       sys.getMainWindow().dispose();
/*     */     }
/*     */     else
/*     */     {
/* 300 */       System.exit(0);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String localizeMessageSafe(SystemInterface sys, IdcMessage msg)
/*     */   {
/* 306 */     if (msg == null)
/*     */     {
/* 308 */       Exception e = new Exception("A null message has been passed to MessageBox.localizeMessageSafe()");
/*     */ 
/* 310 */       Report.trace("applet", null, e);
/* 311 */       return GuiText.m_unknownError;
/*     */     }
/* 313 */     if (sys == null)
/*     */     {
/* 316 */       return LocaleResources.localizeMessage(null, msg, null).toString();
/*     */     }
/* 318 */     return sys.localizeMessage(msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 323 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83709 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.MessageBox
 * JD-Core Version:    0.5.4
 */