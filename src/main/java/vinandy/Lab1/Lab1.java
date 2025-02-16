package vinandy.Lab1;

import mpi.MPI;

public class Lab1 {
    public static void main(String[] args) {
        MPI.Init(args);

        int Rank = MPI.COMM_WORLD.Rank();
        int Size = MPI.COMM_WORLD.Size();

        for (int i = Rank + 1; i <= 10; i += Size) {
            for (int j = 1; j <= 10; j++) {
                System.out.println("Proc " + Rank + " iD: " + Thread.currentThread().getId()
                        + ": " + i + "x" + j + "=" + (i * j));
            }
        }

        MPI.Finalize();
    }
}