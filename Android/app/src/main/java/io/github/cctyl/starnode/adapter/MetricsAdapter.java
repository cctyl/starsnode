package io.github.cctyl.starnode.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
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
        private ImageView metricIcon;
        private TextView metricValue;
        private TextView metricLabel;
        private TextView metricDetail;
        private View progressFill;

        public MetricViewHolder(@NonNull View itemView) {
            super(itemView);
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
            switch (iconType != null ? iconType.toLowerCase() : "") {
                case "cpu":
                    iconRes = R.drawable.ic_cpu_white;
                    break;
                case "memory":
                case "mem":
                    iconRes = R.drawable.ic_memory_white;
                    break;
                case "disk":
                    iconRes = R.drawable.ic_disk_white;
                    break;
                case "network":
                case "net":
                    iconRes = R.drawable.ic_network_white;
                    break;
                default:
                    iconRes = R.drawable.ic_cpu_white; // 默认图标
                    break;
            }
            metricIcon.setImageResource(iconRes);
        }
    }
}