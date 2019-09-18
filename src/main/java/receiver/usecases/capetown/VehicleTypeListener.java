/**
 * 
 */
package receiver.usecases.capetown;


import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierVehicleType;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.vehicles.VehicleType;

import receiver.ReceiverUtils;


/**
 * A class that keeps track of the carrier's fleet selection preferences during simulatio.
 * @author wlbean
 *
 */
public class VehicleTypeListener implements StartupListener, IterationEndsListener {
	private List<Id<VehicleType>> typeList = new ArrayList<Id<VehicleType>>();
	private Carriers carriers;
	private int run;
//	private int tw;
//	private float delFreq;
	//private float demand;
//	private float serDur;
	private String directory;

	
	public VehicleTypeListener(Scenario sc, int run) {
		this.carriers = ReceiverUtils.getCarriers(sc);
		this.run = run;
//		this.tw = tw;
//		this.delFreq = delFreq;
//		//this.demand = demand;
//		this.serDur = serDur;
		this.directory = sc.getConfig().controler().getOutputDirectory();

		
		for(Carrier carrier : carriers.getCarriers().values()){
			for(VehicleType type : carrier.getCarrierCapabilities().getVehicleTypes()){
				Id<VehicleType> typeId = type.getId();
				if(!typeList.contains(typeId)){
					typeList.add(typeId);				
				}
			}
		}

	}
	

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {

		/* Write iteration-specific types. */
        Map<Id<VehicleType>, Integer> typeMap = new TreeMap<>();
        
        for(Carrier c : this.carriers.getCarriers().values()){
        	CarrierPlan cPlan = c.getSelectedPlan();
        	for(ScheduledTour tour : cPlan.getScheduledTours()){
        		Id<VehicleType> id = tour.getVehicle().getVehicleType().getId();  		
        		if(!typeMap.containsKey(id)){
        			typeMap.put(id, 1);
        		} else{
        			int oldValue = typeMap.get(id);
        			typeMap.put(id, oldValue +1);
        		}
        	}
        }

       

          
		String dir = event.getServices().getConfig().controler().getOutputDirectory();
		dir += dir.endsWith("/") ? "" : "/";
        BufferedWriter bw = IOUtils.getBufferedWriter(dir + "ITERS/" + event.getIteration() + ".vehicleTypeCounts.csv");
        

        /*try{
        	bw.write("type,count");
        	bw.newLine();
        	for(Id<VehicleType> id : typeMap.keySet()){
        		bw.write(String.format("%s,%d\n", id.toString(), typeMap.get(id)));
        	}
        } catch (IOException e) {
        	e.printStackTrace();
        	throw new RuntimeException("Cannot write vehicle type counts.");
		} finally{
        	try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot close vehicle type counts.");
			}
        }*/		
        
        /* Write to overall vehicle type file. */
        //String dir2 = String.format("OutputTimeWindows/Vehicles.%02d.tw/", tw);  
//        String dir2 = String.format(directory + "/output/OutputTimeWindows/Vehicles.%s.serdur/", Float.toString(serDur));
        //String dir2 = String.format("OutputTimeWindows/Vehicles.%d.freq/", Math.round(delFreq));
        bw = IOUtils.getAppendingBufferedWriter(directory + "vehicleTypeCounts" + Integer.toString(run) + ".csv");
        //bw = IOUtils.getAppendingBufferedWriter(dir + "vehicleTypeCounts.csv");
        try{
        	bw.write(String.valueOf(event.getIteration()));
        	for(Id<VehicleType> id : typeList){
        		int count = 0;
        		if(typeMap.containsKey(id)){
        			count = typeMap.get(id);
        		}
        		bw.write(String.format(",%d", count));
        	}
        	bw.write(String.format(",%d", run));
        	bw.newLine();
        } catch (IOException e) {
        	e.printStackTrace();
        	throw new RuntimeException("Cannot write vehicle type counts.");
		} finally{
        	try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot close vehicle type counts.");
			}
        }		    
        
	}

	@Override
	public void notifyStartup(StartupEvent event) {
		//String dir = event.getServices().getConfig().controler().getOutputDirectory();
		//dir += dir.endsWith("/") ? "" : "/";

		//String outputDir = String.format("OutputTimeWindows/Vehicles.%02d.tw/", tw);
		String outputDir = String.format(directory);
		//String outputDir = String.format("OutputTimeWindows/Vehicles.%d.freq/", Math.round(delFreq));
        File dir = new File(outputDir);
        
        if (!dir.exists()){
            System.out.println("creating directory "+dir);
            boolean result = dir.mkdirs();
            if(result) {
            	System.out.println(dir+" created");
            }
        }
		
        BufferedWriter bw = IOUtils.getBufferedWriter(dir + "/vehicleTypeCounts" + Integer.toString(run) + ".csv");
        try{
        	bw.write("iteration");
        	for(Id<VehicleType> id : typeList){
        		bw.write(String.format(",%s", id.toString()));
        		//bw.write(String.format(",%s", id.toString()+"_capacity"));
        	}
        	bw.write(String.format(",%s", "run"));
        	bw.newLine();
        } catch (IOException e) {
        	e.printStackTrace();
        	throw new RuntimeException("Cannot write vehicle type counts.");
		} finally{
        	try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot close vehicle type counts.");
			}
        }		
		
	}


}
