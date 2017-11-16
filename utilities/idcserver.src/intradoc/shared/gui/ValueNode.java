/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.gui.iwt.DataRetrievalHelper;
/*     */ import java.util.Properties;
/*     */ import javax.swing.tree.DefaultMutableTreeNode;
/*     */ 
/*     */ public class ValueNode extends DefaultMutableTreeNode
/*     */ {
/*     */   public String m_value;
/*     */   public String m_typeId;
/*     */   public Properties m_props;
/*     */   public String m_id;
/*     */   public int m_parentIndex;
/*     */   public int m_recursionCount;
/*     */   public boolean m_isTree;
/*     */   public DataRetrievalHelper m_dataHelper;
/*     */   public Object m_extraData;
/*     */   public Object m_containingObject;
/*     */ 
/*     */   public ValueNode()
/*     */   {
/*  32 */     this.m_props = new Properties();
/*     */ 
/*  34 */     this.m_parentIndex = -1;
/*     */ 
/*  36 */     this.m_recursionCount = 0;
/*  37 */     this.m_isTree = false;
/*     */ 
/*  40 */     this.m_extraData = null;
/*     */ 
/*  48 */     this.m_containingObject = null;
/*     */   }
/*     */ 
/*     */   public String hashcode()
/*     */   {
/*  53 */     return this.m_value;
/*     */   }
/*     */ 
/*     */   public ValueNode getParent()
/*     */   {
/*  59 */     return (ValueNode)super.getParent();
/*     */   }
/*     */ 
/*     */   public int getParentIndex()
/*     */   {
/*  66 */     return this.m_parentIndex;
/*     */   }
/*     */ 
/*     */   public boolean equals(Object obj)
/*     */   {
/*  72 */     ValueNode node = (ValueNode)obj;
/*  73 */     if (((this.m_value == null) && (node.m_value != null)) || ((this.m_value != null) && (node.m_value == null)))
/*     */     {
/*  76 */       return false;
/*     */     }
/*     */ 
/*  79 */     if (((this.m_id == null) && (node.m_id != null)) || ((this.m_id != null) && (node.m_id == null)))
/*     */     {
/*  82 */       return false;
/*     */     }
/*     */ 
/*  85 */     if ((this.m_value != null) && (!this.m_value.equals(node.m_value)))
/*     */     {
/*  87 */       return false;
/*     */     }
/*     */ 
/*  90 */     if ((this.m_id != null) && (!this.m_id.equals(node.m_id)))
/*     */     {
/*  92 */       return false;
/*     */     }
/*     */ 
/*  95 */     if (this.m_parentIndex != node.m_parentIndex)
/*     */     {
/*  97 */       return false;
/*     */     }
/*     */ 
/* 102 */     return this.m_recursionCount == node.m_recursionCount;
/*     */   }
/*     */ 
/*     */   public Object getUserObject()
/*     */   {
/* 111 */     return this.m_props;
/*     */   }
/*     */ 
/*     */   public void setUserObject(Object obj)
/*     */   {
/* 117 */     this.m_props = ((Properties)obj);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 123 */     if (this.m_dataHelper != null)
/*     */     {
/* 125 */       return (String)this.m_dataHelper.get(this, "label");
/*     */     }
/*     */ 
/* 128 */     return this.m_value;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 133 */     return "releaseInfo=dev,releaseRevision=$Rev: 79025 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.ValueNode
 * JD-Core Version:    0.5.4
 */