/*    */ package intradoc.apps.shared;
/*    */ 
/*    */ import intradoc.common.StringUtils;
/*    */ import intradoc.util.IdcVector;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class AppInfo
/*    */ {
/*    */   public String m_appName;
/*    */   public String m_className;
/*    */   public String m_title;
/*    */   public String[] m_subjects;
/*    */   public String[] m_topics;
/*    */   public String m_gifName;
/*    */   public String m_statusReporter;
/*    */   public int m_appRights;
/*    */ 
/*    */   AppInfo(String[] info)
/*    */   {
/* 44 */     this.m_appName = info[0];
/* 45 */     this.m_className = info[1];
/* 46 */     this.m_title = info[2];
/*    */ 
/* 49 */     Vector vTemp = StringUtils.parseArray(info[3], ',', ',');
/* 50 */     Vector subjects = new IdcVector();
/* 51 */     Vector topics = new IdcVector();
/* 52 */     int size = vTemp.size();
/* 53 */     for (int i = 0; i < size; ++i)
/*    */     {
/* 55 */       String str = (String)vTemp.elementAt(i);
/* 56 */       if (str.startsWith("topic:"))
/*    */       {
/* 58 */         str = str.substring(6);
/* 59 */         topics.addElement(str);
/*    */       }
/*    */       else
/*    */       {
/* 63 */         subjects.addElement(str);
/*    */       }
/*    */     }
/* 66 */     this.m_subjects = StringUtils.convertListToArray(subjects);
/* 67 */     this.m_topics = StringUtils.convertListToArray(topics);
/*    */ 
/* 69 */     this.m_gifName = info[4];
/* 70 */     this.m_statusReporter = info[5];
/* 71 */     this.m_appRights = 0;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 76 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.AppInfo
 * JD-Core Version:    0.5.4
 */