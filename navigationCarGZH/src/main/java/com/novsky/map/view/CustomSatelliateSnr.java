package com.novsky.map.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.bd.comm.protocal.BDSatellite;
import com.bd.comm.protocal.GPSatellite;
import com.novsky.map.util.Utils;

import java.util.List;

/**
 * 自定义卫星星图
 * @author llg
 */
public class CustomSatelliateSnr extends View {
	
	private Context mContext=null;
	
	//private static int MAX_VISUAL_SATELLIATE_NUM = 16;
	private static int MAX_VISUAL_SATELLIATE_NUM = 26;

	private String[] mSatelliatePRN = new String[MAX_VISUAL_SATELLIATE_NUM];//卫星号数

	private float[][] snrRect=new float[MAX_VISUAL_SATELLIATE_NUM][4];
	
    private float[][] prnPixelArr=new float[MAX_VISUAL_SATELLIATE_NUM][2];//
    
    private float[][] snrPixelArr=new float[MAX_VISUAL_SATELLIATE_NUM][3];//载噪比值
    
	private float mStopY=0;

	//private int gapValue=10;
	private int gapValue=10;//柱状图宽度

	//private Bitmap   bitmap2=null;
	
	private int
			bitMapWidth=0,bitMapHeight=0,
			width=0, height=0;
	
