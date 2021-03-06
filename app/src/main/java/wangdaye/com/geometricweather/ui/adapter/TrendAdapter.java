package wangdaye.com.geometricweather.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.History;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.manager.TimeManager;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.ui.widget.weatherView.trend.TrendItemView;
import wangdaye.com.geometricweather.ui.dialog.WeatherDialog;
import wangdaye.com.geometricweather.utils.manager.ChartStyleManager;

/**
 * Trend adapter.
 * */

public class TrendAdapter extends RecyclerView.Adapter<TrendAdapter.ViewHolder> {

    private Context context;
    private OnTrendItemClickListener listener;

    private Weather weather;
    private History history;
    private boolean showDailyPop;
    private boolean showDate;
    private int previewTime;
    private boolean dayTime;
    private int state;
    private int highest, lowest;

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        // widget
        TrendItemView trendItemView;
        TextView textView;
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);

            this.trendItemView = (TrendItemView) itemView.findViewById(R.id.item_trend);
            trendItemView.setOnClickListener(this);

            this.textView = (TextView) itemView.findViewById(R.id.item_trend_txt);
            this.imageView = (ImageView) itemView.findViewById(R.id.item_trend_icon);

            itemView.findViewById(R.id.item_trend_iconBar).setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.item_trend:
                    listener.onTrendItemClick();
                    break;

                case R.id.item_trend_iconBar:
                    WeatherDialog weatherDialog = new WeatherDialog();
                    weatherDialog.setData(weather, getAdapterPosition(), state == TrendItemView.DATA_TYPE_DAILY);
                    weatherDialog.show(
                            GeometricWeather.getInstance().getTopActivity().getFragmentManager(),
                            null);
                    break;
            }
        }
    }

    public TrendAdapter(Context context,
                        Weather weather, History history,
                        boolean showDailyPop, boolean showDate, int previewTime,
                        OnTrendItemClickListener l) {
        this.context = context;
        this.listener = l;
        this.setData(weather, history, showDailyPop, showDate, previewTime, TrendItemView.DATA_TYPE_DAILY);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (weather == null) {
            holder.trendItemView.setNullData();
        } else {
            holder.trendItemView.setData(weather, state, position, highest, lowest, showDailyPop);
            switch (state) {
                case TrendItemView.DATA_TYPE_DAILY:
                    if (showDate) {
                        holder.textView.setText(weather.dailyList.get(position).date.split("-", 2)[1]);
                    } else {
                        if (position == 0) {
                            holder.textView.setText(context.getString(R.string.today));
                        } else {
                            holder.textView.setText(weather.dailyList.get(position).week);
                        }
                    }
                    int resId = WeatherHelper.getWeatherIcon(
                            weather.dailyList.get(position).weatherKinds[dayTime ? 0 : 1],
                            dayTime)[3];
                    switch (previewTime) {
                        case ChartStyleManager.PREVIEW_TIME_DAY:
                            resId = WeatherHelper.getWeatherIcon(
                                    weather.dailyList.get(position).weatherKinds[0],
                                    true)[3];
                            break;

                        case ChartStyleManager.PREVIEW_TIME_NIGHT:
                            resId = WeatherHelper.getWeatherIcon(
                                    weather.dailyList.get(position).weatherKinds[1],
                                    false)[3];
                            break;
                    }
                    Glide.with(context)
                            .load(resId)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(holder.imageView);
                    break;

                case TrendItemView.DATA_TYPE_HOURLY:
                    holder.textView.setText(weather.hourlyList.get(position).time);
                    Glide.with(context)
                            .load(WeatherHelper.getWeatherIcon(
                                    weather.hourlyList.get(position).weatherKind,
                                    weather.hourlyList.get(position).dayTime)[3])
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(holder.imageView);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        switch (state) {
            case TrendItemView.DATA_TYPE_DAILY:
                return weather == null ? 7 : weather.dailyList.size();

            case TrendItemView.DATA_TYPE_HOURLY:
                if (weather == null) {
                    return 7;
                } else if (weather.hourlyList.size() > 1) {
                    return weather.hourlyList.size();
                } else {
                    return 0;
                }

            default:
                return 7;
        }
    }

    public void setData(Weather weather, History history,
                        boolean showDailyPop, boolean showDate, int previewTime,
                        int state) {
        this.weather = weather;
        this.history = history;
        this.showDailyPop = showDailyPop;
        this.showDate = showDate;
        this.previewTime = previewTime;
        this.dayTime = TimeManager.getInstance(context).isDayTime();
        this.state = state;

        calcTempRange();
    }

    private void calcTempRange() {
        if (weather == null) {
            highest = lowest = 0;
        } else {
            switch (state) {
                case TrendItemView.DATA_TYPE_DAILY:
                    if (history != null) {
                        highest = history.maxiTemp;
                        lowest = history.miniTemp;
                    } else {
                        highest = weather.dailyList.get(0).temps[0];
                        lowest = weather.dailyList.get(0).temps[1];
                    }
                    for (int i = 0; i < weather.dailyList.size(); i ++) {
                        if (weather.dailyList.get(i).temps[0] > highest) {
                            highest = weather.dailyList.get(i).temps[0];
                        }
                        if (weather.dailyList.get(i).temps[1] < lowest) {
                            lowest = weather.dailyList.get(i).temps[1];
                        }
                    }
                    break;

                case TrendItemView.DATA_TYPE_HOURLY:
                    if (history != null) {
                        highest = history.maxiTemp;
                        lowest = history.miniTemp;
                    } else {
                        highest = weather.hourlyList.get(0).temp;
                        lowest = weather.hourlyList.get(0).temp;
                    }
                    for (int i = 0; i < weather.hourlyList.size(); i ++) {
                        if (weather.hourlyList.get(i).temp > highest) {
                            highest = weather.hourlyList.get(i).temp;
                        }
                        if (weather.hourlyList.get(i).temp < lowest) {
                            lowest = weather.hourlyList.get(i).temp;
                        }
                    }
                    break;
            }
        }
    }

    // interface.

    public interface OnTrendItemClickListener {
        void onTrendItemClick();
    }
}