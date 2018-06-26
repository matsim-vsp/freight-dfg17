/**
 * 
 */
package receiver.usecases;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.utils.charts.XYLineChart;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.UncheckedIOException;

import receiver.Receiver;
import receiver.ReceiverPlan;
import receiver.Receivers;

/**
 * Generates score stats for receivers. This is adapted from "org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats" by mrieser.
 * 
 * @author wlbean
 */
public class ReceiverScoreStats implements StartupListener, IterationEndsListener, ShutdownListener{

		final private static int INDEX_WORST = 0;
		final private static int INDEX_BEST = 1;
		final private static int INDEX_AVERAGE = 2;
		final private static int INDEX_EXECUTED = 3;

		private BufferedWriter out;
		final private String fileName;
		
		private final boolean createPNG;
		private double[][] history = null;
		private int minIteration = 0;
		
		private Receivers receivers;

		private final static Logger log = Logger.getLogger(ReceiverScoreStats.class);

		/**
		 * Creates a new ScoreStats instance.
		 *
		 * @param filename including the path, excluding the file type extension
		 * @param createPNG true if in every iteration, the scorestats should be visualized in a graph and written to disk.
		 * @throws UncheckedIOException
		 */
		public ReceiverScoreStats(Receivers receivers, final String filename, final boolean createPNG) throws UncheckedIOException {
			this.receivers = receivers;
			this.fileName = filename;
			this.createPNG = createPNG;
		}

		@Override
		public void notifyStartup(final StartupEvent event) {
			if (fileName.toLowerCase(Locale.ROOT).endsWith(".txt")) {
				this.out = IOUtils.getBufferedWriter(fileName);
			} else {
				this.out = IOUtils.getBufferedWriter(fileName + ".txt");
			}
			try {
				this.out.write("ITERATION\tavg. EXECUTED\tavg. WORST\tavg. AVG\tavg. BEST\n");
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			this.minIteration = event.getServices().getConfig().controler().getFirstIteration();
			int maxIter = event.getServices().getConfig().controler().getLastIteration();
			int iterations = maxIter - this.minIteration;
			if (iterations > 5000) iterations = 5000; // limit the history size
			this.history = new double[4][iterations+1];
		}

		@Override
		public void notifyIterationEnds(final IterationEndsEvent event) {
			double sumScoreWorst = 0.0;
			double sumScoreBest = 0.0;
			double sumAvgScores = 0.0;
			double sumExecutedScores = 0.0;
			int nofScoreWorst = 0;
			int nofScoreBest = 0;
			int nofAvgScores = 0;
			int nofExecutedScores = 0;
//			int nofExecutedIvPlans = 0;
//			int nofExecutedOevPlans = 0;

//			for (Person person : this.population.getPersons().values()) {
			for (Receiver receiver : receivers.getReceivers().values()){
				ReceiverPlan worstPlan = null;
				ReceiverPlan bestPlan = null;
				double worstScore = Double.POSITIVE_INFINITY;
				double bestScore = Double.NEGATIVE_INFINITY;
				double sumScores = 0.0;
				double cntScores = 0;
				
				/*
				 * FIXME This probably requires some re-doing. I (JWJ) changed
				 * the code so that each PLAN is scored... but I think it should 
				 * probable just be the SELECTED plan that is scored, right? 
				 * And also, it should just aggregate the plan's score from 
				 * the ReceiverOrder scores, right... that's what I've done 
				 * here for now. 
				 */
				for(ReceiverPlan plan : receiver.getPlans()) {
					Double score = plan.getScore();
					if (score == null) {
						continue;
					}
					
					/* Worst plan */
					if(worstPlan == null) {
						worstPlan = plan;
						worstScore = score;
					} else if(score < worstScore) {
						worstPlan = plan;
						worstScore = score;
					}
					
					/* Best plan */
					if(bestPlan == null) {
						bestPlan = plan;
						bestScore = score;
					} else if(score > bestScore) {
						bestPlan = plan;
						bestScore = score;
					}
					
					/* For calculating the average scores */
					sumScores += score;
					cntScores++;
					
					if (receiver.getSelectedPlan().equals(plan)) {
						sumExecutedScores += score;
						nofExecutedScores++;
					}
				}

				if (worstPlan != null) {
					nofScoreWorst++;
					sumScoreWorst += worstScore;
				}
				if (bestPlan != null) {
					nofScoreBest++;
					sumScoreBest += bestScore;
				}
				if (cntScores > 0) {
					sumAvgScores += (sumScores / cntScores);
					nofAvgScores++;
				}
			}
			log.info("-- avg. score of the executed plan of each agent: " + (sumExecutedScores / nofExecutedScores));
			log.info("-- avg. score of the worst plan of each agent: " + (sumScoreWorst / nofScoreWorst));
			log.info("-- avg. of the avg. plan score per agent: " + (sumAvgScores / nofAvgScores));
			log.info("-- avg. score of the best plan of each agent: " + (sumScoreBest / nofScoreBest));

			try {
				this.out.write(event.getIteration() + "\t" + (sumExecutedScores / nofExecutedScores) + "\t" +
						(sumScoreWorst / nofScoreWorst) + "\t" + (sumAvgScores / nofAvgScores) + "\t" + (sumScoreBest / nofScoreBest) + "\n");
				this.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (this.history != null) {
				int index = event.getIteration() - this.minIteration;
				this.history[INDEX_WORST][index] = (sumScoreWorst / nofScoreWorst);
				this.history[INDEX_BEST][index] = (sumScoreBest / nofScoreBest);
				this.history[INDEX_AVERAGE][index] = (sumAvgScores / nofAvgScores);
				this.history[INDEX_EXECUTED][index] = (sumExecutedScores / nofExecutedScores);

				if (this.createPNG && event.getIteration() != this.minIteration) {
					// create chart when data of more than one iteration is available.
					XYLineChart chart = new XYLineChart("Score Statistics", "iteration", "score");
					double[] iterations = new double[index + 1];
					for (int i = 0; i <= index; i++) {
						iterations[i] = i + this.minIteration;
					}
					double[] values = new double[index + 1];
					System.arraycopy(this.history[INDEX_WORST], 0, values, 0, index + 1);
					chart.addSeries("avg. worst score", iterations, values);
					System.arraycopy(this.history[INDEX_BEST], 0, values, 0, index + 1);
					chart.addSeries("avg. best score", iterations, values);
					System.arraycopy(this.history[INDEX_AVERAGE], 0, values, 0, index + 1);
					chart.addSeries("avg. of plans' average score", iterations, values);
					System.arraycopy(this.history[INDEX_EXECUTED], 0, values, 0, index + 1);
					chart.addSeries("avg. executed score", iterations, values);
					chart.addMatsimLogo();
					chart.saveAsPng(this.fileName + ".png", 800, 600);
				}
				if (index == (this.history[0].length - 1)) {
					// we cannot store more information, so disable the graph feature.
					this.history = null;
				}
			}
		}

		@Override
		public void notifyShutdown(final ShutdownEvent controlerShudownEvent) {
			try {
				this.out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}



