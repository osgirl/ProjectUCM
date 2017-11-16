/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import java.io.File;
/*     */ import java.util.Collection;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import javax.swing.filechooser.FileFilter;
/*     */ 
/*     */ public class FileFilterByExtensions extends FileFilter
/*     */   implements Collection
/*     */ {
/*     */   protected String m_prelocalizedDescriptionKey;
/*     */   protected HashSet m_extensions;
/*     */ 
/*     */   public FileFilterByExtensions(String descriptionKey)
/*     */   {
/*  40 */     init(descriptionKey);
/*     */   }
/*     */ 
/*     */   public FileFilterByExtensions(String descriptionKey, String extension)
/*     */   {
/*  45 */     init(descriptionKey);
/*  46 */     add(extension);
/*     */   }
/*     */ 
/*     */   public FileFilterByExtensions(String descriptionKey, Collection extensions)
/*     */   {
/*  51 */     init(descriptionKey);
/*  52 */     addAll(extensions);
/*     */   }
/*     */ 
/*     */   public FileFilterByExtensions(String descriptionKey, String[] extensions)
/*     */   {
/*  57 */     init(descriptionKey);
/*  58 */     addAll(extensions);
/*     */   }
/*     */ 
/*     */   protected void init(String descriptionKey)
/*     */   {
/*  65 */     this.m_prelocalizedDescriptionKey = descriptionKey;
/*  66 */     this.m_extensions = new HashSet();
/*     */   }
/*     */ 
/*     */   public boolean addAll(String[] extensions)
/*     */   {
/*  71 */     boolean changed = false;
/*     */ 
/*  73 */     for (int i = 0; i < extensions.length; ++i)
/*     */     {
/*  75 */       String extension = extensions[i];
/*  76 */       if ((null == extension) || 
/*  78 */         (!this.m_extensions.add(extension)))
/*     */         continue;
/*  80 */       changed = true;
/*     */     }
/*     */ 
/*  84 */     return changed;
/*     */   }
/*     */ 
/*     */   public String getExtensionsListString()
/*     */   {
/*  89 */     IdcStringBuilder extensionsString = new IdcStringBuilder();
/*  90 */     boolean addSeparator = false;
/*  91 */     Iterator iter = this.m_extensions.iterator();
/*     */ 
/*  93 */     while (iter.hasNext())
/*     */     {
/*  95 */       if (addSeparator)
/*     */       {
/*  97 */         extensionsString.append(", ");
/*     */       }
/*  99 */       String extension = (String)iter.next();
/* 100 */       extensionsString.append("*.");
/* 101 */       extensionsString.append(extension);
/* 102 */       addSeparator = true;
/*     */     }
/* 104 */     return extensionsString.toString();
/*     */   }
/*     */ 
/*     */   public String getFilenameExtensionString(File f)
/*     */   {
/* 109 */     String filename = f.getName();
/* 110 */     int i = filename.lastIndexOf(46);
/* 111 */     String extension = filename.substring(i + 1);
/* 112 */     return convertToExtensionString(extension);
/*     */   }
/*     */ 
/*     */   public String convertToExtensionString(Object extension)
/*     */   {
/*     */     String extensionString;
/*     */     String extensionString;
/* 118 */     if (extension instanceof String)
/*     */     {
/* 120 */       extensionString = (String)extension;
/*     */     }
/*     */     else
/*     */     {
/* 124 */       extensionString = extension.toString();
/*     */     }
/* 126 */     String lowercaseExtension = extensionString.toLowerCase();
/* 127 */     return lowercaseExtension;
/*     */   }
/*     */ 
/*     */   public boolean accept(File f)
/*     */   {
/* 135 */     if (f.isDirectory())
/*     */     {
/* 137 */       return true;
/*     */     }
/* 139 */     String extension = getFilenameExtensionString(f);
/* 140 */     boolean isPresent = contains(extension);
/* 141 */     return isPresent;
/*     */   }
/*     */ 
/*     */   public String getDescription()
/*     */   {
/* 147 */     String extensionsString = getExtensionsListString();
/* 148 */     String description = LocaleResources.getString(this.m_prelocalizedDescriptionKey, null, extensionsString);
/*     */ 
/* 150 */     return description;
/*     */   }
/*     */ 
/*     */   public boolean add(Object extension)
/*     */   {
/*     */     String extensionString;
/*     */     String extensionString;
/* 158 */     if (extension instanceof String)
/*     */     {
/* 160 */       extensionString = (String)extension;
/*     */     }
/*     */     else
/*     */     {
/* 164 */       extensionString = extension.toString();
/*     */     }
/* 166 */     return this.m_extensions.add(extensionString);
/*     */   }
/*     */ 
/*     */   public boolean addAll(Collection extensions)
/*     */   {
/* 171 */     boolean changed = false;
/* 172 */     Iterator iter = extensions.iterator();
/* 173 */     while (iter.hasNext())
/*     */     {
/* 175 */       Object extension = iter.next();
/* 176 */       String extensionString = convertToExtensionString(extension);
/* 177 */       if (this.m_extensions.add(extensionString))
/*     */       {
/* 179 */         changed = true;
/*     */       }
/*     */     }
/* 182 */     return changed;
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 187 */     this.m_extensions.clear();
/*     */   }
/*     */ 
/*     */   public boolean contains(Object extension)
/*     */   {
/* 192 */     String extensionString = convertToExtensionString(extension);
/* 193 */     return this.m_extensions.contains(extensionString);
/*     */   }
/*     */ 
/*     */   public boolean containsAll(Collection extensions)
/*     */   {
/* 198 */     Iterator iter = extensions.iterator();
/* 199 */     while (iter.hasNext())
/*     */     {
/* 201 */       Object extension = iter.next();
/* 202 */       String extensionString = convertToExtensionString(extension);
/* 203 */       if (!this.m_extensions.contains(extensionString))
/*     */       {
/* 205 */         return false;
/*     */       }
/*     */     }
/* 208 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/* 213 */     return this.m_extensions.isEmpty();
/*     */   }
/*     */ 
/*     */   public Iterator iterator()
/*     */   {
/* 218 */     return this.m_extensions.iterator();
/*     */   }
/*     */ 
/*     */   public boolean remove(Object extension)
/*     */   {
/* 223 */     String extensionString = convertToExtensionString(extension);
/* 224 */     return this.m_extensions.remove(extensionString);
/*     */   }
/*     */ 
/*     */   public boolean removeAll(Collection extensions)
/*     */   {
/* 229 */     boolean changed = false;
/* 230 */     Iterator iter = extensions.iterator();
/* 231 */     while (iter.hasNext())
/*     */     {
/* 233 */       Object extension = iter.next();
/* 234 */       String extensionString = convertToExtensionString(extension);
/* 235 */       if (this.m_extensions.remove(extensionString))
/*     */       {
/* 237 */         changed = true;
/*     */       }
/*     */     }
/* 240 */     return changed;
/*     */   }
/*     */ 
/*     */   public boolean retainAll(Collection extensions)
/*     */   {
/* 245 */     boolean changed = false;
/* 246 */     if (this.m_extensions.size() > 0)
/*     */     {
/* 248 */       this.m_extensions.clear();
/* 249 */       changed = true;
/*     */     }
/* 251 */     if (this.m_extensions.addAll(extensions))
/*     */     {
/* 253 */       changed = true;
/*     */     }
/* 255 */     return changed;
/*     */   }
/*     */ 
/*     */   public int size()
/*     */   {
/* 260 */     return this.m_extensions.size();
/*     */   }
/*     */ 
/*     */   public Object[] toArray()
/*     */   {
/* 265 */     return this.m_extensions.toArray();
/*     */   }
/*     */ 
/*     */   public Object[] toArray(Object[] array)
/*     */   {
/* 270 */     return this.m_extensions.toArray(array);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 275 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.FileFilterByExtensions
 * JD-Core Version:    0.5.4
 */