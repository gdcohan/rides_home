import java.util.ArrayList;
import java.util.Comparator;

public class SwapComparator implements Comparator<ArrayList<Integer>> {
    @Override
	public int compare(ArrayList<Integer> a1, ArrayList<Integer> a2) {
	return a1.get(2) - a2.get(2);
    }
}