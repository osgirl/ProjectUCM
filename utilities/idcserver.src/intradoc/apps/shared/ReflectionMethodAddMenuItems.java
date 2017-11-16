/*     */ package intradoc.apps.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JMenuItem;
/*     */ 
/*     */ public class ReflectionMethodAddMenuItems
/*     */ {
/*     */   public static final int MENU_ID_FIELD_INDEX = 0;
/*     */   public static final int MENU_LABEL_FIELD_INDEX = 1;
/*     */   public static final int MENU_TYPE_FIELD_INDEX = 2;
/*     */   public static final int MENU_IS_DISABLED_INDEX = 3;
/*     */   public static final int MENU_CLASS_NAME_INDEX = 4;
/*     */   public static final int MENU_METHOD_NAME_INDEX = 5;
/*  50 */   public static String[] m_extendedApplicationMenuFields = { "eamfID", "eamfLabel", "eamfType", "eamfIsDisabled", "eamfClassName", "eamfMethodName" };
/*     */ 
/*     */   public static void addReflectionMethodMenuItems(JMenu menu, MainFrame mainFrame, DataResultSet menuItems, FieldInfo[] lookupFields, String typeFilter)
/*     */   {
/*  63 */     FieldInfo disabledFi = lookupFields[3];
/*  64 */     int disabledIndex = -1;
/*  65 */     if (disabledFi != null)
/*     */     {
/*  67 */       disabledIndex = disabledFi.m_index;
/*     */     }
/*  69 */     int typeIndex = -1;
/*  70 */     if (typeFilter != null)
/*     */     {
/*  72 */       FieldInfo typeFi = lookupFields[2];
/*  73 */       typeIndex = typeFi.m_index;
/*  74 */       if (typeIndex < 0)
/*     */       {
/*  77 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  82 */     for (menuItems.first(); menuItems.isRowPresent(); menuItems.next())
/*     */     {
/*  84 */       boolean isDisabled = false;
/*  85 */       if (disabledIndex >= 0)
/*     */       {
/*  88 */         String isDisabledStr = menuItems.getStringValue(disabledIndex);
/*  89 */         isDisabled = StringUtils.convertToBool(isDisabledStr, false);
/*     */       }
/*  91 */       if (isDisabled) {
/*     */         continue;
/*     */       }
/*     */ 
/*  95 */       boolean typeMatches = true;
/*  96 */       if (typeIndex >= 0)
/*     */       {
/*  98 */         String typeStr = menuItems.getStringValue(typeIndex);
/*  99 */         typeMatches = StringUtils.matchEx(typeStr, typeFilter, true, true);
/*     */       }
/* 101 */       if (!typeMatches)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 107 */       String menuID = menuItems.getStringValue(lookupFields[0].m_index);
/* 108 */       String label = menuItems.getStringValue(lookupFields[1].m_index);
/* 109 */       String className = menuItems.getStringValue(lookupFields[4].m_index);
/* 110 */       String methodName = menuItems.getStringValue(lookupFields[5].m_index);
/* 111 */       ReflectionMethodActionListener listener = new ReflectionMethodActionListener(mainFrame, menuID, label, className, methodName);
/*     */ 
/* 113 */       ExecutionContext cxt = mainFrame.m_appHelper.getExecutionContext();
/* 114 */       JMenuItem menuItem = new JMenuItem(LocaleResources.getString(label, cxt));
/* 115 */       menuItem.addActionListener(listener);
/* 116 */       menu.add(menuItem);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void addReflectionMethodMenuItemsFromStandardTable(JMenu menu, MainFrame mainFrame, String typeFilter)
/*     */   {
/* 125 */     DataResultSet drset = SharedObjects.getTable("ExtendedApplicationMenuItems");
/* 126 */     if (drset == null)
/*     */     {
/* 128 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 133 */       FieldInfo[] fi = ResultSetUtils.createInfoList(drset, m_extendedApplicationMenuFields, false);
/* 134 */       addReflectionMethodMenuItems(menu, mainFrame, drset, fi, typeFilter);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 139 */       Report.error("system", e, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 145 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.ReflectionMethodAddMenuItems
 * JD-Core Version:    0.5.4
 */