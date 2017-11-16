/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.NumberUtils;
/*    */ import java.util.Hashtable;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class MonikerList
/*    */ {
/* 31 */   public String m_location = null;
/* 32 */   public Hashtable m_monikerMap = null;
/*    */ 
/*    */   public MonikerList(String loc)
/*    */   {
/* 37 */     this.m_location = loc;
/* 38 */     this.m_monikerMap = new Hashtable();
/*    */   }
/*    */ 
/*    */   public void addAndUpdateList(MonikerInfo info)
/*    */   {
/* 43 */     String moniker = info.m_moniker;
/* 44 */     this.m_monikerMap.put(moniker, info);
/*    */   }
/*    */ 
/*    */   public void mergeInto(Vector monikers)
/*    */   {
/* 49 */     int num = monikers.size();
/* 50 */     for (int i = 0; i < num; i += 2)
/*    */     {
/* 52 */       MonikerInfo monikerInfo = new MonikerInfo();
/* 53 */       monikerInfo.m_moniker = ((String)monikers.elementAt(i));
/* 54 */       monikerInfo.m_counter = NumberUtils.parseLong((String)monikers.elementAt(i + 1), -2L);
/*    */ 
/* 57 */       addAndUpdateList(monikerInfo);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void removeFromList(String moniker)
/*    */   {
/* 63 */     this.m_monikerMap.remove(moniker);
/*    */   }
/*    */ 
/*    */   public MonikerInfo getMonikerInfo(String moniker)
/*    */   {
/* 68 */     return (MonikerInfo)this.m_monikerMap.get(moniker);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 73 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.MonikerList
 * JD-Core Version:    0.5.4
 */