package diminishingcoloursbot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotRoleColorHistory {

    private volatile ArrayList<ServerRoleColorHistory> history = new ArrayList<>();

    private boolean success = false;
    private Color response;

    public synchronized void add(long server_id, long user_id, Color color, boolean imposed) {
        success = false;
        history.forEach((server) -> {
            if (server_id == server.getServerId()) {
                server.add(user_id, color, imposed);
                success = true;
            }
        });
        if (!success) {
            history.add(new ServerRoleColorHistory(server_id).add(user_id, color, imposed));
        }
    }

    public synchronized Color rewindLatest(long server_id, long user_id) {
        response = null;
        history.forEach((server) -> {
            if (server.getServerId() == server_id)
                response = server.rewindLatest(user_id);
        });
        return response;
    }

    public synchronized Color getCurrent(long server_id, long user_id) {
        response = null;
        history.forEach((server) -> {
            if (server.getServerId() == server_id)
                server.getAllUsers().forEach((user) -> {
                    if (user.getUserId() == user_id)
                        response = user.getColors()[0];
                });
        });
        return response;
    }

    public synchronized void add(ServerRoleColorHistory serverhistory) {
        history.add(serverhistory);
    }

    public synchronized List<ServerRoleColorHistory> getAllServers() {
        return history;
    }


    /**
     * <p>So this is supposed to track the history of the rolecolors that any user of a server has (this bot doesn't support multiple servers... yet)</p>
     * 
     * <p>the first color in the {@code Color[]} array should be the newest recorded one. </p>
     * 
     * <p>imposed colors (rolecolors set by users other than themselves) are incapable of pushing off the history of non-imposed ones</p>
     * 
     * <p>Any rolecolor changes not made with the slash command is ignored</p>
     */
    public static class ServerRoleColorHistory {

        private volatile ArrayList<UserRoleColorHistory> history = new ArrayList<>();

        private volatile boolean success = false;
        private volatile Color response;

        private long server_id;

        public ServerRoleColorHistory(long server_id) {
            this.server_id = server_id;
        }

        public ServerRoleColorHistory(long server_id, long user_id, Color[] colors, boolean[] imposeds) {
            this.server_id = server_id;
            history.add(new UserRoleColorHistory(user_id, colors, imposeds));
        }

        public long getServerId() {
            return server_id;
        }

        public synchronized ServerRoleColorHistory add(long user_id, Color color, boolean imposed) {
            success = false;
            history.forEach((user) -> {
                if (user_id == user.getUserId()) {
                    user.add(color, imposed);
                    success = true;
                }
            });
            if (!success) {
                history.add(new UserRoleColorHistory(user_id).add(color, imposed));
            }
            return this;
        }

        public synchronized Color rewindLatest(long user_id) {
            response = null;
            history.forEach((user) -> {
                if (user.getUserId() == user_id)
                    response = user.rewindLatest();
            });
            return response;
        }

        public synchronized List<UserRoleColorHistory> getAllUsers() {
            return history;
        }

        public synchronized void fromSave(long user_id, String[] colors, Object[] imposeds) {
            history.forEach((user) -> {
                if (user_id == user.getUserId()) 
                    throw new RuntimeException("Duplicate Users in Save File");
            });
            Color[] actually_colors = Arrays.asList(colors).stream().map(this::mapFromStringToColor).toList().toArray(new Color[0]);


            history.add(new UserRoleColorHistory(user_id, actually_colors, mapFromObjectToBool(imposeds)));
        }

        public Color mapFromStringToColor(String value) {
            try {
                return Color.decode(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        public boolean[] mapFromObjectToBool(Object[] value) {
            boolean[] array = new boolean[3];
            for (int i = 0; i < 3; i++)
                array[i] = ((boolean)value[i]);
            return array;
        }

        protected class UserRoleColorHistory {
            private volatile long user_id;
            private volatile Color[] colors = new Color[3];

            //very big name sense make 
            private boolean[] imposeds = new boolean[3];

            protected UserRoleColorHistory(long user_id, Color[] colors, boolean[] imposeds) {
                this.user_id = user_id;
                this.colors = colors;
                this.imposeds = imposeds;
            }

            protected UserRoleColorHistory(long user_id) {
                this.user_id = user_id;
                colors[0] = null;
                colors[1] = null;
                colors[2] = null;

                imposeds[0] = true;
                imposeds[1] = true;
                imposeds[2] = true;
            }

            protected long getUserId() {
                return user_id;
            }

            protected synchronized Color rewindLatest() {
                colors[0] = colors[1];
                imposeds[0] = imposeds[1];

                colors[1] = colors[2];
                imposeds[1] = imposeds[2];

                colors[2] = null;
                imposeds[2] = true;

                return colors[0];
            }

            protected synchronized UserRoleColorHistory add(Color color, boolean imposed) {
                int temp = -1;
                if (colors[1] == null) 
                    temp = 1;
                else if (colors[2] == null) 
                    temp = 2;
                else if (imposeds[0]) {
                    if (imposeds[2])
                        temp = 2;
                    else if (imposeds[1])
                        temp = 1;
                    else
                        temp = 0;
                } else 
                    temp = 2;
                
                switch (temp) {
                    case 2:
                        colors[2] = colors[1];
                        imposeds[2] = imposeds[1];
                    case 1:
                        colors[1] = colors[0];
                        imposeds[1] = imposeds[0];
                    case 0:
                        colors[0] = color;
                        imposeds[0] = imposed;
                }

                return this;
            }

            protected synchronized Color[] getColors() {
                return colors;
            }

            protected synchronized boolean[] getImposeds() {
                return imposeds;
            }
        }
    }

}
