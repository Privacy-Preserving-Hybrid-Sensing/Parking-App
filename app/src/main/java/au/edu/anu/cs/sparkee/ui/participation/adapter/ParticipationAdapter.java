package au.edu.anu.cs.sparkee.ui.participation.adapter;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.model.Participation;
import au.edu.anu.cs.sparkee.ui.map.overlay.marker.ParkingSpotMarker;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;

import org.ocpsoft.prettytime.PrettyTime;
import org.threeten.bp.ZoneId;

import java.sql.Timestamp;
import java.util.List;

import static au.edu.anu.cs.sparkee.Constants.MARKER_PARKING_AVAILABLE_CONFIDENT_3_DEFAULT;
import static au.edu.anu.cs.sparkee.Constants.MARKER_PARKING_UNAVAILABLE_CONFIDENT_3_DEFAULT;

public class ParticipationAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final String TAG = "ParticipationAdapter";
    public static final int VIEW_TYPE_EMPTY = 0;
    public static final int VIEW_TYPE_NORMAL = 1;
    private Callback mCallback;
    private List<Participation> mActivityList;
    public ParticipationAdapter(List<Participation> activityList) {
        mActivityList = activityList;
    }
    public void setCallback(Callback callback) {
        mCallback = callback;
    }
    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_activity, parent, false));
            case VIEW_TYPE_EMPTY:
            default:
                return new EmptyViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_empty_participation, parent, false));
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (mActivityList != null && mActivityList.size() > 0) {
            return VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_EMPTY;
        }
    }
    @Override
    public int getItemCount() {
        if (mActivityList != null && mActivityList.size() > 0) {
            return mActivityList.size();
        } else {
            return 1;
        }
    }
    public void addItems(List<Participation> participationList) {
        for(int i = mActivityList.size(); i < participationList.size(); i++) {
            mActivityList.add(i, participationList.get(i));
        }
        notifyDataSetChanged();
    }
    public interface Callback {
        void onEmptyViewRetryClick();
    }
    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.img_previous)
        ImageView imgPrevious;
        @BindView(R.id.img_participation)
        ImageView imgParticipation;
        @BindView(R.id.txt_spot_id)
        TextView txtSpotId;
        @BindView(R.id.txt_credit)
        TextView txtCredit;
        @BindView(R.id.txt_zone_name)
        TextView txtZoneName;
        @BindView(R.id.txt_ts_update)
        TextView txtTsUpdate;

        View itemView;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
        }
        protected void clear() {
            imgPrevious.setImageDrawable(null);
            imgParticipation.setImageDrawable(null);
            txtSpotId.setText("");
            txtCredit.setText("");
            txtZoneName.setText("");
            txtTsUpdate.setText("");
        }

        public void onBind(int position) {
            super.onBind(position);
            final Participation mActivity = mActivityList.get(position);


            int iconPrevious = ParkingSpotMarker.getMarkerIcon(mActivity.getPrevious_value(), Constants.MARKER_PARKING_CATEGORY_DEFAULT);
            Drawable dPrevious = itemView.getResources().getDrawable(iconPrevious);
            Glide.with(itemView.getContext())
                    .load(dPrevious)
                    .into(imgPrevious);

            int marker_status = mActivity.getParticipation_value() > 0 ? MARKER_PARKING_AVAILABLE_CONFIDENT_3_DEFAULT : MARKER_PARKING_UNAVAILABLE_CONFIDENT_3_DEFAULT;
            int iconParticipation = ParkingSpotMarker.getMarkerIcon(marker_status, Constants.MARKER_PARKING_CATEGORY_DEFAULT);
            Drawable dParticipation = itemView.getResources().getDrawable(iconParticipation);
            Glide.with(itemView.getContext())
                    .load(dParticipation)
                    .into(imgParticipation);

            if (mActivity.getSpot_name()  != null) {
                txtSpotId.setText(mActivity.getSpot_name());
            }

            int val = mActivity.getIncentive_value();
            String incentive = val == 0 ? "( 0 )" : "( +" + val + " )";
            txtCredit.setText( incentive );

            if (mActivity.getZone_name() != null) {
                txtZoneName.setText(mActivity.getZone_name());
            }

            if (mActivity.getTs_update() != null) {

                PrettyTime p = new PrettyTime();
                String date_str = mActivity.getTs_update().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                String time_str = mActivity.getTs_update().atZone(ZoneId.systemDefault()).toLocalTime().toString();
                String time_pretty = "-";
                try {
                    time_pretty = p.format(Timestamp.valueOf( date_str + " " + time_str ));
                }
                catch(IllegalArgumentException iae) {
                    iae.printStackTrace();
                    Log.d("ERR", "Time Problem");
                }
                txtTsUpdate.setText(time_pretty);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("CLICK", "HORE");
                }
            });

        }
    }

    public class EmptyViewHolder extends BaseViewHolder {
        @BindView(R.id.txt_message)
        TextView txtMessage;

        EmptyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind(int position) {
            super.onBind(position);
            txtMessage.setText("No Participation");
        }
        @Override
        protected void clear() {
        }
    }
}