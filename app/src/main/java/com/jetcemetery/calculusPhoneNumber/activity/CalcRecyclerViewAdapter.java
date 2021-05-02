package com.jetcemetery.calculusPhoneNumber.activity;

import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.jetcemetery.androidcalculus.R;

import java.util.List;

public class CalcRecyclerViewAdapter extends RecyclerView.Adapter<CalcRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "CalcRecyclerViewAdapter";
    private List<String> integralResult_Str;
    private LayoutInflater inflatorObj;
    private ItemClickListener clickListenerObj;

    CalcRecyclerViewAdapter(Context context, List<String> data) {
        this.inflatorObj = LayoutInflater.from(context);
        this.integralResult_Str = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflatorObj.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String data = integralResult_Str.get(position);
        holder.myTextView.setText(data);
    }

    @Override
    public int getItemCount() {
        return integralResult_Str.size();
    }

    public void ClearData() {
        //this function shall delete the current data list
        integralResult_Str.clear();
    }

    public void AddDataList(String[] srcData) {
        for(String str : srcData){
            integralResult_Str.add(str);
        }
        notifyDataSetChanged();
    }

    public String[] getList() {
        return integralResult_Str.toArray(new String[integralResult_Str.size()]);
    }

    public void addManyResult(String[] manyResults) {
        for(String str : manyResults){
            integralResult_Str.add(str);
        }
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.txtV_result);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListenerObj != null){
                Log.d(TAG, "Calling onClick");
                clickListenerObj.onItemClick(view, getAdapterPosition());
                Log.d(TAG, "Post Calling onClick");
            }
        }
    }

    String getItem(int id) {
        return integralResult_Str.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.clickListenerObj = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}