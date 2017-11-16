/*    */ package intradoc.indexer;
/*    */ 
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.ResultSetUtils;
/*    */ import intradoc.shared.ActiveIndexState;
/*    */ import intradoc.util.IdcVector;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class CollectionHandlerUtils
/*    */ {
/* 29 */   static Vector m_ids = null;
/*    */ 
/*    */   public static void init(DataResultSet collections) throws ServiceException
/*    */   {
/* 33 */     if (m_ids != null)
/*    */       return;
/*    */     try
/*    */     {
/* 37 */       m_ids = ResultSetUtils.loadValuesFromSet(collections, "CollectionID");
/*    */     }
/*    */     catch (DataException e)
/*    */     {
/* 41 */       String msg = LocaleUtils.encodeMessage("csIndexerUnableToLoadCollectionID", null);
/* 42 */       throw new ServiceException(msg, e);
/*    */     }
/*    */ 
/* 46 */     if (m_ids.size() == 2)
/*    */       return;
/* 48 */     String msg = LocaleUtils.encodeMessage("csIndexerErrorOnCollectionIDTable", null, "" + m_ids.size());
/* 49 */     throw new ServiceException(msg);
/*    */   }
/*    */ 
/*    */   public static String getActiveIndex(boolean isRebuild, DataResultSet collections)
/*    */     throws ServiceException
/*    */   {
/* 55 */     String activeIndex = ActiveIndexState.getActiveProperty("ActiveIndex");
/*    */ 
/* 57 */     if (isRebuild)
/*    */     {
/* 60 */       Vector ids = new IdcVector();
/*    */       try
/*    */       {
/* 63 */         ids = ResultSetUtils.loadValuesFromSet(collections, "IndexerLabel");
/*    */       }
/*    */       catch (DataException e)
/*    */       {
/* 67 */         String msg = LocaleUtils.encodeMessage("csIndexerUnableToLoadCollectionID", null);
/* 68 */         throw new ServiceException(msg, e);
/*    */       }
/*    */ 
/* 71 */       if (ids.size() != 2)
/*    */       {
/* 73 */         String msg = LocaleUtils.encodeMessage("csIndexerErrorOnCollectionIDTable", null, "" + ids.size());
/* 74 */         throw new ServiceException(msg);
/*    */       }
/* 76 */       String id = (String)ids.elementAt(0);
/* 77 */       if (id.equals(activeIndex))
/*    */       {
/* 79 */         activeIndex = (String)ids.elementAt(1);
/*    */       }
/*    */       else
/*    */       {
/* 83 */         activeIndex = id;
/*    */       }
/*    */     }
/* 86 */     return activeIndex;
/*    */   }
/*    */ 
/*    */   public static String getCollectionID(IndexerWorkObject data)
/*    */   {
/* 91 */     return data.getEnvironmentValue("IDC_Name");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 96 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82413 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.CollectionHandlerUtils
 * JD-Core Version:    0.5.4
 */