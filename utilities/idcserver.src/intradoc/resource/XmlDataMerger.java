/*      */ package intradoc.resource;
/*      */ 
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseOutput;
/*      */ import intradoc.common.PropertiesTreeNode;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.CharArrayWriter;
/*      */ import java.io.IOException;
/*      */ import java.io.Reader;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class XmlDataMerger
/*      */   implements DynamicDataMerger
/*      */ {
/*      */   protected String m_fileName;
/*      */   protected List m_nodes;
/*      */   protected String m_sourceEncoding;
/*      */   protected boolean m_enforceRsetColumns;
/*      */ 
/*      */   public XmlDataMerger()
/*      */   {
/*   33 */     this.m_fileName = null;
/*   34 */     this.m_nodes = null;
/*   35 */     this.m_sourceEncoding = null;
/*   36 */     this.m_enforceRsetColumns = false;
/*      */   }
/*      */ 
/*      */   public void setEnforceResultSetColumns(boolean enforceRsetColumns)
/*      */   {
/*   47 */     this.m_enforceRsetColumns = enforceRsetColumns;
/*      */   }
/*      */ 
/*      */   public void parse(Reader reader, String fileName) throws DataException
/*      */   {
/*   52 */     this.m_fileName = fileName;
/*      */     try
/*      */     {
/*   55 */       ResourceContainer res = new ResourceContainer();
/*   56 */       res.parseAndAddXmlResources(reader, fileName);
/*   57 */       this.m_nodes = res.m_xmlNodes;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*   61 */       throw new DataException(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setNodes(List nodes)
/*      */   {
/*   67 */     this.m_nodes = nodes;
/*      */   }
/*      */ 
/*      */   public List getNodes()
/*      */   {
/*   72 */     return this.m_nodes;
/*      */   }
/*      */ 
/*      */   public void write(String beginTag, ParseOutput parseOutput, String endTag)
/*      */     throws IOException
/*      */   {
/*   78 */     parseOutput.copyToPending(false, false);
/*      */ 
/*   80 */     parseOutput.clearPending();
/*      */ 
/*   83 */     writeToPending(parseOutput, "<" + beginTag + ">");
/*      */ 
/*   85 */     int num = this.m_nodes.size();
/*   86 */     for (int i = 0; i < num; ++i)
/*      */     {
/*   88 */       PropertiesTreeNode node = (PropertiesTreeNode)this.m_nodes.get(i);
/*   89 */       writeNode(parseOutput, node, 1);
/*      */     }
/*      */ 
/*   93 */     writeToPending(parseOutput, endTag);
/*   94 */     parseOutput.writePending();
/*      */   }
/*      */ 
/*      */   protected void writeNode(ParseOutput parseOutput, PropertiesTreeNode node, int depth) throws IOException
/*      */   {
/*   99 */     String tabStr = "";
/*  100 */     for (int i = 0; i < depth; ++i)
/*      */     {
/*  102 */       tabStr = tabStr + "    ";
/*      */     }
/*      */ 
/*  105 */     writeToPending(parseOutput, "\n" + tabStr + "<" + node.m_name);
/*  106 */     Properties props = node.m_properties;
/*  107 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*      */     {
/*  109 */       String name = (String)en.nextElement();
/*  110 */       String value = props.getProperty(name);
/*      */ 
/*  112 */       writeToPending(parseOutput, " " + name + "=\"" + StringUtils.encodeXmlEscapeSequence(value) + "\"");
/*      */     }
/*      */ 
/*  115 */     Vector subNodes = node.m_subNodes;
/*  116 */     int num = subNodes.size();
/*  117 */     if ((num == 0) && (node.m_value.length() == 0))
/*      */     {
/*  119 */       writeToPending(parseOutput, "/>");
/*  120 */       return;
/*      */     }
/*      */ 
/*  123 */     writeToPending(parseOutput, ">");
/*      */ 
/*  125 */     if (node.m_value.length() > 0)
/*      */     {
/*  127 */       writeToPending(parseOutput, StringUtils.encodeXmlEscapeSequence(node.m_value));
/*      */     }
/*      */ 
/*  131 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  133 */       PropertiesTreeNode subNode = (PropertiesTreeNode)subNodes.elementAt(i);
/*  134 */       writeNode(parseOutput, subNode, depth + 1);
/*      */     }
/*      */ 
/*  138 */     writeToPending(parseOutput, "</" + node.m_name + ">");
/*      */   }
/*      */ 
/*      */   public void mergeInto(DataBinder binder, Hashtable mergeRules)
/*      */     throws DataException
/*      */   {
/*  146 */     String[] fieldList = null;
/*      */ 
/*  149 */     if (mergeRules != null)
/*      */     {
/*  151 */       fieldList = (String[])(String[])mergeRules.get("FieldList");
/*      */     }
/*      */ 
/*  154 */     Hashtable cfgRules = extractRules();
/*      */ 
/*  156 */     Properties namedSets = (Properties)cfgRules.get("resultsets");
/*  157 */     if (namedSets == null)
/*      */     {
/*  159 */       namedSets = new Properties();
/*  160 */       cfgRules.put("resultset", namedSets);
/*      */     }
/*      */ 
/*  163 */     mergeInXml(binder, this.m_nodes, fieldList, namedSets, null);
/*      */ 
/*  166 */     if (fieldList != null) {
/*      */       return;
/*      */     }
/*      */ 
/*  170 */     for (Enumeration en = namedSets.keys(); en.hasMoreElements(); )
/*      */     {
/*  172 */       String key = (String)en.nextElement();
/*  173 */       String elmType = namedSets.getProperty(key);
/*  174 */       if (elmType.equals("resultset"))
/*      */       {
/*  176 */         ResultSet rset = binder.getResultSet(key);
/*  177 */         if (rset == null)
/*      */         {
/*  180 */           rset = new DataResultSet();
/*  181 */           binder.addResultSet(key, rset);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void mergeInXml(DataBinder binder, List nodes, String[] fieldList, Properties namedSets, String parent)
/*      */     throws DataException
/*      */   {
/*  192 */     int num = nodes.size();
/*  193 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  195 */       PropertiesTreeNode node = (PropertiesTreeNode)nodes.get(i);
/*  196 */       String key = node.m_name;
/*  197 */       Properties props = node.m_properties;
/*      */ 
/*  200 */       if ((fieldList != null) && (StringUtils.findStringIndex(fieldList, key) < 0))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  210 */       String lookupKey = null;
/*  211 */       if (parent != null)
/*      */       {
/*  213 */         lookupKey = parent + "!" + key;
/*      */       }
/*      */       else
/*      */       {
/*  217 */         lookupKey = key;
/*      */       }
/*      */ 
/*  220 */       String type = null;
/*  221 */       if (namedSets != null)
/*      */       {
/*  223 */         type = namedSets.getProperty(lookupKey);
/*      */       }
/*  225 */       if ((type != null) && (type.equals("resultset")))
/*      */       {
/*  228 */         if (fieldList != null) {
/*      */           continue;
/*      */         }
/*  231 */         DataResultSet rset = (DataResultSet)binder.getResultSet(lookupKey);
/*  232 */         Properties rowValues = new Properties();
/*      */ 
/*  234 */         Vector newColumns = new IdcVector();
/*      */ 
/*  237 */         if (rset == null)
/*      */         {
/*  240 */           rset = new DataResultSet();
/*      */ 
/*  243 */           binder.addResultSet(lookupKey, rset);
/*      */         }
/*      */ 
/*  247 */         findNewColumns(rset, node, lookupKey, newColumns, rowValues);
/*      */ 
/*  250 */         checkAndLoadSubNodes(rset, node.m_subNodes, lookupKey, newColumns, rowValues);
/*      */ 
/*  254 */         int numFields = rset.getNumFields();
/*  255 */         for (int j = 0; j < numFields; ++j)
/*      */         {
/*  257 */           FieldInfo fi = new FieldInfo();
/*  258 */           rset.getIndexFieldInfo(j, fi);
/*      */ 
/*  260 */           String name = fi.m_name;
/*  261 */           String val = rowValues.getProperty(name);
/*  262 */           if (val != null)
/*      */             continue;
/*  264 */           rowValues.put(name, "");
/*      */         }
/*      */ 
/*  269 */         rset.mergeFieldsWithFlags(newColumns, 0);
/*      */ 
/*  271 */         PropParameters params = new PropParameters(rowValues);
/*  272 */         Vector row = rset.createRow(params);
/*  273 */         rset.addRow(row);
/*      */       }
/*      */       else
/*      */       {
/*  278 */         binder.putLocal(lookupKey, node.m_value);
/*  279 */         for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*      */         {
/*  281 */           String name = (String)en.nextElement();
/*  282 */           String value = props.getProperty(name);
/*  283 */           binder.putLocal(lookupKey + ":" + name, value);
/*      */         }
/*  285 */         mergeInXml(binder, node.m_subNodes, fieldList, namedSets, lookupKey);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void findNewColumns(DataResultSet rset, PropertiesTreeNode node, String lookupKey, Vector newColumns, Properties rowValues)
/*      */   {
/*  297 */     Properties props = node.m_properties;
/*      */ 
/*  300 */     FieldInfo fi = new FieldInfo();
/*  301 */     rowValues.put(lookupKey, node.m_value);
/*  302 */     if ((rset == null) || (!rset.getFieldInfo(lookupKey, fi)))
/*      */     {
/*  304 */       fi.m_name = lookupKey;
/*  305 */       newColumns.addElement(fi);
/*      */     }
/*      */ 
/*  309 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*      */     {
/*  311 */       String name = (String)en.nextElement();
/*  312 */       String clmn = lookupKey + ":" + name;
/*  313 */       rowValues.put(clmn, props.getProperty(name));
/*      */ 
/*  315 */       FieldInfo info = new FieldInfo();
/*  316 */       if ((rset == null) || (!rset.getFieldInfo(clmn, info)))
/*      */       {
/*  318 */         info.m_name = clmn;
/*  319 */         newColumns.addElement(info);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkAndLoadSubNodes(DataResultSet rset, Vector subNodes, String lookupKey, Vector newColumns, Properties rowValues)
/*      */     throws DataException
/*      */   {
/*  327 */     int numSubs = subNodes.size();
/*  328 */     Hashtable subNames = new Hashtable();
/*  329 */     for (int j = 0; j < numSubs; ++j)
/*      */     {
/*  331 */       PropertiesTreeNode subNode = (PropertiesTreeNode)subNodes.elementAt(j);
/*  332 */       String subKey = lookupKey + "!" + subNode.m_name;
/*      */ 
/*  335 */       Object obj = subNames.get(subKey);
/*  336 */       if (obj != null)
/*      */       {
/*  338 */         throw new DataException(LocaleUtils.encodeMessage("csXMLDataMergerMalformedResultSet", null, lookupKey));
/*      */       }
/*      */ 
/*  342 */       subNames.put(subKey, subKey);
/*  343 */       findNewColumns(rset, subNode, subKey, newColumns, rowValues);
/*  344 */       checkAndLoadSubNodes(rset, subNode.m_subNodes, lookupKey, newColumns, rowValues);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void mergeFrom(DataBinder binder, Properties defProps, Hashtable cfgRules)
/*      */     throws ServiceException
/*      */   {
/*  356 */     Vector extraRoots = StringUtils.parseArray(binder.getLocal("ExtraRootNodes"), ',', '^');
/*  357 */     Vector editRSList = StringUtils.parseArray(binder.getLocal("resultsets"), ',', '^');
/*      */ 
/*  360 */     DataMergerParams mergerParams = new DataMergerParams();
/*  361 */     String isAppendMode = (String)cfgRules.get("isAppendMode");
/*  362 */     if (isAppendMode != null)
/*      */     {
/*  366 */       mergerParams.m_isAppendMode = StringUtils.convertToBool(isAppendMode, false);
/*  367 */       cfgRules.remove("isAppendMode");
/*  368 */       String mergeableFields = (String)cfgRules.get("appendMergeableFields");
/*  369 */       if (mergeableFields != null)
/*      */       {
/*  371 */         Vector list = StringUtils.parseArray(mergeableFields, ',', ',');
/*  372 */         String[] strList = StringUtils.convertListToArray(list);
/*  373 */         mergerParams.m_appendMergeableFields = strList;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  378 */     Properties namedSets = (Properties)cfgRules.get("resultsets");
/*  379 */     if (namedSets == null)
/*      */     {
/*  381 */       namedSets = new Properties();
/*  382 */       cfgRules.put("resultsets", namedSets);
/*      */     }
/*      */ 
/*  386 */     boolean isFinished = StringUtils.convertToBool((String)cfgRules.get("isFormFinished"), false);
/*  387 */     if (!isFinished)
/*      */     {
/*  389 */       isFinished = StringUtils.convertToBool(binder.getLocal("isFormFinished"), false);
/*      */     }
/*  391 */     cfgRules.put("isFormFinished", String.valueOf(isFinished));
/*      */ 
/*  394 */     Properties localData = binder.getLocalData();
/*  395 */     for (Enumeration en = defProps.keys(); en.hasMoreElements(); )
/*      */     {
/*  397 */       String key = (String)en.nextElement();
/*  398 */       String value = defProps.getProperty(key);
/*      */ 
/*  400 */       localData.put(key, value);
/*      */     }
/*      */ 
/*  403 */     Hashtable treeMap = new Hashtable();
/*  404 */     for (Enumeration en = localData.keys(); en.hasMoreElements(); )
/*      */     {
/*  406 */       String key = (String)en.nextElement();
/*  407 */       ParsedTree tree = parseTreeStructure(key, localData.getProperty(key));
/*  408 */       if (tree == null) continue; if (tree.m_nodeNames.size() == 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  413 */       if ((tree.m_isResultSet) && 
/*  415 */         (mergerParams.m_isAppendMode))
/*      */       {
/*  417 */         Report.trace("system", "Result set " + key + " only allows appends for this operation.", null);
/*      */       }
/*      */ 
/*  423 */       computeTreeResultSet(treeMap, tree, editRSList, namedSets);
/*  424 */       if (!tree.m_isResultSet)
/*      */       {
/*  426 */         if (mergerParams.m_isAppendMode)
/*      */         {
/*  429 */           boolean isFound = false;
/*  430 */           for (int i = 0; i < mergerParams.m_appendMergeableFields.length; ++i)
/*      */           {
/*  432 */             String allowableKey = mergerParams.m_appendMergeableFields[i];
/*  433 */             if (!allowableKey.equalsIgnoreCase(tree.m_key))
/*      */               continue;
/*  435 */             isFound = true;
/*  436 */             break;
/*      */           }
/*      */ 
/*  441 */           if (!isFound) {
/*      */             continue;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  447 */         ParsedTree rootTree = (ParsedTree)treeMap.get(tree.m_key);
/*  448 */         if (rootTree == null)
/*      */         {
/*  450 */           treeMap.put(tree.m_key, tree);
/*      */         }
/*      */         else
/*      */         {
/*  454 */           rootTree.merge(tree);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  460 */     updateNodes(treeMap, extraRoots, editRSList, namedSets, mergerParams);
/*      */ 
/*  463 */     updateRules(cfgRules);
/*      */ 
/*  466 */     cleanUpResultsets(namedSets, binder, mergerParams);
/*      */   }
/*      */ 
/*      */   protected boolean computeTreeResultSet(Hashtable treeMap, ParsedTree tree, Vector editRSList, Properties namedSets)
/*      */   {
/*  474 */     int numNodes = tree.m_nodeNames.size();
/*  475 */     String rsName = tree.m_rsName;
/*  476 */     boolean isFound = false;
/*  477 */     if (rsName == null)
/*      */     {
/*  479 */       rsName = tree.m_key;
/*  480 */       String elmType = namedSets.getProperty(tree.m_key);
/*  481 */       if (elmType == null)
/*      */       {
/*  484 */         String altName = null;
/*  485 */         if (numNodes > 1)
/*      */         {
/*  487 */           for (int i = 0; i < numNodes - 1; ++i)
/*      */           {
/*  489 */             String name = (String)tree.m_nodeNames.elementAt(i);
/*  490 */             if (altName == null)
/*      */             {
/*  492 */               altName = name;
/*      */             }
/*      */             else
/*      */             {
/*  496 */               altName = altName + "!" + name;
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*  501 */         if (altName != null)
/*      */         {
/*  503 */           elmType = namedSets.getProperty(altName);
/*      */         }
/*      */ 
/*  506 */         if (elmType != null)
/*      */         {
/*  508 */           isFound = true;
/*  509 */           tree.m_isSubNode = true;
/*  510 */           rsName = altName;
/*  511 */           tree.m_subName = ((String)tree.m_nodeNames.elementAt(numNodes - 1));
/*      */         }
/*      */         else
/*      */         {
/*  516 */           int num = editRSList.size();
/*  517 */           for (int i = 0; i < num; ++i)
/*      */           {
/*  519 */             String name = (String)editRSList.elementAt(i);
/*  520 */             if (name.equalsIgnoreCase(tree.m_key))
/*      */             {
/*  522 */               isFound = true;
/*  523 */               break;
/*      */             }
/*  525 */             if ((altName == null) || (!name.equalsIgnoreCase(altName)))
/*      */               continue;
/*  527 */             isFound = true;
/*  528 */             rsName = altName;
/*  529 */             tree.m_isSubNode = true;
/*  530 */             tree.m_subName = ((String)tree.m_nodeNames.elementAt(numNodes - 1));
/*  531 */             break;
/*      */           }
/*      */ 
/*  535 */           if (!isFound)
/*      */           {
/*  539 */             if (tree.m_isResultSet)
/*      */             {
/*  541 */               Report.trace(null, LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csXMLDataMergerValueUpdateError", null, tree.m_key), null), null);
/*      */ 
/*  545 */               return false;
/*      */             }
/*  547 */             return true;
/*      */           }
/*      */ 
/*  552 */           namedSets.put(rsName, "resultset");
/*  553 */           elmType = "resultset";
/*      */         }
/*      */ 
/*  556 */         if (!elmType.equals("resultset"))
/*      */         {
/*  558 */           Report.trace(null, LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csXMLDataMergerUndefinedResultSet", null, tree.m_key), null), null);
/*      */ 
/*  562 */           return false;
/*      */         }
/*      */       }
/*  565 */       tree.m_rsName = rsName;
/*      */     }
/*  567 */     if (!tree.m_isResultSet)
/*      */     {
/*  571 */       tree.m_isAppend = true;
/*  572 */       tree.m_isResultSet = true;
/*      */     }
/*      */ 
/*  575 */     String rsMapKey = tree.m_rsName;
/*  576 */     if (!tree.m_isAppend)
/*      */     {
/*  578 */       rsMapKey = rsMapKey + "#" + tree.m_row;
/*      */     }
/*  580 */     ParsedTree rsTree = (ParsedTree)treeMap.get(rsMapKey);
/*  581 */     if (rsTree == null)
/*      */     {
/*  583 */       if (tree.m_isSubNode)
/*      */       {
/*  585 */         tree.m_nodeNames.removeElementAt(numNodes - 1);
/*      */       }
/*      */ 
/*  588 */       rsTree = new ParsedTree(tree.m_rsName, tree.m_nodeNames, tree.m_row);
/*  589 */       rsTree.m_isResultSet = true;
/*  590 */       rsTree.m_isAppend = tree.m_isAppend;
/*  591 */       treeMap.put(rsMapKey, rsTree);
/*      */     }
/*      */ 
/*  594 */     if (tree.m_isSubNode)
/*      */     {
/*  596 */       rsTree.addSubNode(tree);
/*      */     }
/*      */     else
/*      */     {
/*  600 */       rsTree.merge(tree);
/*      */     }
/*      */ 
/*  603 */     return true;
/*      */   }
/*      */ 
/*      */   protected ParsedTree parseTreeStructure(String str, String value)
/*      */   {
/*  608 */     CharArrayWriter outbuf = new CharArrayWriter();
/*  609 */     Vector nodes = new IdcVector();
/*  610 */     boolean isField = false;
/*  611 */     boolean isCount = false;
/*  612 */     String field = null;
/*      */ 
/*  614 */     int keyStopIndex = -1;
/*      */ 
/*  616 */     char[] chArray = str.toCharArray();
/*  617 */     int len = chArray.length;
/*  618 */     for (int i = 0; i < len; ++i)
/*      */     {
/*  620 */       char ch = chArray[i];
/*  621 */       switch (ch)
/*      */       {
/*      */       case '!':
/*  624 */         if ((isField) || (isCount))
/*      */         {
/*  626 */           return null;
/*      */         }
/*  628 */         if (field != null)
/*      */         {
/*  630 */           return null;
/*      */         }
/*  632 */         nodes.addElement(outbuf.toString());
/*  633 */         outbuf.reset();
/*  634 */         break;
/*      */       case ':':
/*  637 */         if ((isField) || (isCount))
/*      */         {
/*  639 */           return null;
/*      */         }
/*  641 */         isField = true;
/*  642 */         nodes.addElement(outbuf.toString());
/*  643 */         outbuf.reset();
/*      */ 
/*  645 */         keyStopIndex = i;
/*  646 */         break;
/*      */       case '#':
/*  649 */         isCount = true;
/*  650 */         String val = outbuf.toString();
/*  651 */         outbuf.reset();
/*  652 */         if (isField)
/*      */         {
/*  654 */           if (field != null)
/*      */           {
/*  656 */             return null;
/*      */           }
/*  658 */           field = val;
/*      */         }
/*      */         else
/*      */         {
/*  662 */           nodes.addElement(val);
/*      */         }
/*      */ 
/*  665 */         if (keyStopIndex >= 0)
/*      */           continue;
/*  667 */         keyStopIndex = i; break;
/*      */       default:
/*  672 */         outbuf.write(ch);
/*      */       }
/*      */     }
/*      */ 
/*  676 */     String val = outbuf.toString();
/*  677 */     int count = 0;
/*  678 */     if (isCount)
/*      */     {
/*  680 */       count = NumberUtils.parseInteger(val, 0);
/*      */     }
/*  682 */     else if (isField)
/*      */     {
/*  684 */       field = val;
/*      */     }
/*      */     else
/*      */     {
/*  688 */       nodes.addElement(val);
/*      */     }
/*      */ 
/*  691 */     String key = null;
/*  692 */     if (keyStopIndex >= 0)
/*      */     {
/*  694 */       key = str.substring(0, keyStopIndex);
/*      */     }
/*      */     else
/*      */     {
/*  698 */       key = str;
/*      */     }
/*      */ 
/*  701 */     ParsedTree tree = new ParsedTree(key, nodes, count);
/*  702 */     tree.m_isResultSet = isCount;
/*  703 */     if (field == null)
/*      */     {
/*  705 */       tree.m_value = value;
/*      */     }
/*      */     else
/*      */     {
/*  709 */       tree.m_props.put(field, value);
/*      */     }
/*  711 */     return tree;
/*      */   }
/*      */ 
/*      */   protected void updateNodes(Hashtable treeMap, Vector extraRoots, Vector editRSList, Properties namedSets, DataMergerParams mergerParams)
/*      */     throws ServiceException
/*      */   {
/*  718 */     for (Enumeration en = treeMap.keys(); en.hasMoreElements(); )
/*      */     {
/*  720 */       String key = (String)en.nextElement();
/*      */ 
/*  724 */       ParsedTree tree = (ParsedTree)treeMap.get(key);
/*  725 */       String fNode = (String)tree.m_nodeNames.elementAt(0);
/*  726 */       if (fNode.equals("idcformrules"))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  733 */       updateNode(tree, this.m_nodes, 0, null, extraRoots, editRSList, namedSets, mergerParams);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean updateNode(ParsedTree tree, List nodes, int depth, Object[] parent, Vector extraRootNodes, Vector editRSList, Properties namedSets, DataMergerParams mergerParams)
/*      */     throws ServiceException
/*      */   {
/*  741 */     if (parent == null)
/*      */     {
/*  743 */       parent = new Object[2];
/*  744 */       parent[0] = null;
/*  745 */       parent[1] = new Integer(depth);
/*      */     }
/*      */ 
/*  748 */     int numNodes = tree.m_nodeNames.size();
/*  749 */     if (numNodes <= depth)
/*      */     {
/*  751 */       return false;
/*      */     }
/*  753 */     String treeNode = (String)tree.m_nodeNames.elementAt(depth);
/*  754 */     boolean isLast = depth == numNodes - 1;
/*      */ 
/*  756 */     boolean isUpdated = false;
/*  757 */     int count = 0;
/*  758 */     int size = nodes.size();
/*      */ 
/*  765 */     if ((!isLast) || (!tree.m_isAppend))
/*      */     {
/*  768 */       PropertiesTreeNode firstNode = null;
/*  769 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  771 */         PropertiesTreeNode node = (PropertiesTreeNode)nodes.get(i);
/*  772 */         if (!node.m_name.equalsIgnoreCase(treeNode)) {
/*      */           continue;
/*      */         }
/*  775 */         if (isLast)
/*      */         {
/*  782 */           if (tree.m_row == count)
/*      */           {
/*  784 */             if (tree.m_value != null)
/*      */             {
/*  786 */               node.m_value = tree.m_value;
/*      */             }
/*      */ 
/*  789 */             DataBinder.mergeHashTables(node.m_properties, tree.m_props);
/*      */ 
/*  792 */             addOrUpdateOrderedNodes(tree, node, firstNode, false);
/*  793 */             isUpdated = true;
/*  794 */             break;
/*      */           }
/*  796 */           if (count == 0)
/*      */           {
/*  798 */             firstNode = node;
/*      */           }
/*  800 */           ++count;
/*      */         }
/*      */         else
/*      */         {
/*  804 */           parent[0] = node;
/*  805 */           parent[1] = new Integer(depth);
/*  806 */           isUpdated = updateNode(tree, node.m_subNodes, ++depth, parent, extraRootNodes, editRSList, namedSets, mergerParams);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  813 */     if (!isUpdated)
/*      */     {
/*  818 */       if ((count == 0) && (depth == 0) && (parent[0] == null) && (!tree.m_isResultSet))
/*      */       {
/*  821 */         boolean isFound = false;
/*  822 */         int num = extraRootNodes.size();
/*  823 */         for (int i = 0; i < num; ++i)
/*      */         {
/*  825 */           String name = (String)extraRootNodes.elementAt(i);
/*  826 */           if (!name.equalsIgnoreCase(treeNode))
/*      */             continue;
/*  828 */           isFound = true;
/*  829 */           break;
/*      */         }
/*      */ 
/*  833 */         if (!isFound)
/*      */         {
/*  835 */           return false;
/*      */         }
/*      */       }
/*      */ 
/*  839 */       if (isLast)
/*      */       {
/*  841 */         addLastNode(nodes, treeNode, tree, count);
/*      */       }
/*      */       else
/*      */       {
/*  845 */         PropertiesTreeNode node = (PropertiesTreeNode)parent[0];
/*  846 */         int parentDepth = ((Integer)parent[1]).intValue();
/*  847 */         int num = tree.m_nodeNames.size();
/*  848 */         for (int i = parentDepth; i < num; ++i)
/*      */         {
/*  850 */           String name = (String)tree.m_nodeNames.elementAt(i);
/*  851 */           List siblingNodes = null;
/*  852 */           if (node == null)
/*      */           {
/*  855 */             siblingNodes = nodes;
/*      */           }
/*      */           else
/*      */           {
/*  859 */             siblingNodes = node.m_subNodes;
/*      */           }
/*      */ 
/*  862 */           if (i == num - 1)
/*      */           {
/*  865 */             addLastNode(siblingNodes, name, tree, count);
/*      */           }
/*      */           else
/*      */           {
/*  870 */             Properties props = new Properties();
/*  871 */             PropertiesTreeNode aNode = new PropertiesTreeNode(name, props);
/*      */ 
/*  873 */             siblingNodes.add(aNode);
/*  874 */             node = aNode;
/*      */           }
/*      */         }
/*      */       }
/*  878 */       isUpdated = true;
/*      */     }
/*      */ 
/*  881 */     return isUpdated;
/*      */   }
/*      */ 
/*      */   protected void updateSubNode(ParsedTree subTree, PropertiesTreeNode node)
/*      */     throws ServiceException
/*      */   {
/*  887 */     String subName = subTree.m_subName;
/*      */ 
/*  889 */     PropertiesTreeNode foundNode = null;
/*  890 */     Vector subNodes = node.m_subNodes;
/*  891 */     int num = subNodes.size();
/*  892 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  894 */       PropertiesTreeNode subNode = (PropertiesTreeNode)subNodes.elementAt(i);
/*  895 */       if (!subNode.m_name.equalsIgnoreCase(subName))
/*      */         continue;
/*  897 */       foundNode = subNode;
/*  898 */       break;
/*      */     }
/*      */     Properties props;
/*      */     Enumeration en;
/*  902 */     if (foundNode != null)
/*      */     {
/*  904 */       if (subTree.m_value != null)
/*      */       {
/*  906 */         foundNode.m_value = subTree.m_value;
/*      */       }
/*      */ 
/*  909 */       props = subTree.m_props;
/*  910 */       for (en = props.keys(); en.hasMoreElements(); )
/*      */       {
/*  912 */         String key = (String)en.nextElement();
/*  913 */         String value = props.getProperty(key);
/*  914 */         foundNode.m_properties.put(key, value);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  920 */       validateNodeName(subName);
/*  921 */       PropertiesTreeNode newNode = new PropertiesTreeNode(subName, subTree.m_props);
/*  922 */       if (subTree.m_value != null)
/*      */       {
/*  924 */         newNode.m_value = subTree.m_value;
/*      */       }
/*  926 */       subNodes.addElement(newNode);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addOrUpdateOrderedNodes(ParsedTree tree, PropertiesTreeNode node, PropertiesTreeNode firstNode, boolean isAdd)
/*      */     throws ServiceException
/*      */   {
/*  940 */     Hashtable subMap = new Hashtable();
/*  941 */     if ((firstNode != null) && (node.m_subNodes.size() == 0))
/*      */     {
/*  943 */       Vector firstSubNodes = firstNode.m_subNodes;
/*  944 */       int size2 = firstSubNodes.size();
/*  945 */       for (int j = 0; j < size2; ++j)
/*      */       {
/*  947 */         PropertiesTreeNode subNode = (PropertiesTreeNode)firstSubNodes.elementAt(j);
/*      */ 
/*  954 */         addSubNode(node, subNode.m_name, tree.m_subNodes);
/*      */ 
/*  957 */         subMap.put(subNode.m_name, subNode.m_name);
/*      */       }
/*      */     }
/*      */ 
/*  961 */     Hashtable subNodes = tree.m_subNodes;
/*  962 */     for (Enumeration en = tree.m_subNodes.keys(); en.hasMoreElements(); )
/*      */     {
/*  964 */       String subName = (String)en.nextElement();
/*      */ 
/*  966 */       Object obj = subMap.get(subName);
/*  967 */       if (obj == null)
/*      */       {
/*  970 */         if (isAdd)
/*      */         {
/*  973 */           addSubNode(node, subName, subNodes);
/*      */         }
/*      */         else
/*      */         {
/*  978 */           ParsedTree subTree = (ParsedTree)tree.m_subNodes.get(subName);
/*  979 */           updateSubNode(subTree, node);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addNode(List nodes, String name, ParsedTree tree)
/*      */     throws ServiceException
/*      */   {
/*  989 */     validateNodeName(name);
/*  990 */     PropertiesTreeNode node = new PropertiesTreeNode(name, tree.m_props);
/*  991 */     if (tree.m_value != null)
/*      */     {
/*  993 */       node.m_value = tree.m_value;
/*      */     }
/*      */ 
/*  996 */     if (tree.m_isResultSet)
/*      */     {
/*  999 */       PropertiesTreeNode firstNode = null;
/* 1000 */       int numNodes = nodes.size();
/* 1001 */       if (numNodes > 0)
/*      */       {
/* 1004 */         for (int i = 0; i < numNodes; ++i)
/*      */         {
/* 1006 */           firstNode = (PropertiesTreeNode)nodes.get(i);
/* 1007 */           if (name.equals(firstNode.m_name)) {
/*      */             break;
/*      */           }
/*      */ 
/* 1011 */           firstNode = null;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1016 */       addOrUpdateOrderedNodes(tree, node, firstNode, true);
/*      */     }
/*      */ 
/* 1019 */     nodes.add(node);
/*      */   }
/*      */ 
/*      */   protected void addSubNode(PropertiesTreeNode node, String subName, Hashtable subNodes)
/*      */     throws ServiceException
/*      */   {
/* 1025 */     ParsedTree subTree = (ParsedTree)subNodes.get(subName);
/* 1026 */     Properties subProps = null;
/* 1027 */     String subValue = "";
/* 1028 */     if (subTree == null)
/*      */     {
/* 1034 */       if (this.m_enforceRsetColumns)
/*      */       {
/* 1036 */         String msg = LocaleUtils.encodeMessage("csXMLDataMergerMissingSubNodeParameter", null, subName, node.m_name);
/*      */ 
/* 1038 */         throw new ServiceException(msg);
/*      */       }
/* 1040 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1042 */         SystemUtils.trace("system", "Column " + subName + " for parent node " + node.m_name + " that was in design of form, did not have a value supplied for it from the form update");
/*      */       }
/*      */ 
/* 1045 */       subProps = new Properties();
/*      */     }
/*      */     else
/*      */     {
/* 1049 */       subProps = subTree.m_props;
/* 1050 */       if (subTree.m_value != null)
/*      */       {
/* 1052 */         subValue = subTree.m_value;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1057 */     validateNodeName(subName);
/* 1058 */     PropertiesTreeNode subNode = new PropertiesTreeNode(subName, subProps);
/* 1059 */     subNode.m_value = subValue;
/* 1060 */     node.addSubNode(subNode);
/*      */   }
/*      */ 
/*      */   protected void addLastNode(List nodes, String name, ParsedTree tree, int count)
/*      */     throws ServiceException
/*      */   {
/* 1067 */     if ((tree.m_isResultSet) && (!tree.m_isAppend))
/*      */     {
/* 1069 */       for (int i = count; i < tree.m_row + 1; ++i)
/*      */       {
/* 1071 */         if (i == tree.m_row)
/*      */         {
/* 1073 */           addNode(nodes, name, tree);
/*      */         }
/*      */         else
/*      */         {
/* 1078 */           Properties props = new Properties();
/* 1079 */           PropertiesTreeNode aNode = new PropertiesTreeNode(name, props);
/*      */ 
/* 1081 */           nodes.add(aNode);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/* 1087 */       addNode(nodes, name, tree);
/*      */   }
/*      */ 
/*      */   protected void cleanUpResultsets(Properties namedSets, DataBinder binder, DataMergerParams mergerParams)
/*      */   {
/* 1094 */     Map deletedRowsMap = computeRowsToDelete(binder, mergerParams);
/* 1095 */     for (Enumeration en = namedSets.keys(); en.hasMoreElements(); )
/*      */     {
/* 1097 */       String key = (String)en.nextElement();
/* 1098 */       ParsedTree tree = parseTreeStructure(key, "");
/*      */ 
/* 1100 */       int size = tree.m_nodeNames.size();
/* 1101 */       if (size == 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1107 */       PropertiesTreeNode node = null;
/* 1108 */       for (int i = 0; i < size - 1; ++i)
/*      */       {
/* 1110 */         String name = (String)tree.m_nodeNames.elementAt(i);
/* 1111 */         if (node == null)
/*      */         {
/* 1114 */           node = findNode(this.m_nodes, name);
/*      */         }
/*      */         else
/*      */         {
/* 1118 */           node = findNode(node.m_subNodes, name);
/*      */         }
/*      */ 
/* 1121 */         if (node == null) {
/*      */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1127 */       if ((size == 1) || (node != null))
/*      */       {
/* 1131 */         String name = (String)tree.m_nodeNames.elementAt(size - 1);
/*      */ 
/* 1133 */         List nodes = null;
/* 1134 */         if (size == 1)
/*      */         {
/* 1136 */           nodes = this.m_nodes;
/*      */         }
/*      */         else
/*      */         {
/* 1140 */           nodes = node.m_subNodes;
/*      */         }
/*      */ 
/* 1145 */         Map deletedList = (Map)deletedRowsMap.get(key);
/* 1146 */         int rowCount = 0;
/* 1147 */         int num = nodes.size();
/* 1148 */         for (int i = 0; i < num; ++i)
/*      */         {
/* 1150 */           PropertiesTreeNode baseNode = (PropertiesTreeNode)nodes.get(i);
/* 1151 */           if (!baseNode.m_name.equalsIgnoreCase(name))
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/* 1156 */           boolean isDelete = false;
/* 1157 */           if (deletedList != null)
/*      */           {
/* 1159 */             Object obj = deletedList.get("" + rowCount);
/* 1160 */             isDelete = obj != null;
/*      */           }
/*      */ 
/* 1163 */           if (!isDelete)
/*      */           {
/* 1166 */             isDelete = isNodeEmpty(baseNode);
/*      */           }
/*      */ 
/* 1169 */           if (isDelete)
/*      */           {
/* 1171 */             nodes.remove(i);
/* 1172 */             --num;
/* 1173 */             --i;
/*      */           }
/*      */ 
/* 1178 */           ++rowCount;
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public PropertiesTreeNode findNode(List nodes, String name)
/*      */   {
/* 1186 */     int num = this.m_nodes.size();
/* 1187 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1189 */       PropertiesTreeNode node = (PropertiesTreeNode)nodes.get(i);
/* 1190 */       if (node.m_name.equalsIgnoreCase(name))
/*      */       {
/* 1195 */         return node;
/*      */       }
/*      */     }
/* 1197 */     return null;
/*      */   }
/*      */ 
/*      */   public boolean isNodeEmpty(PropertiesTreeNode node)
/*      */   {
/* 1203 */     if ((node.m_value != null) && (node.m_value.trim().length() > 0))
/*      */     {
/* 1205 */       return false;
/*      */     }
/*      */ 
/* 1208 */     Properties props = node.m_properties;
/* 1209 */     for (Enumeration enum1 = props.elements(); enum1.hasMoreElements(); )
/*      */     {
/* 1211 */       String value = (String)enum1.nextElement();
/* 1212 */       if (value.trim().length() > 0)
/*      */       {
/* 1214 */         return false;
/*      */       }
/*      */     }
/*      */ 
/* 1218 */     int numSubs = node.m_subNodes.size();
/* 1219 */     for (int j = 0; j < numSubs; ++j)
/*      */     {
/* 1221 */       PropertiesTreeNode n = (PropertiesTreeNode)node.m_subNodes.elementAt(j);
/* 1222 */       boolean isEmpty = isNodeEmpty(n);
/* 1223 */       if (!isEmpty)
/*      */       {
/* 1225 */         return false;
/*      */       }
/*      */     }
/*      */ 
/* 1229 */     return true;
/*      */   }
/*      */ 
/*      */   protected Map computeRowsToDelete(DataBinder binder, DataMergerParams mergerParams)
/*      */   {
/* 1237 */     Map map = new HashMap();
/* 1238 */     if (!mergerParams.m_isAppendMode)
/*      */     {
/* 1240 */       String rowsStr = binder.getLocal("DeleteRows");
/* 1241 */       Vector rows = StringUtils.parseArray(rowsStr, ',', '^');
/* 1242 */       int size = rows.size();
/* 1243 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 1245 */         String pair = (String)rows.elementAt(i);
/* 1246 */         int index = pair.indexOf(":");
/* 1247 */         if (index <= 0)
/*      */           continue;
/* 1249 */         String rsName = pair.substring(0, index);
/* 1250 */         String rowNo = pair.substring(index + 1);
/*      */ 
/* 1252 */         Map list = (Map)map.get(rsName);
/* 1253 */         if (list == null)
/*      */         {
/* 1255 */           list = new HashMap();
/* 1256 */           map.put(rsName, list);
/*      */         }
/* 1258 */         list.put(rowNo, "1");
/*      */       }
/*      */     }
/*      */ 
/* 1262 */     return map;
/*      */   }
/*      */ 
/*      */   public Hashtable extractRules()
/*      */   {
/* 1270 */     Hashtable cfgMap = new Hashtable();
/* 1271 */     int num = this.m_nodes.size();
/* 1272 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1277 */       PropertiesTreeNode node = (PropertiesTreeNode)this.m_nodes.get(i);
/* 1278 */       if (!node.m_name.equals("idcformrules"))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1284 */       Properties props = node.m_properties;
/* 1285 */       for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*      */       {
/* 1287 */         String key = (String)en.nextElement();
/* 1288 */         String value = props.getProperty(key);
/*      */ 
/* 1291 */         if (key.equals("resultsets"))
/*      */         {
/* 1293 */           Vector v = StringUtils.parseArray(value, ',', '^');
/* 1294 */           Properties rProps = new Properties();
/* 1295 */           int numSets = v.size();
/* 1296 */           for (int j = 0; j < numSets; ++j)
/*      */           {
/* 1298 */             String rsetName = (String)v.elementAt(j);
/* 1299 */             rProps.put(rsetName, "resultset");
/*      */           }
/*      */ 
/* 1302 */           cfgMap.put(key, rProps);
/*      */         }
/*      */         else
/*      */         {
/* 1306 */           cfgMap.put(key, value);
/*      */         }
/*      */       }
/* 1309 */       break;
/*      */     }
/*      */ 
/* 1312 */     return cfgMap;
/*      */   }
/*      */ 
/*      */   public void updateRules(Hashtable cfgRules)
/*      */   {
/* 1317 */     PropertiesTreeNode rulesNode = null;
/* 1318 */     int num = this.m_nodes.size();
/* 1319 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1321 */       PropertiesTreeNode node = (PropertiesTreeNode)this.m_nodes.get(i);
/* 1322 */       if (!node.m_name.equals("idcformrules")) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1326 */       rulesNode = node;
/* 1327 */       break;
/*      */     }
/*      */ 
/* 1330 */     Properties props = null;
/* 1331 */     if (rulesNode == null)
/*      */     {
/* 1334 */       props = new Properties();
/* 1335 */       rulesNode = new PropertiesTreeNode("idcformrules", props);
/* 1336 */       this.m_nodes.add(0, rulesNode);
/*      */     }
/*      */     else
/*      */     {
/* 1340 */       props = rulesNode.m_properties;
/*      */     }
/*      */ 
/* 1343 */     for (Enumeration en = cfgRules.keys(); en.hasMoreElements(); )
/*      */     {
/* 1345 */       String key = (String)en.nextElement();
/*      */ 
/* 1348 */       if (key.equals("isAppendMode"))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1353 */       Object value = cfgRules.get(key);
/* 1354 */       if (value instanceof Properties)
/*      */       {
/* 1357 */         Properties rProps = (Properties)value;
/* 1358 */         Vector v = new IdcVector();
/* 1359 */         for (Enumeration renum = rProps.keys(); renum.hasMoreElements(); )
/*      */         {
/* 1361 */           v.addElement(renum.nextElement());
/*      */         }
/*      */ 
/* 1364 */         value = StringUtils.createString(v, ',', '^');
/*      */       }
/* 1366 */       props.put(key, value);
/*      */     }
/*      */ 
/* 1370 */     if (this.m_sourceEncoding == null)
/*      */       return;
/* 1372 */     props.put("jcharset", this.m_sourceEncoding);
/* 1373 */     String isoEncoding = DataSerializeUtils.getIsoEncoding(this.m_sourceEncoding);
/* 1374 */     if (isoEncoding == null)
/*      */       return;
/* 1376 */     props.put("encoding", isoEncoding);
/*      */   }
/*      */ 
/*      */   public String getSourceEncoding()
/*      */   {
/* 1383 */     return this.m_sourceEncoding;
/*      */   }
/*      */ 
/*      */   public void setSourceEncoding(String srcEncoding)
/*      */   {
/* 1388 */     this.m_sourceEncoding = srcEncoding;
/*      */   }
/*      */ 
/*      */   protected void writeToPending(ParseOutput parseOutput, String buf)
/*      */     throws IOException
/*      */   {
/* 1396 */     int len = buf.length();
/* 1397 */     char[] ca = buf.toCharArray();
/*      */ 
/* 1399 */     int curOffset = 0;
/*      */ 
/* 1402 */     int amountRemaining = len;
/*      */ 
/* 1405 */     while (curOffset < len)
/*      */     {
/* 1407 */       int bufLen = parseOutput.m_pendingBuf.length;
/*      */ 
/* 1410 */       int ntocopy = bufLen - parseOutput.m_numPending;
/* 1411 */       if (ntocopy > amountRemaining)
/*      */       {
/* 1413 */         ntocopy = amountRemaining;
/*      */       }
/* 1415 */       System.arraycopy(ca, curOffset, parseOutput.m_pendingBuf, parseOutput.m_numPending, ntocopy);
/* 1416 */       parseOutput.m_numPending += ntocopy;
/* 1417 */       if (parseOutput.m_numPending >= parseOutput.m_maxPending)
/*      */       {
/* 1419 */         parseOutput.writePending();
/*      */       }
/* 1421 */       amountRemaining -= ntocopy;
/* 1422 */       curOffset += ntocopy;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void validateNodeName(String name) throws ServiceException
/*      */   {
/* 1428 */     int result = Validation.checkUrlFileSegment(name);
/* 1429 */     if (result == 0)
/*      */       return;
/* 1431 */     throw new ServiceException(LocaleUtils.encodeMessage("csXMLDataMergerIllegalCharsInNode", null, name));
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1438 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.XmlDataMerger
 * JD-Core Version:    0.5.4
 */