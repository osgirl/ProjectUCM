/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UserView extends BaseView
/*     */ {
/*     */   public UserView(ContainerHelper helper, RefreshView refresher, DocumentLocalizedProfile docProfile)
/*     */   {
/*  44 */     super(helper, refresher, docProfile);
/*  45 */     this.m_columnData.m_columnStr = "dName,dFullName,dUserType,dUserAuthType";
/*     */ 
/*  50 */     this.m_customMetaInSeparatePanel = false;
/*     */   }
/*     */ 
/*     */   public void configureShowColumns(boolean forceRefresh)
/*     */   {
/*  56 */     ViewFields columnFields = this.m_columnData.m_columnFields;
/*  57 */     if ((columnFields != null) && (!forceRefresh))
/*     */       return;
/*  59 */     columnFields = new ViewFields(this.m_cxt);
/*     */     try
/*     */     {
/*  62 */       columnFields.createUserViewFields(SharedObjects.getTable("UserMetaDefinition"));
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  66 */       Report.trace(null, "Unable to read user metadata field info.", e);
/*     */     }
/*  68 */     this.m_columnData.m_columnFields = columnFields;
/*     */   }
/*     */ 
/*     */   public void configureFilter(boolean forceRefresh)
/*     */   {
/*  77 */     super.configureFilter(false);
/*     */ 
/*  79 */     if (this.m_filterFields == null)
/*     */     {
/*  82 */       FilterData data = new FilterData("dUserAuthType", "text", "LIKE", "LOCAL");
/*  83 */       data.m_isUsed = true;
/*  84 */       this.m_filterData.put(data.m_fieldDef.m_name, data);
/*     */     }
/*     */ 
/*  87 */     if ((this.m_filterFields != null) && (!forceRefresh))
/*     */       return;
/*  89 */     this.m_filterFields = new ViewFields(this.m_cxt);
/*     */     try
/*     */     {
/*  92 */       this.m_filterFields.createUserViewFields(SharedObjects.getTable("UserMetaDefinition"));
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  96 */       Report.trace(null, "Unable to read user metadata field info.", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addDisplayMaps()
/*     */   {
/* 105 */     if (this.m_displayCallback == null)
/*     */     {
/* 107 */       this.m_displayColumns = new String[] { "dUserAuthType" };
/*     */ 
/* 109 */       this.m_displayCallback = new DisplayStringCallbackAdaptor()
/*     */       {
/*     */         public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */         {
/* 115 */           String[][] displayMap = (String[][])null;
/* 116 */           if (name.equals("dUserAuthType"))
/*     */           {
/* 118 */             displayMap = TableFields.USER_AUTH_TYPES;
/*     */           }
/* 120 */           if (displayMap == null)
/*     */           {
/* 122 */             return value;
/*     */           }
/*     */ 
/* 125 */           String displayStr = StringUtils.getPresentationString(displayMap, value);
/* 126 */           if (displayStr == null)
/*     */           {
/* 128 */             displayStr = "";
/*     */           }
/* 130 */           return displayStr;
/*     */         }
/*     */       };
/*     */     }
/* 134 */     super.addDisplayMaps();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 139 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.UserView
 * JD-Core Version:    0.5.4
 */