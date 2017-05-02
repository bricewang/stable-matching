/*
 * CURRENT ISSUES:
 * Only the BGW protocol suite supports arithmetic operations natively, but cannot function in single-party computation
 * Integer comparison needs to be properly implemented
 * Discarded suitors/reviewers do not seem to be updated properly
*/

package dk.alexandra.fresco.demo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.configuration.CmdLineUtil;
import dk.alexandra.fresco.framework.sce.SCE;
import dk.alexandra.fresco.framework.sce.SCEFactory;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;

import java.math.BigInteger;

/**
 * A partially secure implementation of the Gale-Shapley stable matching algorithm with single-party computation
 */
public class ObliviousGaleShapley implements Application {
	
	private static final long serialVersionUID = 474747474747474747L;
	
	private int myId;
	private int[][] ps, pr;
	private SInt[][] sPrefs, rPrefs;

	public OInt[] sMatch, rMatch;
	
	public ObliviousGaleShapley(int id, int[][] ps, int[][] pr) {
		this.myId = id;
		this.ps = ps;
		this.pr = pr;
	}

	@Override
	public ProtocolProducer prepareApplication(ProtocolFactory factory) {
		BasicNumericFactory bnFac = (BasicNumericFactory)factory;
		NumericProtocolBuilder npb = new NumericProtocolBuilder(bnFac);
		NumericIOBuilder iob = new NumericIOBuilder(bnFac);

		// Input preferences, initialize matchings to 0
		iob.beginParScope();
			sPrefs = iob.inputMatrix(ps, myId);
			rPrefs = iob.inputMatrix(pr, myId);
			int[] emptyS = new int[ps.length];
			int[] emptyR = new int[pr.length];
			SInt[] s = iob.inputArray(emptyS, myId);
			SInt[] r = iob.inputArray(emptyR, myId);
		iob.endCurScope();

		for (int t = 0; t < sPrefs.length; t++) {
			for (int i = 0; i < sPrefs.length; i++) {
				for (int j = 0; j < rPrefs.length; j++) {
					// TODO: make protocol that sets c to 1 if sPrefs[i][j + 1] > sPrefs[i][s[i]] && rPrefs[j][i + 1] > rPrefs[j][r[j]]
					npb.beginSeqScope();
					SInt c = iob.input((iob.output(npb.sub(sPrefs[i][j + 1], sPrefs[i][iob.output(s[i]).getValue().intValue()])).getValue().compareTo(BigInteger.ZERO) > 0 ? 1 : 0) *
							(iob.output(npb.sub(rPrefs[j][i + 1], rPrefs[j][iob.output(r[j]).getValue().intValue()])).getValue().compareTo(BigInteger.ZERO) > 0 ? 1 : 0), myId);
					// TODO: fix issue of updating discarded suitors/reviewers that Sandeep mentioned
					s[i] = npb.add(npb.mult(c, npb.sub(iob.input(j + 1, myId), s[i])), s[i]);
					r[j] = npb.add(npb.mult(c, npb.sub(iob.input(i + 1, myId), r[j])), r[j]);
					npb.endCurScope();
				}
			}
		}
		
		iob.addProtocolProducer(npb.getProtocol());

		// Output result
		sMatch = iob.outputArray(s);
		rMatch = iob.outputArray(r);
		return iob.getProtocol();
	}
	
	public static void main(String[] args) {
		CmdLineUtil cmdUtil = new CmdLineUtil();
		SCEConfiguration sceConf = null;
		try {
			CommandLine cmd = cmdUtil.parse(args);
			sceConf = cmdUtil.getSCEConfiguration();
			
		} catch (IllegalArgumentException e) {
			System.out.println("Error: " + e);
			System.out.println();
			cmdUtil.displayHelp();
			System.exit(-1);	
		}
		int n = 3;
		int[][] sInput = {{1, 2, 3}, {2, 3, 1}, {1, 3, 2}};
        int[][] rInput = {{2, 3, 1}, {3, 1, 2}, {2, 1, 3}};
        int[][] ps = new int[n][n + 1];
        int[][] pr = new int[n][n + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < sInput.length; j++) {
                ps[i][sInput[i][j]] = n - j;
            }
            for (int j = 0; j < rInput.length; j++) {
                pr[i][rInput[i][j]] = n - j;
            }
        }
		ObliviousGaleShapley gs = new ObliviousGaleShapley(sceConf.getMyId(), ps, pr);
		SCE sce = SCEFactory.getSCEFromConfiguration(sceConf);
		try {
			sce.runApplication(gs);
		} catch (Exception e) {
			System.out.println("Error while doing SSM: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

		System.out.println("Suitor matches:");
		for (int i = 0; i < n; i++) {
			System.out.print(gs.sMatch[i].getValue().intValue() + "\t");
		}
		System.out.println("\nReviewer matches:");
		for (int i = 0; i < n; i++) {
			System.out.print(gs.rMatch[i].getValue().intValue() + "\t");
		}
		System.out.println();
	}
}