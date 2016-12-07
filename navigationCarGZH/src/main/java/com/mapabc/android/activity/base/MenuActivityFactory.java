package com.mapabc.android.activity.base;


/**
 * @description: 获取菜单项对应的Action
 * @author: changbao.wang 2011-05-17
 * @version:
 * @modify:
 * @Copyright: mapabc.com
 */
public class MenuActivityFactory {
	private static String[] searchlocActivities = {	
		Constants.ACTIVITY_SEARCH_FIRSTALPHABET,//名称首字母查询
		Constants.ACTIVITY_SEARCHPOIBYKEYWORD,//关键字查询
		Constants.ACTIVITY_SEARCH_FIRSTALPHABET,//门址首字母查询
		Constants.ACTIVITY_SEARCHAROUND,//周边查询
		Constants.ACTIVITY_MYFAVORITES,//我的收藏
		Constants.ACTIVITY_MYFAVORITES,//历史目的地
		null,//回家
		Constants.ACTIVITY_USEREYE};//电子眼
	
	private static String[] ROUTEMANAGERACTIVITIES = { 
		null,//路径规划原则对话框
		//null, 
		null, //模拟导航
		Constants.ACTIVITY_GLANCEROUTE,//全程概览
		Constants.ACTIVITY_ROUTEDESCRIPTION,//路径详情
		null,//删除当前路径
		null,//删除友邻位置
		Constants.ACTIVITY_TRACK,//轨迹管理
		Constants.ACTIVITY_WZTACTIVITY };// ???
	
	private static String[] OTHERFUNCTIONACTIVITYES = {
		Constants.ACTIVITY_SETTINGFORLIKE,
		null,
		Constants.ACTIVITY_ABOUTAUTONAVI,
		null };// 更多功能
	
	private MenuActivityFactory() {
	}

	public static String getSearchActivityIntent(int pos) {
		String intent = null;
		if (pos >= 0 || pos < searchlocActivities.length) {
			intent = searchlocActivities[pos];
		}
		return intent;
	}

	/**
	 * 获取路径管理菜单项中Action
	 * 
	 * 
	 *@param pos
	 *            菜单序号
	 *@return String
	 */
	public static String getRouteManagerActivityIntent(int pos) {
		String intent = null;
		if (pos >= 0 || pos < ROUTEMANAGERACTIVITIES.length) {
			intent = ROUTEMANAGERACTIVITIES[pos];
		}
		return intent;
	}

	/**
	 * 获取更多功能菜单项中Action
	 * 
	 * 
	 *@param pos
	 *            菜单序号
	 *@return String
	 */
	public static String getOtherFunctionActivityIntent(int pos) {
		String intent = null;
		if (pos >= 0 || pos < OTHERFUNCTIONACTIVITYES.length) {
			intent = OTHERFUNCTIONACTIVITYES[pos];
		}
		return intent;
	}
}

