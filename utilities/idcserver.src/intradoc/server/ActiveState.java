/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import java.util.Date;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ActiveState
/*     */ {
/*  49 */   protected static DataBinder m_activeData = new DataBinder(true);
/*     */   protected static final String m_fileName = "activestate.hda";
/*     */ 
/*     */   public static void load()
/*     */     throws ServiceException
/*     */   {
/*  56 */     synchronized (m_activeData)
/*     */     {
/*  58 */       serializeData(false);
/*     */     }
/*  60 */     String types = m_activeData.getLocal("blFieldTypes");
/*  61 */     if ((types != null) && (types.length() != 0))
/*     */       return;
/*  63 */     m_activeData.setFieldType("lastmodified", "date");
/*     */   }
/*     */ 
/*     */   public static void save()
/*     */     throws ServiceException
/*     */   {
/*  69 */     synchronized (m_activeData)
/*     */     {
/*  71 */       serializeData(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getActiveProperty(String key)
/*     */   {
/*  80 */     String result = null;
/*  81 */     synchronized (m_activeData)
/*     */     {
/*  83 */       result = m_activeData.getLocal(key);
/*     */     }
/*  85 */     return result;
/*     */   }
/*     */ 
/*     */   public static void setActiveProperty(String key, String val)
/*     */   {
/*  90 */     synchronized (m_activeData)
/*     */     {
/*  92 */       m_activeData.putLocal(key, val);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static DataResultSet getResultSet(String rsetName)
/*     */   {
/*  98 */     return (DataResultSet)m_activeData.getResultSet(rsetName);
/*     */   }
/*     */ 
/*     */   protected static void setResultSet(String rsetName, DataResultSet dset)
/*     */   {
/* 103 */     m_activeData.addResultSet(rsetName, dset);
/*     */   }
/*     */ 
/*     */   public static String getResultSetValue(String rsetName, String key)
/*     */   {
/* 116 */     return (String)getResultSetObject(rsetName, key, false);
/*     */   }
/*     */ 
/*     */   public static Date getResultSetDate(String rsetName, String key)
/*     */   {
/* 121 */     return (Date)getResultSetObject(rsetName, key, true);
/*     */   }
/*     */ 
/*     */   protected static Object getResultSetObject(String rsetName, String key, boolean isDate)
/*     */   {
/* 126 */     synchronized (m_activeData)
/*     */     {
/* 128 */       DataResultSet dset = getResultSet(rsetName);
/* 129 */       if (dset == null)
/*     */       {
/* 131 */         return null;
/*     */       }
/*     */ 
/* 134 */       for (dset.first(); dset.isRowPresent(); dset.next())
/*     */       {
/* 136 */         String rowKey = dset.getStringValue(0);
/* 137 */         if (!rowKey.equals(key))
/*     */           continue;
/* 139 */         if (isDate)
/*     */         {
/* 141 */           return dset.getDateValue(1);
/*     */         }
/* 143 */         return dset.getStringValue(1);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 148 */     return null;
/*     */   }
/*     */ 
/*     */   public static void setResultSetValue(String rsetName, String key, String val, String desc)
/*     */   {
/* 158 */     synchronized (m_activeData)
/*     */     {
/* 160 */       DataResultSet dset = getResultSet(rsetName);
/* 161 */       if (dset == null)
/*     */       {
/* 163 */         return;
/*     */       }
/*     */ 
/* 166 */       for (dset.first(); dset.isRowPresent(); dset.next())
/*     */       {
/* 168 */         Vector v = dset.getRowValues(dset.getCurrentRow());
/* 169 */         String rowKey = (String)v.elementAt(0);
/* 170 */         if (!rowKey.equals(key))
/*     */           continue;
/* 172 */         v.setElementAt(val, 1);
/* 173 */         if ((desc != null) && (v.size() > 2))
/*     */         {
/* 175 */           v.setElementAt(desc, 2);
/*     */         }
/* 177 */         return;
/*     */       }
/*     */ 
/* 181 */       Vector v = dset.createEmptyRow();
/* 182 */       v.setElementAt(key, 0);
/* 183 */       v.setElementAt(val, 1);
/* 184 */       if ((desc != null) && (v.size() > 2))
/*     */       {
/* 186 */         v.setElementAt(desc, 2);
/*     */       }
/* 188 */       dset.addRow(v);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void createResultSetIfNone(String rsetName, String[] fieldNames)
/*     */   {
/* 196 */     synchronized (m_activeData)
/*     */     {
/* 198 */       DataResultSet dset = getResultSet(rsetName);
/* 199 */       if (dset != null)
/*     */       {
/* 201 */         return;
/*     */       }
/*     */ 
/* 204 */       dset = new DataResultSet(fieldNames);
/* 205 */       dset.setDateFormat(m_activeData.m_blDateFormat);
/* 206 */       setResultSet(rsetName, dset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getActiveStateDirectory()
/*     */   {
/* 212 */     return LegacyDirectoryLocator.getConfigDirectory();
/*     */   }
/*     */ 
/*     */   protected static void serializeData(boolean isWrite)
/*     */     throws ServiceException
/*     */   {
/* 220 */     String dir = getActiveStateDirectory();
/* 221 */     ResourceUtils.serializeDataBinder(dir, "activestate.hda", m_activeData, isWrite, false);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 227 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ActiveState
 * JD-Core Version:    0.5.4
 */