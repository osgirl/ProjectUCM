/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.FeaturesInterface;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Features
/*     */ {
/*     */ 
/*     */   @Deprecated
/*     */   public static DataResultSet m_featureList;
/*     */ 
/*     */   @Deprecated
/*     */   public static HashMap m_featureLevelMap;
/*     */ 
/*     */   @Deprecated
/*     */   public static HashMap m_featureComponentMap;
/*     */ 
/*     */   @Deprecated
/*     */   public static HashMap m_componentFeatureMap;
/*     */   public static FeaturesInterface m_features;
/*     */ 
/*     */   public static synchronized void init()
/*     */   {
/*  53 */     Exception[] ex = new Exception[1];
/*  54 */     m_features = newFeaturesObject(null, ex);
/*  55 */     if (ex[0] != null)
/*     */     {
/*  57 */       Report.trace("system", ex[0], null);
/*     */     }
/*  59 */     if (m_features instanceof BasicFeatureImplementor)
/*     */     {
/*  61 */       BasicFeatureImplementor features = (BasicFeatureImplementor)m_features;
/*  62 */       m_featureList = features.m_featureList;
/*  63 */       SharedObjects.putTable("Features", m_featureList);
/*     */       try
/*     */       {
/*  66 */         m_featureLevelMap = (HashMap)features.m_featureLevelMap;
/*  67 */         m_featureComponentMap = (HashMap)features.m_featureComponentMap;
/*  68 */         m_componentFeatureMap = (HashMap)features.m_componentFeatureMap;
/*     */       }
/*     */       catch (ClassCastException e)
/*     */       {
/*  72 */         Report.trace(null, "Map objects in " + m_features.getClass().getName() + "are not HashMaps, deprecated pointers will be null.", e);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/*  78 */       Report.trace(null, "m_features is not implemented by BasicFeatureInterface, deprecated pointers will be null.", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static FeaturesInterface newFeaturesObject(String className, Exception[] ex)
/*     */   {
/*  91 */     if (className == null)
/*     */     {
/*  93 */       className = SharedObjects.getEnvironmentValue("FeatureImplementorClass");
/*     */     }
/*  95 */     if (className == null)
/*     */     {
/*  97 */       className = "intradoc.shared.BasicFeatureImplementor";
/*     */     }
/*  99 */     FeaturesInterface f = null;
/*     */     try
/*     */     {
/* 102 */       f = (FeaturesInterface)ClassHelperUtils.createClass(className).newInstance();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 106 */       if ((ex != null) && (ex.length > 0))
/*     */       {
/* 108 */         ex[0] = e;
/*     */       }
/*     */       else
/*     */       {
/* 112 */         Report.trace("system", ex[0], null);
/*     */       }
/*     */     }
/* 115 */     if (f == null)
/*     */     {
/* 117 */       f = new BasicFeatureImplementor();
/*     */     }
/* 119 */     f.init();
/* 120 */     return f;
/*     */   }
/*     */ 
/*     */   public static void registerFeaturesFromResultSet(FeaturesInterface f, DataResultSet featureSet, String componentName)
/*     */   {
/* 126 */     if (featureSet == null)
/*     */       return;
/* 128 */     for (featureSet.first(); featureSet.isRowPresent(); featureSet.next())
/*     */     {
/* 130 */       Properties props = featureSet.getCurrentRowProps();
/* 131 */       Feature feature = new Feature();
/* 132 */       feature.m_featureName = props.getProperty("idcFeatureName");
/* 133 */       feature.m_featureVersion = props.getProperty("idcFeatureVersion");
/* 134 */       feature.m_featureLevel = props.getProperty("idcFeatureLevel");
/* 135 */       String featureString = feature.toString();
/* 136 */       f.registerFeature(featureString, componentName);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void registerFromResultSet(DataResultSet features, String componentName)
/*     */   {
/* 144 */     registerFeaturesFromResultSet(m_features, features, componentName);
/*     */   }
/*     */ 
/*     */   public static Object getLevel(String featureName)
/*     */   {
/* 156 */     Object level = m_features.getLevel(featureName);
/* 157 */     return level;
/*     */   }
/*     */ 
/*     */   public static boolean checkLevel(String featureName, String level)
/*     */   {
/* 164 */     if (m_features == null)
/*     */     {
/* 166 */       Report.trace("system", "Check on feature " + featureName + " at level " + level + " on uninitialized feature system", null);
/*     */ 
/* 168 */       return false;
/*     */     }
/* 170 */     boolean rc = m_features.checkLevel(featureName, level);
/* 171 */     return rc;
/*     */   }
/*     */ 
/*     */   public static List getFeatureComponents(String featureName)
/*     */   {
/* 176 */     return m_features.getFeatureComponents(featureName);
/*     */   }
/*     */ 
/*     */   public static List getComponentFeatures(String componentName)
/*     */   {
/* 181 */     return m_features.getComponentFeatures(componentName);
/*     */   }
/*     */ 
/*     */   public static void require(String featureName, String level, String msg)
/*     */     throws ServiceException
/*     */   {
/* 189 */     m_features.require(featureName, level, msg);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void refuse(String featureName, String level, String msg)
/*     */     throws ServiceException
/*     */   {
/* 200 */     IdcMessage idcmsg = IdcMessageFactory.lc();
/* 201 */     idcmsg.m_msgEncoded = msg;
/*     */ 
/* 203 */     m_features.refuse(featureName, level, idcmsg);
/*     */   }
/*     */ 
/*     */   public static void parseAndAppendFeatures(String str, List features)
/*     */   {
/* 208 */     Vector featureList = StringUtils.parseArray(str, ',', '^');
/* 209 */     for (int i = 0; i < featureList.size(); ++i)
/*     */     {
/* 211 */       String featureString = (String)featureList.elementAt(i);
/* 212 */       Feature feature = parseFeatureFromString(featureString);
/* 213 */       features.add(feature);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void registerFeatures(String list, String componentName)
/*     */   {
/* 219 */     Vector featureList = StringUtils.parseArray(list, ',', '^');
/* 220 */     for (int i = 0; i < featureList.size(); ++i)
/*     */     {
/* 222 */       String feature = (String)featureList.elementAt(i);
/* 223 */       m_features.registerFeature(feature, componentName);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void registerFeature(String featureString, String componentInfo)
/*     */   {
/* 230 */     m_features.registerFeature(featureString, componentInfo);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static Feature parseFeatureFromString(String featureString)
/*     */   {
/* 242 */     return new Feature(featureString);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 247 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80462 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.Features
 * JD-Core Version:    0.5.4
 */