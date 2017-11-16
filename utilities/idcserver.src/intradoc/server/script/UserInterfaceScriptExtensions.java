/*     */ package intradoc.server.script;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ScriptExtensionsAdaptor;
/*     */ import intradoc.common.ScriptInfo;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.ResultSetTreeSort;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class UserInterfaceScriptExtensions extends ScriptExtensionsAdaptor
/*     */ {
/*     */   public UserInterfaceScriptExtensions()
/*     */   {
/*  34 */     this.m_variableTable = new String[0];
/*     */ 
/*  36 */     this.m_variableDefinitionTable = new int[0][];
/*     */ 
/*  38 */     this.m_functionTable = new String[] { "createMenuInclude" };
/*     */ 
/*  50 */     this.m_functionDefinitionTable = new int[][] { { 0, -1, 0, 0, 0 } };
/*     */   }
/*     */ 
/*     */   public boolean evaluateValue(ScriptInfo info, boolean[] bVal, String[] sVal, ExecutionContext context, boolean isConditional)
/*     */   {
/*  60 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/*  61 */     if (binder == null)
/*     */     {
/*  63 */       return false;
/*     */     }
/*     */ 
/*  66 */     int[] config = (int[])(int[])info.m_entry;
/*     */ 
/*  69 */     config[0];
/*     */ 
/*  74 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/*  81 */     int[] config = (int[])(int[])info.m_entry;
/*  82 */     String function = info.m_key;
/*  83 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/*  84 */     PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(context);
/*     */ 
/*  86 */     if ((config == null) || (binder == null) || (pageMerger == null))
/*     */     {
/*  88 */       return false;
/*     */     }
/*     */ 
/*  91 */     int returnType = config[4];
/*  92 */     int nargs = args.length - 1;
/*  93 */     int minParams = config[1];
/*  94 */     if ((minParams >= 0) && (nargs < minParams))
/*     */     {
/*  96 */       String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + minParams);
/*     */ 
/*  98 */       throw new IllegalArgumentException(msg);
/*     */     }
/*     */ 
/* 104 */     String sArg1 = null;
/* 105 */     String sArg2 = null;
/* 106 */     if ((nargs > 0) && 
/* 108 */       (config[2] == 0))
/*     */     {
/* 110 */       sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*     */     }
/*     */ 
/* 113 */     if ((nargs > 1) && 
/* 115 */       (config[3] == 0))
/*     */     {
/* 117 */       sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*     */     }
/*     */ 
/* 122 */     boolean bResult = false;
/* 123 */     int iResult = 0;
/* 124 */     double dResult = 0.0D;
/* 125 */     Object oResult = null;
/*     */ 
/* 127 */     switch (config[0])
/*     */     {
/*     */     case 0:
/* 143 */       checkNonEmpty(sArg1);
/* 144 */       checkNonEmpty(sArg2);
/* 145 */       String rsetName = sArg1;
/* 146 */       String menuType = sArg2;
/*     */ 
/* 148 */       String flags = "";
/* 149 */       if (nargs > 2)
/*     */       {
/* 151 */         flags = ScriptUtils.getDisplayString(args[2], context);
/*     */       }
/*     */ 
/* 154 */       DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, rsetName, true, false);
/* 155 */       if (null == drset)
/*     */       {
/* 157 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*     */ 
/* 159 */         throw new IllegalArgumentException(msg);
/*     */       }
/*     */ 
/* 163 */       boolean isTree = false;
/* 164 */       ResultSetTreeSort sort = null;
/*     */ 
/* 167 */       boolean useMenuCounter = true;
/* 168 */       String customMenuPrefix = null;
/* 169 */       boolean onlyReturnData = false;
/* 170 */       boolean useTreeViewFormat = false;
/* 171 */       if (flags.contains("skipMenuCounter"))
/*     */       {
/* 173 */         useMenuCounter = false;
/*     */       }
/* 175 */       if (flags.contains("useCustomMenuItemPrefix"))
/*     */       {
/* 177 */         customMenuPrefix = binder.getLocal("customMenuItemPrefix");
/*     */       }
/* 179 */       if (flags.contains("getOnlyData"))
/*     */       {
/* 181 */         onlyReturnData = true;
/*     */       }
/* 183 */       if (flags.contains("useTreeViewFormat"))
/*     */       {
/* 185 */         useTreeViewFormat = true;
/*     */       }
/*     */ 
/* 191 */       List startingIncludes = new ArrayList();
/* 192 */       List endingIncludes = new ArrayList();
/* 193 */       if (!onlyReturnData)
/*     */       {
/* 195 */         if (menuType.equalsIgnoreCase("menuBar"))
/*     */         {
/* 197 */           startingIncludes.add("menu_bar_markup_begin");
/* 198 */           startingIncludes.add("menu_bar_markup_end");
/* 199 */           endingIncludes.add("add_idc_menu_bar");
/*     */         }
/* 201 */         else if (menuType.equalsIgnoreCase("simpleMenuBar"))
/*     */         {
/* 203 */           endingIncludes.add("add_idc_simple_menu_bar");
/*     */         }
/* 205 */         else if (menuType.equalsIgnoreCase("imagePopup"))
/*     */         {
/* 207 */           startingIncludes.add("menu_action_popup_image");
/* 208 */           endingIncludes.add("add_popup_menu_image_listener");
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 213 */       int spacerDepth = 0;
/* 214 */       String defaultNodeType = null;
/* 215 */       Map nodeTypes = new HashMap();
/* 216 */       if (useTreeViewFormat)
/*     */       {
/* 218 */         if (menuType.equalsIgnoreCase("trayTreeView"))
/*     */         {
/* 220 */           spacerDepth = 1;
/* 221 */           nodeTypes.put(Integer.valueOf(0), "idc.widget.TopLevelTrayNode");
/* 222 */           defaultNodeType = "idc.widget.ImageAndTextNode";
/*     */         }
/*     */         else
/*     */         {
/* 226 */           defaultNodeType = "idc.widget.TextNode";
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 231 */       Map formatStrings = new HashMap();
/* 232 */       if (!useTreeViewFormat)
/*     */       {
/* 234 */         formatStrings.put("label", "text");
/* 235 */         formatStrings.put("href", "url");
/* 236 */         formatStrings.put("children", "itemdata");
/*     */       }
/*     */       else
/*     */       {
/* 240 */         formatStrings.put("label", "label");
/* 241 */         formatStrings.put("href", "href");
/* 242 */         formatStrings.put("children", "children");
/*     */       }
/*     */ 
/* 246 */       String disabledClasses = binder.getLocal("DisabledMenuClasses");
/* 247 */       if (disabledClasses == null)
/*     */       {
/* 249 */         disabledClasses = SharedObjects.getEnvironmentValue("DisabledMenuClasses");
/*     */       }
/* 251 */       String disabledIds = binder.getLocal("DisabledMenuIds");
/* 252 */       if (disabledClasses == null)
/*     */       {
/* 254 */         disabledClasses = SharedObjects.getEnvironmentValue("DisabledMenuIds");
/*     */       }
/*     */ 
/* 258 */       String[] fields = { "id", "parentId", "class", "label", "href", "loadOrder", "ifClause", "linkTarget", "flags", "preDisplayInclude", "image", "imageOpen", "trayDocUrl", "dynamicLoadFunction" };
/*     */ 
/* 262 */       FieldInfo[] fis = null;
/*     */       try
/*     */       {
/* 265 */         fis = ResultSetUtils.createInfoList(drset, fields, false);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 269 */         throw new IllegalArgumentException(e.getMessage());
/*     */       }
/* 271 */       if (fis[0].m_index < 0)
/*     */       {
/* 273 */         String msg = LocaleUtils.encodeMessage("syColumnDoesNotExist", null, fis[0].m_name);
/* 274 */         throw new IllegalArgumentException(msg);
/*     */       }
/* 276 */       if (fis[3].m_index < 0)
/*     */       {
/* 278 */         String msg = LocaleUtils.encodeMessage("syColumnDoesNotExist", null, fis[3].m_name);
/* 279 */         throw new IllegalArgumentException(msg);
/*     */       }
/* 281 */       if (fis[1].m_index >= 0)
/*     */       {
/* 283 */         isTree = true;
/*     */       }
/*     */ 
/* 287 */       sort = new ResultSetTreeSort(drset, fis[5].m_index, isTree);
/* 288 */       sort.m_fieldSortType = 3;
/* 289 */       if (isTree)
/*     */       {
/* 291 */         sort.m_itemIdColIndex = fis[0].m_index;
/* 292 */         sort.m_parentIdColIndex = fis[1].m_index;
/*     */       }
/* 294 */       sort.sort();
/*     */ 
/* 296 */       IdcCharArrayWriter buffer = pageMerger.getTemporaryWriter();
/*     */       try
/*     */       {
/* 299 */         buffer.write("<$if not menuCounter$><$menuCounter = 0$><$endif$>");
/* 300 */         if (!onlyReturnData)
/*     */         {
/* 302 */           for (String includeName : startingIncludes)
/*     */           {
/* 304 */             buffer.write(pageMerger.evaluateResourceInclude(includeName));
/*     */           }
/*     */ 
/* 307 */           buffer.write("<script type=\"text/javascript\">\n");
/* 308 */           buffer.write("var ");
/* 309 */           if (customMenuPrefix != null)
/*     */           {
/* 311 */             buffer.write(customMenuPrefix + "_");
/*     */           }
/*     */           else
/*     */           {
/* 315 */             buffer.write("menuItems_");
/*     */           }
/* 317 */           if (useMenuCounter)
/*     */           {
/* 319 */             buffer.write("<$menuCounter$>");
/*     */           }
/* 321 */           buffer.write(" = [\n");
/*     */         }
/*     */         else
/*     */         {
/* 325 */           buffer.write("[\n");
/*     */         }
/*     */ 
/* 328 */         String previousId = null;
/* 329 */         boolean previousWasGroup = false;
/* 330 */         boolean hasItems = false;
/* 331 */         ArrayList parents = new ArrayList();
/* 332 */         ArrayList groups = new ArrayList();
/* 333 */         int indent = 1;
/* 334 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/* 336 */           String currentId = drset.getStringValue(fis[0].m_index);
/* 337 */           String parentId = ResultSetUtils.getStringValueIfExists(drset, fis[1].m_index);
/* 338 */           String currentClass = ResultSetUtils.getStringValueIfExists(drset, fis[2].m_index);
/* 339 */           String label = drset.getStringValue(fis[3].m_index);
/* 340 */           String href = ResultSetUtils.getStringValueIfExists(drset, fis[4].m_index);
/* 341 */           String ifClause = ResultSetUtils.getStringValueIfExists(drset, fis[6].m_index);
/* 342 */           String target = ResultSetUtils.getStringValueIfExists(drset, fis[7].m_index);
/* 343 */           String currentFlags = ResultSetUtils.getStringValueIfExists(drset, fis[8].m_index);
/* 344 */           String preDisplayInclude = ResultSetUtils.getStringValueIfExists(drset, fis[9].m_index);
/*     */ 
/* 348 */           String image = null;
/* 349 */           String imageOpen = null;
/* 350 */           String trayDocUrl = null;
/* 351 */           String dynamicLoadCallback = null;
/* 352 */           if (useTreeViewFormat)
/*     */           {
/* 354 */             image = ResultSetUtils.getStringValueIfExists(drset, fis[10].m_index);
/* 355 */             imageOpen = ResultSetUtils.getStringValueIfExists(drset, fis[11].m_index);
/* 356 */             trayDocUrl = ResultSetUtils.getStringValueIfExists(drset, fis[12].m_index);
/* 357 */             dynamicLoadCallback = ResultSetUtils.getStringValueIfExists(drset, fis[13].m_index);
/*     */           }
/*     */ 
/* 360 */           boolean isGroup = (currentFlags != null) && (currentFlags.contains("isGroup"));
/*     */ 
/* 364 */           if ((!useTreeViewFormat) && (((href == null) || (href.length() == 0))))
/*     */           {
/* 366 */             href = "#";
/*     */           }
/*     */ 
/* 370 */           if ((parentId != null) && (parentId.length() > 0) && (!parentId.equals(previousId)))
/*     */           {
/* 372 */             boolean hasValidParent = false;
/* 373 */             for (int i = 0; i < parents.size(); ++i)
/*     */             {
/* 375 */               String tmpParent = (String)parents.get(i);
/* 376 */               if (!parentId.equals(tmpParent))
/*     */                 continue;
/* 378 */               hasValidParent = true;
/* 379 */               break;
/*     */             }
/*     */ 
/* 383 */             if (!hasValidParent)
/*     */             {
/*     */               continue;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 390 */           if ((disabledClasses != null) && (currentClass != null) && (StringUtils.matchEx(currentClass, disabledClasses, true, false))) {
/*     */             continue;
/*     */           }
/*     */ 
/* 394 */           if ((disabledIds != null) && (currentClass != null) && (StringUtils.matchEx(currentId, disabledIds, true, false)))
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 399 */           if ((parentId != null) && (previousId != null) && (previousId.equals(parentId)))
/*     */           {
/* 402 */             parents.add(parentId);
/* 403 */             if (previousWasGroup)
/*     */             {
/* 405 */               groups.add("1");
/*     */             }
/*     */             else
/*     */             {
/* 409 */               groups.add("");
/*     */             }
/*     */ 
/* 413 */             if (previousWasGroup)
/*     */             {
/* 415 */               if (!useTreeViewFormat)
/*     */               {
/* 417 */                 indent(buffer, indent);
/* 418 */                 buffer.write("[\n");
/* 419 */                 ++indent;
/*     */               }
/*     */             }
/*     */             else
/*     */             {
/* 424 */               if (!useTreeViewFormat)
/*     */               {
/* 426 */                 buffer.write(", submenu: { id: \"");
/* 427 */                 if (customMenuPrefix != null)
/*     */                 {
/* 429 */                   buffer.write(customMenuPrefix + "_");
/*     */                 }
/*     */                 else
/*     */                 {
/* 433 */                   buffer.write("menu_");
/*     */                 }
/* 435 */                 if (useMenuCounter)
/*     */                 {
/* 437 */                   buffer.write("<$menuCounter$>_");
/*     */                 }
/* 439 */                 buffer.write(previousId);
/* 440 */                 buffer.write("\"");
/*     */               }
/* 442 */               buffer.write(", ");
/* 443 */               buffer.write((String)formatStrings.get("children"));
/* 444 */               buffer.write(": [\n");
/* 445 */               ++indent;
/*     */             }
/*     */           }
/* 448 */           else if ((parentId != null) && (parents.size() > 0) && (!parentId.equals(parents.get(parents.size() - 1))))
/*     */           {
/* 454 */             if ((parentId.length() > 0) && (parents.indexOf(parentId) < 0))
/*     */             {
/*     */               continue;
/*     */             }
/*     */ 
/* 459 */             int numPopped = 0;
/* 460 */             if (parentId.length() == 0)
/*     */             {
/* 463 */               numPopped = parents.size();
/*     */             }
/*     */             else
/*     */             {
/* 467 */               int index = parents.indexOf(parentId);
/* 468 */               if (index >= 0)
/*     */               {
/* 470 */                 numPopped = parents.size() - 1 - index;
/*     */               }
/*     */             }
/*     */ 
/* 474 */             if (numPopped > 0)
/*     */             {
/* 477 */               if (previousId != null)
/*     */               {
/* 479 */                 if (!previousWasGroup)
/*     */                 {
/* 481 */                   buffer.write("},\n");
/*     */                 }
/* 483 */                 buffer.write("<$endif$>\n");
/*     */               }
/*     */ 
/* 486 */               for (int i = numPopped; i > 0; --i)
/*     */               {
/* 488 */                 String isGroupStr = (String)groups.remove(groups.size() - 1);
/* 489 */                 if (isGroupStr.equals("1"))
/*     */                 {
/* 491 */                   if (!useTreeViewFormat)
/*     */                   {
/* 493 */                     --indent;
/* 494 */                     indent(buffer, indent);
/* 495 */                     buffer.write("],\n");
/*     */                   }
/*     */                 }
/*     */                 else
/*     */                 {
/* 500 */                   --indent;
/* 501 */                   indent(buffer, indent);
/* 502 */                   buffer.write("] ");
/* 503 */                   if (!useTreeViewFormat)
/*     */                   {
/* 505 */                     buffer.write("} ");
/*     */                   }
/* 507 */                   buffer.write("},\n");
/*     */                 }
/*     */ 
/* 510 */                 buffer.write("<$endif$>\n");
/*     */ 
/* 512 */                 parents.remove(parents.size() - 1);
/*     */               }
/*     */             }
/*     */           }
/* 516 */           else if (previousId != null)
/*     */           {
/* 519 */             if (!previousWasGroup)
/*     */             {
/* 521 */               buffer.write("},\n");
/*     */             }
/* 523 */             buffer.write("<$endif$>\n");
/*     */           }
/*     */ 
/* 528 */           buffer.write("<$isExcluded='', isDisabled='', isChecked=''$>");
/* 529 */           if ((preDisplayInclude != null) && (preDisplayInclude.length() > 0))
/*     */           {
/* 531 */             buffer.write("<$exec rsSetRow('" + rsetName + "', " + drset.getCurrentRow() + ")$>\n");
/* 532 */             buffer.write("<$exec rsLoopSingleRowInclude('" + rsetName + "', '" + preDisplayInclude + "')$>\n");
/*     */           }
/*     */ 
/* 535 */           buffer.write("<$if (not #local.isExcluded)");
/* 536 */           if ((ifClause != null) && (ifClause.length() > 0))
/*     */           {
/* 538 */             buffer.write(" and (");
/* 539 */             buffer.write(ifClause);
/* 540 */             buffer.write(")");
/*     */           }
/* 542 */           buffer.write("$>\n");
/*     */ 
/* 544 */           if (!isGroup)
/*     */           {
/* 546 */             hasItems = true;
/* 547 */             indent(buffer, indent);
/* 548 */             buffer.write("{ id: \"");
/* 549 */             if (customMenuPrefix != null)
/*     */             {
/* 551 */               if (customMenuPrefix.length() > 0)
/*     */               {
/* 553 */                 buffer.write(customMenuPrefix + "_");
/*     */               }
/*     */ 
/*     */             }
/*     */             else {
/* 558 */               buffer.write("menuitem_");
/*     */             }
/* 560 */             if (useMenuCounter)
/*     */             {
/* 562 */               buffer.write("<$menuCounter$>_");
/*     */             }
/* 564 */             buffer.write(currentId);
/* 565 */             buffer.write("\", ");
/* 566 */             buffer.write((String)formatStrings.get("label"));
/* 567 */             buffer.write(": \"");
/* 568 */             buffer.write(label);
/* 569 */             buffer.write("\", ");
/* 570 */             buffer.write((String)formatStrings.get("href"));
/* 571 */             buffer.write(": \"");
/* 572 */             buffer.write(href);
/* 573 */             buffer.write("\"");
/* 574 */             buffer.write("<$if #local.isDisabled$>, disabled:true<$endif$>");
/* 575 */             buffer.write("<$if #local.isChecked$>, checked:true<$endif$>");
/* 576 */             if ((target != null) && (target.length() > 0))
/*     */             {
/* 578 */               buffer.write(", target: \"");
/* 579 */               buffer.write(target);
/* 580 */               buffer.write("\"");
/*     */             }
/* 582 */             if (useTreeViewFormat)
/*     */             {
/* 584 */               String nodeType = defaultNodeType;
/* 585 */               int depth = parents.size();
/* 586 */               if (nodeTypes.containsKey(Integer.valueOf(depth)))
/*     */               {
/* 588 */                 nodeType = (String)nodeTypes.get(Integer.valueOf(depth));
/*     */               }
/* 590 */               buffer.write(", type: ");
/* 591 */               buffer.write(nodeType);
/*     */ 
/* 593 */               if ((image != null) && (image.length() > 0))
/*     */               {
/* 595 */                 buffer.write(", image: \"");
/* 596 */                 buffer.write(image);
/* 597 */                 buffer.write("\"");
/*     */ 
/* 599 */                 if ((imageOpen != null) && (imageOpen.length() > 0))
/*     */                 {
/* 601 */                   buffer.write(", imageOpen: \"");
/* 602 */                   buffer.write(imageOpen);
/* 603 */                   buffer.write("\"");
/*     */                 }
/*     */               }
/* 606 */               if ((trayDocUrl != null) && (trayDocUrl.length() > 0))
/*     */               {
/* 608 */                 buffer.write(", trayDocUrl: \"");
/* 609 */                 buffer.write(trayDocUrl);
/* 610 */                 buffer.write("\"");
/*     */               }
/* 612 */               if ((dynamicLoadCallback != null) && (dynamicLoadCallback.length() > 0))
/*     */               {
/* 614 */                 buffer.write(", dynamicLoadCallback: ");
/* 615 */                 buffer.write(dynamicLoadCallback);
/*     */               }
/* 617 */               if (spacerDepth > 0)
/*     */               {
/* 619 */                 buffer.write(", spacerDepth: ");
/* 620 */                 buffer.write(spacerDepth + "");
/*     */               }
/*     */             }
/*     */           }
/*     */ 
/* 625 */           previousId = currentId;
/* 626 */           previousWasGroup = isGroup;
/*     */         }
/*     */ 
/* 630 */         if (hasItems)
/*     */         {
/* 632 */           buffer.write("},\n");
/* 633 */           buffer.write("<$endif$>\n");
/*     */         }
/*     */ 
/* 636 */         for (int i = parents.size(); i > 0; --i)
/*     */         {
/* 638 */           String isGroupStr = (String)groups.remove(groups.size() - 1);
/* 639 */           --indent;
/* 640 */           indent(buffer, indent);
/* 641 */           if (isGroupStr.equals("1"))
/*     */           {
/* 643 */             buffer.write("],\n");
/*     */           }
/*     */           else
/*     */           {
/* 647 */             buffer.write("] ");
/* 648 */             if (!useTreeViewFormat)
/*     */             {
/* 650 */               buffer.write("} ");
/*     */             }
/* 652 */             buffer.write("}\n");
/*     */           }
/*     */ 
/* 655 */           buffer.write("<$endif$>\n");
/*     */ 
/* 657 */           parents.remove(parents.size() - 1);
/*     */         }
/*     */ 
/* 660 */         if (!onlyReturnData)
/*     */         {
/* 662 */           buffer.write("];\n");
/* 663 */           buffer.write("</script>\n");
/*     */ 
/* 665 */           for (String includeName : endingIncludes)
/*     */           {
/* 667 */             buffer.write(pageMerger.evaluateResourceInclude(includeName));
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 672 */           buffer.write("]\n");
/*     */         }
/* 674 */         buffer.write("<$menuCounter = menuCounter + 1$>");
/* 675 */         oResult = buffer.toString();
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 679 */         SystemUtils.traceDumpException("idocscript", null, e);
/*     */       }
/*     */       finally
/*     */       {
/* 683 */         pageMerger.releaseTemporaryWriter(buffer);
/*     */       }
/* 685 */       break;
/*     */     default:
/* 688 */       return false;
/*     */     }
/*     */ 
/* 692 */     args[nargs] = ScriptExtensionUtils.computeReturnObject(returnType, bResult, iResult, dResult, oResult);
/*     */ 
/* 695 */     return true;
/*     */   }
/*     */ 
/*     */   private void indent(IdcCharArrayWriter buffer, int indent) throws IOException
/*     */   {
/* 700 */     while (indent > 0)
/*     */     {
/* 702 */       buffer.append("\t");
/* 703 */       --indent;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 709 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 88039 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.UserInterfaceScriptExtensions
 * JD-Core Version:    0.5.4
 */