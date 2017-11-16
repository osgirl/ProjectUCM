/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.shared.TopicInfo;
/*    */ import intradoc.shared.UserData;
/*    */ import intradoc.shared.UserUtils;
/*    */ 
/*    */ public class UserProfileUtils
/*    */ {
/*    */   public static TopicInfo getTopicInfo(ExecutionContext cxt, String topic)
/*    */   {
/* 37 */     TopicInfo topicInfo = null;
/* 38 */     UserData userData = (UserData)cxt.getCachedObject("UserData");
/* 39 */     if (userData != null)
/*    */     {
/* 41 */       String topicCacheKey = "UserProfileTopic-" + userData.m_name + ":" + topic;
/* 42 */       topicInfo = (TopicInfo)cxt.getCachedObject(topicCacheKey);
/* 43 */       if (topicInfo == null)
/*    */       {
/*    */         try
/*    */         {
/* 47 */           UserProfileManager manager = new UserProfileManager(userData, null, cxt);
/* 48 */           manager.init();
/* 49 */           UserProfileEditor upEditor = manager.getProfileEditor();
/*    */ 
/* 51 */           topicInfo = upEditor.loadTopicInfo(topic);
/* 52 */           cxt.setCachedObject(topicCacheKey, topicInfo);
/*    */         }
/*    */         catch (ServiceException e)
/*    */         {
/* 56 */           Report.trace(null, "Unable to load topic info " + topic + " for user " + userData.m_name + ".", e);
/*    */         }
/*    */       }
/*    */     }
/*    */ 
/* 61 */     return topicInfo;
/*    */   }
/*    */ 
/*    */   @Deprecated
/*    */   public static void getOrCreateCachedProfile(UserData userData, String key)
/*    */   {
/* 70 */     UserUtils.getOrCreateCachedProfile(userData, key);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 75 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80513 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserProfileUtils
 * JD-Core Version:    0.5.4
 */