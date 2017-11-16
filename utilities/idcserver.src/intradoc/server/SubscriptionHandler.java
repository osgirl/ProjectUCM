/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.SqlQueryData;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SubscriptionHandler extends ServiceHandler
/*     */ {
/*     */   public void init(Service service)
/*     */     throws ServiceException, DataException
/*     */   {
/*  43 */     super.init(service);
/*  44 */     service.m_binder.setFieldType("dSubscriptionCreateDate", "date");
/*  45 */     service.m_binder.setFieldType("dSubscriptionNotifyDate", "date");
/*  46 */     service.m_binder.setFieldType("dSubscriptionUsedDate", "date");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void checkAdminRights()
/*     */   {
/*  52 */     if ((this.m_service.getPrivilege() & 0x8) != 0) {
/*     */       return;
/*     */     }
/*  55 */     this.m_binder.removeLocal("dSubscriptionAlias");
/*  56 */     this.m_binder.removeLocal("whereClause");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getDocSubscriptionInfo()
/*     */     throws DataException, ServiceException
/*     */   {
/*  63 */     DataResultSet drset = SharedObjects.getTable("SubscriptionTypes");
/*  64 */     DataResultSet joinedTable = null;
/*  65 */     if (drset == null)
/*     */     {
/*  67 */       String msg = LocaleUtils.encodeMessage("csUnableToFindTable", null, "SubscriptionTypes");
/*     */ 
/*  69 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/*  72 */     if (this.m_currentAction.getNumParams() == 2)
/*     */     {
/*  74 */       joinedTable = new DataResultSet(new String[] { "dSubscriptionType", "scpFields", "scpDescription", "scpEnabled", "notSubscribed", "dSubscriptionCreateDate", "dSubscriptionNotifyDate", "dSubscriptionUsedDate", "dSubscriptionID", "dSubscriptionEmail" });
/*     */ 
/*  81 */       String scpTypesTable = this.m_currentAction.getParamAt(1);
/*  82 */       this.m_binder.addResultSet(scpTypesTable, joinedTable);
/*     */     }
/*     */ 
/*  85 */     String[][] table = ResultSetUtils.createStringTable(drset, new String[] { "scpType", "scpFields", "scpDescription", "scpEnabled" });
/*     */ 
/*  87 */     int docCount = 0;
/*  88 */     int sysCount = 0;
/*  89 */     String lastSubscriptionID = null;
/*     */ 
/*  92 */     String firstEnabledSubscriptionType = null;
/*  93 */     for (int i = 0; i < table.length; ++i)
/*     */     {
/*  95 */       String type = table[i][0];
/*  96 */       String fields = table[i][1];
/*  97 */       String descript = table[i][2];
/*  98 */       String enabled = table[i][3];
/*  99 */       String id = getSubscriptionFieldsValues(fields);
/* 100 */       boolean subscribed = false;
/* 101 */       this.m_binder.putLocal("dSubscriptionType", type);
/* 102 */       this.m_binder.putLocal("dSubscriptionAlias", this.m_binder.getLocal("dUser"));
/* 103 */       this.m_binder.putLocal("dSubscriptionID", id);
/*     */ 
/* 105 */       if (!StringUtils.convertToBool(enabled, false))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 110 */       firstEnabledSubscriptionType = table[i][0];
/* 111 */       ++sysCount;
/* 112 */       String query = this.m_currentAction.getParamAt(0);
/* 113 */       ResultSet rset = this.m_workspace.createResultSet(query, this.m_binder);
/*     */ 
/* 115 */       DataResultSet scpRset = new DataResultSet();
/* 116 */       if ((rset != null) && (!rset.isEmpty()))
/*     */       {
/* 118 */         lastSubscriptionID = ResultSetUtils.getValue(rset, "dSubscriptionID");
/* 119 */         this.m_service.setConditionVar("Subscribe:" + type, true);
/* 120 */         subscribed = true;
/* 121 */         ++docCount;
/* 122 */         scpRset.copy(rset);
/*     */       }
/*     */ 
/* 126 */       this.m_binder.putLocal("queryUsed", query);
/*     */       try
/*     */       {
/* 129 */         if (PluginFilters.filter("checkExtraSubscription", this.m_workspace, this.m_binder, this.m_service) != 0)
/*     */         {
/* 132 */           break label691:
/*     */         }
/*     */       }
/*     */       catch (DataException d)
/*     */       {
/* 137 */         throw new ServiceException(d);
/*     */       }
/* 139 */       String extraScpID = this.m_binder.getLocal("extraScpID");
/* 140 */       if ((extraScpID != null) && (!extraScpID.isEmpty()))
/*     */       {
/* 142 */         lastSubscriptionID = extraScpID;
/* 143 */         subscribed = true;
/* 144 */         ++docCount;
/* 145 */         rset = this.m_binder.getResultSet("extraScpRset");
/* 146 */         scpRset.copy(rset);
/*     */       }
/* 148 */       this.m_binder.removeResultSet("extraScpRset");
/* 149 */       this.m_binder.removeLocal("queryUsed");
/* 150 */       this.m_binder.removeLocal("extraScpID");
/* 151 */       this.m_binder.removeLocal("extraScpType");
/*     */ 
/* 153 */       if (joinedTable == null)
/*     */         continue;
/* 155 */       Vector row = new IdcVector();
/* 156 */       row.addElement(type);
/* 157 */       row.addElement(fields);
/* 158 */       row.addElement(descript);
/* 159 */       row.addElement(enabled);
/*     */ 
/* 161 */       if (subscribed)
/*     */       {
/* 163 */         row.addElement("0");
/* 164 */         row.addElement(ResultSetUtils.getValue(scpRset, "dSubscriptionCreateDate"));
/* 165 */         row.addElement(ResultSetUtils.getValue(scpRset, "dSubscriptionNotifyDate"));
/* 166 */         row.addElement(ResultSetUtils.getValue(scpRset, "dSubscriptionUsedDate"));
/* 167 */         row.addElement(ResultSetUtils.getValue(scpRset, "dSubscriptionID"));
/* 168 */         row.addElement(ResultSetUtils.getValue(scpRset, "dSubscriptionEmail"));
/*     */       }
/*     */       else
/*     */       {
/* 172 */         row.addElement("1");
/* 173 */         row.addElement("");
/* 174 */         row.addElement("");
/* 175 */         row.addElement("");
/* 176 */         row.addElement(id);
/* 177 */         row.addElement("");
/*     */       }
/*     */ 
/* 180 */       label691: joinedTable.addRow(row);
/*     */     }
/*     */ 
/* 186 */     if (docCount > 0)
/*     */     {
/* 188 */       this.m_service.setConditionVar("DocHasSubscription", true);
/* 189 */       if (docCount == 1)
/*     */       {
/* 191 */         this.m_binder.putLocal("dSubscriptionID", lastSubscriptionID);
/*     */       }
/*     */       else
/*     */       {
/* 195 */         this.m_service.setConditionVar("DocHasMultipleSubscriptions", true);
/*     */       }
/*     */     }
/*     */ 
/* 199 */     if (sysCount <= 0)
/*     */       return;
/* 201 */     this.m_service.setConditionVar("SysHasSubscriptionTypes", true);
/* 202 */     if (sysCount == 1)
/*     */     {
/* 204 */       this.m_binder.putLocal("dSubscriptionType", firstEnabledSubscriptionType);
/*     */     }
/*     */     else
/*     */     {
/* 208 */       this.m_service.setConditionVar("SysHasMultipleSubscriptionTypes", true);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addSubscription()
/*     */     throws DataException, ServiceException
/*     */   {
/* 216 */     String type = this.m_binder.getLocal("dSubscriptionType");
/*     */ 
/* 218 */     if ((type == null) || (type.length() == 0))
/*     */     {
/* 220 */       type = "Basic";
/*     */     }
/*     */ 
/* 223 */     Date dte = new Date();
/* 224 */     String dateString = LocaleResources.localizeDate(dte, this.m_service);
/* 225 */     this.m_binder.putLocal("dSubscriptionCreateDate", dateString);
/*     */ 
/* 227 */     DataResultSet drset = SharedObjects.getTable("SubscriptionTypes");
/* 228 */     if (drset == null)
/*     */     {
/* 230 */       String msg = LocaleUtils.encodeMessage("csUnableToFindTable", null, "SubscriptionTypes");
/*     */ 
/* 232 */       throw new ServiceException(msg);
/*     */     }
/* 234 */     if (drset.getNumRows() == 0)
/*     */     {
/* 236 */       throw new ServiceException("!csNoSubscriptionTypes");
/*     */     }
/* 238 */     Vector v = drset.findRow(0, type);
/* 239 */     if (v == null)
/*     */     {
/* 241 */       String msg = LocaleUtils.encodeMessage("csSubscriptionTypeDoesNotExist", null, type);
/*     */ 
/* 243 */       throw new ServiceException(msg);
/*     */     }
/* 245 */     String fields = (String)v.elementAt(1);
/*     */ 
/* 247 */     this.m_binder.putLocal("dSubscriptionType", type);
/*     */ 
/* 249 */     String id = getSubscriptionFieldsValues(fields);
/* 250 */     if (id.trim().length() == 0)
/*     */     {
/* 252 */       this.m_service.createServiceException(null, "!csSubscriptionRequiredCriteria");
/*     */     }
/* 254 */     this.m_binder.putLocal("dSubscriptionID", getSubscriptionFieldsValues(fields));
/*     */ 
/* 256 */     this.m_binder.putLocal("dSubscriptionAlias", this.m_binder.getLocal("dUser"));
/* 257 */     this.m_binder.putLocal("dSubscriptionAliasType", "user");
/*     */ 
/* 259 */     ResultSet rset = this.m_workspace.createResultSet("QuserSubscription", this.m_binder);
/* 260 */     if (rset.isRowPresent())
/*     */     {
/* 262 */       rset = null;
/* 263 */       throw new ServiceException("!csUserAlreadySubscribed");
/*     */     }
/*     */ 
/* 266 */     String email = this.m_binder.getLocal("dSubscriptionEmail");
/*     */ 
/* 269 */     this.m_binder.removeLocal("isNoSubscriptionNotificationSent");
/* 270 */     if ((email == null) || (email.trim().length() == 0))
/*     */     {
/* 272 */       UserData userData = this.m_service.getUserData();
/* 273 */       if (userData != null)
/*     */       {
/* 275 */         String profileEmail = userData.getProperty("dEmail");
/* 276 */         if ((profileEmail == null) || (profileEmail.length() == 0))
/*     */         {
/* 278 */           this.m_binder.putLocal("isNoSubscriptionNotificationSent", "1");
/*     */         }
/*     */       }
/* 281 */       if ((email == null) || (email.length() > 0))
/*     */       {
/* 283 */         email = "";
/* 284 */         this.m_binder.putLocal("dSubscriptionEmail", email);
/*     */       }
/*     */     }
/* 287 */     if ((email != null) && (email.length() > 0))
/*     */     {
/* 289 */       Workspace ws = WorkspaceUtils.getWorkspace("user");
/* 290 */       rset = ws.createResultSet("Quser", this.m_binder);
/* 291 */       if ((rset != null) && (rset.isRowPresent()))
/*     */       {
/* 293 */         ws.execute("UuserEmail", this.m_binder);
/* 294 */         UserData userData = this.m_service.getUserData();
/* 295 */         UserStorage.removeCachedUserData(userData.m_name);
/* 296 */         userData.setProperty("dEmail", email);
/* 297 */         this.m_service.setUserData(userData);
/* 298 */         this.m_binder.putLocal("dSubscriptionEmail", email);
/* 299 */         UserData localUserData = UserStorage.getUserData(userData.m_name);
/* 300 */         if (localUserData != null)
/*     */         {
/* 303 */           SubjectManager.notifyChanged("userlist");
/* 304 */           localUserData.setProperty("dEmail", email);
/*     */         }
/*     */       }
/*     */     }
/* 308 */     String query = this.m_currentAction.getParamAt(0);
/* 309 */     this.m_workspace.execute(query, this.m_binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void removeSubscription() throws DataException, ServiceException
/*     */   {
/* 315 */     checkAdminRights();
/*     */ 
/* 317 */     String type = this.m_binder.getLocal("dSubscriptionType");
/* 318 */     String id = this.m_binder.getLocal("dSubscriptionID");
/* 319 */     String aliasType = this.m_binder.getLocal("dSubscriptionAliasType");
/* 320 */     String alias = this.m_binder.getLocal("dSubscriptionAlias");
/*     */ 
/* 323 */     if ((type == null) || (type.length() == 0))
/*     */     {
/* 325 */       type = "Basic";
/*     */     }
/*     */ 
/* 328 */     if ((id == null) || (id.length() == 0))
/*     */     {
/* 330 */       id = "dDocName";
/*     */     }
/*     */ 
/* 333 */     if ((aliasType == null) || (aliasType.length() == 0))
/*     */     {
/* 335 */       aliasType = "user";
/*     */     }
/*     */ 
/* 338 */     if ((alias == null) || (alias.length() == 0))
/*     */     {
/* 340 */       alias = this.m_binder.getLocal("dUser");
/*     */     }
/*     */ 
/* 343 */     Properties props = new Properties();
/* 344 */     props.put("dSubscriptionAliasType", aliasType);
/* 345 */     props.put("dSubscriptionAlias", alias);
/* 346 */     props.put("dSubscriptionType", type);
/* 347 */     props.put("dSubscriptionID", id);
/* 348 */     PropParameters params = new PropParameters(props);
/*     */ 
/* 350 */     String query = this.m_currentAction.getParamAt(0);
/* 351 */     this.m_workspace.execute(query, params);
/*     */ 
/* 354 */     this.m_binder.removeLocal("dSubscriptionType");
/* 355 */     this.m_binder.removeLocal("dSubscriptionID");
/*     */   }
/*     */ 
/*     */   protected String getSubscriptionFieldsValues(String fields)
/*     */     throws ServiceException, DataException
/*     */   {
/* 362 */     int maxLen = SharedObjects.getEnvironmentInt("MemoFieldSize", 2000);
/* 363 */     Vector subFields = StringUtils.parseArray(fields, ',', '^');
/* 364 */     int size = subFields.size();
/* 365 */     Vector v = new IdcVector();
/*     */ 
/* 367 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 369 */       String temp = (String)subFields.elementAt(i);
/* 370 */       temp = temp.trim();
/*     */ 
/* 372 */       if ((temp == null) || (temp.length() == 0))
/*     */       {
/* 374 */         throw new ServiceException("!csSubscriptionInvalidFieldName");
/*     */       }
/*     */ 
/* 377 */       String tempField = null;
/* 378 */       if (temp.equalsIgnoreCase("fParentGUID"))
/*     */       {
/* 380 */         tempField = this.m_binder.getAllowMissing(temp);
/* 381 */         if (tempField != null)
/*     */         {
/* 383 */           tempField = tempField.trim();
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 388 */         tempField = this.m_binder.get(temp);
/* 389 */         tempField = tempField.trim();
/*     */       }
/*     */ 
/* 392 */       if ((tempField == null) || (tempField.length() == 0))
/*     */       {
/* 394 */         tempField = " ";
/*     */       }
/*     */ 
/* 397 */       v.addElement(tempField);
/*     */     }
/*     */ 
/* 400 */     String values = StringUtils.createString(v, ',', '^');
/*     */ 
/* 402 */     if (values.length() > maxLen)
/*     */     {
/* 404 */       String msg = LocaleUtils.encodeMessage("csSubscriptionTooManyFields", null, Integer.valueOf(maxLen));
/*     */ 
/* 406 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 409 */     return values;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteDocumentSubscription()
/*     */     throws DataException, ServiceException
/*     */   {
/* 421 */     boolean isDelete = false;
/* 422 */     String action = this.m_currentAction.getParamAt(0);
/* 423 */     boolean isDeleteDoc = false;
/* 424 */     if (action.equals("deleteDoc"))
/*     */     {
/* 426 */       isDeleteDoc = true;
/* 427 */       isDelete = true;
/*     */     }
/* 429 */     else if (action.equals("deleteRev"))
/*     */     {
/* 431 */       ResultSet rset = this.m_workspace.createResultSet("QdocNameCheckRevClassID", this.m_binder);
/* 432 */       isDelete = rset.isEmpty();
/*     */     }
/*     */ 
/* 435 */     if (isDelete)
/*     */     {
/* 437 */       this.m_binder.putLocal("dSubscriptionType", "Basic");
/* 438 */       this.m_workspace.execute("DdocSubscription", this.m_binder);
/*     */     }
/*     */ 
/* 441 */     if (this.m_currentAction.getNumParams() <= 1)
/*     */       return;
/* 443 */     String rsetName = this.m_currentAction.getParamAt(1);
/* 444 */     ResultSet rset = this.m_binder.getResultSet(rsetName);
/* 445 */     if (rset == null)
/*     */       return;
/* 447 */     if (isDelete)
/*     */     {
/* 449 */       this.m_binder.putLocal("isAllRevisionsDeleted", "1");
/*     */     }
/* 451 */     if (isDeleteDoc)
/*     */     {
/* 455 */       rset.first();
/*     */     }
/* 457 */     ServiceExtensionUtils.executeDocDeleteSideEffect(rset, this.m_binder, this.m_service);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getSubscriptionList()
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 468 */       String query = this.m_currentAction.getParamAt(0);
/* 469 */       ResultSet rset = this.m_workspace.createResultSet(query, this.m_binder);
/*     */ 
/* 471 */       DataResultSet subList = new DataResultSet();
/* 472 */       subList.copy(rset);
/*     */ 
/* 475 */       query = this.m_currentAction.getParamAt(1);
/* 476 */       rset = this.m_workspace.createResultSet(query, this.m_binder);
/*     */ 
/* 478 */       subList.merge(null, rset, false);
/*     */ 
/* 480 */       this.m_binder.addResultSet("SUBSCRIPTION_LIST", subList);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 484 */       this.m_service.createServiceException(e, "!csSubscriptionCantBuildList");
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void prepSubscription() throws DataException, ServiceException
/*     */   {
/* 491 */     checkAdminRights();
/*     */ 
/* 493 */     String alias = this.m_binder.getAllowMissing("dSubscriptionAlias");
/* 494 */     String userOrAlias = this.m_binder.getAllowMissing("dSubscriptionAliasType");
/* 495 */     String scpType = this.m_binder.get("dSubscriptionType");
/* 496 */     if (alias == null)
/*     */     {
/* 498 */       alias = this.m_binder.get("dUser");
/* 499 */       userOrAlias = "user";
/* 500 */       this.m_binder.putLocal("dSubscriptionAlias", alias);
/* 501 */       this.m_binder.putLocal("dSubscriptionAliasType", userOrAlias);
/*     */     }
/*     */ 
/* 504 */     DataResultSet scpTypes = SharedObjects.getTable("SubscriptionTypes");
/* 505 */     Vector row = scpTypes.findRow(0, scpType);
/* 506 */     if (row == null)
/*     */     {
/* 508 */       String msg = LocaleUtils.encodeMessage("csSubscriptionTypeDoesNotExist", null, scpType);
/*     */ 
/* 510 */       this.m_service.createServiceException(null, msg);
/*     */     }
/* 512 */     String descript = (String)row.elementAt(2);
/* 513 */     String enabled = (String)row.elementAt(3);
/* 514 */     this.m_binder.putLocal("scpDescription", descript);
/* 515 */     this.m_binder.putLocal("scpEnabled", (StringUtils.convertToBool(enabled, false)) ? "enabled" : "disabled");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getDocumentList()
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 524 */       checkAdminRights();
/*     */ 
/* 526 */       String type = this.m_binder.getLocal("dSubscriptionType");
/*     */ 
/* 528 */       if ((type == null) || (type.length() == 0))
/*     */       {
/* 530 */         throw new ServiceException("!csSubscriptionTypeNotSpecified");
/*     */       }
/*     */ 
/* 533 */       IdcStringBuilder whereClause = new IdcStringBuilder();
/* 534 */       SqlQueryData queryData = new SqlQueryData();
/*     */ 
/* 537 */       ResultSet rset = this.m_workspace.createResultSetSQL("SELECT * FROM Documents, Revisions, DocMeta WHERE Documents.dID=0 AND Revisions.dID=0 AND DocMeta.dID=0");
/*     */ 
/* 539 */       DataResultSet fieldInfos = new DataResultSet();
/* 540 */       fieldInfos.copyFieldInfo(rset);
/*     */ 
/* 543 */       String[] FIELD_INFO = { "scpType", "scpFields" };
/* 544 */       DataResultSet subsTypesTable = SharedObjects.getTable("SubscriptionTypes");
/*     */ 
/* 546 */       FieldInfo[] fi = ResultSetUtils.createInfoList(subsTypesTable, FIELD_INFO, true);
/* 547 */       Vector v = subsTypesTable.findRow(0, type);
/* 548 */       String fields = (String)v.elementAt(fi[1].m_index);
/* 549 */       Vector metaFields = StringUtils.parseArray(fields, ',', '^');
/*     */ 
/* 551 */       String values = this.m_binder.getLocal("dSubscriptionID");
/* 552 */       Vector metaValues = StringUtils.parseArray(values, ',', '^');
/*     */ 
/* 554 */       int size = metaFields.size();
/* 555 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 557 */         String field = (String)metaFields.elementAt(i);
/* 558 */         FieldInfo finfo = new FieldInfo();
/* 559 */         fieldInfos.getFieldInfo(field, finfo);
/*     */ 
/* 561 */         if (field.startsWith("x"))
/*     */         {
/* 563 */           field = "DocMeta." + field.trim();
/*     */         }
/*     */         else
/*     */         {
/* 567 */           field = "Revisions." + field.trim();
/*     */         }
/*     */ 
/* 570 */         if (whereClause.length() > 0)
/*     */         {
/* 572 */           whereClause.append(" AND ");
/*     */         }
/*     */ 
/* 575 */         String op = null;
/* 576 */         switch (finfo.m_type)
/*     */         {
/*     */         case 3:
/*     */         case 4:
/* 580 */           op = "numberEquals";
/* 581 */           break;
/*     */         default:
/* 583 */           op = "sqlEq";
/*     */         }
/*     */ 
/* 586 */         String val = "";
/* 587 */         if (i < metaValues.size())
/*     */         {
/* 589 */           val = (String)metaValues.elementAt(i);
/*     */         }
/*     */ 
/* 592 */         queryData.appendClause(field, whereClause, op, val);
/*     */       }
/*     */ 
/* 595 */       String extraWhereClause = this.m_binder.getLocal("whereClause");
/* 596 */       if ((extraWhereClause != null) && (extraWhereClause.length() > 0))
/*     */       {
/* 598 */         if (extraWhereClause.toLowerCase().indexOf("union") >= 0)
/*     */         {
/* 600 */           this.m_service.createServiceException(null, "Bad extra where clause.");
/*     */         }
/* 602 */         whereClause.append(" AND " + extraWhereClause);
/*     */       }
/* 604 */       this.m_binder.putLocal("dataSource", "DocSubscriptionList");
/* 605 */       this.m_binder.putLocal("whereClause", whereClause.toString());
/* 606 */       this.m_binder.putLocal("resultName", "DOCUMENT_LIST");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 610 */       this.m_service.createServiceException(e, "!csSubscriptionUnableToBuildList");
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateSubscriptionType() throws DataException, ServiceException
/*     */   {
/* 617 */     String dir = DirectoryLocator.getAppDataDirectory() + "subscription/";
/* 618 */     String scpType = this.m_binder.get("scpType");
/* 619 */     boolean deleteRows = false;
/*     */     try
/*     */     {
/* 622 */       String scpDescription = this.m_binder.getAllowMissing("scpDescription");
/* 623 */       String scpFields = this.m_binder.getAllowMissing("scpFields");
/* 624 */       String enabled = this.m_binder.getAllowMissing("scpEnabled");
/*     */ 
/* 626 */       DataBinder binder = new DataBinder();
/*     */       DataResultSet drset;
/*     */       DataResultSet drset;
/* 628 */       if (ResourceUtils.serializeDataBinder(dir, "subscription_types.hda", binder, false, false))
/*     */       {
/* 630 */         drset = (DataResultSet)binder.getResultSet("SubscriptionTypes");
/*     */       }
/*     */       else
/*     */       {
/* 635 */         drset = SharedObjects.getTable("SubscriptionTypes");
/* 636 */         binder.addResultSet("SubscriptionTypes", drset);
/*     */       }
/*     */ 
/* 639 */       Vector values = drset.findRow(0, scpType);
/* 640 */       if (values == null)
/*     */       {
/* 642 */         String msg = LocaleUtils.encodeMessage("csSubscriptionTypeDoesNotExist", null, scpType);
/*     */ 
/* 644 */         this.m_service.createServiceException(null, msg);
/*     */       }
/*     */ 
/* 648 */       if (scpDescription != null)
/*     */       {
/* 650 */         values.setElementAt(scpDescription, 2);
/*     */       }
/* 652 */       if (enabled != null)
/*     */       {
/* 654 */         values.setElementAt(enabled, 3);
/*     */       }
/* 656 */       if ((scpFields != null) && (!scpFields.equals(values.elementAt(1))))
/*     */       {
/* 658 */         deleteRows = true;
/* 659 */         values.setElementAt(scpFields, 1);
/*     */       }
/*     */ 
/* 662 */       ResourceUtils.serializeDataBinder(dir, "subscription_types.hda", binder, true, false);
/*     */ 
/* 664 */       SharedObjects.putTable("SubscriptionTypes", drset);
/*     */     }
/*     */     finally
/*     */     {
/* 668 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */ 
/* 671 */     if (!deleteRows) {
/*     */       return;
/*     */     }
/*     */ 
/* 675 */     this.m_workspace.execute("DsubscriptionType", this.m_binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void createSubscriptionType()
/*     */     throws DataException, ServiceException
/*     */   {
/* 682 */     String dir = DirectoryLocator.getAppDataDirectory() + "subscription/";
/* 683 */     FileUtils.reserveDirectory(dir);
/*     */ 
/* 685 */     String scpType = this.m_binder.get("scpType");
/* 686 */     String scpDescription = this.m_binder.getAllowMissing("scpDescription");
/* 687 */     String scpFields = this.m_binder.getAllowMissing("scpFields");
/* 688 */     String scpEnabled = this.m_binder.getAllowMissing("scpEnabled");
/*     */     try
/*     */     {
/* 691 */       if (scpType.contains(" "))
/*     */       {
/* 693 */         String msg = LocaleUtils.encodeMessage("syValidationMsgHasSpaces", null, "subscription type");
/*     */ 
/* 695 */         this.m_service.createServiceException(null, msg);
/*     */       }
/*     */ 
/* 698 */       DataBinder binder = new DataBinder();
/*     */       DataResultSet drset;
/*     */       DataResultSet drset;
/* 700 */       if (ResourceUtils.serializeDataBinder(dir, "subscription_types.hda", binder, false, false))
/*     */       {
/* 702 */         drset = (DataResultSet)binder.getResultSet("SubscriptionTypes");
/*     */       }
/*     */       else
/*     */       {
/* 707 */         drset = SharedObjects.getTable("SubscriptionTypes");
/* 708 */         binder.addResultSet("SubscriptionTypes", drset);
/*     */       }
/*     */ 
/* 712 */       Vector row = drset.findRow(0, scpType);
/* 713 */       if (row != null)
/*     */       {
/* 715 */         String msg = LocaleUtils.encodeMessage("csSubscriptionTypeExists", null, scpType);
/*     */ 
/* 717 */         this.m_service.createServiceException(null, msg);
/*     */       }
/*     */ 
/* 720 */       row = new IdcVector();
/* 721 */       row.addElement(scpType);
/* 722 */       row.addElement(scpFields);
/* 723 */       row.addElement(scpDescription);
/* 724 */       row.addElement(scpEnabled);
/* 725 */       drset.addRow(row);
/*     */ 
/* 727 */       ResourceUtils.serializeDataBinder(dir, "subscription_types.hda", binder, true, false);
/*     */ 
/* 729 */       SharedObjects.putTable("SubscriptionTypes", drset);
/*     */     }
/*     */     finally
/*     */     {
/* 733 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteSubscriptionType() throws DataException, ServiceException
/*     */   {
/* 740 */     String dir = DirectoryLocator.getAppDataDirectory() + "subscription/";
/* 741 */     FileUtils.reserveDirectory(dir);
/*     */     try
/*     */     {
/* 745 */       String scpType = this.m_binder.get("scpType");
/* 746 */       if (scpType.equals("basic"))
/*     */       {
/* 748 */         this.m_service.createServiceException(null, "!csSubscriptionBasicCannotBeDeleted");
/*     */       }
/*     */ 
/* 752 */       DataBinder binder = new DataBinder();
/*     */       DataResultSet drset;
/*     */       DataResultSet drset;
/* 754 */       if (ResourceUtils.serializeDataBinder(dir, "subscription_types.hda", binder, false, false))
/*     */       {
/* 756 */         drset = (DataResultSet)binder.getResultSet("SubscriptionTypes");
/*     */       }
/*     */       else
/*     */       {
/* 761 */         drset = SharedObjects.getTable("SubscriptionTypes");
/* 762 */         binder.addResultSet("SubscriptionTypes", drset);
/*     */       }
/*     */ 
/* 765 */       Vector values = drset.findRow(0, scpType);
/* 766 */       if (values != null)
/*     */       {
/* 768 */         drset.deleteCurrentRow();
/*     */ 
/* 770 */         ResourceUtils.serializeDataBinder(dir, "subscription_types.hda", binder, true, false);
/*     */ 
/* 772 */         SharedObjects.putTable("SubscriptionTypes", drset);
/*     */       }
/*     */       else
/*     */       {
/* 776 */         String msg = LocaleUtils.encodeMessage("csSubscriptionTypeDoesNotExist", null, scpType);
/*     */ 
/* 778 */         this.m_service.createServiceException(null, msg);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 783 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */ 
/* 788 */     this.m_workspace.execute("DsubscriptionType", this.m_binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void computeDocSubscribers() throws DataException, ServiceException
/*     */   {
/* 794 */     checkAdminRights();
/*     */     try
/*     */     {
/* 800 */       if (PluginFilters.filter("preComputeDocSubscribers", this.m_workspace, this.m_binder, this.m_service) != 0)
/*     */       {
/* 803 */         return;
/*     */       }
/*     */     }
/*     */     catch (DataException d)
/*     */     {
/* 808 */       throw new ServiceException(d);
/*     */     }
/*     */ 
/* 811 */     DataResultSet scpTypes = SharedObjects.getTable("SubscriptionTypes");
/* 812 */     Vector searchData = new IdcVector();
/* 813 */     Vector scpUsedTypes = new IdcVector();
/* 814 */     String extraWhereClause = this.m_binder.getLocal("whereClause");
/* 815 */     if ((extraWhereClause == null) || (extraWhereClause.length() == 0))
/*     */     {
/* 817 */       extraWhereClause = "";
/*     */     }
/*     */     else
/*     */     {
/* 821 */       extraWhereClause = "AND " + extraWhereClause;
/*     */     }
/*     */ 
/* 824 */     for (scpTypes.first(); scpTypes.isRowPresent(); scpTypes.next())
/*     */     {
/* 826 */       Properties scpProps = scpTypes.getCurrentRowProps();
/* 827 */       String scpType = (String)scpProps.get("scpType");
/* 828 */       String fieldList = (String)scpProps.get("scpFields");
/* 829 */       Vector fields = StringUtils.parseArray(fieldList, ',', '^');
/* 830 */       int length = fields.size();
/* 831 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 833 */         String s = this.m_binder.getLocal((String)fields.elementAt(i));
/* 834 */         fields.setElementAt(s, i);
/*     */       }
/* 836 */       String searchString = null;
/*     */ 
/* 839 */       if (scpType.equals("Folder"))
/*     */       {
/* 841 */         searchString = StringUtils.createString(fields, ',', ',');
/*     */       }
/*     */       else
/*     */       {
/* 845 */         searchString = StringUtils.createString(fields, ',', '^');
/*     */       }
/* 847 */       searchData.addElement(new String[] { scpType, searchString });
/*     */     }
/*     */ 
/* 850 */     DataResultSet drset = new DataResultSet();
/* 851 */     int length = searchData.size();
/* 852 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 854 */       String[] info = (String[])(String[])searchData.elementAt(i);
/* 855 */       String type = info[0];
/* 856 */       String id = info[1];
/*     */ 
/* 858 */       IdcStringBuilder whereClause = new IdcStringBuilder();
/* 859 */       whereClause.append("dSubscriptionType = '");
/* 860 */       whereClause.append(StringUtils.createQuotableString(type));
/* 861 */       whereClause.append("' AND dSubscriptionID in ('");
/*     */ 
/* 864 */       if (type.equals("Folder"))
/*     */       {
/* 866 */         whereClause.append(id);
/*     */       }
/*     */       else
/*     */       {
/* 870 */         whereClause.append(StringUtils.createQuotableString(id));
/*     */       }
/* 872 */       whereClause.append("') ");
/* 873 */       whereClause.append(extraWhereClause);
/*     */ 
/* 875 */       this.m_binder.putLocal("dataSource", "Subscriptions");
/* 876 */       this.m_binder.putLocal("whereClause", whereClause.toString());
/* 877 */       this.m_binder.putLocal("resultName", "TmpSubscriptions");
/* 878 */       this.m_binder.putLocal("MaxQueryRows", "" + SharedObjects.getEnvironmentInt("MaxStandardDatabaseResults", 500));
/* 879 */       this.m_service.createResultSetSQL();
/* 880 */       DataResultSet rset = (DataResultSet)this.m_binder.getResultSet("TmpSubscriptions");
/* 881 */       this.m_binder.removeResultSet("TmpSubscriptions");
/*     */ 
/* 883 */       if (i == 0)
/*     */       {
/* 885 */         drset.copyFieldInfo(rset);
/*     */       }
/* 887 */       if ((!rset.first()) || (!rset.isRowPresent()))
/*     */         continue;
/* 889 */       drset.merge(null, rset, false);
/* 890 */       scpUsedTypes.addElement(type);
/*     */     }
/*     */ 
/* 894 */     DataResultSet drset2 = new DataResultSet();
/* 895 */     drset2.copyFieldInfo(drset);
/* 896 */     drset2.copy(drset, SharedObjects.getEnvironmentInt("MaxStandardDatabaseResults", 500));
/* 897 */     this.m_binder.addResultSet("SUBSCRIPTIONS", drset2);
/* 898 */     this.m_binder.putLocal("scpTypes", StringUtils.createString(scpUsedTypes, ',', '^'));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void prepSubscriptionDateUpdate() throws ServiceException
/*     */   {
/* 904 */     checkAdminRights();
/*     */ 
/* 906 */     String alias = this.m_binder.getLocal("dSubscriptionAlias");
/* 907 */     if (alias == null)
/*     */     {
/* 910 */       this.m_binder.putLocal("dSubscriptionAlias", this.m_binder.getLocal("dUser"));
/* 911 */       this.m_binder.putLocal("dSubscriptionAliasType", "user");
/*     */     }
/* 913 */     this.m_service.setRedirectUrl(this.m_binder.getLocal("RedirectUrl"));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void setDateToPresent()
/*     */     throws DataException, ServiceException
/*     */   {
/* 922 */     String fieldName = this.m_currentAction.getParamAt(0);
/*     */ 
/* 924 */     Date dte = new Date();
/* 925 */     String dateString = LocaleResources.localizeDate(dte, this.m_service);
/* 926 */     this.m_binder.putLocal(fieldName, dateString);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 931 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100003 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SubscriptionHandler
 * JD-Core Version:    0.5.4
 */