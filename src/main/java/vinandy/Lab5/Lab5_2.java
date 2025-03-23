package vinandy.Lab5;

import mpi.MPI;
import mpi.MPIException;

import java.util.*;

public class Lab5_2 {
    static class Operation implements java.io.Serializable {
        int id, duration;
        List<Integer> dependencies;

        Operation(int id, int duration) {
            this.id = id;
            this.duration = duration;
            this.dependencies = new ArrayList<>();
        }
    }

    public static void main(String[] args) throws MPIException {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        List<Operation> operations = null;
        int[] earliestStart = null;
        int operationSize = 0;

        if (rank == 0) {
            operations = initializeOperations();
            operationSize = operations.size();
            earliestStart = new int[operationSize];
        }

        int[] sizeArray = new int[]{operationSize};
        MPI.COMM_WORLD.Bcast(sizeArray, 0, 1, MPI.INT, 0);
        operationSize = sizeArray[0];

        if (rank != 0) {
            earliestStart = new int[operationSize];
        }

        Object[] operationsArray = new Object[]{operations};
        MPI.COMM_WORLD.Bcast(operationsArray, 0, 1, MPI.OBJECT, 0);
        operations = (List<Operation>) operationsArray[0];

        MPI.COMM_WORLD.Bcast(earliestStart, 0, operationSize, MPI.INT, 0);

        long startTime = System.nanoTime();

        int chunk = operationSize / size;
        int start = rank * chunk;
        int end = (rank == size - 1) ? operationSize : start + chunk;

        int[] localEarliest = new int[operationSize];

        for (int i = start; i < end; i++) {
            Operation op = operations.get(i);
            int maxDepTime = 0;
            for (int dep : op.dependencies) {
                maxDepTime = Math.max(maxDepTime, earliestStart[dep] + operations.get(dep).duration);
            }
            localEarliest[i] = maxDepTime;
        }

        MPI.COMM_WORLD.Reduce(localEarliest, 0, earliestStart, 0, operationSize, MPI.INT, MPI.MAX, 0);

        long endTime = System.nanoTime();

        if (rank == 0) {
            printResults(operations, earliestStart, endTime - startTime);
        }

        MPI.Finalize();
    }
    //Тестовые данные сгенерены, потому что слишком муторная работа
    private static List<Operation> initializeOperations() {
        List<Operation> ops = new ArrayList<>();
        ops.add(new Operation(0, 4));  // A
        ops.add(new Operation(1, 3));  // B
        ops.add(new Operation(2, 2));  // C
        ops.add(new Operation(3, 5));  // D
        ops.add(new Operation(4, 1));  // E
        ops.add(new Operation(5, 6));  // F
        ops.add(new Operation(6, 2));  // G
        ops.add(new Operation(7, 3));  // H
        ops.add(new Operation(8, 4));  // I

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