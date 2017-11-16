/*     */ package intradoc.common.filter;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.util.MapUtils;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class OutputStreamTriggerWrapper extends OutputStream
/*     */ {
/*     */   public int m_flags;
/*     */   public byte[] m_snifferBuffer;
/*     */   public int m_start;
/*     */   public int m_len;
/*     */   public Map m_callbackIn;
/*     */   public Map m_callbackOut;
/*     */   public byte[] m_triggerBytes;
/*     */   public int m_triggerBytesIndex;
/*     */   public OutputStream m_out;
/*     */   public boolean m_closeInternalOutstream;
/*     */   public ParsedTriggerCallback m_callback;
/*     */   public boolean m_stoppedSniffing;
/*     */   public boolean m_stoppedOutput;
/*     */ 
/*     */   public OutputStreamTriggerWrapper(byte[] triggerBytes, ParsedTriggerCallback callback, Map callbackIn)
/*     */   {
/* 112 */     this.m_triggerBytes = triggerBytes;
/* 113 */     this.m_callback = callback;
/* 114 */     this.m_callbackIn = callbackIn;
/* 115 */     this.m_callbackOut = new HashMap();
/*     */   }
/*     */ 
/*     */   public void write(int b)
/*     */     throws IOException
/*     */   {
/* 121 */     byte[] newb = new byte[1];
/* 122 */     newb[0] = (byte)b;
/* 123 */     write(newb, 0, 1);
/*     */   }
/*     */ 
/*     */   public void write(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/* 129 */     if ((b == null) || (b.length == 0) || (len <= 0))
/*     */     {
/* 131 */       return;
/*     */     }
/* 133 */     if ((this.m_stoppedSniffing) || (this.m_stoppedOutput))
/*     */     {
/* 135 */       if (!this.m_stoppedOutput)
/*     */       {
/* 137 */         checkOutputStream();
/* 138 */         this.m_out.write(b, off, len);
/*     */       }
/* 140 */       return;
/*     */     }
/* 142 */     if (this.m_snifferBuffer == null)
/*     */     {
/* 144 */       int size = MapUtils.getIntValueFromMap(this.m_callbackIn, "buffersize", 0);
/* 145 */       this.m_snifferBuffer = ((byte[])(byte[])FileUtils.createBufferForStreaming(size, 0));
/*     */     }
/* 147 */     int bIndex = off;
/* 148 */     int sniffIndex = this.m_start + this.m_len;
/*     */ 
/* 150 */     for (int i = 0; (i < len) && (bIndex < b.length) && (sniffIndex < this.m_snifferBuffer.length); )
/*     */     {
/* 153 */       this.m_snifferBuffer[(sniffIndex++)] = b[bIndex];
/* 154 */       if (this.m_triggerBytes != null)
/*     */       {
/* 156 */         if ((this.m_triggerBytesIndex < 0) || (this.m_triggerBytesIndex >= this.m_triggerBytes.length))
/*     */         {
/* 159 */           this.m_triggerBytesIndex = 0;
/*     */         }
/* 164 */         else if (b[bIndex] == this.m_triggerBytes[this.m_triggerBytesIndex])
/*     */         {
/* 166 */           this.m_triggerBytesIndex += 1;
/* 167 */           if (this.m_triggerBytesIndex >= this.m_triggerBytes.length)
/*     */           {
/* 169 */             int result = this.m_callback.foundTrigger(this.m_snifferBuffer, this.m_start, sniffIndex - this.m_start, this.m_callbackIn, this.m_callbackOut, this);
/*     */ 
/* 171 */             boolean restart = false;
/* 172 */             if (((result & 0x1) != 0) && (!this.m_stoppedOutput))
/*     */             {
/* 174 */               checkOutputStream();
/* 175 */               this.m_out.write(this.m_snifferBuffer, this.m_start, sniffIndex - this.m_start);
/* 176 */               restart = true;
/*     */             }
/* 178 */             if ((result & 0xA) != 0)
/*     */             {
/* 181 */               this.m_stoppedOutput = ((result & 0x8) != 0);
/*     */ 
/* 183 */               this.m_stoppedSniffing = true;
/* 184 */               if ((this.m_stoppedOutput) || (len - bIndex - 1 <= 0))
/*     */                 break;
/* 186 */               checkOutputStream();
/* 187 */               this.m_out.write(b, bIndex + 1, len - bIndex - 1); break;
/*     */             }
/*     */ 
/* 191 */             if ((result & 0x4) != 0)
/*     */             {
/* 193 */               restart = true;
/*     */             }
/* 195 */             if (restart)
/*     */             {
/* 197 */               this.m_start = 0;
/* 198 */               sniffIndex = 0;
/*     */             }
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 204 */           this.m_triggerBytesIndex = 0;
/*     */         }
/*     */       }
/* 151 */       ++bIndex; ++i;
/*     */     }
/*     */ 
/* 210 */     if ((sniffIndex >= this.m_snifferBuffer.length) && (!this.m_stoppedOutput))
/*     */     {
/* 213 */       checkOutputStream();
/* 214 */       this.m_out.write(this.m_snifferBuffer, this.m_start, sniffIndex - this.m_start);
/* 215 */       sniffIndex = 0;
/* 216 */       this.m_stoppedSniffing = true;
/*     */     }
/*     */ 
/* 219 */     if ((!this.m_stoppedSniffing) && (this.m_triggerBytes == null))
/*     */     {
/* 223 */       int result = this.m_callback.foundTrigger(this.m_snifferBuffer, this.m_start, sniffIndex - this.m_start, this.m_callbackIn, this.m_callbackOut, this);
/*     */ 
/* 225 */       boolean restart = false;
/* 226 */       if (((result & 0x1) != 0) && (!this.m_stoppedOutput))
/*     */       {
/* 228 */         checkOutputStream();
/* 229 */         this.m_out.write(this.m_snifferBuffer, this.m_start, sniffIndex - this.m_start);
/* 230 */         restart = true;
/*     */       }
/* 232 */       if ((result & 0x2) != 0)
/*     */       {
/* 234 */         if ((len - bIndex - 1 > 0) && (!this.m_stoppedOutput))
/*     */         {
/* 236 */           this.m_out.write(b, bIndex, len - bIndex);
/*     */         }
/* 238 */         this.m_stoppedSniffing = true;
/*     */       }
/* 240 */       if ((result & 0x4) != 0)
/*     */       {
/* 242 */         restart = true;
/*     */       }
/* 244 */       if (restart)
/*     */       {
/* 246 */         sniffIndex = 0;
/*     */       }
/*     */     }
/* 249 */     this.m_start = sniffIndex;
/*     */   }
/*     */ 
/*     */   public void checkOutputStream() throws IOException
/*     */   {
/* 254 */     if ((this.m_out != null) || (this.m_stoppedOutput))
/*     */       return;
/* 256 */     this.m_out = this.m_callback.getOutputStream(this.m_callbackIn, this.m_callbackOut, this);
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */     throws IOException
/*     */   {
/* 263 */     if (this.m_out == null)
/*     */       return;
/* 265 */     this.m_out.flush();
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 272 */     if ((this.m_out != null) && (this.m_closeInternalOutstream))
/*     */     {
/* 274 */       this.m_out.close();
/*     */     }
/* 276 */     FileUtils.releaseBufferForStreaming(this.m_snifferBuffer);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 281 */     return "releaseInfo=dev,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.filter.OutputStreamTriggerWrapper
 * JD-Core Version:    0.5.4
 */