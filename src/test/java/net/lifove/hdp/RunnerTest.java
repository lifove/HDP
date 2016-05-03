package net.lifove.hdp;

import static org.junit.Assert.*;

import org.junit.Test;

public class RunnerTest {

	@Test
	public void testMain() {
		Runner runner = new Runner();
		runner.runner(null);
		
		assertEquals(runner.sourcePath,null);
		
		runner = new Runner();
		String[] args = {"-s", "test.arff"}; 
		runner.runner(args);
		assertEquals(runner.sourcePath,null); // because of missed options
		
		
		String[] args2 = {"-s", "source.arff", "-t", "target.arff", "-l", "class", "-p", "buggy"}; 
		runner.runner(args2);
		
		assertEquals(runner.sourcePath,args2[1]);
		assertEquals(runner.targetPath,args2[3]);
		assertEquals(runner.labelName,args2[5]);
		assertEquals(runner.posLabelValue,args2[7]);
		assertEquals(runner.cutoff,0.05,0);
		assertEquals(runner.help,false);
		assertEquals(runner.suppress,false);

	}

}
