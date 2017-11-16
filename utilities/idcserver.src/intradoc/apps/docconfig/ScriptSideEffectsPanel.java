/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ScriptSideEffectsPanel extends DocConfigPanel
/*     */   implements ActionListener
/*     */ {
/*     */   protected DataBinder m_scriptData;
/*     */ 
/*     */   public ScriptSideEffectsPanel()
/*     */   {
/*  47 */     this.m_scriptData = null;
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder) throws ServiceException
/*     */   {
/*  52 */     this.m_scriptData = binder;
/*     */ 
/*  54 */     super.initEx(sys, binder);
/*  55 */     initUI();
/*     */ 
/*  58 */     this.m_helper.m_props = this.m_scriptData.getLocalData();
/*  59 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  64 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*  65 */     gh.useGridBag(this);
/*     */ 
/*  67 */     JPanel pnl = new PanePanel();
/*  68 */     this.m_helper.makePanelGridBag(pnl, 2);
/*  69 */     gh.m_gc.anchor = 18;
/*  70 */     gh.m_gc.weightx = 0.0D;
/*  71 */     gh.m_gc.weighty = 0.0D;
/*  72 */     gh.m_gc.insets = new Insets(5, 5, 5, 5);
/*     */ 
/*  74 */     this.m_helper.addLastComponentInRow(pnl, new CustomText(this.m_systemInterface.getString("apDpSideEffectDesc")));
/*     */ 
/*  77 */     this.m_helper.addLabelFieldPair(pnl, this.m_systemInterface.localizeCaption("apDpSideEffectKeyLabel"), new CustomTextField(30), "key");
/*     */ 
/*  80 */     this.m_helper.addLabelFieldPair(pnl, this.m_systemInterface.localizeCaption("apDpSideEffectValueLabel"), new CustomTextField(30), "value");
/*     */ 
/*  84 */     gh.m_gc.fill = 0;
/*  85 */     gh.m_gc.anchor = 10;
/*  86 */     JButton btn = new JButton(this.m_systemInterface.getString("apDpAddKeyPairTitle"));
/*  87 */     btn.addActionListener(this);
/*  88 */     btn.setActionCommand("addKeyPair");
/*  89 */     this.m_helper.addLastComponentInRow(pnl, btn);
/*     */ 
/*  91 */     gh.m_gc.fill = 2;
/*  92 */     this.m_helper.addLastComponentInRow(this, pnl);
/*     */ 
/*  94 */     CustomTextArea ta = new CustomTextArea();
/*  95 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  96 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  97 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*  98 */     this.m_helper.addExchangeComponent(this, ta, "dprSideEffects");
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 103 */     String cmd = e.getActionCommand();
/* 104 */     if (!cmd.equals("addKeyPair"))
/*     */       return;
/* 106 */     DynamicComponentExchange exchange = this.m_helper.m_exchange;
/* 107 */     String key = exchange.getComponentValue("key").trim();
/* 108 */     String value = exchange.getComponentValue("value").trim();
/*     */ 
/* 110 */     if (key.length() == 0)
/*     */     {
/* 112 */       MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apDpMustDefineKey", new Object[0]), 1);
/*     */     }
/*     */     else
/*     */     {
/* 117 */       String se = exchange.getComponentValue("dprSideEffects");
/* 118 */       se = se.trim();
/* 119 */       se = se + "\n<$" + key + "=\"" + value + "\"$>";
/*     */ 
/* 122 */       exchange.setComponentValue("key", "");
/* 123 */       exchange.setComponentValue("value", "");
/* 124 */       exchange.setComponentValue("dprSideEffects", se);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 137 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.ScriptSideEffectsPanel
 * JD-Core Version:    0.5.4
 */