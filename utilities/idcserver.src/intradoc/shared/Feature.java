/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ 
/*     */ public class Feature
/*     */   implements Cloneable
/*     */ {
/*     */   public String m_featureName;
/*     */   public String m_featureVersion;
/*     */   public String m_featureLevel;
/*     */   public String m_componentName;
/*     */ 
/*     */   public Feature()
/*     */   {
/*  45 */     this.m_featureName = "";
/*  46 */     this.m_featureVersion = "";
/*  47 */     this.m_featureLevel = "";
/*     */   }
/*     */ 
/*     */   public Feature(String featureString)
/*     */   {
/*  52 */     fromString(featureString);
/*     */   }
/*     */ 
/*     */   public void fromString(String featureString)
/*     */   {
/*  57 */     int index = featureString.indexOf(':');
/*  58 */     if (index > 0)
/*     */     {
/*  60 */       this.m_featureName = featureString.substring(0, index);
/*  61 */       featureString = featureString.substring(index + 1);
/*     */     }
/*     */     else
/*     */     {
/*  65 */       this.m_featureName = featureString;
/*  66 */       featureString = "0";
/*     */     }
/*  68 */     index = featureString.lastIndexOf('-');
/*  69 */     if (index > 0)
/*     */     {
/*  71 */       this.m_featureVersion = featureString.substring(0, index);
/*  72 */       this.m_featureLevel = featureString.substring(index + 1);
/*     */     }
/*     */     else
/*     */     {
/*  76 */       this.m_featureVersion = "";
/*  77 */       this.m_featureLevel = featureString;
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/*  84 */     IdcStringBuilder featureString = new IdcStringBuilder(this.m_featureName);
/*  85 */     if (((this.m_featureVersion != null) && (this.m_featureVersion.length() > 0)) || ((this.m_featureLevel != null) && (this.m_featureLevel.length() > 0)))
/*     */     {
/*  88 */       featureString.append(':');
/*  89 */       if ((this.m_featureVersion != null) && (this.m_featureVersion.length() > 0))
/*     */       {
/*  91 */         featureString.append(this.m_featureVersion);
/*  92 */         featureString.append('-');
/*     */       }
/*  94 */       if ((this.m_featureLevel != null) && (this.m_featureLevel.length() > 0))
/*     */       {
/*  96 */         featureString.append(this.m_featureLevel);
/*     */       }
/*     */     }
/*  99 */     return featureString.toString();
/*     */   }
/*     */ 
/*     */   public int compareLevelTo(Feature f)
/*     */     throws DataException
/*     */   {
/* 111 */     if (!this.m_featureName.equals(f.m_featureName))
/*     */     {
/* 113 */       String msg = LocaleUtils.encodeMessage("syFeatureMismatch", null, this.m_featureName, f.m_featureName);
/*     */ 
/* 115 */       throw new DataException(msg);
/*     */     }
/* 117 */     return SystemUtils.compareVersions(this.m_featureLevel, f.m_featureLevel);
/*     */   }
/*     */ 
/*     */   public Object clone()
/*     */   {
/*     */     try
/*     */     {
/* 125 */       Feature newFeature = (Feature)super.clone();
/* 126 */       return newFeature;
/*     */     }
/*     */     catch (CloneNotSupportedException e)
/*     */     {
/* 130 */       throw new AssertionError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 136 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75544 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.Feature
 * JD-Core Version:    0.5.4
 */