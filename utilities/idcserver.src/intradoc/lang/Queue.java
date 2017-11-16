/*     */ package intradoc.lang;
/*     */ 
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Queue extends Vector
/*     */ {
/*     */   public Object insert(Object obj)
/*     */   {
/*  47 */     addElement(obj);
/*  48 */     return obj;
/*     */   }
/*     */ 
/*     */   public synchronized Object remove()
/*     */   {
/*  59 */     if (isEmpty()) throw new EmptyQueueException();
/*     */ 
/*  61 */     Object obj = elementAt(0);
/*  62 */     removeElementAt(0);
/*     */ 
/*  64 */     return obj;
/*     */   }
/*     */ 
/*     */   public Object peek()
/*     */   {
/*  75 */     if (isEmpty()) throw new EmptyQueueException();
/*  76 */     return elementAt(0);
/*     */   }
/*     */ 
/*     */   public boolean empty()
/*     */   {
/*  90 */     return isEmpty();
/*     */   }
/*     */ 
/*     */   public int search(Object obj)
/*     */   {
/* 105 */     return indexOf(obj);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 111 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.lang.Queue
 * JD-Core Version:    0.5.4
 */