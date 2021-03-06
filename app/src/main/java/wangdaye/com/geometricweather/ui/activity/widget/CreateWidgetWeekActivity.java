package wangdaye.com.geometricweather.ui.activity.widget;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;

import wangdaye.com.geometricweather.basic.GeoWidgetConfigActivity;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.service.PollingService;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.ChartStyleManager;

/**
 * Create the widget [week] on the launcher.
 * */

public class CreateWidgetWeekActivity extends GeoWidgetConfigActivity
        implements View.OnClickListener {

    private ImageView widgetCard;
    private TextView[] widgetWeeks;
    private ImageView[] widgetIcons;
    private TextView[] widgetTemps;

    private CoordinatorLayout container;

    private Switch showCardSwitch;
    private Switch blackTextSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_widget_week);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @SuppressLint("InflateParams")
    @Override
    public void initWidget() {
        View widgetView = LayoutInflater.from(this).inflate(R.layout.widget_week, null);
        ((ViewGroup) findViewById(R.id.activity_create_widget_week_widgetContainer)).addView(widgetView);

        this.widgetCard = (ImageView) widgetView.findViewById(R.id.widget_week_card);
        widgetCard.setVisibility(View.GONE);

        this.widgetWeeks = new TextView[] {
                (TextView) widgetView.findViewById(R.id.widget_week_week_1),
                (TextView) widgetView.findViewById(R.id.widget_week_week_2),
                (TextView) widgetView.findViewById(R.id.widget_week_week_3),
                (TextView) widgetView.findViewById(R.id.widget_week_week_4),
                (TextView) widgetView.findViewById(R.id.widget_week_week_5)};
        this.widgetIcons = new ImageView[] {
                (ImageView) widgetView.findViewById(R.id.widget_week_icon_1),
                (ImageView) widgetView.findViewById(R.id.widget_week_icon_2),
                (ImageView) widgetView.findViewById(R.id.widget_week_icon_3),
                (ImageView) widgetView.findViewById(R.id.widget_week_icon_4),
                (ImageView) widgetView.findViewById(R.id.widget_week_icon_5)};
        this.widgetTemps = new TextView[] {
                (TextView) widgetView.findViewById(R.id.widget_week_temp_1),
                (TextView) widgetView.findViewById(R.id.widget_week_temp_2),
                (TextView) widgetView.findViewById(R.id.widget_week_temp_3),
                (TextView) widgetView.findViewById(R.id.widget_week_temp_4),
                (TextView) widgetView.findViewById(R.id.widget_week_temp_5)};

        ImageView wallpaper = (ImageView) findViewById(R.id.activity_create_widget_week_wall);
        wallpaper.setImageDrawable(WallpaperManager.getInstance(this).getDrawable());

        this.container = (CoordinatorLayout) findViewById(R.id.activity_create_widget_week_container);

        this.showCardSwitch = (Switch) findViewById(R.id.activity_create_widget_week_showCardSwitch);
        showCardSwitch.setOnCheckedChangeListener(new ShowCardSwitchCheckListener());

        this.blackTextSwitch = (Switch) findViewById(R.id.activity_create_widget_week_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = (Button) findViewById(R.id.activity_create_widget_week_doneButton);
        doneButton.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void refreshWidgetView(Weather weather) {
        if (weather == null) {
            return;
        }

        boolean dayTime = TimeManager.getInstance(this).getDayTime(this, weather, false).isDayTime();
        switch (ChartStyleManager.getInstance(this).getPreviewTime()) {
            case ChartStyleManager.PREVIEW_TIME_DAY:
                dayTime = true;
                break;

            case ChartStyleManager.PREVIEW_TIME_NIGHT:
                dayTime = false;
                break;
        }

        String firstWeekDay;
        String secondWeekDay;
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String[] weatherDates = weather.base.date.split("-");
        if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month
                && Integer.parseInt(weatherDates[2]) == day) {
            firstWeekDay = getString(R.string.today);
            secondWeekDay = weather.dailyList.get(1).week;
        } else if (Integer.parseInt(weatherDates[0]) == year
                && Integer.parseInt(weatherDates[1]) == month
                && Integer.parseInt(weatherDates[2]) == day - 1) {
            firstWeekDay = getString(R.string.yesterday);
            secondWeekDay = getString(R.string.today);
        } else {
            firstWeekDay = weather.dailyList.get(0).week;
            secondWeekDay = weather.dailyList.get(1).week;
        }

        for (int i = 0; i < 5; i ++) {
            if (i == 0) {
                widgetWeeks[i].setText(firstWeekDay);
            } else if (i == 1) {
                widgetWeeks[i].setText(secondWeekDay);
            } else {
                widgetWeeks[i].setText(weather.dailyList.get(i).week);
            }
            int[] imageIds = WeatherHelper.getWeatherIcon(
                    dayTime ? weather.dailyList.get(i).weatherKinds[0] : weather.dailyList.get(i).weatherKinds[1],
                    dayTime);
            widgetIcons[i].setImageResource(imageIds[3]);
            widgetTemps[i].setText(ValueUtils.buildDailyTemp(weather.dailyList.get(i).temps, false, isFahrenheit()));
        }
    }

    // interface.

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_create_widget_week_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_week_setting),
                        MODE_PRIVATE)
                        .edit();
                editor.putBoolean(getString(R.string.key_show_card), showCardSwitch.isChecked());
                editor.putBoolean(getString(R.string.key_black_text), blackTextSwitch.isChecked());
                editor.apply();

                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                int appWidgetId = 0;
                if (extras != null) {
                    appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);
                }

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);

                ServiceHelper.startupService(this, PollingService.FORCE_REFRESH_TYPE_NORMAL_VIEW);
                finish();
                break;
        }
    }

    // on check changed listener(switch).

    private class ShowCardSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widgetCard.setVisibility(View.VISIBLE);
                for (int i = 0; i < 5; i ++) {
                    widgetWeeks[i].setTextColor(ContextCompat.getColor(CreateWidgetWeekActivity.this, R.color.colorTextDark));
                    widgetTemps[i].setTextColor(ContextCompat.getColor(CreateWidgetWeekActivity.this, R.color.colorTextDark));
                }
            } else {
                widgetCard.setVisibility(View.GONE);
                if (!blackTextSwitch.isChecked()) {
                    for (int i = 0; i < 5; i ++) {
                        widgetWeeks[i].setTextColor(ContextCompat.getColor(CreateWidgetWeekActivity.this, R.color.colorTextLight));
                        widgetTemps[i].setTextColor(ContextCompat.getColor(CreateWidgetWeekActivity.this, R.color.colorTextLight));
                    }
                }
            }
        }
    }

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                for (int i = 0; i < 5; i ++) {
                    widgetWeeks[i].setTextColor(ContextCompat.getColor(CreateWidgetWeekActivity.this, R.color.colorTextDark));
                    widgetTemps[i].setTextColor(ContextCompat.getColor(CreateWidgetWeekActivity.this, R.color.colorTextDark));
                }
            } else {
                if (!showCardSwitch.isChecked()) {
                    for (int i = 0; i < 5; i ++) {
                        widgetWeeks[i].setTextColor(ContextCompat.getColor(CreateWidgetWeekActivity.this, R.color.colorTextLight));
                        widgetTemps[i].setTextColor(ContextCompat.getColor(CreateWidgetWeekActivity.this, R.color.colorTextLight));
                    }
                }
            }
        }
    }
}