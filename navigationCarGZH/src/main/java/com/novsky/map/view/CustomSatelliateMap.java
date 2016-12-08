package com.novsky.map.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.bd.comm.protocal.BDSatellite;
import com.bd.comm.protocal.GPSatellite;
import com.novsky.map.util.Utils;

import java.util.List;

/**
 * 自定义卫星星图
 * 
 * @author llg
 */
public class CustomSatelliateMap extends View {

	private Bitmap bitmap2=null;
	private Context mContext=null;
	//private static int MAX_VISUAL_SATELLIATE_NUM = 16;
	private static int MAX_VISUAL_SATELLIATE_NUM = 26;
	private float[][] mCoordinateArr = new float[MAX_VISUAL_SATELLIATE_NUM][2];
	private String[] mSatelliatePRN = new String[MAX_VISUAL_SATELLIATE_NUM];
	private boolean[] mSatelliatePixArr=new boolean[MAX_VISUAL_SATELLIATE_NUM];
	/**
	 * 视野卫星图的中心点，以及半径
	 */
	private float mCenterX = 0, mCenterY = 0, R = 0,mStopY=0;;
    private int bitMapWidth=0,bitMapHeight=0, width=0, height=0;
	private float left=0,top=0;
	private int value=0;

	public CustomSatelliateMap(Context context) {
		super(context);
		this.mContext=context;
	}

	public CustomSatelliateMap(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext=context;
	}

