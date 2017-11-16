/*     */ package intradoc.server.script;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ScriptExtensionsAdaptor;
/*     */ import intradoc.common.ScriptInfo;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.ComponentLoader;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ComponentScriptExtensions extends ScriptExtensionsAdaptor
/*     */ {
/*     */   public static final String COMPONENT_STATUS_ENABLED = "csCompWizStatusEnabled";
/*     */   public static final String COMPONENT_STATUS_DISABLED = "csCompWizStatusDisabled";
/*     */   public static final String COMPONENT_STATUS_NOTINSTALLED = "csCompWizStatusNotInstalled";
/*     */ 
/*     */   public ComponentScriptExtensions()
/*     */   {
/*  40 */     this.m_variableTable = new String[0];
/*     */ 
/*  45 */     this.m_variableDefinitionTable = new int[0][];
/*     */ 
/*  50 */     this.m_functionTable = new String[] { "isComponentEnabled", "getComponentInfo", "isComponentDisabled", "getComponentStatus", "isComponentInstalled", "getContentAccessFilePath", "isContentAccessPresent" };
/*     */ 
/*  68 */     this.m_functionDefinitionTable = new int[][] { { 0, 1, 0, -1, 1 }, { 1, -1, 0, 0, 0 }, { 2, 1, 0, -1, 1 }, { 3, 1, 0, -1, 0 }, { 4, 1, 0, -1, 1 }, { 5, 1, 0, -1, 0 }, { 6, 0, -1, -1, 1 } };
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/*  84 */     int[] config = (int[])(int[])info.m_entry;
/*  85 */     String function = info.m_key;
/*     */ 
/*  87 */     int nargs = args.length - 1;
/*  88 */     int allowedParams = config[1];
/*  89 */     String insufficientArgsMsg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*     */ 
/*  91 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*     */     {
/*  93 */       throw new IllegalArgumentException(insufficientArgsMsg);
/*     */     }
/*     */ 
/*  99 */     String sArg1 = null;
/* 100 */     String sArg2 = null;
/*     */ 
/* 102 */     long lArg2 = 0L;
/* 103 */     lArg2 += 0L;
/* 104 */     if (nargs > 0)
/*     */     {
/* 106 */       if (config[2] == 0)
/*     */       {
/* 108 */         sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*     */       }
/*     */     }
/* 110 */     if ((config[2] != 1) || 
/* 116 */       (nargs > 1))
/*     */     {
/* 118 */       if (config[3] == 0)
/*     */       {
/* 120 */         sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*     */       }
/* 122 */       else if (config[3] == 1)
/*     */       {
/* 124 */         lArg2 = ScriptUtils.getLongVal(args[1], context);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 129 */     boolean bResult = false;
/* 130 */     int iResult = 0;
/* 131 */     double dResult = 0.0D;
/*     */ 
/* 133 */     Object oResult = null;
/*     */ 
/* 135 */     switch (config[0])
/*     */     {
/*     */     case 0:
/* 139 */       DataBinder cmpBinder = ComponentLoader.getComponentBinder(sArg1);
/* 140 */       bResult = cmpBinder != null;
/* 141 */       break;
/*     */     case 1:
/* 149 */       checkNonEmpty(sArg1);
/* 150 */       checkNonEmpty(sArg2);
/*     */ 
/* 152 */       String type = "enabled";
/* 153 */       if (nargs > 2)
/*     */       {
/* 155 */         String typeArg = ScriptUtils.getDisplayString(args[2], context);
/* 156 */         if (typeArg.matches("disabled"))
/*     */         {
/* 158 */           type = "disabled";
/*     */         }
/* 160 */         else if (typeArg.matches("all"))
/*     */         {
/* 162 */           type = "all";
/*     */         }
/*     */       }
/*     */ 
/* 166 */       oResult = "";
/* 167 */       if (!type.matches("disabled"))
/*     */       {
/* 170 */         DataBinder cmptData = (DataBinder)ComponentLoader.m_components.get(sArg1);
/* 171 */         if (cmptData != null)
/*     */         {
/* 173 */           oResult = cmptData.getLocal(sArg2);
/*     */         }
/*     */ 
/*     */       }
/* 179 */       else if (!type.matches("enabled"))
/*     */       {
/* 182 */         DataBinder cmptData = (DataBinder)ComponentLoader.m_disabledComponents.get(sArg1);
/*     */ 
/* 184 */         if (cmptData != null)
/*     */         {
/* 186 */           oResult = cmptData.getLocal(sArg2);
/*     */         }
/*     */       }
/* 189 */       break;
/*     */     case 2:
/* 194 */       checkNonEmpty(sArg1);
/* 195 */       String status = getComponentStatus(sArg1);
/* 196 */       bResult = status.matches("csCompWizStatusDisabled");
/* 197 */       break;
/*     */     case 3:
/* 201 */       checkNonEmpty(sArg1);
/* 202 */       oResult = getComponentStatus(sArg1);
/* 203 */       break;
/*     */     case 4:
/* 207 */       checkNonEmpty(sArg1);
/* 208 */       String status = getComponentStatus(sArg1);
/* 209 */       bResult = !status.matches("csCompWizStatusNotInstalled");
/* 210 */       break;
/*     */     case 5:
/* 214 */       checkNonEmpty(sArg1);
/*     */       try
/*     */       {
/* 217 */         oResult = LegacyDirectoryLocator.getOitFilePath(sArg1, "type_generic");
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 221 */         Report.trace("componentloader", e, null);
/* 222 */         oResult = "";
/*     */       }
/* 224 */       break;
/*     */     case 6:
/* 228 */       boolean isLoad = false;
/* 229 */       if (EnvUtils.isHostedInAppServer())
/*     */       {
/*     */         try
/*     */         {
/* 233 */           LegacyDirectoryLocator.getOitFilePath("lib/contentaccess/textexport", "type_executable");
/* 234 */           isLoad = true;
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 238 */           isLoad = false;
/* 239 */           SharedObjects.putEnvironmentValue("UseLegacyContentAccessComponent", "true");
/* 240 */           Report.trace("componentloader", "csNewContentAccessMissing", e);
/*     */         }
/*     */       }
/* 243 */       bResult = isLoad;
/* 244 */       break;
/*     */     default:
/* 247 */       return false;
/*     */     }
/*     */ 
/* 250 */     args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4], bResult, iResult, dResult, oResult);
/*     */ 
/* 254 */     return true;
/*     */   }
/*     */ 
/*     */   public String getComponentStatus(String componentName)
/*     */   {
/* 259 */     String retVal = "csCompWizStatusNotInstalled";
/*     */     try
/*     */     {
/* 262 */       DataResultSet components = SharedObjects.getTable("Components");
/* 263 */       String name = ResultSetUtils.findValue(components, "name", componentName, "name");
/*     */ 
/* 265 */       int statusIndex = ResultSetUtils.getIndexMustExist(components, "status");
/* 266 */       if ((name != null) && (name.equals(componentName)))
/*     */       {
/* 268 */         String status = components.getStringValue(statusIndex);
/* 269 */         if (status.equalsIgnoreCase("enabled"))
/*     */         {
/* 271 */           retVal = "csCompWizStatusEnabled";
/*     */         }
/*     */         else
/*     */         {
/* 275 */           retVal = "csCompWizStatusDisabled";
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 281 */       SystemUtils.dumpException(null, e);
/*     */     }
/* 283 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 288 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97066 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.ComponentScriptExtensions
 * JD-Core Version:    0.5.4
 */