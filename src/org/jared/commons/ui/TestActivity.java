package org.jared.commons.ui;


import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.Toast;

public class TestActivity extends Activity {

  private String lv_arr[] = { "Android", "iPhone", "BlackBerry", "AndroidPeople", "Symbian", "iPad","Windows Mobile", "Sony","HTC","Motorola" };
  private String lv_arr2[] = { "Eric Taix", "eric.taix@gmail.com" };

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
//    addView1();
    addAdapter();
  }
  
  void addView() {
    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    WorkspaceView work = new WorkspaceView(this, null);
    // Car il y a toujours un petit d�calage du doigt m�me lors d'un scrolling vertical
    work.setTouchSlop(32);
    // Chargement de l'image d fond (peut �tre enlev�e)
    Bitmap backGd = BitmapFactory.decodeResource(getResources(), R.drawable.background_black_1280x1024);
    work.loadWallpaper(backGd);
    
    ListView lv1 = (ListView) inflater.inflate(R.layout.list, null, false);
    lv1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lv_arr));
    ListView lv2 = (ListView) inflater.inflate(R.layout.list, null, false);
    lv2.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lv_arr2));
    View v1= inflater.inflate(R.layout.relative_layout, null, false);
    
    
    // Just to test ListView listener: OnItemClick AND OnItemLongListener
    lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> arg0P, View arg1P, int arg2P, long arg3P) {
        Toast toast = Toast.makeText(TestActivity.this, "Click", Toast.LENGTH_SHORT);
        toast.show();
      }
    });    
    lv1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
      public boolean onItemLongClick(AdapterView<?> arg0P, View arg1P, int arg2P, long arg3P) {
        Toast toast = Toast.makeText(TestActivity.this, "Long Click......", Toast.LENGTH_SHORT);
        toast.show();
        return true;
      }
    });
    
    // Add views to the workspace view
    work.addView(lv1);
    work.addView(v1);
    work.addView(lv2);
    setContentView(work);
  }
  

  static int selection = 0;
  static boolean toRight = true;
  void addAdapter() {
	  LinearLayout layout = new LinearLayout(this);
	  layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	    layout.setOrientation(LinearLayout.VERTICAL);
	    
	    DisplayMetrics m = new DisplayMetrics();
	    this.getWindowManager().getDefaultDisplay().getMetrics(m);
	    
	    final GalleryWidgetAdapterView work = new GalleryWidgetAdapterView(this);
	    work.setLayoutParams(new ViewGroup.LayoutParams(m.widthPixels, 200));
//	    work.setBackgroundColor(0xffffffff);
	    final Vector<Res> res = new Vector<Res>();
	    res.add(new Res(R.drawable.i8, "i8"));
	    res.add(new Res(R.drawable.i9, "i9"));
	    res.add(new Res(R.drawable.i10, "i10"));
	    res.add(new Res(R.drawable.i11, "i11"));
	    res.add(new Res(R.drawable.i12, "i12"));
	    final Adapter adapter = new Adapter(this, res);
	    work.setAdapter(adapter);
	    
	    work.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				System.out.println("adapterview at " + position + " has been clicked");
			}
			
		});
	    layout.addView(work);

	    Button button1 = new Button(this);
	    button1.setText("add");
	    button1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				res.add(new Res(R.drawable.i8, "i8"));
				System.out.println("res.size:" + res.size());
				adapter.notifyDataSetChanged();
			}
		});
	    layout.addView(button1);
	    
	    Button button2 = new Button(this);
	    button2.setText("delete");
	    button2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				res.remove(0);
				System.out.println("res.size:" + res.size());
				adapter.notifyDataSetChanged();
			}
		});
	    layout.addView(button2);
	    
	    
	    Button button3 = new Button(this);
	    button3.setText("locate");
	    button3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("selection : " + selection);
				// TODO Auto-generated method stub
				if(toRight) {
					if(selection == res.size()-2)
						toRight = false;
					selection ++;
					work.setSection(selection);
				} else {
					if(selection == 1)
						toRight = true;
					selection --;
					work.setSection(selection);
				}
			}
		});
	    layout.addView(button3);
	    
	    setContentView(layout);
  }
  
  public static class Res {
	  
	  public int id;
	  public String name;
	  
	  public Res(int id, String name) {
		  this.id = id;
		  this.name = name;
	  }
	  
  }
  
  void addView1() {
	  LinearLayout layout = new LinearLayout(this);
	    layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	    layout.setOrientation(LinearLayout.VERTICAL);
	    
	    DisplayMetrics m = new DisplayMetrics();
	    this.getWindowManager().getDefaultDisplay().getMetrics(m);
	    
//	    GalleryTest work = new GalleryTest(this, null);
	    GalleryWidget work = new GalleryWidget(this);
//	    NaviWidget work = new NaviWidget(this);
	    work.setLayoutParams(new ViewGroup.LayoutParams(m.widthPixels, 200));
//	    work.setBackgroundColor(0xFFFFFFFF);
	    ImageView img = new ImageView(this);
	    img.setImageResource(R.drawable.i8);
	    img.setScaleType(ImageView.ScaleType.FIT_CENTER);
	    img.setBackgroundResource(R.layout.bgselector);
	    work.addView(img);
	    
	    img = new ImageView(this);
	    img.setImageResource(R.drawable.i9);
	    img.setScaleType(ImageView.ScaleType.FIT_CENTER);
	    img.setBackgroundResource(R.layout.bgselector);
	    work.addView(img);
	    
	    img = new ImageView(this);
	    img.setImageResource(R.drawable.i10);
	    img.setScaleType(ImageView.ScaleType.FIT_CENTER);
	    img.setBackgroundResource(R.layout.bgselector);
	    work.addView(img);
	    
	    img = new ImageView(this);
	    img.setImageResource(R.drawable.i11);
	    img.setScaleType(ImageView.ScaleType.FIT_CENTER);
	    img.setBackgroundResource(R.layout.bgselector);
	    work.addView(img);
	    
	    img = new ImageView(this);
	    img.setImageResource(R.drawable.i12);
	    img.setScaleType(ImageView.ScaleType.FIT_CENTER);
	    img.setBackgroundResource(R.layout.bgselector);
	    work.addView(img);

	    layout.addView(work);
	    setContentView(layout);
	    
	    work.setOnItemClickListener(new GalleryWidget.OnItemClickListener() {
			
			@Override
			public void onItemClick(View view) {
				// TODO Auto-generated method stub
				System.out.println("test click");
			}
		});
  }
  
  LinearLayout lay1,lay2,lay;
  private Scroller mScroller;
  private boolean s1,s2;

  void addView2() {
	  ScrollView s;
      mScroller = new Scroller(this);
      //HorizontalScrollView hv = new HorizontalScrollView(this);
       lay1 = new LinearLayout(this){
           @Override 
           public void computeScroll() { 
//               if (mScroller.computeScrollOffset()) { 
//                  // mScrollX = mScroller.getCurrX(); 
//                   scrollTo(mScroller.getCurrX(), 0); 
//                   postInvalidate(); 
//               } 
           } 

       };
       lay2 = new LinearLayout(this){
           @Override 
           public void computeScroll() { 
//               if (mScroller.computeScrollOffset()) { 
//                  // mScrollX = mScroller.getCurrX(); 
//                   scrollTo(mScroller.getCurrX(), 0); 
//                   postInvalidate(); 
//               } 
           } 

       };

      lay1.setBackgroundColor(this.getResources().getColor(android.R.color.darker_gray));
      lay2.setBackgroundColor(this.getResources().getColor(android.R.color.white));
      lay = new LinearLayout(this);
      lay.setOrientation(LinearLayout.VERTICAL);
      LinearLayout.LayoutParams p0 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);     
      this.setContentView(lay, p0);

      LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);     
      p1.weight=1;
      lay.addView(lay1,p1);
      LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);     
      p2.weight=1;
      lay.addView(lay2,p2);
      Button tx = new Button(this);
      Button tx2 = new Button(this);
      tx.setText("aaaaaaaaa");
      //tx.setBackgroundColor(this.getResources().getColor(android.R.color.black));
      tx2.setText("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
      tx.setOnClickListener(new OnClickListener(){

          @Override
          public void onClick(View v) {
              if(!s1){
                  mScroller.startScroll(0, 0, 10, 10,10);
                  s1 = true;
              }else{
                  mScroller.startScroll(0, 0, -10, -10,10);
                  s1 = false;
              }
          }

      });
      tx2.setOnClickListener(new OnClickListener(){

          @Override
          public void onClick(View v) {
              if(!s2){
                  mScroller.startScroll(0, 0, 20, 20,10);
                  s2=true;
              }else{
                  mScroller.startScroll(20, 20, -20, -20,10);
                  s2=false;
              }

          }

      });
//      tx2.setOnKeyListener(new Listener());
      lay1.addView(tx);
      lay2.addView(tx2);
  }
}