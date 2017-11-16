/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.ExportQueryData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class RegisterExporterDlg extends EditDlg
/*     */   implements ActionListener
/*     */ {
/*  58 */   protected String m_idcName = null;
/*  59 */   public Vector m_exporters = null;
/*  60 */   protected JCheckBox m_isAutoBox = null;
/*  61 */   protected FixedSizeList m_exporterList = null;
/*     */ 
/*  63 */   protected JButton m_addBtn = null;
/*  64 */   protected JButton m_removeBtn = null;
/*     */ 
/*  66 */   protected String m_curExporter = null;
/*     */ 
/*     */   public RegisterExporterDlg(SystemInterface sys, String title, CollectionContext context)
/*     */   {
/*  70 */     super(sys, title, context, "RegisterExporters");
/*  71 */     this.m_editItems = "aIsAutomatedExport,aRegisteredExporters";
/*  72 */     this.m_action = "EDIT_EXPORTERS";
/*     */   }
/*     */ 
/*     */   public int init(Properties props)
/*     */   {
/*  78 */     this.m_idcName = this.m_collectionContext.getLocalCollection();
/*  79 */     String str = props.getProperty("aRegisteredExporters");
/*  80 */     this.m_exporters = StringUtils.parseArray(str, ',', ',');
/*     */ 
/*  82 */     return super.init(props);
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel pnl)
/*     */   {
/*  88 */     JPanel expPanel = initExportUI();
/*  89 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  90 */     this.m_helper.addComponent(pnl, expPanel);
/*     */ 
/*  92 */     refreshExporterList(true);
/*     */   }
/*     */ 
/*     */   public JPanel initExportUI()
/*     */   {
/*  97 */     JPanel pnl = new PanePanel();
/*  98 */     this.m_helper.makePanelGridBag(pnl, 1);
/*  99 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/*     */ 
/* 101 */     gridBag.prepareAddLastRowElement(17);
/* 102 */     this.m_isAutoBox = new CustomCheckbox(LocaleResources.getString("apLabelEnableAutomatedExport", this.m_cxt));
/* 103 */     this.m_helper.addExchangeComponent(pnl, this.m_isAutoBox, "aIsAutomatedExport");
/* 104 */     ItemListener autoListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 108 */         RegisterExporterDlg.this.enableDisable();
/*     */       }
/*     */     };
/* 111 */     this.m_isAutoBox.addItemListener(autoListener);
/*     */ 
/* 113 */     JPanel listPanel = new PanePanel();
/* 114 */     listPanel.setLayout(new BorderLayout());
/* 115 */     CustomLabel titleLabel = new CustomLabel(LocaleResources.getString("apLabelRegisteredExporters", this.m_cxt), 1);
/*     */ 
/* 117 */     listPanel.add("North", titleLabel);
/* 118 */     this.m_exporterList = new FixedSizeList(5, 200);
/* 119 */     listPanel.add("Center", this.m_exporterList);
/* 120 */     ItemListener ll = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 124 */         int index = RegisterExporterDlg.this.m_exporterList.getSelectedIndex();
/* 125 */         if (index < 0)
/*     */         {
/* 127 */           return;
/*     */         }
/*     */ 
/* 130 */         RegisterExporterDlg.this.m_curExporter = RegisterExporterDlg.this.m_exporterList.getSelectedItem();
/* 131 */         RegisterExporterDlg.this.m_removeBtn.setEnabled(true);
/*     */       }
/*     */     };
/* 134 */     this.m_exporterList.addItemListener(ll);
/*     */ 
/* 137 */     JPanel btnPanel = new PanePanel();
/* 138 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 140 */     this.m_addBtn = new JButton(LocaleResources.getString("apLabelRegister", this.m_cxt));
/* 141 */     this.m_addBtn.addActionListener(this);
/* 142 */     this.m_removeBtn = new JButton(LocaleResources.getString("apLabelRemove", this.m_cxt));
/* 143 */     this.m_removeBtn.addActionListener(this);
/*     */ 
/* 145 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 146 */     this.m_helper.addLastComponentInRow(btnPanel, this.m_addBtn);
/* 147 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 5, 10, 5);
/* 148 */     this.m_helper.addLastComponentInRow(btnPanel, this.m_removeBtn);
/*     */ 
/* 151 */     JPanel wrapper = new CustomPanel();
/* 152 */     this.m_helper.makePanelGridBag(wrapper, 1);
/*     */ 
/* 154 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 155 */     gh.m_gc.weightx = 1.0D;
/* 156 */     gh.m_gc.weighty = 1.0D;
/*     */ 
/* 158 */     this.m_helper.addLastComponentInRow(wrapper, pnl);
/* 159 */     gh.prepareAddRowElement(13);
/* 160 */     this.m_helper.addComponent(wrapper, listPanel);
/*     */ 
/* 162 */     gh.m_gc.weightx = 0.0D;
/* 163 */     gh.m_gc.weighty = 0.0D;
/* 164 */     gh.prepareAddLastRowElement(17);
/* 165 */     this.m_helper.addComponent(wrapper, btnPanel);
/*     */ 
/* 167 */     return wrapper;
/*     */   }
/*     */ 
/*     */   public boolean prepareOkEvent()
/*     */   {
/* 175 */     boolean result = true;
/* 176 */     Properties props = this.m_helper.m_props;
/* 177 */     boolean isAutomated = StringUtils.convertToBool(props.getProperty("aIsAutomatedExport"), false);
/*     */ 
/* 179 */     if (isAutomated)
/*     */     {
/* 182 */       String exportQueryStr = props.getProperty("aExportQuery");
/* 183 */       if ((exportQueryStr != null) && (exportQueryStr.length() > 0))
/*     */       {
/* 185 */         ExportQueryData queryData = new ExportQueryData();
/* 186 */         queryData.parse(exportQueryStr);
/*     */ 
/* 188 */         boolean isAllRevisions = StringUtils.convertToBool(queryData.getQueryProp("AllRevisions"), false);
/*     */ 
/* 190 */         boolean isMostRecentMatching = StringUtils.convertToBool(queryData.getQueryProp("MostRecentMatching"), false);
/*     */ 
/* 192 */         if ((!isAllRevisions) && (!isMostRecentMatching))
/*     */         {
/* 194 */           IdcMessage msg = IdcMessageFactory.lc("apUnableToSetAutomation", new Object[] { "apLabelAllSelectedRevs", "apLabelMostRecentMatchingRev" });
/*     */ 
/* 196 */           MessageBox.doMessage(this.m_systemInterface, msg, 1);
/* 197 */           result = false;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 202 */       String importer = props.getProperty("aRegisteredImporter");
/* 203 */       if ((importer != null) && (importer.length() > 0))
/*     */       {
/* 205 */         String regExportersStr = props.getProperty("aRegisteredExporters");
/* 206 */         Vector regExporters = StringUtils.parseArray(regExportersStr, ',', ',');
/* 207 */         int size = regExporters.size();
/* 208 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 210 */           String exporter = (String)regExporters.elementAt(i);
/* 211 */           if (!exporter.equals(importer))
/*     */             continue;
/* 213 */           IdcMessage msg = IdcMessageFactory.lc("apExporterMatchesImporter", new Object[] { importer });
/* 214 */           int r = MessageBox.doMessage(this.m_systemInterface, msg, 2);
/* 215 */           if (r != 0)
/*     */             break;
/* 217 */           result = false; break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 225 */     return result;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 233 */     Object target = e.getSource();
/* 234 */     if (target == this.m_addBtn)
/*     */     {
/* 236 */       if (!checkCanAdd())
/*     */       {
/* 238 */         return;
/*     */       }
/* 240 */       this.m_exporters.addElement(this.m_idcName);
/*     */     }
/* 242 */     else if ((target == this.m_removeBtn) && (this.m_curExporter != null))
/*     */     {
/* 245 */       this.m_exporters.removeElement(this.m_curExporter);
/* 246 */       this.m_curExporter = null;
/*     */     }
/* 248 */     refreshExporterList(false);
/*     */   }
/*     */ 
/*     */   protected boolean checkCanAdd()
/*     */   {
/* 254 */     int num = this.m_exporters.size();
/* 255 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 257 */       String elt = (String)this.m_exporters.elementAt(i);
/* 258 */       if (elt.equals(this.m_idcName))
/*     */       {
/* 260 */         return false;
/*     */       }
/*     */     }
/* 263 */     return true;
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 268 */     boolean st = this.m_isAutoBox.isSelected();
/* 269 */     this.m_exporterList.setEnabled(st);
/*     */ 
/* 271 */     boolean hasAdd = checkCanAdd();
/* 272 */     this.m_addBtn.setEnabled((hasAdd) && (st));
/*     */ 
/* 274 */     boolean remEnable = this.m_exporterList.getSelectedIndex() >= 0;
/* 275 */     this.m_removeBtn.setEnabled((remEnable) && (st));
/*     */   }
/*     */ 
/*     */   protected void refreshExporterList(boolean isUpdate)
/*     */   {
/* 280 */     this.m_exporterList.removeAllItems();
/*     */ 
/* 282 */     int num = this.m_exporters.size();
/* 283 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 285 */       this.m_exporterList.add((String)this.m_exporters.elementAt(i));
/*     */     }
/*     */ 
/* 288 */     this.m_helper.m_props.put("aRegisteredExporters", StringUtils.createString(this.m_exporters, ',', ','));
/*     */ 
/* 291 */     if (isUpdate)
/*     */     {
/* 293 */       boolean state = StringUtils.convertToBool(this.m_helper.m_props.getProperty("aIsAutomatedExport"), false);
/*     */ 
/* 295 */       this.m_isAutoBox.setSelected(state);
/*     */     }
/* 297 */     enableDisable();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 302 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.RegisterExporterDlg
 * JD-Core Version:    0.5.4
 */