package receiver;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import receiver.collaboration.ProportionalCostSharingTest;
import receiver.io.ReceiversReaderTest;
import receiver.io.ReceiversWriterTest;
import receiver.reorderPolicy.SSReorderPolicyTest;
import receiver.replanning.TimeWindowMutatorTest;
import receiver.usecases.chessboard.BaseReceiverChessboardScenarioTest;

/**
 * Add all the receiver tests in here so that there is a single class to run
 * and check the state of tests.
 *
 * @author jwjoubert
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ProportionalCostSharingTest.class,
	ReceiversReaderTest.class,
	ReceiversWriterTest.class,
	SSReorderPolicyTest.class,
	TimeWindowMutatorTest.class,
	BaseReceiverChessboardScenarioTest.class,
	ReceiverPlanTest.class
})

public class ReceiverTestSuite {
	
}
