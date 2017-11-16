/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.apputilities.componentwizard.ComponentToolLauncher;
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.FeaturesInterface;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.SimpleParameters;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.ComponentLoader;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.ServerOSSettingsHelper;
/*     */ import intradoc.server.utils.ComponentListEditor;
/*     */ import intradoc.server.utils.ComponentListManager;
/*     */ import intradoc.server.utils.ComponentPreferenceData;
/*     */ import intradoc.shared.Features;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.CollectionUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class InstallComponents
/*     */   implements SectionInstaller
/*     */ {
/*     */   protected SysInstaller m_installer;
/*     */ 
/*     */   public void init(SysInstaller installer)
/*     */   {
/*  43 */     this.m_installer = installer;
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  50 */     init(installer);
/*     */ 
/*  52 */     DataResultSet productNames = (DataResultSet)this.m_installer.m_binder.getResultSet("TemplateProductNames");
/*     */ 
/*  55 */     if (productNames == null)
/*     */     {
/*  58 */       if (!installer.m_isUpdate)
/*     */       {
/*  60 */         installer.prepareForLocks("data/components");
/*  61 */         installer.prepareForLocks("config/");
/*     */       }
/*  63 */       installer.prepareForLocks("data/publish");
/*     */     }
/*     */ 
/*  67 */     DataResultSet drset = (DataResultSet)installer.m_binder.getResultSet("Components");
/*     */ 
/*  69 */     if (drset == null)
/*     */     {
/*  71 */       Report.trace("install", "Components table not defined", null);
/*  72 */       return 0;
/*     */     }
/*     */ 
/*  78 */     ResourceContainer rc = new ResourceContainer();
/*     */     try
/*     */     {
/*  81 */       String file = "resources/core/tables/component_info.htm";
/*  82 */       if (this.m_installer.isRefineryInstall())
/*     */       {
/*  84 */         file = "resources/ibr/tables/ibr_component_info.htm";
/*     */       }
/*  86 */       file = this.m_installer.computeDestination(file);
/*  87 */       Reader r = FileUtils.openDataReader(file);
/*  88 */       rc.parseAndAddResources(r, file);
/*  89 */       DataResultSet list = new DataResultSet();
/*  90 */       list.init(rc.getTable("ReservedFilesList"));
/*  91 */       SharedObjects.putTable("ReservedFilesList", list);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  95 */       throw new ServiceException(e);
/*     */     }
/*  97 */     Properties props = SharedObjects.getSafeEnvironment();
/*     */     try
/*     */     {
/* 100 */       FileUtils.loadProperties(props, this.m_installer.computeDestinationEx("bin/intradoc.cfg", false));
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 105 */       throw new ServiceException(e);
/*     */     }
/* 107 */     Map tables = (Map)AppObjectRepository.getObject("tables");
/* 108 */     String[] tableList = { "PlatformConfigTable", "PermissionConfig", "PermissionExecutables" };
/*     */ 
/* 114 */     for (int i = 0; i < tableList.length; ++i)
/*     */     {
/* 116 */       DataResultSet tmp = (DataResultSet)this.m_installer.m_binder.getResultSet(tableList[i]);
/*     */ 
/* 118 */       tables.put(tableList[i], tmp);
/*     */     }
/*     */ 
/* 121 */     LegacyDirectoryLocator.buildRootDirectories();
/* 122 */     String idcProductName = SharedObjects.getEnvironmentValue("IdcProductName");
/*     */     try
/*     */     {
/* 125 */       ComponentLoader.m_quiet = true;
/* 126 */       SharedObjects.removeEnvironmentValue("PrimaryResourceTable");
/* 127 */       String resourcesDir = this.m_installer.getInstallValue("IdcResourcesDir", null);
/* 128 */       SharedObjects.putEnvironmentValue("IdcResourcesDir", resourcesDir);
/*     */ 
/* 130 */       SharedObjects.putEnvironmentValue("UseHomeDirComponents", "false");
/* 131 */       ServerOSSettingsHelper helper = new ServerOSSettingsHelper();
/* 132 */       intradoc.common.EnvUtils.m_osHelper = helper;
/* 133 */       if (productNames != null)
/*     */       {
/* 135 */         List productList = new ArrayList();
/* 136 */         List productList2 = new ArrayList();
/* 137 */         DataResultSet allComponents = null;
/* 138 */         for (productNames.first(); productNames.isRowPresent(); productNames.next())
/*     */         {
/* 140 */           String product = productNames.getStringValue(0);
/* 141 */           if (productList.indexOf(product) >= 0) {
/*     */             continue;
/*     */           }
/*     */ 
/* 145 */           productList.add(product);
/* 146 */           productList2.add(product);
/*     */         }
/* 148 */         productList2.add("allcomponents");
/*     */ 
/* 150 */         for (String product : productList2)
/*     */         {
/* 153 */           DataResultSet features = SharedObjects.getTable("CoreFeatures");
/* 154 */           features = IdcSystemLoader.filterFeatures(features);
/* 155 */           FeaturesInterface f = Features.newFeaturesObject(null, null);
/* 156 */           Features.registerFeaturesFromResultSet(f, features, null);
/* 157 */           SharedObjects.putObject("Features", "InitialCoreFeatures", f);
/*     */ 
/* 159 */           Report.trace("install", "installing components for product " + product, null);
/*     */ 
/* 161 */           List componentSetProducts = new ArrayList();
/* 162 */           if (product.equals("allcomponents"))
/*     */           {
/* 164 */             componentSetProducts = productList;
/*     */           }
/*     */           else
/*     */           {
/* 168 */             componentSetProducts.add(product);
/*     */           }
/* 170 */           DataResultSet sets = null;
/* 171 */           for (String p : componentSetProducts)
/*     */           {
/* 173 */             String rsetName = "ComponentSetList-" + p;
/* 174 */             DataResultSet s = (DataResultSet)this.m_installer.m_binder.getResultSet(rsetName);
/* 175 */             if (s == null)
/*     */             {
/* 177 */               throw new NullPointerException("!$Missing required ResultSet " + rsetName);
/*     */             }
/*     */ 
/* 180 */             if (sets == null)
/*     */             {
/* 182 */               sets = s;
/*     */             }
/*     */             else
/*     */             {
/*     */               try
/*     */               {
/* 188 */                 sets.mergeWithFlags("ComponentSetName", s, 0, -1);
/*     */               }
/*     */               catch (DataException e)
/*     */               {
/* 192 */                 throw new ServiceException(e);
/*     */               }
/*     */             }
/*     */           }
/*     */ 
/* 197 */           DataResultSet productSet = new DataResultSet();
/* 198 */           for (SimpleParameters params : sets.getSimpleParametersIterable())
/*     */           {
/* 200 */             String rsetName = params.get("ComponentSetName");
/* 201 */             String enabled = params.get("Enabled");
/* 202 */             if (!StringUtils.convertToBool(enabled, false)) {
/*     */               continue;
/*     */             }
/*     */ 
/* 206 */             ResultSet addons = this.m_installer.m_binder.getResultSet(rsetName);
/* 207 */             if (addons == null)
/*     */             {
/* 209 */               throw new NullPointerException("!$Missing required ResultSet " + rsetName);
/*     */             }
/*     */             try
/*     */             {
/* 213 */               productSet.mergeWithFlags("ComponentName", addons, 16, -1);
/*     */             }
/*     */             catch (DataException e)
/*     */             {
/* 218 */               throw new ServiceException(e);
/*     */             }
/*     */           }
/*     */ 
/* 222 */           if (product.equals("allcomponents"))
/*     */           {
/* 224 */             FieldInfo info = new FieldInfo();
/* 225 */             productSet.getFieldInfo("Conditions", info);
/* 226 */             for (productSet.first(); productSet.isRowPresent(); productSet.next())
/*     */             {
/*     */               try
/*     */               {
/* 230 */                 productSet.setCurrentValue(info.m_index, "always");
/*     */               }
/*     */               catch (DataException e)
/*     */               {
/* 234 */                 throw new ServiceException(e);
/*     */               }
/*     */             }
/*     */           }
/*     */ 
/* 239 */           SharedObjects.putEnvironmentValue("IdcProductName", product);
/* 240 */           SharedObjects.putEnvironmentValue("LoadScriptExtensions", "false");
/* 241 */           SharedObjects.putEnvironmentValue("IsInstallerEnv", "true");
/* 242 */           ComponentLoader.reset();
/* 243 */           ComponentListManager.reset();
/*     */           try
/*     */           {
/* 246 */             IdcSystemLoader.initComponentData();
/* 247 */             features = SharedObjects.getTable("CoreFeatures");
/* 248 */             IdcSystemLoader.registerFeatures(features);
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 253 */             throw new ServiceException(e);
/*     */           }
/* 255 */           String stateCfgFile = this.m_installer.computeDestinationEx("config/state.cfg", false);
/* 256 */           String productStateCfgFile = this.m_installer.computeDestinationEx("config/state-" + product + ".cfg", false);
/*     */ 
/* 258 */           FileUtils.deleteFile(stateCfgFile);
/* 259 */           installComponents(productSet);
/*     */ 
/* 261 */           ComponentListEditor editor = ComponentListManager.getEditor();
/* 262 */           DataResultSet newComponents = editor.getComponentSet();
/* 263 */           if (allComponents == null)
/*     */           {
/* 265 */             allComponents = newComponents;
/*     */           }
/*     */           else
/*     */           {
/*     */             try
/*     */             {
/* 271 */               allComponents.mergeWithFlags("name", newComponents, 0, -1);
/*     */             }
/*     */             catch (DataException e)
/*     */             {
/* 275 */               throw new ServiceException(e);
/*     */             }
/*     */           }
/* 278 */           ComponentListManager.reset();
/* 279 */           FileUtils.copyFile(stateCfgFile, productStateCfgFile);
/*     */         }
/* 281 */         SharedObjects.putEnvironmentValue("SkipCreatePrivateDirectoryForSecurity", "true");
/* 282 */         this.m_installer.editConfigFile("bin/intradoc.cfg", "IDC_NO_SANITY_CHECKS", "1");
/* 283 */         this.m_installer.editConfigFile("config/config.cfg", "IdcProductNameList", StringUtils.createStringSimple(productList));
/*     */ 
/* 285 */         SharedObjects.removeEnvironmentValue("SkipCreatePrivateDirectoryForSecurity");
/* 286 */         String allComponentsFile = this.m_installer.getInstallValue("AllComponentsFile", null);
/* 287 */         if (allComponentsFile != null)
/*     */         {
/* 289 */           DataBinder binder = new DataBinder();
/* 290 */           binder.addResultSet("AllComponents", allComponents);
/* 291 */           String dir = FileUtils.getParent(allComponentsFile);
/* 292 */           String file = FileUtils.getName(allComponentsFile);
/* 293 */           ResourceUtils.serializeDataBinderEx(dir, file, binder, 1);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/*     */         try
/*     */         {
/* 300 */           ComponentLoader.reset();
/* 301 */           IdcSystemLoader.initComponentData();
/* 302 */           boolean failOnError = true;
/* 303 */           IdcSystemLoader.loadComponentDataEx(failOnError);
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 307 */           throw new ServiceException(e);
/*     */         }
/* 309 */         installComponents(drset);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 314 */       this.m_installer.m_installerConfig.remove("target.platform");
/* 315 */       SharedObjects.putEnvironmentValue("IdcProductName", idcProductName);
/* 316 */       ComponentListManager.reset();
/*     */     }
/* 318 */     return 0;
/*     */   }
/*     */ 
/*     */   public void installComponents(DataResultSet components)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 326 */       IdcSystemConfig.initFileStoreObjects();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 330 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 333 */     HashMap installedComponents = new HashMap();
/*     */ 
/* 335 */     ComponentInstallUtils compUtils = new ComponentInstallUtils(this.m_installer);
/*     */ 
/* 337 */     List collatedPlatforms = new ArrayList();
/* 338 */     for (int i = this.m_installer.m_myPlatforms.length; i-- > 0; )
/*     */     {
/* 340 */       String platform = this.m_installer.m_myPlatforms[i];
/* 341 */       Properties props = this.m_installer.getInstallerTable("PlatformConfigTable", platform);
/*     */ 
/* 343 */       if (props == null)
/*     */       {
/* 345 */         collatedPlatforms.add(platform);
/*     */       }
/*     */       else
/*     */       {
/* 349 */         String listStr = props.getProperty("CompatiblePlatforms");
/* 350 */         StringUtils.appendListFromSequenceSimple(collatedPlatforms, listStr);
/*     */       }
/*     */     }
/*     */ 
/* 354 */     CollectionUtils.removeDuplicatesFromList(collatedPlatforms);
/* 355 */     Report.trace("install", "collated platform list is " + StringUtils.createStringSimple(collatedPlatforms), null);
/*     */ 
/* 357 */     HashMap missingMap = new HashMap();
/* 358 */     boolean installAll = this.m_installer.getInstallValue("InstallConfiguration", "").equals("Template");
/*     */ 
/* 360 */     installAll = this.m_installer.getInstallBool("InstallAllComponents", installAll);
/* 361 */     int workTotal = collatedPlatforms.size() * components.getNumRows();
/* 362 */     int workProgress = 0;
/* 363 */     ServiceException errors = null;
/* 364 */     HashMap missingCompExceptionMap = new HashMap();
/* 365 */     String installConfig = this.m_installer.getInstallValue("InstallConfiguration", null);
/*     */ 
/* 367 */     boolean isClone = installConfig.startsWith("Clone");
/*     */ 
/* 369 */     ComponentToolLauncher compTool = new ComponentToolLauncher();
/*     */ 
/* 371 */     compTool.m_hasPrefFile = true;
/*     */ 
/* 373 */     for (Iterator i$ = components.getSimpleParametersIterable().iterator(); i$.hasNext(); ) { params = (SimpleParameters)i$.next();
/*     */ 
/* 375 */       compName = params.get("ComponentName");
/* 376 */       platformExceptions = params.get("PlatformExceptions");
/*     */ 
/* 378 */       for (String targetPlatform : collatedPlatforms)
/*     */       {
/* 380 */         ++workProgress;
/* 381 */         if ((platformExceptions != null) && (platformExceptions.indexOf("-" + targetPlatform) >= 0))
/*     */         {
/* 384 */           Report.trace("install", "skipping component " + compName + " for platform " + targetPlatform + " due to exceptions " + platformExceptions, null);
/*     */         }
/*     */ 
/* 389 */         this.m_installer.m_installerConfig.put("target.platform", targetPlatform);
/* 390 */         String path = params.get("ComponentFile");
/* 391 */         int index1 = path.lastIndexOf("/");
/* 392 */         int index2 = path.lastIndexOf(".");
/* 393 */         String componentFile = path.substring(index1 + 1, index2);
/* 394 */         String compDir = "${IdcHomeDir}/components/" + componentFile;
/* 395 */         compDir = this.m_installer.computeDestinationEx(compDir, false);
/* 396 */         boolean found = FileUtils.checkFile(compDir, false, false) == 0;
/* 397 */         if (!found)
/*     */         {
/* 399 */           compDir = "${IdcHomeDir}/components/" + compName;
/* 400 */           compDir = this.m_installer.computeDestinationEx(compDir, false);
/* 401 */           found = FileUtils.checkFile(compDir, false, false) == 0;
/*     */         }
/* 403 */         if (isClone)
/*     */         {
/* 405 */           if (!found)
/*     */           {
/* 407 */             Report.trace(null, "skipping component " + compName + " for platform " + targetPlatform + " because it isn't available in the shiphome.", null);
/*     */           }
/*     */ 
/* 411 */           DataBinder manifest = new DataBinder();
/* 412 */           Report.trace("install", "reading manifest.hda from " + compDir, null);
/* 413 */           ResourceUtils.serializeDataBinderEx(compDir, "manifest.hda", manifest, 4);
/*     */ 
/* 415 */           ResultSet manifestSet = manifest.getResultSet("Manifest");
/*     */           try
/*     */           {
/* 418 */             String compDefinitionFile = ResultSetUtils.findValue(manifestSet, "entryType", "component", "location");
/*     */ 
/* 420 */             index1 = compDefinitionFile.lastIndexOf("/");
/* 421 */             path = compDir + "/" + compDefinitionFile.substring(index1 + 1);
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 425 */             throw new ServiceException(e);
/*     */           }
/*     */ 
/*     */         }
/* 430 */         else if (found)
/*     */         {
/* 432 */           path = FileUtils.getAbsolutePath(compDir, compName);
/*     */         }
/*     */         else
/*     */         {
/* 436 */           if (!path.startsWith("$"))
/*     */           {
/* 438 */             path = "${MediaDirectory}/" + path;
/*     */           }
/* 440 */           path = this.m_installer.computeDestinationEx(path, false);
/*     */         }
/*     */ 
/* 443 */         String conditions = params.get("Conditions");
/* 444 */         Report.trace("install", "considering component " + compName + " at " + path + " with conditions " + conditions, null);
/*     */ 
/* 446 */         boolean isSelected = compUtils.checkConditions(compName, conditions, targetPlatform);
/*     */ 
/* 448 */         boolean isNever = conditions.equals("never");
/* 449 */         if ((!isSelected) && (((!installAll) || (isNever))))
/*     */         {
/* 451 */           Report.trace("install", "skipping unselected component " + compName, null);
/*     */         }
/*     */ 
/* 455 */         if (installedComponents.get(path) != null)
/*     */         {
/* 457 */           Report.trace("install", "skipping " + path + " because it has already been installed", null);
/*     */         }
/*     */ 
/* 461 */         if (targetPlatform.equals(collatedPlatforms.get(0)))
/*     */         {
/* 463 */           missingMap.put(compName, compName);
/*     */         }
/* 465 */         installedComponents.put(path, path);
/*     */         try
/*     */         {
/* 468 */           FileUtils.validatePath(path, null, 1);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 472 */           Report.trace("install", "not installing missing component from " + path, e);
/*     */ 
/* 475 */           missingCompExceptionMap.put(compName, e);
/* 476 */         }continue;
/*     */ 
/* 481 */         missingMap.remove(compName);
/*     */         try
/*     */         {
/* 484 */           int flags = 52;
/*     */ 
/* 487 */           if (this.m_installer.getInstallValue("InstallConfiguration", "").equals("Template"))
/*     */           {
/* 489 */             flags |= 8;
/*     */           }
/* 491 */           if (!isSelected)
/*     */           {
/* 493 */             flags &= -5;
/*     */           }
/* 495 */           String msg = LocaleUtils.encodeMessage("csInstallComponent", null, compName);
/*     */ 
/* 497 */           this.m_installer.reportProgress(0, msg, workProgress, workTotal);
/*     */ 
/* 499 */           compTool.installComponent(path, flags);
/* 500 */           Report.trace("install", "installed component from " + path, null);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 504 */           if ((path == null) || (path.length() == 0))
/*     */           {
/* 506 */             Report.trace("install", "path is undefined, using ComponentName in message", null);
/*     */ 
/* 508 */             path = compName;
/*     */           }
/* 510 */           IdcMessage msg = IdcMessageFactory.lc(e, "csUnableToInstallNewComponent2", new Object[] { path });
/* 511 */           Report.trace("install", e, msg);
/* 512 */           if (errors == null)
/*     */           {
/* 514 */             errors = new ServiceException(null, msg);
/* 515 */             errors.addCause(e);
/*     */           }
/*     */           else
/*     */           {
/* 519 */             ServiceException newError = new ServiceException(null, msg);
/* 520 */             errors.addCause(newError);
/*     */           }
/* 522 */           this.m_installer.m_installLog.error(LocaleUtils.encodeMessage(msg));
/*     */         }
/*     */       } }
/*     */ 
/*     */     SimpleParameters params;
/*     */     String compName;
/*     */     String platformExceptions;
/* 526 */     for (SimpleParameters params : components.getSimpleParametersIterable())
/*     */     {
/* 528 */       String compName = params.get("ComponentName");
/* 529 */       if (missingMap.get(compName) != null)
/*     */       {
/* 531 */         String path = params.get("ComponentFile");
/* 532 */         if ((path == null) || (path.length() == 0))
/*     */         {
/* 534 */           Report.trace("install", "path is undefined, using ComponentName in message", null);
/*     */ 
/* 536 */           path = compName;
/*     */         }
/* 538 */         ServiceException e = (ServiceException)missingCompExceptionMap.get(compName);
/* 539 */         IdcMessage msg = IdcMessageFactory.lc(e, "csUnableToInstallNewComponent2", new Object[] { path });
/* 540 */         this.m_installer.m_installLog.error(LocaleUtils.encodeMessage(msg));
/* 541 */         if (errors == null)
/*     */         {
/* 543 */           errors = new ServiceException(e, "csUnableToInstallNewComponent2", new Object[] { path });
/*     */         }
/*     */         else
/*     */         {
/* 548 */           ServiceException newError = new ServiceException(e, "csUnableToInstallNewComponent2", new Object[] { path });
/*     */ 
/* 550 */           errors.addCause(newError);
/*     */         }
/*     */       }
/*     */     }
/* 554 */     if ((errors == null) || (!installAll))
/*     */       return;
/* 556 */     throw errors;
/*     */   }
/*     */ 
/*     */   public static void handlePreferenceData(DataBinder compInfo, DataResultSet prefData)
/*     */     throws DataException, ServiceException
/*     */   {
/* 563 */     FieldInfo[] infos = ResultSetUtils.createInfoList(prefData, ComponentPreferenceData.PREF_FIELD_INFO, true);
/*     */ 
/* 565 */     for (prefData.first(); prefData.isRowPresent(); prefData.next())
/*     */     {
/* 567 */       String name = prefData.getStringValue(infos[0].m_index);
/* 568 */       String defVal = prefData.getStringValue(infos[6].m_index);
/* 569 */       compInfo.putLocal(name, defVal);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 575 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83035 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.InstallComponents
 * JD-Core Version:    0.5.4
 */