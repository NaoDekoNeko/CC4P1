package project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileNotFoundException;

import org.json.JSONArray;
import org.json.JSONObject;

public class Server {
    private static final int port = 1703;
    private static ServerSocket server;
    static final List<Socket> clients = new ArrayList<>();
    static long startTime;
    static long endTime;
    static long totalTime;

    static double[][] X_train;
    static double[][] X_test;
    static double[] Y_train;
    static double[] Y_test;
    static double[] predictions;

    private static final int minSamplesSplit = 3;
    private static final int maxDepth = 3;

    private static Node root;
    static Vector<String> receiveData = new Vector<>();
    static Vector<Integer> receiveIndex = new Vector<>();

    public static void main(String[] args) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server is running on port " + server.getLocalPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                try {
                    Socket client = server.accept();
                    clients.add(client);
                    System.out.println("New client connected from " + client.getInetAddress().getHostAddress());
                    System.out.println("Total clients connected: " + clients.size());
        
                    System.out.println("Do you want to start the algorithm? (yes/no)");
                    String userInput = scanner.nextLine();
                    if (userInput.equalsIgnoreCase("yes")) {
                        prepareData();
                        System.out.println("Starting the algorithm...");
                        startTime = System.currentTimeMillis();
                        fit();
                        predictions = predict();
                        calculateAccuracy();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        /* 
        new Thread(() -> {
            while (true) {
                try {
                    Socket client = server.accept();
                    clients.add(client);
                    System.out.println("New client connected from " + client.getInetAddress().getHostAddress());
                    System.out.println("Total clients connected: " + clients.size());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Do you want to start the algorithm? (yes/no)");
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("yes")) {
                    prepareData();
                    System.out.println("Starting the algorithm...");
                    startTime = System.currentTimeMillis();
                    fit();
                    predictions = predict();
                    calculateAccuracy();
                }
            }
        }).start();
    */
}

    private static void calculateAccuracy() {
        double correct = 0;
        for (int i = 0; i < predictions.length; i++) {
            if (predictions[i] == Y_test[i]) {
                correct++;
            }
        }
        double accuracy = correct / predictions.length * 100;
        System.out.println("Accuracy: " + accuracy + "%");
        endTime = System.currentTimeMillis();
        totalTime = endTime - startTime;
        System.out.println("Total time: " + totalTime + "ms");
    }

    public static void fit() {
        double[][] dataset = new double[X_train.length][X_train[0].length + 1];
        for (int i = 0; i < X_train.length; i++) {
            System.arraycopy(X_train[i], 0, dataset[i], 0, X_train[i].length);
            dataset[i][X_train[i].length] = Y_test[i];
        }
        root = buildTree(dataset, 0);
    }

    public static Node buildTree(double[][] dataset, int currDepth) {
        class NodeDepth {
            Node node;
            double[][] dataset;
            int depth;

            NodeDepth(Node node, double[][] dataset, int depth) {
                this.node = node;
                this.dataset = dataset;
                this.depth = depth;
            }
        }

        Node root = new Node();
        Stack<NodeDepth> stack = new Stack<>();
        stack.push(new NodeDepth(root, dataset, currDepth));

        while (!stack.isEmpty()) {
            NodeDepth nodeDepth = stack.pop();
            Node node = nodeDepth.node;
            double[][] data = nodeDepth.dataset;
            int depth = nodeDepth.depth;

            int numSamples = data.length;
            int numFeatures = data[0].length - 1;
            double[][] X = new double[numSamples][numFeatures];
            double[] Y = new double[numSamples];

            // Split the dataset into X and Y
            for (int i = 0; i < numSamples; i++) {
                System.arraycopy(data[i], 0, X[i], 0, numFeatures);
                Y[i] = data[i][numFeatures];
            }

            // Split until stopping conditions are met
            if (numSamples >= minSamplesSplit && depth <= maxDepth) {
                JSONObject bestSplit = getBestSplit(data, numSamples, numFeatures);
                if (bestSplit.getDouble("infoGain") > 0) {
                    if (bestSplit.has("datasetLeft") && bestSplit.get("datasetLeft") instanceof JSONArray) {
                        Node leftSubtree = new Node();
                        node.left = leftSubtree;
                        stack.push(new NodeDepth(leftSubtree,
                                toDoubleArray(bestSplit.getJSONArray("datasetLeft").toList()), depth + 1));
                    }

                    if (bestSplit.has("datasetRight") && bestSplit.get("datasetRight") instanceof JSONArray) {
                        Node rightSubtree = new Node();
                        node.right = rightSubtree;
                        stack.push(new NodeDepth(rightSubtree,
                                toDoubleArray(bestSplit.getJSONArray("datasetRight").toList()), depth + 1));
                    }

                    node.featureIndex = bestSplit.getInt("featureIndex");
                    node.threshold = bestSplit.getDouble("threshold");
                    node.infoGain = bestSplit.getDouble("infoGain");
                    continue;
                }
            }

            // Compute leaf node
            node.value = calculateLeafValue(Y);
        }

        return root;
    }

