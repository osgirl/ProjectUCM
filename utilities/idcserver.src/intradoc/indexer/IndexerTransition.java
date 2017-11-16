/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import java.util.Hashtable;
/*     */ 
/*     */ public class IndexerTransition
/*     */ {
/*     */   protected Hashtable m_transitions;
/*     */ 
/*     */   public void init(DataBinder binder)
/*     */     throws DataException
/*     */   {
/*  38 */     this.m_transitions = new Hashtable();
/*  39 */     DataResultSet drset = (DataResultSet)binder.getResultSet("IndexerTransitionsTable");
/*  40 */     FieldInfo[] infos = ResultSetUtils.createInfoList(drset, new String[] { "currentState", "nextState" }, true);
/*     */ 
/*  42 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/*  44 */       String cur = drset.getStringValue(infos[0].m_index);
/*  45 */       String next = drset.getStringValue(infos[1].m_index);
/*     */ 
/*  47 */       int index = cur.indexOf(",");
/*  48 */       if (index <= 0)
/*     */       {
/*  50 */         throw new DataException(LocaleUtils.encodeMessage("csIndexerStateFormatError", null, cur));
/*     */       }
/*     */ 
/*  54 */       String res = cur.substring(index + 1);
/*  55 */       cur = cur.substring(0, index);
/*     */ 
/*  57 */       Transition t = new Transition(cur, res);
/*     */ 
/*  59 */       this.m_transitions.put(t, next);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String computeNextState(String thisState, String result)
/*     */   {
/*  67 */     Transition t = new Transition(thisState, result);
/*  68 */     String next = (String)this.m_transitions.get(t);
/*  69 */     return next;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 108 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ 
/*     */   class Transition
/*     */   {
/*     */     public String m_currentState;
/*     */     public String m_result;
/*     */ 
/*     */     public Transition()
/*     */     {
/*  80 */       this.m_currentState = null;
/*  81 */       this.m_result = null;
/*     */     }
/*     */ 
/*     */     public Transition(String cur, String r)
/*     */     {
/*  86 */       this.m_currentState = cur;
/*  87 */       this.m_result = r;
/*     */     }
/*     */ 
/*     */     public boolean equals(Object o2)
/*     */     {
/*  93 */       Transition t2 = (Transition)o2;
/*  94 */       return (this.m_currentState.equals(t2.m_currentState)) && (this.m_result.equals(t2.m_result));
/*     */     }
/*     */ 
/*     */     public int hashCode()
/*     */     {
/* 101 */       return this.m_currentState.hashCode() * this.m_result.hashCode();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerTransition
 * JD-Core Version:    0.5.4
 */