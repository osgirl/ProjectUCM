/*     */ package intradoc.server.project;
/*     */ 
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ProjectInfo
/*     */ {
/*     */   public String m_projectID;
/*     */   public Properties m_properties;
/*     */   public boolean m_hasWorkflow;
/*     */   public boolean m_hasPreview;
/*     */   public long m_tsWorkflowFile;
/*     */   public long m_tsPreviewFile;
/*     */   public List m_workflowXml;
/*     */   public List m_previewXml;
/*     */ 
/*     */   public ProjectInfo()
/*     */   {
/*  30 */     this.m_projectID = null;
/*  31 */     this.m_properties = null;
/*     */ 
/*  33 */     this.m_hasWorkflow = false;
/*  34 */     this.m_hasPreview = false;
/*     */ 
/*  39 */     this.m_tsWorkflowFile = -2L;
/*  40 */     this.m_tsPreviewFile = -2L;
/*     */ 
/*  42 */     this.m_workflowXml = null;
/*  43 */     this.m_previewXml = null;
/*     */   }
/*     */ 
/*     */   public void init(Properties props)
/*     */   {
/*  50 */     if (props == null)
/*     */     {
/*  52 */       return;
/*     */     }
/*     */ 
/*  55 */     this.m_properties = props;
/*  56 */     this.m_projectID = props.getProperty("dProjectID");
/*     */ 
/*  58 */     updateFunctionFlags();
/*     */   }
/*     */ 
/*     */   public void copyShallow(ProjectInfo info)
/*     */   {
/*  63 */     Properties props = (Properties)info.m_properties.clone();
/*  64 */     init(props);
/*     */ 
/*  66 */     this.m_tsWorkflowFile = info.m_tsWorkflowFile;
/*  67 */     this.m_tsPreviewFile = info.m_tsPreviewFile;
/*     */ 
/*  69 */     this.m_workflowXml = info.m_workflowXml;
/*  70 */     this.m_previewXml = info.m_previewXml;
/*     */   }
/*     */ 
/*     */   public void updateFunctionFlags()
/*     */   {
/*  75 */     this.m_hasWorkflow = false;
/*  76 */     this.m_hasPreview = false;
/*     */ 
/*  78 */     String str = this.m_properties.getProperty("dPrjFunctions");
/*  79 */     Vector functions = StringUtils.parseArray(str, ',', ',');
/*  80 */     int num = functions.size();
/*  81 */     for (int i = 0; i < num; ++i)
/*     */     {
/*  83 */       String function = (String)functions.elementAt(i);
/*  84 */       if (function.equalsIgnoreCase("stagingworkflow"))
/*     */       {
/*  86 */         this.m_hasWorkflow = true;
/*     */       } else {
/*  88 */         if (!function.equalsIgnoreCase("preview"))
/*     */           continue;
/*  90 */         this.m_hasPreview = true;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void put(String name, String value)
/*     */   {
/*  97 */     this.m_properties.put(name, value);
/*     */   }
/*     */ 
/*     */   public String get(String name)
/*     */   {
/* 102 */     return this.m_properties.getProperty(name);
/*     */   }
/*     */ 
/*     */   public void remove(String name)
/*     */   {
/* 107 */     this.m_properties.remove(name);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 112 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.project.ProjectInfo
 * JD-Core Version:    0.5.4
 */