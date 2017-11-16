/*     */ package intradoc.filestore.config;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.filestore.filesystem.BaseEventImplementor;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.shared.FilterImplementor;
/*     */ import java.io.IOException;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ConfigEventImplementor extends BaseEventImplementor
/*     */ {
/*     */   public void init(FileStoreProvider fs, Provider provider)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void notifyOfEvent(IdcFileDescriptor descriptor, Map data, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/*  60 */     String msg = LocaleUtils.encodeMessage("syNotSupported", null, "event filtering", "ConfigFileStore");
/*     */ 
/*  62 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public void registerEventFilter(FilterImplementor filter)
/*     */     throws ServiceException
/*     */   {
/*  75 */     String msg = LocaleUtils.encodeMessage("syNotSupported", null, "event filtering", "ConfigFileStore");
/*     */ 
/*  77 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public void unregisterEventFilter(FilterImplementor filter)
/*     */     throws ServiceException
/*     */   {
/*  90 */     String msg = LocaleUtils.encodeMessage("syNotSupported", null, "event filtering", "ConfigFileStore");
/*     */ 
/*  92 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public List getEventFilters()
/*     */     throws ServiceException
/*     */   {
/* 104 */     String msg = LocaleUtils.encodeMessage("syNotSupported", null, "event filtering", "ConfigFileStore");
/*     */ 
/* 106 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 112 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.config.ConfigEventImplementor
 * JD-Core Version:    0.5.4
 */