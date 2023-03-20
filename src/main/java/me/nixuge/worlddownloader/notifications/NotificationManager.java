package me.nixuge.worlddownloader.notifications;

import java.util.concurrent.LinkedBlockingQueue;

public class NotificationManager {
    private static LinkedBlockingQueue<Notification> pendingNotifications = new LinkedBlockingQueue<>();
    private static Notification currentNotification = null;

    public static void show(Notification notification) {
        System.out.println("new notification!!!");
        pendingNotifications.add(notification);
    }

    public static void update() {
        if (currentNotification != null && !currentNotification.isShown()) {
            currentNotification = null;
        }

        if (currentNotification == null && !pendingNotifications.isEmpty()) {
            currentNotification = pendingNotifications.poll();
            currentNotification.show();
        }

    }

    public static void render(float partialTicks) {
        update();

        if (currentNotification != null)
            // currentNotification.draw(partialTicks);
            currentNotification.render();
            // currentNotification.render();
    }
}
