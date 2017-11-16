/*     */ package intradoc.apps.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class PromptDialog extends JDialog
/*     */ {
/*  41 */   protected String m_helpPage = null;
/*  42 */   protected SystemInterface m_sys = null;
/*  43 */   protected ExecutionContext m_cxt = null;
/*  44 */   public String[] m_options = new String[0];
/*  45 */   public IdcMessage[] m_captions = new IdcMessage[0];
/*  46 */   public boolean[] m_states = new boolean[0];
/*  47 */   public IdcMessage m_msg = null;
/*     */ 
/*  49 */   protected DialogHelper m_helper = null;
/*     */ 
/*     */   public PromptDialog(SystemInterface sys, IdcMessage title, String helpPage)
/*     */   {
/*  53 */     super(sys.getMainWindow(), sys.localizeMessage(title), true);
/*     */ 
/*  55 */     this.m_sys = sys;
/*  56 */     this.m_helper = new DialogHelper();
/*  57 */     this.m_helper.attachToDialog(this, sys, null);
/*  58 */     this.m_cxt = sys.getExecutionContext();
/*     */ 
/*  60 */     if (helpPage != null)
/*     */     {
/*  62 */       this.m_helpPage = DialogHelpTable.getHelpPage(helpPage);
/*     */     }
/*     */ 
/*  65 */     this.m_captions = new IdcMessage[] { IdcMessageFactory.lc("apDeleteAfterArchive", new Object[0]), IdcMessageFactory.lc("apDoTableExport", new Object[0]) };
/*     */ 
/*  68 */     this.m_msg = IdcMessageFactory.lc("apVerifyArchiveExport", new Object[0]);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void init(String[] options, String[] captionKeys, boolean[] states, String msg)
/*     */   {
/*  75 */     IdcMessage idcmsg = IdcMessageFactory.lc();
/*  76 */     idcmsg.m_msgEncoded = msg;
/*  77 */     IdcMessage[] messages = new IdcMessage[captionKeys.length];
/*  78 */     for (int i = 0; i < captionKeys.length; ++i)
/*     */     {
/*  80 */       messages[i] = IdcMessageFactory.lc(captionKeys[i], new Object[0]);
/*     */     }
/*  82 */     init(options, messages, states, idcmsg);
/*     */   }
/*     */ 
/*     */   public void init(String[] options, IdcMessage[] captionKeys, boolean[] states, IdcMessage msg)
/*     */   {
/*  87 */     this.m_options = options;
/*  88 */     this.m_captions = captionKeys;
/*  89 */     this.m_states = states;
/*  90 */     this.m_msg = msg;
/*     */   }
/*     */ 
/*     */   public boolean prompt(Properties props)
/*     */   {
/*  95 */     this.m_helper.m_props = ((Properties)props.clone());
/*     */ 
/*  97 */     JPanel mainPanel = this.m_helper.initStandard(null, null, 2, true, this.m_helpPage);
/*     */ 
/* 100 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(10);
/* 101 */     this.m_helper.addComponent(mainPanel, new CustomText(this.m_sys.localizeMessage(this.m_msg), 50));
/* 102 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 103 */     this.m_helper.m_gridHelper.addEmptyRow(mainPanel);
/*     */ 
/* 105 */     JCheckBox[] optBoxes = null;
/* 106 */     if (this.m_options.length > 0)
/*     */     {
/* 108 */       String optionPanelTitle = "";
/* 109 */       if (this.m_options.length > 1)
/*     */       {
/* 111 */         optionPanelTitle = LocaleResources.getString("apArchiverLabelOptions", this.m_cxt);
/*     */       }
/*     */ 
/* 114 */       JPanel subPanel = createSubPanel(mainPanel, optionPanelTitle);
/*     */ 
/* 116 */       optBoxes = addOptionList(subPanel, this.m_options, this.m_captions, this.m_states);
/*     */     }
/*     */ 
/* 120 */     if (this.m_helper.prompt() == 1)
/*     */     {
/* 122 */       for (int i = 0; (optBoxes != null) && (i < optBoxes.length); ++i)
/*     */       {
/* 124 */         if (optBoxes[i].isSelected())
/*     */         {
/* 126 */           props.put(this.m_options[i], "1");
/*     */         }
/*     */         else
/*     */         {
/* 130 */           props.put(this.m_options[i], "0");
/*     */         }
/*     */       }
/* 133 */       return true;
/*     */     }
/* 135 */     return false;
/*     */   }
/*     */ 
/*     */   protected JPanel createSubPanel(JPanel panel, String title)
/*     */   {
/* 140 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 141 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 142 */     JPanel subPanel = new PanePanel();
/* 143 */     this.m_helper.addComponent(panel, subPanel);
/*     */ 
/* 145 */     this.m_helper.makePanelGridBag(subPanel, 2);
/* 146 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 147 */     this.m_helper.addComponent(subPanel, new CustomText(title, 50));
/* 148 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(10);
/*     */ 
/* 150 */     return subPanel;
/*     */   }
/*     */ 
/*     */   protected JCheckBox[] addOptionList(JPanel panel, String[] options, IdcMessage[] captions, boolean[] states)
/*     */   {
/* 156 */     int len = options.length;
/* 157 */     JCheckBox[] boxes = new JCheckBox[len];
/*     */ 
/* 159 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 161 */       boxes[i] = new JCheckBox(this.m_sys.localizeMessage(captions[i]));
/* 162 */       boxes[i].setBackground(getBackground());
/* 163 */       boxes[i].setSelected(states[i]);
/* 164 */       this.m_helper.addComponent(panel, boxes[i]);
/*     */     }
/*     */ 
/* 167 */     return boxes;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 172 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.PromptDialog
 * JD-Core Version:    0.5.4
 */