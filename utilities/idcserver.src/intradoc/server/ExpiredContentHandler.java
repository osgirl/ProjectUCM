/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import java.util.Date;
/*     */ 
/*     */ public class ExpiredContentHandler extends ServiceHandler
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void buildExpiredContentQuery()
/*     */     throws DataException, ServiceException
/*     */   {
/*  37 */     boolean isExpiredQuery = StringUtils.convertToBool(this.m_binder.getAllowMissing("isExpiredQuery"), true);
/*     */ 
/*  39 */     String startDateOdbc = this.m_binder.getLocal("startDate");
/*  40 */     if ((startDateOdbc != null) && (startDateOdbc.length() != 0))
/*     */     {
/*  42 */       Date d = LocaleResources.parseDate(startDateOdbc, this.m_service);
/*  43 */       startDateOdbc = LocaleUtils.formatODBC(d);
/*     */     }
/*     */     else
/*     */     {
/*  47 */       startDateOdbc = "";
/*     */     }
/*     */ 
/*  50 */     String endDateOdbc = this.m_binder.getLocal("endDate");
/*  51 */     if ((endDateOdbc != null) && (endDateOdbc.length() != 0))
/*     */     {
/*  53 */       Date d = LocaleResources.parseDate(endDateOdbc, this.m_service);
/*  54 */       endDateOdbc = LocaleUtils.formatODBC(d);
/*     */     }
/*     */     else
/*     */     {
/*  58 */       endDateOdbc = "";
/*     */     }
/*     */ 
/*  62 */     String option = this.m_binder.getAllowMissing("searchType");
/*  63 */     if ((isExpiredQuery) && (option != null) && (option.equals(">")) && 
/*  65 */       (startDateOdbc != null) && (startDateOdbc.length() == 0) && (endDateOdbc != null) && (endDateOdbc.length() > 0))
/*     */     {
/*  68 */       startDateOdbc = endDateOdbc;
/*  69 */       endDateOdbc = "";
/*  70 */       this.m_binder.putLocal("startDate", startDateOdbc);
/*     */     }
/*     */ 
/*  74 */     if ((startDateOdbc != null) && (startDateOdbc.length() == 0))
/*     */     {
/*  77 */       startDateOdbc = null;
/*     */ 
/*  81 */       if ((!isExpiredQuery) && (endDateOdbc.length() == 0))
/*     */       {
/*  83 */         this.m_binder.putLocalDate("startDate", new Date());
/*  84 */         startDateOdbc = this.m_binder.getSystem("startDate");
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/*  90 */       this.m_binder.putLocal("userDefinedStartDate", "true");
/*     */     }
/*     */ 
/*  93 */     if ((endDateOdbc != null) && (endDateOdbc.length() == 0))
/*     */     {
/*  97 */       endDateOdbc = null;
/*  98 */       if ((isExpiredQuery) && (startDateOdbc == null))
/*     */       {
/* 100 */         this.m_binder.putLocalDate("endDate", new Date());
/* 101 */         endDateOdbc = this.m_binder.getSystem("endDate");
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 107 */       this.m_binder.putLocal("userDefinedEndDate", "true");
/*     */     }
/*     */ 
/* 110 */     String whereClause = "dRevRank = 0";
/* 111 */     String orderClause = null;
/* 112 */     if (isExpiredQuery)
/*     */     {
/* 115 */       whereClause = whereClause + " AND Revisions.dStatus = 'EXPIRED'";
/* 116 */       orderClause = "ORDER BY Revisions.dOutDate DESC";
/*     */     }
/*     */ 
/* 119 */     String dateClause = "";
/* 120 */     if (endDateOdbc != null)
/*     */     {
/* 122 */       dateClause = dateClause + "Revisions.dOutDate < " + endDateOdbc;
/* 123 */       if (startDateOdbc != null)
/*     */       {
/* 125 */         dateClause = dateClause + " AND ";
/*     */       }
/*     */     }
/* 128 */     else if (isExpiredQuery)
/*     */     {
/* 131 */       dateClause = dateClause + "Revisions.dOutDate IS NULL";
/* 132 */       if (startDateOdbc != null)
/*     */       {
/* 134 */         dateClause = dateClause + " OR ";
/*     */       }
/*     */     }
/*     */ 
/* 138 */     if (startDateOdbc != null)
/*     */     {
/* 140 */       dateClause = dateClause + " Revisions.dOutDate > " + startDateOdbc;
/*     */     }
/* 142 */     whereClause = whereClause + " AND (" + dateClause + ")";
/* 143 */     if (orderClause == null)
/*     */     {
/* 145 */       orderClause = "ORDER BY Revisions.dOutDate";
/*     */     }
/*     */ 
/* 148 */     this.m_binder.putLocal("whereClause", whereClause);
/* 149 */     this.m_binder.putLocal("orderClause", orderClause);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 154 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70705 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ExpiredContentHandler
 * JD-Core Version:    0.5.4
 */