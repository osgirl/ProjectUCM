/*      */ package intradoc.server;
/*      */ 
/*      */ import java.util.Properties;
/*      */ 
/*      */ class BuddyInfo
/*      */ {
/*      */   public String m_fieldName;
/*      */   public String m_parent;
/*      */   public Properties m_ruleProps;
/*      */ 
/*      */   BuddyInfo()
/*      */   {
/* 1454 */     this.m_fieldName = null;
/* 1455 */     this.m_parent = null;
/* 1456 */     this.m_ruleProps = null;
/*      */   }
/*      */ 
/*      */   public boolean equals(Object obj)
/*      */   {
/* 1461 */     BuddyInfo info = (BuddyInfo)obj;
/* 1462 */     return this.m_fieldName.equals(info.m_fieldName);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1467 */     return "releaseInfo=dev,releaseRevision=$Rev: 98902 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.BuddyInfo
 * JD-Core Version:    0.5.4
 */