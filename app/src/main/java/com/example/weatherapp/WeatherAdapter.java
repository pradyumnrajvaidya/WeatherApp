package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder>{
    private final Context context;
    private final ArrayList<WeatherModel> weatherModels;

    public WeatherAdapter(Context context, ArrayList<WeatherModel> weatherModels) {
        this.context = context;
        this.weatherModels = weatherModels;
    }

    @NonNull
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.weather_item,parent,false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.ViewHolder holder, int position) {
        WeatherModel model = weatherModels.get(position);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat output = new SimpleDateFormat("hh-mm aa");
        try{
            Date time = input.parse(model.getTime());
            assert time != null;
            holder.textTime.setText(output.format(time));
        } catch (Exception e){
            e.printStackTrace();
        }
        holder.textTemperature.setText(model.getTemperature() + "°C");
        holder.textWindSpeed.setText(model.getWindSpeed() + "km/h");
        Picasso.get().load("https:".concat(model.getIcon())).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return weatherModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTime,textTemperature,textWindSpeed;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTime = itemView.findViewById(R.id.viewTime);
            textTemperature = itemView.findViewById(R.id.viewTemperature);
            textWindSpeed = itemView.findViewById(R.id.viewWindSpeed);
            imageView = itemView.findViewById(R.id.viewImage);
        }
    }
}
