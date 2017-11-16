/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.gui.iwt.IdcTreeCellRenderer;
/*     */ import intradoc.shared.gui.ValueNode;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import javax.swing.JTree;
/*     */ import javax.swing.tree.DefaultTreeModel;
/*     */ import javax.swing.tree.DefaultTreeSelectionModel;
/*     */ import javax.swing.tree.TreePath;
/*     */ 
/*     */ public class IdcTreeHelper
/*     */ {
/*     */   public JTree m_tree;
/*     */   public DefaultTreeSelectionModel m_selectionModel;
/*     */   public DefaultTreeModel m_dataModel;
/*     */ 
/*     */   public JTree createTree()
/*     */   {
/*  44 */     ValueNode root = new ValueNode();
/*  45 */     root.m_value = "root";
/*  46 */     this.m_tree = new JTree(root);
/*  47 */     this.m_tree.setRootVisible(false);
/*  48 */     this.m_tree.setCellRenderer(new IdcTreeCellRenderer());
/*  49 */     this.m_tree.setShowsRootHandles(true);
/*  50 */     this.m_selectionModel = ((DefaultTreeSelectionModel)this.m_tree.getSelectionModel());
/*  51 */     this.m_dataModel = ((DefaultTreeModel)this.m_tree.getModel());
/*  52 */     this.m_dataModel.setAsksAllowsChildren(true);
/*     */ 
/*  54 */     return this.m_tree;
/*     */   }
/*     */ 
/*     */   public TreePath getTreePath(ValueNode node)
/*     */   {
/*  59 */     List l = new ArrayList();
/*  60 */     l.add(node);
/*  61 */     node = node.getParent();
/*  62 */     while (node != null)
/*     */     {
/*  64 */       l.add(0, node);
/*  65 */       node = node.getParent();
/*     */     }
/*     */ 
/*  68 */     ValueNode[] nodes = new ValueNode[l.size()];
/*  69 */     l.toArray(nodes);
/*  70 */     return new TreePath(nodes);
/*     */   }
/*     */ 
/*     */   public boolean isExpanded(ValueNode node)
/*     */   {
/*  75 */     return this.m_tree.isExpanded(getTreePath(node));
/*     */   }
/*     */ 
/*     */   public ValueNode getSelectedNode()
/*     */   {
/*  80 */     TreePath p = this.m_tree.getSelectionPath();
/*  81 */     if (p != null)
/*     */     {
/*  83 */       return (ValueNode)p.getLastPathComponent();
/*     */     }
/*     */ 
/*  86 */     return null;
/*     */   }
/*     */ 
/*     */   public void removeAllNodes()
/*     */   {
/*  91 */     ValueNode root = (ValueNode)this.m_dataModel.getRoot();
/*  92 */     removeChildrenOfNode(root);
/*     */   }
/*     */ 
/*     */   public void removeChildrenOfNode(ValueNode node)
/*     */   {
/*  97 */     int size = node.getChildCount();
/*  98 */     if (size <= 0)
/*     */       return;
/* 100 */     for (int i = size - 1; i >= 0; --i)
/*     */     {
/* 102 */       this.m_dataModel.removeNodeFromParent((ValueNode)node.getChildAt(i));
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 109 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.IdcTreeHelper
 * JD-Core Version:    0.5.4
 */