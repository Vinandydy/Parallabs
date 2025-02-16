package vinandy.Lab2;

import mpi.MPI;

public class Lab2_2 {
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

            if (rank != 0) {
                MPI.COMM_WORLD.Send(new int[]{localSum}, 0, 1, MPI.INT, 0, 0);
            } else {
                int globalSum = localSum;
                for (int i = 1; i < size; i++) {
                    if (i < range) {
                        int[] receivedSum = new int[1];
                        MPI.COMM_WORLD.Recv(receivedSum, 0, 1, MPI.INT, i, 0);
                        globalSum += receivedSum[0];
                    }
                }
                System.out.println("Total odd sum: " + globalSum);
            }
        }

        MPI.Finalize();
    }
}
