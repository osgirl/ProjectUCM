/*     */ package intradoc.apps.shared;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollBar;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class TracingConfigUI extends DialogCallback
/*     */   implements ItemListener, ActionListener
/*     */ {
/*     */   protected SystemInterface m_int;
/*     */   protected DialogHelper m_helper;
/*     */   protected boolean m_isStandAlone;
/*     */   protected boolean m_isHeavyClient;
/*     */   protected DataResultSet m_allSections;
/*     */   protected Vector m_enabledServerSections;
/*     */   protected boolean[] m_serverIsVerbose;
/*     */   protected Vector m_enabledClientSections;
/*     */   protected boolean[] m_clientIsVerbose;
/*     */   protected JPanel m_mainPanel;
/*     */   protected JCheckBox m_serverConfig;
/*     */   protected JCheckBox m_clientConfig;
/*     */   protected JCheckBox m_verboseCheckbox;
/*     */   protected JScrollPane m_scrollPane;
/*     */   protected JPanel m_sectionsPanel;
/*     */   protected Hashtable m_sectionsCheckboxes;
/*     */   protected JTextField m_customSection;
/*     */   protected JButton m_customSectionAddButton;
/*     */ 
/*     */   public TracingConfigUI()
/*     */   {
/*  63 */     this.m_serverIsVerbose = new boolean[1];
/*     */ 
/*  65 */     this.m_clientIsVerbose = new boolean[1];
/*     */ 
/*  74 */     this.m_sectionsCheckboxes = new Hashtable();
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface systemInterface)
/*     */   {
/*  80 */     this.m_int = systemInterface;
/*  81 */     this.m_helper = new DialogHelper(this.m_int, this.m_int.localizeMessage("!apTracingConfigTitle"), true, false);
/*     */ 
/*  83 */     this.m_isStandAlone = AppLauncher.getIsStandAlone();
/*  84 */     this.m_isHeavyClient = AppLauncher.getIsHeavyClient();
/*     */   }
/*     */ 
/*     */   public void displayTracingConfig()
/*     */   {
/*  89 */     loadState();
/*  90 */     initUI();
/*  91 */     this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void loadState()
/*     */   {
/*  96 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/*  99 */       AppLauncher.executeService("APPEND_TRACING_INFO", binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 103 */       AppLauncher.reportOperationError(this.m_int, e, null);
/*     */     }
/* 105 */     this.m_allSections = ((DataResultSet)binder.getResultSet("IdcTracingSections"));
/* 106 */     String serverList = binder.getLocal("traceSectionsList");
/* 107 */     this.m_enabledServerSections = StringUtils.parseArrayEx(serverList, ',', '^', true);
/* 108 */     this.m_serverIsVerbose[0] = StringUtils.convertToBool(binder.getLocal("traceIsVerbose"), false);
/*     */ 
/* 110 */     if (this.m_isStandAlone)
/*     */       return;
/* 112 */     this.m_enabledClientSections = SystemUtils.getActiveTraces();
/* 113 */     this.m_clientIsVerbose[0] = SystemUtils.m_verbose;
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/* 119 */     this.m_mainPanel = this.m_helper.initStandard(null, this, 3, false, null);
/* 120 */     this.m_helper.m_gridHelper.m_gc.anchor = 17;
/*     */ 
/* 122 */     ButtonGroup group = new ButtonGroup();
/* 123 */     String label = this.m_int.localizeMessage("!apAppletTracingLabel");
/* 124 */     this.m_clientConfig = new JCheckBox(label, false);
/* 125 */     group.add(this.m_clientConfig);
/* 126 */     label = this.m_int.localizeMessage("!apServerTracingLabel");
/* 127 */     this.m_serverConfig = new JCheckBox(label, false);
/* 128 */     group.add(this.m_serverConfig);
/* 129 */     JPanel panel = new CustomPanel();
/* 130 */     panel.add(this.m_clientConfig);
/* 131 */     panel.add(this.m_serverConfig);
/* 132 */     this.m_helper.addLastComponentInRow(this.m_mainPanel, panel);
/*     */ 
/* 134 */     label = this.m_int.localizeMessage("!apTraceIsVerboseOption");
/* 135 */     this.m_verboseCheckbox = new JCheckBox(label);
/* 136 */     this.m_helper.addLastComponentInRow(this.m_mainPanel, this.m_verboseCheckbox);
/*     */ 
/* 138 */     this.m_sectionsPanel = new PanePanel();
/* 139 */     this.m_sectionsPanel.setLayout(new GridBagLayout());
/* 140 */     this.m_scrollPane = new JScrollPane(this.m_sectionsPanel);
/* 141 */     this.m_scrollPane.getVerticalScrollBar().setUnitIncrement(10);
/* 142 */     this.m_scrollPane.setPreferredSize(new Dimension(360, 350));
/* 143 */     for (this.m_allSections.first(); this.m_allSections.isRowPresent(); this.m_allSections.next())
/*     */     {
/* 145 */       Properties props = this.m_allSections.getCurrentRowProps();
/* 146 */       String name = props.getProperty("itsSection");
/* 147 */       JCheckBox box = new JCheckBox(name);
/* 148 */       this.m_sectionsCheckboxes.put(name, box);
/* 149 */       this.m_helper.addLastComponentInRow(this.m_sectionsPanel, box);
/*     */     }
/* 151 */     this.m_helper.addLastComponentInRow(this.m_mainPanel, this.m_scrollPane);
/*     */ 
/* 153 */     this.m_customSection = new JTextField("", 20);
/* 154 */     label = this.m_int.localizeMessage("!apAddTraceSection");
/* 155 */     this.m_customSectionAddButton = new JButton(label);
/* 156 */     this.m_customSectionAddButton.addActionListener(this);
/* 157 */     this.m_helper.addExchangeComponent(this.m_mainPanel, this.m_customSection, "newSection");
/* 158 */     this.m_helper.addLastComponentInRow(this.m_mainPanel, this.m_customSectionAddButton);
/*     */ 
/* 160 */     loadConfigIntoControls(this.m_serverIsVerbose[0], this.m_enabledServerSections);
/* 161 */     if (this.m_isStandAlone)
/*     */     {
/* 163 */       this.m_clientConfig.setEnabled(false);
/* 164 */       this.m_serverConfig.setEnabled(false);
/*     */     }
/*     */     else
/*     */     {
/* 168 */       this.m_clientConfig.setSelected(true);
/* 169 */       loadConfigIntoControls(this.m_clientIsVerbose[0], this.m_enabledClientSections);
/* 170 */       this.m_serverConfig.addItemListener(this);
/* 171 */       this.m_clientConfig.addItemListener(this);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean handleDialogEvent(ActionEvent event)
/*     */   {
/* 178 */     if ((this.m_isStandAlone) || (this.m_serverConfig.isSelected()))
/*     */     {
/* 180 */       loadConfigFromControls(this.m_serverIsVerbose, this.m_enabledServerSections);
/*     */     }
/*     */     else
/*     */     {
/* 184 */       loadConfigFromControls(this.m_clientIsVerbose, this.m_enabledClientSections);
/*     */     }
/*     */ 
/* 187 */     if (this.m_isStandAlone)
/*     */     {
/* 189 */       Report.m_verbose = this.m_serverIsVerbose[0];
/* 190 */       SystemUtils.m_verbose = Report.m_verbose;
/* 191 */       SystemUtils.setActiveTraces(this.m_enabledServerSections);
/*     */     }
/*     */     else
/*     */     {
/* 195 */       Report.m_verbose = this.m_clientIsVerbose[0];
/* 196 */       SystemUtils.m_verbose = Report.m_verbose;
/* 197 */       Report.trace("applet", "TracingConfigUI setting verbose to " + this.m_clientIsVerbose[0], null);
/*     */ 
/* 199 */       SystemUtils.setActiveTraces(this.m_enabledClientSections);
/* 200 */       Report.trace("applet", "TracingConfigUI setting sections to " + this.m_enabledClientSections, null);
/*     */ 
/* 202 */       DataBinder binder = new DataBinder();
/* 203 */       if (this.m_serverIsVerbose[0] != 0)
/*     */       {
/* 205 */         binder.putLocal("traceIsVerbose", "true");
/*     */       }
/* 207 */       StringBuffer list = new StringBuffer();
/* 208 */       for (int i = 0; i < this.m_enabledServerSections.size(); ++i)
/*     */       {
/* 210 */         if (i > 0)
/*     */         {
/* 212 */           list.append(", ");
/*     */         }
/* 214 */         list.append((String)this.m_enabledServerSections.elementAt(i));
/*     */       }
/* 216 */       binder.putLocal("traceSectionsList", list.toString());
/*     */       try
/*     */       {
/* 219 */         AppLauncher.executeService("EDIT_TRACE_OPTIONS", binder);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 223 */         AppLauncher.reportOperationError(this.m_int, e, null);
/* 224 */         return false;
/*     */       }
/*     */     }
/* 227 */     return true;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 232 */     if (e.getStateChange() != 1)
/*     */     {
/* 234 */       return;
/*     */     }
/*     */ 
/* 237 */     if (this.m_serverConfig.isSelected())
/*     */     {
/* 239 */       loadConfigFromControls(this.m_clientIsVerbose, this.m_enabledClientSections);
/* 240 */       loadConfigIntoControls(this.m_serverIsVerbose[0], this.m_enabledServerSections);
/*     */     }
/*     */     else
/*     */     {
/* 244 */       loadConfigFromControls(this.m_serverIsVerbose, this.m_enabledServerSections);
/* 245 */       loadConfigIntoControls(this.m_clientIsVerbose[0], this.m_enabledClientSections);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadConfigIntoControls(boolean isVerbose, Vector sections)
/*     */   {
/* 251 */     this.m_verboseCheckbox.setSelected(isVerbose);
/* 252 */     Enumeration en = this.m_sectionsCheckboxes.elements();
/* 253 */     while (en.hasMoreElements())
/*     */     {
/* 255 */       JCheckBox box = (JCheckBox)en.nextElement();
/* 256 */       box.setSelected(false);
/*     */     }
/*     */ 
/* 259 */     for (int i = 0; i < sections.size(); ++i)
/*     */     {
/* 261 */       String name = (String)sections.elementAt(i);
/* 262 */       JCheckBox box = (JCheckBox)this.m_sectionsCheckboxes.get(name);
/* 263 */       if (box == null)
/*     */       {
/* 265 */         box = newCheckbox(name, false);
/*     */       }
/* 267 */       box.setSelected(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadConfigFromControls(boolean[] isVerbose, Vector sections)
/*     */   {
/* 273 */     sections.removeAllElements();
/* 274 */     isVerbose[0] = this.m_verboseCheckbox.isSelected();
/* 275 */     Enumeration en = this.m_sectionsCheckboxes.keys();
/* 276 */     while (en.hasMoreElements())
/*     */     {
/* 278 */       String section = (String)en.nextElement();
/* 279 */       JCheckBox box = (JCheckBox)this.m_sectionsCheckboxes.get(section);
/* 280 */       if (box.isSelected())
/*     */       {
/* 282 */         sections.addElement(section);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 291 */     String section = this.m_customSection.getText();
/* 292 */     if (section.length() == 0)
/*     */     {
/* 294 */       return;
/*     */     }
/*     */     JCheckBox box;
/* 297 */     if ((box = (JCheckBox)this.m_sectionsCheckboxes.get(section)) == null)
/*     */     {
/* 299 */       box = newCheckbox(section, true);
/*     */     }
/* 301 */     box.setSelected(true);
/*     */   }
/*     */ 
/*     */   public JCheckBox newCheckbox(String section, boolean doScroll)
/*     */   {
/* 306 */     JCheckBox box = new JCheckBox(section);
/* 307 */     this.m_helper.addLastComponentInRow(this.m_sectionsPanel, box);
/* 308 */     this.m_sectionsCheckboxes.put(section, box);
/* 309 */     this.m_sectionsPanel.doLayout();
/*     */ 
/* 312 */     Dimension d = this.m_helper.m_dialog.getSize();
/* 313 */     d.width += 1;
/* 314 */     this.m_helper.m_dialog.setSize(d);
/* 315 */     d.width -= 1;
/* 316 */     this.m_helper.m_dialog.setSize(d);
/* 317 */     if (doScroll)
/*     */     {
/* 321 */       Thread t = new Thread()
/*     */       {
/*     */         public void run()
/*     */         {
/* 326 */           SystemUtils.sleep(100L);
/* 327 */           TracingConfigUI.this.m_scrollPane.getHorizontalScrollBar().setValue(0);
/* 328 */           TracingConfigUI.this.m_scrollPane.getVerticalScrollBar().setValue(700);
/*     */         }
/*     */       };
/* 331 */       t.start();
/*     */     }
/* 333 */     return box;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 338 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86762 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.TracingConfigUI
 * JD-Core Version:    0.5.4
 */