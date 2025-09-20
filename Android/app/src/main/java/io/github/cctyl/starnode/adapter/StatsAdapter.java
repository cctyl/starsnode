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
        }
    }
}