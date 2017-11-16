/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.SystemColor;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JLabel;
/*     */ 
/*     */ public class CustomLabel extends JLabel
/*     */ {
/*     */   protected int m_minWidth;
/*  40 */   protected boolean m_useLocale = false;
/*  41 */   protected ExecutionContext m_context = null;
/*     */ 
/*     */   public CustomLabel(String title)
/*     */   {
/*  45 */     super(title);
/*  46 */     getAccessibleContext().setAccessibleDescription(title);
/*  47 */     setFocusable(true);
/*  48 */     GuiStyles.setCustomStyle(this, 0);
/*  49 */     this.m_minWidth = 0;
/*  50 */     setBackground(SystemColor.control);
/*  51 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public CustomLabel(String title, int custStyle)
/*     */   {
/*  56 */     super(title);
/*  57 */     getAccessibleContext().setAccessibleDescription(title);
/*  58 */     setFocusable(true);
/*  59 */     GuiStyles.setCustomStyle(this, custStyle);
/*  60 */     this.m_minWidth = 0;
/*  61 */     setBackground(SystemColor.control);
/*  62 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public CustomLabel()
/*     */   {
/*  67 */     setFocusable(true);
/*  68 */     GuiStyles.setCustomStyle(this, 0);
/*  69 */     this.m_minWidth = 0;
/*  70 */     setBackground(SystemColor.control);
/*  71 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public void setUseLocale(boolean useLocale, ExecutionContext cxt)
/*     */   {
/*  76 */     this.m_useLocale = useLocale;
/*  77 */     this.m_context = cxt;
/*     */   }
/*     */ 
/*     */   public void setMinWidth(int minWidth)
/*     */   {
/*  82 */     this.m_minWidth = minWidth;
/*     */   }
/*     */ 
/*     */   public Dimension getPreferredSize()
/*     */   {
/*  88 */     setBackground(null);
/*     */ 
/*  90 */     Dimension d = super.getPreferredSize();
/*     */ 
/*  92 */     if (this.m_minWidth > 0)
/*  93 */       d.width = this.m_minWidth;
/*  94 */     return d;
/*     */   }
/*     */ 
/*     */   public Dimension getMinimumSize()
/*     */   {
/* 100 */     setBackground(null);
/*     */ 
/* 102 */     Dimension d = super.getMinimumSize();
/* 103 */     if (this.m_minWidth > 0)
/* 104 */       d.width = this.m_minWidth;
/* 105 */     return d;
/*     */   }
/*     */ 
/*     */   public void setText(String value)
/*     */   {
/* 111 */     if ((this.m_useLocale) && (this.m_context != null))
/*     */     {
/* 113 */       String lValue = LocaleResources.getString(value, this.m_context);
/* 114 */       if (lValue != null)
/*     */       {
/* 116 */         value = lValue;
/*     */       }
/*     */     }
/* 119 */     super.setText(value);
/* 120 */     getAccessibleContext().setAccessibleDescription(value);
/* 121 */     setFocusable(true);
/*     */   }
/*     */ 
/*     */   public void setAlignment(int align)
/*     */   {
/* 126 */     setHorizontalAlignment(align);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 131 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CustomLabel
 * JD-Core Version:    0.5.4
 */