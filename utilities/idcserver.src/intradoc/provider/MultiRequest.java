/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Random;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class MultiRequest
/*     */ {
/*  33 */   protected DataBinder m_binder = null;
/*  34 */   protected String m_boundary = null;
/*  35 */   protected String m_breakStr = null;
/*     */ 
/*  37 */   protected String m_postStr = null;
/*  38 */   protected String m_attachStreamStr = null;
/*     */ 
/*  40 */   protected Vector m_files = null;
/*  41 */   protected ReportProgress m_progress = null;
/*  42 */   protected boolean m_adjustLengthForExtraBytes = true;
/*     */ 
/*     */   public MultiRequest(DataBinder binder)
/*     */   {
/*  46 */     this.m_binder = binder;
/*     */   }
/*     */ 
/*     */   public boolean getAdjustLengthForExtraBytes()
/*     */   {
/*  51 */     return this.m_adjustLengthForExtraBytes;
/*     */   }
/*     */ 
/*     */   public void setAdjustLengthForExtraBytes(boolean adjustLengthForExtraBytes)
/*     */   {
/*  56 */     this.m_adjustLengthForExtraBytes = adjustLengthForExtraBytes;
/*     */   }
/*     */ 
/*     */   public void prepareMultiPartPost() throws DataException
/*     */   {
/*  61 */     Properties props = this.m_binder.getLocalData();
/*  62 */     prepareBoundary();
/*  63 */     createPostAndFileStr(props);
/*     */ 
/*  65 */     String holdKey = this.m_binder.m_suspendedFileKey;
/*  66 */     if ((holdKey != null) && (this.m_binder.m_isSuspended))
/*     */     {
/*  68 */       createAttachStreamHeader(holdKey);
/*     */     }
/*     */ 
/*  74 */     if ((this.m_binder.m_extraTailLength > 0) && (this.m_adjustLengthForExtraBytes))
/*     */     {
/*  76 */       this.m_binder.m_remainingLength += this.m_binder.m_extraTailLength;
/*  77 */       this.m_binder.m_extraTailLength = 0;
/*     */     }
/*     */ 
/*  82 */     this.m_binder.m_isSuspended = false;
/*     */   }
/*     */ 
/*     */   public void prepareBoundary()
/*     */   {
/*  88 */     this.m_boundary = this.m_binder.getEnvironmentValue("BOUNDARY");
/*     */ 
/*  90 */     if (this.m_boundary == null)
/*     */     {
/*  92 */       Random gen = new Random();
/*  93 */       this.m_boundary = ("----------------" + String.valueOf(gen.nextLong()));
/*     */     }
/*  95 */     this.m_breakStr = ("--" + this.m_boundary + "\r\n");
/*     */   }
/*     */ 
/*     */   public void createPostAndFileStr(Properties props)
/*     */   {
/* 100 */     this.m_files = new IdcVector();
/* 101 */     StringBuffer buff = new StringBuffer();
/* 102 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*     */     {
/* 104 */       String key = (String)en.nextElement();
/* 105 */       String value = props.getProperty(key);
/*     */ 
/* 107 */       String filename = null;
/* 108 */       int index = key.indexOf(":path");
/* 109 */       if (index > 0)
/*     */       {
/* 111 */         key = key.substring(0, index);
/* 112 */         filename = value;
/* 113 */         String v = props.getProperty(key);
/* 114 */         if (v != null)
/*     */         {
/* 116 */           value = v;
/*     */         }
/*     */ 
/* 119 */         props.remove(key);
/*     */       }
/*     */       else
/*     */       {
/* 123 */         filename = props.getProperty(key + ":path");
/* 124 */         if (filename != null)
/*     */         {
/* 126 */           props.remove(key + ":path");
/*     */         }
/*     */       }
/*     */ 
/* 130 */       if (filename != null)
/*     */       {
/* 132 */         createFileInfo(key, value, filename);
/*     */       }
/*     */       else
/*     */       {
/* 136 */         buff.append(this.m_breakStr);
/* 137 */         buff.append("name=\"");
/* 138 */         buff.append(key);
/* 139 */         buff.append("\"\r\n\r\n");
/* 140 */         buff.append(value);
/* 141 */         buff.append("\r\n");
/*     */       }
/*     */     }
/* 144 */     this.m_postStr = buff.toString();
/*     */   }
/*     */ 
/*     */   public void createFileInfo(String key, String value, String filename)
/*     */   {
/* 149 */     FileInfo fileInfo = new FileInfo(key, filename);
/* 150 */     this.m_files.addElement(fileInfo);
/*     */ 
/* 152 */     String fileStr = this.m_breakStr;
/* 153 */     fileStr = fileStr + "name=\"";
/* 154 */     fileStr = fileStr + key;
/* 155 */     fileStr = fileStr + "\"; filename=\"";
/* 156 */     fileStr = fileStr + value;
/* 157 */     fileStr = fileStr + "\"\r\n";
/* 158 */     fileStr = fileStr + "Content-Type: text/plain";
/* 159 */     fileStr = fileStr + "\r\n\r\n";
/*     */ 
/* 161 */     fileInfo.m_filePostStr = fileStr;
/*     */   }
/*     */ 
/*     */   public void createAttachStreamHeader(String key)
/*     */   {
/* 166 */     String filename = this.m_binder.getLocal(key);
/* 167 */     if (filename == null)
/*     */     {
/* 169 */       return;
/*     */     }
/*     */ 
/* 172 */     this.m_attachStreamStr = this.m_breakStr;
/* 173 */     this.m_attachStreamStr += "name=\"";
/* 174 */     this.m_attachStreamStr += key;
/* 175 */     this.m_attachStreamStr += "\"; filename=\"";
/* 176 */     this.m_attachStreamStr += filename;
/* 177 */     this.m_attachStreamStr += "\"\r\n";
/*     */   }
/*     */ 
/*     */   public void sendMultiPartPost(OutputStream os) throws IOException, DataException, ServiceException
/*     */   {
/* 182 */     String encoding = this.m_binder.m_javaEncoding;
/* 183 */     os.write(StringUtils.getBytes(this.m_postStr, encoding));
/* 184 */     sendFiles(os);
/* 185 */     if (this.m_attachStreamStr != null)
/*     */     {
/* 187 */       attachStream(os, this.m_binder.m_inStream);
/*     */     }
/*     */     else
/*     */     {
/* 191 */       os.write(StringUtils.getBytes(this.m_breakStr, encoding));
/*     */ 
/* 193 */       String theEnd = "$$\n";
/* 194 */       os.write(StringUtils.getBytes(theEnd, encoding));
/*     */     }
/*     */ 
/* 197 */     os.flush();
/*     */   }
/*     */ 
/*     */   public void populateEnv(Properties requestProps) throws DataException
/*     */   {
/* 202 */     String contentType = this.m_binder.getEnvironmentValue("CONTENT_TYPE");
/* 203 */     int type = 0;
/* 204 */     if (contentType != null)
/*     */     {
/* 206 */       type = DataSerializeUtils.determineContentType(this.m_binder, null);
/*     */     }
/* 208 */     switch (type)
/*     */     {
/*     */     case 1:
/* 212 */       break;
/*     */     default:
/* 216 */       requestProps.put("CONTENT_TYPE", "multipart/form-data; boundary=" + this.m_boundary);
/* 217 */       requestProps.put("REQUEST_METHOD", "POST");
/*     */     }
/*     */ 
/* 222 */     Properties envProps = this.m_binder.getEnvironment();
/* 223 */     for (Enumeration en = envProps.keys(); en.hasMoreElements(); )
/*     */     {
/* 225 */       String key = (String)en.nextElement();
/* 226 */       if (requestProps.get(key) == null)
/*     */       {
/* 228 */         String value = envProps.getProperty(key);
/* 229 */         requestProps.put(key, value);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public long countBytes() throws UnsupportedEncodingException
/*     */   {
/* 236 */     String encoding = this.m_binder.m_javaEncoding;
/* 237 */     long count = 0L;
/* 238 */     if (this.m_files != null)
/*     */     {
/* 240 */       int size = this.m_files.size();
/* 241 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 243 */         FileInfo fileInfo = (FileInfo)this.m_files.elementAt(i);
/* 244 */         if (fileInfo == null)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 249 */         String filename = fileInfo.m_filename;
/* 250 */         if ((filename != null) && (filename.length() != 0))
/*     */         {
/* 252 */           File aFile = new File(filename);
/* 253 */           count += aFile.length();
/*     */         }
/* 255 */         count += StringUtils.getBytes(fileInfo.m_filePostStr, encoding).length + 2;
/*     */       }
/*     */     }
/*     */ 
/* 259 */     count += StringUtils.getBytes(this.m_postStr, encoding).length;
/* 260 */     if (this.m_attachStreamStr != null)
/*     */     {
/* 262 */       count += StringUtils.getBytes(this.m_attachStreamStr, encoding).length + this.m_binder.m_remainingLength;
/*     */     }
/*     */     else
/*     */     {
/* 266 */       count += StringUtils.getBytes(this.m_breakStr, encoding).length + 3;
/*     */     }
/*     */ 
/* 269 */     return count;
/*     */   }
/*     */ 
/*     */   public String getDataBoundary()
/*     */   {
/* 274 */     return this.m_boundary;
/*     */   }
/*     */ 
/*     */   protected void attachStream(OutputStream outStream, InputStream inStream)
/*     */     throws DataException, IOException
/*     */   {
/* 280 */     outStream.write(StringUtils.getBytes(this.m_attachStreamStr, this.m_binder.m_javaEncoding));
/*     */ 
/* 282 */     long len = this.m_binder.m_remainingLength;
/* 283 */     byte[] byteBuf = new byte[1024];
/* 284 */     int readCount = 0;
/* 285 */     while (len > 0L)
/*     */     {
/* 287 */       int readLen = 1024;
/* 288 */       if (len < 1024L)
/*     */       {
/* 290 */         readLen = (int)len;
/*     */       }
/* 292 */       readCount = inStream.read(byteBuf, 0, readLen);
/* 293 */       outStream.write(byteBuf, 0, readCount);
/* 294 */       len -= readCount;
/*     */     }
/*     */ 
/* 297 */     if (this.m_binder.m_extraTailLength <= 0) {
/*     */       return;
/*     */     }
/*     */ 
/* 301 */     inStream.read(byteBuf, 0, this.m_binder.m_extraTailLength);
/* 302 */     this.m_binder.m_extraTailLength = 0;
/*     */   }
/*     */ 
/*     */   protected void sendFiles(OutputStream output)
/*     */     throws DataException, ServiceException
/*     */   {
/* 308 */     if (this.m_files == null)
/*     */     {
/* 310 */       return;
/*     */     }
/*     */ 
/* 313 */     String encoding = this.m_binder.m_javaEncoding;
/* 314 */     String end = "\r\n";
/*     */     try
/*     */     {
/* 317 */       int size = this.m_files.size();
/* 318 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 320 */         FileInfo fileInfo = (FileInfo)this.m_files.elementAt(i);
/* 321 */         if (fileInfo == null) {
/*     */           continue;
/*     */         }
/*     */ 
/* 325 */         output.write(StringUtils.getBytes(fileInfo.m_filePostStr, encoding));
/* 326 */         sendFile(output, fileInfo.m_filename);
/* 327 */         output.write(StringUtils.getBytes(end, encoding));
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 332 */       String msg = LocaleUtils.encodeMessage("csFileUploadError", e.getMessage());
/*     */ 
/* 334 */       throw new DataException(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void sendFile(OutputStream output, String filename)
/*     */     throws IOException, ServiceException
/*     */   {
/* 341 */     if ((filename == null) || (filename.equals("")))
/*     */     {
/* 343 */       return;
/*     */     }
/*     */ 
/* 346 */     File file = new File(filename);
/* 347 */     if (!file.exists())
/*     */     {
/* 349 */       String msg = LocaleUtils.encodeMessage("syFileDoesNotExist", null, filename);
/* 350 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 353 */     long length = file.length();
/*     */ 
/* 355 */     FileInputStream in = null;
/*     */     try
/*     */     {
/* 358 */       in = new FileInputStream(filename);
/*     */ 
/* 360 */       byte[] bArray = new byte[1024];
/* 361 */       int count = 0;
/* 362 */       long globalCount = 0L;
/* 363 */       while (in.available() > 0)
/*     */       {
/* 365 */         count = in.read(bArray);
/* 366 */         output.write(bArray, 0, count);
/* 367 */         globalCount += count;
/* 368 */         reportProgress(filename, (float)globalCount, (float)length);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 373 */       closeStream(in);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void closeStream(InputStream is)
/*     */   {
/*     */     try
/*     */     {
/* 381 */       if (is != null)
/*     */       {
/* 383 */         is.close();
/*     */       }
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 388 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 390 */       Report.debug("system", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setReportProgress(ReportProgress rp)
/*     */   {
/* 397 */     this.m_progress = rp;
/*     */   }
/*     */ 
/*     */   protected void reportProgress(String fileName, float amtDone, float total)
/*     */   {
/* 402 */     if (this.m_progress == null)
/*     */       return;
/* 404 */     String msg = LocaleUtils.encodeMessage("csSendingFile", null, fileName);
/* 405 */     this.m_progress.reportProgress(1, msg, amtDone, total);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 411 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 90487 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.MultiRequest
 * JD-Core Version:    0.5.4
 */