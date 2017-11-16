/*    */ package intradoc.resource;
/*    */ 
/*    */ import java.util.Enumeration;
/*    */ import java.util.Hashtable;
/*    */ import java.util.Properties;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class ParsedTree
/*    */ {
/* 28 */   public String m_key = null;
/* 29 */   public Vector m_nodeNames = null;
/*    */ 
/* 31 */   public boolean m_isResultSet = false;
/* 32 */   public String m_rsName = null;
/*    */ 
/* 35 */   public Hashtable m_subNodes = null;
/* 36 */   public boolean m_isSubNode = false;
/* 37 */   public String m_subName = null;
/* 38 */   public boolean m_isAppend = false;
/*    */ 
/* 40 */   public int m_row = 0;
/*    */ 
/* 42 */   public String m_value = null;
/* 43 */   public Properties m_props = null;
/*    */ 
/*    */   public ParsedTree(String key, Vector nodes, int count)
/*    */   {
/* 47 */     this.m_key = key;
/* 48 */     this.m_nodeNames = nodes;
/* 49 */     this.m_row = count;
/* 50 */     this.m_subNodes = new Hashtable();
/* 51 */     this.m_props = new Properties();
/*    */   }
/*    */ 
/*    */   public void addSubNode(ParsedTree subNode)
/*    */   {
/* 58 */     String subName = subNode.m_subName;
/* 59 */     ParsedTree node = (ParsedTree)this.m_subNodes.get(subName);
/* 60 */     if (node == null)
/*    */     {
/* 62 */       this.m_subNodes.put(subName, subNode);
/*    */     }
/*    */     else
/*    */     {
/* 66 */       node.merge(subNode);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void merge(ParsedTree node)
/*    */   {
/* 72 */     if (node.m_value != null)
/*    */     {
/* 74 */       this.m_value = node.m_value;
/*    */     }
/*    */ 
/* 77 */     Properties props = node.m_props;
/* 78 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*    */     {
/* 80 */       String key = (String)en.nextElement();
/* 81 */       this.m_props.put(key, props.getProperty(key));
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 87 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.ParsedTree
 * JD-Core Version:    0.5.4
 */