/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class TracingOutputStream extends OutputStream
/*     */ {
/*  47 */   public byte[] m_pendingBytes = new byte[42];
/*     */ 
/*  49 */   protected String m_prefix = null;
/*  50 */   protected String m_section = null;
/*  51 */   protected int m_pendingByteCount = 0;
/*  52 */   protected boolean m_isHTTP = false;
/*  53 */   protected String m_headerPrefix = null;
/*     */   protected ByteArrayOutputStream m_byteOutputStream;
/* 116 */   protected int m_lastData = 0;
/*     */ 
/*     */   public TracingOutputStream(String prefix, String section)
/*     */   {
/*  58 */     this.m_prefix = prefix;
/*  59 */     this.m_section = section;
/*     */   }
/*     */ 
/*     */   public void setHttpMode(String headerPrefix)
/*     */   {
/*  64 */     this.m_isHTTP = true;
/*  65 */     if (headerPrefix == null)
/*     */     {
/*  67 */       this.m_headerPrefix = "";
/*     */     }
/*     */     else
/*     */     {
/*  71 */       this.m_headerPrefix = headerPrefix;
/*     */     }
/*  73 */     this.m_byteOutputStream = new ByteArrayOutputStream();
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/*  79 */     flush();
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */   {
/*  85 */     if (this.m_isHTTP)
/*     */     {
/*  87 */       byte[] bytes = this.m_byteOutputStream.toByteArray();
/*  88 */       int start = 0;
/*  89 */       for (int i = 1; i < bytes.length; ++i)
/*     */       {
/*  91 */         byte c = bytes[i];
/*  92 */         if ((c != 13) || (i - start <= 0))
/*     */           continue;
/*  94 */         Report.trace(this.m_section, this.m_headerPrefix + new String(bytes, start, i - start), null);
/*     */ 
/*  96 */         start = i + 1;
/*  97 */         if ((start >= bytes.length) || (bytes[start] != 10))
/*     */           continue;
/*  99 */         ++start;
/* 100 */         ++i;
/*     */       }
/*     */ 
/* 104 */       this.m_isHTTP = false;
/* 105 */       return;
/*     */     }
/*     */ 
/* 108 */     if (this.m_pendingByteCount <= 0) {
/*     */       return;
/*     */     }
/* 111 */     Report.trace(this.m_section, toString(), null);
/* 112 */     this.m_pendingByteCount = 0;
/*     */   }
/*     */ 
/*     */   public void write(int b)
/*     */   {
/* 121 */     if (this.m_isHTTP)
/*     */     {
/* 123 */       this.m_byteOutputStream.write(b);
/* 124 */       this.m_lastData <<= 8;
/* 125 */       this.m_lastData |= b;
/* 126 */       if (this.m_lastData == 218762506)
/*     */       {
/* 128 */         flush();
/*     */       }
/* 130 */       return;
/*     */     }
/* 132 */     this.m_pendingBytes[(this.m_pendingByteCount++)] = (byte)(b % 256);
/* 133 */     if (this.m_pendingByteCount != this.m_pendingBytes.length)
/*     */       return;
/* 135 */     flush();
/*     */   }
/*     */ 
/*     */   public void write(byte[] buf)
/*     */   {
/* 142 */     write(buf, 0, buf.length);
/*     */   }
/*     */ 
/*     */   public void write(byte[] buf, int offset, int length)
/*     */   {
/* 148 */     if (this.m_isHTTP)
/*     */     {
/* 150 */       for (int i = offset; i < length; ++i)
/*     */       {
/* 152 */         write(buf[i]);
/*     */       }
/* 154 */       return;
/*     */     }
/*     */ 
/* 157 */     int spaceLeft = this.m_pendingBytes.length - this.m_pendingByteCount;
/* 158 */     while (length > spaceLeft)
/*     */     {
/* 161 */       System.arraycopy(buf, offset, this.m_pendingBytes, this.m_pendingByteCount, spaceLeft);
/*     */ 
/* 163 */       this.m_pendingByteCount += spaceLeft;
/* 164 */       flush();
/* 165 */       length -= spaceLeft;
/* 166 */       offset += spaceLeft;
/* 167 */       spaceLeft = this.m_pendingBytes.length;
/*     */     }
/* 169 */     System.arraycopy(buf, offset, this.m_pendingBytes, this.m_pendingByteCount, length);
/*     */ 
/* 171 */     this.m_pendingByteCount += length;
/* 172 */     flush();
/*     */   }
/*     */ 
/*     */   public void finalize()
/*     */     throws Throwable
/*     */   {
/* 178 */     close();
/* 179 */     super.finalize();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 185 */     IdcStringBuilder asciiBuf = new IdcStringBuilder();
/*     */ 
/* 187 */     if (null != this.m_prefix)
/*     */     {
/* 189 */       asciiBuf.append(this.m_prefix);
/*     */     }
/*     */ 
/* 192 */     for (int i = 0; i < this.m_pendingBytes.length; ++i)
/*     */     {
/* 194 */       if (i < this.m_pendingByteCount)
/*     */       {
/* 196 */         char c = (char)this.m_pendingBytes[i];
/* 197 */         if ((c <= ' ') || (c >= '') || (c == '<') || (c == '>') || (c == '&'))
/*     */         {
/* 199 */           c = '.';
/*     */         }
/* 201 */         asciiBuf.append(c);
/*     */       }
/*     */       else
/*     */       {
/* 205 */         asciiBuf.append(' ');
/*     */       }
/*     */     }
/*     */ 
/* 209 */     asciiBuf.append("\t[");
/* 210 */     for (int i = 0; i < this.m_pendingBytes.length; ++i)
/*     */     {
/* 212 */       if (i < this.m_pendingByteCount)
/*     */       {
/* 214 */         if (i > 0)
/*     */         {
/* 216 */           asciiBuf.append(' ');
/*     */         }
/* 218 */         NumberUtils.appendHexByte(asciiBuf, this.m_pendingBytes[i]);
/*     */       }
/*     */       else
/*     */       {
/* 222 */         asciiBuf.append("   ");
/*     */       }
/*     */     }
/* 225 */     asciiBuf.append("]\t");
/*     */ 
/* 227 */     String results = CommonDataConversion.uuencode(this.m_pendingBytes, 0, this.m_pendingByteCount);
/*     */ 
/* 229 */     asciiBuf.append(results);
/* 230 */     return asciiBuf.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 235 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78391 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TracingOutputStream
 * JD-Core Version:    0.5.4
 */