/*     */ package intradoc.server.script;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ScriptExtensionsAdaptor;
/*     */ import intradoc.common.ScriptInfo;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.publish.PublishedResourceUtils;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class PageCreationScriptExtensions extends ScriptExtensionsAdaptor
/*     */ {
/*     */   public PageCreationScriptExtensions()
/*     */   {
/*  47 */     this.m_variableTable = new String[0];
/*     */ 
/*  49 */     this.m_variableDefinitionTable = new int[0][];
/*     */ 
/*  51 */     this.m_functionTable = new String[] { "isAvailableWebFeature", "isRequiredWebFeature", "requireWebFeature", "requireWebFeatures", "createRequiredPublishedResourceList", "createWebFeaturesList" };
/*     */ 
/*  69 */     this.m_functionDefinitionTable = new int[][] { { 3, 1, 0, -1, 1 }, { 4, 1, 0, -1, 1 }, { 5, 1, 0, -1, -1 }, { 6, 1, 0, -1, -1 }, { 7, 0, 0, -1, 1 }, { 9, 0, 0, -1, 1 } };
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/*  84 */     int[] config = (int[])(int[])info.m_entry;
/*  85 */     String function = info.m_key;
/*  86 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/*  87 */     PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(context);
/*     */ 
/*  89 */     if ((config == null) || (binder == null) || (pageMerger == null))
/*     */     {
/*  91 */       return false;
/*     */     }
/*     */ 
/*  94 */     int returnType = config[4];
/*  95 */     int nargs = args.length - 1;
/*  96 */     int minParams = config[1];
/*  97 */     if ((minParams >= 0) && (nargs < minParams))
/*     */     {
/*  99 */       String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + minParams);
/*     */ 
/* 101 */       throw new IllegalArgumentException(msg);
/*     */     }
/*     */ 
/* 107 */     String sArg1 = null;
/* 108 */     if ((nargs > 0) && 
/* 110 */       (config[2] == 0))
/*     */     {
/* 112 */       sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*     */     }
/*     */ 
/* 117 */     boolean bResult = false;
/* 118 */     int iResult = 0;
/* 119 */     double dResult = 0.0D;
/* 120 */     Object oResult = null;
/*     */     Set requiredFeatures;
/*     */     String rsetNamePublished;
/* 122 */     switch (config[0])
/*     */     {
/*     */     case 3:
/* 132 */       checkNonEmpty(sArg1);
/*     */       try
/*     */       {
/* 135 */         bResult = PublishedResourceUtils.isFeatureAvailable(sArg1);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 139 */         bResult = false;
/* 140 */         Report.trace("system", "Unable to check for web feature: " + sArg1, e);
/*     */       }
/* 142 */       break;
/*     */     case 4:
/*     */     case 5:
/*     */     case 6:
/* 170 */       checkNonEmpty(sArg1);
/*     */ 
/* 172 */       requiredFeatures = (Set)context.getCachedObject("PageRequiredWebFeatures");
/*     */ 
/* 174 */       if (null == requiredFeatures)
/*     */       {
/* 176 */         requiredFeatures = new HashSet();
/* 177 */         context.setCachedObject("PageRequiredWebFeatures", requiredFeatures);
/*     */       }
/* 179 */       String webFeatureErrorMsg = null;
/*     */       try
/*     */       {
/* 182 */         switch (config[0])
/*     */         {
/*     */         case 4:
/* 185 */           webFeatureErrorMsg = "Unable to check for web feature";
/* 186 */           bResult = PublishedResourceUtils.isFeatureRequired(requiredFeatures, sArg1);
/* 187 */           break;
/*     */         case 5:
/* 189 */           webFeatureErrorMsg = "Unable to add web feature dependency";
/* 190 */           PublishedResourceUtils.addRequiredFeatureDependencies(requiredFeatures, sArg1);
/* 191 */           bResult = true;
/* 192 */           break;
/*     */         case 6:
/* 194 */           webFeatureErrorMsg = "Unable to add web feature dependencies";
/* 195 */           List webFeaturesList = StringUtils.parseArray(sArg1, ',', ',');
/* 196 */           String[] webFeatures = StringUtils.convertToArray(webFeaturesList);
/* 197 */           PublishedResourceUtils.addRequiredFeatureDependenciesFromArray(requiredFeatures, webFeatures);
/* 198 */           bResult = true;
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 204 */         bResult = false;
/* 205 */         String msg = webFeatureErrorMsg + ": " + sArg1;
/* 206 */         if (4 != config[0])
/*     */         {
/* 209 */           Report.trace("system", msg, e);
/*     */         }
/*     */       }
/* 212 */       break;
/*     */     case 7:
/* 222 */       rsetNamePublished = sArg1;
/* 223 */       if ((null == rsetNamePublished) || (rsetNamePublished.length() < 1))
/*     */       {
/* 225 */         rsetNamePublished = "PageRequiredPublishedWebResources";
/*     */       }
/* 227 */       requiredFeatures = (Set)context.getCachedObject("PageRequiredWebFeatures");
/*     */ 
/* 229 */       if ((null != requiredFeatures) && (PublishedResourceUtils.isPublishedResourcesInitialized()))
/*     */       {
/*     */         try
/*     */         {
/* 233 */           List publishedResourcesList = PublishedResourceUtils.createPublishedResourceListFromRequiredFeatures(requiredFeatures);
/*     */ 
/* 235 */           DataResultSet publishedResources = ResultSetUtils.createResultSetFromList(rsetNamePublished, publishedResourcesList, "path");
/*     */ 
/* 237 */           binder.addResultSet(rsetNamePublished, publishedResources);
/* 238 */           bResult = true;
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 242 */           bResult = false;
/*     */ 
/* 244 */           Report.trace("system", "Unable to create required published resource result set", e);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 250 */         bResult = false;
/* 251 */         if (null != requiredFeatures)
/*     */         {
/* 253 */           Report.trace("idocscript", "Published resources not initialized!", null); } 
/* 253 */       }break;
/*     */     case 9:
/* 265 */       rsetNamePublished = sArg1;
/* 266 */       if ((null == rsetNamePublished) || (rsetNamePublished.length() < 1))
/*     */       {
/* 268 */         rsetNamePublished = "WebFeatures";
/*     */       }
/* 270 */       requiredFeatures = (Set)context.getCachedObject("PageRequiredWebFeatures");
/* 271 */       if (null != requiredFeatures)
/*     */       {
/*     */         try
/*     */         {
/* 275 */           DataResultSet webFeatures = PublishedResourceUtils.createWebFeaturesResultSet(requiredFeatures);
/*     */ 
/* 277 */           binder.addResultSet(rsetNamePublished, webFeatures);
/* 278 */           bResult = true;
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 282 */           bResult = false;
/* 283 */           Report.trace("idocscript", e, null);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 288 */         bResult = false;
/* 289 */         Report.trace("idocscript", "Published resources not initialized!", null);
/*     */       }
/* 291 */       break;
/*     */     case 8:
/*     */     default:
/* 294 */       return false;
/*     */     }
/*     */ 
/* 298 */     args[nargs] = ScriptExtensionUtils.computeReturnObject(returnType, bResult, iResult, dResult, oResult);
/*     */ 
/* 301 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 306 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87465 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.PageCreationScriptExtensions
 * JD-Core Version:    0.5.4
 */