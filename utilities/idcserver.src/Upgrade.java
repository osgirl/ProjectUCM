/*     */ import intradoc.apputilities.installer.MigrateUtils;
/*     */ import intradoc.apputilities.installer.MigrationEnvironment;
/*     */ import intradoc.apputilities.installer.Migrator;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.PrintStream;
/*     */ import java.util.List;
/*     */ 
/*     */ public class Upgrade
/*     */   implements ReportProgress
/*     */ {
/*     */   public final Migrator m_upgrader;
/*     */   public boolean m_skipUpgradeDatabase;
/*     */   public boolean m_skipUpgradeFilesystem;
/*     */ 
/*     */   public static void usage(Object[] args)
/*     */   {
/*  33 */     StringBuilder sb = new StringBuilder();
/*  34 */     for (Object arg : args)
/*     */     {
/*  36 */       sb.append(arg);
/*     */     }
/*  38 */     sb.append("Usage: Upgrader [options] [--] /path/to/source/BinDir\n");
/*  39 */     sb.append("Where options include:\n");
/*  40 */     sb.append("  -v | --verbose                Enable verbosity.\n");
/*  41 */     sb.append("  --urm                         Perform URM upgrade instead of UCM.\n");
/*  42 */     sb.append("  --idcdir /target/IntradocDir  Path to target IntradocDir (default=$BinDir/..).\n");
/*  43 */     sb.append("One of these options is required (unless --skip-fs is specified):\n");
/*  44 */     sb.append("  --bindir /target/BinDir       Path to target BinDir.\n");
/*  45 */     sb.append("  --domaindir domain/dir        Path to target domain dir.\n");
/*  46 */     sb.append("One of these options is required:\n");
/*  47 */     sb.append("  --ecmdir /.../Oracle_ECM1   Path to ECM shiphome dir.\n");
/*  48 */     sb.append("  --ucmdir ..Oracle_ECM1/ucm  Path to UCM shiphome dir.\n");
/*  49 */     sb.append("  --homedir .../ucm/idc       Path to IdcHomeDir.\n");
/*  50 */     sb.append("Only one of the following options is allowed:\n");
/*  51 */     sb.append("  --skip-db                   Skip database upgrade.\n");
/*  52 */     sb.append("  --skip-fs                   Skip filesystem upgrade.\n");
/*  53 */     System.err.print(sb.toString());
/*  54 */     System.exit(1);
/*  55 */     throw new RuntimeException();
/*     */   }
/*     */ 
/*     */   public static void main(String[] args) throws Exception
/*     */   {
/*  60 */     Upgrade upgrade = new Upgrade();
/*  61 */     MigrationEnvironment env = upgrade.m_upgrader.m_environment;
/*  62 */     env.m_productName = "cs";
/*  63 */     int argIndex = 0;
/*  64 */     while ((argIndex < args.length) && (args[argIndex].charAt(0) == '-'))
/*     */     {
/*  66 */       String arg = args[(argIndex++)];
/*  67 */       if ((arg.equals("-v")) || (arg.equals("--verbose")))
/*     */       {
/*  69 */         intradoc.common.SystemUtils.m_verbose = true;
/*     */       }
/*     */ 
/*  72 */       if (arg.equals("--")) {
/*     */         break;
/*     */       }
/*     */ 
/*  76 */       if (arg.equals("--skip-db"))
/*     */       {
/*  78 */         upgrade.m_skipUpgradeDatabase = true;
/*  79 */         if (!upgrade.m_skipUpgradeFilesystem)
/*     */           continue;
/*  81 */         usage(new Object[] { "You can only specify one of --skip-db or --skip-fs!\n\n" });
/*     */       }
/*     */ 
/*  85 */       if (arg.equals("--skip-fs"))
/*     */       {
/*  87 */         upgrade.m_skipUpgradeFilesystem = true;
/*  88 */         if (!upgrade.m_skipUpgradeDatabase)
/*     */           continue;
/*  90 */         usage(new Object[] { "You can only specify one of --skip-db or --skip-fs!\n\n" });
/*     */       }
/*     */ 
/*  95 */       if (argIndex >= args.length)
/*     */       {
/*  97 */         usage(new Object[] { "Missing argument for ", arg, "!\n\n" });
/*     */       }
/*  99 */       else if (arg.equals("--idcdir"))
/*     */       {
/* 101 */         env.m_targetIntradocDir = args[(argIndex++)];
/*     */       }
/* 103 */       else if (arg.equals("--bindir"))
/*     */       {
/* 105 */         env.m_targetBinDir = args[(argIndex++)];
/*     */       }
/* 107 */       else if (arg.equals("--domaindir"))
/*     */       {
/* 109 */         env.m_targetDomainDir = args[(argIndex++)];
/*     */       }
/* 111 */       else if (arg.equals("--ecmdir"))
/*     */       {
/* 113 */         String ecmdir = args[(argIndex++)];
/* 114 */         env.m_targetHomeDir = new StringBuilder().append(FileUtils.directorySlashes(ecmdir)).append("ucm/idc/").toString();
/*     */       }
/* 116 */       else if (arg.equals("--ucmdir"))
/*     */       {
/* 118 */         String ucmdir = args[(argIndex++)];
/* 119 */         env.m_targetHomeDir = new StringBuilder().append(FileUtils.directorySlashes(ucmdir)).append("idc/").toString();
/*     */       }
/* 121 */       else if (arg.equals("--homedir"))
/*     */       {
/* 123 */         env.m_targetHomeDir = args[(argIndex++)];
/*     */       }
/* 125 */       else if (arg.equals("--urm"))
/*     */       {
/* 127 */         env.m_productName = "urm";
/*     */       }
/*     */       else
/*     */       {
/* 131 */         usage(new Object[] { "Unknown option: ", arg, "\nn\n" });
/*     */       }
/*     */     }
/* 134 */     if ((!upgrade.m_skipUpgradeFilesystem) && (env.m_targetBinDir == null) && (env.m_targetDomainDir == null))
/*     */     {
/* 136 */       usage(new Object[] { "Either --bindir or --domaindir must be specified for filesystem upgrades." });
/*     */     }
/* 138 */     if (env.m_targetHomeDir == null)
/*     */     {
/* 140 */       usage(new Object[0]);
/*     */     }
/* 142 */     if (argIndex + 1 != args.length)
/*     */     {
/* 144 */       usage(new Object[0]);
/*     */     }
/* 146 */     env.m_sourceBinDir = args[(argIndex++)];
/*     */     try
/*     */     {
/* 150 */       upgrade.doUpgrade();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 154 */       IdcMessage msg = new IdcMessage("csUpgradeFailed", new Object[0]);
/* 155 */       throw new ServiceException(e, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Upgrade()
/*     */   {
/* 167 */     this.m_upgrader = MigrateUtils.createMigrator();
/* 168 */     this.m_upgrader.m_environment.m_reportProgress = this;
/*     */   }
/*     */ 
/*     */   public void doUpgrade()
/*     */     throws DataException, ServiceException
/*     */   {
/* 179 */     Migrator upgrader = this.m_upgrader;
/* 180 */     MigrationEnvironment env = upgrader.m_environment;
/*     */     try
/*     */     {
/* 185 */       int flags = 391;
/*     */ 
/* 188 */       if (!this.m_skipUpgradeDatabase)
/*     */       {
/* 190 */         flags |= 1032;
/*     */       }
/* 192 */       if (!this.m_skipUpgradeFilesystem)
/*     */       {
/* 194 */         flags |= 2048;
/*     */       }
/* 196 */       env.m_migrateType = "Upgrade";
/* 197 */       upgrader.init(flags, new Object[0]);
/*     */     }
/*     */     catch (IdcException e)
/*     */     {
/* 201 */       IdcMessage msg = new IdcMessage("csMigrateFailedInit", new Object[0]);
/* 202 */       throw new ServiceException(e, msg);
/*     */     }
/* 204 */     int numItems = env.m_items.size();
/* 205 */     while (env.m_currentItemIndex < numItems)
/*     */     {
/* 207 */       upgrader.doNextMigrateItem();
/*     */     }
/* 209 */     upgrader.finishMigrate();
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, float amtDone, float max)
/*     */   {
/* 215 */     String line = MigrateUtils.createProgressString(msg, amtDone, max);
/* 216 */     System.out.println(line);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 223 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83285 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     Upgrade
 * JD-Core Version:    0.5.4
 */