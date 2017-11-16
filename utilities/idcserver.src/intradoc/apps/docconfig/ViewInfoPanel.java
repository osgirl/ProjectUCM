/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ViewInfoPanel extends DocConfigPanel
/*     */   implements ActionListener
/*     */ {
/*     */   protected Hashtable m_componentMap;
/*     */ 
/*     */   public ViewInfoPanel()
/*     */   {
/*  52 */     this.m_componentMap = null;
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder viewData) throws ServiceException
/*     */   {
/*  57 */     super.initEx(sys, viewData);
/*     */ 
/*  59 */     this.m_componentMap = new Hashtable();
/*     */ 
/*  61 */     JPanel panel = initUI(viewData);
/*  62 */     this.m_helper.makePanelGridBag(this, 1);
/*  63 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  64 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  65 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  66 */     this.m_helper.addComponent(this, panel);
/*  67 */     this.m_helper.addComponent(this, new PanePanel());
/*     */   }
/*     */ 
/*     */   protected JPanel initUI(DataBinder binder)
/*     */   {
/*  72 */     JPanel pnl = new PanePanel();
/*  73 */     this.m_helper.makePanelGridBag(pnl, 2);
/*     */ 
/*  75 */     String viewType = this.m_helper.m_props.getProperty("schViewType");
/*     */ 
/*  77 */     boolean isNew = StringUtils.convertToBool(this.m_helper.m_props.getProperty("IsNew"), false);
/*  78 */     if (isNew)
/*     */     {
/*  80 */       this.m_helper.addLabelEditPair(pnl, LocaleResources.getString("apSchViewNameLabel", this.m_ctx), 50, "schViewName");
/*     */     }
/*     */     else
/*     */     {
/*  86 */       this.m_helper.addLabelDisplayPair(pnl, LocaleResources.getString("apSchViewNameLabel", this.m_ctx), 50, "schViewName");
/*     */     }
/*     */ 
/*  90 */     this.m_helper.addLabelEditPair(pnl, LocaleResources.getString("apSchViewDescriptionLabel", this.m_ctx), 50, "schViewDescription");
/*     */ 
/*  94 */     if (viewType.equalsIgnoreCase("table"))
/*     */     {
/*  96 */       this.m_helper.addLabelDisplayPairEx(pnl, LocaleResources.getString("apSchTableNameLabel", this.m_ctx), 50, "schTableName", false);
/*     */ 
/*  99 */       this.m_helper.m_gridHelper.prepareAddLastRowElement(13);
/* 100 */       this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/*     */ 
/* 102 */       String label = LocaleResources.getString("apSchChangeColumns", this.m_ctx);
/* 103 */       JButton btn = new JButton(label);
/* 104 */       btn.setActionCommand("changeColumns");
/* 105 */       btn.addActionListener(this);
/* 106 */       this.m_helper.addComponent(pnl, btn);
/*     */ 
/* 108 */       DisplayChoice columnChoices = new DisplayChoice();
/* 109 */       this.m_docContext.updateColumnList(columnChoices, false, false);
/* 110 */       this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apSchInternalLabel", this.m_ctx), columnChoices, "schInternalColumn");
/*     */ 
/* 113 */       this.m_componentMap.put("schInternalColumn", columnChoices);
/*     */ 
/* 115 */       columnChoices = new DisplayChoice();
/* 116 */       this.m_docContext.updateColumnList(columnChoices, false, false);
/* 117 */       this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apSchViewLabelColumn", this.m_ctx), columnChoices, "schLabelColumn");
/*     */ 
/* 120 */       this.m_componentMap.put("schSchViewLabelColumn", columnChoices);
/*     */     }
/* 122 */     else if (viewType.equalsIgnoreCase("optionList"))
/*     */     {
/* 125 */       JComboBox optionListChoice = buildOptionListChoice(binder);
/* 126 */       this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apSchOptionListLabel", this.m_ctx), optionListChoice, "schOptionList");
/*     */     }
/*     */ 
/* 131 */     this.m_helper.addLabelEditPair(pnl, this.m_systemInterface.localizeMessage("!apSchViewDefaultDisplay"), 50, "schDefaultDisplayExpression");
/*     */ 
/* 135 */     return pnl;
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */   {
/* 141 */     for (Enumeration en = this.m_componentMap.elements(); en.hasMoreElements(); )
/*     */     {
/* 143 */       Object obj = en.nextElement();
/* 144 */       if (obj instanceof DisplayChoice)
/*     */       {
/* 146 */         DisplayChoice columnChoices = (DisplayChoice)obj;
/* 147 */         this.m_docContext.updateColumnList(columnChoices, false, false);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected JComboBox createViewList(DataBinder binder, JComboBox paramList)
/*     */   {
/* 154 */     JComboBox pList = paramList;
/* 155 */     Vector optList = binder.getOptionList("ViewList");
/*     */ 
/* 157 */     int size = optList.size();
/* 158 */     String[][] displayList = new String[size + 1][2];
/*     */ 
/* 161 */     String noParentStr = LocaleResources.getString("apSchViewNoParent", this.m_ctx);
/* 162 */     displayList[0][0] = "";
/* 163 */     displayList[0][1] = noParentStr;
/*     */ 
/* 165 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 167 */       String val = (String)optList.elementAt(i);
/* 168 */       displayList[(i + 1)][0] = val;
/* 169 */       displayList[(i + 1)][1] = val;
/*     */     }
/*     */ 
/* 172 */     DisplayChoice viewList = new DisplayChoice();
/* 173 */     viewList.init(displayList);
/*     */ 
/* 175 */     ItemListener listener = new ItemListener(viewList, pList)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 179 */         int state = e.getStateChange();
/* 180 */         if (state != 1)
/*     */           return;
/* 182 */         String str = this.val$viewList.getSelectedInternalValue();
/* 183 */         if (str.length() == 0)
/*     */         {
/* 185 */           this.val$pList.setEnabled(false);
/*     */         }
/*     */         else
/*     */         {
/* 189 */           this.val$pList.setEnabled(true);
/*     */         }
/*     */       }
/*     */     };
/* 194 */     viewList.addItemListener(listener);
/*     */ 
/* 196 */     return viewList;
/*     */   }
/*     */ 
/*     */   protected JComboBox buildOptionListChoice(DataBinder binder)
/*     */   {
/* 201 */     JComboBox choice = new CustomChoice();
/*     */ 
/* 203 */     DataResultSet drset = (DataResultSet)binder.getResultSet("OptionList");
/*     */     try
/*     */     {
/* 206 */       int index = ResultSetUtils.getIndexMustExist(drset, "dOptionListKey");
/* 207 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 209 */         String name = drset.getStringValue(index);
/* 210 */         choice.addItem(name);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 216 */       e.printStackTrace();
/*     */     }
/*     */ 
/* 219 */     return choice;
/*     */   }
/*     */ 
/*     */   public IdcMessage computeValidationErrorMessage(Map options)
/*     */   {
/* 225 */     String viewName = this.m_helper.m_props.getProperty("schViewName");
/* 226 */     if ((viewName == null) || (viewName.length() == 0))
/*     */     {
/* 228 */       IdcMessage msg = IdcMessageFactory.lc("apNullFieldInData", new Object[] { null, "schViewName" });
/* 229 */       return msg;
/*     */     }
/* 231 */     return null;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 239 */     String cmd = e.getActionCommand();
/* 240 */     if (!cmd.equals("changeColumns"))
/*     */       return;
/* 242 */     this.m_docContext.changeColumns();
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 254 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80220 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.ViewInfoPanel
 * JD-Core Version:    0.5.4
 */