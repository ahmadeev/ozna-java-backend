package digital.ozna.javabackend.resource;

import digital.ozna.javabackend.dto.RandomNumberRequest;
import digital.ozna.javabackend.dto.RandomNumberResponse;
import digital.ozna.javabackend.exception.InvalidSessionException;
import digital.ozna.javabackend.utils.JSONParser;
import digital.ozna.javabackend.utils.RandomNumberGenerator;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

@ServerEndpoint(value = "/ws/random-numbers")
public class RandomNumberWebSocket {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // sessionId : run
    // Future -- аналог промисов в js
    private static final Map<String, Future<?>> RUNNING_TASKS = new ConcurrentHashMap<>();

    private static final Set<String> ACTIVE_SESSIONS = ConcurrentHashMap.newKeySet();

    // Пул потоков для управления задачами
    private static final ExecutorService executor = Executors.newCachedThreadPool();

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

        synchronized (session.getId().intern()) {
            RandomNumberRequest request = JSONParser.parseJSON(message, RandomNumberRequest.class);

            if (request == null) return;

            if (request.isRun()) {
                stopExistingTask(session.getId());

                Random random = new Random();

                Future<?> future = executor.submit(() -> {
                    try {
                        while (!Thread.currentThread().isInterrupted() && session.isOpen()) {
                            RandomNumberResponse response = new RandomNumberResponse(
                                    request.getId(),
                                    RandomNumberGenerator.generateNumber(random, request.getMax(), request.getMin()),
                                    LocalDateTime.now().format(formatter)
                            );

                            if (!session.isOpen()) throw new InvalidSessionException("Session is closed");

                        /*try {
                            session.getBasicRemote().sendText(response.toJsonString());
                        } catch (IOException e) {
                            System.out.println("[WS] Failed to send message for session: " + session.getId() + ": " + e.getMessage());
                            // return; // Завершаем задачу, если отправка не удалась
                        }*/

                            session.getAsyncRemote().sendText(response.toJsonString(), result -> {
                                if (!result.isOK()) {
                                    System.err.println("[WS] Failed to send (async) for session: " + session.getId());
                                    result.getException().printStackTrace();
                                    stopExistingTask(session.getId());
                                }
                            });

                            try {
                                Thread.sleep(request.getFrequency());
                            } catch (InterruptedException e) {
                                System.out.println("[WS] Task interrupted for session: " + session.getId());
                                Thread.currentThread().interrupt(); // Восстанавливаем состояние прерывания
                                return;
                            } catch (Exception e) {
                                System.err.println("[WS] Unexpected error for session: " + session.getId() + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    } catch (InvalidSessionException e) {
                        System.out.println("[WS] Invalid session: " + session.getId());
                        stopExistingTask(session.getId());
                        ACTIVE_SESSIONS.remove(session.getId());
                    } catch (Exception e) {
                        stopExistingTask(session.getId());
                        e.printStackTrace();
                    } finally {
                        stopExistingTask(session.getId());
                    }
                });

                RUNNING_TASKS.put(session.getId(), future);
            } else {
                stopExistingTask(session.getId());
            }
        }


    }

    private void stopExistingTask(String sessionId) {
        Future<?> existingTask = RUNNING_TASKS.get(sessionId);
        if (existingTask != null) {
            existingTask.cancel(true); // Прерываем выполнение
            RUNNING_TASKS.remove(sessionId);
        }
    }
}
