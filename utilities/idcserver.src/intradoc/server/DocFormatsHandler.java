/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocFormatsHandler extends ServiceHandler
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void setFormatAccessFlags()
/*     */     throws ServiceException, DataException
/*     */   {
/*  46 */     boolean setAccessFlags = StringUtils.convertToBool(this.m_binder.getLocal("setAccessFlags"), false);
/*     */ 
/*  48 */     if (!setAccessFlags)
/*     */       return;
/*  50 */     DataResultSet drsFormats = SharedObjects.getTable("DocFormats");
/*  51 */     drsFormats.mergeFields(new DataResultSet(new String[] { "canDelete" }));
/*  52 */     String[] fmtKeys = { "dFormat", "dConversion", "dDescription", "dIsEnabled", "idcComponentName", "overrideStatus", "isSystem", "canDelete" };
/*     */ 
/*  54 */     FieldInfo[] fmtInfo = ResultSetUtils.createInfoList(drsFormats, fmtKeys, true);
/*  55 */     int isSystemIndex = fmtInfo[6].m_index;
/*  56 */     int canDeleteIndex = fmtInfo[7].m_index;
/*     */ 
/*  58 */     for (drsFormats.first(); drsFormats.isRowPresent(); drsFormats.next())
/*     */     {
/*  60 */       Vector row = drsFormats.getCurrentRowValues();
/*  61 */       String val = (row.get(isSystemIndex) == "1") ? "0" : "1";
/*  62 */       row.setElementAt(val, canDeleteIndex);
/*     */     }
/*     */ 
/*  65 */     DataResultSet drsExtensions = SharedObjects.getTable("ExtensionFormatMap");
/*  66 */     drsExtensions.mergeFields(new DataResultSet(new String[] { "canDelete" }));
/*  67 */     String[] extKeys = { "dExtension", "dFormat", "dIsEnabled", "idcComponentName", "overrideStatus", "isSystem", "canDelete" };
/*     */ 
/*  69 */     FieldInfo[] extInfo = ResultSetUtils.createInfoList(drsExtensions, extKeys, true);
/*  70 */     isSystemIndex = extInfo[5].m_index;
/*  71 */     canDeleteIndex = extInfo[6].m_index;
/*     */ 
/*  73 */     for (drsExtensions.first(); drsExtensions.isRowPresent(); drsExtensions.next())
/*     */     {
/*  75 */       Vector row = drsExtensions.getCurrentRowValues();
/*  76 */       String val = (row.get(isSystemIndex) == "1") ? "0" : "1";
/*  77 */       row.setElementAt(val, canDeleteIndex);
/*     */     }
/*     */ 
/*  80 */     this.m_binder.addResultSet("DocFormats", drsFormats);
/*  81 */     this.m_binder.addResultSet("ExtensionFormatMap", drsExtensions);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editDocFormat()
/*     */     throws ServiceException, DataException
/*     */   {
/*  93 */     String dFormat = this.m_binder.getLocal("dFormat");
/*     */ 
/*  95 */     if ((dFormat == null) || (Service.isEmptyString(dFormat)))
/*     */     {
/*  97 */       String msg = LocaleUtils.encodeMessage("csUnableToEditFileFormat", null, "");
/*  98 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 101 */     int dbRowCount = WorkspaceUtils.getRowCount("DocFormats", "dFormat='" + dFormat + "'", this.m_workspace);
/* 102 */     List rowInSystemTable = SharedObjects.getTable("DocFormats").findRow(0, dFormat, 0, 0);
/*     */ 
/* 104 */     if (dbRowCount > 0)
/*     */     {
/* 106 */       this.m_workspace.execute("UdocFormat", this.m_binder);
/*     */     }
/* 108 */     else if (rowInSystemTable != null)
/*     */     {
/* 110 */       this.m_workspace.execute("IdocFormat", this.m_binder);
/*     */     }
/*     */     else
/*     */     {
/* 114 */       String msg = LocaleUtils.encodeMessage("csFormatNoLongerExists", null);
/* 115 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editDocExtension()
/*     */     throws ServiceException, DataException
/*     */   {
/* 127 */     String dExtension = this.m_binder.getLocal("dExtension");
/*     */ 
/* 129 */     if ((dExtension == null) || (Service.isEmptyString(dExtension)))
/*     */     {
/* 131 */       String msg = LocaleUtils.encodeMessage("csUnableToAddFileExtension", null, "");
/* 132 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 135 */     int dbRowCount = WorkspaceUtils.getRowCount("ExtensionFormatMap", "dExtension='" + dExtension + "'", this.m_workspace);
/* 136 */     List rowInSystemTable = SharedObjects.getTable("ExtensionFormatMap").findRow(0, dExtension, 0, 0);
/*     */ 
/* 138 */     if (dbRowCount > 0)
/*     */     {
/* 140 */       this.m_workspace.execute("UextensionMap", this.m_binder);
/*     */     }
/* 142 */     else if (rowInSystemTable != null)
/*     */     {
/* 144 */       this.m_workspace.execute("IextensionMap", this.m_binder);
/*     */     }
/*     */     else
/*     */     {
/* 148 */       String msg = LocaleUtils.encodeMessage("csUnableToEditExtenionNoLongerExists", null);
/* 149 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 155 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93092 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocFormatsHandler
 * JD-Core Version:    0.5.4
 */