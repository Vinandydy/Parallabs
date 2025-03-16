package vinandy.Lab4;

import mpi.MPI;

public class Lab4_1 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int start = Integer.parseInt(System.getProperty("start", "1"));
        int end = Integer.parseInt(System.getProperty("end", "100"));
        int range = end - start + 1;

        if (rank >= range) {
            System.out.println("Process " + rank + " is idle.");
        } else {
            int localRange = range / size;
            int remainder = range % size;

            int localStart, localEnd;

            if (rank < remainder) {
                localStart = start + rank * (localRange + 1);
                localEnd = localStart + localRange;
            } else {
                localStart = start + (rank * localRange) + remainder;
                localEnd = localStart + localRange - 1;
            }

            if (localStart > localEnd) {
                int temp = localStart;
                localStart = localEnd;
                localEnd = temp;
            }

            int localSum = 0;
            for (int i = localStart; i <= localEnd; i++) {
                if (i % 2 != 0) { // Проверяем, является ли число нечетным
                    localSum += i;
                }
            }

            System.out.println("Proc " + rank + " (" + localStart + " to " + localEnd + ") local sum: " + localSum);

            // Используем MPI_Reduce для сбора суммы
            int[] globalSum = new int[1];
            MPI.COMM_WORLD.Reduce(new int[]{localSum}, 0, globalSum, 0, 1, MPI.INT, MPI.SUM, 0);

            if (rank == 0) {
                System.out.println("Total odd sum: " + globalSum[0]);
            }
        }

        MPI.Finalize();
    }
}