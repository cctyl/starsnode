package io.github.cctyl.starnode.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.github.cctyl.starnode.R;
import io.github.cctyl.starnode.model.StatItem;
import java.util.List;

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.StatViewHolder> {
    private List<StatItem> statItems;

    public StatsAdapter(List<StatItem> statItems) {
        this.statItems = statItems;
    }

    @NonNull
    @Override
    public StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stat_card, parent, false);
        return new StatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatViewHolder holder, int position) {
        StatItem item = statItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return statItems.size();
    }

    public void updateStats(List<StatItem> newStats) {
        this.statItems = newStats;
        notifyDataSetChanged();
    }

    static class StatViewHolder extends RecyclerView.ViewHolder {
        private ImageView statIcon;
        private TextView statNumber;
        private TextView statLabel;

        public StatViewHolder(@NonNull View itemView) {
            super(itemView);
            statIcon = itemView.findViewById(R.id.statIcon);
            statNumber = itemView.findViewById(R.id.statNumber);
            statLabel = itemView.findViewById(R.id.statLabel);
        }

        public void bind(StatItem item) {
            // 根据标签设置不同的图标
            int iconRes = getIconResource(item.getLabel());
            statIcon.setImageResource(iconRes);
            
            statNumber.setText(item.getNumber());
            statLabel.setText(item.getLabel());
            
            // 根据标签设置不同的背景渐变
            int backgroundRes = getBackgroundResource(item.getLabel());
            if (backgroundRes != 0) {
                itemView.findViewById(R.id.statCardBackground).setBackgroundResource(backgroundRes);
            }
        }
        
        private int getIconResource(String label) {
            switch (label) {
                case "在线设备":
                    return R.drawable.ic_devices_white;
                case "Windows":
                    return R.drawable.ic_windows_white;
                case "Linux":
                    return R.drawable.ic_linux_white;
                case "CPU核心":
                    return R.drawable.ic_cpu_white;
                case "总内存":
                    return R.drawable.ic_memory_white;
                default:
                    return R.drawable.ic_devices_white;
            }
        }
        
        private int getBackgroundResource(String label) {
            switch (label) {
                case "在线设备":
                    return R.drawable.gradient_stat_online;
                case "Windows":
                    return R.drawable.gradient_stat_windows;
                case "Linux":
                    return R.drawable.gradient_stat_linux;
                case "CPU核心":
                    return R.drawable.gradient_stat_cpu;
                case "总内存":
                    return R.drawable.gradient_stat_memory;
                default:
                    return R.drawable.gradient_stat_online;
            }
        }
    }
}