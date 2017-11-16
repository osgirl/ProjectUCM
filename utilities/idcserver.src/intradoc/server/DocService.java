/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.shared.CollaborationUtils;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.ViewFieldDef;
/*      */ import intradoc.shared.ViewFields;
/*      */ import intradoc.shared.schema.SchemaFieldConfig;
/*      */ import intradoc.shared.schema.SchemaFieldData;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import intradoc.shared.schema.SchemaRelationData;
/*      */ import intradoc.shared.schema.SchemaTreePointer;
/*      */ import intradoc.shared.schema.SchemaViewData;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ import java.util.regex.Matcher;
/*      */ import java.util.regex.Pattern;
/*      */ 
/*      */ public class DocService extends Service
/*      */ {
/*      */   public static final int m_metadataStartingOrder = 5000;
/*   43 */   protected String m_resultSetWithComputedDocInfo = null;
/*   44 */   protected Vector m_docFieldInfos = null;
/*      */ 
/*      */   public void init(Workspace ws, OutputStream output, DataBinder binder, ServiceData serviceData)
/*      */     throws DataException
/*      */   {
/*   55 */     super.init(ws, output, binder, serviceData);
/*      */   }
/*      */ 
/*      */   public void createHandlersForService()
/*      */     throws ServiceException, DataException
/*      */   {
/*   61 */     super.createHandlersForService();
/*   62 */     createHandlers("DocService");
/*      */   }
/*      */ 
/*      */   public boolean hasOriginal()
/*      */   {
/*   67 */     boolean retVal = false;
/*      */ 
/*   69 */     String isAllowed = this.m_conditionVars.getProperty("HasOriginal");
/*   70 */     if (isAllowed != null)
/*      */     {
/*   72 */       retVal = StringUtils.convertToBool(isAllowed, false);
/*      */     }
/*      */     else
/*      */     {
/*   77 */       String origName = this.m_binder.getAllowMissing("dOriginalName");
/*   78 */       retVal = (origName != null) && (origName.trim().length() > 0);
/*      */     }
/*      */ 
/*   81 */     return retVal;
/*      */   }
/*      */ 
/*      */   public boolean allowCheckin()
/*      */   {
/*   86 */     boolean rc = false;
/*   87 */     String isAllowed = this.m_conditionVars.getProperty("AllowCheckin");
/*   88 */     rc = false;
/*   89 */     if (isAllowed != null)
/*      */     {
/*   91 */       rc = StringUtils.convertToBool(isAllowed, false);
/*      */     }
/*      */     try
/*      */     {
/*   95 */       rc = canCheckin();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*   99 */       e.printStackTrace();
/*      */     }
/*  101 */     return rc;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkCanCreateDocSecurity() throws DataException, ServiceException
/*      */   {
/*  107 */     boolean isWorkflowRev = StringUtils.convertToBool(this.m_binder.getLocal("IsWorkflow"), false);
/*  108 */     if (isWorkflowRev)
/*      */     {
/*  110 */       this.m_serviceData.m_accessLevel = 1;
/*      */     }
/*      */ 
/*  113 */     if ((SecurityUtils.m_useCollaboration) && 
/*  116 */       (this.m_currentAction.getNumParams() > 0))
/*      */     {
/*  118 */       String rsetName = this.m_currentAction.getParamAt(0);
/*  119 */       ResultSet rset = this.m_binder.getResultSet(rsetName);
/*  120 */       String acct = ResultSetUtils.getValue(rset, "dDocAccount");
/*  121 */       if (acct != null)
/*      */       {
/*  123 */         String clbra = CollaborationUtils.parseCollaborationName(acct);
/*  124 */         if ((clbra != null) && (clbra.length() > 0))
/*      */         {
/*  126 */           Vector clbras = new IdcVector();
/*  127 */           clbras.addElement(clbra);
/*  128 */           setCachedObject("CurrentCollaborations", clbras);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  133 */     checkSecurity();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeDocInfoInHtmlPage()
/*      */     throws DataException, ServiceException
/*      */   {
/*  140 */     this.m_binder.putLocal("ForceDocInfoWebAddressToMatchHref", "1");
/*  141 */     this.m_resultSetWithComputedDocInfo = this.m_currentAction.getParamAt(0);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeURLs() throws DataException, ServiceException
/*      */   {
/*  147 */     computeDocInfoInHtmlPage();
/*  148 */     setConditionVar("computeActions", false);
/*      */   }
/*      */ 
/*      */   protected void addDocumentFields() throws DataException, ServiceException
/*      */   {
/*  153 */     DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/*      */ 
/*  156 */     ViewFields columnFields = new ViewFields(this);
/*  157 */     columnFields.addStandardDocFields();
/*  158 */     columnFields.addMetaFields(drset);
/*  159 */     this.m_docFieldInfos = columnFields.m_viewFields;
/*      */   }
/*      */ 
/*      */   public String getPresentationName(String name) throws DataException, ServiceException
/*      */   {
/*  164 */     if (this.m_docFieldInfos == null)
/*      */     {
/*  166 */       addDocumentFields();
/*      */     }
/*      */ 
/*  169 */     for (int i = 0; i < this.m_docFieldInfos.size(); ++i)
/*      */     {
/*  171 */       ViewFieldDef docDef = (ViewFieldDef)this.m_docFieldInfos.elementAt(i);
/*      */ 
/*  173 */       if (docDef.m_name.equals(name))
/*      */       {
/*  175 */         return docDef.m_caption;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  180 */     return name;
/*      */   }
/*      */ 
/*      */   public void notifyNextRow(String rsetName, boolean hasNext)
/*      */     throws IOException
/*      */   {
/*  186 */     if ((hasNext) && 
/*  189 */       (this.m_resultSetWithComputedDocInfo != null) && (this.m_resultSetWithComputedDocInfo.equals(rsetName)))
/*      */     {
/*  192 */       String isCompute = this.m_conditionVars.getProperty("computeActions");
/*  193 */       boolean isComputeActions = true;
/*  194 */       if (isCompute != null)
/*      */       {
/*  196 */         isComputeActions = StringUtils.convertToBool(isCompute, true);
/*      */       }
/*      */ 
/*  199 */       boolean saveAlwaysUseActiveForGet = this.m_binder.m_alwaysUseActiveForGet;
/*      */       try
/*      */       {
/*  202 */         this.m_binder.m_alwaysUseActiveForGet = true;
/*  203 */         if (isComputeActions)
/*      */         {
/*  205 */           boolean isCheckin = canCheckin();
/*  206 */           setConditionVar("AllowCheckin", isCheckin);
/*      */         }
/*      */ 
/*  209 */         DocCommonHandler handler = (DocCommonHandler)this.m_handlerMap.get("DocCommonHandler");
/*  210 */         if (handler != null)
/*      */         {
/*  212 */           handler.getURL(true, true);
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  217 */         String msg = LocaleUtils.encodeMessage("csCannotComputeContentInfo", e.getMessage());
/*      */ 
/*  219 */         IOException ioException = new IOException(msg);
/*      */ 
/*  221 */         throw ioException;
/*      */       }
/*      */       finally
/*      */       {
/*  225 */         this.m_binder.m_alwaysUseActiveForGet = saveAlwaysUseActiveForGet;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  230 */     super.notifyNextRow(rsetName, hasNext);
/*      */   }
/*      */ 
/*      */   public boolean canCheckin()
/*      */     throws DataException, ServiceException
/*      */   {
/*  236 */     computeGroupPrivilege();
/*  237 */     String user = this.m_binder.getActiveValue("dUser");
/*      */ 
/*  239 */     boolean isCheckin = false;
/*      */ 
/*  242 */     if (StringUtils.convertToBool(this.m_binder.getActiveValue("dIsCheckedOut"), false))
/*      */     {
/*  244 */       String checkoutUser = this.m_binder.getActiveValue("dCheckoutUser");
/*  245 */       isCheckin = (checkAccess(this.m_binder, 8)) || (checkoutUser.equalsIgnoreCase(user) == true);
/*      */     }
/*      */ 
/*  249 */     return isCheckin;
/*      */   }
/*      */ 
/*      */   public void getOptionsListForField(String fieldName, String serviceName, String dpAction)
/*      */     throws ServiceException, DataException
/*      */   {
/*  255 */     ServiceData serviceData = ServiceManager.getService(serviceName);
/*      */ 
/*  257 */     if (serviceData != null)
/*      */     {
/*  259 */       int serviceAccessLevel = serviceData.m_accessLevel;
/*      */ 
/*  261 */       String schemaViewPrivilege = (String)getCachedObject("SchemaViewPrivilege");
/*      */ 
/*  263 */       if ((schemaViewPrivilege == null) || (schemaViewPrivilege.length() <= 0))
/*      */       {
/*  265 */         setCachedObject("SchemaViewPrivilege", Integer.toString(serviceAccessLevel));
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  270 */       String msg = LocaleUtils.encodeMessage("csIdcUIInvaliddpAction", null, dpAction);
/*  271 */       msg = LocaleUtils.appendMessage(msg, LocaleUtils.encodeMessage("csIdcUICannotGetOptions", null));
/*      */ 
/*  273 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/*  276 */     boolean isInfo = StringUtils.convertToBool(this.m_binder.getLocal("isInfo"), false);
/*      */ 
/*  278 */     if (isInfo)
/*      */     {
/*  280 */       return;
/*      */     }
/*      */ 
/*  288 */     String dpActionProfileMap = this.m_binder.getLocal("dpActionProfileMap");
/*      */ 
/*  290 */     if ((dpActionProfileMap != null) && (dpActionProfileMap.length() > 0))
/*      */     {
/*  292 */       this.m_binder.putLocal("dpAction", dpActionProfileMap);
/*  293 */       this.m_binder.putLocal("dpActionOrginal", dpAction);
/*      */     }
/*      */ 
/*  296 */     String tmp = null;
/*      */     try
/*      */     {
/*  299 */       PageMerger pageMerger = getPageMerger();
/*  300 */       tmp = pageMerger.evaluateResourceInclude("load_document_profile");
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  304 */       Report.trace(null, null, e);
/*      */     }
/*      */     finally
/*      */     {
/*  308 */       if (SystemUtils.m_verbose)
/*      */       {
/*  310 */         Report.debug("docprofile", "Loaded profiles with result:" + tmp, null);
/*      */       }
/*  312 */       this.m_binder.putLocal("dpAction", dpAction);
/*      */     }
/*      */ 
/*  316 */     SchemaFieldConfig schemaFieldConfig = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/*  317 */     SchemaFieldData fieldData = (SchemaFieldData)schemaFieldConfig.getData(fieldName);
/*      */ 
/*  319 */     if (fieldData == null)
/*      */     {
/*  321 */       String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_field", null, fieldName);
/*      */ 
/*  323 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/*  326 */     Properties oldLocalData = (Properties)this.m_binder.getLocalData().clone();
/*  327 */     HashMap returnedRSMap = new HashMap();
/*      */ 
/*  329 */     String optionName = fieldData.get("dOptionListKey");
/*      */ 
/*  331 */     String relationship = null;
/*      */ 
/*  333 */     if ((optionName == null) || (optionName.length() <= 0))
/*      */     {
/*  335 */       String msg = LocaleUtils.encodeMessage("csIdcUINotAnOptionListField", null, fieldName);
/*      */ 
/*  337 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/*  340 */     if (optionName.startsWith(SchemaHelper.VIEW_PREFIX))
/*      */     {
/*  342 */       optionName = optionName.substring(SchemaHelper.VIEW_PREFIX.length());
/*      */ 
/*  344 */       String dParentValue = this.m_binder.getLocal("dParentValue");
/*  345 */       if ((dParentValue != null) && (dParentValue.length() > 0))
/*      */       {
/*  347 */         relationship = fieldData.get("DependentRelationship");
/*      */       }
/*      */ 
/*  351 */       addOptionListFromView(fieldName, optionName, relationship, dParentValue);
/*      */     }
/*  353 */     else if (optionName.startsWith(SchemaHelper.TREE_PREFIX))
/*      */     {
/*  355 */       String treeDefinition = fieldData.get("TreeDefinition");
/*      */ 
/*  357 */       String dParentValue = this.m_binder.getLocal("dParentValue");
/*      */ 
/*  359 */       if ((treeDefinition != null) && (dParentValue != null) && (dParentValue.length() > 0))
/*      */       {
/*  362 */         SchemaHelper schHelper = new SchemaHelper();
/*  363 */         addOptionListForTree(fieldName, treeDefinition, dParentValue, schHelper);
/*      */       }
/*      */     }
/*      */ 
/*  367 */     returnedRSMap.put(fieldName + ".options", "1");
/*      */ 
/*  369 */     cleanResponseBinder(oldLocalData, returnedRSMap);
/*      */   }
/*      */ 
/*      */   public void executeServiceAndEvaluateServiceTemplate(String serviceName)
/*      */     throws DataException, ServiceException
/*      */   {
/*  376 */     this.m_binder.putLocal("useOnlyLocalForDpTrigger", "1");
/*  377 */     this.m_binder.putLocal("isIdcUIpage", "1");
/*      */     try
/*      */     {
/*  381 */       Service service = ServiceManager.getInitializedService(serviceName, this.m_binder, this.m_workspace);
/*  382 */       service.copyShallow(this);
/*  383 */       service.doRequestInternal();
/*      */ 
/*  385 */       int serviceAccessLevel = service.getServiceData().m_accessLevel;
/*      */ 
/*  387 */       String schemaViewPrivilege = (String)getCachedObject("SchemaViewPrivilege");
/*      */ 
/*  389 */       if ((schemaViewPrivilege == null) || (schemaViewPrivilege.length() <= 0))
/*      */       {
/*  391 */         setCachedObject("SchemaViewPrivilege", Integer.toString(serviceAccessLevel));
/*      */       }
/*      */ 
/*  394 */       boolean isEvalTemplate = StringUtils.convertToBool(this.m_binder.getLocal("IsEvalTemplate"), false);
/*      */ 
/*  398 */       String pageName = service.getServiceData().m_htmlPage;
/*      */ 
/*  400 */       if ((pageName == null) || (pageName.length() <= 0))
/*      */       {
/*  405 */         pageName = this.m_binder.getLocal("Page");
/*      */       }
/*      */ 
/*  408 */       if ((isEvalTemplate) && (pageName != null) && (pageName.length() > 0))
/*      */       {
/*  410 */         DynamicHtml dynHtml = getTemplatePage(pageName);
/*      */ 
/*  412 */         if (dynHtml != null)
/*      */         {
/*  414 */           IdcCharArrayWriter sw = new IdcCharArrayWriter();
/*      */           try
/*      */           {
/*  418 */             dynHtml.outputHtml(sw, this.m_pageMerger);
/*      */           }
/*      */           catch (Exception e)
/*      */           {
/*  423 */             String msg = LocaleUtils.appendMessage(LocaleUtils.encodeMessage("csIdcUICannotGetDisplayFields", null), e.getMessage());
/*      */ 
/*  425 */             createServiceException(null, msg);
/*      */           }
/*      */           finally
/*      */           {
/*      */             try
/*      */             {
/*  431 */               if (sw != null)
/*      */               {
/*  433 */                 sw.flush();
/*  434 */                 sw.close();
/*      */               }
/*      */             }
/*      */             catch (IOException e)
/*      */             {
/*  439 */               String msg = LocaleUtils.appendMessage(e.getMessage(), LocaleUtils.encodeMessage("csIdcUICannotGetDisplayFields", null));
/*      */ 
/*  441 */               createServiceException(null, msg);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  449 */       String msg = LocaleUtils.appendMessage(e.getMessage(), LocaleUtils.encodeMessage("csIdcUICannotGetDisplayFields", null));
/*      */ 
/*  451 */       createServiceException(null, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public DataResultSet getGroupFields() throws DataException {
/*  456 */     String[] groupInfoCols = { "parentField", "groupFieldList", "groupHeader", "defaultHide" };
/*  457 */     DataResultSet displayGroupInfoRS = new DataResultSet(groupInfoCols);
/*      */ 
/*  460 */     ResultSet grset = this.m_binder.getResultSet("AssociatedTopFields");
/*  461 */     if ((grset != null) && (!grset.isEmpty()))
/*      */     {
/*  463 */       DataBinder binder = new DataBinder();
/*  464 */       for (grset.first(); grset.isRowPresent(); grset.next())
/*      */       {
/*  466 */         String fieldName = grset.getStringValueByName("parentField");
/*  467 */         binder.putLocal("parentField", fieldName);
/*  468 */         this.m_binder.removeResultSet("AssociatedFields:" + fieldName);
/*      */ 
/*  470 */         String value = this.m_binder.getLocal(fieldName + ":groupFieldList");
/*  471 */         if (value == null) continue; if (value.length() == 0) {
/*      */           continue;
/*      */         }
/*      */ 
/*  475 */         binder.putLocal("groupFieldList", value);
/*  476 */         value = this.m_binder.getLocal(fieldName + ":groupHeader");
/*  477 */         binder.putLocal("groupHeader", (value != null) ? value : "");
/*  478 */         value = this.m_binder.getLocal(fieldName + ":groupDefaultHide");
/*  479 */         binder.putLocal("defaultHide", ((value != null) && (value.equals("1"))) ? "1" : "0");
/*  480 */         Vector row = displayGroupInfoRS.createRow(binder);
/*  481 */         displayGroupInfoRS.addRow(row);
/*      */       }
/*      */     }
/*  484 */     return displayGroupInfoRS;
/*      */   }
/*      */ 
/*      */   public HashMap getSearchOperators(HashMap allowedInBinderRSMap)
/*      */     throws DataException, ServiceException
/*      */   {
/*  490 */     HashMap searchOperatorsRSNameMap = new HashMap();
/*      */ 
/*  492 */     SearchIndexerUtils.getSearchOperators(this.m_binder, this);
/*      */ 
/*  494 */     String textFieldDefaultOperator = null;
/*  495 */     String optionFieldDefaultOperator = null;
/*      */ 
/*  497 */     for (String searchOperatorKeys : SearchIndexerUtils.m_searchDefaultOperatorKeys)
/*      */     {
/*  499 */       String value = this.m_binder.getLocal(searchOperatorKeys);
/*      */ 
/*  501 */       if (searchOperatorKeys.equalsIgnoreCase("DefaultSearchOperator"))
/*      */       {
/*  503 */         textFieldDefaultOperator = value;
/*      */       }
/*  505 */       else if (searchOperatorKeys.equalsIgnoreCase("DefaultNBFieldSearchOperator"))
/*      */       {
/*  507 */         optionFieldDefaultOperator = value;
/*      */       }
/*  509 */       else if (searchOperatorKeys.equalsIgnoreCase("SearchFullTextQueryDef"))
/*      */       {
/*  511 */         boolean isFullTextSearch = false;
/*  512 */         if ((value != null) && (value.length() > 0))
/*      */         {
/*  514 */           isFullTextSearch = true;
/*      */         }
/*  516 */         this.m_binder.putLocal("FullTextSearch", "" + isFullTextSearch);
/*  517 */         this.m_binder.putLocal("FullTextSearch:isResponseData", "1");
/*      */       }
/*      */       else
/*      */       {
/*  521 */         this.m_binder.putLocal(searchOperatorKeys + ":isResponseData", "1");
/*      */       }
/*      */     }
/*      */ 
/*  525 */     if (textFieldDefaultOperator == null)
/*      */     {
/*  527 */       textFieldDefaultOperator = "";
/*      */     }
/*  529 */     if (optionFieldDefaultOperator == null)
/*      */     {
/*  531 */       optionFieldDefaultOperator = "";
/*      */     }
/*      */ 
/*  534 */     String[] fieldInfoCols = { "dOperator", "dOperatorExpression", "dOperatorDisplay" };
/*      */ 
/*  536 */     for (String resultSetName : SearchIndexerUtils.m_searchOperatorRSNames)
/*      */     {
/*  538 */       DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(resultSetName);
/*  539 */       String defaultOperator = null;
/*      */ 
/*  541 */       boolean isFoundDefaultOperator = false;
/*  542 */       boolean isTextFieldOperators = false;
/*      */ 
/*  544 */       if ((drset == null) || (!drset.isRowPresent()))
/*      */         continue;
/*  546 */       String defaultOperatorKey = resultSetName + "DefaultOperator";
/*      */ 
/*  549 */       if (resultSetName.equalsIgnoreCase("SearchTextField"))
/*      */       {
/*  551 */         isTextFieldOperators = true;
/*  552 */         defaultOperator = textFieldDefaultOperator;
/*  553 */         searchOperatorsRSNameMap.put("SearchTextField", "1");
/*      */       }
/*  555 */       else if (resultSetName.equalsIgnoreCase("SearchDateField"))
/*      */       {
/*  557 */         defaultOperator = "dateGE";
/*  558 */         searchOperatorsRSNameMap.put("SearchDateField", "1");
/*      */       }
/*  560 */       else if (resultSetName.equalsIgnoreCase("SearchIntegerField"))
/*      */       {
/*  562 */         defaultOperator = "numberEquals";
/*  563 */         searchOperatorsRSNameMap.put("SearchIntegerField", "1");
/*      */       }
/*  565 */       else if (resultSetName.equalsIgnoreCase("SearchBooleanField"))
/*      */       {
/*  567 */         searchOperatorsRSNameMap.put("SearchBooleanField", "1");
/*      */       }
/*  569 */       else if (resultSetName.equalsIgnoreCase("SearchZoneField"))
/*      */       {
/*  571 */         defaultOperator = "zoneHasAsWord";
/*  572 */         searchOperatorsRSNameMap.put("SearchZoneField", "1");
/*      */       }
/*      */ 
/*  575 */       DataResultSet operatorRS = new DataResultSet(fieldInfoCols);
/*  576 */       boolean isFirst = true;
/*  577 */       String firstOperator = null;
/*  578 */       boolean isOptionOperatorFound = false;
/*      */ 
/*  580 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/*  582 */         String operator = drset.getStringValueByName("OperatorName");
/*  583 */         String display = drset.getStringValueByName("OpWwStrings");
/*  584 */         String expression = drset.getStringValueByName("Expression");
/*      */ 
/*  586 */         if ((operator == null) || (operator.length() <= 0) || (display == null) || (display.length() <= 0))
/*      */           continue;
/*  588 */         if (isFirst)
/*      */         {
/*  590 */           firstOperator = operator;
/*  591 */           if ((!isFoundDefaultOperator) && (((defaultOperator == null) || (defaultOperator.length() <= 0))))
/*      */           {
/*  594 */             defaultOperator = firstOperator;
/*  595 */             isFoundDefaultOperator = true;
/*      */           }
/*  597 */           isFirst = false;
/*      */         }
/*      */ 
/*  600 */         if ((!isFoundDefaultOperator) && (operator.equalsIgnoreCase(defaultOperator)))
/*      */         {
/*  602 */           isFoundDefaultOperator = true;
/*      */         }
/*      */ 
/*  605 */         if ((isTextFieldOperators) && (!isOptionOperatorFound) && (operator.equalsIgnoreCase(optionFieldDefaultOperator)))
/*      */         {
/*  608 */           isOptionOperatorFound = true;
/*      */         }
/*      */ 
/*  612 */         Vector row = operatorRS.createEmptyRow();
/*      */ 
/*  614 */         row.setElementAt(operator, operatorRS.getFieldInfoIndex("dOperator"));
/*  615 */         row.setElementAt(expression, operatorRS.getFieldInfoIndex("dOperatorExpression"));
/*  616 */         row.setElementAt(LocaleResources.getString(display, this), operatorRS.getFieldInfoIndex("dOperatorDisplay"));
/*      */ 
/*  618 */         operatorRS.addRow(row);
/*      */       }
/*      */ 
/*  622 */       if (!isFoundDefaultOperator)
/*      */       {
/*  624 */         defaultOperator = firstOperator;
/*  625 */         isFoundDefaultOperator = true;
/*      */       }
/*      */ 
/*  628 */       if ((isFoundDefaultOperator) && 
/*  630 */         (defaultOperator != null) && (defaultOperator.length() > 0))
/*      */       {
/*  632 */         this.m_binder.putLocal(defaultOperatorKey, defaultOperator);
/*      */       }
/*      */ 
/*  636 */       if (isTextFieldOperators)
/*      */       {
/*  638 */         if (!isOptionOperatorFound)
/*      */         {
/*  640 */           optionFieldDefaultOperator = defaultOperator;
/*      */         }
/*      */ 
/*  643 */         this.m_binder.putLocal("SearchOptionFieldDefaultOperator", optionFieldDefaultOperator);
/*      */       }
/*      */ 
/*  646 */       this.m_binder.addResultSet(resultSetName, operatorRS);
/*  647 */       allowedInBinderRSMap.put(resultSetName, "1");
/*      */     }
/*      */ 
/*  651 */     return searchOperatorsRSNameMap;
/*      */   }
/*      */ 
/*      */   public List getSearchOperatorsSettingListForFieldType(String fieldType, boolean isOptionList, HashMap searchOperatorsRSNameMap)
/*      */   {
/*  657 */     String defaultOpText = this.m_binder.getLocal("SearchTextFieldDefaultOperator");
/*  658 */     String defaultOpOptionField = this.m_binder.getLocal("SearchOptionFieldDefaultOperator");
/*  659 */     String defaultOpDate = this.m_binder.getLocal("SearchDateFieldDefaultOperator");
/*  660 */     String defaultOpInteger = this.m_binder.getLocal("SearchIntegerFieldDefaultOperator");
/*  661 */     String defaultOpBoolean = this.m_binder.getLocal("SearchBooleanFieldDefaultOperator");
/*  662 */     String defaultOpZone = this.m_binder.getLocal("SearchZoneFieldDefaultOperator");
/*      */ 
/*  664 */     String defaultOp = "";
/*  665 */     String searchOperatorsRSName = "";
/*      */ 
/*  667 */     if (fieldType.equalsIgnoreCase("date"))
/*      */     {
/*  669 */       defaultOp = defaultOpDate;
/*      */ 
/*  671 */       if (searchOperatorsRSNameMap.get("SearchDateField") != null)
/*      */       {
/*  673 */         searchOperatorsRSName = "SearchDateField";
/*      */       }
/*      */     }
/*  676 */     else if ((fieldType.equalsIgnoreCase("int")) || (fieldType.equalsIgnoreCase("decimal")))
/*      */     {
/*  678 */       defaultOp = defaultOpInteger;
/*  679 */       if (searchOperatorsRSNameMap.get("SearchIntegerField") != null)
/*      */       {
/*  681 */         searchOperatorsRSName = "SearchIntegerField";
/*      */       }
/*      */     }
/*  684 */     else if (fieldType.equalsIgnoreCase("boolean"))
/*      */     {
/*  686 */       defaultOp = defaultOpBoolean;
/*  687 */       searchOperatorsRSName = "SearchBooleanField";
/*      */     }
/*  689 */     else if (fieldType.equalsIgnoreCase("zone"))
/*      */     {
/*  691 */       defaultOp = defaultOpZone;
/*  692 */       if (searchOperatorsRSNameMap.get("SearchZoneField") != null)
/*      */       {
/*  694 */         searchOperatorsRSName = "SearchZoneField";
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  699 */       defaultOp = defaultOpText;
/*      */ 
/*  701 */       if (isOptionList)
/*      */       {
/*  703 */         defaultOp = defaultOpOptionField;
/*      */       }
/*  705 */       if (searchOperatorsRSNameMap.get("SearchTextField") != null)
/*      */       {
/*  707 */         searchOperatorsRSName = "SearchTextField";
/*      */       }
/*      */     }
/*      */ 
/*  711 */     if (defaultOp == null)
/*      */     {
/*  713 */       defaultOp = "";
/*      */     }
/*  715 */     if (searchOperatorsRSName == null)
/*      */     {
/*  717 */       searchOperatorsRSName = "";
/*      */     }
/*  719 */     List opSettingsList = new ArrayList();
/*  720 */     opSettingsList.add(defaultOp);
/*  721 */     opSettingsList.add(searchOperatorsRSName);
/*      */ 
/*  723 */     return opSettingsList;
/*      */   }
/*      */ 
/*      */   public DataResultSet getDisplayFieldsMetaDefinitions(String dpAction, String metaDefinitions, HashMap allowedInBinderRSMap)
/*      */     throws DataException, ServiceException
/*      */   {
/*  730 */     boolean isQuery = StringUtils.convertToBool(this.m_binder.getLocal("isQuery"), false);
/*  731 */     boolean isInfo = StringUtils.convertToBool(this.m_binder.getLocal("isInfo"), false);
/*      */ 
/*  733 */     boolean isdpActionSearch = dpAction.equalsIgnoreCase("Search");
/*  734 */     boolean isdpActionUpdate = dpAction.equalsIgnoreCase("Update");
/*  735 */     boolean isdpActionFldMetaUpdate = dpAction.equalsIgnoreCase("FLDMetadataUpdate");
/*  736 */     boolean isdpActionCheckinNew = dpAction.equalsIgnoreCase("CheckinNew");
/*      */ 
/*  740 */     boolean isMakeFieldsNonRequired = StringUtils.convertToBool(this.m_binder.getLocal("isMakeFieldsNonRequired"), false);
/*      */ 
/*  745 */     boolean isAddMetadataDefaults = StringUtils.convertToBool(this.m_binder.getLocal("isAddMetadataDefaults"), true);
/*      */ 
/*  751 */     boolean isMakeInfoOnlyHidden = false;
/*      */ 
/*  755 */     if ((isQuery) || (isInfo))
/*      */     {
/*  757 */       isAddMetadataDefaults = false;
/*  758 */       isMakeFieldsNonRequired = true;
/*      */     }
/*      */ 
/*  761 */     String dUser = this.m_binder.getLocal("dUser");
/*  762 */     boolean isUserAdmin = SecurityUtils.isUserOfRole(this.m_userData, "admin");
/*      */ 
/*  764 */     String[] fieldInfoCols = { "fieldName", "fieldType", "fieldLabel", "isHidden", "isReadOnly", "isRequired", "requiredMsg", "defaultValue", "displayValue", "isOptionList", "optionList", "optionListType", "isDependent", "dependentOnField", "isPadMultiselectStorage", "multiselectStorageSeparator", "multiselectDisplaySeparator", "isShowSelectionPath", "isStoreSelectionPath", "treeNodeDisplaySeparator", "treeNodeStorageSeparator", "order", "decimalScale", "isError", "errorMsg", "isUserName" };
/*      */ 
/*  771 */     DataResultSet displayFieldsInfoRS = new DataResultSet(fieldInfoCols);
/*  772 */     displayFieldsInfoRS.setDateFormat(this.m_binder.m_localeDateFormat);
/*      */ 
/*  774 */     HashMap searchOperatorsRSNameMap = new HashMap();
/*      */ 
/*  776 */     if (isdpActionSearch)
/*      */     {
/*  778 */       isMakeInfoOnlyHidden = true;
/*  779 */       searchOperatorsRSNameMap = getSearchOperators(allowedInBinderRSMap);
/*      */ 
/*  782 */       FieldInfo fi = new FieldInfo();
/*  783 */       fi.m_name = "defaultOperator";
/*  784 */       fi.m_type = 6;
/*      */ 
/*  786 */       FieldInfo fi2 = new FieldInfo();
/*  787 */       fi2.m_name = "searchOperatorsRSName";
/*  788 */       fi2.m_type = 6;
/*  789 */       ArrayList fieldList = new ArrayList();
/*  790 */       fieldList.add(fi);
/*  791 */       fieldList.add(fi2);
/*  792 */       displayFieldsInfoRS.mergeFieldsWithFlags(fieldList, 0);
/*      */     }
/*      */ 
/*  796 */     HashMap userNameFieldsMap = getUserNameFieldsMap("UserNameFieldsTable");
/*      */ 
/*  799 */     HashMap excludedFields = getExcludedFieldsMap(new String[] { "WCCADFUIExcludedStandardFields", "WCCADFUIExcludedCustomFields" });
/*      */ 
/*  801 */     List metaDefs = StringUtils.makeListFromSequence(metaDefinitions, ',', ',', 32);
/*      */ 
/*  803 */     for (String metadataSet : metaDefs)
/*      */     {
/*  805 */       boolean isDocMetaDefinition = false;
/*      */ 
/*  807 */       if (metadataSet.equals("DocMetaDefinition"))
/*      */       {
/*  809 */         isDocMetaDefinition = true;
/*      */       }
/*      */ 
/*  812 */       DataResultSet metaDefsRS = SharedObjects.getTable(metadataSet);
/*      */ 
/*  814 */       if (metaDefsRS == null)
/*      */       {
/*  816 */         String msg = LocaleUtils.appendMessage(LocaleUtils.encodeMessage("csIdcUICannotGetDisplayFields", null), LocaleUtils.encodeMessage("csIdcUICannotFindMetaDefinition", null, metadataSet));
/*      */ 
/*  819 */         createServiceException(null, msg);
/*      */       }
/*      */ 
/*  822 */       SchemaHelper schHelper = new SchemaHelper();
/*  823 */       SchemaFieldConfig schemaFieldConfig = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/*      */ 
/*  825 */       Properties originalProperties = (Properties)this.m_binder.getLocalData().clone();
/*      */ 
/*  827 */       for (metaDefsRS.first(); metaDefsRS.isRowPresent(); metaDefsRS.next())
/*      */       {
/*  829 */         this.m_binder.setLocalData((Properties)originalProperties.clone());
/*      */ 
/*  832 */         String fieldName = metaDefsRS.getStringValueByName("dName");
/*      */ 
/*  834 */         if (Report.m_verbose)
/*      */         {
/*  836 */           Report.trace("idcdisplayfields", "Field: " + fieldName + " - metadefinition started.", null);
/*      */         }
/*      */ 
/*  840 */         SchemaFieldData fieldData = (SchemaFieldData)schemaFieldConfig.getData(fieldName);
/*      */ 
/*  842 */         boolean isError = false;
/*  843 */         String errorMsg = "";
/*      */ 
/*  845 */         boolean isExcluded = StringUtils.convertToBool(this.m_binder.getLocal(fieldName + ":isExcluded"), false);
/*      */ 
/*  847 */         if (isExcluded) {
/*      */           continue;
/*      */         }
/*      */ 
/*  851 */         if (excludedFields.get(fieldName) != null)
/*      */         {
/*  853 */           if (!Report.m_verbose)
/*      */             continue;
/*  855 */           Report.trace("idcdisplayfields", "Field: " + fieldName + " - excluded as one of fields in WCCADFUIExcludedStandardFields or WCCADFUIExcludedCustomFields.", null);
/*      */         }
/*      */         else
/*      */         {
/*  862 */           if (isQuery)
/*      */           {
/*  864 */             boolean isSearchable = StringUtils.convertToBool(metaDefsRS.getStringValueByName("dIsSearchable"), false);
/*      */ 
/*  867 */             if (!isSearchable) {
/*      */               continue;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  873 */           boolean isEnabled = StringUtils.convertToBool(evaluateScript(metaDefsRS.getStringValueByName("dIsEnabled")), true);
/*      */ 
/*  876 */           if (!isEnabled)
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/*  881 */           String fieldType = metaDefsRS.getStringValueByName("dType");
/*      */ 
/*  883 */           String caption = this.m_binder.getLocal(fieldName + ":fieldCaption");
/*      */ 
/*  885 */           if ((caption == null) || (caption.length() <= 0))
/*      */           {
/*  887 */             caption = metaDefsRS.getStringValueByName("dCaption");
/*      */           }
/*      */ 
/*  890 */           String localizedCaption = LocaleResources.getString(caption, this);
/*      */ 
/*  892 */           boolean isDefaultHidden = StringUtils.convertToBool(evaluateScript(metaDefsRS.getStringValueByName("dIsHidden")), false);
/*      */ 
/*  895 */           boolean isHidden = (isDefaultHidden) || (StringUtils.convertToBool(this.m_binder.getLocal(fieldName + ":isHidden"), false));
/*      */ 
/*  898 */           boolean isInfoOnly = (isInfo) || (StringUtils.convertToBool(this.m_binder.getLocal(fieldName + ":isInfoOnly"), false));
/*      */ 
/*  901 */           if ((!isInfo) && (!isInfoOnly))
/*      */           {
/*  903 */             isInfoOnly = StringUtils.convertToBool(evaluateScript(metaDefsRS.getStringValueByName("dIsInfoOnly")), false);
/*      */           }
/*      */ 
/*  907 */           boolean isRequired = StringUtils.convertToBool(this.m_binder.getLocal(fieldName + ":isRequired"), false);
/*      */ 
/*  910 */           String requiredMsg = "";
/*      */ 
/*  912 */           if (!isRequired)
/*      */           {
/*  914 */             isRequired = StringUtils.convertToBool(evaluateScript(metaDefsRS.getStringValueByName("dIsRequired")), false);
/*      */           }
/*      */ 
/*  918 */           if (isRequired)
/*      */           {
/*  920 */             requiredMsg = this.m_binder.getLocal(fieldName + ":requiredMsg");
/*      */ 
/*  922 */             if ((requiredMsg == null) || (requiredMsg.length() <= 0))
/*      */             {
/*  924 */               requiredMsg = LocaleResources.getString(metaDefsRS.getStringValueByName("dRequiredMsg"), this);
/*      */             }
/*      */ 
/*  927 */             boolean isSetNotRequired = StringUtils.convertToBool(this.m_binder.getLocal(fieldName + ":isSetNotRequired"), false);
/*      */ 
/*  932 */             if ((isMakeFieldsNonRequired) || (isSetNotRequired))
/*      */             {
/*  934 */               isRequired = false;
/*  935 */               requiredMsg = "";
/*      */             }
/*      */           }
/*      */ 
/*  939 */           boolean isUseLocalOnly = StringUtils.convertToBool(this.m_binder.getLocal(fieldName + ":isUseLocalOnly"), false);
/*      */ 
/*  942 */           boolean isValidateDefaults = false;
/*  943 */           boolean isSkipValidateDefaults = false;
/*      */ 
/*  945 */           boolean isForceOptionList = false;
/*  946 */           String forceOptionListName = "";
/*      */ 
/*  948 */           String defaultValue = "";
/*  949 */           String displayValue = "";
/*      */ 
/*  951 */           if (isUseLocalOnly)
/*      */           {
/*  953 */             defaultValue = this.m_binder.getLocal(fieldName);
/*      */           }
/*      */           else
/*      */           {
/*  957 */             defaultValue = this.m_binder.getAllowMissing(fieldName);
/*      */           }
/*      */ 
/*  961 */           if ((isAddMetadataDefaults) && (((defaultValue == null) || (defaultValue.length() <= 0))))
/*      */           {
/*  963 */             defaultValue = evaluateScript(metaDefsRS.getStringValueByName("dDefaultValue"));
/*      */           }
/*      */ 
/*  966 */           boolean isClearDefault = StringUtils.convertToBool(this.m_binder.getLocal(fieldName + ":isClearDefault"), false);
/*      */ 
/*  969 */           if (isClearDefault)
/*      */           {
/*  971 */             defaultValue = "";
/*      */           }
/*      */ 
/*  975 */           if ((fieldType.equalsIgnoreCase("date")) && (defaultValue != null) && (defaultValue.length() > 0) && (defaultValue.startsWith("{ts '")) && (defaultValue.endsWith("'}")))
/*      */           {
/*  978 */             defaultValue = LocaleResources.localizeDate(defaultValue, this);
/*      */           }
/*      */ 
/*  986 */           if (fieldName.equalsIgnoreCase("dOriginalName"))
/*      */           {
/*  988 */             defaultValue = "";
/*      */           }
/*      */ 
/*  991 */           if ((!isInfo) && (fieldName.equalsIgnoreCase("dDocAuthor")))
/*      */           {
/*  993 */             if (isdpActionFldMetaUpdate)
/*      */             {
/*  995 */               isInfoOnly = false;
/*  996 */               isForceOptionList = true;
/*  997 */               forceOptionListName = "view://docAuthors";
/*      */             }
/*  999 */             else if (SharedObjects.getEnvValueAsBoolean("AllowOwnerToChangeAuthor", false))
/*      */             {
/* 1001 */               isSkipValidateDefaults = true;
/*      */ 
/* 1003 */               isInfoOnly = false;
/* 1004 */               isForceOptionList = true;
/* 1005 */               forceOptionListName = "view://docAuthors";
/*      */ 
/* 1010 */               if ((isdpActionUpdate) && (!isUserAdmin) && (defaultValue != null) && (dUser != null) && (!defaultValue.equalsIgnoreCase(dUser)))
/*      */               {
/* 1014 */                 isInfoOnly = true;
/* 1015 */                 isForceOptionList = false;
/* 1016 */                 forceOptionListName = "";
/*      */               }
/*      */             }
/* 1019 */             else if ((isdpActionCheckinNew) && (!SharedObjects.getEnvValueAsBoolean("AllowOwnerToChangeAuthor", false)))
/*      */             {
/* 1021 */               defaultValue = dUser;
/*      */             }
/*      */           }
/*      */ 
/* 1025 */           if ((isMakeInfoOnlyHidden) && (isInfoOnly) && (((defaultValue == null) || (defaultValue.length() <= 0))))
/*      */           {
/* 1028 */             isHidden = true;
/*      */           }
/*      */ 
/* 1031 */           if (defaultValue != null)
/*      */           {
/* 1033 */             displayValue = defaultValue;
/*      */           }
/*      */ 
/* 1036 */           String orderStr = metaDefsRS.getStringValueByName("dOrder");
/*      */ 
/* 1038 */           int orderCount = 0;
/* 1039 */           int order = NumberUtils.parseInteger(orderStr, orderCount++);
/*      */ 
/* 1041 */           if (isDocMetaDefinition)
/*      */           {
/* 1043 */             order += 5000;
/*      */           }
/*      */ 
/* 1046 */           boolean isOptionList = StringUtils.convertToBool(evaluateScript(metaDefsRS.getStringValueByName("dIsOptionList")), false);
/*      */ 
/* 1049 */           boolean isDependent = false;
/*      */ 
/* 1051 */           if (fieldData != null)
/*      */           {
/* 1053 */             isDependent = StringUtils.convertToBool(fieldData.get("IsDependentList"), false);
/*      */           }
/* 1055 */           if (!isDependent)
/*      */           {
/* 1057 */             isDependent = StringUtils.convertToBool(this.m_binder.getLocal(fieldName + ":isDependentList"), false);
/*      */           }
/*      */ 
/* 1060 */           boolean isOptListAdded = false;
/*      */ 
/* 1063 */           String optionsListName = evaluateScript(metaDefsRS.getStringValueByName("dOptionListKey"));
/* 1064 */           String optionsListType = "";
/*      */ 
/* 1066 */           if (isForceOptionList)
/*      */           {
/* 1068 */             isOptionList = true;
/* 1069 */             optionsListName = forceOptionListName;
/*      */           }
/*      */ 
/* 1072 */           if (isHidden)
/*      */           {
/* 1074 */             isOptionList = false;
/*      */           }
/*      */ 
/* 1077 */           boolean isGetOptionListValues = true;
/* 1078 */           boolean isTreeOptionList = false;
/*      */ 
/* 1080 */           if (isInfoOnly)
/*      */           {
/* 1082 */             isGetOptionListValues = false;
/*      */           }
/*      */ 
/* 1087 */           if (isDependent)
/*      */           {
/* 1089 */             isGetOptionListValues = false;
/*      */           }
/*      */ 
/* 1093 */           boolean isShowSelectionPath = false;
/* 1094 */           boolean isStoreSelectionPath = false;
/* 1095 */           String treeNodeDisplaySeparator = null;
/* 1096 */           String treeNodeStorageSeparator = null;
/*      */ 
/* 1100 */           boolean isSkipOptions = StringUtils.convertToBool(this.m_binder.getLocal(fieldName + ":isSkipOptions"), false);
/*      */ 
/* 1102 */           if ((isOptionList) && (!isSkipOptions))
/*      */           {
/* 1104 */             optionsListType = metaDefsRS.getStringValueByName("dOptionListType");
/*      */ 
/* 1106 */             if ((optionsListType.equalsIgnoreCase("choice")) && (!isSkipValidateDefaults))
/*      */             {
/* 1108 */               isValidateDefaults = true;
/*      */             }
/*      */ 
/* 1111 */             if ((optionsListName != null) && (optionsListName.length() > 0))
/*      */             {
/* 1115 */               allowedInBinderRSMap.put(fieldName + ".options", "1");
/*      */ 
/* 1117 */               if (optionsListName.startsWith(SchemaHelper.VIEW_PREFIX))
/*      */               {
/* 1120 */                 optionsListName = optionsListName.substring(SchemaHelper.VIEW_PREFIX.length());
/*      */ 
/* 1122 */                 if (isGetOptionListValues)
/*      */                 {
/* 1124 */                   isOptListAdded = addOptionListFromView(fieldName, optionsListName, null, null);
/*      */                 }
/*      */               }
/* 1127 */               else if (optionsListName.startsWith(SchemaHelper.TREE_PREFIX))
/*      */               {
/* 1129 */                 if (optionsListName.length() == SchemaHelper.TREE_PREFIX.length())
/*      */                 {
/* 1131 */                   isTreeOptionList = true;
/* 1132 */                   if ((isGetOptionListValues) && (fieldData != null))
/*      */                   {
/* 1134 */                     String treeDefinition = fieldData.get("TreeDefinition");
/* 1135 */                     if (treeDefinition != null)
/*      */                     {
/* 1137 */                       optionsListName = SchemaHelper.TREE_PREFIX;
/* 1138 */                       isOptListAdded = addOptionListForTree(fieldName, treeDefinition, null, schHelper);
/*      */ 
/* 1140 */                       isShowSelectionPath = StringUtils.convertToBool(fieldData.get("ShowSelectionPath"), false);
/* 1141 */                       isStoreSelectionPath = StringUtils.convertToBool(fieldData.get("StoreSelectionPath"), false);
/* 1142 */                       treeNodeDisplaySeparator = fieldData.get("TreeNodeDisplaySeparator");
/* 1143 */                       treeNodeStorageSeparator = fieldData.get("TreeNodeStorageSeparator");
/*      */                     }
/*      */                   }
/*      */                 }
/* 1147 */                 isValidateDefaults = false;
/*      */               }
/* 1151 */               else if (isGetOptionListValues)
/*      */               {
/* 1153 */                 addOptionList(fieldName, optionsListName);
/* 1154 */                 isOptListAdded = true;
/*      */               }
/*      */ 
/* 1158 */               if ((defaultValue != null) && (defaultValue.length() > 0))
/*      */               {
/* 1160 */                 displayValue = getDisplayFieldValue(fieldName, optionsListName, defaultValue, isTreeOptionList);
/*      */               }
/*      */             }
/*      */ 
/* 1164 */             if (isInfo)
/*      */             {
/* 1166 */               isValidateDefaults = false;
/*      */             }
/*      */ 
/* 1169 */             if (isOptListAdded)
/*      */             {
/* 1171 */               optionsListName = fieldName + ".options";
/*      */ 
/* 1175 */               if ((isValidateDefaults) && (defaultValue != null) && (defaultValue.length() > 0))
/*      */               {
/* 1177 */                 DataResultSet opts = (DataResultSet)this.m_binder.getResultSet(fieldName + ".options");
/*      */ 
/* 1179 */                 if ((opts != null) && (opts.isRowPresent()))
/*      */                 {
/* 1181 */                   int index = opts.getFieldInfoIndex("dOption");
/* 1182 */                   if ((index < 0) || (opts.findRow(index, defaultValue, 0, 0) == null))
/*      */                   {
/* 1184 */                     defaultValue = "";
/* 1185 */                     displayValue = "";
/* 1186 */                     if (Report.m_verbose)
/*      */                     {
/* 1188 */                       Report.trace("idcdisplayfields", "Field: " + fieldName + " - cleared default value, " + "it was not found in the option list.", null);
/*      */                     }
/*      */                   }
/*      */ 
/*      */                 }
/*      */ 
/*      */               }
/*      */ 
/*      */             }
/*      */             else
/*      */             {
/* 1199 */               boolean isOptionListLoadFailed = StringUtils.convertToBool(this.m_binder.getLocal(optionsListName + ".isFailed"), false);
/*      */ 
/* 1202 */               if (isOptionListLoadFailed)
/*      */               {
/* 1204 */                 isError = true;
/* 1205 */                 String error = this.m_binder.getLocal(optionsListName + ".error");
/* 1206 */                 errorMsg = "Error loading value for option list " + error;
/*      */               }
/*      */ 
/* 1211 */               if (!isSkipOptions)
/*      */               {
/* 1213 */                 optionsListName = "";
/* 1214 */                 optionsListType = "";
/*      */               }
/*      */             }
/*      */           }
/*      */ 
/* 1219 */           String dependentOnField = "";
/*      */ 
/* 1221 */           if ((isDependent) && (fieldData != null))
/*      */           {
/* 1223 */             dependentOnField = fieldData.get("DependentOnField");
/*      */ 
/* 1225 */             if ((dependentOnField == null) || (dependentOnField.length() <= 0))
/*      */             {
/* 1227 */               isDependent = false;
/* 1228 */               dependentOnField = "";
/*      */             }
/* 1230 */             optionsListType = metaDefsRS.getStringValueByName("dOptionListType");
/*      */           }
/*      */ 
/* 1233 */           int indexOfMulti = optionsListType.toLowerCase().indexOf("multi");
/*      */ 
/* 1235 */           boolean isPadMultiselectStorage = false;
/* 1236 */           String multiselectStorageSeparator = null;
/* 1237 */           String multiselectDisplaySeparator = null;
/*      */ 
/* 1239 */           if ((indexOfMulti >= 0) && (fieldData != null))
/*      */           {
/* 1241 */             isPadMultiselectStorage = StringUtils.convertToBool(fieldData.get("PadMultiselectStorage"), false);
/* 1242 */             multiselectStorageSeparator = fieldData.get("MultiselectStorageSeparator");
/* 1243 */             multiselectDisplaySeparator = fieldData.get("MultiselectDisplaySeparator");
/*      */           }
/*      */ 
/* 1246 */           List searchOpList = new ArrayList();
/*      */ 
/* 1248 */           if (isdpActionSearch)
/*      */           {
/* 1250 */             searchOpList = getSearchOperatorsSettingListForFieldType(fieldType, isOptionList, searchOperatorsRSNameMap);
/*      */           }
/*      */ 
/* 1254 */           String decimalScale = metaDefsRS.getStringValueByName("dDecimalScale");
/*      */ 
/* 1256 */           Vector row = displayFieldsInfoRS.createEmptyRow();
/*      */ 
/* 1258 */           row.setElementAt(fieldName, displayFieldsInfoRS.getFieldInfoIndex("fieldName"));
/* 1259 */           row.setElementAt(fieldType, displayFieldsInfoRS.getFieldInfoIndex("fieldType"));
/* 1260 */           row.setElementAt(localizedCaption, displayFieldsInfoRS.getFieldInfoIndex("fieldLabel"));
/*      */ 
/* 1262 */           row.setElementAt((isHidden) ? "1" : "0", displayFieldsInfoRS.getFieldInfoIndex("isHidden"));
/* 1263 */           row.setElementAt((isInfoOnly) ? "1" : "0", displayFieldsInfoRS.getFieldInfoIndex("isReadOnly"));
/* 1264 */           row.setElementAt((isRequired) ? "1" : "0", displayFieldsInfoRS.getFieldInfoIndex("isRequired"));
/* 1265 */           row.setElementAt((requiredMsg != null) ? requiredMsg : "", displayFieldsInfoRS.getFieldInfoIndex("requiredMsg"));
/*      */ 
/* 1267 */           row.setElementAt(defaultValue, displayFieldsInfoRS.getFieldInfoIndex("defaultValue"));
/*      */ 
/* 1269 */           row.setElementAt(displayValue, displayFieldsInfoRS.getFieldInfoIndex("displayValue"));
/*      */ 
/* 1272 */           if (isOptionList)
/*      */           {
/* 1274 */             row.setElementAt((isOptionList) ? "1" : "0", displayFieldsInfoRS.getFieldInfoIndex("isOptionList"));
/* 1275 */             row.setElementAt(((optionsListName != null) && (optionsListName.length() > 0)) ? optionsListName : "", displayFieldsInfoRS.getFieldInfoIndex("optionList"));
/*      */ 
/* 1277 */             row.setElementAt(optionsListType, displayFieldsInfoRS.getFieldInfoIndex("optionListType"));
/*      */           }
/*      */ 
/* 1281 */           row.setElementAt((isDependent) ? "1" : "0", displayFieldsInfoRS.getFieldInfoIndex("isDependent"));
/* 1282 */           row.setElementAt((isDependent) ? dependentOnField : "", displayFieldsInfoRS.getFieldInfoIndex("dependentOnField"));
/*      */ 
/* 1285 */           row.setElementAt((isPadMultiselectStorage) ? "1" : "0", displayFieldsInfoRS.getFieldInfoIndex("isPadMultiselectStorage"));
/* 1286 */           row.setElementAt((multiselectStorageSeparator != null) ? multiselectStorageSeparator : "", displayFieldsInfoRS.getFieldInfoIndex("multiselectStorageSeparator"));
/*      */ 
/* 1288 */           row.setElementAt((multiselectDisplaySeparator != null) ? multiselectDisplaySeparator : "", displayFieldsInfoRS.getFieldInfoIndex("multiselectDisplaySeparator"));
/*      */ 
/* 1291 */           row.setElementAt((isShowSelectionPath) ? "1" : "0", displayFieldsInfoRS.getFieldInfoIndex("isShowSelectionPath"));
/*      */ 
/* 1293 */           row.setElementAt((isStoreSelectionPath) ? "1" : "0", displayFieldsInfoRS.getFieldInfoIndex("isStoreSelectionPath"));
/*      */ 
/* 1295 */           row.setElementAt((treeNodeDisplaySeparator != null) ? treeNodeDisplaySeparator : "", displayFieldsInfoRS.getFieldInfoIndex("treeNodeDisplaySeparator"));
/*      */ 
/* 1297 */           row.setElementAt((treeNodeStorageSeparator != null) ? treeNodeStorageSeparator : "", displayFieldsInfoRS.getFieldInfoIndex("treeNodeStorageSeparator"));
/*      */ 
/* 1300 */           row.setElementAt((decimalScale != null) ? decimalScale : "", displayFieldsInfoRS.getFieldInfoIndex("decimalScale"));
/*      */ 
/* 1303 */           row.setElementAt(order + "", displayFieldsInfoRS.getFieldInfoIndex("order"));
/*      */ 
/* 1305 */           row.setElementAt((isError) ? "1" : "0", displayFieldsInfoRS.getFieldInfoIndex("isError"));
/* 1306 */           row.setElementAt(((errorMsg != null) && (errorMsg.length() > 0)) ? errorMsg : "", displayFieldsInfoRS.getFieldInfoIndex("errorMsg"));
/*      */ 
/* 1309 */           if (isdpActionSearch)
/*      */           {
/* 1311 */             String defaultOp = (String)searchOpList.get(0);
/* 1312 */             String searchOperatorsRSName = (String)searchOpList.get(1);
/*      */ 
/* 1314 */             row.setElementAt((defaultOp != null) ? defaultOp : "", displayFieldsInfoRS.getFieldInfoIndex("defaultOperator"));
/*      */ 
/* 1316 */             row.setElementAt((searchOperatorsRSName != null) ? searchOperatorsRSName : "", displayFieldsInfoRS.getFieldInfoIndex("searchOperatorsRSName"));
/*      */           }
/*      */ 
/* 1321 */           row.setElementAt((userNameFieldsMap.get(fieldName) != null) ? "1" : "0", displayFieldsInfoRS.getFieldInfoIndex("isUserName"));
/*      */ 
/* 1324 */           displayFieldsInfoRS.addRow(row);
/*      */ 
/* 1326 */           if (!Report.m_verbose)
/*      */             continue;
/* 1328 */           Report.trace("idcdisplayfields", "Field: " + fieldName + " - metadefinition finished.", null);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1333 */     return displayFieldsInfoRS;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getDisplayFieldsDefinition()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1341 */     String dpAction = this.m_binder.getLocal("dpAction");
/*      */ 
/* 1343 */     if ((dpAction == null) || (dpAction.length() <= 0))
/*      */     {
/* 1345 */       String msg = LocaleUtils.encodeMessage("syParameterNotFound", null, "dpAction");
/* 1346 */       msg = LocaleUtils.appendMessage(msg, LocaleUtils.encodeMessage("csIdcUICannotGetDisplayFields", null));
/*      */ 
/* 1348 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1352 */     Properties oldLocalData = (Properties)this.m_binder.getLocalData().clone();
/*      */ 
/* 1354 */     DataResultSet webUIPagesRS = SharedObjects.getTable("IdcUIdpActionServicesTable");
/*      */ 
/* 1356 */     Vector dpActionRow = webUIPagesRS.findRow(webUIPagesRS.getFieldInfoIndex("Name"), dpAction);
/*      */ 
/* 1358 */     if (dpActionRow == null)
/*      */     {
/* 1360 */       String msg = LocaleUtils.encodeMessage("csIdcUIInvaliddpAction", null, dpAction);
/* 1361 */       msg = LocaleUtils.appendMessage(msg, LocaleUtils.encodeMessage("csIdcUICannotGetDisplayFields", null));
/*      */ 
/* 1363 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1366 */     String metaDefinitions = webUIPagesRS.getStringValueByName("MetaDefinitions");
/* 1367 */     String serviceName = webUIPagesRS.getStringValueByName("Service");
/*      */ 
/* 1369 */     if ((metaDefinitions == null) || (metaDefinitions.length() <= 0) || (serviceName == null) || (serviceName.length() <= 0))
/*      */     {
/* 1372 */       return;
/*      */     }
/*      */ 
/* 1375 */     String requiredParams = webUIPagesRS.getStringValueByName("RequiredParams");
/*      */ 
/* 1377 */     if ((requiredParams != null) && (requiredParams.length() > 0))
/*      */     {
/* 1379 */       List params = StringUtils.makeListFromSequence(requiredParams, ',', ',', 32);
/*      */ 
/* 1381 */       for (String param : params)
/*      */       {
/* 1383 */         String paramValue = this.m_binder.getLocal(param);
/*      */ 
/* 1385 */         if ((paramValue == null) || (paramValue.length() <= 0))
/*      */         {
/* 1387 */           String msg = LocaleUtils.encodeMessage("syParameterNotFound", null, param);
/* 1388 */           msg = LocaleUtils.appendMessage(msg, LocaleUtils.encodeMessage("csIdcUICannotGetDisplayFields", null));
/*      */ 
/* 1390 */           createServiceException(null, msg);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1395 */     String additionalParams = webUIPagesRS.getStringValueByName("AdditionalParams");
/*      */ 
/* 1397 */     if ((additionalParams != null) && (additionalParams.length() > 0))
/*      */     {
/* 1399 */       List params = StringUtils.makeListFromSequence(additionalParams, ',', '^', 32);
/*      */ 
/* 1401 */       for (String param : params)
/*      */       {
/* 1403 */         int indexofEqual = param.indexOf("=");
/*      */ 
/* 1405 */         if (indexofEqual <= 0)
/*      */         {
/* 1407 */           String msg = LocaleUtils.encodeMessage("csIdcUIInvalidAdditinalParamMissingEqual", null, param);
/* 1408 */           msg = LocaleUtils.appendMessage(msg, LocaleUtils.encodeMessage("csIdcUICannotGetDisplayFields", null));
/*      */ 
/* 1410 */           createServiceException(null, msg);
/*      */         }
/*      */ 
/* 1413 */         String paramKey = param.substring(0, indexofEqual);
/* 1414 */         paramKey = paramKey.trim();
/*      */ 
/* 1416 */         String paramValue = param.substring(indexofEqual + 1);
/* 1417 */         paramValue = paramValue.trim();
/*      */ 
/* 1419 */         String paramValueFromBinder = this.m_binder.getLocal(paramKey);
/*      */ 
/* 1422 */         if ((paramValue != null) && (paramValue.length() > 0) && (paramValueFromBinder == null))
/*      */         {
/* 1424 */           this.m_binder.putLocal(paramKey, paramValue);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1429 */     String dFieldName = this.m_binder.getLocal("dFieldName");
/* 1430 */     if ((dFieldName != null) && (dFieldName.length() > 0))
/*      */     {
/* 1432 */       getOptionsListForField(dFieldName, serviceName, dpAction);
/* 1433 */       return;
/*      */     }
/*      */ 
/* 1436 */     executeServiceAndEvaluateServiceTemplate(serviceName);
/*      */ 
/* 1443 */     this.m_binder.putLocal("dpAction", dpAction);
/*      */ 
/* 1447 */     loadCustomIdocScriptGetDisplayFieldsSetting();
/*      */ 
/* 1449 */     HashMap returnedRSMap = new HashMap();
/*      */ 
/* 1451 */     DataResultSet displayFieldsInfoRS = getDisplayFieldsMetaDefinitions(dpAction, metaDefinitions, returnedRSMap);
/*      */ 
/* 1453 */     DataResultSet displayGroupInfoRS = getGroupFields();
/*      */ 
/* 1456 */     returnedRSMap.put("DisplayFieldInfo", "1");
/* 1457 */     returnedRSMap.put("DisplayGroupInfo", "1");
/*      */ 
/* 1459 */     cleanResponseBinder(oldLocalData, returnedRSMap);
/*      */ 
/* 1461 */     this.m_binder.addResultSet("DisplayFieldInfo", displayFieldsInfoRS);
/* 1462 */     this.m_binder.addResultSet("DisplayGroupInfo", displayGroupInfoRS);
/*      */   }
/*      */ 
/*      */   private HashMap getUserNameFieldsMap(String tableName)
/*      */   {
/* 1469 */     HashMap userNameFieldsMap = new HashMap();
/* 1470 */     DataResultSet userNameFieldsRS = SharedObjects.getTable(tableName);
/* 1471 */     if (userNameFieldsRS != null)
/*      */     {
/* 1473 */       for (userNameFieldsRS.first(); userNameFieldsRS.isRowPresent(); userNameFieldsRS.next())
/*      */       {
/* 1475 */         String field = userNameFieldsRS.getStringValue(0);
/* 1476 */         if (field == null)
/*      */           continue;
/* 1478 */         userNameFieldsMap.put(field, "1");
/*      */       }
/*      */     }
/*      */ 
/* 1482 */     return userNameFieldsMap;
/*      */   }
/*      */ 
/*      */   private HashMap getExcludedFieldsMap(String[] lists)
/*      */   {
/* 1487 */     HashMap excludedFieldsMap = new HashMap();
/* 1488 */     if ((lists != null) && (lists.length > 0))
/*      */     {
/* 1490 */       String excludedFields = "";
/* 1491 */       for (String list : lists)
/*      */       {
/* 1493 */         if ((list == null) || (list.length() <= 0))
/*      */           continue;
/* 1495 */         if (excludedFields.length() > 0)
/*      */         {
/* 1497 */           excludedFields = excludedFields + ",";
/*      */         }
/* 1499 */         excludedFields = excludedFields + SharedObjects.getEnvironmentValue(list);
/*      */       }
/*      */ 
/* 1502 */       List excludedFieldsList = StringUtils.parseArray(excludedFields, ',', ',');
/* 1503 */       if (excludedFieldsList != null)
/*      */       {
/* 1505 */         for (String field : excludedFieldsList)
/*      */         {
/* 1507 */           if ((field != null) && (field.length() > 0))
/*      */           {
/* 1509 */             excludedFieldsMap.put(field, "1");
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1514 */     return excludedFieldsMap;
/*      */   }
/*      */ 
/*      */   private void addOptionListFromResultSet(String fieldName, String listName) throws DataException
/*      */   {
/* 1519 */     ResultSet rset = SharedObjects.getTable(listName);
/* 1520 */     if (rset == null)
/*      */     {
/* 1522 */       return;
/*      */     }
/*      */ 
/* 1526 */     SchemaHelper sh = new SchemaHelper();
/* 1527 */     SchemaViewData viewData = sh.getView(listName);
/* 1528 */     if (viewData == null)
/*      */     {
/* 1530 */       return;
/*      */     }
/*      */ 
/* 1533 */     DataResultSet drset = new DataResultSet();
/* 1534 */     drset.copy(rset);
/* 1535 */     String internalCol = viewData.get("schInternalColumn");
/* 1536 */     String displayCol = viewData.get("schLabelColumn");
/* 1537 */     drset.renameField(internalCol, "dOption");
/* 1538 */     if (!internalCol.equals(displayCol))
/*      */     {
/* 1540 */       drset.renameField(displayCol, "dDescription");
/*      */     }
/* 1542 */     this.m_binder.addResultSet(fieldName + ".options", drset);
/*      */ 
/* 1545 */     String value = this.m_binder.getLocal(fieldName + ":isRestricted");
/* 1546 */     if ((value != null) && (value.equals("1")))
/*      */     {
/* 1548 */       Vector rvals = getRestrictedListValues(fieldName);
/* 1549 */       for (drset.first(); drset.isRowPresent(); )
/*      */       {
/* 1551 */         value = drset.getStringValueByName("dOption");
/* 1552 */         if (isRestrictedFieldValueRemoved(rvals, value, fieldName))
/*      */         {
/* 1554 */           drset.deleteCurrentRow();
/*      */         }
/*      */ 
/* 1558 */         drset.next();
/*      */       }
/*      */ 
/* 1561 */       this.m_binder.removeResultSet(fieldName + ".RestrictedList");
/*      */     }
/*      */ 
/* 1565 */     int indexDesc = drset.getFieldInfoIndex("dDescription");
/* 1566 */     if (indexDesc < 0)
/*      */       return;
/* 1568 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1570 */       drset.setCurrentValue(indexDesc, LocaleResources.getString(drset.getStringValue(indexDesc), this));
/*      */     }
/*      */   }
/*      */ 
/*      */   private void addOptionList(String fieldName, String listName)
/*      */   {
/* 1577 */     Vector allListVals = SharedObjects.getOptList(listName);
/* 1578 */     DataResultSet rset = new DataResultSet(new String[] { "dOption" });
/* 1579 */     this.m_binder.addResultSet(fieldName + ".options", rset);
/*      */ 
/* 1581 */     if ((allListVals == null) || (allListVals.isEmpty()))
/*      */     {
/* 1583 */       return;
/*      */     }
/*      */ 
/* 1586 */     Vector optionList = new Vector(allListVals);
/*      */ 
/* 1589 */     String value = this.m_binder.getLocal(fieldName + ":isRestricted");
/* 1590 */     if ((value != null) && (value.equals("1")))
/*      */     {
/* 1592 */       Vector rvals = getRestrictedListValues(fieldName);
/* 1593 */       retainAllWithRegex(rvals, optionList, fieldName);
/* 1594 */       this.m_binder.removeResultSet(fieldName + ".RestrictedList");
/*      */     }
/*      */ 
/* 1597 */     for (String option : optionList)
/*      */     {
/* 1599 */       Vector row = new Vector();
/* 1600 */       row.add(option);
/* 1601 */       rset.addRow(row);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean addOptionListForTree(String fieldName, String treeDefinition, String parentValue, SchemaHelper schHelper)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1621 */     Object[] def = schHelper.expandTreeDefinition(treeDefinition);
/*      */ 
/* 1623 */     String viewName = null;
/* 1624 */     String relName = null;
/* 1625 */     String initialValue = null;
/*      */ 
/* 1627 */     for (int i = 0; i < def.length; ++i)
/*      */     {
/* 1629 */       Object obj = def[i];
/* 1630 */       if (obj instanceof SchemaViewData)
/*      */       {
/* 1632 */         viewName = ((SchemaViewData)obj).get("schViewName");
/*      */       }
/* 1634 */       else if (obj instanceof SchemaRelationData)
/*      */       {
/* 1636 */         relName = ((SchemaRelationData)obj).get("schRelationName");
/*      */       }
/* 1638 */       else if (obj instanceof SchemaTreePointer)
/*      */       {
/* 1640 */         SchemaTreePointer stp = (SchemaTreePointer)obj;
/* 1641 */         initialValue = stp.m_initialKeyValue;
/*      */       }
/*      */ 
/* 1644 */       if (i > 2) {
/*      */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1650 */     if ((parentValue == null) && (initialValue != null))
/*      */     {
/* 1652 */       parentValue = initialValue;
/*      */     }
/*      */ 
/* 1657 */     if (parentValue == null)
/*      */     {
/* 1659 */       relName = null;
/*      */     }
/*      */ 
/* 1662 */     return addOptionListFromView(fieldName, viewName, relName, parentValue);
/*      */   }
/*      */ 
/*      */   public String getDisplayFieldValue(String fieldName, String viewName, String defaultValue, boolean isTree)
/*      */   {
/* 1668 */     if (isTree)
/*      */     {
/* 1670 */       this.m_binder.putLocal("isIdcTreeField", "1");
/*      */     }
/*      */     else
/*      */     {
/* 1674 */       this.m_binder.putLocal("idcFieldViewName", viewName);
/*      */     }
/* 1676 */     this.m_binder.putLocal("idcFieldName", fieldName);
/* 1677 */     this.m_binder.putLocal("idcFieldValue", defaultValue);
/*      */ 
/* 1679 */     String result = "";
/*      */     try
/*      */     {
/* 1682 */       result = this.m_pageMerger.evaluateResourceInclude("std_idc_get_display_value");
/*      */ 
/* 1684 */       if ((result != null) && (result.length() > 0))
/*      */       {
/* 1686 */         result = result.trim();
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1691 */       Report.trace(null, null, e);
/*      */     }
/*      */ 
/* 1694 */     return result;
/*      */   }
/*      */ 
/*      */   public boolean addOptionListFromView(String fieldName, String viewName, String relation, String parentValue)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1703 */     this.m_binder.putLocal("schViewName", viewName);
/* 1704 */     boolean isOrginalIsJava = this.m_binder.m_isJava;
/*      */ 
/* 1706 */     if ((relation != null) && (relation.length() > 0))
/*      */     {
/* 1708 */       this.m_binder.putLocal("schRelationName", relation);
/*      */     }
/*      */ 
/* 1711 */     if ((parentValue != null) && (relation.length() > 0))
/*      */     {
/* 1713 */       this.m_binder.putLocal("schParentValue", parentValue);
/*      */     }
/*      */ 
/* 1716 */     String contentType = this.m_binder.getContentType();
/*      */     try
/*      */     {
/* 1722 */       this.m_binder.putLocal("IsJava", "1");
/* 1723 */       executeServiceEx("GET_SCHEMA_VIEW_FRAGMENT", true);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1727 */       this.m_binder.putLocal(viewName + ".isFailed", "1");
/* 1728 */       this.m_binder.putLocal(viewName + ".error", e.getMessage());
/* 1729 */       int i = 0;
/*      */ 
/* 1740 */       return i;
/*      */     }
/*      */     finally
/*      */     {
/* 1733 */       if (!isOrginalIsJava)
/*      */       {
/* 1735 */         this.m_binder.removeLocal("IsJava");
/*      */       }
/*      */ 
/* 1738 */       if ((contentType != null) && (contentType.length() > 0))
/*      */       {
/* 1740 */         this.m_binder.setContentType(contentType);
/*      */       }
/*      */     }
/*      */ 
/* 1744 */     String publishedTableName = this.m_binder.getLocal("PublishedTableName");
/*      */ 
/* 1746 */     DataResultSet rsView = (DataResultSet)this.m_binder.getResultSet(publishedTableName);
/*      */ 
/* 1748 */     String internalCol = this.m_binder.getLocal("schInternalColumn");
/* 1749 */     String displayCol = this.m_binder.getLocal("schLabelColumn");
/*      */ 
/* 1751 */     if (rsView == null)
/*      */     {
/* 1753 */       if (Report.m_verbose)
/*      */       {
/* 1755 */         Report.trace("idcdisplayfields", "View: " + viewName + " No Result Set found.", null);
/*      */       }
/*      */ 
/* 1758 */       return false;
/*      */     }
/*      */ 
/* 1761 */     if (internalCol == null)
/*      */     {
/* 1763 */       if (Report.m_verbose)
/*      */       {
/* 1765 */         Report.trace("idcdisplayfields", "View: " + viewName + "No Internal Column defined.", null);
/*      */       }
/*      */ 
/* 1768 */       return false;
/*      */     }
/*      */ 
/* 1771 */     boolean isDisplayColExist = true;
/*      */ 
/* 1773 */     if ((displayCol == null) || (displayCol.length() <= 0))
/*      */     {
/* 1775 */       isDisplayColExist = false;
/*      */     }
/* 1777 */     else if (rsView.getFieldInfoIndex(displayCol) < 0)
/*      */     {
/* 1779 */       isDisplayColExist = false;
/*      */     }
/*      */ 
/* 1782 */     boolean isDisplayDefaultColExist = true;
/*      */ 
/* 1784 */     String defaultDisplay = "Display.default";
/*      */ 
/* 1786 */     if (rsView.getFieldInfoIndex(defaultDisplay) < 0)
/*      */     {
/* 1788 */       isDisplayDefaultColExist = false;
/*      */     }
/*      */ 
/* 1791 */     boolean isLocalizedOnDisplay = StringUtils.convertToBool(this.m_binder.getLocal("schLocalizeWhenDisplayed"), false);
/* 1792 */     String viewDisplayColumn = null;
/*      */ 
/* 1794 */     String displayType = "Display";
/*      */ 
/* 1796 */     IdcLocale locale = (IdcLocale)getLocaleResource(0);
/* 1797 */     String localName = null;
/* 1798 */     if (locale != null)
/*      */     {
/* 1800 */       localName = locale.m_name;
/*      */     }
/*      */ 
/* 1803 */     String localLang = (String)getLocaleResource(1);
/*      */ 
/* 1810 */     if ((localName != null) && (localName.length() > 0) && (rsView.getFieldInfoIndex(displayType + "." + localName) >= 0))
/*      */     {
/* 1813 */       viewDisplayColumn = displayType + "." + localName;
/*      */     }
/* 1815 */     else if ((localLang != null) && (localLang.length() > 0) && (rsView.getFieldInfoIndex(displayType + "." + localLang) >= 0))
/*      */     {
/* 1818 */       viewDisplayColumn = displayType + "." + localLang;
/*      */     }
/* 1820 */     else if (isDisplayDefaultColExist)
/*      */     {
/* 1822 */       viewDisplayColumn = defaultDisplay;
/*      */     }
/* 1824 */     else if (isDisplayColExist)
/*      */     {
/* 1826 */       viewDisplayColumn = displayCol;
/*      */     }
/*      */     else
/*      */     {
/* 1830 */       viewDisplayColumn = internalCol;
/*      */     }
/*      */ 
/* 1833 */     boolean isRestricted = StringUtils.convertToBool(this.m_binder.getLocal(fieldName + ":isRestricted"), false);
/*      */ 
/* 1835 */     Vector restrictedvaluesVec = null;
/* 1836 */     if (isRestricted)
/*      */     {
/* 1838 */       restrictedvaluesVec = getRestrictedListValues(fieldName);
/*      */     }
/*      */ 
/* 1841 */     DataResultSet newRSViewData = new DataResultSet(new String[] { "dOption", "dDescription" });
/* 1842 */     int indexOfOption = newRSViewData.getFieldInfoIndex("dOption");
/* 1843 */     int indexOfDesc = newRSViewData.getFieldInfoIndex("dDescription");
/* 1844 */     if (Report.m_verbose)
/*      */     {
/* 1846 */       Report.trace("idcdisplayfields-localization", "View: " + viewName + " -  isLocalizedOnDisplay: " + isLocalizedOnDisplay, null);
/*      */     }
/*      */ 
/* 1849 */     for (rsView.first(); rsView.isRowPresent(); )
/*      */     {
/* 1851 */       String option = rsView.getStringValueByName(internalCol);
/*      */ 
/* 1853 */       if (option == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1859 */       if ((isRestricted) && (restrictedvaluesVec != null) && (isRestrictedFieldValueRemoved(restrictedvaluesVec, option, fieldName)))
/*      */       {
/* 1861 */         if (Report.m_verbose)
/*      */         {
/* 1863 */           Report.trace("idcdisplayfields", "View: " + viewName + " - Restricted option: " + internalCol + " removed", null);
/*      */         }
/*      */ 
/* 1867 */         rsView.deleteCurrentRow();
/*      */       }
/*      */       else
/*      */       {
/* 1871 */         String dDescription = rsView.getStringValueByName(viewDisplayColumn);
/*      */ 
/* 1873 */         if ((isLocalizedOnDisplay) && (dDescription != null))
/*      */         {
/* 1875 */           this.m_binder.pushActiveResultSet("NewData", rsView);
/* 1876 */           dDescription = LocaleResources.getString(dDescription, this);
/*      */         }
/*      */ 
/* 1879 */         if ((dDescription == null) && (isDisplayColExist))
/*      */         {
/* 1881 */           dDescription = rsView.getStringValueByName(displayCol);
/*      */         }
/* 1883 */         else if (dDescription == null)
/*      */         {
/* 1885 */           dDescription = option;
/*      */         }
/*      */ 
/* 1888 */         List row = newRSViewData.createEmptyRowAsList();
/* 1889 */         row.set(indexOfOption, option);
/* 1890 */         row.set(indexOfDesc, dDescription);
/* 1891 */         newRSViewData.addRowWithList(row);
/* 1892 */         rsView.next();
/*      */       }
/*      */     }
/*      */ 
/* 1896 */     if (Report.m_verbose)
/*      */     {
/* 1898 */       Report.trace("idcdisplayfields-localization", "View: " + viewName + " -  Origianl RS fields: " + rsView.getNumFields() + " New RS fields: " + newRSViewData.getNumFields(), null);
/*      */     }
/*      */ 
/* 1903 */     if (isLocalizedOnDisplay)
/*      */     {
/* 1905 */       this.m_binder.popActiveResultSet();
/*      */     }
/*      */ 
/* 1908 */     this.m_binder.addResultSet(fieldName + ".options", newRSViewData);
/* 1909 */     this.m_binder.removeResultSet(publishedTableName);
/*      */ 
/* 1911 */     return true;
/*      */   }
/*      */ 
/*      */   private Vector getRestrictedListValues(String fieldName)
/*      */   {
/* 1920 */     ResultSet rListSet = this.m_binder.getResultSet(fieldName + ".RestrictedList");
/* 1921 */     Vector rvals = new Vector();
/* 1922 */     if ((rListSet == null) || (rListSet.isEmpty()))
/*      */     {
/* 1924 */       return rvals;
/*      */     }
/*      */ 
/* 1927 */     for (rListSet.first(); rListSet.isRowPresent(); rListSet.next())
/*      */     {
/* 1929 */       String value = rListSet.getStringValue(0);
/* 1930 */       if ((value == null) || (value.length() <= 0))
/*      */         continue;
/* 1932 */       rvals.add(value);
/*      */     }
/*      */ 
/* 1936 */     return rvals;
/*      */   }
/*      */ 
/*      */   private boolean retainAllWithRegex(List restrictedList, List optionList, String fieldName)
/*      */   {
/* 1941 */     int optionListSize = optionList.size();
/* 1942 */     boolean isRemoved = false;
/* 1943 */     for (int i = 0; i < optionListSize; ++i)
/*      */     {
/* 1945 */       String option = (String)optionList.get(i);
/* 1946 */       if (!isRestrictedFieldValueRemoved(restrictedList, option, fieldName))
/*      */         continue;
/* 1948 */       optionList.remove(i);
/* 1949 */       --i;
/* 1950 */       --optionListSize;
/* 1951 */       if (isRemoved)
/*      */         continue;
/* 1953 */       isRemoved = true;
/*      */     }
/*      */ 
/* 1959 */     return isRemoved;
/*      */   }
/*      */ 
/*      */   private boolean isRestrictedFieldValueRemoved(List restrictedList, String option, String fieldName)
/*      */   {
/* 1966 */     if ((restrictedList == null) || (restrictedList.size() == 0))
/*      */     {
/* 1968 */       return false;
/*      */     }
/* 1970 */     ResultSet rListsSet = this.m_binder.getResultSet("RestrictedLists");
/* 1971 */     String type = "strict";
/* 1972 */     if ((rListsSet != null) && (!rListsSet.isEmpty()))
/*      */     {
/* 1974 */       for (rListsSet.first(); rListsSet.isRowPresent(); rListsSet.next())
/*      */       {
/* 1976 */         String name = rListsSet.getStringValue(0);
/* 1977 */         if ((name == null) || (!name.equals(fieldName)))
/*      */           continue;
/* 1979 */         type = rListsSet.getStringValue(1);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1984 */     boolean isRemoved = true;
/* 1985 */     if (type.equalsIgnoreCase("strict"))
/*      */     {
/* 1987 */       isRemoved = !restrictedList.contains(option);
/*      */     }
/* 1989 */     else if (type.equalsIgnoreCase("filter"))
/*      */     {
/* 1991 */       for (int i = 0; i < restrictedList.size(); ++i)
/*      */       {
/* 1993 */         String regexStr = (String)restrictedList.get(i);
/* 1994 */         Pattern regex = Pattern.compile(regexStr);
/* 1995 */         if (regex == null)
/*      */           continue;
/* 1997 */         Matcher matcher = regex.matcher(option);
/* 1998 */         if (!matcher.find())
/*      */           continue;
/* 2000 */         isRemoved = false;
/* 2001 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2006 */     return isRemoved;
/*      */   }
/*      */ 
/*      */   public void cleanResponseBinder(Properties orignalData, HashMap returnedRSMap)
/*      */   {
/* 2011 */     boolean isCleanDataBinder = StringUtils.convertToBool(this.m_binder.getLocal("IsCleanDataBinder"), true);
/*      */ 
/* 2013 */     if (!isCleanDataBinder)
/*      */       return;
/* 2015 */     addLocalDataToResponseBinder(orignalData);
/*      */ 
/* 2018 */     Map resultSetsMap = new HashMap(this.m_binder.getResultSets());
/* 2019 */     String rsRemovedList = "";
/* 2020 */     for (String name : resultSetsMap.keySet())
/*      */     {
/* 2022 */       if (!returnedRSMap.containsKey(name))
/*      */       {
/* 2024 */         rsRemovedList = rsRemovedList + name + ",";
/* 2025 */         this.m_binder.removeResultSet(name);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addLocalDataToResponseBinder(Properties orignalData)
/*      */   {
/* 2033 */     for (Iterator i = this.m_binder.getLocalData().keySet().iterator(); i.hasNext(); )
/*      */     {
/* 2035 */       String datakey = (String)i.next();
/* 2036 */       String token = ":isResponseData";
/*      */ 
/* 2038 */       if (datakey.endsWith(token))
/*      */       {
/* 2040 */         boolean isResponseLocalData = StringUtils.convertToBool(this.m_binder.getLocal(datakey), true);
/*      */ 
/* 2042 */         String key = datakey.substring(0, datakey.indexOf(token));
/*      */ 
/* 2044 */         String value = this.m_binder.getLocal(key).trim();
/*      */ 
/* 2046 */         if ((isResponseLocalData) && (value != null) && (!orignalData.contains(key)))
/*      */         {
/* 2049 */           orignalData.put(key, value);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2054 */     this.m_binder.setLocalData(orignalData);
/*      */   }
/*      */ 
/*      */   public String evaluateScript(String script)
/*      */   {
/* 2060 */     String result = "";
/* 2061 */     if (script == null)
/*      */     {
/* 2063 */       return result;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2068 */       result = this.m_pageMerger.evaluateScript(script);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2072 */       Report.trace(null, null, e);
/*      */     }
/*      */ 
/* 2075 */     return result;
/*      */   }
/*      */ 
/*      */   public void loadCustomIdocScriptGetDisplayFieldsSetting()
/*      */   {
/* 2080 */     String tmp = null;
/*      */     try
/*      */     {
/* 2083 */       PageMerger pageMerger = getPageMerger();
/* 2084 */       tmp = pageMerger.evaluateResourceInclude("load_custom_get_display_fields_settings");
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2088 */       Report.trace(null, null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2095 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105030 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocService
 * JD-Core Version:    0.5.4
 */