	private float left=0,top=0;

	
	public CustomSatelliateSnr(Context context) {
		super(context);
	}

	
	public CustomSatelliateSnr(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	public CustomSatelliateSnr(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext=context;

		Resources resources = this.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		float density1 = dm.density;
		int width3 = dm.widthPixels;
		int height3 = dm.heightPixels;

		// 初始化数据
		initData();
		bitMapWidth=width3-600;
		bitMapHeight=height3/2;

	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		for (int i = 0; i < MAX_VISUAL_SATELLIATE_NUM; i++) {
			mSatelliatePRN[i] = "";
			snrRect[i][0]=-100;
			snrRect[i][1]=-100;
			snrRect[i][2]=-100;
			snrRect[i][3]=-100;
			prnPixelArr[i][0]=-100;
			prnPixelArr[i][1]=-100;
			snrPixelArr[i][0]=-100;
			snrPixelArr[i][1]=-100;
			snrPixelArr[i][2]=-100;
		}
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/*绘制可见卫星图*/
		Paint paint=new Paint();
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(1);
		/*绘制载噪比图*/
		if (height > width) {
			mStopY=(height / 2) + Utils.dp2pixel(30);
		} else if (height < width) {
			mStopY=(float)((height / 2 - (height / 4)) * (4.0 / 3.0));
		}
		//left=(width-bitMapWidth)/2.0f;
		left=(width-bitMapWidth)/2.0f+Utils.dp2pixel(150);//第一个柱状图距离左边界距离
		//top=(height-bitMapHeight)/2.0f-Utils.dp2pixel(0);
		top=(height-bitMapHeight)/2.0f-Utils.dp2pixel(20);
		//canvas.drawBitmap(bitmap2,left, top, paint);
		for(int i=0;i<MAX_VISUAL_SATELLIATE_NUM;i++){

			String prn=(!"".equals(mSatelliatePRN[i]))?mSatelliatePRN[i]:"0";
			Integer prnInt = Integer.valueOf(prn);

			if (prnInt>=160){
				paint.setColor(Color.BLUE);
			}else {
				paint.setColor(Color.RED);
			}
			//paint.setColor(mContext.getResources().getColor(R.color.statellite_snr_bg));
		    canvas.drawRect(snrRect[i][0],snrRect[i][1],snrRect[i][2], snrRect[i][3], paint);
		    //paint.setTextSize(Utils.dp2pixel(15));
		    paint.setTextSize(Utils.dp2pixel(15));//柱状图文字
			int mSatelliatePRN_No = prnInt>=160?(prnInt-160):prnInt;
			//canvas.drawText(mSatelliatePRN[i],prnPixelArr[i][0],prnPixelArr[i][1], paint);
			canvas.drawText(mSatelliatePRN_No+"",prnPixelArr[i][0],prnPixelArr[i][1], paint);
			canvas.drawText(String.valueOf((int)snrPixelArr[i][0]),snrPixelArr[i][1],snrPixelArr[i][2], paint);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		width = MeasureSpec.getSize(widthMeasureSpec);
		height = MeasureSpec.getSize(heightMeasureSpec);
	}
	
	
	public void showMap(List<GPSatellite> list) {

		//清除上次数据
		initData();

		int index=0;
		for (int i = 0; i < MAX_VISUAL_SATELLIATE_NUM; i++) {
			if (i < list.size()) {
				GPSatellite mGPSatellite = list.get(i);
				if(mGPSatellite.getSnr()>0){
					if(mGPSatellite.getPrn()>=160){
						mSatelliatePRN[index] = (mGPSatellite.getPrn()-160+160)+ "";
						//mSatelliatePRN[index] = (mGPSatellite.getPrn()-160)+ "";
					}else{
						mSatelliatePRN[index] =mGPSatellite.getPrn()+"";	
					}
					// 柱状态间距 宽度
					int startValue=(3*index+1)*gapValue;
					int stopValue=(3*index+2)*gapValue;
					float mStartRectValue=left + Utils.dp2pixel(startValue);
					float mStopRectValue=left + Utils.dp2pixel(stopValue);
					//
					float mShowValue=(top+bitMapHeight)-((bitMapHeight*mGPSatellite.getSnr())/60.0f);
					snrRect[index][0]=mStartRectValue;
					snrRect[index][1]=mShowValue;
					snrRect[index][2]=mStopRectValue + 5;//柱状图宽度
					snrRect[index][3]=top+bitMapHeight;

					prnPixelArr[index][0]=mStartRectValue;
					prnPixelArr[index][1]=top+bitMapHeight + Utils.dp2pixel(30);

					snrPixelArr[index][0]=mGPSatellite.getSnr();
					snrPixelArr[index][1]=mStartRectValue;
					snrPixelArr[index][2]=mShowValue-Utils.dp2pixel(5);
					index++;
				}
			} else {
				mSatelliatePRN[i] = "";
				snrRect[i][0]=-100;
				snrRect[i][1]=-100;
				snrRect[i][2]=-100;
				snrRect[i][3]=-100;
				prnPixelArr[i][0]=-100;
				prnPixelArr[i][1]=-100;
				snrPixelArr[i][0]=-100;
				snrPixelArr[i][1]=-100;
				snrPixelArr[i][2]=-100;
			}
		}
		postInvalidate();
	}
	public void showMap2(List<BDSatellite> list) {

		//清除上次数据
		initData();

		int index=0;
		for (int i = 0; i < MAX_VISUAL_SATELLIATE_NUM; i++) {
			if (i < list.size()) {
				BDSatellite mGPSatellite = list.get(i);
				if(mGPSatellite.getSnr()>0){
					if(mGPSatellite.getPrn()>=160){
						mSatelliatePRN[index] = (mGPSatellite.getPrn()-160+160)+ "";
						//mSatelliatePRN[index] = (mGPSatellite.getPrn()-160)+ "";
					}else{
						mSatelliatePRN[index] =mGPSatellite.getPrn()+"";
					}
					int startValue=(3*index+1)*gapValue;
					int stopValue=(3*index+2)*gapValue;
					float mStartRectValue=left + Utils.dp2pixel(startValue);
					float mStopRectValue=left + Utils.dp2pixel(stopValue);
					float mShowValue=(top+bitMapHeight)-((bitMapHeight*mGPSatellite.getSnr())/60.0f);
					snrRect[index][0]=mStartRectValue;
					snrRect[index][1]=mShowValue;
					snrRect[index][2]=mStopRectValue + 5;
					snrRect[index][3]=top+bitMapHeight;

					prnPixelArr[index][0]=mStartRectValue;
					prnPixelArr[index][1]=top+bitMapHeight + Utils.dp2pixel(30);

					snrPixelArr[index][0]=mGPSatellite.getSnr();
					snrPixelArr[index][1]=mStartRectValue;
					snrPixelArr[index][2]=mShowValue-Utils.dp2pixel(5);
					index++;
				}
			} else {
				mSatelliatePRN[i] = "";
				snrRect[i][0]=-100;
				snrRect[i][1]=-100;
				snrRect[i][2]=-100;
				snrRect[i][3]=-100;
				prnPixelArr[i][0]=-100;
				prnPixelArr[i][1]=-100;
				snrPixelArr[i][0]=-100;
				snrPixelArr[i][1]=-100;
				snrPixelArr[i][2]=-100;
			}
		}
		postInvalidate();
	}


	public void clearMap(){
		initData();
		postInvalidate();
	}

}
