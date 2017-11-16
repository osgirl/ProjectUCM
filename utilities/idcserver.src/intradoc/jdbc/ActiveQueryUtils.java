/*     */ package intradoc.jdbc;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.provider.ProviderConnectionStatus;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class ActiveQueryUtils
/*     */ {
/*  31 */   protected static ConcurrentHashMap m_executingStatements = new ConcurrentHashMap();
/*  32 */   protected static ConcurrentHashMap<String, String> m_executingQueryIDs = new ConcurrentHashMap();
/*     */ 
/*  34 */   protected static ConcurrentHashMap<String, String> m_lockedQueries = new ConcurrentHashMap();
/*     */ 
/*  36 */   protected static long m_timeout = 1000L;
/*     */   protected static final int STATEMENT_INDEX = 0;
/*     */   protected static final int CONN_STATUS_INDEX = 1;
/*     */ 
/*     */   public static void addActiveQuery(Statement stmt, ProviderConnectionStatus status)
/*     */   {
/*  43 */     Object[] objs = new Object[2];
/*  44 */     objs[0] = stmt;
/*  45 */     objs[1] = status;
/*     */ 
/*  47 */     String name = Thread.currentThread().getName();
/*  48 */     String id = Long.toString(System.currentTimeMillis() % 100000000L, 16) + "_" + name;
/*  49 */     addActiveQueryEx(name, id, objs);
/*     */   }
/*     */ 
/*     */   public static void addActiveQueryEx(String threadName, String queryID, Object[] objs)
/*     */   {
/*  54 */     Statement stmt = (Statement)objs[0];
/*  55 */     if ((threadName == null) || (queryID == null) || (stmt == null))
/*     */     {
/*  57 */       return;
/*     */     }
/*  59 */     trace("Adding active query with id " + queryID + " with Statement: " + stmt.toString());
/*  60 */     m_executingQueryIDs.put(threadName, queryID);
/*  61 */     m_executingStatements.put(queryID, objs);
/*     */   }
/*     */ 
/*     */   public static void removeActiveQuery() throws DataException
/*     */   {
/*  66 */     String name = Thread.currentThread().getName();
/*  67 */     String id = (String)m_executingQueryIDs.get(name);
/*  68 */     removeActiveQueryEx(name, id);
/*     */   }
/*     */ 
/*     */   public static void removeActiveQueryEx(String threadName, String queryID) throws DataException
/*     */   {
/*  73 */     if ((threadName == null) || (queryID == null))
/*     */     {
/*  75 */       return;
/*     */     }
/*  77 */     reserveLockWithTimeout(queryID, m_timeout);
/*  78 */     trace("Removing active query with id " + queryID + " for thread " + threadName);
/*     */ 
/*  80 */     String oldQueryID = (String)m_executingQueryIDs.get(threadName);
/*  81 */     if (oldQueryID.equalsIgnoreCase(queryID))
/*     */     {
/*  83 */       m_executingQueryIDs.remove(threadName);
/*  84 */       m_executingStatements.remove(queryID);
/*     */     }
/*     */     else
/*     */     {
/*  90 */       trace("Unable to remove query id mismatch. existing ID for the thread is: " + oldQueryID);
/*     */     }
/*  92 */     releaseLock(queryID);
/*     */   }
/*     */ 
/*     */   public static void cancel(String queryID) throws DataException
/*     */   {
/*  97 */     String name = Thread.currentThread().getName();
/*  98 */     cancelEx(name, queryID);
/*     */   }
/*     */ 
/*     */   public static void cancelEx(String threadName, String queryID) throws DataException
/*     */   {
/* 103 */     if ((threadName == null) || (queryID == null))
/*     */     {
/* 105 */       return;
/*     */     }
/*     */ 
/* 108 */     reserveLockWithTimeout(queryID, m_timeout);
/*     */     try
/*     */     {
/* 112 */       Object[] objs = (Object[])(Object[])m_executingStatements.get(queryID);
/* 113 */       if (objs != null)
/*     */       {
/* 115 */         Statement stmt = (Statement)objs[0];
/* 116 */         trace("Cancelling query with id " + queryID + " for thread " + threadName + " with Statement " + stmt.toString());
/*     */ 
/* 118 */         ((ProviderConnectionStatus)objs[1]).setCurrentActionStatus("CANCELLING");
/* 119 */         stmt.cancel();
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 128 */       releaseLock(queryID);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getActiveQueryID(String threadName)
/*     */   {
/* 134 */     return (String)m_executingQueryIDs.get(threadName);
/*     */   }
/*     */ 
/*     */   public static void reserveLockWithTimeout(String queryID, long timeout) throws DataException
/*     */   {
/* 139 */     int waitTime = 0;
/*     */     do { if (reserveLock(queryID))
/*     */         return;
/*     */       try
/*     */       {
/* 144 */         Thread.sleep(50L);
/*     */       }
/*     */       catch (InterruptedException e)
/*     */       {
/* 148 */         Report.trace(null, null, e);
/*     */       }
/* 150 */       waitTime += 50; }
/* 151 */     while (waitTime <= timeout);
/*     */ 
/* 153 */     traceLock("Unable to get active query lock for query " + queryID);
/* 154 */     throw new DataException("!csJdbcUnableReserveQueryLock");
/*     */   }
/*     */ 
/*     */   public static boolean reserveLock(String queryID)
/*     */   {
/* 161 */     boolean reserveLock = false;
/* 162 */     traceLock("Retrieving query lock with id: " + queryID);
/* 163 */     String name = Thread.currentThread().getName();
/* 164 */     if (m_lockedQueries.get(queryID) == null)
/*     */     {
/* 166 */       m_lockedQueries.put(queryID, name);
/* 167 */       reserveLock = true;
/*     */     }
/* 169 */     return reserveLock;
/*     */   }
/*     */ 
/*     */   public static void releaseLock(String queryID)
/*     */   {
/* 174 */     traceLock("Releasing query lock with id: " + queryID);
/* 175 */     String name = Thread.currentThread().getName();
/* 176 */     String lockedName = (String)m_lockedQueries.get(queryID);
/* 177 */     if ((lockedName == null) || (!lockedName.equalsIgnoreCase(name)))
/*     */       return;
/* 179 */     m_lockedQueries.remove(queryID);
/*     */   }
/*     */ 
/*     */   public static void trace(String msg)
/*     */   {
/* 185 */     Report.trace("activedbquery", msg, null);
/*     */   }
/*     */ 
/*     */   public static void traceLock(String msg)
/*     */   {
/* 190 */     Report.trace("activedbquerylock", msg, null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 196 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83042 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.ActiveQueryUtils
 * JD-Core Version:    0.5.4
 */