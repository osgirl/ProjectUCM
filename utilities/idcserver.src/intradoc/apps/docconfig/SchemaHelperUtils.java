/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.IdcTable;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.TableFields;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SchemaHelperUtils
/*     */ {
/*     */   protected static String loadColumnInfo(String selObj, Properties props, DataResultSet[] clmnSets)
/*     */   {
/*  46 */     String errMsg = null;
/*     */     try
/*     */     {
/*  49 */       DataBinder binder = new DataBinder();
/*  50 */       binder.setLocalData(props);
/*  51 */       AppLauncher.executeService("GET_SCHEMA_TABLE_INFO", binder);
/*  52 */       DataResultSet drset = (DataResultSet)binder.getResultSet("TableDefinition");
/*  53 */       if (drset == null)
/*     */       {
/*  55 */         errMsg = LocaleUtils.encodeMessage("apSchMissingTableDefinition", null, binder.getLocal("schTableName"));
/*     */       }
/*     */ 
/*  61 */       DataResultSet rset = new DataResultSet();
/*  62 */       rset.copy(drset);
/*  63 */       clmnSets[0] = rset;
/*  64 */       return errMsg;
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  68 */       Report.trace("schema", null, e);
/*  69 */       errMsg = e.getMessage();
/*     */     }
/*  71 */     return errMsg;
/*     */   }
/*     */ 
/*     */   protected static UdlPanel initListPanel(boolean isCheckbox, ExecutionContext context)
/*     */   {
/*  76 */     UdlPanel columnList = new UdlPanel(LocaleResources.getString("apLabelColumns", context), null, 250, 10, "Columns", true);
/*     */ 
/*  78 */     columnList.m_list.m_table.setRowSelectionAllowed(false);
/*  79 */     if (isCheckbox)
/*     */     {
/*  81 */       columnList.m_list.setFlags(271);
/*     */     }
/*     */ 
/*  87 */     String[] labels = { "apTitleColumnName", "apTitleColumnType", "apTitleColumnLength", "apTitleIsPrimaryKey", "apTitleIsCreateTimestamp", "apTitleIsModifyTimestamp" };
/*     */ 
/*  96 */     LocaleResources.localizeArray(labels, context);
/*     */ 
/*  98 */     if (isCheckbox)
/*     */     {
/* 100 */       ColumnInfo info = new ColumnInfo(" ", "IsSelected", 5.0D);
/* 101 */       info.m_isCheckbox = true;
/* 102 */       columnList.setColumnInfo(info);
/*     */     }
/*     */ 
/* 105 */     ColumnInfo info = new ColumnInfo(labels[0], "ColumnName", 60.0D);
/* 106 */     columnList.setColumnInfo(info);
/*     */ 
/* 108 */     info = new ColumnInfo(labels[1], "ColumnType", 20.0D);
/* 109 */     columnList.setColumnInfo(info);
/*     */ 
/* 111 */     info = new ColumnInfo(labels[2], "ColumnLength", 20.0D);
/* 112 */     columnList.setColumnInfo(info);
/*     */ 
/* 114 */     info = new ColumnInfo(labels[3], "IsPrimaryKey", 30.0D);
/* 115 */     columnList.setColumnInfo(info);
/*     */ 
/* 117 */     if (isCheckbox)
/*     */     {
/* 119 */       columnList.setVisibleColumns("IsSelected,ColumnName,ColumnType,ColumnLength,IsPrimaryKey");
/*     */     }
/*     */     else
/*     */     {
/* 123 */       columnList.setVisibleColumns("ColumnName,ColumnType,ColumnLength,IsPrimaryKey");
/*     */     }
/*     */ 
/* 126 */     columnList.setIDColumn("ColumnName");
/* 127 */     columnList.init();
/* 128 */     columnList.useDefaultListener();
/*     */ 
/* 131 */     DisplayStringCallback dispCallback = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 137 */         if (name.equals("ColumnType"))
/*     */         {
/* 139 */           return value;
/*     */         }
/* 141 */         if (name.startsWith("Is"))
/*     */         {
/* 143 */           boolean isPrimaryKey = StringUtils.convertToBool(value, false);
/* 144 */           if (isPrimaryKey)
/*     */           {
/* 146 */             value = StringUtils.getPresentationString(TableFields.YESNO_OPTIONLIST, "1");
/*     */           }
/*     */           else
/*     */           {
/* 150 */             value = "";
/*     */           }
/*     */         }
/*     */ 
/* 154 */         return value;
/*     */       }
/*     */     };
/* 157 */     columnList.setDisplayCallback("ColumnType", dispCallback);
/* 158 */     columnList.setDisplayCallback("IsPrimaryKey", dispCallback);
/*     */ 
/* 160 */     return columnList;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 165 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.SchemaHelperUtils
 * JD-Core Version:    0.5.4
 */