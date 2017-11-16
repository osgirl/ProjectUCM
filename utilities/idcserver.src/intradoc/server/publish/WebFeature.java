/*    */ package intradoc.server.publish;
/*    */ 
/*    */ import intradoc.shared.Feature;
/*    */ import java.util.Set;
/*    */ 
/*    */ public class WebFeature extends Feature
/*    */ {
/*    */   public PublishedResourceContainer.Class m_class;
/*    */   public PublishedResourceContainer m_container;
/*    */   public Set<WebFeature> m_requiredSet;
/*    */ 
/*    */   public WebFeature(String featureString)
/*    */   {
/* 61 */     super(featureString);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 67 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75544 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.publish.WebFeature
 * JD-Core Version:    0.5.4
 */