    public static JSONObject getBestSplit(double[][] dataset, int numSamples, int numFeatures) {
        JSONObject bestSplit = new JSONObject();
        double maxInfoGain = -Double.MAX_VALUE;

        for (int featureIndex = 0; featureIndex < numFeatures; featureIndex++) {
            double[] featureValues = new double[numSamples];
            for (int i = 0; i < numSamples; i++) {
                featureValues[i] = dataset[i][featureIndex];
            }
            double[] possibleThresholds = Arrays.stream(featureValues).distinct().toArray();

            for (double threshold : possibleThresholds) {
                double[][][] split = split(dataset, featureIndex, threshold);
                double[][] datasetLeft = split[0];
                double[][] datasetRight = split[1];

                if (datasetLeft.length > 0 && datasetRight.length > 0) {
                    double[] y = new double[numSamples];
                    double[] leftY = new double[datasetLeft.length];
                    double[] rightY = new double[datasetRight.length];

                    for (int i = 0; i < numSamples; i++) {
                        y[i] = dataset[i][numFeatures];
                    }
                    for (int i = 0; i < datasetLeft.length; i++) {
                        leftY[i] = datasetLeft[i][numFeatures];
                    }
                    for (int i = 0; i < datasetRight.length; i++) {
                        rightY[i] = datasetRight[i][numFeatures];
                    }

                    double currInfoGain = informationGain(y, leftY, rightY, "gini");

                    if (currInfoGain > maxInfoGain) {
                        bestSplit.put("featureIndex", featureIndex);
                        bestSplit.put("threshold", threshold);
                        bestSplit.put("datasetLeft", datasetLeft);
                        bestSplit.put("datasetRight", datasetRight);
                        bestSplit.put("infoGain", currInfoGain);
                        maxInfoGain = currInfoGain;
                    }
                }
            }
        }

        return bestSplit;
    }

