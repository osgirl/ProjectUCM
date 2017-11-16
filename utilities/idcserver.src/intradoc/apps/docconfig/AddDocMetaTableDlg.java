/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddDocMetaTableDlg extends DialogCallback
/*     */ {
/*  47 */   protected SystemInterface m_systemInterface = null;
/*  48 */   protected ExecutionContext m_context = null;
/*  49 */   protected DialogHelper m_helper = null;
/*     */   protected ComboChoice m_dmsTable;
/*  51 */   protected SharedContext m_shContext = null;
/*  52 */   protected String m_helpPage = null;
/*     */ 
/*  54 */   protected List<String> m_allDMSTables = new ArrayList();
/*     */ 
/*  56 */   protected DataBinder m_binder = null;
/*     */ 
/*     */   public AddDocMetaTableDlg(SystemInterface sys, SharedContext shContext, String title, String helpPage)
/*     */   {
/*  60 */     this.m_systemInterface = sys;
/*  61 */     this.m_context = sys.getExecutionContext();
/*  62 */     this.m_shContext = shContext;
/*  63 */     this.m_helper = new DialogHelper(sys, title, true);
/*  64 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public void init(Properties props)
/*     */   {
/*  69 */     this.m_helper.m_props = props;
/*  70 */     this.m_binder = new DataBinder();
/*     */ 
/*  73 */     this.m_dlgHelper = this.m_helper;
/*     */     try
/*     */     {
/*  77 */       initUI(this, this.m_binder);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  81 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  85 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initUI(DialogCallback okCallback, DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/*  92 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/*  95 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/*  97 */     this.m_binder.setLocalData(this.m_helper.m_props);
/*     */ 
/*  99 */     this.m_dmsTable = new ComboChoice();
/* 100 */     this.m_dmsTable.initChoiceList(getDMSTableList());
/*     */ 
/* 102 */     this.m_helper.addLabelFieldPair(mainPanel, this.m_systemInterface.localizeCaption("apDcMetaTableLabel"), this.m_dmsTable, "dDocMetaSet");
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 108 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   private String[][] getDMSTableList()
/*     */   {
/* 113 */     DataBinder binder = new DataBinder();
/* 114 */     DataBinder binder2 = new DataBinder();
/* 115 */     String docClass = this.m_binder.getLocal("dDocClass");
/* 116 */     List shownDMSTables = new ArrayList();
/*     */     try
/*     */     {
/* 119 */       this.m_shContext.executeService("GET_DOCMETASETS", binder, true);
/* 120 */       if (docClass != null)
/*     */       {
/* 122 */         binder2.putLocal("dDocClass", docClass);
/* 123 */         this.m_shContext.executeService("GET_DOCCLASS_INFO", binder2, true);
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*     */     }
/*     */ 
/* 130 */     DataResultSet rset = (DataResultSet)binder.getResultSet("DocMetaSets");
/*     */ 
/* 132 */     if (!rset.isEmpty())
/*     */     {
/* 134 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/* 136 */         this.m_allDMSTables.add(rset.getStringValueByName("dDocMetaSet"));
/*     */       }
/*     */ 
/* 140 */       shownDMSTables.addAll(this.m_allDMSTables);
/* 141 */       List classDMSTables = new ArrayList();
/* 142 */       classDMSTables.add("DocMeta");
/* 143 */       if (docClass != null)
/*     */       {
/* 145 */         rset = (DataResultSet)binder2.getResultSet("DocMetaSets");
/* 146 */         if (rset != null)
/*     */         {
/* 148 */           for (rset.first(); rset.isRowPresent(); rset.next())
/*     */           {
/* 150 */             classDMSTables.add(rset.getStringValueByName("dDocMetaSet"));
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 155 */       shownDMSTables.removeAll(classDMSTables);
/*     */     }
/*     */ 
/* 158 */     if (shownDMSTables.isEmpty())
/*     */     {
/* 160 */       String[][] choiceList = new String[1][2];
/* 161 */       choiceList[0] = { "", "" };
/* 162 */       return choiceList;
/*     */     }
/*     */ 
/* 165 */     String[][] choiceList = new String[shownDMSTables.size()][2];
/* 166 */     for (int i = 0; i < shownDMSTables.size(); ++i)
/*     */     {
/* 168 */       String choice = (String)shownDMSTables.get(i);
/* 169 */       choiceList[i] = { choice, choice };
/*     */     }
/*     */ 
/* 172 */     return choiceList;
/*     */   }
/*     */ 
/*     */   public boolean handleDialogEvent(ActionEvent event)
/*     */   {
/* 178 */     String docMetaSet = this.m_binder.getLocal("dDocMetaSet");
/*     */ 
/* 180 */     if (!this.m_allDMSTables.contains(docMetaSet))
/*     */     {
/* 182 */       IdcMessage msg = IdcMessageFactory.lc("apAddDMSTablePrompt", new Object[] { docMetaSet });
/* 183 */       int result = MessageBox.doMessage(this.m_systemInterface, msg, 4);
/* 184 */       if (result == 2)
/*     */       {
/* 186 */         if (!docMetaSet.startsWith("DMS"))
/*     */         {
/* 188 */           this.m_errorMessage = IdcMessageFactory.lc("apInvalidDMSTableName", new Object[0]);
/* 189 */           return false;
/*     */         }
/*     */ 
/* 192 */         DataBinder binder = new DataBinder();
/* 193 */         binder.putLocal("dDocMetaSet", docMetaSet);
/*     */         try
/*     */         {
/* 196 */           this.m_shContext.executeService("CREATE_DMS_TABLE", binder, true);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 200 */           this.m_errorMessage = IdcMessageFactory.lc("apErrorCreatingDMSTable", new Object[] { docMetaSet });
/* 201 */           return false;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 206 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 210 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 215 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97937 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.AddDocMetaTableDlg
 * JD-Core Version:    0.5.4
 */