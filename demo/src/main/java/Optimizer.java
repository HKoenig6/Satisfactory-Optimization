import java.io.FileReader;
import java.io.IOException;
import java.util.*;
//import org.apache.commons.lang3.SerializationUtils;
import com.opencsv.*;
import com.opencsv.exceptions.CsvException;

public class Optimizer {

    private static void addNode(Dictionary<String,Node> graph, 
    Dictionary<String,Dictionary<String,String>> recipes, Enumeration<String> materials,
    List<String> marked) {
        String key = "";
        String graphKey = "";
        while (materials.hasMoreElements()) {
            try {              
                key = materials.nextElement();
                //short circuits all rates as they are currently not considered
                Integer.valueOf(key);
                //continue
            } catch (NumberFormatException N) {
                Dictionary<String,String> recipe = recipes.get(formatLabel(key, true));
                if (recipe == null) continue;
                Node nextNode = new Node(recipe);                  
                graphKey = formatLabel(key, false);
                graph.put(graphKey, nextNode);
                marked.add(key);
                Enumeration<String> prerequisites = nextNode.getInfo().elements();
                addNode(graph, recipes, prerequisites, marked);
            }
        }
    }

    private static String formatLabel(String oldLabel, boolean append) {
        if (!append) {
            return oldLabel.contains("_1") ||
            oldLabel.contains("_2") || 
            oldLabel.contains("_3") || 
            oldLabel.contains("_4") ? oldLabel.substring(0,oldLabel.length()-2) : oldLabel;
        } else {
            switch (oldLabel.substring(0, oldLabel.length()-2)) {
                case "_1":
                case "_2":
                case "_3":
                case "_4":
                    return oldLabel;
                default:
                    //TODO implement this string to be flexible with what recipe is used
                    return oldLabel + "_1";
            }
        }
    }

    private static void findRates(Dictionary<String,Node> graph, List<String> marked, 
    List<String> targets, String material) {
        // if this material is a prerequisite of another in the graph, do that material first
        // and store all relevant rates in this local function
        Dictionary<String,Float> allRates = new Hashtable<String,Float>();
        Enumeration<String> keyList = graph.keys();
        while (keyList.hasMoreElements()) {
            String graphKey = keyList.nextElement();
            Node currMaterial = graph.get(graphKey);
            Dictionary<String,String> recipe = currMaterial.getInfo();

            Enumeration<String> entries = recipe.elements();
            Enumeration<String> keys = recipe.keys();
            String entry = "";
            String key = "";
            while (entries.hasMoreElements()) {
                try {
                    entry = entries.nextElement();
                    key = keys.nextElement();
                    Integer.valueOf(entry);
                    //continue
                } catch (NumberFormatException N) {
                    // run findRates on a node that requires this material before running
                    // on this material
                    if (material.equals(entry) && marked.contains(graphKey)) {
                        findRates(graph, marked, targets, graphKey);
                    }
                    if (material.equals(entry)) {

                        System.out.println(graphKey);
                        System.out.println(currMaterial.getModifier() + "\n");

                        allRates.put(graphKey, Integer.valueOf(recipe.get("prate" + key.substring(key.length()-1, key.length()))) * currMaterial.getModifier());
                    }
                }
            }
        }
        Enumeration<String> rateKeys = allRates.keys();
        Node currNode = graph.get(material);
        while (rateKeys.hasMoreElements()) {
            String rateKey = rateKeys.nextElement();
            currNode.addEdge(rateKey, allRates.get(rateKey));
        }
        marked.remove(material);
    }

    public static void main(String[] args) {
        try {
            Dictionary<String, Node> graph = new Hashtable<String,Node>();
            List<String> targets = new ArrayList<String>();

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

            List<String> marked = new ArrayList<String>();

            //TODO edit this later to accomodate for custom rates and alternate recipes
            for (String material: args) {
                Dictionary<String,String> recipe = recipes.get(material);
                Node currNode = new Node(recipe);
                String formattedMaterial = formatLabel(material, false); 
                graph.put(formattedMaterial, currNode);
                marked.add(formattedMaterial);
                targets.add(formattedMaterial);
                //TODO custom rate area of concern
                currNode.addEdge("Target", Float.valueOf(recipe.get("trate")));
            }

            //first, add all necessary nodes
            for (String key: targets) {   
                Enumeration<String> prerequisites = graph.get(key).getInfo().elements();
                addNode(graph, recipes, prerequisites, marked);
            }

            //add all edges and their respective rates
            while(!marked.isEmpty()) {
                try {
                    Enumeration<String> iter = Collections.enumeration(marked);
                    while (iter.hasMoreElements()) {
                        findRates(graph, marked, targets, iter.nextElement());
                    }
                } catch (ConcurrentModificationException CM) {
                    //i dont think thisll work but lets try :)
                }
            }          

            Enumeration<String> test = graph.keys();
            while(test.hasMoreElements()) {
                String material = test.nextElement();
                System.out.println("\n==== " + material + " ====");
                System.out.println(graph.get(material).getEdges());
            }

        } catch (IOException I) {
                System.err.println(I);
        } catch (CsvException C) {
                System.err.println(C);
        }
    }
}
