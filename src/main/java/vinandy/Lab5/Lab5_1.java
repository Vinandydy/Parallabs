package vinandy.Lab5;

import java.util.*;

public class Lab5_1 {
    static class Operation {
        int id;
        int duration;
        List<Integer> dependencies;

        Operation(int id, int duration) {
            this.id = id;
            this.duration = duration;
            this.dependencies = new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        List<Operation> operations = initializeOperations();
        int[] earliestStart = new int[operations.size()];

        long startTime = System.nanoTime();

        for (int i = 0; i < operations.size(); i++) {
            Operation op = operations.get(i);
            int maxDepTime = 0;

            for (int dep : op.dependencies) {
                maxDepTime = Math.max(maxDepTime, earliestStart[dep] + operations.get(dep).duration);
            }
            earliestStart[i] = maxDepTime;
        }

        long endTime = System.nanoTime();

        printResults(operations, earliestStart, endTime - startTime);
    }

    //Тестовые данные сгенерены, потому что слишком муторная работа
    private static List<Operation> initializeOperations() {
        List<Operation> ops = new ArrayList<>();
        ops.add(new Operation(0, 4));  // A

        ops.get(1).dependencies.add(0); // B зависит от A
        ops.get(2).dependencies.add(0); // C зависит от A
        ops.get(3).dependencies.add(1); // D зависит от B
        ops.get(4).dependencies.add(2); // E зависит от C
        ops.get(5).dependencies.add(3); // F зависит от D
        ops.get(6).dependencies.add(4); // G зависит от E
        ops.get(7).dependencies.add(5); // H зависит от F
        ops.get(8).dependencies.add(6); // I зависит от G
        return ops;
    }

    private static void printResults(List<Operation> ops, int[] earliestStart, long time) {
        System.out.println("Earliest start:");
        for (int i = 0; i < ops.size(); i++) {
            System.out.printf("Operation %d: %d\n", i, earliestStart[i]);
        }
    }
}