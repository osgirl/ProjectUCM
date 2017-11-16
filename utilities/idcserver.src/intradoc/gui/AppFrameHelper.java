/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Image;
/*     */ import java.awt.Toolkit;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.border.EmptyBorder;
/*     */ 
/*     */ public class AppFrameHelper extends WindowHelper
/*     */   implements SystemInterface
/*     */ {
/*     */   public JFrame m_appFrame;
/*     */   public String m_appName;
/*     */   public IdcMessage m_appTitle;
/*     */   public ExecutionContext m_cxt;
/*     */   public StatusBar m_statusBar;
/*     */ 
/*     */   public AppFrameHelper()
/*     */   {
/*  61 */     this.m_appFrame = null;
/*  62 */     this.m_statusBar = null;
/*  63 */     this.m_appName = "IDC";
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void attachToAppFrame(JFrame frame, Properties appProps, Image logo, String title)
/*     */   {
/*  71 */     IdcMessage msg = IdcMessageFactory.lc();
/*  72 */     msg.m_msgEncoded = title;
/*  73 */     this.m_appName = title;
/*  74 */     attachToAppFrame(frame, appProps, logo, msg);
/*     */   }
/*     */ 
/*     */   public void attachToAppFrame(JFrame frame, Properties appProps, Image logo, IdcMessage title)
/*     */   {
/*  81 */     attachToWindow(frame, this, appProps);
/*  82 */     frame.setDefaultCloseOperation(0);
/*     */ 
/*  84 */     this.m_appTitle = title;
/*  85 */     this.m_appFrame = frame;
/*     */ 
/*  87 */     JPanel header = new PanePanel();
/*  88 */     header.setLayout(new GridLayout(1, 0));
/*  89 */     if (logo != null)
/*     */     {
/*  91 */       ImageLabel img = new ImageLabel(300, 50, true);
/*  92 */       img.setImage(logo);
/*  93 */       header.add(img);
/*     */     }
/*  95 */     String titleText = LocaleResources.localizeMessage(null, title, null).toString();
/*  96 */     this.m_appName = titleText;
/*  97 */     CustomLabel headerLabel = new CustomLabel(titleText, 3);
/*  98 */     headerLabel.setBorder(new EmptyBorder(5, 5, 0, 0));
/*  99 */     header.add(headerLabel);
/*     */ 
/* 101 */     this.m_appFrame.setLayout(new BorderLayout());
/*     */ 
/* 103 */     this.m_appFrame.add("North", header);
/* 104 */     this.m_appFrame.add("Center", this.m_mainPanel = new PanePanel());
/* 105 */     this.m_appFrame.add("South", this.m_statusBar = new StatusBar());
/*     */ 
/* 107 */     if (EnvUtils.isLinux11())
/*     */     {
/* 110 */       this.m_appFrame.setLocation(10, 10);
/*     */     }
/* 112 */     this.m_statusBar.setText(GuiText.m_readyText);
/*     */     try
/*     */     {
/* 116 */       frame.setLocationByPlatform(true);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 122 */       Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
/*     */ 
/* 125 */       int width = (d.width - 800 > 0) ? (d.width - 800) / 2 : 10;
/* 126 */       int height = (d.height - 600 > 0) ? (d.height - 600) / 2 : 10;
/* 127 */       frame.setBounds(width, height, 50, 50);
/*     */     }
/*     */   }
/*     */ 
/*     */   public JFrame getMainWindow()
/*     */   {
/* 137 */     return this.m_appFrame;
/*     */   }
/*     */ 
/*     */   public void displayStatus(String str)
/*     */   {
/* 142 */     this.m_statusBar.setText(str);
/*     */   }
/*     */ 
/*     */   public void displayStatus(IdcMessage msg)
/*     */   {
/* 147 */     String str = localizeMessage(msg);
/* 148 */     this.m_statusBar.setText(str);
/*     */   }
/*     */ 
/*     */   public String getAppName()
/*     */   {
/* 153 */     return this.m_appName;
/*     */   }
/*     */ 
/*     */   public ExecutionContext getExecutionContext()
/*     */   {
/* 158 */     return this.m_cxt;
/*     */   }
/*     */ 
/*     */   public String localizeMessage(String msg)
/*     */   {
/* 163 */     return LocaleResources.localizeMessage(msg, this.m_cxt);
/*     */   }
/*     */ 
/*     */   public String localizeMessage(IdcMessage msg)
/*     */   {
/* 168 */     return LocaleResources.localizeMessage(null, msg, this.m_cxt).toString();
/*     */   }
/*     */ 
/*     */   public String localizeCaption(String msg)
/*     */   {
/* 173 */     msg = LocaleUtils.encodeMessage("syCaptionWrapper", null, msg);
/*     */ 
/* 175 */     msg = LocaleResources.localizeMessage(msg, this.m_cxt);
/* 176 */     return msg;
/*     */   }
/*     */ 
/*     */   public String getString(String str)
/*     */   {
/* 181 */     return LocaleResources.getString(str, this.m_cxt);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String getValidationErrorMessage(String fieldName, String value)
/*     */   {
/* 188 */     String keyName = "apFieldValidationError_" + fieldName;
/* 189 */     String messageText = LocaleResources.getStringInternal(keyName, this.m_cxt);
/*     */ 
/* 191 */     if (messageText != null)
/*     */     {
/* 193 */       return LocaleUtils.encodeMessage(keyName, null, fieldName, value);
/*     */     }
/*     */ 
/* 196 */     return null;
/*     */   }
/*     */ 
/*     */   public IdcMessage getValidationErrorMessageObject(String fieldName, String value, Map options)
/*     */   {
/* 201 */     String keyName = "apFieldValidationError_" + fieldName;
/* 202 */     String messageText = LocaleResources.getStringInternal(keyName, this.m_cxt);
/*     */ 
/* 204 */     if (messageText != null)
/*     */     {
/* 206 */       return IdcMessageFactory.lc(keyName, new Object[] { fieldName, value });
/*     */     }
/* 208 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 213 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79221 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.AppFrameHelper
 * JD-Core Version:    0.5.4
 */