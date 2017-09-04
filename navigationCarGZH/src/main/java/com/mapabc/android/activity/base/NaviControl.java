package com.mapabc.android.activity.base;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.BDParameterException;
import android.location.BDUnknownException;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.bd.comm.protocal.BDCommManager;
import com.mapabc.android.activity.NaviStudioActivity;
import com.mapabc.android.activity.R;
import com.mapabc.android.activity.listener.CrossingZoomListener;
import com.mapabc.android.activity.listener.DisPatchInfo;
import com.mapabc.android.activity.listener.MyMapListener;
import com.mapabc.android.activity.utils.SettingForLikeTools;
import com.mapabc.android.activity.utils.ToolsUtils;
import com.mapabc.naviapi.MapAPI;
import com.mapabc.naviapi.MapView;
import com.mapabc.naviapi.RouteAPI;
import com.mapabc.naviapi.TTSAPI;
import com.mapabc.naviapi.UtilAPI;
import com.mapabc.naviapi.listener.RouteListener;
import com.mapabc.naviapi.route.GPSRouteInfo;
import com.mapabc.naviapi.route.GpsInfo;
import com.mapabc.naviapi.route.MVPSPosition;
import com.mapabc.naviapi.route.MVPSVPPosition;
import com.mapabc.naviapi.route.RouteSegInfo;
import com.mapabc.naviapi.search.SearchResultInfo;
import com.mapabc.naviapi.type.Const;
import com.mapabc.naviapi.type.FloatValue;
import com.mapabc.naviapi.type.IntValue;
import com.mapabc.naviapi.type.NSLonLat;
import com.mapabc.naviapi.utils.SysParameterManager;
import com.novsky.map.util.Utils;

