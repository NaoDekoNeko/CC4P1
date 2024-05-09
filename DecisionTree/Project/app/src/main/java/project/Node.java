package project;

public class Node {
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
}