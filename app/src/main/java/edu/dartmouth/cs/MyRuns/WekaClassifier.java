// Generated with Weka 3.6.13
//
// This code is public domain and comes with no warranty.
//
// Timestamp: Sun Feb 14 16:09:24 EST 2016

package edu.dartmouth.cs.MyRuns;

class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.N3285b340(i);
        return p;
    }
    static double N3285b340(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() <= 32.716682) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() > 32.716682) {
            p = WekaClassifier.N4e7bb48a1(i);
        }
        return p;
    }
    static double N4e7bb48a1(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 300.304921) {
            p = WekaClassifier.N1b89f8472(i);
        } else if (((Double) i[0]).doubleValue() > 300.304921) {
            p = WekaClassifier.Nccdd3407(i);
        }
        return p;
    }
    static double N1b89f8472(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() <= 261.46112) {
            p = 1;
        } else if (((Double) i[0]).doubleValue() > 261.46112) {
            p = WekaClassifier.N7f0b0b703(i);
        }
        return p;
    }
    static double N7f0b0b703(Object []i) {
        double p = Double.NaN;
        if (i[4] == null) {
            p = 1;
        } else if (((Double) i[4]).doubleValue() <= 23.750363) {
            p = WekaClassifier.N2e1c27394(i);
        } else if (((Double) i[4]).doubleValue() > 23.750363) {
            p = 2;
        }
        return p;
    }
    static double N2e1c27394(Object []i) {
        double p = Double.NaN;
        if (i[23] == null) {
            p = 2;
        } else if (((Double) i[23]).doubleValue() <= 0.192517) {
            p = 2;
        } else if (((Double) i[23]).doubleValue() > 0.192517) {
            p = WekaClassifier.N3a5326d55(i);
        }
        return p;
    }
    static double N3a5326d55(Object []i) {
        double p = Double.NaN;
        if (i[17] == null) {
            p = 1;
        } else if (((Double) i[17]).doubleValue() <= 0.82564) {
            p = 1;
        } else if (((Double) i[17]).doubleValue() > 0.82564) {
            p = WekaClassifier.N291a87dd6(i);
        }
        return p;
    }
    static double N291a87dd6(Object []i) {
        double p = Double.NaN;
        if (i[3] == null) {
            p = 2;
        } else if (((Double) i[3]).doubleValue() <= 15.394144) {
            p = 2;
        } else if (((Double) i[3]).doubleValue() > 15.394144) {
            p = 1;
        }
        return p;
    }
    static double Nccdd3407(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 2;
        } else if (((Double) i[0]).doubleValue() <= 1274.874692) {
            p = WekaClassifier.N1ce815518(i);
        } else if (((Double) i[0]).doubleValue() > 1274.874692) {
            p = 3;
        }
        return p;
    }
    static double N1ce815518(Object []i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 2;
        } else if (((Double) i[64]).doubleValue() <= 9.629047) {
            p = WekaClassifier.N48cf87aa9(i);
        } else if (((Double) i[64]).doubleValue() > 9.629047) {
            p = 2;
        }
        return p;
    }
    static double N48cf87aa9(Object []i) {
        double p = Double.NaN;
        if (i[1] == null) {
            p = 1;
        } else if (((Double) i[1]).doubleValue() <= 22.674013) {
            p = 1;
        } else if (((Double) i[1]).doubleValue() > 22.674013) {
            p = 2;
        }
        return p;
    }
}
