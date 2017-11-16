/*    */ package intradoc.apputilities.componentwizard;
/*    */ 
/*    */ import intradoc.common.DynamicHtml;
/*    */ import intradoc.common.IdcCharArrayWriter;
/*    */ import intradoc.common.ParseOutput;
/*    */ import intradoc.common.ParseSyntaxException;
/*    */ import java.io.IOException;
/*    */ import java.io.Reader;
/*    */ 
/*    */ public class CWDynamicHtml extends DynamicHtml
/*    */ {
/*    */   public void loadHtmlInContextWithoutParsing(Reader reader, ParseOutput parseOutput)
/*    */     throws IOException, ParseSyntaxException
/*    */   {
/* 42 */     IdcCharArrayWriter outbuf = new IdcCharArrayWriter();
/*    */     try
/*    */     {
/* 45 */       parseOutput.m_writer = outbuf;
/*    */ 
/* 47 */       char[] temp = parseOutput.m_outputBuf;
/*    */       while (true)
/*    */       {
/* 51 */         int offset = parseOutput.m_readOffset;
/*    */ 
/* 53 */         int numRead = parseOutput.m_numRead;
/* 54 */         if ((numRead < temp.length / 2) && (reader.ready()))
/*    */         {
/* 56 */           numRead += reader.read(temp, numRead + offset, temp.length - numRead - offset);
/*    */ 
/* 58 */           parseOutput.m_numRead = numRead;
/*    */         }
/*    */ 
/* 61 */         if (numRead <= 0) {
/*    */           break;
/*    */         }
/* 64 */         parseOutput.m_numWaiting = numRead;
/*    */ 
/* 67 */         parseOutput.copyToPending(true, false);
/*    */       }
/*    */ 
/* 70 */       addChunks(reader, outbuf, parseOutput, null);
/*    */     }
/*    */     finally
/*    */     {
/* 74 */       outbuf.releaseBuffers();
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 80 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.CWDynamicHtml
 * JD-Core Version:    0.5.4
 */