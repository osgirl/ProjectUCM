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
/*     */ import intradoc.data.DocClassUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.server.DocService;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.CollaborationUtils;
/*     */ import intradoc.shared.SecurityAccessListUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import java.util.HashMap;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocServiceScriptExtensions extends ScriptExtensionsAdaptor
/*     */ {
/*  35 */   public boolean m_firstTimeIsEditVarObsolete = true;
/*     */ 
/*     */   public DocServiceScriptExtensions()
/*     */   {
/*  39 */     this.m_variableTable = new String[] { "IsCriteriaSubscription", "FILEPRESENT", "IsFilePresent", "AllowCheckin", "IsEditRev", "DocTypeSelected", "HasOriginal" };
/*     */ 
/*  45 */     this.m_functionTable = new String[] { "loadDocMetaDefinition", "isIgnoredDocMetaField", "parseClbraProject", "computeDocUrl", "parseAccessList", "isDocMetaSetInDocClass" };
/*     */ 
/*  55 */     this.m_functionDefinitionTable = new int[][] { { 0, 0, -1, -1, -1 }, { 1, 2, 0, 0, 1 }, { 2, 1, 0, -1, -1, 0 }, { 3, -1, 0, -1, 0 }, { 4, 1, 0, -1, 0 }, { 5, 2, 0, 0, 1 } };
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/*  70 */     int[] config = (int[])(int[])info.m_entry;
/*  71 */     String function = info.m_key;
/*     */ 
/*  73 */     int nargs = args.length - 1;
/*  74 */     int allowedParams = config[1];
/*  75 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*     */     {
/*  77 */       String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*     */ 
/*  79 */       throw new IllegalArgumentException(msg);
/*     */     }
/*     */ 
/*  82 */     String msg = LocaleUtils.encodeMessage("csScriptMustBeInService", null, function, "Service");
/*     */ 
/*  84 */     Service service = ScriptExtensionUtils.getService(context, msg);
/*  85 */     DataBinder binder = service.getBinder();
/*     */ 
/*  87 */     UserData userData = (UserData)context.getCachedObject("UserData");
/*  88 */     if (userData == null)
/*     */     {
/*  90 */       msg = LocaleUtils.encodeMessage("csUserDataNotAvailable", null, function);
/*  91 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/*  98 */     String sArg1 = null;
/*  99 */     String sArg2 = null;
/*     */ 
/* 102 */     if ((nargs > 0) && 
/* 104 */       (config[2] == 0))
/*     */     {
/* 106 */       sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*     */     }
/*     */ 
/* 116 */     if ((nargs > 1) && 
/* 118 */       (config[3] == 0))
/*     */     {
/* 120 */       sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*     */     }
/*     */ 
/* 131 */     boolean bResult = false;
/* 132 */     int iResult = 0;
/* 133 */     double dResult = 0.0D;
/*     */ 
/* 135 */     Object oResult = null;
/*     */ 
/* 137 */     switch (config[0])
/*     */     {
/*     */     case 0:
/* 141 */       ResultSet rset = SharedObjects.getTable("DocMetaDefinition");
/* 142 */       if (rset == null)
/*     */       {
/* 144 */         Report.trace(null, "DocMetaDefinition table is not available.", null);
/*     */       }
/*     */       else {
/* 147 */         binder.addResultSet("DocMetaDefinition", rset);
/* 148 */         oResult = "";
/*     */       }
/*     */ 
/* 150 */       break;
/*     */     case 1:
/*     */       try
/*     */       {
/* 156 */         DataResultSet drset = SharedObjects.getTable("IgnoredFlexFields");
/* 157 */         if (drset != null)
/*     */         {
/* 159 */           String[] keys = { "templatename", "flexareaname", "fields" };
/* 160 */           FieldInfo[] fi = ResultSetUtils.createInfoList(drset, keys, true);
/* 161 */           for (drset.first(); drset.isRowPresent(); drset.next())
/*     */           {
/* 163 */             String ignoreTemplate = drset.getStringValue(fi[0].m_index);
/* 164 */             String ignoreArea = drset.getStringValue(fi[1].m_index);
/* 165 */             if ((!StringUtils.matchEx(sArg2, ignoreTemplate, true, true)) || (!StringUtils.matchEx(sArg1, ignoreArea, true, true))) {
/*     */               continue;
/*     */             }
/* 168 */             bResult = true;
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 175 */         throw new ServiceException(e);
/*     */       }
/*     */     case 2:
/* 181 */       oResult = CollaborationUtils.parseCollaborationName(sArg1);
/* 182 */       if (oResult == null)
/*     */       {
/* 184 */         oResult = ""; } break;
/*     */     case 3:
/* 191 */       if (nargs > 2)
/*     */       {
/* 193 */         throw new IllegalArgumentException(LocaleUtils.encodeMessage("csScriptEvalFunctionRequiresNumberArgs", null, function));
/*     */       }
/*     */ 
/* 196 */       boolean isRelative = false;
/* 197 */       boolean isIgnoreException = false;
/* 198 */       if (nargs > 0)
/*     */       {
/* 200 */         isRelative = StringUtils.convertToBool((String)args[0], false);
/*     */       }
/* 202 */       if (nargs > 1)
/*     */       {
/* 204 */         isIgnoreException = StringUtils.convertToBool((String)args[1], false);
/*     */       }
/*     */ 
/* 207 */       oResult = "";
/*     */       try
/*     */       {
/* 210 */         String procState = binder.get("dProcessingState", true);
/* 211 */         boolean isUrl = (!procState.equals("C")) && (!procState.equals("F"));
/* 212 */         if (isUrl)
/*     */         {
/* 215 */           boolean isAbsolute = !isRelative;
/* 216 */           if (isRelative)
/*     */           {
/* 220 */             isAbsolute = StringUtils.convertToBool(binder.getLocal("isAbsoluteWeb"), false);
/*     */           }
/*     */ 
/* 223 */           FileStoreProvider fs = FileStoreProviderLoader.initFileStore(context);
/*     */ 
/* 225 */           binder.putLocal("RenditionId", "webViewableFile");
/* 226 */           IdcFileDescriptor d = fs.createDescriptor(binder, null, context);
/* 227 */           HashMap fsArgs = new HashMap();
/* 228 */           fsArgs.put("useAbsolute", (isAbsolute) ? "1" : "0");
/* 229 */           oResult = fs.getClientURL(d, null, fsArgs, context);
/*     */         }
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 234 */         if (isIgnoreException)
/*     */         {
/* 236 */           oResult = "";
/*     */         }
/*     */         else
/*     */         {
/* 240 */           throw new ServiceException(e);
/*     */         }
/*     */       }
/* 243 */       break;
/*     */     case 4:
/* 247 */       if (nargs > 1)
/*     */       {
/* 249 */         msg = LocaleUtils.encodeMessage("csScriptEvalFunctionRequiresNumberArgs", null, function);
/* 250 */         throw new IllegalArgumentException(msg);
/*     */       }
/*     */       try
/*     */       {
/* 254 */         DataResultSet accessList = SecurityAccessListUtils.makeResultSetFromAccessListString(sArg1);
/* 255 */         oResult = accessList;
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 259 */         throw new ServiceException(e);
/*     */       }
/*     */     case 5:
/* 265 */       bResult = DocClassUtils.isSetInClass(sArg1, sArg2);
/* 266 */       break;
/*     */     default:
/* 270 */       return false;
/*     */     }
/*     */ 
/* 274 */     args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4], bResult, iResult, dResult, oResult);
/*     */ 
/* 278 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean evaluateValue(ScriptInfo info, boolean[] bVal, String[] sVal, ExecutionContext context, boolean isConditional)
/*     */     throws ServiceException
/*     */   {
/* 286 */     String msg = LocaleUtils.encodeMessage("csUnableToEvaluateNoBinder", null, info.m_key);
/*     */ 
/* 288 */     DataBinder binder = ScriptExtensionUtils.getBinder(context, msg);
/*     */ 
/* 290 */     int[] config = (int[])(int[])info.m_entry;
/* 291 */     sVal[0] = "0";
/* 292 */     bVal[0] = false;
/*     */ 
/* 294 */     switch (config[0])
/*     */     {
/*     */     case 0:
/*     */       try
/*     */       {
/* 299 */         String type = binder.getAllowMissing("dSubscriptionType");
/* 300 */         if (type == null)
/*     */         {
/* 302 */           type = binder.get("scpType");
/*     */         }
/*     */ 
/* 305 */         DataResultSet subsTypesTable = SharedObjects.getTable("SubscriptionTypes");
/*     */ 
/* 307 */         if (subsTypesTable == null)
/*     */         {
/* 310 */           return false;
/*     */         }
/*     */ 
/* 314 */         FieldInfo[] fi = ResultSetUtils.createInfoList(subsTypesTable, new String[] { "scpType", "scpFields", "scpDescription", "scpEnabled" }, true);
/*     */ 
/* 316 */         Vector v = subsTypesTable.findRow(0, type);
/*     */ 
/* 318 */         if (v == null)
/*     */         {
/* 321 */           return false;
/*     */         }
/*     */ 
/* 324 */         String fields = (String)v.elementAt(fi[1].m_index);
/* 325 */         String descript = (String)v.elementAt(fi[2].m_index);
/* 326 */         String enabled = (String)v.elementAt(fi[3].m_index);
/*     */ 
/* 328 */         binder.putLocal("scpType", type);
/* 329 */         binder.putLocal("scpDescription", descript);
/* 330 */         binder.putLocal("scpEnabled", (StringUtils.convertToBool(enabled, false)) ? "enabled" : "disabled");
/*     */ 
/* 332 */         Vector metaFields = StringUtils.parseArray(fields, ',', '^');
/*     */ 
/* 336 */         if (fields.equals("dDocName"))
/*     */         {
/* 338 */           bVal[0] = false;
/* 339 */           sVal[0] = "0";
/* 340 */           return true;
/*     */         }
/*     */ 
/* 343 */         String[] FIELDS = { "fieldName", "field", "value" };
/* 344 */         DataResultSet drset = new DataResultSet(FIELDS);
/*     */ 
/* 347 */         String values = binder.getActiveValue("dSubscriptionID");
/* 348 */         Vector metaValues = StringUtils.parseArray(values, ',', '^');
/*     */ 
/* 350 */         DocService service = getDocService(context, info);
/* 351 */         for (int i = 0; i < metaFields.size(); ++i)
/*     */         {
/* 353 */           String metaField = (String)metaFields.elementAt(i);
/* 354 */           metaField = metaField.trim();
/* 355 */           String tempField = service.getPresentationName(metaField);
/*     */ 
/* 357 */           Vector tempRow = drset.createEmptyRow();
/* 358 */           tempRow.setElementAt(metaField, 0);
/* 359 */           tempRow.setElementAt(tempField, 1);
/* 360 */           tempRow.setElementAt(metaValues.elementAt(i), 2);
/* 361 */           drset.addRow(tempRow);
/*     */         }
/*     */ 
/* 364 */         binder.addResultSet("CriteriaSubscriptionFields", drset);
/*     */ 
/* 366 */         bVal[0] = true;
/* 367 */         sVal[0] = "1";
/* 368 */         return true;
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 372 */         Report.warning(null, e, "csErrorBuildingPage", new Object[0]);
/* 373 */         return false;
/*     */       }
/*     */ 
/*     */     case 1:
/* 377 */       Report.trace(null, "Obsolete variable 'FILEPRESENT' used. ", null);
/*     */     case 2:
/*     */       try
/*     */       {
/* 382 */         String curRev = binder.getActiveValue("dID");
/* 383 */         String rev = binder.getLocal("dID");
/* 384 */         if ((rev != null) && (rev.length() > 0) && 
/* 386 */           (curRev.equals(rev)))
/*     */         {
/* 388 */           bVal[0] = true;
/* 389 */           sVal[0] = "1";
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 399 */         Report.warning(null, e, "csErrorBuildingPage", new Object[0]);
/*     */       }
/* 401 */       return true;
/*     */     case 3:
/* 404 */       return false;
/*     */     case 4:
/* 408 */       if (this.m_firstTimeIsEditVarObsolete)
/*     */       {
/* 410 */         Report.trace(null, "The variable is IsEditRev is obsolete, use IsWorkflow instead.", null);
/* 411 */         this.m_firstTimeIsEditVarObsolete = false;
/*     */       }
/* 413 */       bVal[0] = StringUtils.convertToBool(binder.getLocal("IsWorkflow"), false);
/* 414 */       sVal[0] = ((bVal[0] != 0) ? "1" : "0");
/* 415 */       return true;
/*     */     case 5:
/* 419 */       String curType = binder.getLocal("dDocType");
/* 420 */       String docType = binder.getActiveAllowMissing("dDocType");
/* 421 */       if ((docType != null) && (curType != null))
/*     */       {
/* 423 */         bVal[0] = docType.equals(curType);
/* 424 */         sVal[0] = ((bVal[0] != 0) ? "1" : "0");
/*     */       }
/* 426 */       return true;
/*     */     case 6:
/* 431 */       boolean result = false;
/* 432 */       Service service = ScriptExtensionUtils.getService(context);
/* 433 */       if ((service != null) && (service instanceof DocService))
/*     */       {
/* 435 */         DocService docService = (DocService)service;
/* 436 */         result = docService.hasOriginal();
/*     */       }
/* 438 */       bVal[0] = result;
/* 439 */       sVal[0] = ((bVal[0] != 0) ? "1" : "0");
/* 440 */       return true;
/*     */     }
/*     */ 
/* 443 */     return false;
/*     */   }
/*     */ 
/*     */   protected DocService getDocService(ExecutionContext context, ScriptInfo info)
/*     */     throws ServiceException
/*     */   {
/* 449 */     String msg = LocaleUtils.encodeMessage("csScriptMustBeInService", null, info.m_key, "DocService");
/*     */ 
/* 451 */     Service service = ScriptExtensionUtils.getService(context, msg);
/* 452 */     if (!service instanceof DocService)
/*     */     {
/* 454 */       msg = LocaleUtils.encodeMessage("csScriptMustBeInService", null, info.m_key, "DocService");
/*     */ 
/* 456 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 459 */     return (DocService)service;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 464 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100288 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.DocServiceScriptExtensions
 * JD-Core Version:    0.5.4
 */