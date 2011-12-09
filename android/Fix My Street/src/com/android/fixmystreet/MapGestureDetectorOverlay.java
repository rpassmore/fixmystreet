/**
 * File: MapGestureDetector.java Project: FillThatHole
 *
 * Created: 20 Feb 2011
 *
 * Copyright (C) 2011 Robert Passmore
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.android.fixmystreet;


import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;


import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapGestureDetectorOverlay extends Overlay implements OnGestureListener {
	 private GestureDetector gestureDetector;
	 private OnGestureListener onGestureListener;

	 public MapGestureDetectorOverlay() {
	  gestureDetector = new GestureDetector(this);
	 }

	 public MapGestureDetectorOverlay(OnGestureListener onGestureListener) {
	  this();
	  setOnGestureListener(onGestureListener);
	 }

	 @Override
	 public boolean onTouchEvent(MotionEvent event, MapView mapView) {
	  if (gestureDetector.onTouchEvent(event)) {
	   return true;
	  }
	  return false;
	 }

	 public boolean onDown(MotionEvent e) {
	  if (onGestureListener != null) {
	   return onGestureListener.onDown(e);
	  }
	  return false;
	 }

	 public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
	   float velocityY) {
	  if (onGestureListener != null) {
	   return onGestureListener.onFling(e1, e2, velocityX, velocityY);
	  }
	  return false;
	 }

	 public void onLongPress(MotionEvent e) {
	  if (onGestureListener != null) {
	   onGestureListener.onLongPress(e);
	  }
	 }

	 public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
	   float distanceY) {
	  if (onGestureListener != null) {
	   onGestureListener.onScroll(e1, e2, distanceX, distanceY);
	  }
	  return false;
	 }

	 public void onShowPress(MotionEvent e) {
	  if (onGestureListener != null) {
	   onGestureListener.onShowPress(e);
	  }
	 }

	 public boolean onSingleTapUp(MotionEvent e) {
	  if (onGestureListener != null) {
	   onGestureListener.onSingleTapUp(e);
	  }
	  return false;
	 }

	 public boolean isLongpressEnabled() {
	  return gestureDetector.isLongpressEnabled();
	 }

	 public void setIsLongpressEnabled(boolean isLongpressEnabled) {
	  gestureDetector.setIsLongpressEnabled(isLongpressEnabled);
	 }

	 public OnGestureListener getOnGestureListener() {
	  return onGestureListener;
	 }

	 public void setOnGestureListener(OnGestureListener onGestureListener) {
	  this.onGestureListener = onGestureListener;
	 }
	}
