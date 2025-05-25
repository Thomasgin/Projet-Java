/**
 * Represents the result of a Principal Component Analysis (PCA) applied to image data.
 * Stores the mean vector, the eigenvector base, and the associated eigenvalues.
 */
public class ACPResult {
    
    /**
     * The mean vector of the data.
     */
    public double[] moyenne;
    
    /**
     * The eigenvector base of the PCA. Each column corresponds to an eigenvector.
     */
    public double[][] base; // columns = eigenvectors
    
    /**
     * The eigenvalues associated with each eigenvector.
     */
    public double[] valeurs;

    /**
     * Constructs a new ACPResult object with the specified mean vector, eigenvector base, and eigenvalues.
     *
     * @param moyenne The mean vector of the data.
     * @param base The matrix whose columns are the eigenvectors from the PCA.
     * @param valeurs The eigenvalues corresponding to each eigenvector.
     */
    public ACPResult(double[] moyenne, double[][] base, double[] valeurs) {
        this.moyenne = moyenne;
        this.base = base;
        this.valeurs = valeurs;
    }
}
