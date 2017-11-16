/*     */ package intradoc.soap;
/*     */ 
/*     */ import intradoc.common.CommonDataConversion;
/*     */ import intradoc.data.DataBinder;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class SoapXmlFileParser
/*     */ {
/*     */   protected int m_readBlock;
/*     */   protected boolean m_isXmlMode;
/*     */   protected boolean m_isTagName;
/*     */   protected boolean m_isEndTagName;
/*     */   protected char m_previousChar;
/*     */   protected byte[] m_fileTagBytes;
/*     */   protected int m_tagLength;
/*     */   protected FileOutputStream m_outputStream;
/*     */   protected byte[] m_encodedFileBytes;
/*     */   protected int m_curEncodedByte;
/*     */   protected int m_curEncodedFile;
/*     */ 
/*     */   public SoapXmlFileParser()
/*     */   {
/*  29 */     this.m_readBlock = 10000;
/*  30 */     this.m_isXmlMode = true;
/*  31 */     this.m_isTagName = false;
/*  32 */     this.m_isEndTagName = false;
/*  33 */     this.m_previousChar = ' ';
/*     */ 
/*  35 */     this.m_fileTagBytes = null;
/*  36 */     this.m_tagLength = -1;
/*     */ 
/*  38 */     this.m_outputStream = null;
/*  39 */     this.m_encodedFileBytes = new byte[10000];
/*  40 */     this.m_curEncodedByte = 0;
/*  41 */     this.m_curEncodedFile = 0;
/*     */   }
/*     */ 
/*     */   public String removeFileContent(DataBinder data, BufferedInputStream bis, long length) throws IOException
/*     */   {
/*  46 */     long bytesLeft = length;
/*  47 */     int numPartialMatch = 0;
/*  48 */     StringBuffer buffer = new StringBuffer();
/*     */ 
/*  51 */     String fileTag = "fileContent";
/*  52 */     this.m_tagLength = fileTag.length();
/*  53 */     this.m_fileTagBytes = new byte[this.m_tagLength];
/*  54 */     for (int i = 0; i < this.m_tagLength; ++i)
/*     */     {
/*  56 */       this.m_fileTagBytes[i] = (byte)fileTag.charAt(i);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  61 */       while (bytesLeft > 0L)
/*     */       {
/*  64 */         int numRead = this.m_readBlock;
/*  65 */         if (numRead > bytesLeft)
/*     */         {
/*  67 */           numRead = (int)bytesLeft;
/*     */         }
/*     */ 
/*  71 */         bis.mark(numRead);
/*  72 */         byte[] b = SoapUtils.readStream(bis, numRead, false);
/*     */ 
/*  74 */         boolean isReset = false;
/*     */ 
/*  76 */         if (this.m_isXmlMode)
/*     */         {
/*  78 */           int[] retVal = parseSoapMessage(b, numPartialMatch);
/*  79 */           numPartialMatch = 0;
/*     */ 
/*  81 */           if (retVal[0] == 1)
/*     */           {
/*  84 */             numRead = retVal[1];
/*  85 */             isReset = true;
/*  86 */             this.m_isXmlMode = false;
/*     */           }
/*  88 */           else if (retVal[1] > 0)
/*     */           {
/*  91 */             numPartialMatch = retVal[1];
/*     */           }
/*     */ 
/*  94 */           String str = new String(b, 0, numRead, data.m_clientEncoding);
/*  95 */           buffer.append(str);
/*     */ 
/*  97 */           if (isReset)
/*     */           {
/*  99 */             bis.reset();
/* 100 */             bis.skip(numRead);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 105 */           int index = parseEncodedFile(data, b, buffer);
/* 106 */           if (index >= 0)
/*     */           {
/* 108 */             bis.reset();
/* 109 */             bis.skip(index);
/* 110 */             numRead = index;
/*     */ 
/* 112 */             this.m_isXmlMode = true;
/*     */           }
/*     */         }
/*     */ 
/* 116 */         bytesLeft -= numRead;
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 121 */       if (this.m_outputStream != null)
/*     */       {
/* 123 */         this.m_outputStream.close();
/*     */       }
/*     */     }
/*     */ 
/* 127 */     String soapMessage = buffer.toString();
/* 128 */     return soapMessage;
/*     */   }
/*     */ 
/*     */   protected int[] parseSoapMessage(byte[] b, int numPartialMatch)
/*     */   {
/* 134 */     if (numPartialMatch < 0)
/*     */     {
/* 136 */       numPartialMatch = 0;
/*     */     }
/*     */ 
/* 139 */     int[] retVal = new int[2];
/* 140 */     retVal[0] = 0;
/* 141 */     retVal[1] = 0;
/*     */ 
/* 143 */     int numBytes = b.length;
/* 144 */     int curIndex = 0;
/* 145 */     char curChar = ' ';
/* 146 */     for (int i = 0; i < numBytes; ++i)
/*     */     {
/* 148 */       this.m_previousChar = curChar;
/* 149 */       curChar = (char)b[i];
/*     */ 
/* 151 */       if ((this.m_isTagName) && (curChar == '/'))
/*     */       {
/* 153 */         this.m_isEndTagName = true;
/*     */       }
/*     */ 
/* 156 */       if (!this.m_isTagName)
/*     */       {
/* 158 */         if (b[i] != 60)
/*     */           continue;
/* 160 */         this.m_isTagName = true;
/*     */       }
/* 164 */       else if ((b[i] == 62) || (Character.isWhitespace((char)b[i])))
/*     */       {
/* 166 */         this.m_isTagName = false;
/* 167 */         this.m_isEndTagName = false;
/*     */       }
/*     */       else
/*     */       {
/* 171 */         if ((numPartialMatch == 0) && (this.m_previousChar != '<') && (this.m_previousChar != ':')) {
/*     */           continue;
/*     */         }
/*     */ 
/* 175 */         boolean isMatch = true;
/* 176 */         curIndex = i;
/*     */ 
/* 178 */         while ((numPartialMatch < this.m_tagLength) && (curIndex < numBytes))
/*     */         {
/* 180 */           if (b[curIndex] != this.m_fileTagBytes[numPartialMatch])
/*     */           {
/* 182 */             isMatch = false;
/* 183 */             numPartialMatch = 0;
/*     */ 
/* 185 */             break;
/*     */           }
/*     */ 
/* 188 */           ++numPartialMatch;
/* 189 */           ++curIndex;
/*     */         }
/*     */ 
/* 192 */         if (isMatch)
/*     */         {
/* 196 */           if (this.m_isEndTagName)
/*     */           {
/* 198 */             isMatch = false;
/* 199 */             numPartialMatch = 0;
/*     */ 
/* 201 */             continue;
/*     */           }
/*     */ 
/* 204 */           if (numPartialMatch >= this.m_tagLength)
/*     */           {
/* 206 */             retVal[0] = 1;
/*     */ 
/* 209 */             while ((curIndex < numBytes) && (b[(curIndex++)] != 62));
/* 213 */             retVal[1] = curIndex; break;
/*     */           }
/*     */ 
/* 217 */           retVal[0] = 0;
/* 218 */           retVal[1] = numPartialMatch;
/*     */ 
/* 221 */           break;
/*     */         }
/* 223 */         curChar = (char)b[curIndex];
/*     */       }
/*     */     }
/* 226 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected int parseEncodedFile(DataBinder data, byte[] b, StringBuffer buffer)
/*     */     throws IOException
/*     */   {
/* 234 */     if (SoapMultipartSerializer.isMtomRequest(data))
/*     */     {
/* 237 */       int index = 0;
/*     */ 
/* 240 */       while (Character.isWhitespace((char)b[index]))
/*     */       {
/* 244 */         ++index;
/*     */       }
/*     */ 
/* 248 */       int numToRead = 200;
/* 249 */       if (numToRead > b.length)
/*     */       {
/* 251 */         numToRead = b.length;
/*     */       }
/*     */ 
/* 254 */       String fileContent = new String(b, index, numToRead);
/* 255 */       index = fileContent.indexOf("Include", index);
/* 256 */       if (index >= 0)
/*     */       {
/* 259 */         index = fileContent.indexOf("fileContent");
/* 260 */         if (index > 0)
/*     */         {
/* 263 */           while (index > 0)
/*     */           {
/* 265 */             if (b[index] == 60) {
/*     */               break;
/*     */             }
/*     */ 
/* 269 */             --index;
/*     */           }
/*     */ 
/* 272 */           String mtomTag = fileContent.substring(0, index);
/* 273 */           buffer.append(mtomTag);
/*     */ 
/* 275 */           return index;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 281 */     if (this.m_outputStream == null)
/*     */     {
/* 283 */       this.m_curEncodedFile += 1;
/* 284 */       String fileKey = "decodedfile" + this.m_curEncodedFile;
/* 285 */       String fileName = "temp." + this.m_curEncodedFile;
/* 286 */       String tempFilePath = SoapUtils.getTempFile(data, fileKey, fileName);
/* 287 */       this.m_outputStream = new FileOutputStream(tempFilePath);
/* 288 */       this.m_curEncodedByte = 0;
/*     */     }
/*     */ 
/* 292 */     int numBytes = b.length;
/* 293 */     for (int i = 0; i < numBytes; ++i)
/*     */     {
/* 296 */       if (Character.isWhitespace((char)b[i])) {
/*     */         continue;
/*     */       }
/*     */ 
/* 300 */       if (b[i] == 60)
/*     */       {
/* 303 */         writeDecodedBytes();
/* 304 */         this.m_outputStream.flush();
/* 305 */         this.m_outputStream.close();
/* 306 */         this.m_outputStream = null;
/*     */ 
/* 308 */         return i;
/*     */       }
/*     */ 
/* 312 */       this.m_encodedFileBytes[this.m_curEncodedByte] = b[i];
/* 313 */       this.m_curEncodedByte += 1;
/* 314 */       if (this.m_curEncodedByte != 10000)
/*     */         continue;
/* 316 */       writeDecodedBytes();
/* 317 */       this.m_curEncodedByte = 0;
/*     */     }
/*     */ 
/* 322 */     return -1;
/*     */   }
/*     */ 
/*     */   protected void writeDecodedBytes() throws IOException
/*     */   {
/* 327 */     if (this.m_outputStream == null)
/*     */     {
/* 329 */       return;
/*     */     }
/*     */ 
/* 332 */     String encodedStr = new String(this.m_encodedFileBytes, 0, this.m_curEncodedByte);
/* 333 */     int[] len = new int[1];
/* 334 */     byte[] decodedBytes = CommonDataConversion.uudecode(encodedStr, len);
/* 335 */     this.m_outputStream.write(decodedBytes, 0, len[0]);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 340 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94829 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.SoapXmlFileParser
 * JD-Core Version:    0.5.4
 */