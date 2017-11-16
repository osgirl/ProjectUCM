/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.Feature;
/*     */ import intradoc.shared.Features;
/*     */ import intradoc.zip.ZipFunctions;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ComponentAnalyzerData
/*     */ {
/*     */   public static final int NOT_INSTALLED = 0;
/*     */   public static final int INSTALLED = 1;
/*     */   public static final int WANT_INSTALLED = 2;
/*     */   public String m_name;
/*     */   public String m_path;
/*     */   public DataBinder m_binder;
/*     */   public List m_requiredFeatures;
/*     */   public List m_providedFeatures;
/*     */   public int m_state;
/*     */ 
/*     */   public ComponentAnalyzerData()
/*     */   {
/*  37 */     this.m_name = null;
/*  38 */     this.m_path = null;
/*  39 */     this.m_binder = null;
/*  40 */     this.m_requiredFeatures = null;
/*  41 */     this.m_providedFeatures = null;
/*  42 */     this.m_state = 0;
/*     */   }
/*     */ 
/*     */   public void init(String path) throws DataException, ServiceException {
/*  46 */     this.m_path = path;
/*  47 */     if (path.endsWith(".zip"))
/*     */     {
/*  49 */       loadComponentZip();
/*     */     }
/*  51 */     else if (path.endsWith(".hda"))
/*     */     {
/*  53 */       loadComponentHda();
/*     */     }
/*     */     else
/*     */     {
/*  57 */       String msg = LocaleUtils.encodeMessage("syUnknownExtension3", null, path, "zip,hda");
/*     */ 
/*  59 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/*  62 */     this.m_name = this.m_binder.getLocal("ComponentName");
/*     */ 
/*  64 */     String requiredFeatures = this.m_binder.getLocal("requiredFeatures");
/*  65 */     setRequiredFeatureList(requiredFeatures);
/*  66 */     String providedFeatures = this.m_binder.getLocal("featureExtensions");
/*  67 */     setProvidedFeatureList(providedFeatures);
/*     */ 
/*  69 */     Report.trace("install", "the component " + this.m_name + " requires " + requiredFeatures + " and provides " + providedFeatures, null);
/*     */   }
/*     */ 
/*     */   public void init(String name, List requiredFeatures, List providedFeatures)
/*     */   {
/*  76 */     this.m_name = name;
/*  77 */     this.m_requiredFeatures = requiredFeatures;
/*  78 */     this.m_providedFeatures = providedFeatures;
/*     */   }
/*     */ 
/*     */   public boolean isInstalled()
/*     */   {
/*  83 */     return this.m_state == 1;
/*     */   }
/*     */ 
/*     */   public void setRequiredFeatureList(String featureList)
/*     */   {
/*  88 */     List l = new ArrayList();
/*  89 */     Features.parseAndAppendFeatures(featureList, l);
/*  90 */     this.m_requiredFeatures = l;
/*     */   }
/*     */ 
/*     */   public void setProvidedFeatureList(String featureList)
/*     */   {
/*  95 */     List l = new ArrayList();
/*  96 */     Features.parseAndAppendFeatures(featureList, l);
/*  97 */     for (int i = 0; i < l.size(); ++i)
/*     */     {
/*  99 */       Feature f = (Feature)l.get(i);
/* 100 */       f.m_componentName = this.m_name;
/*     */     }
/* 102 */     this.m_providedFeatures = l;
/*     */   }
/*     */ 
/*     */   public void loadComponentZip() throws DataException
/*     */   {
/* 107 */     String manifestFileName = "manifest.hda";
/* 108 */     String manifestResultSetName = "Manifest";
/* 109 */     DataBinder manifest = ZipFunctions.extractFileAsDataBinder(this.m_path, manifestFileName);
/*     */ 
/* 112 */     DataResultSet content = (DataResultSet)manifest.getResultSet(manifestResultSetName);
/*     */ 
/* 114 */     if (content == null)
/*     */     {
/* 116 */       String msg = LocaleUtils.encodeMessage("syResultSetMissing", null, manifestResultSetName);
/*     */ 
/* 118 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 121 */     String location = null;
/* 122 */     for (content.first(); content.isRowPresent(); content.next())
/*     */     {
/* 124 */       Properties props = content.getCurrentRowProps();
/* 125 */       String entryType = props.getProperty("entryType");
/* 126 */       location = "component/" + props.getProperty("location");
/* 127 */       if (entryType.equals("component")) {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 133 */     if (location == null)
/*     */     {
/* 135 */       String msg = LocaleUtils.encodeMessage("csCompWizCompInfoNotFound", null);
/*     */ 
/* 137 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 140 */     this.m_binder = ZipFunctions.extractFileAsDataBinder(this.m_path, location);
/*     */   }
/*     */ 
/*     */   public void loadComponentHda()
/*     */     throws ServiceException
/*     */   {
/* 146 */     DataBinder binder = new DataBinder();
/* 147 */     String dir = FileUtils.getDirectory(this.m_path);
/* 148 */     String file = FileUtils.getName(this.m_path);
/* 149 */     ResourceUtils.serializeDataBinder(dir, file, binder, false, true);
/* 150 */     this.m_binder = binder;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 155 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.ComponentAnalyzerData
 * JD-Core Version:    0.5.4
 */