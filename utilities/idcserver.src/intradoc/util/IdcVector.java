/*     */ package intradoc.util;
/*     */ 
/*     */ import java.lang.reflect.Array;
/*     */ import java.util.Collection;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.ListIterator;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IdcVector<E> extends Vector<E>
/*     */ {
/*     */   public Object[] m_array;
/*     */   public int m_arrayLength;
/*     */   public int m_length;
/*     */   public int m_generation;
/*  30 */   public int m_arraycopyThreshold = 6;
/*     */ 
/*     */   public IdcVector()
/*     */   {
/*     */   }
/*     */ 
/*     */   public IdcVector(int size)
/*     */   {
/*  39 */     this.m_array = new Object[size];
/*  40 */     this.m_arrayLength = size;
/*     */   }
/*     */ 
/*     */   public IdcVector(Collection<E> col)
/*     */   {
/*  45 */     Object[] tmp = new Object[col.size()];
/*  46 */     int i = 0;
/*  47 */     for (Iterator i$ = col.iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*     */ 
/*  49 */       tmp[(i++)] = obj; }
/*     */ 
/*  51 */     this.m_array = tmp;
/*  52 */     this.m_arrayLength = tmp.length;
/*  53 */     this.m_length = i;
/*     */   }
/*     */ 
/*     */   protected boolean assertState()
/*     */   {
/*  58 */     assert (this.m_length >= 0);
/*  59 */     if (this.m_array == null)
/*     */     {
/*  61 */       assert (this.m_length == 0);
/*  62 */       if ((!$assertionsDisabled) && (this.m_arrayLength != 0)) throw new AssertionError();
/*     */     }
/*     */     else
/*     */     {
/*  66 */       assert (this.m_arrayLength == this.m_array.length);
/*  67 */       assert (this.m_length <= this.m_arrayLength);
/*     */     }
/*     */ 
/*  70 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean verifyState()
/*     */   {
/*  75 */     if (this.m_length < 0) return false;
/*  76 */     if (this.m_array == null)
/*     */     {
/*  78 */       return (this.m_length == 0) && (this.m_arrayLength == 0);
/*     */     }
/*     */ 
/*  81 */     return (this.m_arrayLength == this.m_array.length) && (this.m_length <= this.m_arrayLength);
/*     */   }
/*     */ 
/*     */   protected Object[] alloc(int desiredLength, boolean doCopy)
/*     */   {
/*  87 */     assert (assertState());
/*  88 */     Object[] array = null;
/*  89 */     if (desiredLength < 16)
/*     */     {
/*  91 */       desiredLength = 16;
/*     */     }
/*  93 */     if (desiredLength + 1 > 2 * this.m_arrayLength)
/*     */     {
/*  95 */       array = new Object[desiredLength + 1];
/*     */     }
/*  97 */     else if (desiredLength + 1 < this.m_arrayLength / 4)
/*     */     {
/*  99 */       array = new Object[this.m_arrayLength / 4];
/*     */     }
/*     */     else
/*     */     {
/* 103 */       array = new Object[2 * this.m_arrayLength];
/*     */     }
/* 105 */     if (doCopy)
/*     */     {
/* 107 */       if (this.m_array != null)
/*     */       {
/* 109 */         if (this.m_length < this.m_arraycopyThreshold)
/*     */         {
/* 111 */           for (int i = 0; i < this.m_length; ++i)
/*     */           {
/* 113 */             array[i] = this.m_array[i];
/*     */           }
/*     */ 
/*     */         }
/*     */         else {
/* 118 */           System.arraycopy(this.m_array, 0, array, 0, this.m_length);
/*     */         }
/*     */       }
/* 121 */       this.m_array = array;
/* 122 */       this.m_arrayLength = array.length;
/*     */     }
/* 124 */     assert (assertState());
/* 125 */     return array;
/*     */   }
/*     */ 
/*     */   public boolean add(E e)
/*     */   {
/* 130 */     assert (assertState());
/* 131 */     if (this.m_arrayLength == this.m_length)
/*     */     {
/* 133 */       alloc(this.m_arrayLength, true);
/*     */     }
/* 135 */     this.m_array[(this.m_length++)] = e;
/* 136 */     this.m_generation += 1;
/* 137 */     assert (assertState());
/* 138 */     return true;
/*     */   }
/*     */ 
/*     */   public void add(int index, E e)
/*     */   {
/* 143 */     if (index >= this.m_arrayLength)
/*     */     {
/* 145 */       assert (assertState());
/* 146 */       alloc(index + 1, true);
/* 147 */       this.m_array[index] = e;
/* 148 */       this.m_length = (index + 1);
/* 149 */       assert (assertState());
/* 150 */       this.m_generation += 1;
/* 151 */       return;
/*     */     }
/*     */ 
/* 154 */     if (index >= this.m_length)
/*     */     {
/* 156 */       assert (assertState());
/* 157 */       this.m_array[index] = e;
/* 158 */       this.m_length = (index + 1);
/* 159 */       assert (assertState());
/* 160 */       this.m_generation += 1;
/* 161 */       return;
/*     */     }
/*     */ 
/* 164 */     if (this.m_arrayLength == this.m_length)
/*     */     {
/* 166 */       assert (assertState());
/* 167 */       Object[] array = alloc(this.m_length + 1, false);
/* 168 */       System.arraycopy(this.m_array, 0, array, 0, index);
/* 169 */       array[index] = e;
/* 170 */       System.arraycopy(this.m_array, index, array, index + 1, this.m_length - index);
/* 171 */       this.m_array = array;
/* 172 */       this.m_arrayLength = array.length;
/* 173 */       this.m_length += 1;
/* 174 */       assert (assertState());
/* 175 */       this.m_generation += 1;
/* 176 */       return;
/*     */     }
/*     */ 
/* 179 */     System.arraycopy(this.m_array, index, this.m_array, index + 1, this.m_length - index);
/* 180 */     this.m_array[index] = e;
/* 181 */     this.m_length += 1;
/* 182 */     this.m_generation += 1;
/* 183 */     assert (assertState());
/*     */   }
/*     */ 
/*     */   public boolean addAll(Collection c)
/*     */   {
/* 188 */     assert (assertState());
/* 189 */     int colLength = c.size();
/* 190 */     if (colLength > this.m_arrayLength - this.m_length)
/*     */     {
/* 192 */       alloc(colLength + this.m_length + 1, true);
/*     */     }
/* 194 */     for (Iterator i$ = c.iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*     */ 
/* 196 */       this.m_array[(this.m_length++)] = obj; }
/*     */ 
/* 198 */     this.m_generation += 1;
/* 199 */     assert (assertState());
/* 200 */     return colLength > 0;
/*     */   }
/*     */ 
/*     */   public boolean addAll(int index, Collection c)
/*     */   {
/* 205 */     assert (assertState());
/* 206 */     int colLength = c.size();
/* 207 */     if (colLength > this.m_arrayLength - this.m_length)
/*     */     {
/* 209 */       if (index >= this.m_length)
/*     */       {
/* 211 */         alloc(colLength + this.m_length + 1, true);
/*     */       }
/*     */       else
/*     */       {
/* 215 */         Object[] array = alloc(colLength + this.m_length + 1, false);
/* 216 */         System.arraycopy(this.m_array, 0, array, 0, index);
/* 217 */         System.arraycopy(this.m_array, index, array, index + colLength, this.m_length - index);
/* 218 */         this.m_array = array;
/* 219 */         this.m_arrayLength = array.length;
/*     */       }
/*     */     }
/* 222 */     else if (index < this.m_length)
/*     */     {
/* 224 */       System.arraycopy(this.m_array, index, this.m_array, index + colLength, this.m_length - index);
/*     */     }
/*     */     IdcVector v;
/*     */     int i;
/*     */     Iterator i$;
/* 227 */     if (c instanceof IdcVector)
/*     */     {
/* 229 */       v = (IdcVector)c;
/*     */ 
/* 231 */       for (i = colLength; i-- > 0; )
/*     */       {
/* 233 */         this.m_array[(index + i)] = v.m_array[i];
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 238 */       for (i$ = c.iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*     */ 
/* 240 */         this.m_array[(index++)] = obj; }
/*     */ 
/*     */     }
/* 243 */     this.m_length += colLength;
/* 244 */     this.m_generation += 1;
/* 245 */     assert (assertState());
/* 246 */     return colLength > 0;
/*     */   }
/*     */ 
/*     */   public void addElement(E e)
/*     */   {
/* 252 */     assert (assertState());
/* 253 */     if (this.m_arrayLength == this.m_length)
/*     */     {
/* 255 */       alloc(this.m_arrayLength, true);
/*     */     }
/* 257 */     this.m_array[(this.m_length++)] = e;
/* 258 */     this.m_generation += 1;
/* 259 */     assert (assertState());
/*     */   }
/*     */ 
/*     */   public int capacity()
/*     */   {
/* 264 */     return this.m_arrayLength;
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 269 */     while (this.m_length > 0)
/*     */     {
/* 271 */       this.m_array[(--this.m_length)] = null;
/*     */     }
/* 273 */     this.m_generation += 1;
/* 274 */     assert (assertState());
/*     */   }
/*     */ 
/*     */   public Object clone()
/*     */   {
/* 279 */     assert (assertState());
/* 280 */     IdcVector v = (IdcVector)super.clone();
/* 281 */     v.m_array = new Object[this.m_arrayLength];
/* 282 */     if (this.m_length < 16)
/*     */     {
/* 284 */       for (int i = 0; i < this.m_length; ++i)
/*     */       {
/* 286 */         v.m_array[i] = this.m_array[i];
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 291 */       System.arraycopy(this.m_array, 0, v.m_array, 0, this.m_length);
/*     */     }
/* 293 */     assert (assertState());
/* 294 */     assert (v.assertState());
/* 295 */     return v;
/*     */   }
/*     */ 
/*     */   public boolean contains(Object o)
/*     */   {
/* 300 */     assert (assertState());
/* 301 */     for (int i = 0; i < this.m_length; ++i)
/*     */     {
/* 303 */       if ((this.m_array[i] == null) || ((this.m_array[i] != o) && (!this.m_array[i].equals(o)))) {
/*     */         continue;
/*     */       }
/* 306 */       assert (assertState());
/* 307 */       return true;
/*     */     }
/*     */ 
/* 310 */     assert (assertState());
/* 311 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean containsAll(Collection c)
/*     */   {
/* 316 */     assert (assertState());
/* 317 */     for (Iterator i$ = c.iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*     */ 
/* 319 */       boolean found = false;
/* 320 */       for (int i = 0; i < this.m_length; ++i)
/*     */       {
/* 322 */         if ((this.m_array[i] == null) || ((this.m_array[i] != obj) && (!this.m_array[i].equals(obj)))) {
/*     */           continue;
/*     */         }
/* 325 */         found = true;
/* 326 */         break;
/*     */       }
/*     */ 
/* 329 */       if (!found)
/*     */       {
/* 331 */         assert (assertState());
/* 332 */         return false;
/*     */       } }
/*     */ 
/* 335 */     assert (assertState());
/* 336 */     return true;
/*     */   }
/*     */ 
/*     */   public void copyInto(Object[] a)
/*     */   {
/* 341 */     for (int i = 0; i < this.m_length; ++i)
/*     */     {
/* 343 */       a[i] = this.m_array[i];
/*     */     }
/*     */   }
/*     */ 
/*     */   public E elementAt(int index)
/*     */   {
/* 349 */     return this.m_array[index];
/*     */   }
/*     */ 
/*     */   public Enumeration elements()
/*     */   {
/* 354 */     return new IdcVectorEnumeration(this);
/*     */   }
/*     */ 
/*     */   public void ensureCapacity(int minCapacity)
/*     */   {
/* 359 */     if (minCapacity <= this.m_arrayLength)
/*     */       return;
/* 361 */     alloc(minCapacity, true);
/* 362 */     assert (assertState());
/*     */   }
/*     */ 
/*     */   public boolean equals(Object obj)
/*     */   {
/* 368 */     if (obj == this)
/*     */     {
/* 370 */       return true;
/*     */     }
/* 372 */     if (obj instanceof IdcVector)
/*     */     {
/* 374 */       IdcVector v = (IdcVector)obj;
/* 375 */       if (this.m_length != v.m_length)
/*     */       {
/* 377 */         return false;
/*     */       }
/* 379 */       for (int i = 0; i < this.m_length; ++i)
/*     */       {
/* 381 */         if (this.m_array[i] == v.m_array[i]) continue; if (!this.m_array[i].equals(v.m_array[i]))
/*     */         {
/* 386 */           return false;
/*     */         }
/*     */       }
/*     */     }
/* 389 */     return true;
/*     */   }
/*     */ 
/*     */   public E firstElement()
/*     */   {
/* 394 */     return this.m_array[0];
/*     */   }
/*     */ 
/*     */   public E get(int index)
/*     */   {
/* 399 */     return this.m_array[index];
/*     */   }
/*     */ 
/*     */   public int hashCode()
/*     */   {
/* 404 */     int code = super.hashCode();
/* 405 */     int count = 0;
/* 406 */     for (int i = 0; i < this.m_length; ++i)
/*     */     {
/* 408 */       if (this.m_array[i] == null)
/*     */         continue;
/* 410 */       if (count++ < 5)
/*     */       {
/* 412 */         code *= this.m_array[i].hashCode();
/*     */       }
/*     */       else
/*     */       {
/* 416 */         code += this.m_array[i].hashCode();
/*     */       }
/*     */     }
/*     */ 
/* 420 */     return code;
/*     */   }
/*     */ 
/*     */   public int indexOf(Object o)
/*     */   {
/* 425 */     for (int i = 0; i < this.m_length; ++i)
/*     */     {
/* 427 */       if ((this.m_array[i] == o) || (this.m_array[i].equals(o)))
/*     */       {
/* 430 */         return i;
/*     */       }
/*     */     }
/* 433 */     return -1;
/*     */   }
/*     */ 
/*     */   public int indexOf(Object o, int index)
/*     */   {
/* 438 */     for (int i = index; i < this.m_length; ++i)
/*     */     {
/* 440 */       if ((this.m_array[i] == o) || (this.m_array[i].equals(o)))
/*     */       {
/* 443 */         return i;
/*     */       }
/*     */     }
/* 446 */     return -1;
/*     */   }
/*     */ 
/*     */   public void insertElementAt(E e, int index)
/*     */   {
/* 452 */     assert (assertState());
/* 453 */     if (index >= this.m_arrayLength)
/*     */     {
/* 455 */       alloc(index + 1, true);
/* 456 */       this.m_array[index] = e;
/* 457 */       this.m_length = (index + 1);
/* 458 */       assert (assertState());
/* 459 */       this.m_generation += 1;
/* 460 */       return;
/*     */     }
/*     */ 
/* 463 */     if (index >= this.m_length)
/*     */     {
/* 465 */       this.m_array[index] = e;
/* 466 */       this.m_length = (index + 1);
/* 467 */       assert (assertState());
/* 468 */       this.m_generation += 1;
/* 469 */       return;
/*     */     }
/*     */ 
/* 472 */     if (this.m_arrayLength == this.m_length)
/*     */     {
/* 474 */       Object[] array = alloc(this.m_length + 1, false);
/* 475 */       System.arraycopy(this.m_array, 0, array, 0, index);
/* 476 */       array[index] = e;
/* 477 */       System.arraycopy(this.m_array, index, array, index + 1, this.m_length - index);
/* 478 */       this.m_array = array;
/* 479 */       this.m_arrayLength = array.length;
/* 480 */       this.m_length += 1;
/* 481 */       assert (assertState());
/* 482 */       this.m_generation += 1;
/* 483 */       return;
/*     */     }
/*     */ 
/* 486 */     System.arraycopy(this.m_array, index, this.m_array, index + 1, this.m_length - index);
/* 487 */     this.m_array[index] = e;
/* 488 */     this.m_length += 1;
/* 489 */     this.m_generation += 1;
/* 490 */     assert (assertState());
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/* 495 */     return this.m_length == 0;
/*     */   }
/*     */ 
/*     */   public E lastElement()
/*     */   {
/* 500 */     return this.m_array[(this.m_length - 1)];
/*     */   }
/*     */ 
/*     */   public int lastIndexOf(Object o)
/*     */   {
/* 505 */     for (int i = this.m_length - 1; i >= 0; --i)
/*     */     {
/* 507 */       if ((this.m_array[i] == o) || (this.m_array[i].equals(o)))
/*     */       {
/* 510 */         return i;
/*     */       }
/*     */     }
/* 513 */     return -1;
/*     */   }
/*     */ 
/*     */   public int lastIndexOf(Object o, int index)
/*     */   {
/* 518 */     for (int i = index; i >= 0; --i)
/*     */     {
/* 520 */       if ((this.m_array[i] == o) || (this.m_array[i].equals(o)))
/*     */       {
/* 523 */         return i;
/*     */       }
/*     */     }
/* 526 */     return -1;
/*     */   }
/*     */ 
/*     */   public E remove(int index)
/*     */   {
/* 531 */     assert (assertState());
/* 532 */     Object obj = this.m_array[index];
/*     */ 
/* 536 */     int count = this.m_length - index - 1;
/*     */     int i;
/* 537 */     if (count < 16)
/*     */     {
/* 539 */       System.arraycopy(this.m_array, index + 1, this.m_array, index, count);
/*     */     }
/*     */     else
/*     */     {
/* 543 */       int stop = index + count;
/* 544 */       for (i = index; i < stop; )
/*     */       {
/* 546 */         this.m_array[i] = this.m_array[(++i)];
/*     */       }
/*     */     }
/* 549 */     this.m_length -= 1;
/* 550 */     this.m_array[this.m_length] = null;
/* 551 */     assert (assertState());
/*     */ 
/* 554 */     return obj;
/*     */   }
/*     */ 
/*     */   public boolean remove(Object o)
/*     */   {
/* 559 */     assert (assertState());
/* 560 */     int index = indexOf(o);
/* 561 */     if (index >= 0)
/*     */     {
/* 565 */       int count = this.m_length - index - 1;
/*     */       int i;
/* 566 */       if (count < 16)
/*     */       {
/* 568 */         System.arraycopy(this.m_array, index + 1, this.m_array, index, count);
/*     */       }
/*     */       else
/*     */       {
/* 572 */         int stop = index + count;
/* 573 */         for (i = index; i < stop; )
/*     */         {
/* 575 */           this.m_array[i] = this.m_array[(++i)];
/*     */         }
/*     */       }
/* 578 */       this.m_length -= 1;
/* 579 */       this.m_array[this.m_length] = null;
/* 580 */       assert (assertState());
/* 581 */       this.m_generation += 1;
/*     */ 
/* 583 */       return true;
/*     */     }
/* 585 */     assert (assertState());
/* 586 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean removeAll(Collection c)
/*     */   {
/* 591 */     boolean didRemove = false;
/* 592 */     for (Iterator i$ = c.iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*     */ 
/* 594 */       while (remove(obj))
/*     */       {
/* 596 */         didRemove = true;
/*     */       } }
/*     */ 
/* 599 */     this.m_generation += 1;
/* 600 */     return didRemove;
/*     */   }
/*     */ 
/*     */   public void removeAllElements()
/*     */   {
/* 605 */     while (this.m_length > 0)
/*     */     {
/* 607 */       this.m_array[(--this.m_length)] = null;
/*     */     }
/* 609 */     this.m_generation += 1;
/* 610 */     assert (assertState());
/*     */   }
/*     */ 
/*     */   public boolean removeElement(Object o)
/*     */   {
/* 615 */     assert (assertState());
/* 616 */     int index = indexOf(o);
/* 617 */     if (index >= 0)
/*     */     {
/* 621 */       int count = this.m_length - index - 1;
/*     */       int i;
/* 622 */       if (count < 16)
/*     */       {
/* 624 */         System.arraycopy(this.m_array, index + 1, this.m_array, index, count);
/*     */       }
/*     */       else
/*     */       {
/* 628 */         int stop = index + count;
/* 629 */         for (i = index; i < stop; )
/*     */         {
/* 631 */           this.m_array[i] = this.m_array[(++i)];
/*     */         }
/*     */       }
/* 634 */       this.m_length -= 1;
/* 635 */       this.m_array[this.m_length] = null;
/* 636 */       assert (assertState());
/* 637 */       this.m_generation += 1;
/*     */ 
/* 639 */       return true;
/*     */     }
/* 641 */     return false;
/*     */   }
/*     */ 
/*     */   public void removeElementAt(int index)
/*     */   {
/* 648 */     int count = this.m_length - index - 1;
/*     */     int i;
/* 649 */     if (count < 16)
/*     */     {
/* 651 */       System.arraycopy(this.m_array, index + 1, this.m_array, index, count);
/*     */     }
/*     */     else
/*     */     {
/* 655 */       int stop = index + count;
/* 656 */       for (i = index; i < stop; )
/*     */       {
/* 658 */         this.m_array[i] = this.m_array[(++i)];
/*     */       }
/*     */     }
/* 661 */     this.m_length -= 1;
/* 662 */     this.m_array[this.m_length] = null;
/* 663 */     assert (assertState());
/* 664 */     this.m_generation += 1;
/*     */   }
/*     */ 
/*     */   public boolean retainAll(Collection c)
/*     */   {
/* 670 */     assert (assertState());
/* 671 */     boolean rc = false;
/* 672 */     for (int i = 0; i < this.m_length; ++i)
/*     */     {
/* 674 */       if (c.contains(this.m_array[i]))
/*     */         continue;
/* 676 */       rc = true;
/* 677 */       remove(i);
/* 678 */       this.m_generation += 1;
/* 679 */       --i;
/*     */     }
/*     */ 
/* 682 */     assert (assertState());
/* 683 */     return rc;
/*     */   }
/*     */ 
/*     */   public E set(int index, E e)
/*     */   {
/* 688 */     if (index >= this.m_length)
/*     */     {
/* 690 */       throw new ArrayIndexOutOfBoundsException(new StringBuilder().append("").append(index).append(" >= ").append(this.m_length).toString());
/*     */     }
/* 692 */     Object orig = this.m_array[index];
/* 693 */     this.m_array[index] = e;
/* 694 */     this.m_generation += 1;
/* 695 */     return orig;
/*     */   }
/*     */ 
/*     */   public void setElementAt(E o, int index)
/*     */   {
/* 700 */     if (index >= this.m_length)
/*     */     {
/* 702 */       throw new ArrayIndexOutOfBoundsException(new StringBuilder().append("").append(index).append(" >= ").append(this.m_length).toString());
/*     */     }
/* 704 */     this.m_generation += 1;
/* 705 */     this.m_array[index] = o;
/*     */   }
/*     */ 
/*     */   public void setSize(int targetSize)
/*     */   {
/* 710 */     if (targetSize > this.m_arrayLength)
/*     */     {
/* 712 */       alloc(targetSize, true);
/*     */     }
/* 714 */     this.m_length = targetSize;
/* 715 */     this.m_generation += 1;
/* 716 */     assert (assertState());
/*     */   }
/*     */ 
/*     */   public int size()
/*     */   {
/* 721 */     return this.m_length;
/*     */   }
/*     */ 
/*     */   public List subList(int from, int to)
/*     */   {
/* 726 */     if (to >= this.m_length)
/*     */     {
/* 728 */       throw new ArrayIndexOutOfBoundsException(new StringBuilder().append("").append(to).append(" >= ").append(this.m_length).toString());
/*     */     }
/* 730 */     IdcVector l = new IdcVector(from - to);
/* 731 */     int j = 0;
/* 732 */     for (int i = from; i < to; ++i)
/*     */     {
/* 734 */       l.m_array[(j++)] = this.m_array[i];
/*     */     }
/* 736 */     l.m_length = j;
/* 737 */     return l;
/*     */   }
/*     */ 
/*     */   public Object[] toArray()
/*     */   {
/* 742 */     Object[] a = new Object[this.m_length];
/* 743 */     if (this.m_length > 16)
/*     */     {
/* 745 */       System.arraycopy(this.m_array, 0, a, 0, this.m_length);
/*     */     }
/*     */     else
/*     */     {
/* 749 */       for (int i = 0; i < this.m_length; ++i)
/*     */       {
/* 751 */         a[i] = this.m_array[i];
/*     */       }
/*     */     }
/* 754 */     return a;
/*     */   }
/*     */ 
/*     */   public <T> T[] toArray(T[] a)
/*     */   {
/* 759 */     if (a.length < this.m_length)
/*     */     {
/*     */       try
/*     */       {
/* 763 */         Class c = a.getClass();
/* 764 */         a = (Object[])(Object[])Array.newInstance(c.getComponentType(), this.m_length);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 768 */         throw new AssertionError(e);
/*     */       }
/*     */     }
/* 771 */     if (this.m_length > 16)
/*     */     {
/* 773 */       System.arraycopy(this.m_array, 0, a, 0, this.m_length);
/*     */     }
/*     */     else
/*     */     {
/* 777 */       for (int i = 0; i < this.m_length; ++i)
/*     */       {
/* 779 */         a[i] = this.m_array[i];
/*     */       }
/*     */     }
/* 782 */     return a;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 787 */     StringBuilder builder = new StringBuilder("[");
/* 788 */     for (int i = 0; i < this.m_length; ++i)
/*     */     {
/* 790 */       if (i > 0)
/*     */       {
/* 792 */         builder.append(", ");
/*     */       }
/* 794 */       builder.append(this.m_array[i]);
/*     */     }
/* 796 */     builder.append(']');
/* 797 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public void trimToSize()
/*     */   {
/*     */   }
/*     */ 
/*     */   public Iterator<E> iterator()
/*     */   {
/* 808 */     return new IdcVectorIterator(this);
/*     */   }
/*     */ 
/*     */   public ListIterator<E> listIterator()
/*     */   {
/* 813 */     return new IdcVectorListIterator(this);
/*     */   }
/*     */ 
/*     */   public ListIterator<E> listIterator(int index)
/*     */   {
/* 818 */     return new IdcVectorListIterator(this, index);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 823 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcVector
 * JD-Core Version:    0.5.4
 */