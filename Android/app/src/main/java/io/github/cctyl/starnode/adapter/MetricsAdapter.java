package io.github.cctyl.starnode.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.github.cctyl.starnode.R;
import io.github.cctyl.starnode.model.MetricItem;
import java.util.List;

public class MetricsAdapter extends RecyclerView.Adapter<MetricsAdapter.MetricViewHolder> {
    private List<MetricItem> metrics;

    public MetricsAdapter(List<MetricItem> metrics) {
        this.metrics = metrics;
    }

    @NonNull
    @Override
    public MetricViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_metric_card, parent, false);
        return new MetricViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MetricViewHolder holder, int position) {
        MetricItem metric = metrics.get(position);
        holder.bind(metric);
    }

    @Override
    public int getItemCount() {
        return metrics.size();
    }

    static class MetricViewHolder extends RecyclerView.ViewHolder {
        private FrameLayout metricIconBackground;
        private ImageView metricIcon;
        private TextView metricValue;
        private TextView metricLabel;
        private TextView metricDetail;
        private View progressFill;

        public MetricViewHolder(@NonNull View itemView) {
            super(itemView);
            metricIconBackground = itemView.findViewById(R.id.metricIconBackground);
            metricIcon = itemView.findViewById(R.id.metricIcon);
            metricValue = itemView.findViewById(R.id.metricValue);
            metricLabel = itemView.findViewById(R.id.metricLabel);
            metricDetail = itemView.findViewById(R.id.metricDetail);
            progressFill = itemView.findViewById(R.id.progressFill);
        }

        public void bind(MetricItem metric) {
            // 根据指标类型设置相应的图标资源
            setMetricIcon(metric.getIcon());
            metricValue.setText(metric.getValue());
            metricLabel.setText(metric.getLabel());
            metricDetail.setText(metric.getDetail());

            // 设置进度条
            ViewGroup.LayoutParams params = progressFill.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup parent = (ViewGroup) progressFill.getParent();
                int parentWidth = parent.getWidth();
                if (parentWidth > 0) {
                    params.width = (int) (parentWidth * (metric.getPercentage() / 100.0));
                }
            }
            progressFill.setLayoutParams(params);

            // 设置进度条颜色
            try {
                progressFill.setBackgroundColor(Color.parseColor(metric.getColor()));
            } catch (IllegalArgumentException e) {
                progressFill.setBackgroundColor(Color.parseColor("#45a0ff"));
            }
        }

        private void setMetricIcon(String iconType) {
            int iconRes;
            int backgroundRes;
            
            String type = iconType != null ? iconType.toLowerCase() : "";
            
            switch (type) {
                case "cpu":
                    iconRes = R.drawable.ic_cpu_white;
                    backgroundRes = R.drawable.metric_icon_cpu;
                    break;
                case "memory":
                case "mem":
                case "内存":
                    iconRes = R.drawable.ic_memory_white;
                    backgroundRes = R.drawable.metric_icon_memory;
                    break;
                case "disk":
                case "硬盘":
                case "磁盘":
                case "存储":
                    iconRes = R.drawable.ic_disk_white;
                    backgroundRes = R.drawable.metric_icon_disk;
                    break;
                case "network":
                case "net":
                case "网络":
                    iconRes = R.drawable.ic_network_white;
                    backgroundRes = R.drawable.metric_icon_network;
                    break;
                case "temperature":
                case "temp":
                case "温度":
                    iconRes = R.drawable.ic_cpu_white; // 温度可以用CPU图标
                    backgroundRes = R.drawable.metric_icon_temperature;
                    break;
                case "load":
                case "负载":
                    iconRes = R.drawable.ic_cpu_white;
                    backgroundRes = R.drawable.metric_icon_cpu;
                    break;
                default:
                    iconRes = R.drawable.ic_cpu_white; // 默认图标
                    backgroundRes = R.drawable.metric_icon_cpu; // 默认背景
                    break;
            }
            
            // 设置图标和背景
            metricIcon.setImageResource(iconRes);
            metricIconBackground.setBackgroundResource(backgroundRes);
        }
    }
}