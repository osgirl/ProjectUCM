/*     */ package intradoc.server.project;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.util.List;
/*     */ 
/*     */ public class ProjectFileUtils
/*     */ {
/*     */   public static String m_directory;
/*     */ 
/*     */   public static void init()
/*     */   {
/*  39 */     m_directory = LegacyDirectoryLocator.getProjectDirectory();
/*     */   }
/*     */ 
/*     */   public static void updateProjectXml(ProjectInfo info, List functions, List xmlNodes)
/*     */   {
/*  44 */     String dir = m_directory + info.m_projectID.toLowerCase() + "/";
/*  45 */     int num = functions.size();
/*  46 */     for (int i = 0; i < num; ++i)
/*     */     {
/*  48 */       String function = (String)functions.get(i);
/*  49 */       if (function.equals("stagingworkflow"))
/*     */       {
/*  51 */         File wfFile = FileUtilsCfgBuilder.getCfgFile(dir + "workflow.xml", "Project", false);
/*  52 */         if (wfFile.exists())
/*     */         {
/*  54 */           info.m_tsWorkflowFile = wfFile.lastModified();
/*  55 */           info.m_workflowXml = xmlNodes;
/*     */         }
/*     */       } else {
/*  58 */         if (!function.equals("preview"))
/*     */           continue;
/*  60 */         File prvFile = FileUtilsCfgBuilder.getCfgFile(dir + "preview.xml", "Project", false);
/*  61 */         if (!prvFile.exists())
/*     */           continue;
/*  63 */         info.m_tsPreviewFile = prvFile.lastModified();
/*  64 */         info.m_previewXml = xmlNodes;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void updateProjectInfo(ProjectInfo info, boolean withLock)
/*     */     throws ServiceException
/*     */   {
/*  73 */     String dir = m_directory + info.m_projectID.toLowerCase() + "/";
/*     */ 
/*  75 */     String lockDir = null;
/*  76 */     if (withLock)
/*     */     {
/*  78 */       FileUtils.reserveDirectory(dir);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  83 */       long ts = -2L;
/*  84 */       if (info.m_hasWorkflow)
/*     */       {
/*  86 */         File wfFile = FileUtilsCfgBuilder.getCfgFile(dir + "workflow.xml", "Project", false);
/*  87 */         ts = wfFile.lastModified();
/*  88 */         if (ts != info.m_tsWorkflowFile)
/*     */         {
/*  90 */           info.m_workflowXml = readXmlFileEx(dir, "workflow.xml", lockDir, true);
/*  91 */           info.m_tsWorkflowFile = ts;
/*     */         }
/*     */       }
/*     */ 
/*  95 */       if (info.m_hasPreview)
/*     */       {
/*  97 */         File prFile = FileUtilsCfgBuilder.getCfgFile(dir + "preview.xml", "Project", false);
/*  98 */         ts = prFile.lastModified();
/*  99 */         if (ts != info.m_tsPreviewFile)
/*     */         {
/* 101 */           info.m_previewXml = readXmlFileEx(dir, "preview.xml", lockDir, true);
/* 102 */           info.m_tsPreviewFile = ts;
/*     */         }
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 108 */       if (withLock)
/*     */       {
/* 110 */         FileUtils.releaseDirectory(dir);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static List readXmlFile(String filepath) throws ServiceException
/*     */   {
/* 117 */     String dir = FileUtils.getDirectory(filepath);
/* 118 */     String filename = FileUtils.getName(filepath);
/*     */ 
/* 120 */     return readXmlFileEx(dir, filename, null, false);
/*     */   }
/*     */ 
/*     */   public static List readXmlFileEx(String dir, String filename, String lockDir, boolean isSuppressError)
/*     */     throws ServiceException
/*     */   {
/* 126 */     ResourceContainer container = new ResourceContainer();
/* 127 */     if (lockDir != null)
/*     */     {
/* 129 */       FileUtils.reserveDirectory(lockDir);
/*     */     }
/*     */     try
/*     */     {
/* 133 */       BufferedReader br = FileUtils.openDataReader(dir, filename);
/* 134 */       container.parseAndAddXmlResources(br, dir + filename);
/* 135 */       br.close();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 139 */       String errMsg = LocaleUtils.encodeMessage("csUnableToLoadXMLFile", null, filename);
/*     */ 
/* 141 */       if (isSuppressError)
/*     */       {
/* 143 */         Report.error(null, errMsg, e);
/*     */       }
/*     */       else
/*     */       {
/* 147 */         throw new ServiceException(errMsg, e);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 152 */       if (lockDir != null)
/*     */       {
/* 154 */         FileUtils.releaseDirectory(lockDir);
/*     */       }
/*     */     }
/* 157 */     return container.m_xmlNodes;
/*     */   }
/*     */ 
/*     */   public static void deleteProjectDirectory(String project)
/*     */   {
/* 162 */     String dir = m_directory + project.toLowerCase();
/*     */     try
/*     */     {
/* 165 */       File f = new File(dir);
/* 166 */       FileUtils.deleteDirectory(f, true);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 170 */       Report.warning(null, e, "csProjectUnableToDeleteDirectory", new Object[] { project });
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void deleteReportsDirectory(String project)
/*     */   {
/* 176 */     String dir = m_directory + project.toLowerCase() + "/reports";
/*     */     try
/*     */     {
/* 179 */       File f = new File(dir);
/* 180 */       FileUtils.deleteDirectory(f, true);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 184 */       Report.warning(null, e, "csProjectReportUnableToDeleteDirectory", new Object[] { project });
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void removeFunctionFiles(ProjectInfo info, boolean withLock)
/*     */   {
/*     */     try
/*     */     {
/* 193 */       if (withLock)
/*     */       {
/*     */         try
/*     */         {
/* 197 */           FileUtils.reserveDirectory(m_directory);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 201 */           Report.trace(null, null, e);
/*     */ 
/* 217 */           if (withLock)
/*     */           {
/* 219 */             FileUtils.releaseDirectory(m_directory); } return;
/*     */         }
/*     */       }
/* 205 */       String pDir = m_directory + info.m_projectID.toLowerCase();
/* 206 */       if (!info.m_hasWorkflow)
/*     */       {
/* 208 */         FileUtils.deleteFile(pDir + "/workflow.xml");
/*     */       }
/* 210 */       if (!info.m_hasPreview)
/*     */       {
/* 212 */         FileUtils.deleteFile(pDir + "/preview.xml");
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 217 */       if (withLock)
/*     */       {
/* 219 */         FileUtils.releaseDirectory(m_directory);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 226 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.project.ProjectFileUtils
 * JD-Core Version:    0.5.4
 */