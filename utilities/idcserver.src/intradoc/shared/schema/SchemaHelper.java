/*      */ package intradoc.shared.schema;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcStringBuilder;
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
/*      */ import intradoc.shared.ResultSetTreeSort;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.ViewFieldDef;
/*      */ import intradoc.shared.ViewFields;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.CharConversionException;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SchemaHelper
/*      */ {
/*      */   public SchemaViewConfig m_views;
/*      */   public SchemaRelationConfig m_relationships;
/*      */   public SchemaFieldConfig m_fields;
/*      */   public SchemaTableConfig m_tables;
/*      */   public SchemaTargetConfig m_targets;
/*   38 */   protected SchemaFieldData[] m_stdFields = null;
/*   39 */   protected Hashtable m_fieldMap = null;
/*   40 */   protected Hashtable m_childMap = null;
/*      */ 
/*   42 */   protected Hashtable m_fieldTree = null;
/*   43 */   protected Hashtable m_namedRelTree = null;
/*      */ 
/*   45 */   public static String VIEW_PREFIX = "view://";
/*   46 */   public static String TREE_PREFIX = "tree://";
/*      */ 
/*   48 */   public static String[] SCHEMA_CONTAINERS = { "SchemaViewConfig", "SchemaRelationConfig", "SchemaTableConfig", "SchemaFieldConfig", "SchemaTargetConfig" };
/*      */ 
/*      */   public SchemaHelper()
/*      */   {
/*   56 */     init();
/*      */   }
/*      */ 
/*      */   public void init()
/*      */   {
/*   61 */     this.m_views = ((SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig"));
/*      */ 
/*   63 */     this.m_relationships = ((SchemaRelationConfig)SharedObjects.getTable("SchemaRelationConfig"));
/*      */ 
/*   65 */     this.m_fields = ((SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig"));
/*      */ 
/*   67 */     this.m_tables = ((SchemaTableConfig)SharedObjects.getTable("SchemaTableConfig"));
/*      */ 
/*   69 */     this.m_targets = ((SchemaTargetConfig)SharedObjects.getTable("SchemaTargetConfig"));
/*      */   }
/*      */ 
/*      */   public void refresh()
/*      */   {
/*   75 */     init();
/*      */   }
/*      */ 
/*      */   public void computeMaps()
/*      */   {
/*   80 */     computeFieldMaps();
/*   81 */     buildTrees();
/*      */   }
/*      */ 
/*      */   protected void computeFieldMaps()
/*      */   {
/*   86 */     this.m_childMap = new Hashtable();
/*   87 */     this.m_fieldMap = new Hashtable();
/*      */ 
/*   91 */     SchemaFieldData[] stdFields = getStandardFields();
/*   92 */     for (int i = 0; i < stdFields.length; ++i)
/*      */     {
/*   94 */       SchemaFieldData data = stdFields[i];
/*   95 */       this.m_fieldMap.put(data.get("schFieldName"), data);
/*      */     }
/*      */ 
/*   99 */     for (this.m_fields.first(); this.m_fields.isRowPresent(); this.m_fields.next())
/*      */     {
/*  101 */       SchemaFieldData data = (SchemaFieldData)this.m_fields.getData();
/*  102 */       this.m_fieldMap.put(data.get("schFieldName"), data);
/*      */ 
/*  104 */       boolean isDependent = data.getBoolean("IsDependentList", false);
/*  105 */       if (!isDependent)
/*      */         continue;
/*  107 */       String parent = data.get("DependentOnField");
/*  108 */       Vector list = (Vector)this.m_childMap.get(parent);
/*  109 */       if (list == null)
/*      */       {
/*  111 */         list = new IdcVector();
/*  112 */         this.m_childMap.put(parent, list);
/*      */       }
/*  114 */       list.addElement(data);
/*      */     }
/*      */   }
/*      */ 
/*      */   public SchemaFieldData[] getStandardFields()
/*      */   {
/*  121 */     if (this.m_stdFields == null)
/*      */     {
/*  123 */       Object o = SharedObjects.getObject("SchemaDefinition", "StandardFields");
/*  124 */       if ((o != null) && (o instanceof SchemaFieldData[]))
/*      */       {
/*  126 */         this.m_stdFields = ((SchemaFieldData[])(SchemaFieldData[])o);
/*      */       }
/*      */     }
/*  129 */     if (this.m_stdFields == null)
/*      */     {
/*      */       try
/*      */       {
/*  133 */         SchemaFieldConfig templateFieldConfig = new SchemaFieldConfig();
/*  134 */         Vector v = templateFieldConfig.createEmptyRow();
/*  135 */         templateFieldConfig.addRow(v);
/*  136 */         FieldInfo[] fi = ResultSetUtils.createInfoList(templateFieldConfig, new String[] { "schFieldName", "schCanonicalName" }, true);
/*  137 */         ViewFields viewFields = new ViewFields(null);
/*  138 */         viewFields.addStandardDocFields();
/*  139 */         List fields = viewFields.m_viewFields;
/*  140 */         int nfields = fields.size();
/*  141 */         SchemaFieldData[] stdFields = new SchemaFieldData[nfields];
/*  142 */         for (int i = 0; i < nfields; ++i)
/*      */         {
/*  144 */           ViewFieldDef fieldDef = (ViewFieldDef)fields.get(i);
/*  145 */           stdFields[i] = new SchemaFieldData();
/*  146 */           DataBinder binder = new DataBinder();
/*  147 */           binder.putLocal("schFieldName", fieldDef.m_name);
/*  148 */           binder.putLocal("schOrder", "" + i);
/*  149 */           binder.putLocal("schVersion", "3");
/*  150 */           binder.putLocal("dCaption", fieldDef.m_caption);
/*  151 */           if (fieldDef.m_default != null)
/*      */           {
/*  153 */             binder.putLocal("dDefaultValue", fieldDef.m_default);
/*      */           }
/*  155 */           binder.putLocal("dIsOptionList", (fieldDef.m_isOptionList) ? "1" : "0");
/*  156 */           if (fieldDef.m_optionListKey != null)
/*      */           {
/*  158 */             binder.putLocal("dOptionListKey", fieldDef.m_optionListKey);
/*      */           }
/*  160 */           if (fieldDef.m_optionListType != null)
/*      */           {
/*  162 */             binder.putLocal("dOptionListType", fieldDef.m_optionListType);
/*      */           }
/*  164 */           SchemaFieldConfig fieldConfig = new SchemaFieldConfig();
/*  165 */           fieldConfig.copy(templateFieldConfig);
/*  166 */           fieldConfig.first();
/*  167 */           fieldConfig.setCurrentValue(fi[0].m_index, fieldDef.m_name);
/*  168 */           fieldConfig.setCurrentValue(fi[1].m_index, fieldDef.m_name);
/*  169 */           stdFields[i].init(fieldConfig);
/*      */ 
/*  171 */           stdFields[i].updateEx(binder);
/*      */         }
/*  173 */         SharedObjects.putObject("SchemaDefinition", "StandardFields", stdFields);
/*  174 */         this.m_stdFields = stdFields;
/*      */       }
/*      */       catch (DataException ignore)
/*      */       {
/*  178 */         Report.trace("system", null, ignore);
/*      */       }
/*      */     }
/*  181 */     return this.m_stdFields;
/*      */   }
/*      */ 
/*      */   protected void buildTrees()
/*      */   {
/*  187 */     this.m_fieldTree = new Hashtable();
/*  188 */     this.m_namedRelTree = new Hashtable();
/*      */ 
/*  190 */     for (Enumeration en = this.m_fieldMap.elements(); en.hasMoreElements(); )
/*      */     {
/*  192 */       SchemaFieldData field = (SchemaFieldData)en.nextElement();
/*  193 */       String name = field.get("schFieldName");
/*      */ 
/*  195 */       NamedRelationship namedRel = new NamedRelationship();
/*  196 */       namedRel.m_field = field;
/*  197 */       namedRel.m_children = ((Vector)this.m_childMap.get(name));
/*  198 */       this.m_fieldTree.put(name, namedRel);
/*      */ 
/*  200 */       boolean isDependent = field.getBoolean("IsDependentList", false);
/*  201 */       if (isDependent)
/*      */       {
/*  203 */         String relName = field.get("DependentRelationship");
/*  204 */         if ((relName == null) || (relName.length() == 0))
/*      */         {
/*  206 */           Report.trace("schema", "The dependent field " + name + " is missing a relationship.", null);
/*      */         }
/*      */         else
/*      */         {
/*  210 */           namedRel.m_relation = ((SchemaRelationData)this.m_relationships.getData(relName));
/*      */ 
/*  213 */           String parentName = field.get("DependentOnField");
/*  214 */           if (namedRel.m_relation == null)
/*      */           {
/*  217 */             Report.trace("schema", "The relationship " + relName + " for the dependent field " + name + " is missing.", null);
/*      */           }
/*  220 */           else if ((parentName == null) || (parentName.length() == 0))
/*      */           {
/*  222 */             Report.trace("schema", "The parent field for the dependent field " + name + " is missing.", null);
/*      */           }
/*      */           else
/*      */           {
/*  227 */             namedRel.m_parentField = ((SchemaFieldData)this.m_fieldMap.get(parentName));
/*  228 */             this.m_namedRelTree.put(relName, namedRel);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  234 */       boolean isTree = isTreeField(field);
/*  235 */       if (isTree)
/*      */       {
/*  237 */         buildSubTree(namedRel);
/*      */       }
/*      */       else
/*      */       {
/*  241 */         String key = null;
/*  242 */         boolean isView = isViewField(field);
/*  243 */         if (isView)
/*      */         {
/*  245 */           key = field.get("OptionViewKey");
/*      */         }
/*  247 */         if (key == null)
/*      */         {
/*  249 */           key = field.get("dOptionListKey");
/*      */         }
/*  251 */         if (key == null)
/*      */         {
/*  253 */           Report.trace("schema", "field " + field + " is missing a value for OptionViewKey and dOptionListKey", null);
/*      */         }
/*      */         else
/*      */         {
/*  258 */           if (key.startsWith(VIEW_PREFIX))
/*      */           {
/*  260 */             key = key.substring(VIEW_PREFIX.length());
/*      */           }
/*  262 */           namedRel.m_view = ((SchemaViewData)this.m_views.getData(key));
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void buildSubTree(NamedRelationship namedRel)
/*      */   {
/*  270 */     SchemaFieldData field = namedRel.m_field;
/*  271 */     String fieldName = field.get("schFieldName");
/*      */     try
/*      */     {
/*  274 */       Object[] treeDefinition = expandTreeDefinition(field.get("TreeDefinition"));
/*  275 */       namedRel.m_isTree = true;
/*  276 */       int size = treeDefinition.length;
/*  277 */       if (size > 0)
/*      */       {
/*  279 */         namedRel.m_view = ((SchemaViewData)treeDefinition[0]);
/*      */       }
/*      */ 
/*  282 */       SchemaTreePointer treePointer = null;
/*  283 */       Vector tree = new IdcVector();
/*  284 */       for (int i = size - 1; i >= 1; --i)
/*      */       {
/*  286 */         NamedRelationship currentRel = new NamedRelationship();
/*  287 */         Object obj = treeDefinition[i];
/*  288 */         if (obj instanceof SchemaTreePointer)
/*      */         {
/*  290 */           treePointer = (SchemaTreePointer)obj;
/*  291 */           currentRel.m_treePointer = treePointer;
/*  292 */           currentRel.m_view = ((SchemaViewData)treeDefinition[treePointer.m_recursiveIndex]);
/*  293 */           currentRel.m_isRecursive = true;
/*      */         }
/*  295 */         else if (obj instanceof SchemaViewData)
/*      */         {
/*  297 */           currentRel.m_view = ((SchemaViewData)obj);
/*      */         }
/*  299 */         if (i > 0)
/*      */         {
/*  301 */           --i;
/*  302 */           currentRel.m_relation = ((SchemaRelationData)treeDefinition[i]);
/*      */         }
/*      */ 
/*  305 */         tree.addElement(currentRel);
/*  306 */         currentRel.m_id = (fieldName + "_" + tree.size());
/*  307 */         currentRel.m_isTree = true;
/*      */       }
/*      */ 
/*  310 */       if (treePointer != null)
/*      */       {
/*  312 */         namedRel.m_treePointer = treePointer;
/*      */       }
/*  314 */       namedRel.m_tree = tree;
/*  315 */       namedRel.m_isTree = true;
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  319 */       Report.trace("schema", "SchemaHelper.buildSubTree: Unable to build sub tree for field " + fieldName, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Vector computeParentListForField(String fieldName)
/*      */   {
/*  327 */     NamedRelationship namedRel = (NamedRelationship)this.m_fieldTree.get(fieldName);
/*      */ 
/*  329 */     Vector parentList = computeParentList(namedRel, true);
/*  330 */     return parentList;
/*      */   }
/*      */ 
/*      */   public Vector computeParentList(NamedRelationship namedRel, boolean isIncludeSelf)
/*      */   {
/*  336 */     Vector parentList = new IdcVector();
/*  337 */     if (namedRel == null)
/*      */     {
/*  339 */       return parentList;
/*      */     }
/*      */ 
/*  342 */     if ((isIncludeSelf) && (!namedRel.m_isTree))
/*      */     {
/*  344 */       parentList.addElement(namedRel);
/*      */     }
/*      */ 
/*      */     while (true)
/*      */     {
/*  350 */       if (namedRel.m_isTree)
/*      */       {
/*  352 */         addTreeParentList(parentList, namedRel);
/*  353 */         break;
/*      */       }
/*  355 */       SchemaFieldData fd = namedRel.m_parentField;
/*  356 */       if (fd == null) {
/*      */         break;
/*      */       }
/*      */ 
/*  360 */       namedRel = (NamedRelationship)this.m_fieldTree.get(fd.get("schFieldName"));
/*      */ 
/*  365 */       if ((namedRel == null) || (namedRel.m_view == null))
/*      */         break;
/*  367 */       parentList.addElement(namedRel);
/*      */     }
/*      */ 
/*  374 */     return parentList;
/*      */   }
/*      */ 
/*      */   public void addTreeParentList(Vector parentList, NamedRelationship namedRel)
/*      */   {
/*  380 */     Vector tree = namedRel.m_tree;
/*  381 */     int size = tree.size();
/*  382 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  384 */       Object obj = tree.elementAt(i);
/*  385 */       parentList.addElement(obj);
/*      */     }
/*  387 */     parentList.addElement(namedRel);
/*      */   }
/*      */ 
/*      */   public Vector buildParentTree(String table, String clmn)
/*      */   {
/*  394 */     Vector namedList = new IdcVector();
/*      */ 
/*  396 */     for (this.m_relationships.first(); this.m_relationships.isRowPresent(); )
/*      */     {
/*  399 */       SchemaRelationData relData = (SchemaRelationData)this.m_relationships.getData();
/*      */ 
/*  401 */       String table2 = relData.get("schTable2Table");
/*  402 */       String clmn2 = relData.get("schTable2Column");
/*      */ 
/*  404 */       if ((table2.equals(table)) && (clmn2.equals(clmn)))
/*      */       {
/*  407 */         NamedRelationship namedRel = (NamedRelationship)this.m_namedRelTree.get(relData.get("schRelationName"));
/*      */ 
/*  409 */         Vector parentList = computeParentList(namedRel, false);
/*  410 */         if (parentList.size() > 0)
/*      */         {
/*  412 */           namedList.addElement(parentList);
/*      */         }
/*      */       }
/*  397 */       this.m_relationships.next();
/*      */     }
/*      */ 
/*  416 */     return namedList;
/*      */   }
/*      */ 
/*      */   public SchemaData getSchemaData(String objectClass, String name)
/*      */   {
/*  423 */     SchemaResultSet rset = (SchemaResultSet)SharedObjects.getTable(objectClass);
/*      */ 
/*  425 */     if (rset == null)
/*      */     {
/*  427 */       Report.trace("schema", "objectClass " + objectClass + " doesn't exist.", null);
/*      */ 
/*  429 */       return null;
/*      */     }
/*  431 */     SchemaData data = rset.getData(name);
/*  432 */     return data;
/*      */   }
/*      */ 
/*      */   public SchemaData requireSchemaData(String objectClass, String name)
/*      */     throws DataException
/*      */   {
/*  441 */     SchemaData data = getSchemaData(objectClass, name);
/*  442 */     if (data == null)
/*      */     {
/*  444 */       String key = "apSchemaObjectDoesntExist_";
/*  445 */       String pre = "Schema";
/*  446 */       String post = "Config";
/*  447 */       if ((objectClass.startsWith(pre)) && (objectClass.endsWith(post)))
/*      */       {
/*  449 */         key = key + objectClass.substring(pre.length(), objectClass.length() - post.length()).toLowerCase();
/*      */       }
/*      */       else
/*      */       {
/*  454 */         key = key + objectClass.toLowerCase();
/*      */       }
/*  456 */       String msg = LocaleUtils.encodeMessage(key, null, name);
/*  457 */       throw new DataException(msg);
/*      */     }
/*  459 */     return data;
/*      */   }
/*      */ 
/*      */   public boolean isComplexListType(String optionListKey)
/*      */   {
/*  467 */     return (optionListKey.startsWith(TREE_PREFIX)) || (optionListKey.startsWith(VIEW_PREFIX));
/*      */   }
/*      */ 
/*      */   public boolean isTreeField(SchemaFieldData field)
/*      */   {
/*  474 */     boolean isOptList = field.getBoolean("dIsOptionList", false);
/*  475 */     boolean isTree = false;
/*  476 */     if (isOptList)
/*      */     {
/*  478 */       String listKey = field.get("dOptionListKey");
/*  479 */       String prefix = TREE_PREFIX;
/*      */ 
/*  481 */       isTree = (listKey != null) && (listKey.startsWith(prefix));
/*      */     }
/*  483 */     return isTree;
/*      */   }
/*      */ 
/*      */   public boolean isViewField(SchemaFieldData field)
/*      */   {
/*  488 */     return isViewFieldEx(field, null);
/*      */   }
/*      */ 
/*      */   public boolean isViewFieldEx(SchemaFieldData field, String[] viewName)
/*      */   {
/*  493 */     boolean isView = false;
/*      */ 
/*  495 */     boolean isOptList = field.getBoolean("dIsOptionList", false);
/*  496 */     if (isOptList)
/*      */     {
/*  498 */       String listKey = field.get("dOptionListKey");
/*  499 */       String prefix = VIEW_PREFIX;
/*      */ 
/*  501 */       isView = (listKey != null) && (listKey.startsWith(prefix));
/*  502 */       if ((isView) && (viewName != null))
/*      */       {
/*  504 */         int len = prefix.length();
/*  505 */         viewName[0] = listKey.substring(len);
/*      */       }
/*      */     }
/*  508 */     return isView;
/*      */   }
/*      */ 
/*      */   public Vector computeViews(SchemaTableData tableDef)
/*      */   {
/*  513 */     return computeViewsForTable(tableDef.m_name);
/*      */   }
/*      */ 
/*      */   public Vector computeViewsForTable(String tableName)
/*      */   {
/*  518 */     Vector v = new IdcVector();
/*  519 */     for (this.m_views.first(); this.m_views.isRowPresent(); this.m_views.next())
/*      */     {
/*  521 */       SchemaData data = this.m_views.getData();
/*  522 */       String viewTableName = data.get("schTableName");
/*  523 */       if ((viewTableName == null) || (!viewTableName.equalsIgnoreCase(tableName))) {
/*      */         continue;
/*      */       }
/*  526 */       v.addElement(data);
/*      */     }
/*      */ 
/*  529 */     return v;
/*      */   }
/*      */ 
/*      */   public Vector computeViewRelations(String key)
/*      */     throws DataException
/*      */   {
/*  535 */     String viewName = null;
/*  536 */     if (key.startsWith(TREE_PREFIX))
/*      */     {
/*  538 */       String[] treeDefinition = parseTreeDefinition(key);
/*  539 */       if (treeDefinition.length > 0)
/*      */       {
/*  541 */         viewName = treeDefinition[0].substring(1);
/*      */       }
/*      */     }
/*  544 */     else if (key.startsWith(VIEW_PREFIX))
/*      */     {
/*  546 */       viewName = key.substring(VIEW_PREFIX.length());
/*      */     }
/*      */     else
/*      */     {
/*  550 */       SystemUtils.reportDeprecatedUsage("SchemaHelper.computeViewRelations() without a proper prefix");
/*      */ 
/*  552 */       viewName = key;
/*      */     }
/*      */ 
/*  555 */     SchemaViewData data = (SchemaViewData)this.m_views.getData(viewName);
/*  556 */     if (data == null)
/*      */     {
/*  558 */       String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_view", null, viewName);
/*      */ 
/*  560 */       throw new DataException(msg);
/*      */     }
/*  562 */     String tableName = data.get("schTableName");
/*  563 */     Vector rels = computeRelations(tableName, false);
/*  564 */     return rels;
/*      */   }
/*      */ 
/*      */   public Vector computeAllViewRelations(String viewName)
/*      */   {
/*  569 */     SchemaViewData data = (SchemaViewData)this.m_views.getData(viewName);
/*  570 */     String tableName = data.get("schTableName");
/*  571 */     Vector rels = computeAllRelations(tableName, false);
/*  572 */     return rels;
/*      */   }
/*      */ 
/*      */   public Vector computeNamedRelations(String tableName)
/*      */   {
/*  579 */     return computeRelations(tableName, true);
/*      */   }
/*      */ 
/*      */   public Vector computeRelations(String tableName, boolean mustBeNamed)
/*      */   {
/*  585 */     SchemaRelationConfig relationships = (SchemaRelationConfig)SharedObjects.getTable("SchemaRelationConfig");
/*      */ 
/*  588 */     Vector relList = new IdcVector();
/*  589 */     for (relationships.first(); relationships.isRowPresent(); relationships.next())
/*      */     {
/*  591 */       SchemaRelationData data = (SchemaRelationData)relationships.getData();
/*  592 */       String relationType = data.get("schRelationType");
/*  593 */       if ((relationType != null) && (!relationType.equalsIgnoreCase("table")))
/*      */         continue;
/*  595 */       String table2 = data.get("schTable2Table");
/*  596 */       if (!table2.equals(tableName))
/*      */         continue;
/*  598 */       boolean isAdd = true;
/*  599 */       if (mustBeNamed)
/*      */       {
/*  602 */         isAdd = isNamedRelationship(data.get("schRelationName"));
/*      */       }
/*  604 */       if (!isAdd)
/*      */         continue;
/*  606 */       relList.addElement(data);
/*      */     }
/*      */ 
/*  611 */     return relList;
/*      */   }
/*      */ 
/*      */   public Vector computeAllRelations(String tableName, boolean mustBeNamed)
/*      */   {
/*  617 */     SchemaRelationConfig relationships = (SchemaRelationConfig)SharedObjects.getTable("SchemaRelationConfig");
/*      */ 
/*  620 */     Vector relList = new IdcVector();
/*  621 */     for (relationships.first(); relationships.isRowPresent(); relationships.next())
/*      */     {
/*  623 */       SchemaRelationData data = (SchemaRelationData)relationships.getData();
/*  624 */       String relationType = data.get("schRelationType");
/*  625 */       if ((relationType != null) && (!relationType.equals("table")))
/*      */         continue;
/*  627 */       String table1 = data.get("schTable1Table");
/*  628 */       String table2 = data.get("schTable2Table");
/*  629 */       if ((!table1.equals(tableName)) && (!table2.equals(tableName)))
/*      */         continue;
/*  631 */       boolean isAdd = true;
/*  632 */       if (mustBeNamed)
/*      */       {
/*  635 */         isAdd = isNamedRelationship(data.get("schRelationName"));
/*      */       }
/*  637 */       if (!isAdd)
/*      */         continue;
/*  639 */       relList.addElement(data);
/*      */     }
/*      */ 
/*  644 */     return relList;
/*      */   }
/*      */ 
/*      */   public boolean isNamedRelationship(String relName)
/*      */   {
/*  649 */     return isNamedRelationshipEx(relName, null);
/*      */   }
/*      */ 
/*      */   public boolean isNamedRelationshipEx(String relName, SchemaFieldData[] field)
/*      */   {
/*  654 */     SchemaFieldConfig fields = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/*      */ 
/*  657 */     for (fields.first(); fields.isRowPresent(); fields.next())
/*      */     {
/*  659 */       SchemaFieldData data = (SchemaFieldData)fields.getData();
/*  660 */       boolean isView = isViewField(data);
/*  661 */       boolean isTree = isTreeField(data);
/*  662 */       if ((!isView) && (!isTree))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  667 */       boolean isDependent = data.getBoolean("IsDependentList", false);
/*  668 */       if (isDependent)
/*      */       {
/*  670 */         String rel = data.get("DependentRelationship");
/*  671 */         if (rel.equals(relName))
/*      */         {
/*  673 */           if (field != null)
/*      */           {
/*  675 */             field[0] = data;
/*      */           }
/*  677 */           return true;
/*      */         }
/*      */       }
/*      */ 
/*  681 */       if (!isTree)
/*      */         continue;
/*      */       try
/*      */       {
/*  685 */         String treeDefinition = data.get("TreeDefinition");
/*  686 */         Object[] def = expandTreeDefinition(treeDefinition);
/*  687 */         int len = def.length;
/*  688 */         for (int i = 0; i < len; ++i)
/*      */         {
/*  690 */           Object obj = def[i];
/*  691 */           if (!obj instanceof SchemaRelationData)
/*      */             continue;
/*  693 */           SchemaRelationData relData = (SchemaRelationData)obj;
/*  694 */           String rel = relData.get("schRelationName");
/*  695 */           if (!rel.equals(relName))
/*      */             continue;
/*  697 */           if (field != null)
/*      */           {
/*  699 */             field[0] = data;
/*      */           }
/*  701 */           return true;
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  708 */         Report.trace("schema", "SchemaHelper.isNamedRelationshipEx: Unable to compute tree definition for field " + data.get("schFieldName") + " and relation " + relName, e);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  713 */     return false;
/*      */   }
/*      */ 
/*      */   public NamedRelationship getNamedRelationship(SchemaFieldData fieldData)
/*      */   {
/*  718 */     String fieldName = fieldData.get("schFieldName");
/*  719 */     NamedRelationship namedRel = (NamedRelationship)this.m_fieldTree.get(fieldName);
/*      */ 
/*  721 */     return namedRel;
/*      */   }
/*      */ 
/*      */   public NamedRelationship getNamedRelationship(SchemaRelationData relData)
/*      */   {
/*  726 */     String name = relData.get("schRelationName");
/*  727 */     NamedRelationship namedRel = (NamedRelationship)this.m_namedRelTree.get(name);
/*  728 */     return namedRel;
/*      */   }
/*      */ 
/*      */   public Vector computeNamedViews(String tableName)
/*      */   {
/*  733 */     Vector v = new IdcVector();
/*  734 */     for (this.m_views.first(); this.m_views.isRowPresent(); this.m_views.next())
/*      */     {
/*  736 */       SchemaViewData viewData = (SchemaViewData)this.m_views.getData();
/*      */ 
/*  738 */       String table = viewData.get("schTableName");
/*  739 */       if ((table == null) || (!table.equals(tableName))) {
/*      */         continue;
/*      */       }
/*  742 */       SchemaFieldData fieldData = getNamedField(viewData);
/*  743 */       if (fieldData == null)
/*      */         continue;
/*  745 */       NamedRelationship namedRel = getNamedRelationship(fieldData);
/*  746 */       if (namedRel == null)
/*      */       {
/*  748 */         namedRel = new NamedRelationship();
/*  749 */         namedRel.m_field = fieldData;
/*  750 */         namedRel.m_view = viewData;
/*  751 */         namedRel.m_children = ((Vector)this.m_childMap.get(fieldData.get("schFieldName")));
/*      */       }
/*  753 */       v.addElement(namedRel);
/*      */     }
/*      */ 
/*  757 */     return v;
/*      */   }
/*      */ 
/*      */   public SchemaFieldData getNamedField(SchemaViewData viewData)
/*      */   {
/*  762 */     String viewName = viewData.get("schViewName");
/*  763 */     SchemaFieldData result = null;
/*  764 */     boolean isFound = false;
/*  765 */     for (this.m_fields.first(); this.m_fields.isRowPresent(); this.m_fields.next())
/*      */     {
/*  767 */       SchemaFieldData field = (SchemaFieldData)this.m_fields.getData();
/*  768 */       boolean isView = isViewField(field);
/*  769 */       boolean isTree = isTreeField(field);
/*  770 */       if ((!isView) && (!isTree))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  775 */       if (isView)
/*      */       {
/*  777 */         String prefix = VIEW_PREFIX;
/*  778 */         String listKey = field.get("dOptionListKey");
/*  779 */         listKey = listKey.substring(prefix.length());
/*  780 */         if (viewName.equals(listKey))
/*      */         {
/*  782 */           isFound = true;
/*      */         }
/*      */       } else {
/*  785 */         if (!isTree)
/*      */           continue;
/*      */         try
/*      */         {
/*  789 */           String definition = field.get("TreeDefinition");
/*  790 */           Object[] def = expandTreeDefinition(definition);
/*  791 */           int size = def.length;
/*  792 */           for (int i = 0; i < size; ++i)
/*      */           {
/*  794 */             Object obj = def[i];
/*  795 */             if (!obj instanceof SchemaViewData)
/*      */               continue;
/*  797 */             String vName = ((SchemaViewData)obj).get("schViewName");
/*  798 */             if (!vName.equals(viewName))
/*      */               continue;
/*  800 */             isFound = true;
/*  801 */             break;
/*      */           }
/*      */ 
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*  809 */           SystemUtils.trace("schema", "Error while retrieving the named field for view " + viewName);
/*      */ 
/*  811 */           SystemUtils.dumpException("schema", e);
/*      */         }
/*  813 */         if (!isFound)
/*      */           continue;
/*  815 */         result = field;
/*  816 */         break;
/*      */       }
/*      */     }
/*      */ 
/*  820 */     return result;
/*      */   }
/*      */ 
/*      */   public boolean isNamedView(SchemaViewData viewData)
/*      */   {
/*  825 */     SchemaFieldData field = getNamedField(viewData);
/*  826 */     return field != null;
/*      */   }
/*      */ 
/*      */   public String[] parseTreeDefinition(String tmp)
/*      */   {
/*  831 */     if ((tmp == null) || (tmp.equals("<None>")))
/*      */     {
/*  833 */       tmp = "";
/*      */     }
/*  835 */     if (tmp.startsWith(TREE_PREFIX))
/*      */     {
/*  837 */       tmp = tmp.substring(TREE_PREFIX.length());
/*      */     }
/*  839 */     Vector v = StringUtils.parseArray(tmp, '/', '^');
/*  840 */     String[] definition = new String[v.size()];
/*  841 */     v.copyInto(definition);
/*  842 */     return definition;
/*      */   }
/*      */ 
/*      */   public boolean checkViewInParsedTreeDefinition(String[] parsedTree, String view)
/*      */   {
/*  847 */     if (parsedTree == null)
/*      */     {
/*  849 */       return false;
/*      */     }
/*  851 */     boolean retVal = false;
/*  852 */     for (int i = 0; i < parsedTree.length; ++i)
/*      */     {
/*  854 */       if (!checkTreeDefinitionComponentIsView(parsedTree[i], view))
/*      */         continue;
/*  856 */       retVal = true;
/*  857 */       break;
/*      */     }
/*      */ 
/*  860 */     return retVal;
/*      */   }
/*      */ 
/*      */   public boolean checkTreeDefinitionComponentIsView(String tmp, String view)
/*      */   {
/*  865 */     boolean retVal = false;
/*  866 */     if ((tmp != null) && (tmp.length() > 0) && 
/*  868 */       (tmp.charAt(0) == 'v'))
/*      */     {
/*  870 */       retVal = tmp.regionMatches(true, 1, view, 0, view.length());
/*      */     }
/*      */ 
/*  873 */     return retVal;
/*      */   }
/*      */ 
/*      */   public String checkAndExtractViewNameFromTreeDefinitionComponent(String tmp)
/*      */   {
/*  878 */     String result = null;
/*  879 */     if ((tmp != null) && (tmp.length() > 0) && 
/*  881 */       (tmp.charAt(0) == 'v'))
/*      */     {
/*  883 */       result = tmp.substring(1);
/*      */     }
/*      */ 
/*  886 */     return result;
/*      */   }
/*      */ 
/*      */   public Object[] expandTreeDefinition(String definition)
/*      */     throws DataException
/*      */   {
/*  892 */     String[] definitionStringArray = parseTreeDefinition(definition);
/*      */ 
/*  894 */     Object[] definitionArray = expandTreeDefinitionEx(definitionStringArray);
/*      */ 
/*  896 */     return definitionArray;
/*      */   }
/*      */ 
/*      */   public Object[] expandTreeDefinitionEx(String[] tmp)
/*      */     throws DataException
/*      */   {
/*  903 */     Object[] definition = new Object[tmp.length];
/*  904 */     for (int i = 0; i < definition.length; ++i)
/*      */     {
/*  906 */       String[] suffixes = { "view", "relationship" };
/*  907 */       String value = tmp[i];
/*  908 */       char type = value.charAt(0);
/*  909 */       value = value.substring(1);
/*      */       String msg;
/*  911 */       switch (type)
/*      */       {
/*      */       default:
/*  914 */         msg = LocaleUtils.encodeMessage("apSchemaIllegalTreeDefinition4", null, "" + type, "" + (1 + i));
/*      */ 
/*  917 */         throw new DataException(msg);
/*      */       case 'i':
/*      */         try
/*      */         {
/*  921 */           SchemaTreePointer recursivePointer = new SchemaTreePointer();
/*      */ 
/*  923 */           int index = value.indexOf(":");
/*  924 */           if (index == -1)
/*      */           {
/*  926 */             index = value.length();
/*      */           }
/*      */           else
/*      */           {
/*  930 */             recursivePointer.m_initialKeyValue = value.substring(index + 1);
/*      */           }
/*      */ 
/*  933 */           recursivePointer.m_recursiveIndex = NumberUtils.parseInteger(value.substring(0, index), 0);
/*      */ 
/*  936 */           definition[i] = recursivePointer;
/*      */         }
/*      */         catch (NumberFormatException e)
/*      */         {
/*  940 */           msg = LocaleUtils.encodeMessage("apSchemaTreeDefinitionUnableToParse", null, "" + (i + 1));
/*      */ 
/*  943 */           throw new DataException(msg);
/*      */         }
/*      */       case 'r':
/*      */       case 'v':
/*      */       }
/*  948 */       if (i % 2 == 0)
/*      */       {
/*  950 */         definition[i] = this.m_views.getData(value);
/*      */       }
/*      */       else
/*      */       {
/*  954 */         definition[i] = this.m_relationships.getData(value);
/*      */       }
/*  956 */       if (((i % 2 == 0) ? 1 : 0) != ((type == 'v') ? 1 : 0))
/*      */       {
/*  958 */         msg = LocaleUtils.encodeMessage("apSchemaIllegalTreeDefinition5_" + suffixes[((i + 1) % 2)], null, "" + (1 + i));
/*      */ 
/*  961 */         throw new DataException(msg);
/*      */       }
/*      */ 
/*  964 */       if (definition[i] != null)
/*      */         continue;
/*  966 */       String msg = LocaleUtils.encodeMessage("apSchemaTreeDefinitionUndefined_" + suffixes[(i % 2)], null, value);
/*      */ 
/*  969 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*  974 */     return definition;
/*      */   }
/*      */ 
/*      */   public void validateTreeDefinition(Object[] definition)
/*      */     throws DataException
/*      */   {
/*  980 */     if ((definition.length % 2 != 1) && (definition.length != 0))
/*      */     {
/*  982 */       String msg = LocaleUtils.encodeMessage("apSchemaIllegalTreeDefinition2", null);
/*      */ 
/*  984 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*  987 */     for (int i = 0; i < definition.length; ++i)
/*      */     {
/*  989 */       String suffix = (i % 2 == 1) ? "relationship" : "view";
/*  990 */       if (definition[i] != null)
/*      */         continue;
/*  992 */       String msg = LocaleUtils.encodeMessage("apSchemaTreeDefinitionUndefined2_" + suffix, null);
/*      */ 
/*  995 */       throw new DataException(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String createTreeString(String[] definition)
/*      */   {
/* 1002 */     Vector v = new IdcVector();
/* 1003 */     for (int i = 0; i < definition.length; ++i)
/*      */     {
/* 1005 */       v.addElement(definition[i]);
/*      */     }
/* 1007 */     String tmp = StringUtils.createString(v, '/', '^');
/* 1008 */     return tmp;
/*      */   }
/*      */ 
/*      */   public String createTreeString(Object[] definition)
/*      */     throws DataException
/*      */   {
/* 1014 */     Vector v = new IdcVector();
/*      */ 
/* 1016 */     for (int i = 0; i < definition.length; ++i)
/*      */     {
/* 1018 */       Object obj = definition[i];
/*      */ 
/* 1020 */       if (obj instanceof SchemaTreePointer)
/*      */       {
/* 1022 */         SchemaTreePointer stp = (SchemaTreePointer)obj;
/* 1023 */         String value = "i" + stp.m_recursiveIndex;
/* 1024 */         if (stp.m_initialKeyValue != null)
/*      */         {
/* 1026 */           value = value + ":" + stp.m_initialKeyValue;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*      */         String value;
/* 1029 */         if (obj instanceof SchemaViewData)
/*      */         {
/* 1031 */           SchemaViewData data = (SchemaViewData)obj;
/* 1032 */           value = "v" + data.m_name;
/*      */         }
/*      */         else
/*      */         {
/*      */           String value;
/* 1034 */           if (obj instanceof SchemaRelationData)
/*      */           {
/* 1036 */             SchemaRelationData data = (SchemaRelationData)obj;
/* 1037 */             value = "r" + data.m_name;
/*      */           }
/*      */           else
/*      */           {
/* 1041 */             String msg = LocaleUtils.encodeMessage("apSchemaIllegalTreeObjectType", null, obj.getClass().getName());
/*      */ 
/* 1044 */             throw new DataException(msg);
/*      */           }
/*      */         }
/*      */       }
/*      */       String value;
/* 1047 */       v.addElement(value);
/*      */     }
/* 1049 */     String tmp = StringUtils.createString(v, '/', '^');
/* 1050 */     return tmp;
/*      */   }
/*      */ 
/*      */   public String createJavaScriptTreeDefinition(Object[] treeDef)
/*      */     throws DataException
/*      */   {
/* 1056 */     IdcStringBuilder def = new IdcStringBuilder();
/* 1057 */     def.append("new Array(");
/* 1058 */     for (int i = 0; i < treeDef.length; ++i)
/*      */     {
/* 1060 */       if (i > 0)
/*      */       {
/* 1062 */         def.append(",");
/*      */       }
/*      */ 
/* 1065 */       def.append("new Array(");
/* 1066 */       if (treeDef[i] instanceof SchemaViewData)
/*      */       {
/* 1068 */         SchemaViewData data = (SchemaViewData)treeDef[i];
/* 1069 */         def.append("\"v\",");
/* 1070 */         def.append("g_schemaDefinition.views[\"");
/* 1071 */         def.append(StringUtils.encodeJavascriptFilename(data.m_name));
/* 1072 */         def.append("\"]");
/*      */       }
/* 1074 */       else if (treeDef[i] instanceof SchemaRelationData)
/*      */       {
/* 1076 */         SchemaRelationData data = (SchemaRelationData)treeDef[i];
/* 1077 */         def.append("\"r\",");
/* 1078 */         def.append("g_schemaDefinition.relationships[\"");
/* 1079 */         def.append(StringUtils.encodeJavascriptFilename(data.m_name));
/* 1080 */         def.append("\"]");
/*      */       }
/* 1082 */       else if (treeDef[i] instanceof SchemaTreePointer)
/*      */       {
/* 1084 */         SchemaTreePointer stp = (SchemaTreePointer)treeDef[i];
/*      */ 
/* 1086 */         def.append("\"i\"," + stp.m_recursiveIndex);
/* 1087 */         if (stp.m_initialKeyValue != null)
/*      */         {
/* 1089 */           def.append(",\"");
/* 1090 */           def.append(StringUtils.encodeJavascriptString(stp.m_initialKeyValue));
/*      */ 
/* 1092 */           def.append("\"");
/*      */         }
/*      */       }
/* 1095 */       def.append(")");
/*      */     }
/* 1097 */     def.append(")");
/* 1098 */     return def.toString();
/*      */   }
/*      */ 
/*      */   public ResultSet getTreeSelectionRow(Object[] treeDef, String key, SchemaViewData[] view, SchemaRelationData[] parentRelationship)
/*      */   {
/* 1108 */     if (treeDef.length < 1)
/*      */     {
/* 1110 */       return null;
/*      */     }
/*      */ 
/* 1113 */     int foundIndex = -1;
/* 1114 */     SchemaViewData myView = null;
/* 1115 */     ResultSet childSet = null;
/* 1116 */     boolean hasLoopBackIndex = false;
/* 1117 */     int loopBackTriggerIndex = -1;
/* 1118 */     int loopBackGotoIndex = -1;
/* 1119 */     for (int i = treeDef.length - 1; i >= 0; i -= 2)
/*      */     {
/* 1121 */       boolean processView = true;
/* 1122 */       if (treeDef[i] instanceof SchemaTreePointer)
/*      */       {
/* 1124 */         SchemaTreePointer pointer = (SchemaTreePointer)treeDef[i];
/* 1125 */         loopBackGotoIndex = i;
/* 1126 */         loopBackTriggerIndex = pointer.m_recursiveIndex;
/* 1127 */         hasLoopBackIndex = true;
/* 1128 */         processView = false;
/*      */       }
/*      */ 
/* 1133 */       if (foundIndex >= 0) {
/*      */         break;
/*      */       }
/*      */ 
/* 1137 */       if (!processView)
/*      */         continue;
/* 1139 */       myView = (SchemaViewData)treeDef[i];
/*      */       try
/*      */       {
/* 1142 */         Map args = new HashMap();
/* 1143 */         args.put("primaryKey", new String[] { key });
/* 1144 */         childSet = myView.getViewValues(args);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1148 */         Report.trace("schema", null, e);
/*      */       }
/* 1150 */       if ((childSet == null) || (childSet.isEmpty()))
/*      */         continue;
/* 1152 */       view[0] = myView;
/* 1153 */       foundIndex = i;
/*      */     }
/*      */ 
/* 1158 */     if (foundIndex == -1)
/*      */     {
/* 1160 */       return null;
/*      */     }
/* 1162 */     int startRelationshipIndex = foundIndex;
/* 1163 */     if ((hasLoopBackIndex) && (startRelationshipIndex == loopBackTriggerIndex))
/*      */     {
/* 1168 */       startRelationshipIndex = loopBackGotoIndex;
/*      */     }
/*      */ 
/* 1171 */     int relationShipIndex = startRelationshipIndex - 1;
/* 1172 */     parentRelationship[0] = null;
/* 1173 */     if ((relationShipIndex >= 0) && (relationShipIndex < treeDef.length))
/*      */     {
/* 1175 */       parentRelationship[0] = ((SchemaRelationData)treeDef[relationShipIndex]);
/*      */     }
/*      */ 
/* 1179 */     return childSet;
/*      */   }
/*      */ 
/*      */   public Vector computeTreeSelectionParents(Object[] treeDef, String startKey)
/*      */   {
/* 1184 */     if (treeDef.length < 1)
/*      */     {
/* 1186 */       return null;
/*      */     }
/*      */ 
/* 1189 */     int foundIndex = -1;
/* 1190 */     SchemaViewData myView = null;
/* 1191 */     ResultSet childSet = null;
/* 1192 */     boolean hasLoopBackIndex = false;
/* 1193 */     int loopBackTriggerIndex = -1;
/* 1194 */     int loopBackGotoIndex = -1;
/* 1195 */     for (int i = treeDef.length - 1; i >= 0; i -= 2)
/*      */     {
/* 1197 */       if (treeDef[i] instanceof SchemaTreePointer)
/*      */       {
/* 1199 */         SchemaTreePointer pointer = (SchemaTreePointer)treeDef[i];
/* 1200 */         loopBackGotoIndex = i;
/* 1201 */         loopBackTriggerIndex = pointer.m_recursiveIndex;
/* 1202 */         hasLoopBackIndex = true;
/*      */       }
/*      */       else {
/* 1205 */         myView = (SchemaViewData)treeDef[i];
/*      */         try
/*      */         {
/* 1208 */           Map args = new HashMap();
/* 1209 */           args.put("primaryKey", new String[] { startKey });
/* 1210 */           childSet = myView.getViewValues(args);
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/* 1214 */           Report.trace("schema", null, e);
/*      */         }
/* 1216 */         if ((childSet == null) || (childSet.isEmpty()))
/*      */           continue;
/* 1218 */         foundIndex = i;
/* 1219 */         break;
/*      */       }
/*      */     }
/* 1222 */     if (foundIndex == -1)
/*      */     {
/* 1224 */       return null;
/*      */     }
/*      */ 
/* 1227 */     Vector rc = new IdcVector();
/* 1228 */     rc.insertElementAt(startKey, 0);
/* 1229 */     FieldInfo info = new FieldInfo();
/* 1230 */     int numLoopbacks = 0;
/* 1231 */     for (int i = foundIndex; i >= 0; i -= 2)
/*      */     {
/* 1233 */       Object curTreeDef = treeDef[i];
/* 1234 */       if (curTreeDef instanceof SchemaTreePointer)
/*      */       {
/* 1236 */         loopBackGotoIndex = i;
/* 1237 */         loopBackTriggerIndex = ((SchemaTreePointer)curTreeDef).m_recursiveIndex;
/* 1238 */         hasLoopBackIndex = true;
/*      */       }
/*      */       else {
/* 1241 */         SchemaViewData childView = (SchemaViewData)curTreeDef;
/*      */ 
/* 1243 */         boolean loopingBack = false;
/* 1244 */         int adjustedIndex = i;
/* 1245 */         if ((hasLoopBackIndex) && (i == loopBackTriggerIndex))
/*      */         {
/* 1247 */           if (++numLoopbacks > 10)
/*      */           {
/* 1249 */             Report.trace("schema", "Too many loopbacks for view " + childView, null);
/* 1250 */             rc = null;
/* 1251 */             break;
/*      */           }
/* 1253 */           adjustedIndex = loopBackGotoIndex;
/* 1254 */           loopingBack = true;
/*      */         }
/* 1256 */         if (adjustedIndex < 2) {
/*      */           break;
/*      */         }
/*      */ 
/* 1260 */         SchemaViewData parentView = (SchemaViewData)treeDef[(adjustedIndex - 2)];
/*      */ 
/* 1262 */         SchemaRelationData parentRelation = (SchemaRelationData)treeDef[(adjustedIndex - 1)];
/*      */ 
/* 1264 */         ResultSet rset = null;
/*      */         try
/*      */         {
/* 1267 */           String parentLinkColumnForChild = getTableColumnWithData(childView, parentRelation, 0);
/*      */ 
/* 1270 */           DataResultSet toChildValues = new DataResultSet(new String[] { parentLinkColumnForChild });
/*      */ 
/* 1272 */           childSet.getFieldInfo(parentLinkColumnForChild, info);
/* 1273 */           Vector rowValues = new IdcVector();
/* 1274 */           rowValues.addElement(childSet.getStringValue(info.m_index));
/* 1275 */           toChildValues.addRow(rowValues);
/*      */ 
/* 1280 */           Map args = new HashMap();
/* 1281 */           args.put("isBackwardsRelationship", "1");
/* 1282 */           rset = parentView.getViewValues(parentRelation.m_name, toChildValues, args);
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/* 1287 */           String msg = buildReportMessage("computeTreeSelectionParents childView (" + rc + ") ", myView, childView, parentRelation, parentView);
/*      */ 
/* 1289 */           Report.trace("schema", msg, e);
/* 1290 */           rc = null;
/* 1291 */           break label656:
/*      */         }
/* 1293 */         if ((rset == null) || (rset.isEmpty()))
/*      */         {
/* 1295 */           if (loopingBack)
/*      */           {
/* 1298 */             hasLoopBackIndex = false;
/* 1299 */             continue;
/*      */           }
/* 1301 */           String msg = buildReportMessage("computeTreeSelectionParents empty rset (" + rc + ") ", myView, childView, parentRelation, parentView);
/*      */ 
/* 1304 */           Report.trace("schema", msg, null);
/* 1305 */           rc = null;
/* 1306 */           break;
/*      */         }
/* 1308 */         rset.getFieldInfo(parentView.get("schInternalColumn"), info);
/* 1309 */         String newValue = rset.getStringValue(info.m_index);
/* 1310 */         rc.insertElementAt(newValue, 0);
/* 1311 */         myView = parentView;
/* 1312 */         childSet = rset;
/*      */ 
/* 1315 */         i = adjustedIndex;
/*      */       }
/*      */     }
/* 1317 */     label656: return rc;
/*      */   }
/*      */ 
/*      */   public String buildReportMessage(String prefixMsg, SchemaViewData myView, SchemaViewData childView, SchemaRelationData parentRelation, SchemaViewData parentView)
/*      */   {
/* 1323 */     IdcStringBuilder msgBuf = new IdcStringBuilder();
/* 1324 */     msgBuf.append(prefixMsg);
/* 1325 */     msgBuf.append(" ");
/* 1326 */     String myViewName = getSafeSchemaDataName(myView);
/* 1327 */     msgBuf.append("myViewName=").append(myViewName);
/* 1328 */     if ((childView != null) && (childView != myView))
/*      */     {
/* 1330 */       String childViewName = getSafeSchemaDataName(childView);
/* 1331 */       msgBuf.append(",childViewName=").append(childViewName);
/*      */     }
/* 1333 */     String parentRelationName = getSafeSchemaDataName(parentRelation);
/* 1334 */     msgBuf.append(",parentRelationName=").append(parentRelationName);
/* 1335 */     String parentViewName = getSafeSchemaDataName(parentView);
/* 1336 */     msgBuf.append(",parentViewName=").append(parentViewName);
/* 1337 */     return msgBuf.toString();
/*      */   }
/*      */ 
/*      */   public String getSafeSchemaDataName(SchemaData data)
/*      */   {
/* 1342 */     return (data != null) ? data.m_name : "<null>";
/*      */   }
/*      */ 
/*      */   public String getAndDecode(DataBinder binder, String key)
/*      */     throws DataException
/*      */   {
/* 1348 */     String value = binder.getLocal(key);
/* 1349 */     if (value != null)
/*      */     {
/*      */       try
/*      */       {
/* 1353 */         value = StringUtils.decodeJavascriptFilename(value);
/*      */       }
/*      */       catch (CharConversionException e)
/*      */       {
/* 1357 */         String msg = LocaleUtils.encodeMessage("syParameterNotFound", null, key);
/*      */ 
/* 1359 */         throw new DataException(msg, e);
/*      */       }
/*      */     }
/* 1362 */     return value;
/*      */   }
/*      */ 
/*      */   public SchemaTableData getTable(String tableName)
/*      */   {
/* 1367 */     return (SchemaTableData)this.m_tables.getData(tableName);
/*      */   }
/*      */ 
/*      */   public SchemaFieldData getField(String fieldName)
/*      */   {
/* 1372 */     return (SchemaFieldData)this.m_fields.getData(fieldName);
/*      */   }
/*      */ 
/*      */   public SchemaViewData getView(String viewName)
/*      */   {
/* 1377 */     if ((viewName != null) && (viewName.startsWith(VIEW_PREFIX)))
/*      */     {
/* 1379 */       viewName = viewName.substring(VIEW_PREFIX.length());
/*      */     }
/* 1381 */     return (SchemaViewData)this.m_views.getData(viewName);
/*      */   }
/*      */ 
/*      */   public SchemaRelationData getRelation(String relationName)
/*      */   {
/* 1386 */     return (SchemaRelationData)this.m_relationships.getData(relationName);
/*      */   }
/*      */ 
/*      */   public String getOtherTableTableName(String viewName, String relationName)
/*      */   {
/* 1391 */     SchemaViewData viewDef = getView(viewName);
/* 1392 */     SchemaRelationData relationDef = getRelation(relationName);
/* 1393 */     String table = viewDef.get("schTableName");
/* 1394 */     String table2 = relationDef.get("schTable2Table");
/* 1395 */     return (table.equals(table2)) ? relationDef.get("schTable1Table") : table2;
/*      */   }
/*      */ 
/*      */   public String getOtherTableColumn(String viewName, String relationName)
/*      */   {
/* 1400 */     SchemaViewData viewDef = getView(viewName);
/* 1401 */     SchemaRelationData relationDef = getRelation(relationName);
/* 1402 */     return getTableColumnWithData(viewDef, relationDef, 1);
/*      */   }
/*      */ 
/*      */   public String getMyTableColumn(String viewName, String relationName)
/*      */   {
/* 1407 */     SchemaViewData viewDef = getView(viewName);
/* 1408 */     SchemaRelationData relationDef = getRelation(relationName);
/* 1409 */     return getTableColumnWithData(viewDef, relationDef, 0);
/*      */   }
/*      */ 
/*      */   public String getTableColumnWithData(SchemaViewData viewDef, SchemaRelationData relationDef, int flags)
/*      */   {
/* 1414 */     boolean getOtherView = (flags & 0x1) != 0;
/* 1415 */     String table = viewDef.get("schTableName");
/* 1416 */     boolean equalsTarget = table.equals(relationDef.get("schTable2Table"));
/* 1417 */     boolean useTable2 = (getOtherView) ? false : (!equalsTarget) ? true : equalsTarget;
/* 1418 */     String key = (useTable2) ? "schTable2Column" : "schTable1Column";
/* 1419 */     return relationDef.get(key);
/*      */   }
/*      */ 
/*      */   public DataResultSet createParentSelectorSet(String parentField, String parentValue)
/*      */   {
/* 1425 */     return createParentSelectorSetEx(new String[] { parentField }, new String[] { parentValue });
/*      */   }
/*      */ 
/*      */   public DataResultSet createParentSelectorSetEx(String[] parentFields, String[] parentValues)
/*      */   {
/* 1432 */     DataResultSet rset = new DataResultSet(parentFields);
/* 1433 */     Vector row = new IdcVector();
/* 1434 */     for (int i = 0; i < parentValues.length; ++i)
/*      */     {
/* 1436 */       row.addElement(parentValues[i]);
/*      */     }
/* 1438 */     rset.addRow(row);
/* 1439 */     return rset;
/*      */   }
/*      */ 
/*      */   public String[] constructParentValuesArray(String[] fields, ResultSet resultSet)
/*      */     throws DataException
/*      */   {
/* 1450 */     String[] values = new String[fields.length];
/* 1451 */     if (resultSet == null)
/*      */     {
/* 1453 */       for (int i = 0; i < values.length; ++i)
/*      */       {
/* 1455 */         values[i] = "";
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1460 */       FieldInfo[] infos = ResultSetUtils.createInfoList(resultSet, fields, true);
/*      */ 
/* 1462 */       for (int i = 0; i < values.length; ++i)
/*      */       {
/* 1464 */         values[i] = resultSet.getStringValue(infos[i].m_index);
/*      */       }
/*      */     }
/* 1467 */     return values;
/*      */   }
/*      */ 
/*      */   public int checkViewModificationAuthorization(int requestedAuth, SchemaViewData viewDef, SchemaSecurityFilter filter, DataResultSet rows, DataResultSet permittedRows)
/*      */     throws DataException
/*      */   {
/* 1475 */     if (permittedRows == null)
/*      */     {
/* 1477 */       permittedRows = new DataResultSet();
/*      */     }
/*      */     try
/*      */     {
/* 1481 */       filter.prepareFilter(rows, viewDef, requestedAuth);
/* 1482 */       permittedRows.copyFiltered(rows, null, filter);
/*      */     }
/*      */     finally
/*      */     {
/* 1486 */       filter.releaseFilterResultSet();
/*      */     }
/* 1488 */     if (permittedRows.getNumRows() != rows.getNumRows())
/*      */     {
/* 1490 */       return -20;
/*      */     }
/* 1492 */     return 0;
/*      */   }
/*      */ 
/*      */   public void validateViewModificationAuthorization(String msg, int requestedAuth, SchemaViewData viewDef, SchemaSecurityFilter filter, DataResultSet rows)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1504 */     int rc = checkViewModificationAuthorization(requestedAuth, viewDef, filter, rows, null);
/*      */ 
/* 1506 */     if (rc == 0)
/*      */       return;
/* 1508 */     throw new ServiceException(rc, msg);
/*      */   }
/*      */ 
/*      */   public String[][] initChoicesFromResultSet(DataResultSet rset, String internalColumn, String displayColumn)
/*      */     throws DataException
/*      */   {
/* 1515 */     String[][] choices = new String[rset.getNumRows()][];
/* 1516 */     FieldInfo[] infos = ResultSetUtils.createInfoList(rset, new String[] { internalColumn, displayColumn }, true);
/*      */ 
/* 1518 */     FieldInfo internalColumnInfo = infos[0];
/* 1519 */     FieldInfo displayColumnInfo = infos[1];
/* 1520 */     rset.first();
/* 1521 */     for (int i = 0; i < choices.length; ++i)
/*      */     {
/* 1523 */       String[] row = new String[2];
/* 1524 */       row[0] = rset.getStringValue(internalColumnInfo.m_index);
/* 1525 */       row[1] = rset.getStringValue(displayColumnInfo.m_index);
/* 1526 */       if (row[1].length() == 0)
/*      */       {
/* 1528 */         row[1] = row[0];
/*      */       }
/* 1530 */       choices[i] = row;
/* 1531 */       rset.next();
/*      */     }
/* 1533 */     return choices;
/*      */   }
/*      */ 
/*      */   public String[][] initChoicesFromView(SchemaViewData view, DataResultSet rset)
/*      */     throws DataException
/*      */   {
/* 1539 */     String keyColumn = view.get("schInternalColumn");
/* 1540 */     String labelColumn = view.get("schLabelColumn");
/* 1541 */     return initChoicesFromResultSet(rset, keyColumn, labelColumn);
/*      */   }
/*      */ 
/*      */   public FieldInfo makeFieldInfo(DataResultSet currentRow)
/*      */   {
/* 1546 */     Properties props = currentRow.getCurrentRowProps();
/* 1547 */     FieldInfo info = new FieldInfo();
/* 1548 */     info.m_name = props.getProperty("ColumnName");
/* 1549 */     info.m_type = 6;
/* 1550 */     String type = props.getProperty("ColumnType");
/* 1551 */     if (type.equalsIgnoreCase("date"))
/*      */     {
/* 1553 */       info.m_type = 5;
/*      */     }
/* 1555 */     else if (type.equalsIgnoreCase("int"))
/*      */     {
/* 1557 */       info.m_type = 3;
/*      */     }
/* 1559 */     String length = props.getProperty("ColumnLength");
/* 1560 */     info.m_isFixedLen = true;
/* 1561 */     info.m_maxLen = NumberUtils.parseInteger(length, -1);
/* 1562 */     if (info.m_maxLen == -1)
/*      */     {
/* 1564 */       info.m_isFixedLen = false;
/*      */     }
/* 1566 */     return info;
/*      */   }
/*      */ 
/*      */   public ExecutionContext constructExecutionContext(Map args)
/*      */   {
/* 1571 */     ExecutionContext context = new ExecutionContextAdaptor();
/* 1572 */     if (args != null)
/*      */     {
/* 1574 */       context.setCachedObject(SchemaCacheItem.SCHEMA_ARGS, args);
/*      */     }
/* 1576 */     return context;
/*      */   }
/*      */ 
/*      */   public ResultSet sortViewData(SchemaViewData view, ResultSet rset)
/*      */     throws DataException
/*      */   {
/* 1582 */     String sortField = view.get("schSortField");
/* 1583 */     int sortColumnIndex = ResultSetUtils.getIndexMustExist(rset, sortField);
/*      */ 
/* 1585 */     rset = doSorting(sortColumnIndex, view, rset);
/* 1586 */     return rset;
/*      */   }
/*      */ 
/*      */   public DataResultSet doSorting(int sortColumnIndex, SchemaViewData viewDef, ResultSet rset)
/*      */     throws DataException
/*      */   {
/*      */     DataResultSet sortedResultSet;
/*      */     DataResultSet sortedResultSet;
/* 1594 */     if (rset instanceof DataResultSet)
/*      */     {
/* 1596 */       sortedResultSet = (DataResultSet)rset;
/*      */     }
/*      */     else
/*      */     {
/* 1600 */       sortedResultSet = new DataResultSet();
/* 1601 */       sortedResultSet.copy(rset);
/*      */     }
/* 1603 */     checkCopyAborted(sortedResultSet, viewDef);
/* 1604 */     ResultSetTreeSort sorter = new ResultSetTreeSort(sortedResultSet, sortColumnIndex, false);
/*      */ 
/* 1606 */     sorter.determineFieldType(null);
/* 1607 */     String sortOrder = viewDef.get("schSortOrder");
/* 1608 */     sorter.determineIsAscending(sortOrder);
/* 1609 */     sorter.sort();
/* 1610 */     sortedResultSet.first();
/* 1611 */     return sortedResultSet;
/*      */   }
/*      */ 
/*      */   public boolean checkCopyAborted(DataResultSet drset, SchemaViewData data)
/*      */     throws DataException
/*      */   {
/* 1617 */     if (drset.isCopyAborted())
/*      */     {
/* 1619 */       String failOnAbort = data.get("SchemaSchemaLoaderRowOverflowError", null);
/*      */ 
/* 1621 */       if (StringUtils.convertToBool(failOnAbort, false))
/*      */       {
/* 1623 */         int limit = getViewLoadLimit(data);
/* 1624 */         String msg = LocaleUtils.encodeMessage("apSchemaRowLimitExceeded", null, "" + limit, data.m_name);
/*      */ 
/* 1627 */         throw new DataException(msg);
/*      */       }
/* 1629 */       Report.trace("schemaloader", "stopped loading rows on view '" + data.m_name + "'", null);
/*      */ 
/* 1631 */       return true;
/*      */     }
/* 1633 */     return false;
/*      */   }
/*      */ 
/*      */   public int getViewLoadLimit(SchemaViewData data)
/*      */   {
/* 1638 */     String viewLoadLimit = data.get("ServerSchemaLoaderViewLoadMaxRows", null);
/*      */ 
/* 1640 */     int limit = 0;
/* 1641 */     if (viewLoadLimit != null)
/*      */     {
/* 1643 */       limit = NumberUtils.parseInteger(viewLoadLimit, 0);
/*      */     }
/* 1645 */     return limit;
/*      */   }
/*      */ 
/*      */   public void markViewCacheDirty(String viewName, String tableName)
/*      */     throws DataException
/*      */   {
/* 1653 */     if (this.m_views == null)
/*      */       return;
/* 1655 */     if (viewName != null)
/*      */     {
/* 1657 */       SchemaViewData svd = (SchemaViewData)this.m_views.getData(viewName);
/* 1658 */       if (svd != null)
/*      */       {
/* 1660 */         svd.markCacheDirty(svd, null, null);
/*      */       }
/*      */     }
/*      */ 
/* 1664 */     if (tableName == null)
/*      */       return;
/* 1666 */     for (this.m_views.first(); this.m_views.isRowPresent(); this.m_views.next())
/*      */     {
/* 1668 */       SchemaViewData data = (SchemaViewData)this.m_views.getData();
/* 1669 */       String type = data.get("schViewType");
/* 1670 */       if (type == null) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1674 */       String table = data.get("schTableName");
/* 1675 */       if ((table == null) || (!table.equals(tableName)))
/*      */         continue;
/* 1677 */       data.markEverythingDirty();
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1686 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89426 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaHelper
 * JD-Core Version:    0.5.4
 */