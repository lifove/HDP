package hk.ust.cse.ipam.jc.crossprediction.util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class RserveTest {

	@Test
	public void test() {
		RConnection c;
		try {
			double[] a={1,2,3,4,5,6,7,8,9};
			double[] b={2,3,4,5,6,7,8,9,10};
			c = new RConnection();
			//REXP x = c.eval("R.version.string");
			//System.out.println(x.asString());
			
			c.assign("treated", a);
			c.assign("control", b);
			
			RList l = c.eval("ks.test(control,treated)").asList();
			System.out.println(l.at("p.value").asDouble());
			

		} catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			}
}
