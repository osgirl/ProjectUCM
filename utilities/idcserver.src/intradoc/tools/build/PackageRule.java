/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.util.PatternFilter;
/*     */ import java.io.File;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class PackageRule
/*     */   implements Iterable<Item>
/*     */ {
/*     */   public boolean m_isBundled;
/*     */   public String m_packageFilename;
/*     */   public List<Item> m_items;
/*     */   protected static final String BUNDLE_EXTENSIONS = "ear,jar,war,zip";
/*     */   protected static String[] s_bundleSuffixes;
/*     */ 
/*     */   public PackageRule()
/*     */   {
/*  58 */     this.m_items = new ArrayList();
/*     */   }
/*     */ 
/*     */   public PackageRule(boolean isBundled, String filename)
/*     */   {
/*  64 */     this.m_isBundled = isBundled;
/*  65 */     this.m_packageFilename = filename;
/*     */   }
/*     */ 
/*     */   public Item addItem(String dirname, String entryPrefix, PatternFilter filter)
/*     */   {
/*  70 */     Item item = new Item();
/*  71 */     item.m_dirname = dirname;
/*  72 */     item.m_entryPrefix = entryPrefix;
/*  73 */     item.m_filter = filter;
/*  74 */     this.m_items.add(item);
/*  75 */     return item;
/*     */   }
/*     */ 
/*     */   public Iterator<Item> iterator()
/*     */   {
/*  80 */     return this.m_items.iterator();
/*     */   }
/*     */ 
/*     */   public File getTargetFile(File parentDir)
/*     */   {
/*  85 */     String filename = this.m_packageFilename;
/*  86 */     File dir = null;
/*  87 */     if (!FileUtils.isAbsolutePath(filename))
/*     */     {
/*  89 */       dir = parentDir;
/*     */     }
/*  91 */     File file = new File(dir, filename);
/*  92 */     return file;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/*  98 */     return this.m_packageFilename;
/*     */   }
/*     */ 
/*     */   protected static String[] getBundleSuffixes()
/*     */   {
/* 173 */     String[] suffixes = s_bundleSuffixes;
/* 174 */     if (suffixes == null)
/*     */     {
/* 176 */       String[] extensions = "ear,jar,war,zip".split(",");
/* 177 */       int numExtensions = extensions.length;
/* 178 */       suffixes = new String[numExtensions];
/* 179 */       for (int e = numExtensions - 1; e >= 0; --e)
/*     */       {
/* 181 */         suffixes[e] = ('.' + extensions[e]);
/*     */       }
/* 183 */       s_bundleSuffixes = suffixes;
/*     */     }
/* 185 */     return suffixes;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 191 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99352 $";
/*     */   }
/*     */ 
/*     */   public static class Group
/*     */     implements Iterable<PackageRule>
/*     */   {
/*     */     public List<PackageRule> m_packages;
/*     */     protected Map<String, PackageRule> m_packagesByFilename;
/*     */ 
/*     */     public Group()
/*     */     {
/* 108 */       this.m_packages = new ArrayList();
/* 109 */       this.m_packagesByFilename = new HashMap();
/*     */     }
/*     */ 
/*     */     public Iterator<PackageRule> iterator()
/*     */     {
/* 114 */       return this.m_packages.iterator();
/*     */     }
/*     */ 
/*     */     public PackageRule.Item add(String packagePath, String dirname, PatternFilter filter)
/*     */     {
/* 119 */       String[] suffixes = PackageRule.getBundleSuffixes();
/* 120 */       boolean isBundled = false;
/* 121 */       String entryPrefix = null;
/* 122 */       for (int s = suffixes.length - 1; s >= 0; --s)
/*     */       {
/* 124 */         String suffix = suffixes[s];
/* 125 */         if (packagePath.endsWith(suffix))
/*     */         {
/* 127 */           isBundled = true;
/* 128 */           break;
/*     */         }
/* 130 */         String container = new StringBuilder().append(suffix).append('/').toString();
/* 131 */         int index = packagePath.indexOf(container);
/* 132 */         if (index < 0)
/*     */           continue;
/* 134 */         isBundled = true;
/* 135 */         int slashIndex = index + container.length() - 1;
/* 136 */         entryPrefix = packagePath.substring(slashIndex + 1);
/* 137 */         packagePath = packagePath.substring(0, slashIndex);
/* 138 */         break;
/*     */       }
/*     */ 
/* 141 */       PackageRule pkg = (PackageRule)this.m_packagesByFilename.get(packagePath);
/* 142 */       if (pkg == null)
/*     */       {
/* 144 */         pkg = new PackageRule(isBundled, packagePath);
/* 145 */         this.m_packages.add(pkg);
/* 146 */         this.m_packagesByFilename.put(packagePath, pkg);
/*     */       }
/* 148 */       return pkg.addItem(dirname, entryPrefix, filter);
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 154 */       StringBuilder sb = new StringBuilder();
/* 155 */       boolean isFirst = true;
/* 156 */       for (PackageRule pkg : this.m_packages)
/*     */       {
/* 158 */         if (!isFirst)
/*     */         {
/* 160 */           sb.append(", ");
/*     */         }
/* 162 */         sb.append(pkg);
/*     */       }
/* 164 */       return sb.toString();
/*     */     }
/*     */   }
/*     */ 
/*     */   public class Item
/*     */   {
/*     */     public String m_dirname;
/*     */     public String m_entryPrefix;
/*     */     public PatternFilter m_filter;
/*     */     public String m_stepName;
/*     */ 
/*     */     public Item()
/*     */     {
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/*  42 */       StringBuilder sb = new StringBuilder();
/*  43 */       if (this.m_entryPrefix != null)
/*     */       {
/*  45 */         sb.append(this.m_entryPrefix);
/*     */       }
/*  47 */       sb.append(this.m_dirname);
/*  48 */       return sb.toString();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.PackageRule
 * JD-Core Version:    0.5.4
 */