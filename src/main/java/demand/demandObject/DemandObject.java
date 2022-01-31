package demand.demandObject;

import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.HasPlansAndId;

import demand.mutualReplanning.DemandReplanner;
import demand.scoring.DemandScorer;
import lsp.functions.LSPInfo;
import lsp.shipment.Requirement;


public interface DemandObject extends HasPlansAndId<DemandPlan,DemandObject>{
	
//	/**
//	 * @return
//	 *
//	 * ok (but never used)
//	 */
//	public DemandAgent getShipper();
//
//	/**
//	 * @return
//	 *
//	 * ok (but never used)
//	 */
//	public DemandAgent getRecipient();
	
	/**
	 * @return
	 *
	 * ok
	 */
	Id<DemandObject> getId();
	
	/**
	 * @return
	 *
	 * yyyy is it necessary to expose the plans to the outside?
	 */
	List<? extends DemandPlan> getPlans();
	
	/**
	 * @return
	 *
	 * yy what does this mean?
	 */
	double getStrengthOfFlow();
	
	/**
	 * @return
	 *
	 * yy use coordinate or (better) facility
	 */
	Id<Link> getFromLinkId();
	
	/**
	 * @return
	 *
	 * yy use coordinate or (better) facility
	 */
	Id<Link> getToLinkId();
	
//	/**
//	 * @return
//	 *
//	 * yyyy is it necessary to expose this to the outside?  it is also never used
//	 */
//	public Collection<UtilityFunction> getUtilityFunctions();
	
	/**
	 * probably ok (behavioral method, but it is not so clear how the DemandObject should have behavior)
	 */
	void scoreSelectedPlan();
	
	/**
	 * @return
	 *
	 * probably ok (it implies that the DemandObject knows its plan, and not the handler the plan for
	 * the demand object)
	 */
	DemandPlan getSelectedPlan();
	
	/**
	 * @param plan
	 *
	 * yyyy not ok for a behavioral object
	 */
	void setSelectedPlan(DemandPlan plan);
	
	/**
	 * @param scorer
	 *
	 * yyyy not ok for a behavioral object (can the scorer be changed during iterations?)
	 */
	void setScorer(DemandScorer scorer);
	
	/**
	 * @return
	 *
	 * yyyy is it necessary to expose this to the outside?
	 */
	DemandScorer getScorer();
	
	/**
	 * @return
	 *
	 * yyyy is it necessary to expose this to the outside?
	 */
	DemandReplanner getReplanner();
	
//	/**
//	 * @param replanner
//	 *
//	 * yyyy not ok for a behavioral object (can the replanner be changed from the outside during iterations?)
//	 *
//	 * also never used
//	 */
//	public void setReplanner(DemandReplanner replanner);
	
//	/**
//	 * @param requester
//	 *
//	 * yyyy not ok for a behavioral object (can the requester be changed from the outside during iterations?)
//	 */
//	public void	setOfferRequester(OfferRequester requester);
	
	/**
	 * @return
	 *
	 * yyyy is it necessary to expose this to the outside?
	 */
	OfferRequester getOfferRequester();
	
//	/**
//	 * @param generator
//	 *
//	 * yyyy not ok for a behavioral object (can the generator be changed from the outside during iterations?)
//	 *
//	 * also never used
//	 */
//	public void	setDemandPlanGenerator(DemandPlanGenerator generator);
	
	/**
	 * @return
	 *
	 * yyyy is it necessary to expose this to the outside?
	 */
	DemandPlanGenerator getDemandPlanGenerator();
	
	/**
	 * @return
	 *
	 * yyyy is it necessary to expose this to the outside?
	 */
	Collection<Requirement> getRequirements();
	
	/**
	 * @return
	 *
	 * yyyy is it necessary to expose this to the outside?
	 */
	Collection<LSPInfo> getInfos();
}
