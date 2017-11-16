/*     */ package intradoc.io;
/*     */ 
/*     */ public class IdcByteConversionUtils
/*     */ {
/*     */   private static char[] tableIBM437;
/*     */ 
/*     */   public static byte parseHexDigit(char digit)
/*     */   {
/*  34 */     if ((digit >= '0') && (digit <= '9'))
/*     */     {
/*  36 */       return (byte)(digit - '0');
/*     */     }
/*  38 */     if ((digit >= 'A') && (digit <= 'F'))
/*     */     {
/*  40 */       return (byte)(digit + '\n' - 65);
/*     */     }
/*  42 */     if ((digit >= 'a') && (digit <= 'f'))
/*     */     {
/*  44 */       return (byte)(digit + '\n' - 97);
/*     */     }
/*  46 */     return -1;
/*     */   }
/*     */ 
/*     */   public static int formatUTF8String(String string, IdcByteHandler bytes)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  62 */     char[] chars = string.toCharArray();
/*  63 */     return formatUTF8(chars, 0, chars.length, bytes);
/*     */   }
/*     */ 
/*     */   public static int formatUTF8(char[] chars, int charOffset, int charLength, IdcByteHandler bytes)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  79 */     byte[] byteArray = new byte[4];
/*  80 */     int totalBytes = 0;
/*  81 */     for (int i = 0; i < charLength; ++i)
/*     */     {
/*  83 */       int c = chars[(charOffset + i)];
/*     */       int numBytes;
/*     */       int numBytes;
/*  84 */       if (c <= 127)
/*     */       {
/*  86 */         if (null != bytes)
/*     */         {
/*  88 */           byteArray[0] = (byte)(c & 0x7F);
/*     */         }
/*  90 */         numBytes = 1;
/*     */       }
/*     */       else
/*     */       {
/*     */         int numBytes;
/*  92 */         if (c <= 2047)
/*     */         {
/*  94 */           if (null != bytes)
/*     */           {
/*  96 */             byteArray[0] = (byte)(c >> 6 | 0xC0);
/*  97 */             byteArray[1] = (byte)(c & 0x3F | 0x80);
/*     */           }
/*  99 */           numBytes = 2;
/*     */         } else {
/* 101 */           if ((c >= 56320) && (c < 57344))
/*     */           {
/* 103 */             throw new IdcByteHandlerException("syUTF16InitialSurrogateMissing", new Object[] { Integer.valueOf(c) });
/*     */           }
/*     */           int numBytes;
/* 105 */           if ((c >= 55296) && (c < 56320))
/*     */           {
/* 108 */             char c2 = '\000';
/* 109 */             if (++i < charLength)
/*     */             {
/* 111 */               c2 = chars[(charOffset + i)];
/*     */             }
/* 113 */             if ((c2 < 56320) || (c2 > 57344))
/*     */             {
/* 115 */               throw new IdcByteHandlerException("syUTF16SubsequentSurrogateMissing", new Object[] { Integer.valueOf(c) });
/*     */             }
/* 117 */             c &= 1023;
/* 118 */             c <<= 10;
/* 119 */             c += (c2 & 0x3FF) + 65536;
/*     */ 
/* 121 */             if (null != bytes)
/*     */             {
/* 123 */               byteArray[0] = (byte)(c >> 18 | 0xF0);
/* 124 */               byteArray[1] = (byte)(c >> 12 & 0x3F | 0x80);
/* 125 */               byteArray[2] = (byte)(c >> 6 & 0x3F | 0x80);
/* 126 */               byteArray[3] = (byte)(c & 0x3F | 0x80);
/*     */             }
/* 128 */             numBytes = 4;
/*     */           }
/*     */           else
/*     */           {
/* 133 */             if (null != bytes)
/*     */             {
/* 135 */               byteArray[0] = (byte)(c >> 12 & 0xF | 0xE0);
/* 136 */               byteArray[1] = (byte)(c >> 6 & 0x3F | 0x80);
/* 137 */               byteArray[2] = (byte)(c & 0x3F | 0x80);
/*     */             }
/* 139 */             numBytes = 3;
/*     */           }
/*     */         }
/*     */       }
/* 141 */       totalBytes += numBytes;
/* 142 */       if (null == bytes)
/*     */         continue;
/* 144 */       bytes.writeNext(byteArray, 0, numBytes);
/*     */     }
/*     */ 
/* 147 */     return totalBytes;
/*     */   }
/*     */ 
/*     */   public static int parseUTF8(byte[] bytes, int offset, int length, char[] chars, int charOffset)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 164 */     boolean byteError = false; boolean charError = false; boolean startError = false;
/* 165 */     int numChars = 0; int b = 0; int c = 0; int i = 0;
/* 166 */     while (i < length)
/*     */     {
/* 168 */       b = bytes[(offset + i++)];
/* 169 */       if ((b & 0x80) == 0)
/*     */       {
/* 171 */         c = b;
/*     */       } else {
/* 173 */         if ((b & 0xE0) == 192)
/*     */         {
/* 175 */           c = (b & 0x1F) << 6;
/* 176 */           b = bytes[(offset + i++)];
/* 177 */           if ((b & 0xC0) != 128)
/*     */           {
/* 179 */             byteError = true;
/* 180 */             break;
/*     */           }
/* 182 */           c |= b & 0x3F;
/* 183 */           if (c >= 128)
/*     */             break label476;
/* 185 */           charError = true;
/* 186 */           break;
/*     */         }
/*     */ 
/* 189 */         if ((b & 0xF0) == 224)
/*     */         {
/* 191 */           c = (b & 0xF) << 12;
/* 192 */           b = bytes[(offset + i++)];
/* 193 */           if ((b & 0xC0) != 128)
/*     */           {
/* 195 */             byteError = true;
/* 196 */             break;
/*     */           }
/* 198 */           c |= (b & 0x3F) << 6;
/* 199 */           b = bytes[(offset + i++)];
/* 200 */           if ((b & 0xC0) != 128)
/*     */           {
/* 202 */             byteError = true;
/* 203 */             break;
/*     */           }
/* 205 */           c |= b & 0x3F;
/* 206 */           if ((c >= 2048) && (((c < 55296) || (c >= 57344))))
/*     */             break label476;
/* 208 */           charError = true;
/* 209 */           break;
/*     */         }
/*     */ 
/* 212 */         if ((b & 0xF8) == 240)
/*     */         {
/* 214 */           c = (b & 0x7) << 18;
/* 215 */           b = bytes[(offset + i++)];
/* 216 */           if ((b & 0xC0) != 128)
/*     */           {
/* 218 */             byteError = true;
/* 219 */             break;
/*     */           }
/* 221 */           c |= (b & 0x3F) << 12;
/* 222 */           b = bytes[(offset + i++)];
/* 223 */           if ((b & 0xC0) != 128)
/*     */           {
/* 225 */             byteError = true;
/* 226 */             break;
/*     */           }
/* 228 */           c |= (b & 0x3F) << 6;
/* 229 */           b = bytes[(offset + i++)];
/* 230 */           if ((b & 0xC0) != 128)
/*     */           {
/* 232 */             byteError = true;
/* 233 */             break;
/*     */           }
/* 235 */           c |= b & 0x3F;
/* 236 */           if ((c < 65536) || (c > 1114111))
/*     */           {
/* 238 */             charError = true;
/* 239 */             break;
/*     */           }
/* 241 */           if (null != chars)
/*     */           {
/* 243 */             chars[(charOffset++)] = (char)(c - 65536 >> 10 & 0x7FF | 0xD800);
/* 244 */             c = c & 0x7FF | 0xDC00;
/*     */           }
/* 246 */           ++numChars;
/*     */         }
/*     */         else
/*     */         {
/* 250 */           startError = true;
/* 251 */           break;
/*     */         }
/*     */       }
/* 253 */       if (null != chars)
/*     */       {
/* 255 */         label476: chars[(charOffset++)] = (char)c;
/*     */       }
/* 257 */       ++numChars;
/*     */     }
/* 259 */     if (byteError)
/*     */     {
/* 261 */       throw new IdcByteHandlerException("syUTF8ContinuationByteBad", new Object[] { Integer.valueOf(b & 0xFF) });
/*     */     }
/* 263 */     if (charError)
/*     */     {
/* 265 */       throw new IdcByteHandlerException("syUTF8EncodeCharBad", new Object[] { Integer.valueOf(c) });
/*     */     }
/* 267 */     if (startError)
/*     */     {
/* 269 */       throw new IdcByteHandlerException("syUTF8StartByteBad", new Object[] { Integer.valueOf(b & 0xFF) });
/*     */     }
/* 271 */     return numChars;
/*     */   }
/*     */ 
/*     */   private static void fillTableIBM437()
/*     */   {
/* 279 */     String[] s00 = { "", "►◄↕‼¶§▬↨↑↓→←∟↔▲▼" };
/*     */ 
/* 284 */     String[] s7F = { "⌂", "ÇüéâäàåçêëèïîìÄÅ", "ÉæÆôöòûùÿÖÜ¢£¥₧ƒ", "áíóúñÑªº¿⌐¬½¼¡«»", "░▒▓│┤╡╢╖╕╣║╗╝╜╛┐", "└┴┬├─┼╞╟╚╔╩╦╠═╬╧", "╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀", "αßΓπΣσµτΦΘΩδ∞φε∩", "≡±≥≤⌠⌡÷≈°∙·√ⁿ²■ " };
/*     */ 
/* 296 */     tableIBM437 = new char[256];
/* 297 */     int index = 0;
/* 298 */     for (int row = 0; row < s00.length; ++row)
/*     */     {
/* 300 */       int length = s00[row].length();
/* 301 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 303 */         tableIBM437[(index++)] = s00[row].charAt(i);
/*     */       }
/*     */     }
/* 306 */     while (index < 127)
/*     */     {
/* 308 */       tableIBM437[index] = (char)(index++);
/*     */     }
/* 310 */     for (row = 0; row < s7F.length; ++row)
/*     */     {
/* 312 */       int length = s7F[row].length();
/* 313 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 315 */         tableIBM437[(index++)] = s7F[row].charAt(i);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static int decodeIBM437(byte[] bytes, int offset, int length, char[] chars, int charOffset)
/*     */   {
/* 335 */     if (null == tableIBM437)
/*     */     {
/* 337 */       fillTableIBM437();
/*     */     }
/* 339 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 341 */       int index = bytes[(offset++)] & 0xFF;
/* 342 */       chars[(charOffset + i)] = tableIBM437[index];
/*     */     }
/* 344 */     return length;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 351 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78225 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcByteConversionUtils
 * JD-Core Version:    0.5.4
 */