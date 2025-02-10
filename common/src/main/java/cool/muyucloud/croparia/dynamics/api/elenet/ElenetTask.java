package cool.muyucloud.croparia.dynamics.api.elenet;

import net.minecraft.util.SortedArraySet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class ElenetTask implements Comparable<ElenetTask> {
    public static final short MAX_EXPENSE = 100;
    private static final SortedArraySet<ElenetTask> TASKS = SortedArraySet.create();

    public static void subscribe(Runnable task, short expense, Collection<? extends Elenet<?>> elenets, Collection<? extends ElenetHub<?>> hubs, short rank) {
        ElenetTask task_ = new ElenetTask(task, expense, elenets, hubs, rank);
        TASKS.add(task_);
    }

    public static void subscribe(Runnable task, short expense, Collection<? extends Elenet<?>> elenets, Collection<? extends ElenetHub<?>> hubs) {
        ElenetTask task_ = new ElenetTask(task, expense, elenets, hubs);
        TASKS.add(task_);
    }

    public static void subscribeIfAvailable(Runnable task, short expense, Collection<? extends Elenet<?>> elenets, Collection<? extends ElenetHub<?>> hubs) {
        if (mayAccept(expense)) {
            subscribe(task, expense, elenets, hubs);
        }
    }

    public static short parseExpense(double expense) {
        return (short) ((1D - 1D / (0.01D * expense + 1)) * MAX_EXPENSE);
    }

    /**
     * Whether the specified expense will exceed the limit if the task is to be added<br>
     * If false, it does not mean the task will not be executed, due to the random rank.
     */
    public static boolean mayAccept(short expense) {
        long futural = expense;
        for (ElenetTask task : TASKS) {
            futural += task.expense();
            if (futural > expense) {
                return false;
            }
        }
        return futural < MAX_EXPENSE;
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
    public static boolean isSuspended(ElenetHub<?> hub) {
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
    public static void invoke() {
        short expense = MAX_EXPENSE;
        for (ElenetTask task : TASKS) {
            if (expense > 0 && expense > task.expense() || task.rank == 0) {
                expense -= task.expense();
                task.run();
            }
        }
    }

    public static void onServerStarting() {
        TASKS.clear();
    }

    public static void onServerStopping() {
        TASKS.clear();
    }

    private final Set<Elenet<?>> elenets = new HashSet<>();
    private final Set<ElenetHub<?>> hubs = new HashSet<>();
    private final Runnable task;
    private final short rank;
    private final short expense;

    public ElenetTask(Runnable task, short expense, Collection<? extends Elenet<?>> elenets, Collection<? extends ElenetHub<?>> hubs) {
        this.elenets.addAll(elenets);
        this.hubs.addAll(hubs);
        this.task = task;
        this.expense = expense;
        this.rank = this.genRank();
    }

    public ElenetTask(Runnable task, short expense, Collection<? extends Elenet<?>> elenets, Collection<? extends ElenetHub<?>> hubs, short rank) {
        this.elenets.addAll(elenets);
        this.hubs.addAll(hubs);
        this.task = task;
        this.expense = expense;
        this.rank = rank;
    }

    public short genRank() {
        short least = (short) (Math.random() * 1000);
        if (this.elenets.isEmpty() && this.hubs.isEmpty()) {
            return least;
        }
        for (ElenetTask task : TASKS) {
            boolean flag = false;
            for (Elenet<?> elenet : this.elenets) {
                if (task.isSuspendedThis(elenet)) {
                    least = task.rank;
                    flag = true;
                    break;
                }
            }
            if (flag) continue;
            for (ElenetHub<?> hub : this.hubs) {
                if (task.isSuspendedThis(hub)) {
                    least = task.rank;
                    break;
                }
            }
        }
        return least;
    }

    public boolean isSuspendedThis(Elenet<?> elenet) {
        return elenets.contains(elenet);
    }

    public boolean isSuspendedThis(ElenetHub<?> hub) {
        return hubs.contains(hub);
    }

    public short expense() {
        return expense;
    }

    public void run() {
        task.run();
    }

    @Override
    public int compareTo(@NotNull ElenetTask o) {
        return Short.compare(this.rank, o.rank);
    }
}
