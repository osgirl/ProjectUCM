/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.RandomAccessConfigFile;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.jdbc.JdbcResultSet;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.sql.Clob;
/*     */ import java.util.Date;
/*     */ 
/*     */ public class IdcConfigRandomAccess extends RandomAccessConfigFile
/*     */ {
/*  38 */   public Workspace m_workspace = null;
/*  39 */   public IdcConfigFile m_file = null;
/*     */   public String m_fileID;
/*  41 */   public long m_pos = 1L;
/*  42 */   public Clob m_content = null;
/*  43 */   public String m_flags = null;
/*     */ 
/*     */   public IdcConfigRandomAccess(File file, String flags, Workspace workspace)
/*     */     throws FileNotFoundException, IllegalArgumentException
/*     */   {
/*  48 */     this.m_workspace = workspace;
/*  49 */     this.m_file = ((IdcConfigFile)file);
/*  50 */     this.m_fileID = this.m_file.m_fileID;
/*  51 */     this.m_flags = flags;
/*     */ 
/*  53 */     if ((!this.m_flags.equalsIgnoreCase("r")) && (!this.m_flags.equalsIgnoreCase("rw")) && (!this.m_flags.equalsIgnoreCase("rws")) && (!this.m_flags.equalsIgnoreCase("rwd")))
/*     */     {
/*  56 */       throw new IllegalArgumentException();
/*     */     }
/*     */ 
/*  59 */     if ((this.m_flags.equalsIgnoreCase("r")) && (!this.m_file.exists()))
/*     */     {
/*  61 */       throw new FileNotFoundException("Cannot find IdcConfigRandomAccess file " + this.m_fileID);
/*     */     }
/*  63 */     if ((!this.m_flags.contains("w")) || (this.m_file.exists()))
/*     */       return;
/*  65 */     this.m_file.write("");
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/*  72 */     this.m_pos = 0L;
/*  73 */     this.m_fileID = null;
/*  74 */     this.m_file = null;
/*     */   }
/*     */ 
/*     */   public long length()
/*     */     throws IOException
/*     */   {
/*  80 */     return this.m_file.length();
/*     */   }
/*     */ 
/*     */   public int read(byte[] b)
/*     */     throws IOException
/*     */   {
/*  86 */     return read(b, 0, b.length);
/*     */   }
/*     */ 
/*     */   public int read(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/*  94 */       if (this.m_content == null)
/*     */       {
/*  97 */         DataBinder binder = new DataBinder();
/*  98 */         binder.putLocal("dRTFileID", this.m_fileID);
/*  99 */         JdbcResultSet rset = (JdbcResultSet)this.m_workspace.createResultSet("QtextbyIDRunTimeConfigData", binder);
/*     */ 
/* 103 */         if (rset.isEmpty())
/*     */         {
/* 105 */           String msg = LocaleUtils.encodeMessage("csIdcConfigRandomAccessNoResource", null, this.m_fileID);
/* 106 */           Report.error(null, msg, null);
/* 107 */           return -1;
/*     */         }
/*     */ 
/* 110 */         this.m_content = rset.m_resultSet.getClob("dRTTextObject");
/* 111 */         if (this.m_content == null)
/*     */         {
/* 113 */           return -1;
/*     */         }
/*     */       }
/*     */ 
/* 117 */       String str = this.m_content.getSubString(this.m_pos, len);
/* 118 */       byte[] buf = str.getBytes();
/* 119 */       if (buf.length > 0)
/*     */       {
/* 121 */         System.arraycopy(buf, 0, b, off, buf.length);
/*     */ 
/* 123 */         this.m_pos += buf.length;
/* 124 */         return buf.length;
/*     */       }
/* 126 */       return -1;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 130 */       String msg = LocaleUtils.encodeMessage("csIdcConfigRandomAccessReadError", e.getMessage(), this.m_fileID);
/* 131 */       Report.error(null, msg, e);
/* 132 */     }return -1;
/*     */   }
/*     */ 
/*     */   public void seek(long pos)
/*     */     throws IOException
/*     */   {
/* 143 */     this.m_pos = (pos + 1L);
/*     */   }
/*     */ 
/*     */   public void write(byte[] b)
/*     */     throws IOException
/*     */   {
/* 149 */     if (!this.m_flags.contains("w"))
/*     */     {
/* 151 */       throw new IOException("Don't have write permission on IdcConfigRandomAccess file " + this.m_fileID);
/*     */     }
/*     */ 
/* 154 */     DataBinder binder = new DataBinder();
/* 155 */     binder.putLocal("dRTFileID", this.m_fileID);
/*     */ 
/* 157 */     boolean isInTran = false;
/*     */     try
/*     */     {
/* 161 */       this.m_workspace.beginTran();
/* 162 */       isInTran = true;
/*     */ 
/* 165 */       this.m_workspace.execute("UfileIDRunTimeConfigData", binder);
/*     */ 
/* 167 */       JdbcResultSet rset = (JdbcResultSet)this.m_workspace.createResultSet("QtextbyIDRunTimeConfigData", binder);
/*     */ 
/* 171 */       if (rset.isEmpty())
/*     */       {
/* 173 */         String msg = LocaleUtils.encodeMessage("csIdcConfigRandomAccessNoResource", null, this.m_fileID);
/* 174 */         if (SystemUtils.m_verbose)
/*     */         {
/* 176 */           Report.trace(null, msg, null);
/*     */         }
/*     */ 
/*     */         return;
/*     */       }
/*     */ 
/* 182 */       Clob clob = rset.m_resultSet.getClob("dRTTextObject");
/* 183 */       Date date = new Date();
/* 184 */       this.m_file.m_lastModified = date.getTime();
/* 185 */       String dateStr = LocaleUtils.formatODBC(date);
/* 186 */       if (clob == null)
/*     */       {
/* 191 */         String str = new String(b);
/* 192 */         binder.putLocal("dRTLastModified", dateStr);
/* 193 */         binder.putLocal("dRTTextObject", str);
/* 194 */         this.m_workspace.execute("UtextbyIDRunTimeConfigData", binder);
/*     */       }
/*     */       else
/*     */       {
/* 199 */         clob.setString(this.m_pos, new String(b));
/*     */ 
/* 201 */         binder.putLocal("dRTLastModified", dateStr);
/* 202 */         this.m_workspace.execute("UlastModifiedbyIDRunTimeConfigData", binder);
/*     */ 
/* 205 */         this.m_file.m_isUpdate = true;
/*     */       }
/*     */ 
/* 208 */       isInTran = false;
/* 209 */       this.m_workspace.commitTran();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 214 */       String msg = LocaleUtils.encodeMessage("csIdcConfigRandomAccessWriteError", e.getMessage(), this.m_fileID);
/* 215 */       Report.error(null, msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 219 */       if (isInTran)
/*     */       {
/* 221 */         isInTran = false;
/* 222 */         this.m_workspace.rollbackTran();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 229 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99359 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdcConfigRandomAccess
 * JD-Core Version:    0.5.4
 */