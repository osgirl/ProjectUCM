/*     */ package intradoc.apputilities.idcshell;
/*     */ 
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ScriptContext;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.IdcManagerBase;
/*     */ import intradoc.server.IdcServerManager;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserAttribInfo;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserUtils;
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class InteractiveShellStartup
/*     */ {
/*     */   public static boolean m_isInit;
/*     */   public Properties m_startupEnvironment;
/*     */   public int m_shellMode;
/*     */ 
/*     */   public InteractiveShellStartup()
/*     */   {
/*  38 */     this.m_startupEnvironment = new IdcProperties();
/*     */   }
/*     */ 
/*     */   public int startup(String[] args)
/*     */   {
/*  43 */     DataBinder binder = null;
/*     */     try
/*     */     {
/*  46 */       String failedArg = processArguments(null, args, binder, false);
/*  47 */       if (failedArg != null)
/*     */       {
/*  49 */         usage(failedArg);
/*     */       }
/*     */ 
/*  52 */       init();
/*  53 */       InteractiveShell shell = new InteractiveShell();
/*     */ 
/*  55 */       ScriptContext context = (ScriptContext)AppObjectRepository.getObject("DefaultScriptContext");
/*  56 */       shell.load(context);
/*     */ 
/*  58 */       ResourceContainer res = SharedObjects.getResources();
/*  59 */       String resDir = DirectoryLocator.getResourcesDirectory();
/*  60 */       DataLoader.cacheResourceFile(res, resDir + "core/idoc/std_shell_resources.idoc");
/*     */ 
/*  62 */       binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  63 */       processArguments(shell, args, binder, true);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  67 */       System.out.println("");
/*  68 */       t.printStackTrace();
/*  69 */       System.out.println("");
/*  70 */       while (t != null)
/*     */       {
/*  72 */         if (t.getMessage() != null)
/*     */         {
/*  74 */           System.out.println(LocaleResources.localizeMessage(t.getMessage(), null));
/*     */         }
/*     */ 
/*  77 */         t = t.getCause();
/*     */       }
/*  79 */       return 99;
/*     */     }
/*     */ 
/*  82 */     String rcString = binder.getLocal("rc");
/*  83 */     int rc = 0;
/*  84 */     if (rcString != null)
/*     */     {
/*  86 */       rc = NumberUtils.parseInteger(rcString, 98);
/*  87 */       if (rc < 0)
/*     */       {
/*  89 */         rc *= -1;
/*     */       }
/*     */     }
/*  92 */     return rc;
/*     */   }
/*     */ 
/*     */   public String processArguments(InteractiveShell shell, String[] args, DataBinder binder, boolean doOperations)
/*     */     throws DataException, ServiceException
/*     */   {
/*  99 */     List scriptArgs = new ArrayList();
/* 100 */     for (int i = 0; i < args.length; ++i)
/*     */     {
/* 102 */       String arg = args[i];
/*     */ 
/* 104 */       if (arg.startsWith("-"))
/*     */       {
/* 106 */         if (arg.indexOf("-help") >= 0)
/*     */         {
/* 108 */           return "";
/*     */         }
/* 110 */         if (arg.equals("--no-database"))
/*     */         {
/* 112 */           this.m_startupEnvironment.put("EnableFeature_JDBC", "false");
/* 113 */           this.m_startupEnvironment.put("EnableFeature_Search", "false"); continue;
/*     */         }
/* 115 */         if (arg.equals("--simple"))
/*     */         {
/* 117 */           this.m_shellMode = 0; continue;
/*     */         }
/* 119 */         if (arg.equals("--idoc"))
/*     */         {
/* 121 */           this.m_shellMode = 1; continue;
/*     */         }
/* 123 */         if ((arg.startsWith("--set")) || (arg.startsWith("-set")))
/*     */         {
/* 125 */           int keyIndex = arg.indexOf("-", 2);
/*     */           String assignment;
/*     */           String assignment;
/* 127 */           if (keyIndex == -1)
/*     */           {
/* 129 */             if (args.length < i + 1)
/*     */             {
/* 131 */               return arg;
/*     */             }
/* 133 */             assignment = args[(++i)];
/*     */           }
/*     */           else
/*     */           {
/* 137 */             assignment = arg.substring(keyIndex + 1);
/*     */           }
/* 139 */           String key = assignment;
/* 140 */           String value = "1";
/* 141 */           int eqIndex = assignment.indexOf("=");
/* 142 */           if (eqIndex > 0)
/*     */           {
/* 144 */             key = assignment.substring(0, eqIndex);
/* 145 */             value = assignment.substring(eqIndex + 1);
/*     */           }
/* 147 */           this.m_startupEnvironment.put(key, value);
/* 148 */           continue;
/* 149 */         }if (arg.indexOf("-v") >= 0)
/*     */         {
/* 151 */           SystemUtils.addAsActiveTrace("idcshell"); continue;
/*     */         }
/* 153 */         if (arg.indexOf("-console") >= 0)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 159 */         throw new DataException("Invalid process argument - " + arg);
/*     */       }
/*     */ 
/* 162 */       if (!doOperations)
/*     */         continue;
/* 164 */       scriptArgs.add(arg);
/*     */     }
/*     */ 
/* 167 */     if (doOperations)
/*     */     {
/* 169 */       Service service = new Service();
/* 170 */       ServiceData sd = new ServiceData();
/* 171 */       service.init(null, null, binder, sd);
/* 172 */       service.initDelegatedObjects();
/* 173 */       UserData userData = UserUtils.createUserData("<system>");
/* 174 */       userData.m_hasAttributesLoaded = true;
/* 175 */       userData.checkCreateAttributes(false);
/* 176 */       Map userAttributes = userData.getAttributesMap();
/* 177 */       Vector roles = new Vector();
/* 178 */       UserAttribInfo adminRole = new UserAttribInfo();
/* 179 */       adminRole.m_attribName = "admin";
/* 180 */       adminRole.m_attribPrivilege = 65535;
/* 181 */       roles.addElement(adminRole);
/* 182 */       userAttributes.put("role", roles);
/* 183 */       service.setUserData(userData);
/* 184 */       if (scriptArgs.size() > 0)
/*     */       {
/* 186 */         service.setCachedObject("script_arguments", scriptArgs);
/*     */       }
/* 188 */       shell.interact(null, service, this.m_shellMode);
/* 189 */       SubjectManager.monitor();
/*     */     }
/* 191 */     return null;
/*     */   }
/*     */ 
/*     */   public static void usage(String failedArg)
/*     */   {
/* 196 */     if (failedArg.length() > 0)
/*     */     {
/* 198 */       SystemUtils.outln("Unknown argument: " + failedArg);
/*     */     }
/* 200 */     SystemUtils.outln("Usage: Execute idocscript: IdcShell [args] script\n       Start interactive shell: IdcShell [args]\n     args: \n       --help      This usage information\n       -v          Enable idcshell tracing\n       --set-key=value\n                   Set environment value key to value\n       --no-database\n                   Start the shell without database access\n       --simple    Start the shell in simple parsing mode\n       --idoc      Start the shell in idocscript parsing mode\n\n       More help: IdcShell --simple \"include shell_help\"\n");
/*     */ 
/* 214 */     System.exit(1);
/*     */   }
/*     */ 
/*     */   public void init() throws DataException, ServiceException
/*     */   {
/* 219 */     if (m_isInit)
/*     */     {
/* 221 */       return;
/*     */     }
/* 223 */     IdcSystemConfig.loadInitialConfig();
/*     */ 
/* 225 */     SharedObjects.putEnvironmentValue("IgnoreComponentLoadError", "true");
/* 226 */     SharedObjects.putEnvironmentValue("TolerateLocalizationFailure", "true");
/* 227 */     SharedObjects.putEnvironmentValue("IsServerMode", "false");
/* 228 */     SharedObjects.putEnvironmentValue("InheritParentUserData", "true");
/* 229 */     SharedObjects.putEnvironmentValue("EnableFeature_Publishing", "false");
/* 230 */     SharedObjects.putEnvironmentValue("DisableErrorPageStackTrace", "false");
/*     */ 
/* 232 */     for (Iterator i$ = this.m_startupEnvironment.keySet().iterator(); i$.hasNext(); ) { Object key = i$.next();
/*     */ 
/* 234 */       if (key instanceof String)
/*     */       {
/* 236 */         String value = this.m_startupEnvironment.getProperty((String)key);
/* 237 */         SharedObjects.putEnvironmentValue((String)key, value);
/*     */       } }
/*     */ 
/*     */ 
/* 241 */     Map args = new HashMap();
/* 242 */     IdcSystemConfig.loadAppConfigInfo(args);
/*     */ 
/* 244 */     String productName = SharedObjects.getEnvironmentValue("IdcProductName");
/* 245 */     IdcManagerBase manager = null;
/*     */ 
/* 247 */     if ((productName != null) && (productName.equals("idccs")))
/*     */     {
/* 249 */       manager = new IdcServerManager();
/*     */     }
/* 251 */     else if ((productName != null) && (productName.equals("idcibr")))
/*     */     {
/* 253 */       Class cl = ClassHelperUtils.createClass("docrefinery.server.RefServerManager");
/* 254 */       manager = (IdcManagerBase)ClassHelperUtils.createInstance(cl);
/*     */     }
/* 256 */     if (manager == null)
/*     */     {
/* 258 */       manager = new IdcServerManager();
/*     */     }
/* 260 */     manager.init();
/* 261 */     m_isInit = true;
/*     */   }
/*     */ 
/*     */   public void setData(DataBinder binder, String arg)
/*     */   {
/* 266 */     int index = arg.indexOf("=");
/* 267 */     if (index == -1)
/*     */     {
/* 269 */       throw new AssertionError("!$setData() passed a string without an =");
/*     */     }
/* 271 */     String key = arg.substring(0, index);
/* 272 */     String value = arg.substring(index + 1);
/* 273 */     binder.putLocal(key, value);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 278 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97382 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idcshell.InteractiveShellStartup
 * JD-Core Version:    0.5.4
 */