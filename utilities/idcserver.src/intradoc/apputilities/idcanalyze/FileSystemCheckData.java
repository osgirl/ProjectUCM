/*     */ package intradoc.apputilities.idcanalyze;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.filestore.FileStoreProviderHelper;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.lang.Queue;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class FileSystemCheckData
/*     */ {
/*     */   protected Hashtable m_nameHash;
/*     */   protected Queue m_nameQueue;
/*     */   protected String m_tempDir;
/*     */   protected Properties m_context;
/*     */   protected ExecutionContext m_eContext;
/*     */   protected int m_vaultFileSize;
/*     */   protected int m_maxHashNum;
/*     */   protected int m_numWebHash;
/*     */ 
/*     */   public FileSystemCheckData(Properties cxt)
/*     */   {
/*  56 */     this.m_nameHash = new Hashtable();
/*     */ 
/*  58 */     this.m_nameQueue = new Queue();
/*     */ 
/*  60 */     this.m_context = cxt;
/*  61 */     this.m_tempDir = ((String)this.m_context.get("TempDir"));
/*     */ 
/*  63 */     String envStr = (String)this.m_context.get("VaultFileSize");
/*  64 */     this.m_vaultFileSize = NumberUtils.parseInteger(envStr, 1000);
/*  65 */     envStr = (String)this.m_context.get("MaxHashNum");
/*  66 */     this.m_maxHashNum = NumberUtils.parseInteger(envStr, 15);
/*  67 */     envStr = (String)this.m_context.get("NumWebHash");
/*  68 */     this.m_numWebHash = NumberUtils.parseInteger(envStr, 10);
/*     */ 
/*  70 */     this.m_eContext = new ExecutionContextAdaptor();
/*     */   }
/*     */ 
/*     */   public boolean lookupFile(String filePath, boolean isVault, FileStoreProviderHelper fileHelper)
/*     */     throws DataException
/*     */   {
/*  77 */     Properties props = new Properties();
/*     */ 
/*  79 */     if (filePath == null)
/*     */     {
/*  81 */       return false;
/*     */     }
/*     */ 
/*  84 */     if (!IdcAnalyzeUtils.parsePropertiesFromPath(filePath, props, isVault, fileHelper))
/*     */     {
/*  86 */       return false;
/*     */     }
/*     */ 
/*  89 */     DataBinder binder = null;
/*     */     try
/*     */     {
/*  93 */       binder = retrieveDataBinder(props, filePath, false, isVault);
/*     */     }
/*     */     catch (ServiceException s)
/*     */     {
/*  97 */       return false;
/*     */     }
/*     */ 
/* 100 */     if (binder == null)
/*     */     {
/* 102 */       return false;
/*     */     }
/*     */ 
/* 105 */     DataResultSet drset = (DataResultSet)binder.getResultSet("FileList");
/*     */ 
/* 107 */     int index = filePath.lastIndexOf("/");
/* 108 */     if (index < 0)
/*     */     {
/* 110 */       return false;
/*     */     }
/* 112 */     String key = filePath.substring(index + 1);
/*     */ 
/* 114 */     if ((key == null) || (drset == null))
/*     */     {
/* 116 */       return false;
/*     */     }
/*     */ 
/* 119 */     drset.findRow(0, key);
/* 120 */     Properties oldProps = drset.getCurrentRowProps();
/*     */ 
/* 122 */     if (oldProps == null)
/*     */     {
/* 124 */       return false;
/*     */     }
/* 126 */     for (Enumeration e = oldProps.keys(); e.hasMoreElements(); )
/*     */     {
/* 132 */       String field = (String)e.nextElement();
/* 133 */       String oldValue = (String)oldProps.get(field);
/* 134 */       String newValue = (String)props.get(field);
/* 135 */       if ((newValue == null) || (oldValue == null))
/*     */       {
/* 137 */         return false;
/*     */       }
/* 139 */       if (!oldValue.equals(newValue))
/*     */       {
/* 141 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 145 */     drset.deleteCurrentRow();
/* 146 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean sortDirContents(String dir, String[] fileList, Hashtable fileHash, Vector nonFiles, boolean isVault, FileStoreProviderHelper fileHelper)
/*     */   {
/* 152 */     if ((dir == null) || (fileHash == null) || (fileList == null) || (nonFiles == null))
/*     */     {
/* 154 */       return false;
/*     */     }
/*     */ 
/* 157 */     Properties props = new Properties();
/* 158 */     for (int i = 0; i < fileList.length; ++i)
/*     */     {
/* 160 */       String filePath = dir + fileList[i];
/*     */ 
/* 163 */       if (!IdcAnalyzeUtils.parsePropertiesFromPath(filePath, props, isVault, fileHelper))
/*     */       {
/* 165 */         nonFiles.addElement(fileList[i]);
/*     */       }
/*     */       else
/*     */       {
/* 169 */         String hashStr = computeHashString(props, isVault);
/* 170 */         if (hashStr == null)
/*     */         {
/* 172 */           nonFiles.addElement(fileList[i]);
/*     */         }
/*     */         else {
/* 175 */           Vector sortedList = (Vector)fileHash.get(hashStr);
/*     */ 
/* 177 */           if (sortedList == null)
/*     */           {
/* 179 */             sortedList = new IdcVector();
/* 180 */             fileHash.put(hashStr, sortedList);
/*     */           }
/* 182 */           sortedList.addElement(fileList[i]);
/*     */         }
/*     */       }
/*     */     }
/* 184 */     return true;
/*     */   }
/*     */ 
/*     */   public void addFile(Properties revProps, Object file, boolean isVault)
/*     */   {
/* 189 */     Exception e = null;
/*     */     try
/*     */     {
/* 192 */       DataBinder binder = retrieveDataBinder(revProps, file, true, isVault);
/* 193 */       addToBinder(binder, revProps, file, isVault);
/*     */     }
/*     */     catch (DataException de)
/*     */     {
/* 197 */       e = de;
/*     */     }
/*     */     catch (ServiceException se)
/*     */     {
/* 201 */       e = se;
/*     */     }
/* 203 */     if (e == null)
/*     */       return;
/* 205 */     Report.trace(null, LocaleResources.localizeMessage(LocaleUtils.appendMessage(e.getMessage(), "!syGeneralError"), null), e);
/*     */   }
/*     */ 
/*     */   protected void addToBinder(DataBinder binder, Properties revProps, Object file, boolean isVault)
/*     */     throws ServiceException, DataException
/*     */   {
/* 213 */     if (binder == null)
/*     */     {
/* 215 */       throw new ServiceException("!csIDCAnalyzePathError");
/*     */     }
/*     */ 
/* 218 */     String fileName = "<unknown>";
/* 219 */     if (file instanceof File)
/*     */     {
/* 221 */       String path = ((File)file).getAbsolutePath();
/* 222 */       int nameIndex = path.lastIndexOf("/");
/* 223 */       if (nameIndex >= 0)
/*     */       {
/* 225 */         fileName = path.substring(nameIndex + 1);
/* 226 */       }fileName = fileName.toLowerCase();
/*     */     }
/* 228 */     else if (file instanceof IdcFileDescriptor)
/*     */     {
/* 230 */       IdcFileDescriptor d = (IdcFileDescriptor)file;
/* 231 */       fileName = d.getProperty("uniqueId");
/*     */     }
/*     */ 
/* 235 */     DataResultSet drset = (DataResultSet)binder.getResultSet("FileList");
/* 236 */     String docAccount = (String)revProps.get("dDocAccount");
/* 237 */     String docType = (String)revProps.get("dDocType");
/* 238 */     Vector v = drset.createEmptyRow();
/* 239 */     v.setElementAt(fileName, 0);
/* 240 */     v.setElementAt(docAccount.toLowerCase(), 1);
/* 241 */     v.setElementAt(docType.toLowerCase(), 2);
/* 242 */     drset.addRow(v);
/*     */   }
/*     */ 
/*     */   protected DataBinder retrieveDataBinder(Properties props, Object file, boolean createIfMissing, boolean isVault)
/*     */     throws ServiceException
/*     */   {
/* 248 */     String hashStr = computeHashString(props, isVault);
/*     */ 
/* 250 */     if (hashStr == null)
/*     */     {
/* 252 */       return null;
/*     */     }
/* 254 */     DataBinder tmpBinder = (DataBinder)this.m_nameHash.get(hashStr);
/*     */ 
/* 256 */     if (tmpBinder == null)
/*     */     {
/* 260 */       if (this.m_nameHash.size() == this.m_maxHashNum)
/*     */       {
/* 262 */         String oldest = (String)this.m_nameQueue.remove();
/* 263 */         DataBinder tmp = (DataBinder)this.m_nameHash.get(oldest);
/*     */ 
/* 265 */         String location = tmp.getLocal("Directory");
/* 266 */         String name = tmp.getLocal("FileName");
/* 267 */         ResourceUtils.serializeDataBinder(this.m_tempDir + location, name, tmp, true, true);
/*     */ 
/* 270 */         this.m_nameHash.remove(oldest);
/*     */       }
/*     */ 
/* 273 */       boolean cacheFileMissing = false;
/*     */       try
/*     */       {
/* 276 */         FileUtils.validateFile(this.m_tempDir + hashStr, "foo");
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 280 */         cacheFileMissing = true;
/*     */       }
/*     */ 
/* 283 */       if (!cacheFileMissing)
/*     */       {
/*     */         try
/*     */         {
/* 287 */           tmpBinder = ResourceLoader.loadDataBinderFromFile(this.m_tempDir + hashStr);
/*     */         }
/*     */         catch (ServiceException s)
/*     */         {
/* 293 */           throw new ServiceException("!csIDCAnalyzeGetDataBinderError", s);
/*     */         }
/*     */       }
/* 296 */       else if (createIfMissing)
/*     */       {
/* 300 */         tmpBinder = createFileBinder();
/* 301 */         tmpBinder.putLocal("Hash", hashStr);
/* 302 */         String fileName = FileUtils.getName(hashStr);
/* 303 */         String dir = FileUtils.directorySlashes(FileUtils.getDirectory(hashStr));
/* 304 */         tmpBinder.putLocal("Directory", dir);
/* 305 */         tmpBinder.putLocal("FileName", fileName);
/* 306 */         FileUtils.checkOrCreateSubDirectory(this.m_tempDir, dir);
/*     */       }
/*     */ 
/* 310 */       if (tmpBinder != null)
/*     */       {
/* 312 */         this.m_nameHash.put(hashStr, tmpBinder);
/* 313 */         this.m_nameQueue.insert(hashStr);
/*     */       }
/*     */     }
/* 316 */     return tmpBinder;
/*     */   }
/*     */ 
/*     */   protected DataBinder createFileBinder()
/*     */   {
/* 321 */     String[] fields = { "FileName", "dDocAccount", "dDocType" };
/* 322 */     DataResultSet drset = new DataResultSet(fields);
/*     */ 
/* 324 */     DataBinder binder = new DataBinder();
/* 325 */     binder.addResultSet("FileList", drset);
/* 326 */     return binder;
/*     */   }
/*     */ 
/*     */   protected String computeWebHashDir(Properties revProps)
/*     */   {
/* 331 */     String dDocAccount = (String)revProps.get("dDocAccount");
/* 332 */     String dSecurityGroup = (String)revProps.get("dSecurityGroup");
/* 333 */     if (dSecurityGroup == null)
/*     */     {
/* 335 */       return null;
/*     */     }
/*     */ 
/* 338 */     String tempDir = "weblayout/";
/* 339 */     dSecurityGroup = dSecurityGroup.toLowerCase();
/* 340 */     tempDir = tempDir + dSecurityGroup + "/";
/*     */ 
/* 342 */     int acctHash = 0;
/* 343 */     if (dDocAccount != null)
/*     */     {
/* 345 */       dDocAccount = dDocAccount.toLowerCase();
/* 346 */       acctHash = dDocAccount.hashCode();
/* 347 */       tempDir = tempDir + "acct" + acctHash + "/";
/*     */     }
/* 349 */     return tempDir;
/*     */   }
/*     */ 
/*     */   protected String computeWebHashFile(Properties revProps)
/*     */   {
/* 354 */     String dDocType = (String)revProps.get("dDocType");
/* 355 */     String dDocName = (String)revProps.get("dDocName");
/* 356 */     if ((dDocType == null) || (dDocName == null))
/*     */     {
/* 358 */       return null;
/*     */     }
/*     */ 
/* 361 */     dDocType = dDocType.toLowerCase();
/* 362 */     int docTypeHash = dDocType.hashCode();
/*     */ 
/* 364 */     dDocName = dDocName.toLowerCase();
/* 365 */     int docNameHash = dDocType.hashCode() % this.m_numWebHash;
/*     */ 
/* 367 */     String hashStr = "" + docTypeHash + docNameHash;
/* 368 */     return "web" + hashStr + ".hda";
/*     */   }
/*     */ 
/*     */   protected String computeVaultHashDir(Properties revProps)
/*     */   {
/* 373 */     return "vault/";
/*     */   }
/*     */ 
/*     */   protected String computeVaultHashFile(Properties revProps)
/*     */   {
/* 378 */     String dID = (String)revProps.get("dID");
/*     */ 
/* 380 */     int dIdInt = NumberUtils.parseInteger(dID, 0) / this.m_vaultFileSize;
/*     */ 
/* 382 */     String hashStr = "" + dIdInt;
/* 383 */     return "vault" + hashStr + ".hda";
/*     */   }
/*     */ 
/*     */   protected String computeHashString(Properties props, boolean isVault)
/*     */   {
/* 388 */     String hashDir = null;
/* 389 */     String hashFile = null;
/*     */ 
/* 391 */     if (props == null)
/*     */     {
/* 393 */       return null;
/*     */     }
/*     */ 
/* 396 */     if (isVault)
/*     */     {
/* 398 */       hashDir = computeVaultHashDir(props);
/* 399 */       hashFile = computeVaultHashFile(props);
/*     */     }
/*     */     else
/*     */     {
/* 403 */       hashDir = computeWebHashDir(props);
/* 404 */       hashFile = computeWebHashFile(props);
/*     */     }
/* 406 */     if ((hashDir == null) || (hashFile == null))
/*     */     {
/* 408 */       return null;
/*     */     }
/* 410 */     return hashDir + hashFile.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 415 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idcanalyze.FileSystemCheckData
 * JD-Core Version:    0.5.4
 */