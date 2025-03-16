package vinandy.Lab4;

import mpi.MPI;

public class Lab4_2 {
    public static void main(String[] args) {
        MPI.Init(args);

        int np = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        int[][] matrix = null;
        int rows = 0, cols = 0;

        if (rank == 0) {
            String matrixString = System.getProperty("matrix", "-1 -2 3 4;-5 6 -7 0;9 -10 -11 1");
            String[] rowsArray = matrixString.split(";");
            rows = rowsArray.length;
            cols = rowsArray[0].split("\\s+").length;
            matrix = new int[rows][cols];
            for (int i = 0; i < rows; i++) {
                String[] elements = rowsArray[i].split("\\s+");
                for (int j = 0; j < cols; j++) {
                    matrix[i][j] = Integer.parseInt(elements[j]);
                }
            }
            System.out.println("Initial Matrix:");
            printMatrix(matrix);
        }

        // Рассылка размеров матрицы
        int[] dimensions = new int[2];
        if (rank == 0) {
            dimensions[0] = rows;
            dimensions[1] = cols;
        }
        MPI.COMM_WORLD.Bcast(dimensions, 0, 2, MPI.INT, 0);
        rows = dimensions[0];
        cols = dimensions[1];

        // Определение размера локальных данных
        int blockSize = cols / np;
        int remainder = cols % np;
        int[] counts = new int[np];
        int[] displacements = new int[np];

        for (int i = 0; i < np; i++) {
            counts[i] = (blockSize + (i < remainder ? 1 : 0)) * rows;
            displacements[i] = (i == 0) ? 0 : displacements[i - 1] + counts[i - 1];
        }

        // Локальная матрица
        int localCols = counts[rank] / rows;
        int[] localMatrix = new int[rows * localCols];
        int[] flattenedMatrix = null;

        if (rank == 0) {
            flattenedMatrix = new int[rows * cols];
            for (int r = 0; r < rows; r++) {
                System.arraycopy(matrix[r], 0, flattenedMatrix, r * cols, cols);
            }
        }

        // Распределение данных
        MPI.COMM_WORLD.Scatterv(flattenedMatrix, 0, counts, displacements, MPI.INT, localMatrix, 0, localMatrix.length, MPI.INT, 0);

        // Обмен столбцами попарно
        for (int i = 0; i < localCols; i += 2) {
            if (i + 1 < localCols) {
                for (int r = 0; r < rows; r++) {
                    int temp = localMatrix[r * localCols + i];
                    localMatrix[r * localCols + i] = localMatrix[r * localCols + i + 1];
                    localMatrix[r * localCols + i + 1] = temp;
                }
            }
        }

        // Сбор данных обратно в процесс 0
        int[] resultMatrix = null;
        if (rank == 0) {
            resultMatrix = new int[rows * cols];
        }
        MPI.COMM_WORLD.Gatherv(localMatrix, 0, localMatrix.length, MPI.INT, resultMatrix, 0, counts, displacements, MPI.INT, 0);

        // Подсчет положительных чисел
        int localPositiveCount = 0;
        for (int value : localMatrix) {
            if (value > 0) {
                localPositiveCount++;
            }
        }

        // Сбор количества положительных чисел
        int[] totalPositiveCount = new int[1];
        MPI.COMM_WORLD.Reduce(new int[]{localPositiveCount}, 0, totalPositiveCount, 0, 1, MPI.INT, MPI.SUM, 0);

        // Вывод результата
        if (rank == 0) {
            System.out.println("\nMatrix after swapping columns:");
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    System.out.print(resultMatrix[r * cols + c] + " ");
                }
                System.out.println();
            }
            System.out.println("\nNumber of positive elements: " + totalPositiveCount[0]);
        }

        MPI.Finalize();
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int elem : row) {
                System.out.print(elem + " ");
            }
            System.out.println();
        }
    }
}