/*    */ package intradoc.search;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.data.DataBinder;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class CommonSearchAdaptor
/*    */   implements SearchImplementor
/*    */ {
/*    */   public CommonSearchConnection m_searchConnection;
/*    */   public ExecutionContext m_context;
/*    */ 
/*    */   public void init(CommonSearchConnection sc)
/*    */   {
/* 33 */     this.m_searchConnection = sc;
/*    */   }
/*    */ 
/*    */   public boolean prepareUse(ExecutionContext ctxt) {
/* 37 */     this.m_context = ctxt;
/* 38 */     return true;
/*    */   }
/*    */ 
/*    */   public boolean initCollection(Vector collection) {
/* 42 */     return true;
/*    */   }
/*    */ 
/*    */   public String doQuery(DataBinder binder) {
/* 46 */     return null;
/*    */   }
/*    */ 
/*    */   public String retrieveHighlightInfo(DataBinder binder, int hlType, String hlBegin, String hlEnd)
/*    */   {
/* 52 */     return CommonSearchConnection.createErrorMsg(null, "csSearchRetrieveHighlightInfoNotSupported");
/*    */   }
/*    */ 
/*    */   public String viewDoc(DataBinder binder, int viewType) {
/* 56 */     return CommonSearchConnection.createErrorMsg(null, "csSearchViewDocNotSupported");
/*    */   }
/*    */ 
/*    */   public String getResult()
/*    */   {
/* 62 */     return null;
/*    */   }
/*    */ 
/*    */   public DataBinder getResultAsBinder()
/*    */   {
/* 67 */     return null;
/*    */   }
/*    */ 
/*    */   public String retrieveDocInfo(String docKey, String fields, int numFields)
/*    */   {
/* 72 */     return CommonSearchConnection.createErrorMsg(null, "csSearchRetrieveDocInfoNotSupported");
/*    */   }
/*    */ 
/*    */   public void closeSession()
/*    */   {
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 82 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86052 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.CommonSearchAdaptor
 * JD-Core Version:    0.5.4
 */