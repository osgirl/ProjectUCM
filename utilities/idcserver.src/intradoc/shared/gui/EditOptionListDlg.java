/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextArea;
/*     */ 
/*     */ public class EditOptionListDlg
/*     */   implements ActionListener
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected String m_action;
/*     */   protected String m_optionListKey;
/*     */   protected String m_helpPage;
/*  62 */   protected boolean m_isText = true;
/*     */   protected JCheckBox m_sortAscendingCheckbox;
/*     */   protected JCheckBox m_sortDescendingCheckbox;
/*     */   protected JCheckBox m_ignoreCaseCheckbox;
/*     */   protected JScrollPane m_scrollPane;
/*     */   protected JTextArea m_optionList;
/*     */ 
/*     */   public EditOptionListDlg(SystemInterface sys, String title, String helpPage, String action)
/*     */   {
/*  74 */     this.m_helper = new DialogHelper(sys, title, true);
/*  75 */     this.m_systemInterface = sys;
/*  76 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*  77 */     this.m_action = action;
/*  78 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public void init(Properties data)
/*     */   {
/*  84 */     this.m_helper.m_props = data;
/*     */ 
/*  97 */     String type = this.m_helper.m_props.getProperty("dType");
/*  98 */     if (type == null)
/*     */     {
/* 100 */       type = this.m_helper.m_props.getProperty("schFieldType");
/*     */     }
/* 102 */     type = type.toLowerCase();
/* 103 */     if (type.indexOf("int") >= 0)
/*     */     {
/* 105 */       this.m_isText = false;
/*     */     }
/*     */     else
/*     */     {
/* 109 */       this.m_isText = true;
/*     */     }
/*     */ 
/* 112 */     this.m_optionListKey = data.getProperty("dOptionListKey");
/*     */ 
/* 115 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 122 */           Properties localData = new Properties(EditOptionListDlg.this.m_helper.m_props);
/* 123 */           localData.put("dKey", EditOptionListDlg.this.m_optionListKey);
/*     */ 
/* 125 */           String textString = EditOptionListDlg.this.m_optionList.getText();
/* 126 */           Vector v = new IdcVector();
/*     */ 
/* 129 */           if ((textString != null) && (textString.length() > 0))
/*     */           {
/* 134 */             String curStr = textString;
/* 135 */             String subStr = null;
/*     */             while (true)
/*     */             {
/* 138 */               int index = curStr.indexOf('\n');
/* 139 */               if (index >= 0)
/*     */               {
/* 141 */                 subStr = curStr.substring(0, index);
/*     */               }
/*     */               else
/*     */               {
/* 145 */                 subStr = curStr;
/*     */               }
/*     */ 
/* 148 */               v.addElement(subStr);
/*     */ 
/* 150 */               if (index < 0) {
/*     */                 break;
/*     */               }
/*     */ 
/* 154 */               curStr = curStr.substring(index + 1);
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 159 */           textString = StringUtils.createString(v, '\n', '\n');
/*     */ 
/* 161 */           localData.put("OptionListString", textString);
/*     */ 
/* 163 */           DataBinder binder = new DataBinder();
/* 164 */           binder.setLocalData(localData);
/*     */ 
/* 166 */           AppLauncher.executeService(EditOptionListDlg.this.m_action, binder);
/*     */ 
/* 168 */           v = binder.getOptionList(EditOptionListDlg.this.m_optionListKey);
/*     */ 
/* 172 */           SharedObjects.putOptList(EditOptionListDlg.this.m_optionListKey, v);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 176 */           EditOptionListDlg.this.reportError(exp, "!apErrorUpdatingOptionList");
/* 177 */           return false;
/*     */         }
/*     */ 
/* 180 */         return true;
/*     */       }
/*     */     };
/* 184 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 188 */     String name = data.getProperty("optionListDisplayName");
/* 189 */     if (name == null)
/*     */     {
/* 191 */       name = this.m_optionListKey;
/*     */     }
/* 193 */     String header = LocaleResources.getString("apOptionListFor", this.m_cxt, name);
/* 194 */     this.m_helper.addPanelTitle(mainPanel, header);
/*     */ 
/* 197 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 10, 10, 10);
/* 198 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 199 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 200 */     this.m_optionList = new CustomTextArea(10, 40);
/* 201 */     this.m_scrollPane = new JScrollPane(this.m_optionList);
/* 202 */     this.m_helper.addComponent(mainPanel, this.m_scrollPane);
/*     */ 
/* 205 */     Vector v = SharedObjects.getOptList(this.m_optionListKey);
/* 206 */     if (v != null)
/*     */     {
/* 208 */       String textString = null;
/* 209 */       int num = v.size();
/* 210 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 212 */         String val = (String)v.elementAt(i);
/* 213 */         if (textString == null)
/*     */         {
/* 215 */           textString = val;
/*     */         }
/*     */         else
/*     */         {
/* 219 */           textString = textString + "\n" + val;
/*     */         }
/*     */       }
/* 222 */       this.m_optionList.setText(textString);
/*     */     }
/*     */ 
/* 226 */     JPanel curPanel = new CustomPanel();
/* 227 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 228 */     this.m_helper.addComponent(mainPanel, curPanel);
/* 229 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 230 */     this.m_sortAscendingCheckbox = new CustomCheckbox(LocaleResources.getString("apAscending", this.m_cxt));
/*     */ 
/* 232 */     this.m_helper.addComponent(curPanel, this.m_sortAscendingCheckbox);
/* 233 */     this.m_sortDescendingCheckbox = new CustomCheckbox(LocaleResources.getString("apDescending", this.m_cxt));
/*     */ 
/* 235 */     this.m_helper.addComponent(curPanel, this.m_sortDescendingCheckbox);
/* 236 */     ButtonGroup sortGroup = new ButtonGroup();
/* 237 */     sortGroup.add(this.m_sortAscendingCheckbox);
/* 238 */     sortGroup.add(this.m_sortDescendingCheckbox);
/*     */ 
/* 240 */     if (this.m_isText)
/*     */     {
/* 242 */       this.m_ignoreCaseCheckbox = new CustomCheckbox(LocaleResources.getString("apIgnoreCase", this.m_cxt));
/*     */ 
/* 244 */       this.m_helper.addComponent(curPanel, this.m_ignoreCaseCheckbox);
/* 245 */       this.m_ignoreCaseCheckbox.setSelected(true);
/*     */     }
/*     */ 
/* 248 */     this.m_helper.addCommandButton(curPanel, LocaleResources.getString("apSortNow", this.m_cxt), "", this);
/*     */ 
/* 250 */     this.m_sortAscendingCheckbox.setSelected(true);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 255 */     boolean ascending = this.m_sortAscendingCheckbox.isSelected();
/*     */ 
/* 257 */     boolean isIgnoreCase = true;
/* 258 */     if (this.m_ignoreCaseCheckbox != null)
/*     */     {
/* 260 */       isIgnoreCase = this.m_ignoreCaseCheckbox.isSelected();
/*     */     }
/* 262 */     boolean ignoreCase = isIgnoreCase;
/*     */ 
/* 264 */     IdcComparator cmp = new Object(ignoreCase, ascending)
/*     */     {
/*     */       public int compare(Object lop, Object rop)
/*     */       {
/* 268 */         String s1 = (String)lop;
/* 269 */         String s2 = (String)rop;
/*     */ 
/* 271 */         if (this.val$ignoreCase)
/*     */         {
/* 273 */           s1 = s1.toLowerCase();
/* 274 */           s2 = s2.toLowerCase();
/*     */         }
/*     */ 
/* 278 */         if ((s1.length() == 0) && (s2.length() == 0))
/*     */         {
/* 280 */           return 0;
/*     */         }
/* 282 */         if (s1.length() == 0)
/*     */         {
/* 284 */           return -1;
/*     */         }
/* 286 */         if (s2.length() == 0)
/*     */         {
/* 288 */           return 1;
/*     */         }
/*     */ 
/* 293 */         boolean isCompareAsText = EditOptionListDlg.this.m_isText;
/* 294 */         int i1 = 0;
/* 295 */         int i2 = 0;
/* 296 */         if (!EditOptionListDlg.this.m_isText)
/*     */         {
/*     */           try
/*     */           {
/* 300 */             i1 = Integer.parseInt(s1);
/* 301 */             i2 = Integer.parseInt(s2);
/*     */           }
/*     */           catch (Exception exp)
/*     */           {
/* 305 */             isCompareAsText = true;
/*     */           }
/*     */         }
/*     */         int retVal;
/*     */         int retVal;
/* 311 */         if (isCompareAsText)
/*     */         {
/*     */           int retVal;
/* 313 */           if (this.val$ascending)
/*     */           {
/* 315 */             retVal = s1.compareTo(s2);
/*     */           }
/*     */           else
/*     */           {
/* 319 */             retVal = s2.compareTo(s1);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/*     */           int retVal;
/* 324 */           if (i1 == i2)
/*     */           {
/* 326 */             retVal = 0;
/*     */           }
/*     */           else
/*     */           {
/*     */             int retVal;
/* 328 */             if (this.val$ascending)
/*     */             {
/* 330 */               retVal = (i1 > i2) ? 1 : -1;
/*     */             }
/*     */             else
/*     */             {
/* 334 */               retVal = (i2 > i1) ? 1 : -1;
/*     */             }
/*     */           }
/*     */         }
/* 337 */         return retVal;
/*     */       }
/*     */     };
/* 342 */     String list = this.m_optionList.getText();
/* 343 */     Vector v = new IdcVector();
/* 344 */     if (!this.m_optionList.isEditable())
/*     */     {
/* 346 */       return;
/*     */     }
/*     */ 
/* 350 */     int begin = 0;
/* 351 */     int end = 0;
/* 352 */     for (int i = 0; end < list.length(); ++i)
/*     */     {
/* 354 */       end = list.indexOf("\n", begin);
/* 355 */       if (end == -1)
/*     */       {
/* 357 */         end = list.length();
/*     */       }
/* 359 */       String s = list.substring(begin, end);
/* 360 */       v.setSize(i + 1);
/* 361 */       v.setElementAt(s, i);
/*     */ 
/* 352 */       begin = end + 1;
/*     */     }
/*     */ 
/* 364 */     Sort.sortVector(v, cmp);
/*     */ 
/* 366 */     list = "";
/* 367 */     for (int i = 0; i < v.size(); ++i)
/*     */     {
/* 369 */       if (i > 0)
/*     */       {
/* 371 */         list = list + "\n";
/*     */       }
/* 373 */       list = list + v.elementAt(i);
/*     */     }
/* 375 */     this.m_optionList.setText(list);
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e, String msg)
/*     */   {
/* 380 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 385 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 390 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.EditOptionListDlg
 * JD-Core Version:    0.5.4
 */