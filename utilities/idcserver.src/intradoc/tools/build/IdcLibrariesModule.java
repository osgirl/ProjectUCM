/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.io.HTTPDownloader.StateListener;
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileReader;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class IdcLibrariesModule extends Module
/*     */ {
/*     */   protected List<Module.FetchRule> m_fetchRulesFromFullsource;
/*     */ 
/*     */   public void updateDependencies(int flags, HTTPDownloader.StateListener callback)
/*     */     throws IdcException
/*     */   {
/*  42 */     super.updateDependencies(flags & 0xFFFFFEFF, callback);
/*     */ 
/*  44 */     if ((flags & 0x8) != 0) {
/*     */       return;
/*     */     }
/*  47 */     if (this.m_fetchRulesFromFullsource == null)
/*     */     {
/*  49 */       this.m_fetchRulesFromFullsource = computeFetchRulesFromFullsource(flags);
/*     */     }
/*  51 */     List oldFetchRules = this.m_fetchRules;
/*     */     try
/*     */     {
/*  54 */       this.m_fetchRules = this.m_fetchRulesFromFullsource;
/*  55 */       super.updateDependencies(flags, callback);
/*     */     }
/*     */     finally
/*     */     {
/*  59 */       this.m_fetchRules = oldFetchRules;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected List<Module.FetchRule> computeFetchRulesFromFullsource(int flags)
/*     */     throws IdcException
/*     */   {
/*  66 */     List fetchRules = new ArrayList();
/*  67 */     String fullsourceFilepath = this.m_properties.getProperty("fullsourceFilepath");
/*  68 */     File fullsourceFile = new File(fullsourceFilepath);
/*  69 */     FileReader fr = null;
/*  70 */     BufferedReader br = null;
/*     */     try
/*     */     {
/*  73 */       fr = new FileReader(fullsourceFile);
/*  74 */       br = new BufferedReader(fr);
/*     */ 
/*  76 */       while ((line = br.readLine()) != null)
/*     */       {
/*  79 */         String line;
/*  78 */         pairs = line.split("\\s+");
/*  79 */         if (pairs.length < 1) continue; if (!pairs[0].equals("#;")) {
/*     */           continue;
/*     */         }
/*     */ 
/*  83 */         int a = 1;
/*  84 */         String source = pairs[(a++)];
/*  85 */         char ch0 = source.charAt(0);
/*  86 */         if (ch0 == '?') continue; if (source.endsWith("/")) {
/*     */           continue;
/*     */         }
/*     */ 
/*  90 */         if (ch0 != '$')
/*     */         {
/*  92 */           source = "$UCM/" + source;
/*     */         }
/*     */         String target;
/*  96 */         if ((a < pairs.length) && (pairs[a].equals("=>")))
/*     */         {
/*  98 */           ++a;
/*  99 */           String target = pairs[(a++)];
/* 100 */           if ((target.equals("unknown")) || (target.equals("internal")))
/*     */           {
/* 102 */             target = "$IdcLibraries/" + target + '/';
/*     */           }
/* 104 */           else if (!target.startsWith("$"))
/*     */           {
/* 106 */             target = "$ORACLE_COMMON_MODULES_DIR/" + target;
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 112 */           int modulesIndex = source.indexOf("/modules/");
/*     */           String target;
/* 113 */           if (modulesIndex < 0)
/*     */           {
/* 115 */             int lastSlashIndex = source.lastIndexOf('/');
/* 116 */             target = "$ORACLE_COMMON_MODULES_DIR" + source.substring(lastSlashIndex);
/*     */           }
/*     */           else
/*     */           {
/* 120 */             target = "$ORACLE_COMMON_MODULES_DIR" + source.substring(modulesIndex + 8);
/*     */           }
/*     */         }
/*     */ 
/* 124 */         Module.FetchRule rule = createFetchRule(source, target, null);
/* 125 */         fetchRules.add(rule);
/*     */       }
/* 127 */       String[] pairs = fetchRules;
/*     */ 
/* 142 */       return pairs;
/*     */     }
/*     */     catch (IOException ioe)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       IdcMessage msg;
/* 136 */       if (br != null)
/*     */       {
/* 138 */         FileUtils.closeReader(br);
/*     */       }
/* 140 */       else if (fr != null)
/*     */       {
/* 142 */         FileUtils.closeReader(fr);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 150 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99784 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.IdcLibrariesModule
 * JD-Core Version:    0.5.4
 */