/*     */ package intradoc.common;
/*     */ 
/*     */ import java.applet.Applet;
/*     */ import java.applet.AppletContext;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.util.Enumeration;
/*     */ import java.util.List;
/*     */ 
/*     */ public class Browser
/*     */ {
/*  35 */   protected static AppletContext m_appletContext = null;
/*  36 */   protected static String m_webBrowserPath = null;
/*  37 */   protected static URL m_defaultBaseURL = null;
/*     */ 
/*     */   public static void setPaths(String browserPath, String defaultBaseURL)
/*     */   {
/*  41 */     m_webBrowserPath = browserPath;
/*     */     try
/*     */     {
/*  44 */       m_defaultBaseURL = new URL(defaultBaseURL);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  48 */       Report.trace(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void setAppletContext(AppletContext appletContext, URL defaultBaseURL)
/*     */   {
/*  54 */     m_appletContext = appletContext;
/*  55 */     m_defaultBaseURL = defaultBaseURL;
/*     */   }
/*     */ 
/*     */   public static boolean hasAppletContext()
/*     */   {
/*  60 */     return m_appletContext != null;
/*     */   }
/*     */ 
/*     */   public static void showDocument(String appendedPath) throws ServiceException
/*     */   {
/*  65 */     showDocumentEx(null, null, null, appendedPath);
/*     */   }
/*     */ 
/*     */   public static String computeFullUrlString(String basePath, String relativePath)
/*     */   {
/*     */     try
/*     */     {
/*  72 */       URL baseURL = null;
/*  73 */       if (basePath == null)
/*     */       {
/*  75 */         baseURL = m_defaultBaseURL;
/*     */       }
/*     */       else
/*     */       {
/*  79 */         baseURL = new URL(basePath);
/*     */       }
/*  81 */       if (baseURL == null)
/*     */       {
/*  83 */         return relativePath;
/*     */       }
/*  85 */       URL tempURL = new URL(baseURL, relativePath);
/*  86 */       return tempURL.toString();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  90 */       Report.trace(null, null, e);
/*     */     }
/*  92 */     return relativePath;
/*     */   }
/*     */ 
/*     */   public static URL computeFullUrlPath(URL baseURL, String appendedPath)
/*     */     throws MalformedURLException
/*     */   {
/*  98 */     if (baseURL == null)
/*     */     {
/* 100 */       baseURL = m_defaultBaseURL;
/*     */     }
/* 102 */     URL tempURL = null;
/* 103 */     if (baseURL == null)
/*     */     {
/* 105 */       tempURL = new URL(appendedPath);
/*     */     }
/*     */     else
/*     */     {
/* 109 */       String queryStr = baseURL.getQuery();
/* 110 */       if ((queryStr != null) && (queryStr.length() > 0))
/*     */       {
/* 113 */         String baseUrlPathStr = baseURL.toString();
/* 114 */         String fullPath = baseUrlPathStr + appendedPath;
/* 115 */         tempURL = new URL(fullPath);
/*     */       }
/*     */       else
/*     */       {
/* 121 */         tempURL = new URL(baseURL, appendedPath);
/*     */       }
/*     */     }
/* 124 */     return tempURL;
/*     */   }
/*     */ 
/*     */   public static void showDocumentEx(String windowName, String basePath, URL baseURL, String appendedPath)
/*     */     throws ServiceException
/*     */   {
/* 135 */     String webBrowserPath = m_webBrowserPath;
/*     */     try
/*     */     {
/* 138 */       if (m_appletContext == null)
/*     */       {
/* 140 */         if (webBrowserPath == null)
/*     */         {
/*     */           try
/*     */           {
/* 144 */             NativeOsUtils utils = new NativeOsUtils();
/* 145 */             if (utils.isWindowsRegistrySupported())
/*     */             {
/* 147 */               String keyName = SystemUtils.getAppProperty("WebBrowserPathKey");
/* 148 */               if (keyName == null)
/*     */               {
/* 150 */                 keyName = "HKEY_CLASSES_ROOT\\http\\shell\\open\\command";
/*     */               }
/* 152 */               webBrowserPath = utils.getRegistryValue(keyName);
/*     */             }
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/* 157 */             Report.trace(null, "unable to find web browser from registry", t);
/*     */           }
/*     */         }
/* 160 */         if (webBrowserPath == null)
/*     */         {
/* 164 */           String[] browserList = { "safari", "firefox", "opera", "konqueror", "iexplore" };
/*     */           try
/*     */           {
/* 170 */             NativeOsUtils utils = new NativeOsUtils();
/* 171 */             String pathString = utils.getEnv("PATH");
/* 172 */             if (pathString == null)
/*     */             {
/* 174 */               pathString = utils.getEnv("Path");
/*     */             }
/* 176 */             if (pathString == null)
/*     */             {
/* 178 */               pathString = utils.getEnv("path");
/*     */             }
/*     */ 
/* 181 */             List l = StringUtils.makeListFromSequence(pathString, ':', '^', 0);
/* 182 */             for (String dir : l)
/*     */             {
/* 184 */               for (String browser : browserList)
/*     */               {
/* 186 */                 browser = dir + "/" + browser + EnvUtils.getExecutableFileSuffix();
/* 187 */                 if (FileUtils.checkFile(browser, true, false) != 0)
/*     */                   continue;
/* 189 */                 webBrowserPath = FileUtils.fileSlashes(browser);
/* 190 */                 break;
/*     */               }
/*     */ 
/* 193 */               if (webBrowserPath != null) {
/*     */                 break;
/*     */               }
/*     */             }
/*     */ 
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/* 201 */             Report.trace(null, "unable to find web browser from path", t);
/*     */           }
/*     */         }
/*     */ 
/* 205 */         if (webBrowserPath == null)
/*     */         {
/* 207 */           throw new ServiceException(null, "syBrowserNotFound", new Object[0]);
/*     */         }
/* 209 */         FileUtils.validatePath(webBrowserPath, IdcMessageFactory.lc("syBrowserNotFound", new Object[0]), 1);
/*     */ 
/* 211 */         String[] cmdBuff = new String[2];
/* 212 */         cmdBuff[0] = webBrowserPath;
/* 213 */         String docUrlPath = null;
/* 214 */         if (basePath != null)
/*     */         {
/* 216 */           docUrlPath = "file:///" + basePath + appendedPath;
/*     */         }
/*     */         else
/*     */         {
/* 220 */           URL tempURL = computeFullUrlPath(baseURL, appendedPath);
/* 221 */           docUrlPath = tempURL.toString();
/*     */         }
/* 223 */         cmdBuff[1] = StringUtils.urlEscape7Bit(docUrlPath, '%', FileUtils.m_javaSystemEncoding);
/*     */ 
/* 225 */         Runtime run = Runtime.getRuntime();
/* 226 */         Process proc = run.exec(cmdBuff);
/*     */ 
/* 229 */         InputStream procIn = proc.getInputStream();
/* 230 */         InputStream procErr = proc.getErrorStream();
/* 231 */         Thread errThread = new Thread("Browser stderr", procErr)
/*     */         {
/*     */           public void run()
/*     */           {
/* 236 */             StringBuffer buff = new StringBuffer();
/*     */             try
/*     */             {
/* 239 */               byte[] errbuf = new byte[1024];
/*     */ 
/* 241 */               while ((count = this.val$procErr.read(errbuf)) > 0)
/*     */               {
/*     */                 int count;
/* 243 */                 String str = new String(errbuf, 0, count);
/* 244 */                 buff.append(str);
/*     */               }
/*     */             }
/*     */             catch (Exception ignore)
/*     */             {
/* 249 */               Report.trace("system", "Error reading stderr from browser process", ignore);
/*     */             }
/*     */             finally
/*     */             {
/* 253 */               if (buff.length() > 0)
/*     */               {
/* 255 */                 Report.trace("system", buff.toString(), null);
/*     */               }
/*     */             }
/*     */           }
/*     */         };
/* 261 */         errThread.start();
/*     */ 
/* 263 */         Thread thread = new Thread("Browser stdout", procIn)
/*     */         {
/*     */           public void run()
/*     */           {
/* 268 */             StringBuffer strBuff = new StringBuffer();
/*     */             try
/*     */             {
/* 271 */               byte[] buf = new byte[1024];
/* 272 */               int nread = 0;
/* 273 */               while ((nread = this.val$procIn.read(buf)) > 0)
/*     */               {
/* 275 */                 String str = new String(buf, 0, nread);
/* 276 */                 strBuff.append(str);
/*     */               }
/*     */             }
/*     */             catch (Exception ignore)
/*     */             {
/* 281 */               Report.trace("system", "Error reading stdout from browser process", ignore);
/*     */             }
/*     */             finally
/*     */             {
/* 285 */               if (strBuff.length() > 0)
/*     */               {
/* 287 */                 Report.trace("system", strBuff.toString(), null);
/*     */               }
/*     */             }
/*     */           }
/*     */         };
/* 292 */         thread.start();
/*     */       }
/*     */       else
/*     */       {
/* 296 */         URL tempURL = computeFullUrlPath(baseURL, appendedPath);
/*     */         try
/*     */         {
/* 299 */           Enumeration e = m_appletContext.getApplets();
/* 300 */           boolean isActive = false;
/* 301 */           while ((e != null) && (e.hasMoreElements()) && 
/* 303 */             (e.hasMoreElements()))
/*     */           {
/* 305 */             Applet a = (Applet)e.nextElement();
/* 306 */             if (a.isActive())
/*     */             {
/* 308 */               isActive = true;
/*     */             }
/*     */           }
/*     */ 
/* 312 */           if (!isActive)
/*     */           {
/* 314 */             throw new ServiceException("!syBrowserLocationChanged");
/*     */           }
/* 316 */           if (windowName == null)
/*     */           {
/* 318 */             windowName = "IntradocSupportBrowser";
/*     */           }
/* 320 */           m_appletContext.showDocument(tempURL, "IntradocSupportBrowser");
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/* 324 */           Report.trace(null, "Error attempting to launch browser window.", t);
/* 325 */           throw new ServiceException("!syBrowserLaunchingError", t);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 331 */       throw new ServiceException(LocaleUtils.encodeMessage("syBrowserInterrupted", null, e.getMessage()));
/*     */     }
/*     */ 
/* 334 */     m_webBrowserPath = webBrowserPath;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 339 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79131 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.Browser
 * JD-Core Version:    0.5.4
 */