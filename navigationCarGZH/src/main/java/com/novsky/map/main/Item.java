package com.novsky.map.main;

import android.graphics.drawable.Drawable;

/**
 * 收件箱条目的封装对象
 * @author Administrator
 *
 */
public class Item {
	
	
        public String send_id;//发件人
        public String send_date;//发件时间
        public String send_content;//发送内容
        public Drawable message_flag;//图标
        public Boolean checked;//是否被选中
        public Long rowId;//_ID

}
