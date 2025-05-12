package denoiser;

public class ACPResult {
    public double[] moyenne;
    public double[][] base; // colonnes = vecteurs propres
    public double[] valeurs;

    public ACPResult(double[] moyenne, double[][] base, double[] valeurs) {
        this.moyenne = moyenne;
        this.base = base;
        this.valeurs = valeurs;
    }
}