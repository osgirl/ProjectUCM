/*      */ package intradoc.server.script;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ScriptExtensionsAdaptor;
/*      */ import intradoc.common.ScriptInfo;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.ServiceData;
/*      */ import intradoc.server.schema.SchemaUtils;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.schema.SchemaData;
/*      */ import intradoc.shared.schema.SchemaFieldConfig;
/*      */ import intradoc.shared.schema.SchemaFieldData;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import intradoc.shared.schema.SchemaRelationData;
/*      */ import intradoc.shared.schema.SchemaResultSet;
/*      */ import intradoc.shared.schema.SchemaSecurityFilter;
/*      */ import intradoc.shared.schema.SchemaTreePointer;
/*      */ import intradoc.shared.schema.SchemaViewConfig;
/*      */ import intradoc.shared.schema.SchemaViewData;
/*      */ import intradoc.util.IdcAppendableBase;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SchemaScriptExtensions extends ScriptExtensionsAdaptor
/*      */ {
/*   34 */   protected SchemaHelper m_schemaHelper = null;
/*   35 */   protected SchemaUtils m_schemaUtils = null;
/*   36 */   protected int m_uniqueCounter = 10;
/*      */ 
/*      */   public SchemaScriptExtensions()
/*      */   {
/*   40 */     this.m_variableTable = new String[0];
/*      */ 
/*   42 */     this.m_variableDefinitionTable = new int[0][];
/*      */ 
/*   44 */     this.m_functionTable = new String[] { "loadSchemaData", "clearSchemaData", "getViewValue", "getViewDisplayValue", "getFieldViewValue", "generateJSTreeDefinition", "generateUniqueId", "getTreeNode", "getInitialTreeView", "getTreeDisplayValue", "computeTreeSelectionParents", "getViewValuesResultSet", "getOtherTableColumn", "getMyTableColumn", "getParentValue", "getFieldViewDisplayValue", "getMultiselectSelection", "loadMetaDataDefinition", "buildViewRow", "buildOptionList" };
/*      */ 
/*   75 */     this.m_functionDefinitionTable = new int[][] { { 0, -1, 0, 0, 1 }, { 1, -1, 0, 0, 1 }, { 2, 3, 0, 0, 0 }, { 3, 2, 0, 0, 0 }, { 4, 3, 0, 0, 0 }, { 5, 1, 0, -1, 0 }, { 6, -1, 0, 0, 0 }, { 7, 2, 0, 1, 0 }, { 8, 2, 0, 0, 0 }, { 9, 2, 0, 0, 0 }, { 10, 2, 0, 0, 0 }, { 11, -1, 0, 0, 0 }, { 12, 2, 0, 0, 0 }, { 13, 2, 0, 0, 0 }, { 14, 4, 0, 0, 0 }, { 15, 3, 0, 0, 0 }, { 16, 2, 0, 0, 0 }, { 17, -1, -1, -1, 1 }, { 18, -1, 0, 0, 0 }, { 19, -1, 0, 0, 0 } };
/*      */   }
/*      */ 
/*      */   public void initSchemaHelper(ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/*  102 */     if (this.m_schemaHelper != null)
/*      */     {
/*  105 */       boolean callInit = true;
/*  106 */       if (cxt instanceof Service)
/*      */       {
/*  108 */         Service s = (Service)cxt;
/*  109 */         if (s.isConditionVarTrue("HasInitSchemaHelperInSchemaScript"))
/*      */         {
/*  111 */           callInit = false;
/*      */         }
/*      */         else
/*      */         {
/*  115 */           s.setConditionVar("HasInitSchemaHelperInSchemaScript", true);
/*      */         }
/*      */       }
/*  118 */       if (callInit)
/*      */       {
/*  120 */         this.m_schemaHelper.init();
/*      */       }
/*  122 */       return;
/*      */     }
/*      */ 
/*  125 */     this.m_schemaHelper = ((SchemaHelper)ComponentClassFactory.createClassInstance("SchemaHelper", "intradoc.shared.schema.SchemaHelper", null));
/*      */ 
/*  128 */     this.m_schemaUtils = ((SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", null));
/*      */   }
/*      */ 
/*      */   public boolean evaluateValue(ScriptInfo info, boolean[] bVal, String[] sVal, ExecutionContext context, boolean isConditional)
/*      */   {
/*  136 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/*  137 */     if (binder == null)
/*      */     {
/*  139 */       return false;
/*      */     }
/*      */ 
/*  142 */     int[] config = (int[])(int[])info.m_entry;
/*      */ 
/*  145 */     config[0];
/*      */ 
/*  150 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*  157 */     int[] config = (int[])(int[])info.m_entry;
/*  158 */     String function = info.m_key;
/*  159 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/*  160 */     PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(context);
/*      */ 
/*  162 */     if ((config == null) || (binder == null) || (pageMerger == null))
/*      */     {
/*  164 */       return false;
/*      */     }
/*      */ 
/*  167 */     int returnType = config[4];
/*  168 */     int nargs = args.length - 1;
/*  169 */     int allowedParams = config[1];
/*  170 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*      */     {
/*  172 */       String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*      */ 
/*  174 */       throw new IllegalArgumentException(msg);
/*      */     }
/*      */ 
/*  177 */     initSchemaHelper(context);
/*      */ 
/*  182 */     String sArg1 = null;
/*  183 */     String sArg2 = null;
/*      */ 
/*  185 */     long lArg2 = 0L;
/*  186 */     if ((nargs > 0) && 
/*  188 */       (config[2] == 0))
/*      */     {
/*  190 */       sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*      */     }
/*      */ 
/*  198 */     if (nargs > 1)
/*      */     {
/*  200 */       if (config[3] == 0)
/*      */       {
/*  202 */         sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*      */       }
/*  204 */       else if (config[3] == 1)
/*      */       {
/*  206 */         lArg2 = ScriptUtils.getLongVal(args[1], context);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  211 */     boolean bResult = false;
/*  212 */     int iResult = 0;
/*  213 */     double dResult = 0.0D;
/*  214 */     Object oResult = null;
/*      */ 
/*  216 */     switch (config[0])
/*      */     {
/*      */     case 0:
/*      */     case 1:
/*  221 */       String rsetName = null;
/*  222 */       String objectName = null;
/*  223 */       String prefix = null;
/*  224 */       if (args.length >= 1)
/*      */       {
/*  226 */         rsetName = (String)args[0];
/*      */       }
/*  228 */       if (args.length >= 2)
/*      */       {
/*  230 */         objectName = (String)args[1];
/*      */       }
/*  232 */       if (args.length >= 3)
/*      */       {
/*  234 */         prefix = (String)args[2];
/*      */       }
/*      */       ResultSet rset;
/*      */       ResultSet rset;
/*  237 */       if (rsetName == null)
/*      */       {
/*  239 */         rset = binder.m_currentResultSet;
/*      */       }
/*      */       else
/*      */       {
/*  243 */         rset = binder.getResultSet(rsetName);
/*  244 */         if ((rset == null) && ((rset = SharedObjects.getTable(rsetName)) == null))
/*      */         {
/*  246 */           String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, rsetName);
/*      */ 
/*  248 */           throw new IllegalArgumentException(msg);
/*      */         }
/*      */       }
/*  251 */       if ((rset == null) || (!rset instanceof SchemaResultSet))
/*      */       {
/*  253 */         throw new IllegalArgumentException("!csNotASchemaResultSet");
/*      */       }
/*  255 */       SchemaResultSet srset = (SchemaResultSet)rset;
/*      */ 
/*  257 */       SchemaData data = srset.getData(objectName);
/*  258 */       if (data == null)
/*      */       {
/*  260 */         if ((SystemUtils.m_verbose) || ((objectName != null) && (objectName.startsWith("x"))))
/*      */         {
/*  262 */           Report.debug("schemastorage", "schema object '" + objectName + "' not found in '" + rsetName + "'", null);
/*      */         }
/*      */ 
/*  265 */         bResult = false;
/*      */       }
/*      */       else
/*      */       {
/*  269 */         bResult = true;
/*  270 */         if (config[0] == 0)
/*      */         {
/*  272 */           data.populateBinder(binder, prefix);
/*      */         }
/*      */         else
/*      */         {
/*  276 */           data.clearBinder(binder, prefix);
/*      */         }
/*      */       }
/*  279 */       break;
/*      */     case 2:
/*  284 */       String sArg3 = null;
/*  285 */       if (args[2] != null)
/*      */       {
/*  287 */         sArg3 = ScriptUtils.getDisplayString(args[2], context);
/*      */       }
/*  289 */       checkNonEmpty(sArg1);
/*  290 */       checkNonEmpty(sArg3);
/*  291 */       SchemaViewData data = getViewDefinition("getViewValue", sArg1);
/*  292 */       if (data != null)
/*      */       {
/*  296 */         ResultSet rset = getViewValues("getViewValue", data, sArg2, context);
/*  297 */         if ((rset == null) || (!rset.isRowPresent()))
/*      */         {
/*  299 */           if (sArg2.length() == 0)
/*      */           {
/*  301 */             oResult = "";
/*      */           }
/*      */           else
/*      */           {
/*  305 */             oResult = "err";
/*      */           }
/*      */         }
/*      */         else {
/*  309 */           FieldInfo theField = new FieldInfo();
/*  310 */           if (!rset.getFieldInfo(sArg3, theField))
/*      */           {
/*  312 */             Report.trace("schemapagecreation", "the column '" + sArg3 + "' isn't defined in the view '" + sArg1 + "'", null);
/*      */           }
/*      */           else
/*      */           {
/*  317 */             oResult = getResultSetValue(rset, theField);
/*      */           }
/*      */         }
/*      */       }
/*  319 */       break;
/*      */     case 3:
/*  325 */       if ((sArg1 == null) || (sArg1.equals("")))
/*      */       {
/*  327 */         oResult = sArg2;
/*      */       }
/*      */       else
/*      */       {
/*  331 */         oResult = getViewDisplayValue("getViewDisplayValue", sArg1, sArg2, context);
/*  332 */         if (oResult == null)
/*      */         {
/*  334 */           oResult = "err"; } 
/*  334 */       }break;
/*      */     case 4:
/*  340 */       checkNonEmpty(sArg1);
/*  341 */       String clmnName = null;
/*  342 */       if (nargs == 3)
/*      */       {
/*  344 */         clmnName = ScriptUtils.getDisplayString(args[2], context);
/*      */       }
/*      */ 
/*  348 */       SchemaViewData view = getFieldViewDefinition("getFieldValue", sArg1);
/*      */ 
/*  350 */       oResult = "";
/*  351 */       if ((clmnName == null) || (clmnName.length() == 0))
/*      */       {
/*  354 */         clmnName = view.get("schInternalColumn");
/*      */       }
/*  356 */       ResultSet rset = getViewValues("getFieldValue", view, sArg2, context);
/*  357 */       if (rset != null)
/*      */       {
/*  359 */         FieldInfo fi = new FieldInfo();
/*  360 */         if (rset.getFieldInfo(clmnName, fi))
/*      */         {
/*  362 */           oResult = getResultSetValue(rset, fi);
/*      */         }
/*      */         else
/*      */         {
/*  371 */           oResult = "err";
/*  372 */           Report.trace("schemapagecreation", "The idoc script function getFieldViewValue has encountered an error: the column " + clmnName + " is not in the view for field " + sArg1, null);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  376 */       break;
/*      */     case 5:
/*      */       try
/*      */       {
/*  383 */         Object[] treeDef = this.m_schemaHelper.expandTreeDefinition(sArg1);
/*  384 */         oResult = this.m_schemaHelper.createJavaScriptTreeDefinition(treeDef);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  389 */         Report.trace("schema", null, e);
/*  390 */         return false;
/*      */       }
/*      */     case 6:
/*      */       String separator;
/*      */       String separator;
/*  395 */       if (sArg2 != null)
/*      */       {
/*  397 */         separator = sArg2;
/*      */       }
/*      */       else
/*      */       {
/*  401 */         separator = "-";
/*      */       }
/*      */ 
/*  404 */       oResult = sArg1 + separator + this.m_uniqueCounter++;
/*  405 */       if (this.m_uniqueCounter >= 100000)
/*      */       {
/*  407 */         this.m_uniqueCounter = 10;
/*      */       }
/*      */ 
/*  407 */       break;
/*      */     case 7:
/*      */     case 8:
/*      */       try
/*      */       {
/*  415 */         Object[] treeDefinition = this.m_schemaHelper.expandTreeDefinition(sArg1);
/*  416 */         switch (config[0])
/*      */         {
/*      */         case 7:
/*  420 */           int theIndex = (int)lArg2;
/*  421 */           if ((theIndex < treeDefinition.length) && 
/*  423 */             (treeDefinition[theIndex] instanceof SchemaData))
/*      */           {
/*  425 */             oResult = ((SchemaData)treeDefinition[theIndex]).m_name; } break;
/*      */         case 8:
/*  432 */           if (treeDefinition[(treeDefinition.length - 1)] instanceof SchemaTreePointer)
/*      */           {
/*  435 */             SchemaTreePointer stp = (SchemaTreePointer)treeDefinition[(treeDefinition.length - 1)];
/*      */ 
/*  437 */             if (stp.m_recursiveIndex == 0)
/*      */             {
/*  439 */               SchemaViewData view = (SchemaViewData)treeDefinition[0];
/*      */ 
/*  441 */               SchemaRelationData relationship = (SchemaRelationData)treeDefinition[1];
/*      */ 
/*  443 */               IdcStringBuilder path = new IdcStringBuilder();
/*  444 */               path.append(StringUtils.encodeJavascriptFilename(view.m_name));
/*      */ 
/*  446 */               path.append("/");
/*  447 */               path.append(StringUtils.encodeJavascriptFilename(relationship.m_name));
/*      */ 
/*  449 */               path.append("/");
/*  450 */               if (sArg2 == null)
/*      */               {
/*  452 */                 sArg2 = stp.m_initialKeyValue;
/*  453 */                 if (sArg2 == null)
/*      */                 {
/*  455 */                   sArg2 = "0";
/*      */                 }
/*      */               }
/*  458 */               path.append(StringUtils.encodeJavascriptFilename(sArg2));
/*      */ 
/*  460 */               oResult = path.toString();
/*      */             }
/*      */             else
/*      */             {
/*  464 */               oResult = StringUtils.encodeJavascriptFilename(((SchemaData)treeDefinition[0]).m_name);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  474 */         Report.trace("schema", null, e);
/*  475 */         return false;
/*      */       }
/*      */ 
/*      */     case 9:
/*      */       try
/*      */       {
/*  481 */         checkNonEmpty(sArg1);
/*  482 */         oResult = getDisplayFieldValue(sArg1, sArg2, null, true, binder, context);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  486 */         oResult = "err";
/*  487 */         Report.trace("schemapagecreation", "unable to get display value for field '" + sArg1 + "' with value '" + sArg2 + "'", e);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  493 */         oResult = "err";
/*  494 */         Report.trace("schemapagecreation", "unable to get display value for field '" + sArg1 + "' with value '" + sArg2 + "'", e);
/*      */       }
/*      */ 
/*  498 */       break;
/*      */     case 10:
/*      */       try
/*      */       {
/*  503 */         SchemaFieldData fieldData = (SchemaFieldData)this.m_schemaHelper.m_fields.getData(sArg1);
/*      */ 
/*  505 */         if (fieldData == null)
/*      */         {
/*  507 */           Report.trace("schemapagecreation", "computeTreeSelectionParents(): the field '" + sArg1 + "' doesn't exist", null);
/*      */         }
/*      */         else
/*      */         {
/*  512 */           String treeDefinitionString = fieldData.get("TreeDefinition");
/*      */ 
/*  514 */           if (treeDefinitionString == null)
/*      */           {
/*  516 */             Report.trace("schemapagecreation", "computeTreeSelectionParents(): the field '" + sArg1 + "' doesn't have a tree definition.", null);
/*      */           }
/*  521 */           else if (sArg2.length() == 0)
/*      */           {
/*  523 */             oResult = "";
/*      */           }
/*      */           else
/*      */           {
/*  527 */             Object[] treeDefinition = this.m_schemaHelper.expandTreeDefinition(treeDefinitionString);
/*      */ 
/*  529 */             Vector values = this.m_schemaHelper.computeTreeSelectionParents(treeDefinition, sArg2);
/*      */ 
/*  531 */             if (values == null)
/*      */             {
/*  533 */               Report.trace("schemapagecreation", "computeTreeSelectionParents(): unable to compute the path for '" + sArg2 + "'", null);
/*      */             }
/*      */             else
/*      */             {
/*  538 */               IdcStringBuilder buf = new IdcStringBuilder();
/*  539 */               String separator = fieldData.get("TreeNodeStorageSeparator", "/");
/*  540 */               for (int i = 0; i < values.size(); ++i)
/*      */               {
/*  542 */                 if (i > 0)
/*      */                 {
/*  544 */                   buf.append(separator);
/*      */                 }
/*  546 */                 buf.append((String)values.elementAt(i));
/*      */               }
/*  548 */               oResult = buf.toString();
/*      */             }
/*      */           }
/*      */         }
/*      */       } catch (Exception e) {
/*  552 */         Report.trace("schema", null, e);
/*  553 */         return false;
/*      */       }
/*      */ 
/*      */     case 11:
/*      */       try
/*      */       {
/*  569 */         Map argMap = new HashMap();
/*  570 */         if (args.length > 3)
/*      */         {
/*  572 */           List l = StringUtils.makeListFromSequenceSimple(ScriptUtils.getDisplayString(args[3], context));
/*      */ 
/*  574 */           String[] fieldNames = new String[l.size()];
/*  575 */           String[] fieldValues = new String[l.size()];
/*  576 */           for (int i = 0; i < fieldNames.length; ++i)
/*      */           {
/*  578 */             String filter = (String)l.get(i);
/*  579 */             int index = filter.indexOf("=");
/*  580 */             fieldNames[i] = filter.substring(0, index);
/*  581 */             fieldValues[i] = filter.substring(index + 1);
/*      */           }
/*  583 */           argMap.put("fieldNames", fieldNames);
/*  584 */           argMap.put("fieldValues", fieldValues);
/*      */         }
/*  586 */         getViewValuesResultSetImplement(sArg1, sArg2, (args.length > 2) ? ScriptUtils.getDisplayString(args[2], context) : null, this.m_schemaHelper, binder, argMap, context);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  593 */         Report.trace("schema", null, e);
/*  594 */         throw new IllegalArgumentException(e.getMessage());
/*      */       }
/*      */ 
/*      */     case 12:
/*  599 */       checkNonEmpty(sArg1);
/*  600 */       checkNonEmpty(sArg2);
/*  601 */       oResult = this.m_schemaHelper.getOtherTableColumn(sArg1, sArg2);
/*      */ 
/*  603 */       break;
/*      */     case 13:
/*  606 */       checkNonEmpty(sArg1);
/*  607 */       checkNonEmpty(sArg2);
/*  608 */       oResult = this.m_schemaHelper.getMyTableColumn(sArg1, sArg2);
/*      */ 
/*  610 */       break;
/*      */     case 14:
/*      */       try
/*      */       {
/*  617 */         String sArg3 = null;
/*  618 */         String sArg4 = null;
/*  619 */         if (args[2] != null)
/*      */         {
/*  621 */           sArg3 = ScriptUtils.getDisplayString(args[2], context);
/*      */         }
/*  623 */         if (args[3] != null)
/*      */         {
/*  625 */           sArg4 = ScriptUtils.getDisplayString(args[3], context);
/*      */         }
/*  627 */         if ((sArg4 == null) || (sArg4.length() == 0))
/*      */         {
/*  629 */           oResult = "";
/*      */         }
/*      */         else {
/*  632 */           checkNonEmpty(sArg1);
/*  633 */           checkNonEmpty(sArg2);
/*  634 */           checkNonEmpty(sArg3);
/*      */ 
/*  636 */           String otherColumn = this.m_schemaHelper.getOtherTableColumn(sArg1, sArg2);
/*  637 */           if (otherColumn == null)
/*      */           {
/*  639 */             Report.trace("schemapagecreation", "unable to find the column in  the other table for view '" + sArg1 + "', relationship '" + sArg2 + "', parent field '" + sArg3 + "'", null);
/*      */           }
/*      */           else
/*      */           {
/*  644 */             SchemaFieldData parentField = (SchemaFieldData)this.m_schemaHelper.m_fields.getData(sArg3);
/*      */ 
/*  647 */             if (parentField == null)
/*      */             {
/*  649 */               Report.trace("schemapagecreation", "getParentValue('" + sArg1 + "','" + sArg2 + "','" + sArg3 + "','" + sArg4 + "'): Error. Unable to find the parent field " + sArg3, null);
/*      */             }
/*      */             else
/*      */             {
/*  655 */               String parentViewName = parentField.get("OptionViewKey");
/*  656 */               if (parentViewName == null)
/*      */               {
/*  658 */                 parentViewName = parentField.get("dOptionListKey");
/*      */               }
/*  660 */               SchemaViewData parentView = this.m_schemaHelper.getView(parentViewName);
/*  661 */               if (parentView == null)
/*      */               {
/*  663 */                 String errMsg = LocaleUtils.encodeMessage("csSchGetValueParentViewMissing", null, sArg3, parentViewName);
/*  664 */                 errMsg = LocaleUtils.encodeMessage("csSchGetParentValueError", errMsg, sArg1, sArg2, sArg3);
/*  665 */                 throw new ServiceException(errMsg);
/*      */               }
/*      */ 
/*  668 */               parentViewName = parentView.m_name;
/*  669 */               ResultSet values = getViewValues("getParentValue", parentView, sArg4, context);
/*      */ 
/*  671 */               if (values == null)
/*      */               {
/*  673 */                 Report.trace("schemapagecreation", "zero results for view '" + parentView + "', value '" + sArg4, null);
/*      */               }
/*      */               else
/*      */               {
/*  677 */                 FieldInfo theField = new FieldInfo();
/*  678 */                 if (!values.getFieldInfo(otherColumn, theField))
/*      */                 {
/*  680 */                   Report.trace("scehmapagecreation", "the column '" + otherColumn + "' isn't defined in the view '" + parentViewName + "'", null);
/*      */                 }
/*      */                 else
/*      */                 {
/*  685 */                   oResult = getResultSetValue(values, theField);
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */       } catch (ServiceException e) {
/*  689 */         Report.trace("schema", null, e);
/*  690 */         return false;
/*      */       }
/*      */ 
/*      */     case 15:
/*      */       try
/*      */       {
/*  696 */         String sArg3 = "";
/*  697 */         if (args[2] != null)
/*      */         {
/*  699 */           sArg3 = ScriptUtils.getDisplayString(args[2], context);
/*      */         }
/*  701 */         checkNonEmpty(sArg1);
/*  702 */         checkNonEmpty(sArg2);
/*  703 */         oResult = getDisplayFieldValue(sArg1, sArg3, sArg2, false, binder, context);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  707 */         oResult = "err";
/*  708 */         Report.trace("schemapagecreation", "unable to get display value for field '" + sArg1 + "' with value '" + sArg2 + "'", e);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  714 */         oResult = "err";
/*  715 */         Report.trace("schemapagecreation", "unable to get display value for field '" + sArg1 + "' with value '" + sArg2 + "'", e);
/*      */       }
/*      */ 
/*  719 */       break;
/*      */     case 16:
/*      */       try
/*      */       {
/*  724 */         checkNonEmpty(sArg1);
/*  725 */         if ((sArg2 == null) || (sArg2.length() == 0))
/*      */         {
/*  727 */           oResult = "";
/*      */         }
/*      */         else {
/*  730 */           SchemaFieldData field = this.m_schemaHelper.getField(sArg1);
/*  731 */           String value = sArg2;
/*  732 */           String optionListType = "";
/*  733 */           if (field != null)
/*      */           {
/*  735 */             optionListType = field.get("dOptionListType");
/*      */           }
/*  737 */           if (optionListType == null)
/*      */           {
/*  739 */             optionListType = "";
/*      */           }
/*  741 */           oResult = value;
/*  742 */           if (field != null)
/*      */           {
/*  747 */             boolean isMulti2 = optionListType.indexOf("multi2") >= 0;
/*  748 */             boolean isTree = this.m_schemaHelper.isTreeField(field);
/*  749 */             boolean isDependentList = field.getBoolean("IsDependentList", false);
/*  750 */             boolean addParents = ((isDependentList) && (isMulti2)) || (isTree);
/*      */ 
/*  752 */             List keys = parseMultiselectValueString(field, value);
/*  753 */             IdcStringBuilder selectionBuffer = new IdcStringBuilder();
/*  754 */             String sep = field.get("MultiselectStorageSeparator", ", ");
/*      */ 
/*  757 */             SchemaViewData viewData = null;
/*  758 */             String parentColumnName = "";
/*  759 */             IdcStringBuilder parentValueBuilder = null;
/*  760 */             if (addParents)
/*      */             {
/*  762 */               parentValueBuilder = new IdcStringBuilder();
/*  763 */               parentValueBuilder.m_disableToStringReleaseBuffers = true;
/*  764 */               if (!isTree)
/*      */               {
/*  766 */                 viewData = getFieldViewDefinition("getMultiselectSelection", field.m_name);
/*      */ 
/*  768 */                 parentColumnName = this.m_schemaHelper.getMyTableColumn(viewData.m_name, field.get("DependentRelationship"));
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*  773 */             for (int i = 0; i < keys.size(); ++i)
/*      */             {
/*  775 */               String selectionValue = (String)keys.get(i);
/*  776 */               if (i > 0)
/*      */               {
/*  778 */                 selectionBuffer.append(sep);
/*      */               }
/*  780 */               if (parentValueBuilder != null)
/*      */               {
/*  782 */                 parentValueBuilder.setLength(0);
/*  783 */                 ResultSet rset = null;
/*  784 */                 SchemaRelationData relationData = null;
/*  785 */                 if (isTree)
/*      */                 {
/*  787 */                   viewData = null;
/*  788 */                   parentColumnName = null;
/*  789 */                   String treeDefinitionString = field.get("TreeDefinition");
/*  790 */                   Object[] treeDefinition = null;
/*  791 */                   SchemaViewData[] viewDataPtr = { null };
/*  792 */                   SchemaRelationData[] relationDataPtr = { null };
/*  793 */                   if (treeDefinitionString != null)
/*      */                   {
/*      */                     try
/*      */                     {
/*  797 */                       treeDefinition = this.m_schemaHelper.expandTreeDefinition(treeDefinitionString);
/*      */                     }
/*      */                     catch (DataException e)
/*      */                     {
/*  801 */                       throw new ServiceException(e);
/*      */                     }
/*      */                   }
/*  804 */                   if (treeDefinition != null)
/*      */                   {
/*  806 */                     if (field.getBoolean("StoreSelectionPath", false))
/*      */                     {
/*  809 */                       String fieldSeparator = field.get("TreeNodeStorageSeparator", "/");
/*      */ 
/*  812 */                       Vector values = StringUtils.parseArray(selectionValue, fieldSeparator.charAt(0), '^');
/*      */ 
/*  814 */                       if (values.size() > 0)
/*      */                       {
/*  816 */                         selectionValue = (String)values.get(values.size() - 1);
/*      */                       }
/*      */                     }
/*  819 */                     rset = this.m_schemaHelper.getTreeSelectionRow(treeDefinition, selectionValue, viewDataPtr, relationDataPtr);
/*      */ 
/*  821 */                     viewData = viewDataPtr[0];
/*  822 */                     relationData = relationDataPtr[0];
/*  823 */                     if ((viewData != null) && (relationData != null))
/*      */                     {
/*  825 */                       parentColumnName = this.m_schemaHelper.getMyTableColumn(viewData.m_name, relationData.m_name);
/*      */                     }
/*      */                   }
/*      */ 
/*      */                 }
/*      */                 else
/*      */                 {
/*  832 */                   rset = getViewValues("getMultiselectSelection", viewData, selectionValue, context);
/*      */                 }
/*      */ 
/*  835 */                 if (viewData != null)
/*      */                 {
/*  837 */                   if (isTree)
/*      */                   {
/*  840 */                     parentValueBuilder.append(viewData.m_name);
/*  841 */                     parentValueBuilder.append(":");
/*  842 */                     if (relationData != null)
/*      */                     {
/*  844 */                       parentValueBuilder.append(relationData.m_name);
/*      */                     }
/*  846 */                     parentValueBuilder.append(":");
/*      */                   }
/*  848 */                   if (parentColumnName != null)
/*      */                   {
/*  850 */                     if (rset != null)
/*      */                     {
/*  852 */                       FieldInfo fi = new FieldInfo();
/*  853 */                       if (rset.getFieldInfo(parentColumnName, fi))
/*      */                       {
/*  855 */                         parentValueBuilder.append(rset.getStringValue(fi.m_index));
/*      */                       }
/*      */                       else
/*      */                       {
/*  859 */                         Report.trace("schemapagecreation", "unable to find parent column info '" + parentColumnName + "' in view '" + viewData.m_name + "'", null);
/*      */                       }
/*      */ 
/*      */                     }
/*      */                     else
/*      */                     {
/*  866 */                       Report.trace("schemapagecreation", "unable to find resultset values for view '" + viewData.m_name + "' selection value '" + selectionValue + "'", null);
/*      */                     }
/*      */ 
/*      */                   }
/*      */ 
/*      */                 }
/*      */                 else
/*      */                 {
/*  875 */                   Report.trace("schemapagecreation", "unable to determine view for selection value '" + selectionValue + "'", null);
/*      */                 }
/*      */ 
/*  878 */                 if (parentValueBuilder.length() == 0)
/*      */                 {
/*  880 */                   parentValueBuilder.append("err");
/*      */                 }
/*  882 */                 selectionBuffer.append(parentValueBuilder);
/*  883 */                 selectionBuffer.append(sep);
/*      */               }
/*  885 */               selectionBuffer.append(selectionValue);
/*      */             }
/*  887 */             if (parentValueBuilder != null)
/*      */             {
/*  889 */               parentValueBuilder.releaseBuffers();
/*      */             }
/*  891 */             oResult = selectionBuffer.toString();
/*      */           }
/*      */         }
/*      */       } catch (ServiceException e) {
/*  895 */         oResult = "err";
/*  896 */         Report.trace("schemapagecreation", "unable to get selection value for field '" + sArg1 + "' with value '" + sArg2 + "'", e);
/*      */       }
/*      */ 
/*  900 */       break;
/*      */     case 17:
/*      */       try
/*      */       {
/*  907 */         DataResultSet fieldSet = new DataResultSet();
/*  908 */         DataResultSet metaSet = SharedObjects.getTable("DocMetaDefinition");
/*  909 */         fieldSet.copyFieldInfo(metaSet);
/*      */ 
/*  912 */         Vector v = ResultSetUtils.createFieldInfo(new String[] { "schFieldTarget", "displayScript" }, 100);
/*  913 */         fieldSet.mergeFieldsWithFlags(v, 0);
/*      */ 
/*  916 */         DataResultSet incSet = SharedObjects.getTable("StandardFieldIncludes");
/*  917 */         DataResultSet tmpSet = new DataResultSet();
/*  918 */         tmpSet.copy(incSet);
/*  919 */         tmpSet.renameField("fieldName", "dName");
/*  920 */         tmpSet.renameField("type", "dType");
/*  921 */         fieldSet.merge(null, tmpSet, false);
/*      */ 
/*  923 */         fieldSet.merge(null, metaSet, false);
/*      */ 
/*  926 */         int nameIndex = ResultSetUtils.getIndexMustExist(fieldSet, "dName");
/*      */ 
/*  928 */         DataBinder workBinder = new DataBinder();
/*  929 */         SchemaFieldConfig fieldConfig = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/*  930 */         for (fieldConfig.first(); fieldConfig.isRowPresent(); fieldConfig.next())
/*      */         {
/*  932 */           String name = fieldConfig.getStringValue(fieldConfig.m_nameIndex);
/*  933 */           Vector row = fieldSet.findRow(nameIndex, name);
/*  934 */           if (row != null)
/*      */             continue;
/*  936 */           SchemaData data = fieldConfig.getData(name);
/*  937 */           for (int i = 0; i < fieldSet.getNumFields(); ++i)
/*      */           {
/*  939 */             FieldInfo fi = new FieldInfo();
/*  940 */             fieldSet.getIndexFieldInfo(i, fi);
/*      */ 
/*  942 */             String key = fi.m_name;
/*  943 */             String val = null;
/*      */ 
/*  946 */             if (key.equals("dName"))
/*      */             {
/*  948 */               val = data.get("schFieldName");
/*      */             }
/*  950 */             else if (key.equals("dType"))
/*      */             {
/*  952 */               val = data.get("schFieldType");
/*      */             }
/*  954 */             else if (key.equals("dCaption"))
/*      */             {
/*  956 */               val = data.get("schFieldCaption");
/*      */             }
/*      */             else
/*      */             {
/*  960 */               val = data.get(key);
/*      */             }
/*      */ 
/*  963 */             if (val == null)
/*      */             {
/*  965 */               val = "";
/*      */             }
/*  967 */             workBinder.putLocal(key, val);
/*      */           }
/*      */ 
/*  970 */           row = fieldSet.createRow(workBinder);
/*  971 */           fieldSet.addRow(row);
/*      */         }
/*      */ 
/*  975 */         fieldSet.first();
/*  976 */         binder.addResultSet("MetaDataDefinition", fieldSet);
/*  977 */         binder.pushActiveResultSet("MetaDataDefinition", fieldSet);
/*  978 */         bResult = true;
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  982 */         Report.trace(null, "Unable to load meta data definition set.", e);
/*      */       }
/*  984 */       break;
/*      */     case 18:
/*  989 */       long lArg3 = 0L;
/*      */ 
/*  991 */       if (args.length > 2)
/*      */       {
/*  993 */         lArg3 = ScriptUtils.getLongVal(args[2], context);
/*      */       }
/*      */ 
/*  996 */       oResult = buildViewRow(sArg1, sArg2, (int)lArg3, binder);
/*  997 */       break;
/*      */     case 19:
/*      */       try
/*      */       {
/* 1002 */         oResult = buildOptionList(sArg1, sArg2, binder, context);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1006 */         oResult = "err";
/* 1007 */         Report.trace("buildOptionList", "unable to build option list for field '" + sArg1 + "' with value '" + sArg2 + "'", e);
/*      */       }
/*      */ 
/* 1011 */       break;
/*      */     default:
/* 1014 */       return false;
/*      */     }
/*      */ 
/* 1018 */     args[nargs] = ScriptExtensionUtils.computeReturnObject(returnType, bResult, iResult, dResult, oResult);
/*      */ 
/* 1021 */     return true;
/*      */   }
/*      */ 
/*      */   public String buildViewRow(String publishedTableName, String arrayName, int arrayIndex, DataBinder binder)
/*      */   {
/* 1026 */     IdcStringBuilder viewRow = new IdcStringBuilder("r = {");
/* 1027 */     String defaultFieldNamePrefix = "";
/* 1028 */     String defaultFieldValue = "";
/* 1029 */     String defaultEnd = ".default";
/*      */ 
/* 1031 */     if ((arrayName == null) || (arrayName.equals("")))
/*      */     {
/* 1033 */       arrayName = "rows";
/*      */     }
/*      */ 
/* 1036 */     ResultSet rsetPublished = binder.getResultSet(publishedTableName);
/*      */ 
/* 1040 */     int numOfFields = rsetPublished.getNumFields();
/* 1041 */     for (int fieldNum = 0; fieldNum < numOfFields; ++fieldNum)
/*      */     {
/* 1043 */       String fieldName = StringUtils.encodeJavascriptString(rsetPublished.getFieldName(fieldNum));
/* 1044 */       String fieldValue = StringUtils.encodeJavascriptString(rsetPublished.getStringValue(fieldNum));
/*      */ 
/* 1046 */       if ((!defaultFieldNamePrefix.equals("")) && (fieldName.startsWith(defaultFieldNamePrefix))) { if (fieldValue.equalsIgnoreCase(defaultFieldValue)) continue; if (fieldValue.equals("")) {
/*      */           continue;
/*      */         }
/*      */  }
/*      */ 
/*      */ 
/* 1052 */       if (fieldNum > 0)
/*      */       {
/* 1054 */         viewRow = (IdcStringBuilder)viewRow.append(",");
/*      */       }
/*      */ 
/* 1057 */       viewRow = (IdcStringBuilder)viewRow.append("\"").append(fieldName).append("\"");
/* 1058 */       viewRow = (IdcStringBuilder)viewRow.append(":").append("\"").append(fieldValue).append("\"");
/*      */ 
/* 1060 */       int indexOfDot = fieldName.indexOf(".");
/*      */ 
/* 1062 */       if ((indexOfDot == -1) || (!fieldName.substring(indexOfDot).equalsIgnoreCase(defaultEnd)))
/*      */         continue;
/* 1064 */       defaultFieldNamePrefix = fieldName.substring(0, indexOfDot);
/* 1065 */       defaultFieldValue = fieldValue;
/*      */     }
/*      */ 
/* 1069 */     viewRow.append("};\n");
/*      */ 
/* 1071 */     viewRow.append(arrayName).append("[").append(Integer.toString(arrayIndex)).append("] = r;");
/*      */ 
/* 1073 */     arrayIndex += 1;
/* 1074 */     if (arrayIndex % 1000 == 0)
/*      */     {
/* 1076 */       Report.trace(null, "Finished defining " + arrayIndex + " rows", null);
/*      */     }
/*      */ 
/* 1079 */     return viewRow.toString();
/*      */   }
/*      */ 
/*      */   public String buildOptionList(String publishedTableName, String arrayName, DataBinder binder, ExecutionContext context) throws DataException, ServiceException
/*      */   {
/* 1084 */     boolean isPublishedTableNameInBinder = false;
/* 1085 */     if (binder.getLocal("publishedTableName") == null)
/*      */     {
/* 1087 */       binder.putLocal("publishedTableName", publishedTableName);
/* 1088 */       isPublishedTableNameInBinder = true;
/*      */     }
/* 1090 */     PluginFilters.filter("preBuildOptionList", null, binder, context);
/* 1091 */     if (isPublishedTableNameInBinder)
/*      */     {
/* 1093 */       binder.removeLocal("publishedTableName");
/*      */     }
/*      */ 
/* 1096 */     IdcStringBuilder optionRows = new IdcStringBuilder();
/* 1097 */     ResultSet rsetPublished = binder.getResultSet(publishedTableName);
/* 1098 */     int arrayIndex = 0;
/*      */ 
/* 1100 */     if ((arrayName == null) || (arrayName.equals("")))
/*      */     {
/* 1102 */       arrayName = "rows";
/*      */     }
/*      */ 
/* 1105 */     while ((rsetPublished.first()) && 
/* 1107 */       (rsetPublished.isRowPresent()))
/*      */     {
/* 1109 */       String optionRow = buildViewRow(publishedTableName, arrayName, arrayIndex, binder);
/* 1110 */       optionRows.append(optionRow).append("\n");
/* 1111 */       rsetPublished.next();
/*      */ 
/* 1113 */       arrayIndex += 1;
/*      */     }
/*      */ 
/* 1117 */     return optionRows.toString();
/*      */   }
/*      */ 
/*      */   public List parseMultiselectValueString(SchemaFieldData field, String value)
/*      */   {
/* 1123 */     String sep = field.get("MultiselectStorageSeparator", ", ");
/* 1124 */     List keys = parseList(value, sep);
/* 1125 */     return keys;
/*      */   }
/*      */ 
/*      */   public List parseList(String value, String sep)
/*      */   {
/* 1130 */     int sepLength = sep.length();
/*      */ 
/* 1132 */     List keys = new ArrayList();
/* 1133 */     while ((index = value.indexOf(sep)) >= 0)
/*      */     {
/*      */       int index;
/* 1135 */       String key = value.substring(0, index);
/* 1136 */       value = value.substring(index + sepLength);
/* 1137 */       if (key.length() == 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1142 */       keys.add(key.trim());
/*      */     }
/* 1144 */     if (value.length() > 0)
/*      */     {
/* 1146 */       keys.add(value.trim());
/*      */     }
/* 1148 */     return keys;
/*      */   }
/*      */ 
/*      */   protected SchemaFieldData getFieldDefinition(String function, String fieldName)
/*      */   {
/* 1156 */     SchemaFieldConfig fields = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/* 1157 */     SchemaFieldData field = (SchemaFieldData)fields.getData(fieldName);
/* 1158 */     return field;
/*      */   }
/*      */ 
/*      */   protected SchemaViewData getFieldViewDefinition(String function, String fieldName)
/*      */   {
/* 1163 */     SchemaFieldData field = getFieldDefinition(function, fieldName);
/* 1164 */     SchemaViewData view = null;
/*      */ 
/* 1166 */     if (field == null)
/*      */     {
/* 1168 */       Report.trace("schemapagecreation", function + "() the field '" + fieldName + "' does not exist", null);
/*      */     }
/*      */     else
/*      */     {
/* 1173 */       SchemaHelper helper = new SchemaHelper();
/* 1174 */       String[] name = new String[1];
/* 1175 */       boolean isView = helper.isViewFieldEx(field, name);
/* 1176 */       if (isView)
/*      */       {
/* 1178 */         view = getViewDefinition(function, name[0]);
/*      */       }
/*      */       else
/*      */       {
/* 1182 */         Report.trace("schemapagecreation", function + "() the field '" + fieldName + "' is not a view", null);
/*      */       }
/*      */     }
/*      */ 
/* 1186 */     return view;
/*      */   }
/*      */ 
/*      */   protected SchemaViewData getViewDefinition(String function, String viewName)
/*      */   {
/* 1191 */     SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*      */ 
/* 1193 */     SchemaViewData def = (SchemaViewData)views.getData(viewName);
/*      */ 
/* 1195 */     if (def == null)
/*      */     {
/* 1197 */       Report.trace("schemapagecreation", function + "() the view '" + viewName + "' doesn't exist", null);
/*      */     }
/*      */ 
/* 1200 */     return def;
/*      */   }
/*      */ 
/*      */   protected String getDisplayFieldValue(String fieldName, String value, String viewName, boolean isTree, DataBinder binder, ExecutionContext context)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1206 */     SchemaFieldData field = null;
/* 1207 */     String filterIsTreeBool = (isTree) ? "1" : "";
/* 1208 */     field = this.m_schemaHelper.getField(fieldName);
/* 1209 */     if ((field == null) && (isTree))
/*      */     {
/* 1211 */       Report.trace("schemapagecreation", "Looking for tree display value of " + value + " and no field definition found for field " + fieldName, null);
/*      */ 
/* 1213 */       return null;
/*      */     }
/* 1215 */     String optionListType = "";
/* 1216 */     if (field != null)
/*      */     {
/* 1218 */       optionListType = field.get("dOptionListType");
/*      */     }
/* 1220 */     if (optionListType == null)
/*      */     {
/* 1222 */       optionListType = "";
/*      */     }
/* 1224 */     String result = null;
/* 1225 */     if (optionListType.indexOf("multi") >= 0)
/*      */     {
/* 1227 */       List keys = parseMultiselectValueString(field, value);
/*      */ 
/* 1230 */       String displaySep = field.get("MultiselectDisplaySeparator", ", ");
/* 1231 */       IdcStringBuilder displayStringBuilder = new IdcStringBuilder();
/* 1232 */       for (int i = 0; i < keys.size(); ++i)
/*      */       {
/* 1234 */         String key = (String)keys.get(i);
/*      */         String displayString;
/*      */         String displayString;
/* 1236 */         if (isTree)
/*      */         {
/* 1238 */           displayString = getTreeDisplayValue(field, key, context);
/*      */         }
/*      */         else
/*      */         {
/* 1242 */           displayString = getViewDisplayValue("getFieldDisplayValue", viewName, key, context);
/*      */         }
/*      */ 
/* 1245 */         if (displayString == null)
/*      */         {
/* 1254 */           if ((SystemUtils.m_verbose) || (optionListType.indexOf("multi2") >= 0))
/*      */           {
/* 1256 */             String suffixMsg = "using the view " + viewName;
/* 1257 */             Report.debug("schemapagecreation", "Could not map inside multivalue, internal value '" + key + "' to display value " + suffixMsg, null);
/*      */           }
/*      */ 
/* 1261 */           displayString = key;
/* 1262 */           String[] filterArgs = { fieldName, optionListType, key, displayString, filterIsTreeBool };
/* 1263 */           context.setCachedObject("SchemaFieldData:" + fieldName, field);
/* 1264 */           context.setCachedObject("notFoundSchemaKeyArgs", filterArgs);
/* 1265 */           context.setCachedObject("displayStringBuilder", displayString);
/* 1266 */           if (PluginFilters.filter("notFoundMultiSelectSchemaKey", null, binder, context) != 0)
/*      */           {
/*      */             break;
/*      */           }
/*      */ 
/* 1271 */           displayString = filterArgs[3];
/*      */         }
/* 1273 */         if (i > 0)
/*      */         {
/* 1275 */           displayStringBuilder.append(displaySep);
/*      */         }
/* 1277 */         displayStringBuilder.append(displayString);
/*      */       }
/* 1279 */       result = displayStringBuilder.toString();
/*      */     }
/*      */     else
/*      */     {
/* 1283 */       if (isTree)
/*      */       {
/* 1285 */         result = getTreeDisplayValue(field, value, context);
/*      */       }
/*      */       else
/*      */       {
/* 1289 */         result = getViewDisplayValue("getFieldDisplayValue", viewName, value, context);
/*      */       }
/*      */ 
/* 1292 */       if (result == null)
/*      */       {
/* 1294 */         if ((SystemUtils.m_verbose) || (optionListType.equals("choice")) || (field == null))
/*      */         {
/* 1296 */           String suffixMsg = "using the view " + viewName;
/* 1297 */           String msg = "Could not map internal value '" + value + "' to display value " + suffixMsg;
/*      */ 
/* 1299 */           if (field == null)
/*      */           {
/* 1301 */             msg = msg + ", view could not be found.";
/*      */           }
/* 1303 */           Report.debug("schemapagecreation", msg, null);
/*      */         }
/* 1305 */         String[] filterArgs = { fieldName, optionListType, value, value, filterIsTreeBool };
/* 1306 */         if (field != null)
/*      */         {
/* 1308 */           context.setCachedObject("SchemaFieldData:" + fieldName, field);
/*      */         }
/* 1310 */         context.setCachedObject("notFoundSchemaKeyArgs", filterArgs);
/* 1311 */         PluginFilters.filter("notFoundSchemaKey", null, binder, context);
/* 1312 */         result = filterArgs[3];
/*      */       }
/*      */     }
/* 1315 */     return result;
/*      */   }
/*      */ 
/*      */   protected ResultSet getViewValues(String function, SchemaViewData viewDef, String key, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/* 1322 */     ResultSet rset = null;
/*      */     try
/*      */     {
/* 1325 */       SchemaSecurityFilter filter = this.m_schemaUtils.getSecurityImplementor(viewDef);
/*      */ 
/* 1327 */       if (filter != null)
/*      */       {
/* 1329 */         Service service = (Service)context;
/* 1330 */         filter.init(service);
/*      */       }
/*      */ 
/* 1333 */       Map args = new HashMap();
/* 1334 */       args.put("primaryKey", new String[] { key });
/* 1335 */       args.put("filter", filter);
/* 1336 */       rset = viewDef.getViewValues(args);
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/* 1340 */       Report.trace("schemapagecreation", function + "(): unable to get values from view '" + viewDef.m_name + "' with key '" + key + "'", ignore);
/*      */     }
/*      */ 
/* 1344 */     return rset;
/*      */   }
/*      */ 
/*      */   protected Object getResultSetValue(ResultSet rset, FieldInfo info)
/*      */   {
/* 1349 */     if ((rset == null) || (!rset.isRowPresent()))
/*      */     {
/* 1351 */       return null;
/*      */     }
/* 1353 */     return ResultSetUtils.getResultSetObject(rset, info);
/*      */   }
/*      */ 
/*      */   protected String getViewDisplayValue(String functionName, String viewName, String key, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/* 1360 */     String theValue = null;
/* 1361 */     SchemaViewData data = getViewDefinition(functionName, viewName);
/* 1362 */     if (data == null)
/*      */     {
/* 1364 */       return theValue;
/*      */     }
/* 1366 */     ResultSet rset = getViewValues(functionName, data, key, context);
/* 1367 */     if ((rset == null) || (!rset.isRowPresent()))
/*      */     {
/* 1369 */       if (key.length() == 0)
/*      */       {
/* 1371 */         theValue = "";
/*      */       }
/* 1373 */       return theValue;
/*      */     }
/* 1375 */     theValue = data.computeDisplayValue(rset, context, 1);
/* 1376 */     if (theValue == null)
/*      */     {
/* 1378 */       Report.trace("schemapagecreation", "Unable to find a displayable field for view '" + data.m_name, null);
/*      */ 
/* 1380 */       theValue = "";
/*      */     }
/* 1382 */     if (theValue.length() == 0)
/*      */     {
/* 1384 */       Report.trace("schemapagecreation", "Display value for view '" + data.m_name + "' given key '" + key + "' is empty. Defaulting to the key itself.", null);
/*      */ 
/* 1386 */       theValue = key;
/*      */     }
/*      */ 
/* 1389 */     return theValue;
/*      */   }
/*      */ 
/*      */   protected String getTreeDisplayValue(SchemaFieldData fieldDefinition, String keyString, ExecutionContext context)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1396 */     String treeDefinitionString = fieldDefinition.get("TreeDefinition");
/* 1397 */     if (treeDefinitionString != null)
/*      */     {
/* 1399 */       String fieldSeparator = fieldDefinition.get("TreeNodeStorageSeparator", "/");
/*      */ 
/* 1401 */       String fieldDisplaySeparator = fieldDefinition.get("TreeNodeDisplaySeparator", "/");
/*      */ 
/* 1404 */       IdcStringBuilder value = new IdcStringBuilder();
/* 1405 */       Object[] treeDefinition = this.m_schemaHelper.expandTreeDefinition(treeDefinitionString);
/* 1406 */       boolean showSelectionPath = fieldDefinition.getBoolean("ShowSelectionPath", false);
/*      */       Vector values;
/*      */       Vector values;
/* 1408 */       if (fieldDefinition.getBoolean("StoreSelectionPath", false))
/*      */       {
/* 1410 */         values = StringUtils.parseArray(keyString, fieldSeparator.charAt(0), '^');
/*      */       }
/*      */       else
/*      */       {
/* 1419 */         values = this.m_schemaHelper.computeTreeSelectionParents(treeDefinition, keyString);
/*      */       }
/*      */ 
/* 1422 */       if ((values != null) && (values.size() > 0))
/*      */       {
/* 1424 */         buildTreeDisplayString(value, values, treeDefinition, fieldDisplaySeparator, context, showSelectionPath);
/*      */       }
/*      */       else
/*      */       {
/* 1429 */         value.append(keyString);
/*      */       }
/* 1431 */       return value.toString();
/*      */     }
/* 1433 */     Report.trace("schemapagecreation", "field '" + fieldDefinition.m_name + "' doesn't have a tree definition.", null);
/*      */ 
/* 1435 */     return null;
/*      */   }
/*      */ 
/*      */   protected void buildTreeDisplayString(IdcStringBuilder buffer, Vector values, Object[] treeDefinition, String fieldDisplaySeperator, ExecutionContext context, boolean useFullPath)
/*      */     throws ServiceException
/*      */   {
/* 1444 */     int i = (useFullPath) ? 0 : values.size() - 1;
/* 1445 */     for (; i < values.size(); ++i)
/*      */     {
/* 1447 */       String key = (String)values.elementAt(i);
/* 1448 */       SchemaViewData view = null;
/* 1449 */       int myIndex = 2 * i;
/* 1450 */       if ((myIndex + 1 >= treeDefinition.length) && (treeDefinition[(treeDefinition.length - 1)] instanceof SchemaTreePointer))
/*      */       {
/* 1454 */         SchemaTreePointer stp = (SchemaTreePointer)treeDefinition[(treeDefinition.length - 1)];
/*      */ 
/* 1456 */         int loopIndex = stp.m_recursiveIndex;
/* 1457 */         myIndex -= loopIndex;
/* 1458 */         myIndex %= (treeDefinition.length - loopIndex - 1);
/* 1459 */         myIndex += loopIndex;
/*      */       }
/* 1461 */       String displayValue = null;
/* 1462 */       if (myIndex < treeDefinition.length)
/*      */       {
/* 1464 */         view = (SchemaViewData)treeDefinition[myIndex];
/* 1465 */         displayValue = getViewDisplayValue("getTreeDisplayValue", view.m_name, key, context);
/*      */       }
/*      */ 
/* 1468 */       if (displayValue == null)
/*      */       {
/* 1470 */         String viewName = (view != null) ? view.m_name : "err";
/* 1471 */         Report.trace("schemapagecreation", "unable to find value for view '" + viewName + "' with value '" + key + "'", null);
/*      */ 
/* 1473 */         displayValue = "*" + key;
/*      */       }
/* 1475 */       if ((i > 0) && (useFullPath))
/*      */       {
/* 1477 */         buffer.append(fieldDisplaySeperator);
/*      */       }
/* 1479 */       buffer.append(displayValue);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void getViewValuesResultSet(String viewName, String relationName, String parentValue, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/* 1500 */     initSchemaHelper(context);
/* 1501 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/* 1502 */     if (binder == null)
/*      */     {
/* 1504 */       return;
/*      */     }
/* 1506 */     getViewValuesResultSetImplement(viewName, relationName, parentValue, this.m_schemaHelper, binder, new HashMap(), context);
/*      */   }
/*      */ 
/*      */   public void getViewValuesResultSetImplement(String viewName, String relationName, String parentValue, SchemaHelper schemaHelper, DataBinder binder, Map args, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1516 */       SchemaViewData viewDef = schemaHelper.getView(viewName);
/* 1517 */       if (viewDef == null)
/*      */       {
/* 1519 */         String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_view", null, viewName);
/*      */ 
/* 1521 */         throw new ServiceException(msg);
/*      */       }
/* 1523 */       SchemaSecurityFilter filter = this.m_schemaUtils.getSecurityImplementor(viewDef);
/*      */ 
/* 1525 */       if (filter != null)
/*      */       {
/* 1527 */         filter.init(context);
/*      */       }
/* 1529 */       ResultSet rset = null;
/* 1530 */       args.put("filter", filter);
/* 1531 */       String privString = (String)context.getCachedObject("SchemaViewPrivilege");
/* 1532 */       if (privString == null)
/*      */       {
/* 1534 */         int priv = 1;
/* 1535 */         if (context instanceof Service)
/*      */         {
/* 1537 */           Service service = (Service)context;
/* 1538 */           priv = service.getServiceData().m_accessLevel;
/*      */         }
/* 1540 */         privString = "" + priv;
/*      */       }
/* 1542 */       args.put("privilege", privString);
/*      */ 
/* 1544 */       if ((relationName != null) && (relationName.length() > 0))
/*      */       {
/* 1546 */         SchemaRelationData relationDef = schemaHelper.getRelation(relationName);
/*      */ 
/* 1548 */         if (relationDef != null)
/*      */         {
/* 1550 */           String parentField = schemaHelper.getOtherTableColumn(viewName, relationName);
/* 1551 */           DataResultSet parentValues = new DataResultSet(new String[] { parentField });
/* 1552 */           Vector v = new IdcVector();
/* 1553 */           v.addElement(parentValue);
/* 1554 */           parentValues.addRow(v);
/* 1555 */           args.put("relationName", relationName);
/* 1556 */           args.put("parentValues", parentValues);
/*      */         }
/*      */         else
/*      */         {
/* 1560 */           Vector columns = viewDef.getVector("schViewColumns");
/* 1561 */           String[] columnsArray = new String[columns.size()];
/* 1562 */           columns.copyInto(columnsArray);
/* 1563 */           DataResultSet drset = new DataResultSet(columnsArray);
/* 1564 */           rset = drset;
/*      */         }
/*      */       }
/* 1567 */       if (rset == null)
/*      */       {
/* 1569 */         rset = viewDef.getViewValues(args);
/*      */       }
/* 1571 */       binder.addResultSet("SchemaData", rset);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1575 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1581 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102399 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.SchemaScriptExtensions
 * JD-Core Version:    0.5.4
 */