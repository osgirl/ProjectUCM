/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DisplayLabel;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class TransferPanel extends ArchiverPanel
/*     */   implements ActionListener
/*     */ {
/*     */   protected JButton m_removeBtn;
/*     */   protected Vector m_guiControls;
/*     */ 
/*     */   public TransferPanel()
/*     */   {
/*  54 */     this.m_removeBtn = null;
/*  55 */     this.m_guiControls = new IdcVector();
/*     */   }
/*     */ 
/*     */   public JPanel initUI()
/*     */   {
/*  60 */     JPanel datesPanel = initDatesUI();
/*  61 */     JPanel optionsPanel = initTransferOptionsUI();
/*  62 */     JPanel targetPanel = initTargetUI();
/*     */ 
/*  64 */     JPanel wrapper = new PanePanel();
/*  65 */     this.m_helper.makePanelGridBag(wrapper, 1);
/*  66 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  67 */     this.m_helper.addLastComponentInRow(wrapper, datesPanel);
/*  68 */     this.m_helper.addLastComponentInRow(wrapper, optionsPanel);
/*  69 */     this.m_helper.addLastComponentInRow(wrapper, targetPanel);
/*  70 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected JPanel initDatesUI()
/*     */   {
/*  75 */     JPanel pnl = new PanePanel();
/*  76 */     this.m_helper.makePanelGridBag(pnl, 1);
/*     */ 
/*  78 */     CustomLabel comp = new CustomLabel();
/*  79 */     comp.setMinWidth(110);
/*  80 */     this.m_helper.addLabelFieldPairEx(pnl, LocaleResources.getString("apLabelLastTransferOut", this.m_cxt), comp, "aLastTransferOut", false);
/*     */ 
/*  83 */     comp = new CustomLabel();
/*  84 */     comp.setMinWidth(40);
/*  85 */     this.m_helper.addLabelFieldPairEx(pnl, LocaleResources.getString("apLabelTotal", this.m_cxt), comp, "aTotalTransferedOut", true);
/*     */ 
/*  88 */     comp = new CustomLabel();
/*  89 */     comp.setMinWidth(110);
/*  90 */     this.m_helper.addLabelFieldPairEx(pnl, LocaleResources.getString("apLabelLastTransferIn", this.m_cxt), comp, "aLastTransferIn", false);
/*     */ 
/*  93 */     comp = new CustomLabel();
/*  94 */     comp.setMinWidth(40);
/*  95 */     this.m_helper.addLabelFieldPairEx(pnl, LocaleResources.getString("apLabelTotal", this.m_cxt), comp, "aTotalTransferedIn", true);
/*     */ 
/*  99 */     JPanel wrapper = new CustomPanel();
/* 100 */     wrapper.setLayout(new BorderLayout());
/* 101 */     wrapper.add("Center", pnl);
/*     */ 
/* 103 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected JPanel initTransferOptionsUI()
/*     */   {
/* 108 */     JPanel pnl = new PanePanel();
/* 109 */     this.m_helper.makePanelGridBag(pnl, 1);
/* 110 */     this.m_helper.addPanelTitle(pnl, this.m_systemInterface.getString("apLabelTransferOptions"));
/*     */ 
/* 112 */     DisplayLabel comp = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/* 113 */     this.m_helper.addLabelFieldPairEx(pnl, this.m_systemInterface.localizeCaption("apLabelIsTargetable"), comp, "aIsTargetable", false);
/*     */ 
/* 116 */     comp = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/* 117 */     this.m_helper.addLabelFieldPairEx(pnl, this.m_systemInterface.localizeCaption("apLabelIsTransferAutomated"), comp, "aIsAutomatedTransfer", true);
/*     */ 
/* 121 */     JPanel btnPanel = new PanePanel();
/* 122 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 124 */     JButton editBtn = new JButton(this.m_systemInterface.getString("apDlgButtonEdit"));
/* 125 */     editBtn.setActionCommand("editOptions");
/* 126 */     editBtn.addActionListener(this);
/* 127 */     editBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableEditTransferOptions", this.m_cxt));
/* 128 */     this.m_guiControls.addElement(editBtn);
/*     */ 
/* 130 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 131 */     this.m_helper.addComponent(btnPanel, editBtn);
/*     */ 
/* 134 */     JPanel wrapper = new CustomPanel();
/* 135 */     wrapper.setLayout(new BorderLayout());
/* 136 */     wrapper.add("Center", pnl);
/* 137 */     wrapper.add("East", btnPanel);
/*     */ 
/* 139 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected JPanel initTargetUI()
/*     */   {
/* 144 */     JPanel pnl = new PanePanel();
/* 145 */     this.m_helper.makePanelGridBag(pnl, 1);
/* 146 */     this.m_helper.addPanelTitle(pnl, LocaleResources.getString("apLabelTransferDestination", this.m_cxt));
/*     */ 
/* 149 */     this.m_helper.addLabelDisplayPair(pnl, LocaleResources.getString("apLabelTransferOwner", this.m_cxt), 30, "aTransferOwner");
/*     */ 
/* 151 */     this.m_helper.addLabelDisplayPair(pnl, LocaleResources.getString("apLabelTargetArchive", this.m_cxt), 30, "aTargetArchive");
/*     */ 
/* 155 */     JPanel btnPanel = new PanePanel();
/* 156 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 159 */     this.m_removeBtn = new JButton(LocaleResources.getString("apLabelRemove", this.m_cxt));
/* 160 */     this.m_removeBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableRemoveTransferDest", this.m_cxt));
/* 161 */     this.m_removeBtn.setActionCommand("removeTarget");
/* 162 */     this.m_removeBtn.addActionListener(this);
/*     */ 
/* 164 */     JButton editBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 165 */     editBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apReadableEditTransferDest", this.m_cxt));
/* 166 */     editBtn.setActionCommand("editTarget");
/* 167 */     editBtn.addActionListener(this);
/* 168 */     this.m_guiControls.addElement(editBtn);
/*     */ 
/* 170 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 171 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 172 */     this.m_helper.addComponent(btnPanel, this.m_removeBtn);
/* 173 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 5, 10, 5);
/* 174 */     this.m_helper.addComponent(btnPanel, editBtn);
/*     */ 
/* 177 */     JPanel wrapper = new CustomPanel();
/* 178 */     wrapper.setLayout(new BorderLayout());
/* 179 */     wrapper.add("Center", pnl);
/* 180 */     wrapper.add("East", btnPanel);
/*     */ 
/* 182 */     return wrapper;
/*     */   }
/*     */ 
/*     */   public void enableDisable(boolean isEnabled)
/*     */   {
/* 188 */     int size = this.m_guiControls.size();
/* 189 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 191 */       Component cmp = (Component)this.m_guiControls.elementAt(i);
/* 192 */       cmp.setEnabled(isEnabled);
/*     */     }
/*     */ 
/* 195 */     if (isEnabled)
/*     */     {
/* 197 */       String owner = this.m_helper.m_props.getProperty("aTransferOwner");
/* 198 */       if ((owner == null) || (owner.length() == 0))
/*     */       {
/* 200 */         isEnabled = false;
/*     */       }
/*     */     }
/* 203 */     this.m_removeBtn.setEnabled(isEnabled);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 209 */     String cmd = e.getActionCommand();
/* 210 */     if (cmd.equals("editOptions"))
/*     */     {
/* 212 */       EditTransferOptionsDlg dlg = new EditTransferOptionsDlg(this.m_systemInterface, LocaleResources.getString("apLabelTransferOptions", this.m_cxt), this.m_collectionContext);
/*     */ 
/* 214 */       dlg.init(this.m_helper.m_props);
/*     */     }
/* 216 */     else if (cmd.equals("editTarget"))
/*     */     {
/* 218 */       CollectionArchivesDlg dlg = new CollectionArchivesDlg(this.m_systemInterface, LocaleResources.getString("apLabelArchiveCollections", this.m_cxt), this.m_collectionContext, "EditTransferTarget");
/*     */ 
/* 221 */       dlg.init(this.m_helper.m_props);
/*     */     } else {
/* 223 */       if (!cmd.equals("removeTarget"))
/*     */         return;
/* 225 */       boolean isAutomated = StringUtils.convertToBool(this.m_helper.m_props.getProperty("aIsAutomatedTransfer"), false);
/*     */ 
/* 227 */       if (isAutomated)
/*     */       {
/* 229 */         this.m_collectionContext.reportError(LocaleResources.getString("apStopAutomationBeforeRemoving", this.m_cxt));
/*     */ 
/* 231 */         return;
/*     */       }
/*     */ 
/* 234 */       int result = MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifyRemoveTargetLink", new Object[0]), 4);
/*     */ 
/* 236 */       if (result != 2)
/*     */         return;
/* 238 */       Properties props = new Properties();
/* 239 */       props.put("aTargetArchive", "");
/* 240 */       props.put("aTransferOwner", "");
/*     */       try
/*     */       {
/* 244 */         props.put("EditItems", "aTargetArchive,aTransferOwner");
/*     */ 
/* 246 */         SharedContext shContext = this.m_collectionContext.getSharedContext();
/* 247 */         AppContextUtils.executeService(shContext, "EDIT_ARCHIVEDATA", props, true);
/*     */       }
/*     */       catch (Exception exp)
/*     */       {
/* 251 */         this.m_collectionContext.reportError(exp, "");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 264 */     if ((name.equals("aLastTransferOut")) || (name.equals("aLastTransferIn")))
/*     */     {
/* 267 */       if (!updateComponent)
/*     */         return;
/* 269 */       String value = this.m_helper.m_props.getProperty(name);
/*     */       try
/*     */       {
/* 272 */         exchange.m_compValue = LocaleResources.localizeDate(value, this.m_cxt);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 276 */         exchange.m_compValue = value;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 282 */       super.exchangeField(name, exchange, updateComponent);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 288 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.TransferPanel
 * JD-Core Version:    0.5.4
 */