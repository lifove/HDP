package net.lifove.hdp;

import static org.junit.Assert.*;

import org.junit.Test;

public class RunnerTest {

	@Test
	public void testMain() {
		Runner runner = new Runner();
		runner.runner(null);
		
		assertEquals(runner.sourcePath,null);
	}

}
