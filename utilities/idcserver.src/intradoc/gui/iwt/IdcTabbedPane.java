/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.gui.PromptHandler;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.FocusListener;
/*     */ import java.util.Hashtable;
/*     */ import javax.swing.JTabbedPane;
/*     */ 
/*     */ public class IdcTabbedPane extends JTabbedPane
/*     */ {
/*  30 */   public Hashtable<String, PromptHandler> m_promptHandlers = new Hashtable();
/*  31 */   public Hashtable<String, Boolean> m_usePromptHandlersOnChange = new Hashtable();
/*  32 */   public Hashtable<String, FocusListener> m_focusListeners = new Hashtable();
/*     */ 
/*     */   public void setSelectedIndex(int index)
/*     */   {
/*  42 */     int currentIndex = getSelectedIndex();
/*  43 */     if (currentIndex == -1)
/*     */     {
/*  45 */       super.setSelectedIndex(index);
/*  46 */       return;
/*     */     }
/*     */ 
/*  49 */     String currentTitle = getTitleAt(currentIndex);
/*     */ 
/*  51 */     PromptHandler callback = (PromptHandler)this.m_promptHandlers.get(currentTitle);
/*  52 */     if (callback != null)
/*     */     {
/*  54 */       boolean isValidateOnChange = ((Boolean)this.m_usePromptHandlersOnChange.get(currentTitle)).booleanValue();
/*  55 */       if ((isValidateOnChange) && 
/*  57 */         (callback.prompt() != 1))
/*     */       {
/*  59 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  64 */     FocusListener l = (FocusListener)this.m_focusListeners.get(currentTitle);
/*  65 */     if (l != null)
/*     */     {
/*  67 */       FocusEvent event = new FocusEvent(this, 1005);
/*  68 */       l.focusLost(event);
/*     */     }
/*     */ 
/*  71 */     super.setSelectedIndex(index);
/*  72 */     int newIndex = getSelectedIndex();
/*  73 */     String newTitle = getTitleAt(newIndex);
/*     */ 
/*  75 */     l = (FocusListener)this.m_focusListeners.get(newTitle);
/*  76 */     if (l == null)
/*     */       return;
/*  78 */     FocusEvent event = new FocusEvent(this, 1004);
/*  79 */     l.focusGained(event);
/*     */   }
/*     */ 
/*     */   public boolean validateAllPanes()
/*     */   {
/*  86 */     int numTabs = getTabCount();
/*  87 */     for (int i = 0; i < numTabs; ++i)
/*     */     {
/*  89 */       String title = getTitleAt(i);
/*  90 */       PromptHandler callback = (PromptHandler)this.m_promptHandlers.get(title);
/*  91 */       if ((callback == null) || 
/*  93 */         (callback.prompt() == 1))
/*     */         continue;
/*  95 */       setSelectedIndex(i);
/*  96 */       return false;
/*     */     }
/*     */ 
/* 101 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 106 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.IdcTabbedPane
 * JD-Core Version:    0.5.4
 */