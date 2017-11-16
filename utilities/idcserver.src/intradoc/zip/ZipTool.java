/*     */ package intradoc.zip;
/*     */ 
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class ZipTool
/*     */   implements GenericTracingCallback
/*     */ {
/*  34 */   static final String[][] COMMAND_USAGE = { { "h", "help", null, "Show these usage messages." }, { "a", "add", "append", "replace", null, "Add or replace entries in a zipfile with those specified.  ", "Requires one or more paths or the --from option." }, { "", "compact", null, "Compact zipfile (remove unnecessary holes)." }, { "c", "create", null, "Create zipfile and (optionally) replace existing entries with ", "those specified (requires one or more pathnames or the --from ", "option).  This is the same as the --add command." }, { "", "del", "delete", "rm", "remove", null, "Remove the specified entries from this zipfile (requires ", "paths or the --from option)." }, { "x", "extract", null, "Extract entries from this zipfile.  If no pathnames are ", "specified, extract all entries." }, { "t", "list", null, "List the specified entries in this zipfile.  If no paths are ", "specified, list all entries." }, { "", "mv", "move", "rename", null, "Move specified entries in the zipfile.  Requires additional ", "pathnames:  If two paths are specified and the --from option ", "is not used, rename the entry specified by the first path to ", "the value specified by the second path.  In all other cases, ", "at least one pathname must be specified and it must be a ", "directory.  Additional paths may be specified but the final ", "path must be a directory.  If the --from option is used, the ", "entries specified in that file are also moved, in addition to ", "the remaining paths.  This behavior is similar to the typical ", "unix or shell \"rm\" command." }, { "u", "update", null, "Update the selected entries.  This command is similar to ", "--replace but only affects entries whose modified date is ", "newer than the entries' dates in the zipfile." }, { "", "summary", null, "Show a brief summary of the zipfile.  This is the default ", "command if no other is specified." }, { "V", "version", null, "Display ZipTool version." } };
/*     */ 
/*  99 */   static final String[][] OPTION_USAGE = { { "", "comment=<string>", "commentfrom=<file>", null, "Specify zipfile or entry comment.  For a zipfile comment, ", "specify this option before <zipfile>." }, { "", "exclude=<glob>", null, "List of files/entries to exclude.  <glob> is processed using ", "\"extended\" regex.  Multiple <glob>s can be specified with ", "subsequent --exclude options.  A pathname matching any <glob> ", "will be excluded.  The inclusion list is always specified by ", "paths subsequent to a command or by using the --from option." }, { "F", "from=<file>", null, "List of entries to include, taken from a file.  When this ", "option is used, it is generally not necessary to specify ", "pathnames after the command.  Like other options, this option ", "applies to subsequent commands, so please use with care.", "If \"-\" is specified as <file>, read list of files from ", "standard input." }, { "#", "level=#   (where # is from 0 to 9)", null, "Set compression level (9 = maximum, slowest; 1 = minimum, ", "fastest; 0 = store, no compression).  Default is 9." }, { "n", "norecurse", "norecursion", "no-recurse", "no-recursion", null, "Normally ZipTool will recursively visit all files in a given ", "directory.  Specifying this option prevents that operation; ", "however, the directory entries themselves are still visited." }, { "s", "sort", null, "Sort the entries lexically, by directory.  Note this is not ", "the same as sorting alphabetically: All files and directories ", "are sorted in their parent directory, so all entries in a ", "given subdirectory are grouped together." }, { "v", "verbose", null, "Enable verbose output." }, null, { "", "debug", null } };
/*     */ 
/* 141 */   static final Object[] USAGE_HELP = { "Usage: ziptool [<opts>] <zipfile> [<opts> <cmd> [<paths>]...]", "Commands include:", COMMAND_USAGE, "Options include:", OPTION_USAGE, "", "For additional help, try:  ziptool --verbose --help" };
/*     */ 
/* 149 */   static final Object[] USAGE_HELP_VERBOSE = { "Usage: ziptool [<options>] <zipfile> [<options> <command> [<paths>]...]\n", "Where <command> is one or more of:\n", COMMAND_USAGE, "Most commands allow zero or more of the following options:\n", OPTION_USAGE, "Options apply to all subsequent commands but not to previous commands. ", "When using the long form of options that take parameters, use either of:\n", "\t--longoption parameter\n", "\t--longoption=parameter\n", "although this help will describe the second form.  ", "For options that take parameters, use \"\" or \"--default\" to reset the option ", "to its default value; otherwise the options will either stack (e.g. exclude) ", "or will completely replace previous value (e.g. comment).\n", "\n", "Short (single character) options can be lumped together; those that take ", "parameters require additional arguments, for example:\n", "\tziptool my.zip -Fsv <file>\n", "\n", "If <zipfile> \"-\", use standard output." };
/*     */   Params m_params;
/*     */   ZipToolOperations m_ops;
/*     */   char[][] m_args;
/*     */   int m_argn;
/*     */   boolean m_didCommand;
/*     */   boolean m_isEscaped;
/*     */   static final int LEFT_INDENT_MARGIN = 17;
/*     */   static char[] m_leftMarginChars;
/* 208 */   int m_lineWidth = 78;
/*     */ 
/*     */   ZipTool(String[] args)
/*     */   {
/* 213 */     IdcZipFunctions.initZipEnvironment();
/* 214 */     this.m_args = new char[args.length][];
/* 215 */     for (int i = 0; i < args.length; ++i)
/*     */     {
/* 217 */       this.m_args[i] = args[i].toCharArray();
/*     */     }
/*     */   }
/*     */ 
/*     */   void applyCommand(String commandName, boolean forReal)
/*     */     throws Exception
/*     */   {
/* 230 */     List includes = new ArrayList();
/* 231 */     int numIncludes = 0;
/* 232 */     while (this.m_argn < this.m_args.length)
/*     */     {
/* 234 */       if ((!this.m_isEscaped) && (this.m_args[this.m_argn][0] == '-'))
/*     */       {
/* 236 */         if ((this.m_args[this.m_argn].length != 2) || (this.m_args[this.m_argn][1] != '-'))
/*     */           break;
/* 238 */         this.m_isEscaped = true;
/* 239 */         this.m_argn += 1;
/*     */       }
/*     */ 
/* 244 */       includes.add(new String(this.m_args[(this.m_argn++)]));
/* 245 */       ++numIncludes;
/*     */     }
/* 247 */     if ((this.m_params.m_isDebug) && (!forReal))
/*     */     {
/* 249 */       StringBuilder str = new StringBuilder("\ncommand: ");
/* 250 */       str.append(commandName);
/* 251 */       str.append("\nzipfile: ");
/* 252 */       str.append(this.m_params.m_zipfileName);
/* 253 */       str.append("\nlevel: ");
/* 254 */       if (this.m_params.m_compressionLevel < 0)
/*     */       {
/* 256 */         str.append("default");
/*     */       }
/*     */       else
/*     */       {
/* 260 */         str.append(this.m_params.m_compressionLevel);
/*     */       }
/* 262 */       str.append((this.m_params.m_isVerbose) ? "  verbose" : "  NOT verbose");
/* 263 */       str.append((this.m_params.m_isSorted) ? "  sorted" : "  NOT sorted");
/* 264 */       str.append((this.m_params.m_noRecursion) ? "  NO recursion" : "  recursion");
/* 265 */       str.append("\nfrom: ");
/* 266 */       str.append(this.m_params.m_fromfileName);
/* 267 */       str.append("\ncomment: ");
/* 268 */       str.append(this.m_params.m_comment);
/* 269 */       str.append("\nexcludes:\n");
/* 270 */       int num = this.m_params.m_excludes.size();
/* 271 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 273 */         str.append('\t');
/* 274 */         str.append(this.m_params.m_excludes.get(i));
/* 275 */         str.append('\n');
/*     */       }
/* 277 */       System.err.println(str);
/*     */     }
/* 279 */     if ((forReal) && (null == this.m_ops))
/*     */     {
/* 281 */       this.m_ops = new ZipToolOperations(this.m_params);
/*     */     }
/* 283 */     if (commandName.equals("help"))
/*     */     {
/* 285 */       usage(null);
/*     */     }
/* 287 */     else if (commandName.equals("add"))
/*     */     {
/* 289 */       if (this.m_params.m_doesZipfileUseOutput)
/*     */       {
/* 291 */         usage("--add cannot operate on zipfile '-', try --create");
/*     */       }
/* 293 */       if ((numIncludes < 1) && (null == this.m_params.m_fromfileName))
/*     */       {
/* 295 */         usage("--add requires arguments");
/*     */       }
/* 297 */       if (forReal)
/*     */       {
/* 299 */         this.m_ops.prepareFileList(includes);
/* 300 */         this.m_ops.createOrReplace(false);
/*     */       }
/*     */     }
/* 303 */     else if (commandName.equals("compact"))
/*     */     {
/* 305 */       if (this.m_params.m_doesZipfileUseOutput)
/*     */       {
/* 307 */         usage("--compat cannot operate on zipfile '-'");
/*     */       }
/* 309 */       if (numIncludes > 0)
/*     */       {
/* 311 */         usage("--compact does not take arguments");
/*     */       }
/* 313 */       if (forReal)
/*     */       {
/* 315 */         this.m_ops.compact();
/*     */       }
/*     */     }
/* 318 */     else if (commandName.equals("create"))
/*     */     {
/* 320 */       if (forReal)
/*     */       {
/* 322 */         this.m_ops.prepareFileList(includes);
/* 323 */         this.m_ops.createOrReplace(false);
/*     */       }
/*     */     }
/* 326 */     else if (commandName.equals("del"))
/*     */     {
/* 328 */       if (this.m_params.m_doesZipfileUseOutput)
/*     */       {
/* 330 */         usage("--remove cannot operate on zipfile '-'");
/*     */       }
/* 332 */       if ((numIncludes < 1) && (null == this.m_params.m_fromfileName))
/*     */       {
/* 334 */         usage("--remove requires arguments");
/*     */       }
/* 336 */       if (forReal)
/*     */       {
/* 338 */         this.m_ops.prepareFileList(includes);
/* 339 */         this.m_ops.remove();
/*     */       }
/*     */     }
/* 342 */     else if (commandName.equals("extract"))
/*     */     {
/* 344 */       if (this.m_params.m_doesZipfileUseOutput)
/*     */       {
/* 346 */         usage("--extract cannot operate on zipfile '-'");
/*     */       }
/* 348 */       if (forReal)
/*     */       {
/* 350 */         this.m_ops.prepareFileList(includes);
/* 351 */         this.m_ops.extract();
/*     */       }
/*     */     }
/* 354 */     else if (commandName.equals("list"))
/*     */     {
/* 356 */       if (this.m_params.m_doesZipfileUseOutput)
/*     */       {
/* 358 */         usage("--list cannot operate on zipfile '-'");
/*     */       }
/* 360 */       if (forReal)
/*     */       {
/* 362 */         this.m_ops.prepareFileList(includes);
/* 363 */         this.m_ops.list();
/*     */       }
/*     */     }
/* 366 */     else if (commandName.equals("mv"))
/*     */     {
/* 368 */       if (this.m_params.m_doesZipfileUseOutput)
/*     */       {
/* 370 */         usage("--move cannot operate on zipfile '-'");
/*     */       }
/* 372 */       if ((numIncludes < 1) || ((numIncludes < 2) && (null == this.m_params.m_fromfileName)))
/*     */       {
/* 374 */         usage("--move requires at least two arguments");
/*     */       }
/* 376 */       if (forReal)
/*     */       {
/* 378 */         this.m_ops.prepareFileList(includes);
/* 379 */         this.m_ops.move();
/*     */       }
/*     */     }
/* 382 */     else if (commandName.equals("update"))
/*     */     {
/* 384 */       if ((numIncludes < 1) && (null == this.m_params.m_fromfileName))
/*     */       {
/* 386 */         usage("--update requires arguments");
/*     */       }
/* 388 */       if (forReal)
/*     */       {
/* 390 */         this.m_ops.prepareFileList(includes);
/* 391 */         this.m_ops.createOrReplace(true);
/*     */       }
/*     */     }
/* 394 */     else if (commandName.equals("summary"))
/*     */     {
/* 396 */       if (this.m_params.m_doesZipfileUseOutput)
/*     */       {
/* 398 */         usage("--summary cannot operate on zipfile '-'");
/*     */       }
/* 400 */       if (numIncludes > 0)
/*     */       {
/* 402 */         usage("--summary does not take arguments");
/*     */       }
/* 404 */       if (forReal)
/*     */       {
/* 406 */         this.m_ops.summary();
/*     */       }
/*     */     }
/* 409 */     else if (commandName.equals("version"))
/*     */     {
/* 411 */       if (numIncludes > 0)
/*     */       {
/* 413 */         usage("--version does not take arguments");
/*     */       }
/* 415 */       if (forReal)
/*     */       {
/* 417 */         System.err.println(this.m_ops.getVersionString());
/*     */       }
/*     */     }
/* 420 */     this.m_didCommand = true;
/*     */   }
/*     */ 
/*     */   void applyOption(String optionName, int argOffset)
/*     */     throws Exception
/*     */   {
/* 432 */     String optionValue = null;
/* 433 */     int equals = optionName.indexOf(61);
/* 434 */     if (equals >= 0)
/*     */     {
/* 436 */       optionName = optionName.substring(0, equals);
/* 437 */       if (this.m_argn >= this.m_args.length)
/*     */       {
/* 439 */         usage(new StringBuilder().append("missing parameter for \"").append(optionName).append("\" option").toString());
/*     */       }
/* 441 */       char[] arg = this.m_args[(this.m_argn++)];
/* 442 */       optionValue = new String(arg, argOffset, arg.length - argOffset);
/* 443 */       if (!this.m_isEscaped)
/*     */       {
/* 445 */         if (optionValue.equals("--default"))
/*     */         {
/* 447 */           optionValue = "";
/*     */         }
/* 449 */         if (argOffset == 0)
/*     */         {
/* 451 */           if (optionValue.equals("--"))
/*     */           {
/* 453 */             this.m_isEscaped = true;
/* 454 */             applyOption(optionName, 0);
/* 455 */             return;
/*     */           }
/* 457 */           if (optionValue.startsWith("-"))
/*     */           {
/* 459 */             usage(new StringBuilder().append("missing parameter for \"").append(optionName).append("\" option").toString());
/*     */           }
/*     */         }
/*     */       }
/* 463 */       if (optionValue.equals(""))
/*     */       {
/* 465 */         optionValue = null;
/*     */       }
/*     */     }
/* 468 */     if (optionName.equals("comment"))
/*     */     {
/* 470 */       this.m_params.m_comment = optionValue;
/*     */     }
/* 472 */     else if (optionName.equals("exclude"))
/*     */     {
/* 474 */       if (null == optionValue)
/*     */       {
/* 476 */         this.m_params.m_excludes = new ArrayList();
/*     */       }
/*     */       else
/*     */       {
/* 480 */         this.m_params.m_excludes.add(optionValue);
/*     */       }
/*     */     }
/* 483 */     else if (optionName.equals("from"))
/*     */     {
/* 485 */       this.m_params.m_fromfileName = optionValue;
/* 486 */       if (!optionValue.equals("-"))
/*     */         return;
/* 488 */       this.m_params.m_fromfileName = "";
/* 489 */       if (!this.m_params.m_doesFromUseInput)
/*     */         return;
/* 491 */       usage("standard input already in use");
/* 492 */       this.m_params.m_doesFromUseInput = true;
/*     */     }
/* 496 */     else if (optionName.equals("level"))
/*     */     {
/* 498 */       if (null == optionValue)
/*     */       {
/* 500 */         this.m_params.m_compressionLevel = -1;
/*     */       }
/*     */       else
/*     */       {
/*     */         try
/*     */         {
/* 506 */           int val = Integer.parseInt(optionValue);
/* 507 */           if ((val < 0) || (val > 9))
/*     */           {
/* 509 */             usage("value out of range for compression level");
/*     */           }
/* 511 */           this.m_params.m_compressionLevel = val;
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 515 */           usage("inappropriate value for compression level");
/*     */         }
/*     */       }
/*     */     }
/* 519 */     else if (optionName.equals("norecurse"))
/*     */     {
/* 521 */       this.m_params.m_noRecursion = true;
/*     */     }
/* 523 */     else if (optionName.equals("sort"))
/*     */     {
/* 525 */       this.m_params.m_isSorted = true;
/*     */     }
/* 527 */     else if (optionName.equals("verbose"))
/*     */     {
/* 529 */       this.m_params.m_isVerbose = true;
/*     */     } else {
/* 531 */       if (!optionName.equals("debug"))
/*     */         return;
/* 533 */       this.m_params.m_isDebug = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   void parse(boolean forReal)
/*     */     throws Exception
/*     */   {
/* 546 */     this.m_params = new Params();
/* 547 */     this.m_argn = 0;
/* 548 */     this.m_didCommand = false;
/* 549 */     this.m_isEscaped = false;
/*     */     try
/*     */     {
/* 552 */       while (this.m_argn < this.m_args.length)
/*     */       {
/* 554 */         char[] arg = this.m_args[this.m_argn];
/* 555 */         if (arg.length < 1)
/*     */         {
/* 557 */           usage("empty argument detected");
/*     */         }
/* 559 */         if ((arg[0] != '-') || (arg.length == 1))
/*     */         {
/* 561 */           if (null != this.m_params.m_zipfileName)
/*     */           {
/* 563 */             usage("must specify a command before specifying additional pathnames");
/*     */           }
/* 565 */           if (arg[0] == '-')
/*     */           {
/* 567 */             this.m_params.m_doesZipfileUseOutput = true;
/*     */           }
/* 569 */           this.m_params.m_zipfileName = arg;
/* 570 */           this.m_argn += 1;
/* 571 */           if (this.m_params.m_zipfileName.length >= 1)
/*     */             continue;
/* 573 */           usage("must specify a zipfile or \"-\".");
/*     */         }
/*     */ 
/* 577 */         if (this.m_args[this.m_argn][1] != '-')
/*     */         {
/* 580 */           int argn = this.m_argn++;
/* 581 */           for (int i = 1; i < this.m_args[argn].length; ++i)
/*     */           {
/* 583 */             char c = this.m_args[argn][i];
/* 584 */             if ((c >= '0') && (c <= '9'))
/*     */             {
/* 586 */               this.m_params.m_compressionLevel = (c - '0');
/*     */             }
/*     */             else {
/* 589 */               int index = findShortUsage(OPTION_USAGE, c);
/* 590 */               if (index >= 0)
/*     */               {
/* 592 */                 applyOption(OPTION_USAGE[index][1], 0);
/*     */               }
/*     */               else {
/* 595 */                 index = findShortUsage(COMMAND_USAGE, c);
/* 596 */                 if (index < 0)
/*     */                 {
/* 598 */                   usage(new StringBuilder().append("unknown short option '").append(c).append("'").toString());
/*     */                 }
/* 600 */                 applyCommand(COMMAND_USAGE[index][1], forReal);
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/* 605 */         int equals = 1;
/*     */         do if (++equals >= arg.length)
/*     */             break;
/* 608 */         while (arg[equals] != '=');
/*     */ 
/* 613 */         int index = findLongUsage(OPTION_USAGE, arg, 2, equals - 2);
/* 614 */         if (index >= 0)
/*     */         {
/* 616 */           if (equals < arg.length)
/*     */           {
/* 618 */             applyOption(OPTION_USAGE[index][1], equals + 1);
/*     */           }
/*     */ 
/* 622 */           this.m_argn += 1;
/* 623 */           applyOption(OPTION_USAGE[index][1], 0);
/*     */         }
/*     */ 
/* 627 */         index = findLongUsage(COMMAND_USAGE, arg, 2, equals - 2);
/* 628 */         if (index < 0)
/*     */         {
/* 630 */           usage(new StringBuilder().append("unknown long option \"").append(new String(arg, 2, equals - 2)).append("\"").toString());
/*     */         }
/* 632 */         if (equals < arg.length)
/*     */         {
/* 634 */           usage("commands don't take parameters");
/*     */         }
/* 636 */         this.m_argn += 1;
/* 637 */         applyCommand(COMMAND_USAGE[index][1], forReal);
/*     */       }
/* 639 */       if (!this.m_didCommand)
/*     */       {
/* 641 */         applyCommand("summary", forReal);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 647 */       throw e;
/*     */     }
/*     */     finally
/*     */     {
/* 651 */       if ((forReal) && (null != this.m_ops))
/*     */       {
/* 653 */         this.m_ops.finish();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   int findShortUsage(String[][] use, char arg)
/*     */   {
/* 669 */     for (int i = 0; i < use.length; ++i)
/*     */     {
/* 671 */       if (null == use[i]) {
/*     */         continue;
/*     */       }
/*     */ 
/* 675 */       if (use[i][0].length() < 1) {
/*     */         continue;
/*     */       }
/*     */ 
/* 679 */       char shortUse = use[i][0].charAt(0);
/* 680 */       if (shortUse == '#') {
/*     */         continue;
/*     */       }
/*     */ 
/* 684 */       if (shortUse == arg)
/*     */       {
/* 686 */         return i;
/*     */       }
/*     */     }
/* 689 */     return -1;
/*     */   }
/*     */ 
/*     */   int findLongUsage(String[][] use, char[] arg, int offset, int length)
/*     */   {
/* 703 */     for (int i = 0; i < use.length; ++i)
/*     */     {
/* 705 */       if (null == use[i]) {
/*     */         continue;
/*     */       }
/*     */ 
/* 709 */       for (int j = 1; null != use[i][j]; ++j)
/*     */       {
/* 711 */         char[] longUse = use[i][j].toCharArray();
/* 712 */         int len = longUse.length;
/* 713 */         for (int equals = 0; equals < len; ++equals)
/*     */         {
/* 715 */           if (longUse[equals] != '=')
/*     */             continue;
/* 717 */           len = equals;
/* 718 */           break;
/*     */         }
/*     */ 
/* 721 */         if (len != length) {
/*     */           continue;
/*     */         }
/*     */ 
/* 725 */         int index = length;
/*     */         do if (index-- <= 0)
/*     */             break;
/* 728 */         while (longUse[index] == arg[(offset + index)]);
/*     */ 
/* 733 */         if (index < 0)
/*     */         {
/* 735 */           return i;
/*     */         }
/*     */       }
/*     */     }
/* 739 */     return -1;
/*     */   }
/*     */ 
/*     */   int appendWithMargins(StringBuilder builder, String str, int column, int leftMargin, int rightMargin)
/*     */   {
/* 756 */     int len = str.length();
/* 757 */     while (len > 0)
/*     */     {
/* 759 */       if (column < leftMargin)
/*     */       {
/* 761 */         builder.append(m_leftMarginChars, 0, leftMargin - column);
/* 762 */         column = leftMargin;
/*     */       }
/* 764 */       int newline = str.indexOf(10);
/* 765 */       if ((newline >= 0) && 
/* 767 */         (column + newline < rightMargin))
/*     */       {
/* 769 */         builder.append(str.substring(0, newline + 1));
/* 770 */         str = str.substring(newline + 1);
/* 771 */         len -= newline + 1;
/* 772 */         column = 0;
/*     */       }
/*     */ 
/* 776 */       if (column + len < rightMargin)
/*     */       {
/* 778 */         builder.append(str);
/* 779 */         column += len;
/* 780 */         len = 0;
/*     */       }
/*     */ 
/* 783 */       int spaceLast = str.lastIndexOf(32, rightMargin - column);
/* 784 */       if (spaceLast < 0)
/*     */       {
/* 786 */         builder.append('\n');
/* 787 */         column = 0;
/*     */       }
/*     */ 
/* 790 */       int spaceFirst = spaceLast;
/*     */       do if (--spaceFirst < 0)
/*     */           break;
/* 793 */       while (' ' == str.charAt(spaceFirst));
/*     */ 
/* 798 */       if (++spaceFirst < 0)
/*     */       {
/* 800 */         spaceFirst = 0;
/*     */       }
/*     */       else
/*     */       {
/* 804 */         builder.append(str.substring(0, spaceFirst));
/*     */       }
/* 806 */       str = str.substring(spaceLast + 1);
/* 807 */       len -= spaceLast + 1;
/* 808 */       builder.append('\n');
/* 809 */       column = 0;
/*     */     }
/* 811 */     return column;
/*     */   }
/*     */ 
/*     */   void appendUsage(StringBuilder builder, String[] use)
/*     */   {
/* 822 */     int index = 0;
/* 823 */     String str = use[(index++)];
/* 824 */     int len = str.length();
/*     */     int column;
/* 826 */     if (len > 0)
/*     */     {
/* 828 */       builder.append(" -");
/* 829 */       builder.append(str);
/* 830 */       builder.append(' ');
/* 831 */       column = 3 + len;
/*     */     }
/*     */     else
/*     */     {
/* 835 */       builder.append("    ");
/* 836 */       column = 4;
/*     */     }
/*     */ 
/* 839 */     while (null != (str = use[(index++)]))
/*     */     {
/* 841 */       builder.append("--");
/* 842 */       builder.append(str);
/* 843 */       builder.append(' ');
/* 844 */       column += 3 + str.length();
/*     */     }
/* 846 */     if (!this.m_params.m_isVerbose)
/*     */     {
/* 848 */       return;
/*     */     }
/* 850 */     if (column > 14)
/*     */     {
/* 852 */       builder.append('\n');
/* 853 */       column = 0;
/*     */     }
/* 855 */     builder.append(m_leftMarginChars, 0, 17 - column);
/* 856 */     int column = 17;
/* 857 */     while (index < use.length)
/*     */     {
/* 859 */       str = use[(index++)];
/* 860 */       column = appendWithMargins(builder, str, column, 17, this.m_lineWidth);
/*     */     }
/* 862 */     builder.append('\n');
/*     */   }
/*     */ 
/*     */   public void usage(String msg)
/*     */     throws Exception
/*     */   {
/* 873 */     if (null == m_leftMarginChars)
/*     */     {
/* 875 */       m_leftMarginChars = new char[17];
/* 876 */       for (int i = 0; i < 17; ++i)
/*     */       {
/* 878 */         m_leftMarginChars[i] = ' ';
/*     */       }
/*     */     }
/*     */ 
/* 882 */     StringBuilder builder = new StringBuilder((null == msg) ? "" : msg);
/* 883 */     if (null != msg)
/*     */     {
/* 885 */       builder.append("\n\n");
/*     */     }
/* 887 */     Object[] help = (this.m_params.m_isVerbose) ? USAGE_HELP_VERBOSE : USAGE_HELP;
/* 888 */     int column = 0;
/* 889 */     for (int i = 0; i < help.length; ++i)
/*     */     {
/* 891 */       Object o = help[i];
/* 892 */       if (o instanceof String)
/*     */       {
/* 894 */         column = appendWithMargins(builder, (String)o, column, 0, this.m_lineWidth);
/*     */       }
/* 896 */       if (!o instanceof String[][])
/*     */         continue;
/* 898 */       builder.append('\n');
/* 899 */       String[][] uses = (String[][])(String[][])o;
/* 900 */       for (int j = 0; j < uses.length; ++j)
/*     */       {
/* 902 */         if (null == uses[j]) {
/*     */           break;
/*     */         }
/*     */ 
/* 906 */         appendUsage(builder, uses[j]);
/* 907 */         builder.append('\n');
/*     */       }
/* 909 */       column = 0;
/*     */     }
/*     */ 
/* 912 */     System.err.println(builder);
/* 913 */     System.exit(1);
/* 914 */     throw new Exception("Unable to exit");
/*     */   }
/*     */ 
/*     */   public void report(int level, Object[] args)
/*     */   {
/* 926 */     StringBuffer str = new StringBuffer("ZipTool: ");
/* 927 */     str.append(LEVEL_NAMES[level]);
/* 928 */     str.append(": ");
/* 929 */     for (Object arg : args)
/*     */     {
/* 931 */       if (arg instanceof Throwable)
/*     */       {
/* 933 */         System.err.println();
/* 934 */         Throwable t = (Throwable)arg;
/* 935 */         str.append(t.getMessage());
/* 936 */         if (this.m_params.m_isDebug)
/*     */         {
/* 938 */           System.err.println(str.toString());
/* 939 */           str.setLength(0);
/* 940 */           t.printStackTrace(System.err);
/*     */         }
/*     */       }
/* 943 */       else if (arg instanceof String)
/*     */       {
/* 945 */         str.append((String)arg);
/*     */       }
/*     */       else
/*     */       {
/* 949 */         str.append(arg.toString());
/*     */       }
/*     */     }
/* 952 */     System.err.println(str.toString());
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */     throws Exception
/*     */   {
/* 965 */     ZipTool tool = new ZipTool(args);
/* 966 */     tool.parse(false);
/* 967 */     tool.parse(true);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 973 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ 
/*     */   class Params
/*     */   {
/*     */     char[] m_zipfileName;
/*     */     boolean m_isVerbose;
/*     */     boolean m_isDebug;
/*     */     boolean m_isSorted;
/*     */     boolean m_noRecursion;
/*     */     int m_compressionLevel;
/*     */     List m_includes;
/*     */     List m_excludes;
/*     */     String m_fromfileName;
/*     */     String m_comment;
/*     */     boolean m_doesFromUseInput;
/*     */     boolean m_doesZipfileUseOutput;
/*     */ 
/*     */     Params()
/*     */     {
/* 189 */       this.m_compressionLevel = -1;
/* 190 */       this.m_excludes = new ArrayList();
/* 191 */       this.m_comment = "";
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.zip.ZipTool
 * JD-Core Version:    0.5.4
 */