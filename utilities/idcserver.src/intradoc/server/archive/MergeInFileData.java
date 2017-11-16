/*    */ package intradoc.server.archive;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.FieldInfo;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.filestore.FileStoreProvider;
/*    */ import intradoc.filestore.FileStoreProviderHelper;
/*    */ import intradoc.filestore.IdcFileDescriptor;
/*    */ 
/*    */ public class MergeInFileData
/*    */ {
/*    */   public DataBinder m_docData;
/*    */   public DataResultSet m_docSet;
/*    */   public DataResultSet m_batchSet;
/*    */   public FieldInfo[] m_revInfoFields;
/*    */   public Workspace m_workspace;
/*    */   public DataBinder m_archiveData;
/*    */   public String m_archiveDir;
/*    */   public String m_archiveExportDir;
/*    */   public ExecutionContext m_cxt;
/*    */   public DataBinder m_batchData;
/*    */   public String m_tsDir;
/*    */   public String[] m_docFieldNames;
/*    */   public FieldInfo[] m_docFieldInfos;
/* 42 */   public final int IS_PRIMARY_INDEX = 0;
/* 43 */   public final int IS_WEB_FORMAT_INDEX = 1;
/* 44 */   public final int EXTENSION_INDEX = 2;
/* 45 */   public final int ORIGINAL_NAME_INDEX = 3;
/* 46 */   public final int FORMAT_INDEX = 4;
/*    */   public boolean m_isPrimary;
/*    */   public boolean m_isWebFormat;
/*    */   public boolean m_isInVault;
/*    */   public String m_extension;
/*    */   public String m_format;
/*    */   public String m_originalName;
/*    */   public boolean m_isCopyWebDocs;
/*    */   public boolean m_didCalculateFileNames;
/*    */   public boolean m_didCreateSpecificArchiveDirectory;
/*    */   public boolean m_didCalculateDirectoryNames;
/*    */   public boolean m_didAddFileInformationToDatabase;
/*    */   public boolean m_didCopyFile;
/*    */   public String m_fileName;
/*    */   public String m_filePath;
/*    */   public String m_newFileName;
/*    */   public String m_relativeBatchDocDirSuffix;
/*    */   public String m_relativeNewFilePath;
/*    */   public boolean m_doSimpleFileCopy;
/*    */   public boolean m_didCreateSourceFileDescriptor;
/*    */   public IdcFileDescriptor m_sourceDescriptor;
/*    */   public FileStoreProvider m_fileStore;
/*    */   public FileStoreProviderHelper m_fileUtils;
/*    */ 
/*    */   public MergeInFileData(DataResultSet docSet, Workspace ws, DataBinder data, ExecutionContext cxt)
/*    */   {
/* 77 */     this.m_docSet = docSet;
/* 78 */     this.m_workspace = ws;
/* 79 */     this.m_archiveData = data;
/* 80 */     this.m_cxt = cxt;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 85 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97046 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.MergeInFileData
 * JD-Core Version:    0.5.4
 */