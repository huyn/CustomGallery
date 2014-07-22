package org.jared.commons.ui;

import java.util.Vector;

import org.jared.commons.ui.TestActivity.Res;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class Adapter extends BaseAdapter {
	
	private Context context;
	private Vector<Res> res;
	
	public Adapter(Context context, Vector<Res> res) {
		super();
		this.context = context;
		this.res = res;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return res.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView img = new ImageView(context);
	    img.setImageResource(res.elementAt(position).id);
//		img.setImageResource(R.drawable.i10);
	    img.setScaleType(ImageView.ScaleType.FIT_CENTER);
	    img.setBackgroundResource(R.layout.bgselector);
	    return img;
	}

}
