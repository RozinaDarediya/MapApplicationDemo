package com.theta.mapapplication.map_application.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.theta.mapapplication.R;
import com.theta.mapapplication.map_application.model.RouteStepInfo;

import java.util.List;

/**
 * Created by ashish on 18/12/17.
 */

public class RootInfoAdapter extends RecyclerView.Adapter<RootInfoAdapter.ViewHolder> {

    private Context mContext;
    private List<RouteStepInfo> mItems;
    private ItemListener mListener;

    public void setData(List<RouteStepInfo> mItems){
        this.mItems = mItems;
    }
    public RootInfoAdapter(Context mContext, List<RouteStepInfo> items, ItemListener listener) {
        this.mContext = mContext;
        mItems = items;
        mListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        RouteStepInfo stepInfo = mItems.get(position);

        holder.textView.setText((Html.fromHtml(stepInfo.getHtml_instructions())));
        String dist;
        String dur;
        if (stepInfo.getDistance() != "" || stepInfo.getDistance() != null) {
            dist = stepInfo.getDistance();
        } else dist = "";
        if (stepInfo.getDuration() != "" || stepInfo.getDuration() != null) {
            dur = stepInfo.getDuration();
        } else dur = "";
        holder.tvDistance.setText(dist + "(" + dur + ")");

        if (stepInfo.getManeuver() != null ) {
            if (stepInfo.getManeuver().contains("-")) {
                String image =stepInfo.getManeuver().replace("-", "_");
                int resourceIdentifier = mContext.getResources().getIdentifier(image, "drawable", mContext.getPackageName());
                holder.ivIcon.setImageResource(resourceIdentifier);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textView;
        TextView tvDistance;
        ImageView ivIcon;
        String item;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            textView = (TextView) itemView.findViewById(R.id.textView);
            tvDistance = (TextView) itemView.findViewById(R.id.tvDistance);
            ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(item);
            }
        }
    }

    public interface ItemListener {
        void onItemClick(String item);
    }
}

