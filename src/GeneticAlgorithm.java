import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;


public class GeneticAlgorithm {
  //max werte: seq50 + pop: 5465
  //SEQ60 pop: 500 maxgen: 90 -> 20s
    private static boolean enableParameterInput = false;
    private static int populationSize = 500;
    private static int maxGeneration = 100;
    private static double endFitness = 50; //ist ja abhängig von der amino säuren Kette!
    private static String aminoAcidString = Examples.SEQ60;
    private static double mutationRate = 0.01; //in percentage
    private static double crossoverRate = 0.25;
    private static boolean doTournamentSelection = false;
    private static int tournamentSizeK = 8; //10 guter wert?
    private static double tournamentParameterT = 0.75;
    private static boolean dynamicMutation = false;
    private static double dynamicMutationRateStart = 0.005;
    private static double dynamicMutationRateEnd = 0.03;
    private static boolean testmode = false;

    private static boolean printPopulationCSV = false;

  public static void main(String[] args) throws IOException {

    String fileName = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss'.csv'").format(new Date());

    if(!testmode) {
      if (enableParameterInput = false){
        consoleInput();
      }

      int generation = 1;

      //Population Initailisieren
      Population p = new Population(populationSize, aminoAcidString);
      Folding bestFoldingEver = p.getBestFoldingOfPopulation();
      double bestFitnessGeneration = 0.0;
      double bestFitnessEver = 0.0;


      PrintWriter clean2 = new PrintWriter("foldImages/POPULATION_LOG.csv");
      clean2.print("");
      clean2.close();

      long start = System.currentTimeMillis();

      while (bestFitnessGeneration < endFitness
          && generation <= maxGeneration) { //&&end Fitness. noch nicht ausreichend
        //Selektion
        if(doTournamentSelection == true){
          p.tournamentSelection(tournamentParameterT); //Tournament selection k=2 but best and worse winner
          //p.simpleTournamentSelection(tournamentSizeK); //k=variable best fitness wins
        }
        else{
          p.fitnessproportionateSelection();
        }

        //crossover
        p.doOnePointCrossover(crossoverRate);
        //mutation
        if(dynamicMutation){
          //mutationRate = (maxGeneration - generation) * (dynamicMutationRateStart / maxGeneration);
          mutationRate = generation * ((dynamicMutationRateEnd-dynamicMutationRateStart) / maxGeneration) + dynamicMutationRateStart;
//          if (generation < (maxGeneration *0.1))  mutationRate = ((maxGeneration*0.2) - generation) * (0.3 / maxGeneration);
//          else mutationRate = (maxGeneration - generation) * (0.01 / maxGeneration);
        }
        p.doPointMutation(mutationRate);


        //Generation in LOG schreiben
        bestFitnessGeneration = p.getBestFitnessPopulation();
        if (bestFitnessGeneration > bestFitnessEver) {
          bestFitnessEver = bestFitnessGeneration;
          bestFoldingEver = new Folding(p.getBestFoldingOfPopulation());

        }

        //write CSV


        try (BufferedWriter writer = new BufferedWriter(
            new FileWriter("foldImages/LOG_" + fileName, true))) {

          String s = "";
          s += generation;
          s += "\t";

          s += String.format(Locale.GERMANY, "%f", p.getAverageFitnessPopulation());
          s += "\t";

          s += String.format(Locale.GERMANY, "%f", bestFitnessGeneration);
          s += "\t";

          s += String.format(Locale.GERMANY, "%f", bestFitnessEver);
          s += "\t";

          s += bestFoldingEver.getHhCounter();
          s += "\t";

          s += bestFoldingEver.getOverlapCounter();
          s += "\t";

          s += bestFoldingEver.getGenotype();
          s += "\t";

          s += String.format(Locale.GERMANY, "%f", mutationRate);
          s += "\t";

          s += "\n";

          writer.write(s);

        } catch (FileNotFoundException e) {
          System.out.println(e.getMessage());
        }
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);


        if (generation % 1 == 0 | generation == 1 | generation == maxGeneration) {
          System.out.println(generation + " | Mut.rate: " + df.format(mutationRate) + " | AVG Fitness Gen.: "
              + df.format(p.getAverageFitnessPopulation()) + " | Best Fitness Gen.: "
              + bestFitnessGeneration + " | all time best fitness: " + bestFoldingEver.getFitness()
              + " | HHContacts: "
              + bestFoldingEver.getHhCounter() + " | Overlapps: "
              + bestFoldingEver.getOverlapCounter() + " | Genotype: "
              + bestFoldingEver.getGenotype());

          if(generation == maxGeneration && bestFoldingEver.getFitness() < endFitness){
            System.out.println("END FITNESS NOT FOUND (" + endFitness + ") !!!!!!!!!!!!!!");
          }


        }

        //write CSV for debugging -> able to look into population
        if(printPopulationCSV) {
          try (BufferedWriter writer = new BufferedWriter(
              new FileWriter("foldImages/POPULATION_LOG.csv", true))) {

            StringBuilder sb = new StringBuilder();
            sb.append("########GENERATION: " + generation);
            sb.append("\n");
            sb.append(p.populationToString());

            sb.append("\n\n");

            writer.write(sb.toString());
            writer.close();

          } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
          }
        }

        generation++;
      }

      long end = System.currentTimeMillis();
      SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
      Date resultdate = new Date(end - start);
      System.out.println("Running Time:  " + sdf.format(resultdate));
      System.out.println("Running Time MS:   " + (end - start));

      Phenotype testPhenotype = new Phenotype();
      testPhenotype.drawPhenotype(bestFoldingEver.getGenotype(), aminoAcidString);
      bestFoldingEver.drawFolding();
    }
    else{ //TESTMODE
      //TESTING
      Folding testFolding = new Folding(aminoAcidString);
      testFolding.updateFolding("SLRRLLLSRLRLLLLLRLRS");
      testFolding.drawFolding();
      System.out.println("TEST INPUT fitness: " + testFolding.getFitness());
      System.out.println("TEST INPUT hhcontacts: " + testFolding.getHhCounter());
      System.out.println("TEST INPUT overlapps: " + testFolding.getOverlapCounter());
    }


  }
  public static void consoleInput(){

    Scanner in = new Scanner(System.in);
    //System.out.println("Welche Sequenz?");
    System.out.println("Welche Populationsgröße n?");
    populationSize = in.nextInt();
    System.out.println("Wie viele Generationen?");
    maxGeneration = in.nextInt();
    System.out.println("Bis zu welcher Fitness?");
    endFitness = in.nextDouble();
    //System.out.println("Feste (1) oder Lineare(2) Mutationsrate?");

    System.out.println("Welche feste Mutationsrate? (, statt .!)");
    mutationRate = in.nextDouble();
    //System.out.println("Welche Startrate bei der linearen Mutationsrate?");
    System.out.println("Welche Crossoverrate? (, statt .!)");
    crossoverRate = in.nextDouble();
    boolean eingabeWdh = true;
    while(eingabeWdh == true) {
      System.out.println("Welches Selektionsverfahren? 1: Fitness-proportionale Selektion 2: Turnierselektion");
      int auswahl = in.nextInt();
      switch (auswahl) {
        case 1:
          doTournamentSelection = false;
          eingabeWdh = false;
          break;
        case 2:
          doTournamentSelection = true;
          eingabeWdh = false;
          break;
        default:
          System.out.println("Falsche Eingabe! Nochmal!");
          eingabeWdh = true;
      }
    }
    //System.out.println("Welchen tournamentParamter t bei der Turnierselektion?");

  }
}

