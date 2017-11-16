/*     */ package intradoc.tools.common.eclipse;
/*     */ 
/*     */ import intradoc.tools.common.IdcXMLDocumentWrapper;
/*     */ import intradoc.tools.utils.TextUtils;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Calendar;
/*     */ import java.util.GregorianCalendar;
/*     */ import org.w3c.dom.DOMException;
/*     */ import org.w3c.dom.Node;
/*     */ 
/*     */ public class ProjectEditor
/*     */ {
/*     */   public final WorkspaceEditor m_workspace;
/*     */   public File m_projectDir;
/*     */   public IdcXMLDocumentWrapper m_projectInfo;
/*     */ 
/*     */   public ProjectEditor(WorkspaceEditor workspace)
/*     */   {
/*  47 */     this.m_workspace = workspace;
/*  48 */     this.m_projectInfo = new IdcXMLDocumentWrapper();
/*     */   }
/*     */ 
/*     */   public void initProjectByName(String name)
/*     */     throws IOException
/*     */   {
/*  59 */     File workspaceDir = this.m_workspace.m_workspaceDir;
/*  60 */     if (workspaceDir == null)
/*     */     {
/*  62 */       throw new IOException("workspace directory not initialized");
/*     */     }
/*  64 */     File projectDir = new File(workspaceDir, name);
/*  65 */     initProjectDir(projectDir);
/*     */   }
/*     */ 
/*     */   public void initProjectDir(File dir)
/*     */     throws IOException
/*     */   {
/*  76 */     if (!dir.isDirectory())
/*     */     {
/*  78 */       if (dir.exists())
/*     */       {
/*  80 */         throw new IOException("not a directory: " + dir.getPath());
/*     */       }
/*  82 */       throw new IOException("directory does not exist: " + dir.getPath());
/*     */     }
/*  84 */     this.m_projectDir = dir;
/*     */   }
/*     */ 
/*     */   protected File getAndCheckProjectDir()
/*     */     throws IOException
/*     */   {
/*  90 */     File projectDir = this.m_projectDir;
/*  91 */     if (projectDir == null)
/*     */     {
/*  93 */       throw new IOException("project directory not initialized");
/*     */     }
/*  95 */     return projectDir;
/*     */   }
/*     */ 
/*     */   public void load()
/*     */     throws IOException
/*     */   {
/* 105 */     this.m_projectInfo.clear();
/* 106 */     File projectDir = getAndCheckProjectDir();
/*     */     try
/*     */     {
/* 109 */       this.m_projectInfo.loadFromFile(new File(projectDir, ".project"));
/*     */     }
/*     */     catch (DOMException e)
/*     */     {
/* 113 */       throw new IOException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void save()
/*     */     throws IOException
/*     */   {
/* 125 */     File projectDir = getAndCheckProjectDir();
/* 126 */     File projectFile = new File(projectDir, ".project.new");
/*     */     try
/*     */     {
/* 129 */       this.m_projectInfo.saveToFile(projectFile);
/*     */     }
/*     */     catch (DOMException e)
/*     */     {
/* 133 */       throw new IOException(e);
/*     */     }
/* 135 */     backupOverwrite(projectFile, ".project");
/*     */   }
/*     */ 
/*     */   protected void backupOverwrite(File newFile, String oldFilename) throws IOException
/*     */   {
/* 140 */     File projectDir = this.m_projectDir;
/* 141 */     File oldFile = new File(projectDir, oldFilename);
/* 142 */     long oldFileModified = oldFile.lastModified();
/* 143 */     if (oldFileModified != 0L)
/*     */     {
/* 145 */       Calendar cal = new GregorianCalendar();
/* 146 */       cal.setTimeInMillis(oldFileModified);
/* 147 */       StringBuilder filename = new StringBuilder(oldFilename);
/* 148 */       filename.append('-');
/* 149 */       TextUtils.appendNumericDateTimeTo(filename, cal, "", "-", "");
/* 150 */       File backupFile = new File(projectDir, filename.toString());
/* 151 */       if (!oldFile.renameTo(backupFile))
/*     */       {
/* 153 */         throw new IOException("unable to rename " + oldFile + " to " + newFile);
/*     */       }
/*     */     }
/* 156 */     if (newFile.renameTo(oldFile))
/*     */       return;
/* 158 */     throw new IOException("unable to rename " + newFile + " to " + oldFile);
/*     */   }
/*     */ 
/*     */   public String getProjectName()
/*     */   {
/*     */     try
/*     */     {
/* 172 */       Node node = this.m_projectInfo.lookupNode(null, new String[] { "projectDescription", "name" });
/* 173 */       return this.m_projectInfo.getNodeText(node);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 177 */       GenericTracingCallback trace = this.m_workspace.m_trace;
/* 178 */       if (trace != null)
/*     */       {
/* 180 */         trace.report(4, new Object[] { t });
/*     */       }
/*     */     }
/* 182 */     return null;
/*     */   }
/*     */ 
/*     */   public void addOrReplaceSymlink(String dirInProject, String pathToRealDir)
/*     */     throws IOException
/*     */   {
/* 198 */     File projectDir = getAndCheckProjectDir();
/* 199 */     File file = new File(projectDir, dirInProject);
/* 200 */     if (file.exists())
/*     */     {
/* 202 */       throw new IOException("Cannot create project directory symlink, " + dirInProject + ": already exists\n");
/*     */     }
/* 204 */     IdcXMLDocumentWrapper project = this.m_projectInfo;
/*     */     try
/*     */     {
/* 207 */       Node linkedResources = project.lookupOrAppendNode(null, new String[] { "projectDescription", "linkedResources" });
/* 208 */       Node link = linkedResources.getFirstChild();
/* 209 */       while (link != null)
/*     */       {
/* 211 */         if ((link.getNodeType() == 1) && ("link".equals(link.getNodeName())))
/*     */         {
/* 213 */           Node name = project.lookupNode(link, new String[] { "name" });
/* 214 */           if ((name != null) && (project.getNodeText(name).equals(dirInProject)))
/*     */           {
/* 216 */             Node type = project.lookupNode(link, new String[] { "type" });
/* 217 */             if ((type != null) && (project.getNodeText(type).equals("2")))
/*     */               break;
/* 219 */             throw new DOMException(12, "invalid node type");
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 224 */         link = link.getNextSibling();
/*     */       }
/* 226 */       if (link == null)
/*     */       {
/* 228 */         link = project.lookupOrAppendNode(linkedResources, new String[] { "link" });
/* 229 */         Node node = project.lookupOrAppendNode(link, new String[] { "name" });
/* 230 */         project.replaceNodeWithText(node, dirInProject);
/* 231 */         node = project.lookupOrAppendNode(link, new String[] { "type" });
/* 232 */         project.replaceNodeWithText(node, "2");
/*     */       }
/* 234 */       Node location = project.lookupOrAppendNode(link, new String[] { "location" });
/* 235 */       project.replaceNodeWithText(location, pathToRealDir);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 239 */       throw new IOException("unable to update linked resources", t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 247 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98160 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.eclipse.ProjectEditor
 * JD-Core Version:    0.5.4
 */