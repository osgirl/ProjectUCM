/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.data.IdcProperties;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Iterator;
/*     */ import java.util.LinkedHashMap;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class IdcProtocol
/*     */ {
/*     */   public static int readHeader(byte[] buffer, IdcStringBuilder builder, BufferedInputStream stream, Properties headers)
/*     */     throws IOException
/*     */   {
/*  45 */     if (buffer == null)
/*     */     {
/*  47 */       buffer = new byte[''];
/*     */     }
/*  49 */     if (buffer.length < 128)
/*     */     {
/*  51 */       throw new AssertionError("Provider a buffer of at least 128 bytes to IdcProtocol.readHeader().");
/*     */     }
/*     */ 
/*  54 */     if (builder == null)
/*     */     {
/*  56 */       builder = new IdcStringBuilder();
/*     */     }
/*  58 */     int count = -2;
/*  59 */     char state = 'i';
/*  60 */     int length = -2;
/*  61 */     int offset = 0;
/*  62 */     byte b = 0;
/*  63 */     int c = 0;
/*  64 */     String key = null; String value = null;
/*  65 */     IdcStringBuilder tmp = new IdcStringBuilder();
/*  66 */     while (state != 'F')
/*     */     {
/*  68 */       if (offset >= count)
/*     */       {
/*  70 */         offset = 0;
/*  71 */         if ((count == -2) || (count == buffer.length))
/*     */         {
/*  75 */           stream.mark(128);
/*  76 */           count = stream.read(buffer, 0, buffer.length);
/*     */         }
/*     */         else
/*     */         {
/*  80 */           count = -1;
/*     */         }
/*     */ 
/*  83 */         if (count <= 0)
/*     */         {
/*  85 */           if (state == '?')
/*     */           {
/*  87 */             stream.reset();
/*  88 */             return -3;
/*     */           }
/*  90 */           throw new IOException("!$AJK End of stream prior to finishing headers");
/*     */         }
/*     */       }
/*  93 */       b = buffer[(offset++)];
/*     */ 
/*  95 */       switch (state)
/*     */       {
/*     */       case '?':
/*  98 */         if ((b & 0x80) != 0)
/*     */         {
/* 100 */           stream.reset();
/* 101 */           return -2;
/*     */         }
/* 103 */         if (offset == 16)
/*     */         {
/* 105 */           stream.reset();
/* 106 */           return -3;
/*     */         }
/*     */       case 'i':
/* 109 */         state = (state == b) ? 'd' : '?'; break;
/*     */       case 'd':
/* 110 */         state = (state == b) ? 'c' : '?'; break;
/*     */       case 'c':
/* 111 */         state = (state == b) ? '\n' : '?'; break;
/*     */       case '\n':
/* 112 */         state = (state == b) ? 'K' : '?'; break;
/*     */       case 'k':
/*     */       case 'v':
/* 116 */         if (((b & 0x80) != 0) && ((b & 0x40) == 0))
/*     */         {
/* 119 */           c <<= 6;
/* 120 */           c |= b & 0x3F;
/*     */         }
/*     */         else {
/* 123 */           if ((c & 0xF0000) != 0)
/*     */           {
/* 126 */             c -= 65536;
/* 127 */             char c1 = (char)(c >>> 10 | 0xD800);
/* 128 */             char c2 = (char)(c & 0x3FF | 0xDC00);
/* 129 */             tmp.append(c1);
/* 130 */             tmp.append(c2);
/*     */           }
/*     */           else
/*     */           {
/* 135 */             tmp.append((char)c);
/*     */           }
/*     */ 
/* 138 */           state = (char)(state - ' ');
/*     */         }
/*     */       case 'K':
/*     */       case 'V':
/* 142 */         if ((b & 0x80) != 0)
/*     */         {
/* 144 */           c = b & 0x3F;
/* 145 */           if ((c & 0x20) != 0)
/*     */           {
/* 147 */             c &= 31;
/* 148 */             if ((c & 0x10) != 0)
/*     */             {
/* 150 */               c &= 15;
/*     */             }
/*     */           }
/* 153 */           state = (char)(state + ' ');
/*     */         }
/* 156 */         else if ((b == 61) && (state == 'K'))
/*     */         {
/* 159 */           key = tmp.toStringNoRelease();
/* 160 */           tmp.setLength(0);
/* 161 */           state = 'V';
/*     */         }
/* 164 */         else if ((b == 10) && (state == 'K'))
/*     */         {
/* 169 */           length = NumberUtils.parseInteger(tmp.toString(), -2);
/* 170 */           if (length < -1)
/*     */           {
/* 173 */             stream.reset();
/* 174 */             return -3;
/*     */           }
/* 176 */           state = 'F';
/*     */         }
/* 179 */         else if ((b == 10) && (state == 'V'))
/*     */         {
/* 181 */           value = tmp.toStringNoRelease();
/* 182 */           tmp.setLength(0);
/* 183 */           headers.put(key, value);
/*     */ 
/* 185 */           state = 'K';
/*     */         }
/*     */         else {
/* 188 */           tmp.append((char)b);
/*     */         }
/*     */       }
/*     */     }
/* 192 */     stream.reset();
/* 193 */     stream.read(buffer, 0, offset);
/* 194 */     return length;
/*     */   }
/*     */ 
/*     */   public static Properties getOrReadHeader(byte[] buffer, IdcStringBuilder builder, IncomingConnection connection, ExecutionContext context)
/*     */     throws IOException
/*     */   {
/* 201 */     Properties headers = (Properties)context.getCachedObject("IdcProtocolIncomingHeaders");
/* 202 */     if (headers != null)
/*     */     {
/* 204 */       return headers;
/*     */     }
/* 206 */     IdcProperties idcHeaders = new IdcProperties();
/* 207 */     headers = idcHeaders;
/* 208 */     idcHeaders.setMap(new LinkedHashMap());
/*     */ 
/* 213 */     if (connection instanceof SocketIncomingConnection)
/*     */     {
/* 215 */       SocketIncomingConnection scon = (SocketIncomingConnection)connection;
/* 216 */       InputStream tmp = connection.getInputStream();
/*     */       BufferedInputStream stream;
/* 217 */       if (tmp instanceof BufferedInputStream)
/*     */       {
/* 219 */         stream = (BufferedInputStream)tmp;
/*     */       }
/*     */       else
/*     */       {
/*     */         BufferedInputStream stream;
/* 223 */         scon.setInputStream(stream = new BufferedInputStream(tmp));
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 228 */       throw new IOException("!$AJK: IdcProtocol doesn't support type " + connection.getClass().getName());
/*     */     }
/*     */     BufferedInputStream stream;
/* 232 */     Integer length = new Integer(readHeader(buffer, builder, stream, headers));
/* 233 */     context.setCachedObject("IdcProtocolIncomingHeaders", idcHeaders);
/* 234 */     context.setCachedObject("IdcProtocolIncomingLength", length);
/*     */ 
/* 236 */     return headers;
/*     */   }
/*     */ 
/*     */   public static int getBodyLength(ExecutionContext context)
/*     */   {
/* 241 */     Integer length = (Integer)context.getCachedObject("IdcProtocolIncomingLength");
/* 242 */     if (length != null)
/*     */     {
/* 244 */       return length.intValue();
/*     */     }
/* 246 */     throw new AssertionError("Illegal call to getBodyLength() without calling getOrReadHeader().");
/*     */   }
/*     */ 
/*     */   public static void writeHeader(byte[] buf, OutputStream out, Properties headers, int length)
/*     */     throws IOException
/*     */   {
/* 252 */     if (buf == null)
/*     */     {
/* 254 */       buf = new byte[''];
/*     */     }
/* 256 */     buf[0] = 105;
/* 257 */     buf[1] = 100;
/* 258 */     buf[2] = 99;
/* 259 */     buf[3] = 10;
/* 260 */     int offset = 4;
/*     */     Iterator i$;
/* 261 */     if (headers != null)
/*     */     {
/* 263 */       Set headerKeys = headers.keySet();
/* 264 */       for (i$ = headerKeys.iterator(); i$.hasNext(); ) { Object k = i$.next();
/*     */ 
/* 266 */         if (k instanceof String)
/*     */         {
/* 268 */           String key = (String)k;
/* 269 */           String value = headers.getProperty(key);
/* 270 */           offset = write(buf, offset, out, key, '=');
/* 271 */           offset = write(buf, offset, out, value, '\n');
/*     */         } }
/*     */ 
/*     */     }
/*     */ 
/* 276 */     int off2 = buf.length - 1;
/*     */     do
/*     */     {
/* 279 */       if (off2 <= offset)
/*     */       {
/* 281 */         out.write(buf);
/* 282 */         offset = 0;
/*     */       }
/* 284 */       int tmp = length % 10;
/* 285 */       length /= 10;
/* 286 */       buf[(off2--)] = (byte)(48 + tmp);
/*     */     }
/* 288 */     while (length > 0);
/* 289 */     while (++off2 < buf.length)
/*     */     {
/* 291 */       buf[(offset++)] = buf[off2];
/*     */     }
/* 293 */     buf[(offset++)] = 10;
/*     */ 
/* 295 */     out.write(buf, 0, offset);
/*     */   }
/*     */ 
/*     */   public static int write(byte[] buf, int offset, OutputStream out, String text, char suffix)
/*     */     throws IOException
/*     */   {
/* 301 */     byte[] data = text.getBytes("UTF8");
/* 302 */     for (byte b : data)
/*     */     {
/* 304 */       if (offset == buf.length)
/*     */       {
/* 306 */         out.write(buf);
/* 307 */         offset = 0;
/*     */       }
/* 309 */       buf[(offset++)] = b;
/*     */     }
/* 311 */     if (suffix != 0)
/*     */     {
/* 313 */       if (offset == buf.length)
/*     */       {
/* 315 */         out.write(buf);
/* 316 */         offset = 0;
/*     */       }
/* 318 */       buf[(offset++)] = (byte)suffix;
/*     */     }
/* 320 */     return offset;
/*     */   }
/*     */ 
/*     */   public static Properties setOutputHeader(String key, String value, ExecutionContext context)
/*     */   {
/* 325 */     Properties props = (Properties)context.getCachedObject("IdcProtocolOutputHeaders");
/* 326 */     if (props == null)
/*     */     {
/* 328 */       IdcProperties idcProps = new IdcProperties();
/* 329 */       idcProps.setMap(new LinkedHashMap());
/* 330 */       props = idcProps;
/* 331 */       context.setCachedObject("IdcProtocolOutputHeaders", props);
/*     */     }
/*     */ 
/* 334 */     props.put(key, value);
/* 335 */     return props;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 340 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.IdcProtocol
 * JD-Core Version:    0.5.4
 */