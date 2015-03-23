package se.chai.cardboardremotedesktop;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by henrik on 15. 1. 26.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    //private List<ServerData> serverData;

    public CardAdapter() {
        ServerList.getServerList().setAdapter(this);
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);
        return new CardViewHolder(v, new ImyOnClickListener() {
            @Override
            public void removeAt(int pos) {
                ServerList.getServerList().remove(pos);
                //notifyItemRemoved(pos);
            }
        });
    }

    @Override
    public void onBindViewHolder(CardViewHolder cardViewHolder, int i) {
        ServerData data = ServerList.getServerList().get(i);
        cardViewHolder.icon.setImageResource(R.drawable.screen_icon);
        cardViewHolder.name.setText(data.name);
        cardViewHolder.uri.setText(data.host);
    }

    @Override
    public int getItemCount() {
        return ServerList.getServerList().size();
    }

    public static interface ImyOnClickListener {
        public void removeAt(int pos);
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView icon;
        public ImageView editicon;
        public ImageView deleteicon;
        public TextView name;
        public TextView uri;
        private ImyOnClickListener myListener;

        public CardViewHolder(View itemView, ImyOnClickListener listener) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.server_icon);
            name = (TextView) itemView.findViewById(R.id.server_name);
            uri = (TextView) itemView.findViewById(R.id.server_uri);

            editicon = (ImageView) itemView.findViewById(R.id.server_edit_icon);
            editicon.setImageResource(R.drawable.pen);
            editicon.setOnClickListener(this);

            deleteicon = (ImageView) itemView.findViewById(R.id.server_delete_icon);
            deleteicon.setImageResource(R.drawable.trash);
            deleteicon.setOnClickListener(this);

            myListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == editicon) {
                System.out.println("Launch edit intent");
                ServerData data = ServerList.getServerList().get(getPosition());
                Intent myIntent = new Intent(v.getContext(), EditActivity.class);
                myIntent.putExtra("id", data.id);
                myIntent.putExtra("name", data.name);
                myIntent.putExtra("host", data.host);
                myIntent.putExtra("username", data.username);
                myIntent.putExtra("password", data.password);
                myIntent.putExtra("colormode", data.colormode);
                myIntent.putExtra("viewonly", data.viewonly);

                v.getContext().startActivity(myIntent);
            } else if (v == deleteicon) {
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                //alert.setTitle("Delete ");
                alert.setMessage("Are you sure you want to delete this server configuration?");

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Your action here
                        myListener.removeAt(getPosition());
                    }
                });

                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        });

                alert.show();
            } else {
                System.out.println("Launch display intent");
                Intent myIntent = new Intent(v.getContext(), DisplayActivity.class);
                myIntent.putExtra("dimension", "2d"); // 2d

                myIntent.putExtra("screenType", "Flat"); // oau
//                myIntent.putExtra("videoType", videoTypeValue); // 0, 180, 360
                myIntent.putExtra("projectionType", "Square");
//                myIntent.putExtra("datasource", datasource);

                ServerData data = ServerList.getServerList().get(getPosition());
                myIntent.putExtra("host", data.host);
                myIntent.putExtra("username", data.username);
                myIntent.putExtra("password", data.password);
                myIntent.putExtra("colormode", data.colormode);
                myIntent.putExtra("viewonly", data.viewonly);

                v.getContext().startActivity(myIntent);
            }
        }
    }
}
