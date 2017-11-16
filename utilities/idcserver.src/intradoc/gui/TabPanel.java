/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.gui.iwt.IdcTabbedPane;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.FocusListener;
/*     */ import java.util.Hashtable;
/*     */ 
/*     */ public class TabPanel extends PanePanel
/*     */ {
/*     */   protected IdcTabbedPane m_tabPane;
/*     */ 
/*     */   public TabPanel()
/*     */   {
/*  37 */     init();
/*     */   }
/*     */ 
/*     */   protected void init()
/*     */   {
/*  42 */     setLayout(new BorderLayout());
/*  43 */     this.m_tabPane = new IdcTabbedPane();
/*  44 */     add("Center", this.m_tabPane);
/*     */   }
/*     */ 
/*     */   public void addPane(String name, Component comp)
/*     */   {
/*  49 */     addPane(name, comp, null, false, null);
/*     */   }
/*     */ 
/*     */   public void addPane(String name, Component comp, PromptHandler promptCallback)
/*     */   {
/*  54 */     addPane(name, comp, promptCallback, false, null);
/*     */   }
/*     */ 
/*     */   public void addPane(String name, Component comp, PromptHandler promptCallback, boolean isValidate)
/*     */   {
/*  59 */     addPane(name, comp, promptCallback, isValidate, null);
/*     */   }
/*     */ 
/*     */   public void addPane(String name, Component comp, PromptHandler promptCallback, boolean isValidate, FocusListener l)
/*     */   {
/*  65 */     this.m_tabPane.addTab(name, comp);
/*     */ 
/*  67 */     this.m_tabPane.m_usePromptHandlersOnChange.put(name, new Boolean(isValidate));
/*  68 */     if (promptCallback != null)
/*     */     {
/*  70 */       this.m_tabPane.m_promptHandlers.put(name, promptCallback);
/*     */     }
/*  72 */     if (l == null)
/*     */       return;
/*  74 */     this.m_tabPane.m_focusListeners.put(name, l);
/*     */   }
/*     */ 
/*     */   public void selectPane(String name)
/*     */   {
/*  80 */     int tabCount = this.m_tabPane.getTabCount();
/*  81 */     for (int i = 0; i < tabCount; ++i)
/*     */     {
/*  83 */       if (!this.m_tabPane.getTitleAt(i).equalsIgnoreCase(name))
/*     */         continue;
/*  85 */       this.m_tabPane.setSelectedIndex(i);
/*  86 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateAllPanes()
/*     */   {
/*  93 */     return this.m_tabPane.validateAllPanes();
/*     */   }
/*     */ 
/*     */   public void setFullWidthTab(boolean isFullWidth)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 103 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.TabPanel
 * JD-Core Version:    0.5.4
 */