/*     */ package intradoc.common;
/*     */ 
/*     */ public class CommonDataConversion
/*     */ {
/*  39 */   protected static final byte[] PR2SIX = { 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 62, 64, 64, 64, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 64, 64, 64, 64, 64, 64, 64, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 64, 64, 64, 64, 64, 64, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64 };
/*     */ 
/*  54 */   protected static final char[] SIX2PR = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
/*     */ 
/*     */   public static byte[] uudecode(String strcoded, int[] len)
/*     */   {
/*  65 */     return uudecodeSequence(strcoded, 0, strcoded.length(), len);
/*     */   }
/*     */ 
/*     */   public static byte[] uudecodeSequence(CharSequence strcoded, int off, int seqLen, int[] len)
/*     */   {
/*  70 */     int startoffset = 0;
/*  71 */     int index = 0;
/*     */ 
/*  74 */     int[] bufcoded = new int[seqLen];
/*  75 */     for (int i = 0; i < bufcoded.length; ++i)
/*     */     {
/*  77 */       bufcoded[i] = (strcoded.charAt(i + off) & 0xFF);
/*     */     }
/*     */ 
/*  81 */     while (startoffset < bufcoded.length)
/*     */     {
/*  83 */       int b = bufcoded[startoffset];
/*  84 */       if ((b != 32) && (b != 9) && (b != 10))
/*     */         break;
/*  86 */       ++startoffset;
/*     */     }
/*     */ 
/*  95 */     index = startoffset;
/*  96 */     while (index < bufcoded.length)
/*     */     {
/*  98 */       if (PR2SIX[bufcoded[index]] > 63) {
/*     */         break;
/*     */       }
/*     */ 
/* 102 */       ++index;
/*     */     }
/* 104 */     int nbytes = index - startoffset;
/* 105 */     int nbytesdecoded = (nbytes + 3) / 4 * 3;
/* 106 */     byte[] bufdecoded = new byte[nbytesdecoded];
/* 107 */     int maxdecodelen = bufcoded.length - 3;
/* 108 */     int outindex = 0;
/* 109 */     index = 0;
/*     */ 
/* 111 */     while ((index < maxdecodelen) && (nbytes > 0))
/*     */     {
/* 113 */       int b1 = PR2SIX[bufcoded[index]];
/* 114 */       int b2 = PR2SIX[bufcoded[(index + 1)]];
/* 115 */       int b3 = PR2SIX[bufcoded[(index + 2)]];
/* 116 */       int b4 = PR2SIX[bufcoded[(index + 3)]];
/*     */ 
/* 118 */       bufdecoded[(outindex++)] = (byte)(b1 << 2 | b2 >> 4);
/* 119 */       bufdecoded[(outindex++)] = (byte)(b2 << 4 | b3 >> 2);
/* 120 */       bufdecoded[(outindex++)] = (byte)(b3 << 6 | b4);
/* 121 */       index += 4;
/* 122 */       nbytes -= 4;
/*     */     }
/*     */ 
/* 125 */     if (((nbytes & 0x3) != 0) && (index > 1))
/*     */     {
/* 127 */       int firstTrailByte = PR2SIX[bufcoded[(index - 2)]];
/* 128 */       if (firstTrailByte > 63)
/* 129 */         nbytesdecoded -= 2;
/*     */       else
/* 131 */         --nbytesdecoded;
/*     */     }
/* 133 */     if (len != null)
/*     */     {
/* 135 */       len[0] = nbytesdecoded;
/*     */     }
/* 137 */     return bufdecoded;
/*     */   }
/*     */ 
/*     */   public static String uuencode(byte[] bufin, int offset, int len)
/*     */   {
/* 147 */     IdcStringBuilder bufEncoded = new IdcStringBuilder(len + (len + 3) / 3);
/*     */ 
/* 149 */     int index = offset;
/* 150 */     int blen = len;
/*     */ 
/* 152 */     for (int i = 0; i < len; i += 3)
/*     */     {
/* 154 */       int b1 = (index < blen) ? getUnsigned(bufin[index]) : 0;
/* 155 */       ++index;
/* 156 */       int b2 = (index < blen) ? getUnsigned(bufin[index]) : 0;
/* 157 */       ++index;
/* 158 */       int b3 = (index < blen) ? getUnsigned(bufin[index]) : 0;
/* 159 */       ++index;
/*     */ 
/* 161 */       bufEncoded.append(SIX2PR[(b1 >> 2)]);
/* 162 */       bufEncoded.append(SIX2PR[(b1 << 4 & 0x30 | b2 >> 4 & 0xF)]);
/* 163 */       bufEncoded.append(SIX2PR[(b2 << 2 & 0x3C | b3 >> 6 & 0x3)]);
/* 164 */       bufEncoded.append(SIX2PR[(b3 & 0x3F)]);
/*     */     }
/*     */ 
/* 170 */     int bufLen = bufEncoded.length();
/* 171 */     if (i == len + 1)
/*     */     {
/* 174 */       bufEncoded.setCharAt(bufLen - 1, '=');
/*     */     }
/* 176 */     else if (i == len + 2)
/*     */     {
/* 179 */       bufEncoded.setCharAt(bufLen - 1, '=');
/* 180 */       bufEncoded.setCharAt(bufLen - 2, '=');
/*     */     }
/* 182 */     return bufEncoded.toString();
/*     */   }
/*     */ 
/*     */   static int getUnsigned(byte b)
/*     */   {
/* 187 */     return (b >= 0) ? b : b & 0x7F | 0x80;
/*     */   }
/*     */ 
/*     */   public static String quotEncode(byte[] input, boolean restricted)
/*     */   {
/*     */     char[] okayRanges;
/*     */     char[] okayRanges;
/* 194 */     if (restricted)
/*     */     {
/* 196 */       okayRanges = new char[] { 'a', 'z', 'A', 'Z', '0', '9', '!', '!', '*', '*', '+', '+', '-', '-', '/', '/', '=', '=' };
/*     */     }
/*     */     else
/*     */     {
/* 203 */       okayRanges = new char[] { '!', '<', '>', '>', '@', '^', '`', '~' };
/*     */     }
/*     */ 
/* 209 */     StringBuffer buf = new StringBuffer();
/* 210 */     int l = okayRanges.length / 2;
/* 211 */     for (int i = 0; i < input.length; ++i)
/*     */     {
/* 213 */       boolean done = false;
/* 214 */       byte b = input[i];
/* 215 */       for (int j = 0; j < l; ++j)
/*     */       {
/* 217 */         if ((okayRanges[(2 * j + 1)] < b) || (b < okayRanges[(2 * j)]))
/*     */           continue;
/* 219 */         done = true;
/* 220 */         buf.append((char)b);
/* 221 */         break;
/*     */       }
/*     */ 
/* 224 */       if (done)
/*     */         continue;
/* 226 */       if (b == 32)
/*     */       {
/* 228 */         buf.append('_');
/*     */       }
/*     */       else
/*     */       {
/* 232 */         char c = (char)(getUnsigned(b) / 16);
/* 233 */         if (c >= '\n')
/*     */         {
/* 235 */           c = (char)(c + '7');
/*     */         }
/*     */         else
/*     */         {
/* 239 */           c = (char)(c + '0');
/*     */         }
/* 241 */         buf.append('=');
/* 242 */         buf.append(c);
/* 243 */         c = (char)(getUnsigned(b) % 16);
/* 244 */         if (c >= '\n')
/*     */         {
/* 246 */           c = (char)(c + '7');
/*     */         }
/*     */         else
/*     */         {
/* 250 */           c = (char)(c + '0');
/*     */         }
/* 252 */         buf.append(c);
/*     */       }
/*     */     }
/*     */ 
/* 256 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 261 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93122 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.CommonDataConversion
 * JD-Core Version:    0.5.4
 */