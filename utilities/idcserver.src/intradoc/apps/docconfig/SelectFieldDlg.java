/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SelectFieldDlg
/*     */   implements ItemListener
/*     */ {
/*  55 */   protected SystemInterface m_systemInterface = null;
/*  56 */   protected ExecutionContext m_cxt = null;
/*  57 */   protected DialogHelper m_helper = null;
/*  58 */   protected String m_helpPage = null;
/*     */ 
/*  60 */   protected Hashtable m_fieldMap = null;
/*  61 */   protected DisplayChoice m_fieldChoice = null;
/*  62 */   protected DisplayChoice m_columnChoice = null;
/*     */ 
/*     */   public SelectFieldDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  66 */     this.m_systemInterface = sys;
/*  67 */     this.m_cxt = sys.getExecutionContext();
/*  68 */     this.m_helper = new DialogHelper(sys, title, true);
/*  69 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(Properties props)
/*     */   {
/*  74 */     this.m_helper.m_props = props;
/*  75 */     this.m_fieldMap = new Hashtable();
/*     */ 
/*  77 */     Vector docFieldsDef = getFieldList();
/*  78 */     if (docFieldsDef == null)
/*     */     {
/*  80 */       return 0;
/*     */     }
/*  82 */     initUI(docFieldsDef);
/*     */ 
/*  84 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI(Vector docFieldsDef)
/*     */   {
/*  89 */     JPanel mainPanel = this.m_helper.initStandard(null, null, 2, true, this.m_helpPage);
/*     */ 
/*  92 */     this.m_fieldMap = new Hashtable();
/*  93 */     Vector fields = new IdcVector();
/*  94 */     int size = docFieldsDef.size();
/*  95 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  97 */       FieldDef fieldDef = (FieldDef)docFieldsDef.elementAt(i);
/*  98 */       String[] map = new String[2];
/*  99 */       map[0] = fieldDef.m_name;
/* 100 */       map[1] = fieldDef.m_caption;
/* 101 */       fields.addElement(map);
/* 102 */       this.m_fieldMap.put(map[0], fieldDef);
/*     */     }
/*     */ 
/* 105 */     int num = fields.size();
/* 106 */     String[][] display = new String[num][2];
/* 107 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 109 */       display[i] = ((String[])(String[])fields.elementAt(i));
/*     */     }
/* 111 */     this.m_fieldChoice = new DisplayChoice();
/* 112 */     this.m_fieldChoice.init(display);
/* 113 */     this.m_fieldChoice.addItemListener(this);
/*     */ 
/* 115 */     this.m_helper.addLabelFieldPair(mainPanel, this.m_systemInterface.localizeCaption("apLabelFieldName"), this.m_fieldChoice, "ValueField");
/*     */ 
/* 118 */     CustomText cmp = new CustomText(this.m_systemInterface.getString("apDpFieldSelectDesc"));
/* 119 */     this.m_helper.addLastComponentInRow(mainPanel, cmp);
/*     */ 
/* 121 */     this.m_columnChoice = new DisplayChoice();
/* 122 */     this.m_helper.addLabelFieldPair(mainPanel, this.m_systemInterface.getString("apDpFieldColumnLabel"), this.m_columnChoice, "ValueFieldColumn");
/*     */ 
/* 125 */     checkSelection();
/*     */   }
/*     */ 
/*     */   protected Vector getFieldList()
/*     */   {
/*     */     try
/*     */     {
/* 133 */       ResultSet metaFields = SharedObjects.getTable("DocMetaDefinition");
/* 134 */       ViewFields fieldsObj = new ViewFields(this.m_cxt);
/* 135 */       fieldsObj.m_enabledOnly = false;
/* 136 */       fieldsObj.createAllDocumentFieldsList(metaFields, true, false, true, false);
/* 137 */       return fieldsObj.m_viewFields;
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 142 */       IdcMessage msg = IdcMessageFactory.lc("apUnableToLoadDocMetaDefinition", new Object[0]);
/* 143 */       Report.trace(null, this.m_systemInterface.localizeMessage(msg), e);
/* 144 */       MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */     }
/* 146 */     return null;
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 151 */     String selField = this.m_fieldChoice.getSelectedInternalValue();
/*     */ 
/* 153 */     if ((selField != null) && (selField.length() > 0))
/*     */     {
/* 155 */       ViewFieldDef fieldDef = (ViewFieldDef)this.m_fieldMap.get(selField);
/* 156 */       String viewName = fieldDef.getViewName();
/* 157 */       if (viewName != null)
/*     */       {
/* 159 */         SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*     */ 
/* 161 */         SchemaViewData viewData = (SchemaViewData)views.getData(viewName);
/* 162 */         String str = viewData.get("schViewColumns");
/* 163 */         Vector clmns = StringUtils.parseArray(str, ',', '^');
/*     */ 
/* 165 */         int len = clmns.size() + 1;
/* 166 */         String[][] map = new String[len][2];
/* 167 */         map[0][0] = "";
/* 168 */         map[0][1] = this.m_systemInterface.getString("apDpUseDefaultColumn");
/* 169 */         for (int i = 1; i < len; ++i)
/*     */         {
/*     */           String tmp158_155 = ((String)clmns.elementAt(i - 1)); map[i][1] = tmp158_155; map[i][0] = tmp158_155;
/*     */         }
/* 173 */         this.m_columnChoice.init(map);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 179 */       Vector opts = new IdcVector();
/* 180 */       this.m_columnChoice.init(opts);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 189 */     checkSelection();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 194 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79062 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.SelectFieldDlg
 * JD-Core Version:    0.5.4
 */