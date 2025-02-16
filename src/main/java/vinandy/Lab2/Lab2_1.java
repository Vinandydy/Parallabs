package vinandy.Lab2;

import mpi.MPI;

public class Lab2_1 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();

        int ai = rank + 1;
        int bi = (rank + 1) * 2;

        System.out.println("Proc " + rank + " ID: " + Thread.currentThread().getId()
                + " -> ai: " + ai + ", bi: " + bi);

        int[] received_a = new int[1];
        int[] received_b = new int[1];

        int send_a, send_b;
        int receive_a, receive_b;

        switch (rank) {
            case 0:
                send_a = 2;
                send_b = 1;
                receive_a = 1;
                receive_b = 2;
                break;
            case 1:
                send_a = 0;
                send_b = 3;
                receive_a = 3;
                receive_b = 0;
                break;
            case 2:
                send_a = 3;
                send_b = 0;
                receive_a = 0;
                receive_b = 3;
                break;
            case 3:
                send_a = 1;
                send_b = 2;
                receive_a = 2;
                receive_b = 1;
                break;
            default:
                throw new IllegalStateException("Unexpected rank: " + rank);
        }

        if (rank == 0 || rank == 3) {
            MPI.COMM_WORLD.Send(new int[]{ai}, 0, 1, MPI.INT, send_a, 99);
            MPI.COMM_WORLD.Send(new int[]{bi}, 0, 1, MPI.INT, send_b, 99);
            MPI.COMM_WORLD.Recv(received_a, 0, 1, MPI.INT, receive_a, 99);
            MPI.COMM_WORLD.Recv(received_b, 0, 1, MPI.INT, receive_b, 99);
        } else {
            MPI.COMM_WORLD.Recv(received_a, 0, 1, MPI.INT, receive_a, 99);
            MPI.COMM_WORLD.Recv(received_b, 0, 1, MPI.INT, receive_b, 99);
            MPI.COMM_WORLD.Send(new int[]{ai}, 0, 1, MPI.INT, send_a, 99);
            MPI.COMM_WORLD.Send(new int[]{bi}, 0, 1, MPI.INT, send_b, 99);
        }

        System.out.println("Proc " + rank  + " ID: " + Thread.currentThread().getId()
                + " received: a=" + received_a[0]
                + ", b=" + received_b[0]
                + " -> c" + rank + " = " + (received_a[0] + received_b[0]));

        MPI.Finalize();
    }
}