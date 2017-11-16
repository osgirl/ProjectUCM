/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextArea;
/*     */ 
/*     */ public class EditRestrictedListDlg
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_action;
/*     */   protected DataResultSet m_resultSet;
/*     */   protected String m_helpPage;
/*  55 */   protected ExecutionContext m_context = null;
/*  56 */   protected FieldInfo m_fieldInfo = null;
/*     */ 
/*  59 */   protected JTextArea m_listArea = null;
/*     */ 
/*     */   public EditRestrictedListDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  63 */     this.m_helper = new DialogHelper(sys, title, true);
/*  64 */     this.m_context = sys.getExecutionContext();
/*  65 */     this.m_systemInterface = sys;
/*  66 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(String fieldName, DataBinder ruleData)
/*     */   {
/*  71 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  76 */         Properties props = EditRestrictedListDlg.this.m_helper.m_props;
/*  77 */         String str = EditRestrictedListDlg.this.m_listArea.getText();
/*  78 */         Vector v = StringUtils.parseArray(str, '\n', '=');
/*     */ 
/*  81 */         boolean hasRegEx = StringUtils.convertToBool((String)props.remove("isAllowJavaRegEx"), false);
/*     */ 
/*  84 */         String type = "strict";
/*  85 */         if (hasRegEx)
/*     */         {
/*  87 */           type = "filter";
/*     */         }
/*  89 */         props.put("dprFieldListType", type);
/*     */ 
/*  91 */         EditRestrictedListDlg.this.m_resultSet.removeAll();
/*  92 */         int size = v.size();
/*  93 */         for (int i = 0; i < size; ++i)
/*     */         {
/*  95 */           String val = (String)v.elementAt(i);
/*     */ 
/*  98 */           if (val.endsWith("\r"))
/*     */           {
/* 100 */             val = val.substring(0, val.length() - 1);
/*     */           }
/* 102 */           Vector row = EditRestrictedListDlg.this.m_resultSet.createEmptyRow();
/* 103 */           row.setElementAt(val, 0);
/* 104 */           EditRestrictedListDlg.this.m_resultSet.addRow(row);
/*     */         }
/*     */ 
/* 107 */         return true;
/*     */       }
/*     */     };
/* 110 */     okCallback.m_dlgHelper = this.m_helper;
/* 111 */     this.m_helper.m_props = ruleData.getLocalData();
/*     */ 
/* 113 */     initUI(okCallback);
/* 114 */     initList(ruleData);
/* 115 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI(DialogCallback okCallback)
/*     */   {
/* 120 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 123 */     this.m_helper.addLastComponentInRow(mainPanel, new CustomLabel(this.m_systemInterface.getString("apDpRestrictedList"), 1));
/*     */ 
/* 126 */     String[][] boxInfo = { { "isAllowJavaRegEx", "apDpIsAllowRegEx" } };
/*     */ 
/* 132 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 133 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 134 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 135 */     this.m_helper.addComponent(mainPanel, this.m_listArea = new CustomTextArea(10, 40));
/*     */ 
/* 137 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 138 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 139 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 140 */     for (int i = 0; i < boxInfo.length; ++i)
/*     */     {
/* 142 */       JCheckBox box = new CustomCheckbox(this.m_systemInterface.getString(boxInfo[i][1]));
/* 143 */       this.m_helper.addExchangeComponent(mainPanel, box, boxInfo[i][0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initList(DataBinder binder)
/*     */   {
/* 149 */     StringBuffer buff = new StringBuffer();
/*     */     try
/*     */     {
/* 152 */       this.m_resultSet = ((DataResultSet)binder.getResultSet("RestrictedList"));
/* 153 */       int index = ResultSetUtils.getIndexMustExist(this.m_resultSet, "dpRuleListValue");
/* 154 */       for (this.m_resultSet.first(); this.m_resultSet.isRowPresent(); this.m_resultSet.next())
/*     */       {
/* 156 */         if (buff.length() > 0)
/*     */         {
/* 158 */           buff.append("\n");
/*     */         }
/* 160 */         buff.append(this.m_resultSet.getStringValue(index));
/*     */       }
/* 162 */       this.m_listArea.setText(buff.toString());
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 166 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */ 
/* 170 */     String type = this.m_helper.m_props.getProperty("dprFieldListType");
/* 171 */     boolean isAllowRegEx = (type != null) && (type.equals("filter"));
/*     */ 
/* 173 */     this.m_helper.m_props.put("isAllowJavaRegEx", "" + isAllowRegEx);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 178 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditRestrictedListDlg
 * JD-Core Version:    0.5.4
 */