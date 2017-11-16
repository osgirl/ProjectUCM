/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JRadioButton;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class ViewSecurityPanel extends DocConfigPanel
/*     */   implements ItemListener
/*     */ {
/*     */   protected Vector m_securityComponents;
/*     */   protected JCheckBox m_publishViewDataBox;
/*     */   protected ButtonGroup m_filterGroup;
/*     */   protected JRadioButton m_noSecurityBox;
/*     */   protected JRadioButton m_standardSecurityBox;
/*     */   protected JRadioButton m_customSecurityBox;
/*     */   protected JCheckBox m_allowModificationBox;
/*     */   protected JTextField m_securityClassName;
/*     */   protected boolean m_isInListenerEvent;
/*     */ 
/*     */   public ViewSecurityPanel()
/*     */   {
/*  48 */     this.m_securityComponents = new IdcVector();
/*     */ 
/*  56 */     this.m_isInListenerEvent = false;
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder) throws ServiceException
/*     */   {
/*  61 */     super.initEx(sys, binder);
/*     */ 
/*  63 */     JPanel panel = initUI(binder);
/*  64 */     this.m_helper.makePanelGridBag(this, 1);
/*  65 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  66 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  67 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  68 */     this.m_helper.addComponent(this, panel);
/*  69 */     this.m_helper.addComponent(this, new PanePanel());
/*     */   }
/*     */ 
/*     */   protected JPanel initUI(DataBinder binder)
/*     */   {
/*  74 */     JPanel pnl = new PanePanel();
/*  75 */     this.m_helper.makePanelGridBag(pnl, 0);
/*     */ 
/*  77 */     initSecurityUI(pnl, binder);
/*  78 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void initSecurityUI(JPanel pnl, DataBinder binder)
/*     */   {
/*  83 */     SystemInterface si = this.m_systemInterface;
/*  84 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/*  85 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/*     */ 
/*  87 */     this.m_helper.addComponent(pnl, new CustomLabel(si.getString("apSchSecurityOptions"), 1));
/*     */ 
/*  91 */     Insets insets = this.m_helper.m_gridHelper.m_gc.insets;
/*  92 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(insets.top, insets.left + 15, insets.bottom, insets.right);
/*     */ 
/*  94 */     this.m_publishViewDataBox = new CustomCheckbox(si.getString("apSchPublishData"));
/*     */ 
/*  96 */     this.m_helper.addExchangeComponent(pnl, this.m_publishViewDataBox, "PublishViewData");
/*     */ 
/*  98 */     this.m_publishViewDataBox.addItemListener(this);
/*     */ 
/* 100 */     this.m_allowModificationBox = new CustomCheckbox(si.getString("apSchAllowContributorModification"));
/*     */ 
/* 102 */     this.m_helper.addExchangeComponent(pnl, this.m_allowModificationBox, "schAllowModification");
/* 103 */     this.m_allowModificationBox.addItemListener(this);
/*     */ 
/* 105 */     this.m_noSecurityBox = new JRadioButton(si.getString("apSchNoSecurityFilter"));
/* 106 */     this.m_helper.addComponent(pnl, this.m_noSecurityBox);
/* 107 */     this.m_noSecurityBox.addItemListener(this);
/* 108 */     this.m_securityComponents.addElement(this.m_noSecurityBox);
/*     */ 
/* 110 */     this.m_standardSecurityBox = new JRadioButton(si.getString("apSchStandardSecurityFilter"));
/* 111 */     this.m_helper.addComponent(pnl, this.m_standardSecurityBox);
/* 112 */     this.m_securityComponents.addElement(this.m_standardSecurityBox);
/* 113 */     this.m_standardSecurityBox.addItemListener(this);
/*     */ 
/* 115 */     this.m_customSecurityBox = new JRadioButton(si.localizeCaption("apSchCustomSecurityFilter"));
/* 116 */     this.m_helper.m_gridHelper.m_gc.gridwidth = 1;
/* 117 */     this.m_helper.addComponent(pnl, this.m_customSecurityBox);
/* 118 */     this.m_customSecurityBox.addItemListener(this);
/* 119 */     this.m_securityComponents.addElement(this.m_customSecurityBox);
/*     */ 
/* 121 */     this.m_helper.m_gridHelper.m_gc.anchor = 17;
/* 122 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 123 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 124 */     this.m_securityClassName = new JTextField();
/* 125 */     this.m_helper.addExchangeComponent(pnl, this.m_securityClassName, "schSecurityImplementor");
/*     */   }
/*     */ 
/*     */   public void loadComponents()
/*     */   {
/* 133 */     String publishValues = this.m_helper.m_props.getProperty("PublishViewData");
/* 134 */     if (publishValues == null)
/*     */     {
/* 136 */       this.m_helper.m_props.put("PublishViewData", "1");
/*     */     }
/*     */ 
/* 140 */     super.loadComponents();
/* 141 */     Properties props = this.m_helper.m_props;
/* 142 */     String securityImplementor = props.getProperty("schSecurityImplementor");
/* 143 */     if (securityImplementor != null)
/*     */     {
/* 145 */       if (securityImplementor.equals("intradoc.server.schema.StandardSchemaSecurityFilter"))
/*     */       {
/* 148 */         this.m_standardSecurityBox.setSelected(true);
/*     */       }
/*     */       else
/*     */       {
/* 152 */         this.m_customSecurityBox.setSelected(true);
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 157 */       this.m_noSecurityBox.setSelected(true);
/*     */     }
/*     */ 
/* 160 */     itemStateChanged(null);
/*     */   }
/*     */ 
/*     */   protected void enableDisable(Vector cmpList, boolean enable)
/*     */   {
/* 165 */     int num = cmpList.size();
/* 166 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 168 */       Component cmp = (Component)cmpList.elementAt(i);
/* 169 */       cmp.setEnabled(enable);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 178 */     if (this.m_isInListenerEvent)
/*     */     {
/* 180 */       return;
/*     */     }
/* 182 */     this.m_isInListenerEvent = true;
/*     */ 
/* 184 */     if (e != null)
/*     */     {
/* 186 */       Object o = e.getItem();
/* 187 */       if (o instanceof JRadioButton)
/*     */       {
/* 189 */         JRadioButton radioButton = (JRadioButton)o;
/* 190 */         if (e.getStateChange() == 2)
/*     */         {
/* 192 */           radioButton.setSelected(true);
/*     */         }
/*     */         else
/*     */         {
/* 196 */           if (o != this.m_noSecurityBox)
/*     */           {
/* 198 */             this.m_noSecurityBox.setSelected(false);
/*     */           }
/* 200 */           if (o != this.m_standardSecurityBox)
/*     */           {
/* 202 */             this.m_standardSecurityBox.setSelected(false);
/*     */           }
/* 204 */           if (o != this.m_customSecurityBox)
/*     */           {
/* 206 */             this.m_customSecurityBox.setSelected(false);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 212 */     boolean allowModification = this.m_allowModificationBox.isSelected();
/* 213 */     if ((this.m_publishViewDataBox.isSelected()) && (!allowModification))
/*     */     {
/* 215 */       this.m_noSecurityBox.setSelected(true);
/* 216 */       this.m_standardSecurityBox.setSelected(false);
/* 217 */       this.m_customSecurityBox.setSelected(false);
/* 218 */       this.m_securityClassName.setText("");
/* 219 */       this.m_securityClassName.setEnabled(false);
/* 220 */       enableDisable(this.m_securityComponents, false);
/*     */     }
/*     */     else
/*     */     {
/* 224 */       if ((!this.m_noSecurityBox.isSelected()) && (!this.m_standardSecurityBox.isSelected()) && (!this.m_customSecurityBox.isSelected()))
/*     */       {
/* 228 */         this.m_noSecurityBox.setSelected(true);
/* 229 */         this.m_standardSecurityBox.setSelected(false);
/* 230 */         this.m_customSecurityBox.setSelected(false);
/*     */       }
/*     */ 
/* 233 */       enableDisable(this.m_securityComponents, true);
/* 234 */       if (this.m_allowModificationBox.isSelected())
/*     */       {
/* 236 */         if (this.m_noSecurityBox.isSelected())
/*     */         {
/* 238 */           this.m_standardSecurityBox.setSelected(true);
/* 239 */           this.m_noSecurityBox.setSelected(false);
/* 240 */           this.m_customSecurityBox.setSelected(false);
/*     */         }
/* 242 */         this.m_noSecurityBox.setEnabled(false);
/*     */       }
/*     */     }
/*     */ 
/* 246 */     Properties props = this.m_helper.m_props;
/* 247 */     if (this.m_standardSecurityBox.isSelected())
/*     */     {
/* 249 */       this.m_securityClassName.setText("intradoc.server.schema.StandardSchemaSecurityFilter");
/*     */ 
/* 251 */       this.m_securityClassName.setEnabled(false);
/*     */     }
/* 253 */     else if (this.m_customSecurityBox.isSelected())
/*     */     {
/* 255 */       String securityImplementor = props.getProperty("schSecurityImplementor");
/*     */ 
/* 257 */       if (securityImplementor == null)
/*     */       {
/* 259 */         this.m_securityClassName.setText("intradoc.server.schema.UnknownSchemaSecurityFilter");
/*     */       }
/*     */ 
/* 262 */       this.m_securityClassName.setEnabled(true);
/*     */     }
/*     */     else
/*     */     {
/* 266 */       this.m_securityClassName.setText("");
/* 267 */       this.m_securityClassName.setEnabled(false);
/*     */     }
/*     */ 
/* 270 */     this.m_isInListenerEvent = false;
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 281 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80027 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.ViewSecurityPanel
 * JD-Core Version:    0.5.4
 */