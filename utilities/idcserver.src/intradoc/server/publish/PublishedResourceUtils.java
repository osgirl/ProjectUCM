/*     */ package intradoc.server.publish;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.Feature;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class PublishedResourceUtils
/*     */ {
/*  87 */   protected static String STATIC_FILENAME = "$DataDir/publish/static.hda";
/*  88 */   protected static String DYNAMIC_FILENAME = "$DataDir/publish/dynamic.hda";
/*  89 */   protected static String LEGACY_DYNAMIC_FILENAME = "$DataDir/schema/lastwebfilespublished.hda";
/*     */   public static final String PUBLISHED_FEATURES_TABLE = "PublishedFeatures";
/*     */   public static final String PUBLISHED_WEB_RESOURCE_SUFFIXES_TABLE = "PublishedWebResourceSuffixes";
/*     */   public static final String PUBLISHED_BUNDLES_TABLE = "PublishedBundles";
/*     */   public static final String PUBLISHED_REQUIRED_WEB_FEATURES_SET = "PageRequiredWebFeatures";
/*     */   public static final String DEFAULT_PUBLISHED_RESOURCES_LIST_NAME = "PageRequiredPublishedWebResources";
/*     */   public static final String DEFAULT_WEB_FEATURES_LIST_NAME = "WebFeatures";
/*     */   protected static Map<String, WebFeature> m_features;
/*     */   protected static PublishedResourceContainer[] m_sortedContainers;
/*     */ 
/*     */   public static String toClassname(String classname)
/*     */   {
/* 116 */     int len = classname.length();
/* 117 */     if (len < 1)
/*     */     {
/* 119 */       return classname;
/*     */     }
/* 121 */     if (':' == classname.charAt(len - 1))
/*     */     {
/* 123 */       classname = classname.substring(0, len - 1);
/*     */     }
/*     */ 
/* 126 */     return classname.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static boolean classnameEquals(String class1, String class2)
/*     */   {
/* 138 */     int len1 = class1.length();
/* 139 */     int len2 = class2.length();
/* 140 */     if ((len1 < 1) || (len2 < 1))
/*     */     {
/* 142 */       return false;
/*     */     }
/*     */ 
/* 145 */     if (':' == class1.charAt(len1 - 1))
/*     */     {
/* 147 */       --len1;
/*     */     }
/* 149 */     if (':' == class2.charAt(len2 - 1))
/*     */     {
/* 151 */       --len2;
/*     */     }
/*     */ 
/* 154 */     if (len1 != len2)
/*     */     {
/* 156 */       return false;
/*     */     }
/* 158 */     return class1.regionMatches(true, 0, class2, 0, len1);
/*     */   }
/*     */ 
/*     */   public static boolean classnameMatches(String superclass, String subclass)
/*     */   {
/* 170 */     int superLen = superclass.length();
/* 171 */     int subLen = subclass.length();
/* 172 */     if (superLen < 1)
/*     */     {
/* 174 */       return true;
/*     */     }
/*     */ 
/* 177 */     if (':' == superclass.charAt(superLen - 1))
/*     */     {
/* 179 */       --superLen;
/*     */     }
/*     */ 
/* 182 */     if (subLen < superLen)
/*     */     {
/* 184 */       return false;
/*     */     }
/* 186 */     if (!superclass.regionMatches(true, 0, subclass, 0, superLen))
/*     */     {
/* 188 */       return false;
/*     */     }
/* 190 */     if (superLen == subLen)
/*     */     {
/* 193 */       return true;
/*     */     }
/*     */ 
/* 198 */     return ':' == subclass.charAt(superLen);
/*     */   }
/*     */ 
/*     */   public static String classnameSuper(String subclass)
/*     */   {
/* 212 */     int subLen = subclass.length();
/*     */ 
/* 214 */     if (':' == subclass.charAt(subLen - 1))
/*     */     {
/* 216 */       --subLen;
/*     */     }
/* 218 */     int firstColon = subclass.indexOf(58);
/* 219 */     if (firstColon < 1)
/*     */     {
/* 221 */       return null;
/*     */     }
/* 223 */     int lastColon = subclass.lastIndexOf(58, subLen);
/* 224 */     if (lastColon <= firstColon)
/*     */     {
/* 226 */       return null;
/*     */     }
/* 228 */     return subclass.substring(0, lastColon);
/*     */   }
/*     */ 
/*     */   public static void addRequiredFeatureDependencies(Set<WebFeature> requiredFeatures, String featureStr)
/*     */     throws DataException
/*     */   {
/* 246 */     if (m_features == null)
/*     */     {
/* 248 */       throw new DataException("!csPublishedFeaturesNotInitialized");
/*     */     }
/* 250 */     Feature requiredFeature = new Feature(featureStr);
/* 251 */     String featureName = requiredFeature.m_featureName;
/* 252 */     WebFeature existingFeature = (WebFeature)m_features.get(featureName);
/* 253 */     if (null == existingFeature)
/*     */     {
/* 255 */       String msg = LocaleUtils.encodeMessage("csPublishedFeatureMissing", null, featureName);
/* 256 */       throw new DataException(msg);
/*     */     }
/* 258 */     if (existingFeature.compareLevelTo(requiredFeature) < 0)
/*     */     {
/* 260 */       String msg = LocaleUtils.encodeMessage("csPublishedFeatureLevelMissing", null, featureName, requiredFeature.m_featureLevel, requiredFeature.m_featureLevel);
/*     */ 
/* 262 */       throw new DataException(msg);
/*     */     }
/* 264 */     if (requiredFeatures.contains(featureName))
/*     */     {
/* 267 */       return;
/*     */     }
/* 269 */     requiredFeatures.add(existingFeature);
/* 270 */     requiredFeatures.addAll(existingFeature.m_requiredSet);
/*     */   }
/*     */ 
/*     */   public static void addRequiredFeatureDependenciesFromArray(Set<WebFeature> requiredFeatures, String[] features)
/*     */     throws DataException
/*     */   {
/* 284 */     int numFeatures = features.length;
/* 285 */     for (int i = 0; i < numFeatures; ++i)
/*     */     {
/* 287 */       addRequiredFeatureDependencies(requiredFeatures, features[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean isFeatureRequired(Set<WebFeature> requiredFeatures, String featureStr)
/*     */     throws DataException
/*     */   {
/* 302 */     if (m_features == null)
/*     */     {
/* 304 */       if (SystemUtils.m_verbose)
/*     */       {
/* 306 */         Report.debug("system", "!csPublishedFeaturesNotInitialized", null);
/*     */       }
/* 308 */       return false;
/*     */     }
/* 310 */     Feature requiredFeature = new Feature(featureStr);
/* 311 */     String featureName = requiredFeature.m_featureName;
/* 312 */     WebFeature existingFeature = (WebFeature)m_features.get(featureName);
/* 313 */     if (null == existingFeature)
/*     */     {
/* 315 */       return false;
/*     */     }
/* 317 */     if (!requiredFeatures.contains(existingFeature))
/*     */     {
/* 319 */       return false;
/*     */     }
/*     */ 
/* 323 */     return existingFeature.compareLevelTo(requiredFeature) >= 0;
/*     */   }
/*     */ 
/*     */   public static boolean isFeatureAvailable(String featureStr)
/*     */     throws DataException
/*     */   {
/* 338 */     if (m_features == null)
/*     */     {
/* 340 */       if (SystemUtils.m_verbose)
/*     */       {
/* 342 */         Report.debug("system", "!csPublishedFeaturesNotInitialized", null);
/*     */       }
/* 344 */       return false;
/*     */     }
/* 346 */     Feature feature = new Feature(featureStr);
/* 347 */     String featureName = feature.m_featureName;
/* 348 */     Feature existingFeature = (Feature)m_features.get(featureName);
/*     */ 
/* 351 */     return (null != existingFeature) && (existingFeature.compareLevelTo(feature) >= 0);
/*     */   }
/*     */ 
/*     */   public static boolean isPublishedResourcesInitialized()
/*     */   {
/* 361 */     return (m_features != null) && (m_sortedContainers != null);
/*     */   }
/*     */ 
/*     */   public static List<String> createPublishedResourceListFromRequiredFeatures(Set<WebFeature> requiredFeatures)
/*     */     throws DataException
/*     */   {
/* 374 */     if (!isPublishedResourcesInitialized())
/*     */     {
/* 376 */       throw new DataException("!csPublishedFeaturesNotInitialized");
/*     */     }
/*     */ 
/* 379 */     Set requiredContainers = new HashSet();
/* 380 */     Iterator iter = requiredFeatures.iterator();
/* 381 */     while (iter.hasNext())
/*     */     {
/* 383 */       WebFeature existingFeature = (WebFeature)iter.next();
/* 384 */       requiredContainers.add(existingFeature.m_container);
/*     */     }
/*     */ 
/* 387 */     List resources = new ArrayList();
/* 388 */     int numContainers = m_sortedContainers.length;
/* 389 */     for (int i = 0; i < numContainers; ++i)
/*     */     {
/* 391 */       PublishedResourceContainer container = m_sortedContainers[i];
/* 392 */       if (!requiredContainers.contains(container)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 396 */       int numResources = container.m_sortedResourcePaths.length;
/* 397 */       for (int j = 0; j < numResources; ++j)
/*     */       {
/* 399 */         String path = container.m_sortedResourcePaths[j];
/* 400 */         resources.add(path);
/*     */       }
/*     */     }
/* 403 */     return resources;
/*     */   }
/*     */ 
/*     */   public static DataResultSet createWebFeaturesResultSet(Set<WebFeature> requiredFeatures)
/*     */     throws DataException
/*     */   {
/* 423 */     if (!isPublishedResourcesInitialized())
/*     */     {
/* 425 */       throw new DataException("!csPubishedFeaturesNotInitialized");
/*     */     }
/* 427 */     String[] fieldNames = { "feature", "version", "level", "container", "isRequired" };
/* 428 */     DataResultSet webFeatures = new DataResultSet(fieldNames);
/* 429 */     for (String featureName : m_features.keySet())
/*     */     {
/* 431 */       WebFeature feature = (WebFeature)m_features.get(featureName);
/* 432 */       String containerName = feature.m_container.m_name;
/* 433 */       boolean isRequired = requiredFeatures.contains(feature);
/* 434 */       List row = new ArrayList();
/* 435 */       row.add(featureName);
/* 436 */       row.add(feature.m_featureVersion);
/* 437 */       row.add(feature.m_featureLevel);
/* 438 */       row.add(containerName);
/* 439 */       row.add((isRequired) ? "1" : "");
/* 440 */       webFeatures.addRowWithList(row);
/*     */     }
/* 442 */     return webFeatures;
/*     */   }
/*     */ 
/*     */   protected static String[] getPublishedWebResourceSuffixes()
/*     */     throws DataException
/*     */   {
/* 456 */     ResultSet suffixesTable = SharedObjects.getTable("PublishedWebResourceSuffixes");
/* 457 */     if (null == suffixesTable)
/*     */     {
/* 459 */       String msg = LocaleUtils.encodeMessage("csResourceTableUndefined", null, "PublishedWebResourceSuffixes");
/*     */ 
/* 461 */       throw new DataException(msg);
/*     */     }
/* 463 */     String[] suffixes = ResultSetUtils.createFilteredStringArrayForColumn(suffixesTable, "suffix", null, null, true, false);
/*     */ 
/* 465 */     return suffixes;
/*     */   }
/*     */ 
/*     */   protected static void trackResource(PublishedResource resource, WebPublishState state)
/*     */   {
/* 481 */     String path = resource.m_path;
/* 482 */     PublishedResource overwrittenResource = (PublishedResource)state.m_publishedResources.get(path);
/* 483 */     if (overwrittenResource != null)
/*     */     {
/* 485 */       if (overwrittenResource.m_loadOrder > resource.m_loadOrder)
/*     */       {
/* 487 */         String msg = path + " (" + overwrittenResource.m_loadOrder + ") was overwritten by (" + resource.m_loadOrder + ")";
/* 488 */         Report.trace("publish", msg, null);
/*     */       }
/* 490 */       overwrittenResource.m_class.m_resources.remove(overwrittenResource);
/*     */     }
/*     */ 
/* 493 */     if (resource.m_class != null)
/*     */     {
/* 495 */       resource.m_class.m_resources.add(resource);
/*     */     }
/*     */ 
/* 498 */     state.m_publishedResources.put(path, resource);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 505 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75717 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.publish.PublishedResourceUtils
 * JD-Core Version:    0.5.4
 */