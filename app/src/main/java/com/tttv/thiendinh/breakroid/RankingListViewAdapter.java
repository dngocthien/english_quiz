package com.tttv.thiendinh.breakroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RankingListViewAdapter extends ArrayAdapter<User> implements View.OnClickListener {

    private ArrayList<User> dataSet;
    Context mContext;

    private static class ViewHolder {
        private TextView txt_left, txt_middle, txt_right;
        private ImageView img_avatar;
    }

    public RankingListViewAdapter(ArrayList<User> data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public void onClick(View v) {

        int position = (Integer) v.getTag();
        Object object = getItem(position);
        User dataModel = (User) object;

    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        User dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.txt_left = (TextView) convertView.findViewById(R.id.item_left);
            viewHolder.txt_middle = (TextView) convertView.findViewById(R.id.item_middle);
            viewHolder.txt_right = (TextView) convertView.findViewById(R.id.item_right);
            viewHolder.img_avatar = (ImageView) convertView.findViewById(R.id.img_avatar);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.top_down : R.anim.bottom_up);
        result.startAnimation(animation);
        lastPosition = position;

        if (dataModel.getMy_avatar()!=null)
            Picasso.get().load(dataModel.getMy_avatar()).into(viewHolder.img_avatar);
        else Picasso.get().load(R.drawable.ic_person_black_24dp).into(viewHolder.img_avatar);
        viewHolder.txt_left.setText((position + 1) + "");
        viewHolder.txt_middle.setText(dataModel.getMy_name() + "");
        if (dataModel.getStars() > 1000000)
            viewHolder.txt_right.setText(dataModel.getStars() / 1000000 + "M");
        else if (dataModel.getStars() > 1000)
            viewHolder.txt_right.setText(dataModel.getStars() / 1000 + "K");
        else viewHolder.txt_right.setText(dataModel.getStars() + "");
        return convertView;
    }
}