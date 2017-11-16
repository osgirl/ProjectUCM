/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.common.Report;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ public class SearchOperatorParsedElement
/*    */ {
/*    */   public static final int LITERAL = 0;
/*    */   public static final int FIELD = 1;
/*    */   public static final int VALUE = 2;
/*    */   public String m_parsedElement;
/*    */   public List<SearchOperatorParsedElements.Action> m_actions;
/*    */   public int m_parsedElementType;
/*    */ 
/*    */   public void init(String segment, int type, List<SearchOperatorParsedElements.Action> actions)
/*    */   {
/* 36 */     this.m_parsedElement = segment;
/* 37 */     this.m_parsedElementType = type;
/* 38 */     this.m_actions = actions;
/*    */   }
/*    */ 
/*    */   public SearchOperatorParsedElement clone()
/*    */   {
/* 44 */     SearchOperatorParsedElement element = new SearchOperatorParsedElement();
/* 45 */     element.m_parsedElement = this.m_parsedElement;
/* 46 */     element.m_parsedElementType = this.m_parsedElementType;
/* 47 */     if (this.m_actions != null)
/*    */     {
/* 49 */       element.m_actions = new ArrayList();
/* 50 */       element.m_actions.addAll(this.m_actions);
/*    */ 
/* 52 */       if (Report.m_verbose)
/*    */       {
/* 54 */         String actionString = "";
/*    */ 
/* 56 */         for (int actionNo = 0; actionNo < this.m_actions.size(); ++actionNo)
/*    */         {
/* 58 */           actionString = actionString + "," + ((SearchOperatorParsedElements.Action)this.m_actions.get(actionNo)).getActionString();
/*    */         }
/*    */ 
/* 61 */         Report.trace("searchqueryparse", "Cloning with actionString " + actionString, null);
/*    */       }
/*    */     }
/*    */ 
/* 65 */     return element;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 70 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83592 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SearchOperatorParsedElement
 * JD-Core Version:    0.5.4
 */