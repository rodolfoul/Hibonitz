/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hibonit.app;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

/**
 * Sales comparison demo chart.
 */
public class SpeedChart extends AbstractDemoChart {
	List<double[]> values,dates;
  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
  public String getName() {
    return "Speed";
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return "Speed";
  }
  
  public void setData(ArrayList<double[]> time , ArrayList<double[]> values){	  
	  this.dates = time;	  
	  this.values = values;
  }
  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */
  public Intent execute(Context context) {
	  
	  String[] titles = new String[] { "Speed"};
	    int length = titles.length;
	    length = values.get(0).length;
	    int[] colors = new int[] { Color.BLUE};
	    PointStyle[] styles = new PointStyle[] { PointStyle.POINT};
	    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
	    setChartSettings(renderer, "Speed", "Time[minutes]", "Km/h", dates.get(0)[0],
	    		dates.get(0)[length-1], 0, 30, Color.GRAY, Color.LTGRAY);
	    renderer.setXLabels(5);
	    renderer.setYLabels(10);
	    length = renderer.getSeriesRendererCount();
	    for (int i = 0; i < length; i++) {
	      SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
	      seriesRenderer.setDisplayChartValues(true);
	    }
	    
//	    return ChartFactory.getLineChartIntent(context, buildDataset(titles, dates, values),
//		        renderer);
	    
	  //Get the graphical view 
        GraphicalView graphicalView = ChartFactory.getLineChartView(context, buildDataset(titles, dates, values),
    	        renderer); 

       //Enable the cache 
        graphicalView.setDrawingCacheEnabled(true); 

        //Set the layout manually to 800*600 
        graphicalView.layout(0, 0, 800, 600); 

        //Set the quality to high 
        graphicalView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH); 

        //Build the cache, get the bitmap and close the cache 
        graphicalView.buildDrawingCache(true); 
        graphicalView.setDrawingCacheEnabled(true);
        Bitmap bitmapSource = graphicalView.getDrawingCache();
        Log.v("hibonit", bitmapSource.toString());
      // Bitmap bitmap = Bitmap.createBitmap(bitmapSource); 
//        graphicalView.setDrawingCacheEnabled(false); 
//
//        //Then just export it on the SD Card 
//        try { 
//                File file = new File(Environment.getExternalStorageDirectory(), 
//                                "SpeedGraph.png"); 
//                FileOutputStream output = new FileOutputStream(file); 
//                bitmap.compress(CompressFormat.PNG, 100, output); 
//        } catch (Exception e) { 
//                e.printStackTrace(); 
//        } 
	  
  
  	  return ChartFactory.getLineChartIntent(context, buildDataset(titles, dates, values),
	        renderer);
  }

//    String[] titles = new String[] { "Sales for 2008", "Sales for 2007"};
////    List<double[]> values = new ArrayList<double[]>();
//    values.add(new double[] { 14230, 12300, 14240, 15244, 14900, 12200, 11030, 12000, 12500, 15500,
//        14600, 15000 });
//    values.add(new double[] { 10230, 10900, 11240, 12540, 13500, 14200, 12530, 11200, 10500, 12500,
//        11600, 13500 });
//    int length = values.get(0).length;
//    int[] colors = new int[] { Color.BLUE, Color.CYAN};
//    PointStyle[] styles = new PointStyle[] { PointStyle.POINT, PointStyle.POINT};
//    XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
//    setChartSettings(renderer, "Speed", "Time [minutes]", "Km/h",  0.75,
//            12.25, -5000, 19000, Color.GREEN, Color.LTGRAY);
//    renderer.setXLabels(12);
//    renderer.setYLabels(10);
//    renderer.setChartTitleTextSize(20);
//    renderer.setTextTypeface("sans_serif", Typeface.BOLD);
//    renderer.setLabelsTextSize(14f);
//    renderer.setAxisTitleTextSize(15);
//    renderer.setLegendTextSize(15);
//    length = renderer.getSeriesRendererCount();
//    for (int i = 0; i < length; i++) {
//      XYSeriesRenderer seriesRenderer = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
////      seriesRenderer.setFillBelowLine(i == length - 1);
////      seriesRenderer.setFillBelowLineColor(colors[i]);
//      seriesRenderer.setLineWidth(2.5f);
//      seriesRenderer.setDisplayChartValues(true);
//      seriesRenderer.setChartValuesTextSize(10f);
//    }
//    return ChartFactory.getCubicLineChartIntent(context, buildBarDataset(titles, values), renderer,
//        0.5f);
//  }
}
