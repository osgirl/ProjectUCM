/*      */ package intradoc.apps.shared;
/*      */ 
/*      */ import intradoc.client.UrlClient;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.GuiUtils;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemInterface;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.data.SimpleParameters;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.serialize.DataBinderSerializer;
/*      */ import intradoc.shared.AdditionalRenditions;
/*      */ import intradoc.shared.AliasData;
/*      */ import intradoc.shared.ArchiveCollections;
/*      */ import intradoc.shared.Collaborations;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.LocaleLoader;
/*      */ import intradoc.shared.RoleDefinitions;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedLoader;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserProfileData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import intradoc.shared.Users;
/*      */ import intradoc.shared.schema.SchemaLoader;
/*      */ import intradoc.shared.schema.SchemaResultSet;
/*      */ import intradoc.shared.schema.SchemaViewConfig;
/*      */ import intradoc.shared.workflow.WorkflowData;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.awt.Cursor;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.IOException;
/*      */ import java.io.PrintStream;
/*      */ import java.io.StringReader;
/*      */ import java.net.URL;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Observer;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.swing.JFrame;
/*      */ 
/*      */ public class AppLauncher
/*      */ {
/*   90 */   protected static boolean m_isInitialized = false;
/*   91 */   protected static long m_lastActionTS = 0L;
/*   92 */   protected static int m_bgTimer = 3000;
/*      */ 
/*   94 */   protected static String m_user = null;
/*   95 */   protected static UserData m_userData = null;
/*   96 */   protected static ExecutionContext m_cxt = null;
/*   97 */   protected static IdcDateFormat m_dateFormat = null;
/*   98 */   protected static boolean m_isInitialConfigLoaded = false;
/*      */ 
/*  101 */   protected static UrlClient m_urlClient = null;
/*  102 */   protected static ReportProgress m_serverProgress = null;
/*  103 */   protected static ReportProgress m_archiverProgress = null;
/*      */ 
/*  105 */   protected static SplashFrame m_splashFrame = null;
/*      */ 
/*  107 */   protected static boolean m_isStandAlone = true;
/*  108 */   protected static StandAloneApp m_standAlone = null;
/*  109 */   protected static Parameters m_appParameters = null;
/*      */ 
/*  111 */   protected static Applications m_apps = new Applications();
/*  112 */   protected static Hashtable m_activeApps = new Hashtable();
/*      */ 
/*  114 */   protected static ActiveAppInfo m_activeSubjects = null;
/*  115 */   protected static ActiveAppInfo m_activeTopics = null;
/*  116 */   protected static ActiveAppInfo m_activeMonikers = null;
/*      */   protected static boolean m_loaderInitDone;
/*      */ 
/*      */   public static void setAppParameters(Parameters appParameters)
/*      */   {
/*  126 */     m_appParameters = appParameters;
/*      */   }
/*      */ 
/*      */   public static void init(String appName, boolean isStandAlone, URL cgiUrl)
/*      */     throws DataException
/*      */   {
/*  132 */     if (m_isInitialized == true)
/*      */     {
/*  134 */       return;
/*      */     }
/*      */ 
/*  137 */     m_isStandAlone = isStandAlone;
/*  138 */     if (!m_isStandAlone)
/*      */     {
/*  141 */       m_bgTimer = 30000;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  146 */       if (m_isStandAlone)
/*      */       {
/*  148 */         m_splashFrame = new SplashFrame();
/*  149 */         m_standAlone = new StandAloneApp();
/*  150 */         m_standAlone.init(appName, m_splashFrame);
/*  151 */         m_user = m_standAlone.getUser();
/*  152 */         m_cxt = m_standAlone.getUserContext();
/*  153 */         IdcLocale locale = (IdcLocale)m_cxt.getCachedObject("UserLocale");
/*  154 */         if (locale == null)
/*      */         {
/*  156 */           locale = LocaleResources.getSystemLocale();
/*      */         }
/*  158 */         m_dateFormat = locale.m_dateFormat;
/*      */       }
/*      */       else
/*      */       {
/*  162 */         SharedObjects.init();
/*  163 */         m_urlClient = new UrlClient(cgiUrl);
/*  164 */         pushAppletParametersToApplet();
/*  165 */         m_loaderInitDone = false;
/*      */       }
/*      */ 
/*  168 */       loadEnvironmentValues();
/*  169 */       initConfig();
/*  170 */       m_isInitialized = true;
/*  171 */       ping();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  175 */       IdcMessage error = IdcMessageFactory.lc("apFailedToInitialize", new Object[0]);
/*  176 */       Report.trace("applet", null, e);
/*  177 */       throw new DataException(e, error);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void pushAppletParametersToApplet() throws DataException
/*      */   {
/*  183 */     if (m_urlClient == null)
/*      */     {
/*  185 */       return;
/*      */     }
/*  187 */     if (m_appParameters == null)
/*      */       return;
/*  189 */     int numParams = NumberUtils.parseInteger(m_appParameters.getSystem("NUMSESSIONKEYS"), -1);
/*      */ 
/*  191 */     if (numParams > 0)
/*      */     {
/*  193 */       m_urlClient.m_sessionIDList = new String[numParams][2];
/*  194 */       for (int i = 0; i < numParams; ++i)
/*      */       {
/*  196 */         String key = m_appParameters.getSystem("SESSIONKEY" + i);
/*  197 */         String val = m_appParameters.getSystem("SESSIONID" + i);
/*  198 */         m_urlClient.m_sessionIDList[i][0] = key;
/*  199 */         m_urlClient.m_sessionIDList[i][1] = val;
/*      */       }
/*      */     }
/*  202 */     String param = m_appParameters.getSystem("SOCKETS");
/*  203 */     if ((param == null) || (!StringUtils.convertToBool(param, false))) {
/*      */       return;
/*      */     }
/*  206 */     String name = m_appParameters.getSystem("APPLET-USER");
/*  207 */     String hash = m_appParameters.getSystem("APPLET-CODE");
/*  208 */     m_urlClient.setSocketParams(name, hash);
/*      */   }
/*      */ 
/*      */   public static void init(String appName, boolean isStandAlone, String[][] appInfos)
/*      */     throws DataException
/*      */   {
/*  216 */     if (m_isInitialized)
/*      */     {
/*  218 */       return;
/*      */     }
/*      */ 
/*  221 */     m_isStandAlone = isStandAlone;
/*  222 */     if (m_isStandAlone)
/*      */     {
/*  225 */       m_bgTimer = 30000;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  230 */       if (m_isStandAlone)
/*      */       {
/*  232 */         m_splashFrame = new SplashFrame();
/*  233 */         m_standAlone = new StandAloneApp();
/*  234 */         m_standAlone.init(appName, m_splashFrame);
/*  235 */         m_user = m_standAlone.getUser();
/*  236 */         m_cxt = m_standAlone.getUserContext();
/*  237 */         IdcLocale locale = (IdcLocale)m_cxt.getCachedObject("UserLocale");
/*  238 */         m_dateFormat = locale.m_dateFormat;
/*      */       }
/*      */ 
/*  241 */       loadEnvironmentValues();
/*  242 */       initConfig();
/*      */ 
/*  244 */       Hashtable map = new Hashtable();
/*  245 */       m_apps.loadInfo(appInfos, map);
/*      */ 
/*  247 */       m_isInitialized = true;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  251 */       IdcMessage error = IdcMessageFactory.lc("apFailedToInitialize", new Object[0]);
/*  252 */       throw new DataException(e, error);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadEnvironmentValues()
/*      */   {
/*      */     try
/*      */     {
/*  261 */       if (m_appParameters != null)
/*      */       {
/*  263 */         String[] keys = { "IDC_Name", "InstanceMenuLabel", "IntanceDescription", "UserIsAdmin", "UserAppRights" };
/*      */ 
/*  265 */         for (int i = 0; i < keys.length; ++i)
/*      */         {
/*  267 */           String key = keys[i];
/*  268 */           String param = m_appParameters.get(key);
/*      */ 
/*  270 */           if (param == null)
/*      */             continue;
/*  272 */           SharedObjects.putEnvironmentValue(key, param);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/*  279 */       ignore.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void initConfig()
/*      */     throws ServiceException
/*      */   {
/*  286 */     m_activeTopics = new ActiveAppInfo("topic", null, false, m_cxt);
/*  287 */     m_activeMonikers = new ActiveAppInfo("moniker", null, true, m_cxt);
/*      */ 
/*  290 */     m_activeSubjects = new ActiveAppInfo("subject", new String[] { "config" }, false, m_cxt);
/*  291 */     m_activeSubjects.init();
/*      */ 
/*  296 */     if ((!m_isStandAlone) || (getIsHeavyClient()))
/*      */     {
/*  298 */       Hashtable strings = new Hashtable();
/*  299 */       DataBinder binder = new DataBinder();
/*      */       try
/*      */       {
/*  303 */         DataSerializeUtils.setDataSerialize(new DataBinderSerializer());
/*  304 */         m_cxt = new ExecutionContextAdaptor();
/*      */ 
/*  308 */         IdcDateFormat fmt = (IdcDateFormat)LocaleResources.m_iso8601Format.clone();
/*  309 */         fmt.setTZ(LocaleResources.UTC);
/*  310 */         binder.m_blDateFormat = fmt;
/*  311 */         binder.putLocal("UserDateFormat", "yyyy-MM-dd HH:mm:ss!tUTC");
/*  312 */         executeService("LOAD_USER_LOCALIZATION", binder);
/*      */ 
/*  314 */         String statusCodeString = binder.getLocal("StatusCode");
/*  315 */         int code = NumberUtils.parseInteger(statusCodeString, 0);
/*  316 */         if (code != 0)
/*      */         {
/*  318 */           throw new ServiceException(code, binder.getLocal("StatusMessage"));
/*      */         }
/*  320 */         DataResultSet encodingMap = (DataResultSet)binder.getResultSet("IsoJavaEncodingMap");
/*  321 */         DataSerializeUtils.setEncodingMap(encodingMap);
/*      */ 
/*  323 */         LocaleLoader.readStringsFromBinder(strings, binder);
/*  324 */         SecurityUtils.init();
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  328 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/*  333 */         if (getIsHeavyClient());
/*  337 */         LocaleLoader.loadLocaleConfig(strings, new Hashtable(), binder);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  341 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/*  344 */       LocaleLoader.configureUserLocale(binder, m_cxt);
/*  345 */       m_dateFormat = (IdcDateFormat)m_cxt.getLocaleResource(3);
/*      */ 
/*  347 */       LocaleLoader.doStaticLocalization(m_cxt);
/*  348 */       SharedLoader.postInitialize();
/*      */     }
/*      */ 
/*  353 */     m_activeTopics = new ActiveAppInfo("topic", new String[] { "appcommongui" }, false, m_cxt);
/*      */ 
/*  355 */     m_activeTopics.init();
/*      */ 
/*  357 */     m_apps = (Applications)ComponentClassFactory.createClassInstance("Applications", "intradoc.apps.shared.Applications", LocaleResources.getString("apUnableToLoadAppInfo", m_cxt));
/*      */ 
/*  361 */     m_apps.init(m_isStandAlone, m_activeSubjects.getAllObjects(), m_cxt);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void reportOperationError(SystemInterface sys, String msg)
/*      */   {
/*  368 */     IdcMessage idcmsg = IdcMessageFactory.lc();
/*  369 */     idcmsg.m_msgEncoded = msg;
/*  370 */     reportOperationError(sys, null, idcmsg);
/*      */   }
/*      */ 
/*      */   public static void reportOperationError(SystemInterface sys, Throwable t, IdcMessage msg)
/*      */   {
/*      */     try
/*      */     {
/*  377 */       JFrame f = null;
/*  378 */       if (sys != null)
/*      */       {
/*  380 */         f = sys.getMainWindow();
/*      */       }
/*  382 */       if (f == null)
/*      */       {
/*  384 */         if (m_splashFrame == null)
/*      */         {
/*  386 */           m_splashFrame = new SplashFrame();
/*      */         }
/*  388 */         f = m_splashFrame;
/*      */       }
/*  390 */       if (t != null)
/*      */       {
/*  392 */         IdcMessage tmp = LocaleUtils.createMessageListFromThrowable(t);
/*  393 */         if (msg == null)
/*      */         {
/*  395 */           msg = tmp;
/*      */         }
/*      */         else
/*      */         {
/*  399 */           msg.m_prior = tmp;
/*      */         }
/*      */       }
/*  402 */       MessageBox.reportError(sys, f, msg, IdcMessageFactory.lc("apTitleContentServerMessage", new Object[0]));
/*      */     }
/*      */     catch (NoClassDefFoundError e)
/*      */     {
/*  406 */       Report.trace("applet", null, e);
/*  407 */       String exceptionMessage = e.getMessage();
/*  408 */       if (exceptionMessage != null)
/*      */       {
/*  410 */         System.out.println(exceptionMessage);
/*      */       }
/*  412 */       String text = LocaleResources.localizeMessage(null, msg, null).toString();
/*  413 */       System.out.println(text);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void reportError(String msg)
/*      */   {
/*  421 */     reportOperationError(null, msg);
/*      */   }
/*      */ 
/*      */   public static void reportError(SystemInterface sys, IdcMessage msg)
/*      */   {
/*  426 */     reportOperationError(sys, null, msg);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void reportFatal(String msg)
/*      */   {
/*  433 */     reportError(msg);
/*  434 */     if (!m_isStandAlone)
/*      */       return;
/*  436 */     System.exit(0);
/*      */   }
/*      */ 
/*      */   public static void reportFatal(SystemInterface sys, IdcMessage msg)
/*      */   {
/*  442 */     reportError(sys, msg);
/*  443 */     if (!m_isStandAlone)
/*      */       return;
/*  445 */     System.exit(1);
/*      */   }
/*      */ 
/*      */   public static void launch(String appName)
/*      */     throws ServiceException
/*      */   {
/*  454 */     MainFrame appFrame = (MainFrame)m_activeApps.get(appName);
/*  455 */     if (appFrame == null)
/*      */     {
/*  458 */       if (m_isStandAlone)
/*      */       {
/*  460 */         m_standAlone.doStandaloneAppInit(appName);
/*      */       }
/*      */ 
/*  464 */       AppInfo info = m_apps.getAppInfo(appName);
/*  465 */       if (info == null)
/*      */       {
/*  467 */         String appsList = m_apps.APP_INFO[0][0];
/*  468 */         for (int i = 1; i < m_apps.APP_INFO.length; ++i)
/*      */         {
/*  470 */           appsList = appsList + ", " + m_apps.APP_INFO[i][0];
/*      */         }
/*  472 */         throw new ServiceException(LocaleUtils.encodeMessage("apAppNotDefined", null, appsList));
/*      */       }
/*      */ 
/*  476 */       appFrame = createAppFrame(info);
/*      */ 
/*  479 */       doReportRegistration(appFrame, true, info.m_statusReporter);
/*      */ 
/*  482 */       m_activeApps.put(appName, appFrame);
/*      */     }
/*      */     else
/*      */     {
/*  486 */       appFrame.toFront();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static MainFrame createAppFrame(AppInfo info) throws ServiceException
/*      */   {
/*  492 */     MainFrame frame = (MainFrame)ComponentClassFactory.createClassInstance(info.m_appName, info.m_className, "");
/*      */ 
/*  494 */     frame.m_appHelper.m_cxt = m_cxt;
/*      */ 
/*  496 */     m_activeSubjects.addMonitorObjects(info.m_subjects);
/*  497 */     m_activeTopics.addMonitorObjects(info.m_topics);
/*      */ 
/*  500 */     DataBinder binder = new DataBinder();
/*  501 */     executeService("PING_SERVER", binder);
/*      */ 
/*  504 */     String title = info.m_title;
/*  505 */     frame.setName(info.m_appName);
/*  506 */     IdcMessage titleMessage = IdcMessageFactory.lc();
/*  507 */     titleMessage.m_msgLocalized = title;
/*  508 */     frame.init(titleMessage, false);
/*      */     try
/*      */     {
/*  513 */       if (System.getProperty("os.name").indexOf("Windows") >= 0)
/*      */       {
/*  515 */         frame.setIconImage(GuiUtils.getAppImage("app-icon.gif"));
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  520 */       if (SystemUtils.m_verbose)
/*      */       {
/*  522 */         Report.debug("applet", null, e);
/*      */       }
/*      */     }
/*      */ 
/*  526 */     return frame;
/*      */   }
/*      */ 
/*      */   public static synchronized void removeApp(String appName)
/*      */   {
/*  531 */     MainFrame appFrame = (MainFrame)m_activeApps.get(appName);
/*      */ 
/*  533 */     String reportType = null;
/*  534 */     AppInfo appInfo = m_apps.getAppInfo(appName);
/*  535 */     if (appInfo != null)
/*      */     {
/*  537 */       reportType = appInfo.m_statusReporter;
/*      */     }
/*      */ 
/*  540 */     doReportRegistration(appFrame, false, reportType);
/*      */ 
/*  542 */     if (appInfo == null)
/*      */     {
/*  545 */       Report.trace("applet", LocaleResources.getString("apNoInfoOnApp", m_cxt, appName), null);
/*  546 */       return;
/*      */     }
/*      */ 
/*  549 */     m_activeSubjects.removeMonitoredObjects(appInfo.m_subjects);
/*  550 */     m_activeTopics.removeMonitoredObjects(appInfo.m_topics);
/*      */ 
/*  552 */     m_activeApps.remove(appName);
/*  553 */     if (m_activeApps.size() != 0)
/*      */       return;
/*  555 */     m_isInitialized = false;
/*  556 */     if (!m_isStandAlone)
/*      */       return;
/*  558 */     System.exit(0);
/*      */   }
/*      */ 
/*      */   protected static synchronized void doReportRegistration(MainFrame frame, boolean register, String reportType)
/*      */   {
/*  570 */     if (!frame instanceof ReportProgress)
/*      */       return;
/*  572 */     ReportProgress rp = null;
/*  573 */     if (register)
/*      */     {
/*  575 */       rp = (ReportProgress)frame;
/*      */     }
/*  577 */     if (m_isStandAlone)
/*      */     {
/*  579 */       m_standAlone.setReportProgressCallback(reportType, rp);
/*      */     }
/*  581 */     else if (reportType.equals("indexer"))
/*      */     {
/*  583 */       m_serverProgress = rp;
/*      */     } else {
/*  585 */       if (!reportType.equals("archiver"))
/*      */         return;
/*  587 */       m_archiverProgress = rp;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static synchronized void addReportRequests(DataBinder binder)
/*      */   {
/*  594 */     if (m_serverProgress != null)
/*      */     {
/*  596 */       binder.putLocal("GetCurrentIndexingStatus", "1");
/*      */     }
/*      */ 
/*  599 */     if (m_archiverProgress == null)
/*      */       return;
/*  601 */     binder.putLocal("GetCurrentArchiverStatus", "1");
/*      */   }
/*      */ 
/*      */   protected static synchronized void retrieveReportInfo(DataBinder binder)
/*      */   {
/*  607 */     String curState = binder.getLocal("CurrentIndexingStatus");
/*  608 */     parseReportMsg(m_serverProgress, curState);
/*      */ 
/*  610 */     curState = binder.getLocal("CurrentArchiverStatus");
/*  611 */     parseReportMsg(m_archiverProgress, curState);
/*      */   }
/*      */ 
/*      */   protected static void parseReportMsg(ReportProgress rp, String curState)
/*      */   {
/*  616 */     if ((rp == null) || (curState == null))
/*      */       return;
/*  618 */     Vector v = StringUtils.parseArray(curState, ',', '\\');
/*  619 */     if (v.size() != 4)
/*      */     {
/*  621 */       return;
/*      */     }
/*  623 */     int type = Integer.parseInt((String)v.elementAt(0));
/*  624 */     float amt = Float.valueOf((String)v.elementAt(1)).floatValue();
/*  625 */     float max = Float.valueOf((String)v.elementAt(2)).floatValue();
/*  626 */     String msg = (String)v.elementAt(3);
/*  627 */     rp.reportProgress(type, msg, amt, max);
/*      */   }
/*      */ 
/*      */   public static void retrieveServerEnv(DataBinder binder)
/*      */   {
/*  636 */     m_user = binder.getLocal("dUser");
/*      */ 
/*  639 */     SharedLoader.loadEnvVariableListFromTable(binder);
/*      */ 
/*  641 */     if (m_isInitialConfigLoaded)
/*      */       return;
/*  643 */     if (!getIsHeavyClient())
/*      */     {
/*  645 */       SharedLoader.loadInitialConfig();
/*      */     }
/*  647 */     m_isInitialConfigLoaded = true;
/*      */   }
/*      */ 
/*      */   public static void notifyInternalSubjectChange(String name)
/*      */   {
/*  656 */     m_activeSubjects.setObjectChanged(name);
/*  657 */     m_activeSubjects.notifyObjectObservers(name, null);
/*      */   }
/*      */ 
/*      */   public static void addSubjectObserver(String name, Observer obs)
/*      */   {
/*  665 */     m_activeSubjects.addObjectObserver(name, obs);
/*      */   }
/*      */ 
/*      */   public static void removeSubjectObserver(String name, Observer obs)
/*      */   {
/*  670 */     m_activeSubjects.removeObjectObserver(name, obs);
/*      */   }
/*      */ 
/*      */   public static void addOrRefreshActiveSubjectEvent(String name)
/*      */   {
/*  675 */     m_activeSubjects.addOrRefreshActiveEvent(name);
/*      */   }
/*      */ 
/*      */   public static void addTopicObserver(String name, Observer obs)
/*      */   {
/*  680 */     m_activeTopics.addObjectObserver(name, obs);
/*      */   }
/*      */ 
/*      */   public static void removeTopicObserver(String name, Observer obs)
/*      */   {
/*  685 */     m_activeTopics.removeObjectObserver(name, obs);
/*      */   }
/*      */ 
/*      */   public static void addOrRefreshActiveTopicEvent(String name)
/*      */   {
/*  690 */     m_activeTopics.addOrRefreshActiveEvent(name);
/*      */   }
/*      */ 
/*      */   public static void addMonikerObserver(String name, Observer obs)
/*      */   {
/*  695 */     m_activeMonikers.addMonikerObserver(name, obs);
/*      */   }
/*      */ 
/*      */   public static void removeMonikerObserver(String name, Observer obs)
/*      */   {
/*  700 */     m_activeMonikers.removeMonikerObserver(name, obs);
/*      */   }
/*      */ 
/*      */   public static void addOrRefreshActiveMonikerEvent(String name)
/*      */   {
/*  705 */     m_activeMonikers.addOrRefreshActiveEvent(name);
/*      */   }
/*      */ 
/*      */   public static synchronized void executeService(String action, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  714 */     executeService(action, binder, null);
/*      */   }
/*      */ 
/*      */   public static synchronized void executeService(String action, DataBinder binder, SystemInterface sys)
/*      */     throws ServiceException
/*      */   {
/*  722 */     binder.putLocal("forceLogin", "1");
/*      */ 
/*  725 */     m_lastActionTS = System.currentTimeMillis();
/*      */ 
/*  728 */     binder.setEncodeFlags(m_isStandAlone, true);
/*      */ 
/*  733 */     if ((m_dateFormat != null) && (!binder.m_determinedDataDateFormat))
/*      */     {
/*  735 */       binder.m_blDateFormat = m_dateFormat;
/*      */     }
/*      */ 
/*  739 */     m_activeSubjects.prepForMonitoredObjects(binder);
/*  740 */     m_activeTopics.prepForMonitoredObjects(binder);
/*      */ 
/*  743 */     m_activeMonikers.prepForMonitoredObjects(binder);
/*      */ 
/*  745 */     Cursor prevCursor = null;
/*  746 */     if (sys != null)
/*      */     {
/*  748 */       prevCursor = GuiUtils.setBusy(sys);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  754 */       addReportRequests(binder);
/*      */ 
/*  756 */       Report.trace("applet", "executing service request " + action, null);
/*  757 */       if (m_isStandAlone)
/*      */       {
/*  759 */         m_standAlone.executeService(action, binder);
/*  760 */         if (getIsHeavyClient())
/*      */         {
/*  762 */           retrieveServerEnv(binder);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  768 */         m_urlClient.request(action, binder);
/*      */ 
/*  774 */         DataSerializeUtils.setSystemEncoding(m_urlClient.m_fileEncoding);
/*      */ 
/*  777 */         retrieveServerEnv(binder);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       ServiceException se;
/*  792 */       retrieveReportInfo(binder);
/*  793 */       synchronizeCache(binder);
/*  794 */       m_activeMonikers.synchronizeCounters(binder);
/*  795 */       if ((prevCursor != null) && (sys != null))
/*      */       {
/*  797 */         GuiUtils.setCursor(sys, prevCursor);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void executeService(String action, Properties props)
/*      */     throws ServiceException
/*      */   {
/*  805 */     executeService(action, props, null);
/*      */   }
/*      */ 
/*      */   public static void executeService(String action, Properties props, SystemInterface sys)
/*      */     throws ServiceException
/*      */   {
/*  811 */     DataBinder binder = new DataBinder();
/*  812 */     binder.setLocalData(props);
/*  813 */     executeService(action, binder, sys);
/*      */   }
/*      */ 
/*      */   protected static void synchronizeCache(DataBinder binder)
/*      */   {
/*  821 */     if ((!m_isStandAlone) || (getIsHeavyClient()))
/*      */     {
/*  823 */       for (Enumeration en = binder.getOptionLists(); en.hasMoreElements(); )
/*      */       {
/*  825 */         String key = (String)en.nextElement();
/*  826 */         Vector options = binder.getOptionList(key);
/*  827 */         SharedObjects.putOptList(key, options);
/*      */       }
/*      */       Enumeration en;
/*      */       try
/*      */       {
/*  832 */         for (en = binder.getResultSetList(); en.hasMoreElements(); )
/*      */         {
/*  834 */           String key = (String)en.nextElement();
/*  835 */           DataResultSet rset = (DataResultSet)binder.getResultSet(key);
/*  836 */           rset.first();
/*  837 */           if (key.equals("Users"))
/*      */           {
/*  839 */             Users users = new Users();
/*  840 */             users.load(rset);
/*  841 */             SharedObjects.putTable(key, users);
/*      */           }
/*  843 */           else if (key.equals("RoleDefinition"))
/*      */           {
/*  845 */             RoleDefinitions roles = new RoleDefinitions();
/*  846 */             roles.load(rset);
/*  847 */             SharedObjects.putTable(key, roles);
/*      */           }
/*  849 */           else if ((key.equals("Alias")) || (key.equals("AliasUserMap")))
/*      */           {
/*  851 */             updateAliasData(key, rset);
/*      */           }
/*  853 */           else if (key.equals("ArchiveCollections"))
/*      */           {
/*  855 */             ArchiveCollections collections = new ArchiveCollections();
/*  856 */             collections.load(rset, false);
/*  857 */             SharedObjects.putTable(key, collections);
/*      */           }
/*  859 */           else if (key.equals("AdditionalRenditions"))
/*      */           {
/*  861 */             AdditionalRenditions renSet = new AdditionalRenditions();
/*  862 */             renSet.load(rset);
/*  863 */             SharedObjects.putTable(key, renSet);
/*      */           }
/*      */           else
/*      */           {
/*  867 */             if (key.equals("DocTypes"))
/*      */             {
/*  869 */               SharedLoader.cacheOptList(rset, "dDocType", "docTypes");
/*      */             }
/*  871 */             else if (key.equals("SecurityGroups"))
/*      */             {
/*  873 */               SharedLoader.cacheOptList(rset, "dGroupName", "securityGroups");
/*      */             }
/*  875 */             else if (key.equals("DocumentAccounts"))
/*      */             {
/*  877 */               SharedLoader.cacheOptList(rset, "dDocAccount", "docAccounts");
/*      */             }
/*  879 */             else if (key.equals("Collaborations"))
/*      */             {
/*  881 */               Collaborations.load(rset, true);
/*      */             }
/*  883 */             else if ((key.startsWith("Schema")) && (key.endsWith("Config")))
/*      */               {
/*      */                 continue;
/*      */               }
/*      */ 
/*      */ 
/*  889 */             if (SystemUtils.m_verbose)
/*      */             {
/*  891 */               Report.debug("applet", "setting table " + key, null);
/*      */             }
/*  893 */             SharedObjects.putTable(key, rset);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  900 */         Report.trace("applet", null, e);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  905 */         Report.trace("applet", null, e);
/*      */       }
/*      */     }
/*      */ 
/*  909 */     updateWorkflowData(binder);
/*  910 */     updateUserData(binder);
/*  911 */     updateSchemaData(binder);
/*  912 */     SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*      */ 
/*  914 */     if ((views != null) && (!m_loaderInitDone) && (((!m_isStandAlone) || (getIsHeavyClient()))))
/*      */     {
/*      */       try
/*      */       {
/*  919 */         SchemaLoader loader = (SchemaLoader)ComponentClassFactory.createClassInstance("ClientSchemaLoader", "intradoc.apps.shared.ClientSchemaLoader", null);
/*      */ 
/*  922 */         Hashtable initData = new Hashtable();
/*  923 */         initData.put("AppLauncher", new AppLauncher());
/*  924 */         if (!loader.init(initData))
/*      */         {
/*  926 */           Report.trace("applet", "unable to find a useful SchemaLoader.", null);
/*      */         }
/*      */         else
/*      */         {
/*  930 */           m_loaderInitDone = true;
/*  931 */           views.addLoader(loader);
/*      */         }
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  936 */         Report.trace("applet", null, e);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  941 */     m_activeSubjects.synchronizeCounters(binder);
/*  942 */     m_activeTopics.synchronizeCounters(binder);
/*      */   }
/*      */ 
/*      */   protected static void updateUserData(DataBinder binder)
/*      */   {
/*  948 */     DataResultSet rset = (DataResultSet)binder.getResultSet("Users");
/*  949 */     Users users = null;
/*  950 */     if (rset != null)
/*      */     {
/*  952 */       users = (Users)SharedObjects.getTable("Users");
/*      */     }
/*      */ 
/*  956 */     if (users != null)
/*      */     {
/*  958 */       UserData userData = users.getLocalUserData(m_user);
/*  959 */       if (userData == null)
/*      */       {
/*  961 */         userData = UserUtils.createUserData(m_user);
/*      */       }
/*      */ 
/*  964 */       if (m_userData != null)
/*      */       {
/*  967 */         UserProfileData upData = m_userData.getProfileData();
/*  968 */         userData.setUserProfile(upData);
/*      */       }
/*  970 */       m_userData = userData;
/*      */     }
/*      */ 
/*  973 */     if (m_userData == null)
/*      */       return;
/*  975 */     UserUtils.serializeAttribInfoNoError(binder, m_userData, false, false);
/*      */ 
/*  978 */     if ((m_isStandAlone) && (!getIsHeavyClient()))
/*      */       return;
/*  980 */     m_userData.updateTopics(binder);
/*      */   }
/*      */ 
/*      */   protected static void updateAliasData(String key, DataResultSet rset)
/*      */     throws DataException
/*      */   {
/*  987 */     AliasData aliasData = null;
/*  988 */     DataResultSet dSet = SharedObjects.getTable("Alias");
/*  989 */     if (dSet == null)
/*      */     {
/*  991 */       aliasData = new AliasData();
/*      */     }
/*  993 */     else if (dSet instanceof AliasData)
/*      */     {
/*  995 */       aliasData = (AliasData)dSet;
/*      */     }
/*      */     else
/*      */     {
/*  999 */       return;
/*      */     }
/*      */ 
/* 1002 */     if (key.equals("Alias"))
/*      */     {
/* 1004 */       aliasData.loadAliases(rset);
/*      */     }
/*      */     else
/*      */     {
/* 1008 */       aliasData.loadUsers(rset);
/*      */     }
/*      */ 
/* 1011 */     SharedObjects.putTable("Alias", aliasData);
/*      */   }
/*      */ 
/*      */   public static void updateWorkflowData(DataBinder binder)
/*      */   {
/* 1017 */     DataResultSet rset = (DataResultSet)binder.getResultSet("Workflows");
/* 1018 */     if (rset == null)
/*      */     {
/* 1020 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1025 */       WorkflowData workflowData = new WorkflowData();
/* 1026 */       workflowData.load(binder);
/*      */ 
/* 1028 */       String tableName = "App" + WorkflowData.m_tableName;
/* 1029 */       SharedObjects.putTable(tableName, workflowData);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1033 */       Report.trace("workflow", null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void updateSchemaData(DataBinder binder)
/*      */   {
/* 1043 */     DataResultSet newData = (DataResultSet)binder.getResultSet("SchemaConfigData");
/*      */ 
/* 1045 */     binder.removeResultSet("SchemaConfigData");
/* 1046 */     if (((m_isStandAlone) && (!getIsHeavyClient())) || (newData == null))
/*      */     {
/* 1048 */       return;
/*      */     }
/* 1050 */     IdcMessage errMsg = null;
/* 1051 */     String dataClass = null;
/* 1052 */     String name = null;
/*      */     try
/*      */     {
/* 1056 */       DataResultSet schemaObjectPermissions = SharedObjects.getTable("SchemaObjectPermissions");
/* 1057 */       if (schemaObjectPermissions == null)
/*      */       {
/* 1059 */         schemaObjectPermissions = new DataResultSet(new String[] { "schObjectKey" });
/*      */       }
/*      */ 
/* 1064 */       FieldInfo keyField = new FieldInfo();
/* 1065 */       schemaObjectPermissions.getFieldInfo("schObjectKey", keyField);
/* 1066 */       for (SimpleParameters params : newData.getSimpleParametersIterable())
/*      */       {
/* 1068 */         String className = params.get("schDataClass");
/* 1069 */         name = params.get("schObjectName");
/* 1070 */         String dataString = params.get("schData");
/*      */ 
/* 1072 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1074 */           Report.debug("applet", "loading schema class " + className + " name " + name + " with data " + dataString, null);
/*      */         }
/*      */ 
/* 1079 */         dataClass = className;
/* 1080 */         int index = className.lastIndexOf(".");
/* 1081 */         if (index >= 0)
/*      */         {
/* 1083 */           dataClass = className.substring(index + 1);
/*      */         }
/* 1085 */         SchemaResultSet srs = (SchemaResultSet)m_cxt.getCachedObject(dataClass);
/* 1086 */         if (srs == null)
/*      */         {
/* 1088 */           srs = (SchemaResultSet)ComponentClassFactory.createClassInstance(dataClass, className, null);
/*      */ 
/* 1091 */           m_cxt.setCachedObject(dataClass, srs);
/* 1092 */           SharedObjects.putTable(dataClass, srs);
/*      */         }
/*      */ 
/* 1097 */         if (name.length() > 0)
/*      */         {
/* 1099 */           if (dataString.length() > 0)
/*      */           {
/* 1101 */             DataBinder defBinder = new DataBinder();
/* 1102 */             StringReader defReader = new StringReader(dataString);
/* 1103 */             defBinder.receive(new BufferedReader(defReader));
/*      */ 
/* 1105 */             Report.trace("schemastorage", "Updating " + name, null);
/* 1106 */             srs.update(defBinder, 0L);
/*      */           }
/*      */           else
/*      */           {
/* 1110 */             srs.delete(name);
/*      */           }
/*      */         }
/*      */ 
/* 1114 */         String permKey = dataClass + "/" + name;
/* 1115 */         IdcProperties perms = new IdcProperties();
/* 1116 */         perms.put("schObjectKey", permKey);
/* 1117 */         for (String key : new String[] { "schError_modify", "schError_rename", "schError_synchronize", "schError_delete" })
/*      */         {
/* 1123 */           String val = params.get(key);
/* 1124 */           if ((val == null) || (val.length() <= 0))
/*      */             continue;
/* 1126 */           if (!schemaObjectPermissions.getFieldInfo(key, null))
/*      */           {
/* 1128 */             List l = new ArrayList();
/* 1129 */             FieldInfo tmpInfo = new FieldInfo();
/* 1130 */             newData.getFieldInfo(key, tmpInfo);
/* 1131 */             l.add(tmpInfo);
/* 1132 */             schemaObjectPermissions.mergeFieldsWithFlags(l, 0);
/*      */           }
/* 1134 */           perms.put(key, val);
/*      */         }
/*      */ 
/* 1137 */         Vector row = schemaObjectPermissions.findRow(keyField.m_index, permKey);
/* 1138 */         if (row == null)
/*      */         {
/* 1140 */           row = schemaObjectPermissions.createRow(perms);
/* 1141 */           schemaObjectPermissions.addRow(row);
/*      */         }
/*      */         else
/*      */         {
/* 1145 */           Vector tmp = schemaObjectPermissions.createRow(perms);
/* 1146 */           row.setSize(0);
/* 1147 */           row.addAll(tmp);
/*      */         }
/*      */       }
/* 1150 */       SharedObjects.putTable("SchemaObjectPermissions", schemaObjectPermissions);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1155 */       Report.trace("schemastorage", "Badly formatted binder for schema information. ", e);
/* 1156 */       errMsg = IdcMessageFactory.lc(e, "apSchAppletFormattingError", new Object[] { name });
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1161 */       Report.trace("schemastorage", "Schema result set is badly defined.", e);
/* 1162 */       errMsg = IdcMessageFactory.lc(e, "apSchAppletResultsetDefinitionError", new Object[] { dataClass });
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1167 */       Report.trace("schemastorage", "Unable to update schema information", e);
/* 1168 */       errMsg = IdcMessageFactory.lc(e, "apSchAppletUpdateInfoError", new Object[] { name });
/*      */     }
/*      */     finally
/*      */     {
/* 1172 */       if (errMsg != null)
/*      */       {
/* 1174 */         reportError(null, errMsg);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void ping()
/*      */   {
/* 1182 */     Runnable bg = new Runnable()
/*      */     {
/*      */       public void run()
/*      */       {
/* 1186 */         boolean isExiting = false;
/* 1187 */         while ((!isExiting) && (AppLauncher.m_isInitialized))
/*      */         {
/*      */           try
/*      */           {
/* 1191 */             SystemUtils.sleep(3000L);
/* 1192 */             long elapsed = System.currentTimeMillis() - AppLauncher.m_lastActionTS;
/* 1193 */             if ((elapsed > AppLauncher.m_bgTimer) || ((!SharedObjects.getEnvValueAsBoolean("DisableAppletActiveEventCheck", false)) && (hasActiveEvent())))
/*      */             {
/* 1196 */               DataBinder binder = new DataBinder();
/* 1197 */               AppLauncher.executeService("PING_SERVER", binder);
/*      */             }
/*      */           }
/*      */           catch (ServiceException ignore)
/*      */           {
/* 1202 */             Report.trace("applet", null, ignore);
/*      */           }
/*      */           catch (Throwable t)
/*      */           {
/* 1206 */             Report.trace("applet", null, t);
/* 1207 */             isExiting = true;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */       public boolean hasActiveEvent()
/*      */       {
/* 1214 */         return (AppLauncher.m_activeSubjects.hasActiveEvent()) || (AppLauncher.m_activeMonikers.hasActiveEvent()) || (AppLauncher.m_activeTopics.hasActiveEvent());
/*      */       }
/*      */     };
/* 1219 */     Thread bgThread = new Thread(bg, "AppLauncher ping");
/* 1220 */     bgThread.setDaemon(true);
/* 1221 */     bgThread.start();
/*      */   }
/*      */ 
/*      */   public static Vector getApps()
/*      */   {
/* 1229 */     return m_apps.getApps();
/*      */   }
/*      */ 
/*      */   public static Vector getAppsForUser()
/*      */   {
/* 1234 */     int rights = 0;
/* 1235 */     if (m_user == null)
/*      */     {
/* 1237 */       rights = SharedObjects.getEnvironmentInt("UserAppRights", 0);
/*      */     }
/*      */     else
/*      */     {
/* 1241 */       if (m_userData == null)
/*      */       {
/* 1243 */         Users users = (Users)SharedObjects.getTable("Users");
/* 1244 */         if (users == null)
/*      */         {
/* 1246 */           rights = SharedObjects.getEnvironmentInt("UserAppRights", 0);
/*      */         }
/*      */         else
/*      */         {
/* 1250 */           m_userData = users.getLocalUserData(m_user);
/*      */         }
/*      */       }
/* 1253 */       if (m_userData != null)
/*      */       {
/*      */         try
/*      */         {
/* 1257 */           rights = SecurityUtils.determineGroupPrivilege(m_userData, "#AppsGroup");
/*      */         }
/*      */         catch (Exception ignore)
/*      */         {
/* 1261 */           if (SystemUtils.m_verbose)
/*      */           {
/* 1263 */             Report.debug("applet", null, ignore);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1269 */     boolean isAdmin = isAdmin();
/* 1270 */     Vector userApps = new IdcVector();
/* 1271 */     Vector apps = getApps();
/* 1272 */     int size = apps.size();
/* 1273 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1275 */       AppInfo info = (AppInfo)apps.elementAt(i);
/* 1276 */       if ((!isAdmin) && ((info.m_appRights & rights) == 0))
/*      */         continue;
/* 1278 */       userApps.addElement(info);
/*      */     }
/*      */ 
/* 1282 */     return userApps;
/*      */   }
/*      */ 
/*      */   public static String getUser()
/*      */   {
/* 1287 */     return m_user;
/*      */   }
/*      */ 
/*      */   public static UserData getUserData()
/*      */   {
/* 1292 */     return m_userData;
/*      */   }
/*      */ 
/*      */   public static boolean isAdmin()
/*      */   {
/* 1297 */     boolean isAdmin = SecurityUtils.isUserOfRole(m_userData, "admin");
/* 1298 */     SharedObjects.putEnvironmentValue("UserIsAdmin", String.valueOf(isAdmin));
/* 1299 */     return isAdmin;
/*      */   }
/*      */ 
/*      */   public static boolean getIsStandAlone()
/*      */   {
/* 1304 */     return m_isStandAlone;
/*      */   }
/*      */ 
/*      */   public static boolean getIsHeavyClient()
/*      */   {
/* 1309 */     return (m_isStandAlone) && (m_standAlone.m_isHeavyClient);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1314 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87442 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.AppLauncher
 * JD-Core Version:    0.5.4
 */