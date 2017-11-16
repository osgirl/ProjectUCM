/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.Applications;
/*     */ import intradoc.common.Browser;
/*     */ import intradoc.common.GuiUtils;
/*     */ import intradoc.common.Help;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.LoggingUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Image;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.awt.event.MouseListener;
/*     */ import java.net.URL;
/*     */ import javax.swing.JApplet;
/*     */ 
/*     */ public class IntradocApp extends JApplet
/*     */   implements MouseListener, Parameters
/*     */ {
/*     */   String m_cgiURL;
/*     */   String m_imagesURL;
/*     */   String m_helpURL;
/*     */   String m_gifName;
/*     */   String m_appName;
/*     */   static URL m_cgiURLObject;
/*     */   Image m_appletImg;
/*     */   boolean m_mouseOver;
/*     */   boolean m_appActivating;
/*     */ 
/*     */   public IntradocApp()
/*     */   {
/*  53 */     this.m_cgiURL = "/intradoc-cgi/nph-idc_cgi.exe";
/*  54 */     this.m_imagesURL = "/images/";
/*  55 */     this.m_helpURL = "/help/";
/*  56 */     this.m_gifName = "logo.gif";
/*     */ 
/*  58 */     this.m_appName = "Intradoc App";
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/*  75 */     Applications apps = new Applications();
/*     */ 
/*  77 */     AppFrameHelper sysInterface = new AppFrameHelper();
/*  78 */     if (args.length < 1)
/*     */     {
/*  80 */       String msg = "Please specify an application to start. \n\nUsage: IntradocApp [-console] ";
/*  81 */       if (apps.APP_INFO.length > 0)
/*     */       {
/*  83 */         msg = msg + apps.APP_INFO[0][0];
/*  84 */         for (int i = 1; i < apps.APP_INFO.length; ++i)
/*     */         {
/*  86 */           msg = msg + " | " + apps.APP_INFO[i][0];
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/*  91 */         msg = "No applications are defined in Applications.APP_INFO table.";
/*     */       }
/*     */       try
/*     */       {
/*  95 */         AppLauncher.init("", true, m_cgiURLObject);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*  99 */         e.printStackTrace();
/*     */       }
/* 101 */       IdcMessage idcmsg = IdcMessageFactory.lc();
/* 102 */       idcmsg.m_msgLocalized = msg;
/* 103 */       AppLauncher.reportFatal(sysInterface, idcmsg);
/*     */     }
/*     */ 
/* 107 */     LoggingUtils.setLogFileMsgPrefix("IdcApp");
/*     */ 
/* 110 */     for (int i = 0; i < args.length; ++i)
/*     */     {
/* 112 */       if ((args[i].startsWith("-")) || (args[i].startsWith("/")))
/*     */         continue;
/* 114 */       createFrame(args[i], true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 135 */     String param = getParameter("APP-NAME");
/* 136 */     if (param != null)
/*     */     {
/* 138 */       this.m_appName = param;
/*     */     }
/*     */ 
/* 142 */     param = getParameter("CGI-URL");
/* 143 */     if (param != null)
/*     */     {
/* 145 */       this.m_cgiURL = param;
/*     */     }
/*     */ 
/* 149 */     param = getParameter("GIF-NAME");
/* 150 */     if (param != null)
/*     */     {
/* 152 */       this.m_gifName = param;
/*     */     }
/*     */ 
/* 156 */     param = getParameter("IMAGES-URL");
/* 157 */     if (param != null)
/*     */     {
/* 159 */       this.m_imagesURL = param;
/*     */     }
/*     */ 
/* 163 */     param = getParameter("HELP-URL");
/* 164 */     if (param != null)
/*     */     {
/* 166 */       this.m_helpURL = param;
/*     */     }
/*     */ 
/* 169 */     URL imageURLBase = null;
/* 170 */     URL helpURLBase = null;
/* 171 */     URL urlBase = getCodeBase();
/*     */     try
/*     */     {
/* 174 */       m_cgiURLObject = new URL(urlBase, this.m_cgiURL);
/* 175 */       imageURLBase = new URL(urlBase, this.m_imagesURL);
/* 176 */       helpURLBase = new URL(urlBase, this.m_helpURL);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 180 */       if (SystemUtils.m_verbose)
/*     */       {
/* 182 */         Report.debug("system", null, ignore);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 187 */     Browser.setAppletContext(getAppletContext(), urlBase);
/*     */ 
/* 189 */     if (imageURLBase != null)
/*     */     {
/* 191 */       GuiUtils.setGifAppletContext(getAppletContext(), imageURLBase);
/*     */     }
/*     */ 
/* 195 */     if (helpURLBase != null)
/*     */     {
/* 197 */       Help.setHelpUrlBase(helpURLBase);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 204 */       AppLauncher.setAppParameters(this);
/* 205 */       AppLauncher.pushAppletParametersToApplet();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 209 */       Report.trace("system", "Error pushing applet parameters to applet connection data", e);
/*     */     }
/*     */ 
/* 212 */     this.m_appletImg = GuiUtils.getAppImage(this.m_gifName);
/*     */ 
/* 214 */     addMouseListener(this);
/*     */   }
/*     */ 
/*     */   public void paint(Graphics g)
/*     */   {
/* 223 */     Dimension d = getSize();
/*     */ 
/* 225 */     if ((this.m_mouseOver) || (this.m_appActivating))
/*     */     {
/* 227 */       Color c = Color.gray;
/* 228 */       g.setColor(c);
/* 229 */       g.draw3DRect(1, 1, d.width - 2, d.height - 2, !this.m_appActivating);
/* 230 */       g.setColor(c.brighter());
/* 231 */       g.draw3DRect(2, 2, d.width - 4, d.height - 4, !this.m_appActivating);
/*     */     }
/*     */     else
/*     */     {
/* 235 */       Color c = Color.white;
/* 236 */       g.setColor(c);
/* 237 */       g.drawRect(0, 0, d.width, d.height);
/* 238 */       g.drawRect(1, 1, d.width - 2, d.height - 2);
/* 239 */       g.drawRect(2, 2, d.width - 4, d.height - 4);
/*     */     }
/*     */ 
/* 242 */     g.drawImage(this.m_appletImg, 3, 3, d.width - 5, d.height - 5, getBackground(), this);
/*     */   }
/*     */ 
/*     */   public void mouseEntered(MouseEvent e)
/*     */   {
/* 250 */     this.m_mouseOver = true;
/* 251 */     repaint();
/*     */   }
/*     */ 
/*     */   public void mouseExited(MouseEvent e)
/*     */   {
/* 256 */     this.m_mouseOver = false;
/* 257 */     repaint();
/*     */   }
/*     */ 
/*     */   public void mouseClicked(MouseEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void mousePressed(MouseEvent e)
/*     */   {
/* 267 */     activateApp();
/* 268 */     e.consume();
/*     */   }
/*     */ 
/*     */   public void activateApp()
/*     */   {
/* 273 */     Graphics g = getGraphics();
/*     */ 
/* 276 */     if ((g == null) || (this.m_appActivating))
/*     */     {
/* 278 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 283 */       this.m_appActivating = true;
/* 284 */       paint(g);
/* 285 */       showStatus("Loading...");
/* 286 */       createAppletFrame(this.m_appName, false);
/*     */     }
/*     */     finally
/*     */     {
/* 290 */       this.m_appActivating = false;
/* 291 */       showStatus("Ready...");
/* 292 */       paint(g);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void mouseReleased(MouseEvent e)
/*     */   {
/* 300 */     if (this.m_appActivating)
/*     */     {
/* 302 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 308 */       this.m_appActivating = true;
/* 309 */       createAppletFrame(this.m_appName, false);
/*     */     }
/*     */     finally
/*     */     {
/* 313 */       this.m_appActivating = false;
/*     */     }
/* 315 */     this.m_mouseOver = true;
/* 316 */     repaint();
/*     */   }
/*     */ 
/*     */   public void createAppletFrame(String appName, boolean isStandAlone)
/*     */   {
/* 321 */     AppLauncher.setAppParameters(this);
/* 322 */     createFrame(appName, isStandAlone);
/*     */   }
/*     */ 
/*     */   public static void createFrame(String appName, boolean isStandAlone)
/*     */   {
/*     */     try
/*     */     {
/* 329 */       AppLauncher.init(appName, isStandAlone, m_cgiURLObject);
/* 330 */       AppLauncher.launch(appName);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 334 */       IdcMessage msg = IdcMessageFactory.lc(LocaleUtils.createMessageListFromThrowable(t), "apUnableToStartApplication", new Object[] { appName });
/*     */ 
/* 336 */       Report.trace(null, null, t);
/* 337 */       SystemInterface sys = new AppFrameHelper();
/* 338 */       AppLauncher.reportFatal(sys, msg);
/* 339 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public String get(String key)
/*     */     throws DataException
/*     */   {
/* 346 */     return getParameter(key);
/*     */   }
/*     */ 
/*     */   public String getSystem(String key) throws DataException
/*     */   {
/* 351 */     return getParameter(key);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 357 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82538 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     IntradocApp
 * JD-Core Version:    0.5.4
 */