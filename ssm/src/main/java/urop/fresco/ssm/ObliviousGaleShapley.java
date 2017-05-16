/*
 * CURRENT ISSUES:
 * Unable to index array with SInt value
 * sMatch and rMatch are not set properly (array lengths of 0)
 * Discarded suitors/reviewers do not seem to be updated properly
*/

package urop.fresco.ssm;

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

/**
 * A partially secure implementation of the Gale-Shapley stable matching algorithm with single-party computation
 * 
 * Notes:
 * Player 1 provides the suitor preferences, player 2 provides the reviewer preferences
 * The preference matrix is now 3D, with entry sPrefs[i][j][k] equaling 1 when suitor i prefers reviewer j over reviewer k, 0 otherwise.
 * 
 */
public class ObliviousGaleShapley implements Application {
	
	private static final long serialVersionUID = 474747474747474747L;
	
	private int myId;
	private int ns, nr;
	private int[][][] prefs;
	private SInt[][][] sPrefs, rPrefs;

	public OInt[] sMatch, rMatch;
	
	public ObliviousGaleShapley(int id, int ns, int nr, int[][] prefList) {
		this.myId = id;
		int len1 = (myId == 1) ? ns : nr;
		int len2 = (myId == 1) ? nr+1 : ns+1;
		this.prefs = new int[len1][len2][len2];
        for (int i = 0; i < ns; i++) {
            for (int j = 0; j < prefList.length; j++) {
        		int curr = prefList[i][j];
            	for (int k = 1; k <= nr; k++) {
            		if (prefs[i][k][curr] == 0) {
            			prefs[i][curr][k] = 1;
            		}
            	}
            }
        }
	}

	@Override
	public ProtocolProducer prepareApplication(ProtocolFactory factory) {
		BasicNumericFactory bnFac = (BasicNumericFactory)factory;
		NumericProtocolBuilder npb = new NumericProtocolBuilder(bnFac);
		NumericIOBuilder iob = new NumericIOBuilder(bnFac);
		int[] matches = new int[(myId == 1) ? ns : nr];

		// Input preferences, initialize matchings to 0
		iob.beginParScope();
			sPrefs = new SInt[ns][][];
			rPrefs = new SInt[nr][][];
			for (int i = 0; i < ns; i++) {
				sPrefs[i] = (myId == 1) ? iob.inputMatrix(prefs[i], 1) : iob.inputMatrix(nr + 1, nr + 1, 1);
			}
			for (int i = 0; i < nr; i++) {
				rPrefs[i] = (myId == 2) ? iob.inputMatrix(prefs[i], 2) : iob.inputMatrix(ns + 1, ns + 1, 2);
			}
			SInt[] s = (myId == 1) ? iob.inputArray(matches, 1) : iob.inputArray(ns, 1);
			SInt[] r = (myId == 2) ? iob.inputArray(matches, 2) : iob.inputArray(nr, 2);
//			OInt[] s = iob.outputArray((myId == 1) ? iob.inputArray(matches, 1) : iob.inputArray(ns, 1));
//			OInt[] r = iob.outputArray((myId == 2) ? iob.inputArray(matches, 2) : iob.inputArray(nr, 2));
		iob.endCurScope();

		for (int t = 0; t < sPrefs.length; t++) {
			for (int i = 0; i < sPrefs.length; i++) {
				for (int j = 0; j < rPrefs.length; j++) {
					// TODO: make protocol that sets c to 1 if sPrefs[i][j + 1] > sPrefs[i][s[i]] && rPrefs[j][i + 1] > rPrefs[j][r[j]]
					npb.beginSeqScope();
					// TODO: How do I index by a SInt?
					SInt sSwap = (myId == 1) ? sPrefs[i][j + 1][iob.output(s[i]).getValue().intValue()] : iob.input(1); // Issue 1: cannot index with SInt or open the SInt to all parties
					SInt rSwap = (myId == 2) ? rPrefs[j][i + 1][iob.output(r[j]).getValue().intValue()] : iob.input(2);
					SInt c = npb.mult(sSwap, rSwap);
					// TODO: fix issue of updating discarded suitors/reviewers that Sandeep mentioned
					s[i] = npb.add(npb.mult(c, npb.sub(iob.input(j + 1, myId), s[i])), s[i]);
					r[j] = npb.add(npb.mult(c, npb.sub(iob.input(i + 1, myId), r[j])), r[j]);
					npb.endCurScope();
				}
			}
		}
		
		iob.addProtocolProducer(npb.getProtocol());

		// Output result
		sMatch = iob.outputArray(s); // Issue 2: currently provides suitor and reviewer matches to both parties
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
		int ns = 3;
		int nr = 3;
		int[][] prefList = {{1, 2, 3}, {2, 3, 1}, {1, 3, 2}};
        
		ObliviousGaleShapley gs = new ObliviousGaleShapley(sceConf.getMyId(), ns, nr, prefList);
		SCE sce = SCEFactory.getSCEFromConfiguration(sceConf);
		try {
			sce.runApplication(gs);
		} catch (Exception e) {
			System.out.println("Error while doing SSM: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

		if (gs.sMatch == null) {
			System.out.println("big oops");
		} else {
			System.out.println(gs.sMatch.length);
		}
		// System.out.println("Suitor matches:");
		// for (int i = 0; i < ns; i++) {
		// 	System.out.print(gs.sMatch[i].getValue().intValue() + "\t");
		// }
		// System.out.println("\nReviewer matches:");
		// for (int i = 0; i < ns; i++) {
		// 	System.out.print(gs.rMatch[i].getValue().intValue() + "\t");
		// }
		System.out.println();
	}
}