/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcReleasable;
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class FixedFieldFormatter
/*     */   implements IdcReleasable
/*     */ {
/*     */   public byte[] m_data;
/*     */   public int m_offset;
/*     */   public int m_len;
/*     */   public IdcStringBuilder m_tempBuf;
/*     */   protected String m_strData;
/*     */ 
/*     */   public FixedFieldFormatter(int dataLen)
/*     */   {
/*  54 */     this.m_data = new byte[dataLen];
/*  55 */     this.m_tempBuf = null;
/*  56 */     resetForRead();
/*     */   }
/*     */ 
/*     */   public void resetForRead()
/*     */   {
/*  61 */     this.m_strData = null;
/*     */   }
/*     */ 
/*     */   public int ensureCapacityTempBuf()
/*     */   {
/*  70 */     int curLen = 0;
/*  71 */     if (this.m_tempBuf != null)
/*     */     {
/*  73 */       curLen = this.m_tempBuf.length();
/*     */     }
/*  75 */     if ((this.m_tempBuf == null) || (curLen + this.m_len > 1024))
/*     */     {
/*  77 */       int newCapacity = 1024;
/*  78 */       if (this.m_len > newCapacity)
/*     */       {
/*  80 */         newCapacity = this.m_len;
/*     */       }
/*  82 */       this.m_tempBuf = new IdcStringBuilder(newCapacity);
/*  83 */       curLen = 0;
/*     */     }
/*  85 */     return curLen;
/*     */   }
/*     */ 
/*     */   public String getStringData()
/*     */   {
/*  90 */     if (this.m_strData == null)
/*     */     {
/*  92 */       this.m_strData = new String(this.m_data, 0, this.m_data.length);
/*     */     }
/*  94 */     return this.m_strData;
/*     */   }
/*     */ 
/*     */   public void formatStringBuf(int bufStart)
/*     */   {
/* 100 */     int curoffset = this.m_offset;
/* 101 */     int bufOffset = bufStart;
/* 102 */     int bufLen = this.m_tempBuf.length() - bufStart;
/*     */ 
/* 104 */     for (int i = 0; i < this.m_len; ++i)
/*     */     {
/* 106 */       if (i < bufLen)
/*     */       {
/* 108 */         this.m_data[(curoffset++)] = (byte)this.m_tempBuf.charAt(bufOffset++);
/*     */       }
/*     */       else
/*     */       {
/* 112 */         this.m_data[(curoffset++)] = 32;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 117 */     this.m_offset += this.m_len;
/*     */   }
/*     */ 
/*     */   public void insertCarriageReturn(boolean isWrite)
/*     */     throws IOException
/*     */   {
/* 126 */     if (isWrite)
/*     */     {
/* 128 */       this.m_data[this.m_offset] = 10;
/*     */     }
/* 132 */     else if (this.m_data[this.m_offset] != 10)
/*     */     {
/* 134 */       throw new IOException("!syFormatterStructureMismatch");
/*     */     }
/*     */ 
/* 137 */     this.m_offset += 1;
/*     */   }
/*     */ 
/*     */   public void formatString(String val)
/*     */   {
/* 146 */     int bufStart = ensureCapacityTempBuf();
/* 147 */     if (val != null)
/*     */     {
/* 149 */       this.m_tempBuf.append(val);
/*     */     }
/* 151 */     formatStringBuf(bufStart);
/*     */   }
/*     */ 
/*     */   public String parseString()
/*     */   {
/* 156 */     return parseStringEx(true);
/*     */   }
/*     */ 
/*     */   public String parseStringEx(boolean isTrimmed)
/*     */   {
/* 161 */     String strData = getStringData();
/* 162 */     String temp = strData.substring(this.m_offset, this.m_offset + this.m_len);
/*     */ 
/* 165 */     this.m_offset += this.m_len;
/*     */ 
/* 168 */     if (!isTrimmed)
/*     */     {
/* 170 */       return temp;
/*     */     }
/* 172 */     return temp.trim();
/*     */   }
/*     */ 
/*     */   public void formatInt(int val)
/*     */   {
/* 177 */     int bufStart = ensureCapacityTempBuf();
/* 178 */     this.m_tempBuf.append(val);
/*     */ 
/* 180 */     formatStringBuf(bufStart);
/*     */   }
/*     */ 
/*     */   public int parseInt() throws IOException
/*     */   {
/* 185 */     String temp = parseStringEx(true);
/*     */     try
/*     */     {
/* 188 */       if (temp.length() == 0)
/*     */       {
/* 190 */         return 0;
/*     */       }
/* 192 */       return Integer.parseInt(temp);
/*     */     }
/*     */     catch (NumberFormatException e)
/*     */     {
/* 196 */       throw new IOException(LocaleUtils.encodeMessage("syFormatterIntegerParseError", null, temp));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void formatLong(long val)
/*     */   {
/* 204 */     int bufStart = ensureCapacityTempBuf();
/* 205 */     this.m_tempBuf.append("" + val);
/*     */ 
/* 207 */     formatStringBuf(bufStart);
/*     */   }
/*     */ 
/*     */   public long parseLong() throws IOException
/*     */   {
/* 212 */     String temp = parseStringEx(true);
/*     */     try
/*     */     {
/* 215 */       if (temp.length() == 0)
/*     */       {
/* 217 */         return 0L;
/*     */       }
/* 219 */       return Long.parseLong(temp);
/*     */     }
/*     */     catch (NumberFormatException e)
/*     */     {
/* 223 */       throw new IOException(LocaleUtils.encodeMessage("syFormatterLongIntegerParseError", null, temp));
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean parseBoolean()
/*     */     throws IOException
/*     */   {
/* 232 */     String temp = parseStringEx(true);
/* 233 */     if (temp.length() == 0)
/*     */     {
/* 235 */       return false;
/*     */     }
/* 237 */     char ch = temp.charAt(0);
/* 238 */     ch = Character.toUpperCase(ch);
/* 239 */     return (ch == '1') || (ch == 'T');
/*     */   }
/*     */ 
/*     */   public void formatBoolean(boolean val)
/*     */   {
/* 244 */     char ch = (val) ? 'T' : 'F';
/* 245 */     int curoffset = this.m_offset;
/*     */ 
/* 247 */     for (int i = 0; i < this.m_len; ++i)
/*     */     {
/* 249 */       if (i == 0)
/*     */       {
/* 251 */         this.m_data[(curoffset++)] = (byte)ch;
/*     */       }
/*     */       else
/*     */       {
/* 255 */         this.m_data[(curoffset++)] = 32;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 260 */     this.m_offset += this.m_len;
/*     */   }
/*     */ 
/*     */   public void spaceFill(int len, boolean isWrite)
/*     */   {
/* 267 */     if (isWrite)
/*     */     {
/* 269 */       int tempOffset = this.m_offset;
/* 270 */       for (int i = 0; i < len; ++i)
/*     */       {
/* 272 */         int type = i % 100;
/* 273 */         byte b = 32;
/*     */ 
/* 276 */         if (type == 98)
/*     */         {
/* 278 */           b = 38;
/*     */         }
/* 280 */         else if (type == 99)
/*     */         {
/* 282 */           b = 10;
/*     */         }
/* 284 */         this.m_data[(tempOffset++)] = b;
/*     */       }
/*     */     }
/* 287 */     this.m_offset += len;
/*     */   }
/*     */ 
/*     */   public Object release()
/*     */   {
/* 292 */     if (this.m_tempBuf != null)
/*     */     {
/* 294 */       this.m_tempBuf.release();
/* 295 */       this.m_tempBuf = null;
/*     */     }
/* 297 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 302 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92369 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FixedFieldFormatter
 * JD-Core Version:    0.5.4
 */