/*
 * The MIT License

 * Copyright (c) 2010 Peter Ma

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/

package com.contest.adapters;

import java.util.List;
import java.util.Map;

import com.contest.accessibility.R;
import com.contest.activities.LazyActivity;
import com.tedx.helpers.Common;
import com.tedx.utility.RemoteImageView;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class LazyAdapter extends SimpleAdapter implements Filterable{
	public static final String IMAGE = "LazyAdapter_image";

	private boolean mDone = false;
	private boolean mFlinging = false;
	private LazyActivity mActivity;

	public LazyAdapter(LazyActivity context, List<? extends Map<String, String>> data, int resource, String[] from, int[] to) {
		super(context, data, resource, from, to);
		mActivity = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// see if we need to load the next page
		if (!mDone && (getCount() - position) <= 1) {
			if (mActivity.isTaskFinished()) {
				mActivity.loadNextPage();
			}
		}

		View ret = super.getView(position, convertView, parent);
		if (ret != null) {
			final TextView text1 = (TextView) ret.findViewById(android.R.id.text1);
			final TextView text2 = (TextView) ret.findViewById(android.R.id.text2);

			ImageButton btnShare = (ImageButton) ret.findViewById(R.id.btnShare);
			
			ImageButton btnRating = (ImageButton) ret.findViewById(R.id.btnRating);
			RemoteImageView riv = (RemoteImageView) ret.findViewById(android.R.id.icon);

			btnRating.setOnClickListener(new OnClickListener() {
			    public void onClick(View view) {
			    	Dialog dialog = new Dialog(mActivity);

			    	dialog.setContentView(R.layout.rating_dialog);
			    	dialog.setTitle("Rating The Video");
					
			    	final RatingBar ratVideo = (RatingBar)dialog.findViewById(R.id.ratVideo);
			    	final RatingBar ratChannel = (RatingBar)dialog.findViewById(R.id.ratChannel);
			    	Button btnReview = (Button)dialog.findViewById(R.id.btnReview);			    	

					btnReview.setOnClickListener(new OnClickListener() {
					    public void onClick(View view) {
			                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			                
			                String videorating = text2.getText().toString() + "\n\n" + text1.getText().toString() + "\n\n" +
        					"Rating for the captioned video " + String.valueOf(ratVideo.getRating()) + "/5\n\n" + 
        					"Rating for the youtube channel " + String.valueOf(ratChannel.getRating()) + "/5\n";
        					
			                emailIntent.setType("plain/text");			 
			                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Youtube Video Rating");
			                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, videorating);
							
			                mActivity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
					    }
					});	
				
			    	dialog.show();
			    }
			});	
			
			btnShare.setOnClickListener(new OnClickListener() {
			    public void onClick(View view) {
			        // Start new activity
					Intent intent = new Intent();
				    intent.setAction(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, text2.getText().toString() + "\n" + text1.getText().toString());
					Intent chooser = Intent.createChooser(intent, "Share Video");
					mActivity.startActivity(chooser);
			    }
			});			
						
			text1.setOnClickListener(new OnClickListener() {
			    public void onClick(View view) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(text2.getText().toString()));
				    intent.setPackage("com.google.android.youtube");
				    mActivity.startActivity(intent);
			    }
			});
			
			riv.setOnClickListener(new OnClickListener() {
			    public void onClick(View view) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(text2.getText().toString()));
				    intent.setPackage("com.google.android.youtube");
				    mActivity.startActivity(intent);
			    }
			});
			
			riv.setContentDescription(text1.getText());
			if (riv != null && !mFlinging) {
				riv.loadImage();
			}
		}
		
		return ret;
	}

	public void setStopLoading(boolean done) {
		mDone = done;
	}

	public void setFlinging(boolean flinging) {
		mFlinging = flinging;
	}

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
    
	@Override
	public void setViewImage(final ImageView image, final String value) {
		if (value != null && value.length() > 0 && image instanceof RemoteImageView) {
			RemoteImageView riv = (RemoteImageView) image;
			riv.setLocalURI(Common.getCacheFileName(value));
			riv.setRemoteURI(value);
			super.setViewImage(image, R.drawable.missingphoto);
		} else {
			image.setVisibility(View.GONE);
		}
	}
}