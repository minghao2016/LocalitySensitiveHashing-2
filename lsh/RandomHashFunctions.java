import java.util.Random;
/**
*
* @author Tharindu Kumara
*
* Implementation of the random hashing function
*
*/

public class RandomHashFunctions {
	private int multiplier[];
	private int adder[];
	private int primes[];
	
	public RandomHashFunctions(int n){
		multiplier = new int[n];
		adder = new int[n];
		primes = new int[n];
		Random r = new Random(2);
		for(int i=0;i<n;i++){
			
			int a = (int)r.nextInt(100);
			int b = (int)r.nextInt(100);
			int c = (int)r.nextInt(230000)+23;
			
			multiplier[i] = a;
			adder[i] = b;
			primes[i] = c;
			
		}
	}
	public int hash(int a,int b){
		int hashCode = multiplier[a]*b + adder[a];
		return ((int)Math.abs(hashCode)) % 23;
	}
}	
