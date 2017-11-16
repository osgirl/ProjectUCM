/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CustomSecurityRightsData
/*     */ {
/*  32 */   public static boolean m_isInitialized = false;
/*     */ 
/*  35 */   public static String m_classesTableName = "CustomSecurityClasses";
/*  36 */   public static String m_subClassesTableName = "CustomSecuritySubClasses";
/*  37 */   public static String m_rightsTableName = "CustomSecurityRights";
/*     */ 
/*  40 */   public static Vector m_classList = null;
/*  41 */   public static Properties m_classLabelMap = null;
/*  42 */   public static Properties m_classButtonLabelMap = null;
/*  43 */   public static Properties m_classWindowLabelMap = null;
/*  44 */   public static Properties m_classUseButtonMap = null;
/*     */ 
/*  47 */   public static Hashtable m_classSubClassMap = new Hashtable();
/*  48 */   public static Properties m_subClassLabelMap = new Properties();
/*     */ 
/*  51 */   public static Hashtable m_classRightMap = new Hashtable();
/*  52 */   public static Hashtable m_subClassRightMap = new Hashtable();
/*  53 */   public static Hashtable m_rightMap = new Hashtable();
/*     */ 
/*  56 */   public static Properties m_rightFilterMap = new Properties();
/*     */ 
/*     */   public static void init(boolean isApplet, SystemInterface sysInterface)
/*     */   {
/*  60 */     if ((isApplet) && 
/*  66 */       (SharedObjects.getTable("CustomSecurityClasses") == null))
/*     */     {
/*  68 */       m_isInitialized = false;
/*     */     }
/*     */ 
/*  72 */     if (m_isInitialized)
/*     */     {
/*  74 */       return;
/*     */     }
/*     */ 
/*  79 */     if (!isInstalled())
/*     */     {
/*  81 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  88 */       if (isApplet)
/*     */       {
/*  90 */         loadSharedTables(sysInterface);
/*     */       }
/*     */ 
/*  93 */       loadClasses();
/*  94 */       loadSubClasses();
/*  95 */       loadRights();
/*  96 */       calculateSelectRights();
/*     */ 
/*  98 */       m_isInitialized = true;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 102 */       if (sysInterface != null)
/*     */       {
/* 104 */         MessageBox.reportError(sysInterface, e);
/*     */       }
/*     */       else
/*     */       {
/* 108 */         e.printStackTrace();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean isInstalled()
/*     */   {
/* 115 */     boolean isInstalled = SharedObjects.getEnvValueAsBoolean("UseCustomSecurityRights", true);
/*     */ 
/* 118 */     return isInstalled;
/*     */   }
/*     */ 
/*     */   public static void loadSharedTables(SystemInterface sys) throws ServiceException
/*     */   {
/* 123 */     DataBinder binder = new DataBinder();
/* 124 */     AppLauncher.executeService("GET_CUSTOM_SECURITY_RIGHTS_INFO", binder);
/*     */ 
/* 127 */     String[] tables = { "CustomSecurityClasses", "CustomSecuritySubClasses", "CustomSecurityRights" };
/* 128 */     int numTables = tables.length;
/* 129 */     for (int i = 0; i < numTables; ++i)
/*     */     {
/* 131 */       String tableName = tables[i];
/* 132 */       DataResultSet drset = (DataResultSet)binder.getResultSet(tableName);
/* 133 */       if (drset == null)
/*     */         continue;
/* 135 */       SharedObjects.putTable(tableName, drset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void loadClasses()
/*     */   {
/* 142 */     m_classList = new IdcVector();
/* 143 */     m_classLabelMap = new Properties();
/* 144 */     m_classButtonLabelMap = new Properties();
/* 145 */     m_classWindowLabelMap = new Properties();
/* 146 */     m_classUseButtonMap = new Properties();
/*     */ 
/* 148 */     Properties classMap = new Properties();
/*     */ 
/* 150 */     DataResultSet drset = SharedObjects.getTable(m_classesTableName);
/* 151 */     if (drset == null)
/*     */     {
/* 154 */       return;
/*     */     }
/*     */ 
/* 158 */     Vector classList = sortResultSet(drset, "order");
/*     */ 
/* 161 */     int numClasses = classList.size();
/* 162 */     for (int i = 0; i < numClasses; ++i)
/*     */     {
/* 164 */       Properties props = (Properties)classList.elementAt(i);
/*     */ 
/* 166 */       String className = getLowerCaseProperty(props, "className");
/* 167 */       if (className.trim().equals(""))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 172 */       String label = props.getProperty("btnLabel");
/* 173 */       String buttonLabel = props.getProperty("btnLabel");
/* 174 */       String windowLabel = props.getProperty("windowLabel");
/* 175 */       boolean isUseButton = getBooleanProperty(props, "isUseButton");
/*     */ 
/* 177 */       if (classMap.get(className) != null)
/*     */         continue;
/* 179 */       classMap.put(className, props);
/*     */ 
/* 181 */       m_classList.addElement(className);
/* 182 */       m_classLabelMap.put(className, label);
/* 183 */       m_classButtonLabelMap.put(className, buttonLabel);
/* 184 */       m_classWindowLabelMap.put(className, windowLabel);
/* 185 */       if (!isUseButton)
/*     */         continue;
/* 187 */       m_classUseButtonMap.put(className, "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void loadSubClasses()
/*     */     throws DataException
/*     */   {
/* 195 */     m_classSubClassMap = new Hashtable();
/* 196 */     m_subClassLabelMap = new Properties();
/*     */ 
/* 198 */     DataResultSet drset = SharedObjects.getTable(m_subClassesTableName);
/* 199 */     if (drset == null)
/*     */     {
/* 201 */       return;
/*     */     }
/*     */ 
/* 205 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 208 */       Properties props = drset.getCurrentRowProps();
/*     */ 
/* 210 */       String className = getLowerCaseProperty(props, "className");
/* 211 */       String subClassName = getLowerCaseProperty(props, "subClassName");
/* 212 */       String label = props.getProperty("label");
/* 213 */       if (label == null)
/*     */       {
/* 215 */         label = "";
/*     */       }
/*     */ 
/* 218 */       String key = className + "." + subClassName;
/*     */ 
/* 220 */       if (m_subClassLabelMap.getProperty(key) != null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 224 */       m_subClassLabelMap.put(key, label);
/*     */ 
/* 226 */       Vector subClassList = (Vector)m_classSubClassMap.get(className);
/* 227 */       if (subClassList == null)
/*     */       {
/* 229 */         subClassList = new IdcVector();
/* 230 */         m_classSubClassMap.put(className, subClassList);
/*     */       }
/* 232 */       subClassList.addElement(subClassName);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void loadRights()
/*     */   {
/* 238 */     m_classRightMap = new Hashtable();
/* 239 */     m_subClassRightMap = new Hashtable();
/* 240 */     m_rightMap = new Hashtable();
/*     */ 
/* 242 */     DataResultSet drset = SharedObjects.getTable(m_rightsTableName);
/* 243 */     if (drset == null)
/*     */     {
/* 245 */       return;
/*     */     }
/*     */ 
/* 248 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 251 */       Properties props = drset.getCurrentRowProps();
/*     */ 
/* 253 */       String className = getLowerCaseProperty(props, "className");
/* 254 */       String subClassName = getLowerCaseProperty(props, "subClassName");
/* 255 */       String right = getLowerCaseProperty(props, "right");
/* 256 */       String flag = props.getProperty("flag");
/*     */ 
/* 258 */       String label = props.getProperty("label");
/* 259 */       if ((label == null) || (label.length() == 0))
/*     */       {
/* 261 */         label = right;
/* 262 */         props.put("label", label);
/*     */       }
/*     */ 
/* 266 */       String fullSubClassName = className + "." + subClassName;
/* 267 */       String fullRight = fullSubClassName + "." + right;
/* 268 */       long privilege = NumberUtils.parseHexStringAsLong(flag);
/*     */ 
/* 270 */       props.put("fullRight", fullRight);
/* 271 */       props.put("privilege", Long.toString(privilege));
/*     */ 
/* 273 */       if (m_rightMap.get(fullRight) != null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 277 */       m_rightMap.put(fullRight, props);
/*     */ 
/* 279 */       Vector subClassRightList = (Vector)m_subClassRightMap.get(fullSubClassName);
/* 280 */       if (subClassRightList == null)
/*     */       {
/* 282 */         subClassRightList = new IdcVector();
/* 283 */         m_subClassRightMap.put(fullSubClassName, subClassRightList);
/*     */       }
/* 285 */       subClassRightList.addElement(fullRight);
/*     */ 
/* 287 */       Vector classRightList = (Vector)m_classRightMap.get(className);
/* 288 */       if (classRightList == null)
/*     */       {
/* 290 */         classRightList = new IdcVector();
/* 291 */         m_classRightMap.put(className, classRightList);
/*     */       }
/* 293 */       classRightList.addElement(fullRight);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void calculateSelectRights()
/*     */   {
/* 299 */     m_rightFilterMap = new Properties();
/*     */ 
/* 301 */     int numClasses = m_classList.size();
/* 302 */     for (int i = 0; i < numClasses; ++i)
/*     */     {
/* 304 */       String className = (String)m_classList.elementAt(i);
/* 305 */       Vector rightList = (Vector)m_classRightMap.get(className);
/* 306 */       if (rightList == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 311 */       int numRights = rightList.size();
/* 312 */       for (int j = 0; j < numRights; ++j)
/*     */       {
/* 314 */         String right = (String)rightList.elementAt(j);
/* 315 */         Properties props = (Properties)m_rightMap.get(right);
/*     */ 
/* 317 */         calculateSelectRight(props, className, "selectRights");
/* 318 */         calculateSelectRight(props, className, "unselectRights");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void calculateSelectRight(Properties props, String className, String selectKey)
/*     */   {
/* 325 */     String rights = ((String)props.get(selectKey)).toLowerCase();
/* 326 */     if (rights == null)
/*     */       return;
/* 328 */     Vector rightList = StringUtils.parseArrayEx(rights, ',', '^', true);
/* 329 */     List rightsArray = new ArrayList();
/* 330 */     Iterator it = rightList.iterator();
/* 331 */     while (it.hasNext())
/*     */     {
/* 333 */       rightsArray.add(className + "." + it.next());
/*     */     }
/* 335 */     props.put(selectKey + "ByStrings", rightsArray);
/*     */   }
/*     */ 
/*     */   public static long calculatePrivilegeFromList(String className, String rights)
/*     */   {
/* 341 */     long totalPrivilege = 0L;
/*     */ 
/* 343 */     Vector rightList = StringUtils.parseArrayEx(rights, ',', '^', true);
/* 344 */     int numRights = rightList.size();
/*     */ 
/* 346 */     for (int i = 0; i < numRights; ++i)
/*     */     {
/* 348 */       String right = (String)rightList.elementAt(i);
/* 349 */       right = right.toLowerCase();
/*     */ 
/* 352 */       boolean isAdd = true;
/* 353 */       if (right.startsWith("-"))
/*     */       {
/* 355 */         isAdd = false;
/* 356 */         right = right.substring(1);
/*     */       }
/* 358 */       else if (right.startsWith("+"))
/*     */       {
/* 360 */         right = right.substring(1);
/*     */       }
/*     */ 
/* 364 */       long curPrivilege = 0L;
/* 365 */       if ((right.equals("all")) || (right.startsWith("*.")) || (right.endsWith(".*")))
/*     */       {
/* 367 */         curPrivilege = calculateFilterPrivilege(className, right);
/*     */       }
/*     */       else
/*     */       {
/* 371 */         String fullRight = className + "." + right;
/* 372 */         curPrivilege = getRightPrivilege(fullRight);
/*     */       }
/*     */ 
/* 376 */       if (isAdd)
/*     */       {
/* 378 */         totalPrivilege |= curPrivilege;
/*     */       }
/*     */       else
/*     */       {
/* 382 */         long intersection = totalPrivilege & curPrivilege;
/* 383 */         totalPrivilege -= intersection;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 388 */     if (totalPrivilege < 0L)
/*     */     {
/* 390 */       totalPrivilege = 0L;
/*     */     }
/*     */ 
/* 393 */     return totalPrivilege;
/*     */   }
/*     */ 
/*     */   public static long calculateFilterPrivilege(String className, String filter)
/*     */   {
/* 398 */     long privilege = 0L;
/*     */ 
/* 400 */     String fullFilter = className + "." + filter;
/*     */ 
/* 402 */     String privilegeStr = (String)m_rightFilterMap.get(fullFilter);
/* 403 */     if (privilegeStr != null)
/*     */     {
/* 405 */       privilege = NumberUtils.parseLong(privilegeStr, 0L);
/* 406 */       return privilege;
/*     */     }
/*     */ 
/* 409 */     boolean isStartFilter = false;
/* 410 */     String subFilter = null;
/* 411 */     if (filter.startsWith("*."))
/*     */     {
/* 413 */       isStartFilter = true;
/* 414 */       subFilter = filter.substring(2);
/*     */     }
/* 416 */     else if (filter.endsWith(".*"))
/*     */     {
/* 418 */       int length = fullFilter.length();
/* 419 */       subFilter = fullFilter.substring(0, length - 1);
/*     */     }
/*     */ 
/* 423 */     Vector rightList = (Vector)m_classRightMap.get(className);
/* 424 */     int numRights = rightList.size();
/* 425 */     for (int i = 0; i < numRights; ++i)
/*     */     {
/* 427 */       String right = (String)rightList.elementAt(i);
/* 428 */       Properties rightProps = (Properties)m_rightMap.get(right);
/* 429 */       long rightPrivilege = NumberUtils.parseLong(rightProps.getProperty("privilege"), 0L);
/*     */ 
/* 432 */       boolean isAdd = false;
/* 433 */       if (filter.equals("all"))
/*     */       {
/* 435 */         isAdd = true;
/*     */       }
/* 439 */       else if (isStartFilter)
/*     */       {
/* 441 */         isAdd = right.endsWith(subFilter);
/*     */       }
/*     */       else
/*     */       {
/* 445 */         isAdd = right.startsWith(subFilter);
/*     */       }
/*     */ 
/* 449 */       if (!isAdd)
/*     */         continue;
/* 451 */       privilege |= rightPrivilege;
/*     */     }
/*     */ 
/* 456 */     m_rightFilterMap.put(fullFilter, "" + privilege);
/*     */ 
/* 458 */     return privilege;
/*     */   }
/*     */ 
/*     */   public static boolean hasCustomRights(UserData userData, String right)
/*     */     throws ServiceException
/*     */   {
/* 465 */     Properties rightProps = (Properties)m_rightMap.get(right.toLowerCase());
/* 466 */     if (rightProps == null)
/*     */     {
/* 468 */       return false;
/*     */     }
/*     */ 
/* 471 */     String className = getLowerCaseProperty(rightProps, "className");
/* 472 */     long rightPrivilege = NumberUtils.parseLong(rightProps.getProperty("privilege"), 0L);
/*     */ 
/* 474 */     String groupName = getClassGroup(className);
/* 475 */     RoleDefinitions roleSet = (RoleDefinitions)SharedObjects.getTable("RoleDefinition");
/*     */ 
/* 477 */     Vector roleList = SecurityUtils.getRoleList(userData);
/* 478 */     if (roleList == null)
/*     */     {
/* 480 */       return false;
/*     */     }
/*     */ 
/* 483 */     boolean hasRight = false;
/*     */ 
/* 485 */     int nroles = roleList.size();
/* 486 */     for (int i = 0; i < nroles; ++i)
/*     */     {
/* 488 */       UserAttribInfo uai = (UserAttribInfo)roleList.elementAt(i);
/* 489 */       if (!uai.m_attribType.equals("role"))
/*     */         continue;
/* 491 */       String roleName = uai.m_attribName;
/*     */ 
/* 493 */       Vector psGroups = roleSet.getPsRoleGroups(roleName);
/* 494 */       if (psGroups == null)
/*     */         continue;
/* 496 */       int numGroups = psGroups.size();
/* 497 */       for (int j = 0; j < numGroups; ++j)
/*     */       {
/* 499 */         RoleGroupData data = (RoleGroupData)psGroups.elementAt(j);
/*     */ 
/* 501 */         if ((!data.m_groupName.equals(groupName)) || 
/* 503 */           ((rightPrivilege & data.m_customPrivilege) != rightPrivilege))
/*     */           continue;
/* 505 */         hasRight = true;
/* 506 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 514 */     return hasRight;
/*     */   }
/*     */ 
/*     */   public static Vector sortResultSet(DataResultSet drset, String orderField)
/*     */   {
/* 519 */     Vector list = new IdcVector();
/* 520 */     String finalOrderField = orderField;
/*     */ 
/* 522 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 524 */       Properties rowProps = drset.getCurrentRowProps();
/* 525 */       list.addElement(rowProps);
/*     */     }
/*     */ 
/* 528 */     IdcComparator cmp = new IdcComparator(finalOrderField)
/*     */     {
/*     */       public int compare(Object obj1, Object obj2)
/*     */       {
/* 532 */         Properties p1 = (Properties)obj1;
/* 533 */         Properties p2 = (Properties)obj2;
/*     */ 
/* 535 */         String value1Str = p1.getProperty(this.val$finalOrderField);
/* 536 */         int value1 = NumberUtils.parseInteger(value1Str, 100);
/*     */ 
/* 538 */         String value2Str = p2.getProperty(this.val$finalOrderField);
/* 539 */         int value2 = NumberUtils.parseInteger(value2Str, 100);
/*     */ 
/* 541 */         return value1 - value2;
/*     */       }
/*     */     };
/* 545 */     Sort.sortVector(list, cmp);
/* 546 */     return list;
/*     */   }
/*     */ 
/*     */   public static boolean isValidClass(String className)
/*     */   {
/* 551 */     return m_classLabelMap.getProperty(className) != null;
/*     */   }
/*     */ 
/*     */   public static String getClassGroup(String className)
/*     */   {
/* 556 */     className = capitalizeClass(className);
/* 557 */     String groupName = "$" + className + "Group";
/*     */ 
/* 559 */     return groupName;
/*     */   }
/*     */ 
/*     */   public static String capitalizeClass(String className)
/*     */   {
/* 564 */     int length = className.length();
/* 565 */     if (length == 0)
/*     */     {
/* 567 */       return className;
/*     */     }
/*     */ 
/* 570 */     char firstChar = className.charAt(0);
/* 571 */     firstChar = Character.toUpperCase(firstChar);
/*     */ 
/* 573 */     if (length > 1)
/*     */     {
/* 575 */       className = firstChar + className.substring(1);
/*     */     }
/*     */     else
/*     */     {
/* 579 */       className = "" + firstChar;
/*     */     }
/*     */ 
/* 582 */     return className;
/*     */   }
/*     */ 
/*     */   public static String getClassFromGroup(String groupName)
/*     */   {
/* 587 */     if ((!groupName.startsWith("$")) || (!groupName.endsWith("Group")))
/*     */     {
/* 589 */       return null;
/*     */     }
/*     */ 
/* 592 */     int length = groupName.length();
/* 593 */     String className = groupName.substring(1, length - 5);
/*     */ 
/* 595 */     return className.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static boolean isUseButton(String className)
/*     */   {
/* 600 */     boolean isUseButton = StringUtils.convertToBool(m_classUseButtonMap.getProperty(className), false);
/*     */ 
/* 602 */     return isUseButton;
/*     */   }
/*     */ 
/*     */   public static String getClassLabel(ExecutionContext cxt, String className)
/*     */   {
/* 607 */     String label = m_classLabelMap.getProperty(className);
/* 608 */     if ((label == null) || (label.length() == 0))
/*     */     {
/* 610 */       label = capitalizeClass(className);
/*     */     }
/*     */     else
/*     */     {
/* 614 */       label = LocaleResources.getString(label, cxt);
/*     */     }
/*     */ 
/* 617 */     return label;
/*     */   }
/*     */ 
/*     */   public static String getButtonLabel(ExecutionContext cxt, String className)
/*     */   {
/* 622 */     String buttonLabel = m_classButtonLabelMap.getProperty(className);
/* 623 */     if ((buttonLabel == null) || (buttonLabel.length() == 0))
/*     */     {
/* 625 */       buttonLabel = LocaleResources.getString("apDlgButtonEditCustomRights", cxt, getClassLabel(cxt, className));
/*     */     }
/*     */     else
/*     */     {
/* 630 */       buttonLabel = LocaleResources.getString(buttonLabel, cxt);
/*     */     }
/*     */ 
/* 633 */     return buttonLabel;
/*     */   }
/*     */ 
/*     */   public static String getWindowLabel(ExecutionContext cxt, String className)
/*     */   {
/* 638 */     String windowLabel = m_classButtonLabelMap.getProperty(className);
/* 639 */     if ((windowLabel == null) || (windowLabel.length() == 0))
/*     */     {
/* 641 */       windowLabel = LocaleResources.getString("apTitleEditCustomRights", cxt, getClassLabel(cxt, className));
/*     */     }
/*     */     else
/*     */     {
/* 646 */       windowLabel = LocaleResources.getString(windowLabel, cxt);
/*     */     }
/*     */ 
/* 649 */     return windowLabel;
/*     */   }
/*     */ 
/*     */   public static String getSubClassLabel(ExecutionContext cxt, String className, String subClassName)
/*     */   {
/* 655 */     String key = className + "." + subClassName;
/* 656 */     String label = m_subClassLabelMap.getProperty(key);
/* 657 */     if ((label == null) || (label.equals("")))
/*     */     {
/* 659 */       label = subClassName;
/*     */     }
/*     */     else
/*     */     {
/* 663 */       label = LocaleResources.getString(label, cxt);
/*     */     }
/*     */ 
/* 666 */     return label;
/*     */   }
/*     */ 
/*     */   public static long getRightPrivilege(String right)
/*     */   {
/* 671 */     Properties props = (Properties)m_rightMap.get(right);
/* 672 */     if (props == null)
/*     */     {
/* 674 */       return 0L;
/*     */     }
/*     */ 
/* 677 */     long privilege = NumberUtils.parseLong(props.getProperty("privilege"), 0L);
/* 678 */     return privilege;
/*     */   }
/*     */ 
/*     */   public static String getSubClassRightLabel(ExecutionContext cxt, String fullRight)
/*     */   {
/* 683 */     Properties props = (Properties)m_rightMap.get(fullRight);
/* 684 */     if (props == null)
/*     */     {
/* 686 */       return fullRight;
/*     */     }
/*     */ 
/* 689 */     String className = getLowerCaseProperty(props, "className");
/* 690 */     String subClassName = getLowerCaseProperty(props, "subClassName");
/* 691 */     String right = getLowerCaseProperty(props, "right");
/* 692 */     String rightLabel = props.getProperty("label");
/*     */ 
/* 694 */     String subClassLabel = getSubClassLabel(cxt, className, subClassName);
/* 695 */     if ((rightLabel == null) || (rightLabel.equals("")))
/*     */     {
/* 697 */       rightLabel = right;
/*     */     }
/*     */     else
/*     */     {
/* 701 */       rightLabel = LocaleResources.getString(rightLabel, cxt);
/*     */     }
/*     */ 
/* 704 */     String label = subClassLabel + "." + rightLabel;
/* 705 */     return label;
/*     */   }
/*     */ 
/*     */   public static String getLowerCaseProperty(Properties props, String key)
/*     */   {
/* 710 */     String value = props.getProperty(key);
/* 711 */     if (value == null)
/*     */     {
/* 713 */       value = "";
/*     */     }
/*     */     else
/*     */     {
/* 717 */       value = value.toLowerCase();
/*     */     }
/*     */ 
/* 720 */     return value;
/*     */   }
/*     */ 
/*     */   public static boolean getBooleanProperty(Properties props, String key)
/*     */   {
/* 725 */     String value = props.getProperty(key);
/* 726 */     return StringUtils.convertToBool(value, false);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 731 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89192 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.CustomSecurityRightsData
 * JD-Core Version:    0.5.4
 */