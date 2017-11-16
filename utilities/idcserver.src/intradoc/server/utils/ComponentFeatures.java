/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FeaturesInterface;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.ComponentLoader;
/*     */ import intradoc.shared.Feature;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ComponentFeatures
/*     */ {
/*     */   public static final int CHECK_REQUIRED = 1;
/*     */   public static final int CHECK_PROVIDED = 2;
/*     */   public static final int CHECK_ENABLED = 4;
/*     */   public static final int CHECK_INSTALLED = 8;
/*     */   public FeaturesInterface m_features;
/*     */   protected Map m_appFeatureLevels;
/*     */   protected DataResultSet m_featureErrors;
/*     */ 
/*     */   public ComponentFeatures()
/*     */   {
/*  61 */     this.m_appFeatureLevels = null;
/*     */ 
/*  66 */     this.m_featureErrors = null;
/*     */   }
/*     */ 
/*     */   public void init() throws ServiceException {
/*  70 */     String[] keys = { "componentName", "feature", "featureLevel", "currFeatureLevel", "errMsg" };
/*  71 */     this.m_featureErrors = new DataResultSet(keys);
/*  72 */     this.m_features = ((FeaturesInterface)SharedObjects.getObject("Features", "InitialCoreFeatures"));
/*     */     try
/*     */     {
/*  75 */       this.m_features = ((FeaturesInterface)this.m_features.clone());
/*     */     }
/*     */     catch (CloneNotSupportedException e)
/*     */     {
/*  79 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean checkFeatures(DataBinder componentDefinition, int option)
/*     */     throws ServiceException, DataException
/*     */   {
/*  96 */     String features = null;
/*  97 */     String compName = componentDefinition.getLocal("ComponentName");
/*     */ 
/* 100 */     if (this.m_appFeatureLevels == null)
/*     */     {
/* 102 */       if ((compName != null) && (compName.length() > 0))
/*     */       {
/* 104 */         List compList = new ArrayList();
/* 105 */         compList.add(compName);
/* 106 */         this.m_appFeatureLevels = getAppFeatureLevels(true, false, compList);
/*     */       }
/*     */       else
/*     */       {
/* 110 */         this.m_appFeatureLevels = getAppFeatureLevels(true, false, null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 115 */     if (option == 1)
/*     */     {
/* 117 */       features = componentDefinition.getLocal("requiredFeatures");
/*     */     }
/* 119 */     else if (option == 2)
/*     */     {
/* 121 */       features = componentDefinition.getLocal("featureExtensions");
/*     */     }
/*     */ 
/* 124 */     return checkFeaturesInternal(compName, features, this.m_appFeatureLevels, option);
/*     */   }
/*     */ 
/*     */   public Map getAppFeatureLevels(boolean getEnabled, boolean getDisabled, List specificComponents)
/*     */     throws ServiceException, DataException
/*     */   {
/* 147 */     HashMap componentFeatures = new HashMap();
/*     */ 
/* 149 */     ComponentListManager.init();
/* 150 */     ComponentListEditor compLE = ComponentListManager.m_editor;
/* 151 */     DataResultSet allCompSet = compLE.getComponentSet();
/*     */ 
/* 153 */     FieldInfo[] fi = ResultSetUtils.createInfoList(allCompSet, new String[] { "name", "status", "featureExtensions" }, false);
/*     */ 
/* 155 */     int nameIndex = fi[0].m_index;
/* 156 */     int statusIndex = fi[1].m_index;
/* 157 */     int extIndex = fi[2].m_index;
/*     */ 
/* 159 */     for (allCompSet.first(); allCompSet.isRowPresent(); allCompSet.next())
/*     */     {
/* 161 */       String name = allCompSet.getStringValue(nameIndex);
/* 162 */       String status = allCompSet.getStringValue(statusIndex);
/*     */ 
/* 164 */       if ((((!getEnabled) || (!status.equalsIgnoreCase("enabled")))) && (((!getDisabled) || (!status.equalsIgnoreCase("disabled")))) && (((specificComponents == null) || (!specificComponents.contains(name)))))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 169 */       String fe = allCompSet.getStringValue(extIndex);
/* 170 */       if ((fe == null) || (fe.length() <= 0))
/*     */         continue;
/* 172 */       Vector v = StringUtils.parseArrayEx(fe, ',', '^', true);
/* 173 */       for (int i = 0; i < v.size(); ++i)
/*     */       {
/* 175 */         String featureString = (String)v.elementAt(i);
/* 176 */         Feature f = new Feature(featureString);
/*     */ 
/* 178 */         if (!componentFeatures.containsKey(f.m_featureName))
/*     */         {
/* 180 */           componentFeatures.put(f.m_featureName, f.m_featureLevel);
/*     */         }
/*     */         else
/*     */         {
/* 185 */           String currentLevel = (String)componentFeatures.get(f.m_featureName);
/* 186 */           int rc = SystemUtils.compareVersions(currentLevel, f.m_featureLevel);
/* 187 */           if (rc >= 0)
/*     */             continue;
/* 189 */           componentFeatures.put(f.m_featureName, f.m_featureLevel);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 196 */     return componentFeatures;
/*     */   }
/*     */ 
/*     */   public boolean checkFeaturesInternal(String compName, String features, Map componentFeatures, int option)
/*     */     throws ServiceException, DataException
/*     */   {
/* 212 */     if ((features == null) || (features.length() == 0))
/*     */     {
/* 214 */       return true;
/*     */     }
/*     */ 
/* 225 */     Vector v = StringUtils.parseArrayEx(features, ',', '^', true);
/* 226 */     boolean hadProblems = false;
/* 227 */     for (int i = 0; i < v.size(); ++i)
/*     */     {
/* 229 */       String featureString = (String)v.elementAt(i);
/* 230 */       Feature f = new Feature(featureString);
/* 231 */       boolean goodFeatureLevels = false;
/*     */ 
/* 233 */       if (option == 1)
/*     */       {
/* 235 */         goodFeatureLevels = isRequiredFeaturePresent(f, componentFeatures);
/*     */       }
/* 237 */       else if (option == 2)
/*     */       {
/* 239 */         goodFeatureLevels = isProvidedFeatureNewestVersion(f, componentFeatures);
/*     */       }
/*     */ 
/* 242 */       if (goodFeatureLevels)
/*     */         continue;
/* 244 */       hadProblems = true;
/*     */ 
/* 246 */       String errMsg = null;
/* 247 */       String currFeatLevel = (String)this.m_features.getLevel(f.m_featureName);
/* 248 */       if (option == 1)
/*     */       {
/* 250 */         if ((currFeatLevel == null) || (currFeatLevel.length() == 0))
/*     */         {
/* 252 */           errMsg = "syFeatureNotInstalled";
/*     */         }
/*     */         else
/*     */         {
/* 256 */           errMsg = "syFeatureNotSupported";
/*     */         }
/*     */       }
/* 259 */       else if (option == 2)
/*     */       {
/* 261 */         errMsg = "syFeatureLowerVersion";
/*     */       }
/*     */ 
/* 265 */       if (currFeatLevel == null)
/*     */       {
/* 267 */         currFeatLevel = "";
/*     */       }
/* 269 */       String[] err = { compName, f.m_featureName, f.m_featureLevel, currFeatLevel, errMsg };
/*     */ 
/* 271 */       getFeatureErrors().addRow(StringUtils.convertToVector(err));
/*     */     }
/*     */ 
/* 275 */     return !hadProblems;
/*     */   }
/*     */ 
/*     */   public void verifyFeatures(ResultSet rset, Map components)
/*     */     throws ServiceException, DataException
/*     */   {
/* 287 */     FieldInfo[] info = null;
/*     */     try
/*     */     {
/* 290 */       String[] fields = { "name", "location", "status" };
/* 291 */       info = ResultSetUtils.createInfoList(rset, fields, true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 295 */       throw new ServiceException("!csComponentTableFormatError", e);
/*     */     }
/*     */ 
/* 305 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 308 */       String name = rset.getStringValue(info[0].m_index);
/* 309 */       boolean isEnabled = rset.getStringValue(info[2].m_index).equalsIgnoreCase("Enabled");
/* 310 */       if (!isEnabled) {
/*     */         continue;
/*     */       }
/*     */ 
/* 314 */       DataBinder cmptBinder = (DataBinder)components.get(name);
/* 315 */       if (null == cmptBinder)
/*     */       {
/* 317 */         IdcMessage msg = IdcMessageFactory.lc("csComponentLoadError", new Object[] { name });
/* 318 */         msg.setPrior(IdcMessageFactory.lc("csComponentMissingBinder", new Object[0]));
/* 319 */         ComponentLoader.logComponentLoadError("ComponentLoader", name, msg, null);
/*     */       }
/*     */       else {
/* 322 */         String version = cmptBinder.getLocal("version");
/*     */ 
/* 325 */         boolean noBadFeatures = checkFeatures(cmptBinder, 1);
/* 326 */         if (!noBadFeatures)
/*     */         {
/* 328 */           DataResultSet missingFeatures = getFeatureErrors();
/* 329 */           IdcMessage msg = IdcMessageFactory.lc("csComponentRequiresMissingFeatures", new Object[] { name, version, new ArrayList() });
/*     */ 
/* 331 */           reportBadFeatures(missingFeatures, msg);
/* 332 */           missingFeatures.removeAll();
/*     */         }
/*     */ 
/* 336 */         noBadFeatures = checkFeatures(cmptBinder, 2);
/* 337 */         if (noBadFeatures)
/*     */           continue;
/* 339 */         DataResultSet downgradeFeatures = getFeatureErrors();
/* 340 */         IdcMessage msg = IdcMessageFactory.lc("csComponentHasOldProvidedFeatures", new Object[] { name, version, new ArrayList() });
/*     */ 
/* 342 */         reportBadFeatures(downgradeFeatures, msg);
/* 343 */         downgradeFeatures.removeAll();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isRequiredFeaturePresent(Feature f, Map componentFeatures)
/*     */   {
/* 360 */     if (this.m_features.checkLevel(f.m_featureName, f.m_featureLevel))
/*     */     {
/* 362 */       return true;
/*     */     }
/*     */ 
/* 366 */     if (componentFeatures.containsKey(f.m_featureName))
/*     */     {
/* 368 */       String level = (String)componentFeatures.get(f.m_featureName);
/* 369 */       int rc = SystemUtils.compareVersions(level, f.m_featureLevel);
/* 370 */       if (rc >= 0)
/*     */       {
/* 372 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 376 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean isProvidedFeatureNewestVersion(Feature f, Map componentFeatures)
/*     */   {
/* 391 */     String currFeatureLevel = (String)this.m_features.getLevel(f.m_featureName);
/* 392 */     if ((currFeatureLevel != null) && (SystemUtils.compareVersions(f.m_featureLevel, currFeatureLevel) < 0))
/*     */     {
/* 395 */       return false;
/*     */     }
/*     */ 
/* 400 */     if (componentFeatures.containsKey(f.m_featureName))
/*     */     {
/* 402 */       String level = (String)componentFeatures.get(f.m_featureName);
/* 403 */       int rc = SystemUtils.compareVersions(f.m_featureLevel, level);
/* 404 */       if (rc < 0)
/*     */       {
/* 406 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 410 */     return true;
/*     */   }
/*     */ 
/*     */   public DataResultSet getFeatureErrors()
/*     */   {
/* 418 */     if (this.m_featureErrors == null)
/*     */     {
/* 420 */       String[] keys = { "componentName", "feature", "featureLevel", "currFeatureLevel", "errMsg" };
/* 421 */       this.m_featureErrors = new DataResultSet(keys);
/*     */     }
/*     */ 
/* 424 */     return this.m_featureErrors;
/*     */   }
/*     */ 
/*     */   public void reportBadFeatures(DataResultSet badFeatures, IdcMessage msg)
/*     */   {
/* 435 */     List list = null;
/* 436 */     for (Object arg : msg.m_args)
/*     */     {
/* 438 */       if (!arg instanceof List)
/*     */         continue;
/* 440 */       list = (List)arg;
/*     */     }
/*     */ 
/* 443 */     if (list == null)
/*     */     {
/* 445 */       String text = "List missing from arguments to message for reportBadFeatures";
/* 446 */       if (SystemUtils.m_isDevelopmentEnvironment)
/*     */       {
/* 448 */         throw new AssertionError(text);
/*     */       }
/* 450 */       Report.trace("componentloader", text, null);
/* 451 */       list = new ArrayList();
/*     */     }
/* 453 */     for (badFeatures.first(); badFeatures.isRowPresent(); badFeatures.next())
/*     */     {
/* 455 */       String featureName = badFeatures.getStringValueByName("feature");
/* 456 */       String featureLevel = badFeatures.getStringValueByName("featureLevel");
/* 457 */       String currFeatureLevel = badFeatures.getStringValueByName("currFeatureLevel");
/* 458 */       String errMsg = badFeatures.getStringValueByName("errMsg");
/* 459 */       IdcMessage tmp = IdcMessageFactory.lc(errMsg, new Object[] { featureName, currFeatureLevel, featureLevel });
/* 460 */       list.add(tmp);
/*     */     }
/*     */ 
/* 464 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 465 */     cxt.setCachedObject("LocalizationFlags", "text");
/* 466 */     String str = LocaleResources.localizeMessage(null, msg, cxt).toString();
/* 467 */     SystemUtils.errln(str);
/* 468 */     Report.warning("componentloader", null, msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 473 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96038 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ComponentFeatures
 * JD-Core Version:    0.5.4
 */