package com.example.yuzelli.linechartdemo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.Request;

public class MainActivity extends Activity {
    LineChartView linechart;
    RelativeLayout rl_content;
    private static String content = "";
    private List<AxisValue> mAxisValues = new ArrayList<AxisValue>();
    private static int page = 1;

    private Button btn_getData;
    private Button btn_clean;
    private Context context;
    private MainHandler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        handler = new MainHandler();
        rl_content = this.findViewById(R.id.rl_content);
        btn_getData = this.findViewById(R.id.btn_getData);
        btn_getData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                doGetChartDate();

            }
        });

        btn_clean = this.findViewById(R.id.btn_clean);
        btn_clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               page = 1;
                rl_content.removeAllViews();
                content = "";
            }
        });


    }

    @Override
    protected void onResume() {
        /**
         * 设置为横屏
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

    private void doGetChartDate() {
        OkHttpClientManager manager = OkHttpClientManager.getInstance();
        Map<String, String> map = new HashMap<>();
        map.put("type", "getChartByID");
        map.put("page", page + "");

        String url = OkHttpClientManager.attachHttpGetParams("http://172.18.219.108:8080/ChatDemoService/chartServlet", map);
        manager.getAsync(url, new OkHttpClientManager.DataCallBack() {
            @Override
            public void requestFailure(Request request, IOException e) {
                Toast.makeText(context, "请求失败！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void requestSuccess(String result) throws Exception {

                JSONObject object = new JSONObject(result);
                String flag = object.getString("error");
                if (flag.equals("ok")) {
                    String a = object.optJSONObject("object").optString("content");
                    if (a==null||a.equals("")||a.equals("null")){
                        page = 1;
                        handler.sendEmptyMessage(1002);
                    }else {
                        content = content + object.optJSONObject("object").optString("content");
                        handler.sendEmptyMessage(1001);
                        page++;
                    }


                } else {

                    //Toast.makeText(context, "服务器错误！", Toast.LENGTH_SHORT).show();
                    page = 1;
                    handler.sendEmptyMessage(1002);
                }
            }
        });

    }


    private void initData() {
        List<PointValue> valuesLow = new ArrayList<PointValue>();
        content = content.replaceAll("，", ",");
        content = content.replaceAll(" ", "");
        String contentList[] = content.split(",");
        for (int i = 0; i < contentList.length; i++) {
            valuesLow.add(new PointValue(i, Float.valueOf(contentList[i])));
            mAxisValues.add(new AxisValue(i).setLabel(i + ""));
        }

        initLineChart(valuesLow);

    }

    private void initLineChart(List<PointValue> highPointValues) {
        List<Line> lines = new ArrayList<Line>();
        Line line = new Line(highPointValues).setColor(Color.parseColor("#C0D79C")).setStrokeWidth(1);  //折线的颜色、粗细
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.SQUARE）
        line.setCubic(true);//曲线是否平滑
        line.setFilled(false);//是否填充曲线的面积
        //   line.setHasLabels(true);//曲线的数据坐标是否加上备注
        line.setPointRadius(3);//座标点大小
        line.setHasLabelsOnlyForSelected(false);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用直线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setValueLabelBackgroundColor(Color.TRANSPARENT);//此处设置坐标点旁边的文字背景
        data.setValueLabelBackgroundEnabled(false);
        data.setValueLabelsTextColor(Color.BLACK);  //此处设置坐标点旁边的文字颜色

        data.setLines(lines);


        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);
        axisX.setTextColor(Color.BLACK);  //设置字体颜色
        axisX.setName("X轴");  //表格名称
        axisX.setTextSize(7);//设置字体大小
        axisX.setMaxLabelChars(5);  //最多几个X轴坐标
        axisX.setValues(mAxisValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部

        axisX.setAutoGenerated(true);
//      data.setAxisXTop(axisX);  //x 轴在顶部

        Axis axisY = new Axis();  //Y轴
        axisY.setMaxLabelChars(7); //默认是3，只能看最后三个数字
        axisY.setName("Y轴");//y轴标注
        axisY.setTextSize(7);//设置字体大小

        data.setAxisYLeft(axisY);  //Y轴设置在左边
//      data.setAxisYRight(axisY);  //y轴设置在右边


        //设置行为属性，支持缩放、滑动以及平移
        linechart.setInteractive(true);
        linechart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        linechart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        linechart.setLineChartData(data);
        linechart.setVisibility(View.VISIBLE);

        linechart.setOnValueTouchListener(new LineChartOnValueSelectListener() {
            @Override
            public void onValueDeselected() {

            }

            @Override
            public void onValueSelected(int i, int i1, PointValue pointValue) {
                Toast.makeText(MainActivity.this,(int) pointValue.getX() + "," + pointValue.getY(), Toast.LENGTH_SHORT).show();
            }
        });//为图表设置值得触摸事件
        linechart.setInteractive(true);//设置图表是否可以与用户互动
        linechart.setValueSelectionEnabled(true);//设置图表数据是否选中进行显示

    }


    class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    updataView();
                    break;
                case 1002:
                    doGetChartDate();
                    break;
                default:
                    break;
            }
        }
    }

    private void updataView() {
        linechart = new LineChartView(this);

        rl_content.removeAllViews();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams( DensityUtils.dp2px(context,300*page), DensityUtils.dp2px(context,300));
        rl_content.setLayoutParams(lp);

        initData();
        rl_content.addView(linechart);
    }
}
