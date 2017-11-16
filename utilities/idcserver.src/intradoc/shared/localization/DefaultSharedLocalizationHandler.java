/*     */ package intradoc.shared.localization;
/*     */ 
/*     */ import intradoc.common.CommonLocalizationHandler;
/*     */ import intradoc.common.CommonLocalizationHandlerFactory;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DefaultSharedLocalizationHandler
/*     */   implements SharedLocalizationHandler
/*     */ {
/*     */   public DataResultSet getTimeZones(ExecutionContext context)
/*     */   {
/*  42 */     DataResultSet data = SharedObjects.getTable("SystemTimeZones");
/*  43 */     DataResultSet zones = new DataResultSet();
/*  44 */     zones.copy(data);
/*  45 */     return zones;
/*     */   }
/*     */ 
/*     */   public void verifyPrerequisites()
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void prepareTimeZonesForDisplay(DataResultSet tz, ExecutionContext context, int displayStyle)
/*     */   {
/*  55 */     FieldInfo nameField = new FieldInfo();
/*  56 */     tz.getFieldInfo("lcTimeZone", nameField);
/*     */ 
/*  59 */     tz.first();
/*  60 */     while (tz.isRowPresent())
/*     */     {
/*  62 */       String id = tz.getStringValue(nameField.m_index);
/*  63 */       if ((id.indexOf(47) == -1) && (!id.equals("UTC")))
/*     */       {
/*  65 */         tz.deleteCurrentRow();
/*     */       }
/*     */       else
/*     */       {
/*  69 */         tz.next();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  74 */     int rowCount = tz.getNumRows();
/*  75 */     Object[][] zones = new Object[rowCount][];
/*  76 */     for (int i = 0; i < rowCount; ++i)
/*     */     {
/*  78 */       Vector row = tz.getRowValues(i);
/*  79 */       String timeZoneName = (String)row.elementAt(nameField.m_index);
/*  80 */       TimeZone zone = LocaleResources.getTimeZone(timeZoneName, context);
/*  81 */       zones[i] = { zone, row };
/*     */     }
/*  83 */     Sort.sort(zones, 0, rowCount - 1, new TimeZoneComparator());
/*  84 */     for (int i = 0; i < rowCount; ++i)
/*     */     {
/*  86 */       Vector row = (Vector)zones[i][1];
/*  87 */       tz.setRowValues(row, i);
/*     */     }
/*     */ 
/*  90 */     if (displayStyle <= 0) {
/*     */       return;
/*     */     }
/*  93 */     FieldInfo labelField = new FieldInfo();
/*  94 */     if (!tz.getFieldInfo("lcLabel", labelField))
/*     */     {
/*  96 */       labelField.m_name = "lcLabel";
/*  97 */       List finfo = new ArrayList();
/*  98 */       finfo.add(labelField);
/*  99 */       tz.mergeFieldsWithFlags(finfo, 2);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 105 */       CommonLocalizationHandler clh = CommonLocalizationHandlerFactory.createInstance();
/* 106 */       for (tz.first(); tz.isRowPresent(); tz.next())
/*     */       {
/* 108 */         String id = tz.getStringValue(nameField.m_index);
/* 109 */         String label = clh.getTimeZoneDisplayName(id, displayStyle, context);
/* 110 */         tz.setCurrentValue(labelField.m_index, label);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 147 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84490 $";
/*     */   }
/*     */ 
/*     */   protected class TimeZoneComparator
/*     */     implements IdcComparator
/*     */   {
/*     */     protected TimeZoneComparator()
/*     */     {
/*     */     }
/*     */ 
/*     */     public int compare(Object o1, Object o2)
/*     */     {
/* 124 */       Object[] array1 = (Object[])(Object[])o1;
/* 125 */       Object[] array2 = (Object[])(Object[])o2;
/* 126 */       if ((array1[0] instanceof TimeZone) && (array2[0] instanceof TimeZone))
/*     */       {
/* 128 */         TimeZone tz1 = (TimeZone)array1[0];
/* 129 */         TimeZone tz2 = (TimeZone)array2[0];
/* 130 */         int t1 = tz1.getRawOffset();
/* 131 */         int t2 = tz2.getRawOffset();
/* 132 */         if (t1 == t2)
/*     */         {
/* 134 */           String id1 = tz1.getID();
/* 135 */           String id2 = tz2.getID();
/* 136 */           return id1.compareTo(id2);
/*     */         }
/* 138 */         return t1 - t2;
/*     */       }
/*     */ 
/* 141 */       return 0;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.localization.DefaultSharedLocalizationHandler
 * JD-Core Version:    0.5.4
 */