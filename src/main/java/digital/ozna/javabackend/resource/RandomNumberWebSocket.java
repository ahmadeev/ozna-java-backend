package digital.ozna.javabackend.resource;

import digital.ozna.javabackend.dto.RandomNumberRequest;
import digital.ozna.javabackend.dto.RandomNumberResponse;
import digital.ozna.javabackend.utils.JSONParser;
import digital.ozna.javabackend.utils.RandomNumberGenerator;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

@ServerEndpoint(value = "/ws/random-numbers")
public class RandomNumberWebSocket {
    // private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private static final Map<Session, ScheduledFuture<?>> RUNNING_TASKS = new ConcurrentHashMap<>();
    private static final Set<String> ACTIVE_SESSIONS = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("[WS] Session opened: " + session.getId());
        ACTIVE_SESSIONS.add(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("[WS] Error: (session: " + session + "): " + throwable.getMessage());
        throwable.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("[WS] Session closed: " + session.getId());
        ACTIVE_SESSIONS.remove(session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("[WS] Received message length (session: " + session.getId() + "): " + message.length());

        RandomNumberRequest randomNumberRequest = JSONParser.parseJSON(message, RandomNumberRequest.class);

        // wa: грубо
        if (message.contains("{\"type\":\"ping\"}")) {
            session.getAsyncRemote().sendText("{\"type\":\"pong\"}");
            return;
        }

        if (randomNumberRequest == null) return;

        if (randomNumberRequest.isRun()) {
            startTask(session, randomNumberRequest);
        } else {
            cancelTask(session);
        }
    }

    private void startTask(Session session, RandomNumberRequest request) {
        cancelTask(session);

        Random random = new Random();

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                RandomNumberResponse response = new RandomNumberResponse(
                        request.getId(),
                        RandomNumberGenerator.generateNumber(random, request.getMax(), request.getMin()),
                        LocalDateTime.now().format(formatter)
                );
                session.getAsyncRemote().sendText(response.toJsonString());
            } catch (Exception e) {
                e.printStackTrace();
                cancelTask(session);
            }
        }, 0, request.getFrequency(), TimeUnit.MILLISECONDS);

        RUNNING_TASKS.put(session, future);
    }

    private void cancelTask(Session session) {
        ScheduledFuture<?> task = RUNNING_TASKS.remove(session);
        if (task != null) {
            task.cancel(true);
        }
    }
}
