package com.example.smartbot.controller.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smartbot.R;
import com.example.smartbot.model.Sensor;

import java.util.ArrayList;
import java.util.List;

public class SensorViewAdapter extends RecyclerView.Adapter<SensorViewAdapter.SensorViewHolder> {
    private final Context context;
    private List<Sensor> mSensors;
    private ArrayList<String> nomeSensor;
    private ArrayList<Integer> image;
    private OnItemClickListener mListener;

    public SensorViewAdapter(Context context, List<Sensor> mSensors, ArrayList<String> nomeSensor, ArrayList<Integer> image, OnItemClickListener mListener) {
        this.context = context;
        this.mSensors = mSensors;
        this.nomeSensor = nomeSensor;
        this.image = image;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.linha_sensor, parent, false);
        return new SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SensorViewHolder holder, int position) {
        holder.mNomeSensor.setText(nomeSensor.get(position));
        holder.mImage.setImageResource(image.get(position));
    }

    @Override
    public int getItemCount() {
        return mSensors.size();
    }

    class SensorViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mNomeSensor;
        private ImageView mImage;

        SensorViewHolder(View itemView) {
            super(itemView);
            mNomeSensor = itemView.findViewById(R.id.txtNomeSensor);
            mImage = itemView.findViewById(R.id.imageSensor);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mListener.OnItemClick(position);
        }
    }
}