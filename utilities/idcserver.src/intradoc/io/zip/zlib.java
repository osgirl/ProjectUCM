/*      */ package intradoc.io.zip;
/*      */ 
/*      */ public class zlib
/*      */ {
/*      */   static final String ZLIB_VERSION = "1.2.3";
/*      */   static final int ZLIB_VERNUM = 4656;
/*      */   static final int MAX_WBITS = 15;
/*      */   static final int DEF_WBITS = 15;
/*      */   static final int STORED_BLOCK = 0;
/*      */   static final int STATIC_TREES = 1;
/*      */   static final int DYN_TREES = 2;
/*      */   static final int MIN_MATCH = 3;
/*      */   static final int MAX_MATCH = 258;
/*      */   static final int ENOUGH = 2048;
/*      */   static final int MAXD = 592;
/*      */   static final int CODES = 0;
/*      */   static final int LENS = 1;
/*      */   static final int DISTS = 2;
/*      */   static final int HEAD = 0;
/*      */   static final int DICTID = 9;
/*      */   static final int DICT = 10;
/*      */   static final int TYPE = 11;
/*      */   static final int TYPEDO = 12;
/*      */   static final int STORED = 13;
/*      */   static final int COPY = 14;
/*      */   static final int TABLE = 15;
/*      */   static final int LENLENS = 16;
/*      */   static final int CODELENS = 17;
/*      */   static final int LEN = 18;
/*      */   static final int LENEXT = 19;
/*      */   static final int DIST = 20;
/*      */   static final int DISTEXT = 21;
/*      */   static final int MATCH = 22;
/*      */   static final int LIT = 23;
/*      */   static final int CHECK = 25;
/*      */   static final int DONE = 26;
/*      */   static final int BAD = 27;
/*      */   public static final int Z_NO_FLUSH = 0;
/*      */   public static final int Z_PARTIAL_FLUSH = 1;
/*      */   public static final int Z_SYNC_FLUSH = 2;
/*      */   public static final int Z_FULL_FLUSH = 3;
/*      */   public static final int Z_FINISH = 4;
/*      */   public static final int Z_BLOCK = 5;
/*      */   public static final int Z_OK = 0;
/*      */   public static final int Z_STREAM_END = 1;
/*      */   public static final int Z_NEED_DICT = 2;
/*      */   public static final int Z_ERRNO = -1;
/*      */   public static final int Z_STREAM_ERROR = -2;
/*      */   public static final int Z_DATA_ERROR = -3;
/*      */   public static final int Z_BUF_ERROR = -4;
/*      */   static final int Z_DEFLATED = 8;
/*      */   static final int BASE = 65521;
/*      */   static final int NMAX = 5552;
/*      */   static final int MAXBITS = 15;
/*  333 */   static final short[] LBASE = { 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258, 0, 0 };
/*      */ 
/*  338 */   static final short[] LEXT = { 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 18, 18, 18, 18, 19, 19, 19, 19, 20, 20, 20, 20, 21, 21, 21, 21, 16, 64, 64 };
/*      */ 
/*  347 */   static final short[] DBASE = { 1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577, 0, 0 };
/*      */ 
/*  353 */   static final short[] DEXT = { 16, 16, 16, 16, 17, 17, 18, 18, 19, 19, 20, 20, 21, 21, 22, 22, 23, 23, 24, 24, 25, 25, 26, 26, 27, 27, 28, 28, 29, 29, 64, 64 };
/*      */ 
/*  619 */   static boolean debug = false;
/*      */   static short FIXED_TABLE_FIRST_DCODE;
/*      */   static int[] FIXED_TABLE_CODES;
/*  646 */   static final short[] INFLATE_ORDER = { 16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15 };
/*      */ 
/*      */   static long adler32(long adler, byte[] buf, int index_buf, int len)
/*      */   {
/*  143 */     long sum2 = adler >> 16 & 0xFFFF;
/*  144 */     adler &= 65535L;
/*      */ 
/*  146 */     if (len == 1)
/*      */     {
/*  148 */       adler += buf[(index_buf + 0)];
/*  149 */       if (adler >= 65521L)
/*      */       {
/*  151 */         adler -= 65521L;
/*      */       }
/*  153 */       sum2 += adler;
/*  154 */       if (sum2 >= 65521L)
/*      */       {
/*  156 */         sum2 -= 65521L;
/*      */       }
/*  158 */       return adler & 0xFFFF | sum2 << 16;
/*      */     }
/*      */ 
/*  161 */     if (buf == null)
/*      */     {
/*  163 */       return 1L;
/*      */     }
/*      */ 
/*  166 */     if (len < 16)
/*      */     {
/*  168 */       while (len-- != 0)
/*      */       {
/*  170 */         adler += buf[(index_buf++)];
/*  171 */         sum2 += adler;
/*      */       }
/*  173 */       if (adler >= 65521L)
/*      */       {
/*  175 */         adler -= 65521L;
/*      */       }
/*  177 */       sum2 %= 65521L;
/*  178 */       return adler & 0xFFFF | sum2 << 16;
/*      */     }
/*      */ 
/*  181 */     while (len >= 5552)
/*      */     {
/*  183 */       len -= 5552;
/*  184 */       int n = 347;
/*      */       do
/*      */       {
/*  187 */         adler += buf[(index_buf++)];
/*  188 */         sum2 += adler;
/*  189 */         adler += buf[(index_buf++)];
/*  190 */         sum2 += adler;
/*  191 */         adler += buf[(index_buf++)];
/*  192 */         sum2 += adler;
/*  193 */         adler += buf[(index_buf++)];
/*  194 */         sum2 += adler;
/*  195 */         adler += buf[(index_buf++)];
/*  196 */         sum2 += adler;
/*  197 */         adler += buf[(index_buf++)];
/*  198 */         sum2 += adler;
/*  199 */         adler += buf[(index_buf++)];
/*  200 */         sum2 += adler;
/*  201 */         adler += buf[(index_buf++)];
/*  202 */         sum2 += adler;
/*  203 */         adler += buf[(index_buf++)];
/*  204 */         sum2 += adler;
/*  205 */         adler += buf[(index_buf++)];
/*  206 */         sum2 += adler;
/*  207 */         adler += buf[(index_buf++)];
/*  208 */         sum2 += adler;
/*  209 */         adler += buf[(index_buf++)];
/*  210 */         sum2 += adler;
/*  211 */         adler += buf[(index_buf++)];
/*  212 */         sum2 += adler;
/*  213 */         adler += buf[(index_buf++)];
/*  214 */         sum2 += adler;
/*  215 */         adler += buf[(index_buf++)];
/*  216 */         sum2 += adler;
/*  217 */         adler += buf[(index_buf++)];
/*  218 */         sum2 += adler;
/*  219 */       }while (--n != 0);
/*  220 */       adler %= 65521L;
/*  221 */       sum2 %= 65521L;
/*      */     }
/*      */ 
/*  224 */     if (len != 0)
/*      */     {
/*  226 */       while (len >= 16)
/*      */       {
/*  228 */         len -= 16;
/*  229 */         adler += buf[(index_buf++)];
/*  230 */         sum2 += adler;
/*  231 */         adler += buf[(index_buf++)];
/*  232 */         sum2 += adler;
/*  233 */         adler += buf[(index_buf++)];
/*  234 */         sum2 += adler;
/*  235 */         adler += buf[(index_buf++)];
/*  236 */         sum2 += adler;
/*  237 */         adler += buf[(index_buf++)];
/*  238 */         sum2 += adler;
/*  239 */         adler += buf[(index_buf++)];
/*  240 */         sum2 += adler;
/*  241 */         adler += buf[(index_buf++)];
/*  242 */         sum2 += adler;
/*  243 */         adler += buf[(index_buf++)];
/*  244 */         sum2 += adler;
/*  245 */         adler += buf[(index_buf++)];
/*  246 */         sum2 += adler;
/*  247 */         adler += buf[(index_buf++)];
/*  248 */         sum2 += adler;
/*  249 */         adler += buf[(index_buf++)];
/*  250 */         sum2 += adler;
/*  251 */         adler += buf[(index_buf++)];
/*  252 */         sum2 += adler;
/*  253 */         adler += buf[(index_buf++)];
/*  254 */         sum2 += adler;
/*  255 */         adler += buf[(index_buf++)];
/*  256 */         sum2 += adler;
/*  257 */         adler += buf[(index_buf++)];
/*  258 */         sum2 += adler;
/*  259 */         adler += buf[(index_buf++)];
/*  260 */         sum2 += adler;
/*      */       }
/*  262 */       while (len-- != 0)
/*      */       {
/*  264 */         adler += buf[(index_buf++)];
/*  265 */         sum2 += adler;
/*      */       }
/*  267 */       adler %= 65521L;
/*  268 */       sum2 %= 65521L;
/*      */     }
/*      */ 
/*  271 */     return adler & 0xFFFF | sum2 << 16;
/*      */   }
/*      */ 
/*      */   static int inflate_table(int type, int index_lens, int codes, short bits, inflate_state state)
/*      */     throws zlib.zlibException
/*      */   {
/*  370 */     short[] count = state.count;
/*  371 */     short[] offs = state.offs;
/*  372 */     short[] lens = state.lens;
/*  373 */     short[] work = state.work;
/*  374 */     int[] table = state.codes;
/*      */ 
/*  387 */     for (int len = 0; len <= 15; ++len)
/*      */     {
/*  389 */       count[len] = 0;
/*      */     }
/*  391 */     for (int sym = 0; sym < codes; ++sym)
/*      */     {
/*      */       short tmp75_74 = lens[(index_lens + sym)];
/*      */       short[] tmp75_66 = count; tmp75_66[tmp75_74] = (short)(tmp75_66[tmp75_74] + 1);
/*      */     }
/*      */ 
/*  396 */     int root = bits;
/*  397 */     for (int max = 15; max >= 1; --max)
/*      */     {
/*  399 */       if (count[max] != 0) {
/*      */         break;
/*      */       }
/*      */     }
/*      */ 
/*  404 */     if (root > max) root = max;
/*  405 */     if (max == 0)
/*      */     {
/*  408 */       int code = 1073807360;
/*  409 */       table[(state.index_codes++)] = code;
/*  410 */       table[(state.index_codes++)] = code;
/*  411 */       return 1;
/*      */     }
/*  413 */     for (int min = 1; min <= 15; ++min)
/*      */     {
/*  415 */       if (count[min] != 0) {
/*      */         break;
/*      */       }
/*      */     }
/*      */ 
/*  420 */     if (root < min) root = min;
/*      */ 
/*  422 */     int left = 1;
/*  423 */     for (len = 1; len <= 15; ++len)
/*      */     {
/*  425 */       left <<= 1;
/*  426 */       left -= count[len];
/*  427 */       if (left >= 0)
/*      */         continue;
/*  429 */       throw new zlibException("over-subscribed");
/*      */     }
/*      */ 
/*  432 */     if ((left > 0) && (((type == 0) || (max != 1))))
/*      */     {
/*  434 */       throw new zlibException("incomplete set");
/*      */     }
/*      */ 
/*  437 */     offs[1] = 0;
/*  438 */     for (len = 1; len < 15; ++len)
/*      */     {
/*  440 */       offs[(len + 1)] = (short)(offs[len] + count[len]);
/*      */     }
/*      */ 
/*  443 */     for (sym = 0; sym < codes; ++sym)
/*      */     {
/*  445 */       if (lens[(index_lens + sym)] == 0)
/*      */         continue;
/*      */       short tmp356_355 = lens[(index_lens + sym)];
/*      */       short[] tmp356_347 = offs;
/*      */       short tmp358_357 = tmp356_347[tmp356_355]; tmp356_347[tmp356_355] = (short)(tmp358_357 + 1); work[tmp358_357] = (short)sym;
/*      */     }
/*      */     short[] extra;
/*      */     short[] base;
/*      */     int index_extra;
/*      */     int index_base;
/*      */     int end;
/*  451 */     switch (type)
/*      */     {
/*      */     case 0:
/*  454 */       base = extra = work;
/*  455 */       index_base = index_extra = 0;
/*  456 */       end = 19;
/*  457 */       break;
/*      */     case 1:
/*  459 */       base = LBASE;
/*  460 */       extra = LEXT;
/*  461 */       index_base = index_extra = -257;
/*  462 */       end = 256;
/*  463 */       break;
/*      */     case 2:
/*  465 */       base = DBASE;
/*  466 */       extra = DEXT;
/*  467 */       index_base = index_extra = 0;
/*  468 */       end = -1;
/*  469 */       break;
/*      */     default:
/*  471 */       throw new zlibException("unknown table type");
/*      */     }
/*      */ 
/*  474 */     int huff = 0;
/*  475 */     sym = 0;
/*  476 */     len = min;
/*      */ 
/*  478 */     int index_next = state.index_codes;
/*  479 */     int curr = root;
/*  480 */     int drop = 0;
/*  481 */     int low = -1;
/*  482 */     int used = 1 << root;
/*  483 */     int mask = used - 1;
/*      */ 
/*  485 */     if ((type == 1) && (used >= 1456))
/*      */     {
/*  487 */       throw new zlibException("length table too large");
/*      */     }
/*      */ 
/*      */     int incr;
/*      */     while (true)
/*      */     {
/*  493 */       code = (len - drop & 0xFF) << 16;
/*  494 */       if (work[sym] < end)
/*      */       {
/*  497 */         code |= work[sym] & 0xFFFF;
/*      */       }
/*  499 */       else if (work[sym] > end)
/*      */       {
/*  502 */         code |= (extra[(index_extra + work[sym])] & 0xFF) << 24;
/*      */ 
/*  504 */         code |= base[(index_base + work[sym])] & 0xFFFF;
/*      */       }
/*      */       else
/*      */       {
/*  509 */         code |= 1610612736;
/*      */       }
/*      */ 
/*  512 */       incr = 1 << len - drop;
/*  513 */       int fill = 1 << curr;
/*  514 */       min = fill;
/*      */       do
/*      */       {
/*  517 */         fill -= incr;
/*  518 */         table[(index_next + (huff >> drop) + fill)] = code;
/*  519 */       }while (fill != 0);
/*      */ 
/*  521 */       incr = 1 << len - 1;
/*  522 */       while ((huff & incr) != 0)
/*      */       {
/*  524 */         incr >>= 1;
/*      */       }
/*  526 */       if (incr != 0)
/*      */       {
/*  528 */         huff &= incr - 1;
/*  529 */         huff += incr;
/*      */       }
/*      */       else
/*      */       {
/*  533 */         huff = 0;
/*      */       }
/*      */ 
/*  536 */       ++sym;
/*      */       int tmp748_746 = len;
/*      */       short[] tmp748_744 = count; if ((tmp748_744[tmp748_746] = (short)(tmp748_744[tmp748_746] - 1)) == 0)
/*      */       {
/*  539 */         if (len == max) break;
/*  540 */         len = lens[(index_lens + work[sym])];
/*      */       }
/*      */ 
/*  543 */       if ((len <= root) || ((huff & mask) == low))
/*      */         continue;
/*  545 */       if (drop == 0)
/*      */       {
/*  547 */         drop = root;
/*      */       }
/*      */ 
/*  550 */       index_next += min;
/*      */ 
/*  552 */       curr = len - drop;
/*  553 */       left = 1 << curr;
/*  554 */       while (curr + drop < max)
/*      */       {
/*  556 */         left -= count[(curr + drop)];
/*  557 */         if (left <= 0) {
/*      */           break;
/*      */         }
/*      */ 
/*  561 */         ++curr;
/*  562 */         left <<= 1;
/*      */       }
/*      */ 
/*  565 */       used += (1 << curr);
/*  566 */       if ((type == 1) && (used >= 1456))
/*      */       {
/*  568 */         throw new zlibException("length table too large");
/*      */       }
/*      */ 
/*  571 */       low = huff & mask;
/*      */ 
/*  573 */       code = (curr & 0xFF) << 24;
/*      */ 
/*  575 */       code |= (root & 0xFF) << 16;
/*      */ 
/*  577 */       code |= index_next - state.index_codes & 0xFFFF;
/*  578 */       table[(state.index_codes + low)] = code;
/*      */     }
/*      */ 
/*  583 */     int code = 0x40000000 | (len - drop & 0xFF) << 16;
/*  584 */     while (huff != 0)
/*      */     {
/*  586 */       if ((drop != 0) && ((huff & mask) != low))
/*      */       {
/*  588 */         drop = 0;
/*  589 */         len = root;
/*  590 */         index_next = state.index_codes;
/*      */ 
/*  592 */         code = code & 0xFF00FFFF | (len & 0xFF) << 16;
/*      */       }
/*      */ 
/*  595 */       table[(index_next + (huff >> drop))] = code;
/*      */ 
/*  597 */       incr = 1 << len - 1;
/*  598 */       while ((huff & incr) != 0)
/*      */       {
/*  600 */         incr >>= 1;
/*      */       }
/*  602 */       if (incr != 0)
/*      */       {
/*  604 */         huff &= incr - 1;
/*  605 */         huff += incr;
/*      */       }
/*      */ 
/*  609 */       huff = 0;
/*      */     }
/*      */ 
/*  613 */     state.index_codes += used;
/*  614 */     return root;
/*      */   }
/*      */ 
/*      */   static void makefixed(inflate_state state)
/*      */     throws zlib.zlibException
/*      */   {
/*  624 */     short[] lens = state.lens;
/*      */ 
/*  626 */     short sym = 0;
/*  627 */     for (; sym < 144; lens[sym] = 8) sym = (short)(sym + 1);
/*  628 */     for (; sym < 256; lens[sym] = 9) sym = (short)(sym + 1);
/*  629 */     for (; sym < 280; lens[sym] = 7) sym = (short)(sym + 1);
/*  630 */     for (; sym < 288; lens[sym] = 8) sym = (short)(sym + 1);
/*  631 */     state.index_codes = 0;
/*  632 */     inflate_table(1, 0, 288, 9, state);
/*  633 */     int first_dcode = state.index_codes;
/*  634 */     sym = 0;
/*  635 */     for (; sym < 32; lens[sym] = 5) sym = (short)(sym + 1);
/*  636 */     inflate_table(2, 0, 32, 5, state);
/*      */ 
/*  638 */     int[] fixed = new int[544];
/*  639 */     System.arraycopy(state.codes, 0, fixed, 0, fixed.length);
/*  640 */     FIXED_TABLE_FIRST_DCODE = (short)first_dcode;
/*  641 */     FIXED_TABLE_CODES = fixed;
/*      */   }
/*      */ 
/*      */   public static int inflateReset(z_stream strm)
/*      */   {
/*  709 */     if (strm == null)
/*      */     {
/*  711 */       return -2;
/*      */     }
/*  713 */     inflate_state state = (inflate_state)strm.state;
/*  714 */     strm.total_in = (strm.total_out = 0L);
/*  715 */     strm.msg = null;
/*  716 */     strm.adler = 1L;
/*  717 */     state.mode = 0;
/*  718 */     state.last = false;
/*  719 */     state.havedict = false;
/*  720 */     state.dmax = 32768;
/*  721 */     state.whave = 0;
/*  722 */     state.write = 0;
/*  723 */     state.hold = 0L;
/*  724 */     state.bits = 0;
/*  725 */     state.index_lencode = 0;
/*  726 */     state.index_distcode = 0;
/*  727 */     return 0;
/*      */   }
/*      */ 
/*      */   public static int inflateInit2(z_stream strm, int windowBits)
/*      */   {
/*  732 */     if (strm == null)
/*      */     {
/*  734 */       return -2;
/*      */     }
/*  736 */     strm.msg = null;
/*  737 */     inflate_state state = new inflate_state();
/*  738 */     strm.state = state;
/*  739 */     if (windowBits < 0)
/*      */     {
/*  741 */       state.wrap = false;
/*  742 */       windowBits = -windowBits;
/*      */     }
/*      */     else
/*      */     {
/*  746 */       state.wrap = true;
/*      */     }
/*  748 */     if ((windowBits < 8) || (windowBits > 15))
/*      */     {
/*  750 */       strm.state = null;
/*  751 */       return -2;
/*      */     }
/*  753 */     state.wbits = windowBits;
/*  754 */     state.window = null;
/*  755 */     return inflateReset(strm);
/*      */   }
/*      */ 
/*      */   public static int inflateInit(z_stream strm)
/*      */   {
/*  760 */     return inflateInit2(strm, 15);
/*      */   }
/*      */ 
/*      */   public static int inflate(z_stream strm, int flush)
/*      */   {
/*  772 */     if ((strm == null) || (strm.state == null) || (strm.output == null) || ((strm.input == null) && (strm.avail_in != 0)))
/*      */     {
/*  775 */       return -2;
/*      */     }
/*  777 */     inflate_state state = (inflate_state)strm.state;
/*  778 */     if (state.mode == 11) state.mode = 12;
/*      */ 
/*  783 */     byte[] input = strm.input;
/*  784 */     byte[] output = strm.output;
/*      */ 
/*  786 */     int put = strm.index_out;
/*  787 */     int left = strm.avail_out;
/*  788 */     int next = strm.index_in;
/*  789 */     int have = strm.avail_in;
/*  790 */     long hold = state.hold;
/*  791 */     int bits = state.bits;
/*      */ 
/*  793 */     int in = have;
/*  794 */     int out = left;
/*  795 */     int ret = 0;
/*      */     label962: int copy;
/*      */     while (true)
/*      */     {
/*      */       int BITS;
/*      */       int len;
/*      */       int copy;
/*      */       label2188: int BITS;
/*      */       int code;
/*      */       int this_bits;
/*      */       int this_op;
/*      */       int this_val;
/*      */       int BITS;
/*  799 */       switch (state.mode)
/*      */       {
/*      */       case 0:
/*  802 */         if (!state.wrap)
/*      */         {
/*  804 */           state.mode = 12;
/*      */         }
/*      */ 
/*  808 */         while (bits < 16)
/*      */         {
/*  810 */           if (have == 0) break label3779;
/*  811 */           --have;
/*  812 */           hold += ((input[(next++)] & 0xFF) << bits);
/*  813 */           bits += 8;
/*      */         }
/*      */ 
/*  816 */         BITS = (int)(hold & 0xFF);
/*  817 */         if ((int)((BITS << 8) + (hold >> 8)) % 31 != 0)
/*      */         {
/*  819 */           strm.msg = "incorrect header check";
/*  820 */           state.mode = 27;
/*      */         }
/*      */ 
/*  824 */         BITS = (int)(hold & 0xF);
/*  825 */         if (BITS != 8)
/*      */         {
/*  827 */           strm.msg = "unknown compression method";
/*  828 */           state.mode = 27;
/*      */         }
/*      */ 
/*  832 */         hold >>= 4;
/*  833 */         bits -= 4;
/*      */ 
/*  835 */         BITS = (int)(hold & 0xF);
/*  836 */         len = BITS + 8;
/*  837 */         if (len > state.wbits)
/*      */         {
/*  839 */           strm.msg = "invalid window size";
/*  840 */           state.mode = 27;
/*      */         }
/*      */ 
/*  843 */         state.dmax = (1 << len);
/*  844 */         strm.adler = (state.check = adler32(0L, null, 0, 0));
/*  845 */         state.mode = (((hold & 0x200) != 0L) ? 9 : 11);
/*      */ 
/*  847 */         hold = 0L;
/*  848 */         bits = 0;
/*  849 */         break;
/*      */       case 9:
/*  852 */         while (bits < 32)
/*      */         {
/*  854 */           if (have == 0) break label3779;
/*  855 */           --have;
/*  856 */           hold += ((input[(next++)] & 0xFF) << bits);
/*  857 */           bits += 8;
/*      */         }
/*      */ 
/*  860 */         strm.adler = (state.check = (hold >> 24 & 0xFF) + (hold >> 8 & 0xFF00) + ((hold & 0xFF00) << 8) + ((hold & 0xFF) << 24));
/*      */ 
/*  863 */         hold = 0L;
/*  864 */         bits = 0;
/*  865 */         state.mode = 10;
/*      */       case 10:
/*  867 */         if (!state.havedict)
/*      */         {
/*  870 */           strm.index_out = put;
/*  871 */           strm.avail_out = left;
/*  872 */           strm.index_in = next;
/*  873 */           strm.avail_in = have;
/*  874 */           state.hold = hold;
/*  875 */           state.bits = bits;
/*  876 */           return 2;
/*      */         }
/*  878 */         strm.adler = (state.check = adler32(0L, null, 0, 0));
/*  879 */         state.mode = 11;
/*      */       case 11:
/*  881 */         if (flush == 5)
/*      */           break label3779;
/*      */       case 12:
/*  883 */         if (state.last)
/*      */         {
/*  886 */           hold >>= (bits & 0x7);
/*  887 */           bits -= (bits & 0x7);
/*  888 */           state.mode = 25;
/*      */         }
/*      */ 
/*  892 */         while (bits < 3)
/*      */         {
/*  894 */           if (have == 0) break label3779;
/*  895 */           --have;
/*  896 */           hold += ((input[(next++)] & 0xFF) << bits);
/*  897 */           bits += 8;
/*      */         }
/*      */ 
/*  900 */         BITS = (int)(hold & 1L);
/*  901 */         state.last = (BITS != 0);
/*      */ 
/*  903 */         hold >>= 1;
/*  904 */         --bits;
/*      */ 
/*  906 */         BITS = (int)(hold & 0x3);
/*  907 */         switch (BITS)
/*      */         {
/*      */         case 0:
/*  910 */           state.mode = 13;
/*  911 */           break;
/*      */         case 1:
/*  913 */           if (FIXED_TABLE_CODES == null)
/*      */           {
/*      */             try
/*      */             {
/*  917 */               makefixed(state);
/*      */             }
/*      */             catch (zlibException e)
/*      */             {
/*  921 */               strm.msg = e.toString();
/*  922 */               state.mode = 27;
/*  923 */               break label962:
/*      */             }
/*      */           }
/*  926 */           System.arraycopy(FIXED_TABLE_CODES, 0, state.codes, 0, FIXED_TABLE_CODES.length);
/*  927 */           state.index_lencode = 0;
/*  928 */           state.lenbits = 9;
/*  929 */           state.index_distcode = FIXED_TABLE_FIRST_DCODE;
/*  930 */           state.distbits = 5;
/*  931 */           state.mode = 18;
/*  932 */           break;
/*      */         case 2:
/*  934 */           state.mode = 15;
/*  935 */           break;
/*      */         case 3:
/*  937 */           strm.msg = "invalid block type";
/*  938 */           state.mode = 27;
/*      */         }
/*      */ 
/*  941 */         hold >>= 2;
/*  942 */         bits -= 2;
/*  943 */         break;
/*      */       case 13:
/*  946 */         hold >>= (bits & 0x7);
/*  947 */         bits -= (bits & 0x7);
/*      */ 
/*  949 */         while (bits < 32)
/*      */         {
/*  951 */           if (have == 0) break label3779;
/*  952 */           --have;
/*  953 */           hold += ((input[(next++)] & 0xFF) << bits);
/*  954 */           bits += 8;
/*      */         }
/*      */ 
/*  957 */         if ((hold & 0xFFFF) != ((hold >> 16 ^ 0xFFFF) & 0xFFFF))
/*      */         {
/*  959 */           strm.msg = "invalid stored block lengths";
/*  960 */           state.mode = 27;
/*      */         }
/*      */ 
/*  963 */         state.length = ((int)hold & 0xFFFF);
/*      */ 
/*  965 */         hold = 0L;
/*  966 */         bits = 0;
/*  967 */         state.mode = 14;
/*      */       case 14:
/*  969 */         copy = state.length;
/*  970 */         if (copy != 0)
/*      */         {
/*  972 */           if (copy > have) copy = have;
/*  973 */           if (copy > left) copy = left;
/*  974 */           if (copy == 0) break label3779;
/*  975 */           System.arraycopy(input, next, output, put, copy);
/*  976 */           have -= copy;
/*  977 */           next += copy;
/*  978 */           left -= copy;
/*  979 */           put += copy;
/*  980 */           state.length -= copy;
/*      */         }
/*      */ 
/*  983 */         state.mode = 11;
/*  984 */         break;
/*      */       case 15:
/*  987 */         while (bits < 14)
/*      */         {
/*  989 */           if (have == 0) break label3779;
/*  990 */           --have;
/*  991 */           hold += ((input[(next++)] & 0xFF) << bits);
/*  992 */           bits += 8;
/*      */         }
/*      */ 
/*  995 */         BITS = (int)(hold & 0x1F);
/*  996 */         state.nlen = (BITS + 257);
/*      */ 
/*  998 */         hold >>= 5;
/*  999 */         bits -= 5;
/*      */ 
/* 1001 */         BITS = (int)(hold & 0x1F);
/* 1002 */         state.ndist = (BITS + 1);
/*      */ 
/* 1004 */         hold >>= 5;
/* 1005 */         bits -= 5;
/*      */ 
/* 1007 */         BITS = (int)(hold & 0xF);
/* 1008 */         state.ncode = (BITS + 4);
/*      */ 
/* 1010 */         hold >>= 4;
/* 1011 */         bits -= 4;
/* 1012 */         if ((state.nlen > 286) || (state.ndist > 30))
/*      */         {
/* 1014 */           strm.msg = "too many length or distance symbols";
/* 1015 */           state.mode = 27;
/*      */         }
/*      */ 
/* 1018 */         state.have = 0;
/* 1019 */         state.mode = 16;
/*      */       case 16:
/* 1021 */         while (state.have < state.ncode)
/*      */         {
/* 1024 */           while (bits < 3)
/*      */           {
/* 1026 */             if (have == 0) break label3779;
/* 1027 */             --have;
/* 1028 */             hold += ((input[(next++)] & 0xFF) << bits);
/* 1029 */             bits += 8;
/*      */           }
/*      */ 
/* 1032 */           BITS = (int)(hold & 0x7);
/* 1033 */           state.lens[INFLATE_ORDER[(state.have++)]] = (short)BITS;
/*      */ 
/* 1035 */           hold >>= 3;
/* 1036 */           bits -= 3;
/*      */         }
/* 1038 */         while (state.have < 19)
/*      */         {
/* 1040 */           state.lens[INFLATE_ORDER[(state.have++)]] = 0;
/*      */         }
/* 1042 */         state.index_codes = 0;
/* 1043 */         state.index_lencode = 0;
/*      */         try
/*      */         {
/* 1046 */           state.lenbits = inflate_table(0, 0, 19, 7, state);
/*      */         }
/*      */         catch (zlibException e)
/*      */         {
/* 1050 */           strm.msg = ("invalid code lengths set: " + e.toString());
/* 1051 */           state.mode = 27; } continue;
/*      */ 
/* 1054 */         state.have = 0;
/* 1055 */         state.mode = 17;
/*      */       case 17:
/*      */         int BITS;
/*      */         int this_bits;
/*      */         int this_val;
/*      */         while (true) { if (state.have >= state.nlen + state.ndist)
/*      */             break label2188;
/*      */           int code;
/*      */           while (true)
/*      */           {
/* 1062 */             BITS = (int)(hold & (1 << state.lenbits) - 1);
/* 1063 */             code = state.codes[(state.index_lencode + BITS)];
/*      */ 
/* 1065 */             this_bits = code >> 16 & 0xFF;
/* 1066 */             if (this_bits <= bits)
/*      */               break;
/* 1068 */             if (have == 0) break label3779;
/* 1069 */             --have;
/* 1070 */             hold += ((input[(next++)] & 0xFF) << bits);
/* 1071 */             bits += 8;
/*      */           }
/*      */ 
/* 1074 */           this_val = code & 0xFFFF;
/* 1075 */           if (this_val >= 16) {
/*      */             break;
/*      */           }
/* 1078 */           while (bits < this_bits)
/*      */           {
/* 1080 */             if (have == 0) break label3779;
/* 1081 */             --have;
/* 1082 */             hold += ((input[(next++)] & 0xFF) << bits);
/* 1083 */             bits += 8;
/*      */           }
/*      */ 
/* 1086 */           hold >>= this_bits;
/* 1087 */           bits -= this_bits;
/* 1088 */           state.lens[(state.have++)] = (short)this_val; }
/*      */ 
/*      */         int len;
/*      */         int copy;
/* 1092 */         if (this_val == 16)
/*      */         {
/* 1095 */           while (bits < this_bits + 2)
/*      */           {
/* 1097 */             if (have == 0) break label3779;
/* 1098 */             --have;
/* 1099 */             hold += ((input[(next++)] & 0xFF) << bits);
/* 1100 */             bits += 8;
/*      */           }
/*      */ 
/* 1103 */           hold >>= this_bits;
/* 1104 */           bits -= this_bits;
/* 1105 */           if (state.have == 0)
/*      */           {
/* 1107 */             strm.msg = "invalid bit length repeat";
/* 1108 */             state.mode = 27;
/* 1109 */             break label2188:
/*      */           }
/* 1111 */           len = state.lens[(state.have - 1)];
/*      */ 
/* 1113 */           BITS = (int)(hold & 0x3);
/* 1114 */           copy = 3 + BITS;
/*      */ 
/* 1116 */           hold >>= 2;
/* 1117 */           bits -= 2;
/*      */         }
/* 1119 */         else if (this_val == 17)
/*      */         {
/* 1122 */           while (bits < this_bits + 3)
/*      */           {
/* 1124 */             if (have == 0) break label3779;
/* 1125 */             --have;
/* 1126 */             hold += ((input[(next++)] & 0xFF) << bits);
/* 1127 */             bits += 8;
/*      */           }
/*      */ 
/* 1130 */           hold >>= this_bits;
/* 1131 */           bits -= this_bits;
/* 1132 */           int len = 0;
/*      */ 
/* 1134 */           BITS = (int)(hold & 0x7);
/* 1135 */           int copy = 3 + BITS;
/*      */ 
/* 1137 */           hold >>= 3;
/* 1138 */           bits -= 3;
/*      */         }
/*      */         else
/*      */         {
/* 1143 */           while (bits < this_bits + 7)
/*      */           {
/* 1145 */             if (have == 0) break label3779;
/* 1146 */             --have;
/* 1147 */             hold += ((input[(next++)] & 0xFF) << bits);
/* 1148 */             bits += 8;
/*      */           }
/*      */ 
/* 1151 */           hold >>= this_bits;
/* 1152 */           bits -= this_bits;
/* 1153 */           len = 0;
/*      */ 
/* 1155 */           BITS = (int)(hold & 0x7F);
/* 1156 */           copy = 11 + BITS;
/*      */ 
/* 1158 */           hold >>= 7;
/* 1159 */           bits -= 7;
/*      */         }
/* 1161 */         if (state.have + copy > state.nlen + state.ndist)
/*      */         {
/* 1163 */           strm.msg = "invalid bit length repeat";
/* 1164 */           state.mode = 27;
/*      */         } else {
/*      */           while (true) {
/* 1167 */             if (copy-- != 0);
/* 1169 */             state.lens[(state.have++)] = (short)len;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1174 */         if (state.mode == 27)
/*      */           continue;
/* 1176 */         state.index_codes = 0;
/* 1177 */         state.index_lencode = 0;
/*      */         try
/*      */         {
/* 1180 */           state.lenbits = inflate_table(1, 0, state.nlen, 9, state);
/*      */         }
/*      */         catch (zlibException e)
/*      */         {
/* 1184 */           strm.msg = ("invalid literal/lengths set: " + e.toString());
/* 1185 */           state.mode = 27;
/* 1186 */         }continue;
/*      */ 
/* 1188 */         state.index_distcode = state.index_codes;
/*      */         try
/*      */         {
/* 1191 */           state.distbits = inflate_table(2, state.nlen, state.ndist, 6, state);
/*      */         }
/*      */         catch (zlibException e)
/*      */         {
/* 1195 */           strm.msg = ("invalid distances set: " + e.toString());
/* 1196 */           state.mode = 27;
/*      */         }
/*      */ 
/* 1197 */         continue;
/*      */ 
/* 1199 */         state.mode = 18;
/*      */       case 18:
/*      */         while (true)
/*      */         {
/* 1205 */           BITS = (int)(hold & (1 << state.lenbits) - 1);
/* 1206 */           code = state.codes[(state.index_lencode + BITS)];
/*      */ 
/* 1208 */           this_bits = code >> 16 & 0xFF;
/* 1209 */           if (this_bits <= bits)
/*      */             break;
/* 1211 */           if (have == 0) break label3779;
/* 1212 */           --have;
/* 1213 */           hold += ((input[(next++)] & 0xFF) << bits);
/* 1214 */           bits += 8;
/*      */         }
/* 1216 */         this_op = code >> 24 & 0xFF;
/* 1217 */         this_val = code & 0xFFFF;
/* 1218 */         if ((this_op != 0) && ((this_op & 0xF0) == 0))
/*      */         {
/* 1220 */           int last_op = this_op;
/* 1221 */           int last_bits = this_bits;
/* 1222 */           int last_val = this_val;
/*      */           while (true)
/*      */           {
/* 1226 */             BITS = (int)(hold & (1 << last_bits + last_op) - 1);
/* 1227 */             code = state.codes[(state.index_lencode + last_val + (BITS >> last_bits))];
/*      */ 
/* 1229 */             this_bits = code >> 16 & 0xFF;
/* 1230 */             if (last_bits + this_bits <= bits)
/*      */               break;
/* 1232 */             if (have == 0) break label3779;
/* 1233 */             --have;
/* 1234 */             hold += ((input[(next++)] & 0xFF) << bits);
/* 1235 */             bits += 8;
/*      */           }
/* 1237 */           this_op = code >> 24 & 0xFF;
/* 1238 */           this_val = code & 0xFFFF;
/*      */ 
/* 1240 */           hold >>= last_bits;
/* 1241 */           bits -= last_bits;
/*      */         }
/*      */ 
/* 1244 */         hold >>= this_bits;
/* 1245 */         bits -= this_bits;
/* 1246 */         state.length = this_val;
/* 1247 */         if (this_op == 0)
/*      */         {
/* 1249 */           state.mode = 23;
/*      */         }
/*      */ 
/* 1252 */         if ((this_op & 0x20) != 0)
/*      */         {
/* 1254 */           state.mode = 11;
/*      */         }
/*      */ 
/* 1257 */         if ((this_op & 0x40) != 0)
/*      */         {
/* 1259 */           strm.msg = "invalid literal/length code";
/* 1260 */           state.mode = 27;
/*      */         }
/*      */ 
/* 1263 */         state.extra = (this_op & 0xF);
/* 1264 */         state.mode = 19;
/*      */       case 19:
/* 1266 */         if (state.extra != 0)
/*      */         {
/* 1269 */           while (bits < state.extra)
/*      */           {
/* 1271 */             if (have == 0) break label3779;
/* 1272 */             --have;
/* 1273 */             hold += ((input[(next++)] & 0xFF) << bits);
/* 1274 */             bits += 8;
/*      */           }
/*      */ 
/* 1277 */           BITS = (int)(hold & (1 << state.extra) - 1);
/* 1278 */           state.length += BITS;
/*      */ 
/* 1280 */           hold >>= state.extra;
/* 1281 */           bits -= state.extra;
/*      */         }
/*      */ 
/* 1283 */         state.mode = 20;
/*      */       case 20:
/*      */         while (true)
/*      */         {
/* 1288 */           BITS = (int)(hold & (1 << state.distbits) - 1);
/* 1289 */           code = state.codes[(state.index_distcode + BITS)];
/*      */ 
/* 1291 */           this_bits = code >> 16 & 0xFF;
/* 1292 */           if (this_bits <= bits)
/*      */             break;
/* 1294 */           if (have == 0) break label3779;
/* 1295 */           --have;
/* 1296 */           hold += ((input[(next++)] & 0xFF) << bits);
/* 1297 */           bits += 8;
/*      */         }
/* 1299 */         this_op = code >> 24 & 0xFF;
/* 1300 */         this_val = code & 0xFFFF;
/* 1301 */         if ((this_op & 0xF0) == 0)
/*      */         {
/* 1303 */           int last_op = code >> 24 & 0xFF;
/* 1304 */           int last_bits = code >> 16 & 0xFF;
/* 1305 */           int last_val = code & 0xFFFF;
/*      */           while (true)
/*      */           {
/* 1309 */             BITS = (int)(hold & (1 << last_bits + last_op) - 1);
/* 1310 */             code = state.codes[(state.index_distcode + last_val + (BITS >> last_bits))];
/*      */ 
/* 1312 */             this_bits = code >> 16 & 0xFF;
/* 1313 */             if (last_bits + this_bits <= bits)
/*      */               break;
/* 1315 */             if (have == 0) break label3779;
/* 1316 */             --have;
/* 1317 */             hold += ((input[(next++)] & 0xFF) << bits);
/* 1318 */             bits += 8;
/*      */           }
/* 1320 */           this_op = code >> 24 & 0xFF;
/* 1321 */           this_val = code & 0xFFFF;
/*      */ 
/* 1323 */           hold >>= last_bits;
/* 1324 */           bits -= last_bits;
/*      */         }
/*      */ 
/* 1327 */         hold >>= this_bits;
/* 1328 */         bits -= this_bits;
/* 1329 */         if ((this_op & 0x40) != 0)
/*      */         {
/* 1331 */           strm.msg = "invalid distance code";
/* 1332 */           state.mode = 27;
/*      */         }
/*      */ 
/* 1335 */         state.offset = this_val;
/* 1336 */         state.extra = (this_op & 0xF);
/* 1337 */         state.mode = 21;
/*      */       case 21:
/* 1339 */         if (state.extra != 0)
/*      */         {
/* 1342 */           while (bits < state.extra)
/*      */           {
/* 1344 */             if (have == 0) break label3779;
/* 1345 */             --have;
/* 1346 */             hold += ((input[(next++)] & 0xFF) << bits);
/* 1347 */             bits += 8;
/*      */           }
/*      */ 
/* 1350 */           BITS = (int)(hold & (1 << state.extra) - 1);
/* 1351 */           state.offset += BITS;
/*      */ 
/* 1353 */           hold >>= state.extra;
/* 1354 */           bits -= state.extra;
/*      */         }
/* 1356 */         if (state.offset > state.dmax)
/*      */         {
/* 1358 */           strm.msg = "invalid distance too far back (strict)";
/* 1359 */           state.mode = 27;
/*      */         }
/*      */ 
/* 1362 */         if (state.offset > state.whave + out - left)
/*      */         {
/* 1364 */           strm.msg = "invalid distance too far back";
/* 1365 */           state.mode = 27;
/*      */         }
/*      */ 
/* 1368 */         state.mode = 22;
/*      */       case 22:
/* 1370 */         if (left == 0) break label3779;
/* 1371 */         copy = out - left;
/*      */         byte[] from;
/*      */         int index_from;
/* 1372 */         if (state.offset > copy)
/*      */         {
/* 1374 */           copy = state.offset - copy;
/*      */           int index_from;
/*      */           int index_from;
/* 1375 */           if (copy > state.write)
/*      */           {
/* 1377 */             copy -= state.write;
/* 1378 */             index_from = state.wsize - copy;
/*      */           }
/*      */           else
/*      */           {
/* 1382 */             index_from = state.write - copy;
/*      */           }
/* 1384 */           byte[] from = state.window;
/* 1385 */           if (copy > state.length) copy = state.length;
/*      */         }
/*      */         else
/*      */         {
/* 1389 */           from = output;
/* 1390 */           index_from = put - state.offset;
/* 1391 */           copy = state.length;
/*      */         }
/* 1393 */         if (copy > left) copy = left;
/* 1394 */         left -= copy;
/* 1395 */         state.length -= copy;
/*      */         do
/*      */         {
/* 1398 */           output[(put++)] = from[(index_from++)];
/* 1399 */         }while (--copy != 0);
/* 1400 */         if (state.length != 0) continue; state.mode = 18; break;
/*      */       case 23:
/* 1403 */         if (left == 0) break label3779;
/* 1404 */         output[(put++)] = (byte)state.length;
/* 1405 */         --left;
/* 1406 */         state.mode = 18;
/* 1407 */         break;
/*      */       case 25:
/* 1409 */         if (!state.wrap) {
/*      */           break label3756;
/*      */         }
/* 1412 */         while (bits < 32)
/*      */         {
/* 1414 */           if (have == 0) break label3779;
/* 1415 */           --have;
/* 1416 */           hold += ((input[(next++)] & 0xFF) << bits);
/* 1417 */           bits += 8;
/*      */         }
/* 1419 */         out -= left;
/* 1420 */         strm.total_out += out;
/* 1421 */         state.total += out;
/* 1422 */         if (out != 0)
/*      */         {
/* 1424 */           strm.adler = (state.check = adler32(state.check, output, put - out, out));
/*      */         }
/* 1426 */         out = left;
/*      */ 
/* 1428 */         long REVERSE = (hold >> 24 & 0xFF) + (hold >> 8 & 0xFF00) + ((hold & 0xFF00) << 8) + ((hold & 0xFF) << 24);
/*      */ 
/* 1430 */         if (REVERSE == state.check) break; strm.msg = "incorrect data check";
/* 1433 */         state.mode = 27;
/*      */       case 26:
/*      */       case 27:
/*      */       case 1:
/*      */       case 2:
/*      */       case 3:
/*      */       case 4:
/*      */       case 5:
/*      */       case 6:
/*      */       case 7:
/*      */       case 8:
/*      */       case 24: }  } hold = 0L;
/* 1438 */     bits = 0;
/*      */ 
/* 1440 */     label3756: state.mode = 26;
/*      */ 
/* 1442 */     ret = 1;
/* 1443 */     break label3779:
/*      */ 
/* 1445 */     ret = -3;
/* 1446 */     break label3779:
/*      */ 
/* 1448 */     return -2;
/*      */ 
/* 1453 */     label3779: strm.index_out = put;
/* 1454 */     strm.avail_out = left;
/* 1455 */     strm.index_in = next;
/* 1456 */     strm.avail_in = have;
/* 1457 */     state.hold = hold;
/* 1458 */     state.bits = bits;
/* 1459 */     if ((state.wsize != 0) || ((state.mode < 25) && (out != strm.avail_out)))
/*      */     {
/* 1462 */       if (state.window == null)
/*      */       {
/* 1464 */         state.window = new byte[1 << state.wbits];
/*      */       }
/*      */ 
/* 1467 */       if (state.wsize == 0)
/*      */       {
/* 1469 */         state.wsize = (1 << state.wbits);
/* 1470 */         state.write = 0;
/* 1471 */         state.whave = 0;
/*      */       }
/*      */ 
/* 1474 */       copy = out - strm.avail_out;
/* 1475 */       if (copy >= state.wsize)
/*      */       {
/* 1477 */         System.arraycopy(strm.output, strm.index_out - state.wsize, state.window, 0, state.wsize);
/* 1478 */         state.write = 0;
/* 1479 */         state.whave = state.wsize;
/*      */       }
/*      */       else
/*      */       {
/* 1483 */         int dist = state.wsize - state.write;
/* 1484 */         if (dist > copy) dist = copy;
/* 1485 */         System.arraycopy(strm.output, strm.index_out - copy, state.window, state.write, dist);
/* 1486 */         copy -= dist;
/* 1487 */         if (copy != 0)
/*      */         {
/* 1489 */           System.arraycopy(strm.output, strm.index_out - copy, state.window, 0, copy);
/* 1490 */           state.write = copy;
/* 1491 */           state.whave = state.wsize;
/*      */         }
/*      */         else
/*      */         {
/* 1495 */           state.write += dist;
/* 1496 */           if (state.write == state.wsize) state.write = 0;
/* 1497 */           if (state.whave < state.wsize) state.whave += dist;
/*      */         }
/*      */       }
/*      */     }
/* 1501 */     in -= strm.avail_in;
/* 1502 */     out -= strm.avail_out;
/* 1503 */     strm.total_in += in;
/* 1504 */     strm.total_out += out;
/* 1505 */     state.total += out;
/* 1506 */     if ((state.wrap) && (out != 0))
/*      */     {
/* 1508 */       strm.adler = (state.check = adler32(state.check, strm.output, strm.index_out - out, out));
/*      */     }
/* 1510 */     if (((in == 0) && (out == 0)) || ((flush == 4) && (ret == 0)))
/*      */     {
/* 1512 */       ret = -4;
/*      */     }
/* 1514 */     return ret;
/*      */   }
/*      */ 
/*      */   public static int inflateEnd(z_stream strm)
/*      */   {
/* 1519 */     if ((strm == null) || (strm.state == null))
/*      */     {
/* 1521 */       return -2;
/*      */     }
/* 1523 */     inflate_state state = (inflate_state)strm.state;
/* 1524 */     state.window = null;
/* 1525 */     strm.state = null;
/* 1526 */     return 0;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1533 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89193 $";
/*      */   }
/*      */ 
/*      */   static class inflate_state
/*      */     implements zlib.internal_state
/*      */   {
/*      */     int mode;
/*      */     boolean last;
/*      */     boolean wrap;
/*      */     boolean havedict;
/*      */     static final int flags = 0;
/*      */     int dmax;
/*      */     long check;
/*      */     long total;
/*      */     int wbits;
/*      */     int wsize;
/*      */     int whave;
/*      */     int write;
/*      */     byte[] window;
/*      */     long hold;
/*      */     int bits;
/*      */     int length;
/*      */     int offset;
/*      */     int extra;
/*      */     int index_lencode;
/*      */     int index_distcode;
/*      */     int lenbits;
/*      */     int distbits;
/*      */     int ncode;
/*      */     int nlen;
/*      */     int ndist;
/*      */     int have;
/*      */     short[] count;
/*      */     short[] offs;
/*      */     short[] lens;
/*      */     short[] work;
/*      */     int[] codes;
/*      */     int index_codes;
/*      */ 
/*      */     inflate_state()
/*      */     {
/*  321 */       this.count = new short[16];
/*  322 */       this.offs = new short[16];
/*      */ 
/*  324 */       this.lens = new short[320];
/*  325 */       this.work = new short[288];
/*  326 */       this.codes = new int[2048];
/*      */     }
/*      */   }
/*      */ 
/*      */   public static class z_stream
/*      */   {
/*      */     public byte[] input;
/*      */     public int index_in;
/*      */     public int avail_in;
/*      */     public long total_in;
/*      */     public byte[] output;
/*      */     public int index_out;
/*      */     public int avail_out;
/*      */     public long total_out;
/*      */     public String msg;
/*      */     zlib.internal_state state;
/*      */     long adler;
/*      */   }
/*      */ 
/*      */   static abstract interface internal_state
/*      */   {
/*      */   }
/*      */ 
/*      */   public static class zlibException extends Exception
/*      */   {
/*      */     public zlibException(String message)
/*      */     {
/*   31 */       super(message);
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.zlib
 * JD-Core Version:    0.5.4
 */