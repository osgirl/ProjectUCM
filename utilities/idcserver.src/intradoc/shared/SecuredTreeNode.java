/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ 
/*     */ public class SecuredTreeNode
/*     */ {
/*     */   public static final int MAX_NESTING = 100;
/*     */   public static final int MAX_CHILDREN = 100000;
/*     */   public String m_name;
/*     */   public String m_type;
/*     */   public Object m_data;
/*     */   public int m_dataIndex;
/*     */   public String m_group;
/*     */   public String m_account;
/*     */   public int m_accessAllowed;
/*     */   public String m_accessCode;
/*     */   public String m_lastModified;
/*     */   public boolean m_isDeleted;
/*     */   public SecuredTreeNode m_parent;
/*     */   public SecuredTreeNode m_children;
/*     */   public SecuredTreeNode m_prev;
/*     */   public SecuredTreeNode m_next;
/*     */ 
/*     */   public SecuredTreeNode(String name, String type, String group, String account)
/*     */   {
/* 153 */     this.m_name = name;
/* 154 */     this.m_type = type;
/*     */ 
/* 156 */     this.m_data = null;
/* 157 */     this.m_dataIndex = -1;
/*     */ 
/* 159 */     if (group != null)
/*     */     {
/* 161 */       group = group.toLowerCase();
/*     */     }
/* 163 */     this.m_group = group;
/* 164 */     if (account != null)
/*     */     {
/* 166 */       account = account.toLowerCase();
/*     */     }
/* 168 */     this.m_account = account;
/*     */ 
/* 170 */     this.m_lastModified = "-1";
/* 171 */     this.m_isDeleted = false;
/*     */ 
/* 174 */     this.m_accessAllowed = 15;
/* 175 */     this.m_accessCode = "15";
/*     */ 
/* 177 */     this.m_parent = null;
/* 178 */     this.m_children = null;
/* 179 */     this.m_prev = null;
/* 180 */     this.m_next = null;
/*     */   }
/*     */ 
/*     */   public SecuredTreeNode createSubTreeClone()
/*     */   {
/* 188 */     SecuredTreeNode node = new SecuredTreeNode(this.m_name, this.m_type, this.m_group, this.m_account);
/* 189 */     node.m_data = this.m_data;
/* 190 */     node.m_dataIndex = this.m_dataIndex;
/* 191 */     node.m_lastModified = this.m_lastModified;
/* 192 */     node.m_isDeleted = this.m_isDeleted;
/*     */ 
/* 195 */     node.m_accessAllowed = 0;
/* 196 */     node.m_accessCode = "";
/* 197 */     return node;
/*     */   }
/*     */ 
/*     */   public SecuredTreeNode getRootParent()
/*     */   {
/* 205 */     int nestingCount = 0;
/* 206 */     SecuredTreeNode curNode = this;
/*     */     do { if (curNode.m_parent == null)
/*     */         break label35;
/* 209 */       curNode = curNode.m_parent;
/* 210 */       ++nestingCount; }
/* 211 */     while (nestingCount < 100);
/*     */ 
/* 213 */     throw new Error("!apTreeMaxDepth");
/*     */ 
/* 216 */     label35: return curNode;
/*     */   }
/*     */ 
/*     */   public void appendChild(SecuredTreeNode child)
/*     */   {
/* 224 */     if ((child.m_next != null) || (child.m_prev != null) || (child.m_parent != null))
/*     */     {
/* 226 */       String msg = LocaleUtils.encodeMessage("apTreeChildParentless", null, child.m_name);
/*     */ 
/* 228 */       throw new Error(msg);
/*     */     }
/* 230 */     if (this.m_children == null)
/*     */     {
/* 232 */       this.m_children = child;
/*     */     }
/*     */     else
/*     */     {
/* 236 */       SecuredTreeNode curChild = this.m_children;
/* 237 */       int childCount = 0;
/*     */       do { if (curChild.m_next == null)
/*     */           break label106;
/* 240 */         curChild = curChild.m_next;
/* 241 */         ++childCount; }
/* 242 */       while (childCount < 100000);
/*     */ 
/* 244 */       String msg = LocaleUtils.encodeMessage("apTooManyChildren", null, this.m_name);
/*     */ 
/* 246 */       throw new Error(msg);
/*     */ 
/* 249 */       label106: curChild.m_next = child;
/* 250 */       child.m_prev = curChild;
/*     */     }
/* 252 */     child.m_parent = this;
/*     */   }
/*     */ 
/*     */   public void detachFromParent()
/*     */   {
/* 261 */     boolean childRemains = false;
/* 262 */     if (this.m_prev != null)
/*     */     {
/* 264 */       childRemains = true;
/* 265 */       this.m_prev.m_next = this.m_next;
/*     */     }
/* 267 */     if (this.m_next != null)
/*     */     {
/* 269 */       childRemains = true;
/* 270 */       this.m_next.m_prev = this.m_prev;
/*     */     }
/* 272 */     this.m_prev = null;
/* 273 */     this.m_next = null;
/*     */ 
/* 276 */     if ((!childRemains) && 
/* 278 */       (this.m_parent != null))
/*     */     {
/* 280 */       this.m_parent.m_children = null;
/*     */     }
/*     */ 
/* 283 */     this.m_parent = null;
/*     */   }
/*     */ 
/*     */   public void replaceInTree(SecuredTreeNode newNode)
/*     */   {
/* 293 */     newNode.m_parent = this.m_parent;
/* 294 */     newNode.m_prev = this.m_prev;
/* 295 */     newNode.m_next = this.m_next;
/* 296 */     newNode.m_children = this.m_children;
/*     */ 
/* 298 */     if (this.m_prev != null)
/*     */     {
/* 300 */       this.m_prev.m_next = newNode;
/*     */     }
/* 302 */     if (this.m_next != null)
/*     */     {
/* 304 */       this.m_next.m_prev = newNode;
/*     */     }
/*     */ 
/* 308 */     if ((this.m_parent != null) && (this.m_parent.m_children == this))
/*     */     {
/* 310 */       this.m_parent.m_children = newNode;
/*     */     }
/*     */ 
/* 314 */     int childCount = 0;
/* 315 */     SecuredTreeNode curChild = this.m_children;
/*     */     do { if (curChild == null)
/*     */         break label140;
/* 318 */       curChild.m_parent = newNode;
/* 319 */       curChild = curChild.m_next;
/* 320 */       ++childCount; }
/* 321 */     while (childCount < 100000);
/*     */ 
/* 323 */     String msg = LocaleUtils.encodeMessage("apTooManyChildren", null, this.m_name);
/*     */ 
/* 325 */     throw new Error(msg);
/*     */ 
/* 330 */     label140: this.m_parent = null;
/* 331 */     this.m_prev = null;
/* 332 */     this.m_next = null;
/* 333 */     this.m_children = null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 338 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SecuredTreeNode
 * JD-Core Version:    0.5.4
 */