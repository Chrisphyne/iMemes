package imemes.domain.com.imemes_meme_creator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author cubycode
 * @since 05/08/2018
 * All rights reserved
 */
public class MemesAdapter extends RecyclerView.Adapter<MemesAdapter.MemesVH> {

    private OnImageSelectedListener onSelectedListener;
    private Context context;

    public MemesAdapter(Context context,  OnImageSelectedListener onSelectedListener) {
         this.context = context;
        this.onSelectedListener = onSelectedListener;
    }


    @NonNull
    @Override
    public MemesVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.meme_cell, parent, false);
        return new MemesVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MemesVH holder, int position) {
        // Set title
        holder.titletxt.setText(Configs.memeTitles[position]);

        // Set Image
        holder.memeImg.setImageResource(Configs.memeImages[position]);
        ViewCompat.setTransitionName(holder.memeImg, Configs.memeTitles[position]);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectedListener.onImageSelected(holder.getAdapterPosition(), holder.memeImg);
            }
        });
    }

    @Override
    public int getItemCount() {
        return Configs.memeTitles.length;
    }

    public class MemesVH extends RecyclerView.ViewHolder  {

        private ImageView memeImg;
        private TextView titletxt;

        public MemesVH(View cell) {
            super(cell);
             titletxt =  cell.findViewById(R.id.titleTxt);
            memeImg =  cell.findViewById(R.id.memeImage);
        }

    }

    public interface OnImageSelectedListener {

      void onImageSelected(int adapterPosition, ImageView memeImg);
    }
}