public class NaviControl extends RouteListener implements CarLinesListener {
    public static final int OVER_SPEED_DIALOG = 199;// 计算路经失败
    public static final int H_FIND_PATH_FAILED = 103;// 计算路经失败
    public static final int H_FIN_DPATH_SUCCESS = 102;// 计算路经成功
    public static final int H_UPDATE_UI_FOR_NAVI = 104;// 更新导航UI
    public static final int H_SIMNAVI_STOP = 105;// 模拟导航结束
    public static final int H_ROUTE_RECALCULATION = 106;// 偏航
    public static final int H_ARRIVE_END_POINT = 107;// 到达终点
    public static final int H_ROUTE_RECALCULATION_FINISH = 108;// 偏航结束
    public static final int H_UPDATE_UI_SPEED = 109;// 速度更新
    public static final int H_FIND_PATH_FAILED2 = 110;// 计算路经失败,恢复上一路径
    public static final int H_GPS_FIRST_FIXED = 111;// 第一次定位成功偏差过大将重新计算距离
    public static final int CAR_GPS_SATUS = 112;// 无gps信号
    public static final int H_CONTINUEROUTE = 113;// 续航结果
    RouteLayer routeLayer = new RouteLayer();
    private NextRoadView m_nextRoadView;
    private TextView m_RemainDis; // 剩余距离
    private ImageView m_RemainDisUnit; // 剩余距离单位
    private RelativeLayout m_CrossingLayout; // 放大路口
    private CrossingView crossingview;// 放大路口图
    private ImageView m_NaviTurn; // 转向
    private TextView m_TurnDis; // 转向距离
    private TextView m_NaviCurSpeed; // 当前时速
    private LinearLayout navi_turn_layout;// 转向 左上角
    public String m_roadName = "";
    public static ExecutorService executorService = Executors
            .newFixedThreadPool(10);
    public ProgressDialog pdg = null;
    public ProgressDialog dlgRecalculateRoute = null;
    public AlertDialog dialog;
    private MapView m_mapView;
    public static int mgpsStatus = 2;// 0GPS未连接,1GPS正在搜索中,2GPS连接未定位,3GPS连接已定位
    private int naviAction = 0;// 导航指引记录
    public int segIndex = -1;// 车辆所在路段
    public int calculateType = 0;// 0 本地算路，1网络算路
    private DisPatchInfo disPatchGpsInfo; // gpsInfo信息分发器
    private NaviStudioActivity m_activity;
    private static NaviControl m_NaviControl;
    private boolean blConfirm = false;// 阻止连续点击确定
    private boolean isRefresh = false;// 是否更新UI状态
    public boolean isReCaling = false;// 路径重算当中
    public boolean isShowNaviInfo = true;// 是否隐藏导航控件
    public boolean isAutoStopSimNavi = true;// 是否自动停止模拟导航
    public boolean isTalkStartNavi = false;// 是否播报开始导航语音
    public static final int NAVI_STATUS_SIMNAVI = 1;// 模拟导航进行中
    public static final int NAVI_STATUS_STOPED = 0;// 无路径状态
    public static final int NAVI_STATUS_REALNAVI = 2;// 真实导航进行中
    public int naviStatus = 0; // 0停止状态，1模拟导航状态，2真实导航状态
    public GPSRouteInfo routeInfo = null;// 导航信息
    private ImageView m_NoCrossingLane; // 无放大路口时的车道
    private static final String TAG = "NaviControl";
    private ImageView m_NaviCamera; // 电子眼
    private float x0 = 0;
    private int firstfixed_recal = 0;// 0不需要重新计算路径，1需要重新计算路径
    public GpsInfo gpsInfo = null;
    private boolean IS_SPEAK_HAVEGPS = false;// 是否已经播报GPS已定位
    LayoutParams params_lane = null;
    // int params_lane_state = -1;// 0表示无路口放大图，有车道线，1表示有路口放大图，有车道线
    public NSLonLat vehiclePosition = null;// 模拟导航前保持车辆位置
    private String preLaneName = "";
    private Bitmap bmp;
    private Bitmap camera_bmp;
    public SearchResultInfo end_poiInfo;// 搜索结果中的终点
    public NaviStudioApplication myapp;
    public static boolean haveGPS = false;
    //弹出结束 友好对话框 标记
    boolean isFirst = true;
    private SharedPreferences overSpeednum = null, overSpeedMax = null;
    BDCommManager manager;
    boolean overSpeedsend = true;
    public Timer timer = new Timer();
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what > 0 && msg.what < 60) {
                overSpeedsend = false;
                Log.e(TAG, "overSpeedsend=false  正在倒计时");
            } else {
                overSpeedsend = true;
                Log.e(TAG, "overSpeedsend=true  计时器空闲");
            }
            if (msg.what == 0) {
                timer.cancel();
                timer.purge();
                timer = new Timer();
                Log.e(TAG, "overSpeedsend=false  清除计时器");
            }


            switch (msg.what) {
                case OVER_SPEED_DIALOG:
                    //超速提示窗口
                    showMaxSpeed();
                    break;
                case H_FIND_PATH_FAILED2:// 添加途经点避让点失败
                    m_mapView.goBackCar();
                    showFailedMessage();
                    break;
                case H_FIND_PATH_FAILED:// 计算路径失败
                    routeLayer.deleteLayer();
                    m_mapView.goBackCar();
                    m_mapView.invalidate();
                    showFailedMessage();
                    break;

                case H_FIN_DPATH_SUCCESS:// 计算路径成功
                    Log.e(TAG, "计算路径完成");
                    drawRoute();
                    m_mapView.goBackCar();
                    NSLonLat mlonlat = (NSLonLat) msg.obj;
                    routeInfo = null;
                    Log.e(TAG, "ceshi1");
                    //开始导航
                    startNavigate();
                    Log.e(TAG, "ceshi2");
                    if (mlonlat != null) {
                        CurrentPointListener currentPointListener = new CurrentPointListener(
                                m_activity, m_mapView);
                        if (end_poiInfo != null) {
                            currentPointListener.addPOIToHistory(end_poiInfo);
                            end_poiInfo = null;
                        } else {
                            currentPointListener.addPOIToHistory(mlonlat);
                        }
                    }
                    Log.e(TAG, "FINDPATHSUCCESS end");
                    break;
                case H_UPDATE_UI_FOR_NAVI:// 更新UI
                    GPSRouteInfo routeInfo1 = (GPSRouteInfo) msg.obj;
                    //把GPSRouteInfo的速度替换为实时速度
                    Log.e(TAG, "----超速报警速度 speed = " + routeInfo1.speed);
//				/**
//				 * 由于卫星返回原始速度是除以3.6系数得到的,底层判断是否超速也默认除以3.6系数,但是由于七寸屏使用的是外置连接方式,
//				 * 返回的数据没有系数的，而底层默认还是除以3.6系数,所以在此得到的原始速度需要除以3.6系数。
//				 */
//				float newSpeed = routeInfo1.speed/3.6f;
//				routeInfo1.speed = newSpeed;

                    if (routeInfo1.remainDis > 10000000) {
                        Log.e(TAG, "routeInfo1.remainDis:" + routeInfo1.remainDis);
                    }
                    // Log.e(TAG,
                    // "底层坐标："+routeInfo.vehicleLonLat.x+","+routeInfo.vehicleLonLat.y);
                    //超速报警代码
//				if (routeInfo1.overSpeed) {
//					if (SettingForLikeTools.getOverSpeed(m_activity) == 1) {
//						TTSAPI.getInstance()
//								.addPlayContent(
//										m_activity
//												.getString(R.string.navicontrol_overspeed),
//										Const.AGPRIORITY_NORMAL);
//					}
//				}


                    showCamara(routeInfo1);//控制显示电子眼

                    if (routeInfo1.vehicleLonLat != null
                            && routeInfo1.vehicleLonLat.x > 0) {
                        NaviControl.this.routeInfo = routeInfo1;
                    } else {
                        return;
                    }
                    updateCarStatus(routeInfo.angle, routeInfo.vehicleLonLat);
                    if (routeInfo.segIndex == -1) {
                        if (m_activity != null) {
                            m_activity.setMapCenterRoadName();
                        }
                        m_mapView.invalidate();
                        return;
                    }
                    if (RouteAPI.getInstance().isRouteValid()) {
                        NaviControl.this.updateSegRoadColor(routeInfo1);
                        NaviControl.this.updateCurSegRoadColor(routeInfo1);
                        if (isShowNaviInfo) {
                            updateUIForNavi(routeInfo1);
                            crossingview.refresh(routeInfo1);
                        }
                    } else {
                        if (m_activity != null) {
                            m_activity.setMapCenterRoadName();
                        }
                    }
                    if (!routeInfo1.laneInfo.equals("")) {
                        if (!MyMapListener.CARNOTINCENTER) {
                            m_NoCrossingLane.setVisibility(View.GONE);
                        } else {
                            if ((!ToolsUtils.isLand(m_activity))
                                    && routeInfo1.segRemainDis < 300) {
                                m_NoCrossingLane.setVisibility(View.GONE);
                            } else {
                                m_NoCrossingLane.setVisibility(View.VISIBLE);
                            }
                        }
                        if ((!preLaneName.equals(routeInfo1.laneInfo)) || (m_NoCrossingLane.getVisibility() == View.INVISIBLE)) {
                            updateLane(routeInfo1);
                        }
                        preLaneName = routeInfo1.laneInfo;
                    } else {
                        bmp = null;
                        m_NoCrossingLane.setVisibility(View.GONE);
                    }
                    m_mapView.invalidate();
                    break;
                case H_SIMNAVI_STOP:// 模拟导航结束
                    // Log.e(TAG, "模拟导航结束");
                    if (isAutoStopSimNavi) {
                        simNaviStopedInfo();
                    } else {
                        if (vehiclePosition != null) {
                            MapAPI.getInstance().setVehiclePosInfo(vehiclePosition,
                                    MapAPI.getInstance().getVehicleAngle());
                        }
                    }
                    preLaneName = "";
                    m_NoCrossingLane.setImageBitmap(null);
                    // m_CrossingLane.setImageBitmap(null);
                    m_NaviCamera.setImageBitmap(null);
                    break;
                case H_ROUTE_RECALCULATION:// 偏航
                    Log.e(TAG, "偏航开始");
                    if (NaviControl.this.m_activity.currentPointListener.ad != null) {
                        NaviControl.this.m_activity.currentPointListener.ad
                                .dismiss();
                    }
                    offsetGuide();
                    break;
                case H_ROUTE_RECALCULATION_FINISH:// 偏航结束
                    isTalkStartNavi = false;
                    if (dlgRecalculateRoute != null) {
                        dlgRecalculateRoute.dismiss();
                    }
                    if (msg.arg1 != 1) {
                        routeLayer.deleteLayer();
                        m_mapView.goBackCar();
                        m_mapView.invalidate();
                        if (calculateType == 1) {
                            showReCalMessage();
                        }
                        Log.e(TAG, "偏航重算失败");
                        break;
                    }
                    int visibilicy = m_activity.back_car.getVisibility();
                    if (visibilicy == View.GONE) {
                        Log.e(TAG, "________________偏航重算结束________________");
                        NSLonLat carLonLat = MapAPI.getInstance().getVehiclePos();
                        Log.e(TAG, "carLonLat:" + carLonLat.x + "," + carLonLat.y);
                        MapAPI.getInstance().setMapCenter(carLonLat);
                    }
                    NaviControl.this.drawRoute();
                    //以下代码是为了解决偏航后电子眼不显示的问题
                    if (routeInfo.eyePos.x != 0) {
                        addNaviCamera(routeInfo);
                    }
                    routeInfo = null;
                    startNavigate();
                    m_mapView.goBackCar();
                    m_mapView.invalidate();
                    Log.e(TAG, "偏航结束");
                    break;
                case H_ARRIVE_END_POINT:// 到达终点

                    //弹出友好提示框
                    if (isFirst) {
                        isFirst = false;
                        alterDialogEndNavi();
                    }
//				RouteAPI.getInstance().stopGPSNavi();
//				RouteAPI.getInstance().clearRoute();
//				routeLayer.deleteLayer();
//				NaviControl.getInstance().guideEnd();
                    break;
                case H_UPDATE_UI_SPEED:// 更新速度
                    NaviControl.this.updateNaviSpeed(msg.arg1);
                    break;
                case H_GPS_FIRST_FIXED:// 定位成功，重新计算路径
                    // Log.e(TAG, "定位成功，重新计算路径1");
                    firstfixed_recal = 0;
                    MVPSPosition pos = (MVPSPosition) msg.obj;
                    IntValue distance = new IntValue();
                    IntValue time = new IntValue();
                    boolean re = RouteAPI.getInstance().getDistanceAndTime(
                            distance, time);
                    if (!re) {
                        return;
                    }
                    int len = 99;
                    if (NaviControl.this.routeInfo != null) {
                        len = distance.value - NaviControl.this.routeInfo.remainDis;
                    }
                    if (len < 100 && naviStatus == NAVI_STATUS_REALNAVI) {
                        NSLonLat lonlat = new NSLonLat();
                        lonlat.x = pos.x;
                        lonlat.y = pos.y;
                        if (MapAPI.getInstance().getMapView() == Const.MAP_VIEWSTATE_NORTH) {
                            MapAPI.getInstance().setVehiclePosInfo(lonlat,
                                    pos.angle);
                        } else {
                            MapAPI.getInstance().setVehiclePosInfo(lonlat, 0);
                        }
                        // Log.e(TAG, "定位成功，重新计算路径5");
                        offsetGuide();
                    }
                    break;
                case CAR_GPS_SATUS:// 刷新车位gps状态
                    m_mapView.invalidate();
                    break;
                case H_CONTINUEROUTE:// 续航结束
                    if (dlgRecalculateRoute != null) {
                        dlgRecalculateRoute.dismiss();
                    }
                    if (msg.arg1 == 1) {
                        drawRoute();
                        m_mapView.goBackCar();
                        routeInfo = null;
                        Log.e(TAG, "续航计算路径完成");
                        startNavigate();
                        Log.e(TAG, "H_CONTINUEROUTE end");
                    } else {
                        ToolsUtils.showTipInfo(m_activity,
                                R.string.navicontrol_continue_route_failed);
                    }
                    break;
            }
        }

    };

    /**
     * @param routeInfo1 void
     * @Copyright:mapabc
     * @description:负责电子眼的显示
     * @author fei.zhan
     * @date 2013-5-9
     */
    private void showCamara(GPSRouteInfo routeInfo1) {
        if (routeInfo1.eyePos.x != 0) {
            Log.e(TAG, "routeInfo.eyePos.x != 0");
            if (!MyMapListener.CARNOTINCENTER) {
                m_NaviCamera.setVisibility(View.GONE);
            } else if (MyMapListener.CARNOTINCENTER
                    && routeInfo1.hasCross
                    && !ToolsUtils.isLand(m_mapView.getContext())) {
                m_NaviCamera.setVisibility(View.GONE);
            } else if (!RouteAPI.getInstance().isRouteValid()) {
                m_NaviCamera.setVisibility(View.GONE);
            } else {
                m_NaviCamera.setVisibility(View.VISIBLE);
                camera_bmp = BitmapFactory.decodeResource(m_activity
                                .getResources(),
                        R.drawable.currentpoint_usereye);
                m_NaviCamera.setImageBitmap(camera_bmp);
            }
            if (routeInfo1.eyePos.x != x0) {
                x0 = routeInfo1.eyePos.x;
                addNaviCamera(routeInfo1);
            }

        } else {
            Log.e(TAG, "routeInfo.eyePos.x == 0");
            m_NaviCamera.setVisibility(View.GONE);
            if (routeInfo1.eyeDist == 0) {
                RouteLayer rl = new RouteLayer();
                rl.deleteEleEyePoint(RouteLayer.ELE_EYE);
            }
        }
    }

    public static NaviControl getInstance() {
        if (m_NaviControl == null) {
            m_NaviControl = new NaviControl();
            Log.e(TAG, "new NaviControl");
        }
        return m_NaviControl;
    }

    private NaviControl() {

    }

    public void setLayout(View view, NaviStudioActivity activity,
                          MapView mapView) {
        this.m_activity = activity;
        this.m_mapView = mapView;
        setLayout(view);
        myapp = (NaviStudioApplication) m_activity.getApplication();
    }

