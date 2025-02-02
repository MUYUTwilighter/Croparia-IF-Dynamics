package cool.muyucloud.croparia.dynamics.api.elenet;

import cool.muyucloud.croparia.dynamics.CropariaIfDynamics;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")
public class ElenetTask {
    private static final ConcurrentLinkedQueue<ElenetTask> TASKS = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<Runnable> CALLBACKS = new ConcurrentLinkedQueue<>();

    public static void createAndQueue(Runnable task, Runnable callback, Collection<Elenet<?>> elenets, Collection<ElenetHub> hubs) {
        ElenetTask task_ = new ElenetTask(task, callback, elenets, hubs);
        task_.queue();
    }

    public static boolean isSuspended(Elenet<?> elenet) {
        for (ElenetTask task : TASKS) {
            if (task.isSuspendedThis(elenet)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the specified elenet hub is suspended
     */
    public static boolean isSuspended(ElenetHub hub) {
        for (ElenetTask task : TASKS) {
            if (task.isSuspendedThis(hub)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Invoked at the end of a server tick
     */
    public static void invokeCallback() {
        while (!CALLBACKS.isEmpty()) {
            CALLBACKS.poll().run();
        }
    }

    public static void onServerStarting() {
        TASKS.clear();
        CALLBACKS.clear();
        ElenetTaskExecutor.getInstance().start();
    }

    public static void onServerStopping() {
        ElenetTaskExecutor.getInstance().safeStop();
        TASKS.clear();
        CALLBACKS.clear();
    }

    private final Set<Elenet<?>> elenets = new HashSet<>();
    private final Set<ElenetHub> hubs = new HashSet<>();
    private final Runnable task;
    private final Runnable callback;
    private volatile boolean active = false;

    public ElenetTask(Runnable task, Runnable callback, Collection<Elenet<?>> elenets, Collection<ElenetHub> hubs) {
        this.callback = () -> {
            callback.run();
            synchronized (ElenetTask.TASKS) {
                if (ElenetTask.TASKS.peek() == this) {
                    TASKS.poll();
                }
            }
            this.active = false;
            ElenetTaskExecutor.getInstance().safeNotify();
        };
        this.task = () -> {
            this.active = true;
            task.run();
            CALLBACKS.add(this.callback);
        };
        this.elenets.addAll(elenets);
        this.hubs.addAll(hubs);
    }

    public boolean isSuspendedThis(Elenet<?> elenet) {
        return elenets.contains(elenet);
    }

    public boolean isSuspendedThis(ElenetHub hub) {
        return hubs.contains(hub);
    }

    public void queue() {
        TASKS.add(this);
        ElenetTaskExecutor.getInstance().safeNotify();
    }

    public boolean isActive() {
        return active;
    }

    public static class ElenetTaskExecutor extends Thread {
        private static final ElenetTaskExecutor INSTANCE = new ElenetTaskExecutor();
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final AtomicBoolean paused = new AtomicBoolean(false);

        private ElenetTaskExecutor() {
            this.setName("ElenetTaskExecutor");
            this.setDaemon(true);
        }

        public static ElenetTaskExecutor getInstance() {
            return INSTANCE;
        }

        @Override
        public void run() {
            while (running.get()) {
                synchronized (this) {
                    while (paused.get() || ElenetTask.TASKS.isEmpty() || ElenetTask.TASKS.peek().isActive()) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            return; // end of the thread
                        }
                    }
                }
                // No need to synchronize TASKS as the head task will only be inactive at the end of the callback
                ElenetTask task = ElenetTask.TASKS.peek();
                if (task != null && !task.isActive()) {
                    try {
                        task.task.run();
                    } catch (Exception e) {
                        CropariaIfDynamics.LOGGER.error("ElenetTaskExecutor error", e);
                    }
                }
            }
        }

        public synchronized void safeNotify() {
            if (!ElenetTask.TASKS.isEmpty()) {
                this.notify();
            }
        }

        public void safePause() {
            paused.set(true);
        }

        public synchronized void safeResume() {
            paused.set(false);
            this.notify();
        }

        public void safeStop() {
            running.set(false);
            synchronized (this) {
                this.notify();
            }
            this.interrupt();
        }
    }
}
