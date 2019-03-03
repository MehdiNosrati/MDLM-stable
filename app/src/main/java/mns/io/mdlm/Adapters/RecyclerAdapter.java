package mns.io.mdlm.Adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;


import mns.io.mdlm.R;
import mns.io.mdlm.models.DownloadFile;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private List<DownloadFile> mDataset = new ArrayList<DownloadFile>();



    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView fileName;

        public MyViewHolder(ViewGroup v) {
            super(v);
            fileName = v.findViewById(R.id.file_name);

        }
    }


    public RecyclerAdapter(List<DownloadFile> myDataset) {

        if (myDataset != null) {
            mDataset = myDataset;
        }

    }


    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.download, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.fileName.setText(mDataset.get(position).getFName());


    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}