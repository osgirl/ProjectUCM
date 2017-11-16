/*     */ package intradoc.gui;
/*     */ 
/*     */ import java.awt.Container;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class GridBagHelper
/*     */ {
/*     */   public GridBagConstraints m_gc;
/*     */ 
/*     */   public GridBagHelper()
/*     */   {
/*  37 */     reset();
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/*  42 */     this.m_gc = new GridBagConstraints();
/*  43 */     this.m_gc.insets = new Insets(1, 0, 1, 0);
/*     */   }
/*     */ 
/*     */   public void useGridBag(Container container)
/*     */   {
/*  48 */     container.setLayout(new GridBagLayout());
/*  49 */     reset();
/*     */   }
/*     */ 
/*     */   public void setComponentBuffer(int width, int height)
/*     */   {
/*  54 */     this.m_gc.insets = new Insets(height, width, height, width);
/*     */   }
/*     */ 
/*     */   public void prepareAddRowElement()
/*     */   {
/*  59 */     prepareAddRowElement(10, 1);
/*     */   }
/*     */ 
/*     */   public void prepareAddRowElement(int anchor)
/*     */   {
/*  64 */     prepareAddRowElement(anchor, 1);
/*     */   }
/*     */ 
/*     */   public void prepareAddLastRowElement()
/*     */   {
/*  69 */     prepareAddRowElement(10, 0);
/*     */   }
/*     */ 
/*     */   public void prepareAddLastRowElement(int anchor)
/*     */   {
/*  74 */     prepareAddRowElement(anchor, 0);
/*     */   }
/*     */ 
/*     */   public void prepareAddRowElement(int anchor, int gridwidth)
/*     */   {
/*  79 */     this.m_gc.gridwidth = gridwidth;
/*  80 */     this.m_gc.weightx = 1.0D;
/*  81 */     this.m_gc.anchor = anchor;
/*     */   }
/*     */ 
/*     */   public void addEmptyRowElement(Container container)
/*     */   {
/*  87 */     this.m_gc.gridwidth = 1;
/*  88 */     container.add(new PanePanel());
/*     */   }
/*     */ 
/*     */   public void addEmptyRow(Container container)
/*     */   {
/*  93 */     GridBagLayout gridLayout = (GridBagLayout)container.getLayout();
/*  94 */     prepareAddLastRowElement();
/*  95 */     JPanel emptyComponent = new PanePanel();
/*  96 */     gridLayout.setConstraints(emptyComponent, this.m_gc);
/*  97 */     container.add(emptyComponent);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 102 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.GridBagHelper
 * JD-Core Version:    0.5.4
 */