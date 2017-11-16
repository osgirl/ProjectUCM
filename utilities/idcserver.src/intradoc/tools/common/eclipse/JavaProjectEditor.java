/*     */ package intradoc.tools.common.eclipse;
/*     */ 
/*     */ import intradoc.tools.common.IdcXMLDocumentWrapper;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.w3c.dom.DOMException;
/*     */ import org.w3c.dom.Element;
/*     */ import org.w3c.dom.Node;
/*     */ 
/*     */ public class JavaProjectEditor extends ProjectEditor
/*     */ {
/*     */   public IdcXMLDocumentWrapper m_classpathInfo;
/*     */ 
/*     */   public JavaProjectEditor(WorkspaceEditor workspace)
/*     */   {
/*  42 */     super(workspace);
/*  43 */     this.m_classpathInfo = new IdcXMLDocumentWrapper();
/*     */   }
/*     */ 
/*     */   public void load()
/*     */     throws IOException
/*     */   {
/*  50 */     this.m_classpathInfo.clear();
/*  51 */     super.load();
/*     */     try
/*     */     {
/*  54 */       this.m_classpathInfo.loadFromFile(new File(this.m_projectDir, ".classpath"));
/*     */     }
/*     */     catch (DOMException e)
/*     */     {
/*  58 */       throw new IOException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void save()
/*     */     throws IOException
/*     */   {
/*  65 */     super.save();
/*  66 */     File classpathFile = new File(this.m_projectDir, ".classpath.new");
/*     */     try
/*     */     {
/*  69 */       this.m_classpathInfo.saveToFile(classpathFile);
/*     */     }
/*     */     catch (DOMException e)
/*     */     {
/*  73 */       throw new IOException(e);
/*     */     }
/*  75 */     backupOverwrite(classpathFile, ".classpath");
/*     */   }
/*     */ 
/*     */   public void addClasspathentry(String kind, String path)
/*     */     throws IOException
/*     */   {
/*  88 */     IdcXMLDocumentWrapper doc = this.m_classpathInfo;
/*     */     try
/*     */     {
/*  91 */       Node classpath = doc.lookupOrAppendNode(null, new String[] { "classpath" });
/*  92 */       doc.insertTextBeforeLastTextNode(classpath, "\n\t");
/*  93 */       Element el = doc.insertElementBeforeLastTextNode(classpath, "classpathentry");
/*  94 */       el.setAttribute("kind", kind);
/*  95 */       el.setAttribute("path", path);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  99 */       throw new IOException("unable to add \"" + kind + "\" class path element: " + path, t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addLibraryPath(String path)
/*     */     throws IOException
/*     */   {
/* 111 */     addClasspathentry("lib", path);
/*     */   }
/*     */ 
/*     */   public void addSourcePath(String path)
/*     */     throws IOException
/*     */   {
/* 122 */     addClasspathentry("src", path);
/*     */   }
/*     */ 
/*     */   public List<String> getClasspathentryPaths(String kind)
/*     */     throws IOException
/*     */   {
/* 132 */     List paths = new ArrayList();
/* 133 */     IdcXMLDocumentWrapper doc = this.m_classpathInfo;
/*     */     try
/*     */     {
/* 136 */       Node classpath = doc.lookupOrAppendNode(null, new String[] { "classpath" });
/* 137 */       Node entry = classpath.getFirstChild();
/* 138 */       while (entry != null)
/*     */       {
/* 140 */         if ((entry.getNodeType() == 1) && ("classpathentry".equals(entry.getNodeName())))
/*     */         {
/* 142 */           Element el = (Element)entry;
/* 143 */           String entryKind = el.getAttribute("kind");
/* 144 */           if (kind.equals(entryKind))
/*     */           {
/* 146 */             String path = el.getAttribute("path");
/* 147 */             if (path != null)
/*     */             {
/* 149 */               paths.add(path);
/*     */             }
/*     */           }
/*     */         }
/* 153 */         entry = entry.getNextSibling();
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 158 */       throw new IOException("unable to get \"" + kind + "\" class path elements", t);
/*     */     }
/* 160 */     return paths;
/*     */   }
/*     */ 
/*     */   public List<String> getLibraryPaths()
/*     */     throws IOException
/*     */   {
/* 169 */     return getClasspathentryPaths("lib");
/*     */   }
/*     */ 
/*     */   public List<String> getSourcePaths()
/*     */     throws IOException
/*     */   {
/* 178 */     return getClasspathentryPaths("src");
/*     */   }
/*     */ 
/*     */   public void removeClasspathentry(String kind, String path)
/*     */     throws IOException
/*     */   {
/* 191 */     IdcXMLDocumentWrapper doc = this.m_classpathInfo;
/*     */     try
/*     */     {
/* 194 */       Node classpath = doc.lookupOrAppendNode(null, new String[] { "classpath" });
/* 195 */       Node entry = classpath.getFirstChild();
/* 196 */       while (entry != null)
/*     */       {
/* 198 */         if ((entry.getNodeType() == 1) && ("classpathentry".equals(entry.getNodeName())))
/*     */         {
/* 200 */           Element el = (Element)entry;
/* 201 */           String entryKind = el.getAttribute("kind");
/* 202 */           if (kind.equals(entryKind))
/*     */           {
/* 204 */             String nodePath = el.getAttribute("path");
/* 205 */             if (path.equals(nodePath))
/*     */             {
/* 207 */               entry = entry.getNextSibling();
/* 208 */               doc.removeNodeAndPriorTextNode(el);
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 213 */         entry = entry.getNextSibling();
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 218 */       throw new IOException("unable to remove \"" + kind + "\" class path element \"" + path + "\"", t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeSourcePath(String path)
/*     */     throws IOException
/*     */   {
/* 231 */     removeClasspathentry("src", path);
/*     */   }
/*     */ 
/*     */   public void saveJavaLaunchConfiguration(String launchName, String mainClassname, Map<String, Object> attributes)
/*     */     throws IOException
/*     */   {
/* 248 */     File docFile = this.m_workspace.computeLaunchConfigurationFile(launchName);
/* 249 */     if (docFile == null)
/*     */     {
/* 251 */       throw new IOException("workspace root directory not initialized");
/*     */     }
/* 253 */     File launchDir = docFile.getParentFile();
/* 254 */     launchDir.mkdir();
/* 255 */     if (!launchDir.isDirectory())
/*     */     {
/* 257 */       throw new IOException(launchDir.getPath() + " does not exist or is not a directory");
/* 259 */     }String projectName = getProjectName();
/*     */ 
/* 261 */     IdcXMLDocumentWrapper doc = new IdcXMLDocumentWrapper();
/*     */     Node launchConfiguration;
/*     */     Node node;
/*     */     try {
/* 264 */       doc.createEmptyDocument();
/* 265 */       launchConfiguration = doc.insertElementBeforeLastTextNode(doc.m_document, "launchConfiguration");
/* 266 */       doc.insertTextBeforeLastTextNode(launchConfiguration, "\n");
/* 267 */       doc.setNodeAttributeValue(launchConfiguration, "type", "org.eclipse.jdt.launching.localJavaApplication");
/* 268 */       node = doc.insertElementBeforeLastTextNode(launchConfiguration, "stringAttribute");
/* 269 */       doc.setNodeAttributeValue(node, "key", "org.eclipse.jdt.launching.PROJECT_ATTR");
/* 270 */       doc.setNodeAttributeValue(node, "value", projectName);
/* 271 */       node = doc.insertElementBeforeLastTextNode(launchConfiguration, "stringAttribute");
/* 272 */       doc.setNodeAttributeValue(node, "key", "org.eclipse.jdt.launching.MAIN_TYPE");
/* 273 */       doc.setNodeAttributeValue(node, "value", mainClassname);
/*     */ 
/* 275 */       for (String key : attributes.keySet())
/*     */       {
/* 277 */         Object value = attributes.get(key);
/* 278 */         if (value instanceof Boolean)
/*     */         {
/* 280 */           node = doc.insertElementBeforeLastTextNode(launchConfiguration, "booleanAttribute");
/*     */         }
/* 282 */         else if (value instanceof String)
/*     */         {
/* 284 */           node = doc.insertElementBeforeLastTextNode(launchConfiguration, "stringAttribute");
/*     */         }
/*     */         else
/*     */         {
/*     */           Map map;
/* 286 */           if (value instanceof Map)
/*     */           {
/* 288 */             map = (Map)value;
/* 289 */             node = doc.insertElementBeforeLastTextNode(launchConfiguration, "mapAttribute");
/* 290 */             doc.setNodeAttributeValue(node, "key", key);
/* 291 */             for (String mapkey : map.keySet())
/*     */             {
/* 293 */               String mapvalue = (String)map.get(mapkey);
/* 294 */               Node entry = doc.insertElementBeforeLastTextNode(node, "mapEntry");
/* 295 */               doc.setNodeAttributeValue(entry, "key", mapkey);
/* 296 */               doc.setNodeAttributeValue(entry, "value", mapvalue);
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 302 */           String msg = "unknown type " + value.getClass().getName() + " for attribute \"" + key + "\"";
/* 303 */           throw new IOException(msg);
/*     */         }
/* 305 */         doc.setNodeAttributeValue(node, "key", key);
/* 306 */         doc.setNodeAttributeValue(node, "value", value.toString());
/*     */       }
/*     */     }
/*     */     catch (DOMException dome)
/*     */     {
/* 311 */       throw new IOException("unable to create XML document", dome);
/*     */     }
/* 313 */     doc.saveToFile(docFile);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 320 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97481 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.eclipse.JavaProjectEditor
 * JD-Core Version:    0.5.4
 */