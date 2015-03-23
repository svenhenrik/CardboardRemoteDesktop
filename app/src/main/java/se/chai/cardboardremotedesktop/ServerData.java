package se.chai.cardboardremotedesktop;

/**
 * Created by henrik on 15. 1. 26.
 */
public class ServerData {
    String id = "";
    String name = "";
    String host = "";
    String colormode = "";
    String username = "";
    String password = "";
    boolean viewonly = false;

    int iconResource = R.drawable.vnc_button;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ServerData))
            return false;
        ServerData data = (ServerData) obj;

        if (data.id.equals(id)) return true;
        else return false;
    }

}
