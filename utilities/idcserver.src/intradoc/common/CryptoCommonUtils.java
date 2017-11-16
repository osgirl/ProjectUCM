/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.InputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.security.MessageDigest;
/*     */ import java.security.NoSuchAlgorithmException;
/*     */ import java.security.SecureRandom;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class CryptoCommonUtils
/*     */ {
/*  30 */   protected static boolean m_isInitialized = false;
/*     */ 
/*  32 */   protected static SecureRandom m_sRandom = new SecureRandom();
/*  33 */   protected static int m_suggestedRandomStringSize = 40;
/*  34 */   protected static String m_startRandomString = null;
/*     */ 
/*  36 */   public static String DEFAULT_DIGEST = "SHA-256";
/*     */ 
/*  39 */   public static char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/*  47 */     String op = "";
/*  48 */     String arg1 = null;
/*  49 */     String arg2 = null;
/*  50 */     switch (args.length)
/*     */     {
/*     */     case 3:
/*     */     default:
/*  54 */       arg2 = args[2];
/*     */     case 2:
/*  56 */       arg1 = args[1];
/*     */     case 1:
/*  58 */       op = args[0];
/*     */     case 0:
/*     */     }
/*     */ 
/*  63 */     if ((op.equals("sha1enc")) && (arg1 != null))
/*     */     {
/*  65 */       String ret = sha1UuencodeHash(arg1, arg2);
/*  66 */       System.out.println(ret);
/*     */     }
/*     */     else
/*     */     {
/*  70 */       System.err.println("Usage: intradoc.common.CryptoUtils sha1enc data [ extra_data ]\n");
/*  71 */       System.exit(1);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String sha1UuencodeHash(String start, String extra)
/*     */   {
/*  86 */     return uuencodeHashWithDigest(start, extra, "SHA-1");
/*     */   }
/*     */ 
/*     */   public static String sha256UuencodeHash(String start, String extra)
/*     */   {
/* 100 */     return uuencodeHashWithDigest(start, extra, "SHA-256");
/*     */   }
/*     */ 
/*     */   public static String uuencodeHashWithDigest(String start, String extra, String digestType)
/*     */   {
/* 105 */     String val = null;
/* 106 */     if (start == null)
/*     */     {
/* 108 */       val = extra;
/*     */     }
/* 112 */     else if (extra != null)
/*     */     {
/* 114 */       val = start + extra;
/*     */     }
/*     */     else
/*     */     {
/* 118 */       val = start;
/*     */     }
/*     */ 
/* 121 */     if (val == null)
/*     */     {
/* 123 */       val = "";
/*     */     }
/*     */ 
/* 126 */     byte[] dataBuf = null;
/*     */     try
/*     */     {
/* 129 */       dataBuf = val.getBytes(FileUtils.m_javaSystemEncoding);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 133 */       dataBuf = val.getBytes();
/*     */     }
/*     */ 
/* 136 */     MessageDigest digest = getMessageDigest(digestType);
/* 137 */     if (digest != null)
/*     */     {
/* 139 */       digest.update(dataBuf);
/* 140 */       dataBuf = digest.digest();
/*     */     }
/* 142 */     return CommonDataConversion.uuencode(dataBuf, 0, dataBuf.length);
/*     */   }
/*     */ 
/*     */   public static String sha1UuencodeFile(String pathToFile)
/*     */   {
/* 154 */     File f = new File(pathToFile);
/* 155 */     BufferedInputStream stream = null;
/*     */     try
/*     */     {
/* 158 */       stream = new BufferedInputStream(new FileInputStream(f));
/* 159 */       String str = sha1UuencodeStream(stream);
/*     */ 
/* 167 */       return str;
/*     */     }
/*     */     catch (FileNotFoundException e)
/*     */     {
/* 163 */       Object localObject1 = null;
/*     */ 
/* 167 */       return localObject1; } finally { FileUtils.closeObject(stream); }
/*     */ 
/*     */   }
/*     */ 
/*     */   public static String sha256UuencodeFile(String pathToFile)
/*     */   {
/* 180 */     File f = new File(pathToFile);
/*     */     try
/*     */     {
/* 183 */       BufferedInputStream stream = new BufferedInputStream(new FileInputStream(f));
/* 184 */       return sha256UuencodeStream(stream);
/*     */     }
/*     */     catch (FileNotFoundException e) {
/*     */     }
/* 188 */     return null;
/*     */   }
/*     */ 
/*     */   public static String sha1UuencodeStream(InputStream stream)
/*     */   {
/* 201 */     MessageDigest sha1Digest = getSha1Digest();
/* 202 */     return messageDigestUuencodeStream(stream, sha1Digest);
/*     */   }
/*     */ 
/*     */   public static String sha256UuencodeStream(InputStream stream)
/*     */   {
/* 214 */     MessageDigest sha256Digest = getSha256Digest();
/* 215 */     return messageDigestUuencodeStream(stream, sha256Digest);
/*     */   }
/*     */ 
/*     */   public static String messageDigestUuencodeStream(InputStream stream, MessageDigest msgDigest)
/*     */   {
/*     */     try
/*     */     {
/* 229 */       byte[] buf = new byte[65536];
/* 230 */       int len = -1;
/* 231 */       while ((len = stream.read(buf)) != -1)
/*     */       {
/* 233 */         msgDigest.update(buf, 0, len);
/*     */       }
/* 235 */       byte[] raw = msgDigest.digest();
/* 236 */       return CommonDataConversion.uuencode(raw, 0, raw.length);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 241 */       Report.trace("system", "Unable to uuencode stream.", e);
/* 242 */     }return null;
/*     */   }
/*     */ 
/*     */   public static String hexEncodeStringWithDigest(String str, String digestType, int maxLen)
/*     */   {
/* 248 */     byte[] dataBuf = null;
/*     */     try
/*     */     {
/* 251 */       dataBuf = str.getBytes(FileUtils.m_javaSystemEncoding);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 255 */       dataBuf = str.getBytes();
/*     */     }
/*     */ 
/* 258 */     MessageDigest digest = getMessageDigest(digestType);
/* 259 */     if (digest != null)
/*     */     {
/* 261 */       digest.update(dataBuf);
/* 262 */       dataBuf = digest.digest();
/*     */     }
/*     */ 
/* 265 */     if (maxLen <= 0)
/*     */     {
/* 267 */       maxLen = dataBuf.length * 2;
/*     */     }
/*     */ 
/* 270 */     char[] charBuf = new char[maxLen];
/* 271 */     int nchars = encodeHexBytes(charBuf, 0, dataBuf, 0, dataBuf.length);
/* 272 */     return new String(charBuf, 0, nchars);
/*     */   }
/*     */ 
/*     */   public static String computeStreamDigestAndEncode(ByteArrayOutputStream os, String algorithm)
/*     */     throws NoSuchAlgorithmException
/*     */   {
/* 278 */     MessageDigest md = MessageDigest.getInstance(algorithm);
/* 279 */     md.update(os.toByteArray());
/*     */ 
/* 281 */     byte[] raw = md.digest();
/* 282 */     return CommonDataConversion.uuencode(raw, 0, raw.length);
/*     */   }
/*     */ 
/*     */   public static String computeDigest(byte[] data, String algorithm)
/*     */     throws NoSuchAlgorithmException
/*     */   {
/* 288 */     MessageDigest md = MessageDigest.getInstance(algorithm);
/* 289 */     md.update(data);
/*     */ 
/* 291 */     byte[] raw = md.digest();
/* 292 */     return CommonDataConversion.uuencode(raw, 0, raw.length);
/*     */   }
/*     */ 
/*     */   public static MessageDigest getSha1Digest()
/*     */   {
/* 297 */     return getMessageDigest("SHA-1");
/*     */   }
/*     */ 
/*     */   public static MessageDigest getSha256Digest()
/*     */   {
/* 302 */     return getMessageDigest(DEFAULT_DIGEST);
/*     */   }
/*     */ 
/*     */   public static MessageDigest getMessageDigest(String type)
/*     */   {
/*     */     try
/*     */     {
/* 309 */       return MessageDigest.getInstance(type);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 313 */       Report.trace("system", "Unable to create " + type + " digest.", e);
/*     */     }
/* 315 */     return null;
/*     */   }
/*     */ 
/*     */   public static void checkInit()
/*     */   {
/* 328 */     if (m_isInitialized)
/*     */     {
/* 330 */       return;
/*     */     }
/*     */ 
/* 333 */     Properties env = (Properties)AppObjectRepository.getObject("environment");
/* 334 */     String str = env.getProperty("RandomSeedSize");
/* 335 */     int size = NumberUtils.parseInteger(str, 128);
/*     */ 
/* 343 */     byte[] seed = m_sRandom.generateSeed(size);
/* 344 */     m_sRandom.setSeed(seed);
/*     */ 
/* 348 */     str = env.getProperty("RandomSuggestedStringSize");
/* 349 */     m_suggestedRandomStringSize = NumberUtils.parseInteger(str, m_suggestedRandomStringSize);
/* 350 */     if (m_suggestedRandomStringSize > seed.length * 2)
/*     */     {
/* 352 */       m_suggestedRandomStringSize = seed.length * 2;
/*     */     }
/* 354 */     int strLen = m_suggestedRandomStringSize;
/*     */ 
/* 356 */     char[] buf = new char[seed.length * 2];
/* 357 */     int nchars = encodeHexBytes(buf, 0, seed, 0, seed.length);
/* 358 */     if (strLen > nchars)
/*     */     {
/* 362 */       strLen = nchars;
/*     */     }
/* 364 */     m_startRandomString = new String(buf, 0, strLen);
/*     */ 
/* 366 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public static void getRandomBytes(byte[] b)
/*     */   {
/* 378 */     synchronized (m_sRandom)
/*     */     {
/* 380 */       checkInit();
/* 381 */       m_sRandom.nextBytes(b);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getStartingRandomString()
/*     */   {
/* 393 */     synchronized (m_sRandom)
/*     */     {
/* 395 */       checkInit();
/*     */     }
/* 397 */     return m_startRandomString;
/*     */   }
/*     */ 
/*     */   public static String generateRandomStringOfSuggestedSize()
/*     */   {
/* 407 */     byte[] b = null;
/* 408 */     int strLen = 0;
/* 409 */     synchronized (m_sRandom)
/*     */     {
/* 411 */       checkInit();
/*     */ 
/* 413 */       strLen = m_suggestedRandomStringSize;
/* 414 */       int bytesOfRandomness = (strLen + 1) / 2;
/* 415 */       b = new byte[bytesOfRandomness];
/* 416 */       m_sRandom.nextBytes(b);
/*     */     }
/* 418 */     char[] buf = new char[b.length * 2];
/* 419 */     int nchars = encodeHexBytes(buf, 0, b, 0, b.length);
/* 420 */     if (strLen > nchars)
/*     */     {
/* 424 */       strLen = nchars;
/*     */     }
/*     */ 
/* 427 */     return new String(buf, 0, strLen);
/*     */   }
/*     */ 
/*     */   public static String generateRandomString(int strLen)
/*     */   {
/* 439 */     byte[] b = null;
/*     */ 
/* 441 */     synchronized (m_sRandom)
/*     */     {
/* 443 */       checkInit();
/* 444 */       int bytesOfRandomness = (strLen + 1) / 2;
/* 445 */       b = new byte[bytesOfRandomness];
/* 446 */       m_sRandom.nextBytes(b);
/*     */     }
/* 448 */     char[] buf = new char[b.length * 2];
/* 449 */     int nchars = encodeHexBytes(buf, 0, b, 0, b.length);
/* 450 */     if (strLen > nchars)
/*     */     {
/* 454 */       strLen = nchars;
/*     */     }
/*     */ 
/* 457 */     return new String(buf, 0, strLen);
/*     */   }
/*     */ 
/*     */   public static int encodeHexBytes(char[] buf, int startChar, byte[] bytes, int startByte, int numBytes)
/*     */   {
/* 480 */     int endByte = startByte + numBytes;
/* 481 */     int charIndex = startChar;
/* 482 */     for (int i = startByte; i < endByte; ++i)
/*     */     {
/* 484 */       byte b = bytes[i];
/* 485 */       if (charIndex < buf.length)
/*     */       {
/* 487 */         int nibble = b >>> 4;
/* 488 */         buf[(charIndex++)] = HEX_DIGITS[(nibble & 0xF)];
/*     */       }
/* 490 */       if (charIndex >= buf.length)
/*     */         continue;
/* 492 */       int nibble = b & 0xF;
/* 493 */       buf[(charIndex++)] = HEX_DIGITS[nibble];
/*     */     }
/*     */ 
/* 496 */     return charIndex;
/*     */   }
/*     */ 
/*     */   protected static String generateRandomStringInternal(int strLen)
/*     */   {
/* 501 */     int bytesOfRandomness = (strLen + 1) / 2;
/* 502 */     byte[] b = new byte[bytesOfRandomness];
/* 503 */     m_sRandom.nextBytes(b);
/*     */ 
/* 505 */     char[] buf = new char[b.length * 2];
/* 506 */     int nchars = encodeHexBytes(buf, 0, b, 0, b.length);
/* 507 */     if (strLen > nchars)
/*     */     {
/* 511 */       strLen = nchars;
/*     */     }
/*     */ 
/* 514 */     return new String(buf, 0, strLen);
/*     */   }
/*     */ 
/*     */   public static long getRandomLong()
/*     */   {
/* 524 */     long retVal = 0L;
/* 525 */     synchronized (m_sRandom)
/*     */     {
/* 527 */       checkInit();
/* 528 */       retVal = m_sRandom.nextLong();
/*     */     }
/* 530 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 535 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.CryptoCommonUtils
 * JD-Core Version:    0.5.4
 */