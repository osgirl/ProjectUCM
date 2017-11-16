/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.KeyEvent;
/*     */ import java.awt.event.KeyListener;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.awt.event.MouseListener;
/*     */ import java.util.Collection;
/*     */ import java.util.HashSet;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.JList;
/*     */ import javax.swing.ListModel;
/*     */ 
/*     */ public class IdcList extends JList
/*     */   implements MouseListener, KeyListener
/*     */ {
/*     */   public Collection<ActionListener> m_actionListeners;
/*     */   public Object m_actionSource;
/*     */ 
/*     */   public IdcList()
/*     */   {
/*  40 */     super(new DefaultListModel());
/*  41 */     this.m_actionListeners = new HashSet();
/*  42 */     addMouseListener(this);
/*  43 */     addKeyListener(this);
/*     */   }
/*     */ 
/*     */   public IdcList(ListModel m)
/*     */   {
/*  48 */     super(m);
/*  49 */     this.m_actionListeners = new HashSet();
/*  50 */     addMouseListener(this);
/*  51 */     addKeyListener(this);
/*     */   }
/*     */ 
/*     */   public void addElement(Object item)
/*     */   {
/*  56 */     ListModel model = getModel();
/*  57 */     if (!model instanceof DefaultListModel)
/*     */       return;
/*  59 */     DefaultListModel listModel = (DefaultListModel)model;
/*  60 */     listModel.addElement(item);
/*     */   }
/*     */ 
/*     */   public void addActionListener(ActionListener l)
/*     */   {
/*  66 */     if (l == null)
/*     */     {
/*  68 */       return;
/*     */     }
/*     */ 
/*  71 */     this.m_actionListeners.add(l);
/*     */   }
/*     */ 
/*     */   public void removeActionListener(ActionListener l)
/*     */   {
/*  76 */     if (l == null)
/*     */     {
/*  78 */       return;
/*     */     }
/*     */ 
/*  81 */     this.m_actionListeners.remove(l);
/*     */   }
/*     */ 
/*     */   public void mouseClicked(MouseEvent e)
/*     */   {
/*  86 */     if (e.getClickCount() != 2)
/*     */       return;
/*  88 */     sendActionEvent();
/*     */   }
/*     */ 
/*     */   public void keyPressed(KeyEvent e)
/*     */   {
/*  94 */     if (e.getKeyCode() != 10)
/*     */       return;
/*  96 */     sendActionEvent();
/*     */   }
/*     */ 
/*     */   public void sendActionEvent()
/*     */   {
/*     */     Object source;
/*     */     Object source;
/* 103 */     if (this.m_actionSource != null)
/*     */     {
/* 105 */       source = this.m_actionSource;
/*     */     }
/*     */     else
/*     */     {
/* 109 */       source = this;
/*     */     }
/*     */ 
/* 112 */     ActionEvent action = new ActionEvent(source, 1001, "action");
/* 113 */     for (ActionListener l : this.m_actionListeners)
/*     */     {
/* 115 */       l.actionPerformed(action);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void mousePressed(MouseEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void mouseReleased(MouseEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void mouseEntered(MouseEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void mouseExited(MouseEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void keyTyped(KeyEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void keyReleased(KeyEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 151 */     return "releaseInfo=dev,releaseRevision=$Rev: 78557 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.IdcList
 * JD-Core Version:    0.5.4
 */