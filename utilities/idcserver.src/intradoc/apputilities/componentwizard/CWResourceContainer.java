/*    */ package intradoc.apputilities.componentwizard;
/*    */ 
/*    */ import intradoc.common.DynamicHtml;
/*    */ import intradoc.common.IdcBreakpoints;
/*    */ import intradoc.common.ParseLocationInfo;
/*    */ import intradoc.common.ParseOutput;
/*    */ import intradoc.common.ParseSyntaxException;
/*    */ import intradoc.common.ResourceContainer;
/*    */ import intradoc.common.ResourceOptions;
/*    */ import java.io.IOException;
/*    */ import java.io.Reader;
/*    */ import java.util.Hashtable;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class CWResourceContainer extends ResourceContainer
/*    */ {
/* 40 */   public Map m_dynamicData = new Hashtable();
/*    */ 
/*    */   protected void addResourceEx(String resName, Reader resReader, int curResource, ResourceOptions resOptions, ParseLocationInfo parseInfo, IdcBreakpoints bp)
/*    */     throws IOException, ParseSyntaxException
/*    */   {
/* 52 */     switch (curResource)
/*    */     {
/*    */     case 2:
/*    */     case 3:
/* 56 */       Object res = parseDynamicHtml(resReader, parseInfo);
/* 57 */       boolean isHtml = curResource == 2;
/* 58 */       Map hTb = (isHtml) ? this.m_dynamicHtml : this.m_dynamicData;
/* 59 */       List list = (isHtml) ? this.m_dynamicHtmlList : this.m_dynamicDataList;
/* 60 */       if (list != null)
/*    */       {
/* 62 */         list.add(resName);
/*    */       }
/* 64 */       if (hTb == null)
/*    */         return;
/* 66 */       hTb.put(resName, res); break;
/*    */     default:
/* 71 */       super.addResourceEx(resName, resReader, curResource, resOptions, parseInfo, bp);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static DynamicHtml parseDynamicHtml(Reader reader, ParseLocationInfo parseInfo)
/*    */     throws IOException, ParseSyntaxException
/*    */   {
/* 79 */     CWDynamicHtml dynHtml = new CWDynamicHtml();
/* 80 */     ParseOutput parseOutput = new ParseOutput(parseInfo);
/* 81 */     dynHtml.loadHtmlInContextWithoutParsing(reader, parseOutput);
/* 82 */     parseOutput.releaseBuffers();
/* 83 */     return dynHtml;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 88 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.CWResourceContainer
 * JD-Core Version:    0.5.4
 */