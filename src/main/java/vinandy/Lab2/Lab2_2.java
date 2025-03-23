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

        int[] fullArray = null;
        int localRange = range / size;
        int remainder = range % size;

        if (rank == 0) {
            fullArray = new int[range];
            for (int i = 0; i < range; i++) {
                fullArray[i] = start + i;
            }
        }

        int[] sendcounts = new int[size];
        int[] displs = new int[size];
        int offset = 0;

        for (int i = 0; i < size; i++) {
            if (i < remainder) {
                sendcounts[i] = localRange + 1;
            } else {
                sendcounts[i] = localRange;
            }
            displs[i] = offset;
            offset += sendcounts[i];
        }

        int localSize = sendcounts[rank];
        int[] localArray = new int[localSize];

        MPI.COMM_WORLD.Scatterv(fullArray, 0, sendcounts, displs, MPI.INT,
                localArray, 0, localSize, MPI.INT, 0);

        int localSum = 0;
        int localStart = localArray[0];
        int localEnd = localArray[localArray.length - 1];

        for (int i = 0; i < localSize; i++) {
            if (localArray[i] % 2 != 0) {
                localSum += localArray[i];
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

        MPI.Finalize();
    }
}