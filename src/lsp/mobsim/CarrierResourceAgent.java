package lsp.mobsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Route;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierService;
import org.matsim.contrib.freight.carrier.CarrierShipment;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.FreightConstants;
import org.matsim.contrib.freight.carrier.ScheduledTour;
import org.matsim.contrib.freight.carrier.Tour;
import org.matsim.contrib.freight.carrier.Tour.Delivery;
import org.matsim.contrib.freight.carrier.Tour.Pickup;
import org.matsim.contrib.freight.carrier.Tour.ServiceActivity;
import org.matsim.contrib.freight.carrier.Tour.TourActivity;
import org.matsim.contrib.freight.carrier.Tour.TourElement;
import org.matsim.contrib.freight.scoring.FreightActivity;
import org.matsim.core.events.algorithms.Vehicle2DriverEventHandler;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleUtils;

class CarrierResourceAgent implements ActivityStartEventHandler, ActivityEndEventHandler, PersonDepartureEventHandler, PersonArrivalEventHandler,  LinkEnterEventHandler, LinkLeaveEventHandler, 
							VehicleLeavesTrafficEventHandler {

	
	class CarrierDriverAgent {

		private Leg currentLeg;

		private Activity currentActivity;

		private List<Id<Link>> currentRoute;

		private final Id<Person> driverId;

		private final ScheduledTour scheduledTour;

		private int activityCounter = 0;

		CarrierDriverAgent(Id<Person> driverId, ScheduledTour tour) {
			this.driverId = driverId;
			this.scheduledTour = tour;
			new HashMap<Integer, CarrierShipment>();
		}

		/**
		 * 
		 * @param event
		 */
		public void handleEvent(PersonArrivalEvent event) {
			currentLeg.setTravelTime( event.getTime() - currentLeg.getDepartureTime() );
			double travelTime = currentLeg.getDepartureTime() + currentLeg.getTravelTime() - currentLeg.getDepartureTime();
			currentLeg.setTravelTime(travelTime);
			if (currentRoute.size() > 1) {
				NetworkRoute networkRoute = RouteUtils.createNetworkRoute(currentRoute, null);
				networkRoute.setTravelTime(travelTime);
				networkRoute.setVehicleId(getVehicle().getVehicleId());
				currentLeg.setRoute(networkRoute);
				currentRoute = null;
			} else {
				Id<Link> startLink;
				if(currentRoute.size() != 0){
					startLink = currentRoute.get(0);
				}
				else{
					startLink = event.getLinkId();
				}
				Route genericRoute = RouteUtils.createGenericRouteImpl(startLink, event.getLinkId());    
				genericRoute.setDistance(0.0);
				currentLeg.setRoute(genericRoute);
			}
		}

		public void handleEvent(PersonDepartureEvent event) {
			Leg leg = PopulationUtils.createLeg(event.getLegMode());
			leg.setDepartureTime(event.getTime());
			currentLeg = leg;
			currentRoute = new ArrayList<Id<Link>>();
		}

		public void handleEvent(LinkEnterEvent event) {
            currentRoute.add(event.getLinkId());
            notifyLinkEntered(driverId , event.getVehicleId() , event.getLinkId() , event.getTime(), this.scheduledTour.getVehicle());
		}

		public void handleEvent(LinkLeaveEvent event) {
            currentRoute.add(event.getLinkId());
            notifyLinkLeft(driverId , event.getVehicleId() , event.getLinkId() , event.getTime(), this.scheduledTour.getVehicle());
		}
		
		public void handleEvent(ActivityEndEvent event) {
			if (currentActivity == null) {
				Activity  firstActivity = PopulationUtils.createActivityFromLinkId(event.getActType(), event.getLinkId());
				firstActivity.setFacilityId(event.getFacilityId());
				currentActivity = firstActivity;
			}
			currentActivity.setEndTime(event.getTime());
			activityFinished(event.getActType(), event.getTime()); 
		}

		private TourActivity getTourActivity() {
			return (TourActivity) this.scheduledTour.getTour().getTourElements().get(activityCounter);
		}

		public void handleEvent(ActivityStartEvent event) {
			Activity activity = PopulationUtils.createActivityFromLinkId(event.getActType(), event.getLinkId()); 
			activity.setFacilityId(event.getFacilityId());
			activity.setStartTime(event.getTime());
			if(event.getActType().equals(FreightConstants.END)){
				activity.setEndTime(Time.UNDEFINED_TIME);
				Tour tour = this.scheduledTour.getTour();
				notifyEnd(event.getPersonId(), tour, event.getTime(), this.scheduledTour.getVehicle());
			}
			
			else{
				TourActivity tourActivity = getTourActivity();
				assert activity.getLinkId().toString().equals(tourActivity.getLocation().toString()) : "linkId of activity is not equal to linkId of tourActivity. This must not be.";
				FreightActivity freightActivity = new FreightActivity(activity, tourActivity.getTimeWindow());
				currentActivity = freightActivity;
				activityStarted(event.getActType(), event.getTime());
			}
		}

		public void handleEvent(VehicleLeavesTrafficEvent event) {
			 notifyLeavesTraffic(event.getTime(), driverId, event.getLinkId(), event.getVehicleId(), this.scheduledTour.getVehicle());
		}
		
		
		/**
		 * Informs the carrierAgent that an activity has been finished.
		 * 
		 * @param activityType
		 * @param time
		 */
		private void activityFinished(String activityType, double time) {
			Tour tour = this.scheduledTour.getTour();
			if(FreightConstants.START.equals(activityType)){
				notifyStart(driverId , tour,  time, this.scheduledTour.getVehicle());
				activityCounter+= 1;		
			}	
			else if(FreightConstants.END.equals(activityType)){
				//notifyEnd(driverId , tour,  time);
				//activityCounter+= 2;
			}
			else if (FreightConstants.PICKUP.equals(activityType)){
				Pickup tourElement = (Pickup) tour.getTourElements().get(activityCounter);
				notifyPickup(driverId, tourElement.getShipment(),time);
				//				logger.info("pickup occured");
				activityCounter += 2;
			}
			else if(activityType.equals("service")){
				ServiceActivity tourElement = (ServiceActivity) tour.getTourElements().get(activityCounter);
				notifyServiceEnd(driverId , tourElement.getService() , time, this.scheduledTour.getVehicle());
				//notifyService(driverId , tourElement.getService() , time - tourElement.getDuration());
				activityCounter += 2;
			}
			else if (FreightConstants.DELIVERY.equals(activityType)){
				Delivery tourElement = (Delivery) tour.getTourElements().get(activityCounter);
				notifyDelivery(driverId,tourElement.getShipment(), time);
				activityCounter += 2;
			}			
			else {
				//notify activity ends ??
				activityCounter += 2;
			}
		}

		private void activityStarted(String activityType, double time) {
			Tour tour = this.scheduledTour.getTour();
			if(activityType.equals("service")){
				ServiceActivity tourElement = (ServiceActivity) tour.getTourElements().get(activityCounter);
				notifyServiceStart(driverId , tourElement.getService() , time, this.scheduledTour.getVehicle());
			}
		}
		
		CarrierVehicle getVehicle() {
			return scheduledTour.getVehicle();
		}

		TourElement getPlannedTourElement(int elementIndex){
			int index = elementIndex-1;
			int elementsSize = scheduledTour.getTour().getTourElements().size();
			if(index < 0) return scheduledTour.getTour().getStart();
			else if(index == elementsSize) return scheduledTour.getTour().getEnd();
			else if(index < elementsSize){
				return scheduledTour.getTour().getTourElements().get(index);
			}
			else throw new IllegalStateException("index out of bounds");
		}
	}

	private final Id<Carrier> id;

	private final Carrier carrier;

	private final CarrierResourceTracker tracker;

	private Collection<Id<Person>> driverIds = new ArrayList<>();

	private int nextId = 0;

	private Map<Id<Person>, CarrierDriverAgent> carrierDriverAgents = new HashMap<>();

	private Map<Id<Person>, ScheduledTour> driverTourMap = new HashMap<>();

	private final Vehicle2DriverEventHandler vehicle2DriverEventHandler;

	CarrierResourceAgent(CarrierResourceTracker carrierResourceTracker, Carrier carrier, Vehicle2DriverEventHandler vehicle2DriverEventHandler) {
		this.tracker = carrierResourceTracker;
		this.carrier = carrier;
		this.id = carrier.getId();
		this.vehicle2DriverEventHandler = vehicle2DriverEventHandler;
	}

	public Id<Carrier> getId() {
		return id;
	}

	/**
	 * Returns a list of plans created on the basis of the carrier's plan.
	 * 
	 * <p>A carrier plan consists usually of many tours (activity chains). Each plan in the returned list represents a carrier tour.
	 *  
	 * @return list of plans
	 * @see Plan, CarrierPlan
	 */
	List<MobSimVehicleRoute> createFreightDriverPlans() {
		clear();
		System.out.flush();
		System.err.flush() ;
		List<MobSimVehicleRoute> routes = new ArrayList<MobSimVehicleRoute>();
		//		List<Plan> plans = new ArrayList<Plan>();
		if (carrier.getSelectedPlan() == null) {
			return routes;
		}
		for (ScheduledTour scheduledTour : carrier.getSelectedPlan().getScheduledTours()) {
			Id<Person> driverId = createDriverId(scheduledTour.getVehicle());
			CarrierVehicle carrierVehicle = scheduledTour.getVehicle();
			Person driverPerson = createDriverPerson(driverId);
			Vehicle vehicle = createVehicle(driverPerson,carrierVehicle);
			CarrierDriverAgent carrierDriverAgent = new CarrierDriverAgent(driverId, scheduledTour);
			Plan plan = PopulationUtils.createPlan();
			Activity startActivity = PopulationUtils.createActivityFromLinkId(FreightConstants.START,scheduledTour.getVehicle().getLocation());
			startActivity.setEndTime(scheduledTour.getDeparture());
			plan.addActivity(startActivity);
			for (TourElement tourElement : scheduledTour.getTour().getTourElements()) {				
				if (tourElement instanceof org.matsim.contrib.freight.carrier.Tour.Leg) {
					org.matsim.contrib.freight.carrier.Tour.Leg tourLeg = (org.matsim.contrib.freight.carrier.Tour.Leg) tourElement;
					Route route = tourLeg.getRoute();
					if(route == null) throw new IllegalStateException("missing route for carrier " + this.getId());
					Leg leg = PopulationUtils.createLeg(TransportMode.car);
					leg.setRoute(route);
					leg.setDepartureTime(tourLeg.getExpectedDepartureTime());
					leg.setTravelTime(tourLeg.getExpectedTransportTime());
					leg.setTravelTime( tourLeg.getExpectedDepartureTime() + tourLeg.getExpectedTransportTime() - leg.getDepartureTime() );
					plan.addLeg(leg);
				} else if (tourElement instanceof TourActivity) {
					TourActivity act = (TourActivity) tourElement;
					Activity tourElementActivity = PopulationUtils.createActivityFromLinkId(act.getActivityType(), act.getLocation());					
					double duration = act.getDuration() ;
					tourElementActivity.setMaximumDuration(duration); // "maximum" has become a bit of a misnomer ...
					plan.addActivity(tourElementActivity);
				}
			}
			Activity endActivity = PopulationUtils.createActivityFromLinkId(FreightConstants.END,scheduledTour.getVehicle().getLocation());
			plan.addActivity(endActivity);
			driverPerson.addPlan(plan);
			plan.setPerson(driverPerson);
			MobSimVehicleRoute mobsimRoute = new MobSimVehicleRoute(plan, vehicle);
			routes.add(mobsimRoute);
			//			plans.add(plan);
			carrierDriverAgents.put(driverId, carrierDriverAgent);
			driverTourMap.put(driverId, scheduledTour);
		}
		return routes;
	}

	private Vehicle createVehicle(Person driverPerson, CarrierVehicle carrierVehicle) {
		return VehicleUtils.getFactory().createVehicle(Id.create(driverPerson.getId(), Vehicle.class), carrierVehicle.getVehicleType());
	}

	private void clear() {
		carrierDriverAgents.clear();
		driverTourMap.clear();
		driverIds.clear();
		nextId = 0;
	}

	public Collection<Id<Person>> getDriverIds() {
		return Collections.unmodifiableCollection(driverIds);
	}

	private Person createDriverPerson(Id<Person> driverId) {
		Person person = PopulationUtils.getFactory().createPerson(driverId);
		return person;
	}

	private Id<Person> createDriverId(CarrierVehicle carrierVehicle) {
		Id<Person> id = Id.create("freight_" + carrier.getId() + "_veh_" + carrierVehicle.getVehicleId() + "_" + nextId, Person.class);
		driverIds.add(id);
		++nextId;
		return id;
	}

	public void notifyPickup(Id<Person> driverId, CarrierShipment shipment, double time) {
		tracker.notifyPickedUp(carrier.getId(), driverId, shipment, time);
	}

	public void notifyDelivery(Id<Person> driverId, CarrierShipment shipment,
			double time) {
		tracker.notifyDelivered(carrier.getId(), driverId, shipment, time);
	}

	public void notifyStart(Id<Person> driverId , Tour tour, double time, CarrierVehicle vehicle) {
		tracker.notifyStart(carrier.getId(), driverId, tour, time, vehicle);
	}
	
	public void notifyEnd(Id<Person> driverId , Tour tour, double time, CarrierVehicle vehicle) {
		tracker.notifyEnd(carrier.getId(), driverId, tour, time, vehicle);
	}
	
	public void notifyServiceEnd(Id<Person> driverId ,CarrierService service, double time, CarrierVehicle vehicle) {
		tracker.notifyServiceEnd(carrier.getId(), driverId, service, time, vehicle );
	}
	
	public void notifyServiceStart(Id<Person> driverId ,CarrierService service, double time, CarrierVehicle vehicle) {
		tracker.notifyServiceStart(carrier.getId(), driverId, service, time, vehicle);
	}
	
	public void notifyLinkEntered(Id<Person> driverId ,Id<Vehicle> vehicleId, Id<Link> linkId, double time, CarrierVehicle vehicle) {
		tracker.notifyLinkEntered(carrier.getId(), vehicleId,  driverId, linkId, time, vehicle);
	}
	
	public void notifyLinkLeft(Id<Person> driverId ,Id<Vehicle> vehicleId, Id<Link> linkId, double time, CarrierVehicle vehicle) {
		tracker.notifyLinkLeft(carrier.getId(), vehicleId,  driverId, linkId, time, vehicle);
	}
	
	public void notifyLeavesTraffic(double time, Id<Person> driverId, Id<Link> linkId, Id<Vehicle> vehicleId, CarrierVehicle vehicle) {
		tracker.notifyLeavesTraffic(carrier.getId(), time, driverId, linkId, vehicleId, vehicle);
	}
	
	 
	@Override
	public void handleEvent(PersonArrivalEvent event) {
		//System.out.println("CarrierAgent processes " + event);
		getDriver(event.getPersonId()).handleEvent(event);
	}

	@Override
	public void reset(int iteration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		//System.out.println("CarrierAgent processes " + event);
		getDriver(vehicle2DriverEventHandler.getDriverOfVehicle(event.getVehicleId())).handleEvent(event);
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		//System.out.println("CarrierAgent processes " + event);
		getDriver(vehicle2DriverEventHandler.getDriverOfVehicle(event.getVehicleId())).handleEvent(event);
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {
		//System.out.println("CarrierAgent processes " + event);
		getDriver(event.getPersonId()).handleEvent(event);
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		//System.out.println("CarrierAgent processes " + event);
		getDriver(event.getPersonId()).handleEvent(event);
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		//System.out.println("CarrierAgent processes " + event);
		getDriver(event.getPersonId()).handleEvent(event);
	}

	CarrierDriverAgent getDriver(Id<Person> driverId){
		return carrierDriverAgents.get(driverId);
	}

	@Override
	public void handleEvent(VehicleLeavesTrafficEvent event) {
		getDriver(event.getPersonId()).handleEvent(event);	
	}

}