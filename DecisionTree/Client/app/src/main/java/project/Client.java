package project;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Client {
    private static final int port = 1703;
    private static final String host = "localhost";
    private static Socket client;

    static int featureIndex;
    static double threshold;

    public static void main(String[] args) {
        try {
            client = new Socket(host, port);
            System.out.println("Client has connected to server on port " + client.getPort());

            Thread receiveDataThread = new Thread(() -> {
                try (Scanner scanner = new Scanner(client.getInputStream())) {
                    boolean bDataReceived = false;
                    while (true) {
                        if (!bDataReceived) {
                            if (scanner.hasNextLine()) {
                                String message = scanner.nextLine();
                                JSONObject jsonObject = new JSONObject(message);

                                // Parse featureIndex and threshold
                                int featureIndex = jsonObject.getInt("featureIndex");
                                double threshold = jsonObject.getDouble("threshold");
                                System.out.println("FeatureIndex received from server: " + featureIndex);
                                System.out.println("Threshold received from server: " + threshold);

                                // Parse dataset
                                JSONArray jsonArray = jsonObject.getJSONArray("dataset");
                                double[][] data = new double[jsonArray.length()][];
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONArray innerJsonArray = jsonArray.getJSONArray(i);
                                    data[i] = new double[innerJsonArray.length()];
                                    for (int j = 0; j < innerJsonArray.length(); j++) {
                                        data[i][j] = innerJsonArray.getDouble(j);
                                    }
                                }
                                System.out.println("Data received from server: ");
                                for (double[] row : data) {
                                    System.out.println(Arrays.toString(row));
                                }
                                split(data);
                                bDataReceived = true;
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            receiveDataThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void split(double[][] dataset) {
        List<double[]> leftList = new ArrayList<>();
        List<double[]> rightList = new ArrayList<>();

        for (double[] row : dataset) {
            if (row[featureIndex] <= threshold) {
                leftList.add(row);
            } else {
                rightList.add(row);
            }
        }

        double[][] datasetLeft = new double[leftList.size()][];
        double[][] datasetRight = new double[rightList.size()][];

        for (int i = 0; i < leftList.size(); i++) {
            datasetLeft[i] = leftList.get(i);
        }
        for (int i = 0; i < rightList.size(); i++) {
            datasetRight[i] = rightList.get(i);
        }

        double[][][] datasetLeftRight = new double[][][] { datasetLeft, datasetRight };

        JSONArray jsonArray = new JSONArray(datasetLeftRight);
        try {
            client.getOutputStream().write(jsonArray.toString().getBytes());
            client.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
