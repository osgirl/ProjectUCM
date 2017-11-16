/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class DataStreamWrapper
/*     */ {
/*  36 */   public static int m_bufferSize = 10000;
/*     */ 
/*  40 */   public String m_streamId = null;
/*     */ 
/*  48 */   public boolean m_useStream = false;
/*     */ 
/*  53 */   public Object m_descriptor = null;
/*     */ 
/*  61 */   public boolean m_isSimpleFileStream = false;
/*     */ 
/*  68 */   public String m_filePath = null;
/*     */ 
/*  74 */   public String m_entryName = null;
/*     */ 
/*  79 */   public String m_clientFileName = null;
/*     */ 
/*  83 */   public String m_dataType = null;
/*     */ 
/*  89 */   public String m_dataEncoding = null;
/*     */ 
/*  94 */   public boolean m_determinedExistence = false;
/*     */ 
/* 100 */   public boolean m_streamLocationExists = false;
/*     */ 
/* 104 */   public boolean m_hasStreamLength = false;
/*     */ 
/* 108 */   public long m_streamLength = 0L;
/*     */ 
/* 112 */   public long m_remainingBytes = 0L;
/*     */ 
/* 118 */   public boolean m_isWritingBytes = false;
/*     */ 
/* 123 */   public boolean m_isReadingBytes = false;
/*     */ 
/* 128 */   public boolean m_outStreamActive = false;
/*     */ 
/* 132 */   public OutputStream m_outStream = null;
/*     */ 
/* 137 */   public boolean m_inStreamActive = false;
/*     */ 
/* 141 */   public InputStream m_inStream = null;
/*     */ 
/* 145 */   public byte[] m_streamingBuffer = null;
/*     */ 
/* 149 */   public int m_startBufferByte = 0;
/*     */ 
/* 153 */   public int m_numBufferBytes = 0;
/*     */ 
/* 158 */   public Map m_streamArgs = null;
/*     */ 
/* 163 */   public Map m_cachedStorageData = null;
/*     */ 
/* 170 */   public Object m_streamData = null;
/*     */ 
/*     */   public DataStreamWrapper()
/*     */   {
/*     */   }
/*     */ 
/*     */   public DataStreamWrapper(Object descriptor)
/*     */   {
/* 179 */     this.m_descriptor = descriptor;
/*     */   }
/*     */ 
/*     */   public DataStreamWrapper(String id, Object descriptor, String filePath, String clientFileName)
/*     */   {
/* 186 */     this.m_streamId = id;
/* 187 */     this.m_filePath = filePath;
/* 188 */     this.m_descriptor = descriptor;
/* 189 */     this.m_clientFileName = clientFileName;
/*     */   }
/*     */ 
/*     */   public DataStreamWrapper(String filePath, String clientFileName, String format)
/*     */   {
/* 196 */     setSimpleFileData(filePath, clientFileName, format);
/*     */   }
/*     */ 
/*     */   public DataStreamWrapper(String filePath, String clientFileName, String format, String encoding)
/*     */   {
/* 205 */     setSimpleFileData(filePath, clientFileName, format);
/* 206 */     this.m_dataEncoding = encoding;
/*     */ 
/* 209 */     this.m_isSimpleFileStream = false;
/*     */   }
/*     */ 
/*     */   public void setSimpleFilePath(String filePath)
/*     */   {
/* 218 */     this.m_filePath = filePath;
/* 219 */     this.m_isSimpleFileStream = true;
/*     */   }
/*     */ 
/*     */   public void setSimpleFileData(String filePath, String clientFileName, String format)
/*     */   {
/* 227 */     this.m_streamId = filePath;
/* 228 */     setSimpleFilePath(filePath);
/* 229 */     this.m_descriptor = filePath;
/* 230 */     this.m_clientFileName = clientFileName;
/* 231 */     this.m_dataType = format;
/*     */   }
/*     */ 
/*     */   public void initWithInputStream(InputStream in, long len)
/*     */   {
/* 238 */     this.m_inStream = in;
/* 239 */     this.m_inStreamActive = true;
/* 240 */     this.m_streamLength = len;
/* 241 */     this.m_hasStreamLength = true;
/* 242 */     this.m_remainingBytes = len;
/* 243 */     this.m_useStream = true;
/*     */   }
/*     */ 
/*     */   public void initWithOutputStream(OutputStream out, long len)
/*     */   {
/* 251 */     this.m_outStream = out;
/* 252 */     this.m_outStreamActive = true;
/* 253 */     this.m_streamLength = len;
/* 254 */     this.m_hasStreamLength = true;
/* 255 */     this.m_remainingBytes = len;
/* 256 */     this.m_useStream = true;
/*     */   }
/*     */ 
/*     */   public void checkCreateStreamingBuffer(DataStreamWrapper streamWrapper)
/*     */   {
/* 263 */     if (this.m_streamingBuffer != null)
/*     */       return;
/* 265 */     this.m_streamingBuffer = createBuffer(m_bufferSize);
/* 266 */     this.m_startBufferByte = 0;
/* 267 */     this.m_numBufferBytes = 0;
/*     */   }
/*     */ 
/*     */   public byte[] createBuffer(int size)
/*     */   {
/* 275 */     return new byte[size];
/*     */   }
/*     */ 
/*     */   public void releaseBuffer(byte[] buf)
/*     */   {
/*     */   }
/*     */ 
/*     */   public DataStreamWrapper shallowClone()
/*     */   {
/* 294 */     DataStreamWrapper newWrapper = new DataStreamWrapper(this.m_streamId, this.m_descriptor, this.m_filePath, this.m_clientFileName);
/*     */ 
/* 296 */     newWrapper.m_entryName = this.m_entryName;
/* 297 */     newWrapper.m_dataType = this.m_dataType;
/* 298 */     newWrapper.m_useStream = newWrapper.m_useStream;
/* 299 */     newWrapper.m_streamArgs = newWrapper.m_streamArgs;
/* 300 */     newWrapper.m_streamData = newWrapper.m_streamData;
/* 301 */     newWrapper.m_cachedStorageData = this.m_cachedStorageData;
/*     */ 
/* 303 */     return newWrapper;
/*     */   }
/*     */ 
/*     */   public void release()
/*     */   {
/* 313 */     this.m_inStream = null;
/* 314 */     this.m_inStreamActive = false;
/* 315 */     this.m_outStream = null;
/* 316 */     this.m_outStreamActive = false;
/* 317 */     this.m_isWritingBytes = false;
/* 318 */     this.m_isReadingBytes = false;
/* 319 */     if (this.m_streamingBuffer != null)
/*     */     {
/* 321 */       releaseBuffer(this.m_streamingBuffer);
/* 322 */       this.m_streamingBuffer = null;
/*     */     }
/*     */ 
/* 328 */     this.m_descriptor = null;
/* 329 */     this.m_streamArgs = null;
/* 330 */     this.m_streamData = null;
/* 331 */     this.m_cachedStorageData = null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 336 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82427 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DataStreamWrapper
 * JD-Core Version:    0.5.4
 */