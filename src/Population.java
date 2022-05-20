import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Population {
  private String aminoAcidString;
  private int populationSize;

  private Folding bestFoldingOfPopulation;
  private double averageFitnessPopulation = 0.0;
  private double bestFitnessPopulation = 0.0;
  private double cumFitness = 0.0;
  private ArrayList<Folding> foldings = new ArrayList<>();
  private HashMap<Integer, String> changedFoldingsBuffer = new HashMap<>();

  Population(int populationSize, String aminoAcidString){
    this.aminoAcidString = aminoAcidString;
    this.populationSize = populationSize;

    //create n populations
    for (int i = 0; i < populationSize; i++){
      foldings.add(new Folding(aminoAcidString));
    }

   calculateValues();
  }
  public String populationToString(){
    StringBuilder sb = new StringBuilder();
    sb.append("avg. Fitness of population: " + averageFitnessPopulation + "\n");
    sb.append("best Fitness of population: " + bestFitnessPopulation + "\n");
    sb.append("cum. Fitness of population: " + cumFitness + "\n");

    sb.append("GENOTYPE");
    sb.append("\t");
    sb.append("FITNESS");
    sb.append("\t");
    sb.append("HHCOUNTER");
    sb.append("\t");
    sb.append("OVERLAPS");
    sb.append("\n");
    for (int i = 0; i < populationSize; i++){
      sb.append(foldings.get(i).getGenotype());
      sb.append("\t");
      sb.append(foldings.get(i).getFitness());
      sb.append("\t");
      sb.append(foldings.get(i).getHhCounter());
      sb.append("\t");
      sb.append(foldings.get(i).getOverlapCounter());
      sb.append("\n");

    }

    return sb.toString();
  }
  public void doOnePointCrossover(double crossoverRate){
    Random r = new Random();

    if (!(crossoverRate <= 1 && crossoverRate >= 0)) return;

    int nrOfCrossovers = (int) (populationSize * crossoverRate) / 2;
    for (int i = 0; i < nrOfCrossovers; i++) {
      //GeneticAlgorthm class decides if crossover happens!
      //choose 2 random foldings
      int randomIndex1 = r.nextInt(populationSize);
      int randomIndex2 = r.nextInt(populationSize);

      //choose random position of mutation
      //-2: -1 because folding happen inbeetwen acids + -1 due to ignore first Fold direction
      int randomMutationPosition = r.nextInt(aminoAcidString.length() - 2) + 1;
      String genotype1 = "";
      String genotype2 = "";
      if (changedFoldingsBuffer.containsKey(randomIndex1)){
        genotype1 = changedFoldingsBuffer.get(randomIndex1);
      }
      else{
        genotype1 = foldings.get(randomIndex1).getGenotype();
      }
      if (changedFoldingsBuffer.containsKey(randomIndex2)){
        genotype2 = changedFoldingsBuffer.get(randomIndex2);
      }
      else{
        genotype2 = foldings.get(randomIndex2).getGenotype();
      }

      //split strings
      String leftPartGenotype1 = genotype1.substring(1, randomMutationPosition);
      String rightPartGenotype1 = genotype1.substring(randomMutationPosition, genotype1.length());

      String leftPartGenotype2 = genotype2.substring(1, randomMutationPosition);
      String rightPartGenotype2 = genotype2.substring(randomMutationPosition, genotype2.length());

      String newGenotype1 = "S" + leftPartGenotype2 + rightPartGenotype1;
      String newGenotype2 = "S" + leftPartGenotype1 + rightPartGenotype2;


      if (changedFoldingsBuffer.containsKey(randomIndex1)){
        changedFoldingsBuffer.replace(randomIndex1, newGenotype1);
      }
      else{
        changedFoldingsBuffer.put(randomIndex1, newGenotype1);

      }
      if (changedFoldingsBuffer.containsKey(randomIndex2)){
        changedFoldingsBuffer.replace(randomIndex2, newGenotype2);
      }
      else{
        changedFoldingsBuffer.put(randomIndex2, newGenotype2);
      }
    }
    updatePopulation();

  }

  public void doPointMutation(double mutationRate){
    Random r = new Random();
    if (!(mutationRate <= 1 && mutationRate >= 0)) return;
    //1 percent mutation rate -> SEQ20 -> 19 folds -> 19 * Population size * double percentag (0 bis 1) -> 19 * 1000 * 0.01 -> 190 mutations
    int nrOfMutations = (int)((aminoAcidString.length()-1) * populationSize * mutationRate);

    for (int i = 0; i < nrOfMutations; i++){
      //choose random folding

      int randomIndex = r.nextInt(populationSize);

      //choose random position of mutation
      //-2: -1 because folding happen inbeetwen acids + -1 due to ignore first Fold direction
      int randomMutationPosition = r.nextInt(aminoAcidString.length() - 2) + 1;
      String genotype = "";

      if (changedFoldingsBuffer.containsKey(randomIndex)){
        genotype = changedFoldingsBuffer.get(randomIndex);
      }
      else{
        genotype = foldings.get(randomIndex).getGenotype();
      }

      char mutatedDirection;

      //random resetting -> unabhängig von wert zuvor!
      int randomDirection = r.nextInt(3);
      if (randomDirection == 0) mutatedDirection = 'L';
      else if(randomDirection == 1) mutatedDirection = 'S';
      else mutatedDirection = 'R';

      String newGenotype = genotype.substring(0, randomMutationPosition) + mutatedDirection + genotype.substring(randomMutationPosition+1);

      if (changedFoldingsBuffer.containsKey(randomIndex)){
        changedFoldingsBuffer.replace(randomIndex, newGenotype);
      }
      else{
        changedFoldingsBuffer.put(randomIndex, newGenotype);

      }
    }
    updatePopulation();
  }
  public void updatePopulation(){
    changedFoldingsBuffer.forEach(
        (k,v) -> {
          foldings.get(k).updateFolding(v);
        }
    );
    changedFoldingsBuffer.clear();
  }
  public void calculateValues(){
    double fitnessSum = 0;
    double currentFitness = 0.0;
    bestFitnessPopulation  = 0.0;

    for (int i = 0; i < populationSize; i++){
      currentFitness = foldings.get(i).getFitness();

      if (bestFitnessPopulation < currentFitness){
        bestFitnessPopulation = currentFitness;
        bestFoldingOfPopulation = new Folding(foldings.get(i));
      }
      fitnessSum += currentFitness;

    }
    averageFitnessPopulation = fitnessSum / populationSize;
  }

  public void fitnessproportionateSelection()
  {
    int selectionSize = populationSize;
    Random rng = new Random();

    double[] cumulativeFitnesses = new double[populationSize];
    cumulativeFitnesses[0] = foldings.get(0).getFitness();
    //cumulate fitness values
    for (int i = 1; i < populationSize; i++)
    {
      double fitness = foldings.get(i).getFitness();
      cumulativeFitnesses[i] = cumulativeFitnesses[i - 1] + fitness;
    }

    ArrayList<Folding> selection = new ArrayList<>(selectionSize);
    for (int i = 0; i < selectionSize; i++)
    {
      //random value that fits between a value in the cumulative Fitness array
      double randomFitness = rng.nextDouble() * cumulativeFitnesses[cumulativeFitnesses.length - 1];
      //search the randomFitness in the cumlativeFitnesses
      int index = Arrays.binarySearch(cumulativeFitnesses, randomFitness);
      if (index < 0)
      {
        // Convert negative insertion point to array index.
        index = Math.abs(index + 1);
      }
      selection.add(new Folding(foldings.get(index)));
    }
    foldings = selection;
    calculateValues();

  }


  public void tournamentSelection(double t)
  {
    //k is always = 2 slide 6.93
    //tournament selection but better or worse candidate can both win
    //depending on probability parameter t

    Random r = new Random();
    ArrayList<Folding> selection = new ArrayList<>(populationSize);

    Folding betterCandidate = foldings.get(0);
    Folding worseCandidate = foldings.get(0);

    //choose random k many foldings
    for (int n = 0; n < populationSize; n++) {
        int randomIndex1 = r.nextInt(populationSize);
        int randomIndex2 = r.nextInt(populationSize);
        double randomNumberR = r.nextDouble(); //r < t -> better candidate wins r >= t -> worse candidate wins

        if (foldings.get(randomIndex1).getFitness() < foldings.get(randomIndex2).getFitness()) {
          betterCandidate = foldings.get(randomIndex2);
          worseCandidate = foldings.get(randomIndex1);

        }
        else{ //index 1 > index 2 or both ==
          betterCandidate = foldings.get(randomIndex1);
          worseCandidate = foldings.get(randomIndex2);
        }

        if (randomNumberR < t){ //better candidate wins
          selection.add(new Folding(betterCandidate));

        }
        else{ //worse candidate wins
          selection.add(new Folding(worseCandidate));
        }
      }
    foldings = selection;
    calculateValues();

  }

  public void simpleTournamentSelection(int k){
    //max candidate wins -> chooses max value from subpopulation with size k
    Random r = new Random();
    ArrayList<Folding> selection = new ArrayList<>(populationSize);

    Folding tournamentWinner = foldings.get(0);
    double bestFitness = 0;
    //choose random k many foldings
    for (int n = 0; n < populationSize; n++) {
      for (int i = 0; i < k; i++) {
        int randomIndex = r.nextInt(populationSize);
        if (bestFitness < foldings.get(randomIndex).getFitness()) {
          bestFitness = foldings.get(randomIndex).getFitness();
          tournamentWinner = foldings.get(randomIndex);
        }
      }
      selection.add(new Folding(tournamentWinner));

    }
    foldings = selection;
    calculateValues();
  }

  public double getAverageFitnessPopulation() {
    return averageFitnessPopulation;
  }

  public double getBestFitnessPopulation() {
    return bestFitnessPopulation;
  }

  public Folding getBestFoldingOfPopulation() {
    return bestFoldingOfPopulation;
  }
}