    public static double[][][] split(double[][] dataset, int featureIndex, double threshold) {
        int numClients = clients.size();
        Thread[] threads = new Thread[numClients];
        int rowsPerThread = dataset.length / numClients;

        ConcurrentLinkedQueue<double[][][]> responses = new ConcurrentLinkedQueue<>();

        for (int t = 0; t < numClients; t++) {
            final int socketIndex = t;
            final int startRow = t * rowsPerThread;
            final int endRow = (t == numClients - 1) ? dataset.length : startRow + rowsPerThread;

            double[][] chunk = Arrays.copyOfRange(dataset, startRow, endRow);
            threads[t] = new Thread(() -> {
                sendChunkToClient(chunk, clients.get(socketIndex));
                double[][][] datasetLeftRight = receiveChunkFromClient(clients.get(socketIndex));
                responses.add(datasetLeftRight);
            });

            threads[t].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Combine the responses from all clients
        double[][][] datasetLeftRight = new double[2][][];
        datasetLeftRight[0] = responses.stream().flatMap(r -> Arrays.stream(r[0])).toArray(double[][]::new);
        datasetLeftRight[1] = responses.stream().flatMap(r -> Arrays.stream(r[1])).toArray(double[][]::new);

        return datasetLeftRight;
    }

    private static void sendChunkToClient(double[][] chunk, Socket socket) {
        try {
            JSONArray jsonArray = new JSONArray(chunk);

            socket.getOutputStream().write(jsonArray.toString().getBytes());
            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double[][][] receiveChunkFromClient(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = reader.readLine();

            // Parse the message back into a 3D array
            JSONArray jsonArray = new JSONArray(message);
            double[][][] chunk = new double[jsonArray.length()][][];
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray innerJsonArray = jsonArray.getJSONArray(i);
                chunk[i] = new double[innerJsonArray.length()][];
                for (int j = 0; j < innerJsonArray.length(); j++) {
                    JSONArray innerInnerJsonArray = innerJsonArray.getJSONArray(j);
                    chunk[i][j] = new double[innerInnerJsonArray.length()];
                    for (int k = 0; k < innerInnerJsonArray.length(); k++) {
                        chunk[i][j][k] = innerInnerJsonArray.getDouble(k);
                    }
                }
            }

            return chunk;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static double informationGain(double[] parent, double[] lChild, double[] rChild, String mode) {
        double weightL = (double) lChild.length / parent.length;
        double weightR = (double) rChild.length / parent.length;
        double gain;
        if (mode.equals("gini")) {
            gain = giniIndex(parent) - (weightL * giniIndex(lChild) + weightR * giniIndex(rChild));
        } else {
            gain = entropy(parent) - (weightL * entropy(lChild) + weightR * entropy(rChild));
        }
        return gain;
    }

    public static double entropy(double[] y) {
        double[] classLabels = Arrays.stream(y).distinct().toArray();
        double entropy = 0;
        for (double cls : classLabels) {
            long count = Arrays.stream(y).filter(value -> value == cls).count();
            double pCls = (double) count / y.length;
            entropy += -pCls * Math.log(pCls);
        }
        return entropy;
    }

    public static double giniIndex(double[] y) {
        double[] classLabels = Arrays.stream(y).distinct().toArray();
        double gini = 0;
        for (double cls : classLabels) {
            long count = Arrays.stream(y).filter(value -> value == cls).count();
            double pCls = (double) count / y.length;
            gini += pCls * pCls;
        }
        return 1 - gini;
    }

    public static double calculateLeafValue(double[] Y) {
        Map<Double, Long> frequencyMap = Arrays.stream(Y).boxed()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return Collections.max(frequencyMap.entrySet(), Comparator.comparingLong(Map.Entry::getValue)).getKey();
    }

    public static double[] predict() {
        double[] predictions = new double[X_test.length];
        for (int i = 0; i < X_test.length; i++) {
            predictions[i] = makePrediction(X_test[i], root);
        }
        return predictions;
    }

    public static double makePrediction(double[] x, Node tree) {
        if (tree.value != null)
            return tree.value;
        double featureVal = x[tree.featureIndex];
        if (featureVal <= tree.threshold) {
            return makePrediction(x, tree.left);
        } else {
            return makePrediction(x, tree.right);
        }
    }

    private static double[][] toDoubleArray(List<Object> list) {
        double[][] array = new double[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            List<Object> row = (List<Object>) list.get(i);
            array[i] = new double[row.size()];
            for (int j = 0; j < row.size(); j++) {
                array[i][j] = (double) row.get(j);
            }
        }
        return array;
    }

    static void prepareData() {
        try {
            CSVReader csvReader = new CSVReader("iris.csv");
            // printDoubleArray(csvReader.getData());
            double[][][] trainTest = csvReader.trainTestSplit(0.7);

            X_train = new double[trainTest[0].length][trainTest[0][0].length - 1];
            for (int i = 0; i < trainTest[0].length; i++) {
                X_train[i] = Arrays.copyOfRange(trainTest[0][i], 0, trainTest[0][i].length - 1);
            }

            X_test = new double[trainTest[1].length][trainTest[1][0].length - 1];
            for (int i = 0; i < trainTest[1].length; i++) {
                X_test[i] = Arrays.copyOfRange(trainTest[1][i], 0, trainTest[1][i].length - 1);
            }

            Y_train = new double[trainTest[0].length];
            for (int i = 0; i < trainTest[0].length; i++) {
                Y_train[i] = trainTest[0][i][trainTest[0][i].length - 1];
            }

            Y_test = new double[trainTest[1].length];
            for (int i = 0; i < trainTest[1].length; i++) {
                Y_test[i] = trainTest[1][i][trainTest[1][i].length - 1];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printDoubleArray(double[][] array) {
        for (double[] row : array) {
            for (double value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }

    public static void printArray(double[] array) {
        for (double value : array) {
            System.out.print(value + " ");
        }
        System.out.println();
    }
}

class Node {
    Integer featureIndex;
    Double threshold;
    Node left;
    Node right;
    Double infoGain;
    Double value;

    public Node(Integer featureIndex, Double threshold, Node left, Node right, Double infoGain, Double value) {
        this.featureIndex = featureIndex;
        this.threshold = threshold;
        this.left = left;
        this.right = right;
        this.infoGain = infoGain;
        this.value = value;
    }

    public Node() {
        this.featureIndex = 0;
        this.threshold = 0.0;
        this.left = null;
        this.right = null;
        this.infoGain = 0.0;
        this.value = 0.0;
    }
}

class CSVReader {
    private double[][] data;
    private List<String> columnNames;
    private HashMap<String, Integer> uniqueStrings;

    public CSVReader(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filename));
        List<double[]> dataList = new ArrayList<>();
        uniqueStrings = new HashMap<>();
        int uniqueId = 0;

        // Skip the first line which contains the column names
        columnNames = Arrays.asList(scanner.nextLine().split(","));
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split(",");
            double[] row = new double[line.length];
            for (int i = 0; i < line.length; i++) {
                try {
                    row[i] = Double.parseDouble(line[i]);
                } catch (NumberFormatException e) {
                    if (!uniqueStrings.containsKey(line[i])) {
                        uniqueStrings.put(line[i], uniqueId++);
                    }
                    row[i] = uniqueStrings.get(line[i]);
                }
            }
            dataList.add(row);
        }

        data = dataList.toArray(new double[0][]);
    }

    public double[][] getData() {
        return data;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public double[][][] trainTestSplit(double ratio) {
        List<double[]> dataList = new ArrayList<>(Arrays.asList(data));
        Collections.shuffle(dataList);
        data = dataList.toArray(new double[0][]);

        int trainSize = (int) (data.length * ratio);
        double[][] trainData = Arrays.copyOfRange(data, 0, trainSize);
        double[][] testData = Arrays.copyOfRange(data, trainSize, data.length);
        return new double[][][] { trainData, testData };
    }
}