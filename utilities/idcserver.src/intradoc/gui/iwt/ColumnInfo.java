/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ColumnInfo
/*     */ {
/*  41 */   public int m_columnAlignment = 18;
/*     */   public String m_labelText;
/*     */   public String m_description;
/*     */   public String m_fieldId;
/*  52 */   public boolean m_isVisible = true;
/*     */   public boolean m_isResizable;
/*     */   public boolean m_isResized;
/*     */   public boolean m_isMovable;
/*     */   public boolean m_isRemovable;
/*     */   public double m_weight;
/*     */   public int m_width;
/*     */   public int m_leftEdge;
/*     */   public boolean m_isTruncated;
/*     */   public boolean m_isCheckbox;
/*     */   protected Vector m_actionListeners;
/*     */ 
/*     */   public ColumnInfo(String label, String fieldId, double weight)
/*     */   {
/*  85 */     this.m_labelText = label;
/*  86 */     this.m_description = null;
/*  87 */     this.m_fieldId = fieldId;
/*  88 */     this.m_isResizable = true;
/*  89 */     this.m_isResized = false;
/*  90 */     this.m_isMovable = true;
/*  91 */     this.m_isRemovable = true;
/*  92 */     this.m_weight = weight;
/*  93 */     this.m_isTruncated = false;
/*  94 */     this.m_isCheckbox = false;
/*  95 */     this.m_actionListeners = new IdcVector();
/*     */   }
/*     */ 
/*     */   public Vector getActionListeners()
/*     */   {
/* 100 */     return this.m_actionListeners;
/*     */   }
/*     */ 
/*     */   public void addActionListener(ActionListener l)
/*     */   {
/* 105 */     this.m_actionListeners.addElement(l);
/*     */   }
/*     */ 
/*     */   public void removeActionListener(ActionListener l)
/*     */   {
/* 110 */     this.m_actionListeners.removeElement(l);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 115 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.ColumnInfo
 * JD-Core Version:    0.5.4
 */