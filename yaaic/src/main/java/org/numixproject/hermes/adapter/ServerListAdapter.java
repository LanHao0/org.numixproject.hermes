/*
Yaaic - Yet Another Android IRC Client

Copyright 2009-2013 Sebastian Kaspari

This file is part of Yaaic.

Yaaic is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Yaaic is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Yaaic.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.numixproject.hermes.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.numixproject.hermes.MainActivity;
import org.numixproject.hermes.R;
import org.numixproject.hermes.Hermes;
import org.numixproject.hermes.activity.ConversationActivity;
import org.numixproject.hermes.model.Channel;
import org.numixproject.hermes.model.Conversation;
import org.numixproject.hermes.model.Server;
import org.w3c.dom.Text;
import org.numixproject.hermes.utils.adapterHeight;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.IntegerRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Adapter for server lists
 * 
 * @author Sebastian Kaspari <sebastian@yaaic.org>
 */
public class ServerListAdapter extends BaseAdapter
{
    private static final int COLOR_CONNECTED    = Color.parseColor("#8bc34a");
    private static final int COLOR_DISCONNECTED = Color.parseColor("#9E9E9E");

    private ArrayList<Server> servers;
    ArrayList<String> channels = new ArrayList<String>();
    ArrayList<String> query = new ArrayList<String>();
    /**
     * Create a new adapter for server lists
     */
    public ServerListAdapter()
    {
        loadServers();
    }

    /**
     * Load servers from database
     *
     * Delegate call to yaaic instance
     */
    public void loadServers()
    {
        servers = Hermes.getInstance().getServersAsArrayList();
        notifyDataSetChanged();
    }

    /**
     * Get number of items
     */
    @Override
    public int getCount()
    {
        int size = servers.size();

        // Display "Add server" item
        if (size == 0) {
            return 1;
        }

        return size;
    }

    /**
     * Get item at position
     * 
     * @param position
     */
    @Override
    public Server getItem(int position)
    {
        if (servers.size() == 0) {
            return null; // No server object for the "add server" view
        }

        return servers.get(position);
    }

    /**
     * Get id of item at position
     * 
     * @param position
     */
    @Override
    public long getItemId(int position)
    {
        if (servers.size() == 0) {
            return 0;
        }

        return getItem(position).getId();
    }

    /**
     * Get view for item at given position
     * 
     * @param position
     * @param convertView
     * @param parent
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        Server server = getItem(position);
        final Context mcontext = parent.getContext();

        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (server == null) {
            // Return "Add server" view
            return inflater.inflate(R.layout.addserveritem, null);
        }

        View v = inflater.inflate(R.layout.serveritem, null);

        TextView titleView = (TextView) v.findViewById(R.id.title);
        titleView.setText(server.getTitle());

        TextView serverStatus = (TextView) v.findViewById(R.id.text_ServerStatus);

        ListView roomsList = (ListView) v.findViewById(R.id.rooms_list);

        // Two main strings used in adapter for Rooms and Mentions
        ArrayList<String> RoomsList = new ArrayList<String>();
        ArrayList<Integer> MentionsList = new ArrayList<Integer>();

        channels = server.getCurrentChannelNames();
        query = server.getCurrentQueryNames();


        for (int i = 0; i < channels.size(); i++) {
            try {
                Conversation conversation = server.getConversation(channels.get(i));
                // Only scroll to new conversation if it was selected before
                int Mentions = conversation.getNewMentions();


                if (Mentions == 1) {
                    RoomsList.add(channels.get(i));
                    MentionsList.add(Mentions);
                } else if (Mentions == 0) {
                    // s += channels.get(i) + "\n";
                    RoomsList.add(channels.get(i));
                    MentionsList.add(Mentions);
                } else {
                   // s += channels.get(i) + "(" + Mentions + " mentions)" +  "\n";
                    RoomsList.add(channels.get(i));
                    MentionsList.add(Mentions);
                }
            } catch (Exception E) {
                // Do nothing
            }
        }

        // Set Adapter to Rooms/Mentions list
        roomsList.setAdapter(new mentionsAdapter(RoomsList, MentionsList));

        // ListView workaround to fix height
        adapterHeight.setListViewHeightBasedOnChildren(roomsList);

        // Show MP detsils in Server Card
        String t = "";

        for (int i = 0; i < query.size(); i++) {
            Conversation queries = null;
            try {
                queries = server.getConversation(query.get(i));
            } catch (Exception e) {
                // Do nothing;
            }
            int Queries = 0;
            String QueriesName = null;
            try {
                Queries = queries.getNewMentions();
                QueriesName = queries.getName();
            } catch (Exception e) {
                // do nothing
            }
            if (Queries == 0) {
                t += QueriesName + " (0 messages)" + "\n";
            } else if (Queries == 1) {
                t += QueriesName + " (1 message)" + "\n";
            } else {
                t += QueriesName + " (" + Queries + " messages)" + "\n";
            }
        }

        // Show MP in general counter Server Card
     //   int counter = 0;

   //     for (int i = 0; i < query.size(); i++) {
    //        int Queries = 0;
     //       try {
       //         Queries = server.getConversation(query.get(i)).getNewMentions();
         //       counter = Queries + counter;
           // } catch (Exception e) {
             //   // do nothing
           // }
       // }


         //   mpCounterTextView.setText(counter);

        // MOVED: More button top left of server card
        //final ImageView moreButton = (ImageView) v.findViewById(R.id.moreButton);

        //moreButton.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View v) {
        //        ((MainActivity)mcontext).onCardMoreClicked(position);
        //    }
        // });

        // Final Add new Room button in server card
        final LinearLayout connectNewRoom = (LinearLayout) v.findViewById(R.id.connectNewRoom);
        connectNewRoom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity)mcontext).openServerWithNewRoom(position);
            }
        });

        // If Rooms list is empty, don't display the section in Server cards.
        if(channels.isEmpty())
        {
        } else {
        }

        // If connected on server, set card color
        if (server.isConnected()) {
            serverStatus.setTextColor(COLOR_CONNECTED);
            serverStatus.setText("CONNECTED");
        } else {
            serverStatus.setTextColor(COLOR_DISCONNECTED);
            serverStatus.setText("DISCONNECTED");
        }

        return v;
    }

    // Adapter for Room/Mentions ListView
    class mentionsAdapter extends BaseAdapter {
        ArrayList<String> Room;
        ArrayList<Integer> Mentions;

        mentionsAdapter() {
            Room = null;
            Mentions = null;
        }

        public mentionsAdapter(ArrayList<String> text, ArrayList<Integer> text1) {
            Room = text;
            Mentions = text1;
        }

        public int getCount() {
            // TODO Auto-generated method stub
            return Room.size();
        }

        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row;
            row = inflater.inflate(R.layout.serverchannel_item, parent, false);
            TextView room, mentions;
            room = (TextView) row.findViewById(R.id.room_name);
            mentions = (TextView) row.findViewById(R.id.mentions_number);
            room.setText(Room.get(position));
            try {
                mentions.setText(Mentions.get(position));
            } catch (Exception E) {
                // Do nothing
            }
            return (row);
        }
    }
}
