/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.Feature;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ComponentAnalyzer
/*     */ {
/*     */   public String m_userMessage;
/*     */   public Map m_features;
/*     */   public Map m_components;
/*     */   public Map m_missingFeatures;
/*     */   public ArrayList m_autoInstallList;
/*     */   public Map m_autoDepends;
/*     */ 
/*     */   public ComponentAnalyzer()
/*     */   {
/*  30 */     this.m_userMessage = null;
/*     */ 
/*  32 */     this.m_features = new HashMap();
/*  33 */     this.m_components = new HashMap();
/*     */ 
/*  35 */     this.m_missingFeatures = new HashMap();
/*     */ 
/*  37 */     this.m_autoInstallList = new ArrayList();
/*  38 */     this.m_autoDepends = new HashMap();
/*     */   }
/*     */ 
/*     */   public void addComponentInfo(ComponentAnalyzerData c) throws DataException, ServiceException
/*     */   {
/*  43 */     appendComponentFeatures(c);
/*  44 */     this.m_components.put(c.m_name, c);
/*     */   }
/*     */ 
/*     */   public void addComponentInfo(String path, int state)
/*     */     throws DataException, ServiceException
/*     */   {
/*  50 */     ComponentAnalyzerData c = new ComponentAnalyzerData();
/*  51 */     c.init(path);
/*  52 */     c.m_state = state;
/*  53 */     addComponentInfo(c);
/*     */   }
/*     */ 
/*     */   public void resolveComponentDependencies()
/*     */   {
/*  58 */     Iterator it = this.m_components.values().iterator();
/*  59 */     while (it.hasNext())
/*     */     {
/*  61 */       ComponentAnalyzerData c = (ComponentAnalyzerData)it.next();
/*     */ 
/*  63 */       handleComponent(c);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void handleComponent(ComponentAnalyzerData c)
/*     */   {
/*  69 */     switch (c.m_state)
/*     */     {
/*     */     case 0:
/*  72 */       handleUninstalledComponent(c);
/*  73 */       break;
/*     */     case 1:
/*  75 */       handleInstalledComponent(c);
/*  76 */       break;
/*     */     case 2:
/*  78 */       handleNewComponent(c);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void handleUninstalledComponent(ComponentAnalyzerData c)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void handleInstalledComponent(ComponentAnalyzerData c)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void handleNewComponent(ComponentAnalyzerData c)
/*     */   {
/*  95 */     ArrayList l = new ArrayList();
/*  96 */     for (int i = 0; i < c.m_requiredFeatures.size(); ++i)
/*     */     {
/*  98 */       Feature f = (Feature)c.m_requiredFeatures.get(i);
/*  99 */       Report.trace("install", "checking dependencies for " + c.m_name, null);
/* 100 */       ComponentAnalyzerData extraNewComponent = checkForFeature(f);
/* 101 */       if (extraNewComponent == null)
/*     */         continue;
/* 103 */       if (this.m_autoInstallList.indexOf(extraNewComponent) == -1)
/*     */       {
/* 105 */         this.m_autoInstallList.add(extraNewComponent);
/*     */       }
/* 107 */       if (l.indexOf(extraNewComponent.m_name) != -1)
/*     */         continue;
/* 109 */       l.add(extraNewComponent.m_name);
/*     */     }
/*     */ 
/* 113 */     if (l.size() <= 0)
/*     */       return;
/* 115 */     this.m_autoDepends.put(c.m_name, l);
/*     */   }
/*     */ 
/*     */   public ComponentAnalyzerData checkForFeature(Feature neededFeature)
/*     */   {
/* 121 */     ArrayList featureProviders = (ArrayList)this.m_features.get(neededFeature.m_featureName);
/*     */ 
/* 123 */     if (featureProviders != null)
/*     */     {
/* 125 */       for (int i = 0; i < featureProviders.size(); ++i)
/*     */       {
/* 127 */         Feature f = (Feature)featureProviders.get(i);
/* 128 */         if (SystemUtils.compareVersions(neededFeature.m_featureVersion, f.m_featureVersion) > 0)
/*     */         {
/* 131 */           Report.trace("install", "not using feature " + f.m_featureName + " because of versions " + neededFeature.m_featureVersion + " vs " + f.m_featureVersion, null);
/*     */         }
/*     */         else
/*     */         {
/* 138 */           ComponentAnalyzerData c = (ComponentAnalyzerData)this.m_components.get(f.m_componentName);
/*     */ 
/* 140 */           Report.trace("install", "feature " + f.m_featureName + " provided by " + c.m_name, null);
/*     */ 
/* 142 */           if (c.m_state == 0)
/*     */           {
/* 144 */             Report.trace("install", "feature " + f.m_featureName + " provided by unselected component " + c.m_name, null);
/*     */ 
/* 146 */             if (this.m_userMessage == null)
/*     */             {
/* 148 */               this.m_userMessage = LocaleUtils.encodeMessage("csInstallerComponentDependenciesNotSatisfied", null);
/*     */             }
/*     */ 
/* 151 */             return c;
/*     */           }
/*     */ 
/* 154 */           return null;
/*     */         }
/*     */       }
/*     */     }
/* 157 */     Report.trace("install", "no way to provide feature " + neededFeature.m_featureName, null);
/* 158 */     this.m_missingFeatures.put(neededFeature, neededFeature);
/*     */ 
/* 160 */     return null;
/*     */   }
/*     */ 
/*     */   public void appendComponentFeatures(ComponentAnalyzerData c)
/*     */   {
/* 165 */     for (int i = 0; i < c.m_providedFeatures.size(); ++i)
/*     */     {
/* 167 */       Feature f = (Feature)c.m_providedFeatures.get(i);
/* 168 */       ArrayList featureProviders = (ArrayList)this.m_features.get(f.m_featureName);
/* 169 */       if (featureProviders == null)
/*     */       {
/* 171 */         featureProviders = new ArrayList();
/* 172 */         this.m_features.put(f.m_featureName, featureProviders);
/*     */       }
/* 174 */       featureProviders.add(f);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 180 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.ComponentAnalyzer
 * JD-Core Version:    0.5.4
 */