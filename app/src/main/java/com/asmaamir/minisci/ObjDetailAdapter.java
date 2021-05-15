package com.asmaamir.minisci;


import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ObjDetailAdapter extends RecyclerView.Adapter<ObjDetailAdapter.Holder> {
    public static final String TAG = "ObjDetailAdapter";

    private List<Pair<String, String>> objDetails;

    public ObjDetailAdapter(List<Pair<String, String>> objDetails) {
        this.objDetails = objDetails;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.obj_info_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.detailTitle.setText(objDetails.get(position).first);
        holder.detailContent.setText(objDetails.get(position).second);
    }

    @Override
    public int getItemCount() {
        return objDetails.size();
    }


    public class Holder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView detailTitle;
        TextView detailContent;

        public Holder(View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.obj_info_linear_layout);
            detailTitle = itemView.findViewById(R.id.obj_info_title);
            detailContent = itemView.findViewById(R.id.obj_info_content);
        }
    }
}
