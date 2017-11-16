/*     */ package intradoc.loader;
/*     */ 
/*     */ import intradoc.io.zip.IdcZipEnvironment;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class IdcLoaderUtils
/*     */ {
/*     */   public static String m_pathSeparator;
/*     */   protected static IdcLoaderSecurityManager m_defaultManager;
/*     */ 
/*     */   public static List<String> splitPathElements(String paths)
/*     */   {
/*  43 */     if (null == m_pathSeparator)
/*     */     {
/*  45 */       m_pathSeparator = System.getProperty("path.separator");
/*     */     }
/*  47 */     List pathList = new ArrayList();
/*  48 */     while (paths.length() > 0)
/*     */     {
/*  50 */       int index = paths.indexOf(m_pathSeparator);
/*  51 */       switch (index)
/*     */       {
/*     */       case -1:
/*  54 */         paths = fixupPath(paths);
/*  55 */         pathList.add(paths);
/*  56 */         paths = "";
/*  57 */         break;
/*     */       default:
/*  59 */         String path = paths.substring(0, index);
/*  60 */         path = fixupPath(path);
/*  61 */         pathList.add(path);
/*     */       case 0:
/*  63 */         paths = paths.substring(index + 1);
/*     */       }
/*     */     }
/*     */ 
/*  67 */     return pathList;
/*     */   }
/*     */ 
/*     */   protected static String fixupPath(String path)
/*     */   {
/*  79 */     int len = path.length();
/*     */ 
/*  83 */     int start = 0;
/*  84 */     if ((len > 0) && (path.charAt(0) == '\\'))
/*     */     {
/*  86 */       ++start;
/*  87 */       if ((len > 1) && (path.charAt(start) == '\\'))
/*     */       {
/*  89 */         ++start;
/*     */       }
/*     */       else
/*     */       {
/*  93 */         start = 0;
/*     */       }
/*     */     }
/*  96 */     String prefix = null;
/*  97 */     if (start > 0)
/*     */     {
/*  99 */       prefix = path.substring(0, start);
/* 100 */       path = path.substring(start);
/* 101 */       len = path.length();
/*     */     }
/* 103 */     if ((len > 0) && (path.charAt(len - 1) == '/'))
/*     */     {
/* 105 */       path = path.substring(0, len - 1);
/*     */     }
/* 107 */     path = path.replace('\\', '/');
/* 108 */     path = path.replace("//", "/");
/* 109 */     if (prefix != null)
/*     */     {
/* 111 */       path = prefix + path;
/*     */     }
/* 113 */     return path;
/*     */   }
/*     */ 
/*     */   public static Class[] getCallerClassStack()
/*     */   {
/* 137 */     if (null == m_defaultManager)
/*     */     {
/* 139 */       m_defaultManager = new IdcLoaderSecurityManager();
/*     */     }
/* 141 */     Class[] wholeStack = m_defaultManager.getClassContext();
/* 142 */     int stackSize = wholeStack.length - 2;
/* 143 */     Class[] callerStack = new Class[stackSize];
/* 144 */     System.arraycopy(wholeStack, 2, callerStack, 0, stackSize);
/* 145 */     return callerStack;
/*     */   }
/*     */ 
/*     */   public static Class getCallerClassStackElement(int offset)
/*     */   {
/* 156 */     if (null == m_defaultManager)
/*     */     {
/* 158 */       m_defaultManager = new IdcLoaderSecurityManager();
/*     */     }
/* 160 */     Class[] stack = m_defaultManager.getClassContext();
/* 161 */     return stack[(offset + 2)];
/*     */   }
/*     */ 
/*     */   public static ClassLoader getCallerClassLoader()
/*     */   {
/* 166 */     Class caller = getCallerClassStackElement(1);
/* 167 */     ClassLoader loader = caller.getClassLoader();
/* 168 */     return loader;
/*     */   }
/*     */ 
/*     */   public static void setUseParentForClass(String name, boolean useParent)
/*     */   {
/* 179 */     Class caller = getCallerClassStackElement(1);
/* 180 */     ClassLoader loader = caller.getClassLoader();
/* 181 */     if (!loader instanceof IdcClassLoader)
/*     */       return;
/* 183 */     IdcClassLoader idcLoader = (IdcClassLoader)loader;
/* 184 */     idcLoader.setUseParentForClass(name, useParent);
/*     */   }
/*     */ 
/*     */   public static void setUseParentForClassPrefix(String prefix)
/*     */   {
/* 200 */     Class caller = getCallerClassStackElement(1);
/* 201 */     ClassLoader loader = caller.getClassLoader();
/* 202 */     if (!loader instanceof IdcClassLoader)
/*     */       return;
/* 204 */     IdcClassLoader idcLoader = (IdcClassLoader)loader;
/* 205 */     String[] prefixes = prefix.split(",");
/* 206 */     for (int p = 0; p < prefixes.length; ++p)
/*     */     {
/* 208 */       idcLoader.setUseParentForClassPrefix(prefixes[p]);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static int getSystemPropertyAsIntWithDefault(IdcLoader loader, String propertyName, int defaultValue)
/*     */   {
/* 223 */     int verbosity = loader.getZipEnvironment().m_verbosity;
/* 224 */     String propertyValue = System.getProperty(propertyName);
/* 225 */     if (null != propertyValue)
/*     */     {
/*     */       try
/*     */       {
/* 229 */         Integer i = Integer.valueOf(Integer.parseInt(propertyValue));
/* 230 */         if (verbosity >= 7)
/*     */         {
/* 232 */           loader.report(7, new Object[] { "using system property ", propertyName, " = ", i });
/*     */         }
/* 234 */         return i.intValue();
/*     */       }
/*     */       catch (NumberFormatException ignore)
/*     */       {
/* 238 */         if (verbosity >= 5)
/*     */         {
/* 240 */           loader.report(5, new Object[] { ignore.toString(), " on system property ", propertyName });
/*     */         }
/*     */       }
/*     */     }
/* 244 */     return defaultValue;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 251 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81920 $";
/*     */   }
/*     */ 
/*     */   protected static class IdcLoaderSecurityManager extends SecurityManager
/*     */   {
/*     */     protected Class[] getClassContext()
/*     */     {
/* 123 */       return super.getClassContext();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.loader.IdcLoaderUtils
 * JD-Core Version:    0.5.4
 */