//	public void setLayout(View view, MapActivity activity,
//			MapView mapView) {
//		this.m_activity = activity;
//		this.m_mapView = mapView;
//		setLayout(view);
//		myapp = (NaviStudioApplication) m_activity.getApplication();
//	}

    private void setLayout(View view) {
        manager = BDCommManager.getInstance(m_activity.getBaseContext());
        navi_turn_layout = (LinearLayout) view
                .findViewById(R.id.ll_navi_turn_layout);
        m_nextRoadView = (NextRoadView) view
                .findViewById(R.id.tv_navi_next_road);
        m_NaviTurn = (ImageView) view.findViewById(R.id.iv_navi_turn);
        m_TurnDis = (TextView) view.findViewById(R.id.tv_navi_turn_dis);
        m_RemainDis = (TextView) view.findViewById(R.id.tv_navi_remain_dis);
        m_RemainDisUnit = (ImageView) view
                .findViewById(R.id.ib_navi_remain_dis_unit);
        m_NaviCurSpeed = (TextView) view.findViewById(R.id.tv_navi_speed);
        m_NaviCamera = (ImageView) view.findViewById(R.id.iv_navi_camera);
        crossingview = new CrossingView(view.getContext());
        crossingview.setVisibility(View.GONE);
        crossingview.setCrossingZoomListener(m_CrossingZoomListener);
        m_CrossingLayout = (RelativeLayout) view
                .findViewById(R.id.ll_crossinglayout);
        // m_CrossingLane = (ImageView)
        // view.findViewById(R.id.iv_crossing_lane);
        // m_CrossingLane.setVisibility(View.GONE);
        m_NoCrossingLane = (ImageView) view
                .findViewById(R.id.iv_no_crossing_lane);
        params_lane = (LayoutParams) m_NoCrossingLane.getLayoutParams();
        View view0 = view.findViewById(R.id.view_crossing);
        crossingview.setId(R.id.view_crossing);
        int nIndex = m_CrossingLayout.indexOfChild(view0);
        m_CrossingLayout.removeView(view0);
        ViewParent parent = crossingview.getParent();
        if (parent != null && parent instanceof RelativeLayout) {
            ((RelativeLayout) parent).removeView(crossingview);
        }
        m_CrossingLayout.addView(crossingview, nIndex, view0.getLayoutParams());
        m_CrossingLayout.setVisibility(View.VISIBLE);
        crossingview.setCarLinesListener(this);
    }

    public void setColor(boolean mdayOrNight) {
        if (MapAPI.getInstance().getDayOrNightMode() == Const.MAP_STYLE_DAY) {
            m_nextRoadView.setTextColor(Color.BLACK);
            m_RemainDis.setTextColor(Color.BLACK);
            m_TurnDis.setTextColor(Color.BLACK);
            m_NaviCurSpeed.setTextColor(Color.BLACK);
            m_NaviCurSpeed.setBackgroundResource(R.drawable.navistudio_speed_day);
            if (ToolsUtils.isLand(m_activity)) {
                navi_turn_layout.setBackgroundResource(R.drawable.navistudio_direct_land_day);
            } else {
                navi_turn_layout.setBackgroundResource(R.drawable.navistudio_direct_port_day);
            }
        } else {
            m_nextRoadView.setTextColor(Color.WHITE);
            m_RemainDis.setTextColor(Color.WHITE);
            m_TurnDis.setTextColor(Color.WHITE);
            m_NaviCurSpeed.setTextColor(Color.WHITE);
            m_NaviCurSpeed
                    .setBackgroundResource(R.drawable.navistudio_speed_night);
            if (ToolsUtils.isLand(m_activity)) {
                navi_turn_layout
                        .setBackgroundResource(R.drawable.navistudio_direct_land_night);
            } else {
                navi_turn_layout
                        .setBackgroundResource(R.drawable.navistudio_direct_port_night);
            }
        }
    }

    /*
     * 启动模拟导航
     */
    public void startSimulate() {
        if (naviStatus == NaviControl.NAVI_STATUS_REALNAVI) {
            RouteAPI.getInstance().stopGPSNavi();
            Log.e(TAG, "关闭实时导航");
        }
        if (RouteAPI.getInstance().startSimNavi()) {
            naviStatus = NaviControl.NAVI_STATUS_SIMNAVI;
            vehiclePosition = MapAPI.getInstance().getVehiclePos();
        }
    }

    private void updateCarStatus(float angle, NSLonLat lonlat) {
        if (lonlat.x <= 0) {
            return;
        }
        if (myapp.isTracePlay) {// 如果正在进行轨迹回放，暂时不接受GPS位置更新
            return;
        }
        if (MapAPI.getInstance().getMapView() == Const.MAP_VIEWSTATE_NORTH) {
            if (MapAPI.getInstance().isCarInCenter()) {
                MapAPI.getInstance().setVehiclePosInfo(lonlat,
                        angle);
                MapAPI.getInstance().setMapCenter(lonlat);
            } else {
                MapAPI.getInstance().setVehiclePosInfo(lonlat,
                        angle);
            }
            Log.e(TAG, "设置的车辆角度：" + angle);
        } else {
            MapAPI.getInstance().setMapAngle(360 - angle);
            if (MapAPI.getInstance().isCarInCenter()) {
                MapAPI.getInstance().setVehiclePosInfo(lonlat, 0);
                MapAPI.getInstance().setMapCenter(lonlat);
            } else {
                MapAPI.getInstance().setVehiclePosInfo(lonlat, 0);
            }
        }

    }

    /*
     * 真实导航
     */
    public void updateUIForNavi(GPSRouteInfo gpsInfo) {
        Log.e(TAG, "ceshi8");
        this.isRefresh = false;
        this.naviAction = -1;
        updateNaviRemainDis(gpsInfo.remainDis);
        updateNaviTurn(gpsInfo.segRemainDis, gpsInfo.remainDis,
                gpsInfo.naviAction, gpsInfo.naviAssist);
        if (gpsInfo.speed == 0) {
            updateNaviSpeed(-1);
        } else {
            //更新ui 是在主线程吗?
            //m_NaviCurSpeed.setText(gpsInfo.speed + "");
        }
        setRoadName(gpsInfo.nextRoadName);
        this.guideBegin();
        Log.e(TAG, "ceshi9");
    }

    public void setRoadName(String roadName) {
        try {
            if (m_nextRoadView != null) {
                if (!m_roadName.equals(roadName)) {
                    m_roadName = roadName;
                    m_nextRoadView.setText(m_roadName);
                    m_nextRoadView.requestFocus();
                    m_nextRoadView.invalidate();
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "ERROR", ex);
        }
    }

    /*
     * 更新剩余总路程
     */
    private void updateNaviRemainDis(int dis) {
        if (RouteAPI.getInstance().isRouteValid() && dis == 0) {
            return;
        }
        if (dis > 1000) {
            DecimalFormat df = new DecimalFormat();
            df.applyPattern("#.00");
            String s = df.format((double) dis / 1000.0D);
            m_RemainDis.setText(s);
            m_RemainDisUnit
                    .setBackgroundResource(R.drawable.navistudio_remaindis_km);
        } else {
            m_RemainDis.setText(dis + "");
            m_RemainDisUnit
                    .setBackgroundResource(R.drawable.navistudio_remaindis_m);
        }

    }

    /**
     * 更新走过路段颜色
     *
     * @param gpsInfo 导航信息
     */
    private void updateSegRoadColor(GPSRouteInfo gpsInfo) {
        int newIndex = gpsInfo.segIndex - 1;
        if (this.segIndex != newIndex) {
            if (segIndex == -1) {
                Log.e(TAG, "this.segIndex:" + segIndex);
                Log.e(TAG, "newIndex:" + newIndex);
                this.segIndex = newIndex;
                for (int i = 0; i <= segIndex; i++) {
                    Log.e(TAG, "_____i___" + i);
                    RouteSegInfo segInfo = new RouteSegInfo();
                    if (RouteAPI.getInstance().getSegmentInfo(i, segInfo)) {
                        routeLayer.addRoute(segInfo.lons, segInfo.lats,
                                200 + i, RouteLayer.PAINTER_PASS_LINE);
                    }
                }
            } else {
                Log.e(TAG, "this.segIndex" + segIndex);
                Log.e(TAG, "newIndex" + newIndex);
                this.segIndex = newIndex;
                RouteSegInfo segInfo = new RouteSegInfo();
                if (RouteAPI.getInstance().getSegmentInfo(segIndex, segInfo)) {
                    routeLayer.addRoute(segInfo.lons, segInfo.lats,
                            200 + segIndex, RouteLayer.PAINTER_PASS_LINE);
                }
            }
        }
    }

    /**
     * 更新当前路段部分路的颜色
     *
     * @param gpsInfo 导航信息
     */
    private void updateCurSegRoadColor(GPSRouteInfo gpsInfo) {
        int posIndex = 0;// 车辆所在当前路段点坐标索引
        posIndex = gpsInfo.posIndex;
        RouteSegInfo segInfo = new RouteSegInfo();
        if (RouteAPI.getInstance().getSegmentInfo(gpsInfo.segIndex, segInfo)) {
            NSLonLat vpoint = new NSLonLat();
            if (posIndex >= segInfo.lons.length) {
                Log.e(TAG, "导航点索引错误，posIndex=" + gpsInfo.posIndex + ",segInfo.lons.length=" + segInfo.lons.length);
            }
            Log.e(TAG, "posIndex=" + posIndex + ",segInfo.lons.length=" + segInfo.lons.length);
            if ((posIndex + 1) >= segInfo.lons.length) {
                UtilAPI.getInstance().point2LineVertical(
                        new NSLonLat(segInfo.lons[posIndex],
                                segInfo.lats[posIndex]),
                        new NSLonLat(segInfo.lons[posIndex],
                                segInfo.lats[posIndex]), gpsInfo.vehicleLonLat,
                        vpoint);
                Log.e(TAG, "point2LineVertical_ end");
            } else {
                UtilAPI.getInstance().point2LineVertical(
                        new NSLonLat(segInfo.lons[posIndex],
                                segInfo.lats[posIndex]),
                        new NSLonLat(segInfo.lons[posIndex + 1],
                                segInfo.lats[posIndex + 1]),
                        gpsInfo.vehicleLonLat, vpoint);
                Log.e(TAG, "point2LineVertical end");
            }

            float lons[] = new float[posIndex + 2];
            float lats[] = new float[posIndex + 2];
            System.arraycopy(segInfo.lons, 0, lons, 0, posIndex + 1);
            System.arraycopy(segInfo.lats, 0, lats, 0, posIndex + 1);
            if (vpoint.x <= 0) {
                return;
            }
            lons[posIndex + 1] = vpoint.x;
            lats[posIndex + 1] = vpoint.y;
            routeLayer.addCurSegOverlay(lons, lats,
                    RouteLayer.PAINTER_PASS_LINE);
            Log.e(TAG, "updateCurSegRoadColor end!");
//			Log.e(TAG, "车辆位置对比：segIndex:" + gpsInfo.segIndex + ","
//					+ gpsInfo.vehicleLonLat.x + "," + gpsInfo.vehicleLonLat.y
//					+ "__" + vpoint.x + "," + vpoint.y);
        }
    }

    /*
     * 更新模拟导航速度??
     * 更新速度
     */
    private void updateNaviSpeed(int speed) {
        // 更新速度显示
        if (naviStatus == NAVI_STATUS_SIMNAVI) {
            int deomspeed = SettingForLikeTools.getDemoSpeed(m_activity);
            if (deomspeed == 0) {
                m_NaviCurSpeed.setText(Const.SIM_NAVI_SPEED_LOW_VALUE + "");
            } else if (deomspeed == 1) {
                m_NaviCurSpeed.setText(Const.SIM_NAVI_SPEED_NORMAL_VALUE + "");
            } else {
                m_NaviCurSpeed.setText(Const.SIM_NAVI_SPEED_HIGH_VALUE + "");
            }
        } else {
            if (speed != -1) {
                m_NaviCurSpeed.setText(speed + "");
            }
        }
    }

    /*
     * 更新转向图片，路段剩余距离
     */
    private void updateNaviTurn(int segRemainDis, int remainDis,
                                int naviAction, int naviAssist) {
        String naviInfo = "";

        // 转向距离
        String strTurnDis = null;
        if (segRemainDis > 1000) {
            DecimalFormat df = new DecimalFormat();
            df.applyPattern("#.00km");
            strTurnDis = df.format((double) segRemainDis / 1000.0D);
        } else {
            strTurnDis = (new StringBuilder()).append(segRemainDis).append("m")
                    .toString();
        }
        m_TurnDis.setText(strTurnDis);
        switch (naviAssist) {
            case Const.ASSIACTION_ARRIVE_DESTINATION:// 到达目的地的
                m_NaviTurn.setBackgroundResource(R.drawable.navistudio_tothis);
                return;
            case Const.ASSIACTION_ARRIVE_WAY:// 到达途径地
                m_NaviTurn
                        .setBackgroundResource(R.drawable.currentpoint_jingguothis);
                return;
            case Const.ASSIACTION_ARRIVE_TOLLGATE: // 到达收费站
                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_fee);
                return;
            case Const.ASSIACTION_ENTRY_TUNNEL:// 进入隧道
                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_goto_sd);
                return;
        }
        if (this.naviAction == naviAction) {
            return;
        }
        this.naviAction = naviAction;
        switch (naviAction) {
            case 0: // 无动作
                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_goto_front);
                naviInfo += " 转向:" + "无动作";
                break;
            case 1: // 左转

                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_goto_left);
                naviInfo += " 转向:" + "左转";
                break;
            case 2: // 右转

                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_goto_right);
                naviInfo += " 转向:" + "右转";
                break;
            case 3: // 左前方

                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_goto_lf);

                naviInfo += " 转向:" + "左前方";
                break;
            case 4: // 右前方

                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_goto_rf);

                naviInfo += " 转向:" + "右前方";
                break;
            case 5: // 左后转

                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_goto_lb);
                naviInfo += " 转向:" + "左后转";
                break;
            case 6: // 右后转

                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_goto_rb);
                naviInfo += " 转向:" + "右后转";
                break;
            case 7: // 掉头

                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_goto_back);
                naviInfo += " 转向:" + "掉头";
                break;
            case 8: // 直行

                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_goto_front);
                naviInfo += " 转向:" + "直行";
                break;
            case 9: // 靠左

                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_keep_left);
                naviInfo += " 转向:" + "靠左";
                break;
            case 10: // 靠右

                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_keep_right);
                naviInfo += " 转向:" + "靠右";
                break;
            case 11: // 进入环岛

                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_roundabout);
                naviInfo += " 转向:" + "进入环岛";
                break;
            case 12: // 离开环岛
                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_roundabout);
                naviInfo += " 转向:" + "离开环岛";
                break;
            case 13: // 减速行
                m_NaviTurn.setBackgroundResource(R.drawable.navicontrol_slowdown);
                naviInfo += " 转向:" + "减速行";
                break;
            case 14: // 特有的插入直行
                naviInfo += " 转向:" + "特有的插入直行";
                break;
            // case 15:// 到达目的地附近
            // m_NaviTurn.setBackgroundResource(R.drawable.navistudio_tothis);
            // break;
        }
    }

    public void continueRoute(final java.util.ArrayList list) {
        if (m_activity != null) {
            try {
                if (isReCaling) {
                    return;
                }
                isReCaling = true;
                Log.e(TAG, "路径重算开启");
                dlgRecalculateRoute = new ProgressDialog(m_activity);
                dlgRecalculateRoute
                        .setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dlgRecalculateRoute.setIndeterminate(false);
                dlgRecalculateRoute.setCancelable(false);
                dlgRecalculateRoute.setMessage(m_activity
                        .getString(R.string.navicontrol_calculate_path));
                dlgRecalculateRoute.show();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            RouteAPI.getInstance().clearRoute();
                            RouteAPI.getInstance().setStartPoint(
                                    MapAPI.getInstance().getVehiclePos());
                            calculateType = (Integer) list.get(0);
                            RouteAPI.getInstance().setEndPoint(
                                    (NSLonLat) (list.get(2)));

                            if (list.get(3) instanceof NSLonLat[]) {
                                NSLonLat[] lonlatarray = (NSLonLat[]) list
                                        .get(3);
                                for (NSLonLat lonlat : lonlatarray) {
                                    RouteAPI.getInstance().addPassPoint(lonlat);
                                }
                            }
                            if (list.get(4) instanceof NSLonLat[]) {
                                NSLonLat[] avoidlineinfo = (NSLonLat[]) list
                                        .get(4);
                                for (NSLonLat avoidline : avoidlineinfo) {
                                    RouteAPI.getInstance().addAvoidPoint(
                                            avoidline);
                                }
                            }
                            // Log.e(TAG, "continueRoute开始重新计算路径");
                            int res = 0;
                            if (calculateType == 0) {
                                res = RouteAPI.getInstance().routeCalculation(
                                        -1,
                                        SettingForLikeTools
                                                .getRouteCalcMode(m_activity));
                            } else {
                                res = RouteAPI
                                        .getInstance()
                                        .httpRouteCalculation(
                                                SettingForLikeTools
                                                        .getRouteCalNetMode(m_activity));
                            }
                            // Log.e(TAG, "continueRoute路径重算结束:" + res);
                            Message msg = Message.obtain();
                            msg.arg1 = res;
                            msg.what = H_CONTINUEROUTE;
                            mHandler.sendMessage(msg);
                        } catch (Exception ex) {
                            Log.e(TAG, ex.getMessage());
                        }
                        isReCaling = false;
                    }
                }.start();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 偏离路线，重新计算路径
     */
    private void offsetGuide() {
        if (m_activity != null) {
            try {
                if (isReCaling) {
                    return;
                }
                isReCaling = true;
                final FloatValue matchAngle = new FloatValue();
                final NSLonLat matchPos = new NSLonLat();
//				final GpsInfo gpsInfo = new GpsInfo();
//				if(routeInfo==null){

//				RouteAPI.getInstance().getCurPosition(gpsInfo);
                if (gpsInfo.latitude <= 0) {
                    isReCaling = false;
                    return;
                }
                // Log.e(TAG, "位置1：" + gpsInfo.longitude + "," +
                // gpsInfo.latitude);
                NSLonLat vehiclePos = new NSLonLat();
                vehiclePos.x = gpsInfo.longitude;
                vehiclePos.y = gpsInfo.latitude;
                NSLonLat VPPos = new NSLonLat();
                boolean re = RouteAPI.getInstance().matchProc(vehiclePos, gpsInfo.angle,
                        VPPos, matchPos, matchAngle);
//				}else{
//					matchAngle.value = routeInfo.angle;
//					matchPos.x = routeInfo.vehicleLonLat.x;
//					matchPos.y = routeInfo.vehicleLonLat.y;
//				}
                // Log.e(TAG, "转换后的VP位置坐标：re:" + re + VPPos.x + "," +
                // VPPos.y);
                // Log.e(TAG, "转换后的match位置坐标：" + matchPos.x + "," +
                // matchPos.y);
                if (matchPos.x <= 0) {
                    isReCaling = false;
                    return;
                }
                Log.e(TAG, "偏航X:" + vehiclePos.x + ",Y:" + vehiclePos.y + ";matchx:" + matchPos.x + ",mathcy:" + matchPos.y);
                Log.e(TAG, "路径重算开启");
                // Log.e(TAG, "路径重算开启1");
                TTSAPI.getInstance().addPlayContent("dong3;",
                        Const.AGPRIORITY_NORMAL);
                NaviControl.this.stopRealNavi();
                // Log.e(TAG, "路径重算开启2");
                dlgRecalculateRoute = new ProgressDialog(m_activity);
                dlgRecalculateRoute
                        .setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dlgRecalculateRoute.setIndeterminate(false);
                dlgRecalculateRoute.setMessage(m_activity
                        .getString(R.string.navicontrol_calculate_path));
                dlgRecalculateRoute.setCancelable(false);
                dlgRecalculateRoute.show();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            // Log.e(TAG, "路径重算开启3");
                            RouteAPI.getInstance().clearRoute();
                            // Log.e(TAG, "路径重算开启4");
                            RouteAPI.getInstance().setStartPoint(matchPos);
                            // Log.e(TAG, "路径重算开启5");
                            float angle = -1;
                            if (gpsInfo != null) {
                                angle = matchAngle.value;
                            }
//							Log.e(TAG,"match前角度："+gpsInfo.angle+"重算用的角度："+matchAngle.value+",angle:"+angle);
                            // Log.e(TAG, "路径重算开启6");
                            if (MapAPI.getInstance().getMapView() == Const.MAP_VIEWSTATE_NORTH) {
                                // Log.e(TAG, "路径重算开启7,angle"+angle);
                                MapAPI.getInstance().setVehiclePosInfo(
                                        matchPos, angle);
                            } else {
                                // Log.e(TAG, "路径重算开启8");
                                MapAPI.getInstance().setVehiclePosInfo(
                                        matchPos, 0);
                            }
                            // Log.e(TAG, "offsetGuide开始重新计算路径");
                            int res = 0;
                            if (calculateType == 0) {
                                res = RouteAPI.getInstance().routeCalculation(
                                        angle,
                                        SettingForLikeTools
                                                .getRouteCalcMode(m_activity));
                            } else {
                                res = RouteAPI
                                        .getInstance()
                                        .httpRouteCalculation(
                                                SettingForLikeTools
                                                        .getRouteCalNetMode(m_activity));
                            }
                            // Log.e(TAG, "offsetGuide路径重算结束:" + res);
                            Message msg = Message.obtain();
                            msg.arg1 = res;
                            msg.what = H_ROUTE_RECALCULATION_FINISH;
                            mHandler.sendMessage(msg);
                            isReCaling = false;
                        } catch (Exception ex) {
                            Log.e(TAG, ex.getMessage());
                        }
                    }
                }.start();
            } catch (Exception e) {
            }
        }
    }

    public void simNaviStopedInfo() {
        this.stopSimNavi();
        if (vehiclePosition != null) {
            if (MapAPI.getInstance().getMapView() == Const.MAP_VIEWSTATE_NORTH) {
                MapAPI.getInstance().setVehiclePosInfo(vehiclePosition,
                        MapAPI.getInstance().getVehicleAngle());
            } else {
                MapAPI.getInstance().setVehiclePosInfo(vehiclePosition, 0);
            }
        }
        m_mapView.goBackCar();

        alterDialogEndSimNavi();
    }

    /**
     * 结束模拟导航  对话框
     */
    private void alterDialogEndSimNavi() {
        AlertDialog.Builder builder = new AlertDialog.Builder(m_activity);
        builder.setTitle(R.string.navicontrol_select);
        builder.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.e(TAG, "=======OnCancelListener=======");
                if (mgpsStatus != Const.GPS_STATE_CONNECT_FIXED) {
                    NaviControl.getInstance().routeInfo = null;
                }
                NaviControl.this.startNavigate();
                drawRoute();
                m_mapView.goBackCar();
            }
        });
        builder.setNegativeButton(R.string.navicontrol_deletepath,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NaviControl.this.stopRealNavi();
                        NaviControl.this.guideEnd();
                        NaviControl.this.routeLayer.deleteLayer();
                        RouteAPI.getInstance().clearRoute();
                        m_mapView.goBackCar();
                    }
                }).setPositiveButton(R.string.navicontrol_navi,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("MapActivity", "=======navigate=======");
                        if (mgpsStatus != Const.GPS_STATE_CONNECT_FIXED) {
                            NaviControl.getInstance().routeInfo = null;
                        }
                        NaviControl.this.startNavigate();
                        drawRoute();
                        m_mapView.goBackCar();
                    }
                }).show();
    }

    /**
     * 导航开始
     *
     * @param nMode
     */
    public void guideBegin() {
        updateGuideUI(true);
    }

    /**
     * @Copyright:mapabc
     * @description:刷新车道线，解决横竖屏切换时车道线不显示的问题
     * @date 2012-9-21 void
     */
    public void fleshLane() {
        m_NoCrossingLane.setImageBitmap(bmp);
        // m_CrossingLane.setImageBitmap(bmp);
        m_NaviCamera.setImageBitmap(camera_bmp);
    }

    /**
     * 导航结束
     *
     * @param nMode
     */
    public void guideEnd() {
        updateGuideUI(false);
        setNormalPoint();
    }

    /**
     * 根据导航是否开启状态更新界面
     *
     * @param bEnable 导航是否开启状态
     */
    private void updateGuideUI(boolean bEnable) {
        if (isRefresh == bEnable) {
            return;
        }
        isRefresh = bEnable;
        if (bEnable) {
            navi_turn_layout.setVisibility(View.VISIBLE);
            m_NaviTurn.setVisibility(View.VISIBLE);
            m_RemainDisUnit.setVisibility(View.VISIBLE);
            m_RemainDis.setVisibility(View.VISIBLE);
            m_NaviCurSpeed.setVisibility(View.VISIBLE); // 当前时速
            // m_CrossingLayout.setVisibility(View.VISIBLE);
//			 crossingview.setVisibility(View.VISIBLE);
            // m_NoCrossingLane.setVisibility(View.VISIBLE);
            // m_NaviCamera.setVisibility(View.VISIBLE);
        } else {
            navi_turn_layout.setVisibility(View.GONE);
            m_NaviTurn.setVisibility(View.GONE);
            m_RemainDis.setVisibility(View.INVISIBLE);
            m_RemainDisUnit.setVisibility(View.INVISIBLE);
            m_NaviCurSpeed.setVisibility(View.GONE); // 当前时速

            // m_CrossingLayout.setVisibility(View.GONE);
            crossingview.setVisibility(View.GONE);
            m_NoCrossingLane.setVisibility(View.GONE);
            // m_CrossingLane.setVisibility(View.GONE);
            m_NaviCamera.setVisibility(View.GONE);
            // MapAPI.getInstance().resetHotPoint(true);
        }
    }

    @Override
    public void onGPSInfo(GpsInfo gpsInfo, MVPSVPPosition vpPosition) {
//		 Log.e(TAG, "_________________________onGPSInfo_________________");
        Log.e(TAG, "x:" + gpsInfo.longitude + ",y:" + gpsInfo.latitude + ",matchx:" + vpPosition.pos.matchx + ",matchy:" + vpPosition.pos.matchy + ",time:" + gpsInfo.time.hour + ":" + gpsInfo.time.minute + ":" + gpsInfo.time.second);
        //获取实时速度
        Log.e(TAG, "----0GpsInfo()  speed:" + gpsInfo.speed);
        Log.i(TAG, "----0gpsInfo.speed" + gpsInfo.speed);


        overSpeednum = m_activity.getSharedPreferences("BD_OVER_SPEED_NUM", 0);
        overSpeedMax = m_activity.getSharedPreferences("BD_OVER_SPEED_MAX", 0);

        String num = overSpeednum.getString("BD_OVER_SPEED_NUM", "");
        String max = overSpeedMax.getString("BD_OVER_SPEED_MAX", "");
        if (!num.equals("") && !max.equals("")) {

            int speed = Integer.valueOf(max).intValue();
            String message = "超速报警，此时速度:" + gpsInfo.speed * 3.6 + "km/h,超速报告上限:" + speed + "km/h";


            Log.e(TAG, "----0000num0000" + num + "000max0000" + max);

            if (gpsInfo.speed * 3.6 >= speed && overSpeedsend) {


                Log.e(TAG, "----进入超速报告圈 ");
                TimerTask timerTask = new TimerTask() {
                    int i = 60;

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Message msg = new Message();
                        msg.what = i--;
                        mHandler.sendMessage(msg);
                        Log.e(TAG, "----0000timerTask  倒计时:" + i);
                    }
                };

                Message msg = Message.obtain();
                msg.what = OVER_SPEED_DIALOG;
                mHandler.sendMessage(msg);
            /*超速发送报文方法*/
                try {
                    manager.sendSMSCmdBDV21(num, 1,
                            Utils.checkMsg(message), "N", message);
                } catch (BDUnknownException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (BDParameterException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                timer.schedule(timerTask, 0, 1000);
            }

        }


        if (disPatchGpsInfo == null) {
            disPatchGpsInfo = DisPatchInfo.getInstance();
        }
        if (gpsInfo.latitude != 0.0 && gpsInfo.longitude != 0.0) {
            haveGPS = true;
        } else {
            haveGPS = false;
        }
        disPatchGpsInfo.disPatchGpsInfo(gpsInfo);
        if (gpsInfo != null) {
            if (naviStatus == NAVI_STATUS_REALNAVI) {
                Message msg = Message.obtain();
                msg.what = H_UPDATE_UI_SPEED;
                String numString = m_activity.getString(R.string.speed_coefficient);
                float xnum = Float.parseFloat(numString);
                Log.i(TAG, "xnum =" + xnum);
                Log.i(TAG, "----1gpsinfo界面更新原始速度 speed = " + gpsInfo.speed);
                msg.arg1 = (int) (xnum * gpsInfo.speed);
                Log.i(TAG, "----1gpsinfo界面更新速度*3.6 speed = " + xnum * gpsInfo.speed);
//				msg.arg1 = (int) (1.8*3.6 * gpsInfo.speed);
                mHandler.sendMessage(msg);
                if (firstfixed_recal == 1) {
                    Message msg1 = Message.obtain();
                    msg1.what = H_GPS_FIRST_FIXED;
                    msg1.obj = vpPosition.pos;
                    mHandler.sendMessage(msg1);
                }
            }
            this.gpsInfo = gpsInfo;

            // if(MyWZTListener.LinkState == 1){
            // WZTManager.getGPSInfo(gpsInfo,vpPosition);
            // }
        }
    }

    @Override
    public void onGPSNaviMode(int gpsNaviMode, int statusEx) {
        switch (gpsNaviMode) {
            case Const.GPS_NAVI_MODE_STOP:// 导航停止

                break;
            case Const.GPS_NAVI_MODE_START:// 导航开始
                if (isTalkStartNavi) {
                    isTalkStartNavi = false;
                    TTSAPI.getInstance().addPlayContent(
                            m_activity.getString(R.string.navicontrol_startnavi),
                            Const.AGPRIORITY_CRITICAL);
                }
                break;
            case Const.GPS_NAVI_MODE_ARRIVE_MID:// 到达途经点
                RouteAPI.getInstance().deletePassPoint(statusEx);
                break;
            case Const.GPS_NAVI_MODE_ARRIVE_DEST:// 到达终点

                this.mHandler.sendEmptyMessage(H_ARRIVE_END_POINT);
                break;
            case Const.GPS_NAVI_MODE_LEEWAY:// 偏离路径
                this.mHandler.sendEmptyMessage(H_ROUTE_RECALCULATION);
                break;
        }
    }


    /**
     * 为什么  一进来状态为3
     */
    @Override
    public void onGPSStatus(int gpsStatus) {
        // Log.e(TAG, "onGPSStatus:"+gpsStatus);
        Log.e(TAG, "gpsStatus:" + gpsStatus);
        MapAPI.getInstance().setVehicleGPS(gpsStatus);
        if (gpsStatus != 3) {
            gpsStatus = 2;
        }
        if (this.mgpsStatus != gpsStatus) {
            mgpsStatus = gpsStatus;
            // Log.e(TAG, "mgpsStatus:"+mgpsStatus);
            if (mgpsStatus == Const.GPS_STATE_CONNECT_FIXED) {

                //判断状态
                //if (myapp.isLocationed) {

                if (!IS_SPEAK_HAVEGPS) {
                    TTSAPI
                            .getInstance()
                            .addPlayContent(
                                    m_activity
                                            .getString(R.string.navicontrol_gpsfixed),
                                    Const.AGPRIORITY_CRITICAL);
                    IS_SPEAK_HAVEGPS = true;
                }
                firstfixed_recal = 1;
//				}else {
//					 MapAPI.getInstance().setVehicleGPS(2);
//				}

            } else {
                firstfixed_recal = 0;
                if (m_activity != null) {
                    try {
                        m_activity.gpsControl.refreshStatus();// 刷新gps信号
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            Message msg = new Message();
            msg.what = CAR_GPS_SATUS;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onPlayNaviMessage(String message, boolean gpsNavi) {
        TTSAPI.getInstance().addPlayContent(message, Const.AGPRIORITY_NORMAL);
    }

    @Override
    public void onRouteNaviInfo(GPSRouteInfo routeInfo, boolean gpsNavi) {
        Log.e(TAG, "matchx1:" + routeInfo.vehicleLonLat.x + ",matchy1:" + routeInfo.vehicleLonLat.y);
        Log.i(TAG, "----0GPSRouteInfo()超速报警速度  speed:" + routeInfo.speed
                + "; speedLimit:" + routeInfo.speedLimit
                + "; overSpeed:" + routeInfo.overSpeed);
        if (this.naviStatus == NAVI_STATUS_SIMNAVI) {
            if (gpsNavi) {
                return;
            }
            routeInfo.speed = 0f;
        }

        // Log.e(TAG,
        // "=================导航回调信息===================gpsNavi:"+gpsNavi);
        // Log.e(TAG,
        // "=================导航回调信息===================routeInfo.vehicleLonLat:"+routeInfo.vehicleLonLat.x+","+routeInfo.vehicleLonLat.y);
//		 Log.e(TAG,
//		 "=================导航回调,电子眼距离：==================="+routeInfo.eyeDist);
        // Log.e(TAG,
        // "=================导航回调,电子眼类型：==================="+routeInfo.laneInfo);
        // Log.e(TAG,
        // "=================导航回调,车道线：==================="+routeInfo.laneInfo);
//		 Log.e(TAG,
//		 "=================导航回调,电子眼位置：==================="+routeInfo.eyePos.x+":::"+routeInfo.eyePos.y);
//		Log.e(TAG, "=======车道线========"+routeInfo.laneInfo);
        Message msg = Message.obtain();
        msg.obj = routeInfo;
        msg.what = H_UPDATE_UI_FOR_NAVI;
        this.mHandler.sendMessage(msg);

        if (disPatchGpsInfo == null) {
            disPatchGpsInfo = DisPatchInfo.getInstance();
        }
        disPatchGpsInfo.disPatchRouteInfo(routeInfo, gpsNavi);
    }

    @Override
    public void onSimNaviMode(int simNaviMode) {
        // TODO Auto-generated method stub
        Log.e(TAG, "_________________________OnSimNaviMode:" + simNaviMode);
        if (simNaviMode == Const.SIM_NAVI_MODE_STOP) {
            Message msg = Message.obtain();
            msg.what = H_SIMNAVI_STOP;
            this.mHandler.sendMessage(msg);
            TTSAPI.getInstance().stopPlay(3);
            String simnavistop = m_activity
                    .getString(R.string.navicontrol_simnavistoped);
            TTSAPI.getInstance().addPlayContent(simnavistop,
                    Const.AGPRIORITY_CRITICAL);
        } else if (simNaviMode == Const.SIM_NAVI_MODE_START) {
            NaviControl.this.isAutoStopSimNavi = true;
        }
    }

    /**
     * 计算路径
     */
    public void calculatePath(final NSLonLat lonlat, final int type) {
        blConfirm = false;
        if (mgpsStatus != 3) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    this.m_activity);
            builder.setTitle(R.string.common_tip);
            builder.setMessage(m_activity
                    .getString(R.string.navicontrol_no_gps_location));

            builder.setPositiveButton(R.string.common_confirm,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (blConfirm) {// 阻止连续点击确定
                                return;
                            }
                            calculatePathing(lonlat, type);
                        }
                    });
            builder.setNegativeButton(R.string.common_cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            dialog = builder.create();
            dialog.show();
            dialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                }
            });
        } else {
            calculatePathing(lonlat, type);
        }
    }

    public void drawRoute() {
        routeLayer.deleteLayer();
        int count = RouteAPI.getInstance().getSegmentCount();
        float[][] lons = new float[count][];
        float[][] lats = new float[count][];
        int alllen = 0;
        for (int i = 0; i < count; i++) {
            RouteSegInfo segInfo = new RouteSegInfo();
            if (RouteAPI.getInstance().getSegmentInfo(i, segInfo)) {
                int len = segInfo.lats.length;
                alllen += len;
                lats[i] = new float[len];
                lons[i] = new float[len];
                System.arraycopy(segInfo.lons, 0, lons[i], 0, len);
                System.arraycopy(segInfo.lats, 0, lats[i], 0, len);
            }
        }
        if (alllen != 0) {
            int seglen = 0;
            float[] lonarray = new float[alllen];
            float[] latarray = new float[alllen];
            for (int i = 0; i < count; i++) {
                System.arraycopy(lons[i], 0, lonarray, seglen, lons[i].length);
                System.arraycopy(lats[i], 0, latarray, seglen, lats[i].length);
                seglen += lats[i].length;
            }
            routeLayer.addRoute(lonarray, latarray, 199,
                    RouteLayer.PAINTER_ROUTE_LINE);
        }
        routeLayer.addRoutePos(RouteAPI.getInstance().getStartPoint(),
                routeLayer.ROUTE_POINT_START);
        int passPointCount = RouteAPI.getInstance().getPassPointCount();
        NSLonLat lonlat[] = RouteAPI.getInstance().getPassPoints();
        for (int i = 0; i < passPointCount; i++) {
            routeLayer.addRoutePos(lonlat[i], RouteLayer.ROUTE_POINT_PASS1 + i);
        }
        int avoidPointCount = RouteAPI.getInstance().getAvoidPointCount();
        if (avoidPointCount > 0) {
            NSLonLat[] lonlatarray = RouteAPI.getInstance().getAvoidPoints();
            for (int i = 0; i < avoidPointCount; i++) {
                routeLayer.addRoutePos(lonlatarray[i],
                        RouteLayer.ROUTE_POINT_AVOID + i);
            }
        }
        routeLayer.addRoutePos(RouteAPI.getInstance().getEndPoint(),
                routeLayer.ROUTE_POINT_END);
        segIndex = -1;

    }

    // public void drawRoute1() {
    // routeLayer.deleteLayer();
    // int count = RouteAPI.getInstance().getSegmentCount();
    // for (int i = 0; i < count; i++) {
    // RouteSegInfo segInfo = new RouteSegInfo();
    // if (RouteAPI.getInstance().getSegmentInfo(i, segInfo)) {
    // routeLayer.addRoute(segInfo.lons, segInfo.lats, 200 + i,
    // RouteLayer.PAINTER_ROUTE_LINE);
    // }
    // }
    // routeLayer.addRoutePos(RouteAPI.getInstance().getStartPoint(),
    // routeLayer.ROUTE_POINT_START);
    // int passPointCount = RouteAPI.getInstance().getPassPointCount();
    // NSLonLat lonlat[] = RouteAPI.getInstance().getPassPoints();
    // for (int i = 0; i < passPointCount; i++) {
    // routeLayer.addRoutePos(lonlat[i], RouteLayer.ROUTE_POINT_PASS1 + i);
    // }
    // int avoidPointCount = RouteAPI.getInstance().getAvoidPointCount();
    // if(avoidPointCount>0){
    // NSLonLat[] lonlatarray = RouteAPI.getInstance().getAvoidPoints();
    // for (int i = 0; i < avoidPointCount; i++) {
    // routeLayer.addRoutePos(lonlatarray[i], RouteLayer.ROUTE_POINT_AVOID + i);
    // }
    // }
    // routeLayer.addRoutePos(RouteAPI.getInstance().getEndPoint(),
    // routeLayer.ROUTE_POINT_END);
    // segIndex = -1;
    // }
    public void stopSimNavi() {
        if (this.naviStatus == NAVI_STATUS_SIMNAVI) {
            NaviControl.this.isAutoStopSimNavi = false;
            RouteAPI.getInstance().stopSimNavi();
            if (vehiclePosition != null) {
                MapAPI.getInstance().setVehiclePosInfo(vehiclePosition,
                        MapAPI.getInstance().getVehicleAngle());
            } else {
                NSLonLat lonlat = RouteAPI.getInstance().getStartPoint();
                MapAPI.getInstance().setVehiclePosInfo(lonlat,
                        MapAPI.getInstance().getVehicleAngle());
            }
            this.naviStatus = NAVI_STATUS_STOPED;
            this.guideEnd();
        }
    }

    public void stopRealNavi() {
        if (this.naviStatus == NAVI_STATUS_REALNAVI) {
            RouteAPI.getInstance().stopGPSNavi();
            this.naviStatus = NAVI_STATUS_STOPED;
            this.guideEnd();
        }
    }

    public void startNavigate() {
        this.stopSimNavi();
        if (RouteAPI.getInstance().isRouteValid()) {
            Log.e(TAG, "ceshi3");
            Log.e(TAG, "存在线路");
            if (this.naviStatus != NAVI_STATUS_REALNAVI) {
                if (RouteAPI.getInstance().startGPSNavi()) {
                    this.naviStatus = NAVI_STATUS_REALNAVI;
                    Log.e(TAG, "启动导航");
                } else {
                    return;
                }
            }
        }
        Log.e(TAG, "ceshi4");
        showNaviInfo();
    }

    /**
     * 展示导航信息
     */
    public void showNaviInfo() {
        if (routeInfo == null || routeInfo.remainDis == 0) {
            Log.e(TAG, "ceshi5");
            routeInfo = new GPSRouteInfo();
            RouteSegInfo segInfo = new RouteSegInfo();
            RouteAPI.getInstance().getSegmentInfo(0, segInfo);
            IntValue distance = new IntValue();
            IntValue time = new IntValue();
            RouteAPI.getInstance().getDistanceAndTime(distance, time);
            routeInfo.remainDis = distance.value;
            routeInfo.segRemainDis = segInfo.len;
            routeInfo.naviAction = segInfo.naviAction;
            if (RouteAPI.getInstance().getSegmentCount() > 1) {
                RouteAPI.getInstance().getSegmentInfo(1, segInfo);
            }
            routeInfo.nextRoadName = segInfo.segName;
            Log.e(TAG, "ceshi7");
        }
        Log.e(TAG, "ceshi6");
        Log.e(TAG, "routeInfo.remainDis:" + routeInfo.remainDis);
        this.updateUIForNavi(routeInfo);
    }

    public void showNaviInfo1() {
        routeInfo = new GPSRouteInfo();
        RouteSegInfo segInfo = new RouteSegInfo();
        RouteAPI.getInstance().getSegmentInfo(0, segInfo);
        IntValue distance = new IntValue();
        IntValue time = new IntValue();
        RouteAPI.getInstance().getDistanceAndTime(distance, time);
        routeInfo.remainDis = distance.value;
        routeInfo.segRemainDis = segInfo.len;
        routeInfo.naviAction = segInfo.naviAction;
        if (RouteAPI.getInstance().getSegmentCount() > 1) {
            RouteAPI.getInstance().getSegmentInfo(1, segInfo);
        }
        routeInfo.nextRoadName = segInfo.segName;
        this.updateUIForNavi(routeInfo);
    }


    /**
     * 规划路径
     *
     * @param lonlat
     * @param type
     */
    public void calculatePathing(final NSLonLat lonlat, final int type) {
        blConfirm = true;
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        NaviControl.this.stopRealNavi();
        NaviControl.this.stopSimNavi();
        RouteAPI.getInstance().clearRoute();
        routeLayer.deleteLayer();
        pdg = new ProgressDialog(m_activity);
        pdg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdg.setIndeterminate(false);
        pdg.setCancelable(false);
        pdg.setMessage(m_activity
                .getString(R.string.navicontrol_calculate_path));
        pdg.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (RouteAPI.getInstance().isRouteValid()) {
                    if (NaviControl.this.naviStatus != NAVI_STATUS_REALNAVI) {
                        startNavigate();
                    }
                }
            }
        });
        pdg.show();
        Log.i("calculatpath", "calculatpath");
        m_activity.resetStatus();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                RouteAPI.getInstance().deletePassPoint(-1);
                RouteAPI.getInstance().deleteAvoidPoint(-1);
                // if (mgpsStatus == Const.GPS_STATE_CONNECT_FIXED
                // || RouteAPI.getInstance().getStartPoint() == null) {
                NSLonLat mlonlat = MapAPI.getInstance().getVehiclePos();
                RouteAPI.getInstance().setStartPoint(mlonlat);
                // }
                String roadName = "";
                RouteAPI.getInstance().setEndPoint(lonlat);
                float angle = MapAPI.getInstance().getVehicleAngle();
                if (angle == 0) {
                    angle = -1;
                }
                int res = 0;
                if (type == 0) {
                    res = RouteAPI.getInstance().routeCalculation(angle,
                            SettingForLikeTools.getRouteCalcMode(m_activity));
                } else {
                    res = RouteAPI.getInstance().httpRouteCalculation(
                            SettingForLikeTools.getRouteCalNetMode(m_activity));
                }
                Log.e(TAG, "calculatePathing RES:" + res);
                if (res == 1) {
                    isTalkStartNavi = true;
                    calculateType = type;
                    Message msg = Message.obtain();
                    msg.what = H_FIN_DPATH_SUCCESS;
                    msg.obj = lonlat;
                    mHandler.sendMessage(msg);
                } else {
                    Message msg = Message.obtain();
                    msg.what = H_FIND_PATH_FAILED;
                    mHandler.sendMessage(msg);
                }
                pdg.cancel();
            }
        });
    }

    private String[] getLaneName(GPSRouteInfo info) {
        String infos = info.laneInfo;
        Log.e(TAG, "________infos:" + infos + ",naviAction:" + info.naviAction);
        String[] lanes = new String[]{};
        if (!infos.equals("")) {
            if (infos.contains(",")) {
                lanes = infos.split(",");
            } else {
                lanes = new String[]{infos};
            }

        }
        return lanes;
    }

    private void updateLane(GPSRouteInfo info) {
        try {
            Bitmap[] bmpsBitmaps = getBitmaps(getLaneName(info));
            bmp = compose(bmpsBitmaps);
            m_NoCrossingLane.setImageBitmap(bmp);
            // m_CrossingLane.setImageBitmap(bmp);
        } catch (Exception ex) {
            Log.e(TAG, "error", ex);
        }
    }

    private Bitmap[] getBitmaps(String[] laneNames) {
        int length = laneNames.length;
        Bitmap[] bmps = new Bitmap[length];
        boolean haveWhiteLane = false;
        for (int i = 0; i < length; i++) {
            int laneNameLength = laneNames[i].length();
            if (laneNameLength > 0) {
                // 如果有车道信息，路径使用灰色车道(0为灰色车道路径,1白色车道，2绿色车道)
                int pathIndex = 0;
                if (laneNameLength > 1) {
                    // 白色
                    pathIndex = 1;
                    // 判断是否左转
                    String lane = laneNames[i].substring(2, 3);
                    if (lane.equals("0")) {
                        // 直行

                    } else if (lane.equals("1")) {
                        // 左转
                        if (length > 1 && i == 0 && laneNames[1].length() > 1) {
                            pathIndex = 2;
                        }
                    } else if (lane.equals("2")) {
                        // 右转
                        if (length > 1 && i == length - 1
                                && laneNames[length - 2].length() > 1) {
                            pathIndex = 2;
                        }
                    } else if (lane.equals("3")) {
                        // 左掉头
                        if (length > 1 && i == 0 && laneNames[1].length() > 1) {
                            pathIndex = 2;
                        }
                    } else if (lane.equals("4")) {
                        // 右掉头
                        if (length > 1 && i == length - 1
                                && laneNames[length - 2].length() > 1) {
                            pathIndex = 2;
                        }
                    }
                }
                if (pathIndex == 2) {
                    // 如果是绿色车道的话，只拷贝白色车道名称的第一位做为绿色车道名称
                    bmps[i] = getBitmap(laneNames[i].substring(0, 1), pathIndex);
                } else {
                    // 如果是白色或者灰色车道，直接在名称后面加.bmp
                    bmps[i] = getBitmap(laneNames[i], pathIndex);
                }
            }
        }
        return bmps;
    }

    private Bitmap getBitmap(String filename, int pathIndex) {
        try {
            FileInputStream fs = searchFile(filename, pathIndex);
            Bitmap bmp = BitmapFactory.decodeStream(fs);
            fs.close();
            return bmp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private FileInputStream searchFile(String fileName, int pathIndex) {
        String path = "";
        if (pathIndex == 0) {
            path = SysParameterManager.getBasePath()
                    + "/MapABC/Data/res/lane/gray/" + fileName + ".bmp";
        } else if (pathIndex == 2) {
            path = SysParameterManager.getBasePath()
                    + "/MapABC/Data/res/lane/green/" + fileName + ".bmp";
        } else {
            path = SysParameterManager.getBasePath()
                    + "/MapABC/Data/res/lane/white/" + fileName + ".bmp";
        }
        File file = new File(path);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (Exception ex) {

            }
        }
        return null;
    }

    /**
     * @param bmps
     * @return Bitmap
     * @Copyright:mapabc
     * @description:把多张图片合成一张图片
     * @author fei.zhan
     * @date 2012-9-14
     */
    private Bitmap compose(Bitmap[] bmps) {
        int length = bmps.length;
        if (length == 0)
            return null;
        int width = 0;
        int height = 0;
        for (int i = 0; i < length; i++) {
            if (bmps[i] != null) {
                width = bmps[i].getWidth() * length;
                height = bmps[i].getHeight() * length;
                break;
            }
            if (i == length - 1) {
                return null;
            }
        }
        Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        for (int i = 0; i < length; i++) {
            if (bmps[i] != null) {
                canvas.drawBitmap(bmps[i], bmps[i].getWidth() * i, 0, null);
            }
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);// 保存
        canvas.restore();// 存储
        return bmp;

    }

    private void addNaviCamera(GPSRouteInfo info) {
        NSLonLat lonLat = info.eyePos;
        RouteLayer rl = new RouteLayer();
        rl.addEleEye(lonLat, RouteLayer.ELE_EYE);
    }

    public void setNormalPoint() {
        boolean res = false;
        int screenH = ToolsUtils.getCurScreenHeight(m_activity);
        if (MapAPI.getInstance().getMapView() == Const.MAP_VIEWSTATE_3D) {
            if (crossingview.getVisibility() == View.GONE) {
                res = MapAPI.getInstance().setHotPointPosDisp((short) 0,
                        (short) (screenH / 4));
            } else {
                setHotPoint();
            }
        } else {
            if (crossingview.getVisibility() == View.GONE) {
                res = MapAPI.getInstance().setHotPointPosDisp((short) 0,
                        (short) 0);
            } else {
                setHotPoint();
            }
        }
    }

    public void setHotPoint() {
        int screenW = ToolsUtils.getCurScreenWidth(m_activity);
        int screenH = ToolsUtils.getCurScreenHeight(m_activity);
        if (MapAPI.getInstance().getMapView() == Const.MAP_VIEWSTATE_3D) {
            if (ToolsUtils.isLand(m_activity)
                    && crossingview.getVisibility() != View.GONE) {
                MapAPI.getInstance().setHotPointPosDisp((short) (-screenW / 4),
                        (short) (screenH / 4));
            } else {
                MapAPI.getInstance().setHotPointPosDisp((short) 0,
                        (short) (screenH / 4));
            }
        } else {
            if (ToolsUtils.isLand(m_activity)) {
                MapAPI.getInstance().setHotPointPosDisp((short) (-screenW / 4),
                        (short) 0);
            } else {
                MapAPI.getInstance().setHotPointPosDisp((short) 0,
                        (short) (screenH / 4));
            }
        }
    }

    /**
     * 结束导航对话框
     */
    protected void alterDialogEndNavi() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(m_activity);
        builder1.setCancelable(false);
        builder1.setTitle(R.string.navigate_end_title);
        builder1.setMessage(R.string.navigate_end_message);
        builder1.setPositiveButton("确定", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                RouteAPI.getInstance().stopGPSNavi();
                RouteAPI.getInstance().clearRoute();
                routeLayer.deleteLayer();
                NaviControl.getInstance().guideEnd();

                isFirst = true;

            }
        });
        builder1.show();
    }

    protected void showMaxSpeed() {
        AlertDialog.Builder builder = new Builder(m_activity);
        builder.setTitle("超速报警");
        builder.setMessage("您的当前速度已经超出预设速度！请减速！");
        builder.setNegativeButton("好的", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showFailedMessage() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(m_activity);
        builder1.setTitle(R.string.navicontrol_find_path_failed);
        builder1.setMessage(R.string.navicontrol_not_find_path);
        builder1.show();
    }

    private void showReCalMessage() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(m_activity);
        builder1.setTitle(R.string.navicontrol_find_path_failed);
        builder1.setMessage(R.string.navicontrol_recalculate_path);
        builder1.setNegativeButton(R.string.common_btn_negative,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder1.setPositiveButton(R.string.common_btn_positive,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        calculateType = 0;
                        offsetGuide();
                    }
                });
        builder1.setCancelable(false);
        builder1.show();
    }

    private CrossingZoomListener m_CrossingZoomListener = new CrossingZoomListener() {

        @Override
        public void onStatusedChange(int paramInt) {
            if (paramInt == CrossingZoomListener.CROSSING_ZOOM_VISIBLE_STATUS) {
                setHotPoint();
            } else {
                setNormalPoint();
            }

        }
    };

    public void hideLineInfo() {
        fleshLane();
    }

    public int getCalculateType() {
        return calculateType;
    }

    @Override
    public void draw(Canvas canvas, int x, int y) {
        // TODO Auto-generated method stub
        if (bmp != null) {
            // canvas.drawBitmap(bmp, x,y+NaviStudioActivity.statusBarHeight+18,
            // new Paint());
            canvas.drawBitmap(bmp, x, y, new Paint());
        }

    }

//	@Override
//	public void onSensorChanged(SensorEvent event) {
//		if(mgpsStatus == 3){
//			return ;
//		}
//		Log.e(TAG, "  degree 0: " + event.values[0]);
//		float degree = event.values[0];
//		float vehicleAngle = MapAPI.getInstance().getVehicleAngle();
//		if(Math.abs(vehicleAngle - degree) > 11.25){
//			updateCarStatus(degree,MapAPI.getInstance().getVehiclePos());
//			 m_mapView.invalidate();
//		}
//	}
}
