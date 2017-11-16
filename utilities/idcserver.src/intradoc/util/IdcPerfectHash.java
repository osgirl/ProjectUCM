/*     */ package intradoc.util;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.ObjectInputStream;
/*     */ import java.io.ObjectOutputStream;
/*     */ import java.io.ObjectStreamException;
/*     */ import java.io.PrintStream;
/*     */ import java.io.Serializable;
/*     */ import java.util.Map;
/*     */ import java.util.Random;
/*     */ 
/*     */ public class IdcPerfectHash
/*     */   implements Serializable
/*     */ {
/*     */   public static final long serialVersionUID = 4L;
/*     */   protected Object[] m_data;
/*     */   protected int m_count;
/*     */   protected int m_maxHashCode;
/*     */   protected int m_h1;
/*     */   protected int m_h2;
/*     */   protected int m_h3;
/*     */   protected int[][] m_vertexValues;
/*     */   protected int m_activeMapping;
/*     */   protected int m_graphSize;
/*     */   protected int m_tries;
/*     */   protected int m_treeCount;
/*     */   public Random m_random;
/*     */   public PrintStream m_debugStream;
/*     */   public GenericTracingCallback m_traceCallback;
/*     */ 
/*     */   public IdcPerfectHash()
/*     */   {
/*     */   }
/*     */ 
/*     */   public IdcPerfectHash(Random r)
/*     */   {
/*  58 */     this.m_random = r;
/*     */   }
/*     */ 
/*     */   public void copyFrom(IdcPerfectHash source)
/*     */   {
/*  63 */     this.m_data = source.m_data;
/*  64 */     this.m_count = source.m_count;
/*  65 */     this.m_h1 = source.m_h1;
/*  66 */     this.m_h2 = source.m_h2;
/*  67 */     this.m_h3 = source.m_h3;
/*  68 */     this.m_vertexValues = source.m_vertexValues;
/*  69 */     this.m_activeMapping = source.m_activeMapping;
/*  70 */     this.m_graphSize = source.m_graphSize;
/*  71 */     this.m_tries = source.m_tries;
/*  72 */     this.m_treeCount = source.m_treeCount;
/*     */ 
/*  74 */     if (source.m_random != null)
/*     */     {
/*  76 */       this.m_random = source.m_random;
/*     */     }
/*     */ 
/*  79 */     this.m_debugStream = source.m_debugStream;
/*  80 */     this.m_traceCallback = source.m_traceCallback;
/*     */   }
/*     */ 
/*     */   public int computeH1(int code, int n)
/*     */   {
/*  86 */     code ^= code >>> 20 & code >>> 12;
/*  87 */     code ^= code >>> 7 ^ code >>> 4;
/*     */ 
/*  89 */     int h1 = (code * this.m_h2 + this.m_h3) % n;
/*  90 */     if (h1 < 0)
/*     */     {
/*  92 */       h1 += n;
/*     */     }
/*  94 */     return h1;
/*     */   }
/*     */ 
/*     */   public int computeH2(int code, int n, int h1)
/*     */   {
/*  99 */     code ^= code >>> 7 ^ code >>> 4;
/* 100 */     code ^= code >>> 20 & code >>> 12;
/* 101 */     int h2 = (code * this.m_h1 + this.m_h2) % n;
/* 102 */     if (h2 < 0)
/*     */     {
/* 104 */       h2 += n;
/*     */     }
/* 106 */     if (h2 == h1)
/*     */     {
/* 108 */       h2 = (code * this.m_h3 + this.m_h1) % n;
/* 109 */       if (h2 < 0)
/*     */       {
/* 111 */         h2 += n;
/*     */       }
/*     */     }
/* 114 */     return h2;
/*     */   }
/*     */ 
/*     */   protected int computeHC(int[] vertexValues, int h1, int h2, int n)
/*     */   {
/* 119 */     int hc = (int)((vertexValues[h1] + vertexValues[h2] & 0x7FFFFFFF) % n);
/* 120 */     return hc;
/*     */   }
/*     */ 
/*     */   public void add(Object obj)
/*     */   {
/* 125 */     if (this.m_vertexValues != null)
/*     */     {
/* 127 */       this.m_vertexValues = ((int[][])null);
/*     */     }
/* 129 */     if (this.m_data == null)
/*     */     {
/* 131 */       this.m_data = new Object[256];
/*     */     }
/* 133 */     if (this.m_count >= this.m_data.length)
/*     */     {
/* 135 */       String[] data = new String[2 * this.m_count];
/* 136 */       System.arraycopy(this.m_data, 0, data, 0, this.m_count);
/* 137 */       this.m_data = data;
/*     */     }
/* 139 */     this.m_data[this.m_count] = obj;
/* 140 */     this.m_count += 1;
/*     */   }
/*     */ 
/*     */   public int[] hashify()
/*     */   {
/* 148 */     IntArraySort sorter = new IntArraySort();
/*     */ 
/* 150 */     int[] initialCodes = new int[2 * this.m_count];
/* 151 */     for (int i = 0; i < this.m_count; ++i)
/*     */     {
/* 153 */       initialCodes[(2 * i)] = this.m_data[i].hashCode();
/* 154 */       initialCodes[(2 * i + 1)] = i;
/*     */     }
/* 156 */     sorter.sort(initialCodes, 2, 0);
/* 157 */     for (int i = 1; i < this.m_count; ++i)
/*     */     {
/* 159 */       if (initialCodes[(2 * i)] != initialCodes[(2 * (i - 1))])
/*     */         continue;
/* 161 */       throw new AssertionError("duplicate hashCodes on objects " + this.m_data[initialCodes[(2 * i + 1)]] + " vs " + this.m_data[initialCodes[(2 * i - 1)]]);
/*     */     }
/*     */ 
/* 166 */     initialCodes = null;
/*     */ 
/* 168 */     int n = 11 * this.m_count / 10 + 20;
/* 169 */     int[] edges = new int[6 * this.m_count];
/*     */ 
/* 173 */     trace("hashing for " + this.m_count + " keys");
/* 174 */     this.m_tries = 0;
/*     */     while (true)
/*     */     {
/* 177 */       int tries = 50;
/* 178 */       int a = 115;
/* 179 */       int b = 100;
/* 180 */       if (++this.m_tries % 50 == 0)
/*     */       {
/* 182 */         n = 115 * n / 100;
/* 183 */         trace("graph size is " + n);
/*     */       }
/* 185 */       boolean broken = false;
/*     */       do
/* 187 */         this.m_h1 = this.m_random.nextInt(); while ((this.m_h1 == 0) || (this.m_h1 == 1));
/*     */       do this.m_h2 = this.m_random.nextInt(); while ((this.m_h2 == 0) || (this.m_h2 == 1) || (this.m_h2 == this.m_h1));
/*     */       do this.m_h3 = this.m_random.nextInt(); while ((this.m_h3 == 0) || (this.m_h3 == 1) || (this.m_h3 == this.m_h1) || (this.m_h3 == this.m_h2));
/*     */ 
/* 191 */       for (int i = 0; i < this.m_count; ++i)
/*     */       {
/* 193 */         int code = this.m_data[i].hashCode();
/* 194 */         int h1 = computeH1(code, n);
/* 195 */         int h2 = computeH2(code, n, h1);
/* 196 */         if (h1 == h2)
/*     */         {
/* 198 */           trace("trivial cycle detected");
/* 199 */           broken = true;
/* 200 */           break;
/*     */         }
/* 202 */         assert (h1 < n);
/* 203 */         assert (h2 < n);
/* 204 */         assert (h1 >= 0);
/* 205 */         assert (h2 >= 0);
/* 206 */         edges[(6 * i)] = h1;
/* 207 */         edges[(6 * i + 1)] = h2;
/* 208 */         edges[(6 * i + 2)] = i;
/* 209 */         edges[(6 * i + 3)] = h2;
/* 210 */         edges[(6 * i + 4)] = h1;
/* 211 */         edges[(6 * i + 5)] = i;
/*     */       }
/* 213 */       if (broken)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 218 */       sorter.sort(edges, 3, 0);
/* 219 */       this.m_graphSize = n;
/* 220 */       resetVertexValues(0);
/*     */       try
/*     */       {
/* 223 */         walk(this.m_vertexValues[0], edges, null);
/*     */       }
/*     */       catch (IdcException e)
/*     */       {
/* 228 */         trace(e);
/*     */       }
/*     */     }
/* 231 */     return edges;
/*     */   }
/*     */ 
/*     */   public void resetVertexValues(int graphIndex)
/*     */   {
/* 236 */     if (this.m_vertexValues == null)
/*     */     {
/* 238 */       this.m_vertexValues = new int[graphIndex + 1][];
/*     */     }
/* 240 */     if (graphIndex >= this.m_vertexValues.length)
/*     */     {
/* 242 */       int[][] newVertexValues = new int[graphIndex + 1][];
/* 243 */       System.arraycopy(this.m_vertexValues, 0, newVertexValues, 0, this.m_vertexValues.length);
/* 244 */       this.m_vertexValues = newVertexValues;
/*     */     }
/* 246 */     this.m_vertexValues[graphIndex] = new int[this.m_graphSize];
/*     */   }
/*     */ 
/*     */   public void setActiveMapping(int mappingIndex)
/*     */   {
/* 251 */     this.m_activeMapping = mappingIndex;
/*     */   }
/*     */ 
/*     */   public int getActiveMapping()
/*     */   {
/* 256 */     return this.m_activeMapping;
/*     */   }
/*     */ 
/*     */   public int[] getMapping(int mappingIndex)
/*     */   {
/* 261 */     return this.m_vertexValues[mappingIndex];
/*     */   }
/*     */ 
/*     */   public void walk(int[] vertexValues, int[] edges, Map<Object, Integer> values) throws IdcException
/*     */   {
/* 266 */     boolean[] visited = new boolean[this.m_graphSize];
/* 267 */     int treeNumber = 0;
/* 268 */     for (int i = 0; i < edges.length; i += 3)
/*     */     {
/* 270 */       int vertex = edges[i];
/* 271 */       if (visited[vertex] != 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 275 */       vertexValues[vertex] = 0;
/* 276 */       walk(vertexValues, edges, visited, i / 3, -1, values);
/* 277 */       ++treeNumber;
/*     */     }
/*     */ 
/* 280 */     trace("tree count is " + treeNumber);
/* 281 */     this.m_treeCount = treeNumber;
/*     */   }
/*     */ 
/*     */   public void walk(int[] vertexValues, int[] edges, boolean[] visited, int currentVertexOffset, int priorVertex, Map<Object, Integer> values)
/*     */     throws IdcException
/*     */   {
/* 288 */     int currentVertex = edges[(3 * currentVertexOffset)];
/* 289 */     if (visited[currentVertex] != 0)
/*     */     {
/* 291 */       IdcMessage msg = new IdcMessage();
/* 292 */       msg.m_msgSimple = "!$Graph passed into walk() has a cycle.";
/* 293 */       IdcException e = new IdcException();
/* 294 */       e.m_message = msg;
/* 295 */       throw e;
/*     */     }
/* 297 */     visited[currentVertex] = true;
/*     */ 
/* 299 */     while ((currentVertexOffset < edges.length / 3) && (edges[(3 * currentVertexOffset)] == currentVertex))
/*     */     {
/* 302 */       int nextVertex = edges[(3 * currentVertexOffset + 1)];
/* 303 */       if (nextVertex != priorVertex)
/*     */       {
/* 308 */         int desiredIndex = edges[(3 * currentVertexOffset + 2)];
/* 309 */         if (values != null)
/*     */         {
/* 311 */           Object keyObject = this.m_data[desiredIndex];
/* 312 */           Integer desiredValue = (Integer)values.get(keyObject);
/* 313 */           if (desiredValue != null)
/*     */           {
/* 315 */             desiredIndex = desiredValue.intValue();
/*     */ 
/* 320 */             if (desiredIndex < 0)
/*     */             {
/* 322 */               desiredIndex *= -1;
/* 323 */               --desiredIndex;
/*     */             }
/* 325 */             if (desiredIndex > this.m_maxHashCode)
/*     */             {
/* 327 */               this.m_maxHashCode = desiredIndex;
/*     */             }
/*     */           }
/*     */         }
/* 331 */         vertexValues[nextVertex] = (desiredIndex - vertexValues[currentVertex]);
/*     */ 
/* 334 */         int l = 0;
/* 335 */         int r = edges.length / 3;
/* 336 */         int nextVertexOffset = r / 2;
/* 337 */         while (edges[(3 * nextVertexOffset)] != nextVertex)
/*     */         {
/* 339 */           if (l == r)
/*     */           {
/* 341 */             int i;
/* 341 */             if (this.m_debugStream != null) for (i = 0; i < edges.length; )
/*     */               {
/* 343 */                 this.m_debugStream.println("edge:\t" + edges[(i++)] + "\t" + edges[(i++)] + "\t" + edges[(i++)]);
/*     */               }
/*     */ 
/* 346 */             throw new AssertionError("vertex " + nextVertex + " not found");
/*     */           }
/* 348 */           if (edges[(3 * nextVertexOffset)] > nextVertex)
/*     */           {
/* 350 */             r = nextVertexOffset;
/*     */           }
/*     */           else
/*     */           {
/* 354 */             l = nextVertexOffset + 1;
/*     */           }
/* 356 */           nextVertexOffset = l + (r - l) / 2;
/*     */         }
/*     */ 
/* 360 */         while ((nextVertexOffset >= 0) && (edges[(3 * nextVertexOffset)] == nextVertex))
/*     */         {
/* 362 */           --nextVertexOffset;
/*     */         }
/* 364 */         ++nextVertexOffset;
/*     */ 
/* 366 */         walk(vertexValues, edges, visited, nextVertexOffset, currentVertex, values);
/*     */       }
/* 300 */       ++currentVertexOffset;
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getCode(Object key)
/*     */   {
/* 372 */     if (this.m_vertexValues == null)
/*     */     {
/* 374 */       hashify();
/*     */     }
/* 376 */     int code = key.hashCode();
/* 377 */     int n = this.m_graphSize;
/*     */ 
/* 379 */     int h1 = computeH1(code, n);
/* 380 */     int h2 = computeH2(code, n, h1);
/* 381 */     int hc = computeHC(this.m_vertexValues[0], h1, h2, n);
/* 382 */     if ((hc < 0) || (hc >= this.m_count))
/*     */     {
/* 384 */       return -1;
/*     */     }
/* 386 */     Object match = this.m_data[hc];
/* 387 */     assert (match != null);
/* 388 */     if ((key == match) || ((key.hashCode() == match.hashCode()) && (key.equals(match))))
/*     */     {
/* 390 */       return hc;
/*     */     }
/* 392 */     return -1;
/*     */   }
/*     */ 
/*     */   public int getActiveCode(Object key)
/*     */   {
/* 397 */     if (this.m_vertexValues == null)
/*     */     {
/* 399 */       hashify();
/*     */     }
/* 401 */     int code = key.hashCode();
/* 402 */     int n = this.m_graphSize;
/*     */ 
/* 404 */     int h1 = computeH1(code, n);
/* 405 */     int h2 = computeH2(code, n, h1);
/* 406 */     int hc = computeHC(this.m_vertexValues[0], h1, h2, n);
/* 407 */     if ((hc < 0) || (hc >= this.m_count))
/*     */     {
/* 409 */       return -1;
/*     */     }
/* 411 */     Object match = this.m_data[hc];
/* 412 */     assert (match != null);
/* 413 */     if ((key == match) || ((key.hashCode() == match.hashCode()) && (key.equals(match))))
/*     */     {
/* 416 */       hc = computeHC(this.m_vertexValues[this.m_activeMapping], h1, h2, n);
/* 417 */       return hc;
/*     */     }
/* 419 */     return -1;
/*     */   }
/*     */ 
/*     */   public int size()
/*     */   {
/* 424 */     return this.m_count;
/*     */   }
/*     */ 
/*     */   public int maxHashCode()
/*     */   {
/* 433 */     return this.m_maxHashCode;
/*     */   }
/*     */ 
/*     */   public int treeCount()
/*     */   {
/* 438 */     return this.m_treeCount;
/*     */   }
/*     */ 
/*     */   public int graphSize()
/*     */   {
/* 443 */     return this.m_graphSize;
/*     */   }
/*     */ 
/*     */   public Object get(int code)
/*     */   {
/* 448 */     return this.m_data[code];
/*     */   }
/*     */ 
/*     */   public Object get(Object key)
/*     */   {
/* 453 */     if (this.m_vertexValues == null)
/*     */     {
/* 455 */       hashify();
/*     */     }
/* 457 */     int code = getCode(key);
/* 458 */     if (code < 0)
/*     */     {
/* 460 */       return null;
/*     */     }
/* 462 */     return this.m_data[code];
/*     */   }
/*     */ 
/*     */   private void writeObject(ObjectOutputStream out)
/*     */     throws IOException
/*     */   {
/* 469 */     if (this.m_vertexValues == null)
/*     */     {
/* 471 */       hashify();
/*     */     }
/* 473 */     out.writeInt(this.m_h1);
/* 474 */     out.writeInt(this.m_h2);
/* 475 */     out.writeInt(this.m_h3);
/* 476 */     out.writeInt(this.m_vertexValues.length);
/* 477 */     out.writeInt(this.m_graphSize);
/* 478 */     out.writeInt(this.m_maxHashCode);
/* 479 */     out.writeInt(this.m_treeCount);
/* 480 */     for (int i = 0; i < this.m_vertexValues.length; ++i)
/*     */     {
/* 482 */       for (int j = 0; j < this.m_graphSize; ++j)
/*     */       {
/* 484 */         out.writeInt(this.m_vertexValues[i][j]);
/*     */       }
/*     */     }
/* 487 */     out.writeInt(this.m_count);
/* 488 */     for (int i = 0; i < this.m_count; ++i)
/*     */     {
/* 492 */       out.writeChar(111);
/* 493 */       out.writeObject(this.m_data[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
/*     */   {
/* 499 */     this.m_h1 = in.readInt();
/* 500 */     this.m_h2 = in.readInt();
/* 501 */     this.m_h3 = in.readInt();
/* 502 */     int n = in.readInt();
/* 503 */     this.m_graphSize = in.readInt();
/* 504 */     this.m_maxHashCode = in.readInt();
/* 505 */     this.m_treeCount = in.readInt();
/* 506 */     this.m_vertexValues = new int[n][];
/* 507 */     for (int i = 0; i < n; ++i)
/*     */     {
/* 509 */       this.m_vertexValues[i] = new int[this.m_graphSize];
/* 510 */       for (int j = 0; j < this.m_graphSize; ++j)
/*     */       {
/* 512 */         this.m_vertexValues[i][j] = in.readInt();
/*     */       }
/*     */     }
/* 515 */     this.m_count = in.readInt();
/* 516 */     this.m_data = new Object[this.m_count];
/* 517 */     for (int i = 0; i < this.m_count; ++i)
/*     */     {
/* 519 */       char type = in.readChar();
/* 520 */       if (type != 'o')
/*     */       {
/* 522 */         IdcMessage msg = new IdcMessage("syDeserializationError", new Object[] { "" + type });
/* 523 */         IdcException e = new IdcException(null, msg);
/* 524 */         e.setContainerAttribute("isWrapped", "1");
/* 525 */         IOException ioe = new IOException();
/* 526 */         ioe.initCause(e);
/* 527 */         throw ioe;
/*     */       }
/* 529 */       this.m_data[i] = in.readObject();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void trace(Object dat)
/*     */   {
/* 535 */     GenericTracingCallback tracer = this.m_traceCallback;
/* 536 */     if (tracer == null)
/*     */       return;
/* 538 */     tracer.report(6, new Object[] { dat });
/*     */   }
/*     */ 
/*     */   protected void dummy()
/*     */     throws ObjectStreamException
/*     */   {
/* 545 */     readObjectNoData();
/*     */   }
/*     */ 
/*     */   private void readObjectNoData()
/*     */     throws ObjectStreamException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 555 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80679 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcPerfectHash
 * JD-Core Version:    0.5.4
 */