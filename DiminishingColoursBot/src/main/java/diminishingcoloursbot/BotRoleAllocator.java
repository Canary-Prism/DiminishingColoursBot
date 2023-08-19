package diminishingcoloursbot;

import java.awt.Color;
import java.util.ArrayList;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class BotRoleAllocator {

    private final int MAXIMUM = 250;
    private final int BUFFER = 20;

    private BotRoleColorHistory history = new BotRoleColorHistory();

    private ArrayList<ServerRoleAllocator> allocators = new ArrayList<>();

    private boolean success = false;

    public BotRoleAllocator(BotRoleColorHistory history) {
        this.history = history;
    }

    public synchronized void update(Server server) {
        success = false;
        allocators.forEach((allocator) -> {
            if (allocator.server == server) {
                allocator.update();
                success = true;
            }
        });
        if (!success) {
            allocators.add(new ServerRoleAllocator(server).update());
        }
    }
    
    private class ServerRoleAllocator {
        private Server server;

        private ArrayList<Role> color_roles = new ArrayList<>();
        private ArrayList<UserWithColor> users = new ArrayList<>();
        private ArrayList<ExtColor> target_colors = new ArrayList<>();
        private ArrayList<ExtColor> current_colors = new ArrayList<>();

        private Role positioner;

        private int space;
        private ServerRoleAllocator(Server server) {
            this.server = server;
        }

        private ServerRoleAllocator update() {
            var roles = server.getRoles();

            space = (MAXIMUM - roles.size() - BUFFER);
            color_roles.clear();
            roles.forEach((role) -> {
                if (role.getName().startsWith("​")) {
                    color_roles.add(role);
                }
            });

            positioner = null;
            for (var role : roles) {
                if (role.getName().startsWith("‌​‌"))
                    positioner = role;
            }
            if (positioner == null && space < 1) {
                System.out.println("Not enough space to do anything");
                return this;
            }
            if (positioner == null) {
                positioner = server.createRoleBuilder().setName("‌​‌DiminishingColoursBot Role Position").create().join();
            }

            users.clear();
            server.getMembers().forEach((e) -> {
                if (history.getCurrent(server.getId(), e.getId()) != null) {
                    Color current_color = null;
                    for (var role : e.getRoles(server)) {
                        if (role.getName().startsWith("​")) {
                            current_color = role.getColor().get();
                        }
                    }
                    if (current_color == null)
                        current_color = Color.BLACK;
                    users.add(new UserWithColor(e, history.getCurrent(server.getId(), e.getId()), current_color));
                }
            });

            for (int i = 0; i < users.size();) {
                if (users.get(i).getTargetColor().equals(Color.BLACK)) {
                    for (var e : color_roles) {
                        if (e.getName().startsWith("​")) {
                            e.removeUser(users.get(i).getUser()).join();
                        }
                    }
                    users.remove(i);
                } else {
                    i++;
                }
            }

            target_colors.clear();
            users.forEach((e) -> {
                var color = new ExtColor(e.getTargetColor().getRed(), e.getTargetColor().getGreen(), e.getTargetColor().getBlue());
                color.targets.add(e);
                if (!target_colors.contains(e.getTargetColor()))
                    target_colors.add(color);
                else {
                    target_colors.get(target_colors.indexOf(color)).targets.add(e);
                }
            });

            for (int i = 0; i < color_roles.size();) {
                if (!color_roles.get(i).getColor().isPresent()) {
                    color_roles.get(i).delete();
                    color_roles.remove(i);
                } else {
                    i++;
                }
            }

            current_colors.clear();
            color_roles.forEach((e) -> {
                var color = new ExtColor(e.getColor().get().getRed(), e.getColor().get().getGreen(), e.getColor().get().getBlue());
                if (!current_colors.contains(color))
                    current_colors.add(color);
            });

            while (target_colors.size() > space + color_roles.size()) {
                ArrayList<DifferenceHolder> differences = new ArrayList<>();
                for (int i = 0; i < target_colors.size() - 1; i++) {
                    for (int k = i + 1; k < target_colors.size(); k++) {
                        int difference = target_colors.get(i).compare(target_colors.get(k));
                        differences.add(new DifferenceHolder(difference, i, k));
                    }
                }
                DifferenceHolder smallest = differences.get(0);
                for (var difference : differences) {
                    if (difference.difference < smallest.difference)
                        smallest = difference;
                }
                var first = target_colors.get(smallest.a);
                var second = target_colors.get(smallest.b);
                ArrayList<ExtColor> mixes = new ArrayList<>();
                if (first.mixed)
                    mixes.addAll(first.mix_sources);
                else
                    mixes.add(first);
                if (second.mixed)
                    mixes.addAll(second.mix_sources);
                else
                    mixes.add(second);

                int r = 0, g = 0, b = 0;
                for (var mix : mixes) {
                    r += mix.getRed() * mix.getRed();
                    g += mix.getGreen() * mix.getGreen();
                    b += mix.getBlue() * mix.getBlue();
                }

                var new_color = new ExtColor((int) Math.sqrt(r / mixes.size()), (int) Math.sqrt(g / mixes.size()), (int) Math.sqrt(b / mixes.size()));

                new_color.mixed = true;
                new_color.mix_sources.addAll(mixes);
                for (var mix : mixes) {
                    new_color.targets.addAll(mix.targets);

                }
                target_colors.remove(first);
                target_colors.remove(second);
                target_colors.add(new_color);
            }

            for (var color : target_colors) {
                color.targets.forEach((e) -> {
                    e.setDesignatedColor(color);
                });
            }
            
            for (int i = 0; i < color_roles.size(); i++) {
                if (!target_colors.contains(current_colors.get(i))) {
                    color_roles.get(i).delete();
                }
            }
            for (int i = 0; i < target_colors.size(); i++) {
                if (!current_colors.contains(target_colors.get(i))) {
                    var role = server.createRoleBuilder().setName("​").setColor(target_colors.get(i)).create().join();
                    color_roles.add(role);
                    current_colors.add(target_colors.get(i));
                }
            }

            roles = server.getRoles();
            var mod_roles = new ArrayList<Role>();
            mod_roles.addAll(roles);
            var temp_roles = new ArrayList<Role>();
            for (int i = 0; i < mod_roles.size();) {
                if (mod_roles.get(i).getName().startsWith("​")) {
                    temp_roles.add(mod_roles.get(i));
                    mod_roles.remove(i);
                } else {
                    i++;
                }
            }
            var positionposition = mod_roles.indexOf(positioner);
            for (int i = temp_roles.size() - 1; i >= 0; i--) {
                mod_roles.add(positionposition, temp_roles.get(i));
            }

            server.reorderRoles(mod_roles).join();

            for (var user : users) {
                if (!user.getDesignatedColor().equals(user.getCurrentColor())) {
                    for (var role : user.getUser().getRoles(server)) {
                        if (role.getName().startsWith("​")) {
                            role.removeUser(user.getUser()).join();
                        }
                    }
                    color_roles.get(current_colors.indexOf(user.getDesignatedColor())).addUser(user.getUser()).join();
                }
            }
            
            return this;
        }

        private class UserWithColor {
            private User user;
            
            private Color target_color;
            private Color designated_color;
            private Color current_color;

            private User getUser() {
                return user;
            }


            private Color getTargetColor() {
                return target_color;
            }

            private Color getDesignatedColor() {
                return designated_color;
            }

            private void setDesignatedColor(Color designated_color) {
                this.designated_color = designated_color;
            }

            private Color getCurrentColor() {
                return current_color;
            }

            private UserWithColor(User user, Color target_color, Color current_color) {
                this.user = user;
                this.target_color = target_color;
                this.current_color = current_color;
            }
        }
        private static class ExtColor extends Color {

            private ArrayList<UserWithColor> targets = new ArrayList<>();

            private boolean mixed = false;
            private ArrayList<ExtColor> mix_sources = new ArrayList<>();

            public ExtColor(int r, int g, int b) {
                super(r, g, b);
            }

            public int[] getSeparateRGB() {
                return new int[] {getRed(), getGreen(), getBlue()};
            }

            public int compare(ExtColor second_color) {
                int[] first_color = getSeparateRGB();
                int[] second_color_array = second_color.getSeparateRGB();
                int difference = 0;
                for (int i = 0; i < 3; i++) {
                    difference += Math.abs(first_color[i] - second_color_array[i]);
                }
                return difference;
            }
        }

        private class DifferenceHolder {
            private int difference;
            private int a, b;

            public DifferenceHolder(int difference, int a, int b) {
                this.difference = difference;
                this.a = a;
                this.b = b;
            }
        }
    }
}
