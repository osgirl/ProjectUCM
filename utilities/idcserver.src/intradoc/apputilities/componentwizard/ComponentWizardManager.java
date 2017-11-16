/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.ActiveState;
/*     */ import intradoc.server.IdcExtendedLoader;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.server.utils.ComponentListEditor;
/*     */ import intradoc.server.utils.ComponentListManager;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ComponentWizardManager
/*     */ {
/*  38 */   public IntradocComponent m_component = null;
/*  39 */   public ComponentListEditor m_editor = null;
/*     */ 
/*  41 */   public static boolean m_isEnvironmentLoaded = false;
/*  42 */   public static Workspace m_workspace = null;
/*     */ 
/*     */   public void init()
/*     */     throws DataException, ServiceException
/*     */   {
/*  51 */     ComponentListManager.init();
/*  52 */     this.m_editor = ComponentListManager.getEditor();
/*     */ 
/*  54 */     String intradocDir = CWizardUtils.changeDriveLetterToUpper(this.m_editor.getIntradocDir());
/*  55 */     SharedObjects.putEnvironmentValue("IntradocDir", intradocDir);
/*     */   }
/*     */ 
/*     */   public void addOrEditComponent(Map props, DataBinder orgData, Map args)
/*     */     throws ServiceException
/*     */   {
/*  62 */     String name = (String)props.get("name");
/*     */     try
/*     */     {
/*  65 */       initComponentInfo(name, props, args);
/*     */ 
/*  67 */       String status = "Disabled";
/*  68 */       boolean isNew = StringUtils.convertToBool((String)args.get("isNew"), false);
/*  69 */       if (!isNew)
/*     */       {
/*  71 */         DataBinder cmpData = this.m_editor.getComponentData(name);
/*  72 */         if (cmpData != null)
/*     */         {
/*  74 */           String cmpType = cmpData.getLocal("componentType");
/*  75 */           if ((cmpType == null) || (cmpType.contains("local")))
/*     */           {
/*  77 */             isNew = true;
/*     */           }
/*  81 */           else if (this.m_editor.isComponentEnabled(name))
/*     */           {
/*  83 */             status = "Enabled";
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*  89 */       if ((isNew) && 
/*  91 */         (!this.m_editor.isComponentNameUnique(name)))
/*     */       {
/*  93 */         throw new ServiceException(LocaleUtils.encodeMessage("csCompWizCompExists", null, name));
/*     */       }
/*     */ 
/* 100 */       props.put("status", status);
/*     */ 
/* 102 */       this.m_editor.addComponent(props, orgData);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 106 */       this.m_editor.undoChanges(LocaleUtils.encodeMessage("csCompWizAddError", null, name), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void deleteComponent(Map props) throws DataException, ServiceException
/*     */   {
/* 112 */     this.m_editor.deleteComponent(props);
/*     */   }
/*     */ 
/*     */   public void enableOrDisableComponent(Properties props, boolean isEnabled)
/*     */     throws ServiceException, DataException
/*     */   {
/* 118 */     String name = props.getProperty("name");
/* 119 */     if (isEnabled)
/*     */     {
/* 122 */       IntradocComponent info = new IntradocComponent();
/* 123 */       Map args = new HashMap();
/* 124 */       args.put("isNew", "0");
/* 125 */       info.init(name, props, args, false);
/* 126 */       String errMsg = "";
/* 127 */       boolean isError = info.cacheFileInfo();
/*     */ 
/* 129 */       if ((isError) && (info.m_errorMsg != null))
/*     */       {
/* 131 */         errMsg = info.m_errorMsg;
/*     */       }
/*     */       else
/*     */       {
/* 135 */         for (int i = 0; i < info.m_fileInfo.size(); ++i)
/*     */         {
/* 137 */           ResourceFileInfo fileinfo = (ResourceFileInfo)info.m_fileInfo.elementAt(i);
/* 138 */           if (fileinfo.m_errMsg == null)
/*     */             continue;
/* 140 */           errMsg = errMsg + fileinfo.m_errMsg;
/* 141 */           isError = true;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 146 */       if (isError)
/*     */       {
/* 148 */         throw new ServiceException(LocaleUtils.appendMessage(errMsg, LocaleUtils.encodeMessage("csCompWizCriticalErrors", null, name)));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 153 */     this.m_editor.enableOrDisableComponent(name, isEnabled);
/*     */   }
/*     */ 
/*     */   public DataResultSet getEditComponents()
/*     */   {
/* 158 */     return this.m_editor.getComponentSet();
/*     */   }
/*     */ 
/*     */   public void clearComponentInfo()
/*     */   {
/* 163 */     this.m_component = null;
/*     */   }
/*     */ 
/*     */   public void initComponentInfo(String name, Map<String, String> map, Map args)
/*     */     throws ServiceException
/*     */   {
/* 169 */     this.m_component = new IntradocComponent();
/* 170 */     this.m_component.init(name, map, args, true);
/*     */   }
/*     */ 
/*     */   public static void initFullEnvironment(boolean isLightWeight)
/*     */     throws ServiceException, DataException
/*     */   {
/* 176 */     SharedObjects.putEnvironmentValue("EnableSchemaPublish", "false");
/*     */ 
/* 179 */     PluginFilters.removeAllFilters();
/* 180 */     IdcSystemLoader.loadCustomEnvironmentValues();
/*     */ 
/* 183 */     ActiveState.load();
/*     */ 
/* 185 */     IdcSystemLoader.initLogInfo();
/*     */ 
/* 187 */     IdcExtendedLoader extendedLoader = (IdcExtendedLoader)ComponentClassFactory.createClassInstance("IdcExtendedLoader", "intradoc.server.IdcExtendedLoader", "!csCustomInitializerConstructionError");
/*     */ 
/* 190 */     IdcSystemLoader.setExtendedLoader(extendedLoader);
/* 191 */     int flags = (isLightWeight) ? 2 : 0;
/*     */     try
/*     */     {
/* 194 */       IdcSystemLoader.managePasswords();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 199 */       Report.trace("system", "Could not manage passwords during startup", e);
/*     */     }
/* 201 */     IdcSystemLoader.initProviders(flags);
/* 202 */     if (!isLightWeight)
/*     */     {
/* 204 */       m_workspace = IdcSystemLoader.loadDatabase(2);
/*     */ 
/* 207 */       IdcSystemLoader.loadCaches(m_workspace);
/*     */     }
/*     */ 
/* 210 */     IdcSystemLoader.loadServiceData();
/*     */ 
/* 214 */     m_isEnvironmentLoaded = true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 219 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99914 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.ComponentWizardManager
 * JD-Core Version:    0.5.4
 */