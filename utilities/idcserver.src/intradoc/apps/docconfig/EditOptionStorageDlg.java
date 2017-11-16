/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditOptionStorageDlg
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_context;
/*     */   protected SharedContext m_shContext;
/*     */   protected String m_helpPage;
/*  58 */   protected Properties m_saveProps = null;
/*  59 */   protected boolean m_isNew = false;
/*     */ 
/*     */   public EditOptionStorageDlg(SystemInterface sys, String title, SharedContext shContext, String helpPage)
/*     */   {
/*  64 */     this.m_systemInterface = sys;
/*  65 */     this.m_context = sys.getExecutionContext();
/*  66 */     this.m_shContext = shContext;
/*     */ 
/*  68 */     title = LocaleResources.localizeMessage(title, this.m_context);
/*  69 */     this.m_helper = new DialogHelper(sys, title, true);
/*     */ 
/*  72 */     this.m_helper.m_keepSpaces = true;
/*  73 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(Properties props, String optType, boolean isNew)
/*     */   {
/*  78 */     this.m_saveProps = ((Properties)props.clone());
/*  79 */     this.m_isNew = isNew;
/*  80 */     this.m_helper.m_props = props;
/*     */ 
/*  82 */     initUI(optType);
/*  83 */     prepareInfo();
/*  84 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI(String optType)
/*     */   {
/*  89 */     JPanel panel = createStoragePanel(optType);
/*     */ 
/*  92 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 100 */         Properties props = EditOptionStorageDlg.this.m_helper.m_props;
/* 101 */         boolean isStoreKey = StringUtils.convertToBool(props.getProperty("storeKey"), false);
/* 102 */         String val = "text";
/* 103 */         if (isStoreKey)
/*     */         {
/* 105 */           val = "key";
/*     */         }
/* 107 */         props.put("ValueStorage", val);
/* 108 */         props.remove("storeKey");
/* 109 */         props.remove("storeText");
/*     */ 
/* 111 */         boolean isOldPad = StringUtils.convertToBool(EditOptionStorageDlg.this.m_saveProps.getProperty("PadMultiselectStorage"), false);
/*     */ 
/* 113 */         boolean isPad = StringUtils.convertToBool(props.getProperty("PadMultiselectStorage"), false);
/*     */ 
/* 116 */         IdcMessage error = null;
/* 117 */         if (isPad)
/*     */         {
/* 120 */           String padText = props.getProperty("MultiselectStorageSeparator");
/* 121 */           if ((padText.startsWith(" ")) || (padText.endsWith(" ")))
/*     */           {
/* 123 */             error = IdcMessageFactory.lc("apStorageSeparatorIllegalSpaces", new Object[0]);
/*     */           }
/*     */         }
/* 126 */         if (error != null)
/*     */         {
/* 128 */           MessageBox.doMessage(EditOptionStorageDlg.this.m_systemInterface, error, 1);
/* 129 */           return false;
/*     */         }
/*     */ 
/* 134 */         if (!EditOptionStorageDlg.this.m_isNew)
/*     */         {
/* 136 */           IdcMessage msg = null;
/* 137 */           String oldSep = EditOptionStorageDlg.this.m_saveProps.getProperty("MultiselectStorageSeparator");
/* 138 */           String sep = props.getProperty("MultiselectStorageSeparator");
/* 139 */           if ((oldSep == null) || (!oldSep.equals(sep)))
/*     */           {
/* 141 */             msg = IdcMessageFactory.lc("apStorageSeparatorIsChanged", new Object[0]);
/*     */           }
/* 145 */           else if (isOldPad != isPad)
/*     */           {
/* 147 */             msg = IdcMessageFactory.lc("apStoragePaddingIsDifferent", new Object[0]);
/*     */           }
/*     */ 
/* 151 */           if (msg != null)
/*     */           {
/* 153 */             int r = MessageBox.doMessage(EditOptionStorageDlg.this.m_systemInterface, msg, 2);
/* 154 */             if (r == 0)
/*     */             {
/* 156 */               return false;
/*     */             }
/*     */           }
/*     */         }
/* 160 */         return true;
/*     */       }
/*     */     };
/* 163 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 166 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 167 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 168 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 169 */     this.m_helper.addLastComponentInRow(mainPanel, panel);
/*     */   }
/*     */ 
/*     */   protected JPanel createStoragePanel(String optType)
/*     */   {
/* 174 */     CustomPanel panel = new CustomPanel();
/* 175 */     this.m_helper.makePanelGridBag(panel, 2);
/*     */ 
/* 178 */     CustomLabel storageOptionLabel = new CustomLabel(this.m_systemInterface.getString("apCaptionStorageType"), 1);
/*     */ 
/* 181 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 182 */     this.m_helper.addComponent(panel, storageOptionLabel);
/*     */ 
/* 184 */     Insets insets = this.m_helper.m_gridHelper.m_gc.insets;
/* 185 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(insets.top, insets.left + 10, insets.bottom, insets.right);
/*     */ 
/* 188 */     ButtonGroup boxGroup = new ButtonGroup();
/* 189 */     String[][] storageOptions = { { "storeKey", "apLabelStoreKey" }, { "storeText", "apLabelStoreText" } };
/*     */ 
/* 194 */     for (int i = 0; i < storageOptions.length; ++i)
/*     */     {
/* 196 */       JCheckBox storeBox = new CustomCheckbox(this.m_systemInterface.getString(storageOptions[i][1]), boxGroup);
/*     */ 
/* 198 */       this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 199 */       this.m_helper.addExchangeComponent(panel, storeBox, storageOptions[i][0]);
/*     */     }
/*     */ 
/* 202 */     boolean isEnabled = false;
/* 203 */     if (optType.startsWith("multi"))
/*     */     {
/* 205 */       isEnabled = true;
/*     */     }
/* 207 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 208 */     JCheckBox multiselectStorageSeparatorOnEnds = new CustomCheckbox(this.m_systemInterface.getString("apSchemaPadMultiselectStorageSeparatorOnEnds"));
/*     */ 
/* 210 */     this.m_helper.addExchangeComponent(panel, multiselectStorageSeparatorOnEnds, "PadMultiselectStorage");
/*     */ 
/* 212 */     multiselectStorageSeparatorOnEnds.setEnabled(isEnabled);
/*     */ 
/* 214 */     Component[] fields = this.m_helper.addLabelEditPair(panel, this.m_systemInterface.localizeCaption("apSchemaMultiselectStorageSeparatorLabel"), 5, "MultiselectStorageSeparator");
/*     */ 
/* 217 */     fields[1].setEnabled(isEnabled);
/*     */ 
/* 219 */     fields = this.m_helper.addLabelEditPair(panel, this.m_systemInterface.localizeCaption("apSchemaMultiselectDisplaySeparatorLabel"), 5, "MultiselectDisplaySeparator");
/*     */ 
/* 222 */     fields[1].setEnabled(isEnabled);
/*     */ 
/* 224 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void prepareInfo()
/*     */   {
/* 229 */     Properties props = this.m_helper.m_props;
/* 230 */     String storageType = props.getProperty("ValueStorage");
/* 231 */     if ((storageType != null) && (storageType.equals("text")))
/*     */     {
/* 233 */       props.put("storeText", "1");
/*     */     }
/*     */     else
/*     */     {
/* 237 */       props.put("storeKey", "1");
/*     */     }
/*     */ 
/* 240 */     String separator = this.m_helper.m_props.getProperty("MultiselectDisplaySeparator");
/* 241 */     if (separator == null)
/*     */     {
/* 243 */       props.put("MultiselectDisplaySeparator", ", ");
/*     */     }
/* 245 */     separator = this.m_helper.m_props.getProperty("MultiselectStorageSeparator");
/* 246 */     if (separator != null)
/*     */       return;
/* 248 */     props.put("MultiselectStorageSeparator", ", ");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 254 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditOptionStorageDlg
 * JD-Core Version:    0.5.4
 */