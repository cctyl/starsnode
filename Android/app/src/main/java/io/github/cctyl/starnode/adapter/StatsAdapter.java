package io.github.cctyl.starnode.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        private TextView statIcon;
        private TextView statNumber;
        private TextView statLabel;

        public StatViewHolder(@NonNull View itemView) {
            super(itemView);
            statIcon = itemView.findViewById(R.id.statIcon);
            statNumber = itemView.findViewById(R.id.statNumber);
            statLabel = itemView.findViewById(R.id.statLabel);
        }

        public void bind(StatItem item) {
            statIcon.setText(item.getIcon());
            statNumber.setText(item.getNumber());
            statLabel.setText(item.getLabel());
            
            // 根据标签设置不同的背景渐变
            int backgroundRes = getBackgroundResource(item.getLabel());
            if (backgroundRes != 0) {
                itemView.setBackgroundResource(backgroundRes);
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