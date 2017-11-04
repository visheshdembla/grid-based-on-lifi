import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ResourceManager {

    private static ArrayList<Node> nodeArrayList = new ArrayList<>();

    static ArrayList<Node> getNodeArrayList() {
        return nodeArrayList;
    }

    static void addNode(Node node){
        nodeArrayList.add(node);
    }

    public static void printNodes(){
        Iterator<Node> iterator = nodeArrayList.iterator();
        int i = 1;
        while(iterator.hasNext()){
            System.out.print(i+":"+iterator.next().toString()+"\n");
        }
    }
/*
    public static void closeGridNodes(){
        Iterator<Node> iterator = nodeArrayList.iterator();
        int i = 1;
        while(iterator.hasNext()){
            try {
                iterator.next().getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

*/

}
