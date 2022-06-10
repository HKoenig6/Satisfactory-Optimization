import java.io.FileReader;
import java.util.*;
import com.opencsv.*;

public class Optimizer {

    public static void main(String[] args) {
        try {
            Dictionary<String, Node> graph = new Hashtable<String,Node>();

            //convert CSV to 2D dictionary
            CSVReader csvReader = new CSVReaderHeaderAware(new FileReader("recipes.csv"));
            String[] keys = {"trate","pre1","pre2","pre3","pre4","prate1","prate2","prate3","prate4"};
            Dictionary<String,Dictionary<String,String>> recipes = new Hashtable<String,Dictionary<String,String>>();
            List<String[]> parser = csvReader.readAll();
            for (String[] recipe: parser) {
                recipes.put(recipe[0], new Hashtable<String,String>());
                Dictionary<String,String> info = recipes.get(recipe[0]);
                for (int i = 1; i < recipe.length; i++) {
                    info.put(keys[i-1], recipe[i]);
                }
            }

            //TODO edit this later to accomodate for custom rates and alternate recipes
            for (String node:args) {
                if (node.equals(args[0])) continue;
                graph.put(node, new Node());
            }

        } catch (Exception E) {
            System.err.println(E);
        }
    }
}

