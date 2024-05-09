package project;

import java.util.ArrayList;
import java.util.List;

public class MessageQueue {
    private final List<String> messages = new ArrayList<>();

    public void addMessage(String message) {
        synchronized (messages) {
            messages.add(message);
        }
    }

    public String getNextMessage() {
        synchronized (messages) {
            if (!messages.isEmpty()) {
                String message = messages.get(0);
                messages.remove(0);
                return message;
            }
            return null;
        }
    }

    public int getSize() {
        return messages.size();
    }
}
