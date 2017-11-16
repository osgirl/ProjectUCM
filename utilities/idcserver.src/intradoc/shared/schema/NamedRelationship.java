/*    */ package intradoc.shared.schema;
/*    */ 
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class NamedRelationship
/*    */ {
/*    */   public SchemaViewData m_view;
/*    */   public SchemaFieldData m_field;
/*    */   public SchemaRelationData m_relation;
/*    */   public SchemaFieldData m_parentField;
/*    */   public Vector m_children;
/*    */   public boolean m_isTree;
/*    */   public Vector m_tree;
/*    */   public boolean m_isRecursive;
/*    */   public SchemaTreePointer m_treePointer;
/*    */   public String m_id;
/*    */ 
/*    */   public NamedRelationship()
/*    */   {
/* 35 */     this.m_isTree = false;
/* 36 */     this.m_tree = null;
/*    */ 
/* 38 */     this.m_isRecursive = false;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 46 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 47 */     builder.append("id: ");
/* 48 */     builder.append(this.m_id);
/* 49 */     builder.append(", view: ");
/* 50 */     builder.appendObject(this.m_view);
/* 51 */     builder.append(", field: ");
/* 52 */     builder.appendObject(this.m_field);
/* 53 */     return builder.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 58 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.NamedRelationship
 * JD-Core Version:    0.5.4
 */