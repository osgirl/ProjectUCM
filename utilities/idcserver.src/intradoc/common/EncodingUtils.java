/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.io.IdcByteConversionUtils;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.io.IdcRandomAccessByteArray;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.zip.Deflater;
/*     */ 
/*     */ public class EncodingUtils
/*     */ {
/* 268 */   protected static final char[] BASE64_PEM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
/*     */ 
/*     */   @Deprecated
/*     */   public static String rfc2047Encode(String text, String javaEncoding, String isoEncoding)
/*     */     throws UnsupportedEncodingException
/*     */   {
/*  40 */     return rfc2047Encode(null, null, text, javaEncoding, isoEncoding).toString();
/*     */   }
/*     */ 
/*     */   protected static int weighNon7BitChars(String text)
/*     */   {
/*  45 */     int length = text.length();
/*  46 */     int count = 0;
/*  47 */     for (int i = 0; i < length; ++i)
/*     */     {
/*  49 */       char c = '\000';
/*  50 */       c = text.charAt(i);
/*  51 */       if ((c < ' ') || (c > '~'))
/*     */       {
/*  53 */         ++count;
/*     */       }
/*  55 */       if (c <= 'ÿ')
/*     */         continue;
/*  57 */       ++count;
/*     */     }
/*     */ 
/*  60 */     return count;
/*     */   }
/*     */ 
/*     */   public static IdcAppendable rfc2047Encode(IdcAppendable buf, String header, String text, String javaEncoding, String isoEncoding)
/*     */     throws UnsupportedEncodingException
/*     */   {
/*  67 */     if (buf == null)
/*     */     {
/*  69 */       buf = new IdcStringBuilder(2 * text.length());
/*     */     }
/*  71 */     int length = text.length();
/*  72 */     int count = 0;
/*  73 */     List textParts = null;
/*  74 */     int wordStart = 0;
/*  75 */     boolean inQuote = false;
/*     */ 
/*  77 */     for (int i = 0; i <= length; ++i)
/*     */     {
/*  79 */       char c = '\000';
/*  80 */       if (i < length)
/*     */       {
/*  82 */         c = text.charAt(i);
/*     */       }
/*  84 */       if ((c != 0) && ((
/*  86 */         (c < ' ') || (c > '~'))))
/*     */       {
/*  88 */         ++count;
/*     */       }
/*     */ 
/*  92 */       if (textParts == null)
/*     */       {
/*  94 */         textParts = new ArrayList();
/*     */       }
/*  96 */       if ((c == '"') && (!inQuote))
/*     */       {
/*  98 */         inQuote = true;
/*     */       }
/* 100 */       else switch (c)
/*     */         {
/*     */         case '"':
/* 103 */           inQuote = true;
/* 104 */           break;
/*     */         case '\000':
/*     */         case ' ':
/*     */         case '(':
/*     */         case ')':
/*     */         case ',':
/*     */         case '.':
/*     */         case ':':
/*     */         case ';':
/*     */         case '<':
/*     */         case '>':
/*     */         case '@':
/* 117 */           if (c == ' ')
/*     */           {
/* 121 */             String tmp = text.substring(wordStart, i + 1);
/* 122 */             textParts.add(tmp);
/*     */           }
/*     */           else
/*     */           {
/* 126 */             String tmp = text.substring(wordStart, i);
/* 127 */             textParts.add(tmp);
/* 128 */             if (c > 0)
/*     */             {
/* 130 */               textParts.add("" + c);
/*     */             }
/*     */           }
/* 133 */           wordStart = i + 1;
/*     */         }
/*     */ 
/*     */ 
/*     */     }
/*     */ 
/* 140 */     int lineLength = 15;
/* 141 */     if (header != null)
/*     */     {
/* 143 */       buf.append(header);
/* 144 */       buf.append(": ");
/* 145 */       lineLength = header.length() + 2;
/*     */     }
/* 147 */     if (count == 0)
/*     */     {
/* 149 */       buf.append(text);
/* 150 */       return buf;
/*     */     }
/*     */ 
/* 153 */     String suffix = "?=";
/* 154 */     for (String part : textParts)
/*     */     {
/* 156 */       count = weighNon7BitChars(part);
/* 157 */       length = part.length();
/* 158 */       if (count == 0)
/*     */       {
/* 160 */         if (lineLength + length >= 76);
/* 172 */         buf.append(part);
/* 173 */         lineLength += length;
/*     */       }
/*     */ 
/* 177 */       boolean useQuotedPrintable = length / count > 3;
/*     */       String prefix;
/*     */       String prefix;
/* 178 */       if (useQuotedPrintable)
/*     */       {
/* 181 */         prefix = "=?" + isoEncoding + "?Q?";
/*     */       }
/*     */       else
/*     */       {
/* 186 */         prefix = "=?" + isoEncoding + "?B?";
/*     */       }
/* 188 */       int maxLength = 75 - prefix.length() - suffix.length();
/*     */ 
/* 190 */       if (lineLength - prefix.length() - suffix.length() - 10 > 76)
/*     */       {
/* 193 */         buf.append("\r\n ");
/* 194 */         lineLength = 1;
/*     */       }
/* 196 */       ByteArrayOutputStream out = new ByteArrayOutputStream();
/* 197 */       OutputStreamWriter w = new OutputStreamWriter(out, javaEncoding);
/* 198 */       char[] chars = new char[part.length()];
/* 199 */       part.getChars(0, chars.length, chars, 0);
/*     */       try
/*     */       {
/* 202 */         String oldEnc = null;
/* 203 */         boolean needsPrefix = true;
/*     */ 
/* 205 */         for (int i = 0; i <= chars.length; ++i)
/*     */         {
/* 207 */           if (needsPrefix)
/*     */           {
/* 211 */             if (maxLength - prefix.length() - lineLength <= 10)
/*     */             {
/* 214 */               buf.append("\r\n ");
/* 215 */               lineLength = 1;
/*     */             }
/* 217 */             buf.append(prefix);
/* 218 */             lineLength += prefix.length();
/* 219 */             needsPrefix = false;
/*     */           }
/* 221 */           if (i < chars.length)
/*     */           {
/* 223 */             w.write(chars[i]);
/*     */           }
/* 225 */           w.flush();
/* 226 */           byte[] bytes = out.toByteArray();
/*     */           String enc;
/*     */           String enc;
/* 228 */           if (useQuotedPrintable)
/*     */           {
/* 230 */             enc = CommonDataConversion.quotEncode(bytes, true);
/*     */           }
/*     */           else
/*     */           {
/* 234 */             enc = CommonDataConversion.uuencode(bytes, 0, bytes.length);
/*     */           }
/* 236 */           if ((i == chars.length) || (maxLength - lineLength - enc.length() <= 0))
/*     */           {
/* 239 */             buf.append(oldEnc);
/* 240 */             lineLength += oldEnc.length();
/* 241 */             buf.append(suffix);
/* 242 */             lineLength += 2;
/* 243 */             needsPrefix = true;
/* 244 */             out = new ByteArrayOutputStream();
/* 245 */             w = new OutputStreamWriter(out, javaEncoding);
/* 246 */             oldEnc = null;
/* 247 */             if (i >= chars.length)
/*     */               continue;
/* 249 */             --i;
/*     */           }
/*     */           else
/*     */           {
/* 254 */             oldEnc = enc;
/*     */           }
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 260 */         throw new AssertionError(e);
/*     */       }
/*     */     }
/*     */ 
/* 264 */     return buf;
/*     */   }
/*     */ 
/*     */   public static char[] encodeBase64(byte[] bytes, int offset, int length, char[] chars, int charOffset, int flags)
/*     */   {
/* 273 */     return encodeBase64(BASE64_PEM, '=', bytes, offset, length, chars, charOffset, flags);
/*     */   }
/*     */ 
/*     */   public static char[] encodeBase64(char[] base64_pem, char paddingChar, byte[] bytes, int offset, int length, char[] chars, int charOffset, int flags)
/*     */   {
/* 278 */     int numGroups = (length + 2) / 3;
/* 279 */     int numChars = numGroups << 2;
/* 280 */     if (null == chars)
/*     */     {
/* 282 */       chars = new char[numChars + charOffset];
/*     */     }
/* 284 */     while (length > 0)
/*     */     {
/* 286 */       int val = (bytes[(offset++)] & 0xFF) << 16;
/* 287 */       if (length > 1)
/*     */       {
/* 289 */         val |= (bytes[(offset++)] & 0xFF) << 8;
/*     */       }
/* 291 */       if (length > 2)
/*     */       {
/* 293 */         val |= bytes[(offset++)] & 0xFF;
/*     */       }
/* 295 */       chars[(charOffset++)] = base64_pem[(val >> 18 & 0x3F)];
/* 296 */       chars[(charOffset++)] = base64_pem[(val >> 12 & 0x3F)];
/* 297 */       chars[(charOffset++)] = ((length > 1) ? base64_pem[(val >> 6 & 0x3F)] : paddingChar);
/* 298 */       chars[(charOffset++)] = ((length > 2) ? base64_pem[(val & 0x3F)] : paddingChar);
/* 299 */       length -= 3;
/*     */     }
/* 301 */     return chars;
/*     */   }
/*     */ 
/*     */   public static void appendBase64(byte[] bytes, int offset, int length, IdcAppendable str, int flags)
/*     */   {
/* 306 */     int numChars = (length + 2) / 3 << 2;
/* 307 */     if (str instanceof IdcStringBuilder)
/*     */     {
/* 309 */       IdcStringBuilder builder = (IdcStringBuilder)str;
/* 310 */       if (builder.m_length + numChars < builder.m_capacity)
/*     */       {
/* 312 */         builder.ensureCapacity(builder.m_length + numChars);
/*     */       }
/* 314 */       encodeBase64(bytes, offset, length, builder.m_charArray, builder.m_length, flags);
/* 315 */       builder.m_length += numChars;
/*     */     }
/*     */     else
/*     */     {
/* 319 */       char[] chars = encodeBase64(bytes, offset, length, null, 0, flags);
/* 320 */       str.append(chars, 0, chars.length);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static CharSequence deflateAsBase64(Object uncompressed, Map options)
/*     */     throws IdcByteHandlerException
/*     */   {
/*     */     IdcRandomAccessByteArray uncompressedHandler;
/*     */     IdcRandomAccessByteArray uncompressedHandler;
/* 329 */     if (uncompressed instanceof byte[])
/*     */     {
/* 331 */       uncompressedHandler = new IdcRandomAccessByteArray((byte[])(byte[])uncompressed);
/*     */     }
/*     */     else
/*     */     {
/*     */       int length;
/*     */       char[] uncompressedChars;
/* 337 */       if (uncompressed instanceof char[])
/*     */       {
/* 339 */         char[] uncompressedChars = (char[])(char[])uncompressed;
/* 340 */         length = uncompressedChars.length;
/*     */       }
/*     */       else
/*     */       {
/*     */         int length;
/* 342 */         if (uncompressed instanceof IdcStringBuilder)
/*     */         {
/* 344 */           IdcStringBuilder builder = (IdcStringBuilder)uncompressed;
/* 345 */           char[] uncompressedChars = builder.m_charArray;
/* 346 */           length = builder.m_length;
/*     */         }
/*     */         else
/*     */         {
/*     */           String str;
/*     */           String str;
/* 351 */           if (uncompressed instanceof String)
/*     */           {
/* 353 */             str = (String)uncompressed;
/*     */           }
/*     */           else
/*     */           {
/* 357 */             str = uncompressed.toString();
/*     */           }
/* 359 */           length = str.length();
/* 360 */           uncompressedChars = new char[length];
/* 361 */           str.getChars(0, length, uncompressedChars, 0);
/*     */         }
/*     */       }
/* 363 */       if (length < 1)
/* 364 */         return null;
/* 365 */       int numBytes = IdcByteConversionUtils.formatUTF8(uncompressedChars, 0, length, null);
/* 366 */       uncompressedHandler = new IdcRandomAccessByteArray(numBytes);
/* 367 */       int length = IdcByteConversionUtils.formatUTF8(uncompressedChars, 0, length, uncompressedHandler);
/* 368 */       uncompressedHandler.m_length = length;
/* 369 */       uncompressedHandler.m_position = 0;
/*     */     }
/* 371 */     Deflater deflater = new Deflater(9);
/* 372 */     deflater.setInput(uncompressedHandler.m_bytes, 0, uncompressedHandler.m_length);
/* 373 */     deflater.finish();
/*     */ 
/* 375 */     byte[] compressedBytes = new byte[uncompressedHandler.m_length + 4];
/* 376 */     int numBytes = deflater.deflate(compressedBytes);
/* 377 */     if (!deflater.finished())
/*     */     {
/* 379 */       throw new IdcByteHandlerException("syZipDeflateNotFinished", new Object[0]);
/*     */     }
/* 381 */     IdcStringBuilder str = new IdcStringBuilder((numBytes + 2) / 3 << 2);
/* 382 */     appendBase64(compressedBytes, 0, numBytes, str, 0);
/* 383 */     return str;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 390 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87341 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.EncodingUtils
 * JD-Core Version:    0.5.4
 */