	public CustomSatelliateMap(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext=context;
		bitmap2=((BitmapDrawable)mContext.getResources().getDrawable(com.mapabc.android.activity.R.drawable.statillate_map)).getBitmap();
		//bitMapWidth=bitmap2.getWidth();
		//bitMapHeight=bitmap2.getHeight();

		bitMapWidth=bitmap2.getWidth();
		bitMapHeight=bitmap2.getHeight();

		// 初始化数据
		for (int i = 0; i < MAX_VISUAL_SATELLIATE_NUM; i++) {
			mCoordinateArr[i][0] = -100;
			mCoordinateArr[i][1] = -100;
			mSatelliatePRN[i] = "";
			mSatelliatePixArr[i]=false;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/*绘制可见卫星图*/
		Paint paint=new Paint();
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		//left=(width-bitMapWidth)/2.0f;
		//top=(height-bitMapHeight)/2.0f;
		left=(width-bitMapWidth)/2.0f;
		top=(height-bitMapHeight)/2.0f;
		mCenterX=width/2;
		mCenterY=height/2;
		//R = mCenterY-top-Utils.dp2pixel(15);
		R = mCenterY-top-Utils.dp2pixel(70);//星图离散半径
		canvas.drawBitmap(bitmap2,left, top, paint);
		//Bitmap mBitmap=((BitmapDrawable)mContext.getResources().getDrawable(com.ns.sms.R.drawable.satellite_number_bg)).getBitmap();
		//绘制可见星图的卫星
		for (int i = 0; i < MAX_VISUAL_SATELLIATE_NUM; i++){

			String prn=(!"".equals(mSatelliatePRN[i]))?mSatelliatePRN[i]:"0";
			Integer prnInt = Integer.valueOf(prn);
			paint.setStyle(Style.FILL);
			paint.setColor(Color.WHITE);
			paint.setAntiAlias(true);
			paint.setTextSize(Utils.dp2pixel(20));// 卫星号数字体大小
			if (prnInt>=160){
				//paint.setColor(Color.GREEN);
				paint.setColor(Color.BLUE);
			}else {
				paint.setColor(Color.RED);
			}
			//画圆
			canvas.drawCircle(mCoordinateArr[i][0], mCoordinateArr[i][1],Utils.dp2pixel(15), paint);//小星半径

			//画矩形
			//canvas.drawRect(Utils.dp2pixel(5), Utils.dp2pixel(5), Utils.dp2pixel(20), Utils.dp2pixel(20), paint);
			//canvas.drawRect(mCoordinateArr[i][0], mCoordinateArr[i][1], Utils.dp2pixel(10), Utils.dp2pixel(10), paint);
			//canvas.drawBitmap(mBitmap, mCoordinateArr[i][0]-Utils.dp2pixel(17), mCoordinateArr[i][1]-Utils.dp2pixel(12), paint);
			if(mSatelliatePixArr[i]){
				//paint.setColor(Color.GREEN);
				paint.setColor(Color.BLACK);
			}else{
				//paint.setColor(mContext.getResources().getColor(com.ns.sms.R.color.BD_TAB_COLOR));
				paint.setColor(Color.WHITE);
			}

			float x=0;
			if(prnInt >=100){
				//x=mCoordinateArr[i][0] - Utils.dp2pixel(12);
				x=mCoordinateArr[i][0] - Utils.dp2pixel(5);
			}else if(prnInt >=10){
				x=mCoordinateArr[i][0] - Utils.dp2pixel(10);
			}else{
				x=mCoordinateArr[i][0] - Utils.dp2pixel(5);
			}
			int mSatelliatePRN_No = prnInt>=160?(prnInt-160):prnInt;
			//canvas.drawText(mSatelliatePRN[i],x,mCoordinateArr[i][1] + Utils.dp2pixel(5), paint);
			canvas.drawText(mSatelliatePRN_No+"",x,mCoordinateArr[i][1] + Utils.dp2pixel(8), paint);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//width = MeasureSpec.getSize(widthMeasureSpec);
		//height = MeasureSpec.getSize(heightMeasureSpec);

		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);

		Log.i("MAP", "width="+width+",mCenterX="+mCenterX+",mCenterY="+mCenterY+",R="+R);
	}

	
	public void showMap(List<GPSatellite> list) {
		int index=0;
		for (int i = 0; i < MAX_VISUAL_SATELLIATE_NUM; i++) {
			if (i < list.size()) {
				GPSatellite mGPSatellite = list.get(i);
				if(mGPSatellite.getSnr()>0){
					double r = R * (1.0 - (mGPSatellite.getElevation() / 90.0)); // 高度角
					double x = mCenterX+ (r * Math.sin(2.0 * Math.PI
									* mGPSatellite.getAzimuth() / 360.0)); // 方位角
					double y = mCenterY- (r * Math.cos(2.0 * Math.PI
									* mGPSatellite.getAzimuth() / 360.0));
					mCoordinateArr[index][0] = (float) x;
					mCoordinateArr[index][1] = (float) y;
					if(mGPSatellite.getPrn()>=160){
						mSatelliatePRN[index] = (mGPSatellite.getPrn()-160+160)+ "";
						//mSatelliatePRN[index] = (mGPSatellite.getPrn()-160)+ "";
					}else{
						mSatelliatePRN[index] = mGPSatellite.getPrn()+ "";	
					}
					mSatelliatePixArr[index]=mGPSatellite.usedInFix();
					index++;
				}
			} else {
				mCoordinateArr[i][0] = -100;
				mCoordinateArr[i][1] = -100;
				mSatelliatePRN[i] = "";
			}
			Log.i("CustomSatelliateMap", mCoordinateArr[i][0] + ","
					+ mCoordinateArr[i][1] + "," + mSatelliatePRN[i]);
		}
		invalidate();
	}
	public void showMap2(List<BDSatellite> list) {
		int index=0;
		for (int i = 0; i < MAX_VISUAL_SATELLIATE_NUM; i++) {
			if (i < list.size()) {
				BDSatellite mGPSatellite = list.get(i);
				if(mGPSatellite.getSnr()>0){
					double r = R * (1.0 - (mGPSatellite.getElevation() / 90.0)); // 高度角
					double x = mCenterX+ (r * Math.sin(2.0 * Math.PI
									* mGPSatellite.getAzimuth() / 360.0)); // 方位角
					double y = mCenterY- (r * Math.cos(2.0 * Math.PI
									* mGPSatellite.getAzimuth() / 360.0));
					mCoordinateArr[index][0] = (float) x;
					mCoordinateArr[index][1] = (float) y;
					if(mGPSatellite.getPrn()>=160){
						mSatelliatePRN[index] = (mGPSatellite.getPrn()-160+160)+ "";
						//mSatelliatePRN[index] = (mGPSatellite.getPrn()-160)+ "";
					}else{
						mSatelliatePRN[index] = mGPSatellite.getPrn()+ "";
					}
					mSatelliatePixArr[index]=mGPSatellite.usedInFix();
					index++;
				}
			} else {
				mCoordinateArr[i][0] = -100;
				mCoordinateArr[i][1] = -100;
				mSatelliatePRN[i] = "";
			}
			Log.i("CustomSatelliateMap", mCoordinateArr[i][0] + ","
					+ mCoordinateArr[i][1] + "," + mSatelliatePRN[i]);
		}
		invalidate();
	}
}
