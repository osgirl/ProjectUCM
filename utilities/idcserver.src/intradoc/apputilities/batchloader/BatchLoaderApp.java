/*      */ package intradoc.apputilities.batchloader;
/*      */ 
/*      */ import intradoc.apps.shared.AppLauncher;
/*      */ import intradoc.apps.shared.StandAloneApp;
/*      */ import intradoc.common.AppObjectRepository;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.gui.AppFrameHelper;
/*      */ import intradoc.gui.CustomDialog;
/*      */ import intradoc.gui.CustomTextArea;
/*      */ import intradoc.gui.DialogHelper;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.gui.iwt.IdcFileChooser;
/*      */ import intradoc.server.SearchLoader;
/*      */ import intradoc.server.SubjectEventMonitor;
/*      */ import intradoc.server.SubjectManager;
/*      */ import intradoc.server.utils.SystemPropertiesEditor;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import intradoc.shared.Users;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.text.ParseException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ import javax.swing.JButton;
/*      */ import javax.swing.JDialog;
/*      */ import javax.swing.JFileChooser;
/*      */ import javax.swing.JPanel;
/*      */ 
/*      */ public class BatchLoaderApp
/*      */ {
/*      */   protected BatchLoaderFrame m_frame;
/*      */   protected SystemPropertiesEditor m_propLoader;
/*      */   protected Properties m_idcProperties;
/*      */   protected Properties m_carryOver;
/*      */   protected DataBinder m_dataBinder;
/*      */   protected StandAloneApp m_standAlone;
/*      */   protected ExecutionContext m_cxt;
/*      */   protected String m_batchLoaderPath;
/*      */   protected String m_errorFilePath;
/*      */   protected String m_errorFileName;
/*      */   protected String m_workingDir;
/*   96 */   protected int m_totalRecords = 0;
/*   97 */   protected int m_numRdsProcessed = 0;
/*   98 */   protected int m_numErrors = 0;
/*   99 */   protected int m_numSucceeded = 0;
/*      */ 
/*  102 */   protected boolean m_doBackground = false;
/*  103 */   protected boolean m_doCleanUp = false;
/*  104 */   protected boolean m_enableErrorFile = false;
/*  105 */   protected boolean m_batchLoaderStarted = false;
/*  106 */   protected boolean m_isExiting = false;
/*  107 */   protected boolean m_doSpider = false;
/*  108 */   protected boolean m_isFirstRecord = false;
/*  109 */   protected boolean m_externalBatchload = false;
/*  110 */   protected boolean m_initialized = false;
/*      */   protected Hashtable m_hDocAuthors;
/*      */   protected Hashtable m_hSecurityGroups;
/*      */   protected Hashtable m_hDocTypes;
/*      */   protected Vector m_authorsList;
/*      */   protected Vector m_securityList;
/*      */   protected Vector m_docTypesList;
/*      */   protected Vector m_vDocAuthors;
/*      */   protected Vector m_vSecurityGroups;
/*      */   protected Vector m_vDocTypes;
/*      */   protected Vector m_vDateFormats;
/*      */   protected String m_blfErrorMsg;
/*      */   protected ResourceContainer m_resContainer;
/*  131 */   protected static String m_endRecord = "<<EOD>>";
/*      */ 
/*      */   public BatchLoaderApp(StandAloneApp standAlone, boolean doBackground)
/*      */   {
/*  135 */     this.m_standAlone = standAlone;
/*  136 */     this.m_doBackground = doBackground;
/*      */ 
/*  138 */     this.m_propLoader = new SystemPropertiesEditor();
/*  139 */     this.m_carryOver = new Properties();
/*  140 */     this.m_dataBinder = new DataBinder();
/*  141 */     this.m_resContainer = new ResourceContainer();
/*      */ 
/*  144 */     this.m_workingDir = FileUtils.getWorkingDir();
/*      */   }
/*      */ 
/*      */   public void init()
/*      */     throws ServiceException
/*      */   {
/*  150 */     String[] args = (String[])(String[])AppObjectRepository.getObject("CommandLine");
/*      */ 
/*  153 */     readIntradocCfgFile();
/*  154 */     readCommandLine(args);
/*  155 */     initProperties();
/*      */ 
/*  157 */     this.m_initialized = true;
/*      */   }
/*      */ 
/*      */   protected void dispose()
/*      */   {
/*  162 */     System.exit(0);
/*      */   }
/*      */ 
/*      */   protected void readCommandLine(String[] args)
/*      */   {
/*  168 */     int numArgs = args.length;
/*  169 */     for (int i = 0; i < numArgs; ++i)
/*      */     {
/*  171 */       String tempStr = args[i].toUpperCase();
/*      */ 
/*  173 */       if ((tempStr.startsWith("/N")) || (tempStr.startsWith("-N")))
/*      */       {
/*  175 */         String tempPath = null;
/*      */ 
/*  177 */         if (tempStr.length() == 2)
/*      */         {
/*  179 */           ++i;
/*  180 */           if (i >= numArgs)
/*      */             return;
/*  182 */           tempPath = args[i];
/*      */         }
/*      */         else
/*      */         {
/*  191 */           tempPath = args[i].substring(2, tempStr.length());
/*      */         }
/*  193 */         setBatchLoaderPath(tempPath.trim());
/*      */       }
/*      */       else
/*      */       {
/*  197 */         if ((!tempStr.startsWith("/E")) && (!tempStr.startsWith("-E")))
/*      */           continue;
/*  199 */         setEnableErrorFile(true);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setBatchLoaderPath(String path)
/*      */   {
/*  207 */     if (path == null)
/*      */       return;
/*  209 */     this.m_batchLoaderPath = FileUtils.fileSlashes(path);
/*  210 */     this.m_errorFilePath = FileUtils.getDirectory(this.m_batchLoaderPath);
/*  211 */     this.m_idcProperties.put("BatchLoaderPath", this.m_batchLoaderPath);
/*      */   }
/*      */ 
/*      */   public void setEnableErrorFile(boolean flag)
/*      */   {
/*  217 */     this.m_enableErrorFile = flag;
/*  218 */     this.m_idcProperties.put("EnableErrorFile", String.valueOf(flag));
/*      */   }
/*      */ 
/*      */   protected void initProperties()
/*      */   {
/*  224 */     String batchLoaderPath = this.m_idcProperties.getProperty("BatchLoaderPath");
/*  225 */     if (batchLoaderPath != null)
/*      */     {
/*  227 */       setBatchLoaderPath(batchLoaderPath);
/*      */     }
/*      */ 
/*  230 */     if (this.m_idcProperties.getProperty("MaxErrorsAllowed") != null)
/*      */       return;
/*  232 */     this.m_idcProperties.put("MaxErrorsAllowed", "50");
/*      */   }
/*      */ 
/*      */   public void loadBatchLoader()
/*      */     throws ServiceException
/*      */   {
/*  238 */     if (!this.m_initialized)
/*      */     {
/*  240 */       init();
/*      */     }
/*      */ 
/*  244 */     if (!retrieveProperties())
/*      */     {
/*  246 */       if (this.m_doBackground)
/*      */       {
/*  248 */         dispose();
/*      */       }
/*  250 */       return;
/*      */     }
/*      */ 
/*  254 */     String path = this.m_idcProperties.getProperty("BatchLoaderPath");
/*  255 */     if (path != null)
/*      */     {
/*  257 */       setBatchLoaderPath(path);
/*      */     }
/*      */     else
/*      */     {
/*  261 */       throw new ServiceException(LocaleUtils.encodeMessage("csBatchFileMissing", null));
/*      */     }
/*      */ 
/*  264 */     this.m_enableErrorFile = StringUtils.convertToBool(this.m_idcProperties.getProperty("EnableErrorFile"), false);
/*      */ 
/*  267 */     this.m_doCleanUp = StringUtils.convertToBool(this.m_idcProperties.getProperty("CleanUp"), false);
/*      */ 
/*  270 */     int index = this.m_batchLoaderPath.lastIndexOf(47);
/*  271 */     int dotIndex = this.m_batchLoaderPath.lastIndexOf(46);
/*  272 */     int endIndex = this.m_batchLoaderPath.length();
/*      */ 
/*  274 */     if (dotIndex > index)
/*      */     {
/*  276 */       endIndex = dotIndex;
/*      */     }
/*      */ 
/*  279 */     SimpleDateFormat frmt = new SimpleDateFormat("yyMdHHmm");
/*  280 */     frmt.setTimeZone(TimeZone.getDefault());
/*  281 */     Date dte = new Date();
/*  282 */     String tstamp = frmt.format(dte);
/*      */ 
/*  284 */     this.m_errorFileName = (this.m_batchLoaderPath.substring(index + 1, endIndex) + "_" + tstamp + ".txt");
/*      */ 
/*  287 */     this.m_authorsList = SharedObjects.getOptList("docAuthors");
/*  288 */     this.m_securityList = SharedObjects.getOptList("securityGroups");
/*  289 */     this.m_docTypesList = SharedObjects.getOptList("docTypes");
/*      */ 
/*  291 */     this.m_hDocAuthors = new Hashtable();
/*  292 */     this.m_hSecurityGroups = new Hashtable();
/*  293 */     this.m_hDocTypes = new Hashtable();
/*      */ 
/*  295 */     this.m_vDocAuthors = new IdcVector();
/*  296 */     this.m_vSecurityGroups = new IdcVector();
/*  297 */     this.m_vDocTypes = new IdcVector();
/*      */ 
/*  299 */     this.m_vDateFormats = new IdcVector();
/*      */ 
/*  302 */     this.m_totalRecords = 0;
/*  303 */     this.m_numRdsProcessed = 0;
/*  304 */     this.m_numSucceeded = 0;
/*  305 */     this.m_numErrors = 0;
/*      */ 
/*  308 */     if (this.m_doBackground)
/*      */     {
/*  310 */       String user = this.m_idcProperties.getProperty("BatchLoaderUserName");
/*  311 */       if (user != null)
/*      */       {
/*  313 */         Users users = (Users)SharedObjects.getTable("Users");
/*  314 */         UserData userData = users.getLocalUserData(user);
/*  315 */         if (userData != null)
/*      */         {
/*  317 */           this.m_standAlone.setupUserAccess(userData);
/*  318 */           this.m_cxt = this.m_standAlone.getUserContext();
/*      */         }
/*      */         else
/*      */         {
/*  322 */           this.m_standAlone.setUser(user);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  327 */         displayError("!csBatchLoaderUsernameReq");
/*  328 */         dispose();
/*  329 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  333 */     Runnable bg = new Runnable()
/*      */     {
/*      */       public void run()
/*      */       {
/*      */         try
/*      */         {
/*  339 */           BatchLoaderApp.this.countTotalRecords();
/*  340 */           BatchLoaderApp.this.processBatchLoaderFile();
/*  341 */           if (!BatchLoaderApp.this.m_isExiting)
/*      */           {
/*  343 */             BatchLoaderApp.this.displayDone();
/*      */           }
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  348 */           if ((BatchLoaderApp.this.m_vDocAuthors.size() > 0) || (BatchLoaderApp.this.m_vSecurityGroups.size() > 0) || (BatchLoaderApp.this.m_vDocTypes.size() > 0))
/*      */           {
/*  352 */             BatchLoaderApp.this.displayBatchFileError(e.getMessage());
/*      */           }
/*  354 */           else if (BatchLoaderApp.this.m_vDateFormats.size() > 0)
/*      */           {
/*  356 */             String dateFormat = (String)BatchLoaderApp.this.m_vDateFormats.elementAt(0);
/*  357 */             String errorMsg = LocaleUtils.encodeMessage("csDateParseError", null, dateFormat);
/*      */ 
/*  359 */             errorMsg = LocaleUtils.appendMessage(e.getMessage(), errorMsg);
/*  360 */             BatchLoaderApp.this.displayError(errorMsg);
/*      */           }
/*      */           else
/*      */           {
/*  364 */             BatchLoaderApp.this.displayError(LocaleUtils.encodeMessage("csBatchLoaderFileReadError", null, e.getMessage()));
/*      */           }
/*  366 */           BatchLoaderApp.this.m_batchLoaderStarted = false;
/*  367 */           BatchLoaderApp.this.m_isExiting = true;
/*      */         }
/*      */         finally
/*      */         {
/*  371 */           if (!BatchLoaderApp.this.m_doBackground)
/*      */           {
/*  373 */             BatchLoaderApp.this.m_frame.m_loadBtn.setEnabled(true);
/*      */           }
/*      */           else
/*      */           {
/*  377 */             BatchLoaderApp.this.dispose();
/*      */           }
/*      */         }
/*      */       }
/*      */     };
/*  383 */     this.m_batchLoaderStarted = true;
/*  384 */     this.m_isExiting = false;
/*  385 */     if (!this.m_doBackground)
/*      */     {
/*  387 */       this.m_frame.m_loadBtn.setEnabled(false);
/*      */     }
/*      */ 
/*  390 */     Thread bgThread = new Thread(bg);
/*  391 */     bgThread.start();
/*      */   }
/*      */ 
/*      */   protected String readBatchFileLine(BufferedReader br)
/*      */     throws IOException
/*      */   {
/*  405 */     String line = null;
/*      */ 
/*  407 */     while (null != (line = br.readLine()))
/*      */     {
/*  409 */       line = line.trim();
/*  410 */       if (!line.startsWith("#"))
/*      */       {
/*  412 */         return line;
/*      */       }
/*      */     }
/*  415 */     return null;
/*      */   }
/*      */ 
/*      */   protected void countTotalRecords()
/*      */     throws IOException, ServiceException
/*      */   {
/*  421 */     String line = null;
/*  422 */     File file = new File(this.m_batchLoaderPath);
/*  423 */     BufferedReader br = FileUtils.openDataReader(file);
/*      */ 
/*  426 */     while ((line = readBatchFileLine(br)) != null)
/*      */     {
/*  428 */       if (line.indexOf(m_endRecord) == 0)
/*      */       {
/*  430 */         this.m_totalRecords += 1;
/*      */       }
/*      */ 
/*  434 */       int idx = line.indexOf("=");
/*      */ 
/*  437 */       if (idx > 0)
/*      */       {
/*  439 */         String key = line.substring(0, idx).trim();
/*  440 */         String value = line.substring(idx + 1, line.length()).trim();
/*      */ 
/*  442 */         if ((key.equals("dDocType")) || (key.equals("dDocAuthor")) || (key.equals("dSecurityGroup")))
/*      */         {
/*  445 */           validateEntry(key, value);
/*      */         }
/*  447 */         else if ((((key.equals("DateFormat")) || (key.equals("UserDateFormat")))) && 
/*  451 */           (!value.equals("")))
/*      */         {
/*      */           try
/*      */           {
/*  455 */             IdcDateFormat format = new IdcDateFormat();
/*  456 */             format.init(value);
/*      */           }
/*      */           catch (ParseException e)
/*      */           {
/*  460 */             this.m_vDateFormats.addElement(value);
/*  461 */             throw new ServiceException(e);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  469 */     br.close();
/*      */ 
/*  473 */     if ((this.m_vDocAuthors.size() <= 0) && (this.m_vSecurityGroups.size() <= 0) && (this.m_vDocTypes.size() <= 0)) {
/*      */       return;
/*      */     }
/*      */ 
/*  477 */     throw new ServiceException("!csBatchLoaderErrorDesc");
/*      */   }
/*      */ 
/*      */   protected void processBatchLoaderFile()
/*      */     throws IOException, ServiceException
/*      */   {
/*  483 */     boolean createNewProps = true;
/*  484 */     Properties localData = null;
/*  485 */     Properties defaults = null;
/*  486 */     String line = null;
/*  487 */     int recordNumber = 0;
/*      */ 
/*  489 */     File file = new File(this.m_batchLoaderPath);
/*  490 */     BufferedReader br = FileUtils.openDataReader(file);
/*  491 */     this.m_carryOver = new Properties();
/*      */ 
/*  494 */     this.m_isFirstRecord = true;
/*      */ 
/*  497 */     while (((line = readBatchFileLine(br)) != null) && (!this.m_isExiting))
/*      */     {
/*  499 */       if (createNewProps)
/*      */       {
/*  501 */         defaults = (Properties)this.m_carryOver.clone();
/*  502 */         localData = new Properties(defaults);
/*  503 */         createNewProps = false;
/*      */       }
/*      */ 
/*  506 */       if (line.indexOf(m_endRecord) == 0)
/*      */       {
/*  508 */         processRecord(localData, defaults, ++recordNumber);
/*  509 */         this.m_isFirstRecord = false;
/*  510 */         createNewProps = true;
/*      */       }
/*      */ 
/*  514 */       int idx = line.indexOf("=");
/*      */ 
/*  517 */       if (idx > 0)
/*      */       {
/*  519 */         String key = line.substring(0, idx).trim();
/*  520 */         String value = unencodeValue(line.substring(idx + 1, line.length()).trim());
/*      */ 
/*  522 */         boolean isSpecial = false;
/*  523 */         if ((key.equals("dDocType")) || (key.equals("dDocAuthor")) || (key.equals("dSecurityGroup")))
/*      */         {
/*  527 */           value = validateEntry(key, value);
/*  528 */           isSpecial = true;
/*      */         }
/*      */ 
/*  531 */         if ((key.equals("primaryFile")) || (key.equals("alternateFile")))
/*      */         {
/*  533 */           value = FileUtils.fileSlashes(value);
/*      */         }
/*  535 */         localData.put(key, value);
/*      */ 
/*  537 */         if ((isSpecial) || (key.equals("SetFileDir")) || (key.equals("Action")) || (key.equals("sCollectionID")) || (key.equalsIgnoreCase("MetaDataUsage")) || (key.equalsIgnoreCase("UsageFields")) || (key.equalsIgnoreCase("StyleDirectory")) || (key.equalsIgnoreCase("DateFormat")) || (key.equalsIgnoreCase("UserDateFormat")) || (key.equalsIgnoreCase("InheritPreRevValues")))
/*      */         {
/*  548 */           this.m_carryOver.put(key, value);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  553 */     br.close();
/*      */   }
/*      */ 
/*      */   protected void processRecord(Properties localData, Properties defaults, int recordNumber)
/*      */     throws ServiceException, IOException
/*      */   {
/*  559 */     Properties copyLocalData = (Properties)localData.clone();
/*      */     try
/*      */     {
/*  563 */       String action = localData.getProperty("Action");
/*  564 */       Properties env = new Properties();
/*  565 */       String priFile = localData.getProperty("primaryFile");
/*  566 */       String altFile = localData.getProperty("alternateFile");
/*      */ 
/*  569 */       if (action == null)
/*      */       {
/*  571 */         action = "update";
/*  572 */         localData.put("Action", "update");
/*  573 */         copyLocalData.put("Action", "update");
/*      */       }
/*      */ 
/*  577 */       if (this.m_isFirstRecord)
/*      */       {
/*  579 */         String collectionID = localData.getProperty("sCollectionID");
/*  580 */         if ((collectionID == null) || (collectionID.equalsIgnoreCase("local")))
/*      */         {
/*  582 */           this.m_externalBatchload = false;
/*      */         }
/*      */         else
/*      */         {
/*  586 */           this.m_externalBatchload = true;
/*      */         }
/*      */ 
/*  589 */         if (this.m_externalBatchload)
/*      */         {
/*  592 */           SubjectEventMonitor doNothing = new SubjectEventMonitor()
/*      */           {
/*      */             public boolean checkForChange(String subject, long curTime)
/*      */             {
/*  596 */               return false;
/*      */             }
/*      */ 
/*      */             public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */             {
/*      */             }
/*      */           };
/*  606 */           SubjectManager.addSubjectMonitor("indexerwork", doNothing);
/*      */ 
/*  608 */           SearchLoader.cacheSearchCollections();
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  613 */       String serviceName = null;
/*  614 */       if (this.m_externalBatchload)
/*      */       {
/*  616 */         if (action.equalsIgnoreCase("delete"))
/*      */         {
/*  618 */           serviceName = "DELETE_EXTERNAL_DOC_INFO";
/*      */         }
/*      */         else
/*      */         {
/*  622 */           serviceName = "UPDATE_EXTERNAL_DOC_INFO";
/*      */         }
/*      */ 
/*  626 */         if (this.m_numRdsProcessed + 1 >= this.m_totalRecords)
/*      */         {
/*  628 */           this.m_dataBinder.putLocal("doIndexerWork", "1");
/*      */         }
/*      */         else
/*      */         {
/*  632 */           this.m_dataBinder.putLocal("doIndexerWork", "0");
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  637 */         serviceName = "CHECKIN_ARCHIVE";
/*      */       }
/*      */ 
/*  640 */       checkRequiredFields(localData, action);
/*      */ 
/*  642 */       if (!action.equalsIgnoreCase("delete"))
/*      */       {
/*  645 */         formatFilePath("primaryFile", priFile, localData);
/*      */ 
/*  647 */         formatFilePath("alternateFile", altFile, localData);
/*      */ 
/*  649 */         String val = SharedObjects.getEnvironmentValue("IsOverrideFormat");
/*  650 */         if (val == null)
/*      */         {
/*  652 */           val = SharedObjects.getEnvironmentValue("isOverrideFormat");
/*      */         }
/*  654 */         boolean isOverride = StringUtils.convertToBool(val, false);
/*      */ 
/*  657 */         if ((!isOverride) && 
/*  659 */           (localData.getProperty("primaryOverrideFormat") != null))
/*      */         {
/*  661 */           env.put("IsOverrideFormat", "true");
/*      */         }
/*      */ 
/*  665 */         env.put("doFileCopy", "1");
/*  666 */         this.m_dataBinder.setEnvironment(env);
/*      */       }
/*      */ 
/*  670 */       this.m_dataBinder.setLocalData(localData);
/*      */       try
/*      */       {
/*  689 */         this.m_dataBinder.m_blDateFormat = new IdcDateFormat();
/*  690 */         String dateFormatStr = localData.getProperty("UserDateFormat");
/*  691 */         if ((dateFormatStr == null) || (dateFormatStr.length() == 0))
/*      */         {
/*  693 */           dateFormatStr = localData.getProperty("DateFormat");
/*      */         }
/*  695 */         if ((dateFormatStr == null) || (dateFormatStr.length() == 0))
/*      */         {
/*  697 */           this.m_dataBinder.m_blDateFormat.initDefault();
/*      */         }
/*      */         else
/*      */         {
/*  701 */           this.m_dataBinder.m_blDateFormat.init(dateFormatStr);
/*      */         }
/*  703 */         this.m_dataBinder.putLocal("UserDateFormat", this.m_dataBinder.m_blDateFormat.toPattern());
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  707 */         int maxErrors = Integer.parseInt(this.m_idcProperties.getProperty("MaxErrorsAllowed"));
/*  708 */         this.m_numErrors = maxErrors;
/*      */ 
/*  710 */         throw new Exception(e);
/*      */       }
/*      */ 
/*  713 */       this.m_dataBinder.m_localeDateFormat = null;
/*  714 */       if (this.m_dataBinder.m_localizedFields != null)
/*      */       {
/*  716 */         this.m_dataBinder.m_localizedFields.clear();
/*      */       }
/*  718 */       this.m_dataBinder.m_determinedDataDateFormat = true;
/*      */       try
/*      */       {
/*  723 */         if (this.m_doBackground)
/*      */         {
/*  725 */           this.m_standAlone.executeService(serviceName, this.m_dataBinder);
/*      */         }
/*      */         else
/*      */         {
/*  729 */           AppLauncher.executeService(serviceName, this.m_dataBinder);
/*      */         }
/*  731 */         this.m_numSucceeded += 1;
/*      */ 
/*  734 */         if ((!action.equalsIgnoreCase("delete")) && (this.m_doCleanUp) && (!this.m_externalBatchload))
/*      */         {
/*  737 */           deleteFiles(localData);
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  742 */         IdcMessage msg = IdcMessageFactory.lc(e);
/*  743 */         String serviceErrorMsg = LocaleUtils.encodeMessage(msg);
/*      */ 
/*  745 */         String errorMsg = LocaleUtils.encodeMessage("csBatchLoaderCheckRecord", serviceErrorMsg, "" + recordNumber);
/*      */ 
/*  748 */         checkMaxError(errorMsg, false);
/*      */ 
/*  750 */         if (this.m_enableErrorFile)
/*      */         {
/*  752 */           logErrorFile(copyLocalData, serviceErrorMsg);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception amtDone)
/*      */     {
/*      */       float amtDone;
/*      */       String msg;
/*  759 */       String errorMsg = LocaleUtils.encodeMessage("csBatchLoaderCheckRecord", e.getMessage(), "" + recordNumber);
/*      */ 
/*  762 */       checkMaxError(errorMsg, true);
/*      */ 
/*  764 */       if (this.m_enableErrorFile)
/*      */       {
/*  766 */         logErrorFile(copyLocalData, e.getMessage());
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*      */       float amtDone;
/*      */       String msg;
/*  771 */       if (!this.m_isExiting)
/*      */       {
/*  773 */         float amtDone = ++this.m_numRdsProcessed / this.m_totalRecords * 100.0F;
/*  774 */         String msg = LocaleUtils.encodeMessage("csBatchLoaderProcessData", null, "" + this.m_numRdsProcessed, "" + this.m_totalRecords);
/*      */ 
/*  779 */         if (this.m_doBackground)
/*      */         {
/*  781 */           Report.trace(null, LocaleResources.localizeMessage(msg, this.m_cxt), null);
/*      */         }
/*      */         else
/*      */         {
/*  786 */           this.m_frame.reportProgress(1, msg, amtDone, 100.0F);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String unencodeValue(String val)
/*      */   {
/*  794 */     return this.m_resContainer.unencodeResourceString(val);
/*      */   }
/*      */ 
/*      */   protected void displayBatchFileError(String msg)
/*      */   {
/*  800 */     String prevMsg = "";
/*  801 */     if (this.m_vDocAuthors.size() > 0)
/*      */     {
/*  803 */       prevMsg = LocaleUtils.appendMessage(formatErrorMessage(this.m_vDocAuthors), "!csBatchLoaderUserTitle");
/*      */     }
/*      */ 
/*  806 */     if (this.m_vSecurityGroups.size() > 0)
/*      */     {
/*  808 */       prevMsg = prevMsg + LocaleUtils.appendMessage(formatErrorMessage(this.m_vSecurityGroups), "!csBatchLoaderSecGroupsTitle");
/*      */     }
/*      */ 
/*  812 */     if (this.m_vDocTypes.size() > 0)
/*      */     {
/*  814 */       prevMsg = prevMsg + LocaleUtils.appendMessage(formatErrorMessage(this.m_vDocTypes), "!csBatchLoaderDocTypesTitle");
/*      */     }
/*      */ 
/*  817 */     if (msg == null)
/*      */     {
/*  819 */       msg = "!csUnknownError";
/*      */     }
/*      */ 
/*  822 */     this.m_blfErrorMsg = LocaleUtils.appendMessage(prevMsg, msg);
/*  823 */     this.m_blfErrorMsg = LocaleResources.localizeMessage(this.m_blfErrorMsg, this.m_cxt);
/*  824 */     if (this.m_doBackground)
/*      */     {
/*  826 */       Report.trace(null, this.m_blfErrorMsg, null);
/*  827 */       IdcMessage tempMsg = null;
/*      */       try
/*      */       {
/*  831 */         writeFile(this.m_workingDir + "blferror.log", "!csBatchLoaderLogFileError");
/*  832 */         tempMsg = IdcMessageFactory.lc("csBatchLoaderPreparseErrorSaved", new Object[] { this.m_workingDir });
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  836 */         tempMsg = IdcMessageFactory.lc(e, "csBatchLoaderPreparseErrorSaved", new Object[0]);
/*      */       }
/*      */       finally
/*      */       {
/*  840 */         Report.error(null, null, tempMsg);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  845 */       JDialog dlg = new CustomDialog(this.m_frame, LocaleResources.getString("csBatchLoaderMessage", this.m_cxt), true);
/*      */ 
/*  847 */       DialogHelper dlgHelper = new DialogHelper();
/*  848 */       dlgHelper.attachToDialog(dlg, null, this.m_idcProperties);
/*  849 */       JPanel mainPanel = dlgHelper.m_mainPanel;
/*      */ 
/*  851 */       CustomTextArea cta = new CustomTextArea(this.m_blfErrorMsg, 20, 50);
/*  852 */       dlgHelper.addComponent(mainPanel, cta);
/*  853 */       dlgHelper.addOK(null);
/*      */ 
/*  855 */       ActionListener saveListener = new ActionListener()
/*      */       {
/*      */         public void actionPerformed(ActionEvent e)
/*      */         {
/*  859 */           BatchLoaderApp.this.saveToFile("!csBatchLoaderLogFileError");
/*      */         }
/*      */       };
/*  862 */       dlgHelper.addCommandButton(LocaleResources.getString("csBatchLoaderSaveToFileText", this.m_cxt), saveListener);
/*      */ 
/*  864 */       dlgHelper.show();
/*  865 */       Report.trace(null, this.m_blfErrorMsg, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String formatErrorMessage(Vector vector)
/*      */   {
/*  871 */     String msg = "\n\t\t" + vector.elementAt(0);
/*      */ 
/*  873 */     for (int i = 1; i < vector.size(); ++i)
/*      */     {
/*  875 */       msg = msg + "\n\t\t" + vector.elementAt(i);
/*      */     }
/*      */ 
/*  878 */     return msg;
/*      */   }
/*      */ 
/*      */   protected void displayError(String msg)
/*      */   {
/*  883 */     Report.error(null, msg, null);
/*      */ 
/*  885 */     if (this.m_doBackground)
/*      */     {
/*  887 */       Report.trace(null, LocaleResources.localizeMessage(msg, this.m_cxt), null);
/*      */     }
/*      */     else
/*      */     {
/*  891 */       reportError(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void reportError(String msg)
/*      */   {
/*  899 */     MessageBox.reportError(this.m_frame.m_appHelper, msg);
/*      */   }
/*      */ 
/*      */   public void reportError(IdcMessage msg)
/*      */   {
/*  904 */     MessageBox.reportError(this.m_frame.m_appHelper, msg);
/*      */   }
/*      */ 
/*      */   protected void displayDone()
/*      */   {
/*  909 */     IdcMessage msg = null;
/*  910 */     if ((this.m_enableErrorFile) && (this.m_numErrors > 0))
/*      */     {
/*  912 */       msg = IdcMessageFactory.lc("csBatchLoaderDoneErrors", new Object[] { this.m_batchLoaderPath, Integer.valueOf(this.m_numRdsProcessed), Integer.valueOf(this.m_numSucceeded), Integer.valueOf(this.m_numErrors), FileUtils.directorySlashes(this.m_errorFilePath) + this.m_errorFileName });
/*      */     }
/*      */     else
/*      */     {
/*  918 */       msg = IdcMessageFactory.lc("csBatchLoaderDone", new Object[] { this.m_batchLoaderPath, Integer.valueOf(this.m_numRdsProcessed), Integer.valueOf(this.m_numSucceeded), Integer.valueOf(this.m_numErrors) });
/*      */     }
/*      */ 
/*  922 */     this.m_batchLoaderStarted = false;
/*  923 */     this.m_isExiting = true;
/*      */ 
/*  925 */     Report.info(null, null, msg);
/*      */ 
/*  927 */     if (this.m_doBackground)
/*      */     {
/*  929 */       Report.trace(null, null, msg);
/*      */     }
/*      */     else
/*      */     {
/*  933 */       if (MessageBox.doMessage(this.m_frame.m_appHelper, msg, 1) != 1)
/*      */         return;
/*  935 */       this.m_frame.reportProgress(1, "", 0.0F, 100.0F);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   protected void checkMaxError(String msg, boolean logError)
/*      */   {
/*  944 */     IdcMessage idcmsg = IdcMessageFactory.lc();
/*  945 */     idcmsg.m_msgEncoded = msg;
/*  946 */     checkMaxError(idcmsg, logError);
/*      */   }
/*      */ 
/*      */   protected void checkMaxError(IdcMessage msg, boolean logError)
/*      */   {
/*  951 */     this.m_numErrors += 1;
/*  952 */     int maxErrors = Integer.parseInt(this.m_idcProperties.getProperty("MaxErrorsAllowed"));
/*      */ 
/*  954 */     if (logError)
/*      */     {
/*  956 */       Report.error(null, null, msg);
/*      */     }
/*      */     else
/*      */     {
/*  960 */       Report.trace(null, null, msg);
/*      */     }
/*      */ 
/*  963 */     if (this.m_numErrors < maxErrors)
/*      */       return;
/*  965 */     String finalMsg = "!csBatchLoaderMaxErrorsReached";
/*      */ 
/*  967 */     if (this.m_doBackground)
/*      */     {
/*  969 */       Report.trace(null, LocaleResources.localizeMessage(finalMsg, this.m_cxt), null);
/*      */     }
/*      */     else
/*      */     {
/*  973 */       reportError(finalMsg);
/*  974 */       this.m_totalRecords = 0;
/*  975 */       this.m_numRdsProcessed = 0;
/*  976 */       this.m_numSucceeded = 0;
/*  977 */       this.m_numErrors = 0;
/*  978 */       this.m_frame.reportProgress(1, "", 0.0F, 100.0F);
/*      */     }
/*  980 */     this.m_isExiting = true;
/*  981 */     this.m_batchLoaderStarted = false;
/*      */   }
/*      */ 
/*      */   protected void logErrorFile(Properties copyLocalData, String errorMsg)
/*      */     throws ServiceException, IOException
/*      */   {
/*  988 */     File file = new File(this.m_errorFilePath, this.m_errorFileName);
/*  989 */     File tempFile = new File(this.m_errorFilePath, "temp.log");
/*  990 */     BufferedReader reader = null;
/*  991 */     BufferedWriter writer = FileUtils.openDataWriter(tempFile);
/*      */ 
/*  993 */     if (file.exists())
/*      */     {
/*  995 */       reader = FileUtils.openDataReader(file);
/*  996 */       String line = null;
/*  997 */       while ((line = reader.readLine()) != null)
/*      */       {
/*  999 */         writer.write(line + "\n");
/*      */       }
/* 1001 */       reader.close();
/* 1002 */       file.delete();
/*      */     }
/*      */ 
/* 1006 */     String hashedErrorMsg = addCommentHashes(errorMsg);
/* 1007 */     writer.write("#Internal error version\n");
/* 1008 */     writer.write("#" + hashedErrorMsg + "\n");
/*      */ 
/* 1011 */     errorMsg = LocaleResources.localizeMessage(errorMsg, this.m_cxt);
/* 1012 */     hashedErrorMsg = addCommentHashes(errorMsg);
/* 1013 */     writer.write("#\n#External error version\n");
/* 1014 */     writer.write("#" + hashedErrorMsg + "\n");
/*      */ 
/* 1017 */     for (Enumeration e = copyLocalData.keys(); e.hasMoreElements(); )
/*      */     {
/* 1019 */       String key = (String)e.nextElement();
/* 1020 */       String val = copyLocalData.getProperty(key);
/* 1021 */       writer.write(key + '=' + val + '\n');
/*      */     }
/* 1023 */     writer.write("<<EOD>>\n");
/* 1024 */     writer.close();
/* 1025 */     tempFile.renameTo(file);
/*      */   }
/*      */ 
/*      */   protected String addCommentHashes(String msg)
/*      */   {
/* 1030 */     StringBuffer buffer = new StringBuffer();
/* 1031 */     int startIndex = 0;
/*      */ 
/* 1034 */     int nextIndex = msg.indexOf(10, startIndex);
/* 1035 */     if (nextIndex < 0)
/*      */     {
/* 1037 */       return msg;
/*      */     }
/*      */ 
/* 1040 */     while (nextIndex >= 0)
/*      */     {
/* 1042 */       buffer.append(msg.substring(startIndex, nextIndex));
/* 1043 */       buffer.append("\n#");
/*      */ 
/* 1045 */       startIndex = nextIndex + 1;
/* 1046 */       nextIndex = msg.indexOf(10, startIndex);
/*      */     }
/*      */ 
/* 1049 */     buffer.append(msg.substring(startIndex));
/* 1050 */     return buffer.toString();
/*      */   }
/*      */ 
/*      */   protected void saveToFile(String errMsg)
/*      */   {
/* 1056 */     JFileChooser fileDlg = new IdcFileChooser();
/* 1057 */     fileDlg.setDialogTitle(LocaleResources.getString("csBatchLoaderSaveTitle", this.m_cxt));
/* 1058 */     fileDlg.showSaveDialog(null);
/*      */ 
/* 1061 */     File f = fileDlg.getSelectedFile();
/* 1062 */     if (f == null)
/*      */       return;
/*      */     try
/*      */     {
/* 1066 */       writeFile(f.getAbsolutePath(), errMsg);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1070 */       reportError(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void writeFile(String filePath, String errMsg)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1079 */       File file = new File(filePath);
/* 1080 */       BufferedWriter bw = FileUtils.openDataWriter(file);
/* 1081 */       bw.write(this.m_blfErrorMsg);
/* 1082 */       bw.close();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1086 */       throw new ServiceException(LocaleUtils.encodeMessage("csUnknownError", e.getMessage()));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void formatFilePath(String key, String file, Properties localData)
/*      */   {
/* 1097 */     if ((file == null) || (file.length() == 0))
/*      */     {
/* 1099 */       return;
/*      */     }
/*      */ 
/* 1102 */     String dir = localData.getProperty("SetFileDir");
/* 1103 */     if (dir == null)
/*      */     {
/* 1105 */       if (this.m_batchLoaderPath != null)
/*      */       {
/* 1107 */         dir = FileUtils.getDirectory(this.m_batchLoaderPath);
/*      */       }
/*      */       else
/*      */       {
/* 1111 */         dir = this.m_workingDir;
/*      */       }
/*      */     }
/*      */ 
/* 1115 */     localData.put(key, FileUtils.getAbsolutePath(dir, file));
/*      */   }
/*      */ 
/*      */   protected void deleteFiles(Properties localData)
/*      */   {
/* 1120 */     deleteFile(localData.getProperty("primaryFile"));
/* 1121 */     deleteFile(localData.getProperty("alternateFile"));
/*      */   }
/*      */ 
/*      */   protected void deleteFile(String file)
/*      */   {
/* 1126 */     if ((file == null) || (file.length() == 0))
/*      */     {
/* 1129 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1134 */       File pFile = new File(file);
/*      */ 
/* 1136 */       if (!pFile.delete())
/*      */       {
/* 1138 */         String tmpMsg = LocaleUtils.encodeMessage("csBatchLoaderFileDeleteError", null, file);
/* 1139 */         Report.trace(null, LocaleResources.localizeMessage(tmpMsg, this.m_cxt), null);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1144 */       Report.error(null, e, "csBatchLoaderFileDeleteError", new Object[] { file });
/*      */     }
/*      */   }
/*      */ 
/*      */   public void readIntradocCfgFile() throws ServiceException
/*      */   {
/*      */     Enumeration e;
/*      */     try
/*      */     {
/* 1153 */       String extraKeys = "BatchLoaderPath,EnableErrorFile,MaxErrorsAllowed,CleanUp,BatchLoaderUserName";
/*      */ 
/* 1155 */       this.m_propLoader.addKeys(extraKeys, null);
/*      */ 
/* 1158 */       this.m_propLoader.initIdc();
/* 1159 */       this.m_idcProperties = this.m_propLoader.getIdcProperties();
/*      */ 
/* 1162 */       for (e = this.m_idcProperties.keys(); e.hasMoreElements(); )
/*      */       {
/* 1164 */         String key = (String)e.nextElement();
/*      */ 
/* 1166 */         if ((key.equals("maxErrorsAllowed")) || (key.equals("cleanUp")))
/*      */         {
/* 1168 */           String val = this.m_idcProperties.getProperty(key);
/* 1169 */           this.m_idcProperties.remove(key);
/*      */ 
/* 1171 */           if (key.equals("maxErrorsAllowed"))
/*      */           {
/* 1173 */             this.m_idcProperties.put("MaxErrorsAllowed", val);
/*      */           }
/* 1175 */           else if (key.equals("cleanUp"))
/*      */           {
/* 1177 */             this.m_idcProperties.put("CleanUp", val);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1184 */       throw new ServiceException(LocaleUtils.encodeMessage("csBatchLoaderFileReadError", e.getMessage()));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void writeIntradocCfgFile()
/*      */     throws ServiceException
/*      */   {
/* 1191 */     this.m_idcProperties.put("BatchLoaderUserName", AppLauncher.getUser());
/*      */ 
/* 1193 */     String filePath = SharedObjects.getEnvironmentValue("BinDir") + "intradoc.cfg";
/* 1194 */     FileUtils.validatePath(filePath, IdcMessageFactory.lc("csBatchLoaderConfigFileError", new Object[0]), 3);
/*      */ 
/* 1197 */     this.m_propLoader.mergePropertyValuesEx(this.m_idcProperties, null, true);
/* 1198 */     this.m_propLoader.saveIdc();
/*      */   }
/*      */ 
/*      */   protected boolean retrieveProperties()
/*      */   {
/* 1205 */     if (this.m_doBackground)
/*      */     {
/* 1207 */       Enumeration en = this.m_idcProperties.keys();
/* 1208 */       while (en.hasMoreElements())
/*      */       {
/* 1210 */         String name = (String)en.nextElement();
/* 1211 */         String value = this.m_idcProperties.getProperty(name);
/* 1212 */         String[] errMsg = new String[1];
/* 1213 */         if (!validateProperty(name, value, errMsg))
/*      */         {
/* 1215 */           return false;
/*      */         }
/*      */       }
/*      */     }
/* 1219 */     else if (!this.m_frame.m_appHelper.retrieveComponentValues())
/*      */     {
/* 1221 */       return false;
/*      */     }
/*      */ 
/* 1224 */     return true;
/*      */   }
/*      */ 
/*      */   protected boolean validateProperty(String name, String value, String[] errMsg)
/*      */   {
/* 1229 */     boolean isValid = true;
/* 1230 */     String error = null;
/*      */ 
/* 1232 */     if ((name.equals("MaxErrorsAllowed")) && 
/* 1234 */       (Validation.checkInteger(value) != 0))
/*      */     {
/* 1236 */       error = LocaleResources.getString("!csBatchLoaderMaxErrorsNumeric", this.m_cxt);
/* 1237 */       isValid = false;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1243 */       if (name.equals("BatchLoaderPath"))
/*      */       {
/* 1247 */         FileUtils.validatePath(value, IdcMessageFactory.lc("csBatchLoaderPathError", new Object[0]), 1);
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1252 */       error = e.getMessage();
/* 1253 */       isValid = false;
/*      */     }
/*      */ 
/* 1256 */     if ((!isValid) && (this.m_doBackground))
/*      */     {
/* 1258 */       displayError(error);
/*      */     }
/*      */     else
/*      */     {
/* 1262 */       errMsg[0] = error;
/*      */     }
/*      */ 
/* 1265 */     return isValid;
/*      */   }
/*      */ 
/*      */   protected String validateEntry(String key, String value)
/*      */   {
/* 1270 */     if (key.equals("dDocAuthor"))
/*      */     {
/* 1272 */       Vector authors = this.m_authorsList;
/* 1273 */       if (UserUtils.hasExternalUsers())
/*      */       {
/* 1275 */         authors = null;
/*      */       }
/* 1277 */       return doesEntryExist(value, authors, this.m_hDocAuthors, this.m_vDocAuthors);
/*      */     }
/* 1279 */     if (key.equals("dSecurityGroup"))
/*      */     {
/* 1281 */       return doesEntryExist(value, this.m_securityList, this.m_hSecurityGroups, this.m_vSecurityGroups);
/*      */     }
/*      */ 
/* 1285 */     return doesEntryExist(value, this.m_docTypesList, this.m_hDocTypes, this.m_vDocTypes);
/*      */   }
/*      */ 
/*      */   protected String doesEntryExist(String val, Vector optList, Hashtable hTable, Vector vector)
/*      */   {
/* 1293 */     int i = 0;
/* 1294 */     String csValue = "";
/*      */ 
/* 1297 */     if (optList != null)
/*      */     {
/* 1299 */       for (; ; ++i) { if (i >= optList.size())
/*      */           break label68;
/* 1301 */         csValue = (String)optList.elementAt(i);
/* 1302 */         csValue = csValue.trim();
/*      */ 
/* 1304 */         if (csValue.equalsIgnoreCase(val)) {
/*      */           break label68;
/*      */         }
/*      */  }
/*      */ 
/*      */ 
/*      */     }
/*      */ 
/* 1312 */     if (val.trim().length() > 0)
/*      */     {
/* 1314 */       return val;
/*      */     }
/*      */ 
/* 1321 */     if ((optList == null) || (i == optList.size()))
/*      */     {
/* 1323 */       label68: String temp = (String)hTable.put(val, val);
/*      */ 
/* 1326 */       if (temp == null)
/*      */       {
/* 1328 */         vector.addElement(val);
/*      */       }
/*      */     }
/*      */ 
/* 1332 */     return csValue;
/*      */   }
/*      */ 
/*      */   protected void checkRequiredFields(Properties localData, String action)
/*      */     throws ServiceException
/*      */   {
/* 1338 */     String docName = localData.getProperty("dDocName");
/* 1339 */     String[] requiredFields = { "dDocType", "dDocTitle", "dDocAuthor", "dSecurityGroup" };
/*      */ 
/* 1341 */     if (!action.equalsIgnoreCase("delete"))
/*      */     {
/* 1343 */       if (docName == null)
/*      */       {
/* 1345 */         docName = localData.getProperty("dDocTitle");
/* 1346 */         if (docName == null)
/*      */         {
/* 1348 */           docName = "<undefined>";
/*      */         }
/*      */       }
/*      */ 
/* 1352 */       int numFields = requiredFields.length;
/*      */ 
/* 1354 */       for (int i = 0; i < numFields; ++i)
/*      */       {
/* 1356 */         String name = docName;
/*      */ 
/* 1360 */         if (docName.equals("<undefined>"))
/*      */         {
/* 1362 */           name = localData.getProperty("dDocTitle");
/*      */         }
/*      */ 
/* 1365 */         if (name == null)
/*      */         {
/* 1367 */           name = "";
/*      */         }
/*      */ 
/* 1370 */         if (localData.getProperty(requiredFields[i]) != null)
/*      */           continue;
/* 1372 */         throw new ServiceException(LocaleUtils.encodeMessage("csBatchLoaderRequiredFieldsMissing", null, name, requiredFields[i]));
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1381 */       if ((docName != null) || (this.m_externalBatchload))
/*      */         return;
/* 1383 */       throw new ServiceException("!csBatchLoaderContentIDMissing");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1391 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93277 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.batchloader.BatchLoaderApp
 * JD-Core Version:    0.5.4
 */