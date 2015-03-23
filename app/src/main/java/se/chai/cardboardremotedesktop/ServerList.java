package se.chai.cardboardremotedesktop;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by henrik on 15. 1. 28.
 */
public class ServerList {
    private static final String SERVERLIST_FILENAME = "serverlist.json";
    private static ServerList serverList;
    private ArrayList<ServerData> servers;
    private CardAdapter adapter;

//TODO load server config async
//    private class LoadServersTask extends AsyncTask<Void, Void, Boolean> {
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            if (result == false) {
//
//            }
//        }
//    }

    private ServerList() {

    }

    public static ServerList getServerList() {
        if (serverList == null) {
            serverList = new ServerList();
        }

        return serverList;
    }

    public void load(Context c) {
        servers = new ArrayList<ServerData>();


        try {
            FileInputStream input = c.openFileInput(SERVERLIST_FILENAME);
            JsonReader reader = new JsonReader(new InputStreamReader(input));
            readServerArray(reader);
        } catch (FileNotFoundException e) {
            // Not an error
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAdapter(CardAdapter adapter) {
        this.adapter = adapter;
    }

    private void readServerArray(JsonReader reader) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            servers.add(readServerData(reader));
        }
    }

//    public class ServerData {
//        protected int id;
//        protected String name;
//        protected String host;
//        protected String colormode;
//        protected String username;
//        protected String password;
//        protected int iconResource;
//    }

    private ServerData readServerData(JsonReader reader) throws IOException {
        ServerData data = new ServerData();

        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String jsonname = reader.nextName();
                switch (jsonname) {
                    case "id":
                        data.id = reader.nextString();
                        break;
                    case "name":
                        data.name = reader.nextString();
                        break;
                    case "host":
                        data.host = reader.nextString();
                        break;
                    case "colormode":
                        data.colormode = reader.nextString();
                        break;
                    case "username":
                        data.username = reader.nextString();
                        break;
                    case "password":
                        data.password = reader.nextString();
                        break;
                    case "iconresource":
                        data.iconResource = reader.nextInt();
                        break;
                    case "viewonly":
                        data.viewonly = reader.nextBoolean();
                        break;
                }
            }
            reader.endObject();
        } catch (IllegalStateException e) {
            return null;
        }

        return data;
    }

    public ServerData get(int pos) {
        if (pos >= 0 && pos < servers.size())
            return servers.get(pos);
        else
            return null;
    }

    public void add(ServerData data) {
        int i = servers.indexOf(data);
        if (i != -1) {
            servers.set(i, data);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        } else {
            data.id = UUID.randomUUID().toString();
            servers.add(data);
            if (adapter != null) {
                adapter.notifyItemInserted(servers.size() - 1);
            }
        }
    }

    public void remove(int pos) {
        servers.remove(pos);
        if (adapter != null) {
            adapter.notifyItemRemoved(pos);
        }
    }

    public int size() {
        return servers.size();
    }

//    public class ServerData {
//        protected int id;
//        protected String name;
//        protected String host;
//        protected String colormode;
//        protected String username;
//        protected String password;
//        protected int iconResource;
//    }

    public void save(Context c) {
        try {
            FileOutputStream out = c.openFileOutput(SERVERLIST_FILENAME, Context.MODE_PRIVATE);

            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));
            writer.beginArray();
            for (int i = 0; i < servers.size(); i++) {
                writer.beginObject();
                writer.name("id").value(servers.get(i).id);
                writer.name("name").value(servers.get(i).name);
                writer.name("host").value(servers.get(i).host);
                writer.name("username").value(servers.get(i).username);
                writer.name("password").value(servers.get(i).password);
                writer.name("colormode").value(servers.get(i).colormode);
                writer.name("iconresource").value(servers.get(i).iconResource);
                writer.name("viewonly").value(servers.get(i).viewonly);
                writer.endObject();
            }
            writer.endArray();
            writer.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
