package com.novsky.map.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class VerticalProgressBar extends ProgressBar {

	/*显示百分比*/
	private String text;
	
	private Paint mPaint;
	
	public VerticalProgressBar(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle); 
        initText();
    }  
	
	public VerticalProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initText();
	}
	
    public VerticalProgressBar(Context context) {  
	    super(context);  
	    initText(); 
	}  


    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.rotate(-90);//反转90度，将水平Progressbar竖立起来
    	canvas.translate(-getHeight(), 0);//将经历旋转后得到的VerticalProgressBar移到正确的位置，注意旋转后宽高值互换
    	super.onDraw(canvas);
    }
    
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
    		int heightMeasureSpec) {
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	setMeasuredDimension(getMeasuredHeight(),getMeasuredWidth());//互换宽高值
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	super.onSizeChanged(w, h, oldw, oldh);
    }
    
  //初始化画笔
  	 private void initText(){
          this.mPaint = new Paint();
          this.mPaint.setColor(Color.WHITE);
  	 }
  	 
  	 @Override
  	 public synchronized void setProgress(int progress) {
  	    setText(progress);
  	    super.setProgress(progress);
       }

  	 private void setText(){
  	     setText(this.getProgress());
  	 }
  	                
       //设置文字内容
     private void setText(int progress){
          this.text = String.valueOf(progress);
    }
}
