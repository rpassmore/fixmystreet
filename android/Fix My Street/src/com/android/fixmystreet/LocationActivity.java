/**
 * File: LocationActivity.java Project: FillThatHole
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class LocationActivity extends MapActivity {

  //used to get location info from the returned bundle
  public static final String LOCATION_ADDRESS = "Address";
  public static final String LOCATION_LAT = "Lat";
  public static final String LOCATION_LONG = "Long";  
  public static final String LOCATION_CAN_DRAG = "canDrag";
  public static final String LOCATION_CAN_CREATE = "canCreate";
    
  private Button setLocationButon;
  private ImageButton useGPSButton;
  
  private MapController mapController;
  private MapView mapView;
  private LocationManager locationManager;
  private MyLocationOverlay me;
  private SitesOverlay sitesOverlay;  
  private boolean canDrag = true;
  private boolean canCreate = true;
  public boolean useGPSLocation = false;
  private LocationListener geoUpdateHandler;
  
  public void onCreate(Bundle savedBundle) {
    super.onCreate(savedBundle);
    setContentView(R.layout.location_activity);

    geoUpdateHandler = new GeoUpdateHandler();
    
    setLocationButon = (Button)findViewById(R.id.setLocation); 
    useGPSButton = (ImageButton)findViewById(R.id.useGPSBtn);
    
    // create a map view
    mapView = (MapView) findViewById(R.id.mapView);
    mapView.setBuiltInZoomControls(true);
    mapView.setLongClickable(true);
   
    mapController = mapView.getController();
    mapController.setZoom(18); // Zoom 1 is world view   

    Drawable marker = getResources().getDrawable(R.drawable.marker);
    marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());

    //setup the map overlays
    sitesOverlay = new SitesOverlay(marker);
    mapView.getOverlays().add(new MapGestureDetectorOverlay(new MyOnGestureListener()));
    mapView.getOverlays().add(sitesOverlay);
    me = new MyLocationOverlay(this, mapView);
    mapView.getOverlays().add(me);
    
    //add an existing point to be plotted 
    Bundle bundle = this.getIntent().getExtras();   
    if(getIntent().hasExtra(LOCATION_LAT) && getIntent().hasExtra(LOCATION_LONG)) {
      GeoPoint point = new GeoPoint( (int)bundle.getLong(LOCATION_LAT), (int)bundle.getLong(LOCATION_LONG));
      sitesOverlay.addItem(new OverlayItem(point, "", ""));
      //There is already a location then don't use the GPS location on less asked to 
      useGPSLocation = false;

      //scroll to the stored point    
      mapController.animateTo(point);    
    }
    
    canDrag  = bundle.getBoolean(LOCATION_CAN_DRAG);
    canCreate  = bundle.getBoolean(LOCATION_CAN_CREATE);    
    
    //if the location can not be changed hide and disable the location setting button
    if(!canDrag && !canCreate) {
      setLocationButon.setEnabled(false);
      setLocationButon.setVisibility(View.GONE);
    }
    
    //temp set location for emulator
    //GeoPoint tmpPoint = new GeoPoint((int)( 51.22242812067373 * 1E6),(int)(-2.610454559326172 * 1E6));    
    //mapController.animateTo(tmpPoint);    
    
    if(useGPSLocation) {
      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new GeoUpdateHandler());
    }
    
   
  }

  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }
  
  /**
   * Called when the user clicks the set location button, returns the lat and long of the item in a bundle
   * @param view
   */
  public void locationClickHandler(View view) {
    
    //to return data
    Intent intent = new Intent();
    Bundle bundle = new Bundle();
    //use the last item in the list
    OverlayItem item = sitesOverlay.getItems().get(sitesOverlay.size() - 1);
    if(item != null) {
      bundle.putLong(LOCATION_LAT, item.getPoint().getLatitudeE6()); 
      bundle.putLong(LOCATION_LONG, item.getPoint().getLongitudeE6());

      //get the address from the location co-ordinates
      Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
      
      List<Address> addresses;
      try {
        addresses = geocoder.getFromLocation(item.getPoint().getLatitudeE6() / 1.0E6, item.getPoint().getLongitudeE6() / 1.0E6, 1);
        if (addresses != null && !addresses.isEmpty()) {
          Address returnedAddress = addresses.get(0);
          StringBuilder strReturnedAddress = new StringBuilder();          
          strReturnedAddress.append( returnedAddress.getThoroughfare() );
          strReturnedAddress.append( ", " );
          strReturnedAddress.append( returnedAddress.getPostalCode() );
          
          bundle.putString(LOCATION_ADDRESS, strReturnedAddress.toString());
        }
      } catch (IOException e) {       
        Log.e(getPackageName(), e.toString());
      }
      intent.putExtras(bundle);
      setResult(RESULT_OK, intent);
    } else {
      setResult(RESULT_CANCELED);
    }
    finish();        
  }
  
  /**
   * Called when the user clicks the set use GPS button
   * @param view
   */
  public void useGPSBtnClickHandler(View view) {
    if(useGPSLocation) {
      useGPSLocation = false;
      locationManager.removeUpdates(geoUpdateHandler);
    } else {
      useGPSLocation = true;
      if(locationManager == null) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      }
      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, geoUpdateHandler);
    }
  }

  public class MyOnGestureListener implements OnGestureListener {
    public boolean onDown(MotionEvent e) {
      // TODO Auto-generated method stub
      return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      // TODO Auto-generated method stub
      return false;
    }

    public void onLongPress(MotionEvent e) {
      if(canCreate) {
        GeoPoint point = mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY());
        sitesOverlay.addItem(new OverlayItem(point, "", ""));
        canCreate = false;
      }
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      // TODO Auto-generated method stub
      return false;
    }

    public void onShowPress(MotionEvent e) {
      // TODO Auto-generated method stub

    }

    public boolean onSingleTapUp(MotionEvent e) {
      // TODO Auto-generated method stub
      return false;
    }
  }

  public class GeoUpdateHandler implements LocationListener {

    /**
     * Move the displayed map to the current location
     */
    public void onLocationChanged(Location location) {
      if(useGPSLocation ) {
        int lat = (int) (location.getLatitude() * 1E6);
        int lng = (int) (location.getLongitude() * 1E6);
        GeoPoint point = new GeoPoint(lat, lng);
        mapController.animateTo(point);
      }
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }    
  }

  private class SitesOverlay extends ItemizedOverlay<OverlayItem> {
    // Variables for items and item dragging
    private List<OverlayItem> items = new ArrayList<OverlayItem>();
    private Drawable marker = null;
    private OverlayItem inDrag = null;
    private ImageView dragImage = null;
    private int xDragImageOffset = 0;
    private int yDragImageOffset = 0;
    private int xDragTouchOffset = 0;
    private int yDragTouchOffset = 0;

    private SitesOverlay(Drawable marker) {
      super(marker);
      this.marker = marker;
      
      //call populate to NPE from ItemizedOverlay 
      populate();
      dragImage = (ImageView) findViewById(R.id.drag);
      xDragImageOffset = dragImage.getDrawable().getIntrinsicWidth() / 2;
      yDragImageOffset = dragImage.getDrawable().getIntrinsicHeight();
    }

    @Override
    protected OverlayItem createItem(int i) {
      return items.get(i);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
      super.draw(canvas, mapView, shadow);

      boundCenterBottom(marker);
    }

    @Override
    public int size() {
      return items.size();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
      final int action = event.getAction();
      final int x = (int) event.getX();
      final int y = (int) event.getY();
      boolean result = false;

      if (action == MotionEvent.ACTION_DOWN && canDrag) {
        for (OverlayItem item : items) {
          Point p = new Point(0, 0);

          mapView.getProjection().toPixels(item.getPoint(), p);

          if (hitTest(item, marker, x - p.x, y - p.y)) {
            result = true;
            inDrag = item;
            items.remove(inDrag);
            populate();

            xDragTouchOffset = 0;
            yDragTouchOffset = 0;

            setDragImagePosition(p.x, p.y);
            dragImage.setVisibility(View.VISIBLE);

            xDragTouchOffset = x - p.x;
            yDragTouchOffset = y - p.y;

            break;
          }
        }
      } else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
        setDragImagePosition(x, y);
        result = true;
      } else if (action == MotionEvent.ACTION_UP && inDrag != null) {
        dragImage.setVisibility(View.GONE);

        GeoPoint pt = mapView.getProjection().fromPixels(x - xDragTouchOffset, y - yDragTouchOffset);
        OverlayItem toDrop = new OverlayItem(pt, inDrag.getTitle(), inDrag.getSnippet());

        items.add(toDrop);
        populate();

        inDrag = null;
        result = true;
      }

      return (result || super.onTouchEvent(event, mapView));
    }

    private void setDragImagePosition(int x, int y) {
      RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage.getLayoutParams();

      lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y - yDragImageOffset - yDragTouchOffset, 0, 0);
      dragImage.setLayoutParams(lp);
    }

    public void addItem(OverlayItem newItem) {
      items.add(newItem);
      populate();
    }
    
    public List<OverlayItem> getItems() {
      return items;
    }
  }
}