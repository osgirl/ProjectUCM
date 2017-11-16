/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomDialog;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public abstract class EditLinkBaseDlg extends CustomDialog
/*     */   implements ComponentBinder
/*     */ {
/*     */   public String m_title;
/*     */   public DialogHelper m_helper;
/*     */   public SystemInterface m_sysInterface;
/*     */   public PageManagerContext m_pageServices;
/*     */   public Vector m_linkInfo;
/*     */   public Vector m_oldLinkInfo;
/*     */   public boolean m_isNew;
/*     */   public static final short LINK_TYPE = 0;
/*     */   public static final short LINK_DATA = 1;
/*     */   public static final short LINK_TITLE = 2;
/*     */   public static final short LINK_DES = 3;
/*     */   protected String m_helpPage;
/*  81 */   protected ExecutionContext m_ctx = null;
/*     */ 
/*     */   public EditLinkBaseDlg(SystemInterface sysInterface, PageManagerContext pageServices, Vector linkInfo, boolean isNew, String helpPage)
/*     */   {
/*  86 */     super(sysInterface.getMainWindow(), true);
/*  87 */     this.m_ctx = sysInterface.getExecutionContext();
/*  88 */     this.m_title = null;
/*  89 */     this.m_sysInterface = sysInterface;
/*  90 */     this.m_pageServices = pageServices;
/*  91 */     this.m_linkInfo = linkInfo;
/*  92 */     this.m_oldLinkInfo = ((Vector)linkInfo.clone());
/*  93 */     this.m_isNew = isNew;
/*  94 */     this.m_helpPage = helpPage;
/*  95 */     this.m_helper = new DialogHelper();
/*  96 */     this.m_helper.attachToDialog(this, this.m_sysInterface, null);
/*     */   }
/*     */ 
/*     */   public boolean prompt(DialogCallback okCallback)
/*     */   {
/* 101 */     setTitle(this.m_title);
/*     */ 
/* 103 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 105 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/* 108 */     JPanel top = new CustomPanel();
/* 109 */     gh.prepareAddLastRowElement();
/* 110 */     gh.m_gc.weighty = 1.0D;
/* 111 */     this.m_helper.addComponent(mainPanel, top);
/*     */ 
/* 113 */     gh.useGridBag(top);
/* 114 */     this.m_helper.addPanelTitle(top, LocaleResources.getString("apTitleLinkProperties", this.m_ctx));
/* 115 */     gh.m_gc.fill = 2;
/* 116 */     gh.m_gc.weightx = 1.0D;
/* 117 */     this.m_helper.addLabelFieldPair(top, LocaleResources.getString("apLabelLinkTitle", this.m_ctx), new CustomTextField(50), "LinkTitle");
/*     */ 
/* 119 */     this.m_helper.addLabelFieldPair(top, LocaleResources.getString("apLabelDescription", this.m_ctx), new CustomTextField(50), "LinkDescription");
/*     */ 
/* 124 */     if (!initLinkFields(top))
/*     */     {
/* 126 */       return false;
/*     */     }
/*     */ 
/* 130 */     this.m_helper.loadComponentValues();
/*     */ 
/* 132 */     return this.m_helper.prompt() == 1;
/*     */   }
/*     */ 
/*     */   public abstract boolean initLinkFields(JPanel paramJPanel);
/*     */ 
/*     */   public int determineIndex(String fieldName)
/*     */   {
/* 143 */     int index = -1;
/*     */ 
/* 145 */     if (fieldName.equals("LinkData"))
/*     */     {
/* 147 */       index = 1;
/*     */     }
/* 149 */     else if (fieldName.equals("LinkTitle"))
/*     */     {
/* 151 */       index = 2;
/*     */     }
/* 153 */     else if (fieldName.equals("LinkDescription"))
/*     */     {
/* 155 */       index = 3;
/*     */     }
/* 157 */     return index;
/*     */   }
/*     */ 
/*     */   public void exchangeBoundField(int index, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 164 */     if (updateComponent)
/*     */     {
/* 166 */       exchange.m_compValue = ((String)this.m_linkInfo.elementAt(index));
/*     */     }
/*     */     else
/*     */     {
/* 170 */       this.m_linkInfo.setElementAt(exchange.m_compValue, index);
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(Exception e, String msg)
/*     */   {
/* 180 */     MessageBox.reportError(this.m_sysInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public void reportError(Exception e, IdcMessage msg)
/*     */   {
/* 185 */     MessageBox.reportError(this.m_sysInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 193 */     String name = exchange.m_compName;
/* 194 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 199 */     String name = exchange.m_compName;
/* 200 */     String val = exchange.m_compValue;
/*     */ 
/* 202 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 211 */     int index = determineIndex(name);
/*     */ 
/* 213 */     if (index < 0)
/*     */       return;
/* 215 */     exchangeBoundField(index, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 222 */     if ((name.equals("LinkData")) && 
/* 224 */       (val == null))
/*     */     {
/* 226 */       exchange.m_errorMessage = IdcMessageFactory.lc("apSpecifyLinkReference", new Object[0]);
/* 227 */       return false;
/*     */     }
/*     */ 
/* 230 */     if ((name.equals("LinkTitle")) && 
/* 232 */       (val == null))
/*     */     {
/* 234 */       exchange.m_errorMessage = IdcMessageFactory.lc("apSpecifyTitleForLink", new Object[0]);
/* 235 */       return false;
/*     */     }
/*     */ 
/* 239 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 244 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditLinkBaseDlg
 * JD-Core Version:    0.5.4
 */