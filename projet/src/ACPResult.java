/**
 * ACPResult is a storage class use to get results from ACP
 */
public class ACPResult {
    /**
     * Get the value of mean which had been calculated in ACP computing
     */
    public double[] moyenne;
    /**
     * Get the value of eigen vectors matrix which had been calculated in ACP computing
     */
    public double[][] base; // columns = eigen vector
    /**
     * Get the value of values which had been calculated in ACP computing
     */
    public double[] valeurs;

    /**
     * Constructor of ACPResult
     * @param moyenne mean
     * @param base matrix where there columns are eigen vectors
     * @param valeurs values
     */
    public ACPResult(double[] moyenne, double[][] base, double[] valeurs) {
        this.moyenne = moyenne;
        this.base = base;
        this.valeurs = valeurs;
    }
}