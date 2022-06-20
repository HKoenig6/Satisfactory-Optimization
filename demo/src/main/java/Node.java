import java.util.*;

public class Node {

    private Dictionary<String,String> recipe;
    private Dictionary<String,Float> edges;

    public Node (Dictionary<String,String> recipe) {

        // culls empty info from CSV
        Enumeration<String> keys = recipe.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            try {
                if (Integer.valueOf(recipe.get(key)) == 0) 
                    recipe.remove(key);
                } catch (NumberFormatException N) {
                    // allow non-integer error to pass
            }
        }

        this.recipe = recipe;
        this.edges = new Hashtable<String,Float>();
    }

    public Dictionary<String,String> getInfo() {
        return this.recipe;
    }

    public void addEdge(String material, Float rate) {
        edges.put(material, rate);
    }

    public float getModifier() {
        float edgeSum = 0;
        Enumeration<Float> values = edges.elements();
        while (values.hasMoreElements()) {
            edgeSum += values.nextElement();
        }
        return edgeSum / Float.valueOf(recipe.get("trate"));
    }

    public Dictionary<String, Float> getEdges() {
        return edges;
    }
}
