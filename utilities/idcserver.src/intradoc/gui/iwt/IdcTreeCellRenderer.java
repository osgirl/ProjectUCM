/*    */ package intradoc.gui.iwt;
/*    */ 
/*    */ import intradoc.shared.gui.ValueNode;
/*    */ import java.awt.Component;
/*    */ import javax.swing.Icon;
/*    */ import javax.swing.JTree;
/*    */ import javax.swing.tree.DefaultTreeCellRenderer;
/*    */ 
/*    */ public class IdcTreeCellRenderer extends DefaultTreeCellRenderer
/*    */ {
/*    */   public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFoc)
/*    */   {
/* 34 */     super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFoc);
/*    */ 
/* 36 */     if (value instanceof ValueNode)
/*    */     {
/* 38 */       ValueNode node = (ValueNode)value;
/* 39 */       if (node.m_dataHelper != null)
/*    */       {
/* 41 */         String type = null;
/* 42 */         if (expanded)
/*    */         {
/* 44 */           type = "OpenIcons";
/*    */         }
/*    */         else
/*    */         {
/* 48 */           type = "ClosedIcons";
/*    */         }
/*    */ 
/* 51 */         Icon icon = (Icon)node.m_dataHelper.get(node, type);
/* 52 */         if (icon != null)
/*    */         {
/* 54 */           setIcon(icon);
/*    */         }
/*    */       }
/*    */     }
/*    */ 
/* 59 */     return this;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 64 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.IdcTreeCellRenderer
 * JD-Core Version:    0.5.4
 */