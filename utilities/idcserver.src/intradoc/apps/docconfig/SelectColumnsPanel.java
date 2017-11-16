/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.iwt.DataResultSetTableModel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SelectColumnsPanel extends DocConfigPanel
/*     */ {
/*     */   protected DataResultSet m_resultSet;
/*     */   protected UdlPanel m_columnList;
/*     */ 
/*     */   public SelectColumnsPanel()
/*     */   {
/*  38 */     this.m_resultSet = null;
/*  39 */     this.m_columnList = null;
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder) throws ServiceException
/*     */   {
/*  44 */     super.initEx(sys, binder);
/*     */ 
/*  46 */     initUI();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  51 */     this.m_helper.makePanelGridBag(this, 1);
/*  52 */     this.m_columnList = SchemaHelperUtils.initListPanel(true, this.m_ctx);
/*     */ 
/*  54 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*  55 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/*  56 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  57 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  58 */     this.m_helper.addComponent(this, this.m_columnList);
/*     */   }
/*     */ 
/*     */   public IdcMessage retrievePanelValuesAndValidate()
/*     */   {
/*  65 */     StringBuffer clmnBuff = new StringBuffer();
/*  66 */     IdcMessage errMsg = null;
/*     */ 
/*  68 */     DataResultSet drset = this.m_columnList.m_list.m_tableDataModel.m_rset;
/*  69 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/*  71 */       String isSelectedStr = drset.getStringValueByName("IsSelected");
/*  72 */       boolean isSelected = StringUtils.convertToBool(isSelectedStr, false);
/*  73 */       if (!isSelected)
/*     */         continue;
/*  75 */       if (clmnBuff.length() > 0)
/*     */       {
/*  77 */         clmnBuff.append(',');
/*     */       }
/*     */ 
/*  80 */       clmnBuff.append(drset.getStringValueByName("ColumnName"));
/*     */     }
/*     */ 
/*  83 */     String clmns = clmnBuff.toString();
/*     */ 
/*  85 */     if (clmns.length() == 0)
/*     */     {
/*  88 */       errMsg = IdcMessageFactory.lc("apSchSelectColumns", new Object[0]);
/*     */     }
/*     */     else
/*     */     {
/*  92 */       this.m_helper.m_props.put("schViewColumns", clmnBuff.toString());
/*     */     }
/*  94 */     return errMsg;
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/* 100 */     DataResultSet[] clmnSet = new DataResultSet[1];
/* 101 */     String selObj = this.m_columnList.getSelectedObj();
/* 102 */     String errMsg = SchemaHelperUtils.loadColumnInfo(selObj, this.m_helper.m_props, clmnSet);
/*     */ 
/* 104 */     if (errMsg == null)
/*     */     {
/* 106 */       String str = this.m_helper.m_props.getProperty("schViewColumns");
/* 107 */       if ((str != null) && (str.length() > 0))
/*     */       {
/* 109 */         Vector sels = StringUtils.parseArray(str, ',', '^');
/* 110 */         int size = sels.size();
/* 111 */         String[] selObjects = new String[size];
/* 112 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 114 */           String sel = (String)sels.elementAt(i);
/* 115 */           selObjects[i] = sel;
/*     */         }
/* 117 */         this.m_columnList.refreshListEx(clmnSet[0], selObjects);
/*     */       }
/*     */       else
/*     */       {
/* 121 */         this.m_columnList.refreshList(clmnSet[0], selObj);
/*     */       }
/*     */     }
/* 124 */     if (errMsg == null)
/*     */       return;
/* 126 */     throw new DataException(errMsg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 132 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80220 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.SelectColumnsPanel
 * JD-Core Version:    0.5.4
 */