/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.PathUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.tools.common.IdcClassLoaderWrapper;
/*     */ import intradoc.util.IdcException;
/*     */ import java.io.File;
/*     */ import java.io.PrintStream;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class HelpModule extends Module
/*     */ {
/*     */   protected File m_helpsetJarFile;
/*     */   protected boolean m_hasComputedFetchRules;
/*     */ 
/*     */   protected boolean isHelpPackaged()
/*     */   {
/*  44 */     File helpsetJarFile = this.m_helpsetJarFile;
/*  45 */     if (helpsetJarFile == null)
/*     */     {
/*  47 */       BuildEnvironment env = this.m_manager.m_env;
/*  48 */       helpsetJarFile = this.m_helpsetJarFile = new File(env.m_shiphomeDir, "ucm/ocsh/helpsets/helpsets.jar");
/*     */     }
/*  50 */     if (helpsetJarFile.exists());
/*  50 */     return false;
/*     */   }
/*     */ 
/*     */   public List<Module.FetchRule> loadFetchRules()
/*     */     throws IdcException
/*     */   {
/*  56 */     List rules = super.loadFetchRules();
/*  57 */     if (!this.m_hasComputedFetchRules)
/*     */     {
/*  59 */       BuildEnvironment env = this.m_manager.m_env;
/*  60 */       ExecutionContext cxt = env.m_context;
/*  61 */       Properties props = this.m_properties;
/*  62 */       int substituteFlags = PathUtils.F_VARS_MUST_EXIST;
/*  63 */       DataBinder config = this.m_buildConfig;
/*  64 */       String translatedSource = config.getLocal("HelpTranslatedSource") + '/';
/*  65 */       String helpDirname = config.getLocal("Help") + '/';
/*  66 */       DataResultSet books = (DataResultSet)config.getResultSet("Books");
/*  67 */       String[] fieldNames = { "book", "source", "wptgPath", "target", "languages" };
/*  68 */       FieldInfo[] fields = ResultSetUtils.createInfoList(books, fieldNames, true);
/*  69 */       int bookIndex = fields[0].m_index; int sourceIndex = fields[1].m_index;
/*  70 */       int wpthPathIndex = fields[2].m_index; int targetIndex = fields[3].m_index;
/*  71 */       int langIndex = fields[4].m_index;
/*     */ 
/*  73 */       for (books.first(); books.isRowPresent(); books.next())
/*     */       {
/*  75 */         String bookName = books.getStringValue(bookIndex); String source = books.getStringValue(sourceIndex);
/*  76 */         String wptgPath = books.getStringValue(wpthPathIndex); String targetPath = books.getStringValue(targetIndex);
/*  77 */         String langString = books.getStringValue(langIndex);
/*  78 */         String[] languages = langString.split(",");
/*  79 */         props.setProperty("book", bookName);
/*     */ 
/*  82 */         props.setProperty("lang", "en");
/*  83 */         String relativeTargetPath = PathUtils.substitutePathVariables(targetPath, props, null, substituteFlags, cxt);
/*  84 */         Module.FetchRule rule = createFetchRule(source, helpDirname + relativeTargetPath, null);
/*  85 */         rules.add(rule);
/*     */ 
/*  88 */         for (String language : languages)
/*     */         {
/*  90 */           if (language.isEmpty())
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/*  95 */           if ((wptgPath == null) || (wptgPath.length() <= 0))
/*     */           {
/*  97 */             String msg = "Cannot build Content Server Help Book: " + bookName + " wptgPath must be specified, when trying to copy translated version of book from wptg.";
/*     */ 
/*  99 */             throw new ServiceException(msg);
/*     */           }
/*     */ 
/* 108 */           props.setProperty("lang", language);
/* 109 */           String relativeWPTGPath = PathUtils.substitutePathVariables(wptgPath, props, null, substituteFlags, cxt);
/* 110 */           String translatedPath = translatedSource + relativeWPTGPath;
/*     */ 
/* 112 */           relativeTargetPath = PathUtils.substitutePathVariables(targetPath, props, null, substituteFlags, cxt);
/* 113 */           rule = createFetchRule(translatedPath, helpDirname + relativeTargetPath, null);
/* 114 */           rules.add(rule);
/*     */         }
/*     */       }
/*     */ 
/* 118 */       this.m_hasComputedFetchRules = true;
/*     */     }
/* 120 */     return rules;
/*     */   }
/*     */ 
/*     */   public void buildPackages()
/*     */     throws IdcException
/*     */   {
/* 126 */     BuildManager manager = this.m_manager;
/*     */ 
/* 128 */     if (isHelpPackaged())
/*     */     {
/* 130 */       System.err.println("WARNING: skipping building helpsets.jar because it already exists");
/*     */     }
/*     */     else
/*     */     {
/* 134 */       long timeStart = System.currentTimeMillis();
/*     */ 
/* 141 */       IdcClassLoaderWrapper loader = manager.checkInitServerForProduct("all");
/* 142 */       String classpath = this.m_moduleDirname + "/classes";
/*     */       try
/*     */       {
/* 145 */         loader.addClassPathElement(classpath, 32);
/*     */ 
/* 150 */         Class clBuildHelpPackage = Class.forName("BuildHelpPackage", true, loader.m_loader);
/* 151 */         Field propertiesField = clBuildHelpPackage.getField("m_properties");
/* 152 */         propertiesField.set(null, this.m_properties);
/* 153 */         Method buildMethod = clBuildHelpPackage.getMethod("build", new Class[0]);
/* 154 */         buildMethod.invoke(null, new Object[0]);
/*     */       }
/*     */       catch (InvocationTargetException ite)
/*     */       {
/* 158 */         this.m_helpsetJarFile.delete();
/* 159 */         Throwable t = ite.getCause();
/* 160 */         if (t instanceof IdcException)
/*     */         {
/* 162 */           throw ((IdcException)t);
/*     */         }
/* 164 */         throw new ServiceException(t);
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 168 */         this.m_helpsetJarFile.delete();
/* 169 */         throw new ServiceException(t);
/*     */       }
/* 171 */       long timeDiff = System.currentTimeMillis() - timeStart;
/* 172 */       System.out.println("built helpsets.jar (" + timeDiff + " ms)");
/*     */     }
/*     */ 
/* 175 */     super.buildPackages();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 180 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103103 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.HelpModule
 * JD-Core Version:    0.5.4
 */