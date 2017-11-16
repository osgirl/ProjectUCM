/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class ClassFileV2 extends ClassFile
/*     */ {
/*     */   protected void checkVersion()
/*     */     throws IOException
/*     */   {
/* 315 */     int major = this.major_version & 0xFFFF;
/* 316 */     if ((major >= 45) && (major <= 48))
/*     */       return;
/* 318 */     String msg = "classfile version out of range [45.0, 48.0]: " + major + "." + (this.minor_version & 0xFFFF);
/* 319 */     throw new IOException(msg);
/*     */   }
/*     */ 
/*     */   protected ClassFile.cp_info loadConstantPoolItem(DataInputStream dis, byte tag)
/*     */     throws IOException
/*     */   {
/*     */     short nameIndex;
/* 327 */     switch (tag)
/*     */     {
/*     */     case 7:
/* 330 */       nameIndex = dis.readShort();
/* 331 */       return new CONSTANT_Class_info(nameIndex);
/*     */     case 9:
/*     */     case 10:
/*     */     case 11:
/* 335 */       short classIndex = dis.readShort();
/* 336 */       short nameAndTypeIndex = dis.readShort();
/* 337 */       switch (tag)
/*     */       {
/*     */       case 9:
/* 340 */         return new CONSTANT_Fieldref_info(classIndex, nameAndTypeIndex);
/*     */       case 10:
/* 342 */         return new CONSTANT_Methodref_info(classIndex, nameAndTypeIndex);
/*     */       case 11:
/* 344 */         return new CONSTANT_InterfaceMethodref_info(classIndex, nameAndTypeIndex);
/*     */       }case 8:
/* 347 */       short stringIndex = dis.readShort();
/* 348 */       return new CONSTANT_String_info(stringIndex);
/*     */     case 3:
/* 350 */       int intValue = dis.readInt();
/* 351 */       return new CONSTANT_Integer_info(intValue);
/*     */     case 4:
/* 353 */       float floatValue = dis.readFloat();
/* 354 */       return new CONSTANT_Float_info(floatValue);
/*     */     case 5:
/* 356 */       long longValue = dis.readLong();
/* 357 */       return new CONSTANT_Long_info(longValue);
/*     */     case 6:
/* 359 */       double doubleValue = dis.readDouble();
/* 360 */       return new CONSTANT_Double_info(doubleValue);
/*     */     case 12:
/* 362 */       nameIndex = dis.readShort();
/* 363 */       short descriptorIndex = dis.readShort();
/* 364 */       return new CONSTANT_NameAndType_info(nameIndex, descriptorIndex);
/*     */     case 1:
/* 366 */       int numBytes = dis.readShort() & 0xFFFF;
/* 367 */       byte[] bytes = new byte[numBytes];
/* 368 */       dis.read(bytes);
/* 369 */       return new CONSTANT_Utf8_info(bytes);
/*     */     case 2:
/*     */     }
/* 371 */     throw new IOException("unknown constant pool tag " + tag);
/*     */   }
/*     */ 
/*     */   protected ClassFile.attribute_info loadAttribute(DataInputStream dis, short attrNameIndex)
/*     */     throws IOException
/*     */   {
/* 377 */     ClassFile.cp_info[] pool = this.constant_pool;
/* 378 */     int numConstants = pool.length;
/* 379 */     if ((attrNameIndex < 1) || (attrNameIndex >= numConstants))
/*     */     {
/* 381 */       throw new IOException("bad attribute index: " + attrNameIndex);
/*     */     }
/* 383 */     ClassFile.cp_info constant = pool[attrNameIndex];
/* 384 */     if (!constant instanceof CONSTANT_Utf8_info)
/*     */     {
/* 386 */       throw new IOException("bad attribute index, not a UTF8 constant: " + attrNameIndex);
/*     */     }
/* 388 */     CONSTANT_Utf8_info attrNameInfo = (CONSTANT_Utf8_info)constant;
/* 389 */     String attrName = attrNameInfo.m_string;
/* 390 */     int attrLength = dis.readInt();
/* 391 */     if (attrLength < 0)
/*     */     {
/* 393 */       throw new IOException("attribute length out of range for attribute " + attrName);
/*     */     }
/* 395 */     return loadAttribute(dis, attrNameIndex, attrName, attrLength);
/*     */   }
/*     */ 
/*     */   protected ClassFile.attribute_info loadAttribute(DataInputStream dis, short attrNameIndex, String attrName, int attrLength) throws IOException
/*     */   {
/* 400 */     if (attrName.equals("ConstantValue"))
/*     */     {
/* 402 */       if (attrLength != 2)
/*     */       {
/* 404 */         throw new IOException("bad attribute length for ConstantValue: " + attrLength);
/*     */       }
/* 406 */       ConstantValue_attribute attr = new ConstantValue_attribute();
/* 407 */       attr.attribute_name_index = attrNameIndex;
/* 408 */       attr.constantvalue_index = dis.readShort();
/* 409 */       return attr;
/*     */     }
/* 411 */     if (attrName.equals("Code"))
/*     */     {
/* 413 */       if (attrLength < 12)
/*     */       {
/* 415 */         throw new IOException("bad attribute length for Code: " + attrLength);
/*     */       }
/* 417 */       Code_attribute attr = new Code_attribute();
/* 418 */       attr.attribute_name_index = attrNameIndex;
/* 419 */       attr.max_stack = dis.readShort();
/* 420 */       attr.max_locals = dis.readShort();
/* 421 */       int codeLength = dis.readInt();
/* 422 */       if (codeLength < 0)
/*     */       {
/* 424 */         throw new IOException("bad code length: " + codeLength);
/*     */       }
/* 426 */       if (attrLength < 12 + codeLength)
/*     */       {
/* 428 */         throw new IOException("bad code length " + codeLength + " for attribute length " + attrLength);
/*     */       }
/* 430 */       byte[] code = attr.code = new byte[codeLength];
/* 431 */       dis.read(code);
/* 432 */       int numExceptions = dis.readShort() & 0xFFFF;
/* 433 */       if (attrLength < 12 + codeLength + 8 * numExceptions)
/*     */       {
/* 435 */         String msg = "bad exceptions table length " + numExceptions + " for attribute length " + attrLength;
/* 436 */         throw new IOException(msg);
/*     */       }
/* 438 */       ClassFileV2.Code_attribute.ExceptionHandler[] handlers = attr.exception_table = new ClassFileV2.Code_attribute.ExceptionHandler[numExceptions];
/*     */ 
/* 440 */       for (int e = 0; e < numExceptions; ++e)
/*     */       {
/*     */         Code_attribute tmp355_353 = attr; tmp355_353.getClass(); ClassFileV2.Code_attribute.ExceptionHandler handler = handlers[e] =  = new ClassFileV2.Code_attribute.ExceptionHandler(tmp355_353);
/* 443 */         handler.start_pc = dis.readShort();
/* 444 */         handler.end_pc = dis.readShort();
/* 445 */         handler.handler_pc = dis.readShort();
/* 446 */         handler.catch_type = dis.readShort();
/*     */       }
/* 448 */       attr.attributes = loadAttributes(dis);
/* 449 */       int actualLength = getAttributeLength(attr);
/* 450 */       if (actualLength != attrLength)
/*     */       {
/* 452 */         throw new IOException("attribute length mismatch: " + actualLength + " (expected " + attrLength + ")");
/*     */       }
/* 454 */       return attr;
/*     */     }
/* 456 */     if (attrName.equals("Exceptions"))
/*     */     {
/* 458 */       if (attrLength < 2)
/*     */       {
/* 460 */         throw new IOException("bad attribute length for Exceptions: " + attrLength);
/*     */       }
/* 462 */       Exceptions_attribute attr = new Exceptions_attribute();
/* 463 */       attr.attribute_name_index = attrNameIndex;
/* 464 */       int numExceptions = dis.readShort() & 0xFFFF;
/* 465 */       if (attrLength != 2 + 2 * numExceptions)
/*     */       {
/* 467 */         String msg = "bad attribute length for Exceptions: " + attrLength + " (expected " + (2 + 2 * numExceptions) + ")";
/*     */ 
/* 469 */         throw new IOException(msg);
/*     */       }
/* 471 */       short[] exceptions = attr.exception_index_table = new short[numExceptions];
/* 472 */       for (int e = 0; e < numExceptions; ++e)
/*     */       {
/* 474 */         exceptions[e] = dis.readShort();
/*     */       }
/* 476 */       return attr;
/*     */     }
/* 478 */     if (attrName.equals("InnerClasses"))
/*     */     {
/* 480 */       if (attrLength < 2)
/*     */       {
/* 482 */         throw new IOException("bad attribute length for InnerClasses: " + attrLength);
/*     */       }
/* 484 */       InnerClasses_attribute attr = new InnerClasses_attribute();
/* 485 */       attr.attribute_name_index = attrNameIndex;
/* 486 */       int numClasses = dis.readShort() & 0xFFFF;
/* 487 */       if (attrLength != 2 + 8 * numClasses)
/*     */       {
/* 489 */         String msg = "bad attribute length for InnerClasses: " + attrLength + " (expected " + (2 + 8 * numClasses) + ")";
/*     */ 
/* 491 */         throw new IOException(msg);
/*     */       }
/* 493 */       ClassFileV2.InnerClasses_attribute.InnerClass[] classes = attr.classes = new ClassFileV2.InnerClasses_attribute.InnerClass[numClasses];
/*     */ 
/* 495 */       for (int c = 0; c < numClasses; ++c)
/*     */       {
/*     */         InnerClasses_attribute tmp815_813 = attr; tmp815_813.getClass(); ClassFileV2.InnerClasses_attribute.InnerClass innerClass = classes[c] =  = new ClassFileV2.InnerClasses_attribute.InnerClass(tmp815_813);
/* 498 */         innerClass.inner_class_info_index = dis.readShort();
/* 499 */         innerClass.outer_class_info_index = dis.readShort();
/* 500 */         innerClass.inner_name_index = dis.readShort();
/* 501 */         innerClass.inner_class_access_flags = dis.readShort();
/*     */       }
/* 503 */       return attr;
/*     */     }
/* 505 */     if (attrName.equals("Synthetic"))
/*     */     {
/* 507 */       if (attrLength != 0)
/*     */       {
/* 509 */         throw new IOException("bad attribute length for Synthetic: " + attrLength);
/*     */       }
/* 511 */       Synthetic_attribute attr = new Synthetic_attribute();
/* 512 */       attr.attribute_name_index = attrNameIndex;
/* 513 */       return attr;
/*     */     }
/* 515 */     if (attrName.equals("SourceFile"))
/*     */     {
/* 517 */       if (attrLength != 2)
/*     */       {
/* 519 */         throw new IOException("bad attribute length for SourceFile: " + attrLength);
/*     */       }
/* 521 */       SourceFile_attribute attr = new SourceFile_attribute();
/* 522 */       attr.attribute_name_index = attrNameIndex;
/* 523 */       attr.sourcefile_index = dis.readShort();
/* 524 */       return attr;
/*     */     }
/* 526 */     if (attrName.equals("LineNumberTable"))
/*     */     {
/* 528 */       LineNumberTable_attribute attr = new LineNumberTable_attribute();
/* 529 */       attr.attribute_name_index = attrNameIndex;
/* 530 */       int numRows = dis.readShort() & 0xFFFF;
/* 531 */       if (attrLength != 2 + 4 * numRows)
/*     */       {
/* 533 */         String msg = "bad attribute length for LineNumberTable: " + attrLength + " (expected " + (2 + 4 * numRows) + ")";
/*     */ 
/* 535 */         throw new IOException(msg);
/*     */       }
/* 537 */       ClassFileV2.LineNumberTable_attribute.Line[] lines = attr.line_number_table = new ClassFileV2.LineNumberTable_attribute.Line[numRows];
/*     */ 
/* 539 */       for (int n = 0; n < numRows; ++n)
/*     */       {
/*     */         LineNumberTable_attribute tmp1133_1131 = attr; tmp1133_1131.getClass(); ClassFileV2.LineNumberTable_attribute.Line line = lines[n] =  = new ClassFileV2.LineNumberTable_attribute.Line(tmp1133_1131);
/* 542 */         line.start_pc = dis.readShort();
/* 543 */         line.line_number = dis.readShort();
/*     */       }
/* 545 */       return attr;
/*     */     }
/* 547 */     if (attrName.equals("LocalVariableTable"))
/*     */     {
/* 549 */       LocalVariableTable_attribute attr = new LocalVariableTable_attribute();
/* 550 */       attr.attribute_name_index = attrNameIndex;
/* 551 */       int numVars = dis.readShort() & 0xFFFF;
/* 552 */       if (attrLength != 2 + 10 * numVars)
/*     */       {
/* 554 */         String msg = "bad attribute length for LocalVariableTable: " + attrLength + " (expected " + (2 + 10 * numVars) + ")";
/*     */ 
/* 556 */         throw new IOException(msg);
/*     */       }
/* 558 */       ClassFileV2.LocalVariableTable_attribute.Variable[] vars = attr.local_variable_table = new ClassFileV2.LocalVariableTable_attribute.Variable[numVars];
/*     */ 
/* 560 */       for (int v = 0; v < numVars; ++v)
/*     */       {
/*     */         LocalVariableTable_attribute tmp1303_1301 = attr; tmp1303_1301.getClass(); ClassFileV2.LocalVariableTable_attribute.Variable var = vars[v] =  = new ClassFileV2.LocalVariableTable_attribute.Variable(tmp1303_1301);
/* 563 */         var.start_pc = dis.readShort();
/* 564 */         var.length = dis.readShort();
/* 565 */         var.name_index = dis.readShort();
/* 566 */         var.descriptor_index = dis.readShort();
/* 567 */         var.index = dis.readShort();
/*     */       }
/* 569 */       return attr;
/*     */     }
/* 571 */     if (attrName.equals("Deprecated"))
/*     */     {
/* 573 */       if (attrLength != 0)
/*     */       {
/* 575 */         throw new IOException("bad attribute length for Deprecated: " + attrLength);
/*     */       }
/* 577 */       Deprecated_attribute attr = new Deprecated_attribute();
/* 578 */       attr.attribute_name_index = attrNameIndex;
/* 579 */       return attr;
/*     */     }
/*     */ 
/* 583 */     ClassFile.attribute_info attr = new ClassFile.attribute_info(this);
/* 584 */     attr.attribute_name_index = attrNameIndex;
/* 585 */     byte[] bytes = attr.info = new byte[attrLength];
/* 586 */     dis.read(bytes);
/* 587 */     return attr;
/*     */   }
/*     */ 
/*     */   protected void saveConstantPoolItem(DataOutputStream dos, ClassFile.cp_info item)
/*     */     throws IOException
/*     */   {
/* 593 */     byte tag = item.tag;
/* 594 */     switch (tag)
/*     */     {
/*     */     case 7:
/* 597 */       CONSTANT_Class_info classInfo = (CONSTANT_Class_info)item;
/* 598 */       dos.writeShort(classInfo.name_index);
/* 599 */       return;
/*     */     case 9:
/*     */     case 10:
/*     */     case 11:
/* 603 */       RefInfo refInfo = (RefInfo)item;
/* 604 */       dos.writeShort(refInfo.class_index);
/* 605 */       dos.writeShort(refInfo.name_and_type_index);
/* 606 */       return;
/*     */     case 8:
/* 608 */       CONSTANT_String_info stringInfo = (CONSTANT_String_info)item;
/* 609 */       dos.writeShort(stringInfo.string_index);
/* 610 */       return;
/*     */     case 3:
/* 612 */       CONSTANT_Integer_info integerInfo = (CONSTANT_Integer_info)item;
/* 613 */       dos.writeInt(integerInfo.m_value);
/* 614 */       return;
/*     */     case 4:
/* 616 */       CONSTANT_Float_info floatInfo = (CONSTANT_Float_info)item;
/* 617 */       dos.writeInt(Float.floatToRawIntBits(floatInfo.m_value));
/* 618 */       return;
/*     */     case 5:
/* 620 */       CONSTANT_Long_info longInfo = (CONSTANT_Long_info)item;
/* 621 */       dos.writeLong(longInfo.m_value);
/* 622 */       return;
/*     */     case 6:
/* 624 */       CONSTANT_Double_info doubleInfo = (CONSTANT_Double_info)item;
/*     */ 
/* 626 */       dos.writeLong(Double.doubleToRawLongBits(doubleInfo.m_value));
/* 627 */       return;
/*     */     case 12:
/* 629 */       CONSTANT_NameAndType_info nameAndTypeInfo = (CONSTANT_NameAndType_info)item;
/* 630 */       dos.writeShort(nameAndTypeInfo.name_index);
/* 631 */       dos.writeShort(nameAndTypeInfo.descriptor_index);
/* 632 */       return;
/*     */     case 1:
/* 634 */       CONSTANT_Utf8_info utf8Info = (CONSTANT_Utf8_info)item;
/* 635 */       byte[] bytesUTF8 = utf8Info.bytes;
/* 636 */       int numBytes = bytesUTF8.length;
/* 637 */       if (numBytes > 65535)
/*     */       {
/* 639 */         throw new IOException("UTF8 bytes too long");
/* 641 */       }dos.writeShort(numBytes);
/* 642 */       dos.write(bytesUTF8);
/* 643 */       return;
/*     */     case 2:
/*     */     }
/* 645 */     throw new IOException("unknown constant pool tag " + tag);
/*     */   }
/*     */ 
/*     */   protected void saveAttribute(DataOutputStream dos, ClassFile.attribute_info attribute) throws IOException
/*     */   {
/* 650 */     if (attribute instanceof ConstantValue_attribute)
/*     */     {
/* 652 */       ConstantValue_attribute attr = (ConstantValue_attribute)attribute;
/* 653 */       dos.writeInt(2);
/* 654 */       dos.writeShort(attr.constantvalue_index);
/*     */     }
/* 656 */     else if (attribute instanceof Code_attribute)
/*     */     {
/* 658 */       Code_attribute attr = (Code_attribute)attribute;
/* 659 */       int attrLength = getAttributeLength(attribute);
/* 660 */       dos.writeInt(attrLength);
/* 661 */       dos.writeShort(attr.max_stack);
/* 662 */       dos.writeShort(attr.max_locals);
/* 663 */       byte[] code = attr.code;
/* 664 */       dos.writeInt(code.length);
/* 665 */       dos.write(code);
/* 666 */       ClassFileV2.Code_attribute.ExceptionHandler[] handlers = attr.exception_table;
/* 667 */       int numExceptions = handlers.length;
/* 668 */       if (numExceptions > 65535)
/*     */       {
/* 670 */         throw new IOException("too many exceptions");
/*     */       }
/* 672 */       dos.writeShort(numExceptions);
/* 673 */       for (int e = 0; e < numExceptions; ++e)
/*     */       {
/* 675 */         ClassFileV2.Code_attribute.ExceptionHandler handler = handlers[e];
/* 676 */         dos.writeShort(handler.start_pc);
/* 677 */         dos.writeShort(handler.end_pc);
/* 678 */         dos.writeShort(handler.handler_pc);
/* 679 */         dos.writeShort(handler.catch_type);
/*     */       }
/* 681 */       ClassFile.attribute_info[] attrs = attr.attributes;
/* 682 */       int numAttributes = attrs.length;
/* 683 */       if (numAttributes > 65535)
/*     */       {
/* 685 */         throw new IOException("too many Code attributes");
/*     */       }
/* 687 */       saveAttributes(dos, attrs);
/*     */     }
/* 689 */     else if (attribute instanceof Exceptions_attribute)
/*     */     {
/* 691 */       Exceptions_attribute attr = (Exceptions_attribute)attribute;
/* 692 */       short[] exceptions = attr.exception_index_table;
/* 693 */       int numExceptions = exceptions.length;
/* 694 */       if (numExceptions > 65535)
/*     */       {
/* 696 */         throw new IOException("too many exceptions");
/*     */       }
/* 698 */       dos.writeInt(2 + 2 * numExceptions);
/* 699 */       dos.writeShort(numExceptions);
/* 700 */       for (int e = 0; e < numExceptions; ++e)
/*     */       {
/* 702 */         dos.writeShort(exceptions[e]);
/*     */       }
/*     */     }
/* 705 */     else if (attribute instanceof InnerClasses_attribute)
/*     */     {
/* 707 */       InnerClasses_attribute attr = (InnerClasses_attribute)attribute;
/* 708 */       ClassFileV2.InnerClasses_attribute.InnerClass[] classes = attr.classes;
/* 709 */       int numClasses = classes.length;
/* 710 */       if (numClasses > 65535)
/*     */       {
/* 712 */         throw new IOException("too many inner classes");
/*     */       }
/* 714 */       dos.writeInt(2 + 8 * numClasses);
/* 715 */       dos.writeShort(numClasses);
/* 716 */       for (int c = 0; c < numClasses; ++c)
/*     */       {
/* 718 */         ClassFileV2.InnerClasses_attribute.InnerClass innerClass = classes[c];
/* 719 */         dos.writeShort(innerClass.inner_class_info_index);
/* 720 */         dos.writeShort(innerClass.outer_class_info_index);
/* 721 */         dos.writeShort(innerClass.inner_name_index);
/* 722 */         dos.writeShort(innerClass.inner_class_access_flags);
/*     */       }
/*     */     }
/* 725 */     else if (attribute instanceof Synthetic_attribute)
/*     */     {
/* 727 */       dos.writeInt(0);
/*     */     }
/* 729 */     else if (attribute instanceof SourceFile_attribute)
/*     */     {
/* 731 */       SourceFile_attribute attr = (SourceFile_attribute)attribute;
/* 732 */       dos.writeInt(2);
/* 733 */       dos.writeShort(attr.sourcefile_index);
/*     */     }
/* 735 */     else if (attribute instanceof LineNumberTable_attribute)
/*     */     {
/* 737 */       LineNumberTable_attribute attr = (LineNumberTable_attribute)attribute;
/* 738 */       ClassFileV2.LineNumberTable_attribute.Line[] lines = attr.line_number_table;
/* 739 */       int numRows = lines.length;
/* 740 */       if (numRows > 65535)
/*     */       {
/* 742 */         throw new IOException("line number table too long; table should be split into multiple attributes");
/*     */       }
/* 744 */       dos.writeInt(2 + 4 * numRows);
/* 745 */       dos.writeShort(numRows);
/* 746 */       for (int n = 0; n < numRows; ++n)
/*     */       {
/* 748 */         ClassFileV2.LineNumberTable_attribute.Line line = lines[n];
/* 749 */         dos.writeShort(line.start_pc);
/* 750 */         dos.writeShort(line.line_number);
/*     */       }
/*     */     }
/* 753 */     else if (attribute instanceof LocalVariableTable_attribute)
/*     */     {
/* 755 */       LocalVariableTable_attribute attr = (LocalVariableTable_attribute)attribute;
/* 756 */       ClassFileV2.LocalVariableTable_attribute.Variable[] vars = attr.local_variable_table;
/* 757 */       int numVars = vars.length;
/* 758 */       if (numVars > 65535)
/*     */       {
/* 760 */         throw new IOException("too many local variables");
/*     */       }
/* 762 */       dos.writeInt(2 + 10 * numVars);
/* 763 */       dos.writeShort(numVars);
/* 764 */       for (int v = 0; v < numVars; ++v)
/*     */       {
/* 766 */         ClassFileV2.LocalVariableTable_attribute.Variable var = vars[v];
/* 767 */         dos.writeShort(var.start_pc);
/* 768 */         dos.writeShort(var.length);
/* 769 */         dos.writeShort(var.name_index);
/* 770 */         dos.writeShort(var.descriptor_index);
/* 771 */         dos.writeShort(var.index);
/*     */       }
/*     */     }
/* 774 */     else if (attribute instanceof Deprecated_attribute)
/*     */     {
/* 776 */       dos.writeInt(0);
/*     */     }
/*     */     else
/*     */     {
/* 780 */       byte[] bytes = attribute.info;
/* 781 */       dos.writeInt(bytes.length);
/* 782 */       dos.write(bytes);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected int getAttributeLength(ClassFile.attribute_info attribute)
/*     */   {
/* 788 */     if (attribute instanceof ConstantValue_attribute)
/*     */     {
/* 790 */       return 2;
/*     */     }
/* 792 */     if (attribute instanceof Code_attribute)
/*     */     {
/* 794 */       Code_attribute attr = (Code_attribute)attribute;
/* 795 */       ClassFile.attribute_info[] codeAttrs = attr.attributes;
/* 796 */       int numAttrs = codeAttrs.length;
/* 797 */       int length = 12 + attr.code.length + 8 * attr.exception_table.length + 6 * numAttrs;
/* 798 */       for (int a = 0; a < numAttrs; ++a)
/*     */       {
/* 800 */         ClassFile.attribute_info codeAttr = codeAttrs[a];
/* 801 */         length += getAttributeLength(codeAttr);
/*     */       }
/* 803 */       return length;
/*     */     }
/* 805 */     if (attribute instanceof Exceptions_attribute)
/*     */     {
/* 807 */       Exceptions_attribute attr = (Exceptions_attribute)attribute;
/* 808 */       return 2 + 2 * attr.exception_index_table.length;
/*     */     }
/* 810 */     if (attribute instanceof InnerClasses_attribute)
/*     */     {
/* 812 */       InnerClasses_attribute attr = (InnerClasses_attribute)attribute;
/* 813 */       return 2 + 8 * attr.classes.length;
/*     */     }
/* 815 */     if (attribute instanceof Synthetic_attribute)
/*     */     {
/* 817 */       return 0;
/*     */     }
/* 819 */     if (attribute instanceof SourceFile_attribute)
/*     */     {
/* 821 */       return 2;
/*     */     }
/* 823 */     if (attribute instanceof LineNumberTable_attribute)
/*     */     {
/* 825 */       LineNumberTable_attribute attr = (LineNumberTable_attribute)attribute;
/* 826 */       return 2 + 4 * attr.line_number_table.length;
/*     */     }
/* 828 */     if (attribute instanceof LocalVariableTable_attribute)
/*     */     {
/* 830 */       LocalVariableTable_attribute attr = (LocalVariableTable_attribute)attribute;
/* 831 */       return 2 + 10 * attr.local_variable_table.length;
/*     */     }
/* 833 */     if (attribute instanceof Deprecated_attribute)
/*     */     {
/* 835 */       return 0;
/*     */     }
/*     */ 
/* 838 */     return attribute.info.length;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 844 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99523 $";
/*     */   }
/*     */ 
/*     */   public class Deprecated_attribute extends ClassFile.attribute_info
/*     */   {
/*     */     public Deprecated_attribute()
/*     */     {
/* 309 */       super(ClassFileV2.this);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class LocalVariableTable_attribute extends ClassFile.attribute_info
/*     */   {
/*     */     public Variable[] local_variable_table;
/*     */ 
/*     */     public LocalVariableTable_attribute()
/*     */     {
/* 297 */       super(ClassFileV2.this);
/*     */     }
/*     */ 
/*     */     public class Variable
/*     */     {
/*     */       public short start_pc;
/*     */       public short length;
/*     */ 
/*     */       @ClassFile.ConstantPoolIndex
/*     */       public short name_index;
/*     */ 
/*     */       @ClassFile.ConstantPoolIndex
/*     */       public short descriptor_index;
/*     */       public short index;
/*     */ 
/*     */       public Variable()
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public class LineNumberTable_attribute extends ClassFile.attribute_info
/*     */   {
/*     */     public Line[] line_number_table;
/*     */ 
/*     */     public LineNumberTable_attribute()
/*     */     {
/* 288 */       super(ClassFileV2.this);
/*     */     }
/*     */ 
/*     */     public class Line
/*     */     {
/*     */       public short start_pc;
/*     */       public short line_number;
/*     */ 
/*     */       public Line()
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public class SourceFile_attribute extends ClassFile.attribute_info
/*     */   {
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short sourcefile_index;
/*     */ 
/*     */     public SourceFile_attribute()
/*     */     {
/* 283 */       super(ClassFileV2.this);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class Synthetic_attribute extends ClassFile.attribute_info
/*     */   {
/*     */     public Synthetic_attribute()
/*     */     {
/* 282 */       super(ClassFileV2.this);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class InnerClasses_attribute extends ClassFile.attribute_info
/*     */   {
/*     */     public InnerClass[] classes;
/*     */ 
/*     */     public InnerClasses_attribute()
/*     */     {
/* 271 */       super(ClassFileV2.this);
/*     */     }
/*     */ 
/*     */     public class InnerClass
/*     */     {
/*     */ 
/*     */       @ClassFile.ConstantPoolIndex
/*     */       public short inner_class_info_index;
/*     */ 
/*     */       @ClassFile.ConstantPoolIndex
/*     */       public short outer_class_info_index;
/*     */ 
/*     */       @ClassFile.ConstantPoolIndex
/*     */       public short inner_name_index;
/*     */       public short inner_class_access_flags;
/*     */ 
/*     */       public InnerClass()
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public class Exceptions_attribute extends ClassFile.attribute_info
/*     */   {
/*     */     public short[] exception_index_table;
/*     */ 
/*     */     public Exceptions_attribute()
/*     */     {
/* 266 */       super(ClassFileV2.this);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class Code_attribute extends ClassFile.attribute_info
/*     */   {
/*     */     public short max_stack;
/*     */     public short max_locals;
/*     */     public byte[] code;
/*     */     public ExceptionHandler[] exception_table;
/*     */     public ClassFile.attribute_info[] attributes;
/*     */ 
/*     */     public Code_attribute()
/*     */     {
/* 247 */       super(ClassFileV2.this);
/*     */     }
/*     */ 
/*     */     public class ExceptionHandler
/*     */     {
/*     */       public short start_pc;
/*     */       public short end_pc;
/*     */       public short handler_pc;
/*     */ 
/*     */       @ClassFile.ConstantPoolIndex
/*     */       public short catch_type;
/*     */ 
/*     */       public ExceptionHandler()
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public class ConstantValue_attribute extends ClassFile.attribute_info
/*     */   {
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short constantvalue_index;
/*     */ 
/*     */     public ConstantValue_attribute()
/*     */     {
/* 234 */       super(ClassFileV2.this);
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 242 */       super.appendTo(sb);
/* 243 */       sb.append(": ");
/* 244 */       ClassFileV2.this.appendConstantPoolIndexTo(sb, this.constantvalue_index);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_Utf8_info extends ClassFile.cp_info
/*     */   {
/*     */     public final byte[] bytes;
/*     */     public final String m_string;
/*     */ 
/*     */     public CONSTANT_Utf8_info(byte[] asBytes)
/*     */     {
/* 217 */       super(ClassFileV2.this, 1);
/* 218 */       this.bytes = asBytes;
/* 219 */       this.m_string = new String(asBytes, ClassFile.s_UTF8Charset);
/*     */     }
/*     */ 
/*     */     public CONSTANT_Utf8_info(String asString) {
/* 223 */       super(ClassFileV2.this, 1);
/* 224 */       this.m_string = asString;
/* 225 */       this.bytes = asString.getBytes(ClassFile.s_UTF8Charset);
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 230 */       sb.append(this.m_string);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_NameAndType_info extends ClassFile.cp_info
/*     */   {
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short name_index;
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short descriptor_index;
/*     */ 
/*     */     public CONSTANT_NameAndType_info(short nameIndex, short descriptorIndex)
/*     */     {
/* 196 */       super(ClassFileV2.this, 12);
/* 197 */       this.name_index = nameIndex;
/* 198 */       this.descriptor_index = descriptorIndex;
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 203 */       sb.append("name ");
/* 204 */       ClassFileV2.this.appendConstantPoolIndexTo(sb, this.name_index);
/* 205 */       sb.append(" type ");
/* 206 */       ClassFileV2.this.appendConstantPoolIndexTo(sb, this.descriptor_index);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_Double_info extends ClassFile.cp_info
/*     */   {
/*     */     public double m_value;
/*     */ 
/*     */     public CONSTANT_Double_info(double value)
/*     */     {
/* 179 */       super(ClassFileV2.this, 6);
/* 180 */       this.m_value = value;
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 185 */       sb.append("double ");
/* 186 */       sb.append(this.m_value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_Long_info extends ClassFile.cp_info
/*     */   {
/*     */     public long m_value;
/*     */ 
/*     */     public CONSTANT_Long_info(long value)
/*     */     {
/* 161 */       super(ClassFileV2.this, 5);
/* 162 */       this.m_value = value;
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 167 */       sb.append("long ");
/* 168 */       sb.append(this.m_value);
/* 169 */       sb.append('L');
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_Float_info extends ClassFile.cp_info
/*     */   {
/*     */     public float m_value;
/*     */ 
/*     */     public CONSTANT_Float_info(float value)
/*     */     {
/* 143 */       super(ClassFileV2.this, 4);
/* 144 */       this.m_value = value;
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 149 */       sb.append("float ");
/* 150 */       sb.append(this.m_value);
/* 151 */       sb.append('f');
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_Integer_info extends ClassFile.cp_info
/*     */   {
/*     */     public int m_value;
/*     */ 
/*     */     public CONSTANT_Integer_info(int value)
/*     */     {
/* 127 */       super(ClassFileV2.this, 3);
/* 128 */       this.m_value = value;
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 133 */       sb.append("integer ");
/* 134 */       sb.append(this.m_value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_String_info extends ClassFile.cp_info
/*     */   {
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short string_index;
/*     */ 
/*     */     public CONSTANT_String_info(short stringIndex)
/*     */     {
/* 111 */       super(ClassFileV2.this, 8);
/* 112 */       this.string_index = stringIndex;
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 117 */       sb.append("string ");
/* 118 */       ClassFileV2.this.appendConstantPoolIndexTo(sb, this.string_index);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_InterfaceMethodref_info extends ClassFileV2.RefInfo
/*     */   {
/*     */     public CONSTANT_InterfaceMethodref_info(short classIndex, short nameAndTypeIndex)
/*     */     {
/*  95 */       super(ClassFileV2.this, 11, classIndex, nameAndTypeIndex);
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/* 100 */       sb.append("interface ");
/* 101 */       super.appendTo(sb);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_Methodref_info extends ClassFileV2.RefInfo
/*     */   {
/*     */     public CONSTANT_Methodref_info(short classIndex, short nameAndTypeIndex)
/*     */     {
/*  82 */       super(ClassFileV2.this, 10, classIndex, nameAndTypeIndex);
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/*  87 */       sb.append("method ");
/*  88 */       super.appendTo(sb);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_Fieldref_info extends ClassFileV2.RefInfo
/*     */   {
/*     */     public CONSTANT_Fieldref_info(short classIndex, short nameAndTypeIndex)
/*     */     {
/*  69 */       super(ClassFileV2.this, 9, classIndex, nameAndTypeIndex);
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/*  74 */       sb.append("field ");
/*  75 */       super.appendTo(sb);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class RefInfo extends ClassFile.cp_info
/*     */   {
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short class_index;
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short name_and_type_index;
/*     */ 
/*     */     public RefInfo(byte cpTag, short classIndex, short nameAndTypeIndex)
/*     */     {
/*  53 */       super(ClassFileV2.this, cpTag);
/*  54 */       this.class_index = classIndex;
/*  55 */       this.name_and_type_index = nameAndTypeIndex;
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/*  60 */       ClassFileV2.this.appendConstantPoolIndexTo(sb, this.class_index);
/*  61 */       sb.append(' ');
/*  62 */       ClassFileV2.this.appendConstantPoolIndexTo(sb, this.name_and_type_index);
/*     */     }
/*     */   }
/*     */ 
/*     */   public class CONSTANT_Class_info extends ClassFile.cp_info
/*     */   {
/*     */ 
/*     */     @ClassFile.ConstantPoolIndex
/*     */     public short name_index;
/*     */ 
/*     */     public CONSTANT_Class_info(short nameIndex)
/*     */     {
/*  36 */       super(ClassFileV2.this, 7);
/*  37 */       this.name_index = nameIndex;
/*     */     }
/*     */ 
/*     */     protected void appendTo(StringBuilder sb)
/*     */     {
/*  42 */       sb.append("class ");
/*  43 */       ClassFileV2.this.appendConstantPoolIndexTo(sb, this.name_index);
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.ClassFileV2
 * JD-Core Version:    0.5.4
 */