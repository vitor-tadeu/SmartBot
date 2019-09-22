package com.example.smartbot.controller.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.smartbot.R;
import com.example.smartbot.model.Places;
import com.example.smartbot.model.PlacesAPI;

import java.util.List;

public class PlacesViewAdapter extends RecyclerView.Adapter<PlacesViewAdapter.PlacesViewHolder> {
    private final Context context;
    private List<PlacesAPI.Response> mPlacesResponses;
    public List<Places> mPlaces;
    private OnItemClickListener mListener;

    public PlacesViewAdapter(Context context, List<PlacesAPI.Response> responses, List<Places> places, OnItemClickListener mListener) {
        this.context = context;
        this.mPlacesResponses = responses;
        this.mPlaces = places;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.linha_places, parent, false);
        return new PlacesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlacesViewHolder holder, int position) {
        String endereco = "Endereço: " + mPlaces.get(position).getVicinity();
        String distancia = "Distância: " + mPlaces.get(position).getDistancia();
        String tempo = "Tempo Estimado: " + mPlaces.get(position).getTempo();
        String coordenada = mPlaces.get(position).getLat() + "," + mPlaces.get(position).getLng();
        float rating = Float.parseFloat(mPlaces.get(position).getRating());

        holder.mNome.setText(mPlaces.get(position).getName());
        holder.mRating.setText(mPlaces.get(position).getRating());
        holder.mEndereco.setText(endereco);
        holder.mDistancia.setText(distancia);
        holder.mTempo.setText(tempo);
        holder.mCoordenadas.setText(coordenada);
        holder.mRatingBar.setRating(rating);
    }

    @Override
    public int getItemCount() {
        return mPlaces.size();
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mNome;
        private TextView mEndereco;
        private TextView mCoordenadas;
        private TextView mRating;
        private TextView mDistancia;
        private TextView mTempo;
        private RatingBar mRatingBar;

        PlacesViewHolder(View itemView) {
            super(itemView);
            mNome = itemView.findViewById(R.id.txtNome);
            mEndereco = itemView.findViewById(R.id.txtEndereco);
            mCoordenadas = itemView.findViewById(R.id.txtLatLng);
            mRating = itemView.findViewById(R.id.txtRating);
            mDistancia = itemView.findViewById(R.id.txtDistancia);
            mTempo = itemView.findViewById(R.id.txtTempo);
            mRatingBar = itemView.findViewById(R.id.ratingBar);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mListener.OnItemClick(position);
        }
    }
}