import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import com.opencsv.CSVWriter;

public class SimulationCore {
    /* Class contains main algorithm of the simulation */

    private Parameters params;
    private TotalCharge total_charge;

    public SimulationCore(Parameters _params, TotalCharge _total_charge){
        params = _params;
        total_charge = _total_charge;
    }

    public void simulation(int iterations) {
        /* Main algorithm of the simulation */
        double new_energy;
        int random_index;
        double acceptance_pdb;
        // Statistic code
        double mean_pdb = 0;
        int acc_pdb_count = 0;
        // Data gathering
        double[] temp_data = new double[params.anealing_param];
        double[] energy_data = new double[params.anealing_param];
        double[] mean_pdb_data = new double[params.anealing_param];


        for (int an = 0; an<params.anealing_param; an++) {
            /* Simulated anealing */

            for (int s = 0; s < iterations; s++) {
                random_index = new Random().nextInt(total_charge.number_of_charges);
                total_charge.position_change(random_index);
                total_charge.recalculate_all_energies();
                new_energy = total_charge.calculate_total_energy();
                if (new_energy < total_charge.total_energy) {
                    total_charge.total_energy = new_energy; // change accepted automatically
                } else {
                    acceptance_pdb = total_charge.calculate_change_probability(new_energy);
                    //Statistic
                    mean_pdb += acceptance_pdb;
                    acc_pdb_count += 1;
                    //code
                    if (Math.random() <= acceptance_pdb) {
                        total_charge.total_energy = new_energy; // change accepted, because of the probability of accepting worse state
                    } else {
                        total_charge.revert_change(random_index);
                    }
                }
            }
            mean_pdb = mean_pdb/acc_pdb_count;

            // Data gathering
            mean_pdb_data[an] = mean_pdb;
            temp_data[an] = params.temp_parameter;
            energy_data[an] = total_charge.total_energy;

            // Printing momentum parameters
            System.out.println("Temp: " + params.temp_parameter + " ; Energy: " + total_charge.total_energy + " " +
                    "; Mean pdb: " + mean_pdb + " ;");

            // Update parameters for next iteration
            params.temp_parameter -= 1;
            mean_pdb = 0;
            acc_pdb_count = 0;
        }
        write_file(temp_data, energy_data, mean_pdb_data, "data.txt");
    }

    private static void write_file(double[] temp, double[] energy, double[] mean_pdb,String filePath) {

        File file = new File(filePath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = { "Temp", "Energy", "Mean_pdb"};
            writer.writeNext(header);

            // add data to csv
            String[] data_in = new String[3];

            for (int i=0; i<temp.length; i++) {

                data_in[0] = Double.toString(temp[i]);
                data_in[1] = Double.toString(energy[i]);
                data_in[2] = Double.toString(mean_pdb[i]);

                writer.writeNext(data_in);
            }


            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
