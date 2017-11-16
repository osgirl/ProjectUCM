/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class InputStreamOutputThread extends Thread
/*     */ {
/*     */   protected InputStream m_in;
/*     */   protected Reader m_reader;
/*  31 */   protected Vector m_captureVector = null;
/*     */   protected OutputStream m_out;
/*     */   protected Writer m_writer;
/*     */ 
/*     */   public InputStreamOutputThread(InputStream in)
/*     */   {
/*  37 */     this.m_in = in;
/*     */   }
/*     */ 
/*     */   public InputStreamOutputThread(InputStream in, OutputStream out)
/*     */   {
/*  42 */     this.m_in = in;
/*  43 */     this.m_out = out;
/*     */   }
/*     */ 
/*     */   public void setOutput(OutputStream out)
/*     */   {
/*  48 */     this.m_out = out;
/*     */   }
/*     */ 
/*     */   public InputStreamOutputThread(Reader r)
/*     */   {
/*  53 */     this.m_reader = r;
/*     */   }
/*     */ 
/*     */   public InputStreamOutputThread(Reader r, Writer w)
/*     */   {
/*  58 */     this.m_reader = r;
/*  59 */     this.m_writer = w;
/*     */   }
/*     */ 
/*     */   public void setOutput(Writer w)
/*     */   {
/*  64 */     this.m_writer = w;
/*     */   }
/*     */ 
/*     */   public void captureOutput(Vector output)
/*     */   {
/*  69 */     this.m_captureVector = output;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     try
/*     */     {
/*  78 */       if (this.m_captureVector == null)
/*     */       {
/*  80 */         if (this.m_reader == null)
/*     */         {
/*  82 */           byte[] buf = new byte[16384];
/*  83 */           while ((count = this.m_in.read(buf, 0, buf.length)) > 0)
/*     */           {
/*     */             int count;
/*  85 */             if (this.m_out == null)
/*     */               continue;
/*  87 */             this.m_out.write(buf, 0, count);
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/*  93 */           char[] buf = new char[8192];
/*  94 */           while ((count = this.m_reader.read(buf, 0, buf.length)) > 0)
/*     */           {
/*     */             int count;
/*  96 */             if (this.m_writer == null)
/*     */               continue;
/*  98 */             this.m_writer.write(buf, 0, count);
/*     */           }
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/*     */         BufferedReader reader;
/*     */         BufferedReader reader;
/* 106 */         if (this.m_reader == null)
/*     */         {
/* 108 */           reader = new BufferedReader(new InputStreamReader(this.m_in));
/*     */         }
/*     */         else
/*     */         {
/* 112 */           reader = new BufferedReader(this.m_reader);
/*     */         }
/*     */ 
/* 115 */         while ((line = reader.readLine()) != null)
/*     */         {
/*     */           String line;
/* 117 */           this.m_captureVector.addElement(line);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 123 */       Report.trace(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 129 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.InputStreamOutputThread
 * JD-Core Version:    0.5.